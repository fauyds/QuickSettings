package com.example.ui.onboarding

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.PrivilegeMode
import com.example.service.QuickPanelAccessibilityService
import com.example.system.PrivilegeEngine
import kotlinx.coroutines.launch

/**
 * Card shapes for Privilege Engine screen with 4dp spacing and 5dp inside corners.
 */
private object PrivilegeCardShapes {
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
}

@Composable
fun PrivilegeWizardScreen(
    privilegeEngine: PrivilegeEngine,
    currentMode: PrivilegeMode,
    isRootAvailable: Boolean,
    isShizukuAvailable: Boolean,
    lastLog: String,
    onModeSelected: (PrivilegeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var testCommandText by remember { mutableStateOf("svc wifi enable") }
    val colorScheme = MaterialTheme.colorScheme
    val isDark = com.example.ui.theme.LocalIsDarkTheme.current
    val screenBg = com.example.ui.theme.getSettingsScreenBackgroundColor(isDark)
    val cardBg = com.example.ui.theme.getSettingsCardBackgroundColor(isDark)

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                privilegeEngine.refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        privilegeEngine.refreshPermissions()
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val hasOverlay by privilegeEngine.hasOverlay.collectAsStateWithLifecycle()
    val hasWriteSettings by privilegeEngine.hasWriteSettings.collectAsStateWithLifecycle()
    val hasDnd by privilegeEngine.hasNotificationPolicy.collectAsStateWithLifecycle()
    val hasAccessibility by privilegeEngine.hasAccessibility.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("privilege_wizard_screen"),
        verticalArrangement = Arrangement.spacedBy(PrivilegeCardShapes.CardSpacing)
    ) {
        // TOP CARD: Execution Privilege Modes
        Card(
            shape = PrivilegeCardShapes.TopCard,
            colors = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Row with Monet Pill
                Text(
                    text = "EXECUTION PRIVILEGES ENGINE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                    color = colorScheme.primary
                )

                Text(
                    text = "Choose how QuickPanel triggers hardware toggles and executes actions. Dynamic command routing will automatically adapt.",
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                    color = colorScheme.onSurfaceVariant
                )

                // Select Mode Cards
                val modes = PrivilegeMode.values()
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    modes.forEachIndexed { index, mode ->
                        val isSelected = currentMode == mode
                        val isAvailable = when (mode) {
                            PrivilegeMode.STANDARD -> true
                            PrivilegeMode.SHIZUKU -> isShizukuAvailable
                            PrivilegeMode.ROOT -> isRootAvailable
                        }

                        val itemShape = if (isSelected) {
                            RoundedCornerShape(24.dp)
                        } else {
                            when {
                                modes.size == 1 -> RoundedCornerShape(16.dp)
                                index == 0 -> RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = 4.dp,
                                    bottomEnd = 4.dp
                                )
                                index == modes.size - 1 -> RoundedCornerShape(
                                    topStart = 4.dp,
                                    topEnd = 4.dp,
                                    bottomStart = 16.dp,
                                    bottomEnd = 16.dp
                                )
                                else -> RoundedCornerShape(4.dp)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(itemShape)
                                .background(
                                    if (isSelected) colorScheme.primaryContainer
                                    else screenBg
                                )
                                .clickable {
                                    onModeSelected(mode)
                                    if (mode == PrivilegeMode.SHIZUKU) {
                                        privilegeEngine.requestShizukuPermission()
                                    }
                                }
                                .padding(12.dp)
                                .testTag("privilege_mode_${mode.name.lowercase()}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = mode.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 16.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false),
                                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                        color = if (isSelected) colorScheme.onPrimaryContainer else colorScheme.onSurface
                                    )

                                    Surface(
                                        color = if (isSelected) colorScheme.primary else colorScheme.surfaceVariant,
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = mode.badge,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            lineHeight = 12.sp,
                                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                            maxLines = 1,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = mode.description,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = if (isSelected) colorScheme.onPrimaryContainer.copy(alpha = 0.85f) else colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }

                            if (isSelected) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // CENTER CARD: System Permissions Checklist
        Card(
            shape = PrivilegeCardShapes.CenterCard,
            colors = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "SYSTEM PERMISSIONS CHECKLIST",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                    color = colorScheme.primary
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 1. Overlay Permission
                    PermissionCheckRow(
                        title = "Draw Over Other Apps",
                        description = "SYSTEM_ALERT_WINDOW for floating swipe trigger",
                        icon = Icons.Default.Layers,
                        isGranted = hasOverlay,
                        onGrant = {
                            privilegeEngine.requestOverlayPermission()
                            privilegeEngine.refreshPermissions()
                        },
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = 4.dp,
                            bottomEnd = 4.dp
                        )
                    )

                    // 2. Write System Settings
                    PermissionCheckRow(
                        title = "Modify System Settings",
                        description = "WRITE_SETTINGS for screen brightness & rotation",
                        icon = Icons.Default.Tune,
                        isGranted = hasWriteSettings,
                        onGrant = {
                            privilegeEngine.requestWriteSettingsPermission()
                            privilegeEngine.refreshPermissions()
                        },
                        shape = RoundedCornerShape(4.dp)
                    )

                    // 3. Do Not Disturb Policy
                    PermissionCheckRow(
                        title = "Notification Policy Access",
                        description = "ACCESS_NOTIFICATION_POLICY for DND mode toggle",
                        icon = Icons.Default.Notifications,
                        isGranted = hasDnd,
                        onGrant = {
                            privilegeEngine.requestNotificationPolicyPermission()
                            privilegeEngine.refreshPermissions()
                        },
                        shape = RoundedCornerShape(4.dp)
                    )

                    // 4. Accessibility Service
                    val isModeAccessVisible = currentMode == PrivilegeMode.SHIZUKU || currentMode == PrivilegeMode.ROOT
                    PermissionCheckRow(
                        title = "QuickPanel Accessibility Service",
                        description = "Enables global shade expansion and gestures",
                        icon = Icons.Default.Security,
                        isGranted = hasAccessibility,
                        onGrant = {
                            privilegeEngine.openAccessibilitySettings()
                            privilegeEngine.refreshPermissions()
                        },
                        shape = if (!isModeAccessVisible) {
                            RoundedCornerShape(
                                topStart = 4.dp,
                                topEnd = 4.dp,
                                bottomStart = 16.dp,
                                bottomEnd = 16.dp
                            )
                        } else {
                            RoundedCornerShape(4.dp)
                        }
                    )

                    // 5. Additional mode access granting:
                    // Standard Mode - Hidden
                    // Shizuku Mode - Show "Shizuku ADB Service"
                    // Root Mode - Show "Root Access"
                    val context = LocalContext.current
                    when (currentMode) {
                        PrivilegeMode.STANDARD -> {
                            // Hidden in Standard Mode
                        }
                        PrivilegeMode.SHIZUKU -> {
                            val shizukuStatus by privilegeEngine.shizukuStatus.collectAsStateWithLifecycle()
                            ShizukuPermissionRow(
                                statusState = shizukuStatus,
                                onRequestAccess = {
                                    val resultMsg = privilegeEngine.requestShizukuPermission()
                                    privilegeEngine.refreshPermissions()
                                    Toast.makeText(context, resultMsg, Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(
                                    topStart = 4.dp,
                                    topEnd = 4.dp,
                                    bottomStart = 16.dp,
                                    bottomEnd = 16.dp
                                )
                            )
                        }
                        PrivilegeMode.ROOT -> {
                            val isRootActive by privilegeEngine.isRootAvailable.collectAsStateWithLifecycle()
                            RootPermissionRow(
                                isRootGranted = isRootActive,
                                onRequestAccess = {
                                    val granted = privilegeEngine.requestRootPermission()
                                    privilegeEngine.refreshPermissions()
                                    val msg = if (granted) "Root access granted!" else "Root request failed. Please check root manager."
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(
                                    topStart = 4.dp,
                                    topEnd = 4.dp,
                                    bottomStart = 16.dp,
                                    bottomEnd = 16.dp
                                )
                            )
                        }
                    }
                }
            }
        }

        // BOTTOM CARD: Dynamic Command Execution Console
        Card(
            shape = PrivilegeCardShapes.BottomCard,
            colors = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "DYNAMIC COMMAND EXECUTION CONSOLE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                    color = colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = testCommandText,
                        onValueChange = { testCommandText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("console_command_input"),
                        label = { Text("Shell command", fontSize = 11.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = screenBg,
                            unfocusedContainerColor = screenBg,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedTextColor = colorScheme.onSurface,
                            unfocusedTextColor = colorScheme.onSurface
                        )
                    )

                    Button(
                        onClick = {
                            scope.launch {
                                privilegeEngine.executeCommand(testCommandText)
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("execute_command_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Run", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Log Window
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colorScheme.surfaceContainerLowest)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "> $lastLog",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PermissionCheckRow(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    onGrant: () -> Unit,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = com.example.ui.theme.LocalIsDarkTheme.current
    val screenBg = com.example.ui.theme.getSettingsScreenBackgroundColor(isDark)
    val nonGrantedRed = com.example.ui.theme.getNonGrantedRedColor(isDark)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(screenBg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
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
                    .background(
                        if (isGranted) colorScheme.primary
                        else nonGrantedRed
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isGranted) colorScheme.onPrimary else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 15.sp,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                    color = colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                    color = colorScheme.onSurfaceVariant
                )
            }
        }

        if (isGranted) {
            Surface(
                shape = CircleShape,
                color = colorScheme.primary
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Active",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onPrimary
                    )
                }
            }
        } else {
            Button(
                onClick = onGrant,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = nonGrantedRed,
                    contentColor = Color.White
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(32.dp)
                    .testTag("grant_perm_${title.lowercase().take(6)}")
            ) {
                Text(
                    text = "Grant",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ShizukuPermissionRow(
    statusState: com.example.system.ShizukuStatusState,
    onRequestAccess: () -> Unit,
    showBorder: Boolean = false,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = com.example.ui.theme.LocalIsDarkTheme.current
    val screenBg = com.example.ui.theme.getSettingsScreenBackgroundColor(isDark)
    val nonGrantedRed = com.example.ui.theme.getNonGrantedRedColor(isDark)

    val (statusColor, statusBg) = when (statusState) {
        com.example.system.ShizukuStatusState.RUNNING_GRANTED -> Pair(colorScheme.primary, colorScheme.primaryContainer)
        com.example.system.ShizukuStatusState.PERMISSION_REQUIRED -> Pair(nonGrantedRed, nonGrantedRed)
        com.example.system.ShizukuStatusState.SERVICE_NOT_RUNNING -> Pair(nonGrantedRed, nonGrantedRed)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(screenBg)
            .padding(10.dp),
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
                    .background(
                        if (statusState == com.example.system.ShizukuStatusState.RUNNING_GRANTED) colorScheme.primary
                        else nonGrantedRed
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = if (statusState == com.example.system.ShizukuStatusState.RUNNING_GRANTED) colorScheme.onPrimary else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = "Shizuku ADB Service",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 15.sp,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                    color = colorScheme.onSurface
                )
                Text(
                    text = statusState.label,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                    color = if (statusState == com.example.system.ShizukuStatusState.RUNNING_GRANTED) colorScheme.primary else nonGrantedRed
                )
            }
        }

        Surface(
            onClick = onRequestAccess,
            shape = CircleShape,
            color = if (statusState == com.example.system.ShizukuStatusState.RUNNING_GRANTED)
                colorScheme.primary
            else
                nonGrantedRed,
            contentColor = if (statusState == com.example.system.ShizukuStatusState.RUNNING_GRANTED)
                colorScheme.onPrimary
            else
                Color.White,
            modifier = Modifier.testTag("btn_shizuku_permission_request")
        ) {
            Text(
                text = if (statusState == com.example.system.ShizukuStatusState.RUNNING_GRANTED) "Granted" else "Request Access",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun RootPermissionRow(
    isRootGranted: Boolean,
    onRequestAccess: () -> Unit,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = com.example.ui.theme.LocalIsDarkTheme.current
    val screenBg = com.example.ui.theme.getSettingsScreenBackgroundColor(isDark)
    val nonGrantedRed = com.example.ui.theme.getNonGrantedRedColor(isDark)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(screenBg)
            .padding(10.dp),
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
                    .background(if (isRootGranted) colorScheme.primary else nonGrantedRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = if (isRootGranted) colorScheme.onPrimary else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = "Root Access",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 15.sp,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                    color = colorScheme.onSurface
                )
                Text(
                    text = if (isRootGranted) "Granted (/system/bin/su active)" else "Superuser access required",
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                    color = if (isRootGranted) colorScheme.primary else nonGrantedRed
                )
            }
        }

        Surface(
            onClick = onRequestAccess,
            shape = CircleShape,
            color = if (isRootGranted)
                colorScheme.primary
            else
                nonGrantedRed,
            contentColor = if (isRootGranted)
                colorScheme.onPrimary
            else
                Color.White,
            modifier = Modifier.testTag("btn_root_permission_request")
        ) {
            Text(
                text = if (isRootGranted) "Granted" else "Request Access",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}
