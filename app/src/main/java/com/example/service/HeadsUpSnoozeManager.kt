package com.example.service

import java.util.concurrent.ConcurrentHashMap

/**
 * Manages temporary app-level and global snooze states for Heads-Up notification overlays.
 * Incoming calls and alarms are strictly exempt and will bypass any active snooze.
 */
object HeadsUpSnoozeManager {
    private val snoozedApps = ConcurrentHashMap<String, Long>()
    
    @Volatile
    var globalSnoozeUntilMillis: Long = 0L

    /**
     * Temporarily snoozes all heads-up banners from a specific package for [durationMinutes].
     */
    fun snoozePackage(packageName: String, durationMinutes: Int) {
        val expireTime = System.currentTimeMillis() + (durationMinutes.coerceAtLeast(1) * 60 * 1000L)
        snoozedApps[packageName] = expireTime
    }

    /**
     * Temporarily snoozes all incoming heads-up banners globally for [durationMinutes],
     * with incoming calls and alarms strictly exempt.
     */
    fun snoozeAll(durationMinutes: Int) {
        globalSnoozeUntilMillis = System.currentTimeMillis() + (durationMinutes.coerceAtLeast(1) * 60 * 1000L)
    }

    /**
     * Checks if heads-up banner display is currently snoozed for [packageName].
     * Critical exemptions: Calls and alarms always return false (never snoozed).
     */
    fun isSnoozed(packageName: String, isCall: Boolean = false, isAlarm: Boolean = false): Boolean {
        // Critical exemptions: Calls and alarms always bypass snooze
        if (isCall || isAlarm) return false

        val now = System.currentTimeMillis()

        // 1. Check Global Snooze
        if (now < globalSnoozeUntilMillis) {
            return true
        } else if (globalSnoozeUntilMillis != 0L) {
            globalSnoozeUntilMillis = 0L
        }

        // 2. Check App-Level Snooze
        val appExpires = snoozedApps[packageName]
        if (appExpires != null) {
            if (now < appExpires) {
                return true
            } else {
                snoozedApps.remove(packageName)
            }
        }

        return false
    }

    /**
     * Cancels any active snooze for [packageName], or for all apps if [packageName] is null.
     */
    fun clearSnooze(packageName: String? = null) {
        if (packageName != null) {
            snoozedApps.remove(packageName)
        } else {
            snoozedApps.clear()
            globalSnoozeUntilMillis = 0L
        }
    }

    fun isGlobalSnoozed(): Boolean = System.currentTimeMillis() < globalSnoozeUntilMillis

    fun getSnoozedApps(): Map<String, Long> {
        val now = System.currentTimeMillis()
        snoozedApps.entries.removeIf { it.value <= now }
        return snoozedApps.toMap()
    }
}
