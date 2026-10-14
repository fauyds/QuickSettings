package com.fauyds.quicksettings.ui.tile

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.fauyds.quicksettings.R
import com.fauyds.quicksettings.model.QsGridLayout
import com.fauyds.quicksettings.model.QsTileModel
import com.fauyds.quicksettings.model.TileCategory
import com.fauyds.quicksettings.model.TileState
import com.fauyds.quicksettings.ui.notification.MonetThemeHelper
import com.fauyds.quicksettings.ui.overlay.QsExpansionStage

class UnifiedQsHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val qqsView: QuickQuickSettingsView
    val fullQsView: QuickSettingsFullView
    val editStudioView: TileEditStudioView

    var onTileClick: ((QsTileModel) -> Unit)? = null
        set(value) {
            field = value
            qqsView.onTileClick = value
            fullQsView.onTileClick = value
        }

    var onTileLongClick: ((QsTileModel) -> Unit)? = null
        set(value) {
            field = value
            qqsView.onTileLongClick = value
            fullQsView.onTileLongClick = value
        }

    var onBrightnessChange: ((Int) -> Unit)? = null
        set(value) {
            field = value
            qqsView.onBrightnessChange = value
            fullQsView.onBrightnessChange = value
        }

    var onExpandFull: (() -> Unit)? = null
        set(value) {
            field = value
            qqsView.onExpandFull = value
        }

    var onCollapseToQqs: (() -> Unit)? = null
        set(value) {
            field = value
            fullQsView.onCollapseToQqs = value
        }

    var onClosePanel: (() -> Unit)? = null
        set(value) {
            field = value
            fullQsView.onClosePanel = value
        }

    var onToggleEditMode: (() -> Unit)? = null
        set(value) {
            field = value
            fullQsView.onToggleEditMode = value
            editStudioView.onDone = value
        }

    var onTilesUpdated: ((List<QsTileModel>, List<QsTileModel>) -> Unit)? = null
        set(value) {
            field = value
            editStudioView.onTilesUpdated = value
        }

    init {
        val root = LayoutInflater.from(context).inflate(R.layout.layout_unified_qs, this, true)

        qqsView = root.findViewById(R.id.view_qqs_panel)
        fullQsView = root.findViewById(R.id.view_full_qs_panel)
        editStudioView = root.findViewById(R.id.view_tile_edit_studio)
    }

    fun updateData(
        expansionStage: QsExpansionStage,
        isEditingTiles: Boolean,
        tiles: List<QsTileModel>,
        editActiveTiles: List<QsTileModel>?,
        editInactiveTiles: List<QsTileModel>?,
        gridLayout: QsGridLayout,
        brightness: Int,
        brightnessAtBottom: Boolean,
        showBrightnessInQqs: Boolean,
        activeSquareShape: Boolean,
        qqsTileCount: Int,
        qqsTileSizeDp: Int,
        dragHandleLengthDp: Int,
        isDark: Boolean
    ) {
        val density = context.resources.displayMetrics.density

        // 1. Monet Themed Card Background
        val cardBg = MonetThemeHelper.getCardBackground(context, isDark)
        val cardOutline = MonetThemeHelper.getCardOutline(context, isDark)
        val cornerRadius = 24f * density

        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            this.cornerRadius = cornerRadius
            setColor(cardBg)
            setStroke(1, cardOutline)
        }

        // 2. Stage Routing & Dynamic Layout Heights
        if (isEditingTiles) {
            layoutParams = (layoutParams ?: ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )).apply {
                height = ViewGroup.LayoutParams.MATCH_PARENT
            }

            qqsView.visibility = View.GONE
            fullQsView.visibility = View.GONE
            editStudioView.visibility = View.VISIBLE
            editStudioView.layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )

            val active = (editActiveTiles ?: tiles.filter { it.isVisible }).ifEmpty { tiles }
            val activeIds = active.map { it.id }.toSet()
            val inactive = if (editInactiveTiles != null) {
                editInactiveTiles
            } else {
                val dbInactive = tiles.filter { !it.isVisible && it.id !in activeIds }
                if (dbInactive.isNotEmpty()) {
                    dbInactive
                } else {
                    // Fallback to all unassigned system tiles
                    val registry = com.fauyds.quicksettings.system.TileRegistry.defaultSystemTiles
                    registry.filter { it.tileId !in activeIds }.map { entity ->
                        QsTileModel(
                            id = entity.tileId,
                            label = entity.label,
                            subtitle = entity.subtitle,
                            state = TileState.INACTIVE,
                            category = try { TileCategory.valueOf(entity.categoryName) } catch (_: Throwable) { TileCategory.SYSTEM },
                            orderIndex = entity.orderIndex,
                            isVisible = false,
                            isQqsFavorite = entity.isQqsFavorite,
                            settingsAction = entity.settingsAction
                        )
                    }
                }
            }

            editStudioView.updateData(
                activeTiles = active,
                inactiveTiles = inactive,
                gridLayout = gridLayout,
                activeSquareShape = activeSquareShape,
                isDark = isDark
            )
        } else if (expansionStage == QsExpansionStage.FULL_QS) {
            layoutParams = (layoutParams ?: ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )).apply {
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }

            qqsView.visibility = View.GONE
            fullQsView.visibility = View.VISIBLE
            editStudioView.visibility = View.GONE

            fullQsView.updateData(
                tiles = tiles,
                gridLayout = gridLayout,
                brightness = brightness,
                brightnessAtBottom = brightnessAtBottom,
                activeSquareShape = activeSquareShape,
                isDark = isDark
            )
        } else {
            layoutParams = (layoutParams ?: ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )).apply {
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }

            qqsView.visibility = View.VISIBLE
            fullQsView.visibility = View.GONE
            editStudioView.visibility = View.GONE

            qqsView.updateData(
                tiles = tiles,
                qqsTileCount = qqsTileCount,
                brightness = brightness,
                showBrightnessInQqs = showBrightnessInQqs,
                activeSquareShape = activeSquareShape,
                qqsTileSizeDp = qqsTileSizeDp,
                dragHandleLengthDp = dragHandleLengthDp,
                isDark = isDark
            )
        }
    }
}
