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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.QsSliderTarget
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
    inactiveStrokeWidth: Dp? = null,
    cornerRadius: Dp = 19.dp,
    amplitude: Dp = 3.5.dp,
    wavelength: Dp = 26.dp,
    thumbRadius: Dp = 6.dp,
    wavePeriodMillis: Int = 1300
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
                animation = tween(durationMillis = wavePeriodMillis.coerceAtLeast(100), easing = LinearEasing),
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

            val inactiveStrokePx = (inactiveStrokeWidth ?: strokeWidth).toPx()
            val capStyle = if (cornerRadius <= 1.dp) StrokeCap.Square else StrokeCap.Round

            // 1. Draw Inactive Track (from thumb to end)
            if (currentThumbX < endX - 1f) {
                drawLine(
                    color = inactiveColor,
                    start = Offset(currentThumbX, centerY),
                    end = Offset(endX, centerY),
                    strokeWidth = inactiveStrokePx,
                    cap = capStyle
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
                        style = Stroke(width = strokePx, cap = capStyle)
                    )
                } else {
                    // Straight active line
                    drawLine(
                        color = activeColor,
                        start = Offset(startX, centerY),
                        end = Offset(currentThumbX, centerY),
                        strokeWidth = strokePx,
                        cap = capStyle
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
 * Android 12 (S) SystemUI Thick Pill Style Slider Bar.
 * Features a thick pill container track (~38.dp height) with customizable corner radius,
 * filled active progress track, customizable inactive track thickness, and interactive touch drag gesture handling.
 * At 0% progress, the active progress forms a complete circle (width = height) that never disappears.
 */
@Composable
fun Android12Slider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    height: Dp = 38.dp,
    inactiveTrackThickness: Dp = height,
    cornerRadius: Dp = height / 2
) {
    val rangeSpan = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.0001f)
    val fraction = ((value - valueRange.start) / rangeSpan).coerceIn(0f, 1f)

    val effectiveInactiveHeight = inactiveTrackThickness.coerceIn(2.dp, height)
    val effectiveCornerRadius = cornerRadius.coerceAtLeast(0.dp)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .then(
                if (enabled) {
                    Modifier.pointerInput(valueRange, height) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            down.consume()
                            val trackW = size.width.toFloat().coerceAtLeast(1f)
                            val heightPx = size.height.toFloat()
                            val travelRange = (trackW - heightPx).coerceAtLeast(1f)
                            val downFraction = ((down.position.x - heightPx / 2) / travelRange).coerceIn(0f, 1f)
                            val downValue = valueRange.start + downFraction * rangeSpan
                            onValueChange(downValue)

                            var activePointerId = down.id
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
                                val currentFraction = ((currentTouchX - heightPx / 2) / travelRange).coerceIn(0f, 1f)
                                val newValue = valueRange.start + currentFraction * rangeSpan
                                onValueChange(newValue)
                            }
                        }
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        val totalWidth = maxWidth

        // Inactive Track Background with customizable thickness and corner radius
        val inactiveCorner = minOf(effectiveCornerRadius, effectiveInactiveHeight / 2)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(effectiveInactiveHeight)
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(inactiveCorner))
                .background(inactiveColor)
        )

        // Active Progress Fill:
        // At 0% progress, width is equal to `height`, forming a circular active progress indicator that never disappears!
        // As fraction grows from 0f to 1f, the width expands towards totalWidth.
        val activeWidth = height + (totalWidth - height).coerceAtLeast(0.dp) * fraction
        val activeCorner = if (fraction <= 0.001f) height / 2 else minOf(effectiveCornerRadius, height / 2)
        Box(
            modifier = Modifier
                .width(activeWidth)
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(activeCorner))
                .background(activeColor)
        )
    }
}

/**
 * Universal Quick Settings Progress Slider component supporting 4 distinct styles:
 * 1. ProgressBarStyle.DEFAULT: Classic Jetpack Compose Material 3 Slider with standard track.
 * 2. ProgressBarStyle.STRAIGHT: Clean minimal straight progress bar track with smooth thumb.
 * 3. ProgressBarStyle.WAVY: Android 13/14 sinusoidal squiggly progress bar with animated phase.
 * 4. ProgressBarStyle.ANDROID_12: Android 12 thick pill style slider bar with 0% circle indicator.
 * Supports separate target configuration (All, Brightness, Volume, Media Player).
 */
