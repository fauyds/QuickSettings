package com.example.ui.onboarding

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.PanelPreferences
import com.example.data.TileCommandRepository
import com.example.model.CommandExecutionMode
import com.example.model.PrivilegeMode
import com.example.model.TileActionOverrideType
import com.example.model.TileCommandConfig
import com.example.service.QuickPanelAccessibilityService
import com.example.system.PrivilegeEngine
import com.example.system.ShizukuStatusState
import com.example.ui.MainViewModel
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.getSettingsCardBackgroundColor
import com.example.ui.theme.getSettingsScreenBackgroundColor
import kotlinx.coroutines.launch

/**
 * Initial Setup Wizard for QuickPanel.
 * Guides the user through:
 * 1. Enable corresponding permissions (Non root + Request Shizuku & Root access buttons)
 * 2. Choose execution privilege engine (granted privileges, buttons to request Root/Shizuku,
 *    dropdown of working QS tiles with left icon / right name, and status bar detector details)
 * 3. Tile commands setup (Shows all tiles; options to write ON/OFF ADB commands for Root/Shizuku,
 *    or Intent action input box for Non root mode)
 * 4. Visibility tile setup (Toggles for all tiles + Smart Hide)
 * 5. Trigger & Shade Toggles (Stock shade override, replacement delay, invisible trigger, auto closing)
 *
 * Design:
 * - Top icon with label at bottom without card.
 * - Cards for details.
 * - Bottom action bar: 8dp back/skip button, rounded next button.
 */
