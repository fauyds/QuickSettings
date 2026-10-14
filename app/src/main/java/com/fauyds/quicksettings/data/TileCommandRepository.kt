package com.fauyds.quicksettings.data

import android.content.Context
import com.fauyds.quicksettings.model.CommandExecutionMode
import com.fauyds.quicksettings.model.TileActionOverrideType
import com.fauyds.quicksettings.model.TileCommandConfig
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
        "heads_up" to ("settings put global heads_up_notifications_enabled 1" to "settings put global heads_up_notifications_enabled 0"),
        "sound_mode" to ("cmd audio set-volume 3 100" to "cmd audio set-volume 3 0"),
        "sound_profile" to ("cmd audio set-volume 3 100" to "cmd audio set-volume 3 0"),
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

        if (map.containsKey("sound_mode") && !map.containsKey("sound_profile")) {
            map["sound_profile"] = map["sound_mode"]!!.copy(tileId = "sound_profile")
        } else if (map.containsKey("sound_profile") && !map.containsKey("sound_mode")) {
            map["sound_mode"] = map["sound_profile"]!!.copy(tileId = "sound_mode")
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
        val aliasId = when (tileId) {
            "sound_mode" -> "sound_profile"
            "sound_profile" -> "sound_mode"
            else -> null
        }
        if (aliasId != null) {
            _tileConfigs.value[aliasId]?.let { return it.copy(tileId = tileId) }
        }
        return TileCommandConfig(tileId = tileId)
    }

    fun saveConfig(context: Context, config: TileCommandConfig) {
        appContext = context.applicationContext
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
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

        val aliasId = when (config.tileId) {
            "sound_mode" -> "sound_profile"
            "sound_profile" -> "sound_mode"
            else -> null
        }
        if (aliasId != null) {
            editor
                .putBoolean("override_$aliasId", config.isOverrideEnabled)
                .putString("type_$aliasId", config.overrideType.id)
                .putString("mode_$aliasId", config.executionMode.id)
                .putString("cmd_on_$aliasId", config.commandOn)
                .putString("cmd_off_$aliasId", config.commandOff)
                .putString("intent_action_$aliasId", config.customIntentAction)
                .putString("intent_pkg_$aliasId", config.customIntentPackage)
                .putString("intent_comp_$aliasId", config.customIntentComponent)
                .putBoolean("close_panel_$aliasId", config.closePanelOnPress)
                .putBoolean("force_state_$aliasId", config.forceStateChangeAnyway)
                .putBoolean("block_state_$aliasId", config.blockStateChanging)
                .putBoolean("always_active_$aliasId", config.alwaysActive)
                .putBoolean("bypass_app_$aliasId", config.bypassAppLaunch)
        }
        editor.apply()

        val current = _tileConfigs.value.toMutableMap()
        current[config.tileId] = config
        if (aliasId != null) {
            current[aliasId] = config.copy(tileId = aliasId)
        }
        _tileConfigs.value = current
    }

    fun resetConfig(context: Context, tileId: String) {
        val canonicalId = if (tileId == "sound_profile") "sound_mode" else tileId
        val defaultPair = defaultTilesList.find { it.first == tileId || it.first == canonicalId }?.second ?: ("" to "")
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

    fun setGlobalExecutionMode(context: Context, mode: CommandExecutionMode, enableOverrides: Boolean = true) {
        appContext = context.applicationContext
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val currentMap = _tileConfigs.value.toMutableMap()

        val allTileIds = (defaultTilesList.map { it.first } + currentMap.keys).toSet()

        for (tileId in allTileIds) {
            val existing = currentMap[tileId] ?: TileCommandConfig(tileId = tileId)
            val defaultPair = defaultTilesList.find { it.first == tileId }?.second ?: ("" to "")
            val updated = existing.copy(
                executionMode = mode,
                isOverrideEnabled = if (enableOverrides) true else existing.isOverrideEnabled,
                commandOn = if (existing.commandOn.isBlank()) defaultPair.first else existing.commandOn,
                commandOff = if (existing.commandOff.isBlank()) defaultPair.second else existing.commandOff
            )
            currentMap[tileId] = updated

            editor.putString("mode_$tileId", mode.id)
                .putBoolean("override_$tileId", updated.isOverrideEnabled)
                .putString("cmd_on_$tileId", updated.commandOn)
                .putString("cmd_off_$tileId", updated.commandOff)
        }
        editor.apply()
        _tileConfigs.value = currentMap
    }

    fun setAllOverridesEnabled(context: Context, enable: Boolean, currentMode: CommandExecutionMode? = null) {
        appContext = context.applicationContext
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val currentMap = _tileConfigs.value.toMutableMap()

        val allTileIds = (defaultTilesList.map { it.first } + currentMap.keys).toSet()

        for (tileId in allTileIds) {
            val existing = currentMap[tileId] ?: TileCommandConfig(tileId = tileId)
            val defaultPair = defaultTilesList.find { it.first == tileId }?.second ?: ("" to "")
            val updated = existing.copy(
                isOverrideEnabled = enable,
                executionMode = currentMode ?: existing.executionMode,
                commandOn = if (existing.commandOn.isBlank()) defaultPair.first else existing.commandOn,
                commandOff = if (existing.commandOff.isBlank()) defaultPair.second else existing.commandOff
            )
            currentMap[tileId] = updated

            editor.putBoolean("override_$tileId", enable)
                .putString("mode_$tileId", updated.executionMode.id)
                .putString("cmd_on_$tileId", updated.commandOn)
                .putString("cmd_off_$tileId", updated.commandOff)
        }
        editor.apply()
        _tileConfigs.value = currentMap
    }
}