@Composable
fun QsProgressSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    style: ProgressBarStyle? = null,
    sliderTarget: QsSliderTarget = QsSliderTarget.BRIGHTNESS,
    isPlaying: Boolean = false,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    thumbColor: Color = activeColor,
    strokeWidth: Dp? = null,
    amplitude: Dp? = null,
    wavelength: Dp? = null,
    thumbRadius: Dp? = null,
    wavePeriodMillis: Int? = null
) {
    val sliderSyncAll by com.example.data.PanelPreferences.sliderSyncAll.collectAsState()
    val globalStyle by com.example.data.PanelPreferences.progressBarStyle.collectAsState()
    val brightnessStyle by com.example.data.PanelPreferences.brightnessSliderStyle.collectAsState()
    val volumeStyle by com.example.data.PanelPreferences.volumeSliderStyle.collectAsState()
    val mediaStyle by com.example.data.PanelPreferences.mediaSliderStyle.collectAsState()

    val resolvedStyle = style ?: if (sliderSyncAll) {
        globalStyle
    } else {
        when (sliderTarget) {
            QsSliderTarget.BRIGHTNESS -> brightnessStyle
            QsSliderTarget.VOLUME -> volumeStyle
            QsSliderTarget.MEDIA -> mediaStyle
        }
    }

    // Common and per-style preferences
    val wavyThicknessPref by com.example.data.PanelPreferences.wavyThickness.collectAsState()
    val wavyWobblinessPref by com.example.data.PanelPreferences.wavyWobbliness.collectAsState()
    val wavyWavelengthPref by com.example.data.PanelPreferences.wavyWavelength.collectAsState()
    val wavyThumbRadiusPref by com.example.data.PanelPreferences.wavyThumbRadius.collectAsState()
    val wavySpeedPref by com.example.data.PanelPreferences.wavySpeed.collectAsState()
    val wavyInactiveTrackThicknessPref by com.example.data.PanelPreferences.wavyInactiveTrackThickness.collectAsState()
    val wavyCornerRadiusPref by com.example.data.PanelPreferences.wavyCornerRadius.collectAsState()

    val a12InactiveTrackThicknessPref by com.example.data.PanelPreferences.a12InactiveTrackThickness.collectAsState()
    val a12CornerRadiusPref by com.example.data.PanelPreferences.a12CornerRadius.collectAsState()

    val straightThicknessPref by com.example.data.PanelPreferences.straightThickness.collectAsState()
    val straightInactiveTrackThicknessPref by com.example.data.PanelPreferences.straightInactiveTrackThickness.collectAsState()
    val straightCornerRadiusPref by com.example.data.PanelPreferences.straightCornerRadius.collectAsState()
    val straightThumbRadiusPref by com.example.data.PanelPreferences.straightThumbRadius.collectAsState()

    val fallbackInactiveTrackThickness by com.example.data.PanelPreferences.sliderInactiveTrackThickness.collectAsState()
    val fallbackCornerRadius by com.example.data.PanelPreferences.sliderCornerRadius.collectAsState()

    when (resolvedStyle) {
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
            val effectiveStraightWidth = strokeWidth ?: straightThicknessPref.dp
            val effectiveStraightInactiveWidth = (straightInactiveTrackThicknessPref ?: fallbackInactiveTrackThickness ?: 4f).dp
            val effectiveStraightCorner = (straightCornerRadiusPref ?: fallbackCornerRadius ?: 4f).dp
            val effectiveStraightThumb = (thumbRadius ?: straightThumbRadiusPref.dp)
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
                thumbColor = thumbColor,
                strokeWidth = effectiveStraightWidth,
                inactiveStrokeWidth = effectiveStraightInactiveWidth,
                cornerRadius = effectiveStraightCorner,
                amplitude = 0.dp,
                wavelength = 26.dp,
                thumbRadius = effectiveStraightThumb,
                wavePeriodMillis = 1300
            )
        }
        ProgressBarStyle.WAVY -> {
            val effectiveStrokeWidth = strokeWidth ?: wavyThicknessPref.dp
            val effectiveInactiveStrokeWidth = (wavyInactiveTrackThicknessPref ?: fallbackInactiveTrackThickness ?: 4f).dp
            val effectiveCornerRadius = (wavyCornerRadiusPref ?: fallbackCornerRadius ?: 4f).dp
            val effectiveAmplitude = amplitude ?: wavyWobblinessPref.dp
            val effectiveWavelength = wavelength ?: wavyWavelengthPref.dp
            val effectiveThumbRadius = thumbRadius ?: wavyThumbRadiusPref.dp
            val effectiveWavePeriodMillis = wavePeriodMillis ?: wavySpeedPref
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
                thumbColor = thumbColor,
                strokeWidth = effectiveStrokeWidth,
                inactiveStrokeWidth = effectiveInactiveStrokeWidth,
                cornerRadius = effectiveCornerRadius,
                amplitude = effectiveAmplitude,
                wavelength = effectiveWavelength,
                thumbRadius = effectiveThumbRadius,
                wavePeriodMillis = effectiveWavePeriodMillis
            )
        }
        ProgressBarStyle.ANDROID_12 -> {
            val effectiveA12InactiveHeight = (a12InactiveTrackThicknessPref ?: fallbackInactiveTrackThickness ?: 38f).dp
            val effectiveA12Corner = (a12CornerRadiusPref ?: fallbackCornerRadius ?: 19f).dp
            Android12Slider(
                value = value,
                onValueChange = onValueChange,
                modifier = modifier,
                enabled = enabled,
                valueRange = valueRange,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                height = 38.dp,
                inactiveTrackThickness = effectiveA12InactiveHeight,
                cornerRadius = effectiveA12Corner
            )
        }
    }
}


