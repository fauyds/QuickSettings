package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import com.example.data.PanelPreferences

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
 * 1. Quick Settings Monet Surface Background (rendered behind cards when enabled).
 * Uses the same color as the card containers, but 20% darker (8% darker than previous 12%).
 */
@Composable
fun getQsMonetSurfaceBackgroundColor(isDark: Boolean): Color {
    val enableMonetBg = PanelPreferences.showMonetSurfaceBackground.collectAsState().value
    val surfaceTertiary = PanelPreferences.surfaceBgColorTertiary.collectAsState().value
    val containerColor = if (surfaceTertiary) {
        androidx.compose.material3.MaterialTheme.colorScheme.tertiaryContainer
    } else {
        androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
    }
    return if (enableMonetBg) {
        val factor = if (isDark) 0.90f else 0.87f
        Color(
            red = (containerColor.red * factor).coerceIn(0f, 1f),
            green = (containerColor.green * factor).coerceIn(0f, 1f),
            blue = (containerColor.blue * factor).coerceIn(0f, 1f),
            alpha = 1.0f
        )
    } else {
        containerColor
    }
}

/**
 * 2. Quick Settings Card Container Color (`getQsCardContainerColor` / `getQsPanelBackgroundColor`).
 * Restores container colors for cards so cards remain distinct over the Monet surface.
 */
@Composable
fun getQsCardContainerColor(isDark: Boolean): Color {
    val panelTertiary = PanelPreferences.panelContainerColorTertiary.collectAsState().value
    return if (panelTertiary) {
        androidx.compose.material3.MaterialTheme.colorScheme.tertiaryContainer
    } else {
        androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
    }
}

@Composable
fun getQsPanelBackgroundColor(isDark: Boolean): Color {
    return getQsCardContainerColor(isDark)
}

/**
 * 3. Quick Settings Notification Container Color.
 * When monet on the surface behind everything is enabled in dark mode, lightens the notification containers
 * so notifications stand out clearly over the background.
 */
@Composable
fun getQsNotificationContainerColor(isDark: Boolean): Color {
    val enableMonetBg = PanelPreferences.showMonetSurfaceBackground.collectAsState().value
    val baseContainer = getQsPanelBackgroundColor(isDark)
    return if (enableMonetBg && isDark) {
        Color(
            red = (baseContainer.red * 1.50f).coerceIn(0f, 1f),
            green = (baseContainer.green * 1.50f).coerceIn(0f, 1f),
            blue = (baseContainer.blue * 1.50f).coerceIn(0f, 1f),
            alpha = 1.0f
        )
    } else {
        baseContainer
    }
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


