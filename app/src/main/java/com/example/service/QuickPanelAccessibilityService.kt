package com.example.service

import android.accessibilityservice.AccessibilityService
import android.animation.ValueAnimator
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.view.Display
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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
import com.example.MainActivity
import com.example.data.PanelPreferences
import com.example.data.local.QuickPanelDatabase
import com.example.data.repository.TileRepository
import com.example.system.ExternalTileScanner
import com.example.system.NativeTileManager
import com.example.system.PrivilegeEngine
import com.example.system.TileRegistry
import androidx.compose.runtime.CompositionLocalProvider
import com.example.ui.overlay.LiveScreenRefractionBackdrop
import com.example.ui.overlay.LocalOnBlurRegionBoundsChanged
import com.example.ui.overlay.LocalOnNotificationBlurRegionsChanged
import com.example.ui.overlay.LocalOnQsCardBoundsChanged
import com.example.ui.overlay.QsCardBlurBounds
import com.example.ui.overlay.QsMultiCardBlurWindowHost
import com.example.ui.overlay.QsExpansionStage
import com.example.ui.overlay.QuickSettingsPanel
import com.example.data.GlassBackdropMode
import com.example.ui.overlay.applyWindowBlurBehind
import com.example.ui.overlay.computeQsBlurWindowPlan
import com.example.ui.overlay.computeWindowBlurBehindRadiusPx
import com.example.ui.overlay.isAndroid16FrostedGlassSupported
import com.example.ui.overlay.isCrossWindowBlurSupported
import com.example.ui.overlay.isRuntimeShaderRefractionSupported
import com.example.ui.overlay.quantizeBlurRegionsForPerformance
import com.example.ui.overlay.rememberCrossWindowBlurSupported
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.QuickPanelTheme

/**
 * TrayDev Architecture: AccessibilityService-driven Window Overlay & Universal Tile Interoperability
 * - Manages TYPE_ACCESSIBILITY_OVERLAY windows directly with Z-index above SystemUI.
 * - Universal Accessibility Click Simulation: Scans active window hierarchy and clicks target stock tiles.
 * - Suppresses SystemUI stock shade via performGlobalAction(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE) in < 30ms.
 */
class QuickPanelAccessibilityService : AccessibilityService() {

    companion object {
        private const val CLIENT_TAG = "accessibility_service"
        var instance: QuickPanelAccessibilityService? = null
            private set

        val isRunning: Boolean
            get() = instance != null

        fun expandQuickSettings(): Boolean {
            return instance?.performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS) ?: false
        }

