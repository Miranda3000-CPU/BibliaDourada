package com.example.bibliadourada.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/** Resumo do conteúdo de um banco — usado para validar importações. */
data class DatabaseSummary(
  val books: Int,
  val verses: Int,
  val comments: Int
) {
  val hasScripture: Boolean get() = verses > 0
  val hasAnnotations: Boolean get() = comments > 0
  val isEmpty: Boolean get() = books == 0 && verses == 0 && comments == 0
}

/**
 * Ponto único de acesso ao banco bíblico.
 *
 * O arquivo do banco **não é versionado** (ver .gitignore): ele carrega as
 * anotações de estudo de terceiros. Quando ele não existe, o app cria um
 * schema vazio e continua funcional, oferecendo a importação de dados.
 *
 * Ordem de semeadura em build local (opcional):
 *   1. /data/local/tmp/bible_v2.db  (adb push — atalho de desenvolvimento)
 *   2. assets/databases/bible_v2.db (build local com o banco)
 *   3. schema vazio
 */
class BibleDatabaseHelper private constructor(private val context: Context) {

  private var database: SQLiteDatabase? = null

  @Synchronized
  fun getDatabase(): SQLiteDatabase {
    database?.let { if (it.isOpen) return it }

    val dbFile = context.getDatabasePath(DB_NAME)
    val db = if (dbFile.exists() && dbFile.length() > 0L) {
      openWritable(dbFile)
    } else {
      seedOrCreateEmpty(dbFile)
    }
    database = db
    return db
  }

