package com.example.ui.tile

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.example.R
import com.example.model.QsTileModel
import com.example.ui.notification.MonetThemeHelper

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

    private var tvOreoDate: TextView? = null
    private var ivOreoAlarm: ImageView? = null
    private var btnOreoSettings: ImageView? = null
    private var btnOreoEdit: ImageView? = null
    private var ivOreoExpand: ImageView? = null

    private var tvOreoTopCarrier: TextView? = null
    private var tvOreoTopBatteryText: TextView? = null
    private var ivOreoTopBatteryIcon: ImageView? = null
    private var tvOreoTopTime: TextView? = null

    private var currentTiles: List<QsTileModel> = emptyList()
    private var isDark: Boolean = true

    init {
        val isOreo = com.example.data.PanelPreferences.isAndroidOreoStyle(context)
        val isMarshmallow = com.example.data.PanelPreferences.isAndroidMarshmallowStyle(context)
        val layoutRes = when {
            isMarshmallow -> R.layout.layout_qqs_panel_marshmallow
            isOreo -> R.layout.layout_qqs_panel_oreo
            else -> R.layout.layout_qqs_panel
        }
        val root = LayoutInflater.from(context).inflate(layoutRes, this, true)

        llContainer = root.findViewById(R.id.ll_qqs_container)
        llBrightnessRow = root.findViewById(R.id.ll_qqs_brightness_row)
        flBrightnessIconPill = root.findViewById(R.id.fl_qqs_brightness_icon_pill)
        ivBrightnessIcon = root.findViewById(R.id.iv_qqs_brightness_icon)
        sbBrightness = root.findViewById(R.id.sb_qqs_brightness)
        tvBrightnessPercentage = root.findViewById(R.id.tv_qqs_brightness_percentage)
        llTilesRow = root.findViewById(R.id.ll_qqs_tiles_row)
        flDragHandleContainer = root.findViewById(R.id.fl_qqs_drag_handle_container)
        vDragHandle = root.findViewById(R.id.v_qqs_drag_handle)

        tvOreoDate = root.findViewById(R.id.tv_qqs_oreo_date)
        ivOreoAlarm = root.findViewById(R.id.iv_qqs_oreo_alarm)
        btnOreoSettings = root.findViewById(R.id.btn_qqs_oreo_settings)
        btnOreoEdit = root.findViewById(R.id.btn_qqs_oreo_edit)
        ivOreoExpand = root.findViewById(R.id.iv_qqs_oreo_expand)

        tvOreoTopCarrier = root.findViewById(R.id.tv_oreo_top_carrier)
        tvOreoTopBatteryText = root.findViewById(R.id.tv_oreo_top_battery_text)
        ivOreoTopBatteryIcon = root.findViewById(R.id.iv_oreo_top_battery_icon)
        tvOreoTopTime = root.findViewById(R.id.tv_oreo_top_time)

        btnOreoSettings?.setOnClickListener {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Throwable) {}
        }

        btnOreoEdit?.setOnClickListener {
            onExpandFull?.invoke()
        }

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
        isDark: Boolean,
        brightnessAtBottom: Boolean = false
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
            llContainer.removeView(llBrightnessRow)
            if (brightnessAtBottom) {
                val tilesIndex = llContainer.indexOfChild(llTilesRow)
                if (tilesIndex >= 0) {
                    llContainer.addView(llBrightnessRow, tilesIndex + 1)
                } else {
                    llContainer.addView(llBrightnessRow)
                }
            } else {
                llContainer.addView(llBrightnessRow, 1)
            }
            val clamped = brightness.coerceIn(5, 255)
            sbBrightness.progress = clamped
            val pct = ((clamped / 255f) * 100).toInt()
            tvBrightnessPercentage.visibility = if (com.example.data.PanelPreferences.hideBrightnessPercentage.value) View.GONE else View.VISIBLE
            flBrightnessIconPill.visibility = if (com.example.data.PanelPreferences.hideBrightnessIcon.value) View.GONE else View.VISIBLE

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
        val enableQqsHorizontalScroll = com.example.data.PanelPreferences.enableQqsHorizontalScroll.value
        val displayCount = qqsTileCount.coerceIn(2, 6)
        val activeTiles = if (enableQqsHorizontalScroll) {
            tiles.filter { it.isVisible }
        } else {
            tiles.filter { it.isVisible }.take(displayCount)
        }
        this.currentTiles = activeTiles

        val hsvScroll = llContainer.findViewById<HorizontalScrollView>(R.id.hsv_qqs_tiles_scroll)
        hsvScroll?.isFillViewport = !enableQqsHorizontalScroll

        llTilesRow.removeAllViews()
        val circleSizePx = (qqsTileSizeDp * density).toInt()
        val iconSizePx = (circleSizePx * 0.46f).toInt()

        activeTiles.forEachIndexed { index, tile ->
            val tileView = LayoutInflater.from(context).inflate(R.layout.item_qs_tile, llTilesRow, false)
            val lp = if (enableQqsHorizontalScroll) {
                val marginEndPx = (16 * density).toInt()
                LinearLayout.LayoutParams(circleSizePx, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    gravity = android.view.Gravity.CENTER
                    if (index < activeTiles.size - 1) {
                        marginEnd = marginEndPx
                    }
                }
            } else {
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    gravity = android.view.Gravity.CENTER
                }
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

        val vFadeLeft = llContainer.findViewById<View>(R.id.v_qqs_fade_left)
        val vFadeRight = llContainer.findViewById<View>(R.id.v_qqs_fade_right)

        if (enableQqsHorizontalScroll && hsvScroll != null) {
            val surfaceBgColor = MonetThemeHelper.getSurfaceBackground(context, isDark)
            val transparentBgColor = Color.TRANSPARENT

            vFadeLeft?.background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(surfaceBgColor, transparentBgColor)
            )
            vFadeRight?.background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(transparentBgColor, surfaceBgColor)
            )

            fun updateFades() {
                val canScrollLeft = hsvScroll.canScrollHorizontally(-1)
                val canScrollRight = hsvScroll.canScrollHorizontally(1)
                vFadeLeft?.visibility = if (canScrollLeft) View.VISIBLE else View.GONE
                vFadeRight?.visibility = if (canScrollRight) View.VISIBLE else View.GONE
            }

            hsvScroll.setOnScrollChangeListener { _, _, _, _, _ ->
                updateFades()
            }
            hsvScroll.post {
                updateFades()
            }
        } else {
            vFadeLeft?.visibility = View.GONE
            vFadeRight?.visibility = View.GONE
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

        // 5. Style Specific Formatting
        val isOreo = com.example.data.PanelPreferences.isAndroidOreoStyle(context)
        val isMarshmallow = com.example.data.PanelPreferences.isAndroidMarshmallowStyle(context)
        if (isMarshmallow) {
            val now = java.util.Date()
            val timeFormatter = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
            val dateFormatter = java.text.SimpleDateFormat("EEEE, MMMM d", java.util.Locale.getDefault())
            val headerBg = if (isDark) Color.parseColor("#263238") else Color.parseColor("#E0E5E8")
            val textPrimary = if (isDark) Color.WHITE else Color.parseColor("#263238")
            val textSecondary = if (isDark) Color.parseColor("#FFB0BEC5") else Color.parseColor("#FF546E7A")
            tvOreoTopTime?.text = timeFormatter.format(now)
            tvOreoTopTime?.setTextColor(textPrimary)
            tvOreoDate?.text = dateFormatter.format(now)
            tvOreoDate?.setTextColor(textSecondary)
            val panelOuterCorner = com.example.data.PanelPreferences.panelOuterCorner.value
            val outerPx = panelOuterCorner * density
            llContainer.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, outerPx, outerPx, outerPx, outerPx)
                setColor(headerBg)
            }
        } else if (isOreo) {
            val now = java.util.Date()
            val dateFormatter = java.text.SimpleDateFormat("EEE, MMM d", java.util.Locale.getDefault())
            val timeFormatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            tvOreoDate?.visibility = View.VISIBLE
            tvOreoDate?.text = dateFormatter.format(now)
            tvOreoDate?.setTextColor(textSecondary)
            tvOreoTopTime?.text = timeFormatter.format(now)
            tvOreoTopTime?.setTextColor(textSecondary)
            tvOreoTopCarrier?.setTextColor(textSecondary)
            tvOreoTopBatteryText?.setTextColor(textSecondary)
            ivOreoTopBatteryIcon?.setColorFilter(textSecondary)
            ivOreoAlarm?.visibility = View.GONE
            btnOreoSettings?.setColorFilter(textSecondary)
            btnOreoEdit?.setColorFilter(textSecondary)
            ivOreoExpand?.setColorFilter(textSecondary)
            findViewById<View>(R.id.v_qqs_oreo_tiles_divider)?.setBackgroundColor(if (isDark) Color.parseColor("#33FFFFFF") else Color.parseColor("#33000000"))

            val solidBg = MonetThemeHelper.getSurfaceBackground(context, isDark)
            llContainer.setBackgroundColor(solidBg)
        }
    }
}
