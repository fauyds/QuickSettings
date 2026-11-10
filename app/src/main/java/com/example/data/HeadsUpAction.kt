package com.example.data

/**
 * Interaction actions pool for Heads-Up notification gestures and tap events.
 */
enum class HeadsUpAction(
    val id: String,
    val title: String,
    val description: String
) {
    DISMISS("dismiss", "Dismiss Notification", "Removes banner and cancels notification in notification shade"),
    HIDE_BANNER("hide_banner", "Hide Banner Only", "Dismisses only floating banner; retains notification in shade"),
    OPEN_SHADE("open_shade", "Open Notification Shade", "Dismisses banner and triggers notification shade expansion"),
    OPEN_NOTIFICATION("open_notification", "Open Notification", "Simulates tapping notification: executes notification content intent"),
    OPEN_APP("open_app", "Open App", "Launches app main launcher activity"),
    EXPAND_NOTIFICATION("expand_notification", "Expand Notification", "Toggles expansion in-place to reveal full message body & quick actions"),
    SNOOZE_APP("snooze_app", "Snooze This App", "Temporarily mutes all heads-up banners from this specific app"),
    SNOOZE_ALL("snooze_all", "Snooze All", "Temporarily mutes all incoming heads-up banners globally (except calls & alarms)"),
    NONE("none", "None", "No action / ignore event");

    val isSnoozeAction: Boolean
        get() = this == SNOOZE_APP || this == SNOOZE_ALL

    companion object {
        fun fromId(id: String?): HeadsUpAction =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: NONE
    }
}
