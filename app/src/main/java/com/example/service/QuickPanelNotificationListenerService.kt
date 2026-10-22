package com.example.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.data.NotificationBlacklistManager
import com.example.data.PanelPreferences
import com.example.model.NotificationActionModel
import com.example.model.NotificationItem
import com.example.ui.overlay.QsExpansionStage
import androidx.core.app.NotificationCompat
import androidx.core.app.Person as CorePerson
import androidx.core.graphics.drawable.IconCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuickPanelNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationListener"

        var instance: QuickPanelNotificationListenerService? = null
            private set

        private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
        val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

        @Volatile
        var activePinnedKey: String? = null

        @Volatile
        var isAlarmOverlayActive: Boolean = false

        fun resetPinnedLock(key: String? = null) {
            if (key == null || activePinnedKey == key) {
                activePinnedKey = null
                isAlarmOverlayActive = false
                HeadsUpOverlayManager.resetPinnedLock(key)
            }
        }

        fun resetAlarmLock() {
            resetPinnedLock()
        }

        val isConnected: Boolean
            get() = instance != null

        fun isPermissionGranted(context: Context): Boolean {
            val enabledListeners = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            val myComponent = ComponentName(context, QuickPanelNotificationListenerService::class.java).flattenToString()
            return enabledListeners.contains(myComponent) || enabledListeners.contains(context.packageName)
        }

        fun openPermissionSettings(context: Context) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }

        fun dismissNotification(key: String) {
            try {
                instance?.cancelNotification(key)
                _notifications.value = _notifications.value.filterNot { it.key == key }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to dismiss notification $key: ${e.message}")
            }
        }

        fun dismissAllNotifications() {
            try {
                instance?.cancelAllNotifications()
                _notifications.value = _notifications.value.filter { !it.isClearable }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to dismiss all notifications: ${e.message}")
            }
        }

        fun launchNotification(item: NotificationItem, context: Context): Boolean {
            return try {
                if (item.contentIntent != null) {
                    val options = android.app.ActivityOptions.makeBasic()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        options.setPendingIntentBackgroundActivityStartMode(
                            android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                        )
                    }
                    item.contentIntent.send(context, 0, null, null, null, null, options.toBundle())
                    true
                } else {
                    val launchIntent = context.packageManager.getLaunchIntentForPackage(item.packageName)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                        true
                    } else false
                }
            } catch (e: Exception) {
                try {
                    val launchIntent = context.packageManager.getLaunchIntentForPackage(item.packageName)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                        true
                    } else false
                } catch (e2: Exception) {
                    Log.e(TAG, "Failed to launch notification for ${item.packageName}: ${e2.message}")
                    false
                }
            }
        }

        fun executeAction(action: NotificationActionModel, context: Context? = null): Boolean {
            return try {
                if (action.actionIntent != null) {
                    if (context != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        val options = android.app.ActivityOptions.makeBasic().apply {
                            setPendingIntentBackgroundActivityStartMode(
                                android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                            )
                        }
                        action.actionIntent.send(context, 0, null, null, null, null, options.toBundle())
                    } else {
                        action.actionIntent.send()
                    }
                    true
                } else false
            } catch (e: Exception) {
                Log.e(TAG, "Failed to execute notification action: ${e.message}")
                false
            }
        }

        fun hidePackage(packageName: String, context: Context? = null) {
            NotificationBlacklistManager.hidePackage(packageName, context)
            _notifications.value = _notifications.value.filterNot { it.packageName == packageName }
            instance?.refreshActiveNotifications()
        }

        fun unhidePackage(packageName: String, context: Context? = null) {
            NotificationBlacklistManager.unhidePackage(packageName, context)
            instance?.refreshActiveNotifications()
        }

        fun hideChannel(channelId: String, context: Context? = null) {
            NotificationBlacklistManager.hideChannel(channelId, context)
            _notifications.value = _notifications.value.filterNot { it.channelId == channelId }
            instance?.refreshActiveNotifications()
        }

        fun unhideChannel(channelId: String, context: Context? = null) {
            NotificationBlacklistManager.unhideChannel(channelId, context)
            instance?.refreshActiveNotifications()
        }

        fun isMediaNotification(sbn: StatusBarNotification?, n: Notification?): Boolean {
            if (sbn == null || n == null) return false
            if (n.category == Notification.CATEGORY_TRANSPORT) return true

            val extras = n.extras
            if (extras != null) {
                if (extras.containsKey(Notification.EXTRA_MEDIA_SESSION) ||
                    extras.get("android.mediaSession") != null ||
                    extras.get(Notification.EXTRA_MEDIA_SESSION) != null
                ) {
                    return true
                }

                val template = extras.getCharSequence("android.template")?.toString()
                    ?: extras.getString("android.template")
                if (template != null && template.contains("MediaStyle", ignoreCase = true)) {
                    return true
                }
            }

            val pkg = sbn.packageName
            if (pkg.contains("spotify") || pkg.contains("youtube.music") || pkg.contains("music") || pkg.contains("audio") || pkg.contains("podcast") || pkg.contains("player")) {
                if (n.category == Notification.CATEGORY_TRANSPORT || (extras != null && extras.containsKey(Notification.EXTRA_MEDIA_SESSION))) {
                    return true
                }
            }

            return false
        }

        private fun areNotificationListsEqual(
            oldList: List<NotificationItem>,
            newList: List<NotificationItem>
        ): Boolean {
            if (oldList.size != newList.size) return false
            for (i in oldList.indices) {
                val old = oldList[i]
                val new = newList[i]
                if (old.key != new.key ||
                    old.title != new.title ||
                    old.text != new.text ||
                    old.subText != new.subText ||
                    old.appName != new.appName ||
                    old.postTime != new.postTime ||
                    old.isMessaging != new.isMessaging ||
                    old.isOngoing != new.isOngoing ||
                    old.isClearable != new.isClearable ||
                    old.progress != new.progress ||
                    old.progressMax != new.progressMax ||
                    old.isIndeterminateProgress != new.isIndeterminateProgress ||
                    old.actions.size != new.actions.size
                ) {
                    return false
                }
                for (j in old.actions.indices) {
                    if (old.actions[j].title != new.actions[j].title) return false
                }
            }
            return true
        }

        fun getAllActiveOngoingNotifications(context: Context): List<NotificationItem> {
            val sService = instance ?: return emptyList()
            val sbns = sService.activeNotifications ?: return emptyList()
            val pm = context.packageManager
            val list = mutableListOf<NotificationItem>()

            for (sbn in sbns) {
                val n = sbn.notification ?: continue
                if (isMediaNotification(sbn, n)) continue
                val extras = n.extras ?: continue

                // We only care about ongoing/persistent notifications
                val isOngoing = (n.flags and Notification.FLAG_ONGOING_EVENT != 0) ||
                        (n.flags and Notification.FLAG_NO_CLEAR != 0) ||
                        !sbn.isClearable

                if (!isOngoing) continue

                val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) n.channelId else null
                val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
                    ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()
                    ?: ""
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
                    ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
                    ?: ""
                val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()

                val appName = try {
                    val appInfo = pm.getApplicationInfo(sbn.packageName, 0)
                    val label = pm.getApplicationLabel(appInfo).toString()
                    if (label.isNotBlank()) label else sbn.packageName
                } catch (e: Exception) {
                    sbn.packageName
                }

                val appIcon = try {
                    pm.getApplicationIcon(sbn.packageName)
                } catch (e: Exception) {
                    try {
                        n.smallIcon?.loadDrawable(context)
                    } catch (e2: Exception) {
                        null
                    }
                }

                val progress = extras.getInt(Notification.EXTRA_PROGRESS, -1)
                val progressMax = extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0)
                val isIndeterminateProgress = extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE, false)

                list.add(
                    NotificationItem(
                        key = sbn.key ?: "${sbn.packageName}_${sbn.id}",
                        id = sbn.id,
                        packageName = sbn.packageName,
                        appName = appName,
                        title = if (title.isBlank()) appName else title,
                        text = text,
                        subText = subText,
                        postTime = sbn.postTime,
                        appIcon = appIcon,
                        isClearable = sbn.isClearable,
                        isOngoing = true,
                        channelId = channelId,
                        contentIntent = n.contentIntent,
                        progress = progress,
                        progressMax = progressMax,
                        isIndeterminateProgress = isIndeterminateProgress
                    )
                )
            }
            return list
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        try {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY)
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        Log.d(TAG, "Notification listener connected")
        NotificationBlacklistManager.init(this)
        MediaSessionManagerHelper.init(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val allFilters = FLAG_FILTER_TYPE_CONVERSATIONS or
                        FLAG_FILTER_TYPE_ALERTING or
                        FLAG_FILTER_TYPE_ONGOING or
                        FLAG_FILTER_TYPE_SILENT
                migrateNotificationFilter(allFilters, null)
                Log.d(TAG, "Notification listener filter migrated for ongoing and silent notifications")
            } catch (e: Throwable) {
                Log.d(TAG, "Filter migration note: ${e.message}")
            }
        }

        refreshActiveNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
        Log.d(TAG, "Notification listener disconnected")
    }

    override fun onNotificationRankingUpdate(rankingMap: RankingMap?) {
        super.onNotificationRankingUpdate(rankingMap)
        refreshActiveNotifications()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?, rankingMap: RankingMap?) {
        super.onNotificationPosted(sbn, rankingMap)
        if (sbn == null) return
        val n = sbn.notification ?: return

        // Skip 1-second ticker/progress updates from active media notifications from flooding the shade or heads-up
        if (isMediaNotification(sbn, n)) {
            return
        }

        refreshActiveNotifications()
        triggerHeadsUpIfNeeded(sbn, rankingMap)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return
        val n = sbn.notification ?: return

        // Skip 1-second ticker/progress updates from active media notifications from flooding the shade or heads-up
        if (isMediaNotification(sbn, n)) {
            return
        }

        refreshActiveNotifications()
        triggerHeadsUpIfNeeded(sbn, currentRanking)
    }

    private fun triggerHeadsUpIfNeeded(sbn: StatusBarNotification, rankingMap: RankingMap? = null) {
        if (!PanelPreferences.enableHeadsUpOverlay.value) return

        val n = sbn.notification ?: return
        if (isMediaNotification(sbn, n)) return
        val extras = n.extras ?: return

        // Ignore notifications from self to prevent loops
        if (sbn.packageName == packageName) return

        val category = n.category ?: ""
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()
            ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: ""
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()

        val isAlarm = category == Notification.CATEGORY_ALARM ||
                sbn.packageName.contains("deskclock", ignoreCase = true) ||
                sbn.packageName.contains("clock", ignoreCase = true) ||
                sbn.packageName.contains("alarm", ignoreCase = true) ||
                title.contains("alarm", ignoreCase = true) ||
                title.contains("snooze", ignoreCase = true) ||
                title.contains("スヌーズ", ignoreCase = true) ||
                title.contains("アラーム", ignoreCase = true) ||
                subText?.contains("alarm", ignoreCase = true) == true

        // Check if this is a missed call notification (which should dismiss any active call heads-up)
        val isMissedCall = category == Notification.CATEGORY_MISSED_CALL ||
                category.equals("missed_call", ignoreCase = true) ||
                title.contains("missed call", ignoreCase = true) ||
                title.contains("пропущенный", ignoreCase = true) ||
                text.contains("missed call", ignoreCase = true) ||
                text.contains("пропущенный", ignoreCase = true)

        if (isMissedCall) {
            HeadsUpOverlayManager.dismissIfMatching(sbn.key)
            resetPinnedLock(sbn.key)
        }

        // Comprehensive incoming call detection:
        val template = extras.getString(Notification.EXTRA_TEMPLATE) ?: ""
        val isCallStyle = template.contains("CallStyle", ignoreCase = true) ||
                extras.containsKey("android.callType")

        val hasCallPerson = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                extras.getParcelable("android.callPerson", android.app.Person::class.java) != null
            } catch (_: Throwable) {
                extras.containsKey("android.callPerson")
            }
        } else extras.containsKey("android.callPerson")

        val hasAnswerIntent = extras.containsKey("android.answerIntent") ||
                extras.containsKey("android.declineIntent")

        val isCallCategory = category == Notification.CATEGORY_CALL ||
                category.equals("call", ignoreCase = true)

        val isDialerOrPhonePkg = sbn.packageName.contains("dialer", ignoreCase = true) ||
                sbn.packageName.contains("incallui", ignoreCase = true) ||
                sbn.packageName.contains("telecom", ignoreCase = true) ||
                sbn.packageName.contains("phone", ignoreCase = true) ||
                sbn.packageName == "com.google.android.dialer" ||
                sbn.packageName == "com.samsung.android.incallui" ||
                sbn.packageName == "com.samsung.android.dialer" ||
                sbn.packageName == "com.android.phone" ||
                sbn.packageName == "com.android.server.telecom"

        val hasAnswerDeclineActions = n.actions?.any { action ->
            val at = action.title?.toString() ?: ""
            at.contains("Answer", ignoreCase = true) ||
            at.contains("Decline", ignoreCase = true) ||
            at.contains("Accept", ignoreCase = true) ||
            at.contains("Reject", ignoreCase = true) ||
            at.contains("Ответить", ignoreCase = true) ||
            at.contains("Отклонить", ignoreCase = true) ||
            at.contains("Responder", ignoreCase = true) ||
            at.contains("Rechazar", ignoreCase = true) ||
            at.contains("Répondre", ignoreCase = true) ||
            at.contains("Refuser", ignoreCase = true) ||
            at.contains("Annehmen", ignoreCase = true) ||
            at.contains("Ablehnen", ignoreCase = true) ||
            at.contains("Atender", ignoreCase = true) ||
            at.contains("Recusar", ignoreCase = true) ||
            at.contains("Rispondi", ignoreCase = true) ||
            at.contains("Rifiuta", ignoreCase = true) ||
            at.contains("接听", ignoreCase = true) ||
            at.contains("挂断", ignoreCase = true) ||
            at.contains("통화", ignoreCase = true) ||
            at.contains("응답", ignoreCase = true) ||
            at.contains("거절", ignoreCase = true) ||
            at.contains("応答", ignoreCase = true) ||
            at.contains("拒否", ignoreCase = true) ||
            at.contains("Yanıtla", ignoreCase = true) ||
            at.contains("Reddet", ignoreCase = true) ||
            at.contains("Odbierz", ignoreCase = true) ||
            at.contains("Odrzuć", ignoreCase = true) ||
            at.contains("Відповісти", ignoreCase = true) ||
            at.contains("Відхилити", ignoreCase = true)
        } == true

        val hasCallText = title.contains("incoming call", ignoreCase = true) ||
                title.contains("calling", ignoreCase = true) ||
                title.contains("звонок", ignoreCase = true) ||
                title.contains("вызов", ignoreCase = true) ||
                title.contains("llamada", ignoreCase = true) ||
                title.contains("appel", ignoreCase = true) ||
                title.contains("anruf", ignoreCase = true) ||
                title.contains("chiamata", ignoreCase = true) ||
                title.contains("着信", ignoreCase = true) ||
                title.contains("通話", ignoreCase = true) ||
                text.contains("incoming call", ignoreCase = true) ||
                text.contains("calling", ignoreCase = true) ||
                text.contains("звонок", ignoreCase = true) ||
                text.contains("вызов", ignoreCase = true) ||
                text.contains("llamada", ignoreCase = true) ||
                text.contains("appel", ignoreCase = true) ||
                text.contains("anruf", ignoreCase = true) ||
                text.contains("着信", ignoreCase = true)

        val isCall = !isMissedCall && (
                isCallStyle ||
                hasCallPerson ||
                hasAnswerIntent ||
                (isCallCategory && (hasAnswerDeclineActions || isDialerOrPhonePkg || n.fullScreenIntent != null)) ||
                hasAnswerDeclineActions ||
                (isDialerOrPhonePkg && (n.fullScreenIntent != null || hasCallText))
        )

        val isCallOrAlarm = isAlarm || isCall

        // Ignore group summary container notifications from chat apps (e.g. Telegram, WhatsApp, Gmail).
        val isGroupSummary = (n.flags and Notification.FLAG_GROUP_SUMMARY != 0)
        if (isGroupSummary && !isCallOrAlarm) {
            Log.d(TAG, "Ignoring group summary notification from ${sbn.packageName} for Heads-Up")
            return
        }

        val isOngoing = (n.flags and Notification.FLAG_ONGOING_EVENT != 0) ||
                (n.flags and Notification.FLAG_NO_CLEAR != 0) ||
                !sbn.isClearable

        if (isOngoing && !isCallOrAlarm) return

        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) n.channelId else null
        if (NotificationBlacklistManager.isBlacklisted(sbn.packageName, channelId)) return

        // Check importance or high priority or communication categories
        var isHighImportance = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val ranking = Ranking()
                if (rankingMap?.getRanking(sbn.key, ranking) == true || currentRanking?.getRanking(sbn.key, ranking) == true) {
                    if (ranking.importance >= NotificationManager.IMPORTANCE_HIGH) {
                        isHighImportance = true
                    }
                }
            } catch (e: Exception) {
                // Ignore ranking failure
            }
        }

        val isHighPriority = (n.priority >= Notification.PRIORITY_HIGH) ||
                (n.flags and Notification.FLAG_HIGH_PRIORITY != 0) ||
                (n.fullScreenIntent != null)

        val isKeyCategory = isCallOrAlarm ||
                category == Notification.CATEGORY_MESSAGE ||
                category == Notification.CATEGORY_CALL ||
                category == Notification.CATEGORY_ALARM ||
                category == Notification.CATEGORY_EMAIL ||
                category == Notification.CATEGORY_REMINDER ||
                category == Notification.CATEGORY_EVENT

        if (!isHighImportance && !isHighPriority && !isKeyCategory) {
            return
        }

        // Extract caller name if this is a CallStyle notification where EXTRA_TITLE is empty
        var callCallerName: String? = null
        if (isCall) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    val person = extras.getParcelable("android.callPerson", android.app.Person::class.java)
                    callCallerName = person?.name?.toString()
                } catch (_: Throwable) {}
            }
            if (callCallerName.isNullOrBlank()) {
                callCallerName = extras.getCharSequence("android.callPerson")?.toString()
            }
        }

        // Extract latest individual message cleanly if MessagingStyle is present
        val messagingDetails = extractMessagingDetails(n, extras, sbn, title, text)
        val resolvedTitle = messagingDetails.title
        val resolvedText = messagingDetails.text

        val finalTitle = when {
            !callCallerName.isNullOrBlank() -> callCallerName
            resolvedTitle.isNotBlank() -> resolvedTitle
            else -> ""
        }

        val finalText = when {
            resolvedText.isNotBlank() -> resolvedText
            !subText.isNullOrBlank() -> subText
            isCall -> "Incoming call"
            else -> ""
        }

        if (finalTitle.isBlank() && finalText.isBlank() && n.fullScreenIntent == null) {
            return
        }

        val pm = packageManager
        val appName = try {
            val appInfo = pm.getApplicationInfo(sbn.packageName, 0)
            val label = pm.getApplicationLabel(appInfo).toString()
            if (label.isNotBlank()) label else cleanPackageName(sbn.packageName)
        } catch (e: Exception) {
            cleanPackageName(sbn.packageName)
        }

        val appIcon = try {
            pm.getApplicationIcon(sbn.packageName)
        } catch (e: Exception) {
            try {
                n.smallIcon?.loadDrawable(this)
            } catch (e2: Exception) {
                null
            }
        }

        val avatarBitmap = messagingDetails.avatarBitmap ?: extractLargeIconOrAvatar(n, extras)

        val actionsList = mutableListOf<NotificationActionModel>()
        n.actions?.take(4)?.forEach { action ->
            val actionTitle = action.title?.toString() ?: ""
            if (actionTitle.isNotBlank()) {
                actionsList.add(
                    NotificationActionModel(
                        title = actionTitle,
                        actionIntent = action.actionIntent
                    )
                )
            }
        }

        // If CallStyle has answer/decline intents and actions are missing, populate them automatically
        if (isCall) {
            val hasAnswerInList = actionsList.any {
                it.title.contains("Answer", ignoreCase = true) ||
                it.title.contains("Accept", ignoreCase = true) ||
                it.title.contains("Ответить", ignoreCase = true) ||
                it.title.contains("Responder", ignoreCase = true) ||
                it.title.contains("接听", ignoreCase = true)
            }
            val hasDeclineInList = actionsList.any {
                it.title.contains("Decline", ignoreCase = true) ||
                it.title.contains("Reject", ignoreCase = true) ||
                it.title.contains("Отклонить", ignoreCase = true) ||
                it.title.contains("Rechazar", ignoreCase = true) ||
                it.title.contains("挂断", ignoreCase = true)
            }

            @Suppress("DEPRECATION")
            val answerPendingIntent: PendingIntent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                try {
                    extras.getParcelable("android.answerIntent", PendingIntent::class.java)
                } catch (_: Throwable) {
                    extras.getParcelable("android.answerIntent") as? PendingIntent
                }
            } else {
                extras.getParcelable("android.answerIntent") as? PendingIntent
            }

            @Suppress("DEPRECATION")
            val declinePendingIntent: PendingIntent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                try {
                    extras.getParcelable("android.declineIntent", PendingIntent::class.java)
                } catch (_: Throwable) {
                    extras.getParcelable("android.declineIntent") as? PendingIntent
                }
            } else {
                extras.getParcelable("android.declineIntent") as? PendingIntent
            }

            if (!hasAnswerInList && answerPendingIntent != null) {
                actionsList.add(0, NotificationActionModel(title = "Answer", actionIntent = answerPendingIntent))
            }
            if (!hasDeclineInList && declinePendingIntent != null) {
                actionsList.add(NotificationActionModel(title = "Decline", actionIntent = declinePendingIntent))
            }
        }

        // Generate content-specific ID so identical internal updates don't reset timers or flicker
        val contentHash = "${finalTitle.hashCode()}_${finalText.hashCode()}"
        val headsUpData = HeadsUpOverlayManager.HeadsUpData(
            id = "${sbn.key}_$contentHash",
            key = sbn.key,
            packageName = sbn.packageName,
            appName = appName,
            title = if (finalTitle.isNotBlank()) finalTitle else appName,
            text = finalText,
            subText = subText,
            appIcon = appIcon,
            avatarBitmap = avatarBitmap,
            contentIntent = n.contentIntent,
            fullScreenIntent = n.fullScreenIntent,
            actions = actionsList,
            postTime = sbn.postTime,
            isAlarm = isAlarm,
            isCall = isCall
        )

        Handler(Looper.getMainLooper()).post {
            HeadsUpOverlayManager.showHeadsUp(
                context = this,
                data = headsUpData,
                onTriggerQs = {
                    if (QuickPanelAccessibilityService.isRunning) {
                        QuickPanelAccessibilityService.showCustomPanel(QsExpansionStage.FULL_QS)
                    } else if (QuickSettingsOverlayService.isRunning) {
                        val expandIntent = Intent(this, QuickSettingsOverlayService::class.java).apply {
                            action = QuickSettingsOverlayService.ACTION_TOGGLE_EXPAND
                        }
                        startService(expandIntent)
                    }
                },
                onActionClick = { action ->
                    resetPinnedLock(sbn.key)
                    executeAction(action, this)
                }
            )
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        refreshActiveNotifications()
        if (sbn != null) {
            resetPinnedLock(sbn.key)
            // Dismiss ONLY if the specific notification key matches (never loosely dismiss by packageName)
            HeadsUpOverlayManager.dismissIfMatching(sbn.key)
            HeadsUpOverlayManager.onNotificationKeyRemoved(sbn.key)
        }
    }

    fun refreshActiveNotifications() {
        scope.launch(Dispatchers.IO) {
            try {
                NotificationBlacklistManager.init(this@QuickPanelNotificationListenerService)
                val sbns = activeNotifications ?: return@launch
                val pm = packageManager
                val list = mutableListOf<NotificationItem>()

                for (sbn in sbns) {
                    val n = sbn.notification ?: continue

                    // Skip media playback notifications from duplicating in the standard notification list
                    if (isMediaNotification(sbn, n)) {
                        continue
                    }

                    val extras = n.extras ?: continue

                    val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) n.channelId else null

                    // 1. Blacklist check: hide persistent/system notifications per package or channel
                    if (NotificationBlacklistManager.isBlacklisted(sbn.packageName, channelId)) {
                        continue
                    }

                    // 2. Keep notification history: DO NOT drop older child chat notifications!
                    // If this is a group summary wrapper and child notifications for this package exist,
                    // suppress the redundant generic group summary wrapper so individual chats remain in the shade.
                    val isGroupSummary = (n.flags and Notification.FLAG_GROUP_SUMMARY != 0)
                    val hasChildChatNotifications = sbns.any { other ->
                        other.key != sbn.key &&
                        other.packageName == sbn.packageName &&
                        (other.notification.flags and Notification.FLAG_GROUP_SUMMARY == 0)
                    }
                    if (isGroupSummary && hasChildChatNotifications) {
                        continue
                    }

                    val rawTitle = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
                        ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()
                        ?: ""
                    val rawText = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
                        ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
                        ?: ""
                    val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()

                    val isOngoing = (n.flags and Notification.FLAG_ONGOING_EVENT != 0) ||
                            (n.flags and Notification.FLAG_NO_CLEAR != 0) ||
                            !sbn.isClearable

                    val progress = extras.getInt(Notification.EXTRA_PROGRESS, -1)
                    val progressMax = extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0)
                    val isIndeterminateProgress = extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE, false)
                    val hasProgress = progressMax > 0 || (isIndeterminateProgress && progress >= 0) || isIndeterminateProgress

                    val infoText = extras.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString()
                    val summaryText = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString()

                    // Extract single latest message for MessagingStyle notifications (Telegram, WhatsApp, etc.)
                    val messagingDetails = extractMessagingDetails(n, extras, sbn, rawTitle, rawText)

                    // Skip genuinely empty notifications (never drop ongoing, progress, or messaging notifications)
                    if (messagingDetails.title.isBlank() && messagingDetails.text.isBlank() && !hasProgress && !isOngoing && !messagingDetails.isMessaging) {
                        continue
                    }

                    // 3. Resolve Human-Readable Application Name (prevent raw package names)
                    val appName = try {
                        val appInfo = pm.getApplicationInfo(sbn.packageName, 0)
                        val label = pm.getApplicationLabel(appInfo).toString()
                        if (label.isNotBlank()) label else sbn.packageName
                    } catch (e: Exception) {
                        try {
                            val substName = extras.getCharSequence("android.substName")?.toString()
                            if (!substName.isNullOrBlank()) {
                                substName
                            } else {
                                val packageContext = createPackageContext(sbn.packageName, 0)
                                val lbl = pm.getApplicationLabel(packageContext.applicationInfo).toString()
                                if (lbl.isNotBlank()) lbl else sbn.packageName
                            }
                        } catch (e2: Exception) {
                            sbn.packageName
                        }
                    }

                    // 4. Load App Icon with fallback to smallIcon
                    val appIcon = try {
                        pm.getApplicationIcon(sbn.packageName)
                    } catch (e: Exception) {
                        try {
                            n.smallIcon?.loadDrawable(this@QuickPanelNotificationListenerService)
                        } catch (e2: Exception) {
                            null
                        }
                    }

                    val avatarBitmap = messagingDetails.avatarBitmap
                        ?: extractLargeIconOrAvatar(n, extras)
                    val bigPictureBitmap = extractBigPicture(extras)

                    val resolvedTitle = when {
                        messagingDetails.isMessaging && messagingDetails.title.isNotBlank() -> messagingDetails.title
                        rawTitle.isNotBlank() -> rawTitle
                        !messagingDetails.conversationTitle.isNullOrBlank() -> messagingDetails.conversationTitle
                        !subText.isNullOrBlank() -> subText
                        !infoText.isNullOrBlank() -> infoText
                        hasProgress -> "Downloading"
                        isOngoing -> appName
                        else -> appName
                    }

                    val resolvedText = when {
                        messagingDetails.isMessaging && messagingDetails.text.isNotBlank() -> messagingDetails.text
                        rawText.isNotBlank() -> rawText
                        !infoText.isNullOrBlank() && resolvedTitle != infoText -> infoText
                        !subText.isNullOrBlank() && resolvedTitle != subText -> subText
                        !summaryText.isNullOrBlank() -> summaryText
                        hasProgress && progressMax > 0 -> "$progress / $progressMax"
                        hasProgress && isIndeterminateProgress -> "In progress"
                        isOngoing -> "Running in background"
                        else -> ""
                    }

                    // Effective timestamp: latest message time if messaging, otherwise max of postTime and when
                    val effectiveTime = when {
                        messagingDetails.timestamp != null && messagingDetails.timestamp > 0L -> messagingDetails.timestamp
                        n.`when` > 0L -> maxOf(sbn.postTime, n.`when`)
                        else -> sbn.postTime
                    }

                    val actionsList = mutableListOf<NotificationActionModel>()
                    n.actions?.take(3)?.forEach { action ->
                        val actionTitle = action.title?.toString() ?: ""
                        if (actionTitle.isNotBlank()) {
                            actionsList.add(
                                NotificationActionModel(
                                    title = actionTitle,
                                    actionIntent = action.actionIntent
                                )
                            )
                        }
                    }

                    list.add(
                        NotificationItem(
                            key = sbn.key,
                            id = sbn.id,
                            packageName = sbn.packageName,
                            appName = appName,
                            title = resolvedTitle,
                            text = resolvedText,
                            subText = subText,
                            postTime = effectiveTime,
                            appIcon = appIcon,
                            avatarBitmap = avatarBitmap,
                            bigPictureBitmap = bigPictureBitmap,
                            isClearable = sbn.isClearable,
                            isOngoing = isOngoing,
                            channelId = channelId,
                            contentIntent = n.contentIntent,
                            actions = actionsList,
                            progress = progress,
                            progressMax = progressMax,
                            isIndeterminateProgress = isIndeterminateProgress,
                            isMessaging = messagingDetails.isMessaging,
                            senderName = messagingDetails.senderName,
                            conversationTitle = messagingDetails.conversationTitle,
                            latestMessageTimestamp = messagingDetails.timestamp
                        )
                    )
                }

                // Chat ordering: Move notifications from the most recently updated chat/sender to the top
                // of the messaging section based on the latest message timestamp.
                // Keep notification history: All older existing chat notifications remain preserved in the shade.
                val sortedList = list.sortedWith(
                    compareByDescending<NotificationItem> { it.isMessaging }
                        .thenByDescending { it.postTime }
                )

                if (!areNotificationListsEqual(_notifications.value, sortedList)) {
                    _notifications.value = sortedList
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing active notifications: ${e.message}")
            }
        }
    }

    private fun cleanPackageName(packageName: String): String {
        val lastSegment = packageName.substringAfterLast('.')
        return if (lastSegment.length > 2) {
            lastSegment.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        } else {
            packageName
        }
    }

    private fun extractLargeIconOrAvatar(n: Notification, extras: Bundle): Bitmap? {
        // 1. Try Notification.EXTRA_LARGE_ICON_BIG ("android.largeIcon.big")
        try {
            val bigIconObj = extras.get(Notification.EXTRA_LARGE_ICON_BIG)
            val bmp = parseBitmapOrIcon(bigIconObj)
            if (bmp != null) return bmp
        } catch (e: Throwable) {
            // Ignore
        }

        // 2. Try Notification.EXTRA_LARGE_ICON ("android.largeIcon")
        try {
            val largeIconObj = extras.get(Notification.EXTRA_LARGE_ICON)
            val bmp = parseBitmapOrIcon(largeIconObj)
            if (bmp != null) return bmp
        } catch (e: Throwable) {
            // Ignore
        }

        // 3. Try MessagingStyle person icons (most recent message sender)
        try {
            val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
            if (messages != null && messages.isNotEmpty()) {
                for (i in messages.indices.reversed()) {
                    val msgBundle = messages[i] as? Bundle ?: continue
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        try {
                            val person = msgBundle.getParcelable("sender_person", android.app.Person::class.java)
                            val icon = person?.icon
                            val bmp = parseBitmapOrIcon(icon)
                            if (bmp != null) return bmp
                        } catch (e: Throwable) {}
                    }
                    val senderPerson = msgBundle.get("sender_person")
                    val bmp = parseBitmapOrIcon(senderPerson)
                    if (bmp != null) return bmp
                }
            }
        } catch (e: Throwable) {
            // Ignore
        }

        // 4. Try Notification.EXTRA_MESSAGING_PERSON (API 28+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val person = extras.getParcelable(Notification.EXTRA_MESSAGING_PERSON, android.app.Person::class.java)
                val icon = person?.icon
                val bmp = parseBitmapOrIcon(icon)
                if (bmp != null) return bmp
            } catch (e: Throwable) {}
        }

        // 5. Try Notification.getLargeIcon()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val icon = n.largeIcon
                val bmp = parseBitmapOrIcon(icon)
                if (bmp != null) return bmp
            } catch (e: Throwable) {}
        }

        return null
    }

    private fun extractBigPicture(extras: Bundle): Bitmap? {
        // 1. Try Notification.EXTRA_PICTURE ("android.picture")
        try {
            val pictureObj = extras.get(Notification.EXTRA_PICTURE)
            val bmp = parseBitmapOrIcon(pictureObj)
            if (bmp != null) return bmp
        } catch (e: Throwable) {
            // Ignore
        }

        // 2. Try Notification.EXTRA_BIG_PICTURE_BITMAP or android.bigPictureBitmap
        try {
            val bigPictureBmp = extras.get("android.bigPictureBitmap") ?: extras.get("android.pictureIcon")
            val bmp = parseBitmapOrIcon(bigPictureBmp)
            if (bmp != null) return bmp
        } catch (e: Throwable) {
            // Ignore
        }

        // 3. Try BigPicture / Image attachment in messages
        try {
            val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
            if (messages != null) {
                for (i in messages.indices.reversed()) {
                    val msgBundle = messages[i] as? Bundle ?: continue
                    val dataUri = msgBundle.getParcelable<Uri>("data_uri")
                    val dataType = msgBundle.getString("data_type")
                    if (dataUri != null && dataType?.startsWith("image/") == true) {
                        val bmp = parseBitmapOrIcon(dataUri)
                        if (bmp != null) return bmp
                    }
                }
            }
        } catch (e: Throwable) {}

        return null
    }

    data class MessagingDetails(
        val isMessaging: Boolean,
        val title: String,
        val text: String,
        val senderName: String?,
        val conversationTitle: String?,
        val timestamp: Long?,
        val avatarBitmap: Bitmap?
    )

    /**
     * Extracts the single latest incoming message from a MessagingStyle notification (Telegram, WhatsApp, etc.).
     * Returns the newest individual message content, sender name, timestamp, and avatar.
     */
    private fun extractMessagingDetails(
        n: Notification,
        extras: Bundle,
        sbn: StatusBarNotification,
        fallbackTitle: String,
        fallbackText: String
    ): MessagingDetails {
        val messagingStyle = try {
            NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(n)
        } catch (_: Throwable) {
            null
        }

        var latestMessageText: String? = null
        var latestMessageTime: Long? = null
        var senderName: String? = null
        val convTitle: String? = messagingStyle?.conversationTitle?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
        var avatarBmp: Bitmap? = null
        var isMessaging = messagingStyle != null

        // 1. Primary: Extract latest message from NotificationCompat.MessagingStyle
        if (messagingStyle != null && messagingStyle.messages.isNotEmpty()) {
            val lastMsg = messagingStyle.messages.lastOrNull()
            if (lastMsg != null) {
                val textCandidate = lastMsg.text?.toString()
                if (!textCandidate.isNullOrBlank()) {
                    latestMessageText = textCandidate
                } else if (!lastMsg.dataMimeType.isNullOrBlank()) {
                    latestMessageText = when {
                        lastMsg.dataMimeType?.startsWith("image/") == true -> "Photo"
                        lastMsg.dataMimeType?.startsWith("audio/") == true -> "Voice message"
                        lastMsg.dataMimeType?.startsWith("video/") == true -> "Video"
                        else -> "Media attachment"
                    }
                }
                if (lastMsg.timestamp > 0L) {
                    latestMessageTime = lastMsg.timestamp
                }
                val person = lastMsg.person
                senderName = person?.name?.toString() ?: lastMsg.sender?.toString()
                if (person?.icon != null) {
                    avatarBmp = parseBitmapOrIcon(person.icon)
                }
            }
        }

        // 2. Secondary: Fallback to Notification.EXTRA_MESSAGES bundle array
        if (latestMessageText == null) {
            try {
                val rawMessages = when (val raw = extras.get(Notification.EXTRA_MESSAGES)) {
                    is Array<*> -> raw.toList()
                    is List<*> -> raw
                    else -> null
                }
                if (!rawMessages.isNullOrEmpty()) {
                    isMessaging = true
                    val lastMsgBundle = rawMessages.filterIsInstance<Bundle>().lastOrNull()
                    if (lastMsgBundle != null) {
                        val textCandidate = lastMsgBundle.getCharSequence("text")?.toString()
                        val type = lastMsgBundle.getString("type")
                        if (!textCandidate.isNullOrBlank()) {
                            latestMessageText = textCandidate
                        } else if (!type.isNullOrBlank()) {
                            latestMessageText = when {
                                type.startsWith("image/") -> "Photo"
                                type.startsWith("audio/") -> "Voice message"
                                type.startsWith("video/") -> "Video"
                                else -> "Media attachment"
                            }
                        }
                        val time = lastMsgBundle.getLong("time", 0L)
                        if (time > 0L) {
                            latestMessageTime = time
                        }
                        val senderPerson = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            try {
                                lastMsgBundle.getParcelable("sender_person", android.app.Person::class.java)
                            } catch (_: Throwable) { null }
                        } else null

                        senderName = senderPerson?.name?.toString()
                            ?: lastMsgBundle.getCharSequence("sender")?.toString()

                        if (avatarBmp == null) {
                            avatarBmp = parseBitmapOrIcon(senderPerson)
                                ?: parseBitmapOrIcon(lastMsgBundle.get("sender_person"))
                        }
                    }
                }
            } catch (_: Throwable) {}
        }

        // 3. Fallback check for template, category, or messaging packages
        val template = extras.getString(Notification.EXTRA_TEMPLATE) ?: ""
        val isCategoryMessage = n.category == Notification.CATEGORY_MESSAGE
        val isTemplateMessaging = template.contains("MessagingStyle", ignoreCase = true)
        val isMessagingPackage = sbn.packageName.contains("telegram", ignoreCase = true) ||
                sbn.packageName.contains("whatsapp", ignoreCase = true) ||
                sbn.packageName.contains("signal", ignoreCase = true) ||
                sbn.packageName.contains("messaging", ignoreCase = true) ||
                sbn.packageName.contains("viber", ignoreCase = true) ||
                sbn.packageName.contains("discord", ignoreCase = true)

        if (isCategoryMessage || isTemplateMessaging || (isMessagingPackage && !convTitle.isNullOrBlank())) {
            isMessaging = true
        }

        // 4. Resolve Title (clean away generic summary counts like "(2 messages)")
        val cleanFallbackTitle = fallbackTitle.replace(
            Regex("\\s*\\(\\d+\\s*(?:messages?|msgs?|новы[хe]\\s*сообщен[ияй]+|сообщен[ияй]+)?\\)", RegexOption.IGNORE_CASE),
            ""
        ).trim()

        val resolvedTitle = when {
            !convTitle.isNullOrBlank() && !senderName.isNullOrBlank() && convTitle != senderName -> "$convTitle ($senderName)"
            !convTitle.isNullOrBlank() -> convTitle
            !senderName.isNullOrBlank() -> senderName
            cleanFallbackTitle.isNotBlank() -> cleanFallbackTitle
            else -> fallbackTitle
        }

        val resolvedText = when {
            !latestMessageText.isNullOrBlank() -> latestMessageText
            fallbackText.isNotBlank() -> fallbackText
            else -> ""
        }

        return MessagingDetails(
            isMessaging = isMessaging,
            title = resolvedTitle,
            text = resolvedText,
            senderName = senderName,
            conversationTitle = convTitle,
            timestamp = latestMessageTime,
            avatarBitmap = avatarBmp
        )
    }

    private fun parseBitmapOrIcon(obj: Any?): Bitmap? {
        if (obj == null) return null
        if (obj is Bitmap) {
            return try {
                if (!obj.isRecycled) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && obj.config == Bitmap.Config.HARDWARE) {
                        obj.copy(Bitmap.Config.ARGB_8888, false)
                    } else {
                        obj
                    }
                } else null
            } catch (_: Throwable) {
                null
            }
        }
        if (obj is Icon) {
            try {
                val drawable = obj.loadDrawable(this)
                if (drawable != null) {
                    return drawableToBitmap(drawable)
                }
            } catch (e: Throwable) {
                Log.d(TAG, "Failed loading Icon drawable: ${e.message}")
            }
        }
        if (obj is IconCompat) {
            try {
                val drawable = obj.loadDrawable(this)
                if (drawable != null) {
                    return drawableToBitmap(drawable)
                }
            } catch (_: Throwable) {}
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && obj is android.app.Person) {
            try {
                val icon = obj.icon
                if (icon != null) {
                    return parseBitmapOrIcon(icon)
                }
            } catch (e: Throwable) {}
        }
        if (obj is CorePerson) {
            try {
                val icon = obj.icon
                if (icon != null) {
                    return parseBitmapOrIcon(icon)
                }
            } catch (_: Throwable) {}
        }
        if (obj is Bundle) {
            try {
                val icon = obj.get("icon") ?: obj.get("uri") ?: obj.get("key_icon")
                if (icon != null) {
                    return parseBitmapOrIcon(icon)
                }
            } catch (e: Throwable) {}
        }
        if (obj is Drawable) {
            return drawableToBitmap(obj)
        }
        if (obj is Uri) {
            try {
                contentResolver.openInputStream(obj)?.use { stream ->
                    return android.graphics.BitmapFactory.decodeStream(stream)
                }
            } catch (e: Throwable) {}
        }
        if (obj is String && (obj.startsWith("content://") || obj.startsWith("file://"))) {
            try {
                val uri = Uri.parse(obj)
                contentResolver.openInputStream(uri)?.use { stream ->
                    return android.graphics.BitmapFactory.decodeStream(stream)
                }
            } catch (e: Throwable) {}
        }
        return null
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        return try {
            if (drawable is android.graphics.drawable.BitmapDrawable && drawable.bitmap != null && !drawable.bitmap.isRecycled) {
                val b = drawable.bitmap
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && b.config == Bitmap.Config.HARDWARE) {
                    try {
                        b.copy(Bitmap.Config.ARGB_8888, false)
                    } catch (_: Throwable) {
                        null
                    }
                } else {
                    b
                }
            } else {
                val width = if (drawable.intrinsicWidth in 1..512) drawable.intrinsicWidth else 128
                val height = if (drawable.intrinsicHeight in 1..512) drawable.intrinsicHeight else 128
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
        } catch (_: Throwable) {
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        scope.cancel()
    }
}
