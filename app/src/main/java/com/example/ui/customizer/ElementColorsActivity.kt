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
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PanelPreferences
import com.example.ui.theme.AppSettingsTheme

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

    // Monet Accent Toggles
    val surfaceBgColorTertiary by PanelPreferences.getSurfaceBgColorTertiary(isEditingDark).collectAsState()
    val panelContainerColorTertiary by PanelPreferences.getPanelContainerColorTertiary(isEditingDark).collectAsState()
    val headerGradientColorTertiary by PanelPreferences.getHeaderGradientColorTertiary(isEditingDark).collectAsState()
    val qsTilesColorTertiary by PanelPreferences.getQsTilesColorTertiary(isEditingDark).collectAsState()
    val inactiveTilesColorTertiary by PanelPreferences.getInactiveTilesColorTertiary(isEditingDark).collectAsState()
    val inactiveTilesColorPrimary by PanelPreferences.getInactiveTilesColorPrimary(isEditingDark).collectAsState()
    val slidersColorTertiary by PanelPreferences.getSlidersColorTertiary(isEditingDark).collectAsState()
    val miscElementsColorTertiary by PanelPreferences.getMiscElementsColorTertiary(isEditingDark).collectAsState()
    val clearAllColorTertiary by PanelPreferences.getClearAllColorTertiary(isEditingDark).collectAsState()

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

    Scaffold(
        topBar = {
            TopAppBar(
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
                            text = "Per-Element Color & Accent Customization",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Customize Monet accents or pick exact custom colors using the 2-tab color picker (Presets & Custom Hue/Chroma/Tone sliders) for ${if (isEditingDark) "Dark" else "Light"} mode.",
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
                    useCustom = surfaceBgUseCustom,
                    onUseCustomChange = { PanelPreferences.setSurfaceBgUseCustom(context, isEditingDark, it) },
                    customColor = surfaceBgCustomColor,
                    onCustomColorChange = { PanelPreferences.setSurfaceBgCustomColor(context, isEditingDark, it) },
                    isTertiary = surfaceBgColorTertiary,
                    onTertiaryChange = { PanelPreferences.setSurfaceBgColorTertiary(context, isEditingDark, it) },
                    shape = CustomizeCardShapes.TopCard,
                    testTag = "activity_surface_bg_tertiary_toggle"
                )

                // 2. Panel containers & notifications
                ExpandableElementColorCard(
                    title = "2. Panel Containers & Notifications",
                    subtitle = "Main Quick Settings card panels, sections, and notification cards",
                    useCustom = panelContainerUseCustom,
                    onUseCustomChange = { PanelPreferences.setPanelContainerUseCustom(context, isEditingDark, it) },
                    customColor = panelContainerCustomColor,
                    onCustomColorChange = { PanelPreferences.setPanelContainerCustomColor(context, isEditingDark, it) },
                    isTertiary = panelContainerColorTertiary,
                    onTertiaryChange = { PanelPreferences.setPanelContainerColorTertiary(context, isEditingDark, it) },
                    shape = CustomizeCardShapes.CenterCard,
                    testTag = "activity_panel_container_tertiary_toggle"
                )

                // 3. Header Gradient
                ExpandableElementColorCard(
                    title = "3. Header Gradient",
                    subtitle = "Top Monet gradient background accent layer",
                    useCustom = headerGradientUseCustom,
                    onUseCustomChange = { PanelPreferences.setHeaderGradientUseCustom(context, isEditingDark, it) },
                    customColor = headerGradientCustomColor,
                    onCustomColorChange = { PanelPreferences.setHeaderGradientCustomColor(context, isEditingDark, it) },
                    isTertiary = headerGradientColorTertiary,
                    onTertiaryChange = { PanelPreferences.setHeaderGradientColorTertiary(context, isEditingDark, it) },
                    shape = CustomizeCardShapes.CenterCard,
                    testTag = "activity_header_gradient_tertiary_toggle"
                )

                // 4. Active QS/QQS Tiles
                ExpandableElementColorCard(
                    title = "4. Active QS Tiles",
                    subtitle = "Quick settings active tiles and icons",
                    useCustom = qsTilesUseCustom,
                    onUseCustomChange = { PanelPreferences.setQsTilesUseCustom(context, isEditingDark, it) },
                    customColor = qsTilesCustomColor,
                    onCustomColorChange = { PanelPreferences.setQsTilesCustomColor(context, isEditingDark, it) },
                    isTertiary = qsTilesColorTertiary,
                    onTertiaryChange = { PanelPreferences.setQsTilesColorTertiary(context, isEditingDark, it) },
                    shape = CustomizeCardShapes.CenterCard,
                    testTag = "activity_qs_tiles_tertiary_toggle"
                )

                // 5. Inactive QS Tiles
                ExpandableInactiveTilesColorCard(
                    title = "5. Inactive QS Tiles",
                    subtitle = "Quick settings inactive tiles background and icons",
                    useCustom = inactiveTilesUseCustom,
                    onUseCustomChange = { PanelPreferences.setInactiveTilesUseCustom(context, isEditingDark, it) },
                    customColor = inactiveTilesCustomColor,
                    onCustomColorChange = { PanelPreferences.setInactiveTilesCustomColor(context, isEditingDark, it) },
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
                    shape = CustomizeCardShapes.CenterCard,
                    testTag = "activity_inactive_tiles_color"
                )

                // 6. Sliders/progress bars
                ExpandableElementColorCard(
                    title = "6. Sliders / Progress Bars",
                    subtitle = "Brightness, volume sliders and progress tracks",
                    useCustom = slidersUseCustom,
                    onUseCustomChange = { PanelPreferences.setSlidersUseCustom(context, isEditingDark, it) },
                    customColor = slidersCustomColor,
                    onCustomColorChange = { PanelPreferences.setSlidersCustomColor(context, isEditingDark, it) },
                    isTertiary = slidersColorTertiary,
                    onTertiaryChange = { PanelPreferences.setSlidersColorTertiary(context, isEditingDark, it) },
                    shape = CustomizeCardShapes.CenterCard,
                    testTag = "activity_sliders_tertiary_toggle"
                )

                // 7. Misc elements
                ExpandableElementColorCard(
                    title = "7. Misc Elements",
                    subtitle = "Page indicators, footer buttons, and status icons",
                    useCustom = miscElementsUseCustom,
                    onUseCustomChange = { PanelPreferences.setMiscElementsUseCustom(context, isEditingDark, it) },
                    customColor = miscElementsCustomColor,
                    onCustomColorChange = { PanelPreferences.setMiscElementsCustomColor(context, isEditingDark, it) },
                    isTertiary = miscElementsColorTertiary,
                    onTertiaryChange = { PanelPreferences.setMiscElementsColorTertiary(context, isEditingDark, it) },
                    shape = CustomizeCardShapes.CenterCard,
                    testTag = "activity_misc_elements_tertiary_toggle"
                )

                // 8. Clear All Button / FAB
                ExpandableElementColorCard(
                    title = "8. Clear All Button / FAB",
                    subtitle = "Floating Clear All button and notification list footer button",
                    useCustom = clearAllUseCustom,
                    onUseCustomChange = { PanelPreferences.setClearAllUseCustom(context, isEditingDark, it) },
                    customColor = clearAllCustomColor,
                    onCustomColorChange = { PanelPreferences.setClearAllCustomColor(context, isEditingDark, it) },
                    isTertiary = clearAllColorTertiary,
                    onTertiaryChange = { PanelPreferences.setClearAllColorTertiary(context, isEditingDark, it) },
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
    useCustom: Boolean,
    onUseCustomChange: (Boolean) -> Unit,
    customColor: Int,
    onCustomColorChange: (Int) -> Unit,
    isTertiary: Boolean,
    onTertiaryChange: (Boolean) -> Unit,
    shape: androidx.compose.ui.graphics.Shape,
    testTag: String
) {
    var isExpanded by remember { mutableStateOf(false) }

    val activeColor = if (useCustom) {
        Color(customColor)
    } else {
        if (isTertiary) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
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
                                .background(activeColor)
                        )
                        Text(
                            text = if (useCustom) {
                                String.format("Custom Color (#%06X)", (0xFFFFFF and customColor))
                            } else {
                                if (isTertiary) "Tertiary Color Accent" else "Primary Color Accent"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeColor
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
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Toggle: Custom Color vs Monet Accent
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = "Set Custom Color",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Use exact custom color instead of Monet theme accent",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = useCustom,
                                onCheckedChange = onUseCustomChange,
                                modifier = Modifier.testTag("${testTag}_use_custom_switch")
                            )
                        }

                        if (useCustom) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                                thickness = 1.dp
                            )
                            TwoTabColorPicker(
                                selectedColor = customColor,
                                onColorSelected = onCustomColorChange
                            )
                        } else {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                                thickness = 1.dp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                    Text(
                                        text = "Tertiary Monet Accent",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Switch between Primary and Tertiary Monet theme accent",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isTertiary,
                                    onCheckedChange = onTertiaryChange,
                                    modifier = Modifier.testTag(testTag)
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
    useCustom: Boolean,
    onUseCustomChange: (Boolean) -> Unit,
    customColor: Int,
    onCustomColorChange: (Int) -> Unit,
    isPrimary: Boolean,
    onPrimaryChange: (Boolean) -> Unit,
    isTertiary: Boolean,
    onTertiaryChange: (Boolean) -> Unit,
    shape: androidx.compose.ui.graphics.Shape,
    testTag: String
) {
    var isExpanded by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()
    val cardContainerColor = MaterialTheme.colorScheme.surfaceVariant
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

    val activeColor = if (useCustom) {
        Color(customColor)
    } else {
        when {
            isTertiary -> MaterialTheme.colorScheme.tertiaryContainer
            isPrimary -> MaterialTheme.colorScheme.primaryContainer
            else -> defaultInactiveBg
        }
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
                                .background(activeColor)
                        )
                        Text(
                            text = if (useCustom) {
                                String.format("Custom Color (#%06X)", (0xFFFFFF and customColor))
                            } else {
                                when {
                                    isTertiary -> "Tertiary Container Accent"
                                    isPrimary -> "Primary Container Accent"
                                    else -> "Default Surface Variant"
                                }
                            },
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
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Custom Color Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = "Set Custom Color",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Use exact custom color for inactive tiles instead of Monet theme accent",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = useCustom,
                                onCheckedChange = onUseCustomChange,
                                modifier = Modifier.testTag("${testTag}_use_custom_switch")
                            )
                        }

                        if (useCustom) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                                thickness = 1.dp
                            )
                            TwoTabColorPicker(
                                selectedColor = customColor,
                                onColorSelected = onCustomColorChange
                            )
                        } else {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                                thickness = 1.dp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                    Text(
                                        text = "Primary Container Accent",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Use Primary Container Monet color for inactive tiles",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isPrimary,
                                    onCheckedChange = onPrimaryChange
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                    Text(
                                        text = "Tertiary Container Accent",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Use Tertiary Container Monet color for inactive tiles",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isTertiary,
                                    onCheckedChange = onTertiaryChange
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
