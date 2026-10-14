package com.fauyds.quicksettings.ui.tile

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.fauyds.quicksettings.R
import com.fauyds.quicksettings.model.QsTileModel
import com.fauyds.quicksettings.ui.notification.MonetThemeHelper

class QuickQuickSettingsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val llContainer: LinearLayout
    private val llBrightnessRow: LinearLayout
    private val flBrightnessIconPill: FrameLayout
    private val ivBrightnessIcon: ImageView
    private val sbBrightness: SeekBar
    private val tvBrightnessPercentage: TextView
    private val llTilesRow: LinearLayout
    private val flDragHandleContainer: FrameLayout
    private val vDragHandle: View

    var onTileClick: ((QsTileModel) -> Unit)? = null
    var onTileLongClick: ((QsTileModel) -> Unit)? = null
    var onBrightnessChange: ((Int) -> Unit)? = null
    var onExpandFull: (() -> Unit)? = null

    private var currentTiles: List<QsTileModel> = emptyList()
    private var isDark: Boolean = true

    init {
        val root = LayoutInflater.from(context).inflate(R.layout.layout_qqs_panel, this, true)

        llContainer = root.findViewById(R.id.ll_qqs_container)
        llBrightnessRow = root.findViewById(R.id.ll_qqs_brightness_row)
        flBrightnessIconPill = root.findViewById(R.id.fl_qqs_brightness_icon_pill)
        ivBrightnessIcon = root.findViewById(R.id.iv_qqs_brightness_icon)
        sbBrightness = root.findViewById(R.id.sb_qqs_brightness)
        tvBrightnessPercentage = root.findViewById(R.id.tv_qqs_brightness_percentage)
        llTilesRow = root.findViewById(R.id.ll_qqs_tiles_row)
        flDragHandleContainer = root.findViewById(R.id.fl_qqs_drag_handle_container)
        vDragHandle = root.findViewById(R.id.v_qqs_drag_handle)

        sbBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val clamped = progress.coerceIn(5, 255)
                val pct = ((clamped / 255f) * 100).toInt()
                tvBrightnessPercentage.text = "$pct%"
                ivBrightnessIcon.setImageResource(
                    if (clamped > 128) R.drawable.ic_qs_brightness_high else R.drawable.ic_qs_brightness_low
                )
                if (fromUser) {
                    onBrightnessChange?.invoke(clamped)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        flDragHandleContainer.setOnClickListener {
            onExpandFull?.invoke()
        }
    }

    fun updateData(
        tiles: List<QsTileModel>,
        qqsTileCount: Int,
        brightness: Int,
        showBrightnessInQqs: Boolean,
        activeSquareShape: Boolean,
        qqsTileSizeDp: Int,
        dragHandleLengthDp: Int,
        isDark: Boolean
    ) {
        this.isDark = isDark
        val density = context.resources.displayMetrics.density

        // 1. Resolve Monet Theme Colors
        val primaryAccent = MonetThemeHelper.getPrimaryAccent(context, isDark)
        val textPrimary = MonetThemeHelper.getTextPrimary(context, isDark)
        val textSecondary = MonetThemeHelper.getTextSecondary(context, isDark)
        val cardOutline = MonetThemeHelper.getCardOutline(context, isDark)

        // 2. Classic Brightness Slider Setup
        if (showBrightnessInQqs) {
            llBrightnessRow.visibility = View.VISIBLE
            val clamped = brightness.coerceIn(5, 255)
            sbBrightness.progress = clamped
            val pct = ((clamped / 255f) * 100).toInt()
            tvBrightnessPercentage.text = "$pct%"
            tvBrightnessPercentage.setTextColor(textPrimary)

            flBrightnessIconPill.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                val pillColor = Color.argb(
                    (0.15f * 255).toInt(),
                    Color.red(primaryAccent),
                    Color.green(primaryAccent),
                    Color.blue(primaryAccent)
                )
                setColor(pillColor)
            }
            ivBrightnessIcon.setImageResource(
                if (clamped > 128) R.drawable.ic_qs_brightness_high else R.drawable.ic_qs_brightness_low
            )
            ivBrightnessIcon.setColorFilter(primaryAccent)

            sbBrightness.thumbTintList = ColorStateList.valueOf(primaryAccent)
            sbBrightness.progressTintList = ColorStateList.valueOf(primaryAccent)
            sbBrightness.progressBackgroundTintList = ColorStateList.valueOf(cardOutline)
        } else {
            llBrightnessRow.visibility = View.GONE
        }

        // 3. QQS Tiles Row
        val displayCount = qqsTileCount.coerceIn(2, 6)
        val activeTiles = tiles.filter { it.isVisible }.take(displayCount)
        this.currentTiles = activeTiles

        llTilesRow.removeAllViews()
        val circleSizePx = (qqsTileSizeDp * density).toInt()
        val iconSizePx = (circleSizePx * 0.46f).toInt()

        activeTiles.forEachIndexed { index, tile ->
            val tileView = LayoutInflater.from(context).inflate(R.layout.item_qs_tile, llTilesRow, false)
            val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                gravity = android.view.Gravity.CENTER
            }
            tileView.layoutParams = lp

            QsTileViewHelper.bindTile(
                view = tileView,
                tile = tile,
                isDark = isDark,
                activeSquareShape = activeSquareShape,
                circleSizePx = circleSizePx,
                iconSizePx = iconSizePx,
                showSubtitle = false,
                tileIndex = index,
                onClick = { onTileClick?.invoke(it) },
                onLongClick = { onTileLongClick?.invoke(it) }
            )

            llTilesRow.addView(tileView)
        }

        // 4. Drag Handle Pill Styling
        val handleWidthPx = (dragHandleLengthDp * density).toInt()
        val handleColor = if (isDark) Color.parseColor("#44FFFFFF") else Color.parseColor("#44000000")
        vDragHandle.layoutParams = (vDragHandle.layoutParams ?: FrameLayout.LayoutParams(handleWidthPx, (3.5f * density).toInt())).apply {
            width = handleWidthPx
            height = (3.5f * density).toInt()
        }
        vDragHandle.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 4f * density
            setColor(handleColor)
        }
    }
}
