package com.example.bibliadourada.data

import android.database.sqlite.SQLiteDatabase
import org.json.JSONException
import org.json.JSONObject
import java.text.Normalizer

data class AnnotationImportResult(val imported: Int, val skipped: Int)

/**
 * Importa anotações de estudo a partir de um JSON, mesclando-as no banco atual.
 *
 * Formato aceito (ver docs/BANCO_DE_DADOS.md):
 * ```json
 * {
 *   "format": "biblia-dourada/anotacoes",
 *   "version": 1,
 *   "language": "pt_BR",
 *   "annotations": [
 *     { "book": "Lev", "chapter": 1, "verse": 2, "endVerse": 2, "text": "..." },
 *     { "book": "Gênesis", "chapter": 1, "verse": 0, "text": "..." }
 *   ]
 * }
 * ```
 * `verse: 0` representa um estudo do capítulo inteiro. `book` aceita a
 * abreviação, o nome ou o id numérico do livro.
 */
object AnnotationImporter {

  private const val DEFAULT_LANGUAGE = "pt_BR"

  fun import(db: SQLiteDatabase, json: String): AnnotationImportResult {
    val root = try {
      JSONObject(json)
    } catch (e: JSONException) {
      // Não ecoa o conteúdo do arquivo na mensagem de erro.
      throw IllegalArgumentException("O arquivo não é um JSON de anotações válido.")
    }
    val annotations = root.optJSONArray("annotations")
      ?: root.optJSONArray("comments")
      ?: throw IllegalArgumentException("O JSON não contém o array \"annotations\".")
    val language = root.optString("language", DEFAULT_LANGUAGE)
      .ifBlank { DEFAULT_LANGUAGE }

    var imported = 0
    var skipped = 0
    db.beginTransaction()
    try {
      for (index in 0 until annotations.length()) {
        val item = annotations.optJSONObject(index)
        val ok = item != null && insert(db, item, language)
        if (ok) imported++ else skipped++
      }
      db.setTransactionSuccessful()
    } finally {
      db.endTransaction()
    }
    return AnnotationImportResult(imported, skipped)
  }

  private fun insert(db: SQLiteDatabase, item: JSONObject, language: String): Boolean {
    val text = item.optString("text").trim()
    if (text.isEmpty()) return false

    val chapterNumber = item.optInt("chapter", 0)
    if (chapterNumber <= 0) return false

    val book = resolveBook(db, item.opt("book"), language) ?: return false
    val chapter = resolveChapter(db, book.id, chapterNumber, language) ?: return false

    val verseNumber = item.optInt("verse", 0)
    val endVerseNumber = item.optInt("endVerse", 0)

    val startVerseId = if (verseNumber > 0) {
      resolveVerseId(db, chapter.id, verseNumber, language) ?: return false
    } else {
      0
    }
    val endVerseId = if (endVerseNumber > 0) {
      resolveVerseId(db, chapter.id, endVerseNumber, language) ?: return false
    } else {
      0
    }

    val commentId = db.insert(
      "comment", null,
      android.content.ContentValues().apply {
        put("text", text)
        put("chapter_id", chapter.id)
        put("start_verse_id", startVerseId)
        put("end_verse_id", endVerseId)
        put("language", language)
      }
    )
    if (commentId <= 0) return false

    indexComment(db, commentId, text, startVerseId, verseNumber, chapter, book, language)
    return true
  }

  /** Mantém a tabela FTS de busca de comentários coerente com o que foi importado. */
  private fun indexComment(
    db: SQLiteDatabase,
    commentId: Long,
    text: String,
    verseId: Int,
    verseNumber: Int,
    chapter: ChapterRef,
    book: BookRef,
    language: String
  ) {
    runCatching {
      db.insert(
        "comment_search", null,
        android.content.ContentValues().apply {
          put("comment_id", commentId)
          put("comment_text", text)
          put("comment_text_raw", normalizeForIndex(text))
          put("verse_id", verseId)
          put("verse_number", verseNumber)
          put("chapter_id", chapter.id)
          put("chapter_number", chapter.number)
          put("book_id", book.id)
          put("book_name", book.name)
          put("testament", book.testament)
          put("language", language)
        }
      )
    }
  }

  // ------------------------------------------------------------- resoluções

  private data class BookRef(val id: Int, val name: String, val testament: Int)
  private data class ChapterRef(val id: Int, val number: Int)

  private fun resolveBook(db: SQLiteDatabase, reference: Any?, language: String): BookRef? {
    if (reference == null || reference == JSONObject.NULL) return null
    val selection: String
    val args: Array<String>
    if (reference is Number) {
      selection = "id = ?"
      args = arrayOf(reference.toString())
    } else {
      val value = reference.toString().trim()
      if (value.isEmpty()) return null
      selection = "language = ? AND (abbreviation = ? COLLATE NOCASE OR name = ? COLLATE NOCASE)"
      args = arrayOf(language, value, value)
    }
    return db.rawQuery(
      "SELECT id, name, testament FROM book WHERE $selection LIMIT 1", args
    ).use { cursor ->
      if (cursor.moveToFirst()) {
        BookRef(cursor.getInt(0), cursor.getString(1), cursor.getInt(2))
      } else null
    }
  }

  private fun resolveChapter(
    db: SQLiteDatabase,
    bookId: Int,
    chapterNumber: Int,
    language: String
  ): ChapterRef? = db.rawQuery(
    "SELECT id, number FROM chapter WHERE book_id = ? AND number = ? AND language = ? LIMIT 1",
    arrayOf(bookId.toString(), chapterNumber.toString(), language)
  ).use { cursor ->
    if (cursor.moveToFirst()) ChapterRef(cursor.getInt(0), cursor.getInt(1)) else null
  }

  private fun resolveVerseId(
    db: SQLiteDatabase,
    chapterId: Int,
    verseNumber: Int,
    language: String
  ): Int? = db.rawQuery(
    "SELECT id FROM verse WHERE chapter_id = ? AND number = ? AND language = ? LIMIT 1",
    arrayOf(chapterId.toString(), verseNumber.toString(), language)
  ).use { cursor ->
    if (cursor.moveToFirst()) cursor.getInt(0) else null
  }

  // ----------------------------------------------------------- normalização

  /**
   * Normaliza o texto para o índice FTS, no mesmo formato do banco original:
   * sem acentos, maiúsculo e sem pontuação (ex.: "1.2a: Fala" -> "12A FALA").
   */
  private fun normalizeForIndex(input: String): String {
    val decomposed = Normalizer.normalize(input, Normalizer.Form.NFD)
    val withoutAccents = Regex("\\p{InCombiningDiacriticalMarks}+").replace(decomposed, "")
    return withoutAccents
      .uppercase()
      .replace(Regex("[^A-Z0-9\\s]+"), "")
      .replace(Regex("\\s+"), " ")
      .trim()
  }
}
