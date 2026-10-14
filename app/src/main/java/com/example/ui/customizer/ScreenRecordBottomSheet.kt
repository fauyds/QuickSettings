package com.example.ui.customizer

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.PanelPreferences
import com.example.data.ScreenRecordAudioSource
import com.example.data.ScreenRecordFps
import com.example.data.ScreenRecordResolution
import com.example.service.ScreenRecordPromptActivity
import com.example.service.ScreenRecordService
import java.util.Locale

/**
 * Section Card Shapes for Screen Recording from QS panel ONLY.
 * Outer corners 16dp, inner 5dp.
 */
private object ScreenRecordCardShapes {
    val OuterCorner = 16.dp
    val InnerCorner = 5.dp

    val TopCard = RoundedCornerShape(
        topStart = OuterCorner,
        topEnd = OuterCorner,
        bottomStart = InnerCorner,
        bottomEnd = InnerCorner
    )
    val CenterCard = RoundedCornerShape(InnerCorner)
    val BottomCard = RoundedCornerShape(
        topStart = InnerCorner,
        topEnd = InnerCorner,
        bottomStart = OuterCorner,
        bottomEnd = OuterCorner
    )
    val SingleCard = RoundedCornerShape(OuterCorner)
}

/**
 * Option Button Shapes for Screen Recording from QS panel ONLY.
 * Selected button: round corners (CircleShape).
 * Unselected button: outer corners 8dp and inner corner 8dp.
 */
private object OptionButtonShapes {
    val SelectedShape = CircleShape
    val UnselectedShape = RoundedCornerShape(8.dp)

    fun getUnselectedShape(index: Int, count: Int): Shape = UnselectedShape
    fun getUnselectedHorizontalShape(index: Int, count: Int): Shape = UnselectedShape
}

/**
 * In-Composition Bottom Sheet for Screen Recording Configuration from QS Panel.
 * Safe and compatible with Activity UI & Service Overlay Windows.
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

    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10000f),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Scrim overlay (dimmed background)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest
                    )
            )

            // Bottom Sheet Surface Container - Monet Surface, Outlines Removed
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { /* Consume clicks inside sheet */ }
                    ),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                border = null
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .heightIn(max = 620.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp) // Distance between cards 4dp
                ) {
                    // Drag handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 6.dp)
                            .size(width = 38.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
                    )

                    // Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isRecording) MaterialTheme.colorScheme.errorContainer
                                        else MaterialTheme.colorScheme.primaryContainer
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = if (isRecording) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Screen Recording",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Configure capture settings & quality",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.size(36.dp).testTag("close_screen_record_sheet_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Live Recording Status Banner (if active)
                    if (isRecording) {
                        val minutes = recordingDuration / 60
                        val seconds = recordingDuration % 60
                        val durationFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

                        Surface(
                            shape = ScreenRecordCardShapes.SingleCard,
                            color = MaterialTheme.colorScheme.errorContainer,
                            border = null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FiberManualRecord,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Recording in Progress",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            text = "Elapsed Time: $durationFormatted",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        ScreenRecordService.stopRecording(context)
                                        onDismissRequest()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ),
                                    shape = CircleShape,
                                    modifier = Modifier.testTag("sheet_stop_recording_button")
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Stop", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 1. Video Resolution Section Card (Top Card: 16dp outer, 5dp inner)
                    SettingsCard(
                        shape = ScreenRecordCardShapes.TopCard,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.HighQuality, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "VIDEO RESOLUTION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                val resolutions = ScreenRecordResolution.entries
                                resolutions.forEachIndexed { index, res ->
                                    val isSelected = currentResolution == res
                                    val buttonShape = if (isSelected) {
                                        OptionButtonShapes.SelectedShape
                                    } else {
                                        OptionButtonShapes.getUnselectedShape(index, resolutions.size)
                                    }

                                    OptionItemRow(
                                        title = res.title,
                                        subtitle = res.subtitle,
                                        isSelected = isSelected,
                                        shape = buttonShape,
                                        onClick = { PanelPreferences.setScreenRecordResolution(context, res) },
                                        testTag = "resolution_option_${res.id}"
                                    )
                                }
                            }
                        }
                    }

                    // 2. Audio Source Section Card (Center Card: 5dp inner corners)
                    SettingsCard(
                        shape = ScreenRecordCardShapes.CenterCard,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "AUDIO SOURCE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                val audioSources = ScreenRecordAudioSource.entries
                                audioSources.forEachIndexed { index, audio ->
                                    val isSelected = currentAudioSource == audio
                                    val icon = when (audio) {
                                        ScreenRecordAudioSource.MUTE -> Icons.Default.MicOff
                                        ScreenRecordAudioSource.INTERNAL -> Icons.Default.VolumeUp
                                        ScreenRecordAudioSource.MIC -> Icons.Default.Mic
                                        ScreenRecordAudioSource.INTERNAL_AND_MIC -> Icons.Default.GraphicEq
                                    }
                                    val buttonShape = if (isSelected) {
                                        OptionButtonShapes.SelectedShape
                                    } else {
                                        OptionButtonShapes.getUnselectedShape(index, audioSources.size)
                                    }

                                    OptionItemRow(
                                        title = audio.title,
                                        subtitle = audio.subtitle,
                                        isSelected = isSelected,
                                        icon = icon,
                                        shape = buttonShape,
                                        onClick = { PanelPreferences.setScreenRecordAudioSource(context, audio) },
                                        testTag = "audio_option_${audio.id}"
                                    )
                                }
                            }
                        }
                    }

                    // 3. Frame Rate & Show Touches Section Card (Bottom Card: 5dp inner, 16dp outer)
                    SettingsCard(
                        shape = ScreenRecordCardShapes.BottomCard,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Frame Rate
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "FRAME RATE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val fpsList = ScreenRecordFps.entries
                                fpsList.forEachIndexed { index, fpsItem ->
                                    val isSelected = currentFps == fpsItem
                                    val buttonShape = if (isSelected) {
                                        OptionButtonShapes.SelectedShape
                                    } else {
                                        OptionButtonShapes.getUnselectedHorizontalShape(index, fpsList.size)
                                    }

                                    Surface(
                                        onClick = { PanelPreferences.setScreenRecordFps(context, fpsItem) },
                                        shape = buttonShape,
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                                        border = null,
                                        modifier = Modifier.weight(1f).testTag("fps_option_${fpsItem.fps}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "${fpsItem.fps} FPS",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            // Show Touches Switch
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(OptionButtonShapes.getUnselectedShape(0, 1))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (showTouches) MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.surfaceContainerHighest
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TouchApp,
                                            contentDescription = null,
                                            tint = if (showTouches) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "Show Touches",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Show visual touch indicator on screen",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Switch(
                                    checked = showTouches,
                                    onCheckedChange = { PanelPreferences.setScreenRecordShowTouches(context, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                    ),
                                    modifier = Modifier.testTag("show_touches_switch")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Bottom Action Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            onClick = onDismissRequest,
                            shape = OptionButtonShapes.getUnselectedHorizontalShape(0, 2),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = null,
                            modifier = Modifier.weight(1f).height(42.dp).testTag("sheet_cancel_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Close",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
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
                            shape = OptionButtonShapes.SelectedShape, // Selected primary action button = round corners!
                            color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            border = null,
                            modifier = Modifier.weight(1.5f).height(42.dp).testTag("sheet_primary_record_action_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = if (isRecording) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isRecording) "Stop Recording" else "Start Recording",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
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
private fun OptionItemRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    icon: ImageVector? = null,
    shape: Shape,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = shape,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
        border = null,
        modifier = Modifier.fillMaxWidth().testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
