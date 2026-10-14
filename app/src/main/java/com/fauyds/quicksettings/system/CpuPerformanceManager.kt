package com.fauyds.quicksettings.system

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PerformanceHintManager
import android.os.Process
import android.util.Log
import android.view.Display
import androidx.annotation.RequiresApi

/**
 * Native Android Dynamic Performance Framework (ADPF) and EAS Core Scheduler Manager:
 *
 * Implements Method 2 (Zero-Root / Non-Root Native Android):
 * 1. Process.THREAD_PRIORITY_URGENT_DISPLAY (Nice level -8) to signal the Linux Energy Aware Scheduler (EAS)
 *    to migrate the critical UI/Render thread onto the Prime (Cortex-X / Gold+) core cluster.
 * 2. PerformanceHintManager (Android 12+ / API 31+) Hint Sessions to interact directly with the
 *    underlying PowerHAL and frequency governor, boosting clock speeds and scheduling priority for interactive frames.
 */
class CpuPerformanceManager private constructor(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var hintSession: Any? = null // Holds PerformanceHintManager.Session on API 31+
    private var targetWorkDurationNanos: Long = 16_666_666L // Default 60Hz (16.6ms)
    private var isSessionActive = false

    init {
        acquireUrgentPriority()
        initPerformanceHintSession()
        val engine = com.fauyds.quicksettings.system.NativeTileManager.instance?.privilegeEngine ?: PrivilegeEngine(context)
        checkAndApplyAffinityIfEnabled(engine)
    }

    /**
     * Enforces URGENT_DISPLAY priority for the current calling thread (UI/Render thread).
     */
    fun acquireUrgentPriority() {
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY)
        } catch (e: Exception) {
            Log.w(TAG, "Could not set THREAD_PRIORITY_URGENT_DISPLAY: ${e.message}")
        }
    }

    /**
     * Checks if Prime Core taskset affinity is enabled in preferences and applies it via PrivilegeEngine.
     */
    fun checkAndApplyAffinityIfEnabled(engine: PrivilegeEngine?) {
        try {
            if (com.fauyds.quicksettings.data.PanelPreferences.usePrimeCoreAffinity.value && engine != null && engine.isShizukuGranted()) {
                engine.applyPrimeCoreAffinity(true)
            }
        } catch (_: Exception) {}
    }

    /**
     * Initializes ADPF PerformanceHintManager session on Android 12+ (API 31+).
     */
    private fun initPerformanceHintSession() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val phm = context.getSystemService(PerformanceHintManager::class.java)
                if (phm != null) {
                    val refreshRate = getDisplayRefreshRate()
                    targetWorkDurationNanos = if (refreshRate > 0) {
                        (1_000_000_000L / refreshRate.toLong()).coerceIn(4_000_000L, 20_000_000L)
                    } else {
                        16_666_666L
                    }

                    // Register main thread TID into ADPF Hint Session
                    val mainTid = Process.myTid()
                    val session = phm.createHintSession(intArrayOf(mainTid), targetWorkDurationNanos)
                    hintSession = session
                    isSessionActive = session != null
                    Log.i(TAG, "ADPF HintSession initialized for TID $mainTid (Target: ${targetWorkDurationNanos / 1_000_000}ms @ ${refreshRate}Hz)")
                }
            } catch (e: Exception) {
                Log.w(TAG, "PerformanceHintManager initialization failed: ${e.message}")
            }
        }
    }

    /**
     * Called when a drag or interactive gesture begins.
     * Informs PowerHAL and scheduler to prioritize the Prime core.
     */
    fun onInteractionStart() {
        acquireUrgentPriority()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val session = hintSession as? PerformanceHintManager.Session ?: return
                // Report target duration workload to request immediate EAS clock & core scaling
                session.reportActualWorkDuration(targetWorkDurationNanos)
            } catch (e: Exception) {
                Log.d(TAG, "Interaction hint boost error: ${e.message}")
            }
        }
    }

    /**
     * Reports actual frame work duration during continuous gestures (e.g. shade drag).
     */
    fun reportFrameDuration(durationNanos: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val session = hintSession as? PerformanceHintManager.Session ?: return
                val clamped = durationNanos.coerceAtLeast(1_000_000L)
                session.reportActualWorkDuration(clamped)
            } catch (e: Exception) {
                // Ignore transient frame reporting exceptions
            }
        }
    }

    /**
     * Called when a gesture finishes to return scheduling to standard power-saving balance.
     */
    fun onInteractionEnd() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val session = hintSession as? PerformanceHintManager.Session ?: return
                // Report low duration to signal frame completion
                session.reportActualWorkDuration(targetWorkDurationNanos / 2)
            } catch (e: Exception) {
                Log.d(TAG, "Interaction end hint error: ${e.message}")
            }
        }
    }

    /**
     * Calculates the device display refresh rate (e.g. 60Hz, 90Hz, 120Hz, 144Hz).
     */
    private fun getDisplayRefreshRate(): Float {
        return try {
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            val defaultDisplay = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)
            defaultDisplay?.refreshRate ?: 60f
        } catch (_: Exception) {
            60f
        }
    }

    companion object {
        private const val TAG = "CpuPerformanceManager"

        @Volatile
        private var instance: CpuPerformanceManager? = null

        fun getInstance(context: Context): CpuPerformanceManager {
            return instance ?: synchronized(this) {
                instance ?: CpuPerformanceManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
