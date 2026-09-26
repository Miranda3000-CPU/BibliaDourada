package com.example.bibliadourada.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Paleta semântica do app. Cada tema (escuro ou claro) fornece uma instância,
 * publicada via [LocalBibliaPalette]. Assim os componentes continuam usando os
 * mesmos nomes (GoldImperial, GoldSurface, ...) mas as cores passam a reagir à
 * troca de tema sem precisar de mudanças nos call sites.
 */
data class BibliaPalette(
  val imperial: Color,
  val radiant: Color,
  val amber: Color,
  val champagne: Color,
  val background: Color,
  val surface: Color,
  val surfaceVariant: Color,
  val border: Color,
  val badge: Color,
  val textPrimary: Color,
  val textSecondary: Color,
  val muted: Color,
  val cardBackground: Color,
  val cardElevated: Color,
  val divider: Color,
  val highlight: Color
)

// ---------------------------------------------------------------------------
// Tema escuro — "Bíblia Dourada" original
// ---------------------------------------------------------------------------
val DarkBibliaPalette = BibliaPalette(
  imperial = Color(0xFFD4AF37),
  radiant = Color(0xFFE5C158),
  amber = Color(0xFFC29B38),
  champagne = Color(0xFFF5E8C7),
  background = Color(0xFF12100B),
  surface = Color(0xFF1C160E),
  surfaceVariant = Color(0xFF2A2114),
  border = Color(0xFF4D3B1B),
  badge = Color(0xFFFFD54F),
  textPrimary = Color(0xFFFAF4E6),
  textSecondary = Color(0xFFCDBFA3),
  muted = Color(0xFF8C7A58),
  cardBackground = Color(0xFF1E1810),
  cardElevated = Color(0xFF251E13),
  divider = Color(0x33D4AF37),
  highlight = Color(0x28D4AF37)
)

// ---------------------------------------------------------------------------
// Tema claro — "Bíblia Dourada White"
// Fundo branco quente, dourado escurecido para manter contraste legível.
// ---------------------------------------------------------------------------
val LightBibliaPalette = BibliaPalette(
  imperial = Color(0xFFA67C00),
  radiant = Color(0xFF8C6A0A),
  amber = Color(0xFF9A7614),
  champagne = Color(0xFF4A3A12),
  background = Color(0xFFFDFBF5),
  surface = Color(0xFFFFFFFF),
  surfaceVariant = Color(0xFFF4EDDC),
  border = Color(0xFFE2D3AB),
  badge = Color(0xFF8A6508),
  textPrimary = Color(0xFF2B2416),
  textSecondary = Color(0xFF5F5540),
  muted = Color(0xFF9A8F78),
  cardBackground = Color(0xFFFFFDF8),
  cardElevated = Color(0xFFFAF4E6),
  divider = Color(0x33A67C00),
  highlight = Color(0x28A67C00)
)

val LocalBibliaPalette = staticCompositionLocalOf { DarkBibliaPalette }

/** Acesso à paleta ativa a partir de qualquer composable. */
object BibliaTheme {
  val palette: BibliaPalette
    @Composable
    @ReadOnlyComposable
    get() = LocalBibliaPalette.current
}

// ---------------------------------------------------------------------------
// Aliases reativos — mantêm os nomes usados pelos componentes.
// ---------------------------------------------------------------------------
val GoldImperial: Color
  @Composable @ReadOnlyComposable get() = LocalBibliaPalette.current.imperial

val GoldRadiant: Color
  @Composable @ReadOnlyComposable get() = LocalBibliaPalette.current.radiant

val GoldAmber: Color
  @Composable @ReadOnlyComposable get() = LocalBibliaPalette.current.amber

val GoldChampagne: Color
  @Composable @ReadOnlyComposable get() = LocalBibliaPalette.current.champagne

val GoldDeepBackground: Color
  @Composable @ReadOnlyComposable get() = LocalBibliaPalette.current.background

val GoldSurface: Color
  @Composable @ReadOnlyComposable get() = LocalBibliaPalette.current.surface

val GoldSurfaceVariant: Color
  @Composable @ReadOnlyComposable get() = LocalBibliaPalette.current.surfaceVariant

val GoldBorder: Color
  @Composable @ReadOnlyComposable get() = LocalBibliaPalette.current.border

val GoldBadge: Color
  @Composable @ReadOnlyComposable get() = LocalBibliaPalette.current.badge

val GoldTextPrimary: Color
  @Composable @ReadOnlyComposable get() = LocalBibliaPalette.current.textPrimary

val GoldTextSecondary: Color
  @Composable @ReadOnlyComposable get() = LocalBibliaPalette.current.textSecondary

val GoldMuted: Color
  @Composable @ReadOnlyComposable get() = LocalBibliaPalette.current.muted

val GoldCardBackground: Color
  @Composable @ReadOnlyComposable get() = LocalBibliaPalette.current.cardBackground

val GoldCardElevated: Color
  @Composable @ReadOnlyComposable get() = LocalBibliaPalette.current.cardElevated

val GoldDivider: Color
  @Composable @ReadOnlyComposable get() = LocalBibliaPalette.current.divider

val GoldHighlight: Color
  @Composable @ReadOnlyComposable get() = LocalBibliaPalette.current.highlight
