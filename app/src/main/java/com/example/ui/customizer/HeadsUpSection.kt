package com.example.ui.customizer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HeadsUpAction
import com.example.data.PanelPreferences
import com.example.service.HeadsUpOverlayManager
import com.example.service.HeadsUpSnoozeManager
import com.example.service.QuickPanelNotificationListenerService
import com.example.ui.theme.SegmentedButtonShapes
import kotlin.math.roundToInt

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

    // 4-Directional Swipe Actions
    val swipeUpAction by PanelPreferences.headsUpSwipeUpAction.collectAsState()
    val swipeDownAction by PanelPreferences.headsUpSwipeDownAction.collectAsState()
    val swipeLeftAction by PanelPreferences.headsUpSwipeLeftAction.collectAsState()
    val swipeRightAction by PanelPreferences.headsUpSwipeRightAction.collectAsState()

    // Tap & Press Actions
    val singleTapAction by PanelPreferences.headsUpSingleTapAction.collectAsState()
    val doubleTapAction by PanelPreferences.headsUpDoubleTapAction.collectAsState()
    val longPressAction by PanelPreferences.headsUpLongPressAction.collectAsState()

    // Contextual Snooze Duration
    val snoozeDurationMinutes by PanelPreferences.headsUpSnoozeDurationMinutes.collectAsState()

    // Whether any interaction has been configured to a snooze action
    val hasAnySnoozeAction = swipeUpAction.isSnoozeAction ||
            swipeDownAction.isSnoozeAction ||
            swipeLeftAction.isSnoozeAction ||
            swipeRightAction.isSnoozeAction ||
            singleTapAction.isSnoozeAction ||
            doubleTapAction.isSnoozeAction ||
            longPressAction.isSnoozeAction

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
                                    imageVector = Icons.Default.Timer,
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

    // 4-Directional Swipe Actions Card with Horizontal Scrollable Segmented FilterChips
    val swipeActionsCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("heads_up_swipe_actions_card", CustomizeCategory.HEADS_UP) {
            SettingsCard(shape = shape) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                imageVector = Icons.Default.Swipe,
                                contentDescription = null,
                                tint = colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = "4-Directional Swipe Gestures",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "Choose what happens when swiping the floating notification banner in each direction.",
                                fontSize = 11.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        HeadsUpActionChipChooserRow(
                            icon = Icons.Default.KeyboardArrowUp,
                            label = "Swipe Up",
                            selectedAction = swipeUpAction,
                            onActionSelected = { PanelPreferences.setHeadsUpSwipeUpAction(context, it) },
                            testTag = "heads_up_swipe_up_chips"
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        HeadsUpActionChipChooserRow(
                            icon = Icons.Default.KeyboardArrowDown,
                            label = "Swipe Down",
                            selectedAction = swipeDownAction,
                            onActionSelected = { PanelPreferences.setHeadsUpSwipeDownAction(context, it) },
                            testTag = "heads_up_swipe_down_chips"
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        HeadsUpActionChipChooserRow(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            label = "Swipe Left",
                            selectedAction = swipeLeftAction,
                            onActionSelected = { PanelPreferences.setHeadsUpSwipeLeftAction(context, it) },
                            testTag = "heads_up_swipe_left_chips"
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        HeadsUpActionChipChooserRow(
                            icon = Icons.AutoMirrored.Filled.ArrowForward,
                            label = "Swipe Right",
                            selectedAction = swipeRightAction,
                            onActionSelected = { PanelPreferences.setHeadsUpSwipeRightAction(context, it) },
                            testTag = "heads_up_swipe_right_chips"
                        )
                    }
                }
            }
        }
    }

    // Tap & Press Actions Card with Horizontal Scrollable Segmented FilterChips
    val tapActionsCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("heads_up_tap_actions_card", CustomizeCategory.HEADS_UP) {
            SettingsCard(shape = shape) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = "Tap & Press Actions",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "Control banner response for single tap, double tap, and long press (with haptics).",
                                fontSize = 11.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        HeadsUpActionChipChooserRow(
                            icon = Icons.Default.TouchApp,
                            label = "Single Tap",
                            selectedAction = singleTapAction,
                            onActionSelected = { PanelPreferences.setHeadsUpSingleTapAction(context, it) },
                            testTag = "heads_up_single_tap_chips"
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        HeadsUpActionChipChooserRow(
                            icon = Icons.Default.TouchApp,
                            label = "Double Tap",
                            selectedAction = doubleTapAction,
                            onActionSelected = { PanelPreferences.setHeadsUpDoubleTapAction(context, it) },
                            testTag = "heads_up_double_tap_chips"
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        HeadsUpActionChipChooserRow(
                            icon = Icons.Default.Timer,
                            label = "Long Press",
                            selectedAction = longPressAction,
                            onActionSelected = { PanelPreferences.setHeadsUpLongPressAction(context, it) },
                            testTag = "heads_up_long_press_chips"
                        )
                    }
                }
            }
        }
    }

    // Contextual Temporary Snooze Duration Card (Dynamically displayed when any interaction is a snooze action)
    val snoozeDurationCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("heads_up_snooze_duration_card", CustomizeCategory.HEADS_UP) {
            SettingsCard(shape = shape) {
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.tertiaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Snooze,
                                    contentDescription = null,
                                    tint = colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(
                                    text = "Temporary Snooze Duration",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = "Mutes heads-up popups for this duration upon snooze gesture. Calls & alarms strictly bypass.",
                                    fontSize = 11.sp,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = "$snoozeDurationMinutes min",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.tertiary,
                            modifier = Modifier.testTag("snooze_duration_value_label")
                        )
                    }

                    Slider(
                        value = snoozeDurationMinutes.toFloat(),
                        onValueChange = { PanelPreferences.setHeadsUpSnoozeDurationMinutes(context, it.roundToInt()) },
                        valueRange = 1f..120f,
                        steps = 119,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("heads_up_snooze_duration_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = colorScheme.tertiary,
                            activeTrackColor = colorScheme.tertiary,
                            inactiveTrackColor = colorScheme.tertiary.copy(alpha = 0.2f)
                        )
                    )

                    // Active Snooze indicator if currently snoozing
                    val isGlobalSnoozed = HeadsUpSnoozeManager.isGlobalSnoozed()
                    val snoozedAppsCount = HeadsUpSnoozeManager.getSnoozedApps().size

                    if (isGlobalSnoozed || snoozedAppsCount > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isGlobalSnoozed) "Global snooze active" else "$snoozedAppsCount app(s) snoozed",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = {
                                    HeadsUpSnoozeManager.clearSnooze()
                                }
                            ) {
                                Text("Clear Snooze", fontSize = 11.sp, color = colorScheme.error)
                            }
                        }
                    }
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
            if (pinnedIds.contains("heads_up_swipe_actions_card")) pinnedCards.add(swipeActionsCard)
            if (pinnedIds.contains("heads_up_tap_actions_card")) pinnedCards.add(tapActionsCard)
            if (hasAnySnoozeAction && pinnedIds.contains("heads_up_snooze_duration_card")) pinnedCards.add(snoozeDurationCard)
        }

        if (pinnedCards.isNotEmpty()) {
            ListSubheader(
                text = "Pinned",
                icon = Icons.Default.PushPin
            )
            pinnedCards.forEachIndexed { index, card ->
                card(CustomizeCardShapes.getShapeForIndex(index, pinnedCards.size))
            }
        }

        // 2. Unpinned Group 1: Overlay Engine & Service
        val engineGroup = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
        if (!pinnedIds.contains("heads_up_overlay_engine_card")) engineGroup.add(masterToggleCard)
        if (!enableHeadsUpOverlay && !pinnedIds.contains("system_heads_up_card")) engineGroup.add(systemHeadsUpCard)
        if (enableHeadsUpOverlay && !pinnedIds.contains("heads_up_permission_card")) engineGroup.add(permissionStatusCard)

        if (engineGroup.isNotEmpty()) {
            ListSubheader(
                text = "Overlay Engine & Service",
                icon = Icons.Default.Security
            )
            engineGroup.forEachIndexed { index, card ->
                card(CustomizeCardShapes.getShapeForIndex(index, engineGroup.size))
            }
        }

        if (enableHeadsUpOverlay) {
            // 3. Unpinned Group 2: Display & Timeout
            val displayGroup = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
            if (!pinnedIds.contains("heads_up_test_card")) displayGroup.add(testPreviewCard)
            if (!pinnedIds.contains("heads_up_timeout_card")) displayGroup.add(timeoutSliderCard)

            if (displayGroup.isNotEmpty()) {
                ListSubheader(
                    text = "Display & Timeout",
                    icon = Icons.Default.Timer
                )
                displayGroup.forEachIndexed { index, card ->
                    card(CustomizeCardShapes.getShapeForIndex(index, displayGroup.size))
                }
            }

            // 4. Unpinned Group 3: Gestures & Interactions
            val gesturesGroup = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
            if (!pinnedIds.contains("heads_up_swipe_actions_card")) gesturesGroup.add(swipeActionsCard)
            if (!pinnedIds.contains("heads_up_tap_actions_card")) gesturesGroup.add(tapActionsCard)

            if (gesturesGroup.isNotEmpty()) {
                ListSubheader(
                    text = "Gestures & Interactions",
                    icon = Icons.Default.TouchApp
                )
                gesturesGroup.forEachIndexed { index, card ->
                    card(CustomizeCardShapes.getShapeForIndex(index, gesturesGroup.size))
                }
            }

            // 5. Unpinned Group 4: Snooze Control
            if (hasAnySnoozeAction && !pinnedIds.contains("heads_up_snooze_duration_card")) {
                ListSubheader(
                    text = "Snooze Control",
                    icon = Icons.Default.Snooze
                )
                snoozeDurationCard(CustomizeCardShapes.SingleCard)
            }
        }
    }
}

