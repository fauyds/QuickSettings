package com.example.model

import androidx.compose.runtime.Immutable

enum class CommandExecutionMode(val id: String, val displayName: String) {
    STANDARD("STANDARD", "Standard API"),
    SHIZUKU("SHIZUKU", "Shizuku"),
    ROOT("ROOT", "Root SU");

    companion object {
        fun fromId(id: String?): CommandExecutionMode =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: STANDARD
    }
}

enum class TileActionOverrideType(val id: String, val displayName: String) {
    DEFAULT("DEFAULT", "Default Tile Action"),
    SYSTEM_COMMAND("SYSTEM_COMMAND", "Custom Shell Command"),
    DIRECT_INTENT("DIRECT_INTENT", "Direct Broadcast / Activity Intent"),
    APP_LAUNCH("APP_LAUNCH", "Launch Application"),
    URL_DEEP_LINK("URL_DEEP_LINK", "URL or Deep Link"),
    ACTION_CHAIN("ACTION_CHAIN", "Action Chaining (Sequence)"),
    MEDIA_PLAY_PAUSE("MEDIA_PLAY_PAUSE", "Media Play/Pause"),
    MEDIA_PAUSE("MEDIA_PAUSE", "Media Pause"),
    MEDIA_PLAY("MEDIA_PLAY", "Media Play"),
    MEDIA_NEXT("MEDIA_NEXT", "Media Next Track"),
    MEDIA_PREV("MEDIA_PREV", "Media Prev Track"),
    SCREENSHOT("SCREENSHOT", "Screenshot"),
    MUTE_AUDIO("MUTE_AUDIO", "Toggle Mute");

    companion object {
        fun fromId(id: String?): TileActionOverrideType =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
    }
}

enum class LongPressActionType(val id: String, val displayName: String) {
    DEFAULT("DEFAULT", "Default Settings"),
    SYSTEM_COMMAND("SYSTEM_COMMAND", "Execute Shell Command"),
    DIRECT_INTENT("DIRECT_INTENT", "Launch Custom Intent"),
    URL_DEEP_LINK("URL_DEEP_LINK", "Open URL / Deep Link"),
    APP_LAUNCH("APP_LAUNCH", "Launch Application");

    companion object {
        fun fromId(id: String?): LongPressActionType =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
    }
}

@Immutable
data class ActionChainStep(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: String = "SHELL", // "SHELL", "INTENT", "APP", "URL"
    val target: String = "",
    val delayMs: Long = 0
)

@Immutable
data class TileCommandConfig(
    val tileId: String,
    val isOverrideEnabled: Boolean = false,
    val overrideType: TileActionOverrideType = TileActionOverrideType.DEFAULT,
    val executionMode: CommandExecutionMode = CommandExecutionMode.STANDARD,
    val commandOn: String = "",
    val commandOff: String = "",
    val customIntentAction: String = "",
    val customIntentPackage: String = "",
    val customIntentComponent: String = "",
    val urlOrDeepLink: String = "",
    val targetAppPackage: String = "",
    val iconName: String = "Settings",
    val labelOn: String = "",
    val labelOff: String = "",
    val subtitleOn: String = "",
    val subtitleOff: String = "",
    val isStateToggle: Boolean = false,
    val longPressActionType: LongPressActionType = LongPressActionType.DEFAULT,
    val longPressCommand: String = "",
    val actionChainSteps: List<ActionChainStep> = emptyList(),
    val closePanelOnPress: Boolean = false,
    val forceStateChangeAnyway: Boolean = false,
    val blockStateChanging: Boolean = false,
    val alwaysActive: Boolean = false,
    val bypassAppLaunch: Boolean = true
)
