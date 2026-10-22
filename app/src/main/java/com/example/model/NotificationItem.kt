package com.example.model

import android.app.PendingIntent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable

@Immutable
data class NotificationActionModel(
    val title: String,
    val actionIntent: PendingIntent?
)

@Immutable
data class NotificationItem(
    val key: String,
    val id: Int,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val subText: String? = null,
    val postTime: Long = System.currentTimeMillis(),
    val appIcon: Drawable? = null,
    val avatarBitmap: Bitmap? = null,
    val bigPictureBitmap: Bitmap? = null,
    val isClearable: Boolean = true,
    val isOngoing: Boolean = false,
    val channelId: String? = null,
    val contentIntent: PendingIntent? = null,
    val actions: List<NotificationActionModel> = emptyList(),
    val progress: Int = -1,
    val progressMax: Int = 0,
    val isIndeterminateProgress: Boolean = false,
    val isMessaging: Boolean = false,
    val senderName: String? = null,
    val conversationTitle: String? = null,
    val latestMessageTimestamp: Long? = null
) {
    val hasProgress: Boolean
        get() = progressMax > 0 || (isIndeterminateProgress && progress >= 0) || isIndeterminateProgress
}
