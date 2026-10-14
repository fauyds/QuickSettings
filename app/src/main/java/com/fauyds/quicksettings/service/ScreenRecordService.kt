package com.fauyds.quicksettings.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
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
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.fauyds.quicksettings.MainActivity
import com.fauyds.quicksettings.data.PanelPreferences
import com.fauyds.quicksettings.data.ScreenRecordAudioSource
import com.fauyds.quicksettings.data.ScreenRecordFps
import com.fauyds.quicksettings.data.ScreenRecordResolution
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
        const val NOTIFICATION_ID = 2002

        const val ACTION_START = "com.fauyds.quicksettings.service.action.START_RECORDING"
        const val ACTION_STOP = "com.fauyds.quicksettings.service.action.STOP_RECORDING"
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
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null

    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null

    private var outputFile: File? = null
    private var initialShowTouchesState: Int = 0
    private var didModifyShowTouches: Boolean = false

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        PanelPreferences.init(this)
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopScreenRecording()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START -> {
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screen Recording",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live screen recording controls and elapsed time"
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    private fun startForegroundInternal() {
        val notification = buildRecordingNotification(0)
        val hasMicPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var serviceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && hasMicPermission) {
                val audioSource = PanelPreferences.screenRecordAudioSource.value
                if (audioSource == ScreenRecordAudioSource.MIC || audioSource == ScreenRecordAudioSource.INTERNAL_AND_MIC) {
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

        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val densityDpi = displayMetrics.densityDpi

        // Compute video dimensions based on preferences
        val resolutionPref = PanelPreferences.screenRecordResolution.value
        val (videoWidth, videoHeight) = calculateDimensions(screenWidth, screenHeight, resolutionPref)

        val fpsPref = PanelPreferences.screenRecordFps.value
        val audioSourcePref = PanelPreferences.screenRecordAudioSource.value
        val showTouches = PanelPreferences.screenRecordShowTouches.value

        val hasMicPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

        // Handle Show Touches
        if (showTouches) {
            try {
                initialShowTouchesState = Settings.System.getInt(contentResolver, "show_touches", 0)
                Settings.System.putInt(contentResolver, "show_touches", 1)
                didModifyShowTouches = true
            } catch (e: Exception) {
                Log.w(TAG, "Could not enable show_touches: ${e.message}")
            }
        }

        outputFile = createOutputFile()
        if (outputFile == null) {
            Toast.makeText(this, "Failed to create recording file", Toast.LENGTH_SHORT).show()
            return false
        }

        // Try setup MediaRecorder with audio if permitted, fallback to video-only if audio fails
        var recorderSuccess = false
        var audioEnabled = hasMicPermission && (audioSourcePref != ScreenRecordAudioSource.MUTE)

        if (audioEnabled) {
            recorderSuccess = trySetupAndPrepareRecorder(videoWidth, videoHeight, fpsPref.fps, resolutionPref, audioSourcePref = audioSourcePref)
            if (!recorderSuccess) {
                Log.w(TAG, "Audio recorder setup failed, falling back to video-only recording")
                audioEnabled = false
            }
        }

        if (!recorderSuccess) {
            recorderSuccess = trySetupAndPrepareRecorder(videoWidth, videoHeight, fpsPref.fps, resolutionPref, audioSourcePref = ScreenRecordAudioSource.MUTE)
        }

        if (!recorderSuccess || mediaRecorder == null) {
            Log.e(TAG, "Failed to prepare MediaRecorder")
            Toast.makeText(this, "Screen recorder preparation failed", Toast.LENGTH_SHORT).show()
            stopScreenRecording()
            return false
        }

        // Setup MediaProjection
        try {
            mediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, resultData)
        } catch (e: Exception) {
            Log.e(TAG, "getMediaProjection failed: ${e.message}", e)
        }

        if (mediaProjection == null) {
            Log.e(TAG, "Failed to get MediaProjection instance")
            Toast.makeText(this, "Media projection permission error", Toast.LENGTH_SHORT).show()
            stopScreenRecording()
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                mediaProjection?.registerCallback(object : MediaProjection.Callback() {
                    override fun onStop() {
                        stopScreenRecording()
                    }
                }, mainHandler)
            } catch (e: Exception) {
                Log.w(TAG, "registerCallback failed: ${e.message}")
            }
        }

        // Create VirtualDisplay
        try {
            virtualDisplay = mediaProjection?.createVirtualDisplay(
                "QuickPanelScreenRecorder",
                videoWidth,
                videoHeight,
                densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder!!.surface,
                null,
                null
            )
        } catch (e: Exception) {
            Log.e(TAG, "createVirtualDisplay failed: ${e.message}", e)
            Toast.makeText(this, "Virtual display error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            stopScreenRecording()
            return false
        }

        // Start recorder
        return try {
            mediaRecorder!!.start()
            _isRecording.value = true
            _recordingDurationSeconds.value = 0

            startTimer()
            val audioMsg = if (audioEnabled) "with Audio" else "Video only"
            Toast.makeText(this, "Screen recording started ($audioMsg, ${fpsPref.fps}fps)", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting MediaRecorder: ${e.message}", e)
            Toast.makeText(this, "Recording start failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            stopScreenRecording()
            false
        }
    }

    private fun trySetupAndPrepareRecorder(
        width: Int,
        height: Int,
        fps: Int,
        resolutionPref: ScreenRecordResolution,
        audioSourcePref: ScreenRecordAudioSource
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

            val hasAudio = audioSourcePref != ScreenRecordAudioSource.MUTE
            if (hasAudio) {
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            }

            recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

            if (hasAudio) {
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                recorder.setAudioEncodingBitRate(128_000)
                recorder.setAudioSamplingRate(44_100)
            }

            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            recorder.setVideoSize(width, height)
            recorder.setVideoFrameRate(fps)

            val bitrate = when (resolutionPref) {
                ScreenRecordResolution.NATIVE -> 12_000_000
                ScreenRecordResolution.RES_1080P -> 10_000_000
                ScreenRecordResolution.RES_720P -> 5_000_000
                ScreenRecordResolution.RES_480P -> 2_000_000
            }
            recorder.setVideoEncodingBitRate(bitrate)

            recorder.setOutputFile(outputFile!!.absolutePath)

            recorder.prepare()
            true
        } catch (e: Exception) {
            Log.w(TAG, "trySetupAndPrepareRecorder failed (audio=${audioSourcePref.name}): ${e.message}")
            try {
                mediaRecorder?.reset()
                mediaRecorder?.release()
            } catch (_: Exception) {}
            mediaRecorder = null
            false
        }
    }

    private fun calculateDimensions(screenWidth: Int, screenHeight: Int, pref: ScreenRecordResolution): Pair<Int, Int> {
        val targetWidth = pref.targetWidth
        if (targetWidth == null) {
            val w = (screenWidth - (screenWidth % 2)).coerceAtLeast(320)
            val h = (screenHeight - (screenHeight % 2)).coerceAtLeast(480)
            return Pair(w, h)
        }

        val aspectRatio = screenHeight.toFloat() / screenWidth.toFloat()
        var w = targetWidth
        var h = (targetWidth * aspectRatio).toInt()

        w -= (w % 2)
        h -= (h % 2)
        w = w.coerceAtLeast(320)
        h = h.coerceAtLeast(480)
        return Pair(w, h)
    }

    private fun createOutputFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "ScreenRecord_$timeStamp.mp4"

        val appMoviesDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: filesDir
        val recordingsDir = File(appMoviesDir, "QuickPanel_Recordings")
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs()
        }

        val primaryFile = File(recordingsDir, fileName)
        try {
            if (primaryFile.createNewFile() || primaryFile.exists()) {
                return primaryFile
            }
        } catch (e: Exception) {
            Log.w(TAG, "Primary file creation failed: ${e.message}")
        }

        try {
            val publicMovies = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            val publicDir = File(publicMovies, "QuickPanel_Recordings")
            if (!publicDir.exists()) publicDir.mkdirs()
            val publicFile = File(publicDir, fileName)
            if (publicFile.createNewFile() || publicFile.exists()) {
                return publicFile
            }
        } catch (e: Exception) {
            Log.w(TAG, "Public file creation failed: ${e.message}")
        }

        return try {
            File(cacheDir, fileName).apply { createNewFile() }
        } catch (e: Exception) {
            Log.e(TAG, "Fallback file creation failed: ${e.message}")
            null
        }
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

    private fun stopScreenRecording() {
        timerJob?.cancel()
        timerJob = null

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

        // Restore touches if modified
        if (didModifyShowTouches) {
            try {
                Settings.System.putInt(contentResolver, "show_touches", initialShowTouchesState)
            } catch (e: Exception) {
                Log.w(TAG, "Could not restore show_touches: ${e.message}")
            }
            didModifyShowTouches = false
        }

        _isRecording.value = false
        val savedFile = outputFile
        if (savedFile != null && savedFile.exists() && savedFile.length() > 0) {
            _lastSavedFilePath.value = savedFile.absolutePath
            // Scan file into MediaStore
            MediaScannerConnection.scanFile(
                this,
                arrayOf(savedFile.absolutePath),
                arrayOf("video/mp4")
            ) { path, uri ->
                Log.d(TAG, "Scanned $path into MediaStore: $uri")
            }
            showSavedNotification(savedFile)
            Toast.makeText(this, "Screen record saved: ${savedFile.name}", Toast.LENGTH_LONG).show()
        }
        outputFile = null
    }

    private fun showSavedNotification(file: File) {
        try {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                val uri = Uri.fromFile(file)
                setDataAndType(uri, "video/mp4")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            val viewPendingIntent = PendingIntent.getActivity(
                this,
                203,
                viewIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notif = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Screen recording saved")
                .setContentText(file.name)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setAutoCancel(true)
                .setContentIntent(viewPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            nm.notify(2003, notif)
        } catch (e: Exception) {
            Log.w(TAG, "showSavedNotification error: ${e.message}")
        }
    }

    override fun onDestroy() {
        stopScreenRecording()
        super.onDestroy()
    }
}