@Composable
fun InitialSetupScreen(
    viewModel: MainViewModel,
    onComplete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 5

    val privilegeEngine = viewModel.privilegeEngine
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

    val allPermissionsGranted = hasOverlay && hasWriteSettings && hasDnd && hasAccessibility

    BackHandler {
        if (currentStep > 0) {
            currentStep--
        } else {
            onDismiss()
        }
    }

    val isDark = LocalIsDarkTheme.current
    val screenBg = getSettingsScreenBackgroundColor(isDark)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("initial_setup_screen")
    ) {
        // Top app bar with Step Indicator & Skip Setup Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Step Pill Indicator
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            ) {
                Text(
                    text = "STEP ${currentStep + 1} OF $totalSteps",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }

            TextButton(
                onClick = onComplete,
                modifier = Modifier.testTag("setup_top_skip_button")
            ) {
                Text(
                    text = "Skip Setup",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Scrollable Step Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (currentStep) {
                0 -> PermissionsSetupStep(
                    viewModel = viewModel,
                    hasOverlay = hasOverlay,
                    hasWriteSettings = hasWriteSettings,
                    hasDnd = hasDnd,
                    hasAccessibility = hasAccessibility
                )
                1 -> PrivilegeEngineStep(viewModel = viewModel)
                2 -> TileCommandsStep(viewModel = viewModel)
                3 -> TileVisibilityStep(viewModel = viewModel)
                4 -> TriggerTogglesStep()
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom Action Bar
        // Buttons at very bottom: 8dp back/skip button; rounded next button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 8dp back/skip button (active on step 0 to skip entire initial setup)
            FilledTonalButton(
                onClick = {
                    if (currentStep > 0) {
                        currentStep--
                    } else {
                        onComplete()
                    }
                },
                enabled = true,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .height(48.dp)
                    .testTag(if (currentStep > 0) "setup_back_button" else "setup_skip_button")
            ) {
                Icon(
                    imageVector = if (currentStep > 0) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.SkipNext,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (currentStep > 0) "Back" else "Skip",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            // Rounded next button (un-nextable on first step until all permissions are accepted)
            Button(
                onClick = {
                    if (currentStep < totalSteps - 1) {
                        currentStep++
                    } else {
                        onComplete()
                    }
                },
                enabled = if (currentStep == 0) allPermissionsGranted else true,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("setup_next_button")
            ) {
                Text(
                    text = if (currentStep == totalSteps - 1) "Complete Setup" else "Next",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = if (currentStep == totalSteps - 1) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Top icon with a label at bottom WITHOUT a card.
 */
@Composable
private fun SetupStepHeader(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top Icon
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
        }

        // Label at bottom without card
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
        )

        Text(
            text = subtitle,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
        )
    }
}

/**
 * STEP 1: Enable corresponding permissions
 * Contains required standard permissions AND elevated buttons for Shizuku and Root!
 */
@Composable
private fun PermissionsSetupStep(
    viewModel: MainViewModel,
    hasOverlay: Boolean,
    hasWriteSettings: Boolean,
    hasDnd: Boolean,
    hasAccessibility: Boolean
) {
    val privilegeEngine = viewModel.privilegeEngine
    val isDark = LocalIsDarkTheme.current
    val cardBg = getSettingsCardBackgroundColor(isDark)

    SetupStepHeader(
        icon = Icons.Default.Security,
        title = "Enable Permissions",
        subtitle = "QuickPanel requires system permissions to draw the floating overlay, toggle settings, and intercept gestures."
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card: Standard Required Permissions
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "REQUIRED STANDARD PERMISSIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                    color = MaterialTheme.colorScheme.primary
                )

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
                    shape = RoundedCornerShape(12.dp)
                )

                // 2. Write System Settings
                PermissionCheckRow(
                    title = "Modify System Settings",
                    description = "WRITE_SETTINGS for brightness & auto-rotate",
                    icon = Icons.Default.Tune,
                    isGranted = hasWriteSettings,
                    onGrant = {
                        privilegeEngine.requestWriteSettingsPermission()
                        privilegeEngine.refreshPermissions()
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                // 3. Do Not Disturb Policy
                PermissionCheckRow(
                    title = "Notification Policy Access",
                    description = "ACCESS_NOTIFICATION_POLICY for DND toggles",
                    icon = Icons.Default.Notifications,
                    isGranted = hasDnd,
                    onGrant = {
                        privilegeEngine.requestNotificationPolicyPermission()
                        privilegeEngine.refreshPermissions()
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                // 4. Accessibility Service
                PermissionCheckRow(
                    title = "Accessibility Service",
                    description = "Enables global shade expansion & gestures",
                    icon = Icons.Default.Security,
                    isGranted = hasAccessibility,
                    onGrant = {
                        privilegeEngine.openAccessibilitySettings()
                        privilegeEngine.refreshPermissions()
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

/**
 * STEP 2: Choose execution privilege engine
 * Displays privileges and capabilities, working QS tiles (with drop-down button: left icon, right name),
 * writes if status bar detector will work, and includes buttons to request Root / Shizuku permission!
 */
@Composable
private fun PrivilegeEngineStep(viewModel: MainViewModel) {
    val privilegeEngine = viewModel.privilegeEngine
    val currentMode by privilegeEngine.currentMode.collectAsStateWithLifecycle()
    val isRootAvail by privilegeEngine.isRootAvailable.collectAsStateWithLifecycle()
    val isShizukuAvail by privilegeEngine.isShizukuAvailable.collectAsStateWithLifecycle()
    val shizukuStatus by privilegeEngine.shizukuStatus.collectAsStateWithLifecycle()
    val isDark = LocalIsDarkTheme.current
    val cardBg = getSettingsCardBackgroundColor(isDark)
    val context = LocalContext.current

    SetupStepHeader(
        icon = Icons.Default.Tune,
        title = "Choose Execution Engine",
        subtitle = "Select your execution privilege engine. Check privileges and supported QS tiles for each mode below."
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mode 1: Non root Mode
        PrivilegeModeDetailCard(
            mode = PrivilegeMode.STANDARD,
            badgeLabel = "Safe / Default",
            isSelected = currentMode == PrivilegeMode.STANDARD,
            cardBg = cardBg,
            privilegesDescription = "Accessibility Service, Write Settings, Notification Policy, Overlay (SYSTEM_ALERT_WINDOW)",
            statusBarDetectorWork = "Status Bar Detector: Not working (Requires elevated Shizuku or Root engine to intercept status bar pulls and override stock shade)",
            isStatusBarDetectorWorking = false,
            workingTiles = listOf(
                TileDropdownItem("Flashlight", Icons.Default.FlashlightOn),
                TileDropdownItem("Auto-Rotate", Icons.Default.RotateRight),
                TileDropdownItem("Screen Brightness", Icons.Default.Tune),
                TileDropdownItem("Screen Record", Icons.Default.Videocam),
                TileDropdownItem("Always-On Display", Icons.Default.Wallpaper),
                TileDropdownItem("Wi-Fi / Internet (Opens Dialog)", Icons.Default.Wifi)
            ),
            requestButtonText = null,
            onRequestPermission = null,
            isPermissionGranted = true,
            onSelect = { viewModel.setPrivilegeMode(PrivilegeMode.STANDARD) }
        )

        // Mode 2: Shizuku Mode
        PrivilegeModeDetailCard(
            mode = PrivilegeMode.SHIZUKU,
            badgeLabel = "Elevated ADB",
            isSelected = currentMode == PrivilegeMode.SHIZUKU,
            cardBg = cardBg,
            privilegesDescription = "Elevated ADB IPC Service via Shizuku + Standard Android Permissions",
            statusBarDetectorWork = "Status Bar Detector: Works (Accessibility Service + elevated IPC event dispatching ensures smooth status bar gesture interception)",
            workingTiles = listOf(
                TileDropdownItem("Wi-Fi", Icons.Default.Wifi),
                TileDropdownItem("Mobile Data", Icons.Default.SignalCellular4Bar),
                TileDropdownItem("Airplane Mode", Icons.Default.AirplanemodeActive),
                TileDropdownItem("Bluetooth", Icons.Default.Bluetooth),
                TileDropdownItem("Location", Icons.Default.LocationOn),
                TileDropdownItem("NFC", Icons.Default.Nfc),
                TileDropdownItem("Hotspot / Wi-Fi Tethering", Icons.Default.WifiTethering),
                TileDropdownItem("Battery Saver", Icons.Default.BatterySaver),
                TileDropdownItem("Flashlight", Icons.Default.FlashlightOn),
                TileDropdownItem("Auto-Rotate", Icons.Default.RotateRight),
                TileDropdownItem("Screen Brightness", Icons.Default.Tune),
                TileDropdownItem("Sound & Volume Modes", Icons.Default.VolumeUp),
                TileDropdownItem("Dark Theme", Icons.Default.Brightness4),
                TileDropdownItem("Screen Record", Icons.Default.Videocam),
                TileDropdownItem("Caffeine", Icons.Default.LocalCafe),
                TileDropdownItem("Heads-Up Notifications", Icons.Default.NotificationsActive),
                TileDropdownItem("System UI Restart", Icons.Default.Terminal),
                TileDropdownItem("Custom ADB Shell Commands", Icons.Default.Terminal)
            ),
            requestButtonText = if (shizukuStatus == ShizukuStatusState.RUNNING_GRANTED) "✓ Shizuku Authorized" else "Request Shizuku Permission",
            onRequestPermission = {
                val msg = privilegeEngine.requestShizukuPermission()
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            isPermissionGranted = shizukuStatus == ShizukuStatusState.RUNNING_GRANTED,
            onSelect = {
                viewModel.setPrivilegeMode(PrivilegeMode.SHIZUKU)
                if (shizukuStatus != ShizukuStatusState.RUNNING_GRANTED) {
                    val msg = privilegeEngine.requestShizukuPermission()
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        )

        // Mode 3: Root Mode
        PrivilegeModeDetailCard(
            mode = PrivilegeMode.ROOT,
            badgeLabel = "Full Control",
            isSelected = currentMode == PrivilegeMode.ROOT,
            cardBg = cardBg,
            privilegesDescription = "Direct Superuser (/system/bin/su) Root Shell with unrestricted system privileges",
            statusBarDetectorWork = "Status Bar Detector: Works (Direct instant system execution with immediate gesture response and system shade override)",
            workingTiles = listOf(
                TileDropdownItem("Wi-Fi", Icons.Default.Wifi),
                TileDropdownItem("Mobile Data", Icons.Default.SignalCellular4Bar),
                TileDropdownItem("Airplane Mode", Icons.Default.AirplanemodeActive),
                TileDropdownItem("Bluetooth", Icons.Default.Bluetooth),
                TileDropdownItem("Location", Icons.Default.LocationOn),
                TileDropdownItem("NFC", Icons.Default.Nfc),
                TileDropdownItem("Hotspot / Wi-Fi Tethering", Icons.Default.WifiTethering),
                TileDropdownItem("Battery Saver", Icons.Default.BatterySaver),
                TileDropdownItem("Flashlight", Icons.Default.FlashlightOn),
                TileDropdownItem("Auto-Rotate", Icons.Default.RotateRight),
                TileDropdownItem("Dark Theme", Icons.Default.Brightness4),
                TileDropdownItem("Sound & Volume Modes", Icons.Default.VolumeUp),
                TileDropdownItem("Screen Record", Icons.Default.Videocam),
                TileDropdownItem("Always-On Display", Icons.Default.Wallpaper),
                TileDropdownItem("Ambient Display", Icons.Default.Visibility),
                TileDropdownItem("Caffeine", Icons.Default.LocalCafe),
                TileDropdownItem("Heads-Up Notifications", Icons.Default.NotificationsActive),
                TileDropdownItem("Screenshot", Icons.Default.CameraAlt),
                TileDropdownItem("Fast Reboot / Power Menu", Icons.Default.Terminal),
                TileDropdownItem("Custom Root Shell Commands", Icons.Default.Terminal)
            ),
            requestButtonText = if (isRootAvail) "✓ Root Access Granted" else "Request Root Access",
            onRequestPermission = {
                val ok = privilegeEngine.requestRootPermission()
                val msg = if (ok) "Root access granted!" else "Root access denied or SU not found"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            isPermissionGranted = isRootAvail,
            onSelect = {
                viewModel.setPrivilegeMode(PrivilegeMode.ROOT)
                if (!isRootAvail) {
                    val ok = privilegeEngine.requestRootPermission()
                    val msg = if (ok) "Root access granted!" else "Root access denied or SU not found"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

private data class TileDropdownItem(
    val name: String,
    val icon: ImageVector
)

@Composable
private fun PrivilegeModeDetailCard(
    mode: PrivilegeMode,
    badgeLabel: String,
    isSelected: Boolean,
    cardBg: Color,
    privilegesDescription: String,
    statusBarDetectorWork: String,
    isStatusBarDetectorWorking: Boolean = true,
    workingTiles: List<TileDropdownItem>,
    requestButtonText: String?,
    onRequestPermission: (() -> Unit)?,
    isPermissionGranted: Boolean,
    onSelect: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var isWorkingTilesExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colorScheme.primaryContainer.copy(alpha = 0.45f) else cardBg
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, colorScheme.primary) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("setup_mode_${mode.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Mode Header: Title, Round Badge (8dp horizontal, 6dp vertical), Selection Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = mode.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = colorScheme.onSurface
                    )

                    // Requested Badge: Round, 8dp horizontal, 6dp vertical
                    Surface(
                        color = colorScheme.primary,
                        shape = CircleShape
                    ) {
                        Text(
                            text = badgeLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }

                if (isSelected) {
                    Surface(
                        shape = CircleShape,
                        color = colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Button to request root / shizuku permission if applicable
            if (requestButtonText != null && onRequestPermission != null) {
                if (isPermissionGranted) {
                    FilledTonalButton(
                        onClick = onRequestPermission,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(requestButtonText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = onRequestPermission,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_req_perm_${mode.name.lowercase()}")
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(requestButtonText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Privileges & Capabilities
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "PRIVILEGES & CAPABILITIES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
                Text(
                    text = privilegesDescription,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }

            // Status Bar Detector Status Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = if (isStatusBarDetectorWorking) Icons.Default.CheckCircle else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isStatusBarDetectorWorking) colorScheme.primary else colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = statusBarDetectorWork,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = colorScheme.onSurface
                )
            }

            // Drop-down button that opens the list of tiles - left side: tile icon, right side: its name
            FilledTonalButton(
                onClick = { isWorkingTilesExpanded = !isWorkingTilesExpanded },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dropdown_tiles_${mode.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isWorkingTilesExpanded) "Hide Working QS Tiles (${workingTiles.size})" else "View Working QS Tiles (${workingTiles.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(if (isWorkingTilesExpanded) 180f else 0f)
                    )
                }
            }

            // List of working tiles
            AnimatedVisibility(visible = isWorkingTilesExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colorScheme.surfaceContainerHigh)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    workingTiles.forEach { tile ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            // Left side: tile icon
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tile.icon,
                                    contentDescription = tile.name,
                                    tint = colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Right side: its name
                            Text(
                                text = tile.name,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * STEP 3: Tile commands setup
 * Shows all tiles with option to write ON and OFF command.
 * Selected mode depends on what did you choose (Shizuku, Root, or Standard):
 * - Standard option shows the Intent input box
 * - Root / Shizuku option shows the ADB command input box (ON and OFF command)
 */
private data class SetupTileDefinition(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val defaultIntent: String,
    val defaultOnCmd: String,
    val defaultOffCmd: String
)

private val SetupAllTilesList = listOf(
    SetupTileDefinition("wifi", "Wi-Fi", Icons.Default.Wifi, "android.settings.WIFI_SETTINGS", "svc wifi enable", "svc wifi disable"),
    SetupTileDefinition("bluetooth", "Bluetooth", Icons.Default.Bluetooth, "android.settings.BLUETOOTH_SETTINGS", "cmd bluetooth_manager enable", "cmd bluetooth_manager disable"),
    SetupTileDefinition("torch", "Flashlight", Icons.Default.FlashlightOn, "android.settings.DISPLAY_SETTINGS", "", ""),
    SetupTileDefinition("cellular", "Mobile Data", Icons.Default.SignalCellular4Bar, "android.settings.DATA_ROAMING_SETTINGS", "svc data enable", "svc data disable"),
    SetupTileDefinition("nfc", "NFC", Icons.Default.Nfc, "android.settings.NFC_SETTINGS", "svc nfc enable", "svc nfc disable"),
    SetupTileDefinition("airplane", "Airplane Mode", Icons.Default.AirplanemodeActive, "android.settings.AIRPLANE_MODE_SETTINGS", "cmd connectivity airplane-mode enable", "cmd connectivity airplane-mode disable"),
    SetupTileDefinition("location", "Location", Icons.Default.LocationOn, "android.settings.LOCATION_SOURCE_SETTINGS", "settings put secure location_mode 3", "settings put secure location_mode 0"),
    SetupTileDefinition("autorotate", "Auto-Rotate", Icons.Default.RotateRight, "android.settings.DISPLAY_SETTINGS", "settings put system accelerometer_rotation 1", "settings put system accelerometer_rotation 0"),
    SetupTileDefinition("hotspot", "Hotspot / Wi-Fi Tethering", Icons.Default.WifiTethering, "android.settings.TETHER_SETTINGS", "cmd connectivity tether startwifi", "cmd connectivity tether stopwifi"),
    SetupTileDefinition("battery_saver", "Battery Saver", Icons.Default.BatterySaver, "android.settings.BATTERY_SAVER_SETTINGS", "cmd power set-mode 1", "cmd power set-mode 0"),
    SetupTileDefinition("sound_mode", "Sound & Volume", Icons.Default.VolumeUp, "android.settings.SOUND_SETTINGS", "cmd audio set-volume 3 100", "cmd audio set-volume 3 0"),
    SetupTileDefinition("dark_mode", "Dark Mode", Icons.Default.Brightness4, "android.settings.DISPLAY_SETTINGS", "cmd uimode night yes", "cmd uimode night no"),
    SetupTileDefinition("aod", "Always-On Display", Icons.Default.Wallpaper, "android.settings.DISPLAY_SETTINGS", "settings put secure doze_always_on 1", "settings put secure doze_always_on 0"),
    SetupTileDefinition("ambient_display", "Ambient Display", Icons.Default.Visibility, "android.settings.DISPLAY_SETTINGS", "settings put secure doze_enabled 1", "settings put secure doze_enabled 0"),
    SetupTileDefinition("caffeine", "Caffeine", Icons.Default.LocalCafe, "android.settings.DISPLAY_SETTINGS", "settings put system screen_off_timeout 2147483647", "settings put system screen_off_timeout 30000"),
    SetupTileDefinition("heads_up", "Heads Up", Icons.Default.NotificationsActive, "android.settings.NOTIFICATION_SETTINGS", "settings put global heads_up_notifications_enabled 1", "settings put global heads_up_notifications_enabled 0"),
    SetupTileDefinition("screenshot", "Screenshot", Icons.Default.CameraAlt, "", "input keyevent 120", "input keyevent 120"),
    SetupTileDefinition("screenrecord", "Screen Record", Icons.Default.Videocam, "", "screenrecord --time-limit 15 /sdcard/screenrecord.mp4 &", "pkill -2 screenrecord")
)

@Composable
private fun TileCommandsStep(viewModel: MainViewModel) {
    val privilegeEngine = viewModel.privilegeEngine
    val engineMode by privilegeEngine.currentMode.collectAsStateWithLifecycle()
    val isDark = LocalIsDarkTheme.current
    val cardBg = getSettingsCardBackgroundColor(isDark)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val configs by TileCommandRepository.tileConfigs.collectAsStateWithLifecycle()

    var activeMode by remember(engineMode) { mutableStateOf(engineMode) }

    SetupStepHeader(
        icon = Icons.Default.Terminal,
        title = "Tile Commands Setup",
        subtitle = "Configure tile execution commands and intent actions. Command fields adapt automatically to your selected privilege engine."
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Mode Selector Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TARGET EXECUTION ENGINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ) {
                        Text(
                            text = activeMode.title,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = if (activeMode == PrivilegeMode.STANDARD)
                        "Standard option shows the Intent input box for launching system settings or activities."
                    else
                        "${activeMode.title} option shows ADB shell command input boxes for promptless ON and OFF execution.",
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Mode switch buttons without borders; selected full round, unselected inner corners 8dp, outer corners round
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val modes = PrivilegeMode.entries
                    modes.forEachIndexed { index, mode ->
                        val isSelected = activeMode == mode
                        val shape = if (isSelected) {
                            CircleShape
                        } else {
                            when (index) {
                                0 -> RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp, topEnd = 8.dp, bottomEnd = 8.dp)
                                modes.size - 1 -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = 24.dp, bottomEnd = 24.dp)
                                else -> RoundedCornerShape(8.dp)
                            }
                        }

                        Button(
                            onClick = {
                                activeMode = mode
                                viewModel.setPrivilegeMode(mode)
                            },
                            shape = shape,
                            border = null,
                            elevation = null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("setup_engine_btn_${mode.name.lowercase()}")
                        ) {
                            Text(
                                text = mode.title,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Master Enable All Toggles Card
        val areAllOverridesEnabled = remember(configs) {
            SetupAllTilesList.all { tile -> configs[tile.id]?.isOverrideEnabled == true }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("setup_enable_all_commands_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = "Enable All Tile Overrides",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (areAllOverridesEnabled) "All tile overrides are active" else "Turn on custom command overrides for all tiles",
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Switch(
                    checked = areAllOverridesEnabled,
                    onCheckedChange = { enable ->
                        val currentExecMode = if (activeMode == PrivilegeMode.ROOT) CommandExecutionMode.ROOT
                            else if (activeMode == PrivilegeMode.SHIZUKU) CommandExecutionMode.SHIZUKU
                            else CommandExecutionMode.STANDARD
                        TileCommandRepository.setAllOverridesEnabled(context, enable, currentExecMode)
                    },
                    modifier = Modifier.testTag("setup_toggle_all_commands")
                )
            }
        }

        // Show all tiles with command options
        SetupAllTilesList.forEach { tile ->
            val config = configs[tile.id] ?: remember(tile.id) {
                TileCommandConfig(
                    tileId = tile.id,
                    commandOn = tile.defaultOnCmd,
                    commandOff = tile.defaultOffCmd,
                    customIntentAction = tile.defaultIntent
                )
            }

            SetupTileCommandEditorCard(
                tile = tile,
                config = config,
                mode = activeMode,
                cardBg = cardBg,
                onSaveConfig = { updated ->
                    TileCommandRepository.saveConfig(context, updated)
                },
                onResetDefaults = {
                    TileCommandRepository.resetConfig(context, tile.id)
                    Toast.makeText(context, "Reset ${tile.title} commands", Toast.LENGTH_SHORT).show()
                },
                onTestCommand = { cmd ->
                    coroutineScope.launch {
                        Toast.makeText(context, "Running: $cmd", Toast.LENGTH_SHORT).show()
                        val execMode = if (activeMode == PrivilegeMode.ROOT) CommandExecutionMode.ROOT else CommandExecutionMode.SHIZUKU
                        val ok = privilegeEngine.executeCommandWithMode(cmd, execMode, null)
                        Toast.makeText(context, if (ok) "Command executed successfully!" else "Execution failed", Toast.LENGTH_SHORT).show()
                    }
                },
                onTestIntent = { action, pkg ->
                    try {
                        val intent = if (action.isNotBlank()) {
                            Intent(action).apply {
                                if (pkg.isNotBlank()) setPackage(pkg)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                        } else {
                            Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                        }
                        context.startActivity(intent)
                        Toast.makeText(context, "Launched: ${intent.action}", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not launch intent: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
private fun SetupTileCommandEditorCard(
    tile: SetupTileDefinition,
    config: TileCommandConfig,
    mode: PrivilegeMode,
    cardBg: Color,
    onSaveConfig: (TileCommandConfig) -> Unit,
    onResetDefaults: () -> Unit,
    onTestCommand: (String) -> Unit,
    onTestIntent: (String, String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var isExpanded by remember { mutableStateOf(false) }

    // State for inputs
    var onCmdText by remember(config.commandOn, tile.defaultOnCmd) {
        mutableStateOf(if (config.commandOn.isNotBlank()) config.commandOn else tile.defaultOnCmd)
    }
    var offCmdText by remember(config.commandOff, tile.defaultOffCmd) {
        mutableStateOf(if (config.commandOff.isNotBlank()) config.commandOff else tile.defaultOffCmd)
    }
    var intentActionText by remember(config.customIntentAction, tile.defaultIntent) {
        mutableStateOf(if (config.customIntentAction.isNotBlank()) config.customIntentAction else tile.defaultIntent)
    }
    var intentPkgText by remember(config.customIntentPackage) {
        mutableStateOf(config.customIntentPackage)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("setup_tile_cmd_${tile.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Tile Icon + Name + Expand Chevron
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tile.icon,
                            contentDescription = tile.title,
                            tint = colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = tile.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = if (mode == PrivilegeMode.STANDARD)
                                (if (intentActionText.isNotBlank()) intentActionText.substringAfterLast('.') else "System Default Intent")
                            else
                                (if (onCmdText.isNotBlank()) onCmdText else "Default hardware toggle"),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (config.isOverrideEnabled) {
                        Surface(
                            color = colorScheme.primary,
                            shape = CircleShape
                        ) {
                            Text(
                                text = "CUSTOM",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Switch(
                        checked = config.isOverrideEnabled,
                        onCheckedChange = { isChecked ->
                            onSaveConfig(config.copy(isOverrideEnabled = isChecked))
                        },
                        modifier = Modifier.testTag("setup_switch_override_${tile.id}")
                    )

                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(if (isExpanded) 180f else 0f)
                        )
                    }
                }
            }

            // Expanded Editor
            AnimatedVisibility(visible = isExpanded) {
                val inputFieldColors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (mode == PrivilegeMode.STANDARD) {
                        // Standard option: Intent input box
                        Text(
                            text = "STANDARD INTENT INPUT BOX",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary
                        )

                        TextField(
                            value = intentActionText,
                            onValueChange = {
                                intentActionText = it
                                onSaveConfig(
                                    config.copy(
                                        isOverrideEnabled = true,
                                        executionMode = CommandExecutionMode.STANDARD,
                                        customIntentAction = it,
                                        customIntentPackage = intentPkgText
                                    )
                                )
                            },
                            label = { Text("Intent Action", fontSize = 11.sp) },
                            placeholder = { Text(tile.defaultIntent, fontSize = 11.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = inputFieldColors,
                            modifier = Modifier.fillMaxWidth().testTag("setup_intent_action_${tile.id}")
                        )

                        TextField(
                            value = intentPkgText,
                            onValueChange = {
                                intentPkgText = it
                                onSaveConfig(
                                    config.copy(
                                        isOverrideEnabled = true,
                                        executionMode = CommandExecutionMode.STANDARD,
                                        customIntentAction = intentActionText,
                                        customIntentPackage = it
                                    )
                                )
                            },
                            label = { Text("Package Name (Optional)", fontSize = 11.sp) },
                            placeholder = { Text("e.g. com.android.settings", fontSize = 11.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = inputFieldColors,
                            modifier = Modifier.fillMaxWidth().testTag("setup_intent_pkg_${tile.id}")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onTestIntent(intentActionText, intentPkgText) },
                                shape = CircleShape,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Intent", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = onResetDefaults,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset", fontSize = 11.sp)
                            }
                        }
                    } else {
                        // Root / Shizuku option: ADB command input boxes (ON and OFF)
                        Text(
                            text = "${mode.title.uppercase()} ADB COMMAND INPUT BOXES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary
                        )

                        // ON Command
                        TextField(
                            value = onCmdText,
                            onValueChange = {
                                onCmdText = it
                                onSaveConfig(
                                    config.copy(
                                        isOverrideEnabled = true,
                                        executionMode = if (mode == PrivilegeMode.ROOT) CommandExecutionMode.ROOT else CommandExecutionMode.SHIZUKU,
                                        commandOn = it,
                                        commandOff = offCmdText
                                    )
                                )
                            },
                            label = { Text("ON ADB Command", fontSize = 11.sp) },
                            placeholder = { Text(tile.defaultOnCmd, fontSize = 11.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = inputFieldColors,
                            modifier = Modifier.fillMaxWidth().testTag("setup_cmd_on_${tile.id}")
                        )

                        // OFF Command
                        TextField(
                            value = offCmdText,
                            onValueChange = {
                                offCmdText = it
                                onSaveConfig(
                                    config.copy(
                                        isOverrideEnabled = true,
                                        executionMode = if (mode == PrivilegeMode.ROOT) CommandExecutionMode.ROOT else CommandExecutionMode.SHIZUKU,
                                        commandOn = onCmdText,
                                        commandOff = it
                                    )
                                )
                            },
                            label = { Text("OFF ADB Command", fontSize = 11.sp) },
                            placeholder = { Text(tile.defaultOffCmd, fontSize = 11.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = inputFieldColors,
                            modifier = Modifier.fillMaxWidth().testTag("setup_cmd_off_${tile.id}")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onTestCommand(onCmdText) },
                                shape = CircleShape,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test ON", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { onTestCommand(offCmdText) },
                                shape = CircleShape,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test OFF", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = onResetDefaults,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * STEP 4: Visibility tile setup
 */
@Composable
private fun TileVisibilityStep(viewModel: MainViewModel) {
    val hiddenTileIds by viewModel.hiddenTileIds.collectAsStateWithLifecycle()
    val allRawTiles by viewModel.allRawTiles.collectAsStateWithLifecycle()
    val isDark = LocalIsDarkTheme.current
    val cardBg = getSettingsCardBackgroundColor(isDark)
    val context = LocalContext.current

    val fallbackTiles = remember {
        listOf(
            SetupTileVisEntry("wifi", "Wi-Fi", Icons.Default.Wifi),
            SetupTileVisEntry("cellular", "Mobile Data", Icons.Default.SignalCellular4Bar),
            SetupTileVisEntry("bluetooth", "Bluetooth", Icons.Default.Bluetooth),
            SetupTileVisEntry("torch", "Flashlight", Icons.Default.FlashlightOn),
            SetupTileVisEntry("autorotate", "Auto-Rotate", Icons.Default.RotateRight),
            SetupTileVisEntry("hotspot", "Hotspot / Wi-Fi Tethering", Icons.Default.WifiTethering),
            SetupTileVisEntry("airplane", "Airplane Mode", Icons.Default.AirplanemodeActive),
            SetupTileVisEntry("location", "Location", Icons.Default.LocationOn),
            SetupTileVisEntry("nfc", "NFC", Icons.Default.Nfc),
            SetupTileVisEntry("battery_saver", "Battery Saver", Icons.Default.BatterySaver),
            SetupTileVisEntry("sound_profile", "Sound & Volume", Icons.Default.VolumeUp),
            SetupTileVisEntry("dark_mode", "Dark Theme", Icons.Default.DarkMode),
            SetupTileVisEntry("dnd", "Do Not Disturb", Icons.Default.NotificationsOff),
            SetupTileVisEntry("aod", "Always-On Display", Icons.Default.Wallpaper),
            SetupTileVisEntry("ambient_display", "Ambient Display", Icons.Default.Visibility),
            SetupTileVisEntry("caffeine", "Caffeine", Icons.Default.LocalCafe),
            SetupTileVisEntry("heads_up", "Heads Up", Icons.Default.NotificationsActive),
            SetupTileVisEntry("screenshot", "Screenshot", Icons.Default.CameraAlt),
            SetupTileVisEntry("screenrecord", "Screen Record", Icons.Default.Videocam)
        )
    }

    val displayTiles = remember(allRawTiles) {
        if (allRawTiles.isNotEmpty()) {
            allRawTiles.map { tile ->
                SetupTileVisEntry(
                    id = tile.id,
                    label = tile.label,
                    icon = tile.iconVector ?: when (tile.id) {
                        "wifi" -> Icons.Default.Wifi
                        "cellular" -> Icons.Default.SignalCellular4Bar
                        "bluetooth" -> Icons.Default.Bluetooth
                        "torch" -> Icons.Default.FlashlightOn
                        "autorotate" -> Icons.Default.RotateRight
                        "hotspot" -> Icons.Default.WifiTethering
                        "airplane" -> Icons.Default.AirplanemodeActive
                        "location" -> Icons.Default.LocationOn
                        "nfc" -> Icons.Default.Nfc
                        "battery_saver" -> Icons.Default.BatterySaver
                        "sound_profile", "sound_mode", "volume" -> Icons.Default.VolumeUp
                        "dark_mode" -> Icons.Default.DarkMode
                        "dnd" -> Icons.Default.NotificationsOff
                        "aod" -> Icons.Default.Wallpaper
                        "ambient_display" -> Icons.Default.Visibility
                        "caffeine" -> Icons.Default.LocalCafe
                        "heads_up" -> Icons.Default.NotificationsActive
                        "screenshot" -> Icons.Default.CameraAlt
                        "screenrecord" -> Icons.Default.Videocam
                        else -> Icons.Default.Tune
                    }
                )
            }
        } else {
            fallbackTiles
        }
    }

    SetupStepHeader(
        icon = Icons.Default.Visibility,
        title = "Tile Visibility Setup",
        subtitle = "Choose which Quick Settings tiles appear in your panel. Unsupported tiles can be auto-hidden."
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Smart Hide Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "OPTIMIZE VISIBILITY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "If running in Standard mode, automatically hide advanced tiles that require ADB/Root so you only see fully working tiles.",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FilledTonalButton(
                    onClick = {
                        viewModel.applySmartHideUnsupportedTiles()
                        Toast.makeText(context, "Unsupported tiles hidden for current mode", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("setup_smart_hide_btn")
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Smart Hide Unsupported Tiles", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Essential Tiles Toggle List
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "PRIMARY TILES VISIBILITY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                displayTiles.forEach { tile ->
                    val rawTile = allRawTiles.find { it.id == tile.id }
                    val isHidden = tile.id in hiddenTileIds ||
                        (rawTile != null && !rawTile.isVisible) ||
                        (if (tile.id == "sound_profile" || tile.id == "sound_mode" || tile.id == "volume")
                            "sound_mode" in hiddenTileIds || "sound_profile" in hiddenTileIds || "volume" in hiddenTileIds
                         else false)
                    val isVisible = !isHidden

                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tile.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = tile.label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Switch(
                            checked = isVisible,
                            onCheckedChange = { visible ->
                                viewModel.setTileHidden(tile.id, !visible)
                            },
                            modifier = Modifier.testTag("setup_tile_vis_${tile.id}")
                        )
                    }
                }
            }
        }
    }
}

private data class SetupTileVisEntry(
    val id: String,
    val label: String,
    val icon: ImageVector
)

/**
 * STEP 5: Trigger & Shade Toggles
 * Specific toggles requested:
 * - Stock shade override
 * - Panel replacement delay
 * - Invisible status bar trigger
 * - Use auto system panel closing (if the main variant doesn't work)
 */
@Composable
private fun TriggerTogglesStep() {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val cardBg = getSettingsCardBackgroundColor(isDark)

    val overrideSystemQs by PanelPreferences.overrideSystemQs.collectAsState()
    val panelReplacementDelayMs by PanelPreferences.panelReplacementDelayMs.collectAsState()
    val invisibleStatusBarTrigger by PanelPreferences.invisibleStatusBarTrigger.collectAsState()
    val autoSystemPanelClosing by PanelPreferences.autoSystemPanelClosing.collectAsState()

    SetupStepHeader(
        icon = Icons.Default.Layers,
        title = "Trigger & Shade Toggles",
        subtitle = "Fine-tune how QuickPanel activates and overrides the stock notification shade."
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Toggle 1: Stock shade override
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Stock shade override",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Auto-detect when the stock system notification shade opens to substitute it with the custom panel.",
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Switch(
                    checked = overrideSystemQs,
                    onCheckedChange = { PanelPreferences.setOverrideSystemQs(context, it) },
                    modifier = Modifier.testTag("setup_toggle_override_shade")
                )
            }
        }

        // Toggle 2: Panel replacement delay
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Panel replacement delay",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "${panelReplacementDelayMs}ms",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "Delay applied before opening the custom panel over the stock shade (0ms - 500ms).",
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Panel replacement delay buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val delayPresets = listOf(0, 50, 100, 200)
                    delayPresets.forEachIndexed { index, ms ->
                        val isSelected = panelReplacementDelayMs == ms
                        val shape = if (isSelected) {
                            CircleShape
                        } else {
                            when (index) {
                                0 -> RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp, topEnd = 8.dp, bottomEnd = 8.dp)
                                delayPresets.size - 1 -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = 50.dp, bottomEnd = 50.dp)
                                else -> RoundedCornerShape(8.dp)
                            }
                        }

                        Button(
                            onClick = { PanelPreferences.setPanelReplacementDelayMs(context, ms) },
                            shape = shape,
                            border = null,
                            elevation = null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("setup_delay_preset_$ms")
                        ) {
                            Text(
                                text = "${ms}ms",
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Toggle 3: Invisible status bar trigger
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Invisible status bar trigger",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Make the status bar trigger totally invisible while keeping swipe gestures active.",
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Switch(
                    checked = invisibleStatusBarTrigger,
                    onCheckedChange = { PanelPreferences.setInvisibleStatusBarTrigger(context, it) },
                    modifier = Modifier.testTag("setup_toggle_invisible_trigger")
                )
            }
        }

        // Toggle 4: Use auto system panel closing (if the main variant doesn't work)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Use auto system panel closing",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "A12-A15",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Use auto system panel closing (if the main variant doesn't work). Actively forces stock shade to collapse on Android 12 through 15.",
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Switch(
                    checked = autoSystemPanelClosing,
                    onCheckedChange = { PanelPreferences.setAutoSystemPanelClosing(context, it) },
                    modifier = Modifier.testTag("setup_toggle_auto_panel_closing")
                )
            }
        }
    }
}