        fun expandNotifications(): Boolean {
            return instance?.performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS) ?: false
        }

        @Volatile
        private var lastPrivilegedShadeCollapseTime = 0L

        fun collapseStockNotificationShade(): Boolean {
            var result = false
            val useAutoClosing = PanelPreferences.autoSystemPanelClosing.value
            val now = System.currentTimeMillis()
            val allowPrivilegedCollapse = (now - lastPrivilegedShadeCollapseTime) >= 600L

            instance?.let { service ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    try {
                        result = service.performGlobalAction(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE)
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
                try {
                    @Suppress("DEPRECATION")
                    val closeIntent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
                    service.sendBroadcast(closeIntent)
                } catch (e: Exception) {
                    // Ignore
                }

                if (useAutoClosing) {
                    // Try reflection on StatusBarManager collapsePanels() for Android 12 to 15
                    try {
                        val sbm = service.getSystemService("statusbar")
                        val collapseMethod = sbm?.javaClass?.getMethod("collapsePanels")
                        collapseMethod?.invoke(sbm)
                    } catch (_: Exception) {
                        // Fallback reflection on collapse()
                        try {
                            val sbm = service.getSystemService("statusbar")
                            val collapseMethod = sbm?.javaClass?.getMethod("collapse")
                            collapseMethod?.invoke(sbm)
                        } catch (_: Exception) {}
                    }
                }
            }

            QuickSettingsOverlayService.instance?.let { overlayService ->
                if (instance == null) {
                    try {
                        @Suppress("DEPRECATION")
                        val closeIntent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
                        overlayService.sendBroadcast(closeIntent)
                    } catch (e: Exception) {
                        // Ignore
                    }

                    if (useAutoClosing) {
                        try {
                            val sbm = overlayService.getSystemService("statusbar")
                            val collapseMethod = sbm?.javaClass?.getMethod("collapsePanels")
                            collapseMethod?.invoke(sbm)
                        } catch (_: Exception) {
                            try {
                                val sbm = overlayService.getSystemService("statusbar")
                                val collapseMethod = sbm?.javaClass?.getMethod("collapse")
                                collapseMethod?.invoke(sbm)
                            } catch (_: Exception) {}
                        }
                    }
                }
            }

            if (allowPrivilegedCollapse && !result) {
                val activeService = instance
                val overlayService = QuickSettingsOverlayService.instance
                val engine = activeService?.privilegeEngine ?: overlayService?.privilegeEngine
                val scope = activeService?.serviceScope ?: overlayService?.serviceScope
                if (engine != null && scope != null) {
                    if (engine.isShizukuGranted()) {
                        lastPrivilegedShadeCollapseTime = now
                        scope.launch(Dispatchers.IO) {
                            engine.executeShizukuShell("cmd statusbar collapse; service call statusbar 2")
                        }
                    } else if (useAutoClosing && engine.isRootAvailable.value &&
                        engine.currentMode.value == com.example.model.PrivilegeMode.ROOT
                    ) {
                        lastPrivilegedShadeCollapseTime = now
                        scope.launch(Dispatchers.IO) {
                            engine.executeCommand("cmd statusbar collapse || service call statusbar 2")
                        }
                    }
                }
            }

            return result
        }

        fun pressBack(): Boolean {
            return instance?.performGlobalAction(GLOBAL_ACTION_BACK) ?: false
        }

        fun pressHome(): Boolean {
            return instance?.performGlobalAction(GLOBAL_ACTION_HOME) ?: false
        }

        fun openPowerDialog(): Boolean {
            return instance?.performGlobalAction(GLOBAL_ACTION_POWER_DIALOG) ?: false
        }

        fun showCustomPanel(stage: QsExpansionStage = QsExpansionStage.QQS) {
            instance?.showOverlayPanel(stage)
        }

        fun hideCustomPanel() {
            instance?.hideOverlayPanelSmoothly()
        }

        fun setInterceptionEnabled(enabled: Boolean) {
            instance?.setOverlayInterception(enabled)
        }

        /**
         * Universal Accessibility Click Simulation:
         * Queries the active window node tree, finds the stock SystemUI tile node,
         * executes ACTION_CLICK, and immediately dismisses the stock notification shade.
         */
        fun simulateStockTileClick(
            tileLabel: String,
            keywords: List<String> = emptyList(),
            specifier: String? = null
        ): Boolean {
            return instance?.clickSystemUiTile(tileLabel, keywords, specifier) ?: false
        }

        internal fun isSystemUiShadeWindow(
            packageName: String?,
            title: CharSequence?,
            windowType: Int,
            boundsHeightPx: Int,
            screenHeightPx: Int,
            density: Float
        ): Boolean {
            if (windowType == AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY) return false
            val pkg = packageName?.lowercase()?.trim().orEmpty()
            val titleLower = title?.toString()?.lowercase()?.trim().orEmpty()
            val whitelist = PanelPreferences.stockShadeWhitelist.value
            val isSystemUiPkg = pkg.isNotEmpty() && (
                pkg == "com.android.systemui" ||
                    pkg.endsWith(".systemui") ||
                    whitelist.any { it.equals(pkg, ignoreCase = true) }
                )
            if (!isSystemUiPkg) return false
            if (titleLower.contains("volume")) return false

            val hasShadeTitle = titleLower.contains("notification shade") ||
                titleLower.contains("notificationshade") ||
                titleLower.contains("quick settings") ||
                titleLower.contains("quicksettings") ||
                titleLower.contains("control center") ||
                titleLower.contains("status_bar_expanded")
            val minExplicitTitleHeightPx = (80f * density).toInt()
            if (hasShadeTitle && boundsHeightPx > minExplicitTitleHeightPx) {
                return true
            }

            val minShadeHeightPx = if (screenHeightPx > 0) {
                maxOf((140f * density).toInt(), (screenHeightPx * 0.22f).toInt())
            } else {
                (140f * density).toInt()
            }
            return boundsHeightPx >= minShadeHeightPx
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var windowManager: WindowManager? = null

    // Window Views
    private var triggerContainerView: TriggerContainerLayout? = null
    private var triggerStripView: AccessibilityStatusTriggerView? = null
    private var triggerLayoutParams: WindowManager.LayoutParams? = null
    private var qsBlurWindowHost: QsMultiCardBlurWindowHost? = null
    private var overlayComposeView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null
    private var immersiveModeDetector: com.example.system.ImmersiveModeDetector? = null

    // System Modules
    lateinit var privilegeEngine: PrivilegeEngine
        private set
    lateinit var nativeTileManager: NativeTileManager
        private set
    lateinit var externalTileScanner: ExternalTileScanner
        private set
    lateinit var tileRegistry: TileRegistry
        private set

    private val expansionStage = MutableStateFlow(QsExpansionStage.COLLAPSED)
    var isPanelShowing = false
        private set
    private var isPendingShowOverlay = false

    private val _capturedBackdropBitmap = MutableStateFlow<android.graphics.Bitmap?>(null)
    val capturedBackdropBitmap: StateFlow<android.graphics.Bitmap?> = _capturedBackdropBitmap.asStateFlow()
    private var lastBackdropCaptureTimeMs = 0L
    private var isBackdropCaptureInFlight = false
    internal var captureGeneration = 0L
        private set
    private val mainHandler = Handler(Looper.getMainLooper())

    val pullDownOffset = MutableStateFlow<Float?>(null)

    fun updatePullDownOffset(offset: Float?) {
        pullDownOffset.value = offset
    }

    fun onPullDownReleased(finalDiffY: Float, isFlick: Boolean) {
        pullDownOffset.value = null
    }

    fun resetBackdropCaptureForNewGesture() {
        captureGeneration++
        isBackdropCaptureInFlight = false
        lastBackdropCaptureTimeMs = 0L
        triggerStripView?.alpha = 1f
        if (!isPanelShowing && !isPendingShowOverlay) {
            clearCapturedBackdropBitmap()
        }
    }

    internal fun isSystemUiShadeVisible(): Boolean {
        return try {
            val dm = resources.displayMetrics
            val screenH = dm.heightPixels
            val density = dm.density
            val currentWindows = windows ?: return false
            currentWindows.any { win ->
                val bounds = Rect()
                win.getBoundsInScreen(bounds)
                val root = try {
                    win.root
                } catch (_: Throwable) {
                    null
                }
                val pkg = try {
                    root?.packageName?.toString()
                } catch (_: Throwable) {
                    null
                } finally {
                    if (root != null) {
                        runCatching { root.recycle() }
                    }
                }
                isSystemUiShadeWindow(
                    packageName = pkg,
                    title = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) win.title else null,
                    windowType = win.type,
                    boundsHeightPx = bounds.height(),
                    screenHeightPx = screenH,
                    density = density
                )
            }
        } catch (_: Throwable) {
            false
        }
    }

    private fun captureFreshBackdropAndLaunch(initialStage: QsExpansionStage) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA ||
            !isAndroid16FrostedGlassSupported() ||
            !isRuntimeShaderRefractionSupported() ||
            !PanelPreferences.isLiquidGlassActive()
        ) {
            showOverlayPanelInternal(initialStage)
            return
        }

        isPendingShowOverlay = true
        val requestGen = ++captureGeneration
        collapseStockNotificationShade()
        triggerStripView?.alpha = 0f

        serviceScope.launch {
            var waitedMs = 0L
            val wasShadeVisibleInitially = isSystemUiShadeVisible()
            if (wasShadeVisibleInitially) {
                while (waitedMs < 160L && isSystemUiShadeVisible()) {
                    if (requestGen != captureGeneration || isPanelShowing || isLockedOrUnlocking()) {
                        triggerStripView?.alpha = 1f
                        isPendingShowOverlay = false
                        return@launch
                    }
                    delay(16L)
                    waitedMs += 16L
                }
                // Let SurfaceFlinger commit the frame without stock shade layers to avoid ghosting
                delay(24L)
            }

            if (requestGen != captureGeneration || isPanelShowing || isLockedOrUnlocking()) {
                triggerStripView?.alpha = 1f
                isPendingShowOverlay = false
                return@launch
            }

            val launched = java.util.concurrent.atomic.AtomicBoolean(false)
            val finishLaunch = Runnable {
                triggerStripView?.alpha = 1f
                if (launched.compareAndSet(false, true)) {
                    isPendingShowOverlay = false
                    if (requestGen == captureGeneration && !isLockedOrUnlocking()) {
                        showOverlayPanelInternal(initialStage)
                    }
                }
            }
            mainHandler.postDelayed(finishLaunch, 260L)
            takeBackdropScreenshotForGeneration(requestGen) {
                mainHandler.removeCallbacks(finishLaunch)
                finishLaunch.run()
            }
        }
    }

    private fun takeBackdropScreenshotForGeneration(
        requestGen: Long,
        onComplete: (() -> Unit)? = null
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA) {
            triggerStripView?.alpha = 1f
            onComplete?.invoke()
            return
        }
        isBackdropCaptureInFlight = true
        try {
            takeScreenshot(
                Display.DEFAULT_DISPLAY,
                mainExecutor,
                object : TakeScreenshotCallback {
                    override fun onSuccess(screenshot: ScreenshotResult) {
                        isBackdropCaptureInFlight = false
                        triggerStripView?.alpha = 1f
                        val hb = screenshot.hardwareBuffer
                        val wrapped = try {
                            android.graphics.Bitmap.wrapHardwareBuffer(hb, screenshot.colorSpace)
                        } catch (_: Throwable) {
                            null
                        } finally {
                            try {
                                hb.close()
                            } catch (_: Throwable) {}
                        }
                        if (wrapped != null && requestGen == captureGeneration && !isLockedOrUnlocking()) {
                            lastBackdropCaptureTimeMs = android.os.SystemClock.uptimeMillis()
                            val previous = _capturedBackdropBitmap.value
                            _capturedBackdropBitmap.value = wrapped
                            if (previous != null && previous !== wrapped && !previous.isRecycled) {
                                previous.recycle()
                            }
                        } else {
                            wrapped?.recycle()
                        }
                        onComplete?.invoke()
                    }

                    override fun onFailure(errorCode: Int) {
                        isBackdropCaptureInFlight = false
                        triggerStripView?.alpha = 1f
                        onComplete?.invoke()
                    }
                }
            )
        } catch (_: Throwable) {
            isBackdropCaptureInFlight = false
            triggerStripView?.alpha = 1f
            onComplete?.invoke()
        }
    }

    fun preCaptureScreenBackdropIfNeeded(onComplete: (() -> Unit)? = null) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA ||
            !isAndroid16FrostedGlassSupported() ||
            !isRuntimeShaderRefractionSupported()
        ) {
            onComplete?.invoke()
            return
        }
        if (isPanelShowing || !PanelPreferences.isLiquidGlassActive()) {
            onComplete?.invoke()
            return
        }
        if (isBackdropCaptureInFlight) {
            onComplete?.invoke()
            return
        }
        val requestGen = ++captureGeneration
        takeBackdropScreenshotForGeneration(requestGen, onComplete)
    }

    private fun clearCapturedBackdropBitmap() {
        val current = _capturedBackdropBitmap.value
        _capturedBackdropBitmap.value = null
        if (current != null && !current.isRecycled) {
            try {
                current.recycle()
            } catch (_: Throwable) {}
        }
    }

    var isInterceptionEnabled = true
        private set

    private var dismissAnimatedCallback: (() -> Unit)? = null
    private var lastShadeInterceptionTime = 0L
    var lastUnlockTransitionTime = 0L
        internal set
    private var lastHunEventTime = 0L
    private var lastDetectorCheckTime = 0L
    private var lastWindowsImmersiveCheckTime = 0L
    @Volatile
    private var lastDispatchedTriggerImmersive: Boolean? = null

    private val powerManager: PowerManager? by lazy {
        getSystemService(Context.POWER_SERVICE) as? PowerManager
    }
    private val keyguardManager: KeyguardManager? by lazy {
        getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
    }

    private var wasKeyguardLocked: Boolean = true
    private var ignoreFirstPostUnlockEvent: Boolean = false

    private var lastForegroundAppPackage: String = ""
    private var launcherPackages: Set<String> = emptySet()

    private var isUserDraggingGesture = false
    private var lastDragGestureTimestamp = 0L

    fun notifyUserDragging(dragging: Boolean) {
        isUserDraggingGesture = dragging
        if (dragging) {
            lastDragGestureTimestamp = System.currentTimeMillis()
        }
    }

    var isUnlocking: Boolean = false
        internal set
    private var unlockResetJob: Job? = null

    fun isLockedOrUnlocking(): Boolean {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        val isLocked = keyguardManager?.isKeyguardLocked == true || keyguardManager?.inKeyguardRestrictedInputMode() == true
        val isRecentlyUnlocked = isUnlocking || (System.currentTimeMillis() - lastUnlockTransitionTime < 1200L)
        return isLocked || isRecentlyUnlocked
    }

    private fun updateLauncherPackages() {
        val pkgs = mutableSetOf(
            "com.google.android.apps.nexuslauncher",
            "com.android.launcher3",
            "com.sec.android.app.launcher",
            "com.miui.home",
            "com.oppo.launcher",
            "com.oneplus.launcher",
            "com.huawei.android.launcher",
            "com.teslacoilsw.launcher",
            "com.microsoft.launcher",
            "ch.deletescape.lawnchair.plah",
            "app.lawnchair"
        )
        try {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolveInfos = packageManager.queryIntentActivities(intent, 0)
            for (info in resolveInfos) {
                info.activityInfo?.packageName?.let { pkgs.add(it) }
            }
        } catch (e: Exception) {
            // Ignore
        }
        launcherPackages = pkgs
    }

    private fun isForegroundAppLauncher(): Boolean {
        if (launcherPackages.isEmpty()) {
            updateLauncherPackages()
        }
        // 1. Check last recorded non-systemui foreground window package
        if (launcherPackages.contains(lastForegroundAppPackage)) {
            return true
        }
        // 2. Check active windows hierarchy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val activeWins = windows
                for (window in activeWins) {
                    val root = window.root ?: continue
                    val isMatch = try {
                        val wPkg = root.packageName?.toString() ?: ""
                        wPkg.isNotBlank() &&
                            !wPkg.contains("systemui", ignoreCase = true) &&
                            launcherPackages.contains(wPkg)
                    } finally {
                        runCatching { root.recycle() }
                    }
                    if (isMatch) {
                        return true
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
        return false
    }

    /**
     * Determines whether the current foreground app is operating in Full-Screen / Immersive Mode.
     * Combines Shizuku-based ImmersiveModeDetector with ground-truth window inspection
     * (filtering out our own overlay window).
     */
    fun isImmersiveModeActive(): Boolean {
        // 1. Shizuku detector evaluation
        if (immersiveModeDetector?.isImmersiveModeActive?.value == true) {
            return true
        }

        // 2. Direct Window Inspection Ground Truth (strictly filtering out our own overlay window)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val winList = windows
                if (winList != null && winList.isNotEmpty()) {
                    val displayMetrics = resources.displayMetrics
                    val screenHeight = displayMetrics.heightPixels
                    var hasSystemStatusBar = false
                    var isAppFullscreen = false

                    for (win in winList) {
                        val root = try {
                            win.root
                        } catch (_: Throwable) {
                            null
                        }
                        val pkg = try {
                            root?.packageName?.toString() ?: ""
                        } finally {
                            if (root != null) {
                                runCatching { root.recycle() }
                            }
                        }
                        if (pkg == packageName || pkg.contains("com.aistudio") || pkg.contains("com.example")) {
                            continue
                        }
                        if (win.type == AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY) {
                            continue
                        }

                        val bounds = android.graphics.Rect()
                        win.getBoundsInScreen(bounds)
                        if (bounds.isEmpty) continue

                        if (pkg == "com.android.systemui" || win.type == AccessibilityWindowInfo.TYPE_SYSTEM) {
                            if (bounds.top == 0 && bounds.height() in 1..(60 * displayMetrics.density).toInt()) {
                                hasSystemStatusBar = true
                            }
                        } else if (win.type == AccessibilityWindowInfo.TYPE_APPLICATION) {
                            if (bounds.top == 0 && bounds.height() >= (screenHeight * 0.90f).toInt()) {
                                isAppFullscreen = true
                            }
                        }
                    }

                    if (!hasSystemStatusBar && isAppFullscreen) {
                        return true
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }

        return false
    }

    private val systemStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    isUnlocking = false
                    wasKeyguardLocked = true
                    expansionStage.value = QsExpansionStage.COLLAPSED
                    destroyOverlayWindow()
                }
                Intent.ACTION_SCREEN_ON -> {
                    val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                    if (km?.isKeyguardLocked == true || km?.inKeyguardRestrictedInputMode() == true) {
                        wasKeyguardLocked = true
                    }
                }
                Intent.ACTION_USER_PRESENT -> {
                    isUnlocking = true
                    wasKeyguardLocked = false
                    lastUnlockTransitionTime = System.currentTimeMillis()
                    unlockResetJob?.cancel()
                    unlockResetJob = serviceScope.launch {
                        delay(1200)
                        isUnlocking = false
                    }
                }
                "android.media.VOLUME_CHANGED_ACTION",
                "android.media.STREAM_DEVICES_CHANGED_ACTION",
                "android.media.RINGER_MODE_CHANGED" -> {
                    if (isPanelShowing) {
                        hideOverlayPanelSmoothly()
                        if (PanelPreferences.enableCustomVolumePanel.value) {
                            context?.let { VolumePanelOverlay.show(it) }
                        }
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY)
            com.example.system.CpuPerformanceManager.getInstance(this).acquireUrgentPriority()
        } catch (e: Exception) {
            Log.e("AccessibilityService", "Failed to set urgent display thread priority: ${e.message}")
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY)
            com.example.system.CpuPerformanceManager.getInstance(this).acquireUrgentPriority()
        } catch (e: Exception) {
            // Ignore
        }
        instance = this
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager

        // Initialize tile & system managers cleanly across cold-start / process restart
        com.example.AppRuntimeInitializer.ensureInitialized(this)
        MediaSessionManagerHelper.ensureListening(this, CLIENT_TAG)
        val db = QuickPanelDatabase.getInstance(this)
        val repo = TileRepository(db.tileDao())
        privilegeEngine = PrivilegeEngine(this)
        nativeTileManager = NativeTileManager(this, privilegeEngine, serviceScope)
        externalTileScanner = ExternalTileScanner(this)
        tileRegistry = TileRegistry(this, repo, nativeTileManager, externalTileScanner, serviceScope)

        // Initialize Immersive Mode Detector via Shizuku / Root shell listeners
        immersiveModeDetector = com.example.system.ImmersiveModeDetector(
            privilegeEngine = privilegeEngine,
            scope = serviceScope,
            shouldPauseMonitoring = { isPanelShowing || powerManager?.isInteractive == false }
        )
        immersiveModeDetector?.startMonitoring { isImmersive ->
            updateTriggerVisibility(isImmersive)
        }

        // Configure event types to capture all shade window transitions
        val info = serviceInfo ?: android.accessibilityservice.AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                AccessibilityEvent.TYPE_WINDOWS_CHANGED or
                AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
        info.feedbackType = android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                android.accessibilityservice.AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                android.accessibilityservice.AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS or
                android.accessibilityservice.AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        info.notificationTimeout = 0
        serviceInfo = info

        // Dynamically bind/unbind trigger strip & update isInterceptionEnabled based on Quick Settings Overlay toggle
        serviceScope.launch {
            PanelPreferences.overrideSystemQs.collect { enabled ->
                setOverlayInterception(enabled)
            }
        }

        updateLauncherPackages()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction("android.media.VOLUME_CHANGED_ACTION")
            addAction("android.media.STREAM_DEVICES_CHANGED_ACTION")
            addAction("android.media.RINGER_MODE_CHANGED")
        }
        if (!isSystemReceiverRegistered) {
            try {
                androidx.core.content.ContextCompat.registerReceiver(
                    this,
                    systemStateReceiver,
                    filter,
                    androidx.core.content.ContextCompat.RECEIVER_EXPORTED
                )
                isSystemReceiverRegistered = true
            } catch (_: Throwable) {
                isSystemReceiverRegistered = false
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        com.example.system.SystemThemeObserver.onConfigurationChanged(this, newConfig)
        immersiveModeDetector?.checkNow()
        val immersive = isImmersiveModeActive()
        updateTriggerVisibility(immersive)
    }

    fun setOverlayInterception(enabled: Boolean) {
        isInterceptionEnabled = enabled
        serviceScope.launch(Dispatchers.Main) {
            if (enabled) {
                attachAccessibilityTriggerStrip()
            } else {
                detachAccessibilityTriggerStrip()
                hideOverlayPanelSmoothly()
            }
        }
    }

    private val interceptionInProgress = java.util.concurrent.atomic.AtomicBoolean(false)

    private fun isCandidateShadeOrKeyguardPackage(pkg: String): Boolean {
        if (pkg.isEmpty()) return true
        if (pkg == "com.android.systemui" ||
            pkg.endsWith(".systemui", ignoreCase = true) ||
            pkg.contains("systemui", ignoreCase = true) ||
            pkg.contains("keyguard", ignoreCase = true)
        ) {
            return true
        }
        val whitelistPkgs = PanelPreferences.stockShadeWhitelist.value
        return whitelistPkgs.any { allowedPkg ->
            allowedPkg.isNotBlank() && (pkg.equals(allowedPkg, ignoreCase = true) || pkg.contains(allowedPkg, ignoreCase = true))
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!PanelPreferences.overrideSystemQs.value) return
        if (isPanelShowing) return // Pause detector when our panel is active
        if (event == null) return

        val eventType = event.eventType
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            return
        }

        val pkg = event.packageName?.toString() ?: ""
        val isCandidateShadePkg = isCandidateShadeOrKeyguardPackage(pkg)

        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            pkg.isNotBlank() &&
            pkg != packageName &&
            !isCandidateShadePkg
        ) {
            lastForegroundAppPackage = pkg
        }

        val now = System.currentTimeMillis()
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
            if (powerManager?.isInteractive != false) {
                val immersiveDebounceMs = if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) 0L else 200L
                if (immersiveDebounceMs == 0L || (now - lastWindowsImmersiveCheckTime) >= immersiveDebounceMs) {
                    lastWindowsImmersiveCheckTime = now
                    if (now - lastDetectorCheckTime >= 400L) {
                        lastDetectorCheckTime = now
                        immersiveModeDetector?.checkNow()
                    }
                    val immersive = isImmersiveModeActive()
                    updateTriggerVisibility(immersive)
                }
            }
        }

        // Ignore high-frequency TYPE_WINDOW_CONTENT_CHANGED / third-party window churn (e.g. YouTube playback HUD, captions, scroll)
        // before doing any Binder IPC (event.source), string allocation, or background coroutine dispatch.
        if (!isCandidateShadePkg) {
            return
        }

        val cls = event.className?.toString() ?: ""
        val text = try { event.text.joinToString(" ") } catch (e: Exception) { "" }
        val desc = event.contentDescription?.toString() ?: ""
        val bounds = Rect()
        val viewId = try {
            val src = event.source
            if (src != null) {
                try {
                    src.getBoundsInScreen(bounds)
                    src.viewIdResourceName ?: ""
                } finally {
                    runCatching { src.recycle() }
                }
            } else {
                ""
            }
        } catch (e: Exception) { "" }

        serviceScope.launch(Dispatchers.Default) {
            processEventOptimized(now, pkg, cls, text, desc, viewId, bounds)
        }
    }

    private suspend fun processEventOptimized(
        now: Long,
        pkg: String,
        cls: String,
        text: String,
        desc: String,
        viewId: String,
        bounds: Rect
    ) {
        if (isPanelShowing || !isInterceptionEnabled) {
            return
        }

        // 1. PowerManager interactive check (Screen off / AOD)
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        if (powerManager?.isInteractive == false) {
            return
        }

        // 2. Display state check (AOD / Doze / Screen Off)
        val displayState = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                display?.state
            } else {
                @Suppress("DEPRECATION")
                val wm = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                wm?.defaultDisplay?.state
            }
        } catch (e: Exception) { null }

        if (displayState == Display.STATE_OFF ||
            displayState == Display.STATE_DOZE ||
            displayState == Display.STATE_DOZE_SUSPEND
        ) {
            return
        }

        // 3. Keyguard & Immersive Check
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        val isCurrentlyLocked = keyguardManager?.isKeyguardLocked == true || keyguardManager?.inKeyguardRestrictedInputMode() == true

        // Filter out any keyguard, bouncer, or lockscreen events explicitly
        val isKeyguardNode = cls.contains("keyguard", ignoreCase = true) ||
                cls.contains("bouncer", ignoreCase = true) ||
                cls.contains("lockscreen", ignoreCase = true) ||
                viewId.contains("keyguard", ignoreCase = true) ||
                viewId.contains("bouncer", ignoreCase = true) ||
                desc.contains("keyguard", ignoreCase = true) ||
                text.contains("keyguard", ignoreCase = true)

        if (isKeyguardNode) {
            wasKeyguardLocked = true
            return
        }

        // Detect transition from locked -> unlocked before USER_PRESENT broadcast
        if (wasKeyguardLocked && !isCurrentlyLocked) {
            wasKeyguardLocked = false
            isUnlocking = true
            lastUnlockTransitionTime = now
            unlockResetJob?.cancel()
            unlockResetJob = serviceScope.launch {
                delay(1200)
                isUnlocking = false
            }
            return
        }

        if (isCurrentlyLocked) {
            wasKeyguardLocked = true
            return
        }

        val isRecentlyUnlocked = isUnlocking || (now - lastUnlockTransitionTime < 1200L)
        if (isRecentlyUnlocked || isImmersiveModeActive()) {
            return
        }

        // 4. AOD & Ambient Check (Always On Display keywords)
        val isAodOrAmbient = cls.contains("aod", ignoreCase = true) ||
                cls.contains("doze", ignoreCase = true) ||
                cls.contains("ambient", ignoreCase = true) ||
                cls.contains("alwayson", ignoreCase = true) ||
                cls.contains("always_on", ignoreCase = true) ||
                viewId.contains("aod", ignoreCase = true) ||
                viewId.contains("doze", ignoreCase = true) ||
                viewId.contains("ambient", ignoreCase = true) ||
                desc.contains("always on", ignoreCase = true) ||
                desc.contains("aod", ignoreCase = true) ||
                text.contains("always on", ignoreCase = true) ||
                pkg.contains("aod", ignoreCase = true)

        if (isAodOrAmbient) {
            return
        }

        // 5. Screenshot check (Ignore screenshot notifications, preview windows, and action chips)
        val isScreenshot = cls.contains("screenshot", ignoreCase = true) ||
                cls.contains("screen_capture", ignoreCase = true) ||
                cls.contains("screencap", ignoreCase = true) ||
                cls.contains("captur", ignoreCase = true) ||
                viewId.contains("screenshot", ignoreCase = true) ||
                viewId.contains("screen_capture", ignoreCase = true) ||
                viewId.contains("screencap", ignoreCase = true) ||
                text.contains("screenshot", ignoreCase = true) ||
                desc.contains("screenshot", ignoreCase = true) ||
                pkg.contains("screenshot", ignoreCase = true)

        if (isScreenshot) {
            return
        }

        // 6. Pop-up alerts & floating window check (Toasts, Heads-Up Notifications, Bubbles, Dialogs)
        val isPopupOrAlert = cls.contains("toast", ignoreCase = true) ||
                cls.contains("heads_up", ignoreCase = true) ||
                cls.contains("headsup", ignoreCase = true) ||
                cls.contains("bubble", ignoreCase = true) ||
                cls.contains("clipboard", ignoreCase = true) ||
                cls.contains("popup", ignoreCase = true) ||
                cls.contains("alert", ignoreCase = true) ||
                cls.contains("dialog", ignoreCase = true) ||
                cls.contains("floating", ignoreCase = true) ||
                cls.contains("pip", ignoreCase = true) ||
                cls.contains("volume_dialog", ignoreCase = true) ||
                cls.contains("power_dialog", ignoreCase = true) ||
                cls.contains("magnification", ignoreCase = true) ||
                cls.contains("select_to_speak", ignoreCase = true) ||
                viewId.contains("heads_up", ignoreCase = true) ||
                viewId.contains("toast", ignoreCase = true) ||
                viewId.contains("bubble", ignoreCase = true) ||
                viewId.contains("clipboard", ignoreCase = true) ||
                viewId.contains("popup", ignoreCase = true) ||
                viewId.contains("alert", ignoreCase = true) ||
                viewId.contains("dialog", ignoreCase = true) ||
                viewId.contains("volume", ignoreCase = true)

        if (isPopupOrAlert) {
            return
        }

        // 7. Dynamic Custom Blacklist Check:
        val blacklistPkgs = PanelPreferences.customBlacklistPackages.value
        val blacklistClasses = PanelPreferences.customBlacklistClasses.value

        val isBlacklisted = blacklistPkgs.any { bp -> bp.isNotBlank() && pkg.contains(bp, ignoreCase = true) } ||
                blacklistClasses.any { bc -> bc.isNotBlank() && cls.contains(bc, ignoreCase = true) }

        if (isBlacklisted) {
            return
        }

        // 8. Strict Whitelist by Default:
        val whitelistPkgs = PanelPreferences.stockShadeWhitelist.value
        val isWhitelistedPkg = pkg.isEmpty() ||
                whitelistPkgs.any { allowedPkg ->
                    allowedPkg.isNotBlank() && (pkg.equals(allowedPkg, ignoreCase = true) || pkg.contains(allowedPkg, ignoreCase = true))
                } ||
                pkg == "com.android.systemui" ||
                pkg.endsWith(".systemui", ignoreCase = true) ||
                pkg.contains("systemui", ignoreCase = true)

        if (!isWhitelistedPkg) {
            return
        }

        // 9. Strict Shade Detection Checks:
        if (PanelPreferences.strictShadeDetection.value) {
            val screenWidth = resources.displayMetrics.widthPixels
            val screenHeight = resources.displayMetrics.heightPixels
            if (!bounds.isEmpty) {
                val isSmallWindow = bounds.width() < (screenWidth * 0.70f) ||
                        bounds.height() < (screenHeight * 0.20f)
                if (isSmallWindow) {
                    return
                }
            }

            // Must have a recent physical touch or swipe down at the top of the screen (status bar area)
            val lastTopTouch = TriggerContainerLayout.lastTopTouchTimestamp
            val lastTopSwipe = TriggerContainerLayout.lastTopSwipeDownTimestamp
            val recentTopTouch = (now - lastTopTouch in 0..2500L)
            val recentTopSwipe = (now - lastTopSwipe in 0..2500L)
            if (!recentTopTouch && !recentTopSwipe) {
                return
            }
        }

        val targetClasses = setOf(
            "NotificationPanelView",
            "NotificationPanelViewController",
            "NotificationShadeWindowView",
            "ExpandedStatusBar",
            "NotificationShadeWindow",
            "QSPanel",
            "QuickQSPanel",
            "QuickStatusBarHeader",
            "QuickSettings",
            "QSContainerImpl",
            "QSFragment",
            "QSImpl",
            "QuickSettingsPanel",
            "SecQuickSettings",
            "SecNotificationPanelView",
            "MiuiNotificationPanelView",
            "MiuiQSPanel",
            "ShadeWindowView",
            "CentralSurfaces",
            "NotificationShade",
            "QuickSettingsController"
        )

        val isTargetClass = targetClasses.any { target ->
            cls.equals(target, ignoreCase = true) || cls.endsWith(".$target", ignoreCase = true) || cls.contains(target, ignoreCase = true)
        }

        val isTargetViewId = viewId.contains("qs_frame", ignoreCase = true) ||
                viewId.contains("notification_stack_scroller", ignoreCase = true) ||
                viewId.contains("quick_settings", ignoreCase = true) ||
                viewId.contains("expanded_qs", ignoreCase = true) ||
                viewId.contains("shade_header", ignoreCase = true) ||
                viewId.contains("carrier_group", ignoreCase = true) ||
                viewId.contains("quick_qs_panel", ignoreCase = true)

        val isTargetTextOrDesc = desc.contains("Notification shade", ignoreCase = true) ||
                desc.contains("Quick settings", ignoreCase = true) ||
                text.contains("Notification shade", ignoreCase = true) ||
                text.contains("Quick settings", ignoreCase = true)

        val isShadeEvent = isTargetClass || isTargetViewId || isTargetTextOrDesc

        if (isShadeEvent) {
            val cooldown = PanelPreferences.detectorCooldownMs.value.toLong().coerceAtLeast(80L)
            if (now - lastShadeInterceptionTime < cooldown) {
                return
            }

            // Always open in compact QQS stage on shade interception to prevent accidental auto-expansion to full QS
            val stage = QsExpansionStage.QQS

            // Maintain interceptionInProgress atomic boolean so collapse/open triggers exactly ONCE per valid gesture
            if (interceptionInProgress.compareAndSet(false, true)) {
                lastShadeInterceptionTime = now
                try {
                    val replacementDelayMs = PanelPreferences.panelReplacementDelayMs.value
                    if (replacementDelayMs > 0) {
                        delay(replacementDelayMs.toLong())
                    }
                    collapseStockNotificationShade()
                    withContext(Dispatchers.Main) {
                        showOverlayPanel(stage)
                    }
                } finally {
                    serviceScope.launch(Dispatchers.Default) {
                        delay(250L)
                        interceptionInProgress.set(false)
                    }
                }
            }
        }
    }

    private fun isHeadsUpOrToastNode(node: AccessibilityNodeInfo): Boolean {
        val id = node.viewIdResourceName ?: ""
        val cls = node.className?.toString() ?: ""
        val text = node.text?.toString() ?: ""
        val desc = node.contentDescription?.toString() ?: ""
        val combined = "$id $cls $text $desc"

        if (combined.contains("heads_up", ignoreCase = true) ||
            combined.contains("toast", ignoreCase = true) ||
            id.contains("heads_up_status_bar_view", ignoreCase = true) ||
            id.contains("status_bar_container", ignoreCase = true) ||
            combined.contains("HeadsUpStatusBarView", ignoreCase = true) ||
            combined.contains("HeadsUpTileView", ignoreCase = true) ||
            combined.contains("notification_heads_up", ignoreCase = true) ||
            combined.contains("heads_up_container", ignoreCase = true) ||
            Regex("""\bHUN\b""").containsMatchIn(combined)
        ) {
            return true
        }

        val childCount = node.childCount.coerceAtMost(12)
        for (i in 0 until childCount) {
            val child = node.getChild(i) ?: continue
            val hasHun = isHeadsUpOrToastNode(child)
            child.recycle()
            if (hasHun) return true
        }

        return false
    }

    private fun isFullShadeExpansionContainer(node: AccessibilityNodeInfo?, fallbackViewId: String): Boolean {
        if (fallbackViewId.contains("qs_frame", ignoreCase = true) ||
            fallbackViewId.contains("expanded_qs_scroll_view", ignoreCase = true) ||
            fallbackViewId.contains("notification_stack_scroller", ignoreCase = true) ||
            fallbackViewId.endsWith(":id/qs_frame") ||
            fallbackViewId.endsWith(":id/expanded_qs_scroll_view")
        ) {
            return true
        }

        if (node != null) {
            val id = node.viewIdResourceName ?: ""
            if (id.contains("qs_frame", ignoreCase = true) ||
                id.contains("expanded_qs_scroll_view", ignoreCase = true) ||
                id.contains("notification_stack_scroller", ignoreCase = true) ||
                id.endsWith(":id/qs_frame") ||
                id.endsWith(":id/expanded_qs_scroll_view")
            ) {
                return true
            }

            var p = node.parent
            var depth = 0
            while (p != null && depth < 3) {
                val pid = p.viewIdResourceName ?: ""
                if (pid.contains("qs_frame", ignoreCase = true) ||
                    pid.contains("expanded_qs_scroll_view", ignoreCase = true) ||
                    pid.contains("notification_stack_scroller", ignoreCase = true) ||
                    pid.endsWith(":id/qs_frame") ||
                    pid.endsWith(":id/expanded_qs_scroll_view")
                ) {
                    p.recycle()
                    return true
                }
                val nextP = p.parent
                p.recycle()
                p = nextP
                depth++
            }

            val childCount = node.childCount.coerceAtMost(8)
            for (i in 0 until childCount) {
                val child = node.getChild(i) ?: continue
                val cid = child.viewIdResourceName ?: ""
                val isMatch = cid.contains("qs_frame", ignoreCase = true) ||
                        cid.contains("expanded_qs_scroll_view", ignoreCase = true) ||
                        cid.contains("notification_stack_scroller", ignoreCase = true) ||
                        cid.endsWith(":id/qs_frame") ||
                        cid.endsWith(":id/expanded_qs_scroll_view")
                child.recycle()
                if (isMatch) return true
            }
        }

        return false
    }

    private fun hasTrueShadeExpansion(event: AccessibilityEvent, source: AccessibilityNodeInfo?): Boolean {
        val eventText = event.text.joinToString(" ")
        val eventDesc = event.contentDescription?.toString() ?: ""
        val fullContent = "$eventText $eventDesc"

        // 1. Explicit mExpandedFraction / isExpanded state
        val isExplicitExpanded = fullContent.contains("isExpanded=true", ignoreCase = true) ||
                Regex("""mExpandedFraction\s*=\s*(0\.[3-9]|1\.0|[1-9]\d*)""", RegexOption.IGNORE_CASE).containsMatchIn(fullContent) ||
                Regex("""fraction\s*[:=]\s*(0\.[3-9]|1\.0)""", RegexOption.IGNORE_CASE).containsMatchIn(fullContent)

        if (isExplicitExpanded) return true

        // 2. Inspect source node and hierarchy for ACTION_COLLAPSE or specific matching resource IDs / text / content description
        if (source != null) {
            if (checkNodeForTrueShadeExpansion(source)) return true

            var p = source.parent
            var depth = 0
            while (p != null && depth < 3) {
                if (checkNodeForTrueShadeExpansion(p)) {
                    p.recycle()
                    return true
                }
                val nextP = p.parent
                p.recycle()
                p = nextP
                depth++
            }

            val childCount = source.childCount.coerceAtMost(8)
            for (i in 0 until childCount) {
                val child = source.getChild(i) ?: continue
                if (checkNodeForTrueShadeExpansion(child)) {
                    child.recycle()
                    return true
                }
                child.recycle()
            }
        }

        return false
    }

    private fun checkNodeForTrueShadeExpansion(node: AccessibilityNodeInfo): Boolean {
        // Check if the node action list includes collapse actions (ACTION_COLLAPSE)
        val hasCollapseAction = node.actionList.any { action ->
            action.id == AccessibilityNodeInfo.ACTION_COLLAPSE ||
            action.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_COLLAPSE.id ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && action.label?.contains("collapse", ignoreCase = true) == true)
        }
        if (hasCollapseAction) return true

        val id = node.viewIdResourceName ?: ""
        val text = node.text?.toString() ?: ""
        val desc = node.contentDescription?.toString() ?: ""
        val cls = node.className?.toString() ?: ""

        // Verify if node text/content description or resource ID matches qs_frame, notification_stack_scroller, or expanded_shade specifically
        return id.contains("qs_frame", ignoreCase = true) ||
                id.contains("notification_stack_scroller", ignoreCase = true) ||
                id.contains("expanded_shade", ignoreCase = true) ||
                text.contains("qs_frame", ignoreCase = true) ||
                text.contains("expanded_shade", ignoreCase = true) ||
                desc.contains("qs_frame", ignoreCase = true) ||
                desc.contains("expanded_shade", ignoreCase = true) ||
                desc.contains("Notification shade", ignoreCase = true) ||
                desc.contains("Quick settings", ignoreCase = true) ||
                cls.contains("NotificationPanelView", ignoreCase = true) ||
                cls.contains("NotificationShadeWindowView", ignoreCase = true) ||
                cls.contains("ExpandedStatusBar", ignoreCase = true)
    }

    private fun isBouncerOrBiometricActive(): Boolean {
        try {
            val rootNode = rootInActiveWindow
            if (rootNode != null) {
                val isBouncer = try {
                    val rPkg = rootNode.packageName?.toString() ?: ""
                    val rCls = rootNode.className?.toString() ?: ""
                    rPkg == "com.android.keyguard" ||
                        rPkg.contains("biometric", ignoreCase = true) ||
                        rCls.contains("KeyguardBouncer", ignoreCase = true) ||
                        rCls.contains("Bouncer", ignoreCase = true) ||
                        rCls.contains("Biometric", ignoreCase = true)
                } finally {
                    runCatching { rootNode.recycle() }
                }
                if (isBouncer) return true
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val activeWins = windows
                for (window in activeWins) {
                    val wRoot = window.root ?: continue
                    val isBouncer = try {
                        val wPkg = wRoot.packageName?.toString() ?: ""
                        val wCls = wRoot.className?.toString() ?: ""
                        wPkg == "com.android.keyguard" ||
                            wPkg.contains("biometric", ignoreCase = true) ||
                            wCls.contains("KeyguardBouncer", ignoreCase = true) ||
                            wCls.contains("Bouncer", ignoreCase = true) ||
                            wCls.contains("Biometric", ignoreCase = true)
                    } finally {
                        runCatching { wRoot.recycle() }
                    }
                    if (isBouncer) return true
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        return false
    }

    private fun isPanelFullyExpanded(event: AccessibilityEvent): Boolean {
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val minExpansionHeight = (screenHeight * 0.50f).toInt()
        val statusBarThresholdPx = (80 * displayMetrics.density).toInt()

        fun isNodeCoveringThreshold(node: AccessibilityNodeInfo): Boolean {
            val bounds = android.graphics.Rect()
            node.getBoundsInScreen(bounds)
            if (bounds.isEmpty) return false

            // Explicitly filter out top banners / toasts (~10-15% of screen height)
            if (bounds.height() < (screenHeight * 0.35f).toInt()) {
                return false
            }

            val nodeId = node.viewIdResourceName ?: ""
            val nodeCls = node.className?.toString() ?: ""

            // Exclude heads-up and ticker
            if (nodeId.contains("heads_up", ignoreCase = true) ||
                nodeCls.contains("HeadsUp", ignoreCase = true) ||
                nodeId.contains("ticker", ignoreCase = true) ||
                nodeCls.contains("Ticker", ignoreCase = true)
            ) {
                return false
            }

            // Must originate near top bar and cover > 50% screen height
            return bounds.top <= statusBarThresholdPx && bounds.height() >= minExpansionHeight
        }

        val source = event.source
        if (source != null) {
            try {
                if (isNodeCoveringThreshold(source)) return true

                var parent = source.parent
                var depth = 0
                while (parent != null && depth < 4) {
                    if (isNodeCoveringThreshold(parent)) {
                        runCatching { parent.recycle() }
                        return true
                    }
                    val next = parent.parent
                    runCatching { parent.recycle() }
                    parent = next
                    depth++
                }
                if (parent != null) {
                    runCatching { parent.recycle() }
                }

                val childCount = source.childCount.coerceAtMost(8)
                for (i in 0 until childCount) {
                    val child = source.getChild(i) ?: continue
                    val matched = try {
                        isNodeCoveringThreshold(child)
                    } finally {
                        runCatching { child.recycle() }
                    }
                    if (matched) return true
                }
            } finally {
                runCatching { source.recycle() }
            }
        }

        try {
            val root = rootInActiveWindow
            if (root != null) {
                try {
                    if (isNodeCoveringThreshold(root)) return true
                    val rootChildCount = root.childCount.coerceAtMost(10)
                    for (i in 0 until rootChildCount) {
                        val child = root.getChild(i) ?: continue
                        val matched = try {
                            isNodeCoveringThreshold(child)
                        } finally {
                            runCatching { child.recycle() }
                        }
                        if (matched) return true
                    }
                } finally {
                    runCatching { root.recycle() }
                }
            }
        } catch (e: Exception) {
            // Ignore
        }

        return false
    }

    private fun isTargetNotificationShadeEvent(
        event: AccessibilityEvent,
        cls: String,
        viewId: String
    ): Boolean {
        // Target Class Name Matches
        val isTriggerClass =
            cls.contains("NotificationPanelView", ignoreCase = true) ||
            cls.contains("NotificationShadeWindowView", ignoreCase = true) ||
            cls.contains("ExpandedStatusBar", ignoreCase = true)

        // Target Resource ID Matches
        val isTriggerResourceId =
            viewId.contains("qs_frame", ignoreCase = true) ||
            viewId.contains("notification_stack_scroller", ignoreCase = true)

        if (isTriggerClass || isTriggerResourceId) return true

        // Node Hierarchy Scan (max 2 levels)
        val source = event.source ?: return false
        return try {
            checkNodeTreeForShadeTargets(source)
        } finally {
            runCatching { source.recycle() }
        }
    }

    private fun checkNodeTreeForShadeTargets(node: AccessibilityNodeInfo): Boolean {
        val nodeId = node.viewIdResourceName ?: ""
        val nodeClass = node.className?.toString() ?: ""

        if (nodeId.contains("qs_frame", ignoreCase = true) ||
            nodeId.contains("notification_stack_scroller", ignoreCase = true) ||
            nodeClass.contains("NotificationPanelView", ignoreCase = true) ||
            nodeClass.contains("NotificationShadeWindowView", ignoreCase = true)
        ) {
            return true
        }

        val childCount = node.childCount.coerceAtMost(8)
        for (i in 0 until childCount) {
            val child = node.getChild(i) ?: continue
            val childId = child.viewIdResourceName ?: ""
            val childClass = child.className?.toString() ?: ""
            val isMatch = childId.contains("qs_frame", ignoreCase = true) ||
                    childId.contains("notification_stack_scroller", ignoreCase = true) ||
                    childClass.contains("NotificationPanelView", ignoreCase = true) ||
                    childClass.contains("NotificationShadeWindowView", ignoreCase = true)
            child.recycle()
            if (isMatch) return true
        }
        return false
    }

    override fun onInterrupt() {
        // Interrupted
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null || !PanelPreferences.enableCustomVolumePanel.value) return super.onKeyEvent(event)
        val keyCode = event.keyCode
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
            keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
            keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
        ) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                // If custom Quick Settings panel is open, close it on volume button press
                hideOverlayPanelSmoothly()
                QuickSettingsOverlayService.instance?.hideOverlayPanelSmoothly()

                when (keyCode) {
                    KeyEvent.KEYCODE_VOLUME_UP -> VolumePanelOverlay.onVolumeKey(this, isUp = true)
                    KeyEvent.KEYCODE_VOLUME_DOWN -> VolumePanelOverlay.onVolumeKey(this, isUp = false)
                    KeyEvent.KEYCODE_VOLUME_MUTE -> VolumePanelOverlay.onVolumeMuteKey(this)
                }
            }
            return false
        }
        return super.onKeyEvent(event)
    }

    /**
     * Universal Accessibility Click Simulation (Non-Root Engine):
     * Finds and simulates ACTION_CLICK on the corresponding SystemUI QS tile node.
     * Matches by label, contentDescription, or resourceId across windows and active hierarchy.
     */
    fun clickSystemUiTile(tileLabel: String, keywords: List<String> = emptyList(), specifier: String? = null): Boolean {
        try {
            val root = rootInActiveWindow
            if (root != null) {
                val clicked = try {
                    searchAndClickInNode(root, tileLabel, keywords, specifier)
                } finally {
                    runCatching { root.recycle() }
                }
                if (clicked) {
                    collapseStockNotificationShade()
                    return true
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val winList = windows
                for (window in winList) {
                    val winRoot = window.root ?: continue
                    val clicked = try {
                        searchAndClickInNode(winRoot, tileLabel, keywords, specifier)
                    } finally {
                        runCatching { winRoot.recycle() }
                    }
                    if (clicked) {
                        collapseStockNotificationShade()
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AccessibilityService", "Universal click simulation error: ${e.message}")
        }
        return false
    }

    private fun searchAndClickInNode(
        node: AccessibilityNodeInfo,
        tileLabel: String,
        keywords: List<String>,
        specifier: String?
    ): Boolean {
        val nodeText = node.text?.toString() ?: ""
        val nodeDesc = node.contentDescription?.toString() ?: ""
        val viewId = node.viewIdResourceName ?: ""

        val searchTerms = mutableListOf(tileLabel)
        searchTerms.addAll(keywords)
        specifier?.let { searchTerms.add(it) }

        var isMatch = false
        for (term in searchTerms) {
            if (term.isNotBlank()) {
                if (nodeText.contains(term, ignoreCase = true) ||
                    nodeDesc.contains(term, ignoreCase = true) ||
                    viewId.contains(term, ignoreCase = true)
                ) {
                    isMatch = true
                    break
                }
            }
        }

        if (isMatch) {
            val visitedParents = mutableListOf<AccessibilityNodeInfo>()
            var clickableNode: AccessibilityNodeInfo? = node
            var parentDepth = 0
            while (clickableNode != null && !clickableNode.isClickable && parentDepth < 8) {
                val parent = clickableNode.parent ?: break
                visitedParents.add(parent)
                clickableNode = parent
                parentDepth++
            }
            val target = if (clickableNode != null && clickableNode.isClickable) clickableNode else node
            val didClick = target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            for (p in visitedParents) {
                runCatching { p.recycle() }
            }
            if (didClick) {
                Log.d("AccessibilityService", "Successfully clicked stock tile for term '$tileLabel'")
                return true
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val clicked = try {
                searchAndClickInNode(child, tileLabel, keywords, specifier)
            } finally {
                runCatching { child.recycle() }
            }
            if (clicked) {
                return true
            }
        }
        return false
    }

    /**
     * Attach the trigger strip with TYPE_ACCESSIBILITY_OVERLAY to force Z-order priority
     * above the native SystemUI status bar and notification shade.
     */
    fun attachAccessibilityTriggerStrip() {
        if (triggerContainerView != null || !isInterceptionEnabled) return

        val density = resources.displayMetrics.density
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        val rawStatusBarHeight = if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0

        val triggerHeightPx = if (rawStatusBarHeight > 0) rawStatusBarHeight else (28 * density).toInt()

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            triggerHeightPx,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
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
        val strip = AccessibilityStatusTriggerView(this).apply {
            visibility = if (isImmersiveModeActive()) View.GONE else View.VISIBLE
            onSwipeDownDetected = { isDeepSwipe ->
                if (!isLockedOrUnlocking()) {
                    collapseStockNotificationShade()
                    if (isDeepSwipe) {
                        showOverlayPanel(QsExpansionStage.FULL_QS)
                    } else {
                        showOverlayPanel(QsExpansionStage.QQS)
                    }
                }
            }
        }
        container.setChildTriggerView(strip)
        container.onSwipeDownToOpen = { _ ->
            if (!isLockedOrUnlocking()) {
                collapseStockNotificationShade()
                showOverlayPanel(QsExpansionStage.QQS)
            }
        }

        try {
            windowManager?.addView(container, params)
            triggerContainerView = container
            triggerStripView = strip
            triggerLayoutParams = params
            Log.d("AccessibilityService", "TYPE_ACCESSIBILITY_OVERLAY trigger container attached ($triggerHeightPx px).")
        } catch (e: Exception) {
            Log.e("AccessibilityService", "Failed to add accessibility trigger container: ${e.message}")
        }
    }

    /**
     * Updates accessibility trigger visibility dynamically when Immersive Mode state changes.
     * ACTIVE -> View.GONE
     * INACTIVE -> View.VISIBLE
     */
    fun updateTriggerVisibility(isImmersive: Boolean) {
        if (lastDispatchedTriggerImmersive == isImmersive && triggerContainerView != null) {
            return
        }
        lastDispatchedTriggerImmersive = isImmersive
        if (Looper.myLooper() == Looper.getMainLooper()) {
            triggerContainerView?.updateChildVisibility(isImmersive)
        } else {
            mainHandler.post {
                triggerContainerView?.updateChildVisibility(isImmersive)
            }
        }
    }

    fun detachAccessibilityTriggerStrip() {
        lastDispatchedTriggerImmersive = null
        triggerContainerView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                // Ignore
            }
        }
        triggerContainerView = null
        triggerStripView = null
    }

    /**
     * Show custom Quick Settings overlay with TYPE_ACCESSIBILITY_OVERLAY
     */
    fun showOverlayPanel(initialStage: QsExpansionStage) {
        if (isLockedOrUnlocking()) {
            return
        }
        if ((isPanelShowing && overlayComposeView != null) || isPendingShowOverlay) {
            return
        }

        val shouldCaptureLiveBackdrop = Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA &&
            isAndroid16FrostedGlassSupported() &&
            isRuntimeShaderRefractionSupported() &&
            PanelPreferences.isLiquidGlassActive() &&
            !PanelPreferences.isDynamicRefractionActive()

        if (shouldCaptureLiveBackdrop) {
            captureFreshBackdropAndLaunch(initialStage)
            return
        }

        showOverlayPanelInternal(initialStage)
    }

    private fun showOverlayPanelInternal(initialStage: QsExpansionStage) {
        if (isLockedOrUnlocking()) {
            return
        }
        if (isPanelShowing && overlayComposeView != null) {
            return
        }

        triggerContainerView?.cancelTransientReveal()
        collapseStockNotificationShade()
        serviceScope.launch {
            delay(60)
            if (isSystemUiShadeVisible()) {
                collapseStockNotificationShade()
            }
            delay(100)
            if (isSystemUiShadeVisible()) {
                collapseStockNotificationShade()
            }
        }

        val resolvedInitialStage = if (PanelPreferences.isAndroidKitKatStyle(this)) {
            QsExpansionStage.FULL_QS
        } else {
            initialStage
        }
        expansionStage.value = resolvedInitialStage
        nativeTileManager.refreshAllStates()

        val panelParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
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
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            val initialBlurEnabled = isAndroid16FrostedGlassSupported() &&
                PanelPreferences.enableContainerBlur.value &&
                !PanelPreferences.blurOnlyBehindQsCard.value &&
                PanelPreferences.disablePanelAnimations.value &&
                isCrossWindowBlurSupported(this@QuickPanelAccessibilityService)
            if (initialBlurEnabled) {
                applyWindowBlurBehind(
                    this,
                    computeWindowBlurBehindRadiusPx(
                        enabled = true,
                        density = resources.displayMetrics.density,
                        fraction = 1f,
                        maxBlurDp = PanelPreferences.containerBlurRadiusDp.value,
                        performanceMode = PanelPreferences.glassPerformanceMode.value
                    )
                )
            }
        }

        val qsBlurHost = QsMultiCardBlurWindowHost(this, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)

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
                if (PanelPreferences.enableCustomVolumePanel.value &&
                    (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP ||
                     keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN ||
                     keyCode == android.view.KeyEvent.KEYCODE_VOLUME_MUTE)
                ) {
                    if (event.action == android.view.KeyEvent.ACTION_DOWN) {
                        hideOverlayPanelSmoothly()
                        when (keyCode) {
                            android.view.KeyEvent.KEYCODE_VOLUME_UP -> VolumePanelOverlay.onVolumeKey(this@QuickPanelAccessibilityService, isUp = true)
                            android.view.KeyEvent.KEYCODE_VOLUME_DOWN -> VolumePanelOverlay.onVolumeKey(this@QuickPanelAccessibilityService, isUp = false)
                            android.view.KeyEvent.KEYCODE_VOLUME_MUTE -> VolumePanelOverlay.onVolumeMuteKey(this@QuickPanelAccessibilityService)
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
                    val glassBackdropMode by PanelPreferences.glassBackdropMode.collectAsState()
                    val rawEnableContainerBlur by PanelPreferences.enableContainerBlur.collectAsState()
                    val glassSupported = isAndroid16FrostedGlassSupported()
                    val enableContainerBlur = rawEnableContainerBlur && glassSupported
                    val blurOnlyBehindQsCard by PanelPreferences.blurOnlyBehindQsCard.collectAsState()
                    val containerBlurRadiusDp by PanelPreferences.containerBlurRadiusDp.collectAsState()
                    val containerBlurTransparency by PanelPreferences.containerBlurTransparency.collectAsState()
                    val glassRefractionSensitivity by PanelPreferences.glassRefractionSensitivity.collectAsState()
                    val refractionStyle by PanelPreferences.refractionStyle.collectAsState()
                    val glassRefractionScale by PanelPreferences.glassRefractionScale.collectAsState()
                    val glassRefractionDispersion by PanelPreferences.glassRefractionDispersion.collectAsState()
                    val glassRefractionStyleParam by PanelPreferences.glassRefractionCustomParam.collectAsState()
                    val diamondCrossedCut by PanelPreferences.diamondCrossedCut.collectAsState()
                    val glassPerformanceMode by PanelPreferences.glassPerformanceMode.collectAsState()
                    val capturedBackdrop by capturedBackdropBitmap.collectAsState()
                    val isDarkOverlay = LocalIsDarkTheme.current
                    val crossWindowBlurSupported = rememberCrossWindowBlurSupported()
                    val speedFactor = animSpeedScale.coerceAtLeast(0.01f)
                    val targetBackdropAlpha = when {
                        enableContainerBlur && crossWindowBlurSupported && blurOnlyBehindQsCard -> 0.28f
                        enableContainerBlur && crossWindowBlurSupported -> 0.32f
                        enableContainerBlur -> 0.38f
                        else -> 0.50f
                    }

                    val backdropAlpha = remember { androidx.compose.animation.core.Animatable(if (disablePanelAnimations) targetBackdropAlpha else 0f) }
                    val panelOffsetY = remember { androidx.compose.animation.core.Animatable(if (disablePanelAnimations) 0f else initialOffsetY) }
                    val panelAlpha = remember { androidx.compose.animation.core.Animatable(if (disablePanelAnimations) 1f else 0f) }
                    val panelScale = remember { androidx.compose.animation.core.Animatable(if (disablePanelAnimations) 1f else 0.95f) }

                    var isClosing by remember { mutableStateOf(false) }
                    var wasPulling by remember { mutableStateOf(false) }
                    val pullOffset by pullDownOffset.collectAsState()
                    var qsBlurRegions by remember { mutableStateOf<Map<String, QsCardBlurBounds>>(emptyMap()) }
                    var notifBlurRegions by remember { mutableStateOf<List<QsCardBlurBounds>>(emptyList()) }

                    // Sync behind-window SurfaceFlinger blur radius (full-screen or scoped behind QS/QQS + carrier + notification cards)
                    LaunchedEffect(enableContainerBlur, blurOnlyBehindQsCard, containerBlurRadiusDp, crossWindowBlurSupported, targetBackdropAlpha, density, glassPerformanceMode) {
                        snapshotFlow {
                            val denom = targetBackdropAlpha.coerceAtLeast(0.01f)
                            val fraction = (backdropAlpha.value / denom).coerceIn(0f, 1f)
                            val desiredRadius = computeWindowBlurBehindRadiusPx(
                                enabled = enableContainerBlur && crossWindowBlurSupported,
                                density = density.density,
                                fraction = fraction,
                                maxBlurDp = containerBlurRadiusDp,
                                performanceMode = glassPerformanceMode
                            )
                            val activeRegions = if (glassPerformanceMode && qsBlurRegions.isNotEmpty()) {
                                qsBlurRegions.mapValues { (_, bounds) ->
                                    quantizeBlurRegionsForPerformance(listOf(bounds), stepPx = 2f).first()
                                }
                            } else {
                                qsBlurRegions
                            }
                            val activeNotifRegions = if (glassPerformanceMode) {
                                quantizeBlurRegionsForPerformance(notifBlurRegions, stepPx = 2f)
                            } else {
                                notifBlurRegions
                            }
                            Triple(desiredRadius, activeRegions, activeNotifRegions)
                        }.distinctUntilChanged().collect { (desiredRadius, regions, notifRegions) ->
                            val plan = computeQsBlurWindowPlan(desiredRadius, blurOnlyBehindQsCard)
                            val view = this@apply
                            if (applyWindowBlurBehind(panelParams, plan.fullScreenBlurRadiusPx) && view.isAttachedToWindow) {
                                try {
                                    windowManager?.updateViewLayout(view, panelParams)
                                } catch (_: Exception) {
                                    // Ignore if window detached mid-frame
                                }
                            }
                            val screenLoc = IntArray(2)
                            if (view.isAttachedToWindow) {
                                view.getLocationOnScreen(screenLoc)
                            }
                            qsBlurHost.updateAll(
                                regions = regions,
                                radiusPx = plan.qsCardBlurRadiusPx,
                                offsetX = screenLoc[0],
                                offsetY = screenLoc[1],
                                notificationRegions = notifRegions
                            )
                        }
                    }

                    // Smoothly adjust scrim opacity if Frosted Glass is toggled while panel is visible
                    LaunchedEffect(targetBackdropAlpha) {
                        if (!isClosing && !wasPulling && pullDownOffset.value == null && backdropAlpha.value > 0f) {
                            if (disablePanelAnimations) {
                                backdropAlpha.snapTo(targetBackdropAlpha)
                            } else {
                                backdropAlpha.animateTo(
                                    targetValue = targetBackdropAlpha,
                                    animationSpec = androidx.compose.animation.core.tween(
                                        durationMillis = (180 * speedFactor).toInt().coerceAtLeast(1)
                                    )
                                )
                            }
                        }
                    }

                    // Live finger tracking for pull-down gesture from status bar
                    LaunchedEffect(pullOffset) {
                        val currentPull = pullOffset
                        if (currentPull != null) {
                            wasPulling = true
                            val fraction = (currentPull / revealDistancePx).coerceIn(0f, 1f)
                            val targetY = initialOffsetY * (1f - fraction)
                            panelOffsetY.snapTo(targetY)
                            backdropAlpha.snapTo(targetBackdropAlpha * fraction)
                            panelAlpha.snapTo(0.3f + (0.7f * fraction))
                            panelScale.snapTo(0.95f + (0.05f * fraction))
                        } else if (wasPulling && !isClosing) {
                            wasPulling = false
                            // Finger released: snap open if dragged sufficiently, else dismiss
                            if (disablePanelAnimations) {
                                backdropAlpha.snapTo(targetBackdropAlpha)
                                panelOffsetY.snapTo(0f)
                                panelAlpha.snapTo(1f)
                                panelScale.snapTo(1f)
                            } else {
                                if (backdropAlpha.value >= 0.12f || panelOffsetY.value >= (initialOffsetY * 0.75f)) {
                                    launch {
                                        backdropAlpha.animateTo(
                                            targetValue = targetBackdropAlpha,
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
                                backdropAlpha.snapTo(targetBackdropAlpha)
                                panelOffsetY.snapTo(0f)
                                panelAlpha.snapTo(1f)
                                panelScale.snapTo(1f)
                            } else {
                                launch {
                                    backdropAlpha.animateTo(
                                        targetValue = targetBackdropAlpha,
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
                    val isNotificationPermGranted = remember { QuickPanelNotificationListenerService.isPermissionGranted(this@QuickPanelAccessibilityService) }
                    val brightness by nativeTileManager.brightness.collectAsState()
                    val mediaVol by nativeTileManager.mediaVolume.collectAsState()
                    val ringVol by nativeTileManager.ringVolume.collectAsState()
                    val alarmVol by nativeTileManager.alarmVolume.collectAsState()
                    val notifVol by nativeTileManager.notificationVolume.collectAsState()

                    val allExtraRefractionRegions = remember(qsBlurRegions, notifBlurRegions) {
                        val extraCardRegions = qsBlurRegions
                            .filterKeys { it != "qs_card" }
                            .values
                            .filter { it.isValid }
                        extraCardRegions + notifBlurRegions.filter { it.isValid }
                    }
                    val revealFraction = (backdropAlpha.value / targetBackdropAlpha.coerceAtLeast(0.01f)).coerceIn(0f, 1f)

                    // Stationary Full-Screen Container: Live Screen AGSL Refraction Backdrop + Dim Scrim + Interactive Panel
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { startDismiss() }
                            )
                    ) {
                        LiveScreenRefractionBackdrop(
                            screenBitmap = capturedBackdrop,
                            enableBlur = enableContainerBlur,
                            enableRefraction = glassSupported && glassBackdropMode == GlassBackdropMode.LIQUID,
                            refractionSensitivity = glassRefractionSensitivity,
                            refractionStyle = refractionStyle,
                            refractionScale = glassRefractionScale,
                            refractionDispersion = glassRefractionDispersion,
                            refractionStyleParam = glassRefractionStyleParam,
                            diamondCrossedCut = diamondCrossedCut,
                            blurRadiusDp = containerBlurRadiusDp,
                            transparency = containerBlurTransparency,
                            isDark = isDarkOverlay,
                            revealAlpha = revealFraction,
                            qsCardBounds = qsBlurRegions["qs_card"],
                            extraBlurRegions = allExtraRefractionRegions,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = backdropAlpha.value))
                        )
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
                            CompositionLocalProvider(
                                LocalOnBlurRegionBoundsChanged provides { key, bounds ->
                                    val next = if (bounds.isValid) {
                                        if (qsBlurRegions[key] == bounds) qsBlurRegions else qsBlurRegions + (key to bounds)
                                    } else {
                                        if (key !in qsBlurRegions) qsBlurRegions else qsBlurRegions - key
                                    }
                                    if (next !== qsBlurRegions) qsBlurRegions = next
                                },
                                LocalOnQsCardBoundsChanged provides { bounds ->
                                    val next = if (bounds.isValid) {
                                        if (qsBlurRegions["qs_card"] == bounds) qsBlurRegions else qsBlurRegions + ("qs_card" to bounds)
                                    } else {
                                        if ("qs_card" !in qsBlurRegions) qsBlurRegions else qsBlurRegions - "qs_card"
                                    }
                                    if (next !== qsBlurRegions) qsBlurRegions = next
                                },
                                LocalOnNotificationBlurRegionsChanged provides { regions ->
                                    if (notifBlurRegions != regions) notifBlurRegions = regions
                                }
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
                                        VolumePanelOverlay.show(this@QuickPanelAccessibilityService)
                                    } else {
                                        tileRegistry.handleTileClick(tile)
                                        val config = com.example.data.TileCommandRepository.getConfig(tile.id)
                                        if (tile.id == "screenrecord" || config.closePanelOnPress) {
                                            hideOverlayPanelSmoothly()
                                        }
                                    }
                                },
                                onTileLongClick = {
                                    if (it.id != "screenrecord" && !com.example.system.ComplicationDataHub.opensMicroWindowOnLongClick(it.id)) {
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
                                    QuickPanelNotificationListenerService.launchNotification(item, this@QuickPanelAccessibilityService)
                                    hideOverlayPanelSmoothly()
                                },
                                onNotificationDismiss = { item ->
                                    QuickPanelNotificationListenerService.dismissNotification(item.key)
                                },
                                onNotificationActionClick = { action ->
                                    QuickPanelNotificationListenerService.executeAction(action, this@QuickPanelAccessibilityService)
                                    hideOverlayPanelSmoothly()
                                },
                                onDismissAllNotifications = {
                                    QuickPanelNotificationListenerService.dismissAllNotifications()
                                },
                                onRequestNotificationAccess = {
                                    hideOverlayPanelSmoothly()
                                    QuickPanelNotificationListenerService.openPermissionSettings(this@QuickPanelAccessibilityService)
                                },
                                onNavigateToCustomizer = {
                                    hideOverlayPanelSmoothly()
                                    val intent = Intent(this@QuickPanelAccessibilityService, MainActivity::class.java).apply {
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
        }

        try {
            qsBlurHost.showBehindOverlay()
            qsBlurWindowHost = qsBlurHost
            windowManager?.addView(composeView, panelParams)
            overlayComposeView = composeView
            isPanelShowing = true
            MediaSessionManagerHelper.updateMediaState(force = true)
            if (VolumePanelOverlay.isShowing) {
                VolumePanelOverlay.bringToFront(this)
            }
        } catch (e: Exception) {
            qsBlurHost.dismiss()
            qsBlurWindowHost = null
            Log.e("AccessibilityService", "Failed to show overlay panel: ${e.message}")
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
        qsBlurWindowHost?.dismiss()
        qsBlurWindowHost = null
        lifecycleOwner?.onDestroy()
        lifecycleOwner = null
        isPanelShowing = false
        isPendingShowOverlay = false
        captureGeneration++
        isBackdropCaptureInFlight = false
        lastBackdropCaptureTimeMs = 0L
        triggerStripView?.alpha = 1f
        expansionStage.value = QsExpansionStage.COLLAPSED
        dismissAnimatedCallback = null
        clearCapturedBackdropBitmap()
        com.example.system.ComplicationDataHub.closeMicroWindow()
    }

    private var isSystemReceiverRegistered = false

    override fun onDestroy() {
        runCatching { immersiveModeDetector?.stopMonitoring() }
        immersiveModeDetector = null
        if (isSystemReceiverRegistered) {
            isSystemReceiverRegistered = false
            try {
                unregisterReceiver(systemStateReceiver)
            } catch (_: Throwable) {}
        }
        runCatching { MediaSessionManagerHelper.unregisterClient(CLIENT_TAG) }
        runCatching { destroyOverlayWindow() }
        runCatching { detachAccessibilityTriggerStrip() }
        if (::nativeTileManager.isInitialized) runCatching { nativeTileManager.destroy() }
        if (::externalTileScanner.isInitialized) runCatching { externalTileScanner.destroy() }
        if (::privilegeEngine.isInitialized) runCatching { privilegeEngine.destroy() }
        if (instance === this) {
            instance = null
        }
        runCatching { serviceScope.cancel() }
        super.onDestroy()
    }
}

/**
 * Status bar touch interception view that consumes ACTION_DOWN to intercept
 * gestures before SystemUI handles them.
 */
class AccessibilityStatusTriggerView(context: Context) : View(context) {
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bottomBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val centerPillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sideGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pillRect = RectF()
    private var isPressedState = false
    private var scope: CoroutineScope? = null

    var onSwipeDownDetected: ((isDeepSwipe: Boolean) -> Unit)? = null

    private var startY = 0f
    private var startX = 0f
    private var isDragging = false

    // Immersive Mode 2-Step Gesture State (Supports both Landscape and Portrait Immersive Mode)
    private val isLandscape: Boolean
        get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    fun isImmersiveMode(): Boolean {
        QuickPanelAccessibilityService.instance?.let { service ->
            return service.isImmersiveModeActive()
        }
        return false
    }

    init {
        setWillNotDraw(false)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        scope?.cancel()
        val viewScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        scope = viewScope
        viewScope.launch {
            PanelPreferences.invisibleStatusBarTrigger.collect {
                invalidate()
            }
        }
    }

    override fun onDetachedFromWindow() {
        scope?.cancel()
        scope = null
        super.onDetachedFromWindow()
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
        val service = QuickPanelAccessibilityService.instance
        if (service?.isLockedOrUnlocking() == true) {
            isDragging = false
            setPressedState(false)
            return false
        }
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (keyguardManager?.isKeyguardLocked == true || keyguardManager?.inKeyguardRestrictedInputMode() == true) {
            isDragging = false
            setPressedState(false)
            return false
        }

        val density = resources.displayMetrics.density
        val immediateSwipeThresholdPx = (3 * density).toInt()
        val deepSwipeThresholdPx = (40 * density).toInt()

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                TriggerContainerLayout.recordTopTouch()
                startY = event.rawY
                startX = event.rawX
                isDragging = true
                QuickPanelAccessibilityService.instance?.resetBackdropCaptureForNewGesture()
                QuickPanelAccessibilityService.instance?.notifyUserDragging(true)
                setPressedState(true)
                QuickPanelAccessibilityService.collapseStockNotificationShade()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                TriggerContainerLayout.recordTopTouch()
                QuickPanelAccessibilityService.instance?.notifyUserDragging(true)
                val diffY = event.rawY - startY
                val diffX = Math.abs(event.rawX - startX)

                if (isDragging && diffY >= immediateSwipeThresholdPx && diffY > (diffX * 0.3f)) {
                    TriggerContainerLayout.recordTopSwipeDown()
                    val service = QuickPanelAccessibilityService.instance
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
                    val service = QuickPanelAccessibilityService.instance
                    if (service?.isPanelShowing == true) {
                        TriggerContainerLayout.recordTopSwipeDown()
                        service.onPullDownReleased(diffY, isFlick = diffY >= (10 * density))
                    } else if (diffY >= (4 * density) && diffY > (diffX * 0.3f)) {
                        TriggerContainerLayout.recordTopSwipeDown()
                        service?.showOverlayPanel(if (diffY > deepSwipeThresholdPx) QsExpansionStage.FULL_QS else QsExpansionStage.QQS)
                        onSwipeDownDetected?.invoke(diffY > deepSwipeThresholdPx)
                    }
                }
                setPressedState(false)
                isDragging = false
                startY = 0f
                startX = 0f
                QuickPanelAccessibilityService.instance?.notifyUserDragging(false)
                return true
            }
        }
        return true
    }
}
