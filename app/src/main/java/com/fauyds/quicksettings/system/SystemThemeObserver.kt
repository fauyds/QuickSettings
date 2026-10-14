package com.fauyds.quicksettings.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SystemThemeObserver {
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private var initialized = false
    private var contentObserver: ContentObserver? = null
    private var broadcastReceiver: BroadcastReceiver? = null

    fun init(context: Context) {
        val appContext = context.applicationContext
        val initialDark = detectSystemDarkMode(appContext)
        _isDarkTheme.value = initialDark

        if (initialized) return
        initialized = true

        val handler = Handler(Looper.getMainLooper())

        // 1. ContentObserver for ui_night_mode in Settings.Secure
        try {
            val observer = object : ContentObserver(handler) {
                override fun onChange(selfChange: Boolean) {
                    val dark = detectSystemDarkMode(appContext)
                    _isDarkTheme.value = dark
                }
            }
            appContext.contentResolver.registerContentObserver(
                Settings.Secure.getUriFor("ui_night_mode"),
                true,
                observer
            )
            contentObserver = observer
        } catch (e: Exception) {
            Log.w("SystemThemeObserver", "Failed to register ui_night_mode observer: ${e.message}")
        }

        // 2. BroadcastReceiver for ACTION_CONFIGURATION_CHANGED
        try {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    val dark = detectSystemDarkMode(appContext)
                    _isDarkTheme.value = dark
                }
            }
            val filter = IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED)
            appContext.registerReceiver(receiver, filter)
            broadcastReceiver = receiver
        } catch (e: Exception) {
            Log.w("SystemThemeObserver", "Failed to register configuration receiver: ${e.message}")
        }
    }

    fun onConfigurationChanged(context: Context, newConfig: Configuration) {
        init(context)
        val isDark = (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        _isDarkTheme.value = isDark
    }

    fun detectSystemDarkMode(context: Context): Boolean {
        return try {
            val nightModeSetting = Settings.Secure.getInt(context.contentResolver, "ui_night_mode", -1)
            when (nightModeSetting) {
                1 -> false // UiModeManager.MODE_NIGHT_NO
                2 -> true  // UiModeManager.MODE_NIGHT_YES
                else -> {
                    val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                    uiMode == Configuration.UI_MODE_NIGHT_YES
                }
            }
        } catch (e: Exception) {
            val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            uiMode == Configuration.UI_MODE_NIGHT_YES
        }
    }
}
