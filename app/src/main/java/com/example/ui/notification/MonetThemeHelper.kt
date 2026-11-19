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

    private fun getColorScheme(context: Context, isDark: Boolean): androidx.compose.material3.ColorScheme {
        return com.example.ui.theme.getDynamicColorScheme(context = context, isDark = isDark, dynamicColor = true)
    }

    fun getPrimaryAccent(context: Context, isDark: Boolean): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                return getColorScheme(context, isDark).primary.toArgb()
            } catch (_: Throwable) {}
            try {
                val resId = if (isDark) android.R.color.system_accent1_200 else android.R.color.system_accent1_600
                val color = ContextCompat.getColor(context, resId)
                if (color != 0) return color
            } catch (_: Throwable) {}
        }
        return if (isDark) Color.parseColor("#D0BCFF") else Color.parseColor("#6750A4")
    }

    private fun resolveAccentByMode(
        context: Context,
        isDark: Boolean,
        mode: com.example.data.ComplicationArcMonetMode
    ): Int {
        return when (mode) {
            com.example.data.ComplicationArcMonetMode.SECONDARY -> getSecondaryAccent(context, isDark)
            com.example.data.ComplicationArcMonetMode.TERTIARY -> getTertiaryAccent(context, isDark)
            com.example.data.ComplicationArcMonetMode.PRIMARY,
            com.example.data.ComplicationArcMonetMode.DEFAULT -> getPrimaryAccent(context, isDark)
        }
    }

    fun getClearAllAccent(context: Context, isDark: Boolean): Int {
        val useCustom = com.example.data.PanelPreferences.getClearAllUseCustom(isDark).value
        if (useCustom) {
            return com.example.data.PanelPreferences.getClearAllCustomColor(isDark).value
        }
        val mode = com.example.data.PanelPreferences.getClearAllColorMode(isDark).value
        val baseColor = resolveAccentByMode(context, isDark, mode)
        val hue = com.example.data.PanelPreferences.getClearAllHue(isDark).value
        val sat = com.example.data.PanelPreferences.getClearAllSat(isDark).value
        val valFactor = com.example.data.PanelPreferences.getClearAllVal(isDark).value
        if (hue == 0f && sat == 1f && valFactor == 1f) return baseColor
        val hsv = FloatArray(3)
        Color.colorToHSV(baseColor, hsv)
        hsv[0] = (hsv[0] + hue).let { h ->
            var mod = h % 360f
            if (mod < 0) mod += 360f
            mod
        }
        hsv[1] = (hsv[1] * sat).coerceIn(0f, 1f)
        hsv[2] = (hsv[2] * valFactor).coerceIn(0f, 1f)
        return Color.HSVToColor(Color.alpha(baseColor), hsv)
    }

    fun getQsTilesAccent(context: Context, isDark: Boolean): Int {
        val useCustom = com.example.data.PanelPreferences.getQsTilesUseCustom(isDark).value
        if (useCustom) {
            return com.example.data.PanelPreferences.getQsTilesCustomColor(isDark).value
        }
        val mode = com.example.data.PanelPreferences.getQsTilesColorMode(isDark).value
        val baseColor = resolveAccentByMode(context, isDark, mode)
        val hue = com.example.data.PanelPreferences.getQsTilesHue(isDark).value
        val sat = com.example.data.PanelPreferences.getQsTilesSat(isDark).value
        val valFactor = com.example.data.PanelPreferences.getQsTilesVal(isDark).value
        if (hue == 0f && sat == 1f && valFactor == 1f) return baseColor
        val hsv = FloatArray(3)
        Color.colorToHSV(baseColor, hsv)
        hsv[0] = (hsv[0] + hue).let { h ->
            var mod = h % 360f
            if (mod < 0) mod += 360f
            mod
        }
        hsv[1] = (hsv[1] * sat).coerceIn(0f, 1f)
        hsv[2] = (hsv[2] * valFactor).coerceIn(0f, 1f)
        return Color.HSVToColor(Color.alpha(baseColor), hsv)
    }

    fun getSlidersAccent(context: Context, isDark: Boolean): Int {
        val useCustom = com.example.data.PanelPreferences.getSlidersUseCustom(isDark).value
        if (useCustom) {
            return com.example.data.PanelPreferences.getSlidersCustomColor(isDark).value
        }
        val mode = com.example.data.PanelPreferences.getSlidersColorMode(isDark).value
        val baseColor = resolveAccentByMode(context, isDark, mode)
        val hue = com.example.data.PanelPreferences.getSlidersHue(isDark).value
        val sat = com.example.data.PanelPreferences.getSlidersSat(isDark).value
        val valFactor = com.example.data.PanelPreferences.getSlidersVal(isDark).value
        if (hue == 0f && sat == 1f && valFactor == 1f) return baseColor
        val hsv = FloatArray(3)
        Color.colorToHSV(baseColor, hsv)
        hsv[0] = (hsv[0] + hue).let { h ->
            var mod = h % 360f
            if (mod < 0) mod += 360f
            mod
        }
        hsv[1] = (hsv[1] * sat).coerceIn(0f, 1f)
        hsv[2] = (hsv[2] * valFactor).coerceIn(0f, 1f)
        return Color.HSVToColor(Color.alpha(baseColor), hsv)
    }

    fun getInactiveTilesColor(context: Context, isDark: Boolean): Int {
        val useCustom = com.example.data.PanelPreferences.getInactiveTilesUseCustom(isDark).value
        if (useCustom) {
            return com.example.data.PanelPreferences.getInactiveTilesCustomColor(isDark).value
        }
        val mode = com.example.data.PanelPreferences.getInactiveTilesColorMode(isDark).value
        val scheme = getColorScheme(context, isDark)
        val baseColor = when (mode) {
            com.example.data.ComplicationArcMonetMode.PRIMARY -> scheme.primaryContainer.toArgb()
            com.example.data.ComplicationArcMonetMode.SECONDARY -> scheme.secondaryContainer.toArgb()
            com.example.data.ComplicationArcMonetMode.TERTIARY -> scheme.tertiaryContainer.toArgb()
            com.example.data.ComplicationArcMonetMode.DEFAULT -> getActionBackground(context, isDark)
        }
        val hue = com.example.data.PanelPreferences.getInactiveTilesHue(isDark).value
        val sat = com.example.data.PanelPreferences.getInactiveTilesSat(isDark).value
        val valFactor = com.example.data.PanelPreferences.getInactiveTilesVal(isDark).value
        if (hue == 0f && sat == 1f && valFactor == 1f) return baseColor
        val hsv = FloatArray(3)
        Color.colorToHSV(baseColor, hsv)
        hsv[0] = (hsv[0] + hue).let { h ->
            var mod = h % 360f
            if (mod < 0) mod += 360f
            mod
        }
        hsv[1] = (hsv[1] * sat).coerceIn(0f, 1f)
        hsv[2] = (hsv[2] * valFactor).coerceIn(0f, 1f)
        return Color.HSVToColor(Color.alpha(baseColor), hsv)
    }

    fun getHeaderGradientColor(context: Context, isDark: Boolean): Int {
        val useCustom = com.example.data.PanelPreferences.getHeaderGradientUseCustom(isDark).value
        if (useCustom) {
            return com.example.data.PanelPreferences.getHeaderGradientCustomColor(isDark).value
        }
        val mode = com.example.data.PanelPreferences.getHeaderGradientColorMode(isDark).value
        val baseColor = resolveAccentByMode(context, isDark, mode)
        val hue = com.example.data.PanelPreferences.getHeaderGradientHue(isDark).value
        val sat = com.example.data.PanelPreferences.getHeaderGradientSat(isDark).value
        val valFactor = com.example.data.PanelPreferences.getHeaderGradientVal(isDark).value
        if (hue == 0f && sat == 1f && valFactor == 1f) return baseColor
        val hsv = FloatArray(3)
        Color.colorToHSV(baseColor, hsv)
        hsv[0] = (hsv[0] + hue).let { h ->
            var mod = h % 360f
            if (mod < 0) mod += 360f
            mod
        }
        hsv[1] = (hsv[1] * sat).coerceIn(0f, 1f)
        hsv[2] = (hsv[2] * valFactor).coerceIn(0f, 1f)
        return Color.HSVToColor(Color.alpha(baseColor), hsv)
    }

    fun getSecondaryAccent(context: Context, isDark: Boolean): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                return getColorScheme(context, isDark).secondary.toArgb()
            } catch (_: Throwable) {}
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
                return getColorScheme(context, isDark).tertiary.toArgb()
            } catch (_: Throwable) {}
            try {
                val resId = if (isDark) android.R.color.system_accent3_200 else android.R.color.system_accent3_600
                val color = ContextCompat.getColor(context, resId)
                if (color != 0) return color
            } catch (_: Throwable) {}
        }
        val scheme = if (isDark) com.example.ui.theme.ExpressiveDarkColorScheme else com.example.ui.theme.ExpressiveLightColorScheme
        return scheme.tertiary.toArgb()
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
        val usePanelCustom = com.example.data.PanelPreferences.getPanelContainerUseCustom(isDark).value
        if (usePanelCustom) {
            return com.example.data.PanelPreferences.getPanelContainerCustomColor(isDark).value
        }
        val customColor = com.example.data.PanelPreferences.qsPanelBackgroundColor.value
            ?: com.example.data.PanelPreferences.qsBackgroundColor.value
        val panelMode = com.example.data.PanelPreferences.getPanelContainerColorMode(isDark).value
        val baseColor = if (customColor != null) {
            customColor
        } else {
            val scheme = getColorScheme(context, isDark)
            when (panelMode) {
                com.example.data.ComplicationArcMonetMode.PRIMARY -> scheme.primaryContainer.toArgb()
                com.example.data.ComplicationArcMonetMode.SECONDARY -> scheme.secondaryContainer.toArgb()
                com.example.data.ComplicationArcMonetMode.TERTIARY -> scheme.tertiaryContainer.toArgb()
                com.example.data.ComplicationArcMonetMode.DEFAULT -> scheme.surfaceVariant.toArgb()
            }
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
    fun createMonetPrimaryPillDrawable(
        context: Context,
        isDark: Boolean,
        cornerRadiusPx: Float,
        customBgColor: Int? = null
    ): RippleDrawable {
        val primaryColor = customBgColor ?: getPrimaryAccent(context, isDark)
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

    /**
     * Creates a custom FAB shape drawable based on selected ClearAllFabShape.
     */
    fun createFabShapeDrawable(
        context: Context,
        isDark: Boolean,
        fabShape: com.example.data.ClearAllFabShape,
        density: Float,
        customBgColor: Int? = null
    ): RippleDrawable {
        val primaryColor = customBgColor ?: getPrimaryAccent(context, isDark)
        val rippleColor = if (isDark) Color.parseColor("#44FFFFFF") else Color.parseColor("#33000000")

        val contentShape = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(primaryColor)
            when (fabShape) {
                com.example.data.ClearAllFabShape.CIRCLE -> {
                    shape = GradientDrawable.OVAL
                }
                com.example.data.ClearAllFabShape.PILL -> {
                    cornerRadius = 14f * density
                }
                com.example.data.ClearAllFabShape.ROUNDED -> {
                    cornerRadius = 10f * density
                }
                com.example.data.ClearAllFabShape.CUT_CORNER -> {
                    cornerRadius = 4f * density
                }
                com.example.data.ClearAllFabShape.STAR -> {
                    cornerRadius = 24f * density
                }
            }
        }

        val maskShape = GradientDrawable().apply {
            shape = contentShape.shape
            cornerRadius = contentShape.cornerRadius
            setColor(Color.WHITE)
        }

        return RippleDrawable(ColorStateList.valueOf(rippleColor), contentShape, maskShape)
    }
}
