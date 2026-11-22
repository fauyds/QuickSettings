package com.example.ui.customizer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ComplicationArcMonetMode
import com.example.data.PanelPreferences
import com.example.ui.theme.AppSettingsTheme
import com.example.ui.theme.applyHsvAdjustments
import kotlin.math.roundToInt

private enum class ModeTab { LIGHT, DARK }

class ElementColorsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        PanelPreferences.init(this)

        setContent {
            AppSettingsTheme {
                ElementColorsScreen(onBackClick = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElementColorsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    BackHandler { onBackClick() }

    val systemInDark = isSystemInDarkTheme()
    var selectedTab by remember { mutableStateOf(if (systemInDark) ModeTab.DARK else ModeTab.LIGHT) }
    val isEditingDark = selectedTab == ModeTab.DARK

    // Target ColorScheme matching the selected Light/Dark tab so swatch previews reflect the edited mode accurately
    val editingColorScheme = remember(context, isEditingDark) {
        com.example.ui.theme.getDynamicColorScheme(context = context, isDark = isEditingDark, dynamicColor = true)
    }

    // Monet Color Modes
    val surfaceBgColorMode by PanelPreferences.getSurfaceBgColorMode(isEditingDark).collectAsState()
    val panelContainerColorMode by PanelPreferences.getPanelContainerColorMode(isEditingDark).collectAsState()
    val headerGradientColorMode by PanelPreferences.getHeaderGradientColorMode(isEditingDark).collectAsState()
    val qsTilesColorMode by PanelPreferences.getQsTilesColorMode(isEditingDark).collectAsState()
    val inactiveTilesColorMode by PanelPreferences.getInactiveTilesColorMode(isEditingDark).collectAsState()
    val slidersColorMode by PanelPreferences.getSlidersColorMode(isEditingDark).collectAsState()
    val miscElementsColorMode by PanelPreferences.getMiscElementsColorMode(isEditingDark).collectAsState()
    val clearAllColorMode by PanelPreferences.getClearAllColorMode(isEditingDark).collectAsState()

    // HSB values
    val surfaceBgHue by PanelPreferences.getSurfaceBgHue(isEditingDark).collectAsState()
    val surfaceBgSat by PanelPreferences.getSurfaceBgSat(isEditingDark).collectAsState()
    val surfaceBgVal by PanelPreferences.getSurfaceBgVal(isEditingDark).collectAsState()

    val panelContainerHue by PanelPreferences.getPanelContainerHue(isEditingDark).collectAsState()
    val panelContainerSat by PanelPreferences.getPanelContainerSat(isEditingDark).collectAsState()
    val panelContainerVal by PanelPreferences.getPanelContainerVal(isEditingDark).collectAsState()

    val headerGradientHue by PanelPreferences.getHeaderGradientHue(isEditingDark).collectAsState()
    val headerGradientSat by PanelPreferences.getHeaderGradientSat(isEditingDark).collectAsState()
    val headerGradientVal by PanelPreferences.getHeaderGradientVal(isEditingDark).collectAsState()

    val qsTilesHue by PanelPreferences.getQsTilesHue(isEditingDark).collectAsState()
    val qsTilesSat by PanelPreferences.getQsTilesSat(isEditingDark).collectAsState()
    val qsTilesVal by PanelPreferences.getQsTilesVal(isEditingDark).collectAsState()

    val inactiveTilesHue by PanelPreferences.getInactiveTilesHue(isEditingDark).collectAsState()
    val inactiveTilesSat by PanelPreferences.getInactiveTilesSat(isEditingDark).collectAsState()
    val inactiveTilesVal by PanelPreferences.getInactiveTilesVal(isEditingDark).collectAsState()

    val slidersHue by PanelPreferences.getSlidersHue(isEditingDark).collectAsState()
    val slidersSat by PanelPreferences.getSlidersSat(isEditingDark).collectAsState()
    val slidersVal by PanelPreferences.getSlidersVal(isEditingDark).collectAsState()

    val miscElementsHue by PanelPreferences.getMiscElementsHue(isEditingDark).collectAsState()
    val miscElementsSat by PanelPreferences.getMiscElementsSat(isEditingDark).collectAsState()
    val miscElementsVal by PanelPreferences.getMiscElementsVal(isEditingDark).collectAsState()

    val clearAllHue by PanelPreferences.getClearAllHue(isEditingDark).collectAsState()
    val clearAllSat by PanelPreferences.getClearAllSat(isEditingDark).collectAsState()
    val clearAllVal by PanelPreferences.getClearAllVal(isEditingDark).collectAsState()

    // Custom Color Use & Value StateFlows
    val surfaceBgUseCustom by PanelPreferences.getSurfaceBgUseCustom(isEditingDark).collectAsState()
    val surfaceBgCustomColor by PanelPreferences.getSurfaceBgCustomColor(isEditingDark).collectAsState()

    val panelContainerUseCustom by PanelPreferences.getPanelContainerUseCustom(isEditingDark).collectAsState()
    val panelContainerCustomColor by PanelPreferences.getPanelContainerCustomColor(isEditingDark).collectAsState()

    val headerGradientUseCustom by PanelPreferences.getHeaderGradientUseCustom(isEditingDark).collectAsState()
    val headerGradientCustomColor by PanelPreferences.getHeaderGradientCustomColor(isEditingDark).collectAsState()

    val qsTilesUseCustom by PanelPreferences.getQsTilesUseCustom(isEditingDark).collectAsState()
    val qsTilesCustomColor by PanelPreferences.getQsTilesCustomColor(isEditingDark).collectAsState()

    val inactiveTilesUseCustom by PanelPreferences.getInactiveTilesUseCustom(isEditingDark).collectAsState()
    val inactiveTilesCustomColor by PanelPreferences.getInactiveTilesCustomColor(isEditingDark).collectAsState()

    val slidersUseCustom by PanelPreferences.getSlidersUseCustom(isEditingDark).collectAsState()
    val slidersCustomColor by PanelPreferences.getSlidersCustomColor(isEditingDark).collectAsState()

    val miscElementsUseCustom by PanelPreferences.getMiscElementsUseCustom(isEditingDark).collectAsState()
    val miscElementsCustomColor by PanelPreferences.getMiscElementsCustomColor(isEditingDark).collectAsState()

    val clearAllUseCustom by PanelPreferences.getClearAllUseCustom(isEditingDark).collectAsState()
    val clearAllCustomColor by PanelPreferences.getClearAllCustomColor(isEditingDark).collectAsState()

    // Complication Arcs & Tracks StateFlows
    val activeCompArcMode by PanelPreferences.getActiveCompArcMode(isEditingDark).collectAsState()
    val activeCompArcHue by PanelPreferences.getActiveCompArcHue(isEditingDark).collectAsState()
    val activeCompArcSat by PanelPreferences.getActiveCompArcSat(isEditingDark).collectAsState()
    val activeCompArcVal by PanelPreferences.getActiveCompArcVal(isEditingDark).collectAsState()
    val activeCompArcUseCustom by PanelPreferences.getActiveCompArcUseCustom(isEditingDark).collectAsState()
    val activeCompArcCustomColor by PanelPreferences.getActiveCompArcCustomColor(isEditingDark).collectAsState()

    val inactiveCompArcMode by PanelPreferences.getInactiveCompArcMode(isEditingDark).collectAsState()
    val inactiveCompArcHue by PanelPreferences.getInactiveCompArcHue(isEditingDark).collectAsState()
    val inactiveCompArcSat by PanelPreferences.getInactiveCompArcSat(isEditingDark).collectAsState()
    val inactiveCompArcVal by PanelPreferences.getInactiveCompArcVal(isEditingDark).collectAsState()
    val inactiveCompArcUseCustom by PanelPreferences.getInactiveCompArcUseCustom(isEditingDark).collectAsState()
    val inactiveCompArcCustomColor by PanelPreferences.getInactiveCompArcCustomColor(isEditingDark).collectAsState()

    val compArcTrackMode by PanelPreferences.getCompArcTrackMode(isEditingDark).collectAsState()
    val compArcTrackHue by PanelPreferences.getCompArcTrackHue(isEditingDark).collectAsState()
    val compArcTrackSat by PanelPreferences.getCompArcTrackSat(isEditingDark).collectAsState()
    val compArcTrackVal by PanelPreferences.getCompArcTrackVal(isEditingDark).collectAsState()
    val compArcTrackUseCustom by PanelPreferences.getCompArcTrackUseCustom(isEditingDark).collectAsState()
    val compArcTrackCustomColor by PanelPreferences.getCompArcTrackCustomColor(isEditingDark).collectAsState()

    val hiddenIds by PanelPreferences.hiddenFeatures.collectAsState()
    var showHiddenDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                expandedHeight = 52.dp,
                title = {
                    Text(
                        text = "Per-Element Accent Colors",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("element_colors_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (hiddenIds.isNotEmpty()) {
                        Surface(
                            onClick = { showHiddenDialog = true },
                            shape = CircleShape,
                            color = com.example.ui.theme.ExpressiveTheme.colors.buttonPrimary,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .defaultMinSize(minHeight = 36.dp)
                                .testTag("element_colors_show_hidden_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "Show hidden features",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Hidden (${hiddenIds.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val unselectedCardBg = com.example.ui.theme.getSettingsCardBackgroundColor(systemInDark)

            // Top Mode Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("element_mode_selector_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Light Mode Button
                val isLightSelected = selectedTab == ModeTab.LIGHT
                Button(
                    onClick = { selectedTab = ModeTab.LIGHT },
                    shape = if (isLightSelected) CircleShape else RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLightSelected) MaterialTheme.colorScheme.primary else unselectedCardBg,
                        contentColor = if (isLightSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isLightSelected) 2.dp else 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("mode_tab_light")
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Light",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Dark Mode Button
                val isDarkSelected = selectedTab == ModeTab.DARK
                Button(
                    onClick = { selectedTab = ModeTab.DARK },
                    shape = if (isDarkSelected) CircleShape else RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkSelected) MaterialTheme.colorScheme.primary else unselectedCardBg,
                        contentColor = if (isDarkSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isDarkSelected) 2.dp else 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("mode_tab_dark")
                ) {
                    Icon(
                        imageVector = Icons.Default.NightsStay,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Dark",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Sync Button
                Surface(
                    onClick = {
                        val fromDark = selectedTab == ModeTab.DARK
                        val fromText = if (fromDark) "Dark" else "Light"
                        val toText = if (fromDark) "Light" else "Dark"
                        PanelPreferences.syncElementColors(context, fromDark)
                        Toast.makeText(context, "Synced $fromText settings to $toText mode!", Toast.LENGTH_SHORT).show()
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("sync_mode_settings_button")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync settings between Light and Dark mode",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Description Header Card
            SettingsCard(
                shape = CustomizeCardShapes.SingleCard,
                canPin = false
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Per-Element Color & Accent Customization",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Customize Monet accents with Hue, Saturation & Brightness sliders, or pick exact custom colors using the 2-tab color picker for ${if (isEditingDark) "Dark" else "Light"} mode.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Group of Expandable Preference Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CustomizeCardShapes.CardSpacing)
            ) {
                val allModes = remember { ComplicationArcMonetMode.entries }
                val accentModes = remember {
                    listOf(
                        ComplicationArcMonetMode.PRIMARY,
                        ComplicationArcMonetMode.SECONDARY,
                        ComplicationArcMonetMode.TERTIARY
                    )
                }

                // Group of Expandable Preference Cards (filterable by Hidden Features)
                val visibleColorCards = mutableListOf<Pair<String, @Composable (androidx.compose.ui.graphics.Shape) -> Unit>>()

                // 1. Surface background
                val surfaceFactor = if (isEditingDark) 0.90f else 0.87f
                fun darkenSurface(color: Color) = Color(
                    red = (color.red * surfaceFactor).coerceIn(0f, 1f),
                    green = (color.green * surfaceFactor).coerceIn(0f, 1f),
                    blue = (color.blue * surfaceFactor).coerceIn(0f, 1f),
                    alpha = 1f
                )
                if (!HiddenFeatureCatalog.isCardHidden("elem_color_surface_bg", hiddenIds)) {
                    visibleColorCards.add("elem_color_surface_bg" to { cardShape ->
                        ExpandableElementColorCard(
                            featureId = "elem_color_surface_bg",
                            title = "1. Surface Background",
                            subtitle = "Behind panel containers and background layers",
                            useCustom = surfaceBgUseCustom,
                            onUseCustomChange = { PanelPreferences.setSurfaceBgUseCustom(context, isEditingDark, it) },
                            customColor = surfaceBgCustomColor,
                            onCustomColorChange = { PanelPreferences.setSurfaceBgCustomColor(context, isEditingDark, it) },
                            colorMode = surfaceBgColorMode,
                            onColorModeChange = { PanelPreferences.setSurfaceBgColorMode(context, isEditingDark, it) },
                            availableModes = allModes,
                            defaultColor = darkenSurface(editingColorScheme.surfaceVariant),
                            primaryColor = darkenSurface(editingColorScheme.primaryContainer),
                            secondaryColor = darkenSurface(editingColorScheme.secondaryContainer),
                            tertiaryColor = darkenSurface(editingColorScheme.tertiaryContainer),
                            hue = surfaceBgHue,
                            sat = surfaceBgSat,
                            valFactor = surfaceBgVal,
                            onHsbChange = { h, s, v -> PanelPreferences.setSurfaceBgHsb(context, isEditingDark, h, s, v) },
                            shape = cardShape,
                            testTag = "activity_surface_bg_tertiary_toggle"
                        )
                    })
                }

                // 2. Panel containers & notifications
                if (!HiddenFeatureCatalog.isCardHidden("elem_color_panel_container", hiddenIds)) {
                    visibleColorCards.add("elem_color_panel_container" to { cardShape ->
                        ExpandableElementColorCard(
                            featureId = "elem_color_panel_container",
                            title = "2. Panel Containers & Notifications",
                            subtitle = "Main Quick Settings card panels, sections, and notification cards",
                            useCustom = panelContainerUseCustom,
                            onUseCustomChange = { PanelPreferences.setPanelContainerUseCustom(context, isEditingDark, it) },
                            customColor = panelContainerCustomColor,
                            onCustomColorChange = { PanelPreferences.setPanelContainerCustomColor(context, isEditingDark, it) },
                            colorMode = panelContainerColorMode,
                            onColorModeChange = { PanelPreferences.setPanelContainerColorMode(context, isEditingDark, it) },
                            availableModes = allModes,
                            defaultColor = editingColorScheme.surfaceVariant,
                            primaryColor = editingColorScheme.primaryContainer,
                            secondaryColor = editingColorScheme.secondaryContainer,
                            tertiaryColor = editingColorScheme.tertiaryContainer,
                            hue = panelContainerHue,
                            sat = panelContainerSat,
                            valFactor = panelContainerVal,
                            onHsbChange = { h, s, v -> PanelPreferences.setPanelContainerHsb(context, isEditingDark, h, s, v) },
                            shape = cardShape,
                            testTag = "activity_panel_container_tertiary_toggle"
                        )
                    })
                }

                // 3. Header Gradient
                if (!HiddenFeatureCatalog.isCardHidden("elem_color_header_gradient", hiddenIds)) {
                    visibleColorCards.add("elem_color_header_gradient" to { cardShape ->
                        ExpandableElementColorCard(
                            featureId = "elem_color_header_gradient",
                            title = "3. Header Gradient",
                            subtitle = "Top Monet gradient background accent layer",
                            useCustom = headerGradientUseCustom,
                            onUseCustomChange = { PanelPreferences.setHeaderGradientUseCustom(context, isEditingDark, it) },
                            customColor = headerGradientCustomColor,
                            onCustomColorChange = { PanelPreferences.setHeaderGradientCustomColor(context, isEditingDark, it) },
                            colorMode = headerGradientColorMode,
                            onColorModeChange = { PanelPreferences.setHeaderGradientColorMode(context, isEditingDark, it) },
                            availableModes = accentModes,
                            defaultColor = if (isEditingDark) editingColorScheme.primaryContainer else editingColorScheme.primary,
                            primaryColor = if (isEditingDark) editingColorScheme.primaryContainer else editingColorScheme.primary,
                            secondaryColor = if (isEditingDark) editingColorScheme.secondaryContainer else editingColorScheme.secondary,
                            tertiaryColor = if (isEditingDark) editingColorScheme.tertiaryContainer else editingColorScheme.tertiary,
                            hue = headerGradientHue,
                            sat = headerGradientSat,
                            valFactor = headerGradientVal,
                            onHsbChange = { h, s, v -> PanelPreferences.setHeaderGradientHsb(context, isEditingDark, h, s, v) },
                            shape = cardShape,
                            testTag = "activity_header_gradient_tertiary_toggle"
                        )
                    })
                }

                // 4. Active QS/QQS Tiles
                if (!HiddenFeatureCatalog.isCardHidden("elem_color_qs_tiles", hiddenIds)) {
                    visibleColorCards.add("elem_color_qs_tiles" to { cardShape ->
                        ExpandableElementColorCard(
                            featureId = "elem_color_qs_tiles",
                            title = "4. Active QS Tiles",
                            subtitle = "Quick settings active tiles and icons",
                            useCustom = qsTilesUseCustom,
                            onUseCustomChange = { PanelPreferences.setQsTilesUseCustom(context, isEditingDark, it) },
                            customColor = qsTilesCustomColor,
                            onCustomColorChange = { PanelPreferences.setQsTilesCustomColor(context, isEditingDark, it) },
                            colorMode = qsTilesColorMode,
                            onColorModeChange = { PanelPreferences.setQsTilesColorMode(context, isEditingDark, it) },
                            availableModes = accentModes,
                            defaultColor = editingColorScheme.primary,
                            primaryColor = editingColorScheme.primary,
                            secondaryColor = editingColorScheme.secondary,
                            tertiaryColor = editingColorScheme.tertiary,
                            hue = qsTilesHue,
                            sat = qsTilesSat,
                            valFactor = qsTilesVal,
                            onHsbChange = { h, s, v -> PanelPreferences.setQsTilesHsb(context, isEditingDark, h, s, v) },
                            shape = cardShape,
                            testTag = "activity_qs_tiles_tertiary_toggle"
                        )
                    })
                }

                // 5. Inactive QS Tiles
                val defaultInactiveTileBg = if (isEditingDark) {
                    val sv = editingColorScheme.surfaceVariant
                    Color(
                        red = (sv.red * 0.72f).coerceIn(0f, 1f),
                        green = (sv.green * 0.72f).coerceIn(0f, 1f),
                        blue = (sv.blue * 0.72f).coerceIn(0f, 1f),
                        alpha = sv.alpha
                    )
                } else {
                    Color.White
                }
                if (!HiddenFeatureCatalog.isCardHidden("elem_color_inactive_tiles", hiddenIds)) {
                    visibleColorCards.add("elem_color_inactive_tiles" to { cardShape ->
                        ExpandableElementColorCard(
                            featureId = "elem_color_inactive_tiles",
                            title = "5. Inactive QS Tiles",
                            subtitle = "Quick settings inactive tiles background and icons",
                            useCustom = inactiveTilesUseCustom,
                            onUseCustomChange = { PanelPreferences.setInactiveTilesUseCustom(context, isEditingDark, it) },
                            customColor = inactiveTilesCustomColor,
                            onCustomColorChange = { PanelPreferences.setInactiveTilesCustomColor(context, isEditingDark, it) },
                            colorMode = inactiveTilesColorMode,
                            onColorModeChange = { PanelPreferences.setInactiveTilesColorMode(context, isEditingDark, it) },
                            availableModes = allModes,
                            defaultColor = defaultInactiveTileBg,
                            primaryColor = editingColorScheme.primaryContainer,
                            secondaryColor = editingColorScheme.secondaryContainer,
                            tertiaryColor = editingColorScheme.tertiaryContainer,
                            hue = inactiveTilesHue,
                            sat = inactiveTilesSat,
                            valFactor = inactiveTilesVal,
                            onHsbChange = { h, s, v -> PanelPreferences.setInactiveTilesHsb(context, isEditingDark, h, s, v) },
                            shape = cardShape,
                            testTag = "activity_inactive_tiles_color"
                        )
                    })
                }

                // 6. Sliders/progress bars
                if (!HiddenFeatureCatalog.isCardHidden("elem_color_sliders", hiddenIds)) {
                    visibleColorCards.add("elem_color_sliders" to { cardShape ->
                        ExpandableElementColorCard(
                            featureId = "elem_color_sliders",
                            title = "6. Sliders / Progress Bars",
                            subtitle = "Brightness, volume sliders and progress tracks",
                            useCustom = slidersUseCustom,
                            onUseCustomChange = { PanelPreferences.setSlidersUseCustom(context, isEditingDark, it) },
                            customColor = slidersCustomColor,
                            onCustomColorChange = { PanelPreferences.setSlidersCustomColor(context, isEditingDark, it) },
                            colorMode = slidersColorMode,
                            onColorModeChange = { PanelPreferences.setSlidersColorMode(context, isEditingDark, it) },
                            availableModes = accentModes,
                            defaultColor = editingColorScheme.primary,
                            primaryColor = editingColorScheme.primary,
                            secondaryColor = editingColorScheme.secondary,
                            tertiaryColor = editingColorScheme.tertiary,
                            hue = slidersHue,
                            sat = slidersSat,
                            valFactor = slidersVal,
                            onHsbChange = { h, s, v -> PanelPreferences.setSlidersHsb(context, isEditingDark, h, s, v) },
                            shape = cardShape,
                            testTag = "activity_sliders_tertiary_toggle"
                        )
                    })
                }

                // 7. Misc elements
                if (!HiddenFeatureCatalog.isCardHidden("elem_color_misc_elements", hiddenIds)) {
                    visibleColorCards.add("elem_color_misc_elements" to { cardShape ->
                        ExpandableElementColorCard(
                            featureId = "elem_color_misc_elements",
                            title = "7. Misc Elements",
                            subtitle = "Page indicators, footer buttons, and status icons",
                            useCustom = miscElementsUseCustom,
                            onUseCustomChange = { PanelPreferences.setMiscElementsUseCustom(context, isEditingDark, it) },
                            customColor = miscElementsCustomColor,
                            onCustomColorChange = { PanelPreferences.setMiscElementsCustomColor(context, isEditingDark, it) },
                            colorMode = miscElementsColorMode,
                            onColorModeChange = { PanelPreferences.setMiscElementsColorMode(context, isEditingDark, it) },
                            availableModes = accentModes,
                            defaultColor = editingColorScheme.primary,
                            primaryColor = editingColorScheme.primary,
                            secondaryColor = editingColorScheme.secondary,
                            tertiaryColor = editingColorScheme.tertiary,
                            hue = miscElementsHue,
                            sat = miscElementsSat,
                            valFactor = miscElementsVal,
                            onHsbChange = { h, s, v -> PanelPreferences.setMiscElementsHsb(context, isEditingDark, h, s, v) },
                            shape = cardShape,
                            testTag = "activity_misc_elements_tertiary_toggle"
                        )
                    })
                }

                // 8. Clear All Button / FAB
                if (!HiddenFeatureCatalog.isCardHidden("elem_color_clear_all", hiddenIds)) {
                    visibleColorCards.add("elem_color_clear_all" to { cardShape ->
                        ExpandableElementColorCard(
                            featureId = "elem_color_clear_all",
                            title = "8. Clear All Button / FAB",
                            subtitle = "Floating Clear All button and notification list footer button",
                            useCustom = clearAllUseCustom,
                            onUseCustomChange = { PanelPreferences.setClearAllUseCustom(context, isEditingDark, it) },
                            customColor = clearAllCustomColor,
                            onCustomColorChange = { PanelPreferences.setClearAllCustomColor(context, isEditingDark, it) },
                            colorMode = clearAllColorMode,
                            onColorModeChange = { PanelPreferences.setClearAllColorMode(context, isEditingDark, it) },
                            availableModes = accentModes,
                            defaultColor = editingColorScheme.primary,
                            primaryColor = editingColorScheme.primary,
                            secondaryColor = editingColorScheme.secondary,
                            tertiaryColor = editingColorScheme.tertiary,
                            hue = clearAllHue,
                            sat = clearAllSat,
                            valFactor = clearAllVal,
                            onHsbChange = { h, s, v -> PanelPreferences.setClearAllHsb(context, isEditingDark, h, s, v) },
                            shape = cardShape,
                            testTag = "activity_clear_all_tertiary_toggle"
                        )
                    })
                }

                // 9. Active Complication Arcs
                if (!HiddenFeatureCatalog.isCardHidden("elem_color_active_comp_arc", hiddenIds)) {
                    visibleColorCards.add("elem_color_active_comp_arc" to { cardShape ->
                        ExpandableElementColorCard(
                            featureId = "elem_color_active_comp_arc",
                            title = "9. Active Complication Arcs",
                            subtitle = "Progress arc, horizontal line, and perimeter bar for active complications",
                            useCustom = activeCompArcUseCustom,
                            onUseCustomChange = { PanelPreferences.setActiveCompArcUseCustom(context, isEditingDark, it) },
                            customColor = activeCompArcCustomColor,
                            onCustomColorChange = { PanelPreferences.setActiveCompArcCustomColor(context, isEditingDark, it) },
                            colorMode = activeCompArcMode,
                            onColorModeChange = { PanelPreferences.setActiveCompArcMode(context, isEditingDark, it) },
                            availableModes = allModes,
                            defaultColor = editingColorScheme.onPrimary,
                            primaryColor = editingColorScheme.primary,
                            secondaryColor = editingColorScheme.secondary,
                            tertiaryColor = editingColorScheme.tertiary,
                            defaultModeShortLabel = "Auto",
                            hue = activeCompArcHue,
                            sat = activeCompArcSat,
                            valFactor = activeCompArcVal,
                            onHsbChange = { h, s, v -> PanelPreferences.setActiveCompArcHsb(context, isEditingDark, h, s, v) },
                            shape = cardShape,
                            testTag = "activity_active_comp_arc_color"
                        )
                    })
                }

                // 10. Inactive Complication Arcs
                if (!HiddenFeatureCatalog.isCardHidden("elem_color_inactive_comp_arc", hiddenIds)) {
                    visibleColorCards.add("elem_color_inactive_comp_arc" to { cardShape ->
                        ExpandableElementColorCard(
                            featureId = "elem_color_inactive_comp_arc",
                            title = "10. Inactive Complication Arcs",
                            subtitle = "Progress arc, horizontal line, and perimeter bar for inactive complications",
                            useCustom = inactiveCompArcUseCustom,
                            onUseCustomChange = { PanelPreferences.setInactiveCompArcUseCustom(context, isEditingDark, it) },
                            customColor = inactiveCompArcCustomColor,
                            onCustomColorChange = { PanelPreferences.setInactiveCompArcCustomColor(context, isEditingDark, it) },
                            colorMode = inactiveCompArcMode,
                            onColorModeChange = { PanelPreferences.setInactiveCompArcMode(context, isEditingDark, it) },
                            availableModes = allModes,
                            defaultColor = if (isEditingDark) Color(0xFFCCCCCC) else Color(0xFF555555),
                            primaryColor = editingColorScheme.primary,
                            secondaryColor = editingColorScheme.secondary,
                            tertiaryColor = editingColorScheme.tertiary,
                            defaultModeShortLabel = "Auto",
                            hue = inactiveCompArcHue,
                            sat = inactiveCompArcSat,
                            valFactor = inactiveCompArcVal,
                            onHsbChange = { h, s, v -> PanelPreferences.setInactiveCompArcHsb(context, isEditingDark, h, s, v) },
                            shape = cardShape,
                            testTag = "activity_inactive_comp_arc_color"
                        )
                    })
                }

                // 11. Complication Arc Tracks
                if (!HiddenFeatureCatalog.isCardHidden("elem_color_comp_arc_track", hiddenIds)) {
                    visibleColorCards.add("elem_color_comp_arc_track" to { cardShape ->
                        ExpandableElementColorCard(
                            featureId = "elem_color_comp_arc_track",
                            title = "11. Complication Arc Tracks",
                            subtitle = "Unfilled background track behind complication arcs, lines, and perimeter bars",
                            useCustom = compArcTrackUseCustom,
                            onUseCustomChange = { PanelPreferences.setCompArcTrackUseCustom(context, isEditingDark, it) },
                            customColor = compArcTrackCustomColor,
                            onCustomColorChange = { PanelPreferences.setCompArcTrackCustomColor(context, isEditingDark, it) },
                            colorMode = compArcTrackMode,
                            onColorModeChange = { PanelPreferences.setCompArcTrackMode(context, isEditingDark, it) },
                            availableModes = allModes,
                            defaultColor = (if (isEditingDark) Color.White else Color.Black).copy(alpha = 0.22f),
                            primaryColor = editingColorScheme.primary.copy(alpha = 0.30f),
                            secondaryColor = editingColorScheme.secondary.copy(alpha = 0.30f),
                            tertiaryColor = editingColorScheme.tertiary.copy(alpha = 0.30f),
                            defaultModeShortLabel = "Auto",
                            hue = compArcTrackHue,
                            sat = compArcTrackSat,
                            valFactor = compArcTrackVal,
                            onHsbChange = { h, s, v -> PanelPreferences.setCompArcTrackHsb(context, isEditingDark, h, s, v) },
                            shape = cardShape,
                            testTag = "activity_comp_arc_track_color"
                        )
                    })
                }

                visibleColorCards.forEachIndexed { index, (_, renderCard) ->
                    renderCard(CustomizeCardShapes.getShapeForIndex(index, visibleColorCards.size))
                }
            }

            val disableMonetNotifText by PanelPreferences.disableMonetNotifText.collectAsState()
            val showDisableMonetNotifText = !HiddenFeatureCatalog.isCardHidden("disable_monet_notif_text_card", hiddenIds)

            // Disable Monet for Notifications Text Option Card
            if (showDisableMonetNotifText) {
                SettingsCard(
                    shape = CustomizeCardShapes.SingleCard,
                    onClick = { PanelPreferences.setDisableMonetNotifText(context, !disableMonetNotifText) },
                    featureId = "disable_monet_notif_text_card",
                    featureTitle = "Disable Monet for Notification Text",
                    modifier = Modifier.testTag("disable_monet_notif_text_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Disable Monet for Notification Text",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Disables Monet colors for notification text and the 'Notifications' section label (black text in light mode, white text in dark mode). Main QS header labels are not affected.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = disableMonetNotifText,
                            onCheckedChange = { PanelPreferences.setDisableMonetNotifText(context, it) },
                            modifier = Modifier.testTag("disable_monet_notif_text_switch")
                        )
                    }
                }
            }

            val anyColorCardVisible = !HiddenFeatureCatalog.isCardHidden("elem_color_surface_bg", hiddenIds) ||
                !HiddenFeatureCatalog.isCardHidden("elem_color_panel_container", hiddenIds) ||
                !HiddenFeatureCatalog.isCardHidden("elem_color_header_gradient", hiddenIds) ||
                !HiddenFeatureCatalog.isCardHidden("elem_color_qs_tiles", hiddenIds) ||
                !HiddenFeatureCatalog.isCardHidden("elem_color_inactive_tiles", hiddenIds) ||
                !HiddenFeatureCatalog.isCardHidden("elem_color_sliders", hiddenIds) ||
                !HiddenFeatureCatalog.isCardHidden("elem_color_misc_elements", hiddenIds) ||
                !HiddenFeatureCatalog.isCardHidden("elem_color_clear_all", hiddenIds) ||
                !HiddenFeatureCatalog.isCardHidden("elem_color_active_comp_arc", hiddenIds) ||
                !HiddenFeatureCatalog.isCardHidden("elem_color_inactive_comp_arc", hiddenIds) ||
                !HiddenFeatureCatalog.isCardHidden("elem_color_comp_arc_track", hiddenIds)

            if (!anyColorCardVisible && !showDisableMonetNotifText) {
                AllSectionFeaturesHiddenCard(sectionTitle = "Element Colors")
            }
        }
    }

    if (showHiddenDialog) {
        HiddenFeaturesDialog(onDismiss = { showHiddenDialog = false })
    }
}

@Composable
private fun ColorModeSegmentedBar(
    selectedMode: ComplicationArcMonetMode,
    useCustom: Boolean,
    availableModes: List<ComplicationArcMonetMode>,
    defaultLabel: String,
    onModeSelected: (ComplicationArcMonetMode) -> Unit,
    onCustomSelected: () -> Unit,
    testTagPrefix: String
) {
    val totalSegments = availableModes.size + 1
    val labelFontSize = if (totalSegments >= 5) 10.sp else 11.sp

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Color Mode",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("${testTagPrefix}_color_mode_bar"),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            availableModes.forEachIndexed { idx, mode ->
                val selected = !useCustom && selectedMode == mode
                val segShape = if (selected) {
                    RoundedCornerShape(percent = 50)
                } else {
                    val outerCorner = CornerSize(50)
                    val innerCorner = CornerSize(4.dp)
                    RoundedCornerShape(
                        topStart = if (idx == 0) outerCorner else innerCorner,
                        bottomStart = if (idx == 0) outerCorner else innerCorner,
                        topEnd = innerCorner,
                        bottomEnd = innerCorner
                    )
                }
                val label = if (mode == ComplicationArcMonetMode.DEFAULT) defaultLabel else mode.label
                Surface(
                    onClick = { onModeSelected(mode) },
                    shape = segShape,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = if (selected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("${testTagPrefix}_mode_${mode.id}")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 2.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = labelFontSize,
                            letterSpacing = (-0.2).sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Custom Color Segment at the end of the bar
            val customSelected = useCustom
            val customShape = if (customSelected) {
                RoundedCornerShape(percent = 50)
            } else {
                val outerCorner = CornerSize(50)
                val innerCorner = CornerSize(4.dp)
                RoundedCornerShape(
                    topStart = innerCorner,
                    bottomStart = innerCorner,
                    topEnd = outerCorner,
                    bottomEnd = outerCorner
                )
            }
            Surface(
                onClick = onCustomSelected,
                shape = customShape,
                color = if (customSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                contentColor = if (customSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("${testTagPrefix}_mode_custom")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Custom",
                        fontSize = labelFontSize,
                        letterSpacing = (-0.2).sp,
                        fontWeight = if (customSelected) FontWeight.Bold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandableElementColorCard(
    featureId: String,
    title: String,
    subtitle: String,
    useCustom: Boolean,
    onUseCustomChange: (Boolean) -> Unit,
    customColor: Int,
    onCustomColorChange: (Int) -> Unit,
    colorMode: ComplicationArcMonetMode,
    onColorModeChange: (ComplicationArcMonetMode) -> Unit,
    availableModes: List<ComplicationArcMonetMode>,
    defaultColor: Color,
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color,
    defaultModeShortLabel: String = "Default",
    hue: Float,
    sat: Float,
    valFactor: Float,
    onHsbChange: (hue: Float, sat: Float, valFactor: Float) -> Unit,
    shape: androidx.compose.ui.graphics.Shape,
    testTag: String
) {
    var isExpanded by remember { mutableStateOf(false) }

    val baseThemeColor = when (colorMode) {
        ComplicationArcMonetMode.PRIMARY -> primaryColor
        ComplicationArcMonetMode.SECONDARY -> secondaryColor
        ComplicationArcMonetMode.TERTIARY -> tertiaryColor
        ComplicationArcMonetMode.DEFAULT -> defaultColor
    }
    val activeColor = if (useCustom) {
        Color(customColor)
    } else {
        remember(baseThemeColor, hue, sat, valFactor) {
            applyHsvAdjustments(baseThemeColor, hue, sat, valFactor)
        }
    }

    val statusText = when {
        useCustom -> String.format("Custom Color (#%06X)", (0xFFFFFF and customColor))
        colorMode == ComplicationArcMonetMode.PRIMARY -> "Primary Monet Accent"
        colorMode == ComplicationArcMonetMode.SECONDARY -> "Secondary Monet Accent"
        colorMode == ComplicationArcMonetMode.TERTIARY -> "Tertiary Monet Accent"
        hue != 0f || sat != 1f || valFactor != 1f -> "$defaultModeShortLabel (HSB Adjusted)"
        else -> "$defaultModeShortLabel Color"
    }

    SettingsCard(
        shape = shape,
        featureId = featureId,
        featureTitle = title,
        modifier = Modifier.testTag("${testTag}_card")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pinnableClickable { isExpanded = !isExpanded }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(activeColor)
                                .border(
                                    width = 0.75.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                    shape = CircleShape
                                )
                        )
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse Options" else "Expand Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                val expandedBg = com.example.ui.theme.getSettingsCardBackgroundColor(isSystemInDarkTheme())
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                        thickness = 1.dp
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(expandedBg)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ColorModeSegmentedBar(
                            selectedMode = colorMode,
                            useCustom = useCustom,
                            availableModes = availableModes,
                            defaultLabel = defaultModeShortLabel,
                            onModeSelected = { mode ->
                                if (useCustom) onUseCustomChange(false)
                                onColorModeChange(mode)
                            },
                            onCustomSelected = {
                                if (!useCustom) onUseCustomChange(true)
                            },
                            testTagPrefix = testTag
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                            thickness = 1.dp
                        )

                        if (useCustom) {
                            TwoTabColorPicker(
                                selectedColor = customColor,
                                onColorSelected = onCustomColorChange
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val isCustomized = hue != 0f || sat != 1f || valFactor != 1f

                                // 1. Hue Shift Slider
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Hue Shift",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (hue == 0f) "0° (Default)" else "${if (hue > 0) "+" else ""}${hue.roundToInt()}°",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    ThickElementSlider(
                                        value = hue,
                                        onValueChange = { onHsbChange(it, sat, valFactor) },
                                        valueRange = -180f..180f,
                                        steps = 359,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // 2. Saturation Slider
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Saturation",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${(sat * 100).roundToInt()}%",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    ThickElementSlider(
                                        value = sat,
                                        onValueChange = { onHsbChange(hue, it, valFactor) },
                                        valueRange = 0f..2.0f,
                                        steps = 39,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // 3. Brightness Slider
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Brightness",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${(valFactor * 100).roundToInt()}%",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    ThickElementSlider(
                                        value = valFactor,
                                        onValueChange = { onHsbChange(hue, sat, it) },
                                        valueRange = 0.5f..1.5f,
                                        steps = 19,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                if (isCustomized) {
                                    OutlinedButton(
                                        onClick = { onHsbChange(0f, 1f, 1f) },
                                        modifier = Modifier.align(Alignment.End),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Reset Color Sliders",
                                            fontSize = 12.sp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThickElementSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.surfaceVariant
    Slider(
        value = value.coerceIn(valueRange.start, valueRange.endInclusive),
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
        modifier = modifier,
        thumb = {
            Spacer(modifier = Modifier.size(width = 3.dp, height = 38.dp))
        },
        track = { sliderState ->
            val span = (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                .coerceAtLeast(0.0001f)
            val fraction = ((sliderState.value - sliderState.valueRange.start) / span)
                .coerceIn(0f, 1f)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                val outerR = 8.dp.toPx()
                val innerR = 4.dp.toPx()
                val trackHeightPx = 28.dp.toPx()
                val trackTop = (size.height - trackHeightPx) / 2f
                val trackBottom = trackTop + trackHeightPx

                val thumbHalfPx = 1.5.dp.toPx()
                val gapHalfPx = 4.5.dp.toPx()
                val centerX = size.width * fraction

                val activeEndX = (centerX - gapHalfPx).coerceAtLeast(0f)
                if (activeEndX > 2.dp.toPx()) {
                    val activePath = Path().apply {
                        addRoundRect(
                            RoundRect(
                                left = 0f,
                                top = trackTop,
                                right = activeEndX,
                                bottom = trackBottom,
                                topLeftCornerRadius = CornerRadius(outerR, outerR),
                                bottomLeftCornerRadius = CornerRadius(outerR, outerR),
                                topRightCornerRadius = CornerRadius(innerR, innerR),
                                bottomRightCornerRadius = CornerRadius(innerR, innerR)
                            )
                        )
                    }
                    drawPath(activePath, color = activeColor)
                }

                val inactiveStartX = (centerX + gapHalfPx).coerceAtMost(size.width)
                if (size.width - inactiveStartX > 2.dp.toPx()) {
                    val inactivePath = Path().apply {
                        addRoundRect(
                            RoundRect(
                                left = inactiveStartX,
                                top = trackTop,
                                right = size.width,
                                bottom = trackBottom,
                                topLeftCornerRadius = CornerRadius(innerR, innerR),
                                bottomLeftCornerRadius = CornerRadius(innerR, innerR),
                                topRightCornerRadius = CornerRadius(outerR, outerR),
                                bottomRightCornerRadius = CornerRadius(outerR, outerR)
                            )
                        )
                    }
                    drawPath(inactivePath, color = inactiveColor)
                }

                val handlePath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            left = centerX - thumbHalfPx,
                            top = 0f,
                            right = centerX + thumbHalfPx,
                            bottom = size.height,
                            cornerRadius = CornerRadius(thumbHalfPx, thumbHalfPx)
                        )
                    )
                }
                drawPath(handlePath, color = activeColor)
            }
        }
    )
}
