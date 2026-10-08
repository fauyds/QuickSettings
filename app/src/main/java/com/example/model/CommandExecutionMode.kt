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
    DIRECT_INTENT("DIRECT_INTENT", "Direct Broadcast Intent"),
    MEDIA_PLAY_PAUSE("MEDIA_PLAY_PAUSE", "Media Play/Pause (KEYCODE_MEDIA_PLAY_PAUSE)"),
    MEDIA_PAUSE("MEDIA_PAUSE", "Media Pause (input keyevent 127)"),
    MEDIA_PLAY("MEDIA_PLAY", "Media Play (input keyevent 126)"),
    MEDIA_NEXT("MEDIA_NEXT", "Media Next Track (input keyevent 87)"),
    MEDIA_PREV("MEDIA_PREV", "Media Prev Track (input keyevent 88)"),
    SCREENSHOT("SCREENSHOT", "Screenshot (input keyevent 120)"),
    MUTE_AUDIO("MUTE_AUDIO", "Toggle Mute (input keyevent 164)");

    companion object {
        fun fromId(id: String?): TileActionOverrideType =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
    }
}

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
    val closePanelOnPress: Boolean = false,
    val forceStateChangeAnyway: Boolean = false,
    val blockStateChanging: Boolean = false,
    val alwaysActive: Boolean = false,
    val bypassAppLaunch: Boolean = true
)

