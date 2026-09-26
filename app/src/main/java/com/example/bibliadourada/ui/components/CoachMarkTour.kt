package com.example.bibliadourada.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bibliadourada.theme.GoldAmber
import com.example.bibliadourada.theme.GoldImperial
import com.example.bibliadourada.theme.GoldSurface
import com.example.bibliadourada.theme.GoldTextPrimary
import com.example.bibliadourada.theme.GoldTextSecondary

/** Um passo do tour: destaca um elemento e explica para que ele serve. */
data class TourStep(
  /** Chave registrada por [tourTarget]; `null` mostra um cartão centralizado. */
  val targetKey: String?,
  val title: String,
  val description: String
)

/** Guarda a posição na tela de cada elemento que o tour pode destacar. */
class TourTargetRegistry {
  val bounds = mutableStateMapOf<String, Rect>()
}

val LocalTourTargets = staticCompositionLocalOf { TourTargetRegistry() }

/**
 * Registra este elemento como alvo do tour, guardando seus limites na raiz
 * da composição.
 */
@Composable
fun Modifier.tourTarget(key: String): Modifier {
  val registry = LocalTourTargets.current
  return this.onGloballyPositioned { coordinates ->
    val rect = coordinates.boundsInRoot()
    // Só escreve quando muda, para não animar recomposições à toa.
    if (registry.bounds[key] != rect) {
      registry.bounds[key] = rect
    }
  }
}

/**
 * Tour de primeiro acesso: escurece a tela, recorta o elemento em foco e
 * mostra uma explicação curta. Bloqueia o toque no app enquanto está ativo.
 */
@Composable
fun CoachMarkTour(
  steps: List<TourStep>,
  onFinish: () -> Unit
) {
  if (steps.isEmpty()) return

  var index by remember { mutableIntStateOf(0) }
  val registry = LocalTourTargets.current
  val density = LocalDensity.current
  val step = steps[index.coerceIn(0, steps.lastIndex)]
  val target = step.targetKey?.let { registry.bounds[it] }

  val scrimColor = Color.Black.copy(alpha = 0.78f)
  // Cores lidas aqui: dentro do Canvas (não-composable) só valores já capturados.
  val focusBorderColor = GoldImperial
  val gapPx = with(density) { 14.dp.toPx() }

  fun advance() {
    if (index < steps.lastIndex) index++ else onFinish()
  }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = { advance() }
      )
  ) {
    val heightPx = constraints.maxHeight.toFloat()

    Canvas(modifier = Modifier.fillMaxSize()) {
      if (target == null) {
        drawRect(scrimColor)
        return@Canvas
      }
      val pad = 8.dp.toPx()
      val focus = Rect(
        left = (target.left - pad).coerceAtLeast(0f),
        top = (target.top - pad).coerceAtLeast(0f),
        right = (target.right + pad).coerceAtMost(size.width),
        bottom = (target.bottom + pad).coerceAtMost(size.height)
      )

      // Quatro retângulos ao redor do foco — evita mistura de camadas.
      drawRect(scrimColor, size = Size(size.width, focus.top))
      drawRect(
        scrimColor,
        topLeft = Offset(0f, focus.bottom),
        size = Size(size.width, (size.height - focus.bottom).coerceAtLeast(0f))
      )
      drawRect(
        scrimColor,
        topLeft = Offset(0f, focus.top),
        size = Size(focus.left, focus.height)
      )
      drawRect(
        scrimColor,
        topLeft = Offset(focus.right, focus.top),
        size = Size((size.width - focus.right).coerceAtLeast(0f), focus.height)
      )

      drawRoundRect(
        color = focusBorderColor,
        topLeft = Offset(focus.left, focus.top),
        size = Size(focus.width, focus.height),
        cornerRadius = CornerRadius(16.dp.toPx()),
        style = Stroke(width = 2.dp.toPx())
      )
    }

    // O cartão fica acima do alvo quando ele está na metade de baixo da tela.
    val cardPadding = when {
      target == null -> PaddingValues(horizontal = 24.dp)
      target.center.y > heightPx * 0.6f -> PaddingValues(
        start = 20.dp,
        end = 20.dp,
        bottom = with(density) { (heightPx - target.top + gapPx).toDp() }
      )
      else -> PaddingValues(
        start = 20.dp,
        end = 20.dp,
        top = with(density) { (target.bottom + gapPx).toDp() }
      )
    }

    val alignment = when {
      target == null -> Alignment.Center
      target.center.y > heightPx * 0.6f -> Alignment.BottomCenter
      else -> Alignment.TopCenter
    }

    Column(
      modifier = Modifier
        .align(alignment)
        .padding(cardPadding)
        .clip(RoundedCornerShape(18.dp))
        .background(GoldSurface)
        .border(1.dp, GoldAmber.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
        // Consome o toque para não avançar ao interagir com o cartão.
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = {}
        )
        .padding(18.dp)
    ) {
      Text(
        text = "PASSO ${index + 1} DE ${steps.size}",
        color = GoldAmber,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = step.title,
        color = GoldTextPrimary,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Serif
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = step.description,
        color = GoldTextSecondary,
        fontSize = 13.sp,
        lineHeight = 19.sp
      )
      Spacer(modifier = Modifier.height(16.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Pular tour",
          color = GoldTextSecondary,
          fontSize = 13.sp,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
              onClick = { onFinish() }
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Button(
          onClick = { advance() },
          colors = ButtonDefaults.buttonColors(
            containerColor = GoldImperial,
            contentColor = Color(0xFF1F1600)
          ),
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
        ) {
          Text(
            text = if (index == steps.lastIndex) "Concluir" else "Avançar",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        }
      }
    }
  }
}
