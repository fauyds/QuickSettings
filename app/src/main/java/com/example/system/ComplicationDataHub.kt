package com.example.system

import android.app.ActivityManager
import android.app.DownloadManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.TrafficStats
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Process
import android.os.SystemClock
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.compose.ui.graphics.Color
import com.example.model.MediaState
import com.example.service.MediaSessionManagerHelper
import com.example.service.QuickPanelAccessibilityService
import com.example.service.QuickSettingsOverlayService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.roundToInt

enum class ComplicationMicroWindowType {
    CPU_MONITOR,
    RAM_MONITOR,
    NETWORK_SPEED,
    TIMER,
    STOPWATCH,
    MEDIA_PROGRESS,
    EVENT_COUNTDOWN,
    SIGNAL_QUALITY,
    DOWNLOAD_PROGRESS,
    LUX_METER,
    BRIGHTNESS_SLIDER,
    VOLUME_SLIDER
}

data class ExternalClockTicker(
    val key: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val isTimer: Boolean,
    val isRunning: Boolean,
    val isCountDown: Boolean,
    val baseWhenEpochMs: Long,
    val parsedDisplayMs: Long?,
    val toggleAction: PendingIntent?,
    val resetAction: PendingIntent?,
    val lapAction: PendingIntent?,
    val plusOneMinAction: PendingIntent?,
    val dismissAction: PendingIntent? = null,
    val contentIntent: PendingIntent?,
    val isFinished: Boolean = false,
    val lastUpdatedAtMs: Long = System.currentTimeMillis()
)

data class ActiveDownloadEntry(
    val id: String,
    val title: String,
    val subtitle: String,
    val progressFraction: Float,
    val isIndeterminate: Boolean,
    val speedBytesPerSec: Long,
    val speedText: String,
    val contentIntent: PendingIntent? = null,
    val isFromDownloadManager: Boolean = false
)

data class CpuMonitorState(
    val loadPct: Int = 0,
    val progress: Float = 0f,
    val coreCount: Int = Runtime.getRuntime().availableProcessors().coerceAtLeast(1),
    val avgFreqMhz: Int = 0,
    val maxFreqMhz: Int = 0,
    val sourceLabel: String = "System CPU"
)

data class RamMonitorState(
    val usagePct: Int = 0,
    val progress: Float = 0f,
    val usedGb: Float = 0f,
    val totalGb: Float = 0f,
    val availableGb: Float = 0f,
    val lowMemory: Boolean = false
)

data class NetworkSpeedState(
    val rxBytesPerSec: Long = 0L,
    val txBytesPerSec: Long = 0L,
    val totalBytesPerSec: Long = 0L,
    val peakBytesPerSec: Long = 1_000_000L,
    val configuredScaleMbps: Int = 0, // 0 = Auto
    val compactSubline: String = "0 B/s",
    val detailedSubtitle: String = "↓0 B/s ↑0 B/s",
    val progress: Float = 0f
)

data class TimerComplicationState(
    val totalDurationMs: Long = 300_000L,
    val remainingMs: Long = 300_000L,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false,
    val isSyncedExternal: Boolean = false,
    val externalAppName: String? = null,
    val externalTitle: String? = null,
    val progress: Float = 1f,
    val compactSubline: String = "05:00",
    val detailedSubtitle: String = "Ready"
)

data class StopwatchComplicationState(
    val elapsedMs: Long = 0L,
    val isRunning: Boolean = false,
    val isSyncedExternal: Boolean = false,
    val externalAppName: String? = null,
    val laps: List<Long> = emptyList(),
    val progress: Float = 0f,
    val compactSubline: String = "00:00",
    val detailedSubtitle: String = "Paused"
)

data class EventCountdownState(
    val eventName: String = "Target Event",
    val targetEpochMs: Long = 0L,
    val startEpochMs: Long = 0L,
    val daysLeft: Long = 14L,
    val hoursLeft: Long = 0L,
    val minutesLeft: Long = 0L,
    val isPast: Boolean = false,
    val progress: Float = 0.5f,
    val compactSubline: String = "14d",
    val detailedSubtitle: String = "Target Event • 14d"
)

data class SignalQualityState(
    val isConnected: Boolean = false,
    val transportLabel: String = "Offline",
    val networkTypeShort: String = "Offline",
    val strengthPct: Int = 0,
    val progress: Float = 0f,
    val rssiDbm: Int? = null,
    val linkSpeedMbps: Int? = null,
    val frequencyMhz: Int? = null,
    val qualityLabel: String = "No Signal",
    val detailedSubtitle: String = "Disconnected"
)

data class DownloadProgressState(
    val entries: List<ActiveDownloadEntry> = emptyList(),
    val isDownloading: Boolean = false,
    val overallProgress: Float = 0f,
    val totalSpeedBytesPerSec: Long = 0L,
    val compactSubline: String = "Idle",
    val detailedSubtitle: String = "No downloads"
)

data class LuxMeterState(
    val lux: Float = 0f,
    val hasSensor: Boolean = true,
    val sensorName: String = "Ambient Light Sensor",
    val maxRange: Float = 10_000f,
    val maxScaleLux: Float = 10_000f,
    val brightnessPct: Int = 0,
    val progress: Float = 0f,
    val conditionLabel: String = "Waiting...",
    val compactSubline: String = "0 lx",
    val detailedSubtitle: String = "0 lx • Indoor",
    val isAutoBrightnessEnabled: Boolean = false
)

data class ComplicationSnapshot(
    val cpu: CpuMonitorState = CpuMonitorState(),
    val ram: RamMonitorState = RamMonitorState(),
    val netSpeed: NetworkSpeedState = NetworkSpeedState(),
    val timer: TimerComplicationState = TimerComplicationState(),
    val stopwatch: StopwatchComplicationState = StopwatchComplicationState(),
    val countdown: EventCountdownState = EventCountdownState(),
    val signal: SignalQualityState = SignalQualityState(),
    val downloads: DownloadProgressState = DownloadProgressState(),
    val lux: LuxMeterState = LuxMeterState()
)

object ComplicationDataHub {

    private const val PREFS_NAME = "quickpanel_complications_prefs"
    private const val KEY_EVENT_NAME = "event_countdown_name"
    private const val KEY_EVENT_TARGET_MS = "event_countdown_target_ms"
    private const val KEY_EVENT_START_MS = "event_countdown_start_ms"
    private const val KEY_TIMER_DURATION_MS = "local_timer_duration_ms"
    private const val KEY_NET_SCALE_MBPS = "net_speed_scale_mbps"
    private const val KEY_LUX_MAX_SCALE = "lux_max_scale"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var appContext: Context? = null
    @Volatile
    private var initialized = false
    private var pollingJob: Job? = null

    private val _snapshot = MutableStateFlow(ComplicationSnapshot())
    val snapshot: StateFlow<ComplicationSnapshot> = _snapshot.asStateFlow()

    private val _activeMicroWindow = MutableStateFlow<ComplicationMicroWindowType?>(null)
    val activeMicroWindow: StateFlow<ComplicationMicroWindowType?> = _activeMicroWindow.asStateFlow()

