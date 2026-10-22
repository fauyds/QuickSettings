package com.example.ui.status

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.PanelPreferences
import com.example.model.PrivilegeMode
import com.example.ui.theme.ExpressiveCardShapes

object AppStatusCardShapes {
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

/**
 * App Status screen displaying service health, overlay triggers, diagnostics,
 * and the top gesture trigger strip bar.
 */
@Composable
fun AppStatusScreen(
    isOverlayEnabled: Boolean,
    isOverlayRunning: Boolean,
    hasOverlayPermission: Boolean,
    isAccessibilityRunning: Boolean,
    currentMode: PrivilegeMode,
    isRootAvailable: Boolean,
    isShizukuAvailable: Boolean,
    tilesCount: Int,
    notificationsCount: Int,
    onToggleOverlay: (Boolean) -> Unit,
    onTestFloatingOverlay: () -> Unit,
    onRequestPermission: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onSimulateSwipeDown: () -> Unit,
    onLaunchInitialSetup: (() -> Unit)? = null,
    isInitialSetupDone: Boolean? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = com.example.ui.theme.LocalIsDarkTheme.current
    val screenBg = com.example.ui.theme.getSettingsScreenBackgroundColor(isDark)
    val cardBg = com.example.ui.theme.getSettingsCardBackgroundColor(isDark)
    val nonGrantedRed = com.example.ui.theme.getNonGrantedRedColor(isDark)

    val storedInitialSetupDone by PanelPreferences.initialSetupCompleted.collectAsStateWithLifecycle(initialValue = PanelPreferences.initialSetupCompleted.value)
    val effectiveSetupDone = isInitialSetupDone ?: storedInitialSetupDone

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("app_status_screen"),
        verticalArrangement = Arrangement.spacedBy(AppStatusCardShapes.CardSpacing)
    ) {
        // 1. Header Card
        Card(
            shape = AppStatusCardShapes.TopCard,
            colors = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("app_status_header_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = "App Status",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = "Overlay Service & System Health",
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (onLaunchInitialSetup != null && !effectiveSetupDone) {
            Card(
                shape = AppStatusCardShapes.CenterCard,
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_status_initial_setup_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = "Initial Setup Wizard",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp,
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
                                text = "Setup privileges, configure tiles & triggers",
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
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
                    FilledTonalButton(
                        onClick = onLaunchInitialSetup,
                        shape = RoundedCornerShape(8.dp),
                        colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(containerColor = screenBg),
                        modifier = Modifier.testTag("btn_launch_initial_setup")
                    ) {
                        Text("Start", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // 2. The Recently Edited Element: Top Trigger Strip Bar
        TopTriggerStripBar(
            isOverlayEnabled = isOverlayEnabled,
            isOverlayRunning = isOverlayRunning,
            hasOverlayPermission = hasOverlayPermission,
            isAccessibilityRunning = isAccessibilityRunning,
            onToggleOverlay = onToggleOverlay,
            onTestFloatingOverlay = onTestFloatingOverlay,
            onRequestPermission = onRequestPermission,
            onOpenAccessibilitySettings = onOpenAccessibilitySettings,
            onSimulateSwipeDown = onSimulateSwipeDown
        )

        // 3. System Diagnostics & Services Stack (Consolidated Single Card)
        val allPermissionsAllowed = hasOverlayPermission && isAccessibilityRunning && isOverlayRunning

        Text(
            text = "SERVICE HEALTH DIAGNOSTICS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp)
        )

        Card(
            shape = AppStatusCardShapes.SingleCard,
            colors = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("app_status_service_health_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                if (allPermissionsAllowed) {
                    DiagnosticItemRow(
                        icon = Icons.Default.CheckCircle,
                        title = "All privileges are allowed",
                        subtitle = "Overlay, accessibility & system permissions granted",
                        isGood = true
                    )

                    HorizontalDivider(
                        color = colorScheme.outlineVariant.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                } else {
                    // Item 1: Overlay Window Service
                    DiagnosticItemRow(
                        icon = Icons.Default.Dashboard,
                        title = "Overlay Window Service",
                        subtitle = if (isOverlayRunning) "Running & Ready for Gestures" else "Service Stopped / Inactive",
                        isGood = isOverlayRunning,
                        badgeText = if (isOverlayRunning) "ACTIVE" else "OFF",
                        badgeContainerColor = if (isOverlayRunning) colorScheme.primaryContainer else nonGrantedRed,
                        badgeContentColor = if (isOverlayRunning) colorScheme.onPrimaryContainer else Color.White
                    )

                    HorizontalDivider(
                        color = colorScheme.outlineVariant.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )

                    // Item 2: Accessibility Service
                    DiagnosticItemRow(
                        icon = Icons.Default.TouchApp,
                        title = "Accessibility Service",
                        subtitle = if (isAccessibilityRunning) "Top Edge Interceptor Active" else "Not running (tap to configure)",
                        isGood = isAccessibilityRunning,
                        badgeText = if (isAccessibilityRunning) "READY" else "Configure",
                        badgeContainerColor = if (isAccessibilityRunning) colorScheme.tertiaryContainer else screenBg,
                        badgeContentColor = if (isAccessibilityRunning) colorScheme.onTertiaryContainer else Color.White,
                        onClick = if (!isAccessibilityRunning) onOpenAccessibilitySettings else null
                    )

                    HorizontalDivider(
                        color = colorScheme.outlineVariant.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )

                    // Item 3: System Overlay Permission
                    DiagnosticItemRow(
                        icon = if (hasOverlayPermission) Icons.Default.Check else Icons.Default.Warning,
                        title = "System Overlay Permission",
                        subtitle = if (hasOverlayPermission) "Granted (SYSTEM_ALERT_WINDOW)" else "Permission Required",
                        isGood = hasOverlayPermission,
                        badgeText = if (hasOverlayPermission) "GRANTED" else "Grant",
                        badgeContainerColor = if (hasOverlayPermission) colorScheme.primaryContainer else screenBg,
                        badgeContentColor = if (hasOverlayPermission) colorScheme.onPrimaryContainer else Color.White,
                        onClick = if (!hasOverlayPermission) onRequestPermission else null
                    )

                    HorizontalDivider(
                        color = colorScheme.outlineVariant.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }

                // Item 4: Privilege Engine
                DiagnosticItemRow(
                    icon = Icons.Default.Security,
                    title = "Privilege Engine",
                    subtitle = "${currentMode.title} • Root: ${if (isRootAvailable) "Yes" else "No"} • Shizuku: ${if (isShizukuAvailable) "Yes" else "No"}",
                    isGood = true,
                    badgeText = currentMode.name,
                    badgeContainerColor = colorScheme.secondaryContainer,
                    badgeContentColor = colorScheme.onSecondaryContainer
                )
            }
        }

        // 4. Quick Stats & System Metric Cards (5dp inner corners)
        Text(
            text = "SYSTEM OVERVIEW",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppStatusCardShapes.CardSpacing)
        ) {
            Card(
                shape = RoundedCornerShape(
                    topStart = AppStatusCardShapes.OuterCorner,
                    bottomStart = AppStatusCardShapes.OuterCorner,
                    topEnd = AppStatusCardShapes.InsideCorner,
                    bottomEnd = AppStatusCardShapes.InsideCorner
                ),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Configured Tiles",
                        fontSize = 11.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$tilesCount Active",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(
                    topStart = AppStatusCardShapes.InsideCorner,
                    bottomStart = AppStatusCardShapes.InsideCorner,
                    topEnd = AppStatusCardShapes.OuterCorner,
                    bottomEnd = AppStatusCardShapes.OuterCorner
                ),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Notifications",
                        fontSize = 11.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$notificationsCount Intercepted",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.tertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Top Trigger Strip Bar with Clear Master ON / OFF Switch & Floating Overlay Trigger
 */
@Composable
fun TopTriggerStripBar(
    isOverlayEnabled: Boolean,
    isOverlayRunning: Boolean,
    hasOverlayPermission: Boolean,
    isAccessibilityRunning: Boolean,
    onToggleOverlay: (Boolean) -> Unit,
    onTestFloatingOverlay: () -> Unit,
    onRequestPermission: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onSimulateSwipeDown: () -> Unit = {},
    shape: androidx.compose.ui.graphics.Shape = AppStatusCardShapes.BottomCard
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = com.example.ui.theme.LocalIsDarkTheme.current
    val cardBg = com.example.ui.theme.getSettingsCardBackgroundColor(isDark)
    val screenBg = com.example.ui.theme.getSettingsScreenBackgroundColor(isDark)

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("top_trigger_strip_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Master Overlay Switch Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isOverlayEnabled && isOverlayRunning) colorScheme.primary
                                else if (isOverlayEnabled) colorScheme.primaryContainer
                                else colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = if (isOverlayEnabled && isOverlayRunning) colorScheme.onPrimary
                            else if (isOverlayEnabled) colorScheme.onPrimaryContainer
                            else colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = if (isOverlayEnabled) "Quick Settings Overlay: ON" else "Quick Settings Overlay: OFF",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp,
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
                            text = if (isAccessibilityRunning) "Accessibility Interceptor Ready" else if (hasOverlayPermission) "Overlay Service Ready" else "Requires Overlay / Accessibility Perm",
                            fontSize = 10.sp,
                            lineHeight = 13.sp,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            ),
                            color = if (isOverlayEnabled) colorScheme.primary else colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Switch(
                        checked = isOverlayEnabled,
                        onCheckedChange = onToggleOverlay,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colorScheme.onPrimary,
                            checkedTrackColor = colorScheme.primary,
                            uncheckedThumbColor = colorScheme.outline,
                            uncheckedTrackColor = colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("master_overlay_switch")
                    )
                }
            }

            // Action row: Permissions or Open Floating QS button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!hasOverlayPermission && !isAccessibilityRunning) {
                    Surface(
                        onClick = onRequestPermission,
                        shape = RoundedCornerShape(8.dp),
                        color = screenBg,
                        modifier = Modifier.testTag("grant_overlay_perm_btn")
                    ) {
                        Text(
                            text = "Grant Overlay Permission",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.error,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                } else {
                    Text(
                        text = "Swipe down from top edge to trigger",
                        fontSize = 11.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    onClick = onTestFloatingOverlay,
                    shape = RoundedCornerShape(8.dp),
                    color = screenBg,
                    modifier = Modifier.testTag("open_floating_qs_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Open Floating QS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isGood: Boolean,
    badgeText: String? = null,
    badgeContainerColor: Color = Color.Transparent,
    badgeContentColor: Color = Color.Unspecified,
    onClick: (() -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = com.example.ui.theme.LocalIsDarkTheme.current
    val nonGrantedRed = com.example.ui.theme.getNonGrantedRedColor(isDark)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
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
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (isGood) colorScheme.primary else nonGrantedRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isGood) colorScheme.onPrimary else Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 16.sp,
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
                    text = subtitle,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.Both
                        )
                    ),
                    color = if (isGood) colorScheme.primary else nonGrantedRed
                )
            }
        }

        if (!badgeText.isNullOrEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))

            if (onClick != null) {
                Surface(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    color = badgeContainerColor
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeContentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeContainerColor
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeContentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

