package com.example.ui.overlay

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.PanelPreferences
import com.example.data.ProgressBarStyle
import com.example.data.QsSliderTarget
import com.example.data.SavedTheme
import com.example.ui.components.QsProgressSlider
import com.example.ui.shape.QsShapeManager
import org.json.JSONObject

/**
 * Single Source of Truth configuration model for Quick Settings panel rendering.
 * Shared synchronously between live interactive settings preview and real-time system overlay.
 */
data class QsOverlaySurfaceConfig(
    val themeMode: String = "system",
    val panelOuterCorner: Float = 20f,
    val panelInnerCorner: Float = 12f,
    val panelGlobalCornerEnabled: Boolean = false,
    val enabledTileShapeId: String = "circle",
    val disabledTileShapeId: String = "circle",
    val tileOutlineEnabled: Boolean = false,
    val showQsTileGradient: Boolean = false,
    val progressBarStyle: ProgressBarStyle = ProgressBarStyle.DEFAULT,
    val brightnessSliderStyle: ProgressBarStyle = ProgressBarStyle.DEFAULT,
    val volumeSliderStyle: ProgressBarStyle = ProgressBarStyle.DEFAULT,
    val mediaSliderStyle: ProgressBarStyle = ProgressBarStyle.DEFAULT,
    val sliderSyncAll: Boolean = true,
    val showCustomHeaderImage: Boolean = false,
    val customHeaderImageUri: String = "",
    val customHeaderImageHeight: Float = 120f,
    val customHeaderImageOpacity: Float = 1.0f,
    val showCustomHeaderMonetGradient: Boolean = false,
    val customHeaderMonetGradientHeight: Float = 120f,
    val showMonetSurfaceBackground: Boolean = false,
    val headerGradientColorTertiary: Boolean = false,
    val slidersColorTertiary: Boolean = false,
    val slidersColorTertiaryLight: Boolean = false,
    val slidersColorTertiaryDark: Boolean = false,
    val slidersHue: Float = 0f,
    val slidersSat: Float = 1f,
    val slidersVal: Float = 1f,
    val slidersHueLight: Float = 0f,
    val slidersSatLight: Float = 1f,
    val slidersValLight: Float = 1f,
    val slidersHueDark: Float = 0f,
    val slidersSatDark: Float = 1f,
    val slidersValDark: Float = 1f,
    val qsTilesColorTertiary: Boolean = false,
    val surfaceBgColorTertiary: Boolean = false,
    val panelContainerColorTertiary: Boolean = false,
    val wavyThickness: Float = 6f,
    val wavyWobbliness: Float = 1.2f,
    val wavyWavelength: Float = 24f,
    val straightThickness: Float = 6f,
    val straightCornerRadius: Float = 4f,
    val straightThumbRadius: Float = 6f,
    val a12InactiveThickness: Float = 16f,
    val a12CornerRadius: Float = 8f,
    val qsPanelBackgroundColor: Int? = null,
    val hideBrightnessPercentage: Boolean = false,
    val hideBrightnessIcon: Boolean = false,
    val showBrightnessPlusMinus: Boolean = false,
    val minBrightness: Int = 1,
    val maxBrightness: Int = 2047
) {
    @Composable
    fun getSliderElementColor(isDark: Boolean): Color {
        val isTertiary = if (isDark) slidersColorTertiaryDark else slidersColorTertiaryLight
        val baseColor = if (isTertiary) {
            MaterialTheme.colorScheme.tertiary
        } else {
            MaterialTheme.colorScheme.primary
        }
        val hue = if (isDark) slidersHueDark else slidersHueLight
        val sat = if (isDark) slidersSatDark else slidersSatLight
        val valFactor = if (isDark) slidersValDark else slidersValLight
        return com.example.ui.theme.applyHsvAdjustments(baseColor, hue, sat, valFactor)
    }

    companion object {
        @Composable
        fun collectFromCurrentState(): QsOverlaySurfaceConfig {
            val themeMode by PanelPreferences.themeMode.collectAsState()
            val outerCorner by PanelPreferences.panelOuterCorner.collectAsState()
            val innerCorner by PanelPreferences.panelInnerCorner.collectAsState()
            val globalCorner by PanelPreferences.panelGlobalCornerEnabled.collectAsState()
            val enabledShape by PanelPreferences.enabledTileShapeId.collectAsState()
            val disabledShape by PanelPreferences.disabledTileShapeId.collectAsState()
            val tileOutline by PanelPreferences.tileOutlineEnabled.collectAsState()
            val tileGradient by PanelPreferences.showQsTileGradient.collectAsState()
            val progressStyle by PanelPreferences.progressBarStyle.collectAsState()
            val brightnessStyle by PanelPreferences.brightnessSliderStyle.collectAsState()
            val volumeStyle by PanelPreferences.volumeSliderStyle.collectAsState()
            val mediaStyle by PanelPreferences.mediaSliderStyle.collectAsState()
            val syncAll by PanelPreferences.sliderSyncAll.collectAsState()
            val hideBrightnessPercentage by PanelPreferences.hideBrightnessPercentage.collectAsState()
            val hideBrightnessIcon by PanelPreferences.hideBrightnessIcon.collectAsState()
            val showBrightnessPlusMinus by PanelPreferences.showBrightnessPlusMinus.collectAsState()
            val minBrightness by PanelPreferences.minBrightness.collectAsState()
            val maxBrightness by PanelPreferences.maxBrightness.collectAsState()

            val showHeaderImg by PanelPreferences.showCustomHeaderImage.collectAsState()
            val headerImgUri by PanelPreferences.customHeaderImageUri.collectAsState()
            val headerImgHeight by PanelPreferences.customHeaderImageHeight.collectAsState()
            val headerImgOpacity by PanelPreferences.customHeaderImageOpacity.collectAsState()

            val showMonetGrad by PanelPreferences.showCustomHeaderMonetGradient.collectAsState()
            val monetGradHeight by PanelPreferences.customHeaderMonetGradientHeight.collectAsState()
            val showMonetSurf by PanelPreferences.showMonetSurfaceBackground.collectAsState()

            val headerGradTertiary by PanelPreferences.headerGradientColorTertiary.collectAsState()
            val slidersTertiary by PanelPreferences.slidersColorTertiary.collectAsState()
            val slidersTertiaryLight by PanelPreferences.slidersColorTertiaryLight.collectAsState()
            val slidersTertiaryDark by PanelPreferences.slidersColorTertiaryDark.collectAsState()
            val slidersHue by PanelPreferences.slidersHue.collectAsState()
            val slidersSat by PanelPreferences.slidersSat.collectAsState()
            val slidersVal by PanelPreferences.slidersVal.collectAsState()
            val slidersHueLight by PanelPreferences.slidersHueLight.collectAsState()
            val slidersSatLight by PanelPreferences.slidersSatLight.collectAsState()
            val slidersValLight by PanelPreferences.slidersValLight.collectAsState()
            val slidersHueDark by PanelPreferences.slidersHueDark.collectAsState()
            val slidersSatDark by PanelPreferences.slidersSatDark.collectAsState()
            val slidersValDark by PanelPreferences.slidersValDark.collectAsState()

            val qsTilesTertiary by PanelPreferences.qsTilesColorTertiary.collectAsState()
            val surfaceBgTertiary by PanelPreferences.surfaceBgColorTertiary.collectAsState()
            val panelContainerTertiary by PanelPreferences.panelContainerColorTertiary.collectAsState()

            val wavyThick by PanelPreferences.wavyThickness.collectAsState()
            val wavyWobble by PanelPreferences.wavyWobbliness.collectAsState()
            val wavyWaveLen by PanelPreferences.wavyWavelength.collectAsState()

            val straightThick by PanelPreferences.straightThickness.collectAsState()
            val straightCorner by PanelPreferences.straightCornerRadius.collectAsState()
            val straightThumb by PanelPreferences.straightThumbRadius.collectAsState()

            val a12InactThick by PanelPreferences.a12InactiveTrackThickness.collectAsState()
            val a12Corner by PanelPreferences.a12CornerRadius.collectAsState()
            val qsPanelBg by PanelPreferences.qsPanelBackgroundColor.collectAsState()

            return QsOverlaySurfaceConfig(
                themeMode = themeMode.id,
                panelOuterCorner = outerCorner,
                panelInnerCorner = innerCorner,
                panelGlobalCornerEnabled = globalCorner,
                enabledTileShapeId = enabledShape,
                disabledTileShapeId = disabledShape,
                tileOutlineEnabled = tileOutline,
                showQsTileGradient = tileGradient,
                progressBarStyle = progressStyle,
                brightnessSliderStyle = brightnessStyle,
                volumeSliderStyle = volumeStyle,
                mediaSliderStyle = mediaStyle,
                sliderSyncAll = syncAll,
                showCustomHeaderImage = showHeaderImg,
                customHeaderImageUri = headerImgUri,
                customHeaderImageHeight = headerImgHeight,
                customHeaderImageOpacity = headerImgOpacity,
                showCustomHeaderMonetGradient = showMonetGrad,
                customHeaderMonetGradientHeight = monetGradHeight.toFloat(),
                showMonetSurfaceBackground = showMonetSurf,
                headerGradientColorTertiary = headerGradTertiary,
                slidersColorTertiary = slidersTertiary,
                slidersColorTertiaryLight = slidersTertiaryLight,
                slidersColorTertiaryDark = slidersTertiaryDark,
                slidersHue = slidersHue,
                slidersSat = slidersSat,
                slidersVal = slidersVal,
                slidersHueLight = slidersHueLight,
                slidersSatLight = slidersSatLight,
                slidersValLight = slidersValLight,
                slidersHueDark = slidersHueDark,
                slidersSatDark = slidersSatDark,
                slidersValDark = slidersValDark,
                qsTilesColorTertiary = qsTilesTertiary,
                surfaceBgColorTertiary = surfaceBgTertiary,
                panelContainerColorTertiary = panelContainerTertiary,
                wavyThickness = wavyThick,
                wavyWobbliness = wavyWobble,
                wavyWavelength = wavyWaveLen,
                straightThickness = straightThick,
                straightCornerRadius = straightCorner,
                straightThumbRadius = straightThumb,
                a12InactiveThickness = a12InactThick,
                a12CornerRadius = a12Corner,
                qsPanelBackgroundColor = qsPanelBg,
                hideBrightnessPercentage = hideBrightnessPercentage,
                hideBrightnessIcon = hideBrightnessIcon,
                showBrightnessPlusMinus = showBrightnessPlusMinus,
                minBrightness = minBrightness,
                maxBrightness = maxBrightness
            )
        }

        fun fromTheme(theme: SavedTheme): QsOverlaySurfaceConfig {
            return try {
                val json = JSONObject(theme.jsonPayload)
                val shape = json.optString("tile_shape", "circle")
                val enabledShape = json.optString("enabled_tile_shape", shape)
                val disabledShape = json.optString("disabled_tile_shape", shape)
                val globalCorner = json.optBoolean("panel_global_corner_enabled", false)
                val outerCorner = json.optDouble("panel_outer_corner", 20.0).toFloat()
                val innerCorner = if (globalCorner) outerCorner else json.optDouble("panel_inner_corner", 12.0).toFloat()

                val progressStyleStr = json.optString("progress_bar_style", "default")
                val brightnessStyleStr = json.optString("brightness_slider_style", "default")

                QsOverlaySurfaceConfig(
                    themeMode = json.optString("theme_mode", "system"),
                    enabledTileShapeId = enabledShape,
                    disabledTileShapeId = disabledShape,
                    tileOutlineEnabled = json.optBoolean("tile_outline_enabled", false),
                    showQsTileGradient = json.optBoolean("show_qs_tile_gradient", false),
                    progressBarStyle = ProgressBarStyle.entries.firstOrNull { it.id == progressStyleStr } ?: ProgressBarStyle.DEFAULT,
                    brightnessSliderStyle = ProgressBarStyle.entries.firstOrNull { it.id == brightnessStyleStr } ?: ProgressBarStyle.DEFAULT,
                    minBrightness = json.optInt("min_brightness", 1),
                    maxBrightness = json.optInt("max_brightness", 2047),
                    sliderSyncAll = json.optBoolean("slider_sync_all", true),
                    panelOuterCorner = outerCorner,
                    panelInnerCorner = innerCorner,
                    panelGlobalCornerEnabled = globalCorner,
                    showCustomHeaderImage = json.optBoolean("show_custom_header_image", false),
                    customHeaderImageUri = json.optString("custom_header_image_uri", ""),
                    customHeaderImageHeight = json.optDouble("custom_header_image_height", 120.0).toFloat(),
                    customHeaderImageOpacity = json.optDouble("custom_header_image_opacity", 1.0).toFloat(),
                    showCustomHeaderMonetGradient = json.optBoolean("show_custom_header_monet_gradient", false),
                    customHeaderMonetGradientHeight = json.optInt("custom_header_monet_gradient_height", 120).toFloat(),
                    showMonetSurfaceBackground = json.optBoolean("show_monet_surface_background", false),
                    headerGradientColorTertiary = json.optBoolean("elem_header_gradient_tertiary", false),
                    slidersColorTertiary = json.optBoolean("elem_sliders_tertiary", false),
                    slidersColorTertiaryLight = json.optBoolean("elem_sliders_tertiary_light", json.optBoolean("elem_sliders_tertiary", false)),
                    slidersColorTertiaryDark = json.optBoolean("elem_sliders_tertiary_dark", json.optBoolean("elem_sliders_tertiary", false)),
                    slidersHue = json.optDouble("elem_sliders_hue", 0.0).toFloat(),
                    slidersSat = json.optDouble("elem_sliders_sat", 1.0).toFloat(),
                    slidersVal = json.optDouble("elem_sliders_val", 1.0).toFloat(),
                    slidersHueLight = json.optDouble("elem_sliders_hue_light", json.optDouble("elem_sliders_hue", 0.0)).toFloat(),
                    slidersSatLight = json.optDouble("elem_sliders_sat_light", json.optDouble("elem_sliders_sat", 1.0)).toFloat(),
                    slidersValLight = json.optDouble("elem_sliders_val_light", json.optDouble("elem_sliders_val", 1.0)).toFloat(),
                    slidersHueDark = json.optDouble("elem_sliders_hue_dark", json.optDouble("elem_sliders_hue", 0.0)).toFloat(),
                    slidersSatDark = json.optDouble("elem_sliders_sat_dark", json.optDouble("elem_sliders_sat", 1.0)).toFloat(),
                    slidersValDark = json.optDouble("elem_sliders_val_dark", json.optDouble("elem_sliders_val", 1.0)).toFloat(),
                    qsTilesColorTertiary = json.optBoolean("elem_qs_tiles_tertiary", false),
                    surfaceBgColorTertiary = json.optBoolean("elem_surface_bg_tertiary", false),
                    panelContainerColorTertiary = json.optBoolean("elem_panel_container_tertiary", false),
                    wavyThickness = json.optDouble("wavy_thickness", 6.0).toFloat(),
                    wavyWobbliness = json.optDouble("wavy_wobbliness", 1.2).toFloat(),
                    wavyWavelength = json.optDouble("wavy_wavelength", 24.0).toFloat(),
                    straightThickness = json.optDouble("straight_thickness", 6.0).toFloat(),
                    straightCornerRadius = json.optDouble("straight_corner_radius", 4.0).toFloat(),
                    straightThumbRadius = json.optDouble("straight_thumb_radius", 6.0).toFloat(),
                    a12InactiveThickness = json.optDouble("a12_inactive_thickness", 16.0).toFloat(),
                    a12CornerRadius = json.optDouble("a12_corner_radius", 8.0).toFloat()
                )
            } catch (_: Exception) {
                QsOverlaySurfaceConfig()
            }
        }
    }
}

