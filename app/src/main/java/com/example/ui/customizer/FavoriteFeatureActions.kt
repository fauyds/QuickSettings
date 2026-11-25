package com.example.ui.customizer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BatteryPercentageLocation
import com.example.data.ClearAllFabIcon
import com.example.data.ClearAllFabPosition
import com.example.data.DatePosition
import com.example.data.EditButtonPosition
import com.example.data.HeadsUpAction
import com.example.data.MediaNotificationStyle
import com.example.data.PanelPreferences
import com.example.data.ProgressBarStyle
import com.example.data.QsPageIndicatorStyle
import com.example.data.QsPanelStyle
import com.example.data.QsSliderTarget
import com.example.data.SettingsIconLocation
import com.example.data.ThemeManager
import com.example.data.ThemeMode
import com.example.model.QsGridLayout
import com.example.service.HeadsUpOverlayManager
import com.example.service.HeadsUpSnoozeManager
import com.example.service.QuickPanelNotificationListenerService
import com.example.system.NativeTileManager
import com.example.system.PrivilegeEngine
import com.example.ui.shape.QsShapeManager
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.SegmentedButtonShapes
import com.example.ui.theme.getSettingsScreenBackgroundColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Composable
internal fun FeatureSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    switchTestTag: String? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = if (switchTestTag != null) Modifier.testTag(switchTestTag) else Modifier
        )
    }
}

@Composable
internal fun FeatureSliderBlock(
    label: String,
    valueText: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    sliderTestTag: String? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = valueText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (sliderTestTag != null) Modifier.testTag(sliderTestTag) else Modifier)
        )
    }
}

@Composable
internal fun <T> FeatureSegmentedChooser(
    items: List<T>,
    selectedItem: T?,
    itemLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    testTagPrefix: String? = null,
    itemId: ((T) -> String)? = null,
    isTertiarySelected: (T) -> Boolean = { false },
    leadingIcon: (@Composable (T, Boolean) -> Unit)? = null
) {
    val isDark = LocalIsDarkTheme.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (title != null) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = (item == selectedItem)
                val chipShape = SegmentedButtonShapes.getShape(
                    index = index,
                    count = items.size,
                    isSelected = isSelected
                )
                val useTertiary = isSelected && isTertiarySelected(item)
                val selectedContainer = if (useTertiary) {
                    MaterialTheme.colorScheme.tertiaryContainer
                } else {
                    MaterialTheme.colorScheme.primary
                }
                val selectedLabelColor = if (useTertiary) {
                    MaterialTheme.colorScheme.onTertiaryContainer
                } else {
                    MaterialTheme.colorScheme.onPrimary
                }
                val tagModifier = if (testTagPrefix != null && itemId != null) {
                    Modifier.testTag("${testTagPrefix}${itemId(item)}")
                } else {
                    Modifier
                }
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelect(item) },
                    shape = chipShape,
                    border = null,
                    leadingIcon = leadingIcon?.let { iconContent -> { iconContent(item, isSelected) } },
                    label = {
                        Text(
                            text = itemLabel(item),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = selectedContainer,
                        selectedLabelColor = selectedLabelColor,
                        selectedLeadingIconColor = selectedLabelColor,
                        containerColor = getSettingsScreenBackgroundColor(isDark),
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = tagModifier
                )
            }
        }
    }
}

