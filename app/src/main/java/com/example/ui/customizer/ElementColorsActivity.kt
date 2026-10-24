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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val surfaceBgColorTertiary by PanelPreferences.getSurfaceBgColorTertiary(isEditingDark).collectAsState()
    val panelContainerColorTertiary by PanelPreferences.getPanelContainerColorTertiary(isEditingDark).collectAsState()
    val headerGradientColorTertiary by PanelPreferences.getHeaderGradientColorTertiary(isEditingDark).collectAsState()
    val qsTilesColorTertiary by PanelPreferences.getQsTilesColorTertiary(isEditingDark).collectAsState()
    val inactiveTilesColorTertiary by PanelPreferences.getInactiveTilesColorTertiary(isEditingDark).collectAsState()
    val inactiveTilesColorPrimary by PanelPreferences.getInactiveTilesColorPrimary(isEditingDark).collectAsState()
    val slidersColorTertiary by PanelPreferences.getSlidersColorTertiary(isEditingDark).collectAsState()
    val miscElementsColorTertiary by PanelPreferences.getMiscElementsColorTertiary(isEditingDark).collectAsState()
    val clearAllColorTertiary by PanelPreferences.getClearAllColorTertiary(isEditingDark).collectAsState()

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Element Accent Colors",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val unselectedCardBg = com.example.ui.theme.getSettingsCardBackgroundColor(systemInDark)

            // Top Mode Selector Row (No outer card background wrapper)
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

                // Sync Button (Round Icon Button matching selected button color)
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
                shape = CustomizeCardShapes.SingleCard
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            text = "Per-Element Color & Tone Customization",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Customize Primary or Tertiary accents and Hue, Saturation, Brightness sliders for ${if (isEditingDark) "Dark" else "Light"} mode. Use the sync button at the top to copy settings between modes.",
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
                // 1. Surface background
                ExpandableElementColorCard(
                    title = "1. Surface Background",
                    subtitle = "Behind panel containers and background layers",
                    isTertiary = surfaceBgColorTertiary,
                    onCheckedChange = { PanelPreferences.setSurfaceBgColorTertiary(context, isEditingDark, it) },
                    hue = surfaceBgHue,
                    sat = surfaceBgSat,
                    valFactor = surfaceBgVal,
                    onHsbChange = { h, s, v -> PanelPreferences.setSurfaceBgHsb(context, isEditingDark, h, s, v) },
                    shape = CustomizeCardShapes.TopCard,
                    testTag = "activity_surface_bg_tertiary_toggle"
                )

                // 2. Panel containers & notifications
                ExpandableElementColorCard(
                    title = "2. Panel Containers & Notifications",
                    subtitle = "Main Quick Settings card panels, sections, and notification cards",
                    isTertiary = panelContainerColorTertiary,
                    onCheckedChange = { PanelPreferences.setPanelContainerColorTertiary(context, isEditingDark, it) },
                    hue = panelContainerHue,
                    sat = panelContainerSat,
                    valFactor = panelContainerVal,
                    onHsbChange = { h, s, v -> PanelPreferences.setPanelContainerHsb(context, isEditingDark, h, s, v) },
                    shape = CustomizeCardShapes.CenterCard,
                    testTag = "activity_panel_container_tertiary_toggle"
                )

                // 3. Header Gradient
                ExpandableElementColorCard(
                    title = "3. Header Gradient",
                    subtitle = "Top Monet gradient background accent layer",
                    isTertiary = headerGradientColorTertiary,
                    onCheckedChange = { PanelPreferences.setHeaderGradientColorTertiary(context, isEditingDark, it) },
                    hue = headerGradientHue,
                    sat = headerGradientSat,
                    valFactor = headerGradientVal,
                    onHsbChange = { h, s, v -> PanelPreferences.setHeaderGradientHsb(context, isEditingDark, h, s, v) },
                    shape = CustomizeCardShapes.CenterCard,
                    testTag = "activity_header_gradient_tertiary_toggle"
                )

                // 4. Active QS/QQS Tiles
                ExpandableElementColorCard(
                    title = "4. Active QS Tiles",
                    subtitle = "Quick settings active tiles and icons",
                    isTertiary = qsTilesColorTertiary,
                    onCheckedChange = { PanelPreferences.setQsTilesColorTertiary(context, isEditingDark, it) },
                    hue = qsTilesHue,
                    sat = qsTilesSat,
                    valFactor = qsTilesVal,
                    onHsbChange = { h, s, v -> PanelPreferences.setQsTilesHsb(context, isEditingDark, h, s, v) },
                    shape = CustomizeCardShapes.CenterCard,
                    testTag = "activity_qs_tiles_tertiary_toggle"
                )

                // 5. Inactive QS Tiles
                ExpandableInactiveTilesColorCard(
                    title = "5. Inactive QS Tiles",
                    subtitle = "Quick settings inactive tiles background and icons",
                    isPrimary = inactiveTilesColorPrimary,
                    onPrimaryChange = { enabled ->
                        PanelPreferences.setInactiveTilesColorPrimary(context, isEditingDark, enabled)
                        if (enabled) {
                            PanelPreferences.setInactiveTilesColorTertiary(context, isEditingDark, false)
                        }
                    },
                    isTertiary = inactiveTilesColorTertiary,
                    onTertiaryChange = { enabled ->
                        PanelPreferences.setInactiveTilesColorTertiary(context, isEditingDark, enabled)
                        if (enabled) {
                            PanelPreferences.setInactiveTilesColorPrimary(context, isEditingDark, false)
                        }
                    },
                    hue = inactiveTilesHue,
                    sat = inactiveTilesSat,
                    valFactor = inactiveTilesVal,
                    onHsbChange = { h, s, v -> PanelPreferences.setInactiveTilesHsb(context, isEditingDark, h, s, v) },
                    shape = CustomizeCardShapes.CenterCard,
                    testTag = "activity_inactive_tiles_color"
                )

                // 6. Sliders/progress bars
                ExpandableElementColorCard(
                    title = "6. Sliders / Progress Bars",
                    subtitle = "Brightness, volume sliders and progress tracks",
                    isTertiary = slidersColorTertiary,
                    onCheckedChange = { PanelPreferences.setSlidersColorTertiary(context, isEditingDark, it) },
                    hue = slidersHue,
                    sat = slidersSat,
                    valFactor = slidersVal,
                    onHsbChange = { h, s, v -> PanelPreferences.setSlidersHsb(context, isEditingDark, h, s, v) },
                    shape = CustomizeCardShapes.CenterCard,
                    testTag = "activity_sliders_tertiary_toggle"
                )

                // 7. Misc elements
                ExpandableElementColorCard(
                    title = "7. Misc Elements",
                    subtitle = "Page indicators, footer buttons, and status icons",
                    isTertiary = miscElementsColorTertiary,
                    onCheckedChange = { PanelPreferences.setMiscElementsColorTertiary(context, isEditingDark, it) },
                    hue = miscElementsHue,
                    sat = miscElementsSat,
                    valFactor = miscElementsVal,
                    onHsbChange = { h, s, v -> PanelPreferences.setMiscElementsHsb(context, isEditingDark, h, s, v) },
                    shape = CustomizeCardShapes.CenterCard,
                    testTag = "activity_misc_elements_tertiary_toggle"
                )

                // 8. Clear All Button / FAB
                ExpandableElementColorCard(
                    title = "8. Clear All Button / FAB",
                    subtitle = "Floating Clear All button and notification list footer button",
                    isTertiary = clearAllColorTertiary,
                    onCheckedChange = { PanelPreferences.setClearAllColorTertiary(context, isEditingDark, it) },
                    hue = clearAllHue,
                    sat = clearAllSat,
                    valFactor = clearAllVal,
                    onHsbChange = { h, s, v -> PanelPreferences.setClearAllHsb(context, isEditingDark, h, s, v) },
                    shape = CustomizeCardShapes.BottomCard,
                    testTag = "activity_clear_all_tertiary_toggle"
                )
            }

            val disableMonetNotifText by PanelPreferences.disableMonetNotifText.collectAsState()

            // Disable Monet for Notifications Text Option Card
            SettingsCard(
                shape = CustomizeCardShapes.SingleCard,
                onClick = { PanelPreferences.setDisableMonetNotifText(context, !disableMonetNotifText) },
                modifier = Modifier.testTag("disable_monet_notif_text_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
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
    }
}

