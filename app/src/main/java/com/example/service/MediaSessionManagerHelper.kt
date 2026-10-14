package com.example.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.util.Log
import com.example.data.PanelPreferences
import com.example.model.MediaState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

data class AudioOutputInfo(
    val name: String,
    val isHeadphonesOrBluetooth: Boolean
)

object MediaSessionManagerHelper {

    private const val TAG = "MediaSessionHelper"

    private val userLikedMap = java.util.concurrent.ConcurrentHashMap<String, Boolean>()

    private val _sessionsList = MutableStateFlow<List<MediaState>>(emptyList())
    val sessionsList: StateFlow<List<MediaState>> = _sessionsList.asStateFlow()

    private val _selectedSessionIndex = MutableStateFlow(0)
    val selectedSessionIndex: StateFlow<Int> = _selectedSessionIndex.asStateFlow()

    private val _mediaState = MutableStateFlow<MediaState?>(null)
    val mediaState: StateFlow<MediaState?> = _mediaState.asStateFlow()

    private val activeControllers = CopyOnWriteArrayList<MediaController>()
    private val controllerCallbacks = java.util.concurrent.ConcurrentHashMap<String, MediaController.Callback>()

    private var currentContext: Context? = null
    private var sessionListener: MediaSessionManager.OnActiveSessionsChangedListener? = null

    fun init(context: Context) {
        currentContext = context.applicationContext
        val appContext = context.applicationContext

        val mm = appContext.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            ?: return

        val notificationListenerComponent = ComponentName(appContext, QuickPanelNotificationListenerService::class.java)

        sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            updateActiveControllers(controllers)
        }

        try {
            if (QuickPanelNotificationListenerService.isPermissionGranted(appContext)) {
                mm.addOnActiveSessionsChangedListener(
                    sessionListener!!,
                    notificationListenerComponent
                )
                val initialControllers = mm.getActiveSessions(notificationListenerComponent)
                updateActiveControllers(initialControllers)
            } else {
                updateMediaState()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error registering sessions listener: ${e.message}")
            updateMediaState()
        }
    }

    fun refreshState() {
        val appContext = currentContext
        if (appContext != null && QuickPanelNotificationListenerService.isPermissionGranted(appContext)) {
            try {
                val mm = appContext.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
                val notificationListenerComponent = ComponentName(appContext, QuickPanelNotificationListenerService::class.java)
                val currentControllers = mm?.getActiveSessions(notificationListenerComponent)
                if (currentControllers != null) {
                    updateActiveControllers(currentControllers)
                    return
                }
            } catch (e: Exception) {
                Log.e(TAG, "Refresh active sessions failed: ${e.message}")
            }
        }
        updateMediaState()
    }

    private fun updateActiveControllers(controllers: List<MediaController>?) {
        // Unregister callbacks for existing controllers
        activeControllers.forEach { ctrl ->
            val key = ctrl.sessionToken.toString()
            controllerCallbacks.remove(key)?.let { cb ->
                try { ctrl.unregisterCallback(cb) } catch (e: Exception) {}
            }
        }
        activeControllers.clear()

        if (controllers != null) {
            val filtered = controllers.filter { ctrl ->
                val state = ctrl.playbackState?.state
                state != null && state != PlaybackState.STATE_NONE && state != PlaybackState.STATE_STOPPED
            }.distinctBy { it.packageName }

            filtered.forEach { ctrl ->
                activeControllers.add(ctrl)
                val cb = object : MediaController.Callback() {
                    override fun onPlaybackStateChanged(state: PlaybackState?) {
                        updateMediaState()
                    }

                    override fun onMetadataChanged(metadata: MediaMetadata?) {
                        updateMediaState()
                    }

                    override fun onSessionDestroyed() {
                        refreshState()
                    }
                }
                val key = ctrl.sessionToken.toString()
                controllerCallbacks[key] = cb
                try { ctrl.registerCallback(cb) } catch (e: Exception) {}
            }
        }

        updateMediaState()
    }

    fun selectSession(index: Int) {
        val list = _sessionsList.value
        if (index in list.indices) {
            _selectedSessionIndex.value = index
            _mediaState.value = list[index]
        }
    }

