package com.example.data

import android.content.Context
import com.example.model.ActionChainStep
import com.example.model.CommandExecutionMode
import com.example.model.LongPressActionType
import com.example.model.TileActionOverrideType
import com.example.model.TileCategory
import com.example.model.TileCommandConfig
import com.example.data.local.TileEntity
import com.example.data.repository.TileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
        "screenrecord" to ("screenrecord --time-limit 15 /sdcard/screenrecord.mp4 &" to "pkill -2 screenrecord"),
        "fps_info" to ("service call SurfaceFlinger 1013 i32 1" to "service call SurfaceFlinger 1013 i32 0"),
        "cpu_info" to ("settings put global show_cpu 1" to "settings put global show_cpu 0")
    )

    fun serializeChainSteps(steps: List<ActionChainStep>): String {
        return steps.joinToString(";") { "${it.type}|${it.target.replace("|", "^pipe^").replace(";", "^semi^")}|${it.delayMs}" }
    }

    fun deserializeChainSteps(raw: String?): List<ActionChainStep> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(";").mapNotNull { part ->
            val tokens = part.split("|")
            if (tokens.size >= 3) {
                val type = tokens[0]
                val target = tokens[1].replace("^pipe^", "|").replace("^semi^", ";")
                val delayMs = tokens[2].toLongOrNull() ?: 0L
                ActionChainStep(type = type, target = target, delayMs = delayMs)
            } else null
        }
    }

    private fun readConfigFromPrefs(prefs: android.content.SharedPreferences, tileId: String, defaultCmds: Pair<String, String>? = null): TileCommandConfig {
        val enabled = prefs.getBoolean("override_$tileId", false)
        val typeStr = prefs.getString("type_$tileId", TileActionOverrideType.DEFAULT.id)
        val overrideType = TileActionOverrideType.fromId(typeStr)
        val execModeStr = prefs.getString("mode_$tileId", CommandExecutionMode.STANDARD.id)
        val mode = CommandExecutionMode.fromId(execModeStr)
        val cmdOn = prefs.getString("cmd_on_$tileId", defaultCmds?.first ?: "") ?: (defaultCmds?.first ?: "")
        val cmdOff = prefs.getString("cmd_off_$tileId", defaultCmds?.second ?: "") ?: (defaultCmds?.second ?: "")
        val intentAction = prefs.getString("intent_action_$tileId", "") ?: ""
        val intentPackage = prefs.getString("intent_pkg_$tileId", "") ?: ""
        val intentComponent = prefs.getString("intent_comp_$tileId", "") ?: ""
        val urlOrDeepLink = prefs.getString("url_$tileId", "") ?: ""
        val targetAppPackage = prefs.getString("app_pkg_$tileId", "") ?: ""
        val iconName = prefs.getString("icon_name_$tileId", "Settings") ?: "Settings"
        val labelOn = prefs.getString("label_on_$tileId", "") ?: ""
        val labelOff = prefs.getString("label_off_$tileId", "") ?: ""
        val subtitleOn = prefs.getString("sub_on_$tileId", "") ?: ""
        val subtitleOff = prefs.getString("sub_off_$tileId", "") ?: ""
        val isStateToggle = prefs.getBoolean("is_toggle_$tileId", false)
        val longPressTypeStr = prefs.getString("long_press_type_$tileId", LongPressActionType.DEFAULT.id)
        val longPressType = LongPressActionType.fromId(longPressTypeStr)
        val longPressCmd = prefs.getString("long_press_cmd_$tileId", "") ?: ""
        val chainRaw = prefs.getString("chain_steps_$tileId", "") ?: ""
        val chainSteps = deserializeChainSteps(chainRaw)
        val closePanel = prefs.getBoolean("close_panel_$tileId", false)
        val forceState = prefs.getBoolean("force_state_$tileId", false)
        val blockState = prefs.getBoolean("block_state_$tileId", false)
        val alwaysAct = prefs.getBoolean("always_active_$tileId", false)
        val bypassApp = prefs.getBoolean("bypass_app_$tileId", true)

        return TileCommandConfig(
            tileId = tileId,
            isOverrideEnabled = enabled,
            overrideType = overrideType,
            executionMode = mode,
            commandOn = cmdOn,
            commandOff = cmdOff,
            customIntentAction = intentAction,
            customIntentPackage = intentPackage,
            customIntentComponent = intentComponent,
            urlOrDeepLink = urlOrDeepLink,
            targetAppPackage = targetAppPackage,
            iconName = iconName,
            labelOn = labelOn,
            labelOff = labelOff,
            subtitleOn = subtitleOn,
            subtitleOff = subtitleOff,
            isStateToggle = isStateToggle,
            longPressActionType = longPressType,
            longPressCommand = longPressCmd,
            actionChainSteps = chainSteps,
            closePanelOnPress = closePanel,
            forceStateChangeAnyway = forceState,
            blockStateChanging = blockState,
            alwaysActive = alwaysAct,
            bypassAppLaunch = bypassApp
        )
    }

    fun init(context: Context) {
        appContext = context.applicationContext
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val map = mutableMapOf<String, TileCommandConfig>()

        for ((tileId, defaultCmds) in defaultTilesList) {
            map[tileId] = readConfigFromPrefs(prefs, tileId, defaultCmds)
        }

        // Also load all saved custom/third-party tile preferences
        val allKeys = prefs.all.keys
        for (key in allKeys) {
            if (key.startsWith("override_")) {
                val tileId = key.removePrefix("override_")
                if (tileId !in map) {
                    map[tileId] = readConfigFromPrefs(prefs, tileId)
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
                val config = readConfigFromPrefs(prefs, tileId)
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
            .putString("url_${config.tileId}", config.urlOrDeepLink)
            .putString("app_pkg_${config.tileId}", config.targetAppPackage)
            .putString("icon_name_${config.tileId}", config.iconName)
            .putString("label_on_${config.tileId}", config.labelOn)
            .putString("label_off_${config.tileId}", config.labelOff)
            .putString("sub_on_${config.tileId}", config.subtitleOn)
            .putString("sub_off_${config.tileId}", config.subtitleOff)
            .putBoolean("is_toggle_${config.tileId}", config.isStateToggle)
            .putString("long_press_type_${config.tileId}", config.longPressActionType.id)
            .putString("long_press_cmd_${config.tileId}", config.longPressCommand)
            .putString("chain_steps_${config.tileId}", serializeChainSteps(config.actionChainSteps))
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
                .putString("url_$aliasId", config.urlOrDeepLink)
                .putString("app_pkg_$aliasId", config.targetAppPackage)
                .putString("icon_name_$aliasId", config.iconName)
                .putString("label_on_$aliasId", config.labelOn)
                .putString("label_off_$aliasId", config.labelOff)
                .putString("sub_on_$aliasId", config.subtitleOn)
                .putString("sub_off_$aliasId", config.subtitleOff)
                .putBoolean("is_toggle_$aliasId", config.isStateToggle)
                .putString("long_press_type_$aliasId", config.longPressActionType.id)
                .putString("long_press_cmd_$aliasId", config.longPressCommand)
                .putString("chain_steps_$aliasId", serializeChainSteps(config.actionChainSteps))
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

    fun createCustomTile(
        context: Context,
        repository: TileRepository,
        label: String,
        subtitle: String,
        config: TileCommandConfig,
        scope: CoroutineScope
    ) {
        val tileId = if (config.tileId.isNotBlank()) config.tileId else "custom_${System.currentTimeMillis()}"
        val updatedConfig = config.copy(tileId = tileId, isOverrideEnabled = true)
        saveConfig(context, updatedConfig)

        scope.launch(Dispatchers.IO) {
            val entity = TileEntity(
                tileId = tileId,
                label = label.ifBlank { "Custom Tile" },
                subtitle = subtitle.ifBlank { "Action" },
                categoryName = TileCategory.SYSTEM.name,
                orderIndex = 99,
                isVisible = true,
                isQqsFavorite = false
            )
            repository.insert(entity)
        }
    }

    fun deleteCustomTile(
        context: Context,
        repository: TileRepository,
        tileId: String,
        scope: CoroutineScope
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove("override_$tileId")
            .remove("type_$tileId")
            .remove("mode_$tileId")
            .remove("cmd_on_$tileId")
            .remove("cmd_off_$tileId")
            .remove("url_$tileId")
            .remove("app_pkg_$tileId")
            .remove("icon_name_$tileId")
            .remove("label_on_$tileId")
            .remove("label_off_$tileId")
            .remove("sub_on_$tileId")
            .remove("sub_off_$tileId")
            .remove("is_toggle_$tileId")
            .remove("long_press_type_$tileId")
            .remove("long_press_cmd_$tileId")
            .remove("chain_steps_$tileId")
            .apply()

        val current = _tileConfigs.value.toMutableMap()
        current.remove(tileId)
        _tileConfigs.value = current

        scope.launch(Dispatchers.IO) {
            repository.deleteById(tileId)
        }
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
