package com.example.data

import android.content.Context
import com.example.model.CommandExecutionMode
import com.example.model.TileActionOverrideType
import com.example.model.TileCommandConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TileCommandRepository {
    private const val PREFS_NAME = "tile_custom_commands_prefs"
    private var appContext: Context? = null

    private val _tileConfigs = MutableStateFlow<Map<String, TileCommandConfig>>(emptyMap())
    val tileConfigs: StateFlow<Map<String, TileCommandConfig>> = _tileConfigs.asStateFlow()

    val defaultTilesList = listOf(
        "wifi" to ("svc wifi enable" to "svc wifi disable"),
        "bluetooth" to ("cmd bluetooth_manager enable" to "cmd bluetooth_manager disable"),
        "torch" to ("" to ""),
        "cellular" to ("svc data enable" to "svc data disable"),
        "airplane" to ("cmd connectivity airplane-mode enable" to "cmd connectivity airplane-mode disable"),
        "nfc" to ("svc nfc enable" to "svc nfc disable"),
        "autorotate" to ("settings put system accelerometer_rotation 1" to "settings put system accelerometer_rotation 0"),
        "location" to ("settings put secure location_mode 3" to "settings put secure location_mode 0"),
        "hotspot" to ("cmd connectivity tether startwifi" to "cmd connectivity tether stopwifi"),
        "battery_saver" to ("cmd power set-mode 1" to "cmd power set-mode 0"),
        "dnd" to ("cmd notification set_dnd 1" to "cmd notification set_dnd 0"),
        "dark_mode" to ("cmd uimode night yes" to "cmd uimode night no"),
        "aod" to ("settings put secure doze_always_on 1" to "settings put secure doze_always_on 0"),
        "ambient_display" to ("settings put secure doze_enabled 1" to "settings put secure doze_enabled 0"),
        "caffeine" to ("settings put system screen_off_timeout 2147483647" to "settings put system screen_off_timeout 30000"),
        "screenshot" to ("input keyevent 120" to "input keyevent 120"),
        "screenrecord" to ("screenrecord --time-limit 15 /sdcard/screenrecord.mp4 &" to "pkill -2 screenrecord")
    )

    fun init(context: Context) {
        appContext = context.applicationContext
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val map = mutableMapOf<String, TileCommandConfig>()

        for ((tileId, defaultCmds) in defaultTilesList) {
            val enabled = prefs.getBoolean("override_$tileId", false)
            val typeStr = prefs.getString("type_$tileId", TileActionOverrideType.DEFAULT.id)
            val overrideType = TileActionOverrideType.fromId(typeStr)
            val execModeStr = prefs.getString("mode_$tileId", CommandExecutionMode.STANDARD.id)
            val mode = CommandExecutionMode.fromId(execModeStr)
            val cmdOn = prefs.getString("cmd_on_$tileId", defaultCmds.first) ?: defaultCmds.first
            val cmdOff = prefs.getString("cmd_off_$tileId", defaultCmds.second) ?: defaultCmds.second
            val intentAction = prefs.getString("intent_action_$tileId", "") ?: ""
            val intentPackage = prefs.getString("intent_pkg_$tileId", "") ?: ""
            val intentComponent = prefs.getString("intent_comp_$tileId", "") ?: ""
            val closePanel = prefs.getBoolean("close_panel_$tileId", false)
            val forceState = prefs.getBoolean("force_state_$tileId", false)
            val blockState = prefs.getBoolean("block_state_$tileId", false)
            val alwaysAct = prefs.getBoolean("always_active_$tileId", false)
            val bypassApp = prefs.getBoolean("bypass_app_$tileId", true)

            map[tileId] = TileCommandConfig(
                tileId = tileId,
                isOverrideEnabled = enabled,
                overrideType = overrideType,
                executionMode = mode,
                commandOn = cmdOn,
                commandOff = cmdOff,
                customIntentAction = intentAction,
                customIntentPackage = intentPackage,
                customIntentComponent = intentComponent,
                closePanelOnPress = closePanel,
                forceStateChangeAnyway = forceState,
                blockStateChanging = blockState,
                alwaysActive = alwaysAct,
                bypassAppLaunch = bypassApp
            )
        }

        // Also load all saved custom/third-party tile preferences
        val allKeys = prefs.all.keys
        for (key in allKeys) {
            if (key.startsWith("override_")) {
                val tileId = key.removePrefix("override_")
                if (tileId !in map) {
                    val enabled = prefs.getBoolean("override_$tileId", false)
                    val typeStr = prefs.getString("type_$tileId", TileActionOverrideType.DEFAULT.id)
                    val overrideType = TileActionOverrideType.fromId(typeStr)
                    val execModeStr = prefs.getString("mode_$tileId", CommandExecutionMode.STANDARD.id)
                    val mode = CommandExecutionMode.fromId(execModeStr)
                    val cmdOn = prefs.getString("cmd_on_$tileId", "") ?: ""
                    val cmdOff = prefs.getString("cmd_off_$tileId", "") ?: ""
                    val intentAction = prefs.getString("intent_action_$tileId", "") ?: ""
                    val intentPackage = prefs.getString("intent_pkg_$tileId", "") ?: ""
                    val intentComponent = prefs.getString("intent_comp_$tileId", "") ?: ""
                    val closePanel = prefs.getBoolean("close_panel_$tileId", false)
                    val forceState = prefs.getBoolean("force_state_$tileId", false)
                    val blockState = prefs.getBoolean("block_state_$tileId", false)
                    val alwaysAct = prefs.getBoolean("always_active_$tileId", false)
                    val bypassApp = prefs.getBoolean("bypass_app_$tileId", true)

                    map[tileId] = TileCommandConfig(
                        tileId = tileId,
                        isOverrideEnabled = enabled,
                        overrideType = overrideType,
                        executionMode = mode,
                        commandOn = cmdOn,
                        commandOff = cmdOff,
                        customIntentAction = intentAction,
                        customIntentPackage = intentPackage,
                        customIntentComponent = intentComponent,
                        closePanelOnPress = closePanel,
                        forceStateChangeAnyway = forceState,
                        blockStateChanging = blockState,
                        alwaysActive = alwaysAct,
                        bypassAppLaunch = bypassApp
                    )
                }
            }
        }

        _tileConfigs.value = map
    }

    fun getConfig(tileId: String): TileCommandConfig {
        _tileConfigs.value[tileId]?.let { return it }
        val ctx = appContext
        if (ctx != null) {
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            if (prefs.contains("override_$tileId") || prefs.contains("type_$tileId") || prefs.contains("cmd_on_$tileId")) {
                val enabled = prefs.getBoolean("override_$tileId", false)
                val typeStr = prefs.getString("type_$tileId", TileActionOverrideType.DEFAULT.id)
                val overrideType = TileActionOverrideType.fromId(typeStr)
                val execModeStr = prefs.getString("mode_$tileId", CommandExecutionMode.STANDARD.id)
                val mode = CommandExecutionMode.fromId(execModeStr)
                val cmdOn = prefs.getString("cmd_on_$tileId", "") ?: ""
                val cmdOff = prefs.getString("cmd_off_$tileId", "") ?: ""
                val intentAction = prefs.getString("intent_action_$tileId", "") ?: ""
                val intentPackage = prefs.getString("intent_pkg_$tileId", "") ?: ""
                val intentComponent = prefs.getString("intent_comp_$tileId", "") ?: ""
                val closePanel = prefs.getBoolean("close_panel_$tileId", false)
                val forceState = prefs.getBoolean("force_state_$tileId", false)
                val blockState = prefs.getBoolean("block_state_$tileId", false)
                val alwaysAct = prefs.getBoolean("always_active_$tileId", false)
                val bypassApp = prefs.getBoolean("bypass_app_$tileId", true)

                val config = TileCommandConfig(
                    tileId = tileId,
                    isOverrideEnabled = enabled,
                    overrideType = overrideType,
                    executionMode = mode,
                    commandOn = cmdOn,
                    commandOff = cmdOff,
                    customIntentAction = intentAction,
                    customIntentPackage = intentPackage,
                    customIntentComponent = intentComponent,
                    closePanelOnPress = closePanel,
                    forceStateChangeAnyway = forceState,
                    blockStateChanging = blockState,
                    alwaysActive = alwaysAct,
                    bypassAppLaunch = bypassApp
                )
                val current = _tileConfigs.value.toMutableMap()
                current[tileId] = config
                _tileConfigs.value = current
                return config
            }
        }
        return TileCommandConfig(tileId = tileId)
    }

    fun saveConfig(context: Context, config: TileCommandConfig) {
        appContext = context.applicationContext
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("override_${config.tileId}", config.isOverrideEnabled)
            .putString("type_${config.tileId}", config.overrideType.id)
            .putString("mode_${config.tileId}", config.executionMode.id)
            .putString("cmd_on_${config.tileId}", config.commandOn)
            .putString("cmd_off_${config.tileId}", config.commandOff)
            .putString("intent_action_${config.tileId}", config.customIntentAction)
            .putString("intent_pkg_${config.tileId}", config.customIntentPackage)
            .putString("intent_comp_${config.tileId}", config.customIntentComponent)
            .putBoolean("close_panel_${config.tileId}", config.closePanelOnPress)
            .putBoolean("force_state_${config.tileId}", config.forceStateChangeAnyway)
            .putBoolean("block_state_${config.tileId}", config.blockStateChanging)
            .putBoolean("always_active_${config.tileId}", config.alwaysActive)
            .putBoolean("bypass_app_${config.tileId}", config.bypassAppLaunch)
            .apply()

        val current = _tileConfigs.value.toMutableMap()
        current[config.tileId] = config
        _tileConfigs.value = current
    }

    fun resetConfig(context: Context, tileId: String) {
        val defaultPair = defaultTilesList.find { it.first == tileId }?.second ?: ("" to "")
        val config = TileCommandConfig(
            tileId = tileId,
            isOverrideEnabled = false,
            overrideType = TileActionOverrideType.DEFAULT,
            executionMode = CommandExecutionMode.STANDARD,
            commandOn = defaultPair.first,
            commandOff = defaultPair.second,
            customIntentAction = "",
            customIntentPackage = "",
            closePanelOnPress = false,
            forceStateChangeAnyway = false,
            blockStateChanging = false,
            alwaysActive = false,
            bypassAppLaunch = true
        )
        saveConfig(context, config)
    }
}

