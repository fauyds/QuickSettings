package com.example.ui.tile

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.PanelPreferences
import com.example.data.QsSliderTarget
import com.example.service.MediaSessionManagerHelper
import com.example.service.QuickPanelAccessibilityService
import com.example.service.QuickSettingsOverlayService
import com.example.system.ComplicationDataHub
import com.example.system.ComplicationMicroWindowType
import com.example.system.ComplicationSnapshot
import com.example.system.NativeTileManager
import com.example.ui.components.QsProgressSlider
import com.example.ui.theme.ExpressiveTheme
import com.example.ui.theme.SegmentedButtonShapes
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private object MicroCardShapes {
    private val Outer = 16.dp
    private val Inner = 4.dp

    val Top = RoundedCornerShape(topStart = Outer, topEnd = Outer, bottomStart = Inner, bottomEnd = Inner)
    val Middle = RoundedCornerShape(Inner)
    val Bottom = RoundedCornerShape(topStart = Inner, topEnd = Inner, bottomStart = Outer, bottomEnd = Outer)
    val Single = RoundedCornerShape(Outer)
}

@Composable
fun ComplicationMicroWindowSheet(
    type: ComplicationMicroWindowType,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val snapshot by ComplicationDataHub.snapshot.collectAsState()
    val mediaState by MediaSessionManagerHelper.mediaState.collectAsState()

    LaunchedEffect(type) {
        ComplicationDataHub.refreshTelemetryNow()
    }

    val backDispatcher = LocalOnBackPressedDispatcherOwner.current
    if (backDispatcher != null) {
        BackHandler {
            onDismissRequest()
        }
    }

    val (headerTitle, headerSubtitle, headerIcon) = when (type) {
        ComplicationMicroWindowType.CPU_MONITOR -> Triple("CPU Monitor", snapshot.cpu.sourceLabel, Icons.Default.Memory)
        ComplicationMicroWindowType.RAM_MONITOR -> Triple("Memory Monitor", "Physical RAM Usage", Icons.Default.Storage)
        ComplicationMicroWindowType.NETWORK_SPEED -> Triple("Network Speed", "Real-Time Throughput", Icons.Default.Speed)
        ComplicationMicroWindowType.TIMER -> Triple(
            "Timer",
            if (snapshot.timer.isSyncedExternal) "Synced • ${snapshot.timer.externalAppName ?: "Clock"}" else "Quick Timer",
            Icons.Default.Timer
        )
        ComplicationMicroWindowType.STOPWATCH -> Triple(
            "Stopwatch",
            if (snapshot.stopwatch.isSyncedExternal) "Synced • ${snapshot.stopwatch.externalAppName ?: "Clock"}" else "Quick Stopwatch",
            Icons.Default.AvTimer
        )
        ComplicationMicroWindowType.MEDIA_PROGRESS -> Triple(
            "Media Player",
            mediaState?.appName ?: "Playback Control",
            Icons.Default.MusicNote
        )
        ComplicationMicroWindowType.EVENT_COUNTDOWN -> Triple("Event Countdown", "Target Date & Label", Icons.Default.Event)
        ComplicationMicroWindowType.SIGNAL_QUALITY -> Triple("Signal Quality", snapshot.signal.transportLabel, Icons.Default.SignalCellularAlt)
        ComplicationMicroWindowType.DOWNLOAD_PROGRESS -> Triple("Active Downloads", "System & App Transfers", Icons.Default.Download)
        ComplicationMicroWindowType.LUX_METER -> Triple("Ambient Light", snapshot.lux.sensorName, Icons.Default.WbSunny)
        ComplicationMicroWindowType.BRIGHTNESS_SLIDER -> Triple("Display Brightness", "Adjust Screen & Auto Brightness", Icons.Default.BrightnessHigh)
        ComplicationMicroWindowType.VOLUME_SLIDER -> Triple("Sound & Volume", "Adjust Audio Streams", Icons.Default.VolumeUp)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10050f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.62f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismissRequest
                )
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp)
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .navigationBarsPadding()
                .imePadding()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .testTag("complication_micro_window_${type.name.lowercase()}"),
            shape = RoundedCornerShape(24.dp),
            color = ExpressiveTheme.colors.appBackground,
            tonalElevation = 6.dp,
            shadowElevation = 20.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MicroWindowHeader(
                    title = headerTitle,
                    subtitle = headerSubtitle,
                    icon = headerIcon,
                    onClose = onDismissRequest
                )

                Spacer(modifier = Modifier.height(4.dp))

                when (type) {
                    ComplicationMicroWindowType.CPU_MONITOR -> CpuMicroContent(snapshot, context, onDismissRequest)
                    ComplicationMicroWindowType.RAM_MONITOR -> RamMicroContent(snapshot, context, onDismissRequest)
                    ComplicationMicroWindowType.NETWORK_SPEED -> NetworkSpeedMicroContent(snapshot, context, onDismissRequest)
                    ComplicationMicroWindowType.TIMER -> TimerMicroContent(snapshot, context)
                    ComplicationMicroWindowType.STOPWATCH -> StopwatchMicroContent(snapshot, context)
                    ComplicationMicroWindowType.MEDIA_PROGRESS -> MediaMicroContent(mediaState, context, onDismissRequest)
                    ComplicationMicroWindowType.EVENT_COUNTDOWN -> EventCountdownMicroContent(snapshot, onDismissRequest)
                    ComplicationMicroWindowType.SIGNAL_QUALITY -> SignalQualityMicroContent(snapshot, context, onDismissRequest)
                    ComplicationMicroWindowType.DOWNLOAD_PROGRESS -> DownloadMicroContent(snapshot, context, onDismissRequest)
                    ComplicationMicroWindowType.LUX_METER -> LuxMeterMicroContent(snapshot)
                    ComplicationMicroWindowType.BRIGHTNESS_SLIDER -> BrightnessSliderMicroContent(snapshot, context, onDismissRequest)
                    ComplicationMicroWindowType.VOLUME_SLIDER -> VolumeSliderMicroContent(context, onDismissRequest)
                }
            }
        }
    }
}

