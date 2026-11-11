package com.example.ui.notification

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.view.View
import androidx.core.graphics.PathParser
import kotlin.math.max

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
        customOffsetY: Float? = null
    ) {
        view.elevation = 0f
        ShadowOutlineDrawable.cachedDensity = view.resources.displayMetrics.density

        if (contentDrawable == null) {
            view.background = null
            return
        }

        if (elevation <= 0.2f) {
            view.background = contentDrawable
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

        view.background = LayerDrawable(arrayOf(shadowDrawable, contentDrawable))
    }
}
