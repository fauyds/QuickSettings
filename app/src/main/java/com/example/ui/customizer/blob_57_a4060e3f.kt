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
import androidx.compose.material3.Card
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
import com.example.ui.theme.DarkUiSurface
import com.example.ui.theme.DarkUiSurfaceVariant
import com.example.ui.theme.ExpressiveCardShapes
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.MonetOnPrimary
import com.example.ui.theme.MonetPrimary
import com.example.ui.theme.MonetTertiary
import java.util.Locale

/**
 * In-Composition Bottom Sheet for Screen Recording Configuration.
 * Implemented without android.app.Dialog to be 100% safe and compatible with
 * both Activity UI and Service Overlay Windows (avoiding WindowManager.BadTokenException).
 */
@Composable
fun ScreenRecordBottomSheet(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current

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

            // Bottom Sheet Surface Container
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { /* Consume clicks inside the sheet */ }
                    ),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = if (isDark) DarkUiSurface else Color(0xFFF1F5F9),
                tonalElevation = 8.dp,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .heightIn(max = 620.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Drag handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 6.dp)
                            .size(width = 38.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.2f))
                    )

                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    .background(if (isRecording) Color(0xFFEF4444).copy(alpha = 0.2f) else MonetPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = if (isRecording) Color(0xFFEF4444) else MonetPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Screen Recording",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Configure capture settings & quality",
                                    fontSize = 12.sp,
                                    color = if (isDark) Color.LightGray else Color(0xFF64748B)
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
                                tint = if (isDark) Color.LightGray else Color(0xFF475569),
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
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.15f),
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
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Recording in Progress",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFEF4444)
                                        )
                                        Text(
                                            text = "Elapsed Time: $durationFormatted",
                                            fontSize = 12.sp,
                                            color = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF1E293B)
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        ScreenRecordService.stopRecording(context)
                                        onDismissRequest()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("sheet_stop_recording_button")
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Stop", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 1. Video Resolution Section
                    Card(
                        shape = ExpressiveCardShapes.TopCard,
                        colors = CardDefaults.cardColors(containerColor = if (isDark) DarkUiSurfaceVariant.copy(alpha = 0.6f) else Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.HighQuality, contentDescription = null, tint = MonetPrimary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "VIDEO RESOLUTION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MonetPrimary
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ScreenRecordResolution.entries.forEach { res ->
                                    val isSelected = currentResolution == res
                                    OptionItemRow(
                                        title = res.title,
                                        subtitle = res.subtitle,
                                        isSelected = isSelected,
                                        isDark = isDark,
                                        onClick = { PanelPreferences.setScreenRecordResolution(context, res) },
                                        testTag = "resolution_option_${res.id}"
                                    )
                                }
                            }
                        }
                    }

                    // 2. Audio Source Section
                    Card(
                        shape = ExpressiveCardShapes.CenterCard,
                        colors = CardDefaults.cardColors(containerColor = if (isDark) DarkUiSurfaceVariant.copy(alpha = 0.6f) else Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MonetTertiary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "AUDIO SOURCE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MonetTertiary
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ScreenRecordAudioSource.entries.forEach { audio ->
                                    val isSelected = currentAudioSource == audio
                                    val icon = when (audio) {
                                        ScreenRecordAudioSource.MUTE -> Icons.Default.MicOff
                                        ScreenRecordAudioSource.INTERNAL -> Icons.Default.VolumeUp
                                        ScreenRecordAudioSource.MIC -> Icons.Default.Mic
                                        ScreenRecordAudioSource.INTERNAL_AND_MIC -> Icons.Default.GraphicEq
                                    }
                                    OptionItemRow(
                                        title = audio.title,
                                        subtitle = audio.subtitle,
                                        isSelected = isSelected,
                                        icon = icon,
                                        isDark = isDark,
                                        onClick = { PanelPreferences.setScreenRecordAudioSource(context, audio) },
                                        testTag = "audio_option_${audio.id}"
                                    )
                                }
                            }
                        }
                    }

                    // 3. Frame Rate & Show Touches Section
                    Card(
                        shape = ExpressiveCardShapes.BottomCard,
                        colors = CardDefaults.cardColors(containerColor = if (isDark) DarkUiSurfaceVariant.copy(alpha = 0.6f) else Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Frame Rate
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = MonetPrimary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "FRAME RATE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MonetPrimary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ScreenRecordFps.entries.forEach { fpsItem ->
                                    val isSelected = currentFps == fpsItem
                                    Surface(
                                        onClick = { PanelPreferences.setScreenRecordFps(context, fpsItem) },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) MonetPrimary else if (isDark) DarkUiSurface else Color(0xFFF1F5F9),
                                        border = null MonetPrimary else if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)
                                        ),
                                        modifier = Modifier.weight(1f).testTag("fps_option_${fpsItem.fps}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "${fpsItem.fps} FPS",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isSelected) MonetOnPrimary else if (isDark) Color.White else Color(0xFF1E293B)
                                            )
                                        }
                                    }
                                }
                            }

                            // Show Touches Switch
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark) DarkUiSurface.copy(alpha = 0.6f) else Color(0xFFF1F5F9))
                                    .padding(12.dp),
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
                                            .background(if (showTouches) MonetPrimary.copy(alpha = 0.2f) else if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.05f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TouchApp,
                                            contentDescription = null,
                                            tint = if (showTouches) MonetPrimary else if (isDark) Color.Gray else Color(0xFF64748B),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "Show Touches",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isDark) Color.White else Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = "Show visual touch indicator on screen",
                                            fontSize = 11.sp,
                                            color = if (isDark) Color.Gray else Color(0xFF64748B)
                                        )
                                    }
                                }

                                Switch(
                                    checked = showTouches,
                                    onCheckedChange = { PanelPreferences.setScreenRecordShowTouches(context, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MonetOnPrimary,
                                        checkedTrackColor = MonetPrimary,
                                        uncheckedThumbColor = Color.LightGray,
                                        uncheckedTrackColor = if (isDark) DarkUiSurfaceVariant else Color(0xFFCBD5E1)
                                    ),
                                    modifier = Modifier.testTag("show_touches_switch")
                                )
                            }
                        }
                    }

                    // Bottom Action Controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onDismissRequest,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = null,
                            modifier = Modifier.weight(1f).height(48.dp).testTag("sheet_cancel_button")
                        ) {
                            Text("Close", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                onDismissRequest()
                                if (isRecording) {
                                    ScreenRecordService.stopRecording(context)
                                } else {
                                    ScreenRecordPromptActivity.start(context)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRecording) Color(0xFFEF4444) else MonetPrimary
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1.5f).height(48.dp).testTag("sheet_primary_record_action_button")
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Videocam,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isRecording) "Stop Recording" else "Start Recording",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
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
    isDark: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MonetPrimary.copy(alpha = 0.16f) else if (isDark) DarkUiSurface.copy(alpha = 0.4f) else Color(0xFFF8FAFC),
        border = null,
        modifier = Modifier.fillMaxWidth().testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
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
                        tint = if (isSelected) MonetPrimary else if (isDark) Color.Gray else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MonetPrimary else if (isDark) Color.White else Color(0xFF1E293B)
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = if (isDark) Color.Gray else Color(0xFF64748B)
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MonetPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MonetOnPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
