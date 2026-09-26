package com.example.bibliadourada.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.ripple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bibliadourada.data.model.Book
import com.example.bibliadourada.data.model.Comment
import com.example.bibliadourada.data.model.Verse
import com.example.bibliadourada.theme.GoldAmber
import com.example.bibliadourada.theme.GoldBorder
import com.example.bibliadourada.theme.GoldDeepBackground
import com.example.bibliadourada.theme.GoldImperial
import com.example.bibliadourada.theme.GoldMuted
import com.example.bibliadourada.theme.GoldSurface
import com.example.bibliadourada.theme.GoldSurfaceVariant
import com.example.bibliadourada.theme.GoldTextPrimary
import com.example.bibliadourada.theme.GoldTextSecondary
import com.example.bibliadourada.ui.ReaderUiState
import com.example.bibliadourada.ui.components.BookChapterDialog
import com.example.bibliadourada.ui.components.CommentaryBottomSheet
import com.example.bibliadourada.ui.components.VerseItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
  readerState: ReaderUiState,
  books: List<Book>,
  isBookChapterDialogOpen: Boolean,
  selectedVerseForComment: Verse?,
  verseComments: List<Comment>,
  onOpenBookChapterDialog: () -> Unit,
  onDismissBookChapterDialog: () -> Unit,
  onChapterSelected: (book: Book, chapterNumber: Int) -> Unit,
  onPreviousChapter: () -> Unit,
  onNextChapter: () -> Unit,
  onIncreaseFontSize: () -> Unit,
  onDecreaseFontSize: () -> Unit,
  onVerseClick: (Verse) -> Unit,
  onCommentClick: (Verse) -> Unit,
  onDismissCommentary: () -> Unit,
  onOpenImport: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val chapter = readerState.currentChapter
  val listState = rememberLazyListState()

  // Efeito de scroll automático até o versículo selecionado
  LaunchedEffect(readerState.currentChapter?.id, readerState.targetVerseNumber) {
    val targetVerse = readerState.targetVerseNumber
    if (targetVerse != null && readerState.verses.isNotEmpty()) {
      val targetIndex = readerState.verses.indexOfFirst { it.number == targetVerse }
      if (targetIndex >= 0) {
        listState.animateScrollToItem(targetIndex)
      }
    } else {
      listState.scrollToItem(0)
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(GoldDeepBackground)
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // TopAppBar Nobre em Dourado
      TopAppBar(
        title = {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(GoldSurfaceVariant.copy(alpha = 0.7f))
              .border(1.dp, GoldBorder.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = GoldImperial.copy(alpha = 0.3f)),
                onClick = onOpenBookChapterDialog
              )
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${chapter?.bookName ?: "Livro"} ${chapter?.number ?: 1}",
                color = GoldImperial,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Mudar livro ou capítulo",
                tint = GoldAmber,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        },
        actions = {
          // Botões de ajuste de fonte A- e A+
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(GoldSurface)
              .border(1.dp, GoldBorder.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
              .padding(horizontal = 4.dp, vertical = 2.dp)
          ) {
            IconButton(
              onClick = onDecreaseFontSize,
              modifier = Modifier.size(36.dp)
            ) {
              Text(
                text = "A-",
                color = GoldTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
            }

            Box(
              modifier = Modifier
                .width(1.dp)
                .height(16.dp)
                .background(GoldBorder.copy(alpha = 0.5f))
            )

            IconButton(
              onClick = onIncreaseFontSize,
              modifier = Modifier.size(36.dp)
            ) {
              Text(
                text = "A+",
                color = GoldImperial,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
          Spacer(modifier = Modifier.width(8.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = GoldDeepBackground,
          titleContentColor = GoldImperial
        )
      )

      HorizontalDivider(color = GoldBorder.copy(alpha = 0.4f), thickness = 1.dp)

      // Conteúdo da Leitura
      if (readerState.isLoading) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator(color = GoldImperial)
        }
      } else if (readerState.currentChapter == null) {
        EmptyReaderState(
          onOpenImport = onOpenImport,
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        )
      } else {
        LazyColumn(
          state = listState,
          contentPadding = PaddingValues(vertical = 12.dp),
          modifier = Modifier
            .fillMaxSize()
            .weight(1f)
        ) {
          // Cabeçalho do capítulo
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = chapter?.bookName?.uppercase() ?: "",
                  color = GoldAmber,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Capítulo ${chapter?.number ?: 1}",
                  color = GoldImperial,
                  fontSize = 26.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Serif
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                  modifier = Modifier
                    .width(48.dp)
                    .height(2.dp)
                    .background(GoldAmber.copy(alpha = 0.6f))
                )
              }
            }
          }

          items(readerState.verses, key = { it.id }) { verse ->
            val isTarget = readerState.targetVerseNumber == verse.number
            VerseItem(
              verse = verse,
              fontSize = readerState.fontSizeSp.sp,
              isSelected = isTarget,
              onVerseClick = onVerseClick,
              onCommentClick = onCommentClick
            )
          }

          // Espaço extra no final da lista
          item {
            Spacer(modifier = Modifier.height(16.dp))
          }
        }
      }

      // Barra Inferior de Navegação entre Capítulos
      HorizontalDivider(color = GoldBorder.copy(alpha = 0.4f), thickness = 1.dp)

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(GoldSurface)
          .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Botão Capítulo Anterior
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (readerState.hasPrevious) GoldSurfaceVariant else GoldSurfaceVariant.copy(alpha = 0.3f))
            .border(
              1.dp,
              if (readerState.hasPrevious) GoldBorder else GoldBorder.copy(alpha = 0.3f),
              RoundedCornerShape(12.dp)
            )
            .clickable(
              enabled = readerState.hasPrevious,
              interactionSource = remember { MutableInteractionSource() },
              indication = ripple(color = GoldImperial.copy(alpha = 0.3f)),
              onClick = onPreviousChapter
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Capítulo Anterior",
              tint = if (readerState.hasPrevious) GoldImperial else GoldMuted.copy(alpha = 0.5f),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Anterior",
              color = if (readerState.hasPrevious) GoldTextPrimary else GoldMuted.copy(alpha = 0.5f),
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }

        Text(
          text = "${chapter?.bookName} ${chapter?.number}",
          color = GoldTextSecondary,
          fontSize = 13.sp,
          fontFamily = FontFamily.Serif
        )

        // Botão Próximo Capítulo
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (readerState.hasNext) GoldSurfaceVariant else GoldSurfaceVariant.copy(alpha = 0.3f))
            .border(
              1.dp,
              if (readerState.hasNext) GoldBorder else GoldBorder.copy(alpha = 0.3f),
              RoundedCornerShape(12.dp)
            )
            .clickable(
              enabled = readerState.hasNext,
              interactionSource = remember { MutableInteractionSource() },
              indication = ripple(color = GoldImperial.copy(alpha = 0.3f)),
              onClick = onNextChapter
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Próximo",
              color = if (readerState.hasNext) GoldTextPrimary else GoldMuted.copy(alpha = 0.5f),
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Próximo Capítulo",
              tint = if (readerState.hasNext) GoldImperial else GoldMuted.copy(alpha = 0.5f),
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }

    // Modal de Navegação Rápida (Livros / Capítulos)
    if (isBookChapterDialogOpen) {
      BookChapterDialog(
        books = books,
        currentBookId = chapter?.bookId ?: 1,
        currentChapterNumber = chapter?.number ?: 1,
        onDismiss = onDismissBookChapterDialog,
        onChapterSelected = onChapterSelected
      )
    }

    // ModalBottomSheet com Comentários do Versículo
    if (selectedVerseForComment != null) {
      CommentaryBottomSheet(
        verse = selectedVerseForComment,
        bookName = chapter?.bookName ?: "",
        chapterNumber = chapter?.number ?: 1,
        comments = verseComments,
        onDismiss = onDismissCommentary
      )
    }
  }
}

/** Estado vazio do leitor: sem banco carregado, orienta a importação. */
@Composable
private fun EmptyReaderState(
  onOpenImport: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier.padding(horizontal = 28.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Icon(
        imageVector = Icons.Default.Download,
        contentDescription = null,
        tint = GoldAmber,
        modifier = Modifier.size(42.dp)
      )
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = "Nenhum texto carregado",
        color = GoldTextPrimary,
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Serif
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Importe um banco de dados (.db) ou um arquivo de anotações (.json) " +
          "para começar a leitura.",
        color = GoldTextSecondary,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(18.dp))
      Button(
        onClick = onOpenImport,
        colors = ButtonDefaults.buttonColors(
          containerColor = GoldImperial,
          contentColor = Color(0xFF1F1600)
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
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
