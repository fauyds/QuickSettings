package com.example.ui.notification

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.view.View
import androidx.core.graphics.PathParser
import com.example.data.PanelPreferences
import com.example.ui.overlay.createAndroid16FrostedSurfaceColorFilter
import com.example.ui.overlay.createAndroid16FrostedSurfaceXfermode
import com.example.ui.overlay.createFrostedGlassCardBevelShader
import com.example.ui.overlay.isAndroid16FrostedGlassSupported
import com.example.ui.overlay.isRuntimeShaderRefractionSupported
import kotlin.math.roundToInt

/**
 * Realistic multi-tier Gaussian blur outline shadow drawable for Android Views.
 * Eliminates native RenderNode 3D elevation lighting artifacts (such as dark corner smudges)
 * and continues rendering smoothly when translated out of bounds during gestures or offsets.
 */
class ShadowOutlineDrawable(
    var radii: FloatArray? = null,
    var cornerRadius: Float = 0f,
    var pathData: String? = null,
    var viewportWidth: Float = 100f,
    var viewportHeight: Float = 100f,
    var elevation: Float = 8f,
    var isDark: Boolean = true,
    var isCircle: Boolean = false,
    var isPill: Boolean = false,
    var customAlpha: Float? = null,
    var customOffsetY: Float? = null
) : Drawable() {

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val cachedPath = Path()
    private val matrix = Matrix()
    private var basePath: Path? = null

    init {
        updateBasePath()
    }

    fun updatePathData(newPathData: String?, vw: Float = 100f, vh: Float = 100f) {
        if (pathData != newPathData || viewportWidth != vw || viewportHeight != vh) {
            pathData = newPathData
            viewportWidth = vw
            viewportHeight = vh
            updateBasePath()
            updateCachedPath(bounds)
            invalidateSelf()
        }
    }

    private fun updateBasePath() {
        basePath = pathData?.let {
            try {
                PathParser.createPathFromPathData(it)
            } catch (_: Exception) {
                null
            }
        }
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        updateCachedPath(bounds)
    }

    private fun updateCachedPath(bounds: Rect) {
        cachedPath.reset()
        if (bounds.width() <= 0 || bounds.height() <= 0) return

        val bp = basePath
        if (bp != null) {
            matrix.reset()
            val sx = if (viewportWidth > 0f) bounds.width().toFloat() / viewportWidth else 1f
            val sy = if (viewportHeight > 0f) bounds.height().toFloat() / viewportHeight else 1f
            matrix.setScale(sx, sy)
            bp.transform(matrix, cachedPath)
            cachedPath.offset(bounds.left.toFloat(), bounds.top.toFloat())
        } else if (isCircle) {
            val radius = minOf(bounds.width(), bounds.height()) / 2f
            cachedPath.addCircle(
                bounds.exactCenterX(),
                bounds.exactCenterY(),
                radius,
                Path.Direction.CW
            )
        } else if (isPill) {
            val pillRadius = bounds.height() / 2f
            cachedPath.addRoundRect(
                bounds.left.toFloat(), bounds.top.toFloat(),
                bounds.right.toFloat(), bounds.bottom.toFloat(),
                pillRadius, pillRadius,
                Path.Direction.CW
            )
        } else if (radii != null && radii!!.size >= 8) {
            cachedPath.addRoundRect(
                bounds.left.toFloat(), bounds.top.toFloat(),
                bounds.right.toFloat(), bounds.bottom.toFloat(),
                radii!!,
                Path.Direction.CW
            )
        } else if (cornerRadius > 0f) {
            cachedPath.addRoundRect(
                bounds.left.toFloat(), bounds.top.toFloat(),
                bounds.right.toFloat(), bounds.bottom.toFloat(),
                cornerRadius, cornerRadius,
                Path.Direction.CW
            )
        } else {
            cachedPath.addRect(
                bounds.left.toFloat(), bounds.top.toFloat(),
                bounds.right.toFloat(), bounds.bottom.toFloat(),
                Path.Direction.CW
            )
        }
    }

    override fun draw(canvas: Canvas) {
        if (elevation <= 0.2f || bounds.width() <= 0 || bounds.height() <= 0) return

        val density = if (cachedDensity > 0f) cachedDensity else 2.625f
        val elevationPx = elevation * density
        if (elevationPx <= 0.5f) return

        val ambientBaseAlpha = if (isDark) 0.22f else 0.10f
        val keyBaseAlpha = if (isDark) 0.32f else 0.16f
        val elevationFactor = (elevation / 8f).coerceIn(0.6f, 1.8f)
        val userAlpha = customAlpha ?: (if (isDark) 0.35f else 0.25f)
        val alphaScale = (userAlpha / 0.35f).coerceIn(0.3f, 2.5f)

        val ambientAlpha = (ambientBaseAlpha * elevationFactor * alphaScale).coerceIn(0.04f, 0.45f)
        val keyAlpha = (keyBaseAlpha * elevationFactor * alphaScale).coerceIn(0.06f, 0.55f)

        val ambientRadius = (elevationPx * 1.3f).coerceAtLeast(1f)
        val ambientOffsetY = customOffsetY?.let { it * density + (elevationPx * 0.15f) } ?: (elevationPx * 0.25f)

        val keyRadius = (elevationPx * 0.75f).coerceAtLeast(1f)
        val keyOffsetY = customOffsetY?.let { it * density + (elevationPx * 0.55f) } ?: (elevationPx * 0.65f)

        // 1. Ambient soft omnidirectional shadow
        shadowPaint.color = Color.argb((ambientAlpha * 255).toInt(), 0, 0, 0)
        shadowPaint.maskFilter = BlurMaskFilter(ambientRadius, BlurMaskFilter.Blur.NORMAL)
        canvas.save()
        canvas.translate(0f, ambientOffsetY)
        canvas.drawPath(cachedPath, shadowPaint)
        canvas.restore()

        // 2. Key directional drop shadow
        shadowPaint.color = Color.argb((keyAlpha * 255).toInt(), 0, 0, 0)
        shadowPaint.maskFilter = BlurMaskFilter(keyRadius, BlurMaskFilter.Blur.NORMAL)
        canvas.save()
        canvas.translate(0f, keyOffsetY)
        canvas.drawPath(cachedPath, shadowPaint)
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) {
        shadowPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        shadowPaint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    companion object {
        var cachedDensity: Float = 2.625f
    }
}

object CustomShadowHelper {

    /**
     * Applies a multi-tier Gaussian blur outline shadow to a View without native elevation.
     * Prevents corner smudges and enables smooth translation/out-of-bounds rendering.
     */
    fun applyCustomShadowBackground(
        view: View,
        contentDrawable: Drawable?,
        radii: FloatArray? = null,
        cornerRadius: Float = 0f,
        pathData: String? = null,
        viewportWidth: Float = 100f,
        viewportHeight: Float = 100f,
        elevation: Float,
        isDark: Boolean,
        isCircle: Boolean = false,
        isPill: Boolean = false,
        customAlpha: Float? = null,
        customOffsetY: Float? = null,
        enableTopRimBackground: Boolean = false,
        baseColor: Int? = null,
        topRimAlpha: Float = PanelPreferences.topRimAlpha.value,
        enableFrostedGlass: Boolean = false,
        frostedTransparency: Float = PanelPreferences.containerBlurTransparency.value,
        enableGlassRefraction: Boolean = PanelPreferences.enableGlassRefraction.value,
        glassRefractionSensitivity: Float = PanelPreferences.glassRefractionSensitivity.value,
        refractionStyle: com.example.data.RefractionStyle = PanelPreferences.refractionStyle.value,
        glassRefractionScale: Float = PanelPreferences.glassRefractionScale.value,
        glassRefractionDispersion: Float = PanelPreferences.glassRefractionDispersion.value
    ) {
        view.elevation = 0f
        val density = view.resources.displayMetrics.density
        ShadowOutlineDrawable.cachedDensity = density

        if (contentDrawable == null) {
            view.background = null
            return
        }

        val frostedSurfaceDrawable = if (enableFrostedGlass && !isCircle && isAndroid16FrostedGlassSupported()) {
            FrostedGlassSurfaceDrawable(
                radii = radii?.copyOf(),
                cornerRadius = cornerRadius,
                isDark = isDark,
                transparency = frostedTransparency,
                enableGlassRefraction = enableGlassRefraction,
                glassRefractionSensitivity = glassRefractionSensitivity,
                density = density,
                refractionStyle = refractionStyle,
                glassRefractionScale = glassRefractionScale,
                glassRefractionDispersion = glassRefractionDispersion
            )
        } else null

        val bgGradientDrawable = if (enableTopRimBackground) {
            PanelTopRimBackgroundDrawable(
                radii = radii?.copyOf(),
                cornerRadius = cornerRadius,
                baseColor = baseColor,
                topRimAlpha = topRimAlpha
            )
        } else null

        if (elevation <= 0.2f) {
            val baseLayers = buildList {
                add(contentDrawable)
                if (frostedSurfaceDrawable != null) add(frostedSurfaceDrawable)
                if (bgGradientDrawable != null) add(bgGradientDrawable)
            }
            view.background = if (baseLayers.size == 1) {
                baseLayers[0]
            } else {
                LayerDrawable(baseLayers.toTypedArray())
            }
            return
        }

        val shadowDrawable = ShadowOutlineDrawable(
            radii = radii,
            cornerRadius = cornerRadius,
            pathData = pathData,
            viewportWidth = viewportWidth,
            viewportHeight = viewportHeight,
            elevation = elevation,
            isDark = isDark,
            isCircle = isCircle,
            isPill = isPill,
            customAlpha = customAlpha,
            customOffsetY = customOffsetY
        )

        val layers = buildList {
            add(shadowDrawable)
            add(contentDrawable)
            if (frostedSurfaceDrawable != null) add(frostedSurfaceDrawable)
            if (bgGradientDrawable != null) add(bgGradientDrawable)
        }
        view.background = LayerDrawable(layers.toTypedArray())
    }

    /**
     * Applies a vertical gradient rim highlight (adapting opacity from 20% on dark surfaces
     * to higher contrast on bright/enabled tiles, down to 0% at the bottom) in the View's
     * foreground so it renders on top of card background and content.
     */
    fun applyPanelTopRimForeground(
        view: View,
        enabled: Boolean,
        radii: FloatArray? = null,
        cornerRadius: Float = 0f,
        strokeWidthDp: Float = 1f,
        baseColor: Int? = null,
        topRimAlpha: Float = PanelPreferences.topRimAlpha.value
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        if (!enabled) {
            if (view.foreground is TopRimOutlineDrawable) {
                view.foreground = null
            }
            return
        }
        val density = view.resources.displayMetrics.density
        val strokePx = (strokeWidthDp * density).coerceAtLeast(1f)
        val existing = view.foreground as? TopRimOutlineDrawable
        if (existing != null) {
            existing.updateShape(
                radii = radii,
                cornerRadius = cornerRadius,
                strokeWidthPx = strokePx,
                baseColor = baseColor,
                topRimAlpha = topRimAlpha
            )
        } else {
            view.foreground = TopRimOutlineDrawable(
                radii = radii?.copyOf(),
                cornerRadius = cornerRadius,
                strokeWidthPx = strokePx,
                baseColor = baseColor,
                topRimAlpha = topRimAlpha
            )
        }
    }
}

internal fun calculateBaseColorLuminanceInt(color: Int?): Float {
    if (color == null || Color.alpha(color) < 64) return 0f
    return 0.2126f * (Color.red(color) / 255f) +
        0.7152f * (Color.green(color) / 255f) +
        0.0722f * (Color.blue(color) / 255f)
}

/**
 * Draws a crisp inner border rim with a top-to-bottom vertical gradient from
 * white at the top to 0% opacity at the bottom, adapting opacity for bright fills.
 */
class TopRimOutlineDrawable(
    private var radii: FloatArray? = null,
    private var cornerRadius: Float = 0f,
    private var strokeWidthPx: Float = 2f,
    private var baseColor: Int? = null,
    private var topRimAlpha: Float = 0.20f
) : Drawable() {

    private val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val cachedPath = Path()

    fun updateShape(
        radii: FloatArray?,
        cornerRadius: Float,
        strokeWidthPx: Float,
        baseColor: Int? = this.baseColor,
        topRimAlpha: Float = this.topRimAlpha
    ) {
        val clampedRimAlpha = topRimAlpha.coerceIn(0.05f, 0.30f)
        val radiiChanged = when {
            this.radii == null && radii == null -> false
            this.radii != null && radii != null -> !this.radii.contentEquals(radii)
            else -> true
        }
        if (radiiChanged || this.cornerRadius != cornerRadius || this.strokeWidthPx != strokeWidthPx || this.baseColor != baseColor || this.topRimAlpha != clampedRimAlpha) {
            this.radii = radii?.copyOf()
            this.cornerRadius = cornerRadius
            this.strokeWidthPx = strokeWidthPx
            this.baseColor = baseColor
            this.topRimAlpha = clampedRimAlpha
            updateCachedPath(bounds)
            updateShader(bounds)
            invalidateSelf()
        }
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        updateCachedPath(bounds)
        updateShader(bounds)
    }

    private fun updateCachedPath(bounds: Rect) {
        cachedPath.reset()
        if (bounds.width() <= 0 || bounds.height() <= 0) return
        val r = radii
        when {
            r != null && r.size >= 8 -> {
                cachedPath.addRoundRect(
                    bounds.left.toFloat(), bounds.top.toFloat(),
                    bounds.right.toFloat(), bounds.bottom.toFloat(),
                    r,
                    Path.Direction.CW
                )
            }
            cornerRadius > 0f -> {
                cachedPath.addRoundRect(
                    bounds.left.toFloat(), bounds.top.toFloat(),
                    bounds.right.toFloat(), bounds.bottom.toFloat(),
                    cornerRadius, cornerRadius,
                    Path.Direction.CW
                )
            }
            else -> {
                cachedPath.addRect(
                    bounds.left.toFloat(), bounds.top.toFloat(),
                    bounds.right.toFloat(), bounds.bottom.toFloat(),
                    Path.Direction.CW
                )
            }
        }
    }

    private fun updateShader(bounds: Rect) {
        if (bounds.height() <= 0) {
            rimPaint.shader = null
            return
        }
        val lum = calculateBaseColorLuminanceInt(baseColor)
        val effectiveRimAlpha = topRimAlpha.coerceIn(0.05f, 0.30f)
        val scale = effectiveRimAlpha / 0.20f
        val topAlpha = when {
            lum > 0.85f -> (166f * scale).roundToInt().coerceIn(0, 255)
            lum > 0.30f -> (133f * scale).roundToInt().coerceIn(0, 255)
            else -> (effectiveRimAlpha * 255f).roundToInt().coerceIn(0, 255)
        }
        rimPaint.shader = LinearGradient(
            bounds.left.toFloat(),
            bounds.top.toFloat(),
            bounds.left.toFloat(),
            bounds.bottom.toFloat(),
            Color.argb(topAlpha, 255, 255, 255),
            Color.argb(0, 255, 255, 255),
            Shader.TileMode.CLAMP
        )
    }

    override fun draw(canvas: Canvas) {
        if (cachedPath.isEmpty || bounds.width() <= 0 || bounds.height() <= 0) return
        if (rimPaint.shader == null) updateShader(bounds)
        rimPaint.strokeWidth = strokeWidthPx * 2f
        canvas.save()
        canvas.clipPath(cachedPath)
        canvas.drawPath(cachedPath, rimPaint)
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) {
        rimPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        rimPaint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

/**
 * Draws a subtle vertical background sheen over a card's base background fill
 * when Top Rim is enabled, adapting contrast for bright/enabled tiles.
 */
class PanelTopRimBackgroundDrawable(
    private var radii: FloatArray? = null,
    private var cornerRadius: Float = 0f,
    private var baseColor: Int? = null,
    private var topRimAlpha: Float = 0.20f
) : Drawable() {

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val cachedPath = Path()

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        updateCachedPath(bounds)
        updateShader(bounds)
    }

    private fun updateCachedPath(bounds: Rect) {
        cachedPath.reset()
        if (bounds.width() <= 0 || bounds.height() <= 0) return
        val r = radii
        when {
            r != null && r.size >= 8 -> {
                cachedPath.addRoundRect(
                    bounds.left.toFloat(), bounds.top.toFloat(),
                    bounds.right.toFloat(), bounds.bottom.toFloat(),
                    r,
                    Path.Direction.CW
                )
            }
            cornerRadius > 0f -> {
                cachedPath.addRoundRect(
                    bounds.left.toFloat(), bounds.top.toFloat(),
                    bounds.right.toFloat(), bounds.bottom.toFloat(),
                    cornerRadius, cornerRadius,
                    Path.Direction.CW
                )
            }
            else -> {
                cachedPath.addRect(
                    bounds.left.toFloat(), bounds.top.toFloat(),
                    bounds.right.toFloat(), bounds.bottom.toFloat(),
                    Path.Direction.CW
                )
            }
        }
    }

    private fun updateShader(bounds: Rect) {
        if (bounds.height() <= 0) {
            fillPaint.shader = null
            return
        }
        val lum = calculateBaseColorLuminanceInt(baseColor)
        val effectiveRimAlpha = topRimAlpha.coerceIn(0.05f, 0.30f)
        val scale = effectiveRimAlpha / 0.20f
        val (topColor, bottomColor) = when {
            lum > 0.85f -> Color.argb((13f * scale).roundToInt().coerceIn(0, 255), 0, 0, 0) to
                Color.argb((36f * scale).roundToInt().coerceIn(0, 255), 0, 0, 0)
            lum > 0.30f -> Color.argb((46f * scale).roundToInt().coerceIn(0, 255), 255, 255, 255) to
                Color.argb((38f * scale).roundToInt().coerceIn(0, 255), 0, 0, 0)
            else -> Color.argb((26f * scale).roundToInt().coerceIn(0, 255), 255, 255, 255) to
                Color.argb(0, 255, 255, 255)
        }
        fillPaint.shader = LinearGradient(
            bounds.left.toFloat(),
            bounds.top.toFloat(),
            bounds.left.toFloat(),
            bounds.bottom.toFloat(),
            topColor,
            bottomColor,
            Shader.TileMode.CLAMP
        )
    }

    override fun draw(canvas: Canvas) {
        if (cachedPath.isEmpty || bounds.width() <= 0 || bounds.height() <= 0) return
        if (fillPaint.shader == null) updateShader(bounds)
        canvas.drawPath(cachedPath, fillPaint)
    }

    override fun setAlpha(alpha: Int) {
        fillPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        fillPaint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

/**
 * Android 16+ (API 36 / BAKLAVA) frosted glass surface drawable for native Android Views
 * (individual notification cards, media cards, category headers, permission & empty cards).
 * Mirrors [com.example.ui.overlay.frostedGlassSurface] using AGSL [android.graphics.RuntimeColorFilter]
 * + [android.graphics.RuntimeXfermode] luminous sheen and Glass Edge Refraction highlights,
 * strictly clipped to the card's rounded bounds.
 */
class FrostedGlassSurfaceDrawable(
    private val radii: FloatArray? = null,
    private val cornerRadius: Float = 0f,
    private val isDark: Boolean = true,
    private val transparency: Float = 0.40f,
    private val enableGlassRefraction: Boolean = true,
    private val glassRefractionSensitivity: Float = 0.50f,
    private val density: Float = 2.625f,
    private val refractionStyle: com.example.data.RefractionStyle = com.example.data.RefractionStyle.LIQUID,
    private val glassRefractionScale: Float = refractionStyle.defaultScale,
    private val glassRefractionDispersion: Float = refractionStyle.defaultDispersion
) : Drawable() {

    private val api36ColorFilter = if (isAndroid16FrostedGlassSupported()) {
        createAndroid16FrostedSurfaceColorFilter(isDark = isDark, transparency = transparency)
    } else null

    private val api36Xfermode = if (isAndroid16FrostedGlassSupported()) {
        createAndroid16FrostedSurfaceXfermode(isDark = isDark, transparency = transparency)
    } else null

    val usesAndroid16Api: Boolean
        get() = isAndroid16FrostedGlassSupported() && api36ColorFilter != null

    val usesRefractionShader: Boolean
        get() = enableGlassRefraction && isAndroid16FrostedGlassSupported() && isRuntimeShaderRefractionSupported()

    private val sheenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        if (api36ColorFilter != null) {
            colorFilter = api36ColorFilter
        }
        if (api36Xfermode != null) {
            xfermode = api36Xfermode
        }
    }

    private val refractionBevelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val cachedPath = Path()

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        updateCachedPath(bounds)
        updateShaders(bounds)
    }

    private fun updateCachedPath(bounds: Rect) {
        cachedPath.reset()
        if (bounds.width() <= 0 || bounds.height() <= 0) return
        val r = radii
        when {
            r != null && r.size >= 8 -> {
                cachedPath.addRoundRect(
                    bounds.left.toFloat(), bounds.top.toFloat(),
                    bounds.right.toFloat(), bounds.bottom.toFloat(),
                    r,
                    Path.Direction.CW
                )
            }
            cornerRadius > 0f -> {
                cachedPath.addRoundRect(
                    bounds.left.toFloat(), bounds.top.toFloat(),
                    bounds.right.toFloat(), bounds.bottom.toFloat(),
                    cornerRadius, cornerRadius,
                    Path.Direction.CW
                )
            }
            else -> {
                cachedPath.addRect(
                    bounds.left.toFloat(), bounds.top.toFloat(),
                    bounds.right.toFloat(), bounds.bottom.toFloat(),
                    Path.Direction.CW
                )
            }
        }
    }

    private var shadersConfigured = false

    private fun updateShaders(bounds: Rect) {
        if (bounds.width() <= 0 || bounds.height() <= 0) {
            sheenPaint.shader = null
            refractionBevelPaint.shader = null
            return
        }
        sheenPaint.shader = null
        sheenPaint.color = Color.TRANSPARENT
        shadersConfigured = true

        if (enableGlassRefraction && isRuntimeShaderRefractionSupported() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val topR = radii?.takeIf { it.size >= 8 }?.get(0) ?: cornerRadius
            val botR = radii?.takeIf { it.size >= 8 }?.get(4) ?: cornerRadius
            refractionBevelPaint.shader = createFrostedGlassCardBevelShader(
                widthPx = bounds.width().toFloat(),
                heightPx = bounds.height().toFloat(),
                topRadiusPx = topR,
                bottomRadiusPx = botR,
                density = density,
                sensitivity = glassRefractionSensitivity,
                isDark = isDark,
                refractionStyle = refractionStyle,
                patternScale = glassRefractionScale,
                dispersion = glassRefractionDispersion
            )
        } else {
            refractionBevelPaint.shader = null
        }
    }

    override fun draw(canvas: Canvas) {
        if (cachedPath.isEmpty || bounds.width() <= 0 || bounds.height() <= 0) return
        if (!shadersConfigured) updateShaders(bounds)

        val bevelShader = refractionBevelPaint.shader
        if (enableGlassRefraction && bevelShader != null) {
            canvas.save()
            canvas.clipPath(cachedPath)
            canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
            canvas.drawRect(
                0f,
                0f,
                bounds.width().toFloat(),
                bounds.height().toFloat(),
                refractionBevelPaint
            )
            canvas.restore()
        }
    }

    override fun setAlpha(alpha: Int) {
        sheenPaint.alpha = alpha
        refractionBevelPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        // Keep Android 16 RuntimeColorFilter intact when external filter is null
        if (colorFilter != null) {
            sheenPaint.colorFilter = colorFilter
        } else {
            sheenPaint.colorFilter = api36ColorFilter
        }
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