@Composable
private fun HeadsUpActionChipChooserRow(
    icon: ImageVector,
    label: String,
    selectedAction: HeadsUpAction,
    onActionSelected: (HeadsUpAction) -> Unit,
    testTag: String
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = com.example.ui.theme.LocalIsDarkTheme.current
    val screenBg = com.example.ui.theme.getSettingsScreenBackgroundColor(isDark)
    val allActions = HeadsUpAction.entries

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (selectedAction.isSnoozeAction) colorScheme.tertiaryContainer.copy(alpha = 0.85f)
                else colorScheme.primaryContainer.copy(alpha = 0.6f)
            ) {
                Text(
                    text = selectedAction.title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedAction.isSnoozeAction) colorScheme.onTertiaryContainer else colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Horizontal scrollable FilterChip chooser matching standard customize design system
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .testTag(testTag),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            allActions.forEach { action ->
                val isSelected = (action == selectedAction)
                val isSnooze = action.isSnoozeAction
                val chipShape = SegmentedButtonShapes.getShape(
                    item = action,
                    items = allActions,
                    selectedItem = selectedAction
                )

                FilterChip(
                    selected = isSelected,
                    onClick = { onActionSelected(action) },
                    shape = chipShape,
                    border = null,
                    label = {
                        Text(
                            text = action.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            )
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = screenBg,
                        selectedContainerColor = if (isSnooze) colorScheme.tertiaryContainer else colorScheme.primaryContainer,
                        selectedLabelColor = if (isSnooze) colorScheme.onTertiaryContainer else colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor = if (isSnooze) colorScheme.onTertiaryContainer else colorScheme.onPrimaryContainer,
                        labelColor = colorScheme.onSurface
                    ),
                    modifier = Modifier.testTag("${testTag}_chip_${action.id}")
                )
            }
        }
    }
}