@Composable
private fun ExpandableElementColorCard(
    title: String,
    subtitle: String,
    isTertiary: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    hue: Float,
    sat: Float,
    valFactor: Float,
    onHsbChange: (hue: Float, sat: Float, valFactor: Float) -> Unit,
    shape: androidx.compose.ui.graphics.Shape,
    testTag: String
) {
    var isExpanded by remember { mutableStateOf(false) }

    val baseThemeColor = if (isTertiary) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    val adjustedColor = remember(baseThemeColor, hue, sat, valFactor) {
        applyHsvAdjustments(baseThemeColor, hue, sat, valFactor)
    }

    SettingsCard(
        shape = shape,
        modifier = Modifier.testTag("${testTag}_card")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
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
                                .background(adjustedColor)
                        )
                        Text(
                            text = if (isTertiary) "Tertiary Color Accent" else "Primary Color Accent",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = adjustedColor
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Switch(
                        checked = isTertiary,
                        onCheckedChange = onCheckedChange,
                        modifier = Modifier.testTag(testTag)
                    )
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse Sliders" else "Expand Sliders",
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
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        Slider(
                            value = hue,
                            onValueChange = { onHsbChange(it, sat, valFactor) },
                            valueRange = -180f..180f,
                            steps = 360,
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
                        Slider(
                            value = sat,
                            onValueChange = { onHsbChange(hue, it, valFactor) },
                            valueRange = 0f..2.0f,
                            steps = 40,
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
                        Slider(
                            value = valFactor,
                            onValueChange = { onHsbChange(hue, sat, it) },
                            valueRange = 0.5f..1.5f,
                            steps = 20,
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

@Composable
private fun ExpandableInactiveTilesColorCard(
    title: String,
    subtitle: String,
    isPrimary: Boolean,
    onPrimaryChange: (Boolean) -> Unit,
    isTertiary: Boolean,
    onTertiaryChange: (Boolean) -> Unit,
    hue: Float,
    sat: Float,
    valFactor: Float,
    onHsbChange: (hue: Float, sat: Float, valFactor: Float) -> Unit,
    shape: androidx.compose.ui.graphics.Shape,
    testTag: String
) {
    var isExpanded by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()
    val defaultInactiveBg = if (isDark) {
        val cardContainerColor = MaterialTheme.colorScheme.surfaceVariant
        androidx.compose.ui.graphics.Color(
            red = (cardContainerColor.red * 0.72f).coerceIn(0f, 1f),
            green = (cardContainerColor.green * 0.72f).coerceIn(0f, 1f),
            blue = (cardContainerColor.blue * 0.72f).coerceIn(0f, 1f),
            alpha = cardContainerColor.alpha
        )
    } else {
        androidx.compose.ui.graphics.Color.White
    }

    val baseThemeColor = when {
        isTertiary -> MaterialTheme.colorScheme.tertiaryContainer
        isPrimary -> MaterialTheme.colorScheme.primaryContainer
        else -> defaultInactiveBg
    }
    val adjustedColor = remember(baseThemeColor, hue, sat, valFactor) {
        applyHsvAdjustments(baseThemeColor, hue, sat, valFactor)
    }

    SettingsCard(
        shape = shape,
        modifier = Modifier.testTag("${testTag}_card")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
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
                                .background(adjustedColor)
                        )
                        Text(
                            text = when {
                                isTertiary -> "Tertiary Color Accent"
                                isPrimary -> "Primary Color Accent"
                                else -> "Default Neutral Background"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPrimary || isTertiary) adjustedColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse Sliders" else "Expand Sliders",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Toggles
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Colorize with Primary Color",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = isPrimary,
                        onCheckedChange = onPrimaryChange,
                        modifier = Modifier.testTag("${testTag}_primary_switch")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Colorize with Tertiary Accent",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = isTertiary,
                        onCheckedChange = onTertiaryChange,
                        modifier = Modifier.testTag("${testTag}_tertiary_switch")
                    )
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
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            Slider(
                                value = hue,
                                onValueChange = { onHsbChange(it, sat, valFactor) },
                                valueRange = -180f..180f,
                                steps = 360,
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
                            Slider(
                                value = sat,
                                onValueChange = { onHsbChange(hue, it, valFactor) },
                                valueRange = 0f..2.0f,
                                steps = 40,
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
                            Slider(
                                value = valFactor,
                                onValueChange = { onHsbChange(hue, sat, it) },
                                valueRange = 0.5f..1.5f,
                                steps = 20,
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
