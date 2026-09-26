package com.example.bibliadourada

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bibliadourada.data.model.SearchResultType
import com.example.bibliadourada.data.model.Verse
import com.example.bibliadourada.theme.BibliaDouradaTheme
import com.example.bibliadourada.theme.GoldBorder
import com.example.bibliadourada.theme.GoldDeepBackground
import com.example.bibliadourada.theme.GoldImperial
import com.example.bibliadourada.theme.GoldMuted
import com.example.bibliadourada.theme.GoldSurfaceVariant
import com.example.bibliadourada.ui.BibleViewModel
import com.example.bibliadourada.ui.components.CoachMarkTour
import com.example.bibliadourada.ui.components.ImportDataSheet
import com.example.bibliadourada.ui.components.LocalTourTargets
import com.example.bibliadourada.ui.components.TourStep
import com.example.bibliadourada.ui.components.TourTargetRegistry
import com.example.bibliadourada.ui.components.tourTarget
import com.example.bibliadourada.ui.screens.HomeScreen
import com.example.bibliadourada.ui.screens.ReaderScreen
import com.example.bibliadourada.ui.screens.SearchScreen

enum class BibleTab(val title: String) {
  HOME("Início"),
  READER("Leitura"),
  SEARCH("Pesquisa")
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: BibleViewModel = viewModel()
      val isDarkTheme by viewModel.isDarkTheme.collectAsState()