@Composable
private fun MicroWindowHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ExpressiveTheme.colors.buttonPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(48.dp)
                .testTag("micro_window_close_button")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close micro window",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MicroCard(
    shape: Shape = MicroCardShapes.Single,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = ExpressiveTheme.colors.cardSurface
    ) {
        Box(modifier = Modifier.padding(14.dp)) {
            content()
        }
    }
}

@Composable
private fun MicroArcGauge(
    progress: Float,
    primaryText: String,
    secondaryText: String,
    arcColor: Color,
    sizeDp: Int = 116
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "micro_gauge_progress"
    )
    val animatedColor by animateColorAsState(
        targetValue = arcColor,
        animationSpec = tween(durationMillis = 300),
        label = "micro_gauge_color"
    )
    Box(
        modifier = Modifier.size(sizeDp.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            val stroke = 9.dp.toPx()
            drawArc(
                color = animatedColor.copy(alpha = 0.22f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = animatedColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = primaryText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = secondaryText,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MicroStatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = ExpressiveTheme.colors.nestedElementSurface
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PrimaryPillButton(
    text: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = ExpressiveTheme.colors.buttonPrimary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun SubtlePillButton(
    text: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = ExpressiveTheme.colors.buttonTertiarySubtle,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

private fun launchSystemActionAndCollapse(context: Context, action: String, onDismiss: () -> Unit) {
    val candidates = listOf(action, Settings.ACTION_SETTINGS)
    for (act in candidates) {
        try {
            val intent = Intent(act).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(intent)
            onDismiss()
            QuickPanelAccessibilityService.hideCustomPanel()
            QuickSettingsOverlayService.instance?.hideOverlayPanelSmoothly()
            return
        } catch (_: Throwable) {
        }
    }
}

// =============================================================================
// 1. CPU & RAM Monitors
// =============================================================================

@Composable
private fun CpuMicroContent(snapshot: ComplicationSnapshot, context: Context, onDismiss: () -> Unit) {
    val cpu = snapshot.cpu
    val loadColor = ComplicationDataHub.loadGradientColor(cpu.progress)
    val loadTier = when {
        cpu.loadPct >= 80 -> "Heavy Load"
        cpu.loadPct >= 45 -> "Moderate"
        else -> "Optimal"
    }

    MicroCard(shape = MicroCardShapes.Top) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MicroArcGauge(
                progress = cpu.progress,
                primaryText = "${cpu.loadPct}%",
                secondaryText = loadTier,
                arcColor = loadColor
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MicroStatBox(label = "CPU Cores", value = "${cpu.coreCount} Online", modifier = Modifier.fillMaxWidth())
                val freqStr = if (cpu.avgFreqMhz > 0 && cpu.maxFreqMhz > 0) {
                    "${cpu.avgFreqMhz} / ${cpu.maxFreqMhz} MHz"
                } else {
                    cpu.sourceLabel
                }
                MicroStatBox(label = "Frequency / Source", value = freqStr, modifier = Modifier.fillMaxWidth())
            }
        }
    }

    MicroCard(shape = MicroCardShapes.Bottom) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SubtlePillButton(
                text = "Sample Now",
                icon = Icons.Default.Refresh,
                modifier = Modifier.weight(1f),
                onClick = { ComplicationDataHub.refreshTelemetryNow() }
            )
            PrimaryPillButton(
                text = "Dev Settings",
                icon = Icons.Default.Settings,
                modifier = Modifier.weight(1f),
                onClick = { launchSystemActionAndCollapse(context, Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS, onDismiss) }
            )
        }
    }
}

@Composable
private fun RamMicroContent(snapshot: ComplicationSnapshot, context: Context, onDismiss: () -> Unit) {
    val ram = snapshot.ram
    val loadColor = ComplicationDataHub.loadGradientColor(ram.progress)
    val statusText = when {
        ram.lowMemory -> "Low RAM"
        ram.usagePct >= 82 -> "High"
        else -> "Healthy"
    }

    MicroCard(shape = MicroCardShapes.Top) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MicroArcGauge(
                progress = ram.progress,
                primaryText = "${ram.usagePct}%",
                secondaryText = statusText,
                arcColor = loadColor
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MicroStatBox(
                    label = "Used / Total",
                    value = String.format(Locale.US, "%.2f / %.2f GB", ram.usedGb, ram.totalGb),
                    modifier = Modifier.fillMaxWidth()
                )
                MicroStatBox(
                    label = "Available",
                    value = String.format(Locale.US, "%.2f GB free", ram.availableGb),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    MicroCard(shape = MicroCardShapes.Bottom) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SubtlePillButton(
                text = "Refresh",
                icon = Icons.Default.Refresh,
                modifier = Modifier.weight(1f),
                onClick = { ComplicationDataHub.refreshTelemetryNow() }
            )
            PrimaryPillButton(
                text = "Device Info",
                icon = Icons.Default.Settings,
                modifier = Modifier.weight(1f),
                onClick = { launchSystemActionAndCollapse(context, Settings.ACTION_DEVICE_INFO_SETTINGS, onDismiss) }
            )
        }
    }
}

// =============================================================================
// 2. Network Speed
// =============================================================================

@Composable
private fun NetworkSpeedMicroContent(snapshot: ComplicationSnapshot, context: Context, onDismiss: () -> Unit) {
    val net = snapshot.netSpeed
    MicroCard(shape = MicroCardShapes.Top) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MicroArcGauge(
                progress = net.progress,
                primaryText = net.compactSubline,
                secondaryText = "Total",
                arcColor = ExpressiveTheme.colors.buttonPrimary
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MicroStatBox(
                    label = "Download (↓)",
                    value = ComplicationDataHub.formatCompactSpeed(net.rxBytesPerSec),
                    modifier = Modifier.fillMaxWidth()
                )
                MicroStatBox(
                    label = "Upload (↑) • Scale Peak",
                    value = "${ComplicationDataHub.formatCompactSpeed(net.txBytesPerSec)} • ${ComplicationDataHub.formatCompactSpeed(net.peakBytesPerSec)}",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    val scales = listOf(0 to "Auto", 10 to "10M", 50 to "50M", 100 to "100M")
    MicroCard(shape = MicroCardShapes.Middle) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Arc Bandwidth Scale",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                scales.forEachIndexed { idx, (mbps, label) ->
                    val selected = net.configuredScaleMbps == mbps
                    val segShape = SegmentedButtonShapes.getShape(idx, scales.size, selected)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clip(segShape)
                            .clickable { ComplicationDataHub.setNetworkScaleMbps(mbps) },
                        shape = segShape,
                        color = if (selected) ExpressiveTheme.colors.selectorSelected else ExpressiveTheme.colors.nestedElementSurface
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp)) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    MicroCard(shape = MicroCardShapes.Bottom) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SubtlePillButton(
                text = "Reset Peak",
                icon = Icons.Default.Replay,
                modifier = Modifier.weight(1f),
                onClick = { ComplicationDataHub.resetNetworkPeak() }
            )
            PrimaryPillButton(
                text = "Network",
                icon = Icons.Default.Settings,
                modifier = Modifier.weight(1f),
                onClick = { launchSystemActionAndCollapse(context, Settings.ACTION_WIRELESS_SETTINGS, onDismiss) }
            )
        }
    }
}

