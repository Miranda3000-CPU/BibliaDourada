package com.example.bibliadourada.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.bibliadourada.data.model.Book
import com.example.bibliadourada.data.model.Chapter
import com.example.bibliadourada.data.model.Comment
import com.example.bibliadourada.data.model.DailyReading
import com.example.bibliadourada.data.model.SearchResult
import com.example.bibliadourada.data.model.SearchResultType
import com.example.bibliadourada.data.model.Verse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.text.Normalizer
import java.util.Calendar

interface BibleRepository {
  suspend fun getBooks(): List<Book>
  suspend fun getChaptersForBook(bookId: Int): List<Chapter>
  suspend fun getChapter(chapterId: Int): Chapter?
  suspend fun getChapterByBookAndNumber(bookId: Int, chapterNumber: Int): Chapter?
  suspend fun getVersesForChapter(chapterId: Int): List<Verse>
  suspend fun getCommentsForVerse(verseId: Int): List<Comment>
  suspend fun getDailyReadings(date: String = getTodayDateString()): List<DailyReading>
  suspend fun search(query: String, filter: SearchResultType = SearchResultType.ALL): List<SearchResult>
  suspend fun getAdjacentChapterId(currentChapterId: Int, isNext: Boolean): Int?

  /** Resumo do conteúdo disponível (versículos e anotações). */
  suspend fun getDatabaseSummary(): DatabaseSummary

  /** Substitui o banco atual por um arquivo .db escolhido pelo usuário. */
  suspend fun importDatabase(input: InputStream): DatabaseSummary

  /** Mescla anotações de um JSON no banco atual. */
  suspend fun importAnnotations(json: String): AnnotationImportResult
}

fun getTodayDateString(): String {
  val calendar = Calendar.getInstance()
  val day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
  val month = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
  return "$day/$month"
}

class BibleRepositoryImpl(private val context: Context) : BibleRepository {

  private val helper = BibleDatabaseHelper.getInstance(context)

  private fun getDatabase(): SQLiteDatabase = helper.getDatabase()

  override suspend fun getDatabaseSummary(): DatabaseSummary = withContext(Dispatchers.IO) {
    helper.summarize()
  }

  override suspend fun importDatabase(input: InputStream): DatabaseSummary = withContext(Dispatchers.IO) {
    helper.importDatabase(input)
  }

  override suspend fun importAnnotations(json: String): AnnotationImportResult = withContext(Dispatchers.IO) {
    AnnotationImporter.import(getDatabase(), json)
  }

  override suspend fun getBooks(): List<Book> = withContext(Dispatchers.IO) {
    val db = getDatabase()
    val list = mutableListOf<Book>()
    val query = """
      SELECT b.id, b.testament, b.number, b.name, b.abbreviation, COUNT(c.id) AS total_chapters
      FROM book b
      LEFT JOIN chapter c ON c.book_id = b.id AND c.language = 'pt_BR'
      WHERE b.language = 'pt_BR'
      GROUP BY b.id
      ORDER BY b.testament, b.number
    """.trimIndent()

    db.rawQuery(query, null).use { cursor ->
      val idCol = cursor.getColumnIndexOrThrow("id")
      val testCol = cursor.getColumnIndexOrThrow("testament")
      val numCol = cursor.getColumnIndexOrThrow("number")
      val nameCol = cursor.getColumnIndexOrThrow("name")
      val abbrCol = cursor.getColumnIndexOrThrow("abbreviation")
      val totalCol = cursor.getColumnIndexOrThrow("total_chapters")

      while (cursor.moveToNext()) {
        list.add(
          Book(
            id = cursor.getInt(idCol),
            testament = cursor.getInt(testCol),
            number = cursor.getInt(numCol),
            name = cursor.getString(nameCol),
            abbreviation = cursor.getString(abbrCol),
            totalChapters = cursor.getInt(totalCol)
          )
        )
      }
    }
    list
  }

