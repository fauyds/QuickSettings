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

val MonetTertiary = Color(0xFFCF889A)
val MonetOnTertiary = Color(0xFF4A1828)
val MonetTertiaryContainer = Color(0xFF4A2330)
val MonetOnTertiaryContainer = Color(0xFFF2D3DC)

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
 * Helper to brighten a Color by 10% in dark mode, and 5% in light mode.
 */
fun brightenMonetPanelColor(color: Color, isDark: Boolean): Color {
    val percent = if (isDark) 0.10f else 0.05f
    val factor = 1f + percent
    val scaledR = (color.red * factor).coerceIn(0f, 1f)
    val scaledG = (color.green * factor).coerceIn(0f, 1f)
    val scaledB = (color.blue * factor).coerceIn(0f, 1f)
    val blendR = (color.red + (1f - color.red) * percent).coerceIn(0f, 1f)
    val blendG = (color.green + (1f - color.green) * percent).coerceIn(0f, 1f)
    val blendB = (color.blue + (1f - color.blue) * percent).coerceIn(0f, 1f)
    return Color(
        red = maxOf(scaledR, blendR),
        green = maxOf(scaledG, blendG),
        blue = maxOf(scaledB, blendB),
        alpha = color.alpha
    )
}

fun applyHsvAdjustments(color: Color, hueShift: Float, satScale: Float, brightScale: Float): Color {
    if (hueShift == 0f && satScale == 1f && brightScale == 1f) return color
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(
        android.graphics.Color.argb(
            (color.alpha * 255).toInt().coerceIn(0, 255),
            (color.red * 255).toInt().coerceIn(0, 255),
            (color.green * 255).toInt().coerceIn(0, 255),
            (color.blue * 255).toInt().coerceIn(0, 255)
        ),
        hsv
    )
    hsv[0] = (hsv[0] + hueShift).let { h ->
        var mod = h % 360f
        if (mod < 0) mod += 360f
        mod
    }
    hsv[1] = (hsv[1] * satScale).coerceIn(0f, 1f)
    hsv[2] = (hsv[2] * brightScale).coerceIn(0f, 1f)
    val intColor = android.graphics.Color.HSVToColor((color.alpha * 255).toInt().coerceIn(0, 255), hsv)
    return Color(intColor)
}

/**
 * 1. Quick Settings Monet Surface Background (rendered behind cards when enabled).
 */
/**
 * 1. Quick Settings Monet Surface Background (rendered behind cards when enabled).
 */
@Composable
fun getQsMonetSurfaceBackgroundColor(isDark: Boolean): Color {
    val useCustom = PanelPreferences.getSurfaceBgUseCustom(isDark).collectAsState().value
    if (useCustom) {
        val customColor = PanelPreferences.getSurfaceBgCustomColor(isDark).collectAsState().value
        return Color(customColor)
    }
    val customColor = PanelPreferences.qsPanelBackgroundColor.collectAsState().value
        ?: PanelPreferences.qsBackgroundColor.collectAsState().value
    val mode = PanelPreferences.getSurfaceBgColorMode(isDark).collectAsState().value
    val baseColor = if (customColor != null) {
        Color(customColor)
    } else {
        val scheme = androidx.compose.material3.MaterialTheme.colorScheme
        when (mode) {
            com.example.data.ComplicationArcMonetMode.PRIMARY -> scheme.primaryContainer
            com.example.data.ComplicationArcMonetMode.SECONDARY -> scheme.secondaryContainer
            com.example.data.ComplicationArcMonetMode.TERTIARY -> scheme.tertiaryContainer
            com.example.data.ComplicationArcMonetMode.DEFAULT -> scheme.surfaceVariant
        }
    }
    val factor = if (isDark) 0.90f else 0.87f
    val solidBg = Color(
        red = (baseColor.red * factor).coerceIn(0f, 1f),
        green = (baseColor.green * factor).coerceIn(0f, 1f),
        blue = (baseColor.blue * factor).coerceIn(0f, 1f),
        alpha = 1.0f
    )
    val hue = PanelPreferences.getSurfaceBgHue(isDark).collectAsState().value
    val sat = PanelPreferences.getSurfaceBgSat(isDark).collectAsState().value
    val valFactor = PanelPreferences.getSurfaceBgVal(isDark).collectAsState().value
    return applyHsvAdjustments(solidBg, hue, sat, valFactor)
}