// =============================================================================
// 3 & 4. Timer & Stopwatch (Live Clock Ticker Catch + Standalone Controls)
// =============================================================================

@Composable
private fun TimerMicroContent(snapshot: ComplicationSnapshot, context: Context) {
    val timer = snapshot.timer
    MicroCard(shape = MicroCardShapes.Top) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MicroArcGauge(
                progress = timer.progress,
                primaryText = ComplicationDataHub.formatDurationPrecise(timer.remainingMs),
                secondaryText = if (timer.isSyncedExternal) (timer.externalAppName ?: "Clock App") else if (timer.isRunning) "Running" else "Paused",
                arcColor = ExpressiveTheme.colors.buttonPrimary,
                sizeDp = 138
            )
            if (timer.isSyncedExternal) {
                Surface(
                    shape = CircleShape,
                    color = ExpressiveTheme.colors.nestedElementSurface
                ) {
                    Text(
                        text = "Live ticker from ${timer.externalAppName ?: "Clock"} • ${timer.externalTitle ?: "Timer"}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    if (!timer.isSyncedExternal) {
        val presets = listOf(60_000L to "1m", 180_000L to "3m", 300_000L to "5m", 600_000L to "10m", 900_000L to "15m", 1_800_000L to "30m")
        MicroCard(shape = MicroCardShapes.Middle) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Quick Presets",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presets.forEachIndexed { idx, (ms, label) ->
                        val selected = !timer.isRunning && timer.totalDurationMs == ms
                        val shape = SegmentedButtonShapes.getShape(idx, presets.size, selected)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .clip(shape)
                                .clickable { ComplicationDataHub.setTimerDuration(ms) },
                            shape = shape,
                            color = if (selected) ExpressiveTheme.colors.selectorSelected else ExpressiveTheme.colors.nestedElementSurface
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    MicroCard(shape = MicroCardShapes.Bottom) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SubtlePillButton(
                    text = "-30s",
                    icon = Icons.Default.Remove,
                    modifier = Modifier.weight(1f),
                    onClick = { ComplicationDataHub.adjustTimerSeconds(-30) }
                )
                SubtlePillButton(
                    text = "+1m",
                    icon = Icons.Default.Add,
                    modifier = Modifier.weight(1f),
                    onClick = { ComplicationDataHub.adjustTimerSeconds(60) }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PrimaryPillButton(
                    text = if (timer.isRunning) "Pause" else "Start",
                    icon = if (timer.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    modifier = Modifier.weight(1f),
                    onClick = { ComplicationDataHub.toggleTimer(context) }
                )
                SubtlePillButton(
                    text = "Reset",
                    icon = Icons.Default.Replay,
                    modifier = Modifier.weight(1f),
                    onClick = { ComplicationDataHub.resetTimer() }
                )
            }
            SubtlePillButton(
                text = "Open Clock App",
                icon = Icons.Default.OpenInNew,
                modifier = Modifier.fillMaxWidth(),
                onClick = { ComplicationDataHub.openExternalClockApp(context, isTimer = true) }
            )
        }
    }
}

@Composable
private fun StopwatchMicroContent(snapshot: ComplicationSnapshot, context: Context) {
    val sw = snapshot.stopwatch
    MicroCard(shape = MicroCardShapes.Top) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MicroArcGauge(
                progress = sw.progress,
                primaryText = ComplicationDataHub.formatDurationPrecise(sw.elapsedMs, showTenths = true),
                secondaryText = if (sw.isSyncedExternal) (sw.externalAppName ?: "Clock App") else if (sw.isRunning) "Running" else "Ready",
                arcColor = ExpressiveTheme.colors.buttonPrimary,
                sizeDp = 138
            )
            if (sw.isSyncedExternal) {
                Surface(shape = CircleShape, color = ExpressiveTheme.colors.nestedElementSurface) {
                    Text(
                        text = "Synced with ${sw.externalAppName ?: "Clock App"} ticker",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }

    if (sw.laps.isNotEmpty()) {
        MicroCard(shape = MicroCardShapes.Middle) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Laps (${sw.laps.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                sw.laps.take(5).forEachIndexed { idx, lapMs ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = ExpressiveTheme.colors.nestedElementSurface
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Lap ${sw.laps.size - idx}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = ComplicationDataHub.formatDurationPrecise(lapMs, showTenths = true),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    MicroCard(shape = MicroCardShapes.Bottom) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PrimaryPillButton(
                    text = if (sw.isRunning) "Pause" else "Start",
                    icon = if (sw.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    modifier = Modifier.weight(1f),
                    onClick = { ComplicationDataHub.toggleStopwatch() }
                )
                SubtlePillButton(
                    text = "Lap",
                    icon = Icons.Default.Flag,
                    modifier = Modifier.weight(1f),
                    onClick = { ComplicationDataHub.lapStopwatch() }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SubtlePillButton(
                    text = "Reset",
                    icon = Icons.Default.Replay,
                    modifier = Modifier.weight(1f),
                    onClick = { ComplicationDataHub.resetStopwatch() }
                )
                SubtlePillButton(
                    text = "Open Clock",
                    icon = Icons.Default.OpenInNew,
                    modifier = Modifier.weight(1f),
                    onClick = { ComplicationDataHub.openExternalClockApp(context, isTimer = false) }
                )
            }
        }
    }
}

// =============================================================================
// 5. Media Track Progress
// =============================================================================

@Composable
private fun MediaMicroContent(
    mediaState: com.example.model.MediaState?,
    context: Context,
    onDismiss: () -> Unit
) {
    if (mediaState == null) {
        MicroCard(shape = MicroCardShapes.Top) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = "No Active Media Session",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Start playing music or video to sync progress here.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        MicroCard(shape = MicroCardShapes.Bottom) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PrimaryPillButton(
                    text = "Open Music App",
                    icon = Icons.Default.OpenInNew,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        MediaSessionManagerHelper.openActivePlayerApp(context)
                        onDismiss()
                    }
                )
                SubtlePillButton(
                    text = "Audio Output",
                    icon = Icons.Default.Speaker,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        MediaSessionManagerHelper.openAudioOutputPicker(context)
                        onDismiss()
                    }
                )
            }
        }
        return
    }

    var isSeeking by remember { mutableStateOf(false) }
    var sliderProgress by remember(mediaState.title, mediaState.progress) {
        mutableFloatStateOf(mediaState.progress.coerceIn(0f, 1f))
    }
    val displayProgress = if (isSeeking) sliderProgress else mediaState.progress.coerceIn(0f, 1f)
    val displayPosMs = if (mediaState.totalDurationMs > 0L) {
        (displayProgress * mediaState.totalDurationMs).toLong()
    } else {
        mediaState.currentPositionMs
    }

    MicroCard(shape = MicroCardShapes.Top) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MicroArcGauge(
                    progress = displayProgress,
                    primaryText = "${(displayProgress * 100f).toInt()}%",
                    secondaryText = if (mediaState.isPlaying) "Playing" else "Paused",
                    arcColor = ExpressiveTheme.colors.buttonPrimary,
                    sizeDp = 96
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = mediaState.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = mediaState.artist,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        shape = CircleShape,
                        color = ExpressiveTheme.colors.nestedElementSurface
                    ) {
                        Text(
                            text = "${mediaState.appName} • ${mediaState.outputDevice}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Column {
                Slider(
                    value = displayProgress,
                    onValueChange = {
                        isSeeking = true
                        sliderProgress = it
                    },
                    onValueChangeFinished = {
                        isSeeking = false
                        MediaSessionManagerHelper.seekTo(sliderProgress)
                    },
                    enabled = mediaState.totalDurationMs > 0L,
                    colors = SliderDefaults.colors(
                        thumbColor = ExpressiveTheme.colors.buttonPrimary,
                        activeTrackColor = ExpressiveTheme.colors.buttonPrimary
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = ComplicationDataHub.formatDurationPrecise(displayPosMs),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (mediaState.totalDurationMs > 0L) {
                            ComplicationDataHub.formatDurationPrecise(mediaState.totalDurationMs)
                        } else "--:--",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    MicroCard(shape = MicroCardShapes.Bottom) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SubtlePillButton(
                    text = "Prev",
                    icon = Icons.Default.SkipPrevious,
                    modifier = Modifier.weight(1f),
                    onClick = { MediaSessionManagerHelper.previous() }
                )
                PrimaryPillButton(
                    text = if (mediaState.isPlaying) "Pause" else "Play",
                    icon = if (mediaState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    modifier = Modifier.weight(1f),
                    onClick = { MediaSessionManagerHelper.playPause() }
                )
                SubtlePillButton(
                    text = "Next",
                    icon = Icons.Default.SkipNext,
                    modifier = Modifier.weight(1f),
                    onClick = { MediaSessionManagerHelper.next() }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SubtlePillButton(
                    text = "Output",
                    icon = Icons.Default.Speaker,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        MediaSessionManagerHelper.openAudioOutputPicker(context)
                        onDismiss()
                    }
                )
                SubtlePillButton(
                    text = "Open App",
                    icon = Icons.Default.OpenInNew,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        MediaSessionManagerHelper.openActivePlayerApp(context)
                        onDismiss()
                    }
                )
            }
        }
    }
}

// =============================================================================
// 6. Event Countdown (Custom Name & Date Editor)
// =============================================================================

@Composable
private fun EventCountdownMicroContent(
    snapshot: ComplicationSnapshot,
    onDismiss: () -> Unit
) {
    val countdown = snapshot.countdown
    var draftName by remember(countdown.eventName) { mutableStateOf(countdown.eventName) }
    val initialCal = remember(countdown.targetEpochMs) {
        Calendar.getInstance().apply {
            timeInMillis = if (countdown.targetEpochMs > 0L) countdown.targetEpochMs else System.currentTimeMillis() + 14L * 86_400_000L
        }
    }
    var selYear by remember(countdown.targetEpochMs) { mutableIntStateOf(initialCal.get(Calendar.YEAR)) }
    var selMonth by remember(countdown.targetEpochMs) { mutableIntStateOf(initialCal.get(Calendar.MONTH)) } // 0-based
    var selDay by remember(countdown.targetEpochMs) { mutableIntStateOf(initialCal.get(Calendar.DAY_OF_MONTH)) }

    val draftTargetCal = remember(selYear, selMonth, selDay) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, selYear)
            set(Calendar.MONTH, selMonth)
            val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, selDay.coerceIn(1, maxDay))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    val dateFmt = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()) }
    val monthNames = remember { listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec") }
    val daysDiff = ((draftTargetCal.timeInMillis - System.currentTimeMillis()).coerceAtLeast(0L) / 86_400_000L)

    MicroCard(shape = MicroCardShapes.Top) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MicroArcGauge(
                progress = countdown.progress,
                primaryText = countdown.compactSubline,
                secondaryText = "${countdown.hoursLeft}h left",
                arcColor = ExpressiveTheme.colors.buttonPrimary,
                sizeDp = 104
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = countdown.eventName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = dateFmt.format(draftTargetCal.time),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Preview: ${daysDiff}d remaining",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    MicroCard(shape = MicroCardShapes.Middle) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = draftName,
                onValueChange = { if (it.length <= 36) draftName = it },
                label = { Text("Event Name") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ExpressiveTheme.colors.nestedElementSurface,
                    unfocusedContainerColor = ExpressiveTheme.colors.nestedElementSurface,
                    focusedBorderColor = ExpressiveTheme.colors.buttonPrimary,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth().testTag("event_countdown_name_input")
            )

            Text(
                text = "Quick Offset",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val quickOffsets = listOf(3 to "+3d", 7 to "+7d", 14 to "+14d", 30 to "+30d", 90 to "+90d")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                quickOffsets.forEachIndexed { idx, (daysToAdd, label) ->
                    val shape = SegmentedButtonShapes.getShape(idx, quickOffsets.size, false)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clip(shape)
                            .clickable {
                                val c = Calendar.getInstance().apply {
                                    add(Calendar.DAY_OF_YEAR, daysToAdd)
                                }
                                selYear = c.get(Calendar.YEAR)
                                selMonth = c.get(Calendar.MONTH)
                                selDay = c.get(Calendar.DAY_OF_MONTH)
                            },
                        shape = shape,
                        color = ExpressiveTheme.colors.nestedElementSurface
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DateStepperUnit(
                    label = "Month",
                    valueText = monthNames[selMonth.coerceIn(0, 11)],
                    onDec = {
                        if (selMonth == 0) {
                            selMonth = 11
                            selYear = (selYear - 1).coerceAtLeast(2024)
                        } else {
                            selMonth -= 1
                        }
                    },
                    onInc = {
                        if (selMonth == 11) {
                            selMonth = 0
                            selYear += 1
                        } else {
                            selMonth += 1
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                DateStepperUnit(
                    label = "Day",
                    valueText = selDay.toString().padStart(2, '0'),
                    onDec = {
                        val maxD = Calendar.getInstance().apply {
                            set(Calendar.YEAR, selYear)
                            set(Calendar.MONTH, selMonth)
                        }.getActualMaximum(Calendar.DAY_OF_MONTH)
                        selDay = if (selDay <= 1) maxD else selDay - 1
                    },
                    onInc = {
                        val maxD = Calendar.getInstance().apply {
                            set(Calendar.YEAR, selYear)
                            set(Calendar.MONTH, selMonth)
                        }.getActualMaximum(Calendar.DAY_OF_MONTH)
                        selDay = if (selDay >= maxD) 1 else selDay + 1
                    },
                    modifier = Modifier.weight(1f)
                )
                DateStepperUnit(
                    label = "Year",
                    valueText = selYear.toString(),
                    onDec = { selYear = (selYear - 1).coerceAtLeast(2024) },
                    onInc = { selYear = (selYear + 1).coerceAtMost(2100) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    MicroCard(shape = MicroCardShapes.Bottom) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SubtlePillButton(
                text = "Cancel",
                modifier = Modifier.weight(1f),
                onClick = onDismiss
            )
            PrimaryPillButton(
                text = "Save Event",
                icon = Icons.Default.Event,
                modifier = Modifier.weight(1.3f).testTag("event_countdown_save_button"),
                onClick = {
                    ComplicationDataHub.updateEventCountdown(draftName, draftTargetCal.timeInMillis)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun DateStepperUnit(
    label: String,
    valueText: String,
    onDec: () -> Unit,
    onInc: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = ExpressiveTheme.colors.nestedElementSurface
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDec, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease $label", modifier = Modifier.size(16.dp))
                }
                Text(
                    text = valueText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onInc, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Increase $label", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// =============================================================================
// 7. Signal Quality Index
// =============================================================================

@Composable
private fun SignalQualityMicroContent(snapshot: ComplicationSnapshot, context: Context, onDismiss: () -> Unit) {
    val sig = snapshot.signal
    MicroCard(shape = MicroCardShapes.Top) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MicroArcGauge(
                progress = sig.progress,
                primaryText = if (sig.isConnected) "${sig.strengthPct}%" else "Off",
                secondaryText = sig.qualityLabel,
                arcColor = ExpressiveTheme.colors.buttonPrimary
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MicroStatBox(
                    label = "Connection Type",
                    value = sig.transportLabel,
                    modifier = Modifier.fillMaxWidth()
                )
                val rssiStr = sig.rssiDbm?.let { "$it dBm" } ?: "Unavailable"
                val linkStr = sig.linkSpeedMbps?.let { "$it Mbps" }
                    ?: sig.frequencyMhz?.let { "${it} MHz" }
                    ?: "Live Index"
                MicroStatBox(
                    label = "RSSI / Link",
                    value = "$rssiStr • $linkStr",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    MicroCard(shape = MicroCardShapes.Bottom) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SubtlePillButton(
                text = "Refresh",
                icon = Icons.Default.Refresh,
                modifier = Modifier.weight(1f),
                onClick = { ComplicationDataHub.refreshTelemetryNow() }
            )
            PrimaryPillButton(
                text = "Network Settings",
                icon = Icons.Default.Settings,
                modifier = Modifier.weight(1.2f),
                onClick = { launchSystemActionAndCollapse(context, Settings.ACTION_WIFI_SETTINGS, onDismiss) }
            )
        }
    }
}

// =============================================================================
// 8. Active Download Progress
// =============================================================================

@Composable
private fun DownloadMicroContent(snapshot: ComplicationSnapshot, context: Context, onDismiss: () -> Unit) {
    val dl = snapshot.downloads
    MicroCard(shape = MicroCardShapes.Top) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MicroArcGauge(
                progress = dl.overallProgress,
                primaryText = if (dl.isDownloading) "${(dl.overallProgress * 100f).toInt()}%" else "Idle",
                secondaryText = if (dl.isDownloading) ComplicationDataHub.formatCompactSpeed(dl.totalSpeedBytesPerSec) else "0 active",
                arcColor = ExpressiveTheme.colors.buttonPrimary,
                sizeDp = 104
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MicroStatBox(
                    label = "Active Tasks",
                    value = if (dl.entries.isEmpty()) "None active" else "${dl.entries.size} downloading",
                    modifier = Modifier.fillMaxWidth()
                )
                MicroStatBox(
                    label = "Transfer Rate",
                    value = if (dl.isDownloading) ComplicationDataHub.formatCompactSpeed(dl.totalSpeedBytesPerSec) else "0 B/s",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    MicroCard(shape = MicroCardShapes.Middle) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Detected System & App Downloads",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (dl.entries.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = ExpressiveTheme.colors.nestedElementSurface
                ) {
                    Text(
                        text = "No ongoing downloads detected via DownloadManager or notifications.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                dl.entries.take(5).forEach { entry ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = entry.contentIntent != null) {
                                try {
                                    entry.contentIntent?.send()
                                    onDismiss()
                                    QuickPanelAccessibilityService.hideCustomPanel()
                                    QuickSettingsOverlayService.instance?.hideOverlayPanelSmoothly()
                                } catch (_: Throwable) {}
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = ExpressiveTheme.colors.nestedElementSurface
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = entry.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = entry.speedText.ifBlank { entry.subtitle },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (entry.isIndeterminate) {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                    color = ExpressiveTheme.colors.buttonPrimary
                                )
                            } else {
                                LinearProgressIndicator(
                                    progress = { entry.progressFraction.coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                    color = ExpressiveTheme.colors.buttonPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    MicroCard(shape = MicroCardShapes.Bottom) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SubtlePillButton(
                text = "Refresh",
                icon = Icons.Default.Refresh,
                modifier = Modifier.weight(1f),
                onClick = { ComplicationDataHub.refreshTelemetryNow() }
            )
            PrimaryPillButton(
                text = "Downloads",
                icon = Icons.Default.Download,
                modifier = Modifier.weight(1.2f),
                onClick = {
                    launchSystemActionAndCollapse(context, DownloadManager.ACTION_VIEW_DOWNLOADS, onDismiss)
                }
            )
        }
    }
}

// =============================================================================
// 9. Ambient Light (Lux Meter)
// =============================================================================

@Composable
private fun LuxMeterMicroContent(snapshot: ComplicationSnapshot) {
    val lux = snapshot.lux
    MicroCard(shape = MicroCardShapes.Top) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MicroArcGauge(
                progress = lux.progress,
                primaryText = lux.compactSubline,
                secondaryText = "${lux.brightnessPct}% • ${lux.conditionLabel}",
                arcColor = ExpressiveTheme.colors.buttonPrimary
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MicroStatBox(
                    label = "Measured Light",
                    value = if (lux.hasSensor) "${lux.lux.toInt()} lux" else "No sensor",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    val scales = listOf(1_000f to "1k lx", 10_000f to "10k lx", 50_000f to "50k lx")
    MicroCard(shape = MicroCardShapes.Bottom) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Lux Arc Scale Ceiling (Max sensor: ${lux.maxRange.toInt()} lx)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                scales.forEachIndexed { idx, (maxVal, label) ->
                    val selected = kotlin.math.abs(lux.maxScaleLux - maxVal) < 50f
                    val shape = SegmentedButtonShapes.getShape(idx, scales.size, selected)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clip(shape)
                            .clickable { ComplicationDataHub.setLuxMaxScale(maxVal) },
                        shape = shape,
                        color = if (selected) ExpressiveTheme.colors.selectorSelected else ExpressiveTheme.colors.nestedElementSurface
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// 10. Brightness Slider Menu
// =============================================================================

@Composable
private fun BrightnessSliderMicroContent(
    snapshot: ComplicationSnapshot,
    context: Context,
    onDismiss: () -> Unit
) {
    val nativeManager = NativeTileManager.instance
    val brightnessState = nativeManager?.brightness?.collectAsState()
    val minBrightness by PanelPreferences.minBrightness.collectAsState()
    val maxBrightness by PanelPreferences.maxBrightness.collectAsState()
    val brightnessVal = brightnessState?.value ?: 128
    val safeMin = minBrightness.coerceIn(0, 250)
    val safeMax = maxBrightness.coerceIn(safeMin + 5, 255)
    val clampedVal = brightnessVal.coerceIn(safeMin, safeMax)
    val fraction = ((clampedVal - safeMin).toFloat() / (safeMax - safeMin).toFloat()).coerceIn(0f, 1f)
    val pct = ((brightnessVal / 255f).coerceIn(0f, 1f) * 100f).toInt()
    val isAuto = snapshot.lux.isAutoBrightnessEnabled
    val brightnessIcon = when {
        pct >= 70 -> Icons.Default.BrightnessHigh
        pct >= 35 -> Icons.Default.BrightnessMedium
        else -> Icons.Default.BrightnessLow
    }

    MicroCard(shape = MicroCardShapes.Top) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MicroArcGauge(
                    progress = (brightnessVal / 255f).coerceIn(0f, 1f),
                    primaryText = "$pct%",
                    secondaryText = if (isAuto) "Adaptive" else "Manual",
                    arcColor = ExpressiveTheme.colors.buttonPrimary,
                    sizeDp = 96
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MicroStatBox(
                        label = "Brightness Level",
                        value = "$brightnessVal / 255 ($pct%)",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        nativeManager?.setBrightness((brightnessVal - 20).coerceAtLeast(safeMin))
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = brightnessIcon,
                        contentDescription = "Decrease brightness",
                        tint = ExpressiveTheme.colors.buttonPrimary
                    )
                }
                QsProgressSlider(
                    value = clampedVal.toFloat(),
                    onValueChange = { newVal ->
                        nativeManager?.setBrightness(newVal.toInt().coerceIn(safeMin, safeMax))
                    },
                    valueRange = safeMin.toFloat()..safeMax.toFloat(),
                    sliderTarget = QsSliderTarget.BRIGHTNESS,
                    activeColor = ExpressiveTheme.colors.buttonPrimary,
                    inactiveColor = ExpressiveTheme.colors.nestedElementSurface,
                    thumbColor = ExpressiveTheme.colors.buttonPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("micro_brightness_slider")
                )
                IconButton(
                    onClick = {
                        nativeManager?.setBrightness((brightnessVal + 20).coerceAtMost(safeMax))
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase brightness",
                        tint = ExpressiveTheme.colors.buttonPrimary
                    )
                }
            }

            val presets = listOf(0.25f to "25%", 0.50f to "50%", 0.75f to "75%", 1.00f to "100%")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                presets.forEachIndexed { idx, (presetFrac, label) ->
                    val targetRaw = (safeMin + (safeMax - safeMin) * presetFrac).toInt()
                    val selected = kotlin.math.abs(fraction - presetFrac) < 0.08f
                    val segShape = SegmentedButtonShapes.getShape(idx, presets.size, selected)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clip(segShape)
                            .clickable { nativeManager?.setBrightness(targetRaw) },
                        shape = segShape,
                        color = if (selected) ExpressiveTheme.colors.selectorSelected else ExpressiveTheme.colors.nestedElementSurface
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    MicroCard(shape = MicroCardShapes.Bottom) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SubtlePillButton(
                text = if (isAuto) "Auto On" else "Auto Off",
                icon = Icons.Default.BrightnessAuto,
                modifier = Modifier.weight(1f),
                onClick = {
                    if (Settings.System.canWrite(context)) {
                        val nextMode = if (isAuto) {
                            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                        } else {
                            Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                        }
                        Settings.System.putInt(
                            context.contentResolver,
                            Settings.System.SCREEN_BRIGHTNESS_MODE,
                            nextMode
                        )
                        ComplicationDataHub.refreshTelemetryNow()
                    } else {
                        try {
                            val permIntent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                                data = Uri.parse("package:${context.packageName}")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(permIntent)
                            onDismiss()
                            QuickPanelAccessibilityService.hideCustomPanel()
                            QuickSettingsOverlayService.instance?.hideOverlayPanelSmoothly()
                        } catch (_: Throwable) {}
                    }
                }
            )
            PrimaryPillButton(
                text = "Display Settings",
                icon = Icons.Default.Settings,
                modifier = Modifier.weight(1.2f),
                onClick = { launchSystemActionAndCollapse(context, Settings.ACTION_DISPLAY_SETTINGS, onDismiss) }
            )
        }
    }
}

// =============================================================================
// 11. Volume Slider Menu
// =============================================================================

@Composable
private fun VolumeSliderMicroContent(
    context: Context,
    onDismiss: () -> Unit
) {
    val nativeManager = NativeTileManager.instance
    val mediaVol = nativeManager?.mediaVolume?.collectAsState()?.value ?: 0.5f
    val ringVol = nativeManager?.ringVolume?.collectAsState()?.value ?: 0.5f
    val notifVol = nativeManager?.notificationVolume?.collectAsState()?.value ?: 0.5f
    val alarmVol = nativeManager?.alarmVolume?.collectAsState()?.value ?: 0.5f
    val isDndOn = nativeManager?.isDndEnabled?.collectAsState()?.value ?: false

    MicroCard(shape = MicroCardShapes.Top) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MicroVolumeStreamRow(
                label = "Media",
                icon = if (mediaVol <= 0.01f) Icons.Default.VolumeOff else Icons.Default.MusicNote,
                value = mediaVol,
                onValueChange = { nativeManager?.setStreamVolume(AudioManager.STREAM_MUSIC, it) },
                onIconToggle = {
                    val next = if (mediaVol > 0.05f) 0f else 0.65f
                    nativeManager?.setStreamVolume(AudioManager.STREAM_MUSIC, next)
                }
            )
            MicroVolumeStreamRow(
                label = "Ring",
                icon = if (ringVol <= 0.01f) Icons.Default.VolumeOff else Icons.Default.PhoneAndroid,
                value = ringVol,
                onValueChange = { nativeManager?.setStreamVolume(AudioManager.STREAM_RING, it) },
                onIconToggle = {
                    val next = if (ringVol > 0.05f) 0f else 0.65f
                    nativeManager?.setStreamVolume(AudioManager.STREAM_RING, next)
                }
            )
            MicroVolumeStreamRow(
                label = "Notifications",
                icon = if (notifVol <= 0.01f) Icons.Default.VolumeOff else Icons.Default.Notifications,
                value = notifVol,
                onValueChange = { nativeManager?.setStreamVolume(AudioManager.STREAM_NOTIFICATION, it) },
                onIconToggle = {
                    val next = if (notifVol > 0.05f) 0f else 0.65f
                    nativeManager?.setStreamVolume(AudioManager.STREAM_NOTIFICATION, next)
                }
            )
            MicroVolumeStreamRow(
                label = "Alarm",
                icon = if (alarmVol <= 0.01f) Icons.Default.VolumeOff else Icons.Default.Alarm,
                value = alarmVol,
                onValueChange = { nativeManager?.setStreamVolume(AudioManager.STREAM_ALARM, it) },
                onIconToggle = {
                    val next = if (alarmVol > 0.05f) 0f else 0.75f
                    nativeManager?.setStreamVolume(AudioManager.STREAM_ALARM, next)
                }
            )
        }
    }

    MicroCard(shape = MicroCardShapes.Bottom) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SubtlePillButton(
                text = if (isDndOn) "DND On" else "DND Off",
                icon = Icons.Default.DoNotDisturb,
                modifier = Modifier.weight(1f),
                onClick = { nativeManager?.toggleDnd() }
            )
            PrimaryPillButton(
                text = "Sound Settings",
                icon = Icons.Default.Settings,
                modifier = Modifier.weight(1.2f),
                onClick = { launchSystemActionAndCollapse(context, Settings.ACTION_SOUND_SETTINGS, onDismiss) }
            )
        }
    }
}

@Composable
private fun MicroVolumeStreamRow(
    label: String,
    icon: ImageVector,
    value: Float,
    onValueChange: (Float) -> Unit,
    onIconToggle: () -> Unit
) {
    val pct = (value.coerceIn(0f, 1f) * 100f).toInt()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = ExpressiveTheme.colors.nestedElementSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onIconToggle,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(ExpressiveTheme.colors.buttonPrimary.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Toggle $label mute",
                        tint = ExpressiveTheme.colors.buttonPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$pct%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                QsProgressSlider(
                    value = value.coerceIn(0f, 1f),
                    onValueChange = onValueChange,
                    valueRange = 0f..1f,
                    sliderTarget = QsSliderTarget.VOLUME,
                    activeColor = ExpressiveTheme.colors.buttonPrimary,
                    inactiveColor = ExpressiveTheme.colors.cardSurface,
                    thumbColor = ExpressiveTheme.colors.buttonPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("micro_volume_slider_${label.lowercase()}")
                )
            }
        }
    }
}
