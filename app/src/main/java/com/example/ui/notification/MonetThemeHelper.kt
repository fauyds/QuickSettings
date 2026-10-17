package com.example.ui.notification

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Monet (Material You) dynamic thematic color resolver for Android 12+ (API 31+) system resources
 * with graceful fallback colors for older API versions.
 */
object MonetThemeHelper {

    fun getPrimaryAccent(context: Context, isDark: Boolean): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val resId = if (isDark) android.R.color.system_accent1_200 else android.R.color.system_accent1_600
                val color = ContextCompat.getColor(context, resId)
                if (color != 0) return color
            } catch (_: Throwable) {}
        }
        return if (isDark) Color.parseColor("#D0BCFF") else Color.parseColor("#6750A4")
    }

    fun getSecondaryAccent(context: Context, isDark: Boolean): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val resId = if (isDark) android.R.color.system_accent2_200 else android.R.color.system_accent2_600
                val color = ContextCompat.getColor(context, resId)
                if (color != 0) return color
            } catch (_: Throwable) {}
        }
        return if (isDark) Color.parseColor("#CCC2DC") else Color.parseColor("#625B71")
    }

    fun getTertiaryAccent(context: Context, isDark: Boolean): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val resId = if (isDark) android.R.color.system_accent3_200 else android.R.color.system_accent3_600
                val color = ContextCompat.getColor(context, resId)
                if (color != 0) return color
            } catch (_: Throwable) {}
        }
        return if (isDark) Color.parseColor("#EFB8C8") else Color.parseColor("#7D5260")
    }

    fun getCardBackground(context: Context, isDark: Boolean): Int {
        val isPanelTertiary = com.example.data.PanelPreferences.panelContainerColorTertiary.value
        val baseColor = if (isPanelTertiary) {
            getTertiaryAccent(context, isDark)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val resId = if (isDark) android.R.color.system_neutral2_800 else android.R.color.system_neutral2_100
                val color = ContextCompat.getColor(context, resId)
                if (color != 0) color else (if (isDark) Color.parseColor("#2B2930") else Color.parseColor("#F3EDF7"))
            } catch (_: Throwable) {
                if (isDark) Color.parseColor("#2B2930") else Color.parseColor("#F3EDF7")
            }
        } else {
            if (isDark) Color.parseColor("#2B2930") else Color.parseColor("#F3EDF7")
        }

        if (com.example.data.PanelPreferences.showMonetSurfaceBackground.value && isDark) {
            val r = (Color.red(baseColor) * 1.50f).toInt().coerceIn(0, 255)
            val g = (Color.green(baseColor) * 1.50f).toInt().coerceIn(0, 255)
            val b = (Color.blue(baseColor) * 1.50f).toInt().coerceIn(0, 255)
            return Color.rgb(r, g, b)
        }

        return baseColor
    }

    fun getCardOutline(context: Context, isDark: Boolean): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val resId = if (isDark) android.R.color.system_neutral2_700 else android.R.color.system_neutral2_300
                val color = ContextCompat.getColor(context, resId)
                if (color != 0) return color
            } catch (_: Throwable) {}
        }
        return if (isDark) Color.parseColor("#49454F") else Color.parseColor("#CAC4D0")
    }

    fun getSurfaceBackground(context: Context, isDark: Boolean): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val resId = if (isDark) android.R.color.system_neutral1_900 else android.R.color.system_neutral1_50
                val color = ContextCompat.getColor(context, resId)
                if (color != 0) return color
            } catch (_: Throwable) {}
        }
        return if (isDark) Color.parseColor("#1C1B1F") else Color.parseColor("#FFFBFE")
    }

    fun getTextPrimary(context: Context, isDark: Boolean): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val resId = if (isDark) android.R.color.system_neutral1_50 else android.R.color.system_neutral1_900
                val color = ContextCompat.getColor(context, resId)
                if (color != 0) return color
            } catch (_: Throwable) {}
        }
        return if (isDark) Color.WHITE else Color.parseColor("#1C1B1F")
    }

    fun getTextSecondary(context: Context, isDark: Boolean): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val resId = if (isDark) android.R.color.system_neutral2_200 else android.R.color.system_neutral2_700
                val color = ContextCompat.getColor(context, resId)
                if (color != 0) return color
            } catch (_: Throwable) {}
        }
        return if (isDark) Color.parseColor("#CAC4D0") else Color.parseColor("#49454F")
    }

    fun getActionBackground(context: Context, isDark: Boolean): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val resId = if (isDark) android.R.color.system_neutral2_700 else android.R.color.system_neutral2_200
                val color = ContextCompat.getColor(context, resId)
                if (color != 0) return color
            } catch (_: Throwable) {}
        }
        return if (isDark) Color.parseColor("#3B383E") else Color.parseColor("#E7E0EC")
    }

    fun getOnPrimaryText(context: Context, isDark: Boolean): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val resId = if (isDark) android.R.color.system_accent1_900 else android.R.color.system_accent1_50
                val color = ContextCompat.getColor(context, resId)
                if (color != 0) return color
            } catch (_: Throwable) {}
        }
        return if (isDark) Color.parseColor("#381E72") else Color.WHITE
    }

    /**
     * Creates a Material 3 rounded ripple drawable dynamically with Monet card background and custom per-corner radii.
     * radii: Array of 8 floats [tl_x, tl_y, tr_x, tr_y, br_x, br_y, bl_x, bl_y]
     */
    fun createMonetCardDrawable(
        context: Context,
        isDark: Boolean,
        radii: FloatArray,
        showStroke: Boolean = false
    ): RippleDrawable {
        val bgColor = getCardBackground(context, isDark)
        val outlineColor = getCardOutline(context, isDark)
        val rippleColor = if (isDark) Color.parseColor("#33FFFFFF") else Color.parseColor("#22000000")

        val contentShape = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = radii
            setColor(bgColor)
            if (showStroke) {
                setStroke(1, outlineColor)
            }
        }

        val maskShape = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = radii
            setColor(Color.WHITE)
        }

        return RippleDrawable(ColorStateList.valueOf(rippleColor), contentShape, maskShape)
    }

    /**
     * Creates a Material 3 rounded ripple drawable dynamically with Monet card background and uniform corner radius.
     */
    fun createMonetCardDrawable(
        context: Context,
        isDark: Boolean,
        cornerRadiusPx: Float,
        showStroke: Boolean = false
    ): RippleDrawable {
        val radii = FloatArray(8) { cornerRadiusPx }
        return createMonetCardDrawable(context, isDark, radii, showStroke)
    }

    /**
     * Creates a pill-shaped action button with Monet styling and ripple.
     */
    fun createMonetActionButtonDrawable(context: Context, isDark: Boolean, cornerRadiusPx: Float): RippleDrawable {
        val bgColor = getActionBackground(context, isDark)
        val rippleColor = if (isDark) Color.parseColor("#44FFFFFF") else Color.parseColor("#22000000")

        val contentShape = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = cornerRadiusPx
            setColor(bgColor)
        }

        val maskShape = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = cornerRadiusPx
            setColor(Color.WHITE)
        }

        return RippleDrawable(ColorStateList.valueOf(rippleColor), contentShape, maskShape)
    }

    /**
     * Creates a prominent primary colored pill drawable (e.g. Clear all, Enable button).
     */
    fun createMonetPrimaryPillDrawable(context: Context, isDark: Boolean, cornerRadiusPx: Float): RippleDrawable {
        val primaryColor = getPrimaryAccent(context, isDark)
        val rippleColor = if (isDark) Color.parseColor("#44FFFFFF") else Color.parseColor("#33000000")

        val contentShape = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = cornerRadiusPx
            setColor(primaryColor)
        }

        val maskShape = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = cornerRadiusPx
            setColor(Color.WHITE)
        }

        return RippleDrawable(ColorStateList.valueOf(rippleColor), contentShape, maskShape)
    }
}
