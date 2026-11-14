package com.example.model

import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed hierarchy representing the action executed when a ComplicationTile is tapped.
 */
sealed interface ComplicationAction {
    /**
     * Toggles a Quick Settings tile or feature state (e.g., Battery Saver, Wi-Fi, DND).
     */
    data class Toggle(
        val tileId: String,
        val isActive: Boolean = false,
        val onToggle: ((Boolean) -> Unit)? = null
    ) : ComplicationAction

    /**
     * Launches an installed application package (and optional activity component).
     */
    data class LaunchApp(
        val packageName: String,
        val activityClassName: String? = null
    ) : ComplicationAction

    /**
     * Triggers a shortcut via intent action, deep-link URI, or custom callback.
     */
    data class Shortcut(
        val shortcutId: String,
        val intentAction: String? = null,
        val dataUri: String? = null,
        val onInvoke: (() -> Unit)? = null
    ) : ComplicationAction
}

/**
 * Controls where the dynamic complication text subline (e.g., "78%", "12k") is placed.
 */
enum class ComplicationSublinePlacement(val id: String, val label: String) {
    INSIDE_RING("inside_ring", "Inside Ring"),
    BELOW_RING("below_ring", "Below Ring");

    companion object {
        fun fromId(id: String?): ComplicationSublinePlacement =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: INSIDE_RING
    }
}

/**
 * Controls how ComplicationTile adapts to compact header rows vs grid cells.
 */
enum class ComplicationLayoutMode(val id: String, val label: String) {
    ADAPTIVE("adaptive", "Adaptive"),
    HEADER_ROW("header_row", "Header Row"),
    GRID_TILE("grid_tile", "Grid Tile");

    companion object {
        fun fromId(id: String?): ComplicationLayoutMode =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ADAPTIVE
    }
}

/**
 * Immutable UI model backing ComplicationTile (Wear OS Complication + QS Tile hybrid).
 */
@Immutable
data class ComplicationTileModel(
    val id: String,
    val title: String,
    val subline: String,
    val progress: Float,
    val icon: ImageVector? = null,
    @DrawableRes val iconResId: Int? = null,
    val accentColor: Color? = null,
    val trackColor: Color? = null,
    val isActive: Boolean = false,
    val sublinePlacement: ComplicationSublinePlacement = ComplicationSublinePlacement.INSIDE_RING,
    val layoutMode: ComplicationLayoutMode = ComplicationLayoutMode.ADAPTIVE,
    val tapAction: ComplicationAction = ComplicationAction.Toggle(tileId = id),
    val settingsAction: String = Settings.ACTION_SETTINGS,
    val contentDescription: String = "$title $subline"
)
