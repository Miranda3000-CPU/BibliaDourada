package com.example.bibliadourada.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bibliadourada.data.model.Comment
import com.example.bibliadourada.data.model.Verse
import com.example.bibliadourada.theme.GoldAmber
import com.example.bibliadourada.theme.GoldBadge
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
fun CommentaryBottomSheet(
  verse: Verse?,
  bookName: String,
  chapterNumber: Int,
  comments: List<Comment>,
  onDismiss: () -> Unit,
  sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
  if (verse == null) return

  val verseAnnotated = remember(verse.text) {
    parseHtmlToAnnotatedString(verse.text)
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = GoldDeepBackground,
    contentColor = GoldTextPrimary,
    dragHandle = {
      Box(
        modifier = Modifier
          .padding(top = 10.dp, bottom = 6.dp)
          .size(width = 44.dp, height = 4.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(GoldBorder)
      )
    },
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp)
        .padding(bottom = 36.dp)
    ) {
      // Cabeçalho Dourado Nobre
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = GoldImperial,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "$bookName $chapterNumber:${verse.number}",
            color = GoldImperial,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif
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

      HorizontalDivider(color = GoldBorder.copy(alpha = 0.5f), thickness = 1.dp)

      Spacer(modifier = Modifier.height(16.dp))

      // Card Translúcido com o Texto do Versículo
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(
            Brush.linearGradient(
              listOf(
                GoldSurfaceVariant.copy(alpha = 0.85f),
                GoldSurface.copy(alpha = 0.95f)
              )
            )
          )
          .border(1.dp, GoldBorder.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
          .padding(16.dp)
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(GoldAmber)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "VERSÍCULO",
              color = GoldAmber,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = verseAnnotated,
            color = GoldChampagne,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontFamily = FontFamily.Serif
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Título da Seção de Comentário Espiritual
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "✦ Comentário & Estudo Espiritual",
          color = GoldRadiant,
          fontSize = 16.sp,
          fontWeight = FontWeight.SemiBold,
          fontFamily = FontFamily.Serif
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Comentários
      if (comments.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GoldSurface.copy(alpha = 0.6f))
            .border(1.dp, GoldBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(16.dp)
        ) {
          Text(
            text = "Nenhum comentário específico encontrado para este versículo.",
            color = GoldTextSecondary,
            fontSize = 14.sp
          )
        }
      } else {
        comments.forEachIndexed { index, comment ->
          val commentAnnotated = remember(comment.text) {
            parseHtmlToAnnotatedString(comment.text)
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 6.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(GoldSurface)
              .border(1.dp, GoldBorder.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
              .padding(16.dp)
          ) {
            Column {
              if (comments.size > 1) {
                Text(
                  text = "Nota ${index + 1}",
                  color = GoldAmber,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
              }
              Text(
                text = commentAnnotated,
                color = GoldTextPrimary,
                fontSize = 15.sp,
                lineHeight = 23.sp,
                fontFamily = FontFamily.Default
              )
            }
          }
        }
      }
    }
  }
}
