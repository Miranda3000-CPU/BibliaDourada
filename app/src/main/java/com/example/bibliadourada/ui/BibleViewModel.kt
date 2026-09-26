package com.example.bibliadourada.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliadourada.data.AnnotationImportResult
import com.example.bibliadourada.data.BibleRepository
import com.example.bibliadourada.data.BibleRepositoryImpl
import com.example.bibliadourada.data.DatabaseSummary
import com.example.bibliadourada.data.model.Book
import com.example.bibliadourada.data.model.Chapter
import com.example.bibliadourada.data.model.Comment
import com.example.bibliadourada.data.model.DailyReading
import com.example.bibliadourada.data.model.SearchResult
import com.example.bibliadourada.data.model.SearchResultType
import com.example.bibliadourada.data.model.Verse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

data class ReaderUiState(
  val currentChapter: Chapter? = null,
  val verses: List<Verse> = emptyList(),
  val isLoading: Boolean = false,
  val hasPrevious: Boolean = false,
  val hasNext: Boolean = false,
  val fontSizeSp: Float = 18f,
  val targetVerseNumber: Int? = null
)

data class HomeUiState(
  val dailyReadings: List<DailyReading> = emptyList(),
  val lastReadBookName: String = "Gênesis",
  val lastReadChapterNumber: Int = 1,
  val lastReadChapterId: Int = 1,
  val isLoadingDaily: Boolean = false
)

data class SearchUiState(
  val query: String = "",
  val filter: SearchResultType = SearchResultType.ALL,
  val results: List<SearchResult> = emptyList(),
  val isSearching: Boolean = false
)

/** Resultado da última importação, exibido na ferramenta de importação. */
data class ImportFeedback(val message: String, val isError: Boolean)

