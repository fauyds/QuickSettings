package com.example.data

import android.content.Context
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.model.ComplicationCommandConfig
import com.example.model.ComplicationTriggerBehavior
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Immutable
data class KnownComplicationDefinition(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val defaultTapDescription: String,
    val defaultLongPressDescription: String,
    val defaultShellOn: String,
    val defaultShellOff: String,
    val defaultIntentAction: String
)

object ComplicationCommandRepository {
    private const val PREFS_NAME = "complication_commands_prefs"

    @Volatile
    private var appContext: Context? = null

    val knownComplications: List<KnownComplicationDefinition> = listOf(
        KnownComplicationDefinition(
            id = "comp_bt_battery",
            title = "BT Battery",
            icon = Icons.Default.Bluetooth,
            defaultTapDescription = "Toggles Bluetooth ON / OFF",
            defaultLongPressDescription = "Opens Bluetooth complication menu",
            defaultShellOn = "cmd bluetooth_manager enable",
            defaultShellOff = "cmd bluetooth_manager disable",
            defaultIntentAction = Settings.ACTION_BLUETOOTH_SETTINGS
        ),
        KnownComplicationDefinition(
            id = "comp_volume",
            title = "Volume",
            icon = Icons.Default.VolumeUp,
            defaultTapDescription = "Toggles Sounds ON / OFF",
            defaultLongPressDescription = "Opens Sound & Volume menu",
            defaultShellOn = "cmd audio set-volume 3 100",
            defaultShellOff = "cmd audio set-volume 3 0",
            defaultIntentAction = Settings.ACTION_SOUND_SETTINGS
        ),
        KnownComplicationDefinition(
            id = "comp_battery",
            title = "Battery",
            icon = Icons.Default.BatteryFull,
            defaultTapDescription = "Toggles Power Save ON / OFF",
            defaultLongPressDescription = "Opens Battery complication menu",
            defaultShellOn = "cmd power set-mode 1",
            defaultShellOff = "cmd power set-mode 0",
            defaultIntentAction = Settings.ACTION_BATTERY_SAVER_SETTINGS
        ),
        KnownComplicationDefinition(
            id = "comp_signal",
            title = "Signal",
            icon = Icons.Default.SignalCellularAlt,
            defaultTapDescription = "Toggles Wi-Fi / Mobile Data ON / OFF",
            defaultLongPressDescription = "Opens Signal Quality menu",
            defaultShellOn = "svc wifi enable",
            defaultShellOff = "svc wifi disable",
            defaultIntentAction = Settings.Panel.ACTION_INTERNET_CONNECTIVITY
        ),
        KnownComplicationDefinition(
            id = "comp_net_speed",
            title = "Net Speed",
            icon = Icons.Default.Speed,
            defaultTapDescription = "Toggles Wi-Fi / Mobile Data ON / OFF",
            defaultLongPressDescription = "Opens Network Speed menu",
            defaultShellOn = "svc wifi enable",
            defaultShellOff = "svc wifi disable",
            defaultIntentAction = Settings.Panel.ACTION_INTERNET_CONNECTIVITY
        ),
        KnownComplicationDefinition(
            id = "comp_download",
            title = "Download",
            icon = Icons.Default.Download,
            defaultTapDescription = "Opens the app downloading the file",
            defaultLongPressDescription = "Opens Active Downloads menu",
            defaultShellOn = "am start -a android.intent.action.VIEW_DOWNLOADS",
            defaultShellOff = "",
            defaultIntentAction = "android.intent.action.VIEW_DOWNLOADS"
        ),
        KnownComplicationDefinition(
            id = "comp_media_progress",
            title = "Media",
            icon = Icons.Default.MusicNote,
            defaultTapDescription = "Opens the current playing session",
            defaultLongPressDescription = "Opens Media Player menu",
            defaultShellOn = "input keyevent 85",
            defaultShellOff = "",
            defaultIntentAction = Settings.ACTION_SOUND_SETTINGS
        ),
        KnownComplicationDefinition(
            id = "comp_brightness",
            title = "Brightness",
            icon = Icons.Default.BrightnessHigh,
            defaultTapDescription = "Toggles Auto-Brightness ON / OFF",
            defaultLongPressDescription = "Opens Brightness Slider menu",
            defaultShellOn = "settings put system screen_brightness_mode 1",
            defaultShellOff = "settings put system screen_brightness_mode 0",
            defaultIntentAction = Settings.ACTION_DISPLAY_SETTINGS
        ),
        KnownComplicationDefinition(
            id = "comp_storage",
            title = "Storage",
            icon = Icons.Default.Storage,
            defaultTapDescription = "Opens Internal Storage settings",
            defaultLongPressDescription = "Opens Storage complication menu",
            defaultShellOn = "am start -a android.settings.INTERNAL_STORAGE_SETTINGS",
            defaultShellOff = "",
            defaultIntentAction = Settings.ACTION_INTERNAL_STORAGE_SETTINGS
        ),
        KnownComplicationDefinition(
            id = "comp_cpu",
            title = "CPU",
            icon = Icons.Default.Memory,
            defaultTapDescription = "Opens CPU Monitor menu",
            defaultLongPressDescription = "Opens CPU Monitor menu",
            defaultShellOn = "am start -a android.settings.APPLICATION_DEVELOPMENT_SETTINGS",
            defaultShellOff = "",
            defaultIntentAction = Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
        ),
        KnownComplicationDefinition(
            id = "comp_ram",
            title = "RAM",
            icon = Icons.Default.Storage,
            defaultTapDescription = "Opens Memory Monitor menu",
            defaultLongPressDescription = "Opens Memory Monitor menu",
            defaultShellOn = "am start -a android.settings.DEVICE_INFO_SETTINGS",
            defaultShellOff = "",
            defaultIntentAction = Settings.ACTION_DEVICE_INFO_SETTINGS
        ),
        KnownComplicationDefinition(
            id = "comp_timer",
            title = "Timer",
            icon = Icons.Default.Timer,
            defaultTapDescription = "Toggles Timer (dismisses finished timer)",
            defaultLongPressDescription = "Opens Timer menu (+1m if finished)",
            defaultShellOn = "am start -a android.intent.action.SHOW_TIMERS",
            defaultShellOff = "",
            defaultIntentAction = Settings.ACTION_DATE_SETTINGS
        ),
        KnownComplicationDefinition(
            id = "comp_stopwatch",
            title = "Stopwatch",
            icon = Icons.Default.AvTimer,
            defaultTapDescription = "Starts / Pauses Stopwatch",
            defaultLongPressDescription = "Opens Stopwatch menu",
            defaultShellOn = "am start -a android.intent.action.SHOW_TIMERS",
            defaultShellOff = "",
            defaultIntentAction = Settings.ACTION_DATE_SETTINGS
        ),
        KnownComplicationDefinition(
            id = "comp_countdown",
            title = "Countdown",
            icon = Icons.Default.Event,
            defaultTapDescription = "Opens Event Countdown menu",
            defaultLongPressDescription = "Opens Event Countdown menu",
            defaultShellOn = "am start -a android.settings.DATE_SETTINGS",
            defaultShellOff = "",
            defaultIntentAction = Settings.ACTION_DATE_SETTINGS
        ),
        KnownComplicationDefinition(
            id = "comp_lux",
            title = "Lux Meter",
            icon = Icons.Default.WbSunny,
            defaultTapDescription = "Opens Ambient Light menu",
            defaultLongPressDescription = "Opens Ambient Light menu",
            defaultShellOn = "am start -a android.settings.DISPLAY_SETTINGS",
            defaultShellOff = "",
            defaultIntentAction = Settings.ACTION_DISPLAY_SETTINGS
        )
    )