  override suspend fun getChaptersForBook(bookId: Int): List<Chapter> = withContext(Dispatchers.IO) {
    val db = getDatabase()
    val list = mutableListOf<Chapter>()
    val query = """
      SELECT c.id, c.number, c.book_id, b.name AS book_name
      FROM chapter c
      JOIN book b ON c.book_id = b.id
      WHERE c.book_id = ? AND c.language = 'pt_BR'
      ORDER BY c.number
    """.trimIndent()

    db.rawQuery(query, arrayOf(bookId.toString())).use { cursor ->
      val idCol = cursor.getColumnIndexOrThrow("id")
      val numCol = cursor.getColumnIndexOrThrow("number")
      val bookIdCol = cursor.getColumnIndexOrThrow("book_id")
      val bookNameCol = cursor.getColumnIndexOrThrow("book_name")

      while (cursor.moveToNext()) {
        list.add(
          Chapter(
            id = cursor.getInt(idCol),
            number = cursor.getInt(numCol),
            bookId = cursor.getInt(bookIdCol),
            bookName = cursor.getString(bookNameCol)
          )
        )
      }
    }
    list
  }

  override suspend fun getChapter(chapterId: Int): Chapter? = withContext(Dispatchers.IO) {
    val db = getDatabase()
    val query = """
      SELECT c.id, c.number, c.book_id, b.name AS book_name
      FROM chapter c
      JOIN book b ON c.book_id = b.id
      WHERE c.id = ? AND c.language = 'pt_BR'
      LIMIT 1
    """.trimIndent()

    db.rawQuery(query, arrayOf(chapterId.toString())).use { cursor ->
      if (cursor.moveToFirst()) {
        Chapter(
          id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
          number = cursor.getInt(cursor.getColumnIndexOrThrow("number")),
          bookId = cursor.getInt(cursor.getColumnIndexOrThrow("book_id")),
          bookName = cursor.getString(cursor.getColumnIndexOrThrow("book_name"))
        )
      } else null
    }
  }

  override suspend fun getChapterByBookAndNumber(bookId: Int, chapterNumber: Int): Chapter? = withContext(Dispatchers.IO) {
    val db = getDatabase()
    val query = """
      SELECT c.id, c.number, c.book_id, b.name AS book_name
      FROM chapter c
      JOIN book b ON c.book_id = b.id
      WHERE c.book_id = ? AND c.number = ? AND c.language = 'pt_BR'
      LIMIT 1
    """.trimIndent()

    db.rawQuery(query, arrayOf(bookId.toString(), chapterNumber.toString())).use { cursor ->
      if (cursor.moveToFirst()) {
        Chapter(
          id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
          number = cursor.getInt(cursor.getColumnIndexOrThrow("number")),
          bookId = cursor.getInt(cursor.getColumnIndexOrThrow("book_id")),
          bookName = cursor.getString(cursor.getColumnIndexOrThrow("book_name"))
        )
      } else null
    }
  }

  override suspend fun getVersesForChapter(chapterId: Int): List<Verse> = withContext(Dispatchers.IO) {
    val db = getDatabase()
    val list = mutableListOf<Verse>()
    val query = """
      SELECT v.id, v.chapter_id, v.number, v.text,
        EXISTS(
          SELECT 1 FROM comment c
          WHERE c.language = 'pt_BR'
            AND (
              c.start_verse_id = v.id
              OR (c.start_verse_id > 0 AND c.end_verse_id > 0 AND v.id BETWEEN c.start_verse_id AND c.end_verse_id)
            )
        ) AS has_comment
      FROM verse v
      WHERE v.chapter_id = ? AND v.language = 'pt_BR'
      ORDER BY v.number
    """.trimIndent()

    db.rawQuery(query, arrayOf(chapterId.toString())).use { cursor ->
      val idCol = cursor.getColumnIndexOrThrow("id")
      val chapCol = cursor.getColumnIndexOrThrow("chapter_id")
      val numCol = cursor.getColumnIndexOrThrow("number")
      val textCol = cursor.getColumnIndexOrThrow("text")
      val commCol = cursor.getColumnIndexOrThrow("has_comment")

      while (cursor.moveToNext()) {
        list.add(
          Verse(
            id = cursor.getInt(idCol),
            chapterId = cursor.getInt(chapCol),
            number = cursor.getInt(numCol),
            text = cursor.getString(textCol),
            hasComment = cursor.getInt(commCol) == 1
          )
        )
      }
    }
    list
  }