/**
 * 2. Quick Settings Card Container Color (`getQsCardContainerColor` / `getQsPanelBackgroundColor`).
 */
@Composable
fun getQsCardContainerColor(isDark: Boolean): Color {
    val useCustom = PanelPreferences.getPanelContainerUseCustom(isDark).collectAsState().value
    if (useCustom) {
        val customColor = PanelPreferences.getPanelContainerCustomColor(isDark).collectAsState().value
        return Color(customColor)
    }
    val mode = PanelPreferences.getPanelContainerColorMode(isDark).collectAsState().value
    val scheme = androidx.compose.material3.MaterialTheme.colorScheme
    return when (mode) {
        com.example.data.ComplicationArcMonetMode.PRIMARY -> scheme.primaryContainer
        com.example.data.ComplicationArcMonetMode.SECONDARY -> scheme.secondaryContainer
        com.example.data.ComplicationArcMonetMode.TERTIARY -> scheme.tertiaryContainer
        com.example.data.ComplicationArcMonetMode.DEFAULT -> scheme.surfaceVariant
    }
}

/**
 * Quick Settings Panel Background Color (the panel itself).
 * When "Solid Monet Surface" is enabled, brightened 10% in dark mode and 5% in light mode.
 */
@Composable
fun getQsPanelBackgroundColor(isDark: Boolean): Color {
    val useCustom = PanelPreferences.getPanelContainerUseCustom(isDark).collectAsState().value
    if (useCustom) {
        val customColor = PanelPreferences.getPanelContainerCustomColor(isDark).collectAsState().value
        return Color(customColor)
    }
    val customColor = PanelPreferences.qsPanelBackgroundColor.collectAsState().value
        ?: PanelPreferences.qsBackgroundColor.collectAsState().value
    val baseColor = if (customColor != null) {
        Color(customColor)
    } else {
        getQsCardContainerColor(isDark)
    }
    val isSolidMonet = PanelPreferences.showMonetSurfaceBackground.collectAsState().value
    val panelColor = if (isSolidMonet) {
        brightenMonetPanelColor(baseColor, isDark)
    } else {
        baseColor
    }
    val hue = PanelPreferences.getPanelContainerHue(isDark).collectAsState().value
    val sat = PanelPreferences.getPanelContainerSat(isDark).collectAsState().value
    val valFactor = PanelPreferences.getPanelContainerVal(isDark).collectAsState().value
    return applyHsvAdjustments(panelColor, hue, sat, valFactor)
}

/**
 * 3. Quick Settings Notification Container Color.
 * Exact same color as the panel itself (brightened 10% in dark mode and 5% in light mode
 * when "Solid Monet Surface" is enabled).
 */
@Composable
fun getQsNotificationContainerColor(isDark: Boolean): Color {
    return getQsPanelBackgroundColor(isDark)
}

@Composable
fun getQsTilesElementColor(isDark: Boolean): Color {
    val useCustom = PanelPreferences.getQsTilesUseCustom(isDark).collectAsState().value
    if (useCustom) {
        val customColor = PanelPreferences.getQsTilesCustomColor(isDark).collectAsState().value
        return Color(customColor)
    }
    val mode = PanelPreferences.getQsTilesColorMode(isDark).collectAsState().value
    val scheme = androidx.compose.material3.MaterialTheme.colorScheme
    val baseColor = when (mode) {
        com.example.data.ComplicationArcMonetMode.SECONDARY -> scheme.secondary
        com.example.data.ComplicationArcMonetMode.TERTIARY -> scheme.tertiary
        com.example.data.ComplicationArcMonetMode.PRIMARY,
        com.example.data.ComplicationArcMonetMode.DEFAULT -> scheme.primary
    }
    val hue = PanelPreferences.getQsTilesHue(isDark).collectAsState().value
    val sat = PanelPreferences.getQsTilesSat(isDark).collectAsState().value
    val valFactor = PanelPreferences.getQsTilesVal(isDark).collectAsState().value
    return applyHsvAdjustments(baseColor, hue, sat, valFactor)
}