    private val _configs = MutableStateFlow<Map<String, ComplicationCommandConfig>>(emptyMap())
    val configs: StateFlow<Map<String, ComplicationCommandConfig>> = _configs.asStateFlow()

    private fun readConfigFromPrefs(
        prefs: android.content.SharedPreferences,
        def: KnownComplicationDefinition
    ): ComplicationCommandConfig {
        val id = def.id
        return ComplicationCommandConfig(
            tileId = id,
            tapBehavior = ComplicationTriggerBehavior.fromId(prefs.getString("tap_behavior_$id", ComplicationTriggerBehavior.DEFAULT.id)),
            tapShellCommand = prefs.getString("tap_shell_$id", def.defaultShellOn) ?: def.defaultShellOn,
            tapShellCommandOff = prefs.getString("tap_shell_off_$id", def.defaultShellOff) ?: def.defaultShellOff,
            tapIntentAction = prefs.getString("tap_intent_action_$id", def.defaultIntentAction) ?: def.defaultIntentAction,
            tapIntentPackage = prefs.getString("tap_intent_pkg_$id", "") ?: "",
            tapIntentComponent = prefs.getString("tap_intent_comp_$id", "") ?: "",
            longPressBehavior = ComplicationTriggerBehavior.fromId(prefs.getString("lp_behavior_$id", ComplicationTriggerBehavior.DEFAULT.id)),
            longPressShellCommand = prefs.getString("lp_shell_$id", def.defaultShellOn) ?: def.defaultShellOn,
            longPressIntentAction = prefs.getString("lp_intent_action_$id", def.defaultIntentAction) ?: def.defaultIntentAction,
            longPressIntentPackage = prefs.getString("lp_intent_pkg_$id", "") ?: "",
            longPressIntentComponent = prefs.getString("lp_intent_comp_$id", "") ?: ""
        )
    }