  override suspend fun getCommentsForVerse(verseId: Int): List<Comment> = withContext(Dispatchers.IO) {
    val db = getDatabase()
    val list = mutableListOf<Comment>()
    // Somente o estudo correlacionado ao versículo clicado:
    //  - comentário que começa exatamente nesse versículo, ou
    //  - comentário cujo intervalo (start..end) contém esse versículo.
    // Comentários de capítulo/livro inteiro (start_verse_id = 0) NÃO entram aqui,
    // pois se aplicariam a todos os versículos em vez do versículo específico.
    val query = """
      SELECT c.id, c.text, c.chapter_id, c.start_verse_id, c.end_verse_id
      FROM comment c
      WHERE c.language = 'pt_BR'
        AND (
          c.start_verse_id = ?
          OR (c.start_verse_id > 0 AND c.end_verse_id > 0 AND ? BETWEEN c.start_verse_id AND c.end_verse_id)
        )
      ORDER BY c.start_verse_id ASC, c.id ASC
    """.trimIndent()

    db.rawQuery(query, arrayOf(verseId.toString(), verseId.toString())).use { cursor ->
      val idCol = cursor.getColumnIndexOrThrow("id")
      val textCol = cursor.getColumnIndexOrThrow("text")
      val chapCol = cursor.getColumnIndexOrThrow("chapter_id")
      val startCol = cursor.getColumnIndexOrThrow("start_verse_id")
      val endCol = cursor.getColumnIndexOrThrow("end_verse_id")

      while (cursor.moveToNext()) {
        list.add(
          Comment(
            id = cursor.getInt(idCol),
            text = cursor.getString(textCol),
            chapterId = cursor.getInt(chapCol),
            startVerseId = cursor.getInt(startCol),
            endVerseId = cursor.getInt(endCol)
          )
        )
      }
    }
    list
  }

  override suspend fun getDailyReadings(date: String): List<DailyReading> = withContext(Dispatchers.IO) {
    val db = getDatabase()
    val list = mutableListOf<DailyReading>()
    val query = """
      SELECT r.id, r.date, r.chapter_id, c.number AS chapter_number, b.id AS book_id, b.name AS book_name, b.abbreviation, b.testament
      FROM bible_daily_reading r
      JOIN chapter c ON r.chapter_id = c.id
      JOIN book b ON c.book_id = b.id
      WHERE r.date = ? AND r.language = 'pt_BR'
      ORDER BY r.id
    """.trimIndent()

    db.rawQuery(query, arrayOf(date)).use { cursor ->
      val idCol = cursor.getColumnIndexOrThrow("id")
      val dateCol = cursor.getColumnIndexOrThrow("date")
      val chapIdCol = cursor.getColumnIndexOrThrow("chapter_id")
      val chapNumCol = cursor.getColumnIndexOrThrow("chapter_number")
      val bookIdCol = cursor.getColumnIndexOrThrow("book_id")
      val bookNameCol = cursor.getColumnIndexOrThrow("book_name")
      val abbrCol = cursor.getColumnIndexOrThrow("abbreviation")
      val testCol = cursor.getColumnIndexOrThrow("testament")

      while (cursor.moveToNext()) {
        list.add(
          DailyReading(
            id = cursor.getInt(idCol),
            date = cursor.getString(dateCol),
            chapterId = cursor.getInt(chapIdCol),
            chapterNumber = cursor.getInt(chapNumCol),
            bookId = cursor.getInt(bookIdCol),
            bookName = cursor.getString(bookNameCol),
            abbreviation = cursor.getString(abbrCol),
            testament = cursor.getInt(testCol)
          )
        )
      }
    }
    list
  }