@Composable
fun getQsTilesOnElementColor(isDark: Boolean): Color {
    val useCustom = PanelPreferences.getQsTilesUseCustom(isDark).collectAsState().value
    if (useCustom) {
        val customInt = PanelPreferences.getQsTilesCustomColor(isDark).collectAsState().value
        return if (androidx.core.graphics.ColorUtils.calculateLuminance(customInt) > 0.45) {
            Color(0xFF1A1A1A)
        } else {
            Color.White
        }
    }
    val mode = PanelPreferences.getQsTilesColorMode(isDark).collectAsState().value
    val scheme = androidx.compose.material3.MaterialTheme.colorScheme
    return when (mode) {
        com.example.data.ComplicationArcMonetMode.SECONDARY -> scheme.onSecondary
        com.example.data.ComplicationArcMonetMode.TERTIARY -> scheme.onTertiary
        com.example.data.ComplicationArcMonetMode.PRIMARY,
        com.example.data.ComplicationArcMonetMode.DEFAULT -> scheme.onPrimary
    }
}

@Composable
fun getInactiveTilesElementColor(isDark: Boolean): Color {
    val useCustom = PanelPreferences.getInactiveTilesUseCustom(isDark).collectAsState().value
    if (useCustom) {
        val customColor = PanelPreferences.getInactiveTilesCustomColor(isDark).collectAsState().value
        return Color(customColor)
    }
    val mode = PanelPreferences.getInactiveTilesColorMode(isDark).collectAsState().value
    val scheme = androidx.compose.material3.MaterialTheme.colorScheme
    val cardContainerColor = scheme.surfaceVariant
    val defaultInactiveBg = if (isDark) {
        Color(
            red = (cardContainerColor.red * 0.72f).coerceIn(0f, 1f),
            green = (cardContainerColor.green * 0.72f).coerceIn(0f, 1f),
            blue = (cardContainerColor.blue * 0.72f).coerceIn(0f, 1f),
            alpha = cardContainerColor.alpha
        )
    } else {
        Color.White
    }
    val baseColor = when (mode) {
        com.example.data.ComplicationArcMonetMode.PRIMARY -> scheme.primaryContainer
        com.example.data.ComplicationArcMonetMode.SECONDARY -> scheme.secondaryContainer
        com.example.data.ComplicationArcMonetMode.TERTIARY -> scheme.tertiaryContainer
        com.example.data.ComplicationArcMonetMode.DEFAULT -> defaultInactiveBg
    }
    val hue = PanelPreferences.getInactiveTilesHue(isDark).collectAsState().value
    val sat = PanelPreferences.getInactiveTilesSat(isDark).collectAsState().value
    val valFactor = PanelPreferences.getInactiveTilesVal(isDark).collectAsState().value
    return applyHsvAdjustments(baseColor, hue, sat, valFactor)
}

@Composable
fun getSlidersElementColor(isDark: Boolean): Color {
    val useCustom = PanelPreferences.getSlidersUseCustom(isDark).collectAsState().value
    if (useCustom) {
        val customColor = PanelPreferences.getSlidersCustomColor(isDark).collectAsState().value
        return Color(customColor)
    }
    val mode = PanelPreferences.getSlidersColorMode(isDark).collectAsState().value
    val scheme = androidx.compose.material3.MaterialTheme.colorScheme
    val baseColor = when (mode) {
        com.example.data.ComplicationArcMonetMode.SECONDARY -> scheme.secondary
        com.example.data.ComplicationArcMonetMode.TERTIARY -> scheme.tertiary
        com.example.data.ComplicationArcMonetMode.PRIMARY,
        com.example.data.ComplicationArcMonetMode.DEFAULT -> scheme.primary
    }
    val hue = PanelPreferences.getSlidersHue(isDark).collectAsState().value
    val sat = PanelPreferences.getSlidersSat(isDark).collectAsState().value
    val valFactor = PanelPreferences.getSlidersVal(isDark).collectAsState().value
    return applyHsvAdjustments(baseColor, hue, sat, valFactor)
}

