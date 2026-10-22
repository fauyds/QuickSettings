package com.example.ui.customizer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PanelPreferences
import com.example.service.HeadsUpOverlayManager
import com.example.service.QuickPanelNotificationListenerService

@Composable
fun HeadsUpSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val enableHeadsUpOverlay by PanelPreferences.enableHeadsUpOverlay.collectAsState()
    val headsUpTimeoutSeconds by PanelPreferences.headsUpTimeoutSeconds.collectAsState()
    val isNotifPermissionGranted = remember {
        QuickPanelNotificationListenerService.isPermissionGranted(context)
    }

    val isDark = com.example.ui.theme.LocalIsDarkTheme.current
    val screenBg = com.example.ui.theme.getSettingsScreenBackgroundColor(isDark)
    val nonGrantedRed = com.example.ui.theme.getNonGrantedRedColor(isDark)
    val colorScheme = MaterialTheme.colorScheme

    val enableSystemHeadsUp by PanelPreferences.enableSystemHeadsUp.collectAsState()
    val nativeTileManager = com.example.system.NativeTileManager.instance
    val systemHeadsUpState by (nativeTileManager?.isHeadsUpEnabled ?: androidx.compose.runtime.remember {
        kotlinx.coroutines.flow.MutableStateFlow(enableSystemHeadsUp)
    }).collectAsState()

    val pinnedIds by PinnedCardsManager.getPinnedCardIds(CustomizeCategory.HEADS_UP, context).collectAsState()

    val masterToggleCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("heads_up_overlay_engine_card", CustomizeCategory.HEADS_UP) {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("heads_up_overlay_engine_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Custom Heads-Up Overlay",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                ),
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "Lightweight top overlay for high-priority notifications with direct QS shade swipe trigger.",
                                fontSize = 11.sp,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                ),
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = enableHeadsUpOverlay,
                        onCheckedChange = { PanelPreferences.setEnableHeadsUpOverlay(context, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colorScheme.onPrimary,
                            checkedTrackColor = colorScheme.primary,
                            uncheckedThumbColor = colorScheme.outline,
                            uncheckedTrackColor = screenBg
                        ),
                        modifier = Modifier.testTag("enable_heads_up_toggle")
                    )
                }
            }
        }
    }

    val systemHeadsUpCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("system_heads_up_card", CustomizeCategory.HEADS_UP) {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("system_heads_up_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                                .background(colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = "Enable System Heads-Up",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 17.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "Enable default Android OS floating pop-up banners while custom overlay is turned off.",
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = systemHeadsUpState,
                        onCheckedChange = { checked ->
                            PanelPreferences.setEnableSystemHeadsUp(context, checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colorScheme.onSecondary,
                            checkedTrackColor = colorScheme.secondary,
                            uncheckedThumbColor = colorScheme.outline,
                            uncheckedTrackColor = screenBg
                        ),
                        modifier = Modifier.testTag("enable_system_heads_up_toggle")
                    )
                }
            }
        }
    }

    val permissionStatusCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("heads_up_permission_card", CustomizeCategory.HEADS_UP) {
            SettingsCard(
                shape = shape
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (!isNotifPermissionGranted) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsOff,
                                    contentDescription = null,
                                    tint = nonGrantedRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Notification Listener Access Required",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = nonGrantedRed,
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                                        lineHeightStyle = LineHeightStyle(
                                            alignment = LineHeightStyle.Alignment.Center,
                                            trim = LineHeightStyle.Trim.Both
                                        )
                                    )
                                )
                            }
                            Text(
                                text = "To detect incoming messages, calls, and high-priority alarms, grant Notification Listener permission to QuickPanel.",
                                fontSize = 11.sp,
                                color = colorScheme.onSurfaceVariant,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                )
                            )
                            Button(
                                onClick = {
                                    QuickPanelNotificationListenerService.openPermissionSettings(context)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = nonGrantedRed,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("grant_notification_access_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Grant Notification Access",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
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
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Notification Access is Granted & Active",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.primary,
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
                }
            }
        }
    }

    val testPreviewCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("heads_up_test_card", CustomizeCategory.HEADS_UP) {
            SettingsCard(
                shape = shape
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Independent Preview & Test",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            ),
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = "Inflates a lightweight Compose layout at the top of the screen via WindowManager with try-catch safety.",
                            fontSize = 11.sp,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            ),
                            color = colorScheme.onSurfaceVariant
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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
                                    .weight(1f)
                                    .testTag("test_heads_up_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    HeadsUpOverlayManager.showTestAlarmHeadsUp(context)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorScheme.secondary,
                                    contentColor = colorScheme.onSecondary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_alarm_heads_up_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Alarm", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                HeadsUpOverlayManager.showTestCallHeadsUp(context)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.primaryContainer,
                                contentColor = colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_call_heads_up_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Incoming Call", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    val timeoutSliderCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("heads_up_timeout_card", CustomizeCategory.HEADS_UP) {
            SettingsCard(
                shape = shape
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Auto-Dismiss Timeout",
                                fontSize = 13.sp,
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
                        }
                        Text(
                            text = "$headsUpTimeoutSeconds seconds",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            )
                        )
                    }

                    Slider(
                        value = headsUpTimeoutSeconds.toFloat(),
                        onValueChange = { PanelPreferences.setHeadsUpTimeoutSeconds(context, it.toInt()) },
                        valueRange = 2f..10f,
                        steps = 7,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("heads_up_timeout_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = colorScheme.primary,
                            activeTrackColor = colorScheme.primary,
                            inactiveTrackColor = colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    }

    val gestureHintsCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("heads_up_gestures_card", CustomizeCategory.HEADS_UP) {
            SettingsCard(
                shape = shape
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Gesture Actions:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 15.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "• Swipe Up / Left / Right: Dismiss heads-up banner",
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Swipe Down: Directly trigger Custom Quick Settings Shade",
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                        color = colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "• Tap: Open notification destination app",
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CustomizeCardShapes.CardSpacing)
    ) {
        // 1. Pinned Cards at the very top of Heads-Up
        val pinnedCards = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
        if (pinnedIds.contains("heads_up_overlay_engine_card")) pinnedCards.add(masterToggleCard)
        if (!enableHeadsUpOverlay && pinnedIds.contains("system_heads_up_card")) pinnedCards.add(systemHeadsUpCard)
        if (enableHeadsUpOverlay) {
            if (pinnedIds.contains("heads_up_permission_card")) pinnedCards.add(permissionStatusCard)
            if (pinnedIds.contains("heads_up_test_card")) pinnedCards.add(testPreviewCard)
            if (pinnedIds.contains("heads_up_timeout_card")) pinnedCards.add(timeoutSliderCard)
            if (pinnedIds.contains("heads_up_gestures_card")) pinnedCards.add(gestureHintsCard)
        }

        if (pinnedCards.isNotEmpty()) {
            ListSubheader(
                text = "Pinned",
                icon = androidx.compose.material.icons.Icons.Default.PushPin
            )
            pinnedCards.forEachIndexed { index, card ->
                card(CustomizeCardShapes.getShapeForIndex(index, pinnedCards.size))
            }
        }

        // Unpinned Cards
        if (!enableHeadsUpOverlay) {
            val unpinnedDisabled = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
            if (!pinnedIds.contains("heads_up_overlay_engine_card")) unpinnedDisabled.add(masterToggleCard)
            if (!pinnedIds.contains("system_heads_up_card")) unpinnedDisabled.add(systemHeadsUpCard)

            unpinnedDisabled.forEachIndexed { index, card ->
                card(CustomizeCardShapes.getShapeForIndex(index, unpinnedDisabled.size))
            }
        } else {
            val unpinnedActive = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
            if (!pinnedIds.contains("heads_up_overlay_engine_card")) unpinnedActive.add(masterToggleCard)
            if (!pinnedIds.contains("heads_up_permission_card")) unpinnedActive.add(permissionStatusCard)
            if (!pinnedIds.contains("heads_up_test_card")) unpinnedActive.add(testPreviewCard)
            if (!pinnedIds.contains("heads_up_timeout_card")) unpinnedActive.add(timeoutSliderCard)
            if (!pinnedIds.contains("heads_up_gestures_card")) unpinnedActive.add(gestureHintsCard)

            unpinnedActive.forEachIndexed { index, card ->
                card(CustomizeCardShapes.getShapeForIndex(index, unpinnedActive.size))
            }
        }
    }
}