  override suspend fun search(query: String, filter: SearchResultType): List<SearchResult> = withContext(Dispatchers.IO) {
    val ftsQuery = normalizeForFts(query)
    if (ftsQuery.isBlank()) return@withContext emptyList()

    val db = getDatabase()
    val list = mutableListOf<SearchResult>()

    if (filter == SearchResultType.ALL || filter == SearchResultType.VERSE) {
      val verseSql = """
        SELECT verse_id, verse_number, chapter_id, chapter_number, book_id, book_name, verse_text
        FROM bible_search
        WHERE language = 'pt_BR' AND verse_text_raw MATCH ?
        LIMIT 40
      """.trimIndent()

      try {
        db.rawQuery(verseSql, arrayOf(ftsQuery)).use { cursor ->
          val verseIdCol = cursor.getColumnIndexOrThrow("verse_id")
          val bookIdCol = cursor.getColumnIndexOrThrow("book_id")
          val bookNameCol = cursor.getColumnIndexOrThrow("book_name")
          val chapIdCol = cursor.getColumnIndexOrThrow("chapter_id")
          val chapNumCol = cursor.getColumnIndexOrThrow("chapter_number")
          val verseNumCol = cursor.getColumnIndexOrThrow("verse_number")
          val textCol = cursor.getColumnIndexOrThrow("verse_text")

          while (cursor.moveToNext()) {
            list.add(
              SearchResult(
                id = cursor.getLong(verseIdCol),
                type = SearchResultType.VERSE,
                bookId = cursor.getInt(bookIdCol),
                bookName = cursor.getString(bookNameCol),
                chapterId = cursor.getInt(chapIdCol),
                chapterNumber = cursor.getInt(chapNumCol),
                verseNumber = cursor.getInt(verseNumCol),
                text = cursor.getString(textCol)
              )
            )
          }
        }
      } catch (_: Exception) { }
    }

    if (filter == SearchResultType.ALL || filter == SearchResultType.COMMENT) {
      // Para um resultado de comentário, `id` precisa ser o id do VERSÍCULO
      // (e não o rowid do comentário), pois é ele que a tela usa ao abrir o
      // estudo: getCommentsForVerse(verseId).
      val commentSql = """
        SELECT comment_id, verse_id, verse_number, chapter_id, chapter_number, book_id, book_name, comment_text
        FROM comment_search
        WHERE language = 'pt_BR' AND comment_text_raw MATCH ?
        LIMIT 40
      """.trimIndent()

      try {
        db.rawQuery(commentSql, arrayOf(ftsQuery)).use { cursor ->
          val verseIdCol = cursor.getColumnIndexOrThrow("verse_id")
          val bookIdCol = cursor.getColumnIndexOrThrow("book_id")
          val bookNameCol = cursor.getColumnIndexOrThrow("book_name")
          val chapIdCol = cursor.getColumnIndexOrThrow("chapter_id")
          val chapNumCol = cursor.getColumnIndexOrThrow("chapter_number")
          val verseNumCol = cursor.getColumnIndexOrThrow("verse_number")
          val textCol = cursor.getColumnIndexOrThrow("comment_text")

          while (cursor.moveToNext()) {
            list.add(
              SearchResult(
                id = cursor.getLong(verseIdCol),
                type = SearchResultType.COMMENT,
                bookId = cursor.getInt(bookIdCol),
                bookName = cursor.getString(bookNameCol),
                chapterId = cursor.getInt(chapIdCol),
                chapterNumber = cursor.getInt(chapNumCol),
                verseNumber = cursor.getInt(verseNumCol),
                text = cursor.getString(textCol)
              )
            )
          }
        }
      } catch (_: Exception) { }
    }

    list
  }

  override suspend fun getAdjacentChapterId(currentChapterId: Int, isNext: Boolean): Int? = withContext(Dispatchers.IO) {
    val targetId = if (isNext) currentChapterId + 1 else currentChapterId - 1
    if (targetId in 1..1189) targetId else null
  }

  private fun normalizeForFts(input: String): String {
    val temp = Normalizer.normalize(input, Normalizer.Form.NFD)
    val withoutAccents = Regex("\\p{InCombiningDiacriticalMarks}+").replace(temp, "")
    val clean = withoutAccents.uppercase().replace(Regex("[^A-Z0-9 ]+"), " ").trim()
    return clean.split("\\s+".toRegex()).filter { it.isNotBlank() }.joinToString(" ") { "$it*" }
  }
}
