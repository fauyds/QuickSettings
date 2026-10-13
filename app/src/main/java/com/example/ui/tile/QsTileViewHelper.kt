package com.example.ui.tile

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.R
import com.example.data.PanelPreferences
import com.example.data.TileShape
import com.example.model.QsTileModel
import com.example.model.TileState
import com.example.ui.notification.MonetThemeHelper

object QsTileViewHelper {

    fun bindTile(
        view: View,
        tile: QsTileModel,
        isDark: Boolean,
        activeSquareShape: Boolean = false,
        circleSizePx: Int = -1,
        iconSizePx: Int = -1,
        showSubtitle: Boolean = true,
        showBadge: Boolean = false,
        isBadgeRemove: Boolean = true,
        tileIndex: Int = -1,
        onBadgeClick: ((QsTileModel) -> Unit)? = null,
        onClick: ((QsTileModel) -> Unit)? = null,
        onLongClick: ((QsTileModel) -> Unit)? = null
    ) {
        val context = view.context
        val density = context.resources.displayMetrics.density

        val flIconBg: FrameLayout = view.findViewById(R.id.fl_tile_icon_bg)
        val ivIcon: ImageView = view.findViewById(R.id.iv_tile_icon)
        val ivBadge: ImageView = view.findViewById(R.id.iv_tile_badge)
        val tvLabel: TextView = view.findViewById(R.id.tv_tile_label)
        val tvSubtitle: TextView = view.findViewById(R.id.tv_tile_subtitle)

        val isActive = tile.state == TileState.ACTIVE
        val isUnavailable = tile.state == TileState.UNAVAILABLE

        // 1. Resolve Monet Dynamic Colors
        val primaryAccent = MonetThemeHelper.getPrimaryAccent(context, isDark)
        val onPrimary = MonetThemeHelper.getOnPrimaryText(context, isDark)
        val inactiveBg = MonetThemeHelper.getActionBackground(context, isDark)
        val textPrimary = MonetThemeHelper.getTextPrimary(context, isDark)
        val textSecondary = MonetThemeHelper.getTextSecondary(context, isDark)

        val bgColor = when (tile.state) {
            TileState.ACTIVE -> primaryAccent
            TileState.INACTIVE -> inactiveBg
            TileState.UNAVAILABLE -> {
                val alpha = (Color.alpha(inactiveBg) * 0.5f).toInt()
                Color.argb(alpha, Color.red(inactiveBg), Color.green(inactiveBg), Color.blue(inactiveBg))
            }
        }

        val fgColor = when (tile.state) {
            TileState.ACTIVE -> onPrimary
            TileState.INACTIVE -> textPrimary
            TileState.UNAVAILABLE -> {
                val alpha = 100
                Color.argb(alpha, Color.red(textSecondary), Color.green(textSecondary), Color.blue(textSecondary))
            }
        }

        // 2. Tile Size and Shape (Circle vs Rounded Rectangle)
        val targetCircleSize = if (circleSizePx > 0) circleSizePx else (56 * density).toInt()
        val targetIconSize = if (iconSizePx > 0) iconSizePx else (26 * density).toInt()

        flIconBg.layoutParams = (flIconBg.layoutParams ?: LinearLayout.LayoutParams(targetCircleSize, targetCircleSize)).apply {
            width = targetCircleSize
            height = targetCircleSize
        }
        ivIcon.layoutParams = (ivIcon.layoutParams ?: FrameLayout.LayoutParams(targetIconSize, targetIconSize)).apply {
            width = targetIconSize
            height = targetIconSize
        }

        val enabledShapeId = PanelPreferences.enabledTileShapeId.value
        val disabledShapeId = PanelPreferences.disabledTileShapeId.value
        val chosenShapeId = if (isActive) enabledShapeId else disabledShapeId

        val shapeDrawable = com.example.ui.shape.QsShapeManager.createDrawable(chosenShapeId, bgColor)
        val maskDrawable = com.example.ui.shape.QsShapeManager.createDrawable(chosenShapeId, Color.WHITE)

        val rippleColor = if (isDark) Color.parseColor("#44FFFFFF") else Color.parseColor("#33000000")
        flIconBg.background = RippleDrawable(ColorStateList.valueOf(rippleColor), shapeDrawable, maskDrawable)

        // 3. Tile Icon
        val drawable = QsTileIconHelper.getTileDrawable(context, tile)
        ivIcon.setImageDrawable(drawable)
        ivIcon.setColorFilter(fgColor)

        // 4. Texts and Clipping Prevention
        (view as? android.view.ViewGroup)?.let { vg ->
            vg.clipChildren = false
            vg.clipToPadding = false
        }
        flIconBg.clipChildren = false
        flIconBg.clipToPadding = false

        android.util.Log.d("TileDebug", "QsTileViewHelper.bindTile: tile='${tile.label}', subtitle='${tile.subtitle}', tvSubtitle=$tvSubtitle")

        tvLabel.includeFontPadding = false
        tvLabel.text = tile.label
        tvLabel.setTextColor(textPrimary)
        if (tileIndex == 1) {
            tvLabel.translationY = -8f * density
        } else {
            tvLabel.translationY = 0f
        }

        tvSubtitle.includeFontPadding = false
        tvSubtitle.setPadding(0, 0, 0, 0)
        tvSubtitle.setSingleLine(true)
        tvSubtitle.setLineSpacing(0f, 0.85f)
        if (showSubtitle) {
            tvSubtitle.visibility = View.VISIBLE
            tvSubtitle.text = tile.subtitle
            tvSubtitle.setTextColor(textSecondary)
            tvSubtitle.maxLines = 1
            tvSubtitle.ellipsize = android.text.TextUtils.TruncateAt.END
            tvSubtitle.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 8.5f)
            if (tileIndex == 1) {
                tvSubtitle.translationY = -8f * density
            } else {
                tvSubtitle.translationY = -3.5f * density
            }
            tvSubtitle.post {
                tvSubtitle.visibility = View.VISIBLE
                tvSubtitle.includeFontPadding = false
                tvSubtitle.setPadding(0, 0, 0, 0)
                tvSubtitle.setSingleLine(true)
                tvSubtitle.setLineSpacing(0f, 0.85f)
            }
        } else {
            tvSubtitle.visibility = View.GONE
            tvSubtitle.translationY = 0f
        }

        // 5. Action Badge (for Edit Mode)
        if (showBadge) {
            ivBadge.visibility = View.VISIBLE
            if (isBadgeRemove) {
                ivBadge.setImageResource(R.drawable.ic_qs_remove)
                ivBadge.setColorFilter(Color.WHITE)
                val badgeShape = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#E53935")) // Red remove badge
                }
                ivBadge.background = badgeShape
            } else {
                ivBadge.setImageResource(R.drawable.ic_qs_add)
                ivBadge.setColorFilter(Color.WHITE)
                val badgeShape = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(primaryAccent) // Monet Primary accent add badge
                }
                ivBadge.background = badgeShape
            }
            ivBadge.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onBadgeClick?.invoke(tile)
            }
        } else {
            ivBadge.visibility = View.GONE
            ivBadge.setOnClickListener(null)
        }

        // 6. Click and Long Click Listeners
        if (onClick != null || onLongClick != null) {
            view.setOnClickListener {
                if (!isUnavailable) {
                    it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onClick?.invoke(tile)
                }
            }
            view.setOnLongClickListener {
                if (!isUnavailable) {
                    it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    onLongClick?.invoke(tile)
                    true
                } else {
                    false
                }
            }
        } else {
            view.setOnClickListener(null)
            view.setOnLongClickListener(null)
        }
    }
}
