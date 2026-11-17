package com.example.ui.customizer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.PanelPreferences
import com.example.data.ScreenRecordAudioSource
import com.example.data.ScreenRecordFps
import com.example.data.ScreenRecordResolution
import com.example.service.ScreenRecordPromptActivity
import com.example.service.ScreenRecordService
import com.example.ui.theme.ExpressiveCardShapes
import com.example.ui.theme.ExpressiveTheme
import com.example.ui.theme.SegmentedButtonShapes
import java.util.Locale

private val PillShape = RoundedCornerShape(percent = 50)
private val NoFontPaddingTextStyle = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))

/**
 * Redesigned Material 3 Expressive Bottom Sheet for Screen Recording configuration from QS Panel.
 * Uses compact horizontal segmented controls inside ExpressiveCardShapes cards with a docked action bar
 * so all capture options and the Start Recording action remain immediately reachable without scrolling.
 */
@Composable
fun ScreenRecordBottomSheet(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    val currentResolution by PanelPreferences.screenRecordResolution.collectAsState()
    val currentAudioSource by PanelPreferences.screenRecordAudioSource.collectAsState()
    val currentFps by PanelPreferences.screenRecordFps.collectAsState()
    val showTouches by PanelPreferences.screenRecordShowTouches.collectAsState()

    val isRecording by ScreenRecordService.isRecording.collectAsState()
    val recordingDuration by ScreenRecordService.recordingDurationSeconds.collectAsState()

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val backDispatcher = androidx.activity.compose.LocalOnBackPressedDispatcherOwner.current
    if (backDispatcher != null) {
        BackHandler {
            isVisible = false
            onDismissRequest()
        }
    }

    val colors = ExpressiveTheme.colors

    val shortResolutionLabel = when (currentResolution) {
        ScreenRecordResolution.NATIVE -> "Native"
        ScreenRecordResolution.RES_1080P -> "1080p"
        ScreenRecordResolution.RES_720P -> "720p"
        ScreenRecordResolution.RES_480P -> "480p"
    }
    val shortAudioLabel = when (currentAudioSource) {
        ScreenRecordAudioSource.MUTE -> "Muted"
        ScreenRecordAudioSource.INTERNAL -> "Internal"
        ScreenRecordAudioSource.MIC -> "Mic"
        ScreenRecordAudioSource.INTERNAL_AND_MIC -> "System + Mic"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10000f),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.56f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest
                    )
            )
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                animationSpec = spring(dampingRatio = 0.85f, stiffness = 420f),
                initialOffsetY = { it / 2 }
            ) + fadeIn(tween(200)),
            exit = fadeOut(tween(140))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { /* Consume taps inside sheet */ }
                    )
                    .testTag("screen_record_bottom_sheet"),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = colors.appBackground,
                shadowElevation = 20.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(top = 10.dp, bottom = 14.dp)
                ) {
                    // Drag handle pill
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 10.dp)
                            .size(width = 36.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f))
                    )

                    // Top Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isRecording) MaterialTheme.colorScheme.errorContainer
                                        else colors.buttonPrimary
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isRecording) Icons.Default.FiberManualRecord else Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(
                                    text = "Screen Recorder",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    style = NoFontPaddingTextStyle,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$shortResolutionLabel • ${currentFps.fps} FPS • $shortAudioLabel",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    style = NoFontPaddingTextStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // 48dp accessible touch target with crisp 36dp circular surface
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true)
                                ) { onDismissRequest() }
                                .testTag("close_screen_record_sheet_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(colors.cardSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Scrollable Middle Content (only scrolls on ultra-compact screens; otherwise fits cleanly)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(max = 520.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(ExpressiveCardShapes.CardSpacing)
                    ) {
                        // Active Recording Banner (animated)
                        AnimatedVisibility(
                            visible = isRecording,
                            enter = expandVertically(spring(dampingRatio = 0.85f, stiffness = 380f)) + fadeIn(tween(180)),
                            exit = shrinkVertically(spring(dampingRatio = 0.85f, stiffness = 380f)) + fadeOut(tween(140))
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "rec_pulse")
                            val pulseAlpha by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 0.3f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(650, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "rec_dot_alpha"
                            )
                            val minutes = recordingDuration / 60
                            val seconds = recordingDuration % 60
                            val durationFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

                            Surface(
                                shape = ExpressiveCardShapes.SingleCard,
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .alpha(pulseAlpha)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.error)
                                        )
                                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                            Text(
                                                text = "Recording Active",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                style = NoFontPaddingTextStyle,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                            Text(
                                                text = durationFormatted,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                style = NoFontPaddingTextStyle.copy(fontFeatureSettings = "tnum"),
                                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                                            )
                                        }
                                    }

                                    Surface(
                                        onClick = {
                                            ScreenRecordService.stopRecording(context)
                                            onDismissRequest()
                                        },
                                        shape = PillShape,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier
                                            .heightIn(min = 40.dp)
                                            .testTag("sheet_stop_recording_button")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Stop,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onError,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Stop",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                style = NoFontPaddingTextStyle,
                                                color = MaterialTheme.colorScheme.onError
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // CARD 1 (Top Card: 16dp top corners, 4dp bottom corners): Video Resolution & Frame Rate
                        Surface(
                            shape = ExpressiveCardShapes.TopCard,
                            color = colors.cardSurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.HighQuality,
                                            contentDescription = null,
                                            tint = colors.buttonPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "VIDEO QUALITY",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.9.sp,
                                            style = NoFontPaddingTextStyle,
                                            color = colors.buttonPrimary
                                        )
                                    }
                                    Text(
                                        text = currentResolution.subtitle,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        style = NoFontPaddingTextStyle,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // 4-segment Expressive Resolution Selector (4dp gap, 50% selected, 4dp/50% unselected)
                                val resolutions = ScreenRecordResolution.entries
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    resolutions.forEachIndexed { index, res ->
                                        val isSelected = currentResolution == res
                                        val segShape = if (isSelected) {
                                            PillShape
                                        } else {
                                            SegmentedButtonShapes.getShape(
                                                index = index,
                                                count = resolutions.size,
                                                isSelected = false
                                            )
                                        }
                                        val primaryLabel = when (res) {
                                            ScreenRecordResolution.NATIVE -> "Native"
                                            ScreenRecordResolution.RES_1080P -> "1080p"
                                            ScreenRecordResolution.RES_720P -> "720p"
                                            ScreenRecordResolution.RES_480P -> "480p"
                                        }
                                        val badgeLabel = when (res) {
                                            ScreenRecordResolution.NATIVE -> "MAX"
                                            ScreenRecordResolution.RES_1080P -> "FHD"
                                            ScreenRecordResolution.RES_720P -> "HD"
                                            ScreenRecordResolution.RES_480P -> "SD"
                                        }

                                        ExpressiveSegmentButton(
                                            selected = isSelected,
                                            shape = segShape,
                                            onClick = { PanelPreferences.setScreenRecordResolution(context, res) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(52.dp)
                                                .testTag("resolution_option_${res.id}")
                                        ) { contentColor ->
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically)
                                            ) {
                                                Text(
                                                    text = primaryLabel,
                                                    fontSize = 12.5.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                    style = NoFontPaddingTextStyle,
                                                    color = contentColor,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = badgeLabel,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    style = NoFontPaddingTextStyle,
                                                    color = contentColor.copy(alpha = if (isSelected) 0.88f else 0.65f),
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }

                                // Frame Rate inline segmented row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Speed,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Frame Rate",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            style = NoFontPaddingTextStyle,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    val fpsList = ScreenRecordFps.entries
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        fpsList.forEachIndexed { index, fpsItem ->
                                            val isSelected = currentFps == fpsItem
                                            val segShape = if (isSelected) {
                                                PillShape
                                            } else {
                                                SegmentedButtonShapes.getShape(
                                                    index = index,
                                                    count = fpsList.size,
                                                    isSelected = false
                                                )
                                            }

                                            ExpressiveSegmentButton(
                                                selected = isSelected,
                                                shape = segShape,
                                                onClick = { PanelPreferences.setScreenRecordFps(context, fpsItem) },
                                                modifier = Modifier
                                                    .height(32.dp)
                                                    .widthIn(min = 72.dp)
                                                    .testTag("fps_option_${fpsItem.fps}")
                                            ) { contentColor ->
                                                Text(
                                                    text = "${fpsItem.fps} FPS",
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                    style = NoFontPaddingTextStyle,
                                                    color = contentColor,
                                                    modifier = Modifier.padding(horizontal = 10.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // CARD 2 (Center Card: 4dp corners): Audio Source
                        Surface(
                            shape = ExpressiveCardShapes.CenterCard,
                            color = colors.cardSurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.GraphicEq,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "AUDIO SOURCE",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.9.sp,
                                            style = NoFontPaddingTextStyle,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                    Text(
                                        text = currentAudioSource.subtitle,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        style = NoFontPaddingTextStyle,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                val audioSources = ScreenRecordAudioSource.entries
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    audioSources.forEachIndexed { index, audio ->
                                        val isSelected = currentAudioSource == audio
                                        val icon: ImageVector = when (audio) {
                                            ScreenRecordAudioSource.MUTE -> Icons.Default.MicOff
                                            ScreenRecordAudioSource.INTERNAL -> Icons.Default.VolumeUp
                                            ScreenRecordAudioSource.MIC -> Icons.Default.Mic
                                            ScreenRecordAudioSource.INTERNAL_AND_MIC -> Icons.Default.GraphicEq
                                        }
                                        val compactLabel = when (audio) {
                                            ScreenRecordAudioSource.MUTE -> "Mute"
                                            ScreenRecordAudioSource.INTERNAL -> "Internal"
                                            ScreenRecordAudioSource.MIC -> "Mic"
                                            ScreenRecordAudioSource.INTERNAL_AND_MIC -> "Both"
                                        }
                                        val segShape = if (isSelected) {
                                            PillShape
                                        } else {
                                            SegmentedButtonShapes.getShape(
                                                index = index,
                                                count = audioSources.size,
                                                isSelected = false
                                            )
                                        }

                                        ExpressiveSegmentButton(
                                            selected = isSelected,
                                            shape = segShape,
                                            onClick = { PanelPreferences.setScreenRecordAudioSource(context, audio) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(58.dp)
                                                .testTag("audio_option_${audio.id}")
                                        ) { contentColor ->
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = audio.title,
                                                    tint = contentColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = compactLabel,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    style = NoFontPaddingTextStyle,
                                                    color = contentColor,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // CARD 3 (Bottom Card: 4dp top corners, 16dp bottom corners): Show Touches
                        Surface(
                            shape = ExpressiveCardShapes.BottomCard,
                            color = colors.cardSurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 60.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true)
                                    ) {
                                        PanelPreferences.setScreenRecordShowTouches(context, !showTouches)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (showTouches) colors.buttonPrimary
                                                else colors.nestedElementSurface
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TouchApp,
                                            contentDescription = null,
                                            tint = if (showTouches) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                        Text(
                                            text = "Show Touches",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            style = NoFontPaddingTextStyle,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Highlight taps & swipes while recording",
                                            fontSize = 11.5.sp,
                                            style = NoFontPaddingTextStyle,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Switch(
                                    checked = showTouches,
                                    onCheckedChange = { PanelPreferences.setScreenRecordShowTouches(context, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                        checkedTrackColor = colors.buttonPrimary,
                                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                        uncheckedTrackColor = colors.nestedElementSurface
                                    ),
                                    modifier = Modifier.testTag("show_touches_switch")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pinned Standalone Pill Action Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = onDismissRequest,
                            shape = PillShape,
                            color = colors.buttonTertiarySubtle,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("sheet_cancel_button")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "Close",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    style = NoFontPaddingTextStyle,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }

                        Surface(
                            onClick = {
                                onDismissRequest()
                                if (isRecording) {
                                    ScreenRecordService.stopRecording(context)
                                } else {
                                    ScreenRecordPromptActivity.start(context)
                                }
                            },
                            shape = PillShape,
                            color = if (isRecording) MaterialTheme.colorScheme.error else colors.buttonPrimary,
                            modifier = Modifier
                                .weight(1.7f)
                                .height(50.dp)
                                .testTag("sheet_primary_record_action_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                                    contentDescription = null,
                                    tint = if (isRecording) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isRecording) "Stop Recording" else "Start Recording",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    style = NoFontPaddingTextStyle,
                                    color = if (isRecording) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpressiveSegmentButton(
    selected: Boolean,
    shape: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (contentColor: Color) -> Unit
) {
    val colors = ExpressiveTheme.colors
    val targetContainer = if (selected) colors.selectorSelected else colors.nestedElementSurface
    val containerColor by animateColorAsState(
        targetValue = targetContainer,
        animationSpec = tween(180),
        label = "segment_bg"
    )
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content(contentColor)
    }
}
