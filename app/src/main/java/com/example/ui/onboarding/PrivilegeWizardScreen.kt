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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("privilege_wizard_screen"),
        verticalArrangement = Arrangement.spacedBy(PrivilegeCardShapes.CardSpacing)
    ) {
        // TOP CARD: Execution Privilege Modes
        Card(
            shape = PrivilegeCardShapes.TopCard,
            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainer),
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
                    color = colorScheme.onSurfaceVariant
                )

                // Select Mode Cards
                PrivilegeMode.values().forEach { mode ->
                    val isSelected = currentMode == mode
                    val isAvailable = when (mode) {
                        PrivilegeMode.STANDARD -> true
                        PrivilegeMode.SHIZUKU -> isShizukuAvailable
                        PrivilegeMode.ROOT -> isRootAvailable
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
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
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = mode.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) colorScheme.onSurface else colorScheme.onSurfaceVariant
                                )

                                Surface(
                                    color = if (isSelected) colorScheme.primaryContainer else colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = mode.badge,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = mode.description,
                                fontSize = 11.sp,
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }

                        if (isSelected) {
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

        // CENTER CARD: System Permissions Checklist
        Card(
            shape = PrivilegeCardShapes.CenterCard,
            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainer),
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

                // 1. Overlay Permission
                val hasOverlay = privilegeEngine.hasOverlayPermission()
                PermissionCheckRow(
                    title = "Draw Over Other Apps",
                    description = "SYSTEM_ALERT_WINDOW for floating swipe trigger",
                    icon = Icons.Default.Layers,
                    isGranted = hasOverlay,
                    onGrant = { privilegeEngine.requestOverlayPermission() }
                )

                // 2. Write System Settings
                val hasWriteSettings = privilegeEngine.hasWriteSettingsPermission()
                PermissionCheckRow(
                    title = "Modify System Settings",
                    description = "WRITE_SETTINGS for screen brightness & rotation",
                    icon = Icons.Default.Tune,
                    isGranted = hasWriteSettings,
                    onGrant = { privilegeEngine.requestWriteSettingsPermission() }
                )

                // 3. Do Not Disturb Policy
                val hasDnd = privilegeEngine.hasNotificationPolicyPermission()
                PermissionCheckRow(
                    title = "Notification Policy Access",
                    description = "ACCESS_NOTIFICATION_POLICY for DND mode toggle",
                    icon = Icons.Default.Notifications,
                    isGranted = hasDnd,
                    onGrant = { privilegeEngine.requestNotificationPolicyPermission() }
                )

                // 4. Accessibility Service
                val hasAccessibility = QuickPanelAccessibilityService.isRunning
                PermissionCheckRow(
                    title = "QuickPanel Accessibility Service",
                    description = "Enables global shade expansion and gestures",
                    icon = Icons.Default.Security,
                    isGranted = hasAccessibility,
                    onGrant = { privilegeEngine.openAccessibilitySettings() }
                )

                // 5. Shizuku Access & Service Status
                val shizukuStatus by privilegeEngine.shizukuStatus.collectAsStateWithLifecycle()
                val context = LocalContext.current
                ShizukuPermissionRow(
                    statusState = shizukuStatus,
                    onRequestAccess = {
                        val resultMsg = privilegeEngine.requestShizukuPermission()
                        Toast.makeText(context, resultMsg, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // BOTTOM CARD: Dynamic Command Execution Console
        Card(
            shape = PrivilegeCardShapes.BottomCard,
            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainer),
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
                            focusedContainerColor = colorScheme.surfaceContainerHigh,
                            unfocusedContainerColor = colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
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
    onGrant: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surfaceContainerHigh)
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
                        if (isGranted) colorScheme.primaryContainer
                        else colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isGranted) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 10.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }

        if (isGranted) {
            Surface(
                shape = CircleShape,
                color = colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Active",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                }
            }
        } else {
            Button(
                onClick = onGrant,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
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
    showBorder: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme

    val (statusColor, statusBg) = when (statusState) {
        com.example.system.ShizukuStatusState.RUNNING_GRANTED -> Pair(colorScheme.primary, colorScheme.primaryContainer)
        com.example.system.ShizukuStatusState.PERMISSION_REQUIRED -> Pair(colorScheme.tertiary, colorScheme.tertiaryContainer)
        com.example.system.ShizukuStatusState.SERVICE_NOT_RUNNING -> Pair(colorScheme.error, colorScheme.errorContainer)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surfaceContainerHigh)
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
                    .background(statusBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = "Shizuku ADB Service",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = statusState.label,
                    fontSize = 10.sp,
                    color = statusColor
                )
            }
        }

        Surface(
            onClick = onRequestAccess,
            shape = CircleShape,
            color = if (statusState == com.example.system.ShizukuStatusState.RUNNING_GRANTED)
                colorScheme.primary.copy(alpha = 0.15f)
            else
                colorScheme.primary,
            contentColor = if (statusState == com.example.system.ShizukuStatusState.RUNNING_GRANTED)
                colorScheme.primary
            else
                colorScheme.onPrimary,
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
