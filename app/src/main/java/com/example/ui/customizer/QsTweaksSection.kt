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
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.LineHeightStyle
import com.example.data.BatteryPercentageLocation
import com.example.data.ClearAllFabPosition
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
    val isAndroidOreoStyle by PanelPreferences.isAndroidOreoStyle.collectAsState()
    val isAndroidMarshmallowStyle by PanelPreferences.isAndroidMarshmallowStyle.collectAsState()
    val isAndroidKitKatStyle by PanelPreferences.isAndroidKitKatStyle.collectAsState()
    val isClassicStyle = isAndroidOreoStyle || isAndroidMarshmallowStyle || isAndroidKitKatStyle
    val gridLayout by PanelPreferences.gridLayout.collectAsState()
    val enableQqsHorizontalScroll by PanelPreferences.enableQqsHorizontalScroll.collectAsState()
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
    val clearAllFabPosition by PanelPreferences.clearAllFabPosition.collectAsState()
    val useClearAllFab by PanelPreferences.useClearAllFab.collectAsState()
    val enablePanelShadows by PanelPreferences.enablePanelShadows.collectAsState()
    val panelShadowElevation by PanelPreferences.panelShadowElevation.collectAsState()
    val panelShadowOffsetY by PanelPreferences.panelShadowOffsetY.collectAsState()
    val panelShadowAlpha by PanelPreferences.panelShadowAlpha.collectAsState()
    val showCarrier by PanelPreferences.showCarrier.collectAsState()
    val showCarrierDivider by PanelPreferences.showCarrierDivider.collectAsState()
    val carrierDividerAsLine by PanelPreferences.carrierDividerAsLine.collectAsState()
    val hideCarrierInExpanded by PanelPreferences.hideCarrierInExpanded.collectAsState()

    var fontSizesExpanded by rememberSaveable { mutableStateOf(false) }

    val pinnedIds by PinnedCardsManager.getPinnedCardIds(CustomizeCategory.QS_TWEAKS, context).collectAsState()
    val hiddenIds by PinnedCardsManager.getHiddenCardIds().collectAsState()
    val isUserHidden = { id: String -> HiddenFeatureCatalog.isCardHidden(id, hiddenIds) }

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

                    SettingsTitleDescription(
                        title = "QS Grid Layout Density",
                        description = "Choose column and row dimensions for expanded Quick Settings tiles.",
                        modifier = Modifier.weight(1f),
                        descriptionFontSize = 12.sp
                    )
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

    val enableQqsHorizontalScrollCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("enable_qqs_horizontal_scroll_card", CustomizeCategory.QS_TWEAKS, "QQS Horizontal Scroll") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("enable_qqs_horizontal_scroll_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        SettingsTitleDescription(
                            title = "Horizontal Scroll QS Tiles",
                            description = "Allow horizontally scrolling through all active QQS tiles in collapsed header.",
                            modifier = Modifier.weight(1f),
                            descriptionFontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = enableQqsHorizontalScroll,
                        onCheckedChange = { PanelPreferences.setEnableQqsHorizontalScroll(context, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("enable_qqs_horizontal_scroll_switch")
                    )
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
                Column(verticalArrangement = Arrangement.spacedBy(SettingsTypographyDefaults.TitleDescriptionSpacing)) {
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
                                lineHeight = SettingsTypographyDefaults.compactLineHeight(14.sp),
                                style = SettingsTypographyDefaults.CompactTextStyle,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "${dragHandleLength.toInt()} dp",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = SettingsTypographyDefaults.compactLineHeight(13.sp),
                            style = SettingsTypographyDefaults.CompactTextStyle,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "Customize the width of the quick settings expansion drag pill indicator.",
                        fontSize = 12.sp,
                        lineHeight = SettingsTypographyDefaults.compactLineHeight(12.sp),
                        style = SettingsTypographyDefaults.CompactTextStyle,
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

                    SettingsTitleDescription(
                        title = "Date Position in Header",
                        description = "Choose where to place the date display relative to the clock time in the QQS header bar.",
                        modifier = Modifier.weight(1f),
                        descriptionFontSize = 12.sp
                    )
                }

                if (isClassicStyle) {
                    val isHidden = datePosition == DatePosition.HIDDEN
                    val options = listOf("Shown", "Hidden")
                    val selectedOption = if (isHidden) "Hidden" else "Shown"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        options.forEach { opt ->
                            val isSelected = opt == selectedOption
                            val chipShape = SegmentedButtonShapes.getShape(
                                item = opt,
                                items = options,
                                selectedItem = selectedOption
                            )
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (opt == "Shown") {
                                        PanelPreferences.setDatePosition(context, DatePosition.BELOW_TIME)
                                    } else {
                                        PanelPreferences.setDatePosition(context, DatePosition.HIDDEN)
                                    }
                                },
                                label = { Text(opt, fontSize = 10.sp) },
                                shape = chipShape,
                                border = null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.background,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.testTag("date_pos_oreo_${opt.lowercase()}")
                            )
                        }
                    }
                } else {
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

                    SettingsTitleDescription(
                        title = "Edit Pencil Icon Location",
                        description = "Choose whether the edit (pencil) icon is located in the QS footer, top header, both, or hidden.",
                        modifier = Modifier.weight(1f),
                        descriptionFontSize = 12.sp
                    )
                }

                if (isClassicStyle) {
                    val isHidden = editButtonPosition == EditButtonPosition.NONE
                    val options = listOf("Shown", "Hidden")
                    val selectedOption = if (isHidden) "Hidden" else "Shown"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        options.forEach { opt ->
                            val isSelected = opt == selectedOption
                            val chipShape = SegmentedButtonShapes.getShape(
                                item = opt,
                                items = options,
                                selectedItem = selectedOption
                            )
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (opt == "Shown") {
                                        PanelPreferences.setEditButtonPosition(context, EditButtonPosition.FOOTER)
                                    } else {
                                        PanelPreferences.setEditButtonPosition(context, EditButtonPosition.NONE)
                                    }
                                },
                                label = { Text(opt, fontSize = 10.sp) },
                                shape = chipShape,
                                border = null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.background,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.testTag("edit_pos_oreo_${opt.lowercase()}")
                            )
                        }
                    }
                } else {
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
}

    val moveCollapseToQqsFooterCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("move_collapse_to_qqs_footer_card", CustomizeCategory.QS_TWEAKS, "Collapse Button in QQS Footer") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("move_collapse_to_qqs_footer_card")
            ) {
                val moveCollapseToQqsFooter by PanelPreferences.moveCollapseToQqsFooter.collectAsState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        SettingsTitleDescription(
                            title = "Move Collapse Button to QQS Footer",
                            description = "Show the expand/collapse chevron button directly inside the QQS footer bar",
                            modifier = Modifier.weight(1f),
                            descriptionFontSize = 12.sp
                        )
                    }

                    Switch(
                        checked = moveCollapseToQqsFooter,
                        onCheckedChange = { PanelPreferences.setMoveCollapseToQqsFooter(context, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("move_collapse_to_qqs_footer_toggle")
                    )
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

                    SettingsTitleDescription(
                        title = "Battery Percentage Location",
                        description = "Choose where to place the battery percentage indicator.",
                        modifier = Modifier.weight(1f),
                        descriptionFontSize = 12.sp
                    )
                }

                if (isClassicStyle) {
                    val isHidden = batteryPercentageLocation == BatteryPercentageLocation.HIDDEN
                    val options = listOf("Shown", "Hidden")
                    val selectedOption = if (isHidden) "Hidden" else "Shown"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        options.forEach { opt ->
                            val isSelected = opt == selectedOption
                            val chipShape = SegmentedButtonShapes.getShape(
                                item = opt,
                                items = options,
                                selectedItem = selectedOption
                            )
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (opt == "Shown") {
                                        PanelPreferences.setBatteryPercentageLocation(context, BatteryPercentageLocation.HEADER)
                                    } else {
                                        PanelPreferences.setBatteryPercentageLocation(context, BatteryPercentageLocation.HIDDEN)
                                    }
                                },
                                label = { Text(opt, fontSize = 10.sp) },
                                shape = chipShape,
                                border = null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.background,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.testTag("battery_pos_oreo_${opt.lowercase()}")
                            )
                        }
                    }
                } else {
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

                    SettingsTitleDescription(
                        title = "Settings Icon Location",
                        description = "Choose where the system settings icon is placed.",
                        modifier = Modifier.weight(1f),
                        descriptionFontSize = 12.sp
                    )
                }

                if (isClassicStyle) {
                    val isHidden = settingsIconLocation == SettingsIconLocation.HIDDEN
                    val options = listOf("Shown", "Hidden")
                    val selectedOption = if (isHidden) "Hidden" else "Shown"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        options.forEach { opt ->
                            val isSelected = opt == selectedOption
                            val chipShape = SegmentedButtonShapes.getShape(
                                item = opt,
                                items = options,
                                selectedItem = selectedOption
                            )
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (opt == "Shown") {
                                        PanelPreferences.setSettingsIconLocation(context, SettingsIconLocation.EXPANDED_QS)
                                    } else {
                                        PanelPreferences.setSettingsIconLocation(context, SettingsIconLocation.HIDDEN)
                                    }
                                },
                                label = { Text(opt, fontSize = 10.sp) },
                                shape = chipShape,
                                border = null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.background,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.testTag("settings_pos_oreo_${opt.lowercase()}")
                            )
                        }
                    }
                } else {
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
                                if (collapseExpandOptions) Modifier.pinnableClickable { fontSizesExpanded = !fontSizesExpanded } else Modifier
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

                        SettingsTitleDescription(
                            title = "Tile Label & Subtitle Font Size",
                            description = "Adjust the typography scale for Quick Settings tile labels and subtitles.",
                            modifier = Modifier.weight(1f),
                            descriptionFontSize = 12.sp
                        )
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

                    SettingsTitleDescription(
                        title = "Hide Date in Expanded QS",
                        description = "Hide the date display when the Quick Settings panel is fully expanded.",
                        descriptionFontSize = 12.sp
                    )
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

                    SettingsTitleDescription(
                        title = "Disable Labels in Expanded QS",
                        description = "Hide text labels in expanded QS panel and scale tiles bigger automatically.",
                        descriptionFontSize = 12.sp
                    )
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

                    SettingsTitleDescription(
                        title = "Hide QQS Drag Handle",
                        description = "Hide the visual drag pill indicator at the bottom of the compact QQS panel.",
                        descriptionFontSize = 12.sp
                    )
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

                    SettingsTitleDescription(
                        title = "Disable Empty Notification Labels",
                        description = "Hide the 'Notifications' header and 'No new notifications' label in QQS when there are no active notifications.",
                        descriptionFontSize = 12.sp
                    )
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

                    SettingsTitleDescription(
                        title = "Hide Brightness Percentage",
                        description = "Hide the percentage text indicator next to the brightness slider.",
                        descriptionFontSize = 12.sp
                    )
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

                    SettingsTitleDescription(
                        title = "Hide Brightness Icon",
                        description = "Hide the brightness icon on the left side of the brightness slider.",
                        descriptionFontSize = 12.sp
                    )
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

                    SettingsTitleDescription(
                        title = "Show Brightness +/- Buttons",
                        description = "Add minus (-) and plus (+) buttons to the sides of the brightness slider for fine control.",
                        descriptionFontSize = 12.sp
                    )
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

                        SettingsTitleDescription(
                            title = "Disable Clear All Background",
                            description = "Remove the background container tint from the notification clear all button.",
                            descriptionFontSize = 12.sp
                        )
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

    val disableClearAllLabelCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        val disableClearAllLabel by PanelPreferences.disableClearAllLabel.collectAsState()
        val clearAllIconSize by PanelPreferences.clearAllIconSize.collectAsState()

        PinnableCardWrapper("disable_clear_all_label_card", CustomizeCategory.QS_TWEAKS, "Disable Clear All Label") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("disable_clear_all_label_card")
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
                                    imageVector = Icons.Default.NotificationsOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            SettingsTitleDescription(
                                title = "Disable Clear All Label",
                                description = "Hide the 'Clear All' text label, remove pill background, and keep only the clear icon.",
                                descriptionFontSize = 12.sp
                            )
                        }

                        Switch(
                            checked = disableClearAllLabel,
                            onCheckedChange = { PanelPreferences.setDisableClearAllLabel(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("disable_clear_all_label_toggle")
                        )
                    }

                    AnimatedVisibility(
                        visible = disableClearAllLabel,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // 1. Icon Size Slider
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Clear All Icon Size",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${clearAllIconSize}dp",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = clearAllIconSize.toFloat(),
                                    onValueChange = { PanelPreferences.setClearAllIconSize(context, kotlin.math.round(it).toInt()) },
                                    valueRange = 16f..48f,
                                    steps = 31,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("clear_all_icon_size_slider")
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- SIM CARD CARRIER CARDS ---

    // Show SIM Card Carrier Card
    val showCarrierCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("show_carrier_card", CustomizeCategory.QS_TWEAKS, "Show SIM Card Carrier") {
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
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SimCard,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        SettingsTitleDescription(
                            title = "Show SIM Card Carrier",
                            description = "Show the dynamic SIM card network operator name on the bottom left of the QQS container.",
                            modifier = Modifier.weight(1f),
                            descriptionFontSize = 12.sp
                        )
                    }

                    Switch(
                        checked = showCarrier,
                        onCheckedChange = { PanelPreferences.setShowCarrier(context, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("show_carrier_toggle")
                    )
                }
            }
        }
    }

    // Divider Between Carrier & QQS Tiles Card
    val carrierDividerCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("carrier_divider_card", CustomizeCategory.QS_TWEAKS, "Carrier Divider") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("carrier_divider_card")
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
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HorizontalRule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            SettingsTitleDescription(
                                title = "Divider Between Carrier & QQS Tiles",
                                description = "Show a divider separating the SIM card carrier name and QQS tiles",
                                modifier = Modifier.weight(1f),
                                descriptionFontSize = 12.sp
                            )
                        }

                        Switch(
                            checked = showCarrierDivider,
                            onCheckedChange = { PanelPreferences.setShowCarrierDivider(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("show_carrier_divider_toggle")
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SettingsTitleDescription(
                            title = "Replace Carrier Divider with Line",
                            description = "Keep carrier and tiles inside one card with a crisp divider line instead of splitting cards",
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp),
                            titleFontSize = 13.sp,
                            titleFontWeight = FontWeight.SemiBold,
                            descriptionFontSize = 11.5.sp
                        )

                        Switch(
                            checked = carrierDividerAsLine,
                            onCheckedChange = { PanelPreferences.setCarrierDividerAsLine(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("carrier_divider_as_line_toggle")
                        )
                    }
                }
            }
        }
    }

    // Hide Carrier in Expanded QS Card
    val hideCarrierInExpandedCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("hide_carrier_in_expanded_card", CustomizeCategory.QS_TWEAKS, "Hide Carrier in Expanded QS") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("hide_carrier_in_expanded_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        SettingsTitleDescription(
                            title = "Hide Carrier in Expanded QS",
                            description = "Hide the SIM card carrier label when Quick Settings is fully expanded",
                            modifier = Modifier.weight(1f),
                            descriptionFontSize = 12.sp
                        )
                    }

                    Switch(
                        checked = hideCarrierInExpanded,
                        onCheckedChange = { PanelPreferences.setHideCarrierInExpanded(context, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("hide_carrier_in_expanded_toggle")
                    )
                }
            }
        }
    }

    val enablePanelShadowsCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        var panelShadowsExpanded by rememberSaveable { mutableStateOf(false) }
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
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (collapseExpandOptions) Modifier.pinnableClickable { panelShadowsExpanded = !panelShadowsExpanded } else Modifier
                                )
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

                            SettingsTitleDescription(
                                title = "Panel & Notification Shadows",
                                description = "Enable depth shadows under QS panel cards, notification items, and the Clear All button.",
                                descriptionFontSize = 12.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (enablePanelShadows && (panelShadowElevation != 8f || panelShadowOffsetY != 0f || Math.abs(panelShadowAlpha - 0.35f) > 0.01f)) {
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

                            if (collapseExpandOptions) {
                                IconButton(
                                    onClick = { panelShadowsExpanded = !panelShadowsExpanded },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("toggle_expand_panel_shadows")
                                ) {
                                    Icon(
                                        imageVector = if (panelShadowsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (panelShadowsExpanded) "Collapse" else "Expand",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                        visible = enablePanelShadows && (!collapseExpandOptions || panelShadowsExpanded),
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
                                    SettingsTitleDescription(
                                        title = "Shadow Size / Elevation",
                                        description = if (panelShadowElevation.toInt() == 8) "Default (8 dp)"
                                                      else if (panelShadowElevation < 8) "Subtle Shadow"
                                                      else "Deep Shadow",
                                        titleFontSize = 13.sp,
                                        titleFontWeight = FontWeight.SemiBold,
                                        descriptionFontSize = 11.sp
                                    )
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
                                    SettingsTitleDescription(
                                        title = "Shadow Vertical Offset",
                                        description = if (panelShadowOffsetY.toInt() == 0) "Default (Centered / 0 dp)"
                                                      else if (panelShadowOffsetY < 0) "Moved Upwards"
                                                      else "Moved Downwards",
                                        titleFontSize = 13.sp,
                                        titleFontWeight = FontWeight.SemiBold,
                                        descriptionFontSize = 11.sp
                                    )
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

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            // 3. Shadow Transparency / Opacity Slider
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SettingsTitleDescription(
                                        title = "Shadow Transparency",
                                        description = if (Math.abs(panelShadowAlpha - 0.35f) < 0.03f) "Default (35% Opacity)"
                                                      else if (panelShadowAlpha < 0.35f) "More Transparent"
                                                      else "More Opaque",
                                        titleFontSize = 13.sp,
                                        titleFontWeight = FontWeight.SemiBold,
                                        descriptionFontSize = 11.sp
                                    )
                                    Text(
                                        text = "${Math.round(panelShadowAlpha * 100)}%",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Slider(
                                    value = panelShadowAlpha,
                                    onValueChange = { PanelPreferences.setPanelShadowAlpha(context, it) },
                                    valueRange = 0.05f..0.90f,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("panel_shadow_transparency_slider"),
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
        var clearAllFabOffsetsExpanded by rememberSaveable { mutableStateOf(false) }
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
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (collapseExpandOptions) Modifier.pinnableClickable { clearAllFabOffsetsExpanded = !clearAllFabOffsetsExpanded } else Modifier
                                )
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

                            SettingsTitleDescription(
                                title = "Clear All FAB Position & Size",
                                description = "Adjust the horizontal position, vertical position, and size of the floating Clear All button.",
                                descriptionFontSize = 12.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
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

                            if (collapseExpandOptions) {
                                IconButton(
                                    onClick = { clearAllFabOffsetsExpanded = !clearAllFabOffsetsExpanded },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("toggle_expand_clear_all_fab_offsets")
                                ) {
                                    Icon(
                                        imageVector = if (clearAllFabOffsetsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (clearAllFabOffsetsExpanded) "Collapse" else "Expand",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = !collapseExpandOptions || clearAllFabOffsetsExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 0. Anchor Alignment Preset
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Anchor Alignment Preset",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val allPositions = ClearAllFabPosition.entries
                                    allPositions.forEach { pos ->
                                        val isSelected = clearAllFabPosition == pos
                                        val chipShape = SegmentedButtonShapes.getShape(
                                            item = pos,
                                            items = allPositions,
                                            selectedItem = clearAllFabPosition
                                        )
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { PanelPreferences.setClearAllFabPosition(context, pos) },
                                            label = { Text(pos.label, fontSize = 11.sp) },
                                            shape = chipShape,
                                            border = null,
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                containerColor = MaterialTheme.colorScheme.background,
                                                labelColor = MaterialTheme.colorScheme.onSurface
                                            ),
                                            modifier = Modifier.testTag("clear_all_fab_pos_${pos.id}")
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            // 1. Horizontal Offset Slider
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SettingsTitleDescription(
                                        title = "Horizontal Offset",
                                        description = if (clearAllFabOffsetX.toInt() == 0) "Default (Centered / Anchored)"
                                                      else if (clearAllFabOffsetX < 0) "Shifted Left"
                                                      else "Shifted Right",
                                        titleFontSize = 13.sp,
                                        titleFontWeight = FontWeight.SemiBold,
                                        descriptionFontSize = 11.sp
                                    )
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
                                    SettingsTitleDescription(
                                        title = "Vertical Offset",
                                        description = if (clearAllFabOffsetY.toInt() == 0) "Default (Bottom Floating)"
                                                      else if (clearAllFabOffsetY < 0) "Lowered towards Edge"
                                                      else "Raised Upwards",
                                        titleFontSize = 13.sp,
                                        titleFontWeight = FontWeight.SemiBold,
                                        descriptionFontSize = 11.sp
                                    )
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
                                    SettingsTitleDescription(
                                        title = "FAB Button Size",
                                        description = if (clearAllFabSize == 64) "Default Size (64 dp)"
                                                      else if (clearAllFabSize < 64) "Compact"
                                                      else "Large",
                                        titleFontSize = 13.sp,
                                        titleFontWeight = FontWeight.SemiBold,
                                        descriptionFontSize = 11.sp
                                    )
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
                                    lineHeight = SettingsTypographyDefaults.compactLineHeight(11.sp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                    style = SettingsTypographyDefaults.CompactTextStyle
                                )
                            }
                        }
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
        val hiddenInOreoTweakIds = buildSet {
            if (isClassicStyle) {
                add("qs_drag_handle_length_card")
                add("hide_date_in_expanded_card")
                add("hide_qqs_drag_handle_card")
            }
            if (isAndroidOreoStyle) {
                add("show_carrier_card")
                add("carrier_divider_card")
                add("hide_carrier_in_expanded_card")
            }
            if (isAndroidKitKatStyle || isAndroidMarshmallowStyle) {
                add("qs_grid_density_card")
                add("enable_qqs_horizontal_scroll_card")
                add("move_collapse_to_qqs_footer_card")
            }
            if (isAndroidKitKatStyle) {
                add("battery_percentage_location_card")
                add("settings_icon_location_card")
                add("edit_pencil_location_card")
                add("enable_panel_shadows_card")
                add("disable_clear_all_bg_card")
                add("clear_all_fab_offset_card")
                add("carrier_divider_card")
                add("hide_carrier_in_expanded_card")
            }
        }
        val pinnedCards = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
        if (!hiddenInOreoTweakIds.contains("qs_grid_density_card") && !isUserHidden("qs_grid_density_card") && pinnedIds.contains("qs_grid_density_card")) pinnedCards.add(gridDensityCard)
        if (!hiddenInOreoTweakIds.contains("enable_qqs_horizontal_scroll_card") && !isUserHidden("enable_qqs_horizontal_scroll_card") && pinnedIds.contains("enable_qqs_horizontal_scroll_card")) pinnedCards.add(enableQqsHorizontalScrollCard)
        if (!hiddenInOreoTweakIds.contains("qs_drag_handle_length_card") && !isUserHidden("qs_drag_handle_length_card") && pinnedIds.contains("qs_drag_handle_length_card")) pinnedCards.add(dragHandleLengthCard)
        if (!hiddenInOreoTweakIds.contains("date_position_card") && !isUserHidden("date_position_card") && pinnedIds.contains("date_position_card")) pinnedCards.add(datePositionCard)
        if (!hiddenInOreoTweakIds.contains("battery_percentage_location_card") && !isUserHidden("battery_percentage_location_card") && pinnedIds.contains("battery_percentage_location_card")) pinnedCards.add(batteryPercentageLocationCard)
        if (!hiddenInOreoTweakIds.contains("settings_icon_location_card") && !isUserHidden("settings_icon_location_card") && pinnedIds.contains("settings_icon_location_card")) pinnedCards.add(settingsIconLocationCard)
        if (!hiddenInOreoTweakIds.contains("edit_pencil_location_card") && !isUserHidden("edit_pencil_location_card") && pinnedIds.contains("edit_pencil_location_card")) pinnedCards.add(editPencilLocationCard)
        if (!hiddenInOreoTweakIds.contains("move_collapse_to_qqs_footer_card") && !isUserHidden("move_collapse_to_qqs_footer_card") && pinnedIds.contains("move_collapse_to_qqs_footer_card")) pinnedCards.add(moveCollapseToQqsFooterCard)
        if (!hiddenInOreoTweakIds.contains("qs_tile_font_size_card") && !isUserHidden("qs_tile_font_size_card") && pinnedIds.contains("qs_tile_font_size_card")) pinnedCards.add(tileFontSizeCard)
        if (!hiddenInOreoTweakIds.contains("hide_date_in_expanded_card") && !isUserHidden("hide_date_in_expanded_card") && pinnedIds.contains("hide_date_in_expanded_card")) pinnedCards.add(hideDateInExpandedCard)
        if (!hiddenInOreoTweakIds.contains("disable_labels_in_expanded_card") && !isUserHidden("disable_labels_in_expanded_card") && pinnedIds.contains("disable_labels_in_expanded_card")) pinnedCards.add(disableLabelsInExpandedCard)
        if (!hiddenInOreoTweakIds.contains("hide_qqs_drag_handle_card") && !isUserHidden("hide_qqs_drag_handle_card") && pinnedIds.contains("hide_qqs_drag_handle_card")) pinnedCards.add(hideQqsDragHandleCard)
        if (!hiddenInOreoTweakIds.contains("hide_qqs_empty_notif_labels_card") && !isUserHidden("hide_qqs_empty_notif_labels_card") && pinnedIds.contains("hide_qqs_empty_notif_labels_card")) pinnedCards.add(hideQqsEmptyNotifLabelsCard)
        if (!hiddenInOreoTweakIds.contains("hide_brightness_percentage_card") && !isUserHidden("hide_brightness_percentage_card") && pinnedIds.contains("hide_brightness_percentage_card")) pinnedCards.add(hideBrightnessPercentageCard)
        if (!hiddenInOreoTweakIds.contains("hide_brightness_icon_card") && !isUserHidden("hide_brightness_icon_card") && pinnedIds.contains("hide_brightness_icon_card")) pinnedCards.add(hideBrightnessIconCard)
        if (!hiddenInOreoTweakIds.contains("show_brightness_plus_minus_card") && !isUserHidden("show_brightness_plus_minus_card") && pinnedIds.contains("show_brightness_plus_minus_card")) pinnedCards.add(showBrightnessPlusMinusCard)
        if (!hiddenInOreoTweakIds.contains("enable_panel_shadows_card") && !isUserHidden("enable_panel_shadows_card") && pinnedIds.contains("enable_panel_shadows_card")) pinnedCards.add(enablePanelShadowsCard)
        if (!hiddenInOreoTweakIds.contains("disable_clear_all_bg_card") && !isUserHidden("disable_clear_all_bg_card") && pinnedIds.contains("disable_clear_all_bg_card")) pinnedCards.add(disableClearAllBgCard)
        if (!hiddenInOreoTweakIds.contains("disable_clear_all_label_card") && !isUserHidden("disable_clear_all_label_card") && pinnedIds.contains("disable_clear_all_label_card")) pinnedCards.add(disableClearAllLabelCard)
        if (!hiddenInOreoTweakIds.contains("clear_all_fab_offset_card") && !isUserHidden("clear_all_fab_offset_card") && pinnedIds.contains("clear_all_fab_offset_card")) pinnedCards.add(clearAllFabOffsetsCard)
        if (!hiddenInOreoTweakIds.contains("show_carrier_card") && !isUserHidden("show_carrier_card") && pinnedIds.contains("show_carrier_card")) pinnedCards.add(showCarrierCard)
        if (!hiddenInOreoTweakIds.contains("carrier_divider_card") && !isUserHidden("carrier_divider_card") && pinnedIds.contains("carrier_divider_card")) pinnedCards.add(carrierDividerCard)
        if (!hiddenInOreoTweakIds.contains("hide_carrier_in_expanded_card") && !isUserHidden("hide_carrier_in_expanded_card") && pinnedIds.contains("hide_carrier_in_expanded_card")) pinnedCards.add(hideCarrierInExpandedCard)

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
        val unpinnedGridDensity = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
        if (!hiddenInOreoTweakIds.contains("qs_grid_density_card") && !isUserHidden("qs_grid_density_card") && !pinnedIds.contains("qs_grid_density_card")) unpinnedGridDensity.add(gridDensityCard)
        if (!hiddenInOreoTweakIds.contains("enable_qqs_horizontal_scroll_card") && !isUserHidden("enable_qqs_horizontal_scroll_card") && !pinnedIds.contains("enable_qqs_horizontal_scroll_card")) unpinnedGridDensity.add(enableQqsHorizontalScrollCard)

        if (unpinnedGridDensity.isNotEmpty()) {
            CollapsibleSubheaderSection(
                title = "Grid & Tile Density",
                icon = androidx.compose.material.icons.Icons.Default.GridView
            ) {
                unpinnedGridDensity.forEachIndexed { index, card ->
                    card(CustomizeCardShapes.getShapeForIndex(index, unpinnedGridDensity.size))
                }
            }
        }

        // 3. Unpinned Drag Handle Options
        val showDragHandle = !isClassicStyle && !isUserHidden("qs_drag_handle_length_card") && !pinnedIds.contains("qs_drag_handle_length_card")
        if (showDragHandle) {
            CollapsibleSubheaderSection(
                title = "Drag Handle Options",
                icon = androidx.compose.material.icons.Icons.Default.DragHandle
            ) {
                dragHandleLengthCard(CustomizeCardShapes.SingleCard)
            }
        }

        // 4. Unpinned Header & Controls
        val unpinnedHeaderControls = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
        if (!hiddenInOreoTweakIds.contains("date_position_card") && !isUserHidden("date_position_card") && !pinnedIds.contains("date_position_card")) unpinnedHeaderControls.add(datePositionCard)
        if (!hiddenInOreoTweakIds.contains("battery_percentage_location_card") && !isUserHidden("battery_percentage_location_card") && !pinnedIds.contains("battery_percentage_location_card")) unpinnedHeaderControls.add(batteryPercentageLocationCard)
        if (!hiddenInOreoTweakIds.contains("settings_icon_location_card") && !isUserHidden("settings_icon_location_card") && !pinnedIds.contains("settings_icon_location_card")) unpinnedHeaderControls.add(settingsIconLocationCard)
        if (!hiddenInOreoTweakIds.contains("edit_pencil_location_card") && !isUserHidden("edit_pencil_location_card") && !pinnedIds.contains("edit_pencil_location_card")) unpinnedHeaderControls.add(editPencilLocationCard)
        if (!hiddenInOreoTweakIds.contains("move_collapse_to_qqs_footer_card") && !isUserHidden("move_collapse_to_qqs_footer_card") && !pinnedIds.contains("move_collapse_to_qqs_footer_card")) unpinnedHeaderControls.add(moveCollapseToQqsFooterCard)
        if (!hiddenInOreoTweakIds.contains("enable_panel_shadows_card") && !isUserHidden("enable_panel_shadows_card") && !pinnedIds.contains("enable_panel_shadows_card")) unpinnedHeaderControls.add(enablePanelShadowsCard)
        if (!hiddenInOreoTweakIds.contains("disable_clear_all_bg_card") && !isUserHidden("disable_clear_all_bg_card") && !pinnedIds.contains("disable_clear_all_bg_card")) unpinnedHeaderControls.add(disableClearAllBgCard)
        if (!hiddenInOreoTweakIds.contains("disable_clear_all_label_card") && !isUserHidden("disable_clear_all_label_card") && !pinnedIds.contains("disable_clear_all_label_card")) unpinnedHeaderControls.add(disableClearAllLabelCard)
        if (!hiddenInOreoTweakIds.contains("clear_all_fab_offset_card") && !isUserHidden("clear_all_fab_offset_card") && !pinnedIds.contains("clear_all_fab_offset_card")) unpinnedHeaderControls.add(clearAllFabOffsetsCard)

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

        // 5. SIM Card Carrier
        val unpinnedCarrier = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
        if (!hiddenInOreoTweakIds.contains("show_carrier_card")) {
            if (!isUserHidden("show_carrier_card") && !pinnedIds.contains("show_carrier_card")) unpinnedCarrier.add(showCarrierCard)
            if (showCarrier) {
                if (!hiddenInOreoTweakIds.contains("carrier_divider_card") && !isUserHidden("carrier_divider_card") && !pinnedIds.contains("carrier_divider_card")) unpinnedCarrier.add(carrierDividerCard)
                if (!hiddenInOreoTweakIds.contains("hide_carrier_in_expanded_card") && !isUserHidden("hide_carrier_in_expanded_card") && !pinnedIds.contains("hide_carrier_in_expanded_card")) unpinnedCarrier.add(hideCarrierInExpandedCard)
            }
        }

        if (unpinnedCarrier.isNotEmpty()) {
            CollapsibleSubheaderSection(
                title = "SIM Card Carrier",
                icon = Icons.Default.SimCard
            ) {
                unpinnedCarrier.forEachIndexed { index, card ->
                    card(CustomizeCardShapes.getShapeForIndex(index, unpinnedCarrier.size))
                }
            }
        }

        // 6. Brightness Controls
        val unpinnedBrightnessControls = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
        if (!isUserHidden("hide_brightness_percentage_card") && !pinnedIds.contains("hide_brightness_percentage_card")) unpinnedBrightnessControls.add(hideBrightnessPercentageCard)
        if (!isUserHidden("hide_brightness_icon_card") && !pinnedIds.contains("hide_brightness_icon_card")) unpinnedBrightnessControls.add(hideBrightnessIconCard)
        if (!isUserHidden("show_brightness_plus_minus_card") && !pinnedIds.contains("show_brightness_plus_minus_card")) unpinnedBrightnessControls.add(showBrightnessPlusMinusCard)

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

        // 7. Unpinned Typography & Font Sizes
        val unpinnedTypography = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
        if (!isUserHidden("qs_tile_font_size_card") && !pinnedIds.contains("qs_tile_font_size_card")) unpinnedTypography.add(tileFontSizeCard)
        if (!isClassicStyle && !isUserHidden("hide_date_in_expanded_card") && !pinnedIds.contains("hide_date_in_expanded_card")) unpinnedTypography.add(hideDateInExpandedCard)
        if (!isUserHidden("disable_labels_in_expanded_card") && !pinnedIds.contains("disable_labels_in_expanded_card")) unpinnedTypography.add(disableLabelsInExpandedCard)
        if (!isClassicStyle && !isUserHidden("hide_qqs_drag_handle_card") && !pinnedIds.contains("hide_qqs_drag_handle_card")) unpinnedTypography.add(hideQqsDragHandleCard)
        if (!isUserHidden("hide_qqs_empty_notif_labels_card") && !pinnedIds.contains("hide_qqs_empty_notif_labels_card")) unpinnedTypography.add(hideQqsEmptyNotifLabelsCard)

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

        if (pinnedCards.isEmpty() &&
            unpinnedGridDensity.isEmpty() &&
            !showDragHandle &&
            unpinnedHeaderControls.isEmpty() &&
            unpinnedCarrier.isEmpty() &&
            unpinnedBrightnessControls.isEmpty() &&
            unpinnedTypography.isEmpty()
        ) {
            AllSectionFeaturesHiddenCard(sectionTitle = "Quick Settings Tweaks")
        }
    }
}