    // CPU sampling state
    private var prevProcTotal: Long = -1L
    private var prevProcIdle: Long = -1L
    private var prevAppCpuMs: Long = -1L
    private var prevAppWallMs: Long = -1L

    // Network sampling state
    private var prevRxBytes: Long = -1L
    private var prevTxBytes: Long = -1L
    private var prevTrafficTimeMs: Long = -1L
    private var observedPeakBps: Long = 1_000_000L
    private var configuredScaleMbps: Int = 0

    // DownloadManager sampling state
    private var prevDmDownloadedBytes: Long = -1L
    private var prevDmSampleTimeMs: Long = -1L

    // Notification-sourced tickers & downloads
    @Volatile
    private var externalTimerTicker: ExternalClockTicker? = null
    @Volatile
    private var externalTimerMaxDurationMs: Long = 0L
    @Volatile
    private var externalTimerKey: String? = null
    @Volatile
    private var externalStopwatchTicker: ExternalClockTicker? = null
    @Volatile
    private var notificationDownloads: List<ActiveDownloadEntry> = emptyList()

    // Local Timer state
    @Volatile
    private var configuredTimerDurationMs: Long = 300_000L
    @Volatile
    private var localTimerTotalMs: Long = 300_000L
    @Volatile
    private var localTimerRemainingMs: Long = 300_000L
    @Volatile
    private var localTimerEndElapsedMs: Long = 0L
    @Volatile
    private var localTimerRunning: Boolean = false
    @Volatile
    private var localTimerFinishedAlerted: Boolean = false
    @Volatile
    private var activeTimerRingtone: android.media.Ringtone? = null

    // Local Stopwatch state
    private var localSwAccumulatedMs: Long = 0L
    private var localSwStartElapsedMs: Long = 0L
    private var localSwRunning: Boolean = false
    private var localSwLaps: List<Long> = emptyList()

    // Event Countdown persisted state
    private var eventName: String = "Target Event"
    private var eventTargetMs: Long = 0L
    private var eventStartMs: Long = 0L

    // Lux sensor state
    @Volatile
    private var currentLux: Float = 0f
    @Volatile
    private var hasLuxReading: Boolean = false
    @Volatile
    private var luxSensorAvailable: Boolean = false
    @Volatile
    private var luxSensorName: String = "Ambient Light Sensor"
    @Volatile
    private var luxSensorMaxRange: Float = 10_000f
    private var luxMaxScale: Float = 10_000f
    private var sensorManager: SensorManager? = null
    private var lightSensorListener: SensorEventListener? = null

