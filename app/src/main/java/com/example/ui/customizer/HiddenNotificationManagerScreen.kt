package com.example.ui.customizer

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NotificationBlacklistManager
import com.example.model.NotificationItem
import com.example.service.QuickPanelNotificationListenerService

/**
 * Card shapes for Notif Hub screen.
 * 4dp padding between cards, and inside corners strictly 5dp.
 */
private object NotifHubCardShapes {
    val OuterCorner = 16.dp
    val InsideCorner = 5.dp
    val CardSpacing = 4.dp

    val TopCard = RoundedCornerShape(
        topStart = OuterCorner,
        topEnd = OuterCorner,
        bottomStart = InsideCorner,
        bottomEnd = InsideCorner
    )
    val CenterCard = RoundedCornerShape(InsideCorner)
    val BottomCard = RoundedCornerShape(
        topStart = InsideCorner,
        topEnd = InsideCorner,
        bottomStart = OuterCorner,
        bottomEnd = OuterCorner
    )
    val SingleCard = RoundedCornerShape(OuterCorner)
}

@Composable
fun HiddenNotificationManagerScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isGranted = remember { QuickPanelNotificationListenerService.isPermissionGranted(context) }
    val colorScheme = MaterialTheme.colorScheme
    val isDark = com.example.ui.theme.LocalIsDarkTheme.current
    val screenBg = com.example.ui.theme.getSettingsScreenBackgroundColor(isDark)
    val cardBg = com.example.ui.theme.getSettingsCardBackgroundColor(isDark)
    val nonGrantedRed = com.example.ui.theme.getNonGrantedRedColor(isDark)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("notif_hub_screen"),
        verticalArrangement = Arrangement.spacedBy(NotifHubCardShapes.CardSpacing)
    ) {
        // Section Header with Monet Pill Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "NOTIFICATION HUB & BLACKLIST",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                color = colorScheme.primary
            )

            Surface(
                color = if (isGranted) colorScheme.primaryContainer else nonGrantedRed,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isGranted) "Active" else "Permission Needed",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isGranted) colorScheme.onPrimaryContainer else Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        if (!isGranted) {
            Card(
                shape = NotifHubCardShapes.SingleCard,
                colors = CardDefaults.cardColors(containerColor = nonGrantedRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Notification Access Required",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "To block active ongoing notifications, hide system notifications, or restore previously blacklisted notification rules, please grant Notification Listener access in the system settings.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Button(
                        onClick = { QuickPanelNotificationListenerService.openPermissionSettings(context) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = nonGrantedRed
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Grant Notification Listener Permission", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            val hiddenPkgs by NotificationBlacklistManager.hiddenPackages.collectAsState()
            val hiddenChannels by NotificationBlacklistManager.hiddenChannels.collectAsState()

            var liveOngoingList by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }

            LaunchedEffect(hiddenPkgs, hiddenChannels) {
                liveOngoingList = QuickPanelNotificationListenerService.getAllActiveOngoingNotifications(context)
            }

            // Status Info Card
            SettingsCard(
                shape = NotifHubCardShapes.SingleCard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = "Notification Interceptor Active",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 17.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = "Ongoing & persistent notifications on this device are analyzed in real-time. Block any noisy persistent cards below.",
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Live Ongoing List Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "LIVE ONGOING NOTIFICATIONS (${liveOngoingList.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = colorScheme.primary
                )
            }

            if (liveOngoingList.isEmpty()) {
                SettingsCard(
                    shape = NotifHubCardShapes.SingleCard,
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No active ongoing notifications found on the device. (Note: Only background services and persistent/clear-proof notification cards can be blacklisted).",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                liveOngoingList.forEachIndexed { index, item ->
                    val isHidden = hiddenPkgs.contains(item.packageName) ||
                            (item.channelId != null && hiddenChannels.contains(item.channelId))

                    // Grouped card shape with 5dp inside corners
                    val cardShape = when {
                        liveOngoingList.size == 1 -> NotifHubCardShapes.SingleCard
                        index == 0 -> NotifHubCardShapes.TopCard
                        index == liveOngoingList.size - 1 -> NotifHubCardShapes.BottomCard
                        else -> NotifHubCardShapes.CenterCard
                    }

                    SettingsCard(
                        shape = cardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isHidden) screenBg else cardBg
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notif_item_${item.packageName}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isHidden) colorScheme.primary.copy(alpha = 0.35f)
                                            else colorScheme.primary
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f, fill = false),
                                    verticalArrangement = Arrangement.spacedBy(1.dp)
                                ) {
                                    Text(
                                        text = item.appName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 16.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                        color = if (isHidden) colorScheme.onSurface.copy(alpha = 0.5f) else colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (item.title.isNotBlank()) item.title else item.text,
                                        fontSize = 12.sp,
                                        lineHeight = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                        color = colorScheme.onSurfaceVariant.copy(alpha = if (isHidden) 0.5f else 0.8f)
                                    )
                                    if (!item.channelId.isNullOrBlank()) {
                                        Surface(
                                            color = colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "Channel: ${item.channelId}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                                color = colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Hide / Unhide Toggle
                            Button(
                                onClick = {
                                    if (isHidden) {
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
                                    // Refresh local list
                                    liveOngoingList = QuickPanelNotificationListenerService.getAllActiveOngoingNotifications(context)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isHidden) colorScheme.primary else colorScheme.secondaryContainer,
                                    contentColor = if (isHidden) colorScheme.onPrimary else colorScheme.onSecondaryContainer
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = if (isHidden) "Unhide" else "Hide",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Blacklist Rules Section Header
            val blacklistRules = remember(hiddenPkgs, hiddenChannels) {
                val rules = mutableListOf<Pair<String, Boolean>>() // Pair(Id, IsPackage)
                hiddenPkgs.forEach { rules.add(it to true) }
                hiddenChannels.forEach { rules.add(it to false) }
                rules
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "BLACKLISTED RULES (${blacklistRules.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = colorScheme.primary
                )
            }

            if (blacklistRules.isEmpty()) {
                SettingsCard(
                    shape = NotifHubCardShapes.SingleCard,
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No notifications are currently blacklisted.",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                blacklistRules.forEachIndexed { index, (ruleId, isPackage) ->
                    // Grouped card shape with 5dp inside corners
                    val ruleCardShape = when {
                        blacklistRules.size == 1 -> NotifHubCardShapes.SingleCard
                        index == 0 -> NotifHubCardShapes.TopCard
                        index == blacklistRules.size - 1 -> NotifHubCardShapes.BottomCard
                        else -> NotifHubCardShapes.CenterCard
                    }

                    SettingsCard(
                        shape = ruleCardShape,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        color = if (isPackage) colorScheme.primaryContainer else colorScheme.tertiaryContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (isPackage) "Package Rule" else "Channel Rule",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            color = if (isPackage) colorScheme.onPrimaryContainer else colorScheme.onTertiaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = ruleId,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Delete Rule / Restore Button
                            IconButton(
                                onClick = {
                                    if (isPackage) {
                                        QuickPanelNotificationListenerService.unhidePackage(ruleId, context)
                                    } else {
                                        QuickPanelNotificationListenerService.unhideChannel(ruleId, context)
                                    }
                                    // Refresh local list
                                    liveOngoingList = QuickPanelNotificationListenerService.getAllActiveOngoingNotifications(context)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove rule",
                                    tint = colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
