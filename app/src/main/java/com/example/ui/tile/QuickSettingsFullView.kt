package com.example.ui.tile

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.provider.Settings
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.model.QsGridLayout
import com.example.model.QsTileModel
import com.example.ui.notification.MonetThemeHelper

class QuickSettingsFullView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val llTopBrightnessRow: LinearLayout
    private val flTopBrightnessIconPill: FrameLayout
    private val ivTopBrightnessIcon: ImageView
    private val sbTopBrightness: SeekBar
    private val tvTopBrightnessPercentage: TextView

    private val rvTiles: RecyclerView
    private val tileLayoutManager: LinearLayoutManager
    private val snapHelper: PagerSnapHelper

    private val llBottomBrightnessRow: LinearLayout
    private val flBottomBrightnessIconPill: FrameLayout
    private val ivBottomBrightnessIcon: ImageView
    private val sbBottomBrightness: SeekBar
    private val tvBottomBrightnessPercentage: TextView

    private val rlFooterRow: RelativeLayout
    private val btnCollapse: ImageView
    private val llPageIndicators: LinearLayout
    private val btnEditMode: ImageView
    private val btnSettings: ImageView
    private val btnPower: ImageView

    private val pageAdapter: QsPageAdapter

    var onTileClick: ((QsTileModel) -> Unit)? = null
    var onTileLongClick: ((QsTileModel) -> Unit)? = null
    var onBrightnessChange: ((Int) -> Unit)? = null
    var onCollapseToQqs: (() -> Unit)? = null
    var onToggleEditMode: (() -> Unit)? = null
    var onClosePanel: (() -> Unit)? = null

    private var isDark: Boolean = true
    private var currentPageIndex: Int = 0

    init {
        val root = LayoutInflater.from(context).inflate(R.layout.layout_full_qs_panel, this, true)

        llTopBrightnessRow = root.findViewById(R.id.ll_qs_top_brightness_row)
        flTopBrightnessIconPill = root.findViewById(R.id.fl_qs_top_brightness_icon_pill)
        ivTopBrightnessIcon = root.findViewById(R.id.iv_qs_top_brightness_icon)
        sbTopBrightness = root.findViewById(R.id.sb_qs_top_brightness)
        tvTopBrightnessPercentage = root.findViewById(R.id.tv_qs_top_brightness_percentage)

        rvTiles = root.findViewById(R.id.rv_qs_tiles)
        tileLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvTiles.layoutManager = tileLayoutManager
        snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rvTiles)

        llBottomBrightnessRow = root.findViewById(R.id.ll_qs_bottom_brightness_row)
        flBottomBrightnessIconPill = root.findViewById(R.id.fl_qs_bottom_brightness_icon_pill)
        ivBottomBrightnessIcon = root.findViewById(R.id.iv_qs_bottom_brightness_icon)
        sbBottomBrightness = root.findViewById(R.id.sb_qs_bottom_brightness)
        tvBottomBrightnessPercentage = root.findViewById(R.id.tv_qs_bottom_brightness_percentage)

        rlFooterRow = root.findViewById(R.id.rl_qs_footer_row)
        btnCollapse = root.findViewById(R.id.btn_qs_collapse)
        llPageIndicators = root.findViewById(R.id.ll_qs_page_indicators)
        btnEditMode = root.findViewById(R.id.btn_qs_edit_mode)
        btnSettings = root.findViewById(R.id.btn_qs_settings)
        btnPower = root.findViewById(R.id.btn_qs_power)

        pageAdapter = QsPageAdapter(
            onTileClick = { onTileClick?.invoke(it) },
            onTileLongClick = { onTileLongClick?.invoke(it) }
        )
        rvTiles.adapter = pageAdapter

        rvTiles.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val centerView = snapHelper.findSnapView(tileLayoutManager)
                    if (centerView != null) {
                        val pos = tileLayoutManager.getPosition(centerView)
                        if (pos != RecyclerView.NO_POSITION && pos != currentPageIndex) {
                            currentPageIndex = pos
                            updatePageIndicators(pos, pageAdapter.pages.size)
                        }
                    }
                }
            }
        })

        val brightnessListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val clamped = progress.coerceIn(5, 255)
                val pct = ((clamped / 255f) * 100).toInt()
                tvTopBrightnessPercentage.text = "$pct%"
                tvBottomBrightnessPercentage.text = "$pct%"
                val iconRes = if (clamped > 128) R.drawable.ic_qs_brightness_high else R.drawable.ic_qs_brightness_low
                ivTopBrightnessIcon.setImageResource(iconRes)
                ivBottomBrightnessIcon.setImageResource(iconRes)
                if (fromUser) {
                    onBrightnessChange?.invoke(clamped)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
        sbTopBrightness.setOnSeekBarChangeListener(brightnessListener)
        sbBottomBrightness.setOnSeekBarChangeListener(brightnessListener)

        btnCollapse.setOnClickListener {
            onCollapseToQqs?.invoke()
        }

        btnEditMode.setOnClickListener {
            onToggleEditMode?.invoke()
        }

        btnSettings.setOnClickListener {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Throwable) {}
            onClosePanel?.invoke()
        }

        btnPower.setOnClickListener {
            val accessibilityService = com.example.service.QuickPanelAccessibilityService.instance
            if (accessibilityService != null) {
                accessibilityService.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_POWER_DIALOG)
            } else {
                try {
                    val intent = Intent("android.intent.action.ACTION_POWER_MENU").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (_: Throwable) {}
            }
            onClosePanel?.invoke()
        }
    }

    fun updateData(
        tiles: List<QsTileModel>,
        gridLayout: QsGridLayout,
        brightness: Int,
        brightnessAtBottom: Boolean,
        activeSquareShape: Boolean,
        isDark: Boolean
    ) {
        this.isDark = isDark
        val density = context.resources.displayMetrics.density

        // 1. Resolve Monet Theme Colors
        val primaryAccent = MonetThemeHelper.getPrimaryAccent(context, isDark)
        val textPrimary = MonetThemeHelper.getTextPrimary(context, isDark)
        val textSecondary = MonetThemeHelper.getTextSecondary(context, isDark)
        val cardOutline = MonetThemeHelper.getCardOutline(context, isDark)

        val pillColor = Color.argb(
            (0.15f * 255).toInt(),
            Color.red(primaryAccent),
            Color.green(primaryAccent),
            Color.blue(primaryAccent)
        )

        // 2. Brightness Sliders Setup
        val clampedBrightness = brightness.coerceIn(5, 255)
        val pct = ((clampedBrightness / 255f) * 100).toInt()
        val iconRes = if (clampedBrightness > 128) R.drawable.ic_qs_brightness_high else R.drawable.ic_qs_brightness_low

        if (brightnessAtBottom) {
            llTopBrightnessRow.visibility = View.GONE
            llBottomBrightnessRow.visibility = View.VISIBLE
            sbBottomBrightness.progress = clampedBrightness
            tvBottomBrightnessPercentage.text = "$pct%"
            tvBottomBrightnessPercentage.setTextColor(textPrimary)
            flBottomBrightnessIconPill.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(pillColor)
            }
            ivBottomBrightnessIcon.setImageResource(iconRes)
            ivBottomBrightnessIcon.setColorFilter(primaryAccent)
            sbBottomBrightness.thumbTintList = ColorStateList.valueOf(primaryAccent)
            sbBottomBrightness.progressTintList = ColorStateList.valueOf(primaryAccent)
            sbBottomBrightness.progressBackgroundTintList = ColorStateList.valueOf(cardOutline)
        } else {
            llTopBrightnessRow.visibility = View.VISIBLE
            llBottomBrightnessRow.visibility = View.GONE
            sbTopBrightness.progress = clampedBrightness
            tvTopBrightnessPercentage.text = "$pct%"
            tvTopBrightnessPercentage.setTextColor(textPrimary)
            flTopBrightnessIconPill.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(pillColor)
            }
            ivTopBrightnessIcon.setImageResource(iconRes)
            ivTopBrightnessIcon.setColorFilter(primaryAccent)
            sbTopBrightness.thumbTintList = ColorStateList.valueOf(primaryAccent)
            sbTopBrightness.progressTintList = ColorStateList.valueOf(primaryAccent)
            sbTopBrightness.progressBackgroundTintList = ColorStateList.valueOf(cardOutline)
        }

        // 3. Tile Pages Chunking
        val activeTiles = tiles.filter { it.isVisible }
        val tilesPerPage = (gridLayout.columns * gridLayout.rows).coerceAtLeast(1)
        val pages = activeTiles.chunked(tilesPerPage)
        val finalPages = if (pages.isEmpty()) listOf(emptyList()) else pages

        pageAdapter.pages = finalPages
        pageAdapter.gridLayout = gridLayout
        pageAdapter.isDark = isDark
        pageAdapter.activeSquareShape = activeSquareShape
        pageAdapter.notifyDataSetChanged()

        // 4. Page Indicators Setup
        if (currentPageIndex >= finalPages.size) {
            currentPageIndex = 0
        }
        updatePageIndicators(currentPageIndex, finalPages.size)

        // 5. Footer Buttons Tinting
        btnCollapse.setColorFilter(textSecondary)
        btnEditMode.setColorFilter(textSecondary)
        btnSettings.setColorFilter(textSecondary)
        btnPower.setColorFilter(textSecondary)
    }

    private fun updatePageIndicators(selectedIndex: Int, totalPages: Int) {
        val density = context.resources.displayMetrics.density
        llPageIndicators.removeAllViews()

        if (totalPages <= 1) {
            llPageIndicators.visibility = View.GONE
            return
        }

        llPageIndicators.visibility = View.VISIBLE
        val primaryAccent = MonetThemeHelper.getPrimaryAccent(context, isDark)
        val inactiveDotColor = if (isDark) Color.parseColor("#44FFFFFF") else Color.parseColor("#44000000")

        for (i in 0 until totalPages) {
            val isSelected = i == selectedIndex
            val dot = View(context).apply {
                val dotWidth = if (isSelected) (14 * density).toInt() else (6 * density).toInt()
                val dotHeight = (6 * density).toInt()
                val lp = LinearLayout.LayoutParams(dotWidth, dotHeight).apply {
                    marginEnd = (4 * density).toInt()
                }
                layoutParams = lp
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 3f * density
                    setColor(if (isSelected) primaryAccent else inactiveDotColor)
                }
            }
            llPageIndicators.addView(dot)
        }
    }
}
