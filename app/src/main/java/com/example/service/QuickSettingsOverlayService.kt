package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.animation.ValueAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.PanelPreferences
import com.example.data.local.QuickPanelDatabase
import com.example.data.repository.TileRepository
import com.example.system.ExternalTileScanner
import com.example.system.NativeTileManager
import com.example.system.PrivilegeEngine
import com.example.system.TileRegistry
import com.example.ui.overlay.QsExpansionStage
import com.example.ui.overlay.QuickSettingsPanel
import com.example.ui.theme.QuickPanelTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class QuickSettingsOverlayService : Service() {

    companion object {
        const val CHANNEL_ID = "quickpanel_overlay_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START_OVERLAY"
        const val ACTION_STOP = "ACTION_STOP_OVERLAY"
        const val ACTION_TOGGLE_EXPAND = "ACTION_TOGGLE_EXPAND"

        var instance: QuickSettingsOverlayService? = null
            private set

        var isRunning: Boolean = false
            private set
    }

    val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var windowManager: WindowManager? = null

    // Views
    private var triggerContainerView: TriggerContainerLayout? = null
    private var triggerStripView: View? = null
    private var triggerLayoutParams: WindowManager.LayoutParams? = null
    private var overlayComposeView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null

    // System Modules
    lateinit var privilegeEngine: PrivilegeEngine
    private lateinit var nativeTileManager: NativeTileManager
    private lateinit var externalTileScanner: ExternalTileScanner
    private lateinit var tileRegistry: TileRegistry
    private var immersiveModeDetector: com.example.system.ImmersiveModeDetector? = null

    private val expansionStage = MutableStateFlow(QsExpansionStage.COLLAPSED)
    var isPanelShowing = false
        private set

    val pullDownOffset = MutableStateFlow<Float?>(null)

    fun updatePullDownOffset(offset: Float?) {
        pullDownOffset.value = offset
    }

    fun onPullDownReleased(finalDiffY: Float, isFlick: Boolean) {
        pullDownOffset.value = null
    }

    // Callback to dismiss panel smoothly with animation
    private var dismissAnimatedCallback: (() -> Unit)? = null

    private val volumeStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (isPanelShowing) {
                hideOverlayPanelSmoothly()
                if (PanelPreferences.enableCustomVolumePanel.value) {
                    context?.let { VolumePanelOverlay.show(it) }
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY)
            com.example.system.CpuPerformanceManager.getInstance(this).acquireUrgentPriority()
        } catch (e: Exception) {
            Log.e("OverlayService", "Failed to set urgent display thread priority: ${e.message}")
        }
        instance = this
        isRunning = true
        PanelPreferences.init(this)
        MediaSessionManagerHelper.init(this)
        com.example.system.SystemThemeObserver.init(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        createNotificationChannel()

        // Initialize engines
        PanelPreferences.init(this)
        val db = QuickPanelDatabase.getInstance(this)
        val repo = TileRepository(db.tileDao())
        privilegeEngine = PrivilegeEngine(this)
        nativeTileManager = NativeTileManager(this, privilegeEngine, serviceScope)
        externalTileScanner = ExternalTileScanner(this)
        tileRegistry = TileRegistry(this, repo, nativeTileManager, externalTileScanner, serviceScope)

        // Initialize Immersive Mode Detector via Shizuku / Root shell listeners
        immersiveModeDetector = com.example.system.ImmersiveModeDetector(privilegeEngine, serviceScope)
        immersiveModeDetector?.startMonitoring { isImmersive ->
            updateTriggerVisibility(isImmersive)
        }

        serviceScope.launch {
            PanelPreferences.overrideSystemQs.collect {
                updateTriggerVisibility(isImmersiveModeActive())
            }
        }

        val filter = IntentFilter().apply {
            addAction("android.media.VOLUME_CHANGED_ACTION")
            addAction("android.media.STREAM_DEVICES_CHANGED_ACTION")
            addAction("android.media.RINGER_MODE_CHANGED")
        }
        registerReceiver(volumeStateReceiver, filter)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        com.example.system.SystemThemeObserver.onConfigurationChanged(this, newConfig)
        immersiveModeDetector?.checkNow()
        val immersive = isImmersiveModeActive()
        updateTriggerVisibility(immersive)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (intent?.action == ACTION_TOGGLE_EXPAND) {
            if (isPanelShowing) {
                hideOverlayPanelSmoothly()
            } else {
                showOverlayPanel(QsExpansionStage.FULL_QS)
            }
            return START_STICKY
        }

        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        isRunning = true

        if (Settings.canDrawOverlays(this)) {
            attachTriggerStrip()
        } else {
            Log.w("OverlayService", "SYSTEM_ALERT_WINDOW permission not granted.")
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "QuickPanel Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps custom status bar swipe overlay active."
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val toggleIntent = Intent(this, QuickSettingsOverlayService::class.java).apply {
            action = ACTION_TOGGLE_EXPAND
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            toggleIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("QuickPanel Active")
            .setContentText("Swipe down from top status bar to open Quick Settings")
            .setSmallIcon(android.R.drawable.ic_menu_preferences)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_agenda, "Open QS Overlay", togglePendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /**
     * Attach the visible trigger strip overlapping the system status bar.
     * Matches the exact thickness of the system status bar, stretching from side to side (edge-to-edge).
     */
    private fun attachTriggerStrip() {
        if (triggerContainerView != null) return

        val density = resources.displayMetrics.density
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        val rawStatusBarHeight = if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0

        // Exact thickness of the system status bar (with safe fallback to 28dp)
        val triggerHeightPx = if (rawStatusBarHeight > 0) rawStatusBarHeight else (28 * density).toInt()

        val layoutType = if (QuickPanelAccessibilityService.isRunning) {
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            triggerHeightPx,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.FILL_HORIZONTAL
            x = 0
            y = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        }

        val container = TriggerContainerLayout(this)
        val strip = StatusTriggerView(this).apply {
            visibility = if (isImmersiveModeActive() || (QuickPanelAccessibilityService.isRunning && PanelPreferences.overrideSystemQs.value)) View.GONE else View.VISIBLE
            onSwipeDownDetected = { _ ->
                showOverlayPanel(QsExpansionStage.QQS)
            }
        }
        container.setChildTriggerView(strip)
        container.onSwipeDownToOpen = { _ ->
            showOverlayPanel(QsExpansionStage.QQS)
        }

        try {
            windowManager?.addView(container, params)
            triggerContainerView = container
            triggerStripView = strip
            triggerLayoutParams = params
            Log.d("OverlayService", "Transparent container with status bar trigger strip attached side-to-side.")
        } catch (e: Exception) {
            Log.e("OverlayService", "Failed to add trigger container: ${e.message}")
        }
    }

    /**
     * Updates trigger visibility dynamically when Immersive Mode state changes.
     * ACTIVE -> View.GONE
     * INACTIVE -> View.VISIBLE
     */
    fun updateTriggerVisibility(isImmersive: Boolean) {
        Handler(Looper.getMainLooper()).post {
            // If QuickPanelAccessibilityService is running and overrideSystemQs is enabled,
            // let AccessibilityService manage the sole trigger strip to avoid duplicate triggers!
            if (QuickPanelAccessibilityService.isRunning && PanelPreferences.overrideSystemQs.value) {
                triggerStripView?.visibility = View.GONE
                return@post
            }

            triggerContainerView?.updateChildVisibility(isImmersive)
        }
    }

    /**
     * Show the floating custom Quick Settings overlay window with smooth finger-tracking animations.
     */
    fun showOverlayPanel(initialStage: QsExpansionStage) {
        if (isPanelShowing && overlayComposeView != null) {
            return
        }

        triggerContainerView?.cancelTransientReveal()
        QuickPanelAccessibilityService.collapseStockNotificationShade()
        serviceScope.launch {
            delay(60)
            QuickPanelAccessibilityService.collapseStockNotificationShade()
            delay(100)
            QuickPanelAccessibilityService.collapseStockNotificationShade()
        }
        expansionStage.value = initialStage
        nativeTileManager.refreshAllStates()

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val panelParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
            PixelFormat.TRANSLUCENT
        ).apply {
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        }

        val owner = OverlayLifecycleOwner()
        owner.onCreate()
        lifecycleOwner = owner

        val composeView = ComposeView(this).apply {
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            owner.attachTo(this)
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()
            setOnKeyListener { _, keyCode, event ->
                if (com.example.data.PanelPreferences.enableCustomVolumePanel.value &&
                    (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP ||
                     keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN ||
                     keyCode == android.view.KeyEvent.KEYCODE_VOLUME_MUTE)
                ) {
                    if (event.action == android.view.KeyEvent.ACTION_DOWN) {
                        hideOverlayPanelSmoothly()
                        when (keyCode) {
                            android.view.KeyEvent.KEYCODE_VOLUME_UP -> VolumePanelOverlay.onVolumeKey(this@QuickSettingsOverlayService, isUp = true)
                            android.view.KeyEvent.KEYCODE_VOLUME_DOWN -> VolumePanelOverlay.onVolumeKey(this@QuickSettingsOverlayService, isUp = false)
                            android.view.KeyEvent.KEYCODE_VOLUME_MUTE -> VolumePanelOverlay.onVolumeMuteKey(this@QuickSettingsOverlayService)
                        }
                    }
                    false
                } else {
                    false
                }
            }
            setContent {
                QuickPanelTheme {
                    val coroutineScope = rememberCoroutineScope()
                    val density = androidx.compose.ui.platform.LocalDensity.current
                    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
                    val initialOffsetY = with(density) { (-200).dp.toPx() }
                    val revealDistancePx = with(density) { 240.dp.toPx() }

                    val animSpeedScale by PanelPreferences.animationSpeedScale.collectAsState()
                    val disablePanelAnimations by PanelPreferences.disablePanelAnimations.collectAsState()
                    val speedFactor = animSpeedScale.coerceAtLeast(0.01f)

                    val backdropAlpha = remember { androidx.compose.animation.core.Animatable(if (disablePanelAnimations) 0.50f else 0f) }
                    val panelOffsetY = remember { androidx.compose.animation.core.Animatable(if (disablePanelAnimations) 0f else initialOffsetY) }
                    val panelAlpha = remember { androidx.compose.animation.core.Animatable(if (disablePanelAnimations) 1f else 0f) }
                    val panelScale = remember { androidx.compose.animation.core.Animatable(if (disablePanelAnimations) 1f else 0.95f) }

                    var isClosing by remember { mutableStateOf(false) }
                    var wasPulling by remember { mutableStateOf(false) }
                    val pullOffset by pullDownOffset.collectAsState()

                    // Live finger tracking for pull-down gesture from status bar
                    LaunchedEffect(pullOffset) {
                        val currentPull = pullOffset
                        if (currentPull != null) {
                            wasPulling = true
                            val fraction = (currentPull / revealDistancePx).coerceIn(0f, 1f)
                            val targetY = initialOffsetY * (1f - fraction)
                            panelOffsetY.snapTo(targetY)
                            backdropAlpha.snapTo(0.50f * fraction)
                            panelAlpha.snapTo(0.3f + (0.7f * fraction))
                            panelScale.snapTo(0.95f + (0.05f * fraction))
                        } else if (wasPulling && !isClosing) {
                            wasPulling = false
                            // Finger released: snap open if dragged sufficiently, else dismiss
                            if (disablePanelAnimations) {
                                backdropAlpha.snapTo(0.50f)
                                panelOffsetY.snapTo(0f)
                                panelAlpha.snapTo(1f)
                                panelScale.snapTo(1f)
                            } else {
                                if (backdropAlpha.value >= 0.12f || panelOffsetY.value >= (initialOffsetY * 0.75f)) {
                                    launch {
                                        backdropAlpha.animateTo(
                                            targetValue = 0.50f,
                                            animationSpec = androidx.compose.animation.core.tween(
                                                durationMillis = (240 * speedFactor).toInt(),
                                                easing = androidx.compose.animation.core.FastOutSlowInEasing
                                            )
                                        )
                                    }
                                    launch {
                                        panelOffsetY.animateTo(
                                            targetValue = 0f,
                                            animationSpec = androidx.compose.animation.core.spring(
                                                dampingRatio = 0.78f,
                                                stiffness = (340f / speedFactor.coerceAtLeast(0.5f))
                                            )
                                        )
                                    }
                                    launch {
                                        panelAlpha.animateTo(
                                            targetValue = 1f,
                                            animationSpec = androidx.compose.animation.core.tween(
                                                durationMillis = (180 * speedFactor).toInt(),
                                                easing = androidx.compose.animation.core.LinearOutSlowInEasing
                                            )
                                        )
                                    }
                                    launch {
                                        panelScale.animateTo(
                                            targetValue = 1f,
                                            animationSpec = androidx.compose.animation.core.spring(
                                                dampingRatio = 0.80f,
                                                stiffness = (360f / speedFactor.coerceAtLeast(0.5f))
                                            )
                                        )
                                    }
                                } else {
                                    // Settle back closed
                                    isClosing = true
                                    launch {
                                        backdropAlpha.animateTo(0f, androidx.compose.animation.core.tween(180))
                                    }
                                    launch {
                                        panelOffsetY.animateTo(-screenHeightPx * 0.75f, androidx.compose.animation.core.tween(200))
                                    }
                                    launch {
                                        panelAlpha.animateTo(0f, androidx.compose.animation.core.tween(150))
                                    }
                                    delay(200)
                                    destroyOverlayWindow()
                                }
                            }
                        }
                    }

                    // Standard initial reveal if not driven by continuous pullDownOffset
                    LaunchedEffect(Unit) {
                        if (pullDownOffset.value == null) {
                            if (disablePanelAnimations) {
                                backdropAlpha.snapTo(0.50f)
                                panelOffsetY.snapTo(0f)
                                panelAlpha.snapTo(1f)
                                panelScale.snapTo(1f)
                            } else {
                                launch {
                                    backdropAlpha.animateTo(
                                        targetValue = 0.50f,
                                        animationSpec = androidx.compose.animation.core.tween(
                                            durationMillis = (280 * speedFactor).toInt(),
                                            easing = androidx.compose.animation.core.FastOutSlowInEasing
                                        )
                                    )
                                }
                                launch {
                                    panelOffsetY.animateTo(
                                        targetValue = 0f,
                                        animationSpec = androidx.compose.animation.core.spring(
                                            dampingRatio = 0.78f,
                                            stiffness = (340f / speedFactor.coerceAtLeast(0.5f))
                                        )
                                    )
                                }
                                launch {
                                    panelAlpha.animateTo(
                                        targetValue = 1f,
                                        animationSpec = androidx.compose.animation.core.tween(
                                            durationMillis = (220 * speedFactor).toInt(),
                                            easing = androidx.compose.animation.core.LinearOutSlowInEasing
                                        )
                                    )
                                }
                                launch {
                                    panelScale.animateTo(
                                        targetValue = 1f,
                                        animationSpec = androidx.compose.animation.core.spring(
                                            dampingRatio = 0.80f,
                                            stiffness = (360f / speedFactor.coerceAtLeast(0.5f))
                                        )
                                    )
                                }
                            }
                        }
                    }

                    val startDismiss: () -> Unit = {
                        if (!isClosing) {
                            isClosing = true
                            if (disablePanelAnimations) {
                                destroyOverlayWindow()
                            } else {
                                coroutineScope.launch {
                                    val closeDuration = (220 * speedFactor).toInt()
                                    val fadeJob = launch {
                                        backdropAlpha.animateTo(
                                            targetValue = 0f,
                                            animationSpec = androidx.compose.animation.core.tween(
                                                durationMillis = closeDuration,
                                                easing = androidx.compose.animation.core.FastOutLinearInEasing
                                            )
                                        )
                                    }
                                    val slideJob = launch {
                                        panelOffsetY.animateTo(
                                            targetValue = -screenHeightPx * 0.75f,
                                            animationSpec = androidx.compose.animation.core.tween(
                                                durationMillis = closeDuration,
                                                easing = androidx.compose.animation.core.FastOutLinearInEasing
                                            )
                                        )
                                    }
                                    val alphaJob = launch {
                                        panelAlpha.animateTo(
                                            targetValue = 0f,
                                            animationSpec = androidx.compose.animation.core.tween(
                                                durationMillis = (closeDuration * 0.8f).toInt(),
                                                easing = androidx.compose.animation.core.FastOutLinearInEasing
                                            )
                                        )
                                    }
                                    val scaleJob = launch {
                                        panelScale.animateTo(
                                            targetValue = 0.95f,
                                            animationSpec = androidx.compose.animation.core.tween(
                                                durationMillis = closeDuration,
                                                easing = androidx.compose.animation.core.FastOutLinearInEasing
                                            )
                                        )
                                    }
                                    fadeJob.join()
                                    slideJob.join()
                                    alphaJob.join()
                                    scaleJob.join()
                                    destroyOverlayWindow()
                                }
                            }
                        }
                    }

                    dismissAnimatedCallback = {
                        startDismiss()
                    }

                    val stage by expansionStage.collectAsState()
                    val tiles by tileRegistry.allTiles.collectAsState()
                    val qsGridLayout by PanelPreferences.gridLayout.collectAsState()
                    val notifications by QuickPanelNotificationListenerService.notifications.collectAsState()
                    val isNotificationPermGranted = remember { QuickPanelNotificationListenerService.isPermissionGranted(this@QuickSettingsOverlayService) }
                    val brightness by nativeTileManager.brightness.collectAsState()
                    val mediaVol by nativeTileManager.mediaVolume.collectAsState()
                    val ringVol by nativeTileManager.ringVolume.collectAsState()
                    val alarmVol by nativeTileManager.alarmVolume.collectAsState()
                    val notifVol by nativeTileManager.notificationVolume.collectAsState()

                    // Stationary Full-Screen Dark Tinted Backdrop
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = backdropAlpha.value))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { startDismiss() }
                            )
                    ) {
                        // Animated Panel Content Container (Slides/springs on open and glide-up on close; panel & notifications stay freely movable)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .graphicsLayer {
                                    translationY = panelOffsetY.value
                                    alpha = panelAlpha.value
                                    scaleX = panelScale.value
                                    scaleY = panelScale.value
                                    clip = false
                                }
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { /* consume click inside panel */ }
                                )
                        ) {
                            QuickSettingsPanel(
                                expansionStage = stage,
                                onStageChange = { newStage ->
                                    if (newStage == QsExpansionStage.COLLAPSED) {
                                        startDismiss()
                                    } else {
                                        expansionStage.value = newStage
                                    }
                                },
                                    tiles = tiles,
                                    gridLayout = qsGridLayout,
                                    onTileClick = { tile ->
                                        if (tile.id == "sound_profile" || tile.id == "sound_mode" || tile.id == "volume") {
                                            VolumePanelOverlay.show(this@QuickSettingsOverlayService)
                                        } else {
                                            tileRegistry.handleTileClick(tile)
                                            val config = com.example.data.TileCommandRepository.getConfig(tile.id)
                                            if (tile.id == "screenrecord" || config.closePanelOnPress) {
                                                hideOverlayPanelSmoothly()
                                            }
                                        }
                                    },
                                    onTileLongClick = {
                                        if (it.id != "screenrecord") {
                                            hideOverlayPanelSmoothly()
                                        }
                                        tileRegistry.handleTileLongClick(it)
                                    },
                                    brightness = brightness,
                                    onBrightnessChange = { nativeTileManager.setBrightness(it) },
                                    mediaVolume = mediaVol,
                                    ringVolume = ringVol,
                                    alarmVolume = alarmVol,
                                    notificationVolume = notifVol,
                                    onVolumeChange = { stream, fraction ->
                                        nativeTileManager.setStreamVolume(stream, fraction)
                                    },
                                    notifications = notifications,
                                    isNotificationPermissionGranted = isNotificationPermGranted,
                                    onNotificationClick = { item ->
                                        hideOverlayPanelSmoothly()
                                        QuickPanelNotificationListenerService.launchNotification(item, this@QuickSettingsOverlayService)
                                    },
                                    onNotificationDismiss = { item ->
                                        QuickPanelNotificationListenerService.dismissNotification(item.key)
                                    },
                                    onNotificationActionClick = { action ->
                                        hideOverlayPanelSmoothly()
                                        QuickPanelNotificationListenerService.executeAction(action)
                                    },
                                    onDismissAllNotifications = {
                                        QuickPanelNotificationListenerService.dismissAllNotifications()
                                    },
                                    onRequestNotificationAccess = {
                                        hideOverlayPanelSmoothly()
                                        QuickPanelNotificationListenerService.openPermissionSettings(this@QuickSettingsOverlayService)
                                    },
                                    onNavigateToNotificationHub = {
                                        hideOverlayPanelSmoothly()
                                        val intent = Intent(this@QuickSettingsOverlayService, MainActivity::class.java).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                            putExtra("TARGET_TAB", 8)
                                        }
                                        startActivity(intent)
                                    },
                                    onNavigateToCustomizer = {
                                        hideOverlayPanelSmoothly()
                                        val intent = Intent(this@QuickSettingsOverlayService, MainActivity::class.java).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                        }
                                        startActivity(intent)
                                    }
                                )
                            }
                        }
                    }
                }
            }

        try {
            windowManager?.addView(composeView, panelParams)
            overlayComposeView = composeView
            isPanelShowing = true
            if (VolumePanelOverlay.isShowing) {
                VolumePanelOverlay.bringToFront(this)
            }
        } catch (e: Exception) {
            Log.e("OverlayService", "Failed to show overlay panel: ${e.message}")
        }
    }

    fun hideOverlayPanelSmoothly() {
        if (!isPanelShowing) return
        dismissAnimatedCallback?.invoke() ?: destroyOverlayWindow()
    }

    private fun destroyOverlayWindow() {
        overlayComposeView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                // Ignore
            }
        }
        overlayComposeView = null
        lifecycleOwner?.onDestroy()
        lifecycleOwner = null
        isPanelShowing = false
        expansionStage.value = QsExpansionStage.COLLAPSED
        dismissAnimatedCallback = null
    }

    fun isImmersiveModeActive(): Boolean {
        return QuickPanelAccessibilityService.instance?.isImmersiveModeActive()
            ?: (immersiveModeDetector?.isImmersiveModeActive?.value == true)
    }

    override fun onDestroy() {
        super.onDestroy()
        immersiveModeDetector?.stopMonitoring()
        immersiveModeDetector = null
        try {
            unregisterReceiver(volumeStateReceiver)
        } catch (e: Exception) {
            // Ignore
        }
        MediaSessionManagerHelper.unregisterAll()
        destroyOverlayWindow()
        triggerContainerView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                // Ignore
            }
        }
        triggerContainerView = null
        triggerStripView = null
        isRunning = false
        if (instance == this) {
            instance = null
        }
        serviceScope.cancel()
    }
}