    fun init(context: Context) {
        val ctx = context.applicationContext
        appContext = ctx
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            initialized = true
            loadPreferences(ctx)
            initLuxSensor(ctx)
            startPolling()
        }
    }

    private fun loadPreferences(ctx: Context) {
        val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        eventName = prefs.getString(KEY_EVENT_NAME, "Target Event")?.takeIf { it.isNotBlank() } ?: "Target Event"
        val now = System.currentTimeMillis()
        val savedTarget = prefs.getLong(KEY_EVENT_TARGET_MS, 0L)
        val savedStart = prefs.getLong(KEY_EVENT_START_MS, 0L)
        if (savedTarget <= 0L) {
            val cal = Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.DAY_OF_YEAR, 14)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            eventTargetMs = cal.timeInMillis
            eventStartMs = now
            prefs.edit()
                .putString(KEY_EVENT_NAME, eventName)
                .putLong(KEY_EVENT_TARGET_MS, eventTargetMs)
                .putLong(KEY_EVENT_START_MS, eventStartMs)
                .apply()
        } else {
            eventTargetMs = savedTarget
            eventStartMs = if (savedStart > 0L) savedStart else (savedTarget - 14L * 86_400_000L)
        }
        configuredTimerDurationMs = prefs.getLong(KEY_TIMER_DURATION_MS, 300_000L).coerceIn(10_000L, 36_000_000L)
        localTimerTotalMs = configuredTimerDurationMs
        localTimerRemainingMs = localTimerTotalMs
        configuredScaleMbps = prefs.getInt(KEY_NET_SCALE_MBPS, 0)
        luxMaxScale = prefs.getFloat(KEY_LUX_MAX_SCALE, 10_000f).coerceIn(500f, 100_000f)
    }

    private fun initLuxSensor(ctx: Context) {
        try {
            val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            sensorManager = sm
            val lightSensor = sm?.getDefaultSensor(Sensor.TYPE_LIGHT)
            if (lightSensor != null) {
                luxSensorAvailable = true
                luxSensorName = lightSensor.name ?: "Ambient Light Sensor"
                luxSensorMaxRange = lightSensor.maximumRange.coerceAtLeast(100f)
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        val value = event?.values?.firstOrNull() ?: return
                        if (!value.isNaN() && value >= 0f) {
                            currentLux = value
                            hasLuxReading = true
                            val currentSnap = _snapshot.value
                            _snapshot.value = currentSnap.copy(lux = buildLuxState(ctx))
                        }
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }
                lightSensorListener = listener
                sm.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_UI)
            } else {
                luxSensorAvailable = false
            }
        } catch (_: Throwable) {
            luxSensorAvailable = false
        }
    }

    private fun startPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = scope.launch {
            while (isActive) {
                refreshTelemetryNow()
                val intervalMs = when {
                    _activeMicroWindow.value != null -> 500L
                    localTimerRunning || localSwRunning ||
                        externalTimerTicker?.isRunning == true ||
                        externalStopwatchTicker?.isRunning == true -> 500L
                    else -> 1200L
                }
                delay(intervalMs)
            }
        }
    }

    fun refreshTelemetryNow() {
        val ctx = appContext ?: return
        try {
            val cpu = sampleCpuState()
            val ram = sampleRamState(ctx)
            val net = sampleNetworkSpeed()
            val timer = buildTimerState()
            val stopwatch = buildStopwatchState()
            val countdown = buildEventCountdownState()
            val signal = sampleSignalQuality(ctx)
            val downloads = sampleDownloadProgress(ctx, net.rxBytesPerSec)
            val lux = buildLuxState(ctx)

            _snapshot.value = ComplicationSnapshot(
                cpu = cpu,
                ram = ram,
                netSpeed = net,
                timer = timer,
                stopwatch = stopwatch,
                countdown = countdown,
                signal = signal,
                downloads = downloads,
                lux = lux
            )
        } catch (_: Throwable) {
        }
    }

    // =========================================================================
    // 1. Hardware Monitors (CPU & RAM) + Color-Coded Load Gradient
    // =========================================================================

    fun loadGradientColor(fraction: Float): Color {
        val f = fraction.coerceIn(0f, 1f)
        val green = Color(0xFF4CAF50)
        val amber = Color(0xFFFF9800)
        val red = Color(0xFFF44336)
        return if (f <= 0.5f) {
            val t = f / 0.5f
            androidx.compose.ui.graphics.lerp(green, amber, t)
        } else {
            val t = (f - 0.5f) / 0.5f
            androidx.compose.ui.graphics.lerp(amber, red, t)
        }
    }

    private fun sampleCpuState(): CpuMonitorState {
        val cores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)

        // Source A: /proc/stat delta (accurate system CPU load on pre-Oreo / permissive kernels)
        val procStatPct = readProcStatCpuPct()

        // Source B: cpufreq scaling_cur_freq / cpuinfo_max_freq across online cores (works on unrooted modern Android)
        val freqStats = readCpuFreqUtilization(cores)

        // Source C: /proc/loadavg
        val loadAvgPct = readLoadAvgPct(cores)

        // Source D: Process elapsed CPU time delta
        val processPct = readProcessCpuFallbackPct(cores)

        val resolvedPct = when {
            procStatPct != null -> procStatPct
            freqStats.first != null -> freqStats.first!!
            loadAvgPct != null -> loadAvgPct
            else -> processPct
        }.coerceIn(0, 100)

        val sourceLabel = when {
            procStatPct != null -> "/proc/stat"
            freqStats.first != null -> "CPU Freq Scaling"
            loadAvgPct != null -> "Kernel LoadAvg"
            else -> "Process CPU"
        }

        return CpuMonitorState(
            loadPct = resolvedPct,
            progress = (resolvedPct / 100f).coerceIn(0f, 1f),
            coreCount = cores,
            avgFreqMhz = freqStats.second,
            maxFreqMhz = freqStats.third,
            sourceLabel = sourceLabel
        )
    }

    private fun readProcStatCpuPct(): Int? {
        return try {
            val line = File("/proc/stat").bufferedReader().use { it.readLine() } ?: return null
            if (!line.startsWith("cpu ")) return null
            val parts = line.trim().split(Regex("\\s+")).drop(1).mapNotNull { it.toLongOrNull() }
            if (parts.size < 4) return null
            val idle = parts[3] + (parts.getOrNull(4) ?: 0L)
            val total = parts.sum()
            val prevT = prevProcTotal
            val prevI = prevProcIdle
            prevProcTotal = total
            prevProcIdle = idle
            if (prevT <= 0L) return null
            val totalDelta = (total - prevT).coerceAtLeast(1L)
            val idleDelta = (idle - prevI).coerceIn(0L, totalDelta)
            (((totalDelta - idleDelta).toDouble() / totalDelta.toDouble()) * 100.0).roundToInt().coerceIn(0, 100)
        } catch (_: Throwable) {
            null
        }
    }

    private fun readCpuFreqUtilization(cores: Int): Triple<Int?, Int, Int> {
        return try {
            var sumRatio = 0.0
            var sumCurKhz = 0L
            var sumMaxKhz = 0L
            var measuredCores = 0
            for (i in 0 until cores) {
                val baseDir = "/sys/devices/system/cpu/cpu$i/cpufreq"
                val curFile = File("$baseDir/scaling_cur_freq")
                val maxFile = File("$baseDir/cpuinfo_max_freq").takeIf { it.canRead() }
                    ?: File("$baseDir/scaling_max_freq")
                if (!curFile.canRead() || !maxFile.canRead()) continue
                val curKhz = curFile.readText().trim().toLongOrNull() ?: continue
                val maxKhz = maxFile.readText().trim().toLongOrNull() ?: continue
                if (maxKhz <= 0L) continue
                val ratio = (curKhz.toDouble() / maxKhz.toDouble()).coerceIn(0.0, 1.0)
                sumRatio += ratio
                sumCurKhz += curKhz
                sumMaxKhz += maxKhz
                measuredCores++
            }
            if (measuredCores > 0) {
                val avgPct = ((sumRatio / measuredCores) * 100.0).roundToInt().coerceIn(1, 100)
                val avgCurMhz = ((sumCurKhz / measuredCores) / 1000L).toInt()
                val avgMaxMhz = ((sumMaxKhz / measuredCores) / 1000L).toInt()
                Triple(avgPct, avgCurMhz, avgMaxMhz)
            } else {
                Triple(null, 0, 0)
            }
        } catch (_: Throwable) {
            Triple(null, 0, 0)
        }
    }

    private fun readLoadAvgPct(cores: Int): Int? {
        return try {
            val line = File("/proc/loadavg").readText().trim()
            val oneMin = line.split(Regex("\\s+")).firstOrNull()?.toDoubleOrNull() ?: return null
            ((oneMin / cores.toDouble()) * 100.0).roundToInt().coerceIn(1, 100)
        } catch (_: Throwable) {
            null
        }
    }

    private fun readProcessCpuFallbackPct(cores: Int): Int {
        val nowWall = SystemClock.elapsedRealtime()
        val nowCpu = Process.getElapsedCpuTime()
        val prevWall = prevAppWallMs
        val prevCpu = prevAppCpuMs
        prevAppWallMs = nowWall
        prevAppCpuMs = nowCpu
        if (prevWall <= 0L || nowWall <= prevWall) return 12
        val wallDelta = (nowWall - prevWall).coerceAtLeast(1L)
        val cpuDelta = (nowCpu - prevCpu).coerceAtLeast(0L)
        return ((cpuDelta.toDouble() / (wallDelta.toDouble() * cores)) * 100.0).roundToInt().coerceIn(2, 100)
    }

    private fun sampleRamState(ctx: Context): RamMonitorState {
        return try {
            val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return RamMonitorState()
            val info = ActivityManager.MemoryInfo()
            am.getMemoryInfo(info)
            val total = info.totalMem.coerceAtLeast(1L)
            val avail = info.availMem.coerceIn(0L, total)
            val used = (total - avail).coerceAtLeast(0L)
            val fraction = (used.toDouble() / total.toDouble()).toFloat().coerceIn(0f, 1f)
            val pct = (fraction * 100f).roundToInt().coerceIn(0, 100)
            val bytesPerGb = 1024.0 * 1024.0 * 1024.0
            RamMonitorState(
                usagePct = pct,
                progress = fraction,
                usedGb = (used / bytesPerGb).toFloat(),
                totalGb = (total / bytesPerGb).toFloat(),
                availableGb = (avail / bytesPerGb).toFloat(),
                lowMemory = info.lowMemory
            )
        } catch (_: Throwable) {
            RamMonitorState()
        }
    }

    // =========================================================================
    // 2. Real Network Speed (TrafficStats)
    // =========================================================================

    private fun sampleNetworkSpeed(): NetworkSpeedState {
        val nowMs = SystemClock.elapsedRealtime()
        val rx = TrafficStats.getTotalRxBytes()
        val tx = TrafficStats.getTotalTxBytes()
        if (rx == TrafficStats.UNSUPPORTED.toLong() || tx == TrafficStats.UNSUPPORTED.toLong()) {
            return NetworkSpeedState(compactSubline = "N/A", detailedSubtitle = "Unsupported")
        }

        val prevTime = prevTrafficTimeMs
        val prevRx = prevRxBytes
        val prevTx = prevTxBytes
        prevTrafficTimeMs = nowMs
        prevRxBytes = rx
        prevTxBytes = tx

        if (prevTime <= 0L || nowMs <= prevTime || prevRx < 0L || prevTx < 0L) {
            return _snapshot.value.netSpeed
        }

        val dtMs = (nowMs - prevTime).coerceAtLeast(100L)
        val rxBps = (((rx - prevRx).coerceAtLeast(0L) * 1000.0) / dtMs).toLong()
        val txBps = (((tx - prevTx).coerceAtLeast(0L) * 1000.0) / dtMs).toLong()
        val totalBps = rxBps + txBps

        if (totalBps > observedPeakBps) {
            observedPeakBps = totalBps
        } else {
            observedPeakBps = maxOf(1_000_000L, (observedPeakBps * 0.985).toLong())
        }

        val peakRefBps = if (configuredScaleMbps > 0) {
            (configuredScaleMbps * 125_000L).coerceAtLeast(125_000L)
        } else {
            observedPeakBps.coerceAtLeast(1_000_000L)
        }

        val progress = if (totalBps <= 0L) {
            0f
        } else if (configuredScaleMbps > 0) {
            (totalBps.toFloat() / peakRefBps.toFloat()).coerceIn(0.03f, 1f)
        } else {
            val logVal = log10((totalBps / 1024.0) + 1.0)
            val logPeak = log10((peakRefBps / 1024.0) + 1.0).coerceAtLeast(1.0)
            (logVal / logPeak).toFloat().coerceIn(0.03f, 1f)
        }

        return NetworkSpeedState(
            rxBytesPerSec = rxBps,
            txBytesPerSec = txBps,
            totalBytesPerSec = totalBps,
            peakBytesPerSec = peakRefBps,
            configuredScaleMbps = configuredScaleMbps,
            compactSubline = formatCompactSpeed(totalBps),
            detailedSubtitle = "↓${formatCompactSpeed(rxBps)} ↑${formatCompactSpeed(txBps)}",
            progress = progress
        )
    }

    fun setNetworkScaleMbps(mbps: Int) {
        configuredScaleMbps = mbps.coerceAtLeast(0)
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()
            ?.putInt(KEY_NET_SCALE_MBPS, configuredScaleMbps)
            ?.apply()
        refreshTelemetryNow()
    }

    fun resetNetworkPeak() {
        observedPeakBps = 1_000_000L
        refreshTelemetryNow()
    }

    fun formatCompactSpeed(bytesPerSec: Long): String {
        val bps = bytesPerSec.coerceAtLeast(0L)
        return when {
            bps < 1024L -> "${bps}B/s"
            bps < 1024L * 1024L -> "${bps / 1024L}K/s"
            else -> String.format(Locale.US, "%.1fM/s", bps / (1024.0 * 1024.0))
        }
    }

    // =========================================================================
    // 3 & 4. Timer & Stopwatch + External Clock App Notification Catching
    // =========================================================================

    fun updateExternalClockTickers(
        timerTicker: ExternalClockTicker?,
        stopwatchTicker: ExternalClockTicker?
    ) {
        if (timerTicker != null) {
            val liveRemaining = resolveTickerMillis(timerTicker)
            if (externalTimerKey != timerTicker.key || liveRemaining > externalTimerMaxDurationMs) {
                externalTimerKey = timerTicker.key
                externalTimerMaxDurationMs = liveRemaining.coerceAtLeast(1_000L)
            }
        } else {
            externalTimerKey = null
            externalTimerMaxDurationMs = 0L
        }
        externalTimerTicker = timerTicker
        externalStopwatchTicker = stopwatchTicker
        refreshTelemetryNow()
    }

    private fun resolveTickerMillis(ticker: ExternalClockTicker): Long {
        if (ticker.isRunning && ticker.baseWhenEpochMs > 0L) {
            val now = System.currentTimeMillis()
            return if (ticker.isCountDown) {
                (ticker.baseWhenEpochMs - now).coerceAtLeast(0L)
            } else {
                (now - ticker.baseWhenEpochMs).coerceAtLeast(0L)
            }
        }
        return (ticker.parsedDisplayMs ?: 0L).coerceAtLeast(0L)
    }

    private fun stopTimerAlert(context: Context? = appContext) {
        try {
            activeTimerRingtone?.stop()
        } catch (_: Throwable) {
        } finally {
            activeTimerRingtone = null
        }
        try {
            val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            vibrator?.cancel()
        } catch (_: Throwable) {
        }
    }

    private fun buildTimerState(): TimerComplicationState {
        val ext = externalTimerTicker
        if (ext != null) {
            val rawRem = if (ext.isFinished) 0L else resolveTickerMillis(ext)
            val extFinished = ext.isFinished || (ext.isCountDown && rawRem == 0L && (ext.isRunning || ext.dismissAction != null || ext.resetAction != null))
            val rem = if (extFinished) 0L else rawRem
            val maxDur = maxOf(externalTimerMaxDurationMs, rem, 1_000L)
            val progress = if (extFinished) 0f else (rem.toFloat() / maxDur.toFloat()).coerceIn(0f, 1f)
            val formatted = formatDurationCompact(rem)
            return TimerComplicationState(
                totalDurationMs = maxDur,
                remainingMs = rem,
                isRunning = ext.isRunning && !extFinished,
                isFinished = extFinished,
                isSyncedExternal = true,
                externalAppName = ext.appName,
                externalTitle = ext.title,
                progress = progress,
                compactSubline = formatted,
                detailedSubtitle = when {
                    extFinished -> "Finished"
                    ext.isRunning -> "${ext.appName} • $formatted"
                    else -> "Paused • $formatted"
                }
            )
        }

        val nowElapsed = SystemClock.elapsedRealtime()
        val rem = if (localTimerRunning) {
            val left = (localTimerEndElapsedMs - nowElapsed).coerceAtLeast(0L)
            if (left == 0L) {
                if (localTimerRunning && !localTimerFinishedAlerted) {
                    localTimerFinishedAlerted = true
                    try {
                        val ctx = appContext
                        if (ctx != null) {
                            stopTimerAlert(ctx)
                            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                            val ringtone = RingtoneManager.getRingtone(ctx, uri)
                            activeTimerRingtone = ringtone
                            ringtone?.play()
                            val vibrator = ctx.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator?.vibrate(android.os.VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400), -1))
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator?.vibrate(longArrayOf(0, 400, 200, 400), -1)
                            }
                        }
                    } catch (_: Throwable) {}
                }
                localTimerRunning = false
                localTimerRemainingMs = 0L
            }
            left
        } else {
            localTimerRemainingMs
        }
        val total = localTimerTotalMs.coerceAtLeast(1_000L)
        val isFinished = !localTimerRunning && rem == 0L
        val progress = (rem.toFloat() / total.toFloat()).coerceIn(0f, 1f)
        val formatted = formatDurationCompact(rem)
        return TimerComplicationState(
            totalDurationMs = total,
            remainingMs = rem,
            isRunning = localTimerRunning,
            isFinished = isFinished,
            isSyncedExternal = false,
            externalAppName = null,
            externalTitle = null,
            progress = progress,
            compactSubline = formatted,
            detailedSubtitle = when {
                localTimerRunning -> "Running • $formatted"
                isFinished -> "Finished"
                rem < total -> "Paused • $formatted"
                else -> "Ready • $formatted"
            }
        )
    }

    private fun buildStopwatchState(): StopwatchComplicationState {
        val ext = externalStopwatchTicker
        if (ext != null) {
            val elapsed = resolveTickerMillis(ext)
            val progress = ((elapsed % 60_000L).toFloat() / 60_000f).coerceIn(0f, 1f)
            val formatted = formatDurationCompact(elapsed)
            return StopwatchComplicationState(
                elapsedMs = elapsed,
                isRunning = ext.isRunning,
                isSyncedExternal = true,
                externalAppName = ext.appName,
                laps = localSwLaps,
                progress = progress,
                compactSubline = formatted,
                detailedSubtitle = if (ext.isRunning) "${ext.appName} • $formatted" else "Paused • $formatted"
            )
        }

        val elapsed = if (localSwRunning) {
            localSwAccumulatedMs + (SystemClock.elapsedRealtime() - localSwStartElapsedMs).coerceAtLeast(0L)
        } else {
            localSwAccumulatedMs
        }
        val progress = if (elapsed == 0L) 0f else ((elapsed % 60_000L).toFloat() / 60_000f).coerceIn(0.01f, 1f)
        val formatted = formatDurationCompact(elapsed)
        return StopwatchComplicationState(
            elapsedMs = elapsed,
            isRunning = localSwRunning,
            isSyncedExternal = false,
            externalAppName = null,
            laps = localSwLaps,
            progress = progress,
            compactSubline = formatted,
            detailedSubtitle = if (localSwRunning) "Running • $formatted" else if (elapsed > 0L) "Paused • $formatted" else "Ready"
        )
    }

    fun isTimerFinished(): Boolean {
        val ext = externalTimerTicker
        if (ext != null) {
            val rawRem = if (ext.isFinished) 0L else resolveTickerMillis(ext)
            return ext.isFinished || (ext.isCountDown && rawRem == 0L && (ext.isRunning || ext.dismissAction != null || ext.resetAction != null))
        }
        return _snapshot.value.timer.isFinished || (!localTimerRunning && localTimerRemainingMs <= 0L)
    }

    fun dismissFinishedTimer(context: Context) {
        stopTimerAlert(context)
        val ext = externalTimerTicker
        if (ext != null) {
            val action = ext.dismissAction ?: ext.resetAction ?: ext.toggleAction
            if (action != null) {
                try {
                    action.send()
                } catch (_: Throwable) {}
            }
            externalTimerTicker = null
            externalTimerKey = null
            externalTimerMaxDurationMs = 0L
        }
        localTimerRunning = false
        localTimerFinishedAlerted = false
        localTimerTotalMs = configuredTimerDurationMs
        localTimerRemainingMs = configuredTimerDurationMs
        refreshTelemetryNow()
    }

    fun addOneMinuteToTimer(context: Context) {
        stopTimerAlert(context)
        val ext = externalTimerTicker
        if (ext != null) {
            if (ext.plusOneMinAction != null) {
                try {
                    ext.plusOneMinAction.send()
                    externalTimerMaxDurationMs = maxOf(externalTimerMaxDurationMs, 60_000L)
                    externalTimerTicker = ext.copy(
                        isFinished = false,
                        isRunning = true,
                        isCountDown = true,
                        baseWhenEpochMs = System.currentTimeMillis() + 60_000L
                    )
                    refreshTelemetryNow()
                    return
                } catch (_: Throwable) {}
            }
            // Fallback if OEM clock notification has no +1m action: dismiss external alert and run local 1m timer
            val dismissPending = ext.dismissAction ?: ext.resetAction
            if (dismissPending != null) {
                try {
                    dismissPending.send()
                } catch (_: Throwable) {}
            }
            externalTimerTicker = null
            externalTimerKey = null
            externalTimerMaxDurationMs = 0L
        }

        localTimerFinishedAlerted = false
        if (localTimerRunning) {
            localTimerEndElapsedMs = maxOf(SystemClock.elapsedRealtime() + 1_000L, localTimerEndElapsedMs + 60_000L)
            localTimerTotalMs = maxOf(localTimerTotalMs, localTimerEndElapsedMs - SystemClock.elapsedRealtime())
        } else {
            val baseRem = if (localTimerRemainingMs <= 0L) 60_000L else localTimerRemainingMs + 60_000L
            localTimerRemainingMs = baseRem.coerceIn(10_000L, 36_000_000L)
            localTimerTotalMs = if (baseRem == 60_000L) 60_000L else maxOf(localTimerTotalMs, localTimerRemainingMs)
            localTimerEndElapsedMs = SystemClock.elapsedRealtime() + localTimerRemainingMs
            localTimerRunning = true
        }
        refreshTelemetryNow()
    }

    fun toggleTimer(context: Context) {
        if (isTimerFinished()) {
            dismissFinishedTimer(context)
            return
        }
        val ext = externalTimerTicker
        if (ext != null) {
            if (ext.toggleAction != null) {
                try {
                    ext.toggleAction.send()
                    return
                } catch (_: Throwable) {}
            }
            if (ext.contentIntent != null) {
                try {
                    ext.contentIntent.send()
                    closeMicroWindow()
                    QuickPanelAccessibilityService.hideCustomPanel()
                    QuickSettingsOverlayService.instance?.hideOverlayPanelSmoothly()
                    return
                } catch (_: Throwable) {}
            }
        }
        if (localTimerRunning) {
            localTimerRemainingMs = (localTimerEndElapsedMs - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
            localTimerRunning = false
        } else {
            if (localTimerRemainingMs <= 0L) {
                localTimerRemainingMs = localTimerTotalMs
            }
            localTimerFinishedAlerted = false
            localTimerEndElapsedMs = SystemClock.elapsedRealtime() + localTimerRemainingMs
            localTimerRunning = true
        }
        refreshTelemetryNow()
    }

    fun resetTimer() {
        stopTimerAlert()
        val ext = externalTimerTicker
        if (ext?.resetAction != null) {
            try {
                ext.resetAction.send()
                return
            } catch (_: Throwable) {}
        }
        localTimerRunning = false
        localTimerFinishedAlerted = false
        localTimerTotalMs = configuredTimerDurationMs
        localTimerRemainingMs = configuredTimerDurationMs
        refreshTelemetryNow()
    }

    fun setTimerDuration(durationMs: Long) {
        stopTimerAlert()
        val clamped = durationMs.coerceIn(10_000L, 36_000_000L)
        configuredTimerDurationMs = clamped
        localTimerTotalMs = clamped
        localTimerRemainingMs = clamped
        localTimerRunning = false
        localTimerFinishedAlerted = false
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()
            ?.putLong(KEY_TIMER_DURATION_MS, clamped)
            ?.apply()
        refreshTelemetryNow()
    }

    fun adjustTimerSeconds(deltaSeconds: Int) {
        val ext = externalTimerTicker
        if (ext != null && deltaSeconds == 60 && ext.plusOneMinAction != null) {
            try {
                ext.plusOneMinAction.send()
                return
            } catch (_: Throwable) {}
        }
        val deltaMs = deltaSeconds * 1000L
        if (localTimerRunning) {
            localTimerEndElapsedMs = maxOf(SystemClock.elapsedRealtime() + 1000L, localTimerEndElapsedMs + deltaMs)
            localTimerTotalMs = maxOf(localTimerTotalMs, localTimerEndElapsedMs - SystemClock.elapsedRealtime())
        } else {
            localTimerRemainingMs = (localTimerRemainingMs + deltaMs).coerceIn(10_000L, 36_000_000L)
            localTimerTotalMs = maxOf(localTimerTotalMs, localTimerRemainingMs)
            if (deltaSeconds < 0 && localTimerRemainingMs < localTimerTotalMs) {
                localTimerTotalMs = localTimerRemainingMs
            }
        }
        refreshTelemetryNow()
    }

    fun toggleStopwatch() {
        val ext = externalStopwatchTicker
        if (ext != null) {
            if (ext.toggleAction != null) {
                try {
                    ext.toggleAction.send()
                    return
                } catch (_: Throwable) {}
            }
            if (ext.contentIntent != null) {
                try {
                    ext.contentIntent.send()
                    closeMicroWindow()
                    QuickPanelAccessibilityService.hideCustomPanel()
                    QuickSettingsOverlayService.instance?.hideOverlayPanelSmoothly()
                    return
                } catch (_: Throwable) {}
            }
        }
        if (localSwRunning) {
            localSwAccumulatedMs += (SystemClock.elapsedRealtime() - localSwStartElapsedMs).coerceAtLeast(0L)
            localSwRunning = false
        } else {
            localSwStartElapsedMs = SystemClock.elapsedRealtime()
            localSwRunning = true
        }
        refreshTelemetryNow()
    }

    fun lapStopwatch() {
        val ext = externalStopwatchTicker
        if (ext?.lapAction != null) {
            try {
                ext.lapAction.send()
                return
            } catch (_: Throwable) {}
        }
        val current = if (localSwRunning) {
            localSwAccumulatedMs + (SystemClock.elapsedRealtime() - localSwStartElapsedMs).coerceAtLeast(0L)
        } else {
            localSwAccumulatedMs
        }
        if (current > 0L) {
            localSwLaps = (listOf(current) + localSwLaps).take(20)
            refreshTelemetryNow()
        }
    }

    fun resetStopwatch() {
        val ext = externalStopwatchTicker
        if (ext?.resetAction != null) {
            try {
                ext.resetAction.send()
                return
            } catch (_: Throwable) {}
        }
        localSwRunning = false
        localSwAccumulatedMs = 0L
        localSwLaps = emptyList()
        refreshTelemetryNow()
    }

    fun openExternalClockApp(context: Context, isTimer: Boolean) {
        val ticker = if (isTimer) externalTimerTicker else externalStopwatchTicker
        if (ticker?.contentIntent != null) {
            try {
                ticker.contentIntent.send()
                closeMicroWindow()
                QuickPanelAccessibilityService.hideCustomPanel()
                QuickSettingsOverlayService.instance?.hideOverlayPanelSmoothly()
                return
            } catch (_: Throwable) {}
        }
        val intent = Intent(
            if (isTimer) "android.intent.action.SET_TIMER" else "android.intent.action.SHOW_ALARMS"
        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        try {
            context.startActivity(intent)
            closeMicroWindow()
            QuickPanelAccessibilityService.hideCustomPanel()
            QuickSettingsOverlayService.instance?.hideOverlayPanelSmoothly()
        } catch (_: Throwable) {
            val clockPkgs = listOf(
                "com.google.android.deskclock",
                "com.android.deskclock",
                "com.sec.android.app.clockpackage"
            )
            for (pkg in clockPkgs) {
                val launch = context.packageManager.getLaunchIntentForPackage(pkg)
                if (launch != null) {
                    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launch)
                    closeMicroWindow()
                    QuickPanelAccessibilityService.hideCustomPanel()
                    QuickSettingsOverlayService.instance?.hideOverlayPanelSmoothly()
                    return
                }
            }
        }
    }

    fun formatDurationCompact(ms: Long): String {
        val totalSec = (ms.coerceAtLeast(0L) / 1000L)
        val hours = totalSec / 3600L
        val mins = (totalSec % 3600L) / 60L
        val secs = totalSec % 60L
        return if (hours > 0L) {
            String.format(Locale.US, "%d:%02d", hours, mins)
        } else {
            String.format(Locale.US, "%02d:%02d", mins, secs)
        }
    }

    fun formatDurationPrecise(ms: Long, showTenths: Boolean = false): String {
        val clamped = ms.coerceAtLeast(0L)
        val totalSec = clamped / 1000L
        val hours = totalSec / 3600L
        val mins = (totalSec % 3600L) / 60L
        val secs = totalSec % 60L
        val tenths = (clamped % 1000L) / 100L
        return when {
            hours > 0L -> String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs)
            showTenths -> String.format(Locale.US, "%02d:%02d.%d", mins, secs, tenths)
            else -> String.format(Locale.US, "%02d:%02d", mins, secs)
        }
    }

    // =========================================================================
    // 5. Event Countdown
    // =========================================================================

    private fun buildEventCountdownState(): EventCountdownState {
        val now = System.currentTimeMillis()
        val diff = eventTargetMs - now
        val isPast = diff <= 0L
        val absDiff = abs(diff)
        val days = diff.coerceAtLeast(0L) / 86_400_000L
        val hours = (diff.coerceAtLeast(0L) % 86_400_000L) / 3_600_000L
        val mins = (diff.coerceAtLeast(0L) % 3_600_000L) / 60_000L

        val totalSpan = (eventTargetMs - eventStartMs).coerceAtLeast(86_400_000L)
        val progress = if (isPast) {
            1f
        } else {
            (diff.toDouble() / totalSpan.toDouble()).toFloat().coerceIn(0.02f, 1f)
        }

        val compact = when {
            isPast && absDiff < 86_400_000L -> "Today"
            isPast -> "Done"
            days >= 1L -> "${days}d"
            hours >= 1L -> "${hours}h"
            else -> "${mins}m"
        }

        return EventCountdownState(
            eventName = eventName,
            targetEpochMs = eventTargetMs,
            startEpochMs = eventStartMs,
            daysLeft = days,
            hoursLeft = hours,
            minutesLeft = mins,
            isPast = isPast,
            progress = progress,
            compactSubline = compact,
            detailedSubtitle = "$eventName • $compact"
        )
    }

    fun updateEventCountdown(name: String, targetEpochMs: Long) {
        val cleanedName = name.trim().ifBlank { "Target Event" }
        val now = System.currentTimeMillis()
        eventName = cleanedName
        eventTargetMs = targetEpochMs
        eventStartMs = now
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()
            ?.putString(KEY_EVENT_NAME, eventName)
            ?.putLong(KEY_EVENT_TARGET_MS, eventTargetMs)
            ?.putLong(KEY_EVENT_START_MS, eventStartMs)
            ?.apply()
        refreshTelemetryNow()
    }

    // =========================================================================
    // 6. Signal Quality Index (Wi-Fi / Cellular)
    // =========================================================================

    @Suppress("DEPRECATION")
    private fun sampleSignalQuality(ctx: Context): SignalQualityState {
        return try {
            val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = cm?.activeNetwork
            val caps = if (network != null) cm.getNetworkCapabilities(network) else null

            if (caps == null) {
                return SignalQualityState(
                    isConnected = false,
                    transportLabel = "Offline",
                    networkTypeShort = "Offline",
                    strengthPct = 0,
                    progress = 0f,
                    qualityLabel = "Disconnected",
                    detailedSubtitle = "No Network"
                )
            }

            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                val wm = ctx.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                val wifiInfo: WifiInfo? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    (caps.transportInfo as? WifiInfo) ?: wm?.connectionInfo
                } else {
                    wm?.connectionInfo
                }
                val rssi = wifiInfo?.rssi?.takeIf { it > -127 && it < 0 } ?: -62
                val pct = ((rssi - (-100)).toFloat() / ((-45) - (-100)).toFloat() * 100f)
                    .roundToInt()
                    .coerceIn(0, 100)
                val standard = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    when (wifiInfo?.wifiStandard) {
                        ScanResult.WIFI_STANDARD_11BE -> "Wi-Fi 7"
                        ScanResult.WIFI_STANDARD_11AX -> "Wi-Fi 6"
                        ScanResult.WIFI_STANDARD_11AC -> "Wi-Fi 5"
                        ScanResult.WIFI_STANDARD_11N -> "Wi-Fi 4"
                        else -> "Wi-Fi"
                    }
                } else {
                    "Wi-Fi"
                }
                val linkMbps = wifiInfo?.linkSpeed?.takeIf { it > 0 }
                val freqMhz = wifiInfo?.frequency?.takeIf { it > 0 }
                val quality = when {
                    pct >= 80 -> "Excellent"
                    pct >= 60 -> "Good"
                    pct >= 35 -> "Fair"
                    else -> "Weak"
                }
                return SignalQualityState(
                    isConnected = true,
                    transportLabel = standard,
                    networkTypeShort = standard,
                    strengthPct = pct,
                    progress = pct / 100f,
                    rssiDbm = rssi,
                    linkSpeedMbps = linkMbps,
                    frequencyMhz = freqMhz,
                    qualityLabel = quality,
                    detailedSubtitle = "$standard • $pct%"
                )
            }

            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                val tm = ctx.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                var pct = 75
                var dbm: Int? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    try {
                        val sig = tm?.signalStrength
                        if (sig != null) {
                            val cellStrength = sig.cellSignalStrengths.firstOrNull()
                            val rawDbm = cellStrength?.dbm
                            if (rawDbm != null && rawDbm in -140..-35) {
                                dbm = rawDbm
                                pct = ((rawDbm - (-125)).toFloat() / ((-65) - (-125)).toFloat() * 100f)
                                    .roundToInt()
                                    .coerceIn(5, 100)
                            } else {
                                pct = ((sig.level.coerceIn(0, 4) / 4f) * 100f).roundToInt()
                            }
                        }
                    } catch (_: Throwable) {}
                }
                val genLabel = try {
                    when (tm?.dataNetworkType) {
                        TelephonyManager.NETWORK_TYPE_NR -> "5G"
                        TelephonyManager.NETWORK_TYPE_LTE -> "4G LTE"
                        TelephonyManager.NETWORK_TYPE_HSPAP,
                        TelephonyManager.NETWORK_TYPE_HSPA,
                        TelephonyManager.NETWORK_TYPE_UMTS -> "3G"
                        TelephonyManager.NETWORK_TYPE_EDGE,
                        TelephonyManager.NETWORK_TYPE_GPRS -> "2G"
                        else -> "Cellular"
                    }
                } catch (_: SecurityException) {
                    if (caps.linkDownstreamBandwidthKbps >= 50_000) "5G" else "4G"
                } catch (_: Throwable) {
                    "Cellular"
                }
                val shortType = if (genLabel == "4G LTE") "LTE" else genLabel
                val downMbps = (caps.linkDownstreamBandwidthKbps / 1000).takeIf { it > 0 }
                val quality = when {
                    pct >= 80 -> "Excellent"
                    pct >= 60 -> "Good"
                    pct >= 35 -> "Fair"
                    else -> "Weak"
                }
                return SignalQualityState(
                    isConnected = true,
                    transportLabel = genLabel,
                    networkTypeShort = shortType,
                    strengthPct = pct,
                    progress = pct / 100f,
                    rssiDbm = dbm,
                    linkSpeedMbps = downMbps,
                    frequencyMhz = null,
                    qualityLabel = quality,
                    detailedSubtitle = "$shortType • $pct%"
                )
            }

            // Ethernet / VPN / Other
            val downMbps = (caps.linkDownstreamBandwidthKbps / 1000).takeIf { it > 0 }
            SignalQualityState(
                isConnected = true,
                transportLabel = "Connected",
                networkTypeShort = "Online",
                strengthPct = 100,
                progress = 1f,
                linkSpeedMbps = downMbps,
                qualityLabel = "Connected",
                detailedSubtitle = "Online • 100%"
            )
        } catch (_: Throwable) {
            SignalQualityState()
        }
    }

    // =========================================================================
    // 7. Active Download Progress (DownloadManager + NotificationListener)
    // =========================================================================

    fun updateNotificationDownloads(entries: List<ActiveDownloadEntry>) {
        notificationDownloads = entries
        refreshTelemetryNow()
    }

    private fun sampleDownloadProgress(ctx: Context, currentRxBps: Long): DownloadProgressState {
        val dmEntries = queryDownloadManagerEntries(ctx)
        val notifEntries = notificationDownloads
        val combined = (dmEntries + notifEntries).distinctBy { it.title to it.isFromDownloadManager }

        if (combined.isEmpty()) {
            prevDmDownloadedBytes = -1L
            prevDmSampleTimeMs = -1L
            return DownloadProgressState(
                entries = emptyList(),
                isDownloading = false,
                overallProgress = 0f,
                totalSpeedBytesPerSec = 0L,
                compactSubline = "Idle",
                detailedSubtitle = "No downloads"
            )
        }

        val determinate = combined.filter { !it.isIndeterminate && it.progressFraction >= 0f }
        val overallProgress = if (determinate.isNotEmpty()) {
            determinate.map { it.progressFraction }.average().toFloat().coerceIn(0f, 1f)
        } else {
            0.35f
        }

        val explicitSpeed = combined.maxOfOrNull { it.speedBytesPerSec } ?: 0L
        val effectiveSpeed = if (explicitSpeed > 0L) explicitSpeed else currentRxBps
        val pct = (overallProgress * 100f).roundToInt().coerceIn(0, 100)

        val compactSubline = when {
            effectiveSpeed >= 1024L -> formatCompactSpeed(effectiveSpeed)
            determinate.isNotEmpty() -> "$pct%"
            else -> "Active"
        }
        val firstTitle = combined.firstOrNull()?.title?.take(14) ?: "Downloading"
        val detailedSubtitle = if (effectiveSpeed >= 1024L) {
            "$pct% • ${formatCompactSpeed(effectiveSpeed)}"
        } else {
            "$firstTitle ($pct%)"
        }

        return DownloadProgressState(
            entries = combined,
            isDownloading = true,
            overallProgress = overallProgress,
            totalSpeedBytesPerSec = effectiveSpeed,
            compactSubline = compactSubline,
            detailedSubtitle = detailedSubtitle
        )
    }

    private fun queryDownloadManagerEntries(ctx: Context): List<ActiveDownloadEntry> {
        return try {
            val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager ?: return emptyList()
            val query = DownloadManager.Query().setFilterByStatus(
                DownloadManager.STATUS_RUNNING or DownloadManager.STATUS_PENDING or DownloadManager.STATUS_PAUSED
            )
            val entries = mutableListOf<ActiveDownloadEntry>()
            var totalDownloadedSum = 0L
            dm.query(query)?.use { cursor ->
                val idxId = cursor.getColumnIndex(DownloadManager.COLUMN_ID)
                val idxTitle = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
                val idxBytes = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val idxTotal = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                val idxStatus = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                while (cursor.moveToNext()) {
                    val id = if (idxId >= 0) cursor.getLong(idxId) else 0L
                    val title = (if (idxTitle >= 0) cursor.getString(idxTitle) else null)?.ifBlank { "System Download" }
                        ?: "System Download"
                    val downloaded = if (idxBytes >= 0) cursor.getLong(idxBytes).coerceAtLeast(0L) else 0L
                    val total = if (idxTotal >= 0) cursor.getLong(idxTotal) else -1L
                    val status = if (idxStatus >= 0) cursor.getInt(idxStatus) else DownloadManager.STATUS_RUNNING
                    totalDownloadedSum += downloaded
                    val isIndet = total <= 0L
                    val frac = if (!isIndet) (downloaded.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0.5f
                    val statusText = when (status) {
                        DownloadManager.STATUS_PAUSED -> "Paused"
                        DownloadManager.STATUS_PENDING -> "Queued"
                        else -> if (!isIndet) "${(frac * 100).roundToInt()}%" else "Downloading"
                    }
                    entries.add(
                        ActiveDownloadEntry(
                            id = "dm_$id",
                            title = title,
                            subtitle = statusText,
                            progressFraction = frac,
                            isIndeterminate = isIndet,
                            speedBytesPerSec = 0L,
                            speedText = "",
                            isFromDownloadManager = true
                        )
                    )
                }
            }
            val now = SystemClock.elapsedRealtime()
            if (entries.isNotEmpty() && prevDmSampleTimeMs > 0L && now > prevDmSampleTimeMs && prevDmDownloadedBytes >= 0L) {
                val dt = (now - prevDmSampleTimeMs).coerceAtLeast(100L)
                val deltaBytes = (totalDownloadedSum - prevDmDownloadedBytes).coerceAtLeast(0L)
                val bps = ((deltaBytes * 1000.0) / dt).toLong()
                if (bps > 0L) {
                    val spdText = formatCompactSpeed(bps)
                    val first = entries[0]
                    entries[0] = first.copy(speedBytesPerSec = bps, speedText = spdText)
                }
            }
            prevDmDownloadedBytes = totalDownloadedSum
            prevDmSampleTimeMs = now
            entries
        } catch (_: Throwable) {
            emptyList()
        }
    }

    // =========================================================================
    // 8. Ambient Light (Lux Meter)
    // =========================================================================

    private fun buildLuxState(ctx: Context): LuxMeterState {
        val isAutoBright = try {
            Settings.System.getInt(
                ctx.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            ) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
        } catch (_: Throwable) {
            false
        }

        if (!luxSensorAvailable) {
            return LuxMeterState(
                lux = 0f,
                hasSensor = false,
                sensorName = "No Light Sensor",
                maxRange = 0f,
                maxScaleLux = luxMaxScale,
                brightnessPct = 0,
                progress = 0f,
                conditionLabel = "Unavailable",
                compactSubline = "N/A",
                detailedSubtitle = "No Light Sensor",
                isAutoBrightnessEnabled = isAutoBright
            )
        }

        val lux = if (hasLuxReading) currentLux.coerceAtLeast(0f) else 0f
        val maxLog = log10(luxMaxScale.toDouble() + 1.0).coerceAtLeast(1.0)
        val frac = if (lux <= 0f) 0f else (log10(lux.toDouble() + 1.0) / maxLog).toFloat().coerceIn(0f, 1f)
        val pct = (frac * 100f).roundToInt().coerceIn(0, 100)

        val condition = when {
            lux < 10f -> "Dark"
            lux < 80f -> "Dim"
            lux < 400f -> "Indoor"
            lux < 2_000f -> "Bright"
            lux < 10_000f -> "Daylight"
            else -> "Sunlight"
        }

        val compact = when {
            lux < 1000f -> "${lux.roundToInt()} lx"
            else -> String.format(Locale.US, "%.1fk", lux / 1000f)
        }

        return LuxMeterState(
            lux = lux,
            hasSensor = true,
            sensorName = luxSensorName,
            maxRange = luxSensorMaxRange,
            maxScaleLux = luxMaxScale,
            brightnessPct = pct,
            progress = frac,
            conditionLabel = condition,
            compactSubline = compact,
            detailedSubtitle = "$compact • $condition",
            isAutoBrightnessEnabled = isAutoBright
        )
    }

    fun setLuxMaxScale(maxLux: Float) {
        luxMaxScale = maxLux.coerceIn(500f, 100_000f)
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()
            ?.putFloat(KEY_LUX_MAX_SCALE, luxMaxScale)
            ?.apply()
        refreshTelemetryNow()
    }

    // =========================================================================
    // Micro Window Control
    // =========================================================================

    fun opensMicroWindowOnLongClick(tileId: String): Boolean {
        return tileId in setOf(
            "comp_cpu",
            "comp_ram",
            "comp_net_speed",
            "comp_timer",
            "comp_stopwatch",
            "comp_media_progress",
            "comp_countdown",
            "comp_signal",
            "comp_download",
            "comp_lux",
            "comp_brightness",
            "comp_volume"
        )
    }

    fun openMicroWindowForTile(tileId: String): Boolean {
        val type = when (tileId) {
            "comp_cpu" -> ComplicationMicroWindowType.CPU_MONITOR
            "comp_ram" -> ComplicationMicroWindowType.RAM_MONITOR
            "comp_net_speed" -> ComplicationMicroWindowType.NETWORK_SPEED
            "comp_timer" -> ComplicationMicroWindowType.TIMER
            "comp_stopwatch" -> ComplicationMicroWindowType.STOPWATCH
            "comp_media_progress" -> ComplicationMicroWindowType.MEDIA_PROGRESS
            "comp_countdown" -> ComplicationMicroWindowType.EVENT_COUNTDOWN
            "comp_signal" -> ComplicationMicroWindowType.SIGNAL_QUALITY
            "comp_download" -> ComplicationMicroWindowType.DOWNLOAD_PROGRESS
            "comp_lux" -> ComplicationMicroWindowType.LUX_METER
            "comp_brightness" -> ComplicationMicroWindowType.BRIGHTNESS_SLIDER
            "comp_volume" -> ComplicationMicroWindowType.VOLUME_SLIDER
            else -> null
        } ?: return false
        _activeMicroWindow.value = type
        refreshTelemetryNow()
        return true
    }

    fun openMicroWindow(type: ComplicationMicroWindowType) {
        _activeMicroWindow.value = type
        refreshTelemetryNow()
    }

    fun closeMicroWindow() {
        _activeMicroWindow.value = null
    }

    fun getActiveMediaState(): MediaState? = MediaSessionManagerHelper.mediaState.value
}
