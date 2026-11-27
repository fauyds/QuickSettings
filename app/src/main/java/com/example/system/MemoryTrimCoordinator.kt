package com.example.system

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.util.Log
import com.example.service.HeadsUpOverlayManager
import com.example.service.MediaSessionManagerHelper
import com.example.service.QuickPanelAccessibilityService
import com.example.service.QuickPanelNotificationListenerService
import com.example.service.QuickSettingsOverlayService
import com.example.service.VolumePanelOverlay
import com.example.ui.overlay.clearAppIconBitmapCache

object MemoryTrimCoordinator {
    private const val TAG = "MemoryTrimCoordinator"
    private const val SOFT_MEMORY_THRESHOLD_MB = 290L
    private const val TRIM_COOLDOWN_MS = 60_000L
    private var lastTrimTimestamp = 0L

    fun checkSoftThresholdThrottled(context: Context, source: String) {
        val now = System.currentTimeMillis()
        if (now - lastTrimTimestamp < TRIM_COOLDOWN_MS) return

        val appCtx = context.applicationContext
        val am = appCtx.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(memInfo)

        val pssMb = try {
            val debugMem = Debug.MemoryInfo()
            Debug.getMemoryInfo(debugMem)
            debugMem.totalPss / 1024L
        } catch (_: Throwable) {
            val runtime = Runtime.getRuntime()
            (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L)
        }

        if (pssMb >= SOFT_MEMORY_THRESHOLD_MB) {
            val isUiActive = QuickPanelAccessibilityService.instance?.isPanelShowing == true ||
                QuickSettingsOverlayService.instance?.isPanelShowing == true ||
                VolumePanelOverlay.isShowing ||
                HeadsUpOverlayManager.currentDataFlow.value != null

            if (!isUiActive) {
                lastTrimTimestamp = now
                Log.d(TAG, "Soft RAM threshold reached ($pssMb MB >= $SOFT_MEMORY_THRESHOLD_MB MB) via $source while UI idle. Performing safe cache trim...")
                performSafeTrim(appCtx)
            }
        }
    }

    fun performSafeTrim(context: Context) {
        try {
            QuickPanelAccessibilityService.instance?.clearCapturedBackdropIfInactive()
        } catch (_: Throwable) {}

        try {
            MediaSessionManagerHelper.trimCaches(releaseArtwork = true)
        } catch (_: Throwable) {}

        try {
            clearAppIconBitmapCache()
            ExternalTileIconLoader.clearCache()
        } catch (_: Throwable) {}

        try {
            QuickPanelNotificationListenerService.trimCaches()
        } catch (_: Throwable) {}

        try {
            HeadsUpOverlayManager.trimCaches()
        } catch (_: Throwable) {}

        try {
            PrivilegeEngine.trimIdleRootSessionIfNeeded(minIdleMs = 10_000L)
        } catch (_: Throwable) {}

        try {
            System.gc()
            Runtime.getRuntime().gc()
        } catch (_: Throwable) {}
    }
}