/**
 * Custom Status Bar Trigger View
 * Spans from side-to-side across the entire status bar thickness,
 * visibly overlapping the system status bar with a sleek Material You bar,
 * side-to-side glowing bottom accent edge, and centered elongated pull pill.
 */
class StatusTriggerView(context: Context) : View(context) {
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bottomBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val centerPillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sideGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pillRect = RectF()
    private var isPressedState = false
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    var onSwipeDownDetected: ((isDeepSwipe: Boolean) -> Unit)? = null

    private var startY = 0f
    private var startX = 0f
    private var isDragging = false

    // Immersive Mode 2-Step Gesture State (Supports both Landscape and Portrait Immersive Mode)
    private val isLandscape: Boolean
        get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    fun isImmersiveMode(): Boolean {
        QuickSettingsOverlayService.instance?.let { service ->
            return service.isImmersiveModeActive()
        }
        return false
    }

    init {
        setWillNotDraw(false)
        scope.launch {
            PanelPreferences.invisibleStatusBarTrigger.collect {
                invalidate()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scope.cancel()
    }

    private fun setPressedState(pressed: Boolean) {
        if (isPressedState != pressed) {
            isPressedState = pressed
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (PanelPreferences.invisibleStatusBarTrigger.value) return
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        // 1. Simplified, highly transparent full-width trigger background without pill or outline
        backgroundPaint.shader = null
        backgroundPaint.color = if (isPressedState) 0x1A000000.toInt() else 0x0D000000.toInt()
        canvas.drawRect(0f, 0f, w, h, backgroundPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val density = resources.displayMetrics.density
        val immediateSwipeThresholdPx = (3 * density).toInt()
        val deepSwipeThresholdPx = (40 * density).toInt()

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startY = event.rawY
                startX = event.rawX
                isDragging = true
                setPressedState(true)
                QuickPanelAccessibilityService.collapseStockNotificationShade()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val diffY = event.rawY - startY
                val diffX = Math.abs(event.rawX - startX)

                if (isDragging && diffY >= immediateSwipeThresholdPx && diffY > (diffX * 0.3f)) {
                    val service = QuickSettingsOverlayService.instance
                    if (service?.isPanelShowing != true) {
                        service?.showOverlayPanel(if (diffY > deepSwipeThresholdPx) QsExpansionStage.FULL_QS else QsExpansionStage.QQS)
                    }
                    service?.updatePullDownOffset(diffY)
                    return true
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val diffY = event.rawY - startY
                val diffX = Math.abs(event.rawX - startX)
                if (isDragging) {
                    val service = QuickSettingsOverlayService.instance
                    if (service?.isPanelShowing == true) {
                        service.onPullDownReleased(diffY, isFlick = diffY >= (10 * density))
                    } else if (diffY >= (4 * density) && diffY > (diffX * 0.3f)) {
                        service?.showOverlayPanel(if (diffY > deepSwipeThresholdPx) QsExpansionStage.FULL_QS else QsExpansionStage.QQS)
                        onSwipeDownDetected?.invoke(diffY > deepSwipeThresholdPx)
                    }
                }
                setPressedState(false)
                isDragging = false
                startY = 0f
                startX = 0f
                return true
            }
        }
        return true
    }
}
