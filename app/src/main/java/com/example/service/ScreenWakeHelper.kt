package com.example.service

import android.content.Context
import android.os.PowerManager
import android.util.Log

/**
 * Helper to ensure the screen automatically turns on and stays lit
 * for incoming phone calls and high-priority alerts when the phone is locked or screen is off.
 */
object ScreenWakeHelper {
    private const val TAG = "ScreenWakeHelper"

    @Volatile
    private var activeWakeLock: PowerManager.WakeLock? = null

    /**
     * Wakes up the device screen and keeps it illuminated.
     * Uses SCREEN_BRIGHT_WAKE_LOCK with ACQUIRE_CAUSES_WAKEUP and ON_AFTER_RELEASE
     * to immediately power on the physical display when an incoming call arrives.
     */
    @Synchronized
    fun wakeScreen(context: Context, tag: String = "QuickPanel:IncomingCall", durationMs: Long = 20000L) {
        try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return

            // Release any previously held lock before acquiring a fresh one
            try {
                if (activeWakeLock?.isHeld == true) {
                    activeWakeLock?.release()
                }
            } catch (_: Throwable) {}

            @Suppress("DEPRECATION")
            val wakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                        PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        PowerManager.ON_AFTER_RELEASE,
                tag
            )
            wakeLock.setReferenceCounted(false)
            wakeLock.acquire(durationMs)
            activeWakeLock = wakeLock
            Log.d(TAG, "Screen woke up successfully for '$tag' (duration: ${durationMs}ms)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to wake up screen for '$tag': ${e.message}", e)
        }
    }

    /**
     * Safely releases any currently held WakeLock.
     */
    @Synchronized
    fun releaseWakeLock() {
        try {
            if (activeWakeLock?.isHeld == true) {
                activeWakeLock?.release()
                Log.d(TAG, "Active wakeLock released")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing wakeLock: ${e.message}")
        }
        activeWakeLock = null
    }

    /**
     * Checks if the device screen is currently turned off or non-interactive.
     */
    fun isScreenOff(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return pm?.isInteractive == false
    }

    /**
     * Checks if the keyguard is currently active/locked.
     */
    fun isKeyguardLocked(context: Context): Boolean {
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as? android.app.KeyguardManager
        return km?.isKeyguardLocked == true || km?.inKeyguardRestrictedInputMode() == true
    }
}
