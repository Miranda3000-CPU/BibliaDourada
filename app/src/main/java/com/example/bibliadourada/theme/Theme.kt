package com.example.bibliadourada.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkGoldColorScheme = darkColorScheme(
  primary = DarkBibliaPalette.imperial,
  onPrimary = Color(0xFF1F1600),
  primaryContainer = DarkBibliaPalette.surfaceVariant,
  onPrimaryContainer = DarkBibliaPalette.radiant,
  secondary = DarkBibliaPalette.amber,
  onSecondary = Color(0xFF1F1600),
  secondaryContainer = Color(0xFF2D2314),
  onSecondaryContainer = DarkBibliaPalette.champagne,
  tertiary = DarkBibliaPalette.radiant,
  onTertiary = Color(0xFF1F1600),
  tertiaryContainer = Color(0xFF382C15),
  onTertiaryContainer = DarkBibliaPalette.champagne,
  background = DarkBibliaPalette.background,
  onBackground = DarkBibliaPalette.textPrimary,
  surface = DarkBibliaPalette.surface,
  onSurface = DarkBibliaPalette.textPrimary,
  surfaceVariant = DarkBibliaPalette.surfaceVariant,
  onSurfaceVariant = DarkBibliaPalette.textSecondary,
  outline = DarkBibliaPalette.border,
  outlineVariant = Color(0xFF3A2E16),
  scrim = Color(0xFF0A0805),
  inverseSurface = DarkBibliaPalette.champagne,
  inverseOnSurface = DarkBibliaPalette.background,
  inversePrimary = DarkBibliaPalette.amber
)

private val LightGoldColorScheme = lightColorScheme(
  primary = LightBibliaPalette.imperial,
  onPrimary = Color.White,
  primaryContainer = LightBibliaPalette.surfaceVariant,
  onPrimaryContainer = LightBibliaPalette.radiant,
  secondary = LightBibliaPalette.amber,
  onSecondary = Color.White,
  secondaryContainer = LightBibliaPalette.surfaceVariant,
  onSecondaryContainer = LightBibliaPalette.champagne,
  tertiary = LightBibliaPalette.radiant,
  onTertiary = Color.White,
  tertiaryContainer = LightBibliaPalette.cardElevated,
  onTertiaryContainer = LightBibliaPalette.champagne,
  background = LightBibliaPalette.background,
  onBackground = LightBibliaPalette.textPrimary,
  surface = LightBibliaPalette.surface,
  onSurface = LightBibliaPalette.textPrimary,
  surfaceVariant = LightBibliaPalette.surfaceVariant,
  onSurfaceVariant = LightBibliaPalette.textSecondary,
  outline = LightBibliaPalette.border,
  outlineVariant = Color(0xFFEFE3C4),
  scrim = Color(0x99000000),
  inverseSurface = LightBibliaPalette.champagne,
  inverseOnSurface = LightBibliaPalette.background,
  inversePrimary = LightBibliaPalette.imperial
)

/**
 * Tema do app. Suporta dois esquemas: claro ("white", padrão) e escuro.
 */
@Composable
fun BibliaDouradaTheme(
  darkTheme: Boolean = false,
  content: @Composable () -> Unit
) {
  val palette = if (darkTheme) DarkBibliaPalette else LightBibliaPalette
  val colorScheme = if (darkTheme) DarkGoldColorScheme else LightGoldColorScheme

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = palette.background.toArgb()
        window.navigationBarColor = palette.background.toArgb()
        val insetsController = WindowCompat.getInsetsController(window, view)
        // No tema claro os ícones da barra precisam ser escuros.
        insetsController.isAppearanceLightStatusBars = !darkTheme
        insetsController.isAppearanceLightNavigationBars = !darkTheme
      }
    }
  }

  CompositionLocalProvider(LocalBibliaPalette provides palette) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
    )
  }
}
