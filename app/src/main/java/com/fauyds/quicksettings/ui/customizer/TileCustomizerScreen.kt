package com.fauyds.quicksettings.ui.customizer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.fauyds.quicksettings.ui.theme.SegmentedButtonShapes
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fauyds.quicksettings.model.QsGridLayout
import com.fauyds.quicksettings.model.QsTileModel
import com.fauyds.quicksettings.model.TileCategory
import com.fauyds.quicksettings.ui.theme.ExpressiveCardShapes
import com.fauyds.quicksettings.ui.theme.MonetPrimary
import com.fauyds.quicksettings.ui.theme.MonetOnPrimary
import com.fauyds.quicksettings.ui.theme.MonetTertiary

@Composable
fun TileCustomizerScreen(
    tiles: List<QsTileModel>,
    selectedGridLayout: QsGridLayout = QsGridLayout.DEFAULT,
    onSelectGridLayout: (QsGridLayout) -> Unit = {},
    qqsTileSize: Int = 48,
    onSelectQqsTileSize: (Int) -> Unit = {},
    qqsTileCount: Int = 6,
    onSelectQqsTileCount: (Int) -> Unit = {},
    editButtonPosition: com.fauyds.quicksettings.data.EditButtonPosition = com.fauyds.quicksettings.data.EditButtonPosition.FOOTER,
    onSelectEditButtonPosition: (com.fauyds.quicksettings.data.EditButtonPosition) -> Unit = {},
    showDateInQqsHeader: Boolean = false,
    onToggleShowDateInQqsHeader: (Boolean) -> Unit = {},
    onToggleVisibility: (String, Boolean) -> Unit,
    onMoveTile: (fromIndex: Int, toIndex: Int) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val activeTiles = remember(tiles) { tiles.filter { it.isVisible } }
    val hiddenTiles = remember(tiles) { tiles.filter { !it.isVisible } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(ExpressiveCardShapes.CardSpacing) // 4.dp
    ) {
        // TOP CARD: top corners 32dp, bottom corners 8dp
        SettingsCard(
            shape = ExpressiveCardShapes.TopCard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "TILE STUDIO & GRID LAYOUT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MonetPrimary
                )

                Text(
                    text = "Reorder active tiles, adjust QQS tile size & quantity, or move tiles between active and available pools.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // QQS Icon Quantity Slider
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QQS Icon Quantity:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$qqsTileCount icons",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                        )
                    }

                    androidx.compose.material3.Slider(
                        value = qqsTileCount.toFloat(),
                        onValueChange = { onSelectQqsTileCount(it.toInt()) },
                        valueRange = 2f..6f,
                        steps = 3,
                        colors = androidx.compose.material3.SliderDefaults.colors(
                            thumbColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            activeTrackColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("qqs_tile_count_slider")
                    )
                }

                // QQS Tile Circle Size Slider
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QQS Tile Size:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${qqsTileSize}dp",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                        )
                    }

                    androidx.compose.material3.Slider(
                        value = qqsTileSize.toFloat(),
                        onValueChange = { onSelectQqsTileSize(it.toInt()) },
                        valueRange = 36f..64f,
                        colors = androidx.compose.material3.SliderDefaults.colors(
                            thumbColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            activeTrackColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("qqs_tile_size_slider")
                    )
                }

                // Date in QQS Header option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Move Date to Header Inline:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Display date inline next to time in header",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    androidx.compose.material3.Switch(
                        checked = showDateInQqsHeader,
                        onCheckedChange = onToggleShowDateInQqsHeader,
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            checkedThumbColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("switch_date_in_qqs")
                    )
                }

                // Edit Pencil Icon Position Option
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Edit Pencil Icon Location:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val allPositions = com.fauyds.quicksettings.data.EditButtonPosition.entries
                        allPositions.forEach { pos ->
                            val isSelected = editButtonPosition == pos
                            val shape = SegmentedButtonShapes.getShape(
                                item = pos,
                                items = allPositions,
                                selectedItem = editButtonPosition
                            )
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectEditButtonPosition(pos) },
                                shape = shape,
                                border = null,
                                label = { Text(pos.label, fontSize = 10.sp) },
                                colors = settingsChipColors(),
                                modifier = Modifier.testTag("edit_pos_${pos.id}")
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Grid Density:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val allGrids = QsGridLayout.entries
                    allGrids.forEach { grid ->
                        val isSelected = selectedGridLayout == grid
                        val shape = SegmentedButtonShapes.getShape(
                            item = grid,
                            items = allGrids,
                            selectedItem = selectedGridLayout
                        )
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectGridLayout(grid) },
                            shape = shape,
                            border = null,
                            label = { Text(grid.label, fontSize = 11.sp) },
                            colors = settingsChipColors(selectedLabelColor = Color(0xFF003732)),
                            modifier = Modifier.testTag("grid_col_${grid.id}")
                        )
                    }
                }
            }
        }

        // CENTER CARD: top corners 8dp, bottom corners 8dp
        SettingsCard(
            shape = ExpressiveCardShapes.CenterCard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE TILES (${activeTiles.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = MonetTertiary
                    )
                    Text(
                        text = "Top $qqsTileCount in QQS",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (activeTiles.isEmpty()) {
                    Text("No active tiles. Add from below.", color = Color.Gray, fontSize = 12.sp)
                } else {
                    activeTiles.forEachIndexed { index, tile ->
                        ActiveTileReorderItem(
                            index = index,
                            tile = tile,
                            isFirst = index == 0,
                            isLast = index == activeTiles.lastIndex,
                            onMoveUp = { onMoveTile(index, index - 1) },
                            onMoveDown = { onMoveTile(index, index + 1) },
                            onRemove = { onToggleVisibility(tile.id, true) }
                        )
                    }
                }
            }
        }

        // BOTTOM CARD: top corners 8dp, bottom corners 32dp
        SettingsCard(
            shape = ExpressiveCardShapes.BottomCard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "AVAILABLE / HIDDEN TILES (${hiddenTiles.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = MonetPrimary
                )

                if (hiddenTiles.isEmpty()) {
                    Text("All tiles are currently active in your Quick Settings panel.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                } else {
                    hiddenTiles.forEach { tile ->
                        HiddenTileItem(
                            tile = tile,
                            onAdd = { onToggleVisibility(tile.id, false) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    onClick = onNavigateBack,
                    shape = CircleShape,
                    color = MonetPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_and_preview_button")
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Save & Return to Overlay",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MonetOnPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveTileReorderItem(
    index: Int,
    tile: QsTileModel,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                val isThirdParty = tile.isExternal || tile.category == TileCategory.THIRD_PARTY || tile.id.contains("/")
                if (isThirdParty) {
                    val pkg = tile.packageName ?: if (tile.id.contains("/")) tile.id.substringBefore('/') else null
                    val cls = tile.serviceClassName ?: if (tile.id.contains("/")) tile.id.substringAfter('/') else null
                    com.fauyds.quicksettings.system.ExternalTileIcon(
                        packageName = pkg,
                        serviceClassName = cls,
                        tintColor = MaterialTheme.colorScheme.onPrimary,
                        contentDescription = tile.label,
                        fallbackDrawable = tile.externalDrawable,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    tile.iconVector?.let {
                        Icon(it, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                    } ?: Icon(Icons.Default.DragHandle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                }
            }

            Column {
                Text(
                    text = "${index + 1}. ${tile.label}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${tile.category.title} • ${tile.subtitle}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Move Up
            IconButton(
                onClick = onMoveUp,
                enabled = !isFirst,
                modifier = Modifier.size(32.dp).testTag("move_up_${tile.id}")
            ) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = "Move Up",
                    tint = if (!isFirst) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Move Down
            IconButton(
                onClick = onMoveDown,
                enabled = !isLast,
                modifier = Modifier.size(32.dp).testTag("move_down_${tile.id}")
            ) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Move Down",
                    tint = if (!isLast) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Remove to available
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp).testTag("remove_tile_${tile.id}")
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Hide Tile",
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun HiddenTileItem(
    tile: QsTileModel,
    onAdd: () -> Unit
) {
    val isDark = com.fauyds.quicksettings.ui.theme.LocalIsDarkTheme.current
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val inactiveBg = if (isDark) {
        Color(
            red = (surfaceVariant.red * 0.72f).coerceIn(0f, 1f),
            green = (surfaceVariant.green * 0.72f).coerceIn(0f, 1f),
            blue = (surfaceVariant.blue * 0.72f).coerceIn(0f, 1f),
            alpha = surfaceVariant.alpha
        )
    } else {
        Color.White
    }
    val inactiveIcon = if (isDark) Color.White.copy(alpha = 0.90f) else Color(0xFF1E293B)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(com.fauyds.quicksettings.ui.theme.getSettingsScreenBackgroundColor(isDark))
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(inactiveBg),
                contentAlignment = Alignment.Center
            ) {
                val isThirdParty = tile.isExternal || tile.category == TileCategory.THIRD_PARTY || tile.id.contains("/")
                if (isThirdParty) {
                    val pkg = tile.packageName ?: if (tile.id.contains("/")) tile.id.substringBefore('/') else null
                    val cls = tile.serviceClassName ?: if (tile.id.contains("/")) tile.id.substringAfter('/') else null
                    com.fauyds.quicksettings.system.ExternalTileIcon(
                        packageName = pkg,
                        serviceClassName = cls,
                        tintColor = inactiveIcon,
                        contentDescription = tile.label,
                        fallbackDrawable = tile.externalDrawable,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    tile.iconVector?.let {
                        Icon(it, contentDescription = null, tint = inactiveIcon, modifier = Modifier.size(18.dp))
                    } ?: Icon(Icons.Default.Add, contentDescription = null, tint = inactiveIcon, modifier = Modifier.size(18.dp))
                }
            }

            Column {
                Text(
                    text = tile.label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = tile.category.title,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Surface(
            onClick = onAdd,
            shape = CircleShape,
            color = MonetPrimary.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MonetPrimary.copy(alpha = 0.4f)),
            modifier = Modifier.testTag("add_tile_${tile.id}")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = MonetPrimary, modifier = Modifier.size(14.dp))
                Text("Add", fontSize = 11.sp, color = MonetPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
