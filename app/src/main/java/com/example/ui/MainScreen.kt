package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
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

    val tiles by viewModel.allRawTiles.collectAsStateWithLifecycle()
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

    val hasOverlayPermission by viewModel.privilegeEngine.hasOverlay.collectAsStateWithLifecycle()
    val isAccessibilityRunning by viewModel.privilegeEngine.hasAccessibility.collectAsStateWithLifecycle()
    val showScreenRecordSheet by viewModel.tileRegistry.showScreenRecordSheet.collectAsStateWithLifecycle()

    if (showScreenRecordSheet) {
        com.example.ui.customizer.ScreenRecordBottomSheet(
            onDismissRequest = { viewModel.tileRegistry.showScreenRecordSheet.value = false }
        )
    }

    val showInitialSetup by viewModel.showInitialSetup.collectAsStateWithLifecycle()
    if (showInitialSetup) {
        com.example.ui.onboarding.InitialSetupScreen(
            viewModel = viewModel,
            onComplete = { viewModel.completeInitialSetup() },
            onDismiss = { viewModel.dismissInitialSetup() }
        )
        return
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
            SettingsBottomNavigationBar(
                selectedTab = selectedTab,
                isOverlayRunning = isOverlayRunning,
                isRootAvail = isRootAvail,
                isShizukuAvail = isShizukuAvail,
                onTabSelected = { viewModel.setSelectedTab(it) }
            )
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
                    val isInitialSetupCompleted by com.example.data.PanelPreferences.initialSetupCompleted.collectAsStateWithLifecycle()
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
                        },
                        onLaunchInitialSetup = { viewModel.openInitialSetup() },
                        isInitialSetupDone = isInitialSetupCompleted
                    )
                }

                10 -> {
                    // TAB 10: About Screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        com.example.ui.customizer.AboutSection()
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsBottomNavigationBar(
    selectedTab: Int,
    isOverlayRunning: Boolean,
    isRootAvail: Boolean,
    isShizukuAvail: Boolean,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val scrollState = rememberScrollState()

    // All navigation items (Tile Studio and Hardware tabs retained in routing, hidden from navigation bar)
    val showTileStudioInNavBar = false
    val showHardwareInNavBar = false
    val allNavItems = listOf(
        Triple("Preview", Icons.Default.Dashboard, 0),
        Triple("App Status", Icons.Default.Speed, 9),
        Triple("Tile Studio", Icons.Default.Tune, 1),
        Triple("Tile Commands", Icons.Default.Terminal, 6),
        Triple("Visibility", Icons.Default.Visibility, 7),
        Triple("Hardware", Icons.Default.Build, 2),
        Triple("Privileges", Icons.Default.Security, 4),
        Triple("Customize", Icons.Default.Layers, 5),
        Triple("Notif Hub", Icons.Default.Notifications, 8),
        Triple("About", Icons.Default.Info, 10)
    )
    val visibleNavItems = remember {
        allNavItems.filter { item ->
            when (item.third) {
                1 -> showTileStudioInNavBar
                2 -> showHardwareInNavBar
                else -> true
            }
        }
    }

    LaunchedEffect(selectedTab) {
        val targetIndex = visibleNavItems.indexOfFirst { it.third == selectedTab }
        if (targetIndex >= 0) {
            val itemWidthPx = with(density) { 76.dp.toPx() }
            val screenWidthPx = with(density) { 360.dp.toPx() }
            val targetScroll = (targetIndex * itemWidthPx - (screenWidthPx / 2f - itemWidthPx / 2f)).toInt().coerceAtLeast(0)
            scrollState.animateScrollTo(
                targetScroll,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bottom_nav_bar"),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                visibleNavItems.forEach { (label, icon, index) ->
                    val isSelected = selectedTab == index

                    val pillBgColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "pill_bg_$index"
                    )
                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "icon_tint_$index"
                    )
                    val labelColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "label_color_$index"
                    )
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.0f else 0.94f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "scale_$index"
                    )

                    Column(
                        modifier = Modifier
                            .height(56.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = MaterialTheme.colorScheme.primary)
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onTabSelected(index)
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .testTag("nav_tab_$index"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(pillBgColor)
                                .padding(horizontal = if (isSelected) 14.dp else 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            BadgedBox(
                                badge = {
                                    if (index == 4 && (isRootAvail || isShizukuAvail)) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = iconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = labelColor,
                            maxLines = 1,
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

            // Left gradient fade hint for scrollable overflow
            AnimatedVisibility(
                visible = scrollState.canScrollBackward,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceContainer,
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Right gradient fade hint for scrollable overflow
            AnimatedVisibility(
                visible = scrollState.canScrollForward,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surfaceContainer
                                )
                            )
                        )
                )
            }
        }
    }
}
