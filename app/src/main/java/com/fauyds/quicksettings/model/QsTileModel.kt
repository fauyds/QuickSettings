package com.fauyds.quicksettings.model

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Unified abstract Tile model representing all UI tiles (System & External)
 */
@Immutable
data class QsTileModel(
    val id: String,
    val label: String,
    val subtitle: String,
    val state: TileState,
    val category: TileCategory,
    val iconVector: ImageVector? = null,
    val externalDrawable: Drawable? = null,
    val isExternal: Boolean = false,
    val packageName: String? = null,
    val serviceClassName: String? = null,
    val settingsAction: String? = null,
    val orderIndex: Int = 0,
    val isVisible: Boolean = true,
    val isQqsFavorite: Boolean = false
)