@Composable
internal fun RenderBasicFeatureActions(feature: FavoriteFeature, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current

    when (feature) {
        FavoriteFeature.BASIC_THEME_MODE -> {
            val themeMode by PanelPreferences.themeMode.collectAsState()
            val modes = ThemeMode.entries
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                modes.forEachIndexed { index, mode ->
                    val isSelected = themeMode == mode
                    val buttonShape = SegmentedButtonShapes.getShape(
                        index = index,
                        count = modes.size,
                        isSelected = isSelected,
                        innerCorner = 8.dp
                    )
                    Surface(
                        onClick = { PanelPreferences.setThemeMode(context, mode) },
                        shape = buttonShape,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else getSettingsScreenBackgroundColor(isDark),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("feature_theme_mode_${mode.name.lowercase()}")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (mode) {
                                    ThemeMode.SYSTEM -> Icons.Default.SettingsSuggest
                                    ThemeMode.LIGHT -> Icons.Default.WbSunny
                                    ThemeMode.DARK -> Icons.Default.DarkMode
                                },
                                contentDescription = mode.displayName,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        FavoriteFeature.BASIC_COLLAPSE_OPTIONS -> {
            val collapseExpandOptions by PanelPreferences.collapseExpandOptions.collectAsState()
            FeatureSwitchRow(
                label = "Compact Chips View",
                checked = collapseExpandOptions,
                onCheckedChange = { PanelPreferences.setCollapseExpandOptions(context, it) },
                modifier = modifier,
                switchTestTag = "feature_collapse_options_switch"
            )
        }

        FavoriteFeature.BASIC_COLLAPSE_SUBHEADERS -> {
            val collapseExpandSubheaders by PanelPreferences.collapseExpandSubheaders.collectAsState()
            FeatureSwitchRow(
                label = "Collapse All Subheaders",
                checked = collapseExpandSubheaders,
                onCheckedChange = { PanelPreferences.setCollapseExpandSubheaders(context, it) },
                modifier = modifier,
                switchTestTag = "feature_collapse_subheaders_switch"
            )
        }

        FavoriteFeature.BASIC_BACKUP_RESTORE -> {
            var showExportDialog by remember { mutableStateOf(false) }
            var showImportDialog by remember { mutableStateOf(false) }
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showExportDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("feature_basic_export_btn")
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export", fontSize = 12.sp)
                }
                FilledTonalButton(
                    onClick = { showImportDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("feature_basic_import_btn")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Import", fontSize = 12.sp)
                }
            }
            if (showExportDialog) {
                ExportSettingsDialog(onDismiss = { showExportDialog = false })
            }
            if (showImportDialog) {
                ImportSettingsDialog(onDismiss = { showImportDialog = false })
            }
        }

        else -> Unit
    }
}

@Composable
internal fun RenderHeadsUpFeatureActions(feature: FavoriteFeature, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    when (feature) {
        FavoriteFeature.HEADS_UP_MASTER -> {
            val enabled by PanelPreferences.enableHeadsUpOverlay.collectAsState()
            FeatureSwitchRow(
                label = "Custom Heads-Up Banner Engine",
                checked = enabled,
                onCheckedChange = { PanelPreferences.setEnableHeadsUpOverlay(context, it) },
                modifier = modifier,
                switchTestTag = "feature_heads_up_master_switch"
            )
        }

        FavoriteFeature.HEADS_UP_SYSTEM -> {
            val enabled by PanelPreferences.enableSystemHeadsUp.collectAsState()
            FeatureSwitchRow(
                label = "Allow Android System Heads-Up",
                checked = enabled,
                onCheckedChange = { PanelPreferences.setEnableSystemHeadsUp(context, it) },
                modifier = modifier,
                switchTestTag = "feature_heads_up_system_switch"
            )
        }

        FavoriteFeature.HEADS_UP_PERMISSION -> {
            val isGranted = remember { QuickPanelNotificationListenerService.isPermissionGranted(context) }
            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isGranted) "Status: Notification access granted" else "Status: Notification access required",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Button(
                    onClick = { QuickPanelNotificationListenerService.openPermissionSettings(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("feature_heads_up_permission_btn")
                ) {
                    Text(if (isGranted) "Manage Notification Access" else "Grant Notification Access", fontSize = 12.sp)
                }
            }
        }

        FavoriteFeature.HEADS_UP_TEST -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { HeadsUpOverlayManager.showTestHeadsUp(context) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("feature_heads_up_test_notif_btn")
                ) {
                    Text("Notif", fontSize = 12.sp)
                }
                FilledTonalButton(
                    onClick = { HeadsUpOverlayManager.showTestAlarmHeadsUp(context) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("feature_heads_up_test_alarm_btn")
                ) {
                    Text("Alarm", fontSize = 12.sp)
                }
                FilledTonalButton(
                    onClick = { HeadsUpOverlayManager.showTestCallHeadsUp(context) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("feature_heads_up_test_call_btn")
                ) {
                    Text("Call", fontSize = 12.sp)
                }
            }
        }

        FavoriteFeature.HEADS_UP_TIMEOUT -> {
            val timeout by PanelPreferences.headsUpTimeoutSeconds.collectAsState()
            FeatureSliderBlock(
                label = "Auto-Dismiss Timeout",
                valueText = "${timeout}s",
                value = timeout.toFloat(),
                onValueChange = { PanelPreferences.setHeadsUpTimeoutSeconds(context, it.roundToInt()) },
                valueRange = 2f..10f,
                steps = 7,
                modifier = modifier,
                sliderTestTag = "feature_heads_up_timeout_slider"
            )
        }

        FavoriteFeature.HEADS_UP_GESTURES -> {
            val up by PanelPreferences.headsUpSwipeUpAction.collectAsState()
            val down by PanelPreferences.headsUpSwipeDownAction.collectAsState()
            val left by PanelPreferences.headsUpSwipeLeftAction.collectAsState()
            val right by PanelPreferences.headsUpSwipeRightAction.collectAsState()
            val actions = HeadsUpAction.entries

            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureSegmentedChooser(
                    title = "Swipe Up Action",
                    items = actions,
                    selectedItem = up,
                    itemLabel = { it.title },
                    onSelect = { PanelPreferences.setHeadsUpSwipeUpAction(context, it) },
                    testTagPrefix = "feature_hu_up_",
                    itemId = { it.id },
                    isTertiarySelected = { it.isSnoozeAction }
                )
                FeatureSegmentedChooser(
                    title = "Swipe Down Action",
                    items = actions,
                    selectedItem = down,
                    itemLabel = { it.title },
                    onSelect = { PanelPreferences.setHeadsUpSwipeDownAction(context, it) },
                    testTagPrefix = "feature_hu_down_",
                    itemId = { it.id },
                    isTertiarySelected = { it.isSnoozeAction }
                )
                FeatureSegmentedChooser(
                    title = "Swipe Left Action",
                    items = actions,
                    selectedItem = left,
                    itemLabel = { it.title },
                    onSelect = { PanelPreferences.setHeadsUpSwipeLeftAction(context, it) },
                    testTagPrefix = "feature_hu_left_",
                    itemId = { it.id },
                    isTertiarySelected = { it.isSnoozeAction }
                )
                FeatureSegmentedChooser(
                    title = "Swipe Right Action",
                    items = actions,
                    selectedItem = right,
                    itemLabel = { it.title },
                    onSelect = { PanelPreferences.setHeadsUpSwipeRightAction(context, it) },
                    testTagPrefix = "feature_hu_right_",
                    itemId = { it.id },
                    isTertiarySelected = { it.isSnoozeAction }
                )
            }
        }

        FavoriteFeature.HEADS_UP_TAP_ACTIONS -> {
            val singleTap by PanelPreferences.headsUpSingleTapAction.collectAsState()
            val doubleTap by PanelPreferences.headsUpDoubleTapAction.collectAsState()
            val longPress by PanelPreferences.headsUpLongPressAction.collectAsState()
            val actions = HeadsUpAction.entries

            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureSegmentedChooser(
                    title = "Single Tap Action",
                    items = actions,
                    selectedItem = singleTap,
                    itemLabel = { it.title },
                    onSelect = { PanelPreferences.setHeadsUpSingleTapAction(context, it) },
                    testTagPrefix = "feature_hu_tap_",
                    itemId = { it.id },
                    isTertiarySelected = { it.isSnoozeAction }
                )
                FeatureSegmentedChooser(
                    title = "Double Tap Action",
                    items = actions,
                    selectedItem = doubleTap,
                    itemLabel = { it.title },
                    onSelect = { PanelPreferences.setHeadsUpDoubleTapAction(context, it) },
                    testTagPrefix = "feature_hu_dtap_",
                    itemId = { it.id },
                    isTertiarySelected = { it.isSnoozeAction }
                )
                FeatureSegmentedChooser(
                    title = "Long Press Action",
                    items = actions,
                    selectedItem = longPress,
                    itemLabel = { it.title },
                    onSelect = { PanelPreferences.setHeadsUpLongPressAction(context, it) },
                    testTagPrefix = "feature_hu_long_",
                    itemId = { it.id },
                    isTertiarySelected = { it.isSnoozeAction }
                )
            }
        }

        FavoriteFeature.HEADS_UP_SNOOZE_DURATION -> {
            val minutes by PanelPreferences.headsUpSnoozeDurationMinutes.collectAsState()
            var snoozeTick by remember { mutableStateOf(0) }
            val hasActiveSnooze = remember(snoozeTick) {
                HeadsUpSnoozeManager.isGlobalSnoozed() || HeadsUpSnoozeManager.getSnoozedApps().isNotEmpty()
            }
            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FeatureSliderBlock(
                    label = "Snooze Duration",
                    valueText = "$minutes min",
                    value = minutes.toFloat(),
                    onValueChange = { PanelPreferences.setHeadsUpSnoozeDurationMinutes(context, it.roundToInt()) },
                    valueRange = 1f..120f,
                    steps = 119,
                    sliderTestTag = "feature_hu_snooze_slider"
                )
                if (hasActiveSnooze) {
                    OutlinedButton(
                        onClick = {
                            HeadsUpSnoozeManager.clearSnooze()
                            snoozeTick++
                            Toast.makeText(context, "Active heads-up snooze cleared", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear Active Snooze", fontSize = 12.sp)
                    }
                }
            }
        }

        FavoriteFeature.HEADS_UP_DESIGN_TEST -> {
            Button(
                onClick = {
                    HeadsUpOverlayManager.showTestHeadsUp(context)
                    Toast.makeText(context, "Testing Heads-Up Banner...", Toast.LENGTH_SHORT).show()
                },
                modifier = modifier
                    .fillMaxWidth()
                    .testTag("feature_hu_design_test_btn")
            ) {
                Text("Preview Heads-Up Banner", fontSize = 12.sp)
            }
        }

        FavoriteFeature.HEADS_UP_DESIGN_EDGE_TO_EDGE -> {
            val edgeToEdge by PanelPreferences.headsUpEdgeToEdge.collectAsState()
            FeatureSwitchRow(
                label = "Edge-to-Edge Banner",
                checked = edgeToEdge,
                onCheckedChange = { PanelPreferences.setHeadsUpEdgeToEdge(context, it) },
                modifier = modifier,
                switchTestTag = "feature_hu_edge_to_edge_switch"
            )
        }

        FavoriteFeature.HEADS_UP_DESIGN_CORNER_RADIUS -> {
            val radius by PanelPreferences.headsUpCornerRadius.collectAsState()
            FeatureSliderBlock(
                label = "Corner Radius",
                valueText = "$radius dp",
                value = radius.toFloat(),
                onValueChange = { PanelPreferences.setHeadsUpCornerRadius(context, it.roundToInt()) },
                valueRange = 0f..24f,
                steps = 23,
                modifier = modifier,
                sliderTestTag = "feature_hu_corner_slider"
            )
        }

        FavoriteFeature.HEADS_UP_DESIGN_ELEVATION -> {
            val elevation by PanelPreferences.headsUpElevation.collectAsState()
            FeatureSliderBlock(
                label = "Elevation & Shadow",
                valueText = "$elevation dp",
                value = elevation.toFloat(),
                onValueChange = { PanelPreferences.setHeadsUpElevation(context, it.roundToInt()) },
                valueRange = 0f..24f,
                steps = 23,
                modifier = modifier,
                sliderTestTag = "feature_hu_elevation_slider"
            )
        }

        FavoriteFeature.HEADS_UP_DESIGN_TOP_MARGIN -> {
            val topMargin by PanelPreferences.headsUpTopMargin.collectAsState()
            FeatureSliderBlock(
                label = "Top Margin",
                valueText = "$topMargin dp",
                value = topMargin.toFloat(),
                onValueChange = { PanelPreferences.setHeadsUpTopMargin(context, it.roundToInt()) },
                valueRange = 0f..24f,
                steps = 23,
                modifier = modifier,
                sliderTestTag = "feature_hu_top_margin_slider"
            )
        }

        else -> Unit
    }
}

@Composable
internal fun RenderQsDesignFeatureActions(feature: FavoriteFeature, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    when (feature) {
        FavoriteFeature.DESIGN_PANEL_STYLE -> {
            val panelStyle by PanelPreferences.panelStyle.collectAsState()
            FeatureSegmentedChooser(
                title = "Panel Preset Style",
                items = QsPanelStyle.entries,
                selectedItem = panelStyle,
                itemLabel = { it.displayName },
                onSelect = { PanelPreferences.setPanelStyle(context, it) },
                modifier = modifier,
                testTagPrefix = "feature_panel_style_",
                itemId = { it.id }
            )
        }

        FavoriteFeature.DESIGN_QQS_SIZE,
        FavoriteFeature.DESIGN_TILE_SIZE -> {
            val qqsTileSize by PanelPreferences.qqsTileSize.collectAsState()
            FeatureSliderBlock(
                label = "QQS Tile Size",
                valueText = "$qqsTileSize dp",
                value = qqsTileSize.toFloat(),
                onValueChange = { PanelPreferences.setQqsTileSize(context, it.roundToInt()) },
                valueRange = 32f..64f,
                modifier = modifier,
                sliderTestTag = "feature_qqs_size_slider"
            )
        }

        FavoriteFeature.DESIGN_QQS_QUANTITY -> {
            val qqsTileCount by PanelPreferences.qqsTileCount.collectAsState()
            FeatureSliderBlock(
                label = "QQS Icon Quantity",
                valueText = "$qqsTileCount icons",
                value = qqsTileCount.toFloat(),
                onValueChange = { PanelPreferences.setQqsTileCount(context, it.roundToInt()) },
                valueRange = 2f..6f,
                steps = 3,
                modifier = modifier,
                sliderTestTag = "feature_qqs_quantity_slider"
            )
        }

        FavoriteFeature.DESIGN_QQS_2X1_COMPLICATIONS -> {
            val enabled by PanelPreferences.enableQqsComplicationsRow.collectAsState()
            val useHorizontalLine by PanelPreferences.qqs2x1HorizontalLine.collectAsState()
            val usePerimeterProgress by PanelPreferences.qqs2x1PerimeterProgress.collectAsState()
            val tileHeightDp by PanelPreferences.qqs2x1TileHeightDp.collectAsState()
            val activeCornerDp by PanelPreferences.qqs2x1ActiveCornerDp.collectAsState()
            val inactiveCornerDp by PanelPreferences.qqs2x1InactiveCornerDp.collectAsState()

            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureSwitchRow(
                    label = "2x1 Complications QQS Row",
                    checked = enabled,
                    onCheckedChange = { PanelPreferences.setEnableQqsComplicationsRow(context, it) }
                )
                if (enabled) {
                    FeatureSwitchRow(
                        label = "Horizontal Progress Line",
                        checked = useHorizontalLine,
                        onCheckedChange = { PanelPreferences.setQqs2x1HorizontalLine(context, it) }
                    )
                    FeatureSwitchRow(
                        label = "Perimeter Progress Around 2x1 Tiles",
                        checked = usePerimeterProgress,
                        onCheckedChange = { PanelPreferences.setQqs2x1PerimeterProgress(context, it) }
                    )
                    FeatureSliderBlock(
                        label = "Tile Height",
                        valueText = "$tileHeightDp dp",
                        value = tileHeightDp.toFloat(),
                        onValueChange = { PanelPreferences.setQqs2x1TileHeightDp(context, it.roundToInt()) },
                        valueRange = 40f..84f
                    )
                    FeatureSliderBlock(
                        label = "Active Corner",
                        valueText = "${activeCornerDp.roundToInt()} dp",
                        value = activeCornerDp,
                        onValueChange = { PanelPreferences.setQqs2x1ActiveCornerDp(context, kotlin.math.round(it)) },
                        valueRange = 0f..40f
                    )
                    FeatureSliderBlock(
                        label = "Inactive Corner",
                        valueText = "${inactiveCornerDp.roundToInt()} dp",
                        value = inactiveCornerDp,
                        onValueChange = { PanelPreferences.setQqs2x1InactiveCornerDp(context, kotlin.math.round(it)) },
                        valueRange = 0f..40f
                    )
                }
            }
        }

        FavoriteFeature.DESIGN_LEGACY_TILE_SHAPES -> {
            val enabled by PanelPreferences.enableLegacyTileShapes.collectAsState()
            FeatureSwitchRow(
                label = "QS Tile Background Support",
                checked = enabled,
                onCheckedChange = { PanelPreferences.setEnableLegacyTileShapes(context, it) },
                modifier = modifier,
                switchTestTag = "feature_legacy_tile_shapes_switch"
            )
        }

        FavoriteFeature.DESIGN_ACTIVE_SHAPE -> {
            val shapeId by PanelPreferences.enabledTileShapeId.collectAsState()
            val allShapes by QsShapeManager.allShapes.collectAsState()
            FeatureSegmentedChooser(
                title = "Active Shape",
                items = allShapes,
                selectedItem = allShapes.firstOrNull { it.id == shapeId },
                itemLabel = { it.label },
                onSelect = { PanelPreferences.setEnabledTileShapeId(context, it.id) },
                modifier = modifier,
                testTagPrefix = "feature_enabled_tile_shape_",
                itemId = { it.id },
                leadingIcon = { shape, selected ->
                    val composeShape = remember(shape.id) { QsShapeManager.getComposeShape(shape.id) }
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(composeShape)
                            .background(if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                    )
                }
            )
        }

        FavoriteFeature.DESIGN_INACTIVE_SHAPE -> {
            val shapeId by PanelPreferences.disabledTileShapeId.collectAsState()
            val allShapes by QsShapeManager.allShapes.collectAsState()
            FeatureSegmentedChooser(
                title = "Inactive Shape",
                items = allShapes,
                selectedItem = allShapes.firstOrNull { it.id == shapeId },
                itemLabel = { it.label },
                onSelect = { PanelPreferences.setDisabledTileShapeId(context, it.id) },
                modifier = modifier,
                testTagPrefix = "feature_disabled_tile_shape_",
                itemId = { it.id },
                leadingIcon = { shape, selected ->
                    val composeShape = remember(shape.id) { QsShapeManager.getComposeShape(shape.id) }
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(composeShape)
                            .background(if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                    )
                }
            )
        }

        FavoriteFeature.DESIGN_COMPLICATION_ACTIVE_SHAPE -> {
            val shapeId by PanelPreferences.complicationTileShapeId.collectAsState()
            val allShapes by QsShapeManager.allShapes.collectAsState()
            FeatureSegmentedChooser(
                title = "Active Complication Shape",
                items = allShapes,
                selectedItem = allShapes.firstOrNull { it.id == shapeId },
                itemLabel = { it.label },
                onSelect = { PanelPreferences.setComplicationTileShapeId(context, it.id) },
                modifier = modifier,
                testTagPrefix = "feature_comp_active_shape_",
                itemId = { it.id },
                leadingIcon = { shape, selected ->
                    val composeShape = remember(shape.id) { QsShapeManager.getComposeShape(shape.id) }
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(composeShape)
                            .background(if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                    )
                }
            )
        }

        FavoriteFeature.DESIGN_COMPLICATION_INACTIVE_SHAPE -> {
            val shapeId by PanelPreferences.inactiveComplicationTileShapeId.collectAsState()
            val allShapes by QsShapeManager.allShapes.collectAsState()
            FeatureSegmentedChooser(
                title = "Inactive Complication Shape",
                items = allShapes,
                selectedItem = allShapes.firstOrNull { it.id == shapeId },
                itemLabel = { it.label },
                onSelect = { PanelPreferences.setInactiveComplicationTileShapeId(context, it.id) },
                modifier = modifier,
                testTagPrefix = "feature_comp_inactive_shape_",
                itemId = { it.id },
                leadingIcon = { shape, selected ->
                    val composeShape = remember(shape.id) { QsShapeManager.getComposeShape(shape.id) }
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(composeShape)
                            .background(if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                    )
                }
            )
        }

        FavoriteFeature.DESIGN_COMPLICATION_ARC_THICKNESS -> {
            val thickness by PanelPreferences.complicationArcThicknessDp.collectAsState()
            FeatureSliderBlock(
                label = "Progress Arc Thickness",
                valueText = "${String.format("%.1f", thickness)} dp",
                value = thickness,
                onValueChange = { PanelPreferences.setComplicationArcThicknessDp(context, (it * 2f).roundToInt() / 2f) },
                valueRange = 1f..8f,
                steps = 13,
                modifier = modifier
            )
        }

        FavoriteFeature.DESIGN_TILE_STROKE -> {
            val stroke by PanelPreferences.tileOutlineEnabled.collectAsState()
            val compStroke by PanelPreferences.complicationOutlineEnabled.collectAsState()
            val thickness by PanelPreferences.tileOutlineThicknessDp.collectAsState()
            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureSwitchRow(
                    label = "Tile Outline Stroke",
                    checked = stroke,
                    onCheckedChange = { PanelPreferences.setTileOutlineEnabled(context, it) },
                    switchTestTag = "feature_tile_outline_switch"
                )
                FeatureSwitchRow(
                    label = "Complication Outline Stroke",
                    checked = compStroke,
                    onCheckedChange = { PanelPreferences.setComplicationOutlineEnabled(context, it) }
                )
                if (stroke || compStroke) {
                    FeatureSliderBlock(
                        label = "Outline Thickness",
                        valueText = "${String.format("%.1f", thickness)} dp",
                        value = thickness,
                        onValueChange = { PanelPreferences.setTileOutlineThicknessDp(context, (it * 2f).roundToInt() / 2f) },
                        valueRange = 0.5f..6f,
                        steps = 10
                    )
                }
            }
        }

        FavoriteFeature.DESIGN_TILE_GRADIENT -> {
            val gradient by PanelPreferences.showQsTileGradient.collectAsState()
            FeatureSwitchRow(
                label = "Monet Tile Gradient",
                checked = gradient,
                onCheckedChange = { PanelPreferences.setShowQsTileGradient(context, it) },
                modifier = modifier
            )
        }

        FavoriteFeature.DESIGN_PAGE_INDICATOR -> {
            val style by PanelPreferences.qsPageIndicatorStyle.collectAsState()
            val activeDotRadius by PanelPreferences.piActiveDotRadius.collectAsState()
            val inactiveDotRadius by PanelPreferences.piInactiveDotRadius.collectAsState()
            val activeLineThickness by PanelPreferences.piActiveLineThickness.collectAsState()
            val inactiveLineThickness by PanelPreferences.piInactiveLineThickness.collectAsState()
            val activeLineWidth by PanelPreferences.piActiveLineWidth.collectAsState()
            val inactiveLineWidth by PanelPreferences.piInactiveLineWidth.collectAsState()
            val activeSlidingThickness by PanelPreferences.piActiveSlidingThickness.collectAsState()
            val inactiveSlidingThickness by PanelPreferences.piInactiveSlidingThickness.collectAsState()

            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureSegmentedChooser(
                    title = "Indicator Style",
                    items = QsPageIndicatorStyle.entries,
                    selectedItem = style,
                    itemLabel = { it.label },
                    onSelect = { PanelPreferences.setQsPageIndicatorStyle(context, it) },
                    testTagPrefix = "feature_page_indicator_",
                    itemId = { it.id }
                )
                when (style) {
                    QsPageIndicatorStyle.DOTS -> {
                        FeatureSliderBlock(
                            label = "Active Dot Radius",
                            valueText = "${String.format("%.1f", activeDotRadius)} dp",
                            value = activeDotRadius,
                            onValueChange = { PanelPreferences.setPiActiveDotRadius(context, (it * 2f).roundToInt() / 2f) },
                            valueRange = 2f..10f
                        )
                        FeatureSliderBlock(
                            label = "Inactive Dot Radius",
                            valueText = "${String.format("%.1f", inactiveDotRadius)} dp",
                            value = inactiveDotRadius,
                            onValueChange = { PanelPreferences.setPiInactiveDotRadius(context, (it * 2f).roundToInt() / 2f) },
                            valueRange = 1.5f..8f
                        )
                    }
                    QsPageIndicatorStyle.HORIZONTAL_LINES -> {
                        FeatureSliderBlock(
                            label = "Active Line Thickness",
                            valueText = "${String.format("%.1f", activeLineThickness)} dp",
                            value = activeLineThickness,
                            onValueChange = { PanelPreferences.setPiActiveLineThickness(context, (it * 2f).roundToInt() / 2f) },
                            valueRange = 1f..10f
                        )
                        FeatureSliderBlock(
                            label = "Active Line Width",
                            valueText = "${activeLineWidth.roundToInt()} dp",
                            value = activeLineWidth,
                            onValueChange = { PanelPreferences.setPiActiveLineWidth(context, it.roundToInt().toFloat()) },
                            valueRange = 6f..40f
                        )
                        FeatureSliderBlock(
                            label = "Inactive Line Thickness",
                            valueText = "${String.format("%.1f", inactiveLineThickness)} dp",
                            value = inactiveLineThickness,
                            onValueChange = { PanelPreferences.setPiInactiveLineThickness(context, (it * 2f).roundToInt() / 2f) },
                            valueRange = 1f..8f
                        )
                        FeatureSliderBlock(
                            label = "Inactive Line Width",
                            valueText = "${inactiveLineWidth.roundToInt()} dp",
                            value = inactiveLineWidth,
                            onValueChange = { PanelPreferences.setPiInactiveLineWidth(context, it.roundToInt().toFloat()) },
                            valueRange = 4f..32f
                        )
                    }
                    QsPageIndicatorStyle.DOTS_WITH_ACTIVE_LINE -> {
                        FeatureSliderBlock(
                            label = "Active Line Width",
                            valueText = "${activeLineWidth.roundToInt()} dp",
                            value = activeLineWidth,
                            onValueChange = { PanelPreferences.setPiActiveLineWidth(context, it.roundToInt().toFloat()) },
                            valueRange = 6f..40f
                        )
                        FeatureSliderBlock(
                            label = "Active Line Thickness",
                            valueText = "${String.format("%.1f", activeLineThickness)} dp",
                            value = activeLineThickness,
                            onValueChange = { PanelPreferences.setPiActiveLineThickness(context, (it * 2f).roundToInt() / 2f) },
                            valueRange = 1f..10f
                        )
                        FeatureSliderBlock(
                            label = "Inactive Dot Radius",
                            valueText = "${String.format("%.1f", inactiveDotRadius)} dp",
                            value = inactiveDotRadius,
                            onValueChange = { PanelPreferences.setPiInactiveDotRadius(context, (it * 2f).roundToInt() / 2f) },
                            valueRange = 1.5f..8f
                        )
                    }
                    QsPageIndicatorStyle.SLIDING_TRACK -> {
                        FeatureSliderBlock(
                            label = "Active Sliding Bar Thickness",
                            valueText = "${String.format("%.1f", activeSlidingThickness)} dp",
                            value = activeSlidingThickness,
                            onValueChange = { PanelPreferences.setPiActiveSlidingThickness(context, (it * 2f).roundToInt() / 2f) },
                            valueRange = 1f..12f
                        )
                        FeatureSliderBlock(
                            label = "Background Line Thickness",
                            valueText = "${String.format("%.1f", inactiveSlidingThickness)} dp",
                            value = inactiveSlidingThickness,
                            onValueChange = { PanelPreferences.setPiInactiveSlidingThickness(context, (it * 2f).roundToInt() / 2f) },
                            valueRange = 1f..12f
                        )
                    }
                }
            }
        }

        FavoriteFeature.DESIGN_CLEAR_ALL_FAB -> {
            val useFab by PanelPreferences.useClearAllFab.collectAsState()
            val fabPos by PanelPreferences.clearAllFabPosition.collectAsState()
            val fabShapeId by PanelPreferences.clearAllFabShapeId.collectAsState()
            val fabIcon by PanelPreferences.clearAllFabIcon.collectAsState()
            val allShapes by QsShapeManager.allShapes.collectAsState()

            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureSwitchRow(
                    label = "Clear All Icon FAB",
                    checked = useFab,
                    onCheckedChange = { PanelPreferences.setUseClearAllFab(context, it) },
                    switchTestTag = "feature_clear_all_fab_switch"
                )
                if (useFab) {
                    FeatureSegmentedChooser(
                        title = "FAB Anchor Position",
                        items = ClearAllFabPosition.entries,
                        selectedItem = fabPos,
                        itemLabel = { it.label },
                        onSelect = { PanelPreferences.setClearAllFabPosition(context, it) },
                        testTagPrefix = "feature_fab_pos_",
                        itemId = { it.id }
                    )
                    FeatureSegmentedChooser(
                        title = "FAB Shape",
                        items = allShapes,
                        selectedItem = allShapes.firstOrNull { it.id == fabShapeId },
                        itemLabel = { it.label },
                        onSelect = { PanelPreferences.setClearAllFabShapeId(context, it.id) },
                        testTagPrefix = "feature_fab_shape_",
                        itemId = { it.id },
                        leadingIcon = { shape, selected ->
                            val composeShape = remember(shape.id) { QsShapeManager.getComposeShape(shape.id) }
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(composeShape)
                                    .background(if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                            )
                        }
                    )
                    FeatureSegmentedChooser(
                        title = "FAB Icon",
                        items = ClearAllFabIcon.entries,
                        selectedItem = fabIcon,
                        itemLabel = { it.label },
                        onSelect = { PanelPreferences.setClearAllFabIcon(context, it) },
                        testTagPrefix = "feature_fab_icon_",
                        itemId = { it.id },
                        leadingIcon = { icon, selected ->
                            Icon(
                                painter = painterResource(id = icon.drawableRes),
                                contentDescription = null,
                                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }

        FavoriteFeature.DESIGN_MEDIA_STYLE -> {
            val showInNotif by PanelPreferences.showMediaInNotificationContainer.collectAsState()
            val mediaStyle by PanelPreferences.mediaNotificationStyle.collectAsState()
            val currentOption = when {
                !showInNotif -> "Android 11"
                mediaStyle == MediaNotificationStyle.COMPACT -> "Android 6"
                else -> "Android 10"
            }
            val options = listOf("Android 11", "Android 10", "Android 6")
            FeatureSegmentedChooser(
                title = "Media Style",
                items = options,
                selectedItem = currentOption,
                itemLabel = { it },
                onSelect = { option ->
                    when (option) {
                        "Android 11" -> {
                            PanelPreferences.setShowMediaInNotificationContainer(context, false)
                            PanelPreferences.setMediaNotificationStyle(context, MediaNotificationStyle.ANDROID_10)
                        }
                        "Android 10" -> {
                            PanelPreferences.setShowMediaInNotificationContainer(context, true)
                            PanelPreferences.setMediaNotificationStyle(context, MediaNotificationStyle.ANDROID_10)
                        }
                        "Android 6" -> {
                            PanelPreferences.setShowMediaInNotificationContainer(context, true)
                            PanelPreferences.setMediaNotificationStyle(context, MediaNotificationStyle.COMPACT)
                        }
                    }
                },
                modifier = modifier,
                testTagPrefix = "feature_media_style_",
                itemId = { it.lowercase().replace(" ", "_") }
            )
        }

        FavoriteFeature.DESIGN_PROGRESS_STYLE -> {
            val progressStyle by PanelPreferences.progressBarStyle.collectAsState()
            val allStyles = ProgressBarStyle.entries
            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureSegmentedChooser(
                    items = allStyles,
                    selectedItem = progressStyle,
                    itemLabel = { it.label },
                    onSelect = { style ->
                        PanelPreferences.setProgressBarStyle(context, style)
                        QsSliderTarget.entries.forEach { target ->
                            PanelPreferences.setSliderStyleForTarget(context, target, style)
                        }
                    },
                    testTagPrefix = "feature_progress_style_",
                    itemId = { it.id },
                    leadingIcon = { style, selected ->
                        Box(
                            modifier = Modifier.size(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val iconColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                            when (style) {
                                ProgressBarStyle.DEFAULT -> {
                                    Canvas(modifier = Modifier.size(14.dp, 8.dp)) {
                                        drawRoundRect(
                                            color = iconColor.copy(alpha = 0.35f),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                        )
                                        drawRoundRect(
                                            color = iconColor,
                                            size = androidx.compose.ui.geometry.Size(size.width * 0.55f, size.height),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                        )
                                        drawCircle(
                                            color = iconColor,
                                            radius = 3.8.dp.toPx(),
                                            center = Offset(size.width * 0.55f, size.height / 2f)
                                        )
                                    }
                                }
                                ProgressBarStyle.STRAIGHT -> {
                                    Canvas(modifier = Modifier.size(14.dp, 8.dp)) {
                                        drawLine(
                                            color = iconColor.copy(alpha = 0.35f),
                                            start = Offset(0f, size.height / 2f),
                                            end = Offset(size.width, size.height / 2f),
                                            strokeWidth = 2.2.dp.toPx(),
                                            cap = StrokeCap.Round
                                        )
                                        drawLine(
                                            color = iconColor,
                                            start = Offset(0f, size.height / 2f),
                                            end = Offset(size.width * 0.6f, size.height / 2f),
                                            strokeWidth = 2.2.dp.toPx(),
                                            cap = StrokeCap.Round
                                        )
                                        drawCircle(
                                            color = iconColor,
                                            radius = 3.dp.toPx(),
                                            center = Offset(size.width * 0.6f, size.height / 2f)
                                        )
                                    }
                                }
                                ProgressBarStyle.WAVY -> {
                                    Canvas(modifier = Modifier.size(14.dp, 8.dp)) {
                                        val wavePath = Path()
                                        val cy = size.height / 2f
                                        wavePath.moveTo(0f, cy)
                                        val step = size.width / 4f
                                        wavePath.quadraticBezierTo(step * 0.5f, cy - 2.8.dp.toPx(), step, cy)
                                        wavePath.quadraticBezierTo(step * 1.5f, cy + 2.8.dp.toPx(), step * 2f, cy)
                                        wavePath.quadraticBezierTo(step * 2.5f, cy - 2.8.dp.toPx(), step * 3f, cy)
                                        wavePath.lineTo(size.width, cy)
                                        drawPath(
                                            path = wavePath,
                                            color = iconColor,
                                            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }
                                }
                                ProgressBarStyle.ANDROID_12 -> {
                                    Canvas(modifier = Modifier.size(14.dp, 8.dp)) {
                                        drawRoundRect(
                                            color = iconColor.copy(alpha = 0.35f),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                        )
                                        drawRoundRect(
                                            color = iconColor,
                                            size = androidx.compose.ui.geometry.Size(size.width * 0.65f, size.height),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }

        FavoriteFeature.DESIGN_PANEL_CORNERS -> {
            val globalEnabled by PanelPreferences.panelGlobalCornerEnabled.collectAsState()
            val outerCorner by PanelPreferences.panelOuterCorner.collectAsState()
            val innerCorner by PanelPreferences.panelInnerCorner.collectAsState()

            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureSwitchRow(
                    label = "Custom Panel Corner Radii",
                    checked = globalEnabled,
                    onCheckedChange = { PanelPreferences.setPanelGlobalCornerEnabled(context, it) },
                    switchTestTag = "feature_panel_corners_switch"
                )
                if (globalEnabled) {
                    FeatureSliderBlock(
                        label = "Outer Corners",
                        valueText = "${outerCorner.roundToInt()} dp",
                        value = outerCorner,
                        onValueChange = { PanelPreferences.setPanelOuterCorner(context, kotlin.math.round(it)) },
                        valueRange = 0f..36f
                    )
                    FeatureSliderBlock(
                        label = "Inner Adjacent Corners",
                        valueText = "${innerCorner.roundToInt()} dp",
                        value = innerCorner,
                        onValueChange = { PanelPreferences.setPanelInnerCorner(context, kotlin.math.round(it)) },
                        valueRange = 0f..36f
                    )
                }
            }
        }

        FavoriteFeature.DESIGN_PANEL_TOP_RIM -> {
            val enableTopRim by PanelPreferences.enablePanelTopRim.collectAsState()
            val topRimAlpha by PanelPreferences.topRimAlpha.collectAsState()
            val topRimPercent = kotlin.math.round(topRimAlpha * 100f).toInt().coerceIn(5, 30)
            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureSwitchRow(
                    label = "Panel Top Rim",
                    subtitle = "Draws a top-to-bottom gradient rim highlight and panel background gradient.",
                    checked = enableTopRim,
                    onCheckedChange = { PanelPreferences.setEnablePanelTopRim(context, it) },
                    switchTestTag = "feature_panel_top_rim_switch"
                )
                FeatureSliderBlock(
                    label = "Rim Sensitivity",
                    valueText = "${topRimPercent}%",
                    value = topRimPercent.toFloat(),
                    onValueChange = {
                        PanelPreferences.setTopRimAlpha(context, kotlin.math.round(it).coerceIn(5f, 30f) / 100f)
                    },
                    valueRange = 5f..30f,
                    sliderTestTag = "feature_panel_rim_sensitivity_slider"
                )
            }
        }

        FavoriteFeature.DESIGN_TILE_TOP_RIM -> {
            val enableTileRim by PanelPreferences.enableTileTopRim.collectAsState()
            FeatureSwitchRow(
                label = "Tile & Complication Top Rim",
                subtitle = "Draws a top-to-bottom gradient rim and background gradient on QS tiles and complications.",
                checked = enableTileRim,
                onCheckedChange = { PanelPreferences.setEnableTileTopRim(context, it) },
                switchTestTag = "feature_tile_top_rim_switch"
            )
        }

        FavoriteFeature.DESIGN_RIM_SENSITIVITY -> {
            val topRimAlpha by PanelPreferences.topRimAlpha.collectAsState()
            val topRimPercent = kotlin.math.round(topRimAlpha * 100f).toInt().coerceIn(5, 30)
            FeatureSliderBlock(
                label = "Rim Sensitivity",
                valueText = "${topRimPercent}%",
                value = topRimPercent.toFloat(),
                onValueChange = {
                    PanelPreferences.setTopRimAlpha(context, kotlin.math.round(it).coerceIn(5f, 30f) / 100f)
                },
                valueRange = 5f..30f,
                sliderTestTag = "feature_rim_sensitivity_slider"
            )
        }

        FavoriteFeature.DESIGN_HEADER_IMAGE -> {
            val headerImg by PanelPreferences.showCustomHeaderImage.collectAsState()
            val height by PanelPreferences.customHeaderImageHeight.collectAsState()
            val opacity by PanelPreferences.customHeaderImageOpacity.collectAsState()

            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureSwitchRow(
                    label = "Custom Header Image",
                    checked = headerImg,
                    onCheckedChange = { PanelPreferences.setShowCustomHeaderImage(context, it) }
                )
                if (headerImg) {
                    FeatureSliderBlock(
                        label = "Banner Height",
                        valueText = "${height.roundToInt()} dp",
                        value = height,
                        onValueChange = { PanelPreferences.setCustomHeaderImageHeight(context, kotlin.math.round(it)) },
                        valueRange = 60f..180f
                    )
                    FeatureSliderBlock(
                        label = "Image Opacity",
                        valueText = "${(opacity * 100).roundToInt()}%",
                        value = opacity,
                        onValueChange = { PanelPreferences.setCustomHeaderImageOpacity(context, it) },
                        valueRange = 0.1f..1f
                    )
                }
            }
        }

        FavoriteFeature.DESIGN_MONET_GRADIENT -> {
            val monetGradient by PanelPreferences.showCustomHeaderMonetGradient.collectAsState()
            val solidSurface by PanelPreferences.showMonetSurfaceBackground.collectAsState()
            val height by PanelPreferences.customHeaderMonetGradientHeight.collectAsState()

            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureSwitchRow(
                    label = "Header Monet Gradient",
                    checked = monetGradient,
                    onCheckedChange = { PanelPreferences.setShowCustomHeaderMonetGradient(context, it) }
                )
                if (monetGradient) {
                    FeatureSliderBlock(
                        label = "Gradient Height",
                        valueText = "${height.roundToInt()} dp",
                        value = height,
                        onValueChange = { PanelPreferences.setCustomHeaderMonetGradientHeight(context, kotlin.math.round(it)) },
                        valueRange = 40f..180f
                    )
                }
                FeatureSwitchRow(
                    label = "Solid Monet Surface",
                    checked = solidSurface,
                    onCheckedChange = { PanelPreferences.setShowMonetSurfaceBackground(context, it) }
                )
            }
        }

        FavoriteFeature.DESIGN_ELEMENT_COLORS -> {
            Button(
                onClick = {
                    context.startActivity(Intent(context, ElementColorsActivity::class.java))
                },
                modifier = modifier
                    .fillMaxWidth()
                    .testTag("feature_open_element_colors_btn")
            ) {
                Text("Open Element Accent Colors", fontSize = 12.sp)
            }
        }

        FavoriteFeature.DESIGN_HEADER_STATUS_BACKGROUNDS -> {
            val dateBg by PanelPreferences.dateBackgroundEnabled.collectAsState()
            val dateCorner by PanelPreferences.dateBackgroundCorner.collectAsState()
            val batteryBg by PanelPreferences.batteryBackgroundEnabled.collectAsState()
            val batteryCorner by PanelPreferences.batteryBackgroundCorner.collectAsState()
            val clockBg by PanelPreferences.clockBackgroundEnabled.collectAsState()
            val clockCorner by PanelPreferences.clockBackgroundCorner.collectAsState()
            val carrierBg by PanelPreferences.carrierBackgroundEnabled.collectAsState()
            val carrierCorner by PanelPreferences.carrierBackgroundCorner.collectAsState()

            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FeatureSwitchRow("Date Pill Background", dateBg, { PanelPreferences.setDateBackgroundEnabled(context, it) })
                if (dateBg) {
                    FeatureSliderBlock("Date Pill Corner", "${dateCorner.roundToInt()} dp", dateCorner, { PanelPreferences.setDateBackgroundCorner(context, it) }, 0f..24f)
                }
                FeatureSwitchRow("Battery Pill Background", batteryBg, { PanelPreferences.setBatteryBackgroundEnabled(context, it) })
                if (batteryBg) {
                    FeatureSliderBlock("Battery Pill Corner", "${batteryCorner.roundToInt()} dp", batteryCorner, { PanelPreferences.setBatteryBackgroundCorner(context, it) }, 0f..24f)
                }
                FeatureSwitchRow("Clock Pill Background", clockBg, { PanelPreferences.setClockBackgroundEnabled(context, it) })
                if (clockBg) {
                    FeatureSliderBlock("Clock Pill Corner", "${clockCorner.roundToInt()} dp", clockCorner, { PanelPreferences.setClockBackgroundCorner(context, it) }, 0f..24f)
                }
                FeatureSwitchRow("Carrier Pill Background", carrierBg, { PanelPreferences.setCarrierBackgroundEnabled(context, it) })
                if (carrierBg) {
                    FeatureSliderBlock("Carrier Pill Corner", "${carrierCorner.roundToInt()} dp", carrierCorner, { PanelPreferences.setCarrierBackgroundCorner(context, it) }, 0f..24f)
                }
            }
        }

        FavoriteFeature.DESIGN_FOOTER_ICON_BACKGROUNDS -> {
            val iconBg by PanelPreferences.footerIconBackground.collectAsState()
            val outerSpacing by PanelPreferences.footerOuterSpacing.collectAsState()
            val outerCorner by PanelPreferences.footerOuterCorner.collectAsState()
            val innerCorner by PanelPreferences.footerInnerCorner.collectAsState()
            val spacing by PanelPreferences.footerSpacing.collectAsState()

            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureSwitchRow(
                    label = "Footer Icon Backgrounds",
                    checked = iconBg,
                    onCheckedChange = { PanelPreferences.setFooterIconBackground(context, it) }
                )
                FeatureSwitchRow(
                    label = "Outer 3dp Spacing Between Buttons",
                    checked = outerSpacing,
                    onCheckedChange = { PanelPreferences.setFooterOuterSpacing(context, it) }
                )
                if (iconBg) {
                    FeatureSliderBlock("Outer Corner", "${outerCorner.roundToInt()} dp", outerCorner, { PanelPreferences.setFooterOuterCorner(context, it) }, 0f..24f)
                    FeatureSliderBlock("Inner Corner", "${innerCorner.roundToInt()} dp", innerCorner, { PanelPreferences.setFooterInnerCorner(context, it) }, 0f..20f)
                    FeatureSliderBlock("Button Spacing", "${spacing.roundToInt()} dp", spacing, { PanelPreferences.setFooterSpacing(context, it) }, 0f..20f)
                }
            }
        }

        FavoriteFeature.DESIGN_BRIGHTNESS_BOTTOM -> {
            val moveBottom by PanelPreferences.brightnessAtBottom.collectAsState()
            FeatureSwitchRow(
                label = "Brightness Bar at Bottom",
                checked = moveBottom,
                onCheckedChange = { PanelPreferences.setBrightnessAtBottom(context, it) },
                modifier = modifier
            )
        }

        FavoriteFeature.DESIGN_BRIGHTNESS_QQS -> {
            val showQqsBrightness by PanelPreferences.brightnessInQqs.collectAsState()
            FeatureSwitchRow(
                label = "Brightness Bar in QQS",
                checked = showQqsBrightness,
                onCheckedChange = { PanelPreferences.setBrightnessInQqs(context, it) },
                modifier = modifier
            )
        }

        FavoriteFeature.DESIGN_BRIGHTNESS_GRADIENT -> {
            val gradient by PanelPreferences.showBrightnessGradient.collectAsState()
            FeatureSwitchRow(
                label = "Brightness Bar Gradient",
                checked = gradient,
                onCheckedChange = { PanelPreferences.setShowBrightnessGradient(context, it) },
                modifier = modifier
            )
        }

        FavoriteFeature.DESIGN_BRIGHTNESS_CONTROLS_BG -> {
            val bg by PanelPreferences.showBrightnessControlsBackground.collectAsState()
            FeatureSwitchRow(
                label = "Brightness Controls Background",
                checked = bg,
                onCheckedChange = { PanelPreferences.setShowBrightnessControlsBackground(context, it) },
                modifier = modifier,
                switchTestTag = "feature_brightness_controls_bg_switch"
            )
        }

        else -> Unit
    }
}

@Composable
internal fun RenderQsTweaksFeatureActions(feature: FavoriteFeature, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    when (feature) {
        FavoriteFeature.TWEAKS_GRID_LAYOUT -> {
            val gridLayout by PanelPreferences.gridLayout.collectAsState()
            FeatureSegmentedChooser(
                title = "Grid Dimensions",
                items = QsGridLayout.entries,
                selectedItem = gridLayout,
                itemLabel = { it.label },
                onSelect = { PanelPreferences.setGridLayout(context, it) },
                modifier = modifier,
                testTagPrefix = "feature_grid_col_",
                itemId = { it.id }
            )
        }

        FavoriteFeature.TWEAKS_QQS_HORIZONTAL_SCROLL -> {
            val horizontalScroll by PanelPreferences.enableQqsHorizontalScroll.collectAsState()
            FeatureSwitchRow(
                label = "Horizontal Scroll QS Tiles",
                checked = horizontalScroll,
                onCheckedChange = { PanelPreferences.setEnableQqsHorizontalScroll(context, it) },
                modifier = modifier,
                switchTestTag = "feature_qqs_horizontal_scroll_switch"
            )
        }

        FavoriteFeature.TWEAKS_QQS_TILES -> {
            val qqsTileCount by PanelPreferences.qqsTileCount.collectAsState()
            FeatureSliderBlock(
                label = "QQS Tile Count",
                valueText = "$qqsTileCount tiles",
                value = qqsTileCount.toFloat(),
                onValueChange = { PanelPreferences.setQqsTileCount(context, it.roundToInt()) },
                valueRange = 2f..6f,
                steps = 3,
                modifier = modifier
            )
        }

        FavoriteFeature.TWEAKS_DRAG_HANDLE -> {
            val handleLength by PanelPreferences.dragHandleLength.collectAsState()
            FeatureSliderBlock(
                label = "Drag Handle Width",
                valueText = "${handleLength.roundToInt()} dp",
                value = handleLength,
                onValueChange = { PanelPreferences.setDragHandleLength(context, it) },
                valueRange = 20f..80f,
                modifier = modifier
            )
        }

        FavoriteFeature.TWEAKS_DATE_POSITION -> {
            val datePosition by PanelPreferences.datePosition.collectAsState()
            FeatureSegmentedChooser(
                items = DatePosition.entries,
                selectedItem = datePosition,
                itemLabel = { it.label },
                onSelect = { PanelPreferences.setDatePosition(context, it) },
                modifier = modifier,
                testTagPrefix = "feature_date_pos_",
                itemId = { it.id }
            )
        }

        FavoriteFeature.TWEAKS_EDIT_POSITION -> {
            val editButtonPosition by PanelPreferences.editButtonPosition.collectAsState()
            FeatureSegmentedChooser(
                items = EditButtonPosition.entries,
                selectedItem = editButtonPosition,
                itemLabel = { it.label },
                onSelect = { PanelPreferences.setEditButtonPosition(context, it) },
                modifier = modifier,
                testTagPrefix = "feature_edit_pos_",
                itemId = { it.id }
            )
        }

        FavoriteFeature.TWEAKS_COLLAPSE_QQS_FOOTER -> {
            val moveToFooter by PanelPreferences.moveCollapseToQqsFooter.collectAsState()
            FeatureSwitchRow(
                label = "Move Collapse Arrow to QQS Footer",
                checked = moveToFooter,
                onCheckedChange = { PanelPreferences.setMoveCollapseToQqsFooter(context, it) },
                modifier = modifier
            )
        }

        FavoriteFeature.TWEAKS_BATTERY_PERCENT_LOCATION -> {
            val location by PanelPreferences.batteryPercentageLocation.collectAsState()
            FeatureSegmentedChooser(
                title = "Battery Percentage Placement",
                items = BatteryPercentageLocation.entries,
                selectedItem = location,
                itemLabel = { it.label },
                onSelect = { PanelPreferences.setBatteryPercentageLocation(context, it) },
                modifier = modifier,
                testTagPrefix = "feature_battery_pos_",
                itemId = { it.id }
            )
        }

        FavoriteFeature.TWEAKS_SETTINGS_ICON_LOCATION -> {
            val location by PanelPreferences.settingsIconLocation.collectAsState()
            FeatureSegmentedChooser(
                title = "Settings Gear Placement",
                items = SettingsIconLocation.entries,
                selectedItem = location,
                itemLabel = { it.label },
                onSelect = { PanelPreferences.setSettingsIconLocation(context, it) },
                modifier = modifier,
                testTagPrefix = "feature_settings_pos_",
                itemId = { it.id }
            )
        }

        FavoriteFeature.TWEAKS_FONT_SIZES -> {
            val labelSize by PanelPreferences.qsTileLabelFontSize.collectAsState()
            val subtitleSize by PanelPreferences.qsTileSubtitleFontSize.collectAsState()
            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureSliderBlock(
                    label = "Tile Label Font Size",
                    valueText = "${String.format("%.1f", labelSize)} sp",
                    value = labelSize,
                    onValueChange = { PanelPreferences.setQsTileLabelFontSize(context, it) },
                    valueRange = 8f..18f,
                    steps = 19
                )
                FeatureSliderBlock(
                    label = "Tile Subtitle Font Size",
                    valueText = "${String.format("%.1f", subtitleSize)} sp",
                    value = subtitleSize,
                    onValueChange = { PanelPreferences.setQsTileSubtitleFontSize(context, it) },
                    valueRange = 6f..14f,
                    steps = 15
                )
            }
        }

        FavoriteFeature.TWEAKS_HIDE_DATE -> {
            val hideDate by PanelPreferences.hideDateInExpanded.collectAsState()
            FeatureSwitchRow(
                label = "Hide Date in Expanded QS",
                checked = hideDate,
                onCheckedChange = { PanelPreferences.setHideDateInExpanded(context, it) },
                modifier = modifier
            )
        }

        FavoriteFeature.TWEAKS_DISABLE_LABELS -> {
            val disableLabels by PanelPreferences.disableLabelsInExpanded.collectAsState()
            FeatureSwitchRow(
                label = "Disable Tile Labels in Expanded QS",
                checked = disableLabels,
                onCheckedChange = { PanelPreferences.setDisableLabelsInExpanded(context, it) },
                modifier = modifier
            )
        }

        FavoriteFeature.TWEAKS_HIDE_QQS_DRAG -> {
            val hideQqsDragHandle by PanelPreferences.hideQqsDragHandle.collectAsState()
            FeatureSwitchRow(
                label = "Hide QQS Drag Handle",
                checked = hideQqsDragHandle,
                onCheckedChange = { PanelPreferences.setHideQqsDragHandle(context, it) },
                modifier = modifier
            )
        }

        FavoriteFeature.TWEAKS_HIDE_QQS_EMPTY_NOTIF -> {
            val hideEmpty by PanelPreferences.hideQqsEmptyNotifLabels.collectAsState()
            FeatureSwitchRow(
                label = "Hide Empty Notification Hint",
                checked = hideEmpty,
                onCheckedChange = { PanelPreferences.setHideQqsEmptyNotifLabels(context, it) },
                modifier = modifier
            )
        }

        FavoriteFeature.TWEAKS_HIDE_BRIGHTNESS_PERCENT -> {
            val hidePct by PanelPreferences.hideBrightnessPercentage.collectAsState()
            FeatureSwitchRow(
                label = "Hide Brightness Percentage",
                checked = hidePct,
                onCheckedChange = { PanelPreferences.setHideBrightnessPercentage(context, it) },
                modifier = modifier
            )
        }

        FavoriteFeature.TWEAKS_HIDE_BRIGHTNESS_ICON -> {
            val hideIcon by PanelPreferences.hideBrightnessIcon.collectAsState()
            FeatureSwitchRow(
                label = "Hide Auto-Brightness Icon",
                checked = hideIcon,
                onCheckedChange = { PanelPreferences.setHideBrightnessIcon(context, it) },
                modifier = modifier
            )
        }

        FavoriteFeature.TWEAKS_BRIGHTNESS_PLUS_MINUS -> {
            val showPlusMinus by PanelPreferences.showBrightnessPlusMinus.collectAsState()
            FeatureSwitchRow(
                label = "Show Brightness - / + Buttons",
                checked = showPlusMinus,
                onCheckedChange = { PanelPreferences.setShowBrightnessPlusMinus(context, it) },
                modifier = modifier
            )
        }

        FavoriteFeature.TWEAKS_DISABLE_CLEAR_ALL_BG -> {
            val disableBg by PanelPreferences.disableClearAllBackground.collectAsState()
            FeatureSwitchRow(
                label = "Transparent Clear All Background",
                checked = disableBg,
                onCheckedChange = { PanelPreferences.setDisableClearAllBackground(context, it) },
                modifier = modifier
            )
        }

        FavoriteFeature.TWEAKS_DISABLE_CLEAR_ALL_LABEL -> {
            val disableLabel by PanelPreferences.disableClearAllLabel.collectAsState()
            val iconSize by PanelPreferences.clearAllIconSize.collectAsState()
            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureSwitchRow(
                    label = "Icon-Only Clear All Button",
                    checked = disableLabel,
                    onCheckedChange = { PanelPreferences.setDisableClearAllLabel(context, it) }
                )
                if (disableLabel) {
                    FeatureSliderBlock(
                        label = "Clear All Icon Size",
                        valueText = "$iconSize dp",
                        value = iconSize.toFloat(),
                        onValueChange = { PanelPreferences.setClearAllIconSize(context, it.roundToInt()) },
                        valueRange = 16f..48f,
                        steps = 31
                    )
                }
            }
        }

        FavoriteFeature.DESIGN_SIM_CARRIER -> {
            val showCarrier by PanelPreferences.showCarrier.collectAsState()
            FeatureSwitchRow(
                label = "Show SIM Card Carrier",
                checked = showCarrier,
                onCheckedChange = { PanelPreferences.setShowCarrier(context, it) },
                modifier = modifier
            )
        }

        FavoriteFeature.DESIGN_CARRIER_DIVIDER -> {
            val showDivider by PanelPreferences.showCarrierDivider.collectAsState()
            val asLine by PanelPreferences.carrierDividerAsLine.collectAsState()
            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FeatureSwitchRow(
                    label = "Carrier Divider",
                    checked = showDivider,
                    onCheckedChange = { PanelPreferences.setShowCarrierDivider(context, it) }
                )
                if (showDivider) {
                    FeatureSwitchRow(
                        label = "Replace Divider with Solid Line",
                        checked = asLine,
                        onCheckedChange = { PanelPreferences.setCarrierDividerAsLine(context, it) }
                    )
                }
            }
        }

        FavoriteFeature.TWEAKS_HIDE_CARRIER_EXPANDED -> {
            val hideCarrier by PanelPreferences.hideCarrierInExpanded.collectAsState()
            FeatureSwitchRow(
                label = "Hide Carrier in Expanded QS",
                checked = hideCarrier,
                onCheckedChange = { PanelPreferences.setHideCarrierInExpanded(context, it) },
                modifier = modifier
            )
        }

        FavoriteFeature.TWEAKS_PANEL_SHADOWS -> {
            val shadows by PanelPreferences.enablePanelShadows.collectAsState()
            val elevation by PanelPreferences.panelShadowElevation.collectAsState()
            val offsetY by PanelPreferences.panelShadowOffsetY.collectAsState()
            val alpha by PanelPreferences.panelShadowAlpha.collectAsState()

            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureSwitchRow(
                    label = "Enable Panel Shadows",
                    checked = shadows,
                    onCheckedChange = { PanelPreferences.setEnablePanelShadows(context, it) }
                )
                if (shadows) {
                    FeatureSliderBlock(
                        label = "Shadow Elevation",
                        valueText = "${elevation.roundToInt()} dp",
                        value = elevation,
                        onValueChange = { PanelPreferences.setPanelShadowElevation(context, kotlin.math.round(it)) },
                        valueRange = 2f..32f
                    )
                    FeatureSliderBlock(
                        label = "Vertical Offset Y",
                        valueText = "${if (offsetY.toInt() > 0) "+" else ""}${offsetY.toInt()} dp",
                        value = offsetY,
                        onValueChange = { PanelPreferences.setPanelShadowOffsetY(context, kotlin.math.round(it)) },
                        valueRange = -24f..24f
                    )
                    FeatureSliderBlock(
                        label = "Shadow Opacity",
                        valueText = "${(alpha * 100).roundToInt()}%",
                        value = alpha,
                        onValueChange = { PanelPreferences.setPanelShadowAlpha(context, it) },
                        valueRange = 0.05f..0.8f
                    )
                    OutlinedButton(
                        onClick = { PanelPreferences.resetPanelShadowParams(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset Shadow Parameters", fontSize = 12.sp)
                    }
                }
            }
        }

        FavoriteFeature.TWEAKS_CLEAR_ALL_FAB_OFFSETS -> {
            val fabPos by PanelPreferences.clearAllFabPosition.collectAsState()
            val offsetX by PanelPreferences.clearAllFabOffsetX.collectAsState()
            val offsetY by PanelPreferences.clearAllFabOffsetY.collectAsState()
            val size by PanelPreferences.clearAllFabSize.collectAsState()

            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureSegmentedChooser(
                    title = "FAB Anchor",
                    items = ClearAllFabPosition.entries,
                    selectedItem = fabPos,
                    itemLabel = { it.label },
                    onSelect = { PanelPreferences.setClearAllFabPosition(context, it) },
                    testTagPrefix = "feature_tweaks_fab_pos_",
                    itemId = { it.id }
                )
                FeatureSliderBlock(
                    label = "Horizontal Offset X",
                    valueText = "${if (offsetX.toInt() > 0) "+" else ""}${offsetX.toInt()} dp",
                    value = offsetX,
                    onValueChange = { PanelPreferences.setClearAllFabOffsetX(context, kotlin.math.round(it)) },
                    valueRange = -80f..80f
                )
                FeatureSliderBlock(
                    label = "Vertical Offset Y",
                    valueText = "${if (offsetY.toInt() > 0) "+" else ""}${offsetY.toInt()} dp",
                    value = offsetY,
                    onValueChange = { PanelPreferences.setClearAllFabOffsetY(context, kotlin.math.round(it)) },
                    valueRange = -80f..80f
                )
                FeatureSliderBlock(
                    label = "FAB Diameter",
                    valueText = "$size dp",
                    value = size.toFloat(),
                    onValueChange = { PanelPreferences.setClearAllFabSize(context, it.roundToInt()) },
                    valueRange = 40f..96f
                )
                OutlinedButton(
                    onClick = { PanelPreferences.resetClearAllFabOffsets(context) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reset FAB Offsets", fontSize = 12.sp)
                }
            }
        }

        else -> Unit
    }
}

@Composable
internal fun RenderThemesAndMiscAndAboutActions(feature: FavoriteFeature, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(feature) {
        if (feature.category == CustomizeCategory.THEMES) {
            ThemeManager.init(context)
        }
    }

    when (feature) {
        // --- THEMES ---
        FavoriteFeature.THEMES_SAVE -> {
            var themeName by remember { mutableStateOf("") }
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = themeName,
                    onValueChange = { themeName = it },
                    placeholder = { Text("Theme name...", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("feature_theme_name_input")
                )
                Button(
                    onClick = {
                        val saved = ThemeManager.saveCurrentSettingsAsTheme(context, themeName)
                        Toast.makeText(context, "Saved \"${saved.name}\"", Toast.LENGTH_SHORT).show()
                        themeName = ""
                    },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    modifier = Modifier.testTag("feature_theme_save_btn")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Save", fontSize = 12.sp)
                }
            }
        }

        FavoriteFeature.THEMES_TRANSFER -> {
            val savedThemes by ThemeManager.savedThemes.collectAsState()
            var showImportDialog by remember { mutableStateOf(false) }
            var importJson by remember { mutableStateOf("") }
            var importError by remember { mutableStateOf<String?>(null) }

            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showImportDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Import JSON", fontSize = 12.sp)
                }
                FilledTonalButton(
                    onClick = {
                        val target = savedThemes.firstOrNull()
                            ?: ThemeManager.saveCurrentSettingsAsTheme(context, "Quick Snapshot")
                        val json = ThemeManager.exportThemeToJson(target)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("QuickSettings Theme", json))
                        Toast.makeText(context, "Copied \"${target.name}\" JSON", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Copy JSON", fontSize = 12.sp)
                }
            }

            if (showImportDialog) {
                AlertDialog(
                    onDismissRequest = { showImportDialog = false },
                    title = { Text("Import Theme JSON", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = importJson,
                                onValueChange = {
                                    importJson = it
                                    importError = null
                                },
                                placeholder = { Text("Paste theme JSON payload...", fontSize = 12.sp) },
                                minLines = 4,
                                maxLines = 8,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (importError != null) {
                                Text(
                                    text = importError!!,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            val res = ThemeManager.importThemeFromJson(context, importJson)
                            res.onSuccess { imported ->
                                ThemeManager.applyTheme(context, imported)
                                Toast.makeText(context, "Imported & applied \"${imported.name}\"", Toast.LENGTH_SHORT).show()
                                showImportDialog = false
                                importJson = ""
                            }.onFailure { err ->
                                importError = err.message ?: "Invalid theme JSON"
                            }
                        }) {
                            Text("Import")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showImportDialog = false }) { Text("Cancel") }
                    }
                )
            }
        }

        FavoriteFeature.THEMES_SAVED_LIST -> {
            val savedThemes by ThemeManager.savedThemes.collectAsState()
            val activeThemeId by ThemeManager.activeThemeId.collectAsState()
            if (savedThemes.isEmpty()) {
                Text(
                    text = "No saved themes yet. Save your current look above to build your theme library.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    savedThemes.forEach { theme ->
                        val isActive = theme.id == activeThemeId
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isActive) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = theme.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${theme.settingsCount} settings",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    FilledTonalButton(
                                        onClick = {
                                            ThemeManager.applyTheme(context, theme)
                                            Toast.makeText(context, "Applied \"${theme.name}\"", Toast.LENGTH_SHORT).show()
                                        },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(if (isActive) "Active" else "Apply", fontSize = 11.sp)
                                    }
                                    IconButton(
                                        onClick = { ThemeManager.deleteTheme(context, theme.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete theme",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- MISCELLANEOUS ---
        FavoriteFeature.MISC_ANIMATION_SPEED -> {
            val disableAnimations by PanelPreferences.disablePanelAnimations.collectAsState()
            FeatureSwitchRow(
                label = "Disable Panel Animations",
                checked = disableAnimations,
                onCheckedChange = { PanelPreferences.setDisablePanelAnimations(context, it) },
                modifier = modifier,
                switchTestTag = "feature_disable_animations_switch"
            )
        }

        FavoriteFeature.MISC_PRIME_CORE -> {
            val usePrime by PanelPreferences.usePrimeCoreAffinity.collectAsState()
            val coroutineScope = rememberCoroutineScope()
            var hasRoot by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                hasRoot = withContext(Dispatchers.IO) {
                    val engine = NativeTileManager.instance?.privilegeEngine ?: PrivilegeEngine(context)
                    engine.isRootGranted()
                }
            }
            FeatureSwitchRow(
                label = "Pin UI Threads to Prime Core",
                subtitle = if (!hasRoot) "Requires Root access" else "Active on prime core",
                checked = usePrime,
                onCheckedChange = { enabled ->
                    coroutineScope.launch(Dispatchers.IO) {
                        val engine = NativeTileManager.instance?.privilegeEngine ?: PrivilegeEngine(context)
                        if (enabled) {
                            if (engine.isRootGranted()) {
                                engine.applyPrimeCoreAffinity(true)
                                PanelPreferences.setUsePrimeCoreAffinity(context, true)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "High Smoothness Mode enabled via Root!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Root permission required for High Smoothness mode", Toast.LENGTH_SHORT).show()
                                    engine.requestRootPermission()
                                }
                            }
                        } else {
                            engine.applyPrimeCoreAffinity(false)
                            PanelPreferences.setUsePrimeCoreAffinity(context, false)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Standard CPU scheduling restored", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = modifier,
                switchTestTag = "feature_prime_core_switch"
            )
        }

        FavoriteFeature.MISC_VOLUME_PANEL -> {
            val enabled by PanelPreferences.enableCustomVolumePanel.collectAsState()
            FeatureSwitchRow(
                label = "Hide QS/QQS on Volume Key Press",
                checked = enabled,
                onCheckedChange = { PanelPreferences.setEnableCustomVolumePanel(context, it) },
                modifier = modifier,
                switchTestTag = "feature_volume_hide_switch"
            )
        }

        FavoriteFeature.MISC_STATUS_TRIGGER -> {
            val gestures by PanelPreferences.invisibleStatusBarTrigger.collectAsState()
            FeatureSwitchRow(
                label = "Invisible Status Bar Trigger",
                checked = gestures,
                onCheckedChange = { PanelPreferences.setInvisibleStatusBarTrigger(context, it) },
                modifier = modifier
            )
        }

        FavoriteFeature.MISC_PANEL_AUTO_CLOSE -> {
            val autoClose by PanelPreferences.autoSystemPanelClosing.collectAsState()
            FeatureSwitchRow(
                label = "Auto-Close Stock System Panel",
                checked = autoClose,
                onCheckedChange = { PanelPreferences.setAutoSystemPanelClosing(context, it) },
                modifier = modifier
            )
        }

        FavoriteFeature.MISC_REPLACEMENT_DELAY -> {
            val delayMs by PanelPreferences.panelReplacementDelayMs.collectAsState()
            val presets = listOf(0, 50, 150, 300, 500, 1000)
            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureSliderBlock(
                    label = "Replacement Delay",
                    valueText = "$delayMs ms",
                    value = delayMs.toFloat(),
                    onValueChange = { PanelPreferences.setPanelReplacementDelayMs(context, (it / 10).roundToInt() * 10) },
                    valueRange = 0f..1000f,
                    steps = 99
                )
                FeatureSegmentedChooser(
                    items = presets,
                    selectedItem = presets.firstOrNull { it == delayMs },
                    itemLabel = { "${it}ms" },
                    onSelect = { PanelPreferences.setPanelReplacementDelayMs(context, it) }
                )
            }
        }

        FavoriteFeature.MISC_SYSTEM_OVERRIDE -> {
            val sysOverride by PanelPreferences.overrideSystemQs.collectAsState()
            val strictShade by PanelPreferences.strictShadeDetection.collectAsState()
            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FeatureSwitchRow(
                    label = "Stock Shade Auto-Override",
                    checked = sysOverride,
                    onCheckedChange = { PanelPreferences.setOverrideSystemQs(context, it) }
                )
                if (sysOverride) {
                    FeatureSwitchRow(
                        label = "Shade Swipe Only",
                        checked = strictShade,
                        onCheckedChange = { PanelPreferences.setStrictShadeDetection(context, it) }
                    )
                }
            }
        }

        FavoriteFeature.MISC_COOLDOWN -> {
            val cooldown by PanelPreferences.detectorCooldownMs.collectAsState()
            FeatureSliderBlock(
                label = "Detector Cooldown",
                valueText = "$cooldown ms",
                value = cooldown.toFloat(),
                onValueChange = { PanelPreferences.setDetectorCooldownMs(context, (it / 10).roundToInt() * 10) },
                valueRange = 10f..1000f,
                steps = 98,
                modifier = modifier
            )
        }

        FavoriteFeature.MISC_MEDIA_PREVIEW -> {
            val mediaPreview by PanelPreferences.forceShowMediaPreview.collectAsState()
            FeatureSwitchRow(
                label = "Force Media Player Preview",
                checked = mediaPreview,
                onCheckedChange = { PanelPreferences.setForceShowMediaPreview(context, it) },
                modifier = modifier
            )
        }

        FavoriteFeature.MISC_ENDLESS_ROOT -> {
            val endlessRoot by PanelPreferences.endlessRootMode.collectAsState()
            FeatureSwitchRow(
                label = "Endless Root Mode",
                checked = endlessRoot,
                onCheckedChange = { PanelPreferences.setEndlessRootMode(context, it) },
                modifier = modifier
            )
        }

        FavoriteFeature.MISC_MIN_BRIGHTNESS -> {
            val minBrightness by PanelPreferences.minBrightness.collectAsState()
            val presets = listOf(1, 5, 10, 20, 40)
            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureSliderBlock(
                    label = "Minimum Hardware Brightness",
                    valueText = minBrightness.toString(),
                    value = minBrightness.toFloat(),
                    onValueChange = { PanelPreferences.setMinBrightness(context, it.roundToInt().coerceIn(1, 2047)) },
                    valueRange = 1f..2047f
                )
                FeatureSegmentedChooser(
                    items = presets,
                    selectedItem = presets.firstOrNull { it == minBrightness },
                    itemLabel = { it.toString() },
                    onSelect = { PanelPreferences.setMinBrightness(context, it) }
                )
            }
        }

        FavoriteFeature.MISC_MAX_BRIGHTNESS -> {
            val maxBrightness by PanelPreferences.maxBrightness.collectAsState()
            val presets = listOf(255, 511, 1023, 2047)
            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureSliderBlock(
                    label = "Maximum Hardware Brightness",
                    valueText = maxBrightness.toString(),
                    value = maxBrightness.toFloat(),
                    onValueChange = { PanelPreferences.setMaxBrightness(context, it.roundToInt().coerceIn(1, 2047)) },
                    valueRange = 1f..2047f
                )
                FeatureSegmentedChooser(
                    items = presets,
                    selectedItem = presets.firstOrNull { it == maxBrightness },
                    itemLabel = { it.toString() },
                    onSelect = { PanelPreferences.setMaxBrightness(context, it) }
                )
            }
        }

        // --- ABOUT ---
        FavoriteFeature.ABOUT_INFO -> {
            Text(
                text = "QuickSettings v2.0.1 — Material 3 Expressive Quick Settings & Heads-Up customization suite.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        FavoriteFeature.ABOUT_DEVELOPER -> {
            Text(
                text = "Maxim Tarna — Creator & Lead Developer",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        FavoriteFeature.ABOUT_TELEGRAM -> {
            val telegramUrl = "https://t.me/allphonecustomization"
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { runCatching { uriHandler.openUri(telegramUrl) } },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("feature_about_telegram_open_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Join Channel", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Telegram URL", telegramUrl))
                        Toast.makeText(context, "Copied t.me/allphonecustomization", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("feature_about_telegram_copy_btn")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Copy Link", fontSize = 12.sp)
                }
            }
        }

        FavoriteFeature.ABOUT_KEY_FEATURES -> {
            Text(
                text = "16+ custom tile shapes, dynamic Monet accents, interactive progress sliders, floating heads-up banners, and zero-lag panel triggers.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        else -> Unit
    }
}
