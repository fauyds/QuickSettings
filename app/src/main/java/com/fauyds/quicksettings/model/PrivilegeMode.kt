package com.fauyds.quicksettings.model

enum class PrivilegeMode(
    val title: String,
    val description: String,
    val badge: String
) {
    STANDARD(
        title = "Non root",
        description = "Uses Accessibility Service, Write Settings permission, and native System Settings dialog fallbacks.",
        badge = "Safe / Default"
    ),
    SHIZUKU(
        title = "Shizuku Mode",
        description = "Executes elevated ADB/System Shell commands via the Shizuku IPC service without requiring full root.",
        badge = "Elevated ADB"
    ),
    ROOT(
        title = "Root",
        description = "Direct root shell (su) execution for immediate privileged system toggles without confirmation dialogs.",
        badge = "Full Control"
    )
}
