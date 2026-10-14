package com.fauyds.quicksettings.model

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable

/**
 * State representing current active media playback.
 */
@Immutable
data class MediaState(
    val title: String = "Never Let You Go",
    val artist: String = "Kygo",
    val appName: String = "MUSER",
    val packageName: String = "com.spotify.music",
    val isPlaying: Boolean = false,
    val isFavorite: Boolean = false,
    val progress: Float = 0.02f, // 0.0f to 1.0f
    val currentPositionMs: Long = 4000L, // 00:04
    val totalDurationMs: Long = 232000L, // 03:52
    val outputDevice: String = "Phone speaker",
    val isHeadphonesOrBluetooth: Boolean = false,
    val artworkBitmap: Bitmap? = null,
    val sessionIndex: Int = 0,
    val supportsRepeat: Boolean = false,
    val repeatMode: Int = 0, // 0 = None, 1 = One, 2 = All
    val supportsShuffle: Boolean = false,
    val shuffleMode: Int = 0, // 0 = None, 1 = All
    val supportsFavorite: Boolean = false
) {
    val formattedCurrentTime: String
        get() = formatDuration(currentPositionMs)

    val formattedTotalTime: String
        get() = formatDuration(totalDurationMs)

    companion object {
        fun formatDuration(millis: Long): String {
            val totalSeconds = (millis / 1000).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }

        val Preview = MediaState(
            title = "Never Let You Go",
            artist = "Kygo",
            appName = "MUSER",
            isPlaying = false,
            isFavorite = false,
            progress = 0.02f,
            currentPositionMs = 4000L,
            totalDurationMs = 232000L,
            outputDevice = "Phone speaker",
            supportsRepeat = true,
            repeatMode = 0,
            supportsShuffle = true,
            shuffleMode = 0,
            supportsFavorite = true
        )
    }
}
