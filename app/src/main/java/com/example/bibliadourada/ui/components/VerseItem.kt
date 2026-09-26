package com.example.bibliadourada.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bibliadourada.data.model.Verse
import com.example.bibliadourada.theme.GoldAmber
import com.example.bibliadourada.theme.GoldBadge
import com.example.bibliadourada.theme.GoldBorder
import com.example.bibliadourada.theme.GoldImperial
import com.example.bibliadourada.theme.GoldMuted
import com.example.bibliadourada.theme.GoldSurfaceVariant
import com.example.bibliadourada.theme.GoldTextPrimary

@Composable
fun VerseItem(
  verse: Verse,
  fontSize: TextUnit = 18.sp,
  isSelected: Boolean = false,
  onVerseClick: (Verse) -> Unit = {},
  onCommentClick: (Verse) -> Unit = {}
) {
  val annotatedText = remember(verse.text) {
    parseHtmlToAnnotatedString(verse.text)
  }

  val backgroundColor = if (isSelected) {
    GoldSurfaceVariant.copy(alpha = 0.6f)
  } else {
    Color.Transparent
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(backgroundColor)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(color = GoldImperial.copy(alpha = 0.2f)),
        onClick = {
          if (verse.hasComment) {
            onCommentClick(verse)
          } else {
            onVerseClick(verse)
          }
        }
      )
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
      ) {
        // Número do versículo em tom dourado suave
        Text(
          text = "${verse.number}",
          color = GoldAmber,
          fontSize = (fontSize.value * 0.75f).sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Serif,
          modifier = Modifier
            .padding(top = 2.dp, end = 10.dp)
            .width(28.dp)
        )

        // Texto do versículo com formatação HTML renderizada
        Text(
          text = annotatedText,
          color = GoldTextPrimary,
          fontSize = fontSize,
          lineHeight = (fontSize.value * 1.55f).sp,
          fontFamily = FontFamily.Serif,
          modifier = Modifier.weight(1f)
        )
      }

      // Badge dourado brilhante de estudo se houver comentário
      if (verse.hasComment) {
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier
            .padding(start = 38.dp)
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(GoldSurfaceVariant)
              .border(0.8.dp, GoldBorder.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = GoldBadge.copy(alpha = 0.3f)),
                onClick = { onCommentClick(verse) }
              )
              .padding(horizontal = 10.dp, vertical = 3.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "✦",
                color = GoldBadge,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Estudo",
                color = GoldBadge,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
              )
            }
          }
        }
      }
    }
  }
}