@Composable
fun getHeaderGradientElementColor(isDark: Boolean): Color {
    val useCustom = PanelPreferences.getHeaderGradientUseCustom(isDark).collectAsState().value
    if (useCustom) {
        val customColor = PanelPreferences.getHeaderGradientCustomColor(isDark).collectAsState().value
        return Color(customColor)
    }
    val mode = PanelPreferences.getHeaderGradientColorMode(isDark).collectAsState().value
    val scheme = androidx.compose.material3.MaterialTheme.colorScheme
    val baseColor = if (isDark) {
        when (mode) {
            com.example.data.ComplicationArcMonetMode.SECONDARY -> scheme.secondaryContainer
            com.example.data.ComplicationArcMonetMode.TERTIARY -> scheme.tertiaryContainer
            com.example.data.ComplicationArcMonetMode.PRIMARY,
            com.example.data.ComplicationArcMonetMode.DEFAULT -> scheme.primaryContainer
        }
    } else {
        when (mode) {
            com.example.data.ComplicationArcMonetMode.SECONDARY -> scheme.secondary
            com.example.data.ComplicationArcMonetMode.TERTIARY -> scheme.tertiary
            com.example.data.ComplicationArcMonetMode.PRIMARY,
            com.example.data.ComplicationArcMonetMode.DEFAULT -> scheme.primary
        }
    }
    val hue = PanelPreferences.getHeaderGradientHue(isDark).collectAsState().value
    val sat = PanelPreferences.getHeaderGradientSat(isDark).collectAsState().value
    val valFactor = PanelPreferences.getHeaderGradientVal(isDark).collectAsState().value
    return applyHsvAdjustments(baseColor, hue, sat, valFactor)
}

@Composable
fun getMiscElementColor(isDark: Boolean): Color {
    val useCustom = PanelPreferences.getMiscElementsUseCustom(isDark).collectAsState().value
    if (useCustom) {
        val customColor = PanelPreferences.getMiscElementsCustomColor(isDark).collectAsState().value
        return Color(customColor)
    }
    val mode = PanelPreferences.getMiscElementsColorMode(isDark).collectAsState().value
    val scheme = androidx.compose.material3.MaterialTheme.colorScheme
    val baseColor = when (mode) {
        com.example.data.ComplicationArcMonetMode.SECONDARY -> scheme.secondary
        com.example.data.ComplicationArcMonetMode.TERTIARY -> scheme.tertiary
        com.example.data.ComplicationArcMonetMode.PRIMARY,
        com.example.data.ComplicationArcMonetMode.DEFAULT -> scheme.primary
    }
    val hue = PanelPreferences.getMiscElementsHue(isDark).collectAsState().value
    val sat = PanelPreferences.getMiscElementsSat(isDark).collectAsState().value
    val valFactor = PanelPreferences.getMiscElementsVal(isDark).collectAsState().value
    return applyHsvAdjustments(baseColor, hue, sat, valFactor)
}

@Composable
fun getClearAllElementColor(isDark: Boolean): Color {
    val useCustom = PanelPreferences.getClearAllUseCustom(isDark).collectAsState().value
    if (useCustom) {
        val customColor = PanelPreferences.getClearAllCustomColor(isDark).collectAsState().value
        return Color(customColor)
    }
    val mode = PanelPreferences.getClearAllColorMode(isDark).collectAsState().value
    val scheme = androidx.compose.material3.MaterialTheme.colorScheme
    val baseColor = when (mode) {
        com.example.data.ComplicationArcMonetMode.SECONDARY -> scheme.secondary
        com.example.data.ComplicationArcMonetMode.TERTIARY -> scheme.tertiary
        com.example.data.ComplicationArcMonetMode.PRIMARY,
        com.example.data.ComplicationArcMonetMode.DEFAULT -> scheme.primary
    }
    val hue = PanelPreferences.getClearAllHue(isDark).collectAsState().value
    val sat = PanelPreferences.getClearAllSat(isDark).collectAsState().value
    val valFactor = PanelPreferences.getClearAllVal(isDark).collectAsState().value
    return applyHsvAdjustments(baseColor, hue, sat, valFactor)
}

@Composable
fun resolveActiveComplicationArcOverride(isDark: Boolean): Color? {
    val useCustom = PanelPreferences.getActiveCompArcUseCustom(isDark).collectAsState().value
    if (useCustom) {
        val customColor = PanelPreferences.getActiveCompArcCustomColor(isDark).collectAsState().value
        return Color(customColor)
    }
    val mode = PanelPreferences.getActiveCompArcMode(isDark).collectAsState().value
    val hue = PanelPreferences.getActiveCompArcHue(isDark).collectAsState().value
    val sat = PanelPreferences.getActiveCompArcSat(isDark).collectAsState().value
    val valFactor = PanelPreferences.getActiveCompArcVal(isDark).collectAsState().value
    if (mode == com.example.data.ComplicationArcMonetMode.DEFAULT &&
        hue == 0f && sat == 1f && valFactor == 1f
    ) {
        return null
    }
    val baseColor = when (mode) {
        com.example.data.ComplicationArcMonetMode.PRIMARY -> androidx.compose.material3.MaterialTheme.colorScheme.primary
        com.example.data.ComplicationArcMonetMode.SECONDARY -> androidx.compose.material3.MaterialTheme.colorScheme.secondary
        com.example.data.ComplicationArcMonetMode.TERTIARY -> androidx.compose.material3.MaterialTheme.colorScheme.tertiary
        com.example.data.ComplicationArcMonetMode.DEFAULT -> androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
    }
    return applyHsvAdjustments(baseColor, hue, sat, valFactor)
}

