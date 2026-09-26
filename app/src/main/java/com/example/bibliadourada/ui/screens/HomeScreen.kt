package com.example.bibliadourada.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bibliadourada.R
import com.example.bibliadourada.data.DatabaseSummary
import com.example.bibliadourada.data.getTodayDateString
import com.example.bibliadourada.data.model.Book
import com.example.bibliadourada.data.model.DailyReading
import com.example.bibliadourada.theme.GoldAmber
import com.example.bibliadourada.theme.GoldBorder
import com.example.bibliadourada.theme.GoldChampagne
import com.example.bibliadourada.theme.GoldDeepBackground
import com.example.bibliadourada.theme.GoldImperial
import com.example.bibliadourada.theme.GoldRadiant
import com.example.bibliadourada.theme.GoldSurface
import com.example.bibliadourada.theme.GoldSurfaceVariant
import com.example.bibliadourada.theme.GoldTextPrimary
import com.example.bibliadourada.theme.GoldTextSecondary
import com.example.bibliadourada.ui.HomeUiState
import com.example.bibliadourada.ui.components.AboutAppSheet
import com.example.bibliadourada.ui.components.tourTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  homeState: HomeUiState,
  books: List<Book>,
  onContinueReading: (chapterId: Int) -> Unit,
  onOpenDailyChapter: (chapterId: Int) -> Unit,
  onOpenBook: (book: Book) -> Unit,
  isDarkTheme: Boolean = false,
  dataSummary: DatabaseSummary = DatabaseSummary(0, 0, 0),
  onOpenImport: () -> Unit = {},
  onReplayTour: () -> Unit = {},
  onToggleTheme: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var selectedTestamentFilter by remember { mutableIntStateOf(0) } // 0 = AT, 1 = NT
  var showAbout by rememberSaveable { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(GoldDeepBackground),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 28.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    // Cabeçalho da Bíblia Dourada
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
              painter = painterResource(R.drawable.ic_logo_biblia),
              contentDescription = null,
              modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "BÍBLIA DOURADA",
              color = GoldImperial,
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Serif,
              letterSpacing = 1.2.sp
            )
          }
          Text(
            text = "Edição de Estudo & Meditação",
            color = GoldTextSecondary,
            fontSize = 13.sp,
            fontFamily = FontFamily.Serif
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Alternância entre tema escuro e tema claro ("white")
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(GoldSurfaceVariant)
              .border(1.dp, GoldBorder, CircleShape)
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = GoldImperial.copy(alpha = 0.3f)),
                onClick = onToggleTheme
              )
              .tourTarget("btn_tema"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
              contentDescription = if (isDarkTheme) {
                "Mudar para tema claro"
              } else {
                "Mudar para tema escuro"
              },
              tint = GoldRadiant,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(GoldSurfaceVariant)
              .border(1.dp, GoldBorder, CircleShape)
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = GoldImperial.copy(alpha = 0.3f)),
                onClick = { showAbout = true }
              )
              .tourTarget("btn_sobre"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.MenuBook,
              contentDescription = "Sobre o aplicativo",
              tint = GoldRadiant,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }

    if (dataSummary.verses == 0) {
      // Base vazia: orienta a importação em vez de mostrar cards sem conteúdo.
      item {
        EmptyDataCard(onOpenImport = onOpenImport)
      }
    } else {
      // 1. Card Luxuoso da Leitura Diária do Dia (3 capítulos sugeridos)
      item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.2.dp, GoldBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GoldSurface)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.verticalGradient(
                listOf(
                  GoldSurfaceVariant.copy(alpha = 0.6f),
                  GoldSurface
                )
              )
            )
            .padding(18.dp)
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.CalendarMonth,
                  contentDescription = null,
                  tint = GoldAmber,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "LEITURA DIÁRIA",
                  color = GoldAmber,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                )
              }

              Text(
                text = getTodayDateString(),
                color = GoldChampagne,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = "Capítulos Sugeridos para Hoje",
              color = GoldTextPrimary,
              fontSize = 17.sp,
              fontWeight = FontWeight.SemiBold,
              fontFamily = FontFamily.Serif
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (homeState.isLoadingDaily) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
              ) {
                CircularProgressIndicator(color = GoldImperial, modifier = Modifier.size(28.dp))
              }
            } else if (homeState.dailyReadings.isEmpty()) {
              Text(
                text = "Carregando plano de leitura...",
                color = GoldTextSecondary,
                fontSize = 13.sp
              )
            } else {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                homeState.dailyReadings.forEachIndexed { index, reading ->
                  DailyReadingItem(
                    index = index + 1,
                    reading = reading,
                    onClick = { onOpenDailyChapter(reading.chapterId) }
                  )
                }
              }
            }
          }
        }
      }
    }

    // 2. Card "Continuar Leitura" (último livro/capítulo acessado)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, GoldBorder.copy(alpha = 0.8f), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = GoldSurface)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.horizontalGradient(
                listOf(
                  GoldSurfaceVariant.copy(alpha = 0.7f),
                  GoldSurface
                )
              )
            )
            .padding(18.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GoldAmber.copy(alpha = 0.15f))
                .border(1.dp, GoldAmber.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = null,
                tint = GoldRadiant,
                modifier = Modifier.size(24.dp)
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
              Text(
                text = "CONTINUAR LEITURA",
                color = GoldAmber,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "${homeState.lastReadBookName} ${homeState.lastReadChapterNumber}",
                color = GoldChampagne,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
              )
            }
          }

          Button(
            onClick = { onContinueReading(homeState.lastReadChapterId) },
            colors = ButtonDefaults.buttonColors(
              containerColor = GoldImperial,
              contentColor = Color(0xFF1F1600)
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
          ) {
            Text(
              text = "Ler",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }

    // 3. Atalhos para Livros da Bíblia
    item {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Livros da Palavra",
            color = GoldTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif
          )

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(
              selected = selectedTestamentFilter == 0,
              onClick = { selectedTestamentFilter = 0 },
              label = {
                Text(
                  "Antigo",
                  fontSize = 12.sp,
                  fontWeight = if (selectedTestamentFilter == 0) FontWeight.Bold else FontWeight.Normal
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GoldImperial,
                selectedLabelColor = Color(0xFF1F1600),
                containerColor = GoldSurface,
                labelColor = GoldTextSecondary
              ),
              border = FilterChipDefaults.filterChipBorder(
                borderColor = GoldBorder,
                selectedBorderColor = GoldImperial,
                enabled = true,
                selected = selectedTestamentFilter == 0
              )
            )

            FilterChip(
              selected = selectedTestamentFilter == 1,
              onClick = { selectedTestamentFilter = 1 },
              label = {
                Text(
                  "Novo",
                  fontSize = 12.sp,
                  fontWeight = if (selectedTestamentFilter == 1) FontWeight.Bold else FontWeight.Normal
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GoldImperial,
                selectedLabelColor = Color(0xFF1F1600),
                containerColor = GoldSurface,
                labelColor = GoldTextSecondary
              ),
              border = FilterChipDefaults.filterChipBorder(
                borderColor = GoldBorder,
                selectedBorderColor = GoldImperial,
                enabled = true,
                selected = selectedTestamentFilter == 1
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val filtered = remember(books, selectedTestamentFilter) {
          books.filter { if (selectedTestamentFilter == 0) it.isOldTestament else !it.isOldTestament }
        }

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(vertical = 4.dp)
        ) {
          items(filtered, key = { it.id }) { book ->
            BookShortcutCard(book = book, onClick = { onOpenBook(book) })
          }
        }
      }
    }
    } // fim do bloco "base carregada"
  }

  // Modal "Sobre o aplicativo" (aberto pelo ícone da Bíblia no cabeçalho)
  if (showAbout) {
    AboutAppSheet(
      onDismiss = { showAbout = false },
      onOpenImport = {
        showAbout = false
        onOpenImport()
      },
      onReplayTour = {
        showAbout = false
        onReplayTour()
      }
    )
  }
}

