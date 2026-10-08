package com.example.ui.customizer

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.NotificationBlacklistManager
import com.example.model.NotificationItem
import com.example.service.QuickPanelNotificationListenerService
import com.example.ui.theme.DarkUiSurface
import com.example.ui.theme.DarkUiSurfaceVariant
import com.example.ui.theme.ExpressiveCardShapes
import com.example.ui.theme.LocalIsDarkTheme

/**
 * In-Composition Modal Window / Bottom Sheet for viewing blocked notifications,
 * unhiding them, and viewing/disabling live non-dismissable ongoing notifications.
 * Safe for Activity UI and WindowManager Service Overlays.
 */
@Composable
fun NotificationManagerBottomSheet(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = LocalIsDarkTheme.current
    val isGranted = remember { QuickPanelNotificationListenerService.isPermissionGranted(context) }

    val hiddenPkgs by NotificationBlacklistManager.hiddenPackages.collectAsState()
    val hiddenChannels by NotificationBlacklistManager.hiddenChannels.collectAsState()

    var liveOngoingList by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Live Ongoing, 1: Blocked Rules
    var isVisible by remember { mutableStateOf(false) }

    fun refreshOngoingList() {
        liveOngoingList = QuickPanelNotificationListenerService.getAllActiveOngoingNotifications(context)
    }

    LaunchedEffect(hiddenPkgs, hiddenChannels) {
        refreshOngoingList()
        isVisible = true
    }

    val backDispatcher = androidx.activity.compose.LocalOnBackPressedDispatcherOwner.current
    if (backDispatcher != null) {
        BackHandler {
            isVisible = false
            onDismissRequest()
        }
    }

    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10000f),
            contentAlignment = Alignment.BottomCenter
        ) {
            // 1. Scrim dim backdrop
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest
                    )
            )

            // 2. Modal Card Container
            Surface(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = if (isDark) DarkUiSurface else MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { /* consume clicks */ }
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Drag pill indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(width = 36.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDark) Color.White.copy(alpha = 0.25f)
                                else Color.Black.copy(alpha = 0.2f)
                            )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "NOTIFICATION MANAGER",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Manage blocked rules & persistent cards",
                                    fontSize = 11.sp,
                                    color = if (isDark) Color.LightGray else Color(0xFF666666)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    refreshOngoingList()
                                },
                                modifier = Modifier.size(32.dp).testTag("notif_manager_refresh_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = onDismissRequest,
                                modifier = Modifier.size(32.dp).testTag("notif_manager_close_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = if (isDark) Color.White else Color(0xFF333333),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!isGranted) {
                        // Permission warning banner
                        Card(
                            shape = ExpressiveCardShapes.SingleCard,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Notification Access Required",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Text(
                                    text = "Grant notification listener access to analyze and block persistent notifications.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                                )
                                Button(
                                    onClick = {
                                        QuickPanelNotificationListenerService.openPermissionSettings(context)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("grant_notif_perm_btn")
                                ) {
                                    Text("Grant Permission in Settings", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Segmented Filter Tabs
                        val totalBlockedCount = hiddenPkgs.size + hiddenChannels.size
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedTab == 0,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedTab = 0
                                },
                                label = {
                                    Text(
                                        text = "Live Ongoing (${liveOngoingList.size})",
                                        fontSize = 11.sp,
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = if (isDark) DarkUiSurfaceVariant else MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = if (isDark) Color.White else Color(0xFF333333)
                                ),
                                modifier = Modifier.weight(1f).testTag("tab_live_ongoing")
                            )

                            FilterChip(
                                selected = selectedTab == 1,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedTab = 1
                                },
                                label = {
                                    Text(
                                        text = "Blocked Rules ($totalBlockedCount)",
                                        fontSize = 11.sp,
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = if (isDark) DarkUiSurfaceVariant else MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = if (isDark) Color.White else Color(0xFF333333)
                                ),
                                modifier = Modifier.weight(1f).testTag("tab_blocked_rules")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Content List Area (scrollable, height constrained)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 380.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (selectedTab == 0) {
                                // 1. LIVE ONGOING NOTIFICATIONS LIST
                                if (liveOngoingList.isEmpty()) {
                                    Card(
                                        shape = ExpressiveCardShapes.SingleCard,
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isDark) DarkUiSurfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.NotificationsActive,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "No active ongoing notifications",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDark) Color.White else Color(0xFF333333)
                                            )
                                            Text(
                                                text = "All active notifications on this device are standard/clearable.",
                                                fontSize = 11.sp,
                                                color = if (isDark) Color.Gray else Color(0xFF777777)
                                            )
                                        }
                                    }
                                } else {
                                    liveOngoingList.forEach { item ->
                                        val isPkgHidden = hiddenPkgs.contains(item.packageName)
                                        val isChannelHidden = item.channelId != null && hiddenChannels.contains(item.channelId)
                                        val isBlocked = isPkgHidden || isChannelHidden

                                        Card(
                                            shape = ExpressiveCardShapes.SingleCard,
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isBlocked) {
                                                    if (isDark) DarkUiSurfaceVariant.copy(alpha = 0.25f) else Color.LightGray.copy(alpha = 0.2f)
                                                } else {
                                                    if (isDark) DarkUiSurfaceVariant else MaterialTheme.colorScheme.surfaceVariant
                                                }
                                            ),
                                            modifier = Modifier.fillMaxWidth().testTag("ongoing_item_${item.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    modifier = Modifier.weight(1f),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    // App Icon Avatar
                                                    NotificationAvatarView(
                                                        appIcon = item.appIcon,
                                                        avatarBitmap = item.avatarBitmap,
                                                        isBlocked = isBlocked
                                                    )

                                                    Column {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Text(
                                                                text = item.appName,
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isBlocked) Color.Gray else if (isDark) Color.White else Color(0xFF222222),
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )

                                                            if (isBlocked) {
                                                                Surface(
                                                                    shape = RoundedCornerShape(4.dp),
                                                                    color = Color.Red.copy(alpha = 0.15f)
                                                                ) {
                                                                    Text(
                                                                        text = "Blocked",
                                                                        fontSize = 9.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = Color(0xFFFF5252),
                                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                                    )
                                                                }
                                                            } else {
                                                                Surface(
                                                                    shape = RoundedCornerShape(4.dp),
                                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                                ) {
                                                                    Text(
                                                                        text = "Ongoing",
                                                                        fontSize = 9.sp,
                                                                        fontWeight = FontWeight.Medium,
                                                                        color = MaterialTheme.colorScheme.primary,
                                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        val displayTitle = if (item.title.isNotBlank()) item.title else item.text
                                                        if (displayTitle.isNotBlank()) {
                                                            Text(
                                                                text = displayTitle,
                                                                fontSize = 11.sp,
                                                                color = if (isBlocked) Color.Gray else if (isDark) Color.LightGray else Color(0xFF444444),
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }

                                                        if (!item.channelId.isNullOrBlank()) {
                                                            Text(
                                                                text = "Channel: ${item.channelId}",
                                                                fontSize = 9.sp,
                                                                color = if (isDark) Color.Gray else Color(0xFF888888),
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(8.dp))

                                                // Disable / Block / Unhide Toggle Action Button
                                                Button(
                                                    onClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        if (isBlocked) {
                                                            QuickPanelNotificationListenerService.unhidePackage(item.packageName, context)
                                                            if (!item.channelId.isNullOrBlank()) {
                                                                QuickPanelNotificationListenerService.unhideChannel(item.channelId, context)
                                                            }
                                                        } else {
                                                            QuickPanelNotificationListenerService.hidePackage(item.packageName, context)
                                                            if (!item.channelId.isNullOrBlank()) {
                                                                QuickPanelNotificationListenerService.hideChannel(item.channelId, context)
                                                            }
                                                        }
                                                        refreshOngoingList()
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isBlocked) {
                                                            MaterialTheme.colorScheme.primary
                                                        } else {
                                                            if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)
                                                        },
                                                        contentColor = if (isBlocked) {
                                                            MaterialTheme.colorScheme.onPrimary
                                                        } else {
                                                            if (isDark) Color(0xFFFF6B6B) else Color(0xFFD32F2F)
                                                        }
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(30.dp).testTag("toggle_ongoing_${item.packageName}")
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isBlocked) Icons.Default.Visibility else Icons.Default.Block,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Text(
                                                            text = if (isBlocked) "Unhide" else "Disable",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // 2. BLOCKED RULES LIST
                                val blacklistRules = remember(hiddenPkgs, hiddenChannels) {
                                    val rules = mutableListOf<Pair<String, Boolean>>() // (id, isPackage)
                                    hiddenPkgs.forEach { rules.add(it to true) }
                                    hiddenChannels.forEach { rules.add(it to false) }
                                    rules
                                }

                                if (blacklistRules.isEmpty()) {
                                    Card(
                                        shape = ExpressiveCardShapes.SingleCard,
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isDark) DarkUiSurfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "No blocked notifications",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDark) Color.White else Color(0xFF333333)
                                            )
                                            Text(
                                                text = "Any notifications you block will appear here for unhiding.",
                                                fontSize = 11.sp,
                                                color = if (isDark) Color.Gray else Color(0xFF777777)
                                            )
                                        }
                                    }
                                } else {
                                    // Action to unhide all if multiple
                                    if (blacklistRules.size > 1) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Button(
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    hiddenPkgs.forEach { QuickPanelNotificationListenerService.unhidePackage(it, context) }
                                                    hiddenChannels.forEach { QuickPanelNotificationListenerService.unhideChannel(it, context) }
                                                    refreshOngoingList()
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                ),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp).testTag("unhide_all_btn"),
                                                border = null
                                            ) {
                                                Text("Unhide All (${blacklistRules.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    blacklistRules.forEach { (ruleId, isPackage) ->
                                        Card(
                                            shape = ExpressiveCardShapes.SingleCard,
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isDark) DarkUiSurfaceVariant else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                            modifier = Modifier.fillMaxWidth().testTag("rule_card_$ruleId")
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    modifier = Modifier.weight(1f),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(30.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFFFF5252).copy(alpha = 0.15f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Block,
                                                            contentDescription = null,
                                                            tint = Color(0xFFFF5252),
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                    }

                                                    Column {
                                                        Text(
                                                            text = if (isPackage) "Package: $ruleId" else "Channel: $ruleId",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isDark) Color.White else Color(0xFF222222),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = if (isPackage) "Blocked from alert stream" else "Blocked channel alert",
                                                            fontSize = 10.sp,
                                                            color = if (isDark) Color.Gray else Color(0xFF777777)
                                                        )
                                                    }
                                                }

                                                Button(
                                                    onClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        if (isPackage) {
                                                            QuickPanelNotificationListenerService.unhidePackage(ruleId, context)
                                                        } else {
                                                            QuickPanelNotificationListenerService.unhideChannel(ruleId, context)
                                                        }
                                                        refreshOngoingList()
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary,
                                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(28.dp).testTag("unhide_rule_$ruleId")
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Visibility,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Text("Unhide", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bottom Done / Close Action Button
                    Surface(
                        onClick = onDismissRequest,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().testTag("notif_manager_done_btn")
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Done",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationAvatarView(
    appIcon: Drawable?,
    avatarBitmap: Bitmap?,
    isBlocked: Boolean
) {
    if (avatarBitmap != null) {
        Image(
            bitmap = avatarBitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
        )
    } else if (appIcon != null) {
        val bitmap = remember(appIcon) {
            try {
                if (appIcon is android.graphics.drawable.BitmapDrawable && appIcon.bitmap != null) {
                    appIcon.bitmap
                } else {
                    val w = if (appIcon.intrinsicWidth > 0) appIcon.intrinsicWidth else 96
                    val h = if (appIcon.intrinsicHeight > 0) appIcon.intrinsicHeight else 96
                    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bmp)
                    appIcon.setBounds(0, 0, canvas.width, canvas.height)
                    appIcon.draw(canvas)
                    bmp
                }
            } catch (e: Exception) {
                null
            }
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        } else {
            DefaultBellAvatar(isBlocked)
        }
    } else {
        DefaultBellAvatar(isBlocked)
    }
}

@Composable
private fun DefaultBellAvatar(isBlocked: Boolean) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isBlocked) Color.Gray.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isBlocked) Icons.Default.NotificationsOff else Icons.Default.NotificationsActive,
            contentDescription = null,
            tint = if (isBlocked) Color.Gray else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
    }
}
