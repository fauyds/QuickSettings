package com.example.ui.customizer

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.PushPin
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
import com.example.data.DatePosition
import com.example.data.EditButtonPosition
import com.example.data.PanelPreferences
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
    val hideQqsDragHandle by PanelPreferences.hideQqsDragHandle.collectAsState()
    val qsTileLabelFontSize by PanelPreferences.qsTileLabelFontSize.collectAsState()
    val qsTileSubtitleFontSize by PanelPreferences.qsTileSubtitleFontSize.collectAsState()
    val hideDateInExpanded by PanelPreferences.hideDateInExpanded.collectAsState()
    val disableLabelsInExpanded by PanelPreferences.disableLabelsInExpanded.collectAsState()
    val collapseExpandOptions by PanelPreferences.collapseExpandOptions.collectAsState()

    var fontSizesExpanded by rememberSaveable { mutableStateOf(false) }

    val pinnedIds by PinnedCardsManager.getPinnedCardIds(CustomizeCategory.QS_TWEAKS, context).collectAsState()

    val gridDensityCard = @Composable {
        PinnableCardWrapper("qs_grid_density_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = CustomizeCardShapes.SingleCard
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
                        val shape = SegmentedButtonShapes.getShape(
                            item = grid,
                            items = allGrids,
                            selectedItem = gridLayout
                        )
                        FilterChip(
                            selected = isSelected,
                            onClick = { PanelPreferences.setGridLayout(context, grid) },
                            label = { Text(grid.label, fontSize = 11.sp) },
                            shape = shape,
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

    val dragHandleLengthCard = @Composable {
        PinnableCardWrapper("qs_drag_handle_length_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = CustomizeCardShapes.SingleCard
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("20 dp", fontSize = 10.sp, color = Color.Gray)
                    Text("Default (36 dp)", fontSize = 10.sp, color = Color.Gray)
                    Text("80 dp", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

    val datePositionCard = @Composable {
        PinnableCardWrapper("date_position_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = CustomizeCardShapes.TopCard,
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
                        val shape = SegmentedButtonShapes.getShape(
                            item = pos,
                            items = allDatePositions,
                            selectedItem = datePosition
                        )
                        FilterChip(
                            selected = isSelected,
                            onClick = { PanelPreferences.setDatePosition(context, pos) },
                            label = { Text(pos.label, fontSize = 10.sp) },
                            shape = shape,
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

    val editPencilLocationCard = @Composable {
        PinnableCardWrapper("edit_pencil_location_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = CustomizeCardShapes.BottomCard
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
                        val shape = SegmentedButtonShapes.getShape(
                            item = pos,
                            items = allEditPositions,
                            selectedItem = editButtonPosition
                        )
                        FilterChip(
                            selected = isSelected,
                            onClick = { PanelPreferences.setEditButtonPosition(context, pos) },
                            label = { Text(pos.label, fontSize = 10.sp) },
                            shape = shape,
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

    val tileFontSizeCard = @Composable {
        PinnableCardWrapper("qs_tile_font_size_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = CustomizeCardShapes.TopCard,
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

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("8 sp", fontSize = 10.sp, color = Color.Gray)
                                Text("Default (12 sp)", fontSize = 10.sp, color = Color.Gray)
                                Text("18 sp", fontSize = 10.sp, color = Color.Gray)
                            }
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

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("6 sp", fontSize = 10.sp, color = Color.Gray)
                                Text("Default (9 sp)", fontSize = 10.sp, color = Color.Gray)
                                Text("14 sp", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

    val hideDateInExpandedCard = @Composable {
        PinnableCardWrapper("hide_date_in_expanded_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = CustomizeCardShapes.TopCard
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

    val disableLabelsInExpandedCard = @Composable {
        PinnableCardWrapper("disable_labels_in_expanded_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = CustomizeCardShapes.CenterCard
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

    val hideQqsDragHandleCard = @Composable {
        PinnableCardWrapper("hide_qqs_drag_handle_card", CustomizeCategory.QS_TWEAKS) {
            SettingsCard(
                shape = CustomizeCardShapes.BottomCard
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

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ExpressiveCardShapes.CardSpacing)
    ) {
        // 1. Pinned Cards at the very top of Quick Settings Tweaks
        val pinnedCards = mutableListOf<@Composable () -> Unit>()
        if (pinnedIds.contains("qs_grid_density_card")) pinnedCards.add(gridDensityCard)
        if (pinnedIds.contains("qs_drag_handle_length_card")) pinnedCards.add(dragHandleLengthCard)
        if (pinnedIds.contains("date_position_card")) pinnedCards.add(datePositionCard)
        if (pinnedIds.contains("edit_pencil_location_card")) pinnedCards.add(editPencilLocationCard)
        if (pinnedIds.contains("qs_tile_font_size_card")) pinnedCards.add(tileFontSizeCard)
        if (pinnedIds.contains("hide_date_in_expanded_card")) pinnedCards.add(hideDateInExpandedCard)
        if (pinnedIds.contains("disable_labels_in_expanded_card")) pinnedCards.add(disableLabelsInExpandedCard)
        if (pinnedIds.contains("hide_qqs_drag_handle_card")) pinnedCards.add(hideQqsDragHandleCard)

        if (pinnedCards.isNotEmpty()) {
            ListSubheader(
                text = "Pinned",
                icon = androidx.compose.material.icons.Icons.Default.PushPin
            )
            pinnedCards.forEach { card -> card() }
        }

        // 2. Unpinned Grid & Tile Density
        if (!pinnedIds.contains("qs_grid_density_card")) {
            CollapsibleSubheaderSection(
                title = "Grid & Tile Density",
                icon = androidx.compose.material.icons.Icons.Default.GridView
            ) {
                gridDensityCard()
            }
        }

        // 3. Unpinned Drag Handle Options
        if (!pinnedIds.contains("qs_drag_handle_length_card")) {
            CollapsibleSubheaderSection(
                title = "Drag Handle Options",
                icon = androidx.compose.material.icons.Icons.Default.DragHandle
            ) {
                dragHandleLengthCard()
            }
        }

        // 4. Unpinned Header & Controls
        if (!pinnedIds.contains("date_position_card") || !pinnedIds.contains("edit_pencil_location_card")) {
            CollapsibleSubheaderSection(
                title = "Header & Controls",
                icon = androidx.compose.material.icons.Icons.Default.Tune
            ) {
                if (!pinnedIds.contains("date_position_card")) {
                    datePositionCard()
                }
                if (!pinnedIds.contains("edit_pencil_location_card")) {
                    editPencilLocationCard()
                }
            }
        }

        // 5. Unpinned Typography & Font Sizes
        if (!pinnedIds.contains("qs_tile_font_size_card") ||
            !pinnedIds.contains("hide_date_in_expanded_card") ||
            !pinnedIds.contains("disable_labels_in_expanded_card") ||
            !pinnedIds.contains("hide_qqs_drag_handle_card")
        ) {
            CollapsibleSubheaderSection(
                title = "Typography & Font Sizes",
                icon = androidx.compose.material.icons.Icons.Default.TextFields
            ) {
                if (!pinnedIds.contains("qs_tile_font_size_card")) {
                    tileFontSizeCard()
                }
                if (!pinnedIds.contains("hide_date_in_expanded_card")) {
                    hideDateInExpandedCard()
                }
                if (!pinnedIds.contains("disable_labels_in_expanded_card")) {
                    disableLabelsInExpandedCard()
                }
                if (!pinnedIds.contains("hide_qqs_drag_handle_card")) {
                    hideQqsDragHandleCard()
                }
            }
        }
    }
}
