package com.example.ui.notification

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.toPath

/**
 * Lightweight Android Drawable that renders any Compose [ImageVector] directly to a Canvas
 * without generating intermediate XML vector drawables or bitmaps.
 */
class ImageVectorDrawable(
    private val imageVector: ImageVector
) : Drawable() {

    private data class PathEntry(
        val basePath: Path,
        val fillAlpha: Float,
        val baseColor: Int,
        val scaledPath: Path = Path()
    )

    private val entries: List<PathEntry>
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }
    private val scaleMatrix = Matrix()
    private var drawableAlpha = 255
    private var activeColorFilter: ColorFilter? = null

    init {
        val collected = mutableListOf<PathEntry>()
        collectPaths(imageVector.root, Matrix(), collected)
        entries = collected
    }

    private fun collectPaths(
        group: VectorGroup,
        parentMatrix: Matrix,
        out: MutableList<PathEntry>
    ) {
        val localMatrix = Matrix()
        if (group.pivotX != 0f || group.pivotY != 0f) {
            localMatrix.postTranslate(-group.pivotX, -group.pivotY)
        }
        if (group.scaleX != 1f || group.scaleY != 1f) {
            localMatrix.postScale(group.scaleX, group.scaleY)
        }
        if (group.rotation != 0f) {
            localMatrix.postRotate(group.rotation)
        }
        if (group.pivotX != 0f || group.pivotY != 0f || group.translationX != 0f || group.translationY != 0f) {
            localMatrix.postTranslate(group.pivotX + group.translationX, group.pivotY + group.translationY)
        }

        val combinedMatrix = Matrix(parentMatrix)
        combinedMatrix.preConcat(localMatrix)

        for (node in group) {
            when (node) {
                is VectorGroup -> collectPaths(node, combinedMatrix, out)
                is VectorPath -> {
                    if (node.pathData.isNotEmpty() && node.fill != null) {
                        val composePath = node.pathData.toPath()
                        val path = Path(composePath.asAndroidPath()).apply {
                            fillType = if (node.pathFillType == PathFillType.EvenOdd) {
                                Path.FillType.EVEN_ODD
                            } else {
                                Path.FillType.WINDING
                            }
                            transform(combinedMatrix)
                        }
                        val fillColor = (node.fill as? SolidColor)?.value?.toArgb() ?: Color.BLACK
                        out.add(
                            PathEntry(
                                basePath = path,
                                fillAlpha = node.fillAlpha.coerceIn(0f, 1f),
                                baseColor = fillColor
                            )
                        )
                    }
                }
            }
        }
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        updateScaledPaths(bounds)
    }

    private fun updateScaledPaths(bounds: Rect) {
        val w = bounds.width().toFloat()
        val h = bounds.height().toFloat()
        if (w <= 0f || h <= 0f) return

        val vw = imageVector.viewportWidth.takeIf { it > 0f } ?: 24f
        val vh = imageVector.viewportHeight.takeIf { it > 0f } ?: 24f
        val scale = minOf(w / vw, h / vh)
        val dx = bounds.left + (w - vw * scale) * 0.5f
        val dy = bounds.top + (h - vh * scale) * 0.5f

        scaleMatrix.reset()
        scaleMatrix.setScale(scale, scale)
        scaleMatrix.postTranslate(dx, dy)

        for (entry in entries) {
            entry.basePath.transform(scaleMatrix, entry.scaledPath)
        }
    }

    override fun draw(canvas: Canvas) {
        if (bounds.isEmpty || entries.isEmpty()) return
        for (entry in entries) {
            paint.color = if (activeColorFilter != null) Color.WHITE else entry.baseColor
            paint.colorFilter = activeColorFilter
            paint.alpha = (drawableAlpha * entry.fillAlpha).toInt().coerceIn(0, 255)
            canvas.drawPath(entry.scaledPath, paint)
        }
    }

    override fun setAlpha(alpha: Int) {
        if (drawableAlpha != alpha) {
            drawableAlpha = alpha.coerceIn(0, 255)
            invalidateSelf()
        }
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        activeColorFilter = colorFilter
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
