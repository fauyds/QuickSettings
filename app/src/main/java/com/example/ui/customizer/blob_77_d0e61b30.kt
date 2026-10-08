package com.example.ui.customizer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PanelPreferences
import com.example.data.TileShape
import com.example.ui.theme.ExpressiveCardShapes
import com.example.ui.theme.MonetPrimary

@Composable
fun QsDesignSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val qqsTileSizeValue by PanelPreferences.qqsTileSize.collectAsState()
    val qqsTileCount by PanelPreferences.qqsTileCount.collectAsState()
    val currentEnabledTileShape by PanelPreferences.enabledTileShape.collectAsState()
    val currentDisabledTileShape by PanelPreferences.disabledTileShape.collectAsState()
    val showCarrier by PanelPreferences.showCarrier.collectAsState()
    val showCarrierDivider by PanelPreferences.showCarrierDivider.collectAsState()
    val brightnessAtBottom by PanelPreferences.brightnessAtBottom.collectAsState()
    val brightnessInQqs by PanelPreferences.brightnessInQqs.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ExpressiveCardShapes.CardSpacing)
    ) {
        // 1. QQS Icon Size Card
        Card(
            shape = CustomizeCardShapes.TopCard,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QQS Icon Size",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$qqsTileSizeValue dp",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "Adjust the size of the circular icons in the compact Quick Quick Settings (QQS) panel.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Slider(
                    value = qqsTileSizeValue.toFloat(),
                    onValueChange = { PanelPreferences.setQqsTileSize(context, it.toInt()) },
                    valueRange = 32f..64f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("qqs_tile_size_slider"),
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
                    Text("32 dp", fontSize = 10.sp, color = Color.Gray)
                    Text("Default (48 dp)", fontSize = 10.sp, color = Color.Gray)
                    Text("64 dp", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

        // 2. QQS Icon Quantity Card
        Card(
            shape = CustomizeCardShapes.CenterCard,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QQS Icon Quantity",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$qqsTileCount icons",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "Set the number of quick toggle circular icons shown in the collapsed header row (from 2 to 6).",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Slider(
                    value = qqsTileCount.toFloat(),
                    onValueChange = { PanelPreferences.setQqsTileCount(context, it.toInt()) },
                    valueRange = 2f..6f,
                    steps = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("qqs_tile_count_slider"),
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
                    Text("2 icons", fontSize = 10.sp, color = Color.Gray)
                    Text("Default (6 icons)", fontSize = 10.sp, color = Color.Gray)
                    Text("6 icons", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

        // 3. Tile Shape Customization (Enabled / Disabled)
        Card(
            shape = CustomizeCardShapes.CenterCard,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tile_shape_selection_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Enabled Tile Shape
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Enabled Tile Shape",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Choose the shape when a tile is active/ON",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TileShape.entries.forEach { shape ->
                            val selected = (currentEnabledTileShape == shape)
                            FilterChip(
                                selected = selected,
                                onClick = { PanelPreferences.setEnabledTileShape(context, shape) },
                                label = {
                                    Text(
                                        text = shape.label,
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(
                                                when (shape) {
                                                    TileShape.CIRCLE -> CircleShape
                                                    TileShape.SQUIRCLE -> RoundedCornerShape(4.dp)
                                                    TileShape.SQUARE -> RoundedCornerShape(1.dp)
                                                    TileShape.TEARDROP -> RoundedCornerShape(topStart = 7.dp, topEnd = 2.dp, bottomStart = 7.dp, bottomEnd = 7.dp)
                                                    TileShape.PILL -> RoundedCornerShape(6.dp)
                                                }
                                            )
                                            .background(
                                                if (selected) MaterialTheme.colorScheme.onPrimary
                                                else MaterialTheme.colorScheme.primary
                                            )
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.testTag("enabled_tile_shape_${shape.id}")
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Disabled Tile Shape
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Disabled Tile Shape",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Choose the shape when a tile is inactive/OFF",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TileShape.entries.forEach { shape ->
                            val selected = (currentDisabledTileShape == shape)
                            FilterChip(
                                selected = selected,
                                onClick = { PanelPreferences.setDisabledTileShape(context, shape) },
                                label = {
                                    Text(
                                        text = shape.label,
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(
                                                when (shape) {
                                                    TileShape.CIRCLE -> CircleShape
                                                    TileShape.SQUIRCLE -> RoundedCornerShape(4.dp)
                                                    TileShape.SQUARE -> RoundedCornerShape(1.dp)
                                                    TileShape.TEARDROP -> RoundedCornerShape(topStart = 7.dp, topEnd = 2.dp, bottomStart = 7.dp, bottomEnd = 7.dp)
                                                    TileShape.PILL -> RoundedCornerShape(6.dp)
                                                }
                                            )
                                            .background(
                                                if (selected) MaterialTheme.colorScheme.onPrimary
                                                else MaterialTheme.colorScheme.primary
                                            )
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.testTag("disabled_tile_shape_${shape.id}")
                            )
                        }
                    }
                }
            }
        }

        // 4. Show SIM Card Carrier Card
        Card(
            shape = CustomizeCardShapes.CenterCard,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Show SIM Card Carrier",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Show the dynamic SIM card network operator name on the bottom left of the QQS container.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

        // 5. Divider Between Carrier & QQS Tiles Card
        AnimatedVisibility(
            visible = showCarrier,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                shape = CustomizeCardShapes.CenterCard,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("carrier_divider_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Divider Between Carrier & QQS Tiles",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Show a divider line separating the SIM card carrier name and QQS tiles",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
            }
        }

        // 6. Move Brightness to Bottom Card
        Card(
            shape = CustomizeCardShapes.CenterCard,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Move Brightness to Bottom",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Move the brightness slider bar to the bottom of the QS panel (just above the footer).",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = brightnessAtBottom,
                    onCheckedChange = { PanelPreferences.setBrightnessAtBottom(context, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("brightness_at_bottom_toggle")
                )
            }
        }

        // 7. Show Brightness Slider in QQS Card
        Card(
            shape = CustomizeCardShapes.BottomCard,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Show Brightness Slider in QQS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Show the brightness slider inside the compact QQS container view (always visible).",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = brightnessInQqs,
                    onCheckedChange = { PanelPreferences.setBrightnessInQqs(context, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("brightness_in_qqs_toggle")
                )
            }
        }
    }
}
