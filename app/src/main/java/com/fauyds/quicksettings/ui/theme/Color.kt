package com.fauyds.quicksettings.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Expressive Dark Palette with rich Monet containers
val DarkUiBackground = Color.Black.copy(alpha = 0.95f)
val DarkUiSurface = Color(0xFF132223)
val DarkUiSurfaceVariant = Color(0xFF1B2E2E)
val DarkUiSurfaceContainerHigh = Color(0xFF243B3B)

// Expressive Light Palette (Monet Tinted)
val LightUiBackground = Color(0xFFEBF3F2)
val LightUiSurface = Color(0xFFE4F0EF)
val LightUiSurfaceVariant = Color(0xFFDCECEB)
val LightUiSurfaceContainerHigh = Color(0xFFD8E8E7)
val LightUiSurfaceContainer = Color(0xFFE4F0EF)
val LightUiSurfaceContainerLow = Color(0xFFE8F3F2)
val LightUiSurfaceContainerLowest = Color(0xFFF2F8F7)
val LightUiSurfaceContainerHighest = Color(0xFFD0E2E1)

// Backward compatible aliases
val MonetSurface = DarkUiSurface
val MonetSurfaceVariant = DarkUiSurfaceVariant

// Monet Expressive Accents (Primary Cyan/Teal & Tertiary Rose/Pink/Violet)
val MonetPrimary = Color(0xFF4EE3D0)
val MonetOnPrimary = Color(0xFF003732)
val MonetPrimaryContainer = Color(0xFF005049)
val MonetOnPrimaryContainer = Color(0xFF70FFF0)

val DarkMonetPrimary = Color(0xFF4EE3D0)
val DarkMonetOnPrimary = Color(0xFF003732)
val DarkMonetPrimaryContainer = Color(0xFF005049)
val DarkMonetOnPrimaryContainer = Color(0xFF70FFF0)

val LightMonetPrimary = Color(0xFF006A60)
val LightMonetOnPrimary = Color(0xFFFFFFFF)
val LightMonetPrimaryContainer = Color(0xFF73F8E5)
val LightMonetOnPrimaryContainer = Color(0xFF00201C)

val MonetSecondary = Color(0xFF7DD3FC)
val MonetOnSecondary = Color(0xFF003548)
val MonetSecondaryContainer = Color(0xFF004D67)

val MonetTertiary = Color(0xFFFF85A1)
val MonetOnTertiary = Color(0xFF5D0026)
val MonetTertiaryContainer = Color(0xFF830038)
val MonetOnTertiaryContainer = Color(0xFFFFD9E1)

// Functional indicators - Dark Mode
val TileActiveBg = Color(0xFF4EE3D0)
val TileActiveText = Color(0xFF003732)
val TileInactiveBg = Color(0xFF232A36)
val TileInactiveText = Color(0xFFD6DFEC)
val TileUnavailableBg = Color(0xFF161A22)
val TileUnavailableText = Color(0xFF6B7280)

// Functional indicators - Light Mode
val LightTileActiveBg = Color(0xFF006A60)
val LightTileActiveText = Color(0xFFFFFFFF)
val LightTileInactiveBg = Color(0xFFE2E8F0)
val LightTileInactiveText = Color(0xFF1E293B)
val LightTileUnavailableBg = Color(0xFFF1F5F9)
val LightTileUnavailableText = Color(0xFF94A3B8)

// Slider & Glow Gradients
val AccentGradientStart = Color(0xFF4EE3D0)
val AccentGradientEnd = Color(0xFFFF85A1)

// Explicit Decoupled Component Colors

/**
 * 1. Quick Settings Overlay Panel Background (`qsPanelBackgroundColor`).
 * Uses adaptive Monet surfaceVariant in both dark and light modes.
 */
@Composable
fun getQsPanelBackgroundColor(isDark: Boolean): Color {
    return androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
}

/**
 * 2. Settings Root Screen / Scaffold Background (`settingsScreenBackgroundColor`).
 * Light: Gray 7% (#ECECEC), Dark: #121212
 */
fun getSettingsScreenBackgroundColor(isDark: Boolean): Color {
    return if (isDark) Color(0xFF121212) else Color(0xFFECECEC)
}

/**
 * 3. Settings Card Container Color (`settingsCardBackgroundColor`).
 * Light: Totally white (#FFFFFF), Dark: #1E1E1E
 */
fun getSettingsCardBackgroundColor(isDark: Boolean): Color {
    return if (isDark) Color(0xFF1E1E1E) else Color.White
}

/**
 * 4. Non-Granted / Inactive Solid Red Color.
 * Light: #C62828, Dark: #B71C1C
 */
fun getNonGrantedRedColor(isDark: Boolean): Color {
    return if (isDark) Color(0xFFB71C1C) else Color(0xFFC62828)
}