  private fun openWritable(dbFile: File): SQLiteDatabase =
    SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READWRITE)

  private fun seedOrCreateEmpty(dbFile: File): SQLiteDatabase {
    dbFile.parentFile?.mkdirs()
    if (trySeedFromLocalSources(dbFile)) {
      Log.d(TAG, "Banco semeado a partir de uma fonte local.")
      return openWritable(dbFile)
    }
    Log.d(TAG, "Nenhum banco encontrado — criando schema vazio.")
    val db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
    EMPTY_SCHEMA.forEach(db::execSQL)
    return db
  }

  private fun trySeedFromLocalSources(dbFile: File): Boolean {
    val staged = File("/data/local/tmp/$DB_NAME")
    if (staged.exists() && staged.canRead() && staged.length() > 0L) {
      val ok = runCatching { copyStream(staged.inputStream(), dbFile) }.isSuccess
      if (ok) return true
    }
    return runCatching {
      context.assets.open("databases/$DB_NAME").use { copyStream(it, dbFile) }
      true
    }.getOrDefault(false)
  }

  // ------------------------------------------------------------------ leitura

  /** Resumo do banco atualmente aberto. Nunca lança. */
  fun summarize(): DatabaseSummary {
    val db = getDatabase()
    return DatabaseSummary(
      books = countRows(db, "book"),
      verses = countRows(db, "verse"),
      comments = countRows(db, "comment")
    )
  }

  private fun countRows(db: SQLiteDatabase, table: String): Int =
    runCatching {
      db.rawQuery("SELECT COUNT(*) FROM $table", null).use { cursor ->
        if (cursor.moveToFirst()) cursor.getInt(0) else 0
      }
    }.getOrDefault(0)

  private fun tableExists(db: SQLiteDatabase, table: String): Boolean =
    runCatching {
      db.rawQuery(
        "SELECT 1 FROM sqlite_master WHERE type IN ('table','view') AND name = ? LIMIT 1",
        arrayOf(table)
      ).use { it.moveToFirst() }
    }.getOrDefault(false)

  // ------------------------------------------------------------- importação

  /**
   * Substitui o banco atual por um arquivo .db escolhido pelo usuário.
   * Lança [IOException] quando o arquivo não é um banco utilizável.
   */
  @Synchronized
  fun importDatabase(input: InputStream): DatabaseSummary {
    val dbFile = context.getDatabasePath(DB_NAME)
    dbFile.parentFile?.mkdirs()

    val staging = File(dbFile.parentFile, "$DB_NAME.import")
    copyStream(input, staging)

    val summary = inspect(staging)
      ?: throw IOException("O arquivo não é um banco de dados reconhecido.")
    if (!summary.hasScripture) {
      staging.delete()
      throw IOException("O banco importado não contém versículos.")
    }

    close()
    if (!dbFile.delete() && dbFile.exists()) {
      staging.delete()
      throw IOException("Não foi possível substituir o banco atual.")
    }
    if (!staging.renameTo(dbFile)) {
      staging.copyTo(dbFile, overwrite = true)
      staging.delete()
    }

    openWritable(dbFile)
    Log.d(TAG, "Banco importado: $summary")
    return summary
  }

  /** Lê o resumo de um arquivo .db sem alterar o banco em uso. */
  private fun inspect(file: File): DatabaseSummary? {
    var db: SQLiteDatabase? = null
    return try {
      db = SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
      if (!tableExists(db, "verse")) {
        null
      } else {
        DatabaseSummary(
          books = countRows(db, "book"),
          verses = countRows(db, "verse"),
          comments = countRows(db, "comment")
        )
      }
    } catch (e: Exception) {
      Log.w(TAG, "Falha ao inspecionar arquivo importado: ${e.message}")
      null
    } finally {
      runCatching { db?.close() }
    }
  }

  // ------------------------------------------------------------------- infra

  private fun copyStream(input: InputStream, destFile: File) {
    val tempFile = File(destFile.parentFile, "${destFile.name}.tmp")
    try {
      input.use { source ->
        FileOutputStream(tempFile).use { output ->
          val buffer = ByteArray(1024 * 256)
          var read: Int
          while (source.read(buffer).also { read = it } != -1) {
            output.write(buffer, 0, read)
          }
          output.flush()
        }
      }
      if (destFile.exists()) destFile.delete()
      if (!tempFile.renameTo(destFile)) {
        tempFile.copyTo(destFile, overwrite = true)
        tempFile.delete()
      }
    } catch (e: Exception) {
      tempFile.delete()
      throw IOException("Falha ao gravar o banco: ${e.message}", e)
    }
  }

  @Synchronized
  fun close() {
    runCatching { database?.takeIf { it.isOpen }?.close() }
    database = null
  }

  companion object {
    private const val TAG = "BibleDatabaseHelper"
    const val DB_NAME = "bible_v2.db"

    /**
     * Schema mínimo para o app rodar vazio. Espelha as tabelas do banco
     * original (ver docs/BANCO_DE_DADOS.md).
     */
    private val EMPTY_SCHEMA = listOf(
      """
      CREATE TABLE IF NOT EXISTS book (
        id INTEGER NOT NULL, testament INTEGER, number INTEGER, name TEXT,
        abbreviation TEXT, "language" TEXT, PRIMARY KEY (id AUTOINCREMENT)
      )
      """.trimIndent(),
      """
      CREATE TABLE IF NOT EXISTS chapter (
        id INTEGER NOT NULL, number INTEGER, text TEXT, book_id INTEGER,
        language TEXT, PRIMARY KEY (id AUTOINCREMENT)
      )
      """.trimIndent(),
      """
      CREATE TABLE IF NOT EXISTS verse (
        id INTEGER NOT NULL, text TEXT, number INTEGER, chapter_id INTEGER,
        language TEXT, PRIMARY KEY (id AUTOINCREMENT)
      )
      """.trimIndent(),
      """
      CREATE TABLE IF NOT EXISTS comment (
        id INTEGER NOT NULL, text TEXT, chapter_id INTEGER,
        start_verse_id INTEGER, end_verse_id INTEGER, language TEXT,
        PRIMARY KEY (id AUTOINCREMENT)
      )
      """.trimIndent(),
      """
      CREATE TABLE IF NOT EXISTS bible_daily_reading (
        id INTEGER NOT NULL, date TEXT NOT NULL, chapter_id INTEGER NOT NULL,
        language TEXT, PRIMARY KEY (id AUTOINCREMENT)
      )
      """.trimIndent(),
      """
      CREATE TABLE IF NOT EXISTS highlight_color (
        id INTEGER NOT NULL, name TEXT, hexadecimal TEXT, usage_amount INTEGER,
        language TEXT, PRIMARY KEY (id AUTOINCREMENT)
      )
      """.trimIndent(),
      """
      CREATE VIRTUAL TABLE IF NOT EXISTS bible_search USING fts3(
        rowid INTEGER, verse_id INTEGER, verse_number INTEGER, verse_text TEXT,
        verse_text_raw TEXT, chapter_id INTEGER, chapter_number INTEGER,
        book_id INTEGER, book_name TEXT, testament INTEGER, language TEXT
      )
      """.trimIndent(),
      """
      CREATE VIRTUAL TABLE IF NOT EXISTS comment_search USING fts3(
        rowid INTEGER, comment_id INTEGER, comment_text TEXT,
        comment_text_raw TEXT, verse_id INTEGER, verse_number INTEGER,
        chapter_id INTEGER, chapter_number INTEGER, book_id INTEGER,
        book_name TEXT, testament INTEGER, language TEXT
      )
      """.trimIndent()
    )

    @Volatile
    private var INSTANCE: BibleDatabaseHelper? = null

    fun getInstance(context: Context): BibleDatabaseHelper =
      INSTANCE ?: synchronized(this) {
        INSTANCE ?: BibleDatabaseHelper(context.applicationContext).also { INSTANCE = it }
      }
  }
}
