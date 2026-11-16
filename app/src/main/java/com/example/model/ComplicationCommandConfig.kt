package com.example.model

import androidx.compose.runtime.Immutable

enum class ComplicationTriggerBehavior(val id: String, val label: String) {
    DEFAULT("DEFAULT", "Default"),
    COMPLICATION_MENU("COMPLICATION_MENU", "Menu"),
    STANDARD_COMMAND("STANDARD", "Standard"),
    SHIZUKU_COMMAND("SHIZUKU", "Shizuku"),
    ROOT_COMMAND("ROOT", "Root");

    companion object {
        fun fromId(id: String?): ComplicationTriggerBehavior =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
    }
}

@Immutable
data class ComplicationCommandConfig(
    val tileId: String,
    val tapBehavior: ComplicationTriggerBehavior = ComplicationTriggerBehavior.DEFAULT,
    val tapShellCommand: String = "",
    val tapShellCommandOff: String = "",
    val tapIntentAction: String = "",
    val tapIntentPackage: String = "",
    val tapIntentComponent: String = "",
    val longPressBehavior: ComplicationTriggerBehavior = ComplicationTriggerBehavior.DEFAULT,
    val longPressShellCommand: String = "",
    val longPressIntentAction: String = "",
    val longPressIntentPackage: String = "",
    val longPressIntentComponent: String = ""
)