/** Estado vazio: o app não distribui as anotações, então orienta a importação. */
@Composable
private fun EmptyDataCard(onOpenImport: () -> Unit) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .border(1.2.dp, GoldAmber.copy(alpha = 0.7f), RoundedCornerShape(20.dp)),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = GoldSurface)
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Download,
          contentDescription = null,
          tint = GoldAmber,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "CONTEÚDO NÃO CARREGADO",
          color = GoldAmber,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "O aplicativo está vazio",
        color = GoldTextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Serif
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "As anotações de estudo não acompanham o aplicativo, para respeitar " +
          "os direitos autorais de quem as produziu. Importe um banco de dados " +
          "(.db) ou um arquivo de anotações (.json) que você já possua.",
        color = GoldTextSecondary,
        fontSize = 13.sp,
        lineHeight = 19.sp
      )

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = onOpenImport,
        colors = ButtonDefaults.buttonColors(
          containerColor = GoldImperial,
          contentColor = Color(0xFF1F1600)
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
      ) {
        Text(
          text = "Importar dados",
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        )
      }
    }
  }
}

@Composable
private fun DailyReadingItem(
  index: Int,
  reading: DailyReading,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(GoldDeepBackground)
      .border(1.dp, GoldBorder.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(color = GoldImperial.copy(alpha = 0.2f)),
        onClick = onClick
      )
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(GoldSurfaceVariant),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "$index",
            color = GoldRadiant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Text(
            text = "${reading.bookName} ${reading.chapterNumber}",
            color = GoldTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Serif
          )
          Text(
            text = if (reading.testament == 0) "Antigo Testamento" else "Novo Testamento",
            color = GoldTextSecondary,
            fontSize = 11.sp
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "Ler",
          color = GoldAmber,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = null,
          tint = GoldAmber,
          modifier = Modifier.size(14.dp)
        )
      }
    }
  }
}

@Composable
private fun BookShortcutCard(
  book: Book,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .width(108.dp)
      .clip(RoundedCornerShape(14.dp))
      .background(GoldSurface)
      .border(1.dp, GoldBorder.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(color = GoldImperial.copy(alpha = 0.25f)),
        onClick = onClick
      )
      .padding(12.dp)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth()
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(GoldSurfaceVariant)
          .border(1.dp, GoldBorder, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = book.abbreviation,
          color = GoldRadiant,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = book.name,
        color = GoldChampagne,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(2.dp))

      Text(
        text = "${book.totalChapters} caps",
        color = GoldTextSecondary,
        fontSize = 10.sp
      )
    }
  }
}
