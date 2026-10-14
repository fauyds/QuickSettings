package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import com.example.data.ProgressBarStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * Wavy Slider & Progress Bar inspired by Android 13/14 SystemUI Squiggly Progress.
 * When isWavy is true:
 * - Active progress track renders as a smooth, organic sinusoidal wave.
 * - When isPlaying is true, the wave gently ripples forward over time.
 * - Seamlessly transitions to flat lines at the start and thumb points.
 * - Dynamic rubber stretching: isDragging stays true throughout the entire gesture,
 *   stretching the wavelength continuously from 0:00 to currentThumbX, flattening slightly
 *   under tension without resetting.
 * - Only upon physical finger lift does it animate back with an elastic bouncy spring.
 * When isWavy is false:
 * - Renders a sleek standard Material Design progress/slider line.
 */
@Composable
fun WavySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    isWavy: Boolean = true,
    isPlaying: Boolean = false,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    thumbColor: Color = activeColor,
    showThumb: Boolean = true,
    strokeWidth: Dp = 4.dp,
    amplitude: Dp = 3.5.dp,
    wavelength: Dp = 26.dp,
    thumbRadius: Dp = 6.dp
) {
    var isDragging by remember { mutableStateOf(false) }

    // Progressive drag stretch ratio: 0f = resting tight wave, 1f = max stretched wave.
    // Scales progressively with the actual drag displacement, NOT instantaneously upon touch.
    var dragProgressRatio by remember { mutableFloatStateOf(0f) }

    val animatedStretchRatio by animateFloatAsState(
        targetValue = if (isDragging) dragProgressRatio else 0f,
        animationSpec = if (isDragging) snap() else spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "wavy_rubber_stretch"
    )

    val animatedThumbRadius by animateDpAsState(
        targetValue = if (isDragging) thumbRadius * 1.35f else thumbRadius,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "thumb_radius"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "wavy_slider_anim")
    val phase by if (isWavy && isPlaying) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1300, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "phase"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    val rangeSpan = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.0001f)
    val fraction = ((value - valueRange.start) / rangeSpan).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .then(
                if (enabled) {
                    Modifier.pointerInput(valueRange) {
                        awaitEachGesture {
                            // 1. Detect touch down: record dragStartX and current position; NO stretch on touch down
                            val down = awaitFirstDown(requireUnconsumed = false)
                            down.consume()
                            isDragging = true
                            val dragStartX = down.position.x

                            val baseThumbRPx = thumbRadius.toPx()
                            val trackW = (size.width - 2 * baseThumbRPx).coerceAtLeast(1f)
                            val downFraction = ((down.position.x - baseThumbRPx) / trackW).coerceIn(0f, 1f)
                            val downValue = valueRange.start + downFraction * rangeSpan
                            onValueChange(downValue)

                            // Initial touch down without movement: stretch ratio is strictly 0 (normal, tight wave)
                            dragProgressRatio = 0f

                            var activePointerId = down.id

                            try {
                                // 2. Continuously track progressive drag displacement
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == activePointerId }
                                        ?: event.changes.firstOrNull()
                                        ?: break

                                    if (!change.pressed) {
                                        change.consume()
                                        break
                                    }

                                    change.consume()
                                    activePointerId = change.id

                                    val currentTouchX = change.position.x
                                    val currentFraction = ((currentTouchX - baseThumbRPx) / trackW).coerceIn(0f, 1f)
                                    val newValue = valueRange.start + currentFraction * rangeSpan
                                    onValueChange(newValue)

                                    // Progressive stretch calculation:
                                    // Scale linearly with actual drag distance moved from dragStartX
                                    val stretchDragSpan = (trackW * 0.45f).coerceAtLeast(120.dp.toPx())
                                    val dragDeltaX = (currentTouchX - dragStartX).coerceAtLeast(0f)
                                    dragProgressRatio = (dragDeltaX / stretchDragSpan).coerceIn(0f, 1f)
                                }
                            } finally {
                                // 3. Finger lifted / gesture ended: trigger spring bounce back to baseline
                                isDragging = false
                            }
                        }
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val baseThumbRPx = thumbRadius.toPx()
            val thumbRPx = animatedThumbRadius.toPx()
            val strokePx = strokeWidth.toPx()
            val defaultAmpPx = amplitude.toPx()
            val defaultWaveLenPx = wavelength.toPx()
            val centerY = size.height / 2f

            val startX = baseThumbRPx
            val endX = size.width - baseThumbRPx
            val totalTrackWidth = (endX - startX).coerceAtLeast(1f)
            val currentThumbX = startX + fraction * totalTrackWidth

            // Progressive calculation:
            // animatedStretchRatio is a continuous float mapped to actual drag displacement,
            // NOT a static boolean flag. (0f = resting wave, 1f = max stretched wave)
            val stretchRatio = animatedStretchRatio

            // Interpolate wavelength linearly from defaultWaveLength up to maxStretchedWaveLength based on drag distance
            val maxStretchedWaveLen = defaultWaveLenPx * 2.6f
            val waveLenPx = (defaultWaveLenPx + (maxStretchedWaveLen - defaultWaveLenPx) * stretchRatio)
                .coerceAtLeast(defaultWaveLenPx * 0.5f)

            // Interpolate amplitude from defaultAmp down to minAmp based on drag distance (clamped to prevent flattening completely)
            val minAmpPx = maxOf(defaultAmpPx * 0.5f, 1.6.dp.toPx())
            val ampPx = (defaultAmpPx + (minAmpPx - defaultAmpPx) * stretchRatio)
                .coerceAtLeast(0.8f)

            // 1. Draw Inactive Track (from thumb to end)
            if (currentThumbX < endX - 1f) {
                drawLine(
                    color = inactiveColor,
                    start = Offset(currentThumbX, centerY),
                    end = Offset(endX, centerY),
                    strokeWidth = strokePx,
                    cap = StrokeCap.Round
                )
            }

            // 2. Draw Active Track (from start to thumb)
            if (fraction > 0.005f) {
                if (isWavy && (currentThumbX - startX) > 6f) {
                    val path = Path()
                    path.moveTo(startX, centerY)

                    val stepPx = 3f
                    var x = startX
                    while (x <= currentThumbX) {
                        val dist = x - startX
                        // Ramp wave amplitude in at start and out right at the thumb
                        val rampIn = (dist / (waveLenPx * 0.35f)).coerceIn(0f, 1f)
                        val rampOut = ((currentThumbX - x) / (waveLenPx * 0.35f)).coerceIn(0f, 1f)
                        val envelope = minOf(rampIn, rampOut)
                        val waveOffset = ampPx * envelope * sin((dist / waveLenPx) * (2 * PI.toFloat()) - phase)
                        val y = centerY + waveOffset
                        path.lineTo(x, y)
                        x += stepPx
                    }
                    path.lineTo(currentThumbX, centerY)

                    drawPath(
                        path = path,
                        color = activeColor,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                } else {
                    // Straight active line
                    drawLine(
                        color = activeColor,
                        start = Offset(startX, centerY),
                        end = Offset(currentThumbX, centerY),
                        strokeWidth = strokePx,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 3. Draw Thumb
            if (showThumb) {
                // Outer subtle glow proportional to stretch/drag
                if (isDragging || stretchRatio > 0.05f) {
                    drawCircle(
                        color = thumbColor.copy(alpha = 0.22f * stretchRatio.coerceIn(0f, 1f)),
                        radius = thumbRPx * 1.8f,
                        center = Offset(currentThumbX, centerY)
                    )
                }
                drawCircle(
                    color = thumbColor,
                    radius = thumbRPx,
                    center = Offset(currentThumbX, centerY)
                )
            }
        }
    }
}

/**
 * Non-interactive Wavy Progress Indicator (for display or compact widgets).
 */
@Composable
fun WavyProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    isWavy: Boolean = true,
    isPlaying: Boolean = false,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeWidth: Dp = 4.dp,
    amplitude: Dp = 3.5.dp,
    wavelength: Dp = 26.dp
) {
    WavySlider(
        value = progress.coerceIn(0f, 1f),
        onValueChange = {},
        modifier = modifier,
        enabled = false,
        isWavy = isWavy,
        isPlaying = isPlaying,
        activeColor = activeColor,
        inactiveColor = inactiveColor,
        showThumb = false,
        strokeWidth = strokeWidth,
        amplitude = amplitude,
        wavelength = wavelength
    )
}

/**
 * Universal Quick Settings Progress Slider component supporting 3 distinct styles:
 * 1. ProgressBarStyle.DEFAULT: The previous style - classic Jetpack Compose Material 3 Slider with standard track.
 * 2. ProgressBarStyle.STRAIGHT: Clean minimal straight progress bar track with smooth thumb.
 * 3. ProgressBarStyle.WAVY: Android 13/14 sinusoidal squiggly progress bar with animated phase.
 */
@Composable
fun QsProgressSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    style: ProgressBarStyle = ProgressBarStyle.DEFAULT,
    isPlaying: Boolean = false,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    thumbColor: Color = activeColor
) {
    when (style) {
        ProgressBarStyle.DEFAULT -> {
            Slider(
                value = value.coerceIn(valueRange.start, valueRange.endInclusive),
                onValueChange = onValueChange,
                enabled = enabled,
                valueRange = valueRange,
                colors = SliderDefaults.colors(
                    thumbColor = thumbColor,
                    activeTrackColor = activeColor,
                    inactiveTrackColor = inactiveColor
                ),
                modifier = modifier
            )
        }
        ProgressBarStyle.STRAIGHT -> {
            WavySlider(
                value = value,
                onValueChange = onValueChange,
                modifier = modifier,
                enabled = enabled,
                valueRange = valueRange,
                isWavy = false,
                isPlaying = false,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                thumbColor = thumbColor
            )
        }
        ProgressBarStyle.WAVY -> {
            WavySlider(
                value = value,
                onValueChange = onValueChange,
                modifier = modifier,
                enabled = enabled,
                valueRange = valueRange,
                isWavy = true,
                isPlaying = isPlaying,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                thumbColor = thumbColor
            )
        }
    }
}