    private var lastUpdateMediaTime = 0L
    private var progressTickerJob: kotlinx.coroutines.Job? = null
    private var previewPositionMs = 81200L
    private val helperScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main + kotlinx.coroutines.SupervisorJob())

    private fun checkStartTicker(states: List<MediaState>) {
        val anyPlaying = states.any { it.isPlaying }
        if (anyPlaying) {
            if (progressTickerJob == null || progressTickerJob?.isActive != true) {
                progressTickerJob = helperScope.launch {
                    while (this.isActive) {
                        kotlinx.coroutines.delay(1000L)
                        if (activeControllers.isEmpty() && PanelPreferences.forceShowMediaPreview.value) {
                            previewPositionMs = (previewPositionMs + 1000L) % 232000L
                        }
                        updateMediaStateInternal()
                    }
                }
            }
        } else {
            progressTickerJob?.cancel()
            progressTickerJob = null
        }
    }

    fun updateMediaState() {
        updateMediaStateInternal()
    }

    private fun updateMediaStateInternal() {
        val now = System.currentTimeMillis()
        if (now - lastUpdateMediaTime < 100) return // Throttle rapid updates within 100ms
        lastUpdateMediaTime = now
        val context = currentContext ?: return
        val forcePreview = PanelPreferences.forceShowMediaPreview.value

        if (activeControllers.isNotEmpty()) {
            val states = activeControllers.mapIndexed { idx, controller ->
                buildMediaState(context, controller, idx)
            }
            _sessionsList.value = states

            var targetIndex = _selectedSessionIndex.value
            if (targetIndex !in states.indices) {
                targetIndex = 0
            }

            // Auto-advance selection if another session newly starts playing
            val playingIndex = states.indexOfFirst { it.isPlaying }
            if (playingIndex != -1 && playingIndex != targetIndex && states.getOrNull(targetIndex)?.isPlaying == false) {
                targetIndex = playingIndex
            }

            _selectedSessionIndex.value = targetIndex
            _mediaState.value = states.getOrNull(targetIndex)
            checkStartTicker(states)
        } else if (forcePreview) {
            val totalPreviewDur = 232000L
            val previewProgress = (previewPositionMs.toFloat() / totalPreviewDur.toFloat()).coerceIn(0f, 1f)

            val preview1 = MediaState(
                title = "Never Let You Go",
                artist = "Kygo",
                appName = "Spotify",
                packageName = "com.spotify.music",
                isPlaying = true,
                isFavorite = false,
                progress = previewProgress,
                currentPositionMs = previewPositionMs,
                totalDurationMs = totalPreviewDur,
                outputDevice = getActiveOutputDevice(context).name,
                isHeadphonesOrBluetooth = getActiveOutputDevice(context).isHeadphonesOrBluetooth,
                sessionIndex = 0
            )
            val preview2 = MediaState(
                title = "Higher Love",
                artist = "Kygo & Whitney Houston",
                appName = "YouTube Music",
                packageName = "com.google.android.apps.youtube.music",
                isPlaying = false,
                isFavorite = true,
                progress = 0.52f,
                currentPositionMs = 112000L,
                totalDurationMs = 215000L,
                outputDevice = getActiveOutputDevice(context).name,
                isHeadphonesOrBluetooth = getActiveOutputDevice(context).isHeadphonesOrBluetooth,
                sessionIndex = 1
            )
            val preview3 = MediaState(
                title = "Synthwave Nights",
                artist = "Lazerhawk",
                appName = "AIMP",
                packageName = "com.aimp.player",
                isPlaying = false,
                isFavorite = false,
                progress = 0.18f,
                currentPositionMs = 45000L,
                totalDurationMs = 250000L,
                outputDevice = getActiveOutputDevice(context).name,
                isHeadphonesOrBluetooth = getActiveOutputDevice(context).isHeadphonesOrBluetooth,
                sessionIndex = 2
            )

            val previews = listOf(preview1, preview2, preview3)
            _sessionsList.value = previews

            var targetIndex = _selectedSessionIndex.value
            if (targetIndex !in previews.indices) {
                targetIndex = 0
            }
            _selectedSessionIndex.value = targetIndex
            _mediaState.value = previews.getOrNull(targetIndex)
            checkStartTicker(previews)
        } else {
            _sessionsList.value = emptyList()
            _mediaState.value = null
            checkStartTicker(emptyList())
        }
    }

    private fun buildMediaState(context: Context?, controller: MediaController, sessionIndex: Int): MediaState {
        val metadata = controller.metadata
        val playbackState = controller.playbackState

        val packageName = controller.packageName
        val appName = try {
            if (context != null) {
                val pm = context.packageManager
                val appInfo = pm.getApplicationInfo(packageName, 0)
                pm.getApplicationLabel(appInfo).toString()
            } else packageName.substringAfterLast('.')
        } catch (e: Exception) {
            packageName.substringAfterLast('.')
        }

        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: "Unknown Track"

        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)
            ?: "Unknown Artist"

        val totalDurationMs = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
        val rawPos = playbackState?.position ?: 0L
        val lastUpdateTime = playbackState?.lastPositionUpdateTime ?: 0L
        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING || playbackState?.state == PlaybackState.STATE_BUFFERING
        val speed = playbackState?.playbackSpeed ?: 1.0f

        val currentPositionMs = if (isPlaying && lastUpdateTime > 0) {
            val elapsed = android.os.SystemClock.elapsedRealtime() - lastUpdateTime
            val estimated = (rawPos + (elapsed * speed).toLong())
            if (totalDurationMs > 0) estimated.coerceIn(0L, totalDurationMs) else estimated.coerceAtLeast(0L)
        } else {
            rawPos
        }

        val progress = if (totalDurationMs > 0) {
            (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f

        val artworkBitmap = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)

        val outputInfo = getActiveOutputDevice(context)

        var isFavorite = false
        try {
            val rating = metadata?.getRating(MediaMetadata.METADATA_KEY_USER_RATING)
                ?: metadata?.getRating(MediaMetadata.METADATA_KEY_RATING)
            if (rating != null && rating.isRated) {
                if (rating.ratingStyle == android.media.Rating.RATING_HEART) {
                    isFavorite = rating.hasHeart()
                } else if (rating.ratingStyle == android.media.Rating.RATING_THUMB_UP_DOWN) {
                    isFavorite = rating.isThumbUp
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Rating error: ${e.message}")
        }

        val trackKey = "$packageName:$title:$artist"
        if (userLikedMap.containsKey(trackKey)) {
            isFavorite = userLikedMap[trackKey] ?: false
        }

        val actions = playbackState?.actions ?: 0L
        val actionSetRepeatMode = 1L shl 18
        val actionSetShuffleMode = 1L shl 21
        val supportsRepeat = (actions and actionSetRepeatMode) != 0L ||
                (playbackState?.customActions?.any { it.action.contains("repeat", ignoreCase = true) } == true)
        val repeatMode = try {
            val method = controller::class.java.getMethod("getRepeatMode")
            (method.invoke(controller) as? Int) ?: 0
        } catch (e: Exception) {
            0
        }

        val supportsShuffle = (actions and actionSetShuffleMode) != 0L ||
                (playbackState?.customActions?.any { it.action.contains("shuffle", ignoreCase = true) } == true)
        val shuffleMode = try {
            val method = controller::class.java.getMethod("getShuffleMode")
            (method.invoke(controller) as? Int) ?: 0
        } catch (e: Exception) {
            0
        }

        val supportsFavorite = (actions and PlaybackState.ACTION_SET_RATING) != 0L ||
                metadata?.getRating(MediaMetadata.METADATA_KEY_USER_RATING) != null ||
                metadata?.getRating(MediaMetadata.METADATA_KEY_RATING) != null ||
                (playbackState?.customActions?.any { it.action.contains("like", ignoreCase = true) || it.action.contains("thumb", ignoreCase = true) || it.action.contains("fav", ignoreCase = true) } == true) ||
                userLikedMap.containsKey(trackKey)

        return MediaState(
            title = title,
            artist = artist,
            appName = appName,
            packageName = packageName,
            isPlaying = isPlaying,
            isFavorite = isFavorite,
            progress = progress,
            currentPositionMs = currentPositionMs,
            totalDurationMs = totalDurationMs,
            outputDevice = outputInfo.name,
            isHeadphonesOrBluetooth = outputInfo.isHeadphonesOrBluetooth,
            artworkBitmap = artworkBitmap,
            sessionIndex = sessionIndex,
            supportsRepeat = supportsRepeat,
            repeatMode = repeatMode,
            supportsShuffle = supportsShuffle,
            shuffleMode = shuffleMode,
            supportsFavorite = supportsFavorite
        )
    }

    private fun getActiveOutputDevice(context: Context?): AudioOutputInfo {
        if (context == null) return AudioOutputInfo("Phone speaker", false)
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            val outputs = audioManager?.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val headDevice = outputs?.firstOrNull { device ->
                device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                device.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                device.type == AudioDeviceInfo.TYPE_BLE_SPEAKER ||
                device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                device.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                device.type == AudioDeviceInfo.TYPE_USB_HEADSET
            }
            if (headDevice != null) {
                val name = headDevice.productName?.toString()
                val finalName = if (!name.isNullOrBlank()) name else "Bluetooth Audio"
                AudioOutputInfo(finalName, true)
            } else {
                AudioOutputInfo("Phone speaker", false)
            }
        } catch (e: Exception) {
            AudioOutputInfo("Phone speaker", false)
        }
    }

    fun playPause() {
        val targetIdx = _selectedSessionIndex.value
        val controller = activeControllers.getOrNull(targetIdx) ?: activeControllers.firstOrNull()

        if (controller != null) {
            val state = controller.playbackState?.state
            if (state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_BUFFERING) {
                controller.transportControls.pause()
            } else {
                controller.transportControls.play()
            }
        } else if (PanelPreferences.forceShowMediaPreview.value) {
            val currentList = _sessionsList.value.toMutableList()
            if (targetIdx in currentList.indices) {
                val current = currentList[targetIdx]
                currentList[targetIdx] = current.copy(isPlaying = !current.isPlaying)
                _sessionsList.value = currentList
                _mediaState.value = currentList[targetIdx]
            }
        }
    }

    fun previous() {
        val targetIdx = _selectedSessionIndex.value
        val controller = activeControllers.getOrNull(targetIdx) ?: activeControllers.firstOrNull()

        if (controller != null) {
            controller.transportControls.skipToPrevious()
        } else if (PanelPreferences.forceShowMediaPreview.value) {
            val currentList = _sessionsList.value.toMutableList()
            if (targetIdx in currentList.indices) {
                val current = currentList[targetIdx]
                currentList[targetIdx] = current.copy(
                    progress = 0f,
                    currentPositionMs = 0L
                )
                _sessionsList.value = currentList
                _mediaState.value = currentList[targetIdx]
            }
        }
    }

    fun next() {
        val targetIdx = _selectedSessionIndex.value
        val controller = activeControllers.getOrNull(targetIdx) ?: activeControllers.firstOrNull()

        if (controller != null) {
            controller.transportControls.skipToNext()
        } else if (PanelPreferences.forceShowMediaPreview.value) {
            val currentList = _sessionsList.value.toMutableList()
            if (targetIdx in currentList.indices) {
                val current = currentList[targetIdx]
                currentList[targetIdx] = current.copy(
                    progress = 0.5f,
                    currentPositionMs = (0.5f * current.totalDurationMs).toLong()
                )
                _sessionsList.value = currentList
                _mediaState.value = currentList[targetIdx]
            }
        }
    }

    fun seekTo(newProgress: Float) {
        val targetIdx = _selectedSessionIndex.value
        val controller = activeControllers.getOrNull(targetIdx) ?: activeControllers.firstOrNull()
        val duration = controller?.metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L

        if (controller != null && duration > 0) {
            val targetMs = (newProgress * duration).toLong()
            controller.transportControls.seekTo(targetMs)
        } else if (PanelPreferences.forceShowMediaPreview.value) {
            val currentList = _sessionsList.value.toMutableList()
            if (targetIdx in currentList.indices) {
                val current = currentList[targetIdx]
                currentList[targetIdx] = current.copy(
                    progress = newProgress,
                    currentPositionMs = (newProgress * current.totalDurationMs).toLong()
                )
                _sessionsList.value = currentList
                _mediaState.value = currentList[targetIdx]
            }
        }
    }

    fun toggleFavorite() {
        val targetIdx = _selectedSessionIndex.value
        val controller = activeControllers.getOrNull(targetIdx) ?: activeControllers.firstOrNull()
        val current = _mediaState.value ?: return
        val newFav = !current.isFavorite

        val pkg = controller?.packageName ?: current.packageName
        val trackKey = "$pkg:${current.title}:${current.artist}"
        userLikedMap[trackKey] = newFav

        val currentList = _sessionsList.value.toMutableList()
        if (targetIdx in currentList.indices) {
            currentList[targetIdx] = currentList[targetIdx].copy(isFavorite = newFav)
            _sessionsList.value = currentList
            _mediaState.value = currentList[targetIdx]
        }

        if (controller != null) {
            try {
                val rating = android.media.Rating.newHeartRating(newFav)
                controller.transportControls.setRating(rating)
            } catch (e: Exception) {
                Log.d(TAG, "setRating failed: ${e.message}")
            }
        }
    }

    fun toggleRepeat() {
        val targetIdx = _selectedSessionIndex.value
        val controller = activeControllers.getOrNull(targetIdx) ?: activeControllers.firstOrNull()
        val current = _mediaState.value ?: return

        val nextMode = when (current.repeatMode) {
            0 -> 2 // REPEAT_MODE_ALL
            2 -> 1 // REPEAT_MODE_ONE
            else -> 0 // REPEAT_MODE_NONE
        }

        val currentList = _sessionsList.value.toMutableList()
        if (targetIdx in currentList.indices) {
            currentList[targetIdx] = currentList[targetIdx].copy(repeatMode = nextMode)
            _sessionsList.value = currentList
            _mediaState.value = currentList[targetIdx]
        }

        if (controller != null) {
            try {
                val method = controller.transportControls::class.java.getMethod("setRepeatMode", Int::class.javaPrimitiveType)
                method.invoke(controller.transportControls, nextMode)
            } catch (e: Exception) {
                controller.playbackState?.customActions?.firstOrNull { it.action.contains("repeat", ignoreCase = true) }?.let { action ->
                    try { controller.transportControls.sendCustomAction(action, null) } catch (e2: Exception) {}
                }
            }
        }
    }

    fun toggleShuffle() {
        val targetIdx = _selectedSessionIndex.value
        val controller = activeControllers.getOrNull(targetIdx) ?: activeControllers.firstOrNull()
        val current = _mediaState.value ?: return

        val nextMode = if (current.shuffleMode == 0 || current.shuffleMode == -1) {
            1 // SHUFFLE_MODE_ALL
        } else {
            0 // SHUFFLE_MODE_NONE
        }

        val currentList = _sessionsList.value.toMutableList()
        if (targetIdx in currentList.indices) {
            currentList[targetIdx] = currentList[targetIdx].copy(shuffleMode = nextMode)
            _sessionsList.value = currentList
            _mediaState.value = currentList[targetIdx]
        }

        if (controller != null) {
            try {
                val method = controller.transportControls::class.java.getMethod("setShuffleMode", Int::class.javaPrimitiveType)
                method.invoke(controller.transportControls, nextMode)
            } catch (e: Exception) {
                controller.playbackState?.customActions?.firstOrNull { it.action.contains("shuffle", ignoreCase = true) }?.let { action ->
                    try { controller.transportControls.sendCustomAction(action, null) } catch (e2: Exception) {}
                }
            }
        }
    }

    fun openActivePlayerApp(context: Context) {
        val targetIdx = _selectedSessionIndex.value
        val controller = activeControllers.getOrNull(targetIdx) ?: activeControllers.firstOrNull()
        val pkg = controller?.packageName ?: _mediaState.value?.packageName

        var launched = false

        if (!pkg.isNullOrEmpty()) {
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    context.startActivity(launchIntent)
                    launched = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error launching app for package $pkg: ${e.message}")
            }
        }

        if (!launched && controller != null) {
            val sessionActivity = controller.sessionActivity
            if (sessionActivity != null) {
                try {
                    sessionActivity.send()
                    launched = true
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending sessionActivity intent: ${e.message}")
                }
            }
        }

        if (!launched) {
            try {
                val musicIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_MUSIC)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                }
                context.startActivity(musicIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Error launching fallback music app: ${e.message}")
            }
        }
    }

    fun openAudioOutputPicker(context: Context) {
        val targetIdx = _selectedSessionIndex.value
        val activePkg = activeControllers.getOrNull(targetIdx)?.packageName ?: _mediaState.value?.packageName
        val intents = listOf(
            Intent("com.android.settings.panel.action.MEDIA_OUTPUT").apply {
                if (!activePkg.isNullOrEmpty()) {
                    putExtra("com.android.settings.panel.extra.PACKAGE_NAME", activePkg)
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            Intent("android.settings.MEDIA_ROUTER_PANEL_SETTINGS").apply {
                if (!activePkg.isNullOrEmpty()) {
                    putExtra("com.android.settings.panel.extra.PACKAGE_NAME", activePkg)
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            Intent("android.settings.PANEL_SETTINGS").apply {
                if (!activePkg.isNullOrEmpty()) {
                    putExtra("com.android.settings.panel.extra.PACKAGE_NAME", activePkg)
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            Intent(android.provider.Settings.ACTION_SOUND_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )

        for (intent in intents) {
            try {
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                // Try next fallback
            }
        }
    }

    fun unregisterAll() {
        activeControllers.forEach { ctrl ->
            val key = ctrl.sessionToken.toString()
            controllerCallbacks.remove(key)?.let { cb ->
                try { ctrl.unregisterCallback(cb) } catch (e: Exception) {}
            }
        }
        activeControllers.clear()
        val appContext = currentContext
        if (appContext != null && sessionListener != null) {
            try {
                val mm = appContext.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
                mm?.removeOnActiveSessionsChangedListener(sessionListener!!)
            } catch (e: Exception) {}
            sessionListener = null
        }
    }
}