      BibliaDouradaTheme(darkTheme = isDarkTheme) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          BibliaApp(
            viewModel = viewModel,
            onToggleTheme = { viewModel.toggleTheme() }
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibliaApp(
  viewModel: BibleViewModel,
  onToggleTheme: () -> Unit
) {
  var currentTab by rememberSaveable { mutableStateOf(BibleTab.HOME) }

  val books by viewModel.books.collectAsState()
  val readerState by viewModel.readerState.collectAsState()
  val homeState by viewModel.homeState.collectAsState()
  val searchState by viewModel.searchState.collectAsState()
  val selectedVerseForComment by viewModel.selectedVerseForComment.collectAsState()
  val verseComments by viewModel.verseComments.collectAsState()
  val isBookChapterDialogOpen by viewModel.isBookChapterDialogOpen.collectAsState()
  val isDarkTheme by viewModel.isDarkTheme.collectAsState()
  val dataSummary by viewModel.dataSummary.collectAsState()
  val isImportSheetOpen by viewModel.isImportSheetOpen.collectAsState()
  val isImporting by viewModel.isImporting.collectAsState()
  val importFeedback by viewModel.importFeedback.collectAsState()

  val openImport = { viewModel.openImportSheet() }

  // Posições na tela usadas pelo tour de primeiro acesso
  val tourRegistry = remember { TourTargetRegistry() }
  val showOnboarding by viewModel.showOnboarding.collectAsState()

  CompositionLocalProvider(LocalTourTargets provides tourRegistry) {
  Box(modifier = Modifier.fillMaxSize()) {
  Scaffold(
    bottomBar = {
      Column {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(GoldBorder.copy(alpha = 0.5f))
        )
        NavigationBar(
          containerColor = GoldDeepBackground,
          contentColor = GoldImperial,
          tonalElevation = 8.dp
        ) {
          // Tab Início
          NavigationBarItem(
            selected = currentTab == BibleTab.HOME,
            onClick = { currentTab = BibleTab.HOME },
            modifier = Modifier.tourTarget("tab_inicio"),
            icon = {
              Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Início",
                modifier = Modifier.size(24.dp)
              )
            },
            label = {
              Text(
                text = "Início",
                fontSize = 12.sp,
                fontWeight = if (currentTab == BibleTab.HOME) FontWeight.Bold else FontWeight.Normal,
                fontFamily = FontFamily.Serif
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = GoldImperial,
              unselectedIconColor = GoldMuted,
              selectedTextColor = GoldImperial,
              unselectedTextColor = GoldMuted,
              indicatorColor = GoldSurfaceVariant
            )
          )

          // Tab Leitura
          NavigationBarItem(
            selected = currentTab == BibleTab.READER,
            onClick = { currentTab = BibleTab.READER },
            modifier = Modifier.tourTarget("tab_leitura"),
            icon = {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = "Leitura",
                modifier = Modifier.size(24.dp)
              )
            },
            label = {
              Text(
                text = "Leitura",
                fontSize = 12.sp,
                fontWeight = if (currentTab == BibleTab.READER) FontWeight.Bold else FontWeight.Normal,
                fontFamily = FontFamily.Serif
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = GoldImperial,
              unselectedIconColor = GoldMuted,
              selectedTextColor = GoldImperial,
              unselectedTextColor = GoldMuted,
              indicatorColor = GoldSurfaceVariant
            )
          )

          // Tab Pesquisa
          NavigationBarItem(
            selected = currentTab == BibleTab.SEARCH,
            onClick = { currentTab = BibleTab.SEARCH },
            modifier = Modifier.tourTarget("tab_pesquisa"),
            icon = {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Pesquisa",
                modifier = Modifier.size(24.dp)
              )
            },
            label = {
              Text(
                text = "Pesquisa",
                fontSize = 12.sp,
                fontWeight = if (currentTab == BibleTab.SEARCH) FontWeight.Bold else FontWeight.Normal,
                fontFamily = FontFamily.Serif
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = GoldImperial,
              unselectedIconColor = GoldMuted,
              selectedTextColor = GoldImperial,
              unselectedTextColor = GoldMuted,
              indicatorColor = GoldSurfaceVariant
            )
          )
        }
      }
    }
  ) { innerPadding ->
    Crossfade(
      targetState = currentTab,
      label = "TabCrossfade",
      modifier = Modifier.padding(innerPadding)
    ) { tab ->
      when (tab) {
        BibleTab.HOME -> {
          HomeScreen(
            homeState = homeState,
            books = books,
            isDarkTheme = isDarkTheme,
            dataSummary = dataSummary,
            onToggleTheme = onToggleTheme,
            onOpenImport = openImport,
            onReplayTour = { viewModel.restartOnboarding() },
            onContinueReading = { chapterId ->
              viewModel.loadChapter(chapterId)
              currentTab = BibleTab.READER
            },
            onOpenDailyChapter = { chapterId ->
              viewModel.loadChapter(chapterId)
              currentTab = BibleTab.READER
            },
            onOpenBook = { book ->
              viewModel.loadChapterByBookAndNumber(book.id, 1)
              currentTab = BibleTab.READER
            }
          )
        }
        BibleTab.READER -> {
          ReaderScreen(
            readerState = readerState,
            books = books,
            isBookChapterDialogOpen = isBookChapterDialogOpen,
            selectedVerseForComment = selectedVerseForComment,
            verseComments = verseComments,
            onOpenImport = openImport,
            onOpenBookChapterDialog = { viewModel.openBookChapterDialog() },
            onDismissBookChapterDialog = { viewModel.dismissBookChapterDialog() },
            onChapterSelected = { book, chapterNum ->
              viewModel.loadChapterByBookAndNumber(book.id, chapterNum)
            },
            onPreviousChapter = { viewModel.navigateAdjacentChapter(isNext = false) },
            onNextChapter = { viewModel.navigateAdjacentChapter(isNext = true) },
            onIncreaseFontSize = { viewModel.adjustFontSize(2f) },
            onDecreaseFontSize = { viewModel.adjustFontSize(-2f) },
            onVerseClick = { /* Opcional: seleção visual ou marcador */ },
            onCommentClick = { verse -> viewModel.openCommentary(verse) },
            onDismissCommentary = { viewModel.dismissCommentary() }
          )
        }
        BibleTab.SEARCH -> {
          SearchScreen(
            searchState = searchState,
            onQueryChange = { viewModel.onSearchQueryChanged(it) },
            onFilterChange = { viewModel.onSearchFilterChanged(it) },
            onResultClick = { result ->
              viewModel.loadChapter(result.chapterId, targetVerseNumber = result.verseNumber)
              currentTab = BibleTab.READER
              if (result.type == SearchResultType.COMMENT) {
                viewModel.openCommentary(
                  Verse(
                    id = result.id.toInt(),
                    chapterId = result.chapterId,
                    number = result.verseNumber,
                    text = result.text,
                    hasComment = true
                  )
                )
              }
            }
          )
        }
      }
    }
  }

  // Ferramenta de importação de dados (acessível de qualquer tela)
  if (isImportSheetOpen) {
    ImportDataSheet(
      summary = dataSummary,
      isImporting = isImporting,
      feedback = importFeedback,
      onDismiss = {
        viewModel.dismissImportSheet()
        viewModel.clearImportFeedback()
      },
      onImportDatabase = { uri -> viewModel.importDatabase(uri) },
      onImportAnnotations = { uri -> viewModel.importAnnotations(uri) }
    )
  }

  // Tour de primeiro acesso: escurece a tela e apresenta cada seção.
  if (showOnboarding) {
    CoachMarkTour(
      steps = onboardingSteps,
      onFinish = { viewModel.completeOnboarding() }
    )
  }
  }
  }
}

/** Roteiro do tour exibido na primeira abertura do aplicativo. */
private val onboardingSteps = listOf(
  TourStep(
    targetKey = null,
    title = "Bem-vindo ao Bíblia Dourada",
    description = "Um tour rápido pelas telas. Toque em Avançar para conhecer " +
      "cada parte do aplicativo."
  ),
  TourStep(
    targetKey = "tab_inicio",
    title = "Início",
    description = "Seu ponto de partida: a leitura diária, o capítulo de onde " +
      "você parou e atalhos para todos os livros."
  ),
  TourStep(
    targetKey = "tab_leitura",
    title = "Leitura",
    description = "O texto bíblico. Toque em um versículo com o selo ✦ Estudo " +
      "para abrir as anotações daquele versículo. Os botões A− e A+ ajustam " +
      "o tamanho da letra."
  ),
  TourStep(
    targetKey = "tab_pesquisa",
    title = "Pesquisa",
    description = "Procure qualquer palavra nos versículos e nos estudos. " +
      "Funciona mesmo sem acentos."
  ),
  TourStep(
    targetKey = "btn_tema",
    title = "Tema claro e escuro",
    description = "Alterne a aparência do aplicativo a qualquer momento. " +
      "A sua escolha fica salva."
  ),
  TourStep(
    targetKey = "btn_sobre",
    title = "Sobre e dados",
    description = "Versão, autoria, contato e a ferramenta para importar um " +
      "banco de dados ou anotações. Você também pode rever este tour aqui."
  )
)