    private val initLock = Any()
    @Volatile
    private var isInitialized = false

    fun init(context: Context, forceRefresh: Boolean = false) {
        if (isInitialized && !forceRefresh) return
        synchronized(initLock) {
            if (isInitialized && !forceRefresh) return
            val appCtx = context.applicationContext
            appContext = appCtx
            val prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val map = mutableMapOf<String, ComplicationCommandConfig>()
            for (def in knownComplications) {
                map[def.id] = readConfigFromPrefs(prefs, def)
            }
            _configs.value = map
            isInitialized = true
        }
    }

    fun getConfig(tileId: String, context: Context? = null): ComplicationCommandConfig {
        _configs.value[tileId]?.let { return it }
        val ctx = context?.applicationContext ?: appContext
        if (ctx != null && _configs.value.isEmpty()) {
            init(ctx)
            _configs.value[tileId]?.let { return it }
        }
        val def = knownComplications.firstOrNull { it.id == tileId }
        return if (def != null) {
            ComplicationCommandConfig(
                tileId = tileId,
                tapShellCommand = def.defaultShellOn,
                tapShellCommandOff = def.defaultShellOff,
                tapIntentAction = def.defaultIntentAction,
                longPressShellCommand = def.defaultShellOn,
                longPressIntentAction = def.defaultIntentAction
            )
        } else {
            ComplicationCommandConfig(tileId = tileId)
        }
    }

    fun saveConfig(context: Context, config: ComplicationCommandConfig) {
        val appCtx = context.applicationContext
        appContext = appCtx
        val prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = config.tileId
        prefs.edit()
            .putString("tap_behavior_$id", config.tapBehavior.id)
            .putString("tap_shell_$id", config.tapShellCommand)
            .putString("tap_shell_off_$id", config.tapShellCommandOff)
            .putString("tap_intent_action_$id", config.tapIntentAction)
            .putString("tap_intent_pkg_$id", config.tapIntentPackage)
            .putString("tap_intent_comp_$id", config.tapIntentComponent)
            .putString("lp_behavior_$id", config.longPressBehavior.id)
            .putString("lp_shell_$id", config.longPressShellCommand)
            .putString("lp_intent_action_$id", config.longPressIntentAction)
            .putString("lp_intent_pkg_$id", config.longPressIntentPackage)
            .putString("lp_intent_comp_$id", config.longPressIntentComponent)
            .apply()

        val updated = _configs.value.toMutableMap()
        updated[id] = config
        _configs.value = updated
    }

    fun resetConfig(context: Context, tileId: String) {
        val def = knownComplications.firstOrNull { it.id == tileId }
        val reset = ComplicationCommandConfig(
            tileId = tileId,
            tapBehavior = ComplicationTriggerBehavior.DEFAULT,
            tapShellCommand = def?.defaultShellOn ?: "",
            tapShellCommandOff = def?.defaultShellOff ?: "",
            tapIntentAction = def?.defaultIntentAction ?: "",
            tapIntentPackage = "",
            tapIntentComponent = "",
            longPressBehavior = ComplicationTriggerBehavior.DEFAULT,
            longPressShellCommand = def?.defaultShellOn ?: "",
            longPressIntentAction = def?.defaultIntentAction ?: "",
            longPressIntentPackage = "",
            longPressIntentComponent = ""
        )
        saveConfig(context, reset)
    }

    fun setAllTapBehavior(
        context: Context,
        behavior: ComplicationTriggerBehavior
    ) {
        init(context)
        val current = _configs.value
        for (def in knownComplications) {
            val existing = current[def.id] ?: getConfig(def.id, context)
            saveConfig(context, existing.copy(tapBehavior = behavior))
        }
    }

    fun resetAll(context: Context) {
        for (def in knownComplications) {
            resetConfig(context, def.id)
        }
    }
}
