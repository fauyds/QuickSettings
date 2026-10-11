package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.NotificationActionModel
import com.example.model.NotificationItem
import com.example.service.QuickPanelAccessibilityService
import com.example.ui.customizer.TileCustomizerScreen
import com.example.ui.nativecontrols.NativeControlsScreen
import com.example.ui.onboarding.PrivilegeWizardScreen
import com.example.ui.overlay.QsExpansionStage
import com.example.ui.overlay.QuickSettingsPanel
import com.example.ui.scanner.ExternalTilesScreen
import com.example.ui.theme.DarkUiBackground
import com.example.ui.theme.DarkUiSurface
import com.example.ui.theme.DarkUiSurfaceVariant
import com.example.ui.theme.ExpressiveCardShapes
import com.example.ui.theme.QuickPanelTheme
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.getSettingsScreenBackgroundColor
import com.example.ui.theme.MonetOnPrimary
import com.example.ui.theme.MonetPrimary
import com.example.ui.theme.MonetTertiary

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    val tiles by viewModel.tiles.collectAsStateWithLifecycle()
    val expansionStage by viewModel.expansionStage.collectAsStateWithLifecycle()
    val isOverlayEnabled by viewModel.isOverlayEnabled.collectAsStateWithLifecycle()
    val isOverlayRunning by viewModel.isOverlayServiceRunning.collectAsStateWithLifecycle()
    val qsGridLayout by viewModel.qsGridLayout.collectAsStateWithLifecycle()

    // Native state collections
    val isWifi by viewModel.nativeTileManager.isWifiEnabled.collectAsStateWithLifecycle()
    val isBt by viewModel.nativeTileManager.isBluetoothEnabled.collectAsStateWithLifecycle()
    val isCellular by viewModel.nativeTileManager.isCellularEnabled.collectAsStateWithLifecycle()
    val isTorch by viewModel.nativeTileManager.isTorchEnabled.collectAsStateWithLifecycle()
    val isRotate by viewModel.nativeTileManager.isAutoRotateEnabled.collectAsStateWithLifecycle()
    val isDnd by viewModel.nativeTileManager.isDndEnabled.collectAsStateWithLifecycle()
    val isAirplane by viewModel.nativeTileManager.isAirplaneModeEnabled.collectAsStateWithLifecycle()
    val isLocation by viewModel.nativeTileManager.isLocationEnabled.collectAsStateWithLifecycle()
    val isHotspot by viewModel.nativeTileManager.isHotspotEnabled.collectAsStateWithLifecycle()
    val brightness by viewModel.nativeTileManager.brightness.collectAsStateWithLifecycle()
    val mediaVol by viewModel.nativeTileManager.mediaVolume.collectAsStateWithLifecycle()
    val ringVol by viewModel.nativeTileManager.ringVolume.collectAsStateWithLifecycle()
    val alarmVol by viewModel.nativeTileManager.alarmVolume.collectAsStateWithLifecycle()
    val notifVol by viewModel.nativeTileManager.notificationVolume.collectAsStateWithLifecycle()

    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val isNotifPermGranted = remember { viewModel.isNotificationPermissionGranted() }

    // External tiles
    val scannedTiles by viewModel.externalTileScanner.scannedTiles.collectAsStateWithLifecycle()
    val isScanning by viewModel.externalTileScanner.isScanning.collectAsStateWithLifecycle()

    // Privileges
    val currentMode by viewModel.privilegeEngine.currentMode.collectAsStateWithLifecycle()
    val isRootAvail by viewModel.privilegeEngine.isRootAvailable.collectAsStateWithLifecycle()
    val isShizukuAvail by viewModel.privilegeEngine.isShizukuAvailable.collectAsStateWithLifecycle()
    val lastLog by viewModel.privilegeEngine.lastExecutionLog.collectAsStateWithLifecycle()

    val hasOverlayPermission = viewModel.privilegeEngine.hasOverlayPermission()
    val isAccessibilityRunning = QuickPanelAccessibilityService.isRunning
    val showScreenRecordSheet by viewModel.tileRegistry.showScreenRecordSheet.collectAsStateWithLifecycle()

    if (showScreenRecordSheet) {
        com.example.ui.customizer.ScreenRecordBottomSheet(
            onDismissRequest = { viewModel.tileRegistry.showScreenRecordSheet.value = false }
        )
    }

    // Handle back button when in sub-tabs
    BackHandler(enabled = selectedTab != 0) {
        viewModel.setSelectedTab(0)
    }

    val isDarkSettings = LocalIsDarkTheme.current
    val settingsScreenBackgroundColor = com.example.ui.theme.getSettingsScreenBackgroundColor(isDarkSettings)

    Scaffold(
        containerColor = settingsScreenBackgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .border(
                        0.5.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("bottom_nav_bar"),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // All navigation items (Tile Studio and Hardware tabs retained in code and routing, hidden from navigation bar)
                val showTileStudioInNavBar = false
                val showHardwareInNavBar = false
                val allNavItems = listOf(
                    Triple("Overlay", Icons.Default.Dashboard, 0),
                    Triple("App Status", Icons.Default.Info, 9),
                    Triple("Tile Studio", Icons.Default.Tune, 1),
                    Triple("Tile Commands", Icons.Default.Terminal, 6),
                    Triple("Visibility", Icons.Default.Visibility, 7),
                    Triple("Hardware", Icons.Default.Build, 2),
                    Triple("Privileges", Icons.Default.Security, 4),
                    Triple("Customize", Icons.Default.Layers, 5),
                    Triple("Notif Hub", Icons.Default.NotificationsOff, 8)
                )
                val visibleNavItems = allNavItems.filter { item ->
                    when (item.third) {
                        1 -> showTileStudioInNavBar
                        2 -> showHardwareInNavBar
                        else -> true
                    }
                }

                visibleNavItems.forEach { (label, icon, index) ->
                    val isSelected = selectedTab == index
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) { viewModel.setSelectedTab(index) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("nav_tab_$index"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Screen content switcher
            when (selectedTab) {
                0 -> {
                    // TAB 0: Interactive QS Overlay Panel with dark tint below the panel in preview screen only
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.55f))
                    ) {
                        QuickSettingsPanel(
                            expansionStage = expansionStage,
                            onStageChange = { viewModel.setExpansionStage(it) },
                            tiles = tiles,
                            gridLayout = qsGridLayout,
                            onTileClick = { viewModel.handleTileClick(it) },
                            onTileLongClick = { viewModel.handleTileLongClick(it) },
                            brightness = brightness,
                            onBrightnessChange = { viewModel.setBrightness(it) },
                            mediaVolume = mediaVol,
                            ringVolume = ringVol,
                            alarmVolume = alarmVol,
                            notificationVolume = notifVol,
                            onVolumeChange = { stream, fraction -> viewModel.setStreamVolume(stream, fraction) },
                            notifications = notifications,
                            isNotificationPermissionGranted = isNotifPermGranted,
                            onNotificationClick = { viewModel.launchNotification(it) },
                            onNotificationDismiss = { viewModel.dismissNotification(it.key) },
                            onNotificationActionClick = { viewModel.executeNotificationAction(it) },
                            onDismissAllNotifications = { viewModel.dismissAllNotifications() },
                            onRequestNotificationAccess = { viewModel.openNotificationListenerSettings() },
                            onNavigateToCustomizer = { viewModel.setSelectedTab(1) },
                            onNavigateToNotificationHub = { viewModel.setSelectedTab(8) }
                        )
                    }
                }

                1 -> {
                    val qqsTileSize by viewModel.qqsTileSize.collectAsStateWithLifecycle()
                    val qqsTileCount by viewModel.qqsTileCount.collectAsStateWithLifecycle()
                    val editButtonPosition by viewModel.editButtonPosition.collectAsStateWithLifecycle()
                    val showDateInQqsHeader by viewModel.showDateInQqsHeader.collectAsStateWithLifecycle()
                    // TAB 1: Tile Customizer / Reordering Screen
                    TileCustomizerScreen(
                        tiles = tiles,
                        selectedGridLayout = qsGridLayout,
                        onSelectGridLayout = { viewModel.setQsGridLayout(it) },
                        qqsTileSize = qqsTileSize,
                        onSelectQqsTileSize = { viewModel.setQqsTileSize(it) },
                        qqsTileCount = qqsTileCount,
                        onSelectQqsTileCount = { viewModel.setQqsTileCount(it) },
                        editButtonPosition = editButtonPosition,
                        onSelectEditButtonPosition = { viewModel.setEditButtonPosition(it) },
                        showDateInQqsHeader = showDateInQqsHeader,
                        onToggleShowDateInQqsHeader = { viewModel.setShowDateInQqsHeader(it) },
                        onToggleVisibility = { id, visible -> viewModel.toggleTileVisibility(id, visible) },
                        onMoveTile = { from, to -> viewModel.moveTile(from, to) },
                        onNavigateBack = { viewModel.setSelectedTab(0) }
                    )
                }

                2 -> {
                    // TAB 2: Native Controls & Hardware Observers
                    NativeControlsScreen(
                        nativeManager = viewModel.nativeTileManager,
                        isWifi = isWifi,
                        isBt = isBt,
                        isCellular = isCellular,
                        isTorch = isTorch,
                        isRotate = isRotate,
                        isDnd = isDnd,
                        isAirplane = isAirplane,
                        isLocation = isLocation,
                        isHotspot = isHotspot,
                        brightness = brightness,
                        mediaVolume = mediaVol,
                        ringVolume = ringVol,
                        alarmVolume = alarmVol,
                        notifVolume = notifVol
                    )
                }

                4 -> {
                    // TAB 4: Privilege Engine Setup Wizard
                    PrivilegeWizardScreen(
                        privilegeEngine = viewModel.privilegeEngine,
                        currentMode = currentMode,
                        isRootAvailable = isRootAvail,
                        isShizukuAvailable = isShizukuAvail,
                        lastLog = lastLog,
                        onModeSelected = { viewModel.setPrivilegeMode(it) }
                    )
                }

                5 -> {
                    // TAB 5: Customize Screen
                    com.example.ui.customizer.CustomizeScreen()
                }

                6 -> {
                    // TAB 6: Tile Commands Customization
                    com.example.ui.customizer.TileCommandsScreen(privilegeEngine = viewModel.privilegeEngine)
                }

                7 -> {
                    // TAB 7: Tile Visibility & Blacklist Manager
                    com.example.ui.customizer.TileVisibilityScreen(viewModel = viewModel)
                }

                8 -> {
                    // TAB 8: Hidden Notification Manager Screen
                    com.example.ui.customizer.HiddenNotificationManagerScreen()
                }

                9 -> {
                    // TAB 9: App Status Screen (with recently edited TopTriggerStripBar element)
                    com.example.ui.status.AppStatusScreen(
                        isOverlayEnabled = isOverlayEnabled,
                        isOverlayRunning = isOverlayRunning,
                        hasOverlayPermission = hasOverlayPermission,
                        isAccessibilityRunning = isAccessibilityRunning,
                        currentMode = currentMode,
                        isRootAvailable = isRootAvail,
                        isShizukuAvailable = isShizukuAvail,
                        tilesCount = tiles.size,
                        notificationsCount = notifications.size,
                        onToggleOverlay = { viewModel.toggleOverlayService(it) },
                        onTestFloatingOverlay = { viewModel.testFloatingOverlayWindow() },
                        onRequestPermission = { viewModel.privilegeEngine.requestOverlayPermission() },
                        onOpenAccessibilitySettings = { viewModel.privilegeEngine.openAccessibilitySettings() },
                        onSimulateSwipeDown = {
                            if (expansionStage == QsExpansionStage.COLLAPSED) {
                                viewModel.setExpansionStage(QsExpansionStage.QQS)
                            } else if (expansionStage == QsExpansionStage.QQS) {
                                viewModel.setExpansionStage(QsExpansionStage.FULL_QS)
                            }
                        }
                    )
                }
            }
        }
    }
}