class BibleViewModel(
  application: Application,
  private val repository: BibleRepository
) : AndroidViewModel(application) {

  // Construtor exigido por AndroidViewModelFactory (criação via reflexão no viewModel())
  constructor(application: Application) : this(application, BibleRepositoryImpl(application))

  private val prefs = application.getSharedPreferences("biblia_dourada_prefs", Context.MODE_PRIVATE)

  private val _books = MutableStateFlow<List<Book>>(emptyList())
  val books: StateFlow<List<Book>> = _books.asStateFlow()

  private val _readerState = MutableStateFlow(
    ReaderUiState(
      fontSizeSp = prefs.getFloat("font_size_sp", 18f)
    )
  )
  val readerState: StateFlow<ReaderUiState> = _readerState.asStateFlow()

  private val _homeState = MutableStateFlow(
    HomeUiState(
      lastReadBookName = prefs.getString("last_book_name", "Gênesis") ?: "Gênesis",
      lastReadChapterNumber = prefs.getInt("last_chapter_num", 1),
      lastReadChapterId = prefs.getInt("last_chapter_id", 1)
    )
  )
  val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

  private val _searchState = MutableStateFlow(SearchUiState())
  val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

  // Comentário selecionado para o BottomSheet
  private val _selectedVerseForComment = MutableStateFlow<Verse?>(null)
  val selectedVerseForComment: StateFlow<Verse?> = _selectedVerseForComment.asStateFlow()

  private val _verseComments = MutableStateFlow<List<Comment>>(emptyList())
  val verseComments: StateFlow<List<Comment>> = _verseComments.asStateFlow()

  // Controle do Dialog de Seleção de Livro/Capítulo
  private val _isBookChapterDialogOpen = MutableStateFlow(false)
  val isBookChapterDialogOpen: StateFlow<Boolean> = _isBookChapterDialogOpen.asStateFlow()

  // Tema: false = claro ("white", padrão), true = escuro
  private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("dark_theme", false))
  val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

  // Tour de boas-vindas: aparece só na primeira abertura
  private val _showOnboarding = MutableStateFlow(!prefs.getBoolean("onboarding_done", false))
  val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()

  // Estado dos dados importados (versículos/anotações)
  private val _dataSummary = MutableStateFlow(DatabaseSummary(0, 0, 0))
  val dataSummary: StateFlow<DatabaseSummary> = _dataSummary.asStateFlow()

  // Ferramenta de importação
  private val _isImportSheetOpen = MutableStateFlow(false)
  val isImportSheetOpen: StateFlow<Boolean> = _isImportSheetOpen.asStateFlow()

  private val _isImporting = MutableStateFlow(false)
  val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

  private val _importFeedback = MutableStateFlow<ImportFeedback?>(null)
  val importFeedback: StateFlow<ImportFeedback?> = _importFeedback.asStateFlow()

  init {
    refreshAll()
  }

  /** (Re)carrega o resumo do banco e todos os conteúdos exibidos. */
  fun refreshAll() {
    viewModelScope.launch {
      _dataSummary.value = repository.getDatabaseSummary()
      loadBooks()
      loadDailyReadings()
      loadChapter(_homeState.value.lastReadChapterId)
    }
  }

  fun openImportSheet() {
    _isImportSheetOpen.value = true
  }

  fun dismissImportSheet() {
    _isImportSheetOpen.value = false
  }

  fun clearImportFeedback() {
    _importFeedback.value = null
  }

  /** Importa um banco .db completo, substituindo o atual. */
  fun importDatabase(uri: Uri) {
    viewModelScope.launch {
      _isImporting.value = true
      _importFeedback.value = null
      runCatching {
        val input = getApplication<Application>().contentResolver.openInputStream(uri)
          ?: throw IOException("Não foi possível abrir o arquivo selecionado.")
        input.use { repository.importDatabase(it) }
      }.onSuccess { summary ->
        _importFeedback.value = ImportFeedback(
          message = "Banco importado: " +
            plural(summary.books, "livro", "livros") + ", " +
            plural(summary.verses, "versículo", "versículos") + " e " +
            plural(summary.comments, "anotação", "anotações") + ".",
          isError = false
        )
        reloadAfterImport()
      }.onFailure { error ->
        _importFeedback.value = ImportFeedback(
          message = error.message ?: "Falha ao importar o banco.",
          isError = true
        )
      }
      _isImporting.value = false
    }
  }

  /** Mescla anotações de um arquivo .json no banco atual. */
  fun importAnnotations(uri: Uri) {
    viewModelScope.launch {
      _isImporting.value = true
      _importFeedback.value = null
      runCatching {
        val json = getApplication<Application>().contentResolver.openInputStream(uri)
          ?.bufferedReader()
          ?.use { it.readText() }
          ?: throw IOException("Não foi possível abrir o arquivo selecionado.")
        if (_dataSummary.value.verses == 0) {
          throw IOException("Importe primeiro um banco com os versículos (.db).")
        }
        repository.importAnnotations(json)
      }.onSuccess { result ->
        _importFeedback.value = ImportFeedback(
          message = buildAnnotationMessage(result),
          isError = result.imported == 0
        )
        reloadAfterImport()
      }.onFailure { error ->
        _importFeedback.value = ImportFeedback(
          message = error.message ?: "Falha ao importar as anotações.",
          isError = true
        )
      }
      _isImporting.value = false
    }
  }

  private fun buildAnnotationMessage(result: AnnotationImportResult): String = buildString {
    append(
      if (result.imported == 1) "1 anotação importada."
      else "${result.imported} anotações importadas."
    )
    if (result.skipped > 0) {
      append(" ")
      append(
        if (result.skipped == 1) "1 entrada foi ignorada"
        else "${result.skipped} entradas foram ignoradas"
      )
      append(" por não corresponder a um livro, capítulo ou versículo existente.")
    }
  }

  private fun plural(count: Int, singular: String, plural: String): String =
    "$count ${if (count == 1) singular else plural}"

  private suspend fun reloadAfterImport() {
    val summary = repository.getDatabaseSummary()
    _dataSummary.value = summary
    _books.value = repository.getBooks()

    _homeState.value = _homeState.value.copy(isLoadingDaily = true)
    val daily = repository.getDailyReadings()
    _homeState.value = _homeState.value.copy(dailyReadings = daily, isLoadingDaily = false)

    val preferred = _homeState.value.lastReadChapterId
    val chapter = repository.getChapter(preferred) ?: repository.getChapter(1)
    if (chapter != null) {
      loadChapter(chapter.id)
    } else {
      _readerState.value = _readerState.value.copy(
        currentChapter = null,
        verses = emptyList(),
        isLoading = false
      )
    }
  }

  private fun loadBooks() {
    viewModelScope.launch {
      val list = repository.getBooks()
      _books.value = list
    }
  }

  private fun loadDailyReadings() {
    viewModelScope.launch {
      _homeState.value = _homeState.value.copy(isLoadingDaily = true)
      val daily = repository.getDailyReadings()
      _homeState.value = _homeState.value.copy(
        dailyReadings = daily,
        isLoadingDaily = false
      )
    }
  }

  fun loadChapter(chapterId: Int, targetVerseNumber: Int? = null) {
    viewModelScope.launch {
      _readerState.value = _readerState.value.copy(isLoading = true, targetVerseNumber = targetVerseNumber)
      val chapter = repository.getChapter(chapterId)
      if (chapter != null) {
        val verses = repository.getVersesForChapter(chapterId)
        val hasPrev = chapterId > 1
        val hasNext = chapterId < 1189

        _readerState.value = _readerState.value.copy(
          currentChapter = chapter,
          verses = verses,
          isLoading = false,
          hasPrevious = hasPrev,
          hasNext = hasNext,
          targetVerseNumber = targetVerseNumber
        )

        // Salva nas preferências como último lido
        _homeState.value = _homeState.value.copy(
          lastReadBookName = chapter.bookName,
          lastReadChapterNumber = chapter.number,
          lastReadChapterId = chapter.id
        )
        prefs.edit()
          .putString("last_book_name", chapter.bookName)
          .putInt("last_chapter_num", chapter.number)
          .putInt("last_chapter_id", chapter.id)
          .apply()
      } else {
        _readerState.value = _readerState.value.copy(isLoading = false)
      }
    }
  }

  fun loadChapterByBookAndNumber(bookId: Int, chapterNumber: Int, targetVerseNumber: Int? = null) {
    viewModelScope.launch {
      _readerState.value = _readerState.value.copy(isLoading = true)
      val chapter = repository.getChapterByBookAndNumber(bookId, chapterNumber)
      if (chapter != null) {
        loadChapter(chapter.id, targetVerseNumber)
      } else {
        _readerState.value = _readerState.value.copy(isLoading = false)
      }
    }
  }

  fun navigateAdjacentChapter(isNext: Boolean) {
    val current = _readerState.value.currentChapter ?: return
    val nextId = if (isNext) current.id + 1 else current.id - 1
    if (nextId in 1..1189) {
      loadChapter(nextId)
    }
  }

  fun adjustFontSize(delta: Float) {
    val current = _readerState.value.fontSizeSp
    val newSize = (current + delta).coerceIn(14f, 32f)
    _readerState.value = _readerState.value.copy(fontSizeSp = newSize)
    prefs.edit().putFloat("font_size_sp", newSize).apply()
  }

  fun openCommentary(verse: Verse) {
    if (_readerState.value.currentChapter == null) return
    viewModelScope.launch {
      _selectedVerseForComment.value = verse
      val comments = repository.getCommentsForVerse(verse.id)
      _verseComments.value = comments
    }
  }

  fun dismissCommentary() {
    _selectedVerseForComment.value = null
    _verseComments.value = emptyList()
  }

  fun openBookChapterDialog() {
    _isBookChapterDialogOpen.value = true
  }

  fun dismissBookChapterDialog() {
    _isBookChapterDialogOpen.value = false
  }

  fun toggleTheme() {
    val newValue = !_isDarkTheme.value
    _isDarkTheme.value = newValue
    prefs.edit().putBoolean("dark_theme", newValue).apply()
  }

  /** Marca o tour como visto — ele não volta a aparecer sozinho. */
  fun completeOnboarding() {
    _showOnboarding.value = false
    prefs.edit().putBoolean("onboarding_done", true).apply()
  }

  /** Reabre o tour a pedido do usuário (painel "Sobre o aplicativo"). */
  fun restartOnboarding() {
    _showOnboarding.value = true
  }

  fun onSearchQueryChanged(newQuery: String) {
    _searchState.value = _searchState.value.copy(query = newQuery)
    executeSearch(newQuery, _searchState.value.filter)
  }

  fun onSearchFilterChanged(newFilter: SearchResultType) {
    _searchState.value = _searchState.value.copy(filter = newFilter)
    executeSearch(_searchState.value.query, newFilter)
  }

  private fun executeSearch(query: String, filter: SearchResultType) {
    if (query.trim().length < 2) {
      _searchState.value = _searchState.value.copy(results = emptyList(), isSearching = false)
      return
    }
    viewModelScope.launch {
      _searchState.value = _searchState.value.copy(isSearching = true)
      val results = repository.search(query, filter)
      _searchState.value = _searchState.value.copy(
        results = results,
        isSearching = false
      )
    }
  }
}
