package com.example.bibliadourada.data.model

data class Book(
  val id: Int,
  val testament: Int, // 0 = Antigo Testamento, 1 = Novo Testamento
  val number: Int,
  val name: String,
  val abbreviation: String,
  val totalChapters: Int = 0
) {
  val isOldTestament: Boolean get() = testament == 0
}

data class Chapter(
  val id: Int,
  val number: Int,
  val bookId: Int,
  val bookName: String = ""
)

data class Verse(
  val id: Int,
  val chapterId: Int,
  val number: Int,
  val text: String,
  val hasComment: Boolean = false
)

data class Comment(
  val id: Int,
  val text: String,
  val chapterId: Int,
  val startVerseId: Int,
  val endVerseId: Int
)

data class DailyReading(
  val id: Int,
  val date: String,
  val chapterId: Int,
  val chapterNumber: Int,
  val bookId: Int,
  val bookName: String,
  val abbreviation: String,
  val testament: Int
)

enum class SearchResultType {
  ALL,
  VERSE,
  COMMENT
}

data class SearchResult(
  val id: Long,
  val type: SearchResultType,
  val bookId: Int,
  val bookName: String,
  val chapterId: Int,
  val chapterNumber: Int,
  val verseNumber: Int,
  val text: String
)
