package com.example

import android.app.Application
import android.content.Context
import com.example.data.ComplicationCommandRepository
import com.example.data.NotificationBlacklistManager
import com.example.data.NotificationCategoryManager
import com.example.data.PanelPreferences
import com.example.data.ThemeManager
import com.example.data.TileCommandRepository
import com.example.system.ComplicationDataHub
import com.example.system.SystemThemeObserver

object AppRuntimeInitializer {
    private val initLock = Any()

    @Volatile
    private var initialized = false

    fun ensureInitialized(context: Context) {
        if (initialized) return
        synchronized(initLock) {
            if (initialized) return
            val appCtx = context.applicationContext
            runCatching { PanelPreferences.init(appCtx) }
            runCatching { TileCommandRepository.init(appCtx) }
            runCatching { ComplicationCommandRepository.init(appCtx) }
            runCatching { NotificationBlacklistManager.init(appCtx) }
            runCatching { NotificationCategoryManager.init(appCtx) }
            runCatching { ThemeManager.init(appCtx) }
            runCatching { SystemThemeObserver.init(appCtx) }
            runCatching { ComplicationDataHub.init(appCtx) }
            initialized = true
        }
    }
}

class QuickPanelApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppRuntimeInitializer.ensureInitialized(this)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_BACKGROUND || level == TRIM_MEMORY_RUNNING_LOW || level == TRIM_MEMORY_UI_HIDDEN) {
            runCatching { com.example.system.MemoryTrimCoordinator.performSafeTrim(this) }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        runCatching { com.example.system.MemoryTrimCoordinator.performSafeTrim(this) }
    }
}

