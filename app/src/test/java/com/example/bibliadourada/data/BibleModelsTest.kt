package com.example.bibliadourada.data

import com.example.bibliadourada.data.model.Book
import com.example.bibliadourada.data.model.SearchResultType
import com.example.bibliadourada.data.model.Verse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BibleModelsTest {

  @Test
  fun book_testament_checksCorrectly() {
    val oldBook = Book(id = 1, testament = 0, number = 1, name = "Gênesis", abbreviation = "Gen", totalChapters = 50)
    val newBook = Book(id = 40, testament = 1, number = 1, name = "Mateus", abbreviation = "Mat", totalChapters = 28)

    assertTrue(oldBook.isOldTestament)
    assertFalse(newBook.isOldTestament)
  }

  @Test
  fun verse_properties_assignedCorrectly() {
    val verse = Verse(id = 10, chapterId = 1, number = 5, text = "E disse Deus...", hasComment = true)

    assertEquals(10, verse.id)
    assertEquals(1, verse.chapterId)
    assertEquals(5, verse.number)
    assertTrue(verse.hasComment)
  }

  @Test
  fun dateString_formatMatchesExpected() {
    val dateStr = getTodayDateString()
    assertTrue(dateStr.matches(Regex("\\d{2}/\\d{2}")))
  }

  @Test
  fun searchFilter_allEnumValuesAvailable() {
    val types = SearchResultType.values()
    assertTrue(types.contains(SearchResultType.ALL))
    assertTrue(types.contains(SearchResultType.VERSE))
    assertTrue(types.contains(SearchResultType.COMMENT))
  }
}
