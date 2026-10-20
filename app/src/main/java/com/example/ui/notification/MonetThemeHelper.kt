package com.example.ui.notification

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import androidx.compose.ui.graphics.toArgb
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

    fun desaturateAndSoften(colorInt: Int, isDark: Boolean): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(colorInt, hsv)
        hsv[1] = (hsv[1] * 0.50f).coerceIn(0f, 1f)
        if (isDark) {
            hsv[2] = hsv[2].coerceAtMost(0.78f)
        } else {
            hsv[2] = (hsv[2] * 0.90f).coerceIn(0f, 1f)
        }
        return Color.HSVToColor(Color.alpha(colorInt), hsv)
    }

    fun getTertiaryAccent(context: Context, isDark: Boolean): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val resId = if (isDark) android.R.color.system_accent3_200 else android.R.color.system_accent3_600
                val color = ContextCompat.getColor(context, resId)
                if (color != 0) return desaturateAndSoften(color, isDark)
            } catch (_: Throwable) {}
        }
        val fallback = if (isDark) Color.parseColor("#CF889A") else Color.parseColor("#7D4E62")
        return desaturateAndSoften(fallback, isDark)
    }

    fun brightenColor(colorInt: Int, isDark: Boolean): Int {
        val percent = if (isDark) 0.10f else 0.05f
        val r = Color.red(colorInt) / 255f
        val g = Color.green(colorInt) / 255f
        val b = Color.blue(colorInt) / 255f
        val factor = 1f + percent
        val scaledR = (r * factor).coerceIn(0f, 1f)
        val scaledG = (g * factor).coerceIn(0f, 1f)
        val scaledB = (b * factor).coerceIn(0f, 1f)
        val blendR = (r + (1f - r) * percent).coerceIn(0f, 1f)
        val blendG = (g + (1f - g) * percent).coerceIn(0f, 1f)
        val blendB = (b + (1f - b) * percent).coerceIn(0f, 1f)
        val newR = (Math.max(scaledR, blendR) * 255f).toInt().coerceIn(0, 255)
        val newG = (Math.max(scaledG, blendG) * 255f).toInt().coerceIn(0, 255)
        val newB = (Math.max(scaledB, blendB) * 255f).toInt().coerceIn(0, 255)
        val a = Color.alpha(colorInt)
        return Color.argb(a, newR, newG, newB)
    }

    fun getCardBackground(context: Context, isDark: Boolean): Int {
        val customColor = com.example.data.PanelPreferences.qsPanelBackgroundColor.value
            ?: com.example.data.PanelPreferences.qsBackgroundColor.value
        val isPanelTertiary = com.example.data.PanelPreferences.panelContainerColorTertiary.value
        val baseColor = if (customColor != null) {
            customColor
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val scheme = if (isDark) androidx.compose.material3.dynamicDarkColorScheme(context)
                else androidx.compose.material3.dynamicLightColorScheme(context)
                val color = if (isPanelTertiary) desaturateAndSoften(scheme.tertiaryContainer.toArgb(), isDark) else scheme.surfaceVariant.toArgb()
                color
            } catch (_: Throwable) {
                val scheme = if (isDark) com.example.ui.theme.ExpressiveDarkColorScheme else com.example.ui.theme.ExpressiveLightColorScheme
                val color = if (isPanelTertiary) desaturateAndSoften(scheme.tertiaryContainer.toArgb(), isDark) else scheme.surfaceVariant.toArgb()
                color
            }
        } else {
            val scheme = if (isDark) com.example.ui.theme.ExpressiveDarkColorScheme else com.example.ui.theme.ExpressiveLightColorScheme
            val color = if (isPanelTertiary) desaturateAndSoften(scheme.tertiaryContainer.toArgb(), isDark) else scheme.surfaceVariant.toArgb()
            color
        }

        val isSolidMonet = com.example.data.PanelPreferences.showMonetSurfaceBackground.value
        return if (isSolidMonet) {
            brightenColor(baseColor, isDark)
        } else {
            baseColor
        }
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
        showStroke: Boolean = false,
        customBgColor: Int? = null
    ): RippleDrawable {
        val bgColor = customBgColor ?: getCardBackground(context, isDark)
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
        showStroke: Boolean = false,
        customBgColor: Int? = null
    ): RippleDrawable {
        val radii = FloatArray(8) { cornerRadiusPx }
        return createMonetCardDrawable(context, isDark, radii, showStroke, customBgColor)
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
