package com.example.ui.customizer

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Settings
import com.example.data.BatteryPercentageLocation
import com.example.data.DatePosition
import com.example.data.EditButtonPosition
import com.example.data.PanelPreferences
import com.example.data.SettingsIconLocation
import com.example.model.QsGridLayout
import com.example.ui.theme.ExpressiveCardShapes
import com.example.ui.theme.SegmentedButtonShapes
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.background

import androidx.compose.material3.HorizontalDivider

@Composable
fun QsTweaksSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val gridLayout by PanelPreferences.gridLayout.collectAsState()
    val dragHandleLength by PanelPreferences.dragHandleLength.collectAsState()
    val datePosition by PanelPreferences.datePosition.collectAsState()
    val editButtonPosition by PanelPreferences.editButtonPosition.collectAsState()
    val batteryPercentageLocation by PanelPreferences.batteryPercentageLocation.collectAsState()
    val settingsIconLocation by PanelPreferences.settingsIconLocation.collectAsState()
    val hideQqsDragHandle by PanelPreferences.hideQqsDragHandle.collectAsState()
    val hideQqsEmptyNotifLabels by PanelPreferences.hideQqsEmptyNotifLabels.collectAsState()
    val qsTileLabelFontSize by PanelPreferences.qsTileLabelFontSize.collectAsState()
    val qsTileSubtitleFontSize by PanelPreferences.qsTileSubtitleFontSize.collectAsState()
    val hideDateInExpanded by PanelPreferences.hideDateInExpanded.collectAsState()
    val disableLabelsInExpanded by PanelPreferences.disableLabelsInExpanded.collectAsState()
    val collapseExpandOptions by PanelPreferences.collapseExpandOptions.collectAsState()
    val hideBrightnessPercentage by PanelPreferences.hideBrightnessPercentage.collectAsState()
    val hideBrightnessIcon by PanelPreferences.hideBrightnessIcon.collectAsState()
    val showBrightnessPlusMinus by PanelPreferences.showBrightnessPlusMinus.collectAsState()
    val disableClearAllBackground by PanelPreferences.disableClearAllBackground.collectAsState()
    val clearAllFabOffsetX by PanelPreferences.clearAllFabOffsetX.collectAsState()
    val clearAllFabOffsetY by PanelPreferences.clearAllFabOffsetY.collectAsState()
    val clearAllFabSize by PanelPreferences.clearAllFabSize.collectAsState()
    val useClearAllFab by PanelPreferences.useClearAllFab.collectAsState()
    val enablePanelShadows by PanelPreferences.enablePanelShadows.collectAsState()
    val panelShadowElevation by PanelPreferences.panelShadowElevation.collectAsState()
    val panelShadowOffsetY by PanelPreferences.panelShadowOffsetY.collectAsState()

    var fontSizesExpanded by rememberSaveable { mutableStateOf(false) }

    val pinnedIds by PinnedCardsManager.getPinnedCardIds(CustomizeCategory.QS_TWEAKS, context).collectAsState()

    val gridDensityCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("qs_grid_density_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = shape
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.GridView,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = "QS Grid Layout Density",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 17.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Choose column and row dimensions for expanded Quick Settings tiles.",
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val allGrids = QsGridLayout.entries
                    allGrids.forEach { grid ->
                        val isSelected = gridLayout == grid
                        val chipShape = SegmentedButtonShapes.getShape(
                            item = grid,
                            items = allGrids,
                            selectedItem = gridLayout
                        )
                        FilterChip(
                            selected = isSelected,
                            onClick = { PanelPreferences.setGridLayout(context, grid) },
                            label = { Text(grid.label, fontSize = 11.sp) },
                            shape = chipShape,
                            border = null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.background,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.testTag("grid_col_${grid.id}")
                        )
                    }
                }
            }
        }
    }
}

    val dragHandleLengthCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("qs_drag_handle_length_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = shape
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 36.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-16).dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.DragHandle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "Drag Handle Length",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 17.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "${dragHandleLength.toInt()} dp",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "Customize the width of the quick settings expansion drag pill indicator.",
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Slider(
                    value = dragHandleLength,
                    onValueChange = { PanelPreferences.setDragHandleLength(context, it) },
                    valueRange = 20f..80f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    )
                )
            }
        }
    }
}

    val datePositionCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("date_position_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("date_position_card")
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = "Date Position in Header",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 17.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Choose where to place the date display relative to the clock time in the QQS header bar.",
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val allDatePositions = DatePosition.entries
                    allDatePositions.forEach { pos ->
                        val isSelected = datePosition == pos
                        val chipShape = SegmentedButtonShapes.getShape(
                            item = pos,
                            items = allDatePositions,
                            selectedItem = datePosition
                        )
                        FilterChip(
                            selected = isSelected,
                            onClick = { PanelPreferences.setDatePosition(context, pos) },
                            label = { Text(pos.label, fontSize = 10.sp) },
                            shape = chipShape,
                            border = null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.background,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.testTag("date_pos_${pos.id}")
                        )
                    }
                }
            }
        }
    }
}

    val editPencilLocationCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("edit_pencil_location_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = shape
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = "Edit Pencil Icon Location",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 17.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Choose whether the edit (pencil) icon is located in the QS footer, top header, both, or hidden.",
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val allEditPositions = EditButtonPosition.entries
                    allEditPositions.forEach { pos ->
                        val isSelected = editButtonPosition == pos
                        val chipShape = SegmentedButtonShapes.getShape(
                            item = pos,
                            items = allEditPositions,
                            selectedItem = editButtonPosition
                        )
                        FilterChip(
                            selected = isSelected,
                            onClick = { PanelPreferences.setEditButtonPosition(context, pos) },
                            label = { Text(pos.label, fontSize = 10.sp) },
                            shape = chipShape,
                            border = null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.background,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.testTag("edit_pos_${pos.id}")
                        )
                    }
                }
            }
        }
    }
}

    val batteryPercentageLocationCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("battery_percentage_location_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("battery_percentage_location_card")
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.BatteryFull,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = "Battery Percentage Location",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 17.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Choose where to place the battery percentage indicator.",
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val allBatteryLocs = BatteryPercentageLocation.entries
                    allBatteryLocs.forEach { loc ->
                        val isSelected = batteryPercentageLocation == loc
                        val chipShape = SegmentedButtonShapes.getShape(
                            item = loc,
                            items = allBatteryLocs,
                            selectedItem = batteryPercentageLocation
                        )
                        FilterChip(
                            selected = isSelected,
                            onClick = { PanelPreferences.setBatteryPercentageLocation(context, loc) },
                            label = { Text(loc.label, fontSize = 10.sp) },
                            shape = chipShape,
                            border = null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.background,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.testTag("battery_pos_${loc.id}")
                        )
                    }
                }
            }
        }
    }
}

    val settingsIconLocationCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("settings_icon_location_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("settings_icon_location_card")
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = "Settings Icon Location",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 17.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Choose where the system settings icon is placed.",
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val allSettingsLocs = SettingsIconLocation.entries
                    allSettingsLocs.forEach { loc ->
                        val isSelected = settingsIconLocation == loc
                        val chipShape = SegmentedButtonShapes.getShape(
                            item = loc,
                            items = allSettingsLocs,
                            selectedItem = settingsIconLocation
                        )
                        FilterChip(
                            selected = isSelected,
                            onClick = { PanelPreferences.setSettingsIconLocation(context, loc) },
                            label = { Text(loc.label, fontSize = 10.sp) },
                            shape = chipShape,
                            border = null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.background,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.testTag("settings_pos_${loc.id}")
                        )
                    }
                }
            }
        }
    }
}

    val tileFontSizeCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("qs_tile_font_size_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("qs_tile_font_size_card")
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (collapseExpandOptions) Modifier.clickable { fontSizesExpanded = !fontSizesExpanded } else Modifier
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.TextFields,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = "Tile Label & Subtitle Font Size",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 17.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Adjust the typography scale for Quick Settings tile labels and subtitles.",
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (collapseExpandOptions) {
                        IconButton(
                            onClick = { fontSizesExpanded = !fontSizesExpanded },
                            modifier = Modifier.size(36.dp).testTag("toggle_expand_font_size")
                        ) {
                            Icon(
                                imageVector = if (fontSizesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (fontSizesExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = !collapseExpandOptions || fontSizesExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Label Font Size Slider
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Label Font Size",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${String.format("%.1f", qsTileLabelFontSize)} sp",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Slider(
                                value = qsTileLabelFontSize,
                                onValueChange = { PanelPreferences.setQsTileLabelFontSize(context, it) },
                                valueRange = 8f..18f,
                                steps = 19,
                                modifier = Modifier.fillMaxWidth().testTag("tile_label_font_size_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                )
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // 2. Subtitle Font Size Slider
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Subtitle Font Size",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${String.format("%.1f", qsTileSubtitleFontSize)} sp",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Slider(
                                value = qsTileSubtitleFontSize,
                                onValueChange = { PanelPreferences.setQsTileSubtitleFontSize(context, it) },
                                valueRange = 6f..14f,
                                steps = 15,
                                modifier = Modifier.fillMaxWidth().testTag("tile_subtitle_font_size_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

    val hideDateInExpandedCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("hide_date_in_expanded_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = shape
            ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.EventBusy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = "Hide Date in Expanded QS",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 17.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Hide the date display when the Quick Settings panel is fully expanded.",
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = hideDateInExpanded,
                    onCheckedChange = { PanelPreferences.setHideDateInExpanded(context, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("hide_date_in_expanded_toggle")
                )
            }
        }
    }
}

    val disableLabelsInExpandedCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("disable_labels_in_expanded_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = shape
            ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = "Disable Labels in Expanded QS",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 17.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Hide text labels in expanded QS panel and scale tiles bigger automatically.",
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = disableLabelsInExpanded,
                    onCheckedChange = { PanelPreferences.setDisableLabelsInExpanded(context, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("disable_labels_in_expanded_toggle")
                )
            }
        }
    }
}

    val hideQqsDragHandleCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("hide_qqs_drag_handle_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = shape
            ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Remove,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = "Hide QQS Drag Handle",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 17.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Hide the visual drag pill indicator at the bottom of the compact QQS panel.",
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = hideQqsDragHandle,
                    onCheckedChange = { PanelPreferences.setHideQqsDragHandle(context, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("hide_qqs_drag_handle_toggle")
                )
            }
        }
    }
}

    val hideQqsEmptyNotifLabelsCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("hide_qqs_empty_notif_labels_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = shape
            ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = "Disable Empty Notification Labels",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 17.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Hide the 'Notifications' header and 'No new notifications' label in QQS when there are no active notifications.",
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = hideQqsEmptyNotifLabels,
                    onCheckedChange = { PanelPreferences.setHideQqsEmptyNotifLabels(context, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("hide_qqs_empty_notif_labels_toggle")
                )
            }
        }
    }
}

    val hideBrightnessPercentageCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("hide_brightness_percentage_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = shape
            ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.TextFields,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = "Hide Brightness Percentage",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 17.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Hide the percentage text indicator next to the brightness slider.",
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = hideBrightnessPercentage,
                    onCheckedChange = { PanelPreferences.setHideBrightnessPercentage(context, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("hide_brightness_percentage_toggle")
                )
            }
        }
    }
}

    val hideBrightnessIconCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("hide_brightness_icon_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = shape
            ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.BrightnessMedium,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = "Hide Brightness Icon",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 17.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Hide the brightness icon on the left side of the brightness slider.",
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = hideBrightnessIcon,
                    onCheckedChange = { PanelPreferences.setHideBrightnessIcon(context, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("hide_brightness_icon_toggle")
                )
            }
        }
    }
}

    val showBrightnessPlusMinusCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("show_brightness_plus_minus_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = shape
            ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = "Show Brightness +/- Buttons",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 17.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Add minus (-) and plus (+) buttons to the sides of the brightness slider for fine control.",
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = showBrightnessPlusMinus,
                    onCheckedChange = { PanelPreferences.setShowBrightnessPlusMinus(context, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("show_brightness_plus_minus_toggle")
                )
            }
        }
    }
}

    val disableClearAllBgCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("disable_clear_all_bg_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = shape
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = "Disable Clear All Background",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 17.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Remove the background container tint from the notification clear all button.",
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = disableClearAllBackground,
                        onCheckedChange = { PanelPreferences.setDisableClearAllBackground(context, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("disable_clear_all_bg_toggle")
                    )
                }
            }
        }
    }

    val enablePanelShadowsCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("enable_panel_shadows_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = shape
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text(
                                    text = "Panel & Notification Shadows",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 17.sp,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Enable depth shadows under QS panel cards, notification items, and the Clear All button.",
                                    fontSize = 12.sp,
                                    lineHeight = 15.sp,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (enablePanelShadows && (panelShadowElevation != 8f || panelShadowOffsetY != 0f)) {
                                IconButton(
                                    onClick = { PanelPreferences.resetPanelShadowParams(context) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("panel_shadow_reset_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RestartAlt,
                                        contentDescription = "Reset Shadow Parameters",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Switch(
                                checked = enablePanelShadows,
                                onCheckedChange = { PanelPreferences.setEnablePanelShadows(context, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.testTag("enable_panel_shadows_toggle")
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = enablePanelShadows,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            // 1. Shadow Elevation / Size Slider
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Shadow Size / Elevation",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (panelShadowElevation.toInt() == 8) "Default (8 dp)"
                                                   else if (panelShadowElevation < 8) "Subtle Shadow"
                                                   else "Deep Shadow",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${panelShadowElevation.toInt()} dp",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Slider(
                                    value = panelShadowElevation,
                                    onValueChange = { PanelPreferences.setPanelShadowElevation(context, Math.round(it).toFloat()) },
                                    valueRange = 2f..32f,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("panel_shadow_elevation_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                    )
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            // 2. Vertical Offset Slider
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Shadow Vertical Offset",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (panelShadowOffsetY.toInt() == 0) "Default (Centered / 0 dp)"
                                                   else if (panelShadowOffsetY < 0) "Moved Upwards"
                                                   else "Moved Downwards",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${if (panelShadowOffsetY.toInt() > 0) "+" else ""}${panelShadowOffsetY.toInt()} dp",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Slider(
                                    value = panelShadowOffsetY,
                                    onValueChange = { PanelPreferences.setPanelShadowOffsetY(context, Math.round(it).toFloat()) },
                                    valueRange = -20f..20f,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("panel_shadow_offset_y_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    val clearAllFabOffsetsCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("clear_all_fab_offset_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = shape
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text(
                                    text = "Clear All FAB Position & Size",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 17.sp,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Adjust the horizontal position, vertical position, and size of the floating Clear All button.",
                                    fontSize = 12.sp,
                                    lineHeight = 15.sp,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (clearAllFabOffsetX != 0f || clearAllFabOffsetY != 0f || clearAllFabSize != 64) {
                            IconButton(
                                onClick = { PanelPreferences.resetClearAllFabOffsets(context) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("clear_all_fab_reset_offsets_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = "Reset FAB Offsets and Size",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // 1. Horizontal Offset Slider
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Horizontal Offset",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (clearAllFabOffsetX.toInt() == 0) "Default (Centered / Anchored)"
                                           else if (clearAllFabOffsetX < 0) "Shifted Left"
                                           else "Shifted Right",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${if (clearAllFabOffsetX.toInt() > 0) "+" else ""}${clearAllFabOffsetX.toInt()} dp",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Slider(
                            value = clearAllFabOffsetX,
                            onValueChange = { PanelPreferences.setClearAllFabOffsetX(context, Math.round(it).toFloat()) },
                            valueRange = -180f..180f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("clear_all_fab_offset_x_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                            )
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // 2. Vertical Offset Slider
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Vertical Offset",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (clearAllFabOffsetY.toInt() == 0) "Default (Bottom Floating)"
                                           else if (clearAllFabOffsetY < 0) "Lowered towards Edge"
                                           else "Raised Upwards",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${if (clearAllFabOffsetY.toInt() > 0) "+" else ""}${clearAllFabOffsetY.toInt()} dp",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Slider(
                            value = clearAllFabOffsetY,
                            onValueChange = { PanelPreferences.setClearAllFabOffsetY(context, Math.round(it).toFloat()) },
                            valueRange = -40f..180f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("clear_all_fab_offset_y_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                            )
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // 3. FAB Button Size Slider
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "FAB Button Size",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (clearAllFabSize == 64) "Default Size (64 dp)"
                                           else if (clearAllFabSize < 64) "Compact"
                                           else "Large",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "$clearAllFabSize dp",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Slider(
                            value = clearAllFabSize.toFloat(),
                            onValueChange = { PanelPreferences.setClearAllFabSize(context, Math.round(it)) },
                            valueRange = 40f..96f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("clear_all_fab_size_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                            )
                        )
                    }

                    if (!useClearAllFab) {
                        Text(
                            text = "Note: Enable \"Clear All FAB Button\" under Customize → QS Design to display the floating button.",
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ExpressiveCardShapes.CardSpacing)
    ) {
        // 1. Pinned Cards at the very top of Quick Settings Tweaks
        val pinnedCards = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
        if (pinnedIds.contains("qs_grid_density_card")) pinnedCards.add(gridDensityCard)
        if (pinnedIds.contains("qs_drag_handle_length_card")) pinnedCards.add(dragHandleLengthCard)
        if (pinnedIds.contains("date_position_card")) pinnedCards.add(datePositionCard)
        if (pinnedIds.contains("battery_percentage_location_card")) pinnedCards.add(batteryPercentageLocationCard)
        if (pinnedIds.contains("settings_icon_location_card")) pinnedCards.add(settingsIconLocationCard)
        if (pinnedIds.contains("edit_pencil_location_card")) pinnedCards.add(editPencilLocationCard)
        if (pinnedIds.contains("qs_tile_font_size_card")) pinnedCards.add(tileFontSizeCard)
        if (pinnedIds.contains("hide_date_in_expanded_card")) pinnedCards.add(hideDateInExpandedCard)
        if (pinnedIds.contains("disable_labels_in_expanded_card")) pinnedCards.add(disableLabelsInExpandedCard)
        if (pinnedIds.contains("hide_qqs_drag_handle_card")) pinnedCards.add(hideQqsDragHandleCard)
        if (pinnedIds.contains("hide_qqs_empty_notif_labels_card")) pinnedCards.add(hideQqsEmptyNotifLabelsCard)
        if (pinnedIds.contains("hide_brightness_percentage_card")) pinnedCards.add(hideBrightnessPercentageCard)
        if (pinnedIds.contains("hide_brightness_icon_card")) pinnedCards.add(hideBrightnessIconCard)
        if (pinnedIds.contains("show_brightness_plus_minus_card")) pinnedCards.add(showBrightnessPlusMinusCard)
        if (pinnedIds.contains("enable_panel_shadows_card")) pinnedCards.add(enablePanelShadowsCard)
        if (pinnedIds.contains("disable_clear_all_bg_card")) pinnedCards.add(disableClearAllBgCard)
        if (pinnedIds.contains("clear_all_fab_offset_card")) pinnedCards.add(clearAllFabOffsetsCard)

        if (pinnedCards.isNotEmpty()) {
            ListSubheader(
                text = "Pinned",
                icon = androidx.compose.material.icons.Icons.Default.PushPin
            )
            pinnedCards.forEachIndexed { index, card ->
                card(CustomizeCardShapes.getShapeForIndex(index, pinnedCards.size))
            }
        }

        // 2. Unpinned Grid & Tile Density
        if (!pinnedIds.contains("qs_grid_density_card")) {
            CollapsibleSubheaderSection(
                title = "Grid & Tile Density",
                icon = androidx.compose.material.icons.Icons.Default.GridView
            ) {
                gridDensityCard(CustomizeCardShapes.SingleCard)
            }
        }

        // 3. Unpinned Drag Handle Options
        if (!pinnedIds.contains("qs_drag_handle_length_card")) {
            CollapsibleSubheaderSection(
                title = "Drag Handle Options",
                icon = androidx.compose.material.icons.Icons.Default.DragHandle
            ) {
                dragHandleLengthCard(CustomizeCardShapes.SingleCard)
            }
        }

        // 4. Unpinned Header & Controls
        val unpinnedHeaderControls = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
        if (!pinnedIds.contains("date_position_card")) unpinnedHeaderControls.add(datePositionCard)
        if (!pinnedIds.contains("battery_percentage_location_card")) unpinnedHeaderControls.add(batteryPercentageLocationCard)
        if (!pinnedIds.contains("settings_icon_location_card")) unpinnedHeaderControls.add(settingsIconLocationCard)
        if (!pinnedIds.contains("edit_pencil_location_card")) unpinnedHeaderControls.add(editPencilLocationCard)
        if (!pinnedIds.contains("enable_panel_shadows_card")) unpinnedHeaderControls.add(enablePanelShadowsCard)
        if (!pinnedIds.contains("disable_clear_all_bg_card")) unpinnedHeaderControls.add(disableClearAllBgCard)
        if (!pinnedIds.contains("clear_all_fab_offset_card")) unpinnedHeaderControls.add(clearAllFabOffsetsCard)

        if (unpinnedHeaderControls.isNotEmpty()) {
            CollapsibleSubheaderSection(
                title = "Header & Controls",
                icon = androidx.compose.material.icons.Icons.Default.Tune
            ) {
                unpinnedHeaderControls.forEachIndexed { index, card ->
                    card(CustomizeCardShapes.getShapeForIndex(index, unpinnedHeaderControls.size))
                }
            }
        }

        // 5. Unpinned Brightness Controls
        val unpinnedBrightnessControls = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
        if (!pinnedIds.contains("hide_brightness_percentage_card")) unpinnedBrightnessControls.add(hideBrightnessPercentageCard)
        if (!pinnedIds.contains("hide_brightness_icon_card")) unpinnedBrightnessControls.add(hideBrightnessIconCard)
        if (!pinnedIds.contains("show_brightness_plus_minus_card")) unpinnedBrightnessControls.add(showBrightnessPlusMinusCard)

        if (unpinnedBrightnessControls.isNotEmpty()) {
            CollapsibleSubheaderSection(
                title = "Brightness Controls",
                icon = androidx.compose.material.icons.Icons.Default.BrightnessMedium
            ) {
                unpinnedBrightnessControls.forEachIndexed { index, card ->
                    card(CustomizeCardShapes.getShapeForIndex(index, unpinnedBrightnessControls.size))
                }
            }
        }

        // 6. Unpinned Typography & Font Sizes
        val unpinnedTypography = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
        if (!pinnedIds.contains("qs_tile_font_size_card")) unpinnedTypography.add(tileFontSizeCard)
        if (!pinnedIds.contains("hide_date_in_expanded_card")) unpinnedTypography.add(hideDateInExpandedCard)
        if (!pinnedIds.contains("disable_labels_in_expanded_card")) unpinnedTypography.add(disableLabelsInExpandedCard)
        if (!pinnedIds.contains("hide_qqs_drag_handle_card")) unpinnedTypography.add(hideQqsDragHandleCard)
        if (!pinnedIds.contains("hide_qqs_empty_notif_labels_card")) unpinnedTypography.add(hideQqsEmptyNotifLabelsCard)

        if (unpinnedTypography.isNotEmpty()) {
            CollapsibleSubheaderSection(
                title = "Typography & Font Sizes",
                icon = androidx.compose.material.icons.Icons.Default.TextFields
            ) {
                unpinnedTypography.forEachIndexed { index, card ->
                    card(CustomizeCardShapes.getShapeForIndex(index, unpinnedTypography.size))
                }
            }
        }
    }
}
