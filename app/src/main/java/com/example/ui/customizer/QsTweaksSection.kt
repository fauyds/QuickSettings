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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import com.example.data.BatteryPercentageLocation
import com.example.data.ClearAllFabPosition
import com.example.data.DatePosition
import com.example.data.EditButtonPosition
import com.example.data.PanelPreferences
import com.example.data.QsPageIndicatorStyle
import com.example.data.SettingsIconLocation
import com.example.ui.overlay.QsPageIndicator
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
    val dragHandleLength by PanelPreferences.dragHandleLength.collectAsState()
    val datePosition by PanelPreferences.datePosition.collectAsState()
    val editButtonPosition by PanelPreferences.editButtonPosition.collectAsState()
    val batteryPercentageLocation by PanelPreferences.batteryPercentageLocation.collectAsState()
    val pageIndicatorStyle by PanelPreferences.qsPageIndicatorStyle.collectAsState()
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
    val showBrightnessControlsBackground by PanelPreferences.showBrightnessControlsBackground.collectAsState()
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

    val footerIconBackgroundCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("footer_icon_background_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("footer_icon_background_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            Icon(
                                imageVector = Icons.Default.Tune,
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
                                text = "Footer Icon Backgrounds",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 17.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Enable M3 rounded backgrounds for QS footer action buttons with 10dp outer and 3dp inner corners.",
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    val footerIconBgEnabled by PanelPreferences.footerIconBackground.collectAsState()
                    val footerOuterSpacingEnabled by PanelPreferences.footerOuterSpacing.collectAsState()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enable Footer Icon Backgrounds",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = footerIconBgEnabled,
                            onCheckedChange = { PanelPreferences.setFooterIconBackground(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("footer_icon_bg_toggle")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Outer 3dp Spacing Between Buttons",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Adds 3dp spacing with 3dp inner corners. When disabled, spacing and inner corners are 0dp.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = footerOuterSpacingEnabled,
                            onCheckedChange = { PanelPreferences.setFooterOuterSpacing(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("footer_outer_spacing_toggle")
                        )
                    }

                    if (footerIconBgEnabled) {
                        val footerOuterCorner by PanelPreferences.footerOuterCorner.collectAsState()
                        val footerInnerCorner by PanelPreferences.footerInnerCorner.collectAsState()
                        val footerSpacing by PanelPreferences.footerSpacing.collectAsState()

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // 1. Outer Corner Radius Slider
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Outer Corner Radius",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Exposed ends corner curvature",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${footerOuterCorner.toInt()} dp",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = footerOuterCorner,
                                onValueChange = { PanelPreferences.setFooterOuterCorner(context, Math.round(it).toFloat()) },
                                valueRange = 0f..24f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("footer_outer_corner_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                )
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // 2. Inner Corner Radius Slider
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Inner Corner Radius",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Adjacent inner edges curvature",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${footerInnerCorner.toInt()} dp",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = footerInnerCorner,
                                onValueChange = { PanelPreferences.setFooterInnerCorner(context, Math.round(it).toFloat()) },
                                valueRange = 0f..20f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("footer_inner_corner_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                )
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // 3. Spacing Between Buttons Slider
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Button Spacing",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Gap between footer button backgrounds",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${footerSpacing.toInt()} dp",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = footerSpacing,
                                onValueChange = { PanelPreferences.setFooterSpacing(context, Math.round(it).toFloat()) },
                                valueRange = 0f..20f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("footer_spacing_slider"),
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

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Move Collapse Button to QQS Footer",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Show the expand/collapse chevron button directly inside the QQS footer bar",
                                fontSize = 12.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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

    val headerStatusBackgroundCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("header_status_background_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("header_status_background_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
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
                            Icon(
                                imageVector = Icons.Default.Tune,
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
                                text = "Header & Status Backgrounds",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 17.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Toggle rounded backgrounds and corner radius separately for date, battery, clock, and carrier.",
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    val dateBgEnabled by PanelPreferences.dateBackgroundEnabled.collectAsState()
                    val dateBgCorner by PanelPreferences.dateBackgroundCorner.collectAsState()
                    val batteryBgEnabled by PanelPreferences.batteryBackgroundEnabled.collectAsState()
                    val batteryBgCorner by PanelPreferences.batteryBackgroundCorner.collectAsState()
                    val clockBgEnabled by PanelPreferences.clockBackgroundEnabled.collectAsState()
                    val clockBgCorner by PanelPreferences.clockBackgroundCorner.collectAsState()
                    val carrierBgEnabled by PanelPreferences.carrierBackgroundEnabled.collectAsState()
                    val carrierBgCorner by PanelPreferences.carrierBackgroundCorner.collectAsState()

                    // 1. Date Background & Corner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Date Background",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = dateBgEnabled,
                            onCheckedChange = { PanelPreferences.setDateBackgroundEnabled(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("date_bg_toggle")
                        )
                    }
                    AnimatedVisibility(visible = dateBgEnabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Date Corner Radius",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${dateBgCorner.toInt()} dp",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = dateBgCorner,
                                onValueChange = { PanelPreferences.setDateBackgroundCorner(context, Math.round(it).toFloat()) },
                                valueRange = 0f..24f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("date_bg_corner_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // 2. Battery Background & Corner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Battery Background",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = batteryBgEnabled,
                            onCheckedChange = { PanelPreferences.setBatteryBackgroundEnabled(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("battery_bg_toggle")
                        )
                    }
                    AnimatedVisibility(visible = batteryBgEnabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Battery Corner Radius",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${batteryBgCorner.toInt()} dp",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = batteryBgCorner,
                                onValueChange = { PanelPreferences.setBatteryBackgroundCorner(context, Math.round(it).toFloat()) },
                                valueRange = 0f..24f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("battery_bg_corner_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // 3. Clock Background & Corner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Clock Background",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = clockBgEnabled,
                            onCheckedChange = { PanelPreferences.setClockBackgroundEnabled(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("clock_bg_toggle")
                        )
                    }
                    AnimatedVisibility(visible = clockBgEnabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Clock Corner Radius",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${clockBgCorner.toInt()} dp",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = clockBgCorner,
                                onValueChange = { PanelPreferences.setClockBackgroundCorner(context, Math.round(it).toFloat()) },
                                valueRange = 0f..24f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("clock_bg_corner_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // 4. Carrier Label Background & Corner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Carrier Label Background",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = carrierBgEnabled,
                            onCheckedChange = { PanelPreferences.setCarrierBackgroundEnabled(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("carrier_bg_toggle")
                        )
                    }
                    AnimatedVisibility(visible = carrierBgEnabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Carrier Label Corner Radius",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${carrierBgCorner.toInt()} dp",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = carrierBgCorner,
                                onValueChange = { PanelPreferences.setCarrierBackgroundCorner(context, Math.round(it).toFloat()) },
                                valueRange = 0f..24f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("carrier_bg_corner_slider"),
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

    val pageIndicatorStyleCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("qs_page_indicator_style_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("qs_page_indicator_style_card")
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
                            Icon(
                                imageVector = Icons.Default.DragHandle,
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
                                text = "QS Page Indicator Style",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 17.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = pageIndicatorStyle.description,
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    val piActiveLineThickness by PanelPreferences.piActiveLineThickness.collectAsState()
                    val piInactiveLineThickness by PanelPreferences.piInactiveLineThickness.collectAsState()
                    val piActiveLineWidth by PanelPreferences.piActiveLineWidth.collectAsState()
                    val piInactiveLineWidth by PanelPreferences.piInactiveLineWidth.collectAsState()
                    val piActiveDotRadius by PanelPreferences.piActiveDotRadius.collectAsState()
                    val piInactiveDotRadius by PanelPreferences.piInactiveDotRadius.collectAsState()
                    val piActiveSlidingThickness by PanelPreferences.piActiveSlidingThickness.collectAsState()
                    val piInactiveSlidingThickness by PanelPreferences.piInactiveSlidingThickness.collectAsState()

                    // Live interactive preview of the selected indicator style
                    var previewPage by remember { mutableIntStateOf(0) }
                    val animatedPreviewPos by animateFloatAsState(
                        targetValue = previewPage.toFloat(),
                        animationSpec = tween(durationMillis = 240),
                        label = "preview_page_indicator"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .clickable { previewPage = (previewPage + 1) % 3 }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Preview (tap)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        QsPageIndicator(
                            pageCount = 3,
                            scrollPosition = animatedPreviewPos,
                            style = pageIndicatorStyle,
                            activeColor = MaterialTheme.colorScheme.primary,
                            inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f),
                            onPageClick = { previewPage = it },
                            modifier = Modifier.testTag("page_indicator_preview"),
                            activeLineThickness = piActiveLineThickness,
                            inactiveLineThickness = piInactiveLineThickness,
                            activeLineWidth = piActiveLineWidth,
                            inactiveLineWidth = piInactiveLineWidth,
                            activeDotRadius = piActiveDotRadius,
                            inactiveDotRadius = piInactiveDotRadius,
                            activeSlidingThickness = piActiveSlidingThickness,
                            inactiveSlidingThickness = piInactiveSlidingThickness
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val allIndicatorStyles = QsPageIndicatorStyle.entries
                        allIndicatorStyles.forEach { style ->
                            val isSelected = pageIndicatorStyle == style
                            val chipShape = SegmentedButtonShapes.getShape(
                                item = style,
                                items = allIndicatorStyles,
                                selectedItem = pageIndicatorStyle
                            )
                            FilterChip(
                                selected = isSelected,
                                onClick = { PanelPreferences.setQsPageIndicatorStyle(context, style) },
                                label = { Text(style.label, fontSize = 11.sp) },
                                shape = chipShape,
                                border = null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.background,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.testTag("page_indicator_style_${style.id}")
                            )
                        }
                    }

                    @Composable
                    fun indicatorSlider(
                        title: String,
                        value: Float,
                        valueRange: ClosedFloatingPointRange<Float>,
                        stepHalfDp: Boolean,
                        testTagName: String,
                        onValueChange: (Float) -> Unit
                    ) {
                        val displayVal = if (value % 1f == 0f) {
                            "${value.toInt()} dp"
                        } else {
                            "${String.format(java.util.Locale.US, "%.1f", value)} dp"
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = displayVal,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = value,
                                onValueChange = { raw ->
                                    val snapped = if (stepHalfDp) {
                                        kotlin.math.round(raw * 2f) / 2f
                                    } else {
                                        kotlin.math.round(raw)
                                    }
                                    onValueChange(snapped)
                                },
                                valueRange = valueRange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag(testTagName),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    when (pageIndicatorStyle) {
                        QsPageIndicatorStyle.DOTS -> {
                            indicatorSlider(
                                title = "Active Dot Radius",
                                value = piActiveDotRadius,
                                valueRange = 1.5f..8f,
                                stepHalfDp = true,
                                testTagName = "pi_active_dot_radius_slider"
                            ) { PanelPreferences.setPiActiveDotRadius(context, it) }

                            indicatorSlider(
                                title = "Inactive Dot Radius",
                                value = piInactiveDotRadius,
                                valueRange = 1.0f..6f,
                                stepHalfDp = true,
                                testTagName = "pi_inactive_dot_radius_slider"
                            ) { PanelPreferences.setPiInactiveDotRadius(context, it) }
                        }
                        QsPageIndicatorStyle.HORIZONTAL_LINES -> {
                            indicatorSlider(
                                title = "Active Line Thickness",
                                value = piActiveLineThickness,
                                valueRange = 1f..10f,
                                stepHalfDp = true,
                                testTagName = "pi_active_line_thickness_slider"
                            ) { PanelPreferences.setPiActiveLineThickness(context, it) }

                            indicatorSlider(
                                title = "Inactive Line Thickness",
                                value = piInactiveLineThickness,
                                valueRange = 1f..10f,
                                stepHalfDp = true,
                                testTagName = "pi_inactive_line_thickness_slider"
                            ) { PanelPreferences.setPiInactiveLineThickness(context, it) }

                            indicatorSlider(
                                title = "Active Line Width",
                                value = piActiveLineWidth,
                                valueRange = 8f..48f,
                                stepHalfDp = false,
                                testTagName = "pi_active_line_width_slider"
                            ) { PanelPreferences.setPiActiveLineWidth(context, it) }

                            indicatorSlider(
                                title = "Inactive Line Width",
                                value = piInactiveLineWidth,
                                valueRange = 4f..32f,
                                stepHalfDp = false,
                                testTagName = "pi_inactive_line_width_slider"
                            ) { PanelPreferences.setPiInactiveLineWidth(context, it) }
                        }
                        QsPageIndicatorStyle.DOTS_WITH_ACTIVE_LINE -> {
                            indicatorSlider(
                                title = "Active Line Thickness",
                                value = piActiveLineThickness,
                                valueRange = 1f..10f,
                                stepHalfDp = true,
                                testTagName = "pi_active_line_thickness_slider"
                            ) { PanelPreferences.setPiActiveLineThickness(context, it) }

                            indicatorSlider(
                                title = "Active Line Width",
                                value = piActiveLineWidth,
                                valueRange = 8f..48f,
                                stepHalfDp = false,
                                testTagName = "pi_active_line_width_slider"
                            ) { PanelPreferences.setPiActiveLineWidth(context, it) }

                            indicatorSlider(
                                title = "Inactive Dot Radius",
                                value = piInactiveDotRadius,
                                valueRange = 1.0f..6f,
                                stepHalfDp = true,
                                testTagName = "pi_inactive_dot_radius_slider"
                            ) { PanelPreferences.setPiInactiveDotRadius(context, it) }
                        }
                        QsPageIndicatorStyle.SLIDING_TRACK -> {
                            indicatorSlider(
                                title = "Active Sliding Line Thickness",
                                value = piActiveSlidingThickness,
                                valueRange = 1f..12f,
                                stepHalfDp = true,
                                testTagName = "pi_active_sliding_thickness_slider"
                            ) { PanelPreferences.setPiActiveSlidingThickness(context, it) }

                            indicatorSlider(
                                title = "Inactive Sliding Line Thickness",
                                value = piInactiveSlidingThickness,
                                valueRange = 1f..12f,
                                stepHalfDp = true,
                                testTagName = "pi_inactive_sliding_thickness_slider"
                            ) { PanelPreferences.setPiInactiveSlidingThickness(context, it) }
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

    val showBrightnessControlsBackgroundCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("show_brightness_controls_background_card", CustomizeCategory.QS_TWEAKS) {
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
                            text = "Brightness Controls Background",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 17.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Place the brightness icon, -/+ buttons, and percentage inside pill backgrounds.",
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = showBrightnessControlsBackground,
                    onCheckedChange = { PanelPreferences.setShowBrightnessControlsBackground(context, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("show_brightness_controls_background_toggle")
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
                                    if (collapseExpandOptions) Modifier.clickable { panelShadowsExpanded = !panelShadowsExpanded } else Modifier
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

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            // 3. Shadow Transparency / Opacity Slider
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Shadow Transparency",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (Math.abs(panelShadowAlpha - 0.35f) < 0.03f) "Default (35% Opacity)"
                                                   else if (panelShadowAlpha < 0.35f) "More Transparent"
                                                   else "More Opaque",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
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
                                    if (collapseExpandOptions) Modifier.clickable { clearAllFabOffsetsExpanded = !clearAllFabOffsetsExpanded } else Modifier
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
            if (isAndroidKitKatStyle || isAndroidMarshmallowStyle) {
                add("qs_grid_density_card")
                add("qs_page_indicator_style_card")
                add("move_collapse_to_qqs_footer_card")
            }
            if (isAndroidKitKatStyle) {
                add("battery_percentage_location_card")
                add("settings_icon_location_card")
                add("edit_pencil_location_card")
                add("enable_panel_shadows_card")
                add("disable_clear_all_bg_card")
                add("clear_all_fab_offset_card")
            }
        }
        val pinnedCards = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
        if (!hiddenInOreoTweakIds.contains("qs_grid_density_card") && pinnedIds.contains("qs_grid_density_card")) pinnedCards.add(gridDensityCard)
        if (!hiddenInOreoTweakIds.contains("qs_drag_handle_length_card") && pinnedIds.contains("qs_drag_handle_length_card")) pinnedCards.add(dragHandleLengthCard)
        if (!hiddenInOreoTweakIds.contains("date_position_card") && pinnedIds.contains("date_position_card")) pinnedCards.add(datePositionCard)
        if (!hiddenInOreoTweakIds.contains("battery_percentage_location_card") && pinnedIds.contains("battery_percentage_location_card")) pinnedCards.add(batteryPercentageLocationCard)
        if (!hiddenInOreoTweakIds.contains("settings_icon_location_card") && pinnedIds.contains("settings_icon_location_card")) pinnedCards.add(settingsIconLocationCard)
        if (!hiddenInOreoTweakIds.contains("edit_pencil_location_card") && pinnedIds.contains("edit_pencil_location_card")) pinnedCards.add(editPencilLocationCard)
        if (!hiddenInOreoTweakIds.contains("footer_icon_background_card") && pinnedIds.contains("footer_icon_background_card")) pinnedCards.add(footerIconBackgroundCard)
        if (!hiddenInOreoTweakIds.contains("move_collapse_to_qqs_footer_card") && pinnedIds.contains("move_collapse_to_qqs_footer_card")) pinnedCards.add(moveCollapseToQqsFooterCard)
        if (!hiddenInOreoTweakIds.contains("qs_page_indicator_style_card") && pinnedIds.contains("qs_page_indicator_style_card")) pinnedCards.add(pageIndicatorStyleCard)
        if (!hiddenInOreoTweakIds.contains("header_status_background_card") && pinnedIds.contains("header_status_background_card")) pinnedCards.add(headerStatusBackgroundCard)
        if (!hiddenInOreoTweakIds.contains("qs_tile_font_size_card") && pinnedIds.contains("qs_tile_font_size_card")) pinnedCards.add(tileFontSizeCard)
        if (!hiddenInOreoTweakIds.contains("hide_date_in_expanded_card") && pinnedIds.contains("hide_date_in_expanded_card")) pinnedCards.add(hideDateInExpandedCard)
        if (!hiddenInOreoTweakIds.contains("disable_labels_in_expanded_card") && pinnedIds.contains("disable_labels_in_expanded_card")) pinnedCards.add(disableLabelsInExpandedCard)
        if (!hiddenInOreoTweakIds.contains("hide_qqs_drag_handle_card") && pinnedIds.contains("hide_qqs_drag_handle_card")) pinnedCards.add(hideQqsDragHandleCard)
        if (!hiddenInOreoTweakIds.contains("hide_qqs_empty_notif_labels_card") && pinnedIds.contains("hide_qqs_empty_notif_labels_card")) pinnedCards.add(hideQqsEmptyNotifLabelsCard)
        if (!hiddenInOreoTweakIds.contains("hide_brightness_percentage_card") && pinnedIds.contains("hide_brightness_percentage_card")) pinnedCards.add(hideBrightnessPercentageCard)
        if (!hiddenInOreoTweakIds.contains("hide_brightness_icon_card") && pinnedIds.contains("hide_brightness_icon_card")) pinnedCards.add(hideBrightnessIconCard)
        if (!hiddenInOreoTweakIds.contains("show_brightness_plus_minus_card") && pinnedIds.contains("show_brightness_plus_minus_card")) pinnedCards.add(showBrightnessPlusMinusCard)
        if (!hiddenInOreoTweakIds.contains("show_brightness_controls_background_card") && pinnedIds.contains("show_brightness_controls_background_card")) pinnedCards.add(showBrightnessControlsBackgroundCard)
        if (!hiddenInOreoTweakIds.contains("enable_panel_shadows_card") && pinnedIds.contains("enable_panel_shadows_card")) pinnedCards.add(enablePanelShadowsCard)
        if (!hiddenInOreoTweakIds.contains("disable_clear_all_bg_card") && pinnedIds.contains("disable_clear_all_bg_card")) pinnedCards.add(disableClearAllBgCard)
        if (!hiddenInOreoTweakIds.contains("clear_all_fab_offset_card") && pinnedIds.contains("clear_all_fab_offset_card")) pinnedCards.add(clearAllFabOffsetsCard)

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
        if (!hiddenInOreoTweakIds.contains("qs_grid_density_card") && !pinnedIds.contains("qs_grid_density_card")) {
            CollapsibleSubheaderSection(
                title = "Grid & Tile Density",
                icon = androidx.compose.material.icons.Icons.Default.GridView
            ) {
                gridDensityCard(CustomizeCardShapes.SingleCard)
            }
        }

        // 3. Unpinned Drag Handle Options
        if (!isClassicStyle && !pinnedIds.contains("qs_drag_handle_length_card")) {
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
        if (!pinnedIds.contains("footer_icon_background_card")) unpinnedHeaderControls.add(footerIconBackgroundCard)
        if (!hiddenInOreoTweakIds.contains("move_collapse_to_qqs_footer_card") && !pinnedIds.contains("move_collapse_to_qqs_footer_card")) unpinnedHeaderControls.add(moveCollapseToQqsFooterCard)
        if (!hiddenInOreoTweakIds.contains("qs_page_indicator_style_card") && !pinnedIds.contains("qs_page_indicator_style_card")) unpinnedHeaderControls.add(pageIndicatorStyleCard)
        if (!pinnedIds.contains("header_status_background_card")) unpinnedHeaderControls.add(headerStatusBackgroundCard)
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

        // 5. Brightness Controls
        val unpinnedBrightnessControls = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
        if (!pinnedIds.contains("hide_brightness_percentage_card")) unpinnedBrightnessControls.add(hideBrightnessPercentageCard)
        if (!pinnedIds.contains("hide_brightness_icon_card")) unpinnedBrightnessControls.add(hideBrightnessIconCard)
        if (!pinnedIds.contains("show_brightness_plus_minus_card")) unpinnedBrightnessControls.add(showBrightnessPlusMinusCard)
        if (!pinnedIds.contains("show_brightness_controls_background_card")) unpinnedBrightnessControls.add(showBrightnessControlsBackgroundCard)

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
        if (!isClassicStyle && !pinnedIds.contains("hide_date_in_expanded_card")) unpinnedTypography.add(hideDateInExpandedCard)
        if (!pinnedIds.contains("disable_labels_in_expanded_card")) unpinnedTypography.add(disableLabelsInExpandedCard)
        if (!isClassicStyle && !pinnedIds.contains("hide_qqs_drag_handle_card")) unpinnedTypography.add(hideQqsDragHandleCard)
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
