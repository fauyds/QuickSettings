package com.example.ui.customizer

import androidx.compose.foundation.BorderStroke
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
import com.example.ui.theme.SegmentedButtonShapes
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.runtime.collectAsState
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
import com.example.data.PanelPreferences
import com.example.model.QsGridLayout
import com.example.model.QsTileModel
import com.example.model.TileCategory
import com.example.ui.theme.ExpressiveCardShapes
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.MonetPrimary
import com.example.ui.theme.MonetOnPrimary
import com.example.ui.theme.MonetTertiary

@Composable
fun TileCustomizerScreen(
    tiles: List<QsTileModel>,
    selectedGridLayout: QsGridLayout = QsGridLayout.DEFAULT,
    onSelectGridLayout: (QsGridLayout) -> Unit = {},
    qqsTileSize: Int = 48,
    onSelectQqsTileSize: (Int) -> Unit = {},
    qqsTileCount: Int = 6,
    onSelectQqsTileCount: (Int) -> Unit = {},
    editButtonPosition: com.example.data.EditButtonPosition = com.example.data.EditButtonPosition.FOOTER,
    onSelectEditButtonPosition: (com.example.data.EditButtonPosition) -> Unit = {},
    showDateInQqsHeader: Boolean = false,
    onToggleShowDateInQqsHeader: (Boolean) -> Unit = {},
    onToggleVisibility: (String, Boolean) -> Unit,
    onMoveTile: (fromIndex: Int, toIndex: Int) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val activeTiles = remember(tiles) { tiles.filter { it.isVisible } }
    val availableTiles = remember(tiles) { tiles.filter { !it.isVisible } }

    val isKitKat by PanelPreferences.isAndroidKitKatStyle.collectAsState()
    val isMarshmallow by PanelPreferences.isAndroidMarshmallowStyle.collectAsState()
    val panelOuterCorner by PanelPreferences.panelOuterCorner.collectAsState()
    val isDark = LocalIsDarkTheme.current

    val screenBg = if (isKitKat) {
        if (isDark) Color(0xFF000000) else Color(0xFFE2E2E2)
    } else if (isMarshmallow) {
        if (isDark) Color(0xFF1E272C) else Color(0xFFECEFF1)
    } else {
        MaterialTheme.colorScheme.background
    }

    val topCardShape = if (isKitKat || isMarshmallow) RoundedCornerShape(panelOuterCorner.dp) else ExpressiveCardShapes.TopCard
    val centerCardShape = if (isKitKat || isMarshmallow) RoundedCornerShape(panelOuterCorner.dp) else ExpressiveCardShapes.CenterCard
    val bottomCardShape = if (isKitKat || isMarshmallow) RoundedCornerShape(panelOuterCorner.dp) else ExpressiveCardShapes.BottomCard

    val cardContainerColor = if (isKitKat) {
        if (isDark) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    } else if (isMarshmallow) {
        if (isDark) Color(0xFF263238) else Color(0xFFFFFFFF)
    } else {
        com.example.ui.theme.getSettingsCardBackgroundColor(isDark)
    }

    val topCardBorder = if (isKitKat) {
        Modifier.border(1.dp, if (isDark) Color(0xFF2C2C2C) else Color(0xFFCCCCCC), topCardShape)
    } else if (isMarshmallow) {
        Modifier.border(1.dp, if (isDark) Color(0xFF37474F) else Color(0xFFCFD8DC), topCardShape)
    } else {
        Modifier
    }

    val centerCardBorder = if (isKitKat) {
        Modifier.border(1.dp, if (isDark) Color(0xFF2C2C2C) else Color(0xFFCCCCCC), centerCardShape)
    } else if (isMarshmallow) {
        Modifier.border(1.dp, if (isDark) Color(0xFF37474F) else Color(0xFFCFD8DC), centerCardShape)
    } else {
        Modifier
    }

    val bottomCardBorder = if (isKitKat) {
        Modifier.border(1.dp, if (isDark) Color(0xFF2C2C2C) else Color(0xFFCCCCCC), bottomCardShape)
    } else if (isMarshmallow) {
        Modifier.border(1.dp, if (isDark) Color(0xFF37474F) else Color(0xFFCFD8DC), bottomCardShape)
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(if (isKitKat || isMarshmallow) 8.dp else ExpressiveCardShapes.CardSpacing)
    ) {
        // TOP CARD
        SettingsCard(
            shape = topCardShape,
            colors = CardDefaults.cardColors(containerColor = cardContainerColor),
            modifier = Modifier.fillMaxWidth().then(topCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isKitKat) {
                    Text(
                        text = "QUICK SETTINGS TILES (ANDROID 4.4.4 KITKAT)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color(0xFF33B5E5)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(Color(0xFF33B5E5))
                    )
                    Text(
                        text = "Customize Quick Settings tile grid density and arrange active and available tiles. KitKat uses a unified grid without collapsed QQS header icons.",
                        fontSize = 12.5.sp,
                        color = if (isDark) Color(0xFFAAAAAA) else Color(0xFF555555)
                    )
                } else if (isMarshmallow) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Color(0xFF00BFA5),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "SYSTEM UI TUNER • QUICK SETTINGS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color(0xFF00BFA5)
                        )
                    }
                    Text(
                        text = "Rearrange tiles to reorder your Marshmallow Quick Settings panel. Top tiles appear in the collapsed Quick Settings header.",
                        fontSize = 12.5.sp,
                        color = if (isDark) Color(0xFFB0BEC5) else Color(0xFF546E7A)
                    )
                } else {
                    Text(
                        text = "TILE STUDIO & GRID LAYOUT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MonetPrimary
                    )

                    Text(
                        text = "Reorder active panel tiles, adjust QQS settings, or add/remove tiles between active and available panel pools. (To totally hide or blacklist tiles, visit the Tile Visibility tab).",
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
                            val allPositions = com.example.data.EditButtonPosition.entries
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
                }

                // Grid Density Selection
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
                        color = if (isKitKat) (if (isDark) Color.White else Color.Black) else if (isMarshmallow) (if (isDark) Color.White else Color(0xFF263238)) else MaterialTheme.colorScheme.onSurface
                    )

                    val allGrids = QsGridLayout.entries
                    allGrids.forEach { grid ->
                        val isSelected = selectedGridLayout == grid
                        val chipShape = if (isKitKat || isMarshmallow) RoundedCornerShape(panelOuterCorner.dp) else SegmentedButtonShapes.getShape(
                            item = grid,
                            items = allGrids,
                            selectedItem = selectedGridLayout
                        )
                        val chipBorder = if (isKitKat) {
                            BorderStroke(1.dp, if (isSelected) Color(0xFF33B5E5) else (if (isDark) Color(0xFF333333) else Color(0xFFCCCCCC)))
                        } else if (isMarshmallow) {
                            BorderStroke(1.dp, if (isSelected) Color(0xFF00BFA5) else (if (isDark) Color(0xFF455A64) else Color(0xFFCFD8DC)))
                        } else null

                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectGridLayout(grid) },
                            shape = chipShape,
                            border = chipBorder,
                            label = {
                                Text(
                                    grid.label,
                                    fontSize = 11.sp,
                                    color = if (isKitKat) (if (isSelected) Color.White else (if (isDark) Color(0xFFAAAAAA) else Color(0xFF444444)))
                                    else if (isMarshmallow) (if (isSelected) Color.White else (if (isDark) Color(0xFFCFD8DC) else Color(0xFF37474F)))
                                    else Color.Unspecified
                                )
                            },
                            colors = if (isKitKat) {
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF33B5E5),
                                    containerColor = if (isDark) Color(0xFF222222) else Color(0xFFEEEEEE)
                                )
                            } else if (isMarshmallow) {
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF009688),
                                    containerColor = if (isDark) Color(0xFF37474F) else Color(0xFFECEFF1)
                                )
                            } else {
                                settingsChipColors(selectedLabelColor = Color(0xFF003732))
                            },
                            modifier = Modifier.testTag("grid_col_${grid.id}")
                        )
                    }
                }
            }
        }

        // CENTER CARD: ACTIVE TILES
        SettingsCard(
            shape = centerCardShape,
            colors = CardDefaults.cardColors(containerColor = cardContainerColor),
            modifier = Modifier.fillMaxWidth().then(centerCardBorder)
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
                        color = if (isKitKat) Color(0xFF33B5E5) else if (isMarshmallow) Color(0xFF00BFA5) else MonetTertiary
                    )
                    Text(
                        text = if (isKitKat) "Tiles displayed in Quick Settings" else if (isMarshmallow) "Top tiles appear in header" else "Top $qqsTileCount in QQS",
                        fontSize = 11.sp,
                        color = if (isKitKat) (if (isDark) Color(0xFF888888) else Color(0xFF666666)) else if (isMarshmallow) (if (isDark) Color(0xFF90A4AE) else Color(0xFF78909C)) else MaterialTheme.colorScheme.onSurfaceVariant
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
                            isKitKat = isKitKat,
                            isMarshmallow = isMarshmallow,
                            onMoveUp = { onMoveTile(index, index - 1) },
                            onMoveDown = { onMoveTile(index, index + 1) },
                            onRemove = { onToggleVisibility(tile.id, true) }
                        )
                    }
                }
            }
        }

        // BOTTOM CARD: AVAILABLE TILES
        SettingsCard(
            shape = bottomCardShape,
            colors = CardDefaults.cardColors(containerColor = cardContainerColor),
            modifier = Modifier.fillMaxWidth().then(bottomCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "AVAILABLE TILES FOR PANEL (${availableTiles.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = if (isKitKat) Color(0xFF33B5E5) else if (isMarshmallow) Color(0xFF00BFA5) else MonetPrimary
                )

                if (availableTiles.isEmpty()) {
                    Text(
                        text = "All available tiles are currently added to your Quick Settings panel.",
                        color = if (isKitKat) (if (isDark) Color(0xFFAAAAAA) else Color(0xFF555555)) else if (isMarshmallow) (if (isDark) Color(0xFFB0BEC5) else Color(0xFF546E7A)) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                } else {
                    availableTiles.forEach { tile ->
                        HiddenTileItem(
                            tile = tile,
                            isKitKat = isKitKat,
                            isMarshmallow = isMarshmallow,
                            onAdd = { onToggleVisibility(tile.id, false) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    onClick = onNavigateBack,
                    shape = if (isKitKat || isMarshmallow) RoundedCornerShape(panelOuterCorner.dp) else CircleShape,
                    color = if (isKitKat) Color(0xFF33B5E5) else if (isMarshmallow) Color(0xFF00BFA5) else MonetPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_and_preview_button")
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isKitKat || isMarshmallow) "SAVE & RETURN TO OVERLAY" else "Save & Return to Overlay",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = if (isKitKat || isMarshmallow) 1.sp else 0.sp,
                            color = Color.White
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
    isKitKat: Boolean = false,
    isMarshmallow: Boolean = false,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val panelOuterCorner by PanelPreferences.panelOuterCorner.collectAsState()
    val itemShape = if (isKitKat || isMarshmallow) RoundedCornerShape(panelOuterCorner.dp) else RoundedCornerShape(12.dp)
    val itemBg = if (isKitKat) {
        if (isDark) Color(0xFF1E1E1E) else Color(0xFFF2F2F2)
    } else if (isMarshmallow) {
        if (isDark) Color(0xFF2E3C43) else Color(0xFFECEFF1)
    } else {
        MaterialTheme.colorScheme.background
    }
    val itemBorder = if (isKitKat) {
        BorderStroke(1.dp, if (isDark) Color(0xFF262626) else Color(0xFFCCCCCC))
    } else if (isMarshmallow) {
        BorderStroke(1.dp, if (isDark) Color(0xFF3E4F57) else Color(0xFFCFD8DC))
    } else {
        BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    }

    val iconShape = if (isKitKat || isMarshmallow) RoundedCornerShape(panelOuterCorner.dp) else CircleShape
    val iconBg = if (isKitKat) {
        if (isDark) Color(0xFF141414) else Color(0xFFDFDFDF)
    } else if (isMarshmallow) {
        if (isDark) Color(0xFF263238) else Color(0xFFDFE4E7)
    } else {
        MaterialTheme.colorScheme.primary
    }
    val iconTint = if (isKitKat) {
        Color(0xFF33B5E5)
    } else if (isMarshmallow) {
        Color(0xFF00BFA5)
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    val textPrimary = if (isKitKat) {
        if (isDark) Color.White else Color(0xFF222222)
    } else if (isMarshmallow) {
        if (isDark) Color.White else Color(0xFF263238)
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val textSecondary = if (isKitKat) {
        if (isDark) Color(0xFF888888) else Color(0xFF666666)
    } else if (isMarshmallow) {
        if (isDark) Color(0xFF90A4AE) else Color(0xFF546E7A)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val arrowTint = if (isKitKat) Color(0xFF33B5E5) else if (isMarshmallow) Color(0xFF00BFA5) else MaterialTheme.colorScheme.onSurface
    val removeTint = if (isKitKat) Color(0xFFFF4444) else if (isMarshmallow) Color(0xFFE53935) else Color(0xFFFF6B6B)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(itemShape)
            .background(itemBg)
            .border(itemBorder, itemShape)
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
                    .size(36.dp)
                    .clip(iconShape)
                    .background(iconBg)
                    .then(
                        if (isKitKat) Modifier.border(1.dp, Color(0xFF33B5E5).copy(alpha = 0.5f), iconShape) else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                val isThirdParty = tile.isExternal || tile.category == TileCategory.THIRD_PARTY || tile.id.contains("/")
                if (isThirdParty) {
                    val pkg = tile.packageName ?: if (tile.id.contains("/")) tile.id.substringBefore('/') else null
                    val cls = tile.serviceClassName ?: if (tile.id.contains("/")) tile.id.substringAfter('/') else null
                    com.example.system.ExternalTileIcon(
                        packageName = pkg,
                        serviceClassName = cls,
                        tintColor = iconTint,
                        contentDescription = tile.label,
                        fallbackDrawable = tile.externalDrawable,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    tile.iconVector?.let {
                        Icon(it, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                    } ?: Icon(Icons.Default.DragHandle, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                }
            }

            Column {
                Text(
                    text = if (isKitKat) "${index + 1}. ${tile.label.uppercase()}" else "${index + 1}. ${tile.label}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
                Text(
                    text = if (isKitKat) "${tile.category.title.uppercase()} • ${tile.subtitle.uppercase()}" else "${tile.category.title} • ${tile.subtitle}",
                    fontSize = 10.sp,
                    color = textSecondary
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
                    tint = if (!isFirst) arrowTint else arrowTint.copy(alpha = 0.35f),
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
                    tint = if (!isLast) arrowTint else arrowTint.copy(alpha = 0.35f),
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
                    tint = removeTint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun HiddenTileItem(
    tile: QsTileModel,
    isKitKat: Boolean = false,
    isMarshmallow: Boolean = false,
    onAdd: () -> Unit
) {
    val isDark = com.example.ui.theme.LocalIsDarkTheme.current
    val panelOuterCorner by PanelPreferences.panelOuterCorner.collectAsState()
    val itemShape = if (isKitKat || isMarshmallow) RoundedCornerShape(panelOuterCorner.dp) else RoundedCornerShape(12.dp)
    val itemBg = if (isKitKat) {
        if (isDark) Color(0xFF1E1E1E) else Color(0xFFF2F2F2)
    } else if (isMarshmallow) {
        if (isDark) Color(0xFF2E3C43) else Color(0xFFECEFF1)
    } else {
        com.example.ui.theme.getSettingsScreenBackgroundColor(isDark)
    }
    val itemBorder = if (isKitKat) {
        BorderStroke(1.dp, if (isDark) Color(0xFF262626) else Color(0xFFCCCCCC))
    } else if (isMarshmallow) {
        BorderStroke(1.dp, if (isDark) Color(0xFF3E4F57) else Color(0xFFCFD8DC))
    } else {
        BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    }

    val iconShape = if (isKitKat || isMarshmallow) RoundedCornerShape(panelOuterCorner.dp) else CircleShape
    val iconBg = if (isKitKat) {
        if (isDark) Color(0xFF141414) else Color(0xFFDFDFDF)
    } else if (isMarshmallow) {
        if (isDark) Color(0xFF263238) else Color(0xFFDFE4E7)
    } else {
        val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
        if (isDark) {
            Color(
                red = (surfaceVariant.red * 0.72f).coerceIn(0f, 1f),
                green = (surfaceVariant.green * 0.72f).coerceIn(0f, 1f),
                blue = (surfaceVariant.blue * 0.72f).coerceIn(0f, 1f),
                alpha = surfaceVariant.alpha
            )
        } else {
            Color.White
        }
    }
    val iconTint = if (isKitKat) {
        if (isDark) Color(0xFF888888) else Color(0xFF666666)
    } else if (isMarshmallow) {
        if (isDark) Color(0xFF90A4AE) else Color(0xFF78909C)
    } else {
        if (isDark) Color.White.copy(alpha = 0.90f) else Color(0xFF1E293B)
    }

    val textPrimary = if (isKitKat) {
        if (isDark) Color.White else Color(0xFF222222)
    } else if (isMarshmallow) {
        if (isDark) Color.White else Color(0xFF263238)
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val textSecondary = if (isKitKat) {
        if (isDark) Color(0xFF888888) else Color(0xFF666666)
    } else if (isMarshmallow) {
        if (isDark) Color(0xFF90A4AE) else Color(0xFF546E7A)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val addBtnShape = if (isKitKat || isMarshmallow) RoundedCornerShape(panelOuterCorner.dp) else CircleShape
    val addBtnColor = if (isKitKat) Color(0xFF33B5E5).copy(alpha = 0.15f) else if (isMarshmallow) Color(0xFF00BFA5).copy(alpha = 0.15f) else MonetPrimary.copy(alpha = 0.15f)
    val addBtnBorder = if (isKitKat) Color(0xFF33B5E5).copy(alpha = 0.5f) else if (isMarshmallow) Color(0xFF00BFA5).copy(alpha = 0.5f) else MonetPrimary.copy(alpha = 0.4f)
    val addBtnTint = if (isKitKat) Color(0xFF33B5E5) else if (isMarshmallow) Color(0xFF00BFA5) else MonetPrimary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(itemShape)
            .background(itemBg)
            .border(itemBorder, itemShape)
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
                    .size(36.dp)
                    .clip(iconShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                val isThirdParty = tile.isExternal || tile.category == TileCategory.THIRD_PARTY || tile.id.contains("/")
                if (isThirdParty) {
                    val pkg = tile.packageName ?: if (tile.id.contains("/")) tile.id.substringBefore('/') else null
                    val cls = tile.serviceClassName ?: if (tile.id.contains("/")) tile.id.substringAfter('/') else null
                    com.example.system.ExternalTileIcon(
                        packageName = pkg,
                        serviceClassName = cls,
                        tintColor = iconTint,
                        contentDescription = tile.label,
                        fallbackDrawable = tile.externalDrawable,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    tile.iconVector?.let {
                        Icon(it, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                    } ?: Icon(Icons.Default.Add, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                }
            }

            Column {
                Text(
                    text = if (isKitKat) tile.label.uppercase() else tile.label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = textPrimary
                )
                Text(
                    text = if (isKitKat) tile.category.title.uppercase() else tile.category.title,
                    fontSize = 10.sp,
                    color = textSecondary
                )
            }
        }

        Surface(
            onClick = onAdd,
            shape = addBtnShape,
            color = addBtnColor,
            border = BorderStroke(1.dp, addBtnBorder),
            modifier = Modifier.testTag("add_tile_${tile.id}")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = addBtnTint, modifier = Modifier.size(14.dp))
                Text(if (isKitKat) "ADD" else "Add", fontSize = 11.sp, color = addBtnTint, fontWeight = FontWeight.Bold)
            }
        }
    }
}