@Composable
fun resolveInactiveComplicationArcOverride(isDark: Boolean): Color? {
    val useCustom = PanelPreferences.getInactiveCompArcUseCustom(isDark).collectAsState().value
    if (useCustom) {
        val customColor = PanelPreferences.getInactiveCompArcCustomColor(isDark).collectAsState().value
        return Color(customColor)
    }
    val mode = PanelPreferences.getInactiveCompArcMode(isDark).collectAsState().value
    val hue = PanelPreferences.getInactiveCompArcHue(isDark).collectAsState().value
    val sat = PanelPreferences.getInactiveCompArcSat(isDark).collectAsState().value
    val valFactor = PanelPreferences.getInactiveCompArcVal(isDark).collectAsState().value
    if (mode == com.example.data.ComplicationArcMonetMode.DEFAULT &&
        hue == 0f && sat == 1f && valFactor == 1f
    ) {
        return null
    }
    val baseColor = when (mode) {
        com.example.data.ComplicationArcMonetMode.PRIMARY -> androidx.compose.material3.MaterialTheme.colorScheme.primary
        com.example.data.ComplicationArcMonetMode.SECONDARY -> androidx.compose.material3.MaterialTheme.colorScheme.secondary
        com.example.data.ComplicationArcMonetMode.TERTIARY -> androidx.compose.material3.MaterialTheme.colorScheme.tertiary
        com.example.data.ComplicationArcMonetMode.DEFAULT -> if (isDark) Color(0xFFCCCCCC) else Color(0xFF555555)
    }
    return applyHsvAdjustments(baseColor, hue, sat, valFactor)
}

@Composable
fun resolveComplicationArcTrackOverride(isDark: Boolean): Color? {
    val useCustom = PanelPreferences.getCompArcTrackUseCustom(isDark).collectAsState().value
    if (useCustom) {
        val customColor = PanelPreferences.getCompArcTrackCustomColor(isDark).collectAsState().value
        return Color(customColor)
    }
    val mode = PanelPreferences.getCompArcTrackMode(isDark).collectAsState().value
    val hue = PanelPreferences.getCompArcTrackHue(isDark).collectAsState().value
    val sat = PanelPreferences.getCompArcTrackSat(isDark).collectAsState().value
    val valFactor = PanelPreferences.getCompArcTrackVal(isDark).collectAsState().value
    if (mode == com.example.data.ComplicationArcMonetMode.DEFAULT &&
        hue == 0f && sat == 1f && valFactor == 1f
    ) {
        return null
    }
    val baseColor = when (mode) {
        com.example.data.ComplicationArcMonetMode.PRIMARY -> androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
        com.example.data.ComplicationArcMonetMode.SECONDARY -> androidx.compose.material3.MaterialTheme.colorScheme.secondary.copy(alpha = 0.30f)
        com.example.data.ComplicationArcMonetMode.TERTIARY -> androidx.compose.material3.MaterialTheme.colorScheme.tertiary.copy(alpha = 0.30f)
        com.example.data.ComplicationArcMonetMode.DEFAULT -> (if (isDark) Color.White else Color.Black).copy(alpha = 0.22f)
    }
    return applyHsvAdjustments(baseColor, hue, sat, valFactor)
}

@Composable
fun getActiveComplicationArcPreviewColor(isDark: Boolean): Color {
    return resolveActiveComplicationArcOverride(isDark)
        ?: androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
}

@Composable
fun getInactiveComplicationArcPreviewColor(isDark: Boolean): Color {
    return resolveInactiveComplicationArcOverride(isDark)
        ?: if (isDark) Color(0xFFCCCCCC) else Color(0xFF555555)
}

@Composable
fun getComplicationArcTrackPreviewColor(isDark: Boolean): Color {
    return resolveComplicationArcTrackOverride(isDark)
        ?: (if (isDark) Color.White.copy(alpha = 0.28f) else Color.Black.copy(alpha = 0.22f))
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


