package com.example.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaRecorder
import android.media.MediaScannerConnection
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.PanelPreferences
import com.example.data.ScreenRecordAudioSource
import com.example.data.ScreenRecordResolution
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScreenRecordService : Service() {

    companion object {
        private const val TAG = "ScreenRecordService"
        const val CHANNEL_ID = "screen_record_channel"
        const val SAVED_CHANNEL_ID = "screen_record_saved_channel"
        const val NOTIFICATION_ID = 2002
        const val SAVED_NOTIFICATION_ID = 2003

        const val ACTION_START = "com.example.service.action.START_RECORDING"
        const val ACTION_STOP = "com.example.service.action.STOP_RECORDING"
        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"

        private val _isRecording = MutableStateFlow(false)
        val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

        private val _recordingDurationSeconds = MutableStateFlow(0)
        val recordingDurationSeconds: StateFlow<Int> = _recordingDurationSeconds.asStateFlow()

        private val _lastSavedFilePath = MutableStateFlow<String?>(null)
        val lastSavedFilePath: StateFlow<String?> = _lastSavedFilePath.asStateFlow()

        fun startRecording(context: Context, resultCode: Int, data: Intent) {
            val intent = Intent(context, ScreenRecordService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_RESULT_CODE, resultCode)
                putExtra(EXTRA_RESULT_DATA, data)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopRecording(context: Context) {
            val intent = Intent(context, ScreenRecordService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.w(TAG, "startService(ACTION_STOP) failed, trying startForegroundService: ${e.message}")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        context.startForegroundService(intent)
                    } catch (_: Exception) {}
                }
            }
        }
    }

    private data class VideoEncoderConfig(
        val width: Int,
        val height: Int,
        val fps: Int,
        val bitrate: Int
    )

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null

    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null

    private var outputMediaUri: Uri? = null
    private var outputPfd: ParcelFileDescriptor? = null
    private var outputDisplayName: String? = null
    private var outputFile: File? = null

    private var initialShowTouchesState: Int = 0
    private var didModifyShowTouches: Boolean = false
    private var isSessionActive: Boolean = false

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        PanelPreferences.init(this)
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopScreenRecording(showResultToast = true)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                if (isSessionActive) {
                    stopScreenRecording(showResultToast = false)
                }

                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
                val resultData = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(EXTRA_RESULT_DATA) as? Intent
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error unparceling EXTRA_RESULT_DATA: ${e.message}")
                    null
                }

                if (resultCode != 0 && resultData != null) {
                    startForegroundInternal()
                    val started = startScreenRecording(resultCode, resultData)
                    if (!started) {
                        stopSelf()
                        return START_NOT_STICKY
                    }
                } else {
                    Log.e(TAG, "Missing MediaProjection resultCode or data intent")
                    stopSelf()
                    return START_NOT_STICKY
                }
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java) ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screen Recording",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live screen recording controls and elapsed time"
                setShowBadge(false)
            }
            nm.createNotificationChannel(channel)

            val savedChannel = NotificationChannel(
                SAVED_CHANNEL_ID,
                "Saved Screen Recordings",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts when a screen recording is saved"
            }
            nm.createNotificationChannel(savedChannel)
        }
    }

    private fun startForegroundInternal() {
        val notification = buildRecordingNotification(0)
        val hasMicPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var serviceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && hasMicPermission) {
                val audioSource = PanelPreferences.screenRecordAudioSource.value
                if (audioSource != ScreenRecordAudioSource.MUTE) {
                    serviceType = serviceType or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                }
            }
            try {
                startForeground(NOTIFICATION_ID, notification, serviceType)
            } catch (e: Exception) {
                Log.w(TAG, "startForeground with types failed: ${e.message}")
                try {
                    startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
                } catch (e2: Exception) {
                    Log.w(TAG, "startForeground fallback mediaProjection failed: ${e2.message}")
                    startForeground(NOTIFICATION_ID, notification)
                }
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildRecordingNotification(durationSeconds: Int): Notification {
        val minutes = durationSeconds / 60
        val seconds = durationSeconds % 60
        val timeString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

        val stopIntent = Intent(this, ScreenRecordService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            201,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            202,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Recording Screen • $timeString")
            .setContentText("Tap Stop when finished or pull down Quick Settings")
            .setSmallIcon(android.R.drawable.presence_video_online)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Stop Recording", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(durationSeconds: Int) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.notify(NOTIFICATION_ID, buildRecordingNotification(durationSeconds))
    }

    private fun startScreenRecording(resultCode: Int, resultData: Intent): Boolean {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(displayMetrics)

        val screenWidth = displayMetrics.widthPixels.coerceAtLeast(320)
        val screenHeight = displayMetrics.heightPixels.coerceAtLeast(480)
        val densityDpi = displayMetrics.densityDpi.coerceAtLeast(160)

        val resolutionPref = PanelPreferences.screenRecordResolution.value
        val fpsPref = PanelPreferences.screenRecordFps.value
        val audioSourcePref = PanelPreferences.screenRecordAudioSource.value
        val showTouches = PanelPreferences.screenRecordShowTouches.value

        val encoderConfig = resolveSupportedVideoConfig(
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            resolutionPref = resolutionPref,
            requestedFps = fpsPref.fps
        )

        val hasMicPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (showTouches) {
            try {
                initialShowTouchesState = Settings.System.getInt(contentResolver, "show_touches", 0)
                Settings.System.putInt(contentResolver, "show_touches", 1)
                didModifyShowTouches = true
            } catch (e: Exception) {
                Log.w(TAG, "Could not enable show_touches: ${e.message}")
            }
        }

        if (!prepareOutputDestination()) {
            showToast("Failed to create recording output")
            cleanupFailedStart()
            return false
        }

        isSessionActive = true

        var recorderSuccess = false
        var audioEnabled = hasMicPermission && (audioSourcePref != ScreenRecordAudioSource.MUTE)

        if (audioEnabled) {
            recorderSuccess = trySetupAndPrepareRecorder(
                config = encoderConfig,
                includeAudio = true
            )
            if (!recorderSuccess) {
                Log.w(TAG, "Audio recorder setup failed, falling back to video-only recording")
                audioEnabled = false
                // Reopen MediaStore FD cleanly before retrying video-only
                reopenOutputDescriptorIfNeeded()
            }
        }

        if (!recorderSuccess) {
            recorderSuccess = trySetupAndPrepareRecorder(
                config = encoderConfig,
                includeAudio = false
            )
        }

        if (!recorderSuccess || mediaRecorder == null) {
            Log.e(TAG, "Failed to prepare MediaRecorder")
            showToast("Screen recorder preparation failed")
            stopScreenRecording(showResultToast = false)
            return false
        }

        try {
            mediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, resultData)
        } catch (e: Exception) {
            Log.e(TAG, "getMediaProjection failed: ${e.message}", e)
        }

        if (mediaProjection == null) {
            Log.e(TAG, "Failed to get MediaProjection instance")
            showToast("Media projection permission error")
            stopScreenRecording(showResultToast = false)
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                mediaProjection?.registerCallback(object : MediaProjection.Callback() {
                    override fun onStop() {
                        if (isSessionActive) {
                            stopScreenRecording(showResultToast = true)
                            stopSelf()
                        }
                    }
                }, mainHandler)
            } catch (e: Exception) {
                Log.w(TAG, "registerCallback failed: ${e.message}")
            }
        }

        try {
            virtualDisplay = mediaProjection?.createVirtualDisplay(
                "QuickPanelScreenRecorder",
                encoderConfig.width,
                encoderConfig.height,
                densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder!!.surface,
                null,
                null
            )
        } catch (e: Exception) {
            Log.e(TAG, "createVirtualDisplay failed: ${e.message}", e)
            showToast("Virtual display error: ${e.localizedMessage}")
            stopScreenRecording(showResultToast = false)
            return false
        }

        if (virtualDisplay == null) {
            Log.e(TAG, "createVirtualDisplay returned null")
            showToast("Could not create screen capture display")
            stopScreenRecording(showResultToast = false)
            return false
        }

        return try {
            mediaRecorder!!.start()
            _isRecording.value = true
            _recordingDurationSeconds.value = 0

            startTimer()
            val audioMsg = if (audioEnabled) "with Audio" else "Video only"
            showToast("Screen recording started ($audioMsg, ${encoderConfig.fps}fps)")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting MediaRecorder: ${e.message}", e)
            showToast("Recording start failed: ${e.localizedMessage}")
            stopScreenRecording(showResultToast = false)
            false
        }
    }

    private fun trySetupAndPrepareRecorder(
        config: VideoEncoderConfig,
        includeAudio: Boolean
    ): Boolean {
        try {
            mediaRecorder?.reset()
            mediaRecorder?.release()
        } catch (_: Exception) {}

        return try {
            @Suppress("DEPRECATION")
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                MediaRecorder()
            }
            mediaRecorder = recorder

            if (includeAudio) {
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            }

            recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

            if (includeAudio) {
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                recorder.setAudioEncodingBitRate(128_000)
                recorder.setAudioSamplingRate(44_100)
            }

            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            recorder.setVideoSize(config.width, config.height)
            recorder.setVideoFrameRate(config.fps)
            recorder.setVideoEncodingBitRate(config.bitrate)

            val pfd = outputPfd
            val file = outputFile
            if (pfd != null) {
                recorder.setOutputFile(pfd.fileDescriptor)
            } else if (file != null) {
                recorder.setOutputFile(file.absolutePath)
            } else {
                throw IllegalStateException("No output destination available")
            }

            recorder.prepare()
            true
        } catch (e: Exception) {
            Log.w(TAG, "trySetupAndPrepareRecorder failed (audio=$includeAudio): ${e.message}", e)
            try {
                mediaRecorder?.reset()
                mediaRecorder?.release()
            } catch (_: Exception) {}
            mediaRecorder = null
            false
        }
    }

    private fun resolveSupportedVideoConfig(
        screenWidth: Int,
        screenHeight: Int,
        resolutionPref: ScreenRecordResolution,
        requestedFps: Int
    ): VideoEncoderConfig {
        val targetWidth = resolutionPref.targetWidth
        var desiredW: Int
        var desiredH: Int

        if (targetWidth == null) {
            desiredW = screenWidth
            desiredH = screenHeight
        } else {
            val aspectRatio = screenHeight.toFloat() / screenWidth.toFloat().coerceAtLeast(1f)
            desiredW = targetWidth
            desiredH = (targetWidth * aspectRatio).toInt()
        }

        val desiredBitrate = when (resolutionPref) {
            ScreenRecordResolution.NATIVE -> 8_000_000
            ScreenRecordResolution.RES_1080P -> 8_000_000
            ScreenRecordResolution.RES_720P -> 5_000_000
            ScreenRecordResolution.RES_480P -> 2_000_000
        }

        val caps = findBestAvcVideoCapabilities()
        if (caps != null) {
            try {
                val wAlign = maxOf(caps.widthAlignment, 2)
                val hAlign = maxOf(caps.heightAlignment, 2)

                val minW = caps.supportedWidths.lower
                val maxW = caps.supportedWidths.upper
                desiredW = (desiredW - (desiredW % wAlign)).coerceIn(minW, maxW)
                desiredW -= (desiredW % wAlign)

                val heightsForW = caps.getSupportedHeightsFor(desiredW)
                desiredH = (desiredH - (desiredH % hAlign)).coerceIn(heightsForW.lower, heightsForW.upper)
                desiredH -= (desiredH % hAlign)

                if (!caps.isSizeSupported(desiredW, desiredH)) {
                    // Scale proportionally down to a guaranteed supported size
                    val scale = minOf(
                        maxW.toFloat() / desiredW.coerceAtLeast(1),
                        heightsForW.upper.toFloat() / desiredH.coerceAtLeast(1),
                        1f
                    )
                    desiredW = (((desiredW * scale).toInt() / wAlign) * wAlign).coerceIn(minW, maxW)
                    val adjustedHeights = caps.getSupportedHeightsFor(desiredW)
                    desiredH = (((desiredH * scale).toInt() / hAlign) * hAlign)
                        .coerceIn(adjustedHeights.lower, adjustedHeights.upper)
                }

                val supportedRates = caps.getSupportedFrameRatesFor(desiredW, desiredH)
                val safeFps = requestedFps.coerceIn(
                    supportedRates.lower.toInt().coerceAtLeast(15),
                    supportedRates.upper.toInt().coerceAtLeast(15)
                )
                val safeBitrate = desiredBitrate.coerceIn(
                    caps.bitrateRange.lower,
                    caps.bitrateRange.upper
                )
                return VideoEncoderConfig(desiredW, desiredH, safeFps, safeBitrate)
            } catch (e: Exception) {
                Log.w(TAG, "Falling back from MediaCodecList query: ${e.message}")
            }
        }

        // Safe fallback aligned to 16px (universally accepted by AVC encoders)
        val safeW = ((desiredW / 16) * 16).coerceAtLeast(320)
        val safeH = ((desiredH / 16) * 16).coerceAtLeast(480)
        return VideoEncoderConfig(safeW, safeH, requestedFps.coerceIn(15, 60), desiredBitrate)
    }

    private fun findBestAvcVideoCapabilities(): MediaCodecInfo.VideoCapabilities? {
        return try {
            val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
            var fallbackCaps: MediaCodecInfo.VideoCapabilities? = null
            for (info in codecList.codecInfos) {
                if (!info.isEncoder) continue
                val supportsAvc = info.supportedTypes.any { it.equals("video/avc", ignoreCase = true) }
                if (!supportsAvc) continue
                val caps = info.getCapabilitiesForType("video/avc")?.videoCapabilities ?: continue
                val isSoftware = info.name.startsWith("OMX.google.", ignoreCase = true) ||
                    info.name.startsWith("c2.android.", ignoreCase = true)
                if (!isSoftware) {
                    return caps
                }
                if (fallbackCaps == null) {
                    fallbackCaps = caps
                }
            }
            fallbackCaps
        } catch (_: Exception) {
            null
        }
    }

    private fun prepareOutputDestination(): Boolean {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "ScreenRecord_$timeStamp.mp4"
        outputDisplayName = fileName
        outputMediaUri = null
        outputPfd = null
        outputFile = null

        // Android 10+ (API 29+): Write directly into public MediaStore Movies/QuickPanel_Recordings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/QuickPanel_Recordings")
                put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000L)
                put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
            val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            try {
                val uri = contentResolver.insert(collection, values)
                    ?: contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    val pfd = try {
                        contentResolver.openFileDescriptor(uri, "rw")
                    } catch (_: Exception) {
                        contentResolver.openFileDescriptor(uri, "w")
                    }
                    if (pfd != null) {
                        outputMediaUri = uri
                        outputPfd = pfd
                        return true
                    } else {
                        try {
                            contentResolver.delete(uri, null, null)
                        } catch (_: Exception) {}
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "MediaStore output creation failed, falling back to File: ${e.message}", e)
            }
        }

        // Legacy (< API 29) or MediaStore fallback
        val createdFile = createLegacyOutputFile(fileName)
        if (createdFile != null) {
            outputFile = createdFile
            return true
        }
        return false
    }

    private fun reopenOutputDescriptorIfNeeded() {
        val uri = outputMediaUri ?: return
        try {
            outputPfd?.close()
        } catch (_: Exception) {}
        outputPfd = try {
            contentResolver.openFileDescriptor(uri, "rw")
                ?: contentResolver.openFileDescriptor(uri, "w")
        } catch (e: Exception) {
            Log.w(TAG, "reopenOutputDescriptorIfNeeded failed: ${e.message}")
            null
        }
    }

    private fun createLegacyOutputFile(fileName: String): File? {
        try {
            @Suppress("DEPRECATION")
            val publicMovies = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            val publicDir = File(publicMovies, "QuickPanel_Recordings")
            if (!publicDir.exists()) publicDir.mkdirs()
            val publicFile = File(publicDir, fileName)
            if (publicFile.createNewFile() || publicFile.exists()) {
                return publicFile
            }
        } catch (e: Exception) {
            Log.w(TAG, "Public Movies file creation failed: ${e.message}")
        }

        try {
            val appMoviesDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: filesDir
            val recordingsDir = File(appMoviesDir, "QuickPanel_Recordings")
            if (!recordingsDir.exists()) recordingsDir.mkdirs()
            val primaryFile = File(recordingsDir, fileName)
            if (primaryFile.createNewFile() || primaryFile.exists()) {
                return primaryFile
            }
        } catch (e: Exception) {
            Log.w(TAG, "App Movies file creation failed: ${e.message}")
        }

        return try {
            File(cacheDir, fileName).apply { createNewFile() }
        } catch (e: Exception) {
            Log.e(TAG, "Fallback cache file creation failed: ${e.message}")
            null
        }
    }

    private fun cleanupFailedStart() {
        isSessionActive = false
        try {
            outputPfd?.close()
        } catch (_: Exception) {}
        outputPfd = null

        outputMediaUri?.let { uri ->
            try {
                contentResolver.delete(uri, null, null)
            } catch (_: Exception) {}
        }
        outputMediaUri = null

        outputFile?.let { file ->
            try {
                if (file.exists()) file.delete()
            } catch (_: Exception) {}
        }
        outputFile = null
        restoreShowTouchesIfNeeded()
        removeForegroundNotification()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            var seconds = 0
            while (isActive && _isRecording.value) {
                delay(1000L)
                seconds++
                _recordingDurationSeconds.value = seconds
                updateNotification(seconds)
            }
        }
    }

    private fun stopScreenRecording(showResultToast: Boolean) {
        if (!isSessionActive && mediaRecorder == null && virtualDisplay == null && outputMediaUri == null && outputFile == null) {
            return
        }
        isSessionActive = false

        timerJob?.cancel()
        timerJob = null

        val wasRecordingActive = _isRecording.value
        _isRecording.value = false

        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "MediaRecorder stop failed: ${e.message}")
        }

        try {
            mediaRecorder?.reset()
        } catch (e: Exception) {
            Log.w(TAG, "MediaRecorder reset failed: ${e.message}")
        }

        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            Log.w(TAG, "MediaRecorder release failed: ${e.message}")
        }
        mediaRecorder = null

        try {
            outputPfd?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Closing output ParcelFileDescriptor failed: ${e.message}")
        }
        outputPfd = null

        try {
            virtualDisplay?.release()
        } catch (e: Exception) {
            Log.w(TAG, "VirtualDisplay release failed: ${e.message}")
        }
        virtualDisplay = null

        try {
            mediaProjection?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "MediaProjection stop failed: ${e.message}")
        }
        mediaProjection = null

        restoreShowTouchesIfNeeded()
        removeForegroundNotification()

        val uri = outputMediaUri
        val file = outputFile
        val displayName = outputDisplayName ?: "ScreenRecord.mp4"
        outputMediaUri = null
        outputFile = null
        outputDisplayName = null

        if (uri != null) {
            val savedSize = queryUriSizeBytes(uri)
            if (savedSize > 0L) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        val publishValues = ContentValues().apply {
                            put(MediaStore.Video.Media.IS_PENDING, 0)
                        }
                        contentResolver.update(uri, publishValues, null, null)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed clearing IS_PENDING: ${e.message}")
                    }
                }
                _lastSavedFilePath.value = uri.toString()
                showSavedNotification(uri, displayName)
                if (showResultToast) {
                    showToast("Screen recording saved to Movies/QuickPanel_Recordings")
                }
            } else {
                try {
                    contentResolver.delete(uri, null, null)
                } catch (_: Exception) {}
                if (wasRecordingActive && showResultToast) {
                    showToast("Screen recording failed to save (empty recording)")
                }
            }
        } else if (file != null) {
            if (file.exists() && file.length() > 0L) {
                _lastSavedFilePath.value = file.absolutePath
                MediaScannerConnection.scanFile(
                    this,
                    arrayOf(file.absolutePath),
                    arrayOf("video/mp4")
                ) { path, scannedUri ->
                    Log.d(TAG, "Scanned $path into MediaStore: $scannedUri")
                    mainHandler.post {
                        showSavedNotification(scannedUri, file.name)
                    }
                }
                if (showResultToast) {
                    showToast("Screen recording saved: ${file.name}")
                }
            } else {
                try {
                    if (file.exists()) file.delete()
                } catch (_: Exception) {}
                if (wasRecordingActive && showResultToast) {
                    showToast("Screen recording failed to save (empty recording)")
                }
            }
        }
    }

    private fun queryUriSizeBytes(uri: Uri): Long {
        try {
            contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                if (pfd.statSize > 0L) return pfd.statSize
            }
        } catch (_: Exception) {}

        try {
            contentResolver.query(uri, arrayOf(MediaStore.Video.Media.SIZE), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(MediaStore.Video.Media.SIZE)
                    if (idx >= 0) {
                        val size = cursor.getLong(idx)
                        if (size > 0L) return size
                    }
                }
            }
        } catch (_: Exception) {}

        return try {
            contentResolver.openInputStream(uri)?.use { stream ->
                val buf = ByteArray(1024)
                val read = stream.read(buf)
                if (read > 0) read.toLong() else 0L
            } ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    private fun restoreShowTouchesIfNeeded() {
        if (didModifyShowTouches) {
            try {
                Settings.System.putInt(contentResolver, "show_touches", initialShowTouchesState)
            } catch (e: Exception) {
                Log.w(TAG, "Could not restore show_touches: ${e.message}")
            }
            didModifyShowTouches = false
        }
    }

    private fun removeForegroundNotification() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        } catch (_: Exception) {}
    }

    private fun showSavedNotification(contentUri: Uri?, displayName: String) {
        try {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            val viewPendingIntent = if (contentUri != null && contentUri.scheme != "file") {
                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(contentUri, "video/mp4")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                PendingIntent.getActivity(
                    this,
                    203,
                    viewIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                val fallbackIntent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                PendingIntent.getActivity(
                    this,
                    203,
                    fallbackIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

            val notif = NotificationCompat.Builder(this, SAVED_CHANNEL_ID)
                .setContentTitle("Screen recording saved")
                .setContentText("Saved to Movies/QuickPanel_Recordings/$displayName")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setAutoCancel(true)
                .setContentIntent(viewPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            nm.notify(SAVED_NOTIFICATION_ID, notif)
        } catch (e: Exception) {
            Log.w(TAG, "showSavedNotification error: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        } else {
            mainHandler.post {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        stopScreenRecording(showResultToast = false)
        super.onDestroy()
    }
}