/**
 * Reusable, shared layout and container rendering component for Quick Settings panel.
 * Guarantees 100% visual parity across live interactive preview and system overlay.
 */
@Composable
fun QsOverlaySurface(
    config: QsOverlaySurfaceConfig,
    modifier: Modifier = Modifier,
    brightnessValue: Float = 175f,
    onBrightnessChange: ((Float) -> Unit)? = null,
    headerImageAsCard: Boolean = false,
    testTagPrefix: String = "qs_surface"
) {
    val isSystemDark = isSystemInDarkTheme()
    val isPanelDark = when (config.themeMode.lowercase()) {
        "dark" -> true
        "light" -> false
        else -> isSystemDark
    }

    // Panel & Surface Colors matching QuickSettingsPanel.kt & Color.kt
    val basePanelBg = config.qsPanelBackgroundColor?.let { Color(it) } ?: if (isPanelDark) Color(0xFF14171A) else Color(0xFFF3F4F6)
    val surfaceContainerColor = if (config.surfaceBgColorTertiary) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val baseMonetColor = config.qsPanelBackgroundColor?.let { Color(it) } ?: surfaceContainerColor
    val monetSurfaceBg = if (config.showMonetSurfaceBackground) {
        val scaledR = (baseMonetColor.red * 1.10f).coerceIn(0f, 1f)
        val scaledG = (baseMonetColor.green * 1.10f).coerceIn(0f, 1f)
        val scaledB = (baseMonetColor.blue * 1.10f).coerceIn(0f, 1f)
        val blendR = (baseMonetColor.red + (1f - baseMonetColor.red) * 0.10f).coerceIn(0f, 1f)
        val blendG = (baseMonetColor.green + (1f - baseMonetColor.green) * 0.10f).coerceIn(0f, 1f)
        val blendB = (baseMonetColor.blue + (1f - baseMonetColor.blue) * 0.10f).coerceIn(0f, 1f)
        Color(
            red = maxOf(scaledR, blendR),
            green = maxOf(scaledG, blendG),
            blue = maxOf(scaledB, blendB),
            alpha = baseMonetColor.alpha
        )
    } else {
        basePanelBg
    }

    val cardContainerBg = config.qsPanelBackgroundColor?.let { Color(it) } ?: if (config.showMonetSurfaceBackground) {
        monetSurfaceBg
    } else if (config.panelContainerColorTertiary) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        if (isPanelDark) {
            Color(
                red = (MaterialTheme.colorScheme.surfaceVariant.red * 0.72f).coerceIn(0f, 1f),
                green = (MaterialTheme.colorScheme.surfaceVariant.green * 0.72f).coerceIn(0f, 1f),
                blue = (MaterialTheme.colorScheme.surfaceVariant.blue * 0.72f).coerceIn(0f, 1f),
                alpha = 1.0f
            )
        } else Color.White
    }

    val contentColor = if (isPanelDark) Color.White else Color(0xFF1E293B)
    val subtextColor = if (isPanelDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    // Slider settings & colors following QuickSettingsPanel.kt
    val sliderAccentColor = config.getSliderElementColor(isPanelDark)

    val sliderInactiveColor = com.example.ui.theme.getInactiveTilesElementColor(isPanelDark)

    val resolvedSliderStyle = if (config.sliderSyncAll) config.progressBarStyle else config.brightnessSliderStyle

    // Tile settings & colors
    val tileAccentColor = if (config.qsTilesColorTertiary) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }
    val tileOnAccentColor = if (config.qsTilesColorTertiary) {
        MaterialTheme.colorScheme.onTertiary
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    val enabledTileShape = QsShapeManager.getComposeShape(config.enabledTileShapeId)
    val disabledTileShape = QsShapeManager.getComposeShape(config.disabledTileShapeId)

    val outerCorner = config.panelOuterCorner.dp.coerceIn(8.dp, 28.dp)
    val innerCorner = (if (config.panelGlobalCornerEnabled) config.panelOuterCorner else config.panelInnerCorner).dp.coerceIn(6.dp, 24.dp)

    var localBrightness by remember { mutableFloatStateOf(brightnessValue) }

    // Main Quick Settings Panel Surface Container
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(outerCorner))
            .background(monetSurfaceBg)
            .testTag(testTagPrefix)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. HEADER IMAGE BANNER OR MONET GRADIENT BANNER
            if (headerImageAsCard && config.showCustomHeaderImage) {
                // In Themes section only: Custom header image card rendered ON TOP OF gradient preview
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Background layer: Gradient preview
                    val gradientStartColor = if (isPanelDark) {
                        if (config.headerGradientColorTertiary) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    } else {
                        if (config.headerGradientColorTertiary) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    }

                    val gradientBrush = if (config.showCustomHeaderMonetGradient) {
                        Brush.verticalGradient(
                            colors = listOf(
                                gradientStartColor,
                                Color.Transparent
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(
                                tileAccentColor.copy(alpha = 0.85f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((config.customHeaderMonetGradientHeight * 0.45f).dp.coerceIn(50.dp, 100.dp))
                            .clip(RoundedCornerShape(topStart = outerCorner, topEnd = outerCorner))
                            .background(gradientBrush)
                    )

                    // Foreground layer (ON TOP OF gradient preview): Custom header image preview card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(innerCorner),
                            colors = CardDefaults.cardColors(containerColor = cardContainerBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((config.customHeaderImageHeight * 0.45f).dp.coerceIn(40.dp, 90.dp))
                        ) {
                            if (config.customHeaderImageUri.isNotBlank()) {
                                AsyncImage(
                                    model = config.customHeaderImageUri,
                                    contentDescription = "Header Image Card",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(innerCorner))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    tileAccentColor.copy(alpha = 0.85f),
                                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f),
                                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                                                )
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            } else if (config.showCustomHeaderImage && !headerImageAsCard) {
                // Full length header image banner for non-Themes sections
                if (config.customHeaderImageUri.isNotBlank()) {
                    AsyncImage(
                        model = config.customHeaderImageUri,
                        contentDescription = "Header Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((config.customHeaderImageHeight * 0.45f).dp.coerceIn(30.dp, 100.dp))
                            .clip(RoundedCornerShape(topStart = outerCorner, topEnd = outerCorner))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(topStart = outerCorner, topEnd = outerCorner))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        tileAccentColor.copy(alpha = 0.85f),
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )
                }
            } else if (config.showCustomHeaderMonetGradient) {
                val gradientStartColor = if (isPanelDark) {
                    if (config.headerGradientColorTertiary) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                } else {
                    if (config.headerGradientColorTertiary) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((config.customHeaderMonetGradientHeight * 0.45f).dp.coerceIn(28.dp, 80.dp))
                        .clip(RoundedCornerShape(topStart = outerCorner, topEnd = outerCorner))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    gradientStartColor,
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 14.dp,
                        end = 14.dp,
                        bottom = 14.dp,
                        top = if (headerImageAsCard && config.showCustomHeaderImage) 0.dp else 14.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (headerImageAsCard) {
                    // In Themes section ONLY: Panel container extends up to include the brightness slider with innerCorner matching real settings
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(innerCorner))
                            .background(cardContainerBg)
                            .padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Brightness Slider row inside container
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (!config.hideBrightnessIcon) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(sliderAccentColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.BrightnessMedium,
                                            contentDescription = null,
                                            tint = sliderAccentColor,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                if (config.showBrightnessPlusMinus) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = null,
                                        tint = sliderAccentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                QsProgressSlider(
                                    value = (if (onBrightnessChange != null) brightnessValue else localBrightness).coerceIn(config.minBrightness.toFloat(), config.maxBrightness.toFloat()),
                                    onValueChange = {
                                        localBrightness = it
                                        onBrightnessChange?.invoke(it)
                                    },
                                    valueRange = config.minBrightness.toFloat()..config.maxBrightness.toFloat(),
                                    style = resolvedSliderStyle,
                                    sliderTarget = QsSliderTarget.BRIGHTNESS,
                                    isPlaying = (resolvedSliderStyle == ProgressBarStyle.WAVY),
                                    activeColor = sliderAccentColor,
                                    inactiveColor = sliderInactiveColor,
                                    thumbColor = sliderAccentColor,
                                    modifier = Modifier.weight(1f)
                                )

                                if (config.showBrightnessPlusMinus) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = sliderAccentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                if (!config.hideBrightnessPercentage) {
                                    val currentB = (if (onBrightnessChange != null) brightnessValue else localBrightness).coerceIn(config.minBrightness.toFloat(), config.maxBrightness.toFloat())
                                    val range = (config.maxBrightness - config.minBrightness).coerceAtLeast(1)
                                    val pct = (((currentB - config.minBrightness) / range) * 100).toInt().coerceIn(0, 100)
                                    Text(
                                        text = "$pct%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPanelDark) Color.White else Color(0xFF333333)
                                    )
                                }
                            }

                            // Tiles Row inside container
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Top
                            ) {
                                // One Enabled Tile
                                SharedQsTileView(
                                    icon = Icons.Default.Wifi,
                                    label = "Wi-Fi",
                                    subtitle = "Connected",
                                    isActive = true,
                                    tileShape = enabledTileShape,
                                    hasOutline = config.tileOutlineEnabled,
                                    hasGradient = config.showQsTileGradient,
                                    tileAccentColor = tileAccentColor,
                                    tileOnAccentColor = tileOnAccentColor,
                                    tertiaryColor = MaterialTheme.colorScheme.tertiary,
                                    isPanelDark = isPanelDark,
                                    contentColor = contentColor,
                                    subtextColor = subtextColor,
                                    modifier = Modifier.weight(1f)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                // One Disabled Tile
                                SharedQsTileView(
                                    icon = Icons.Default.Bluetooth,
                                    label = "Bluetooth",
                                    subtitle = "Off",
                                    isActive = false,
                                    tileShape = disabledTileShape,
                                    hasOutline = config.tileOutlineEnabled,
                                    hasGradient = false,
                                    tileAccentColor = tileAccentColor,
                                    tileOnAccentColor = tileOnAccentColor,
                                    tertiaryColor = MaterialTheme.colorScheme.tertiary,
                                    isPanelDark = isPanelDark,
                                    contentColor = contentColor,
                                    subtextColor = subtextColor,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                } else {
                    // Standard separate slider and tiles container for other sections
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!config.hideBrightnessIcon) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(sliderAccentColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BrightnessMedium,
                                    contentDescription = null,
                                    tint = sliderAccentColor,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        if (config.showBrightnessPlusMinus) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = null,
                                tint = sliderAccentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        QsProgressSlider(
                            value = (if (onBrightnessChange != null) brightnessValue else localBrightness).coerceIn(config.minBrightness.toFloat(), config.maxBrightness.toFloat()),
                            onValueChange = {
                                localBrightness = it
                                onBrightnessChange?.invoke(it)
                            },
                            valueRange = config.minBrightness.toFloat()..config.maxBrightness.toFloat(),
                            style = resolvedSliderStyle,
                            sliderTarget = QsSliderTarget.BRIGHTNESS,
                            isPlaying = (resolvedSliderStyle == ProgressBarStyle.WAVY),
                            activeColor = sliderAccentColor,
                            inactiveColor = sliderInactiveColor,
                            thumbColor = sliderAccentColor,
                            modifier = Modifier.weight(1f)
                        )

                        if (config.showBrightnessPlusMinus) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = sliderAccentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (!config.hideBrightnessPercentage) {
                            val currentB = (if (onBrightnessChange != null) brightnessValue else localBrightness).coerceIn(config.minBrightness.toFloat(), config.maxBrightness.toFloat())
                            val range = (config.maxBrightness - config.minBrightness).coerceAtLeast(1)
                            val pct = (((currentB - config.minBrightness) / range) * 100).toInt().coerceIn(0, 100)
                            Text(
                                text = "$pct%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPanelDark) Color.White else Color(0xFF333333)
                            )
                        }
                    }

                    // TILES CONTAINER CARD
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(innerCorner))
                            .background(cardContainerBg)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Top
                        ) {
                            SharedQsTileView(
                                icon = Icons.Default.Wifi,
                                label = "Wi-Fi",
                                subtitle = "Connected",
                                isActive = true,
                                tileShape = enabledTileShape,
                                hasOutline = config.tileOutlineEnabled,
                                hasGradient = config.showQsTileGradient,
                                tileAccentColor = tileAccentColor,
                                tileOnAccentColor = tileOnAccentColor,
                                tertiaryColor = MaterialTheme.colorScheme.tertiary,
                                isPanelDark = isPanelDark,
                                contentColor = contentColor,
                                subtextColor = subtextColor,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            SharedQsTileView(
                                icon = Icons.Default.Bluetooth,
                                label = "Bluetooth",
                                subtitle = "Off",
                                isActive = false,
                                tileShape = disabledTileShape,
                                hasOutline = config.tileOutlineEnabled,
                                hasGradient = false,
                                tileAccentColor = tileAccentColor,
                                tileOnAccentColor = tileOnAccentColor,
                                tertiaryColor = MaterialTheme.colorScheme.tertiary,
                                isPanelDark = isPanelDark,
                                contentColor = contentColor,
                                subtextColor = subtextColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Shared tile view component for Quick Settings surface rendering.
 * Explicitly renders border outlines (strokes/contours) when hasOutline is enabled.
 */
@Composable
private fun SharedQsTileView(
    icon: ImageVector,
    label: String,
    subtitle: String,
    isActive: Boolean,
    tileShape: Shape,
    hasOutline: Boolean,
    hasGradient: Boolean,
    tileAccentColor: Color,
    tileOnAccentColor: Color,
    tertiaryColor: Color,
    isPanelDark: Boolean,
    contentColor: Color,
    subtextColor: Color,
    modifier: Modifier = Modifier
) {
    val inactiveBg = com.example.ui.theme.getInactiveTilesElementColor(isPanelDark)
    val inactiveIconTint = if (isPanelDark) Color.White.copy(alpha = 0.90f) else Color(0xFF1E293B)

    val gradientBrush = remember(tileAccentColor, tertiaryColor) {
        Brush.linearGradient(listOf(tileAccentColor, tertiaryColor))
    }

    val outlineColor = if (isActive) tileAccentColor else if (isPanelDark) Color(0xFF384048) else Color(0xFFCBD5E1)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.wrapContentHeight()
    ) {
        // Shaped Icon Container (48dp x 48dp)
        Box(
            modifier = Modifier
                .size(48.dp)
                .then(
                    if (hasOutline) {
                        if (isActive && hasGradient) {
                            Modifier.border(1.5.dp, gradientBrush, tileShape)
                        } else {
                            Modifier.border(1.5.dp, outlineColor, tileShape)
                        }
                    } else Modifier
                )
                .clip(tileShape)
                .then(
                    if (hasOutline) {
                        Modifier.background(Color.Transparent)
                    } else if (isActive) {
                        if (hasGradient) Modifier.background(gradientBrush) else Modifier.background(tileAccentColor)
                    } else {
                        Modifier.background(inactiveBg)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) {
                    if (hasOutline) tileAccentColor else tileOnAccentColor
                } else {
                    inactiveIconTint
                },
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tile Label Centered
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            ),
            modifier = Modifier.offset(y = (-1).dp)
        )

        // Tile Subtitle Centered
        Text(
            text = subtitle,
            fontSize = 9.sp,
            color = subtextColor,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            ),
            modifier = Modifier.offset(y = (-1).dp)
        )
    }
}
