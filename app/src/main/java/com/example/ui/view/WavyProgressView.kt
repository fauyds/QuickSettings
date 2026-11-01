package com.example.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import com.example.data.ProgressBarStyle
import kotlin.math.PI
import kotlin.math.sin

/**
 * High-performance, lightweight classic Android View for the Android 10 media notification progress bar.
 * Replaces heavy Compose Canvas subcompositions inside RecyclerView to eliminate frame drops and scrolling lag.
 */
class WavyProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density = context.resources.displayMetrics.density

    // Configurable properties
    var progress: Float = 0f
        set(value) {
            val clamped = value.coerceIn(0f, 1f)
            if (field != clamped) {
                field = clamped
                if (!isDragging) {
                    invalidate()
                }
            }
        }

    var isPlaying: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                updateWaveAnimation()
            }
        }

    var activeColor: Int = Color.WHITE
        set(value) {
            if (field != value) {
                field = value
                activePaint.color = value
                thumbPaint.color = thumbColor.takeIf { it != 0 } ?: value
                invalidate()
            }
        }

    var inactiveColor: Int = Color.parseColor("#40FFFFFF")
        set(value) {
            if (field != value) {
                field = value
                inactivePaint.color = value
                invalidate()
            }
        }

    var thumbColor: Int = Color.WHITE
        set(value) {
            if (field != value) {
                field = value
                thumbPaint.color = if (value != 0) value else activeColor
                invalidate()
            }
        }

    var sliderStyle: ProgressBarStyle = ProgressBarStyle.WAVY
        set(value) {
            if (field != value) {
                field = value
                updateWaveAnimation()
                invalidate()
            }
        }

    var onSeekListener: ((Float) -> Unit)? = null
    var onSeekStateChanged: ((Boolean) -> Unit)? = null

    // Dimensions
    var strokeWidthPx: Float = 4f * density
    var amplitudePx: Float = 3.5f * density
    var wavelengthPx: Float = 26f * density
    var thumbRadiusPx: Float = 6f * density

    // Internal state
    private var isDragging = false
    private var dragStartX = 0f
    private var dragStretchRatio = 0f
    private var animatedThumbRadiusScale = 1f

    private var phase = 0f
    private var waveAnimator: ValueAnimator? = null

    // Pre-allocated graphic objects to prevent GC during scroll & draw
    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = activeColor
    }

    private val inactivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = inactiveColor
    }

    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = thumbColor
    }

    private val pillInactivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = inactiveColor
    }

    private val pillActivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = activeColor
    }

    private val wavePath = Path()
    private val pillRect = RectF()

    init {
        thumbPaint.color = thumbColor
    }

    private fun updateWaveAnimation() {
        val shouldAnimate = isPlaying && (sliderStyle == ProgressBarStyle.WAVY || sliderStyle == ProgressBarStyle.DEFAULT)
        if (shouldAnimate) {
            if (waveAnimator == null && isAttachedToWindow) {
                waveAnimator = ValueAnimator.ofFloat(0f, (2 * PI).toFloat()).apply {
                    duration = 1300L
                    repeatCount = ValueAnimator.INFINITE
                    repeatMode = ValueAnimator.RESTART
                    interpolator = LinearInterpolator()
                    addUpdateListener { anim ->
                        phase = anim.animatedValue as Float
                        if (!isDragging) {
                            postInvalidateOnAnimation()
                        }
                    }
                    start()
                }
            }
        } else {
            waveAnimator?.cancel()
            waveAnimator = null
            phase = 0f
            invalidate()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateWaveAnimation()
    }

    override fun onDetachedFromWindow() {
        waveAnimator?.cancel()
        waveAnimator = null
        super.onDetachedFromWindow()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = (36 * density).toInt()
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val finalHeight = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> minOf(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), finalHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val centerY = h / 2f
        val currentThumbRadius = thumbRadiusPx * animatedThumbRadiusScale

        when (sliderStyle) {
            ProgressBarStyle.ANDROID_12 -> {
                drawAndroid12Style(canvas, w, centerY)
            }
            ProgressBarStyle.STRAIGHT -> {
                drawStraightStyle(canvas, w, centerY, currentThumbRadius)
            }
            else -> {
                // Default and Wavy both use the squiggly Android 13/14 wave
                drawWavyStyle(canvas, w, centerY, currentThumbRadius)
            }
        }
    }

    private fun drawWavyStyle(canvas: Canvas, w: Float, centerY: Float, currentThumbRadius: Float) {
        val startX = thumbRadiusPx
        val endX = w - thumbRadiusPx
        val trackWidth = (endX - startX).coerceAtLeast(1f)
        val thumbX = (startX + progress * trackWidth).coerceIn(startX, endX)

        // 1. Draw Inactive Track
        if (thumbX < endX - 1f) {
            inactivePaint.strokeWidth = strokeWidthPx
            canvas.drawLine(thumbX, centerY, endX, centerY, inactivePaint)
        }

        // 2. Draw Active Track (Squiggly Wave)
        if (progress > 0.005f) {
            val distActive = thumbX - startX
            if (distActive > 4f) {
                wavePath.reset()
                wavePath.moveTo(startX, centerY)

                val stretchRatio = dragStretchRatio
                val maxStretchedWaveLen = wavelengthPx * 2.6f
                val waveLen = (wavelengthPx + (maxStretchedWaveLen - wavelengthPx) * stretchRatio)
                    .coerceAtLeast(wavelengthPx * 0.5f)

                val minAmp = maxOf(amplitudePx * 0.5f, 1.6f * density)
                val amp = (amplitudePx + (minAmp - amplitudePx) * stretchRatio).coerceAtLeast(0.8f)

                val stepPx = 3f * density
                var x = startX
                while (x <= thumbX) {
                    val dist = x - startX
                    val rampIn = (dist / (waveLen * 0.35f)).coerceIn(0f, 1f)
                    val rampOut = ((thumbX - x) / (waveLen * 0.35f)).coerceIn(0f, 1f)
                    val envelope = minOf(rampIn, rampOut)
                    val waveOffset = amp * envelope * sin((dist / waveLen) * (2 * PI.toFloat()) - phase)
                    wavePath.lineTo(x, centerY + waveOffset)
                    x += stepPx
                }
                wavePath.lineTo(thumbX, centerY)

                activePaint.strokeWidth = strokeWidthPx
                canvas.drawPath(wavePath, activePaint)
            } else {
                activePaint.strokeWidth = strokeWidthPx
                canvas.drawLine(startX, centerY, thumbX, centerY, activePaint)
            }
        }

        // 3. Draw Thumb
        canvas.drawCircle(thumbX, centerY, currentThumbRadius, thumbPaint)
    }

    private fun drawStraightStyle(canvas: Canvas, w: Float, centerY: Float, currentThumbRadius: Float) {
        val startX = thumbRadiusPx
        val endX = w - thumbRadiusPx
        val trackWidth = (endX - startX).coerceAtLeast(1f)
        val thumbX = (startX + progress * trackWidth).coerceIn(startX, endX)

        inactivePaint.strokeWidth = strokeWidthPx
        activePaint.strokeWidth = strokeWidthPx

        if (thumbX < endX - 1f) {
            canvas.drawLine(thumbX, centerY, endX, centerY, inactivePaint)
        }
        if (thumbX > startX + 1f) {
            canvas.drawLine(startX, centerY, thumbX, centerY, activePaint)
        }

        canvas.drawCircle(thumbX, centerY, currentThumbRadius, thumbPaint)
    }

    private fun drawAndroid12Style(canvas: Canvas, w: Float, centerY: Float) {
        val barHeight = 16f * density
        val radius = barHeight / 2f
        val top = centerY - radius
        val bottom = centerY + radius

        // Inactive pill
        pillInactivePaint.color = inactiveColor
        pillRect.set(0f, top, w, bottom)
        canvas.drawRoundRect(pillRect, radius, radius, pillInactivePaint)

        // Active pill
        val activeWidth = (w * progress).coerceIn(0f, w)
        if (activeWidth > 0f) {
            pillActivePaint.color = activeColor
            pillRect.set(0f, top, activeWidth, bottom)
            canvas.drawRoundRect(pillRect, radius, radius, pillActivePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                com.example.service.MediaSessionManagerHelper.isSeekingMediaProgress.value = true
                isDragging = true
                dragStartX = event.x
                dragStretchRatio = 0f
                animatedThumbRadiusScale = 1.35f
                onSeekStateChanged?.invoke(true)
                updateProgressFromTouch(event.x)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                if (isDragging) {
                    val trackW = (width - 2 * thumbRadiusPx).coerceAtLeast(1f)
                    val stretchDragSpan = (trackW * 0.45f).coerceAtLeast(120f * density)
                    val dragDeltaX = (event.x - dragStartX).coerceAtLeast(0f)
                    dragStretchRatio = (dragDeltaX / stretchDragSpan).coerceIn(0f, 1f)

                    updateProgressFromTouch(event.x)
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    isDragging = false
                    com.example.service.MediaSessionManagerHelper.isSeekingMediaProgress.value = false
                    dragStretchRatio = 0f
                    animatedThumbRadiusScale = 1f
                    onSeekStateChanged?.invoke(false)
                    parent?.requestDisallowInterceptTouchEvent(false)
                    invalidate()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updateProgressFromTouch(touchX: Float) {
        val startX = thumbRadiusPx
        val endX = width - thumbRadiusPx
        val trackWidth = (endX - startX).coerceAtLeast(1f)
        val newProgress = ((touchX - startX) / trackWidth).coerceIn(0f, 1f)
        progress = newProgress
        onSeekListener?.invoke(newProgress)
        invalidate()
    }
}
