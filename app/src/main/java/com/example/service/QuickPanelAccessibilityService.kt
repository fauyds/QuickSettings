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
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.provider.Settings
import android.util.Log
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
import com.example.ui.overlay.QsExpansionStage
import com.example.ui.overlay.QuickSettingsPanel
import com.example.ui.theme.QuickPanelTheme

/**
 * TrayDev Architecture: AccessibilityService-driven Window Overlay & Universal Tile Interoperability
 * - Manages TYPE_ACCESSIBILITY_OVERLAY windows directly with Z-index above SystemUI.
 * - Universal Accessibility Click Simulation: Scans active window hierarchy and clicks target stock tiles.
 * - Suppresses SystemUI stock shade via performGlobalAction(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE) in < 30ms.
 */
class QuickPanelAccessibilityService : AccessibilityService() {

    companion object {
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

        fun collapseStockNotificationShade(): Boolean {
            var result = false
            val useAutoClosing = PanelPreferences.autoSystemPanelClosing.value

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

                if (service.privilegeEngine.isShizukuGranted()) {
                    service.serviceScope.launch(Dispatchers.IO) {
                        service.privilegeEngine.executeShizukuShell("cmd statusbar collapse; service call statusbar 2")
                    }
                } else if (useAutoClosing && service.privilegeEngine.isRootAvailable.value) {
                    service.serviceScope.launch(Dispatchers.IO) {
                        service.privilegeEngine.executeCommand("cmd statusbar collapse || service call statusbar 2")
                    }
                }
            }

            QuickSettingsOverlayService.instance?.let { overlayService ->
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

                if (overlayService.privilegeEngine.isShizukuGranted()) {
                    overlayService.serviceScope.launch(Dispatchers.IO) {
                        overlayService.privilegeEngine.executeShizukuShell("cmd statusbar collapse; service call statusbar 2")
                    }
                } else if (useAutoClosing && overlayService.privilegeEngine.isRootAvailable.value) {
                    overlayService.serviceScope.launch(Dispatchers.IO) {
                        overlayService.privilegeEngine.executeCommand("cmd statusbar collapse || service call statusbar 2")
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
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var windowManager: WindowManager? = null

    // Window Views
    private var triggerContainerView: TriggerContainerLayout? = null
    private var triggerStripView: AccessibilityStatusTriggerView? = null
    private var triggerLayoutParams: WindowManager.LayoutParams? = null
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

    val pullDownOffset = MutableStateFlow<Float?>(null)

    fun updatePullDownOffset(offset: Float?) {
        pullDownOffset.value = offset
    }

    fun onPullDownReleased(finalDiffY: Float, isFlick: Boolean) {
        pullDownOffset.value = null
    }

    var isInterceptionEnabled = true
        private set

    private var dismissAnimatedCallback: (() -> Unit)? = null
    private var lastShadeInterceptionTime = 0L
    private var lastUnlockTransitionTime = 0L
    private var lastHunEventTime = 0L
    private var lastDetectorCheckTime = 0L

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
        private set
    private var unlockResetJob: Job? = null

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
                    val wPkg = root.packageName?.toString() ?: ""
                    if (wPkg.isNotBlank() && !wPkg.contains("systemui", ignoreCase = true)) {
                        if (launcherPackages.contains(wPkg)) {
                            return true
                        }
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
                        val root = win.root
                        val pkg = root?.packageName?.toString() ?: ""
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
                Intent.ACTION_USER_PRESENT -> {
                    isUnlocking = true
                    lastUnlockTransitionTime = System.currentTimeMillis()
                    unlockResetJob?.cancel()
                    unlockResetJob = serviceScope.launch {
                        delay(800)
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

        // Initialize tile & system managers
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

        com.example.system.SystemThemeObserver.init(this)
        updateLauncherPackages()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction("android.media.VOLUME_CHANGED_ACTION")
            addAction("android.media.STREAM_DEVICES_CHANGED_ACTION")
            addAction("android.media.RINGER_MODE_CHANGED")
        }
        registerReceiver(systemStateReceiver, filter)
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

        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
            immersiveModeDetector?.checkNow()
            val immersive = isImmersiveModeActive()
            updateTriggerVisibility(immersive)
        }

        val pkg = event.packageName?.toString() ?: ""
        val cls = event.className?.toString() ?: ""
        val text = try { event.text.joinToString(" ") } catch (e: Exception) { "" }
        val desc = event.contentDescription?.toString() ?: ""
        val viewId = try {
            val src = event.source
            val id = src?.viewIdResourceName ?: ""
            src?.recycle()
            id
        } catch (e: Exception) { "" }
        val now = System.currentTimeMillis()

        serviceScope.launch(Dispatchers.Default) {
            processEventOptimized(now, pkg, cls, text, desc, viewId)
        }
    }

    private suspend fun processEventOptimized(
        now: Long,
        pkg: String,
        cls: String,
        text: String,
        desc: String,
        viewId: String
    ) {
        if (isPanelShowing || !isInterceptionEnabled) {
            return
        }

        // Keyguard & Immersive Check
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        val isCurrentlyLocked = keyguardManager?.isKeyguardLocked == true || keyguardManager?.inKeyguardRestrictedInputMode() == true
        if (isCurrentlyLocked || isImmersiveModeActive()) {
            return
        }

        // 1. Dynamic Custom Blacklist Check:
        val blacklistPkgs = PanelPreferences.customBlacklistPackages.value
        val blacklistClasses = PanelPreferences.customBlacklistClasses.value

        val isBlacklisted = blacklistPkgs.any { bp -> bp.isNotBlank() && pkg.contains(bp, ignoreCase = true) } ||
                blacklistClasses.any { bc -> bc.isNotBlank() && cls.contains(bc, ignoreCase = true) }

        if (isBlacklisted) {
            return
        }

        // 2. Strict Whitelist by Default:
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
                val rPkg = rootNode.packageName?.toString() ?: ""
                val rCls = rootNode.className?.toString() ?: ""
                if (rPkg == "com.android.keyguard" ||
                    rPkg.contains("biometric", ignoreCase = true) ||
                    rCls.contains("KeyguardBouncer", ignoreCase = true) ||
                    rCls.contains("Bouncer", ignoreCase = true) ||
                    rCls.contains("Biometric", ignoreCase = true)
                ) {
                    return true
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val activeWins = windows
                for (window in activeWins) {
                    val wRoot = window.root ?: continue
                    val wPkg = wRoot.packageName?.toString() ?: ""
                    val wCls = wRoot.className?.toString() ?: ""
                    if (wPkg == "com.android.keyguard" ||
                        wPkg.contains("biometric", ignoreCase = true) ||
                        wCls.contains("KeyguardBouncer", ignoreCase = true) ||
                        wCls.contains("Bouncer", ignoreCase = true) ||
                        wCls.contains("Biometric", ignoreCase = true)
                    ) {
                        return true
                    }
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
            if (isNodeCoveringThreshold(source)) return true

            var parent = source.parent
            var depth = 0
            while (parent != null && depth < 4) {
                if (isNodeCoveringThreshold(parent)) {
                    parent.recycle()
                    return true
                }
                val next = parent.parent
                parent.recycle()
                parent = next
                depth++
            }

            val childCount = source.childCount.coerceAtMost(8)
            for (i in 0 until childCount) {
                val child = source.getChild(i) ?: continue
                if (isNodeCoveringThreshold(child)) {
                    child.recycle()
                    return true
                }
                child.recycle()
            }
        }

        try {
            val root = rootInActiveWindow
            if (root != null) {
                if (isNodeCoveringThreshold(root)) return true
                val rootChildCount = root.childCount.coerceAtMost(10)
                for (i in 0 until rootChildCount) {
                    val child = root.getChild(i) ?: continue
                    if (isNodeCoveringThreshold(child)) {
                        child.recycle()
                        return true
                    }
                    child.recycle()
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
        return checkNodeTreeForShadeTargets(source)
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
                val clicked = searchAndClickInNode(root, tileLabel, keywords, specifier)
                if (clicked) {
                    collapseStockNotificationShade()
                    return true
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val winList = windows
                for (window in winList) {
                    val winRoot = window.root ?: continue
                    val clicked = searchAndClickInNode(winRoot, tileLabel, keywords, specifier)
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
            var clickableNode: AccessibilityNodeInfo? = node
            while (clickableNode != null && !clickableNode.isClickable) {
                clickableNode = clickableNode.parent
            }
            val target = clickableNode ?: node
            val didClick = target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            if (didClick) {
                Log.d("AccessibilityService", "Successfully clicked stock tile for term '$tileLabel'")
                return true
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (searchAndClickInNode(child, tileLabel, keywords, specifier)) {
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
                val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                if (keyguardManager?.isKeyguardLocked != true && keyguardManager?.inKeyguardRestrictedInputMode() != true) {
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
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            if (keyguardManager?.isKeyguardLocked != true && keyguardManager?.inKeyguardRestrictedInputMode() != true) {
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
        Handler(Looper.getMainLooper()).post {
            triggerContainerView?.updateChildVisibility(isImmersive)
        }
    }

    fun detachAccessibilityTriggerStrip() {
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
        if (isPanelShowing && overlayComposeView != null) {
            return
        }

        triggerContainerView?.cancelTransientReveal()
        collapseStockNotificationShade()
        serviceScope.launch {
            delay(60)
            collapseStockNotificationShade()
            delay(100)
            collapseStockNotificationShade()
        }

        expansionStage.value = initialStage
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
                    val isNotificationPermGranted = remember { QuickPanelNotificationListenerService.isPermissionGranted(this@QuickPanelAccessibilityService) }
                    val brightness by nativeTileManager.brightness.collectAsState()
                    val mediaVol by nativeTileManager.mediaVolume.collectAsState()
                    val ringVol by nativeTileManager.ringVolume.collectAsState()
                    val alarmVol by nativeTileManager.alarmVolume.collectAsState()
                    val notifVol by nativeTileManager.notificationVolume.collectAsState()

                    // Stationary Full-Screen Dim Backdrop (Never moves up or down when panel/notifications move)
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

        try {
            windowManager?.addView(composeView, panelParams)
            overlayComposeView = composeView
            isPanelShowing = true
            if (VolumePanelOverlay.isShowing) {
                VolumePanelOverlay.bringToFront(this)
            }
        } catch (e: Exception) {
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
        lifecycleOwner?.onDestroy()
        lifecycleOwner = null
        isPanelShowing = false
        expansionStage.value = QsExpansionStage.COLLAPSED
        dismissAnimatedCallback = null
    }

    override fun onDestroy() {
        super.onDestroy()
        immersiveModeDetector?.stopMonitoring()
        immersiveModeDetector = null
        try {
            unregisterReceiver(systemStateReceiver)
        } catch (e: Exception) {
            // Ignore
        }
        MediaSessionManagerHelper.unregisterAll()
        destroyOverlayWindow()
        detachAccessibilityTriggerStrip()
        instance = null
        serviceScope.cancel()
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
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

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
                QuickPanelAccessibilityService.instance?.notifyUserDragging(true)
                setPressedState(true)
                QuickPanelAccessibilityService.collapseStockNotificationShade()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                QuickPanelAccessibilityService.instance?.notifyUserDragging(true)
                val diffY = event.rawY - startY
                val diffX = Math.abs(event.rawX - startX)

                if (isDragging && diffY >= immediateSwipeThresholdPx && diffY > (diffX * 0.3f)) {
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
                QuickPanelAccessibilityService.instance?.notifyUserDragging(false)
                return true
            }
        }
        return true
    }
}
