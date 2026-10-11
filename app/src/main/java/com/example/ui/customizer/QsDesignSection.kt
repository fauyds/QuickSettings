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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.TextButton
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.PanelPreferences
import com.example.data.ProgressBarStyle
import com.example.data.TileShape
import com.example.ui.components.WavySlider
import com.example.ui.components.QsProgressSlider
import com.example.ui.theme.ExpressiveCardShapes
import com.example.ui.theme.SegmentedButtonShapes
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
    val wavyProgressBars by PanelPreferences.wavyProgressBars.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ExpressiveCardShapes.CardSpacing)
    ) {
        // 1. QQS Icon Size Card
        SettingsCard(
            shape = CustomizeCardShapes.TopCard
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
                        Text(
                            text = "QQS Icon Size",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 17.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$qqsTileSizeValue dp",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "Adjust the size of the circular icons in the compact Quick Quick Settings (QQS) panel.",
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

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
        SettingsCard(
            shape = CustomizeCardShapes.CenterCard
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
                        Text(
                            text = "QQS Icon Quantity",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 17.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$qqsTileCount icons",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "Set the number of quick toggle circular icons shown in the collapsed header row (from 2 to 6).",
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

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
        SettingsCard(
            shape = CustomizeCardShapes.CenterCard,
            modifier = Modifier.testTag("tile_shape_selection_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Enabled Tile Shape
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Enabled Tile Shape",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Choose the shape when a tile is active/ON",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
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
                        val allShapes = TileShape.entries
                        allShapes.forEach { shape ->
                            val selected = (currentEnabledTileShape == shape)
                            val chipShape = SegmentedButtonShapes.getShape(
                                item = shape,
                                items = allShapes,
                                selectedItem = currentEnabledTileShape
                            )
                            FilterChip(
                                selected = selected,
                                onClick = { PanelPreferences.setEnabledTileShape(context, shape) },
                                shape = chipShape,
                                border = null,
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
                                                    // 8 Custom Shape Placeholders
                                                    TileShape.CUSTOM_1 -> RoundedCornerShape(topStart = 7.dp, topEnd = 2.dp, bottomEnd = 7.dp, bottomStart = 2.dp)
                                                    TileShape.CUSTOM_2 -> RoundedCornerShape(topStart = 2.dp, topEnd = 7.dp, bottomEnd = 2.dp, bottomStart = 7.dp)
                                                    TileShape.CUSTOM_3 -> RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp, bottomEnd = 2.dp, bottomStart = 2.dp)
                                                    TileShape.CUSTOM_4 -> RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp, bottomEnd = 7.dp, bottomStart = 7.dp)
                                                    TileShape.CUSTOM_5 -> RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomEnd = 1.dp, bottomStart = 6.dp)
                                                    TileShape.CUSTOM_6 -> RoundedCornerShape(topStart = 7.dp, topEnd = 4.dp, bottomEnd = 7.dp, bottomStart = 4.dp)
                                                    TileShape.CUSTOM_7 -> RoundedCornerShape(5.dp)
                                                    TileShape.CUSTOM_8 -> RoundedCornerShape(2.dp)
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
                                    containerColor = MaterialTheme.colorScheme.background,
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
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val allShapes = TileShape.entries
                        allShapes.forEach { shape ->
                            val selected = (currentDisabledTileShape == shape)
                            val chipShape = SegmentedButtonShapes.getShape(
                                item = shape,
                                items = allShapes,
                                selectedItem = currentDisabledTileShape
                            )
                            FilterChip(
                                selected = selected,
                                onClick = { PanelPreferences.setDisabledTileShape(context, shape) },
                                shape = chipShape,
                                border = null,
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
                                                    // 8 Custom Shape Placeholders
                                                    TileShape.CUSTOM_1 -> RoundedCornerShape(topStart = 7.dp, topEnd = 2.dp, bottomEnd = 7.dp, bottomStart = 2.dp)
                                                    TileShape.CUSTOM_2 -> RoundedCornerShape(topStart = 2.dp, topEnd = 7.dp, bottomEnd = 2.dp, bottomStart = 7.dp)
                                                    TileShape.CUSTOM_3 -> RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp, bottomEnd = 2.dp, bottomStart = 2.dp)
                                                    TileShape.CUSTOM_4 -> RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp, bottomEnd = 7.dp, bottomStart = 7.dp)
                                                    TileShape.CUSTOM_5 -> RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomEnd = 1.dp, bottomStart = 6.dp)
                                                    TileShape.CUSTOM_6 -> RoundedCornerShape(topStart = 7.dp, topEnd = 4.dp, bottomEnd = 7.dp, bottomStart = 4.dp)
                                                    TileShape.CUSTOM_7 -> RoundedCornerShape(5.dp)
                                                    TileShape.CUSTOM_8 -> RoundedCornerShape(2.dp)
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
                                    containerColor = MaterialTheme.colorScheme.background,
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
            SettingsCard(
                shape = CustomizeCardShapes.CenterCard,
                modifier = Modifier.testTag("carrier_divider_card")
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

        // 8. Progress Bar Style Card (Previous M3, Straight, Wavy)
        var previewSliderValue by remember { mutableFloatStateOf(0.65f) }
        val currentProgressBarStyle by PanelPreferences.progressBarStyle.collectAsState()
        SettingsCard(
            shape = CustomizeCardShapes.BottomCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Progress Bar & Slider Style",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (currentProgressBarStyle != ProgressBarStyle.DEFAULT) {
                            TextButton(
                                onClick = { PanelPreferences.setProgressBarStyle(context, ProgressBarStyle.DEFAULT) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("revert_progress_bar_style_button")
                            ) {
                                Text(
                                    text = "Revert to Previous",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Text(
                        text = "Customize the progress bar and slider appearance for media player, volume, and brightness. Choose between the previous Material 3 slider, sleek straight bar, or Android 13/14 animated wavy track.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 3-way Segmented Button Row (No outline, selected rounded, non-selected inner 8dp / outer round)
                val allStyles = ProgressBarStyle.entries
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    allStyles.forEach { style ->
                        val selected = (currentProgressBarStyle == style)
                        val chipShape = SegmentedButtonShapes.getShape(
                            item = style,
                            items = allStyles,
                            selectedItem = currentProgressBarStyle
                        )
                        FilterChip(
                            selected = selected,
                            onClick = { PanelPreferences.setProgressBarStyle(context, style) },
                            shape = chipShape,
                            border = null,
                            label = {
                                Text(
                                    text = style.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier.size(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val iconColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                    when (style) {
                                        ProgressBarStyle.DEFAULT -> {
                                            // Mini classic M3 rounded slider
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
                                            // Mini straight line with thumb
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
                                            // Mini wavy path
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
                                    }
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.background,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("progress_bar_style_${style.id}")
                        )
                    }
                }

                // Interactive live preview inside card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Interactive Preview",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = when (currentProgressBarStyle) {
                                ProgressBarStyle.DEFAULT -> "Previous Style (Default M3)"
                                ProgressBarStyle.STRAIGHT -> "Straight Progress Bar"
                                ProgressBarStyle.WAVY -> "Squiggly Wave Active"
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    QsProgressSlider(
                        value = previewSliderValue,
                        onValueChange = { previewSliderValue = it },
                        style = currentProgressBarStyle,
                        isPlaying = (currentProgressBarStyle == ProgressBarStyle.WAVY),
                        activeColor = MaterialTheme.colorScheme.primary,
                        inactiveColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thumbColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wavy_progress_bars_preview_slider")
                    )
                }
            }
        }
    }
}
