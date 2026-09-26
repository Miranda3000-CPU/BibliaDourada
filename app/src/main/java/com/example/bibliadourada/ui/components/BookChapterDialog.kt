package com.example.bibliadourada.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.bibliadourada.data.model.Book
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookChapterDialog(
  books: List<Book>,
  currentBookId: Int,
  currentChapterNumber: Int,
  onDismiss: () -> Unit,
  onChapterSelected: (book: Book, chapterNumber: Int) -> Unit
) {
  val initialBook = remember(books, currentBookId) {
    books.find { it.id == currentBookId } ?: books.firstOrNull()
  }
  var selectedTab by remember {
    mutableIntStateOf(if (initialBook?.isOldTestament == false) 1 else 0)
  }

  var selectedBookForChapters by remember {
    mutableStateOf<Book?>(null)
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .fillMaxHeight(0.85f)
        .clip(RoundedCornerShape(24.dp))
        .background(GoldDeepBackground)
        .border(1.2.dp, GoldBorder, RoundedCornerShape(24.dp))
    ) {
      Column(
        modifier = Modifier.fillMaxSize()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (selectedBookForChapters != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(onClick = { selectedBookForChapters = null }) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Voltar aos livros",
                  tint = GoldImperial
                )
              }
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${selectedBookForChapters?.name}",
                color = GoldImperial,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
              )
            }
          } else {
            Text(
              text = "Navegar na Bíblia",
              color = GoldImperial,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Serif,
              modifier = Modifier.padding(start = 8.dp)
            )
          }

          IconButton(onClick = onDismiss) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Fechar",
              tint = GoldTextSecondary
            )
          }
        }

        AnimatedContent(
          targetState = selectedBookForChapters,
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          label = "BookChapterTransition"
        ) { bookTarget ->
          if (bookTarget == null) {
            Column(modifier = Modifier.fillMaxSize()) {
              PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = GoldDeepBackground,
                contentColor = GoldImperial,
                indicator = {
                  TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(selectedTab),
                    color = GoldImperial,
                    width = 48.dp
                  )
                },
                divider = {
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(1.dp)
                      .background(GoldBorder.copy(alpha = 0.5f))
                  )
                }
              ) {
                Tab(
                  selected = selectedTab == 0,
                  onClick = { selectedTab = 0 },
                  text = {
                    Text(
                      text = "Antigo Testamento (39)",
                      color = if (selectedTab == 0) GoldImperial else GoldTextSecondary,
                      fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                      fontSize = 13.sp
                    )
                  }
                )
                Tab(
                  selected = selectedTab == 1,
                  onClick = { selectedTab = 1 },
                  text = {
                    Text(
                      text = "Novo Testamento (27)",
                      color = if (selectedTab == 1) GoldImperial else GoldTextSecondary,
                      fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                      fontSize = 13.sp
                    )
                  }
                )
              }

              val filteredBooks = remember(books, selectedTab) {
                books.filter { if (selectedTab == 0) it.isOldTestament else !it.isOldTestament }
              }

              LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
              ) {
                items(filteredBooks, key = { it.id }) { book ->
                  val isCurrentBook = book.id == currentBookId
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(12.dp))
                      .background(if (isCurrentBook) GoldSurfaceVariant else GoldSurface)
                      .border(
                        width = if (isCurrentBook) 1.5.dp else 1.dp,
                        color = if (isCurrentBook) GoldImperial else GoldBorder.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp)
                      )
                      .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = GoldImperial.copy(alpha = 0.3f)),
                        onClick = { selectedBookForChapters = book }
                      )
                      .padding(vertical = 12.dp, horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.Center
                    ) {
                      Text(
                        text = book.abbreviation,
                        color = if (isCurrentBook) GoldRadiant else GoldAmber,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                      )
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(
                        text = book.name,
                        color = if (isCurrentBook) GoldChampagne else GoldTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                      )
                    }
                  }
                }
              }
            }
          } else {
            Column(modifier = Modifier.fillMaxSize()) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(GoldSurfaceVariant.copy(alpha = 0.5f))
                  .padding(horizontal = 16.dp, vertical = 8.dp)
              ) {
                Text(
                  text = "Selecione o Capítulo (1 a ${bookTarget.totalChapters})",
                  color = GoldTextSecondary,
                  fontSize = 13.sp,
                  fontFamily = FontFamily.Serif
                )
              }

              LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
              ) {
                items(count = bookTarget.totalChapters) { index ->
                  val chapterNum = index + 1
                  val isCurrent = bookTarget.id == currentBookId && chapterNum == currentChapterNumber
                  Box(
                    modifier = Modifier
                      .aspectRatio(1f)
                      .clip(RoundedCornerShape(12.dp))
                      .background(if (isCurrent) GoldImperial else GoldSurface)
                      .border(
                        width = 1.dp,
                        color = if (isCurrent) GoldRadiant else GoldBorder.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp)
                      )
                      .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = GoldImperial.copy(alpha = 0.3f)),
                        onClick = {
                          onChapterSelected(bookTarget, chapterNum)
                          onDismiss()
                        }
                      ),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = "$chapterNum",
                      color = if (isCurrent) Color(0xFF1F1600) else GoldTextPrimary,
                      fontSize = 16.sp,
                      fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
