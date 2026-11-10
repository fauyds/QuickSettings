package com.example.ui.customizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PanelPreferences
import com.example.service.HeadsUpOverlayManager

@Composable
fun HeadsUpDesignSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    val edgeToEdge by PanelPreferences.headsUpEdgeToEdge.collectAsState()
    val cornerRadius by PanelPreferences.headsUpCornerRadius.collectAsState()
    val elevation by PanelPreferences.headsUpElevation.collectAsState()
    val topMargin by PanelPreferences.headsUpTopMargin.collectAsState()

    val pinnedIds by PinnedCardsManager.getPinnedCardIds(CustomizeCategory.HEADS_UP_DESIGN, context).collectAsState()

    // 1. Test Notification Card
    val testNotificationCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("heads_up_design_test_card", CustomizeCategory.HEADS_UP_DESIGN) {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("heads_up_design_test_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Test Notification",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                )
                            )
                            Text(
                                text = "Trigger a floating notification banner to preview corner radius, elevation, edge-to-edge mode, and top margin settings.",
                                fontSize = 12.sp,
                                color = colorScheme.onSurfaceVariant,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            HeadsUpOverlayManager.showTestHeadsUp(context)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("heads_up_design_test_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Test Notification",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // 2. Edge-to-Edge Mode Card
    val edgeToEdgeCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("heads_up_edge_to_edge_card", CustomizeCategory.HEADS_UP_DESIGN) {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("heads_up_edge_to_edge_card")
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
                                .background(colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CropFree,
                                contentDescription = null,
                                tint = colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Edge-to-Edge Mode",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                )
                            )
                            Text(
                                text = if (edgeToEdge) {
                                    "Forced 0dp horizontal margins (full width). Top corners are square (0dp), Corner Radius controls bottom corners only."
                                } else {
                                    "Applies horizontal side margins (8dp). Corner Radius applies uniformly to all 4 corners."
                                },
                                fontSize = 12.sp,
                                color = colorScheme.onSurfaceVariant,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                )
                            )
                        }
                    }

                    Switch(
                        checked = edgeToEdge,
                        onCheckedChange = { PanelPreferences.setHeadsUpEdgeToEdge(context, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colorScheme.onPrimary,
                            checkedTrackColor = colorScheme.primary,
                            uncheckedThumbColor = colorScheme.outline,
                            uncheckedTrackColor = colorScheme.surface
                        ),
                        modifier = Modifier.testTag("heads_up_edge_to_edge_switch")
                    )
                }
            }
        }
    }

    // 3. Corner Radius Slider Card
    val cornerRadiusCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("heads_up_corner_radius_card", CustomizeCategory.HEADS_UP_DESIGN) {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("heads_up_corner_radius_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RoundedCorner,
                                    contentDescription = null,
                                    tint = colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Corner Radius",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface,
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                                        lineHeightStyle = LineHeightStyle(
                                            alignment = LineHeightStyle.Alignment.Center,
                                            trim = LineHeightStyle.Trim.Both
                                        )
                                    )
                                )
                                Text(
                                    text = if (edgeToEdge) "Bottom-left & bottom-right corners (top is 0dp)" else "Uniformly applied to all 4 corners",
                                    fontSize = 12.sp,
                                    color = colorScheme.onSurfaceVariant,
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                                        lineHeightStyle = LineHeightStyle(
                                            alignment = LineHeightStyle.Alignment.Center,
                                            trim = LineHeightStyle.Trim.Both
                                        )
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${cornerRadius} dp",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                        }
                    }

                    Slider(
                        value = cornerRadius.toFloat(),
                        onValueChange = { PanelPreferences.setHeadsUpCornerRadius(context, it.toInt()) },
                        valueRange = 0f..24f,
                        steps = 23,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("heads_up_corner_radius_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = colorScheme.primary,
                            activeTrackColor = colorScheme.primary,
                            inactiveTrackColor = colorScheme.onSurface.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    }

    // 4. Elevation / Shadow Slider Card
    val elevationCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("heads_up_elevation_card", CustomizeCategory.HEADS_UP_DESIGN) {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("heads_up_elevation_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Waves,
                                    contentDescription = null,
                                    tint = colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Elevation / Shadow",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface,
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                                        lineHeightStyle = LineHeightStyle(
                                            alignment = LineHeightStyle.Alignment.Center,
                                            trim = LineHeightStyle.Trim.Both
                                        )
                                    )
                                )
                                Text(
                                    text = "Ambient card drop shadow intensity & elevation depth",
                                    fontSize = 12.sp,
                                    color = colorScheme.onSurfaceVariant,
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                                        lineHeightStyle = LineHeightStyle(
                                            alignment = LineHeightStyle.Alignment.Center,
                                            trim = LineHeightStyle.Trim.Both
                                        )
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${elevation} dp",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                        }
                    }

                    Slider(
                        value = elevation.toFloat(),
                        onValueChange = { PanelPreferences.setHeadsUpElevation(context, it.toInt()) },
                        valueRange = 0f..16f,
                        steps = 15,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("heads_up_elevation_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = colorScheme.primary,
                            activeTrackColor = colorScheme.primary,
                            inactiveTrackColor = colorScheme.onSurface.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    }

    // 5. Top Margin / Offset Slider Card
    val topMarginCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("heads_up_top_margin_card", CustomizeCategory.HEADS_UP_DESIGN) {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("heads_up_top_margin_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerticalAlignTop,
                                    contentDescription = null,
                                    tint = colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Top Margin / Offset",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface,
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                                        lineHeightStyle = LineHeightStyle(
                                            alignment = LineHeightStyle.Alignment.Center,
                                            trim = LineHeightStyle.Trim.Both
                                        )
                                    )
                                )
                                Text(
                                    text = "Offset banner from status bar and camera cutout",
                                    fontSize = 12.sp,
                                    color = colorScheme.onSurfaceVariant,
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                                        lineHeightStyle = LineHeightStyle(
                                            alignment = LineHeightStyle.Alignment.Center,
                                            trim = LineHeightStyle.Trim.Both
                                        )
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${topMargin} dp",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                        }
                    }

                    Slider(
                        value = topMargin.toFloat(),
                        onValueChange = { PanelPreferences.setHeadsUpTopMargin(context, it.toInt()) },
                        valueRange = 0f..24f,
                        steps = 23,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("heads_up_top_margin_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = colorScheme.primary,
                            activeTrackColor = colorScheme.primary,
                            inactiveTrackColor = colorScheme.onSurface.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    }

    // Build Cards Layout
    val allCards = listOf(
        "heads_up_design_test_card" to testNotificationCard,
        "heads_up_edge_to_edge_card" to edgeToEdgeCard,
        "heads_up_corner_radius_card" to cornerRadiusCard,
        "heads_up_elevation_card" to elevationCard,
        "heads_up_top_margin_card" to topMarginCard
    )

    val pinnedCards = allCards.filter { pinnedIds.contains(it.first) }.map { it.second }
    val unpinnedCards = allCards.filter { !pinnedIds.contains(it.first) }.map { it.second }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CustomizeCardShapes.CardSpacing)
    ) {
        if (pinnedCards.isNotEmpty()) {
            ListSubheader(
                text = "Pinned",
                icon = Icons.Default.PushPin
            )
            pinnedCards.forEachIndexed { index, card ->
                card(CustomizeCardShapes.getShapeForIndex(index, pinnedCards.size))
            }
        }

        unpinnedCards.forEachIndexed { index, card ->
            card(CustomizeCardShapes.getShapeForIndex(index, unpinnedCards.size))
        }
    }
}
