package com.example.service

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import java.util.concurrent.ConcurrentHashMap
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.data.HeadsUpAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PanelPreferences
import com.example.model.NotificationActionModel
import com.example.ui.overlay.QsExpansionStage
import com.example.ui.theme.DarkUiSurface
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.MonetOnPrimary
import com.example.ui.theme.MonetPrimary
import com.example.ui.theme.MonetTertiary
import com.example.ui.theme.QuickPanelTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Isolated & Safe Custom Heads-Up Overlay Engine.
 * - Inflates lightweight Compose floating banner at the top of the screen via WindowManager.
 * - Prioritizes TYPE_ACCESSIBILITY_OVERLAY (with fallback to TYPE_APPLICATION_OVERLAY).
 * - Anti-Flickering Alarm Stabilization: In-place reactive state update without re-inflating WindowManager.
 * - One-time native suppression avoiding layout loops with System UI.
 * - Persistent pinned display for Alarms until user acts (Snooze / Stop).
 * - Full try-catch safety on WindowManager calls with error Toast notifications.
 * - Dynamic Action Buttons:
 *   * Alarms: Snooze / Stop (スヌーズ / ストップ)
 *   * Calls: Answer / Decline (応答 / 拒否)
 * - Direct PendingIntent dispatch with Android 14+ background launch permissions.
 * - Swipe-up / horizontal to dismiss.
 * - Swipe-down to directly trigger the Custom Quick Settings shade.
 */
object HeadsUpOverlayManager {

    private const val TAG = "HeadsUpOverlayManager"

    data class HeadsUpData(
        val id: String = java.util.UUID.randomUUID().toString(),
        val key: String? = null,
        val packageName: String,
        val appName: String,
        val title: String,
        val text: String,
        val subText: String? = null,
        val appIcon: Drawable? = null,
        val avatarBitmap: Bitmap? = null,
        val contentIntent: PendingIntent? = null,
        val fullScreenIntent: PendingIntent? = null,
        val actions: List<NotificationActionModel> = emptyList(),
        val postTime: Long = System.currentTimeMillis(),
        val isAlarm: Boolean = false,
        val isCall: Boolean = false
    ) {
        val isAlarmOrCall: Boolean
            get() = isAlarm || isCall
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var currentComposeView: ComposeView? = null
    private var currentLifecycleOwner: OverlayLifecycleOwner? = null
    private var currentWindowManager: WindowManager? = null
    private var currentLayoutParams: WindowManager.LayoutParams? = null
    private var currentNotificationId: String? = null
    private var dismissAnimatedCallback: (() -> Unit)? = null

    // Reactive State Holder for in-place updates (avoids destroying/re-adding WindowManager views)
    private val _currentDataFlow = MutableStateFlow<HeadsUpData?>(null)
    val currentDataFlow: StateFlow<HeadsUpData?> = _currentDataFlow.asStateFlow()

    val displayedHeadsUpKeys: MutableSet<String> = java.util.Collections.synchronizedSet(mutableSetOf<String>())

    data class NotificationAlertRecord(
        val key: String,
        val title: String,
        val text: String,
        val subText: String?,
        val postTime: Long,
        val alertedAtMillis: Long = System.currentTimeMillis()
    )

    // Tracks recently alerted notifications by key to prevent re-alerting or repeating the same message/thread
    private val recentlyAlertedMap = ConcurrentHashMap<String, NotificationAlertRecord>()

    private fun recordAlert(data: HeadsUpData) {
        val key = data.key ?: data.id
        recentlyAlertedMap[key] = NotificationAlertRecord(
            key = key,
            title = data.title,
            text = data.text,
            subText = data.subText,
            postTime = data.postTime
        )
    }

    private fun pruneOldAlertRecords() {
        val cutoff = System.currentTimeMillis() - 300_000L // 5 minutes
        recentlyAlertedMap.entries.removeIf { it.value.alertedAtMillis < cutoff }
    }

    fun onNotificationKeyRemoved(key: String?) {
        if (key == null) return
        displayedHeadsUpKeys.remove(key)
        scope.launch {
            delay(1000)
            recentlyAlertedMap.remove(key)
        }
    }

    @Volatile
    var activePinnedKey: String? = null

    @Volatile
    var isAlarmOverlayActive: Boolean = false

    fun resetPinnedLock(key: String? = null) {
        if (key != null) {
            displayedHeadsUpKeys.remove(key)
        } else {
            displayedHeadsUpKeys.clear()
        }
        if (key == null || activePinnedKey == key) {
            activePinnedKey = null
            isAlarmOverlayActive = false
            QuickPanelNotificationListenerService.activePinnedKey = null
            QuickPanelNotificationListenerService.isAlarmOverlayActive = false
        }
    }

    fun resetAlarmStateLock() {
        resetPinnedLock()
    }

    private var currentOnTriggerQs: (() -> Unit)? = null
    private var currentOnActionClick: ((NotificationActionModel) -> Unit)? = null

    /**
     * Check if an Alarm Heads-Up overlay is currently active.
     */
    fun isAlarmHeadsUpShowing(): Boolean {
        return _currentDataFlow.value?.isAlarm == true && currentComposeView != null
    }

    /**
     * Dismiss the overlay if it matches the given key (e.g., when notification is dismissed or cancelled).
     */
    fun dismissIfMatching(key: String?, packageName: String? = null) {
        key?.let { displayedHeadsUpKeys.remove(it) }
        val current = _currentDataFlow.value ?: return
        if (key != null && current.key == key) {
            Log.d(TAG, "Dismissing Heads-Up because matching notification was removed: key=$key")
            dismissHeadsUpAnimated()
        }
    }

    /**
     * Display a Custom Heads-Up banner overlay for an incoming notification.
     * Uses in-place reactive updates to prevent rapid flickering on fast notification updates (e.g. Alarms).
     */
    fun showHeadsUp(
        context: Context,
        data: HeadsUpData,
        onTriggerQs: (() -> Unit)? = null,
        onActionClick: ((NotificationActionModel) -> Unit)? = null
    ) {
        try {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY)
            com.example.system.CpuPerformanceManager.getInstance(context).onInteractionStart()
        } catch (_: Exception) {}
        if (!PanelPreferences.enableHeadsUpOverlay.value && !data.isCall && !data.isAlarm) {
            Log.d(TAG, "Heads-Up overlay is disabled in PanelPreferences.")
            return
        }

        // Screen should turn on only for incoming call and alarm notifications
        if (ScreenWakeHelper.isScreenOff(context) && !data.isCall && !data.isAlarm) {
            Log.d(TAG, "Screen is off and notification is not call or alarm: skipping heads-up overlay to keep screen off.")
            return
        }

        val targetKey = data.key ?: data.id

        // 1. Prevent Loop on Duplicate Keys / Active Window Updates:
        // If an onNotificationPosted arrives for an already displayed key, update the content smoothly in-place
        // WITHOUT removing or re-inflating the view in WindowManager.
        if (currentComposeView != null) {
            val existing = _currentDataFlow.value
            val isSameNotification = existing != null && (
                    (data.key != null && existing.key == data.key) ||
                    (existing.packageName == data.packageName && existing.key == data.key)
            )

            if (isSameNotification && existing != null) {
                // Check if content and actions are identical
                val isIdentical = existing.title == data.title &&
                        existing.text == data.text &&
                        existing.subText == data.subText &&
                        existing.actions.size == data.actions.size

                if (isIdentical) {
                    Log.d(TAG, "Identical notification update for key $targetKey ignored — preventing banner flicker.")
                    return
                }

                // Genuinely new content, action update, or caller name in the same call/notification:
                _currentDataFlow.value = data
                currentOnTriggerQs = onTriggerQs
                currentOnActionClick = onActionClick
                currentNotificationId = data.id
                displayedHeadsUpKeys.add(targetKey)
                recordAlert(data)
                if (data.isAlarm || data.isCall) {
                    activePinnedKey = targetKey
                    QuickPanelNotificationListenerService.activePinnedKey = activePinnedKey
                }
                if (data.isCall || data.isAlarm) {
                    ScreenWakeHelper.wakeScreen(context, if (data.isCall) "QuickPanel:HeadsUpCall" else "QuickPanel:HeadsUpAlarm")
                    currentLayoutParams?.let { lp ->
                        @Suppress("DEPRECATION")
                        val callFlags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        if ((lp.flags and callFlags) != callFlags) {
                            lp.flags = lp.flags or callFlags
                            try {
                                currentWindowManager?.updateViewLayout(currentComposeView, lp)
                            } catch (_: Exception) {}
                        }
                    }
                } else if (ScreenWakeHelper.isKeyguardLocked(context)) {
                    currentLayoutParams?.let { lp ->
                        val lockFlag = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        if ((lp.flags and lockFlag) != lockFlag) {
                            lp.flags = lp.flags or lockFlag
                            try {
                                currentWindowManager?.updateViewLayout(currentComposeView, lp)
                            } catch (_: Exception) {}
                        }
                    }
                }
                Log.d(TAG, "In-place smooth update for active key $targetKey: '${data.title}' - '${data.text}'")
                return
            }

            // If a different notification arrives while an existing one is showing:
            if (existing != null) {
                _currentDataFlow.value = data
                currentOnTriggerQs = onTriggerQs
                currentOnActionClick = onActionClick
                currentNotificationId = data.id
                displayedHeadsUpKeys.add(targetKey)
                recordAlert(data)
                if (data.isAlarm || data.isCall) {
                    activePinnedKey = targetKey
                    QuickPanelNotificationListenerService.activePinnedKey = activePinnedKey
                }
                if (data.isCall || data.isAlarm) {
                    ScreenWakeHelper.wakeScreen(context, if (data.isCall) "QuickPanel:HeadsUpCall" else "QuickPanel:HeadsUpAlarm")
                    currentLayoutParams?.let { lp ->
                        @Suppress("DEPRECATION")
                        val callFlags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        if ((lp.flags and callFlags) != callFlags) {
                            lp.flags = lp.flags or callFlags
                            try {
                                currentWindowManager?.updateViewLayout(currentComposeView, lp)
                            } catch (_: Exception) {}
                        }
                    }
                } else if (ScreenWakeHelper.isKeyguardLocked(context)) {
                    currentLayoutParams?.let { lp ->
                        val lockFlag = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        if ((lp.flags and lockFlag) != lockFlag) {
                            lp.flags = lp.flags or lockFlag
                            try {
                                currentWindowManager?.updateViewLayout(currentComposeView, lp)
                            } catch (_: Exception) {}
                        }
                    }
                }
                Log.d(TAG, "Replacing active banner with incoming key $targetKey")
                return
            }
        }

        // 2. Banner is NOT currently showing. Check if this notification was already alerted recently.
        // Prevents re-alerting or repeating old messages for already active notification threads.
        if (!data.isAlarm && !data.isCall) {
            val previousAlert = recentlyAlertedMap[targetKey]
            if (previousAlert != null) {
                val isContentUnchanged = previousAlert.title == data.title && previousAlert.text == data.text
                if (isContentUnchanged) {
                    Log.d(TAG, "Notification $targetKey already alerted with identical text. Suppressing duplicate banner.")
                    return
                }
            }
        }

        // Clean up any stale records older than 5 minutes
        pruneOldAlertRecords()

        // Dismiss any stale views if needed before creating new window
        dismissExistingOverlay()

        displayedHeadsUpKeys.add(targetKey)
        recordAlert(data)
        if (data.isAlarm || data.isCall) {
            activePinnedKey = targetKey
            QuickPanelNotificationListenerService.activePinnedKey = activePinnedKey
        }

        // Set initial reactive data and callbacks
        _currentDataFlow.value = data
        currentOnTriggerQs = onTriggerQs
        currentOnActionClick = onActionClick
        currentNotificationId = data.id

        // Determine the best Context & WindowManager to attach the overlay
        val targetContext: Context
        val targetWindowManager: WindowManager
        val windowType: Int

        if (QuickPanelAccessibilityService.instance != null) {
            targetContext = QuickPanelAccessibilityService.instance!!
            targetWindowManager = targetContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowType = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        } else if (QuickSettingsOverlayService.instance != null) {
            targetContext = QuickSettingsOverlayService.instance!!
            targetWindowManager = targetContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
        } else if (Settings.canDrawOverlays(context)) {
            targetContext = context.applicationContext
            targetWindowManager = targetContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
        } else {
            targetContext = context.applicationContext
            targetWindowManager = targetContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowType = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        }

        val isLocked = ScreenWakeHelper.isKeyguardLocked(targetContext)
        val isScreenOff = ScreenWakeHelper.isScreenOff(targetContext)

        var windowFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH

        val isCallOrAlarm = data.isCall || data.isAlarm
        if (isCallOrAlarm) {
            @Suppress("DEPRECATION")
            windowFlags = windowFlags or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            ScreenWakeHelper.wakeScreen(targetContext, if (data.isCall) "QuickPanel:HeadsUpCall" else "QuickPanel:HeadsUpAlarm")
        } else if (isLocked) {
            windowFlags = windowFlags or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        }

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            windowFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            x = 0
            y = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        val lifecycleOwner = OverlayLifecycleOwner()
        lifecycleOwner.onCreate()

        val composeView = ComposeView(targetContext).apply {
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            clipChildren = false
            clipToPadding = false
            setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                override fun onChildViewAdded(parent: View?, child: View?) {
                    if (child is ViewGroup) {
                        child.clipChildren = false
                        child.clipToPadding = false
                    }
                }
                override fun onChildViewRemoved(parent: View?, child: View?) {}
            })
            lifecycleOwner.attachTo(this)
            setContent {
                QuickPanelTheme {
                    val activeData by _currentDataFlow.collectAsState()
                    activeData?.let { dataItem ->
                        HeadsUpBannerCard(
                            data = dataItem,
                            targetContext = targetContext,
                            onDismiss = {
                                resetPinnedLock(dataItem.key)
                                dismissHeadsUpAnimated()
                            },
                            onOpen = {
                                resetPinnedLock(dataItem.key)
                                launchNotificationTarget(targetContext, dataItem)
                                dismissHeadsUpAnimated()
                            },
                            onSwipeDownToQs = {
                                resetPinnedLock(dataItem.key)
                                dismissHeadsUpAnimated()
                                if (currentOnTriggerQs != null) {
                                    currentOnTriggerQs?.invoke()
                                } else {
                                    triggerDefaultQsShade(targetContext)
                                }
                            },
                            onActionTrigger = { action ->
                                resetPinnedLock(dataItem.key)
                                executeNotificationAction(targetContext, action)
                                currentOnActionClick?.invoke(action)
                                dismissHeadsUpAnimated()
                            }
                        )
                    }
                }
            }
        }

        try {
            targetWindowManager.addView(composeView, layoutParams)
            currentComposeView = composeView
            currentLifecycleOwner = lifecycleOwner
            currentWindowManager = targetWindowManager
            currentLayoutParams = layoutParams
            Log.d(TAG, "Custom Heads-Up Overlay inflated successfully for ${data.packageName}")
        } catch (e: Exception) {
            Log.e(TAG, "WindowManager addView error: ${e.localizedMessage}", e)
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    context,
                    "Heads-Up Overlay Error: ${e.localizedMessage ?: "Permission denied or WindowManager exception"}",
                    Toast.LENGTH_LONG
                ).show()
            }
            lifecycleOwner.onDestroy()
            currentComposeView = null
            currentLayoutParams = null
            _currentDataFlow.value = null
        }
    }

    /**
     * Trigger a Test Message Heads-Up notification preview banner.
     */
    fun showTestHeadsUp(context: Context) {
        val pm = context.packageManager
        val myAppIcon = try {
            pm.getApplicationIcon(context.packageName)
        } catch (e: Exception) {
            null
        }

        val testData = HeadsUpData(
            id = "test_msg_heads_up_${System.currentTimeMillis()}",
            packageName = context.packageName,
            appName = "QuickPanel",
            title = "Alex Rivera",
            text = "Hey! Are we still meeting for the demo at 3 PM? Let me know when you're ready.",
            subText = "Direct Message",
            appIcon = myAppIcon,
            postTime = System.currentTimeMillis()
        )

        showHeadsUp(
            context = context,
            data = testData,
            onTriggerQs = {
                triggerDefaultQsShade(context)
            }
        )
    }

    /**
     * Trigger a Test Native Alarm Heads-Up notification with real Snooze and Stop Action Buttons!
     */
    fun showTestAlarmHeadsUp(context: Context) {
        val pm = context.packageManager
        val myAppIcon = try {
            pm.getApplicationIcon(context.packageName)
        } catch (e: Exception) {
            null
        }

        val snoozeIntent = PendingIntent.getBroadcast(
            context,
            201,
            Intent("com.example.ACTION_MOCK_SNOOZE"),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = PendingIntent.getBroadcast(
            context,
            202,
            Intent("com.example.ACTION_MOCK_STOP"),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmData = HeadsUpData(
            id = "test_alarm_heads_up_${System.currentTimeMillis()}",
            packageName = "com.google.android.deskclock",
            appName = "Clock",
            title = "Alarm (07:30 AM)",
            text = "Wake up — Morning Standup",
            subText = "Upcoming Alarm",
            appIcon = myAppIcon,
            actions = listOf(
                NotificationActionModel(title = "Snooze (10m)", actionIntent = snoozeIntent),
                NotificationActionModel(title = "Stop", actionIntent = stopIntent)
            ),
            postTime = System.currentTimeMillis(),
            isAlarm = true
        )

        showHeadsUp(
            context = context,
            data = alarmData,
            onTriggerQs = {
                triggerDefaultQsShade(context)
            },
            onActionClick = { action ->
                Toast.makeText(context, "Alarm action: ${action.title}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    /**
     * Trigger a Test Incoming Call Heads-Up notification with Answer and Decline Action Buttons!
     */
    fun showTestCallHeadsUp(context: Context) {
        if (!PanelPreferences.enableHeadsUpOverlay.value) {
            PanelPreferences.setEnableHeadsUpOverlay(context, true)
            Toast.makeText(context, "Enabled Heads-Up Overlay", Toast.LENGTH_SHORT).show()
        }
        resetPinnedLock()

        val pm = context.packageManager
        val myAppIcon = try {
            pm.getApplicationIcon(context.packageName)
        } catch (e: Exception) {
            null
        }

        val answerIntent = PendingIntent.getBroadcast(
            context,
            301,
            Intent("com.example.ACTION_MOCK_ANSWER"),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val declineIntent = PendingIntent.getBroadcast(
            context,
            302,
            Intent("com.example.ACTION_MOCK_DECLINE"),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val callData = HeadsUpData(
            id = "test_call_heads_up_${System.currentTimeMillis()}",
            packageName = "com.google.android.dialer",
            appName = "Phone",
            title = "Sarah Jenkins",
            text = "Mobile +1 (555) 019-2834",
            subText = "Incoming Call",
            appIcon = myAppIcon,
            actions = listOf(
                NotificationActionModel(title = "Answer", actionIntent = answerIntent),
                NotificationActionModel(title = "Decline", actionIntent = declineIntent)
            ),
            postTime = System.currentTimeMillis(),
            isCall = true
        )

        showHeadsUp(
            context = context,
            data = callData,
            onTriggerQs = {
                triggerDefaultQsShade(context)
            },
            onActionClick = { action ->
                Toast.makeText(context, "Call action: ${action.title}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun triggerDefaultQsShade(context: Context) {
        if (QuickPanelAccessibilityService.isRunning) {
            QuickPanelAccessibilityService.showCustomPanel(QsExpansionStage.FULL_QS)
        } else if (QuickSettingsOverlayService.isRunning) {
            val intent = Intent(context, QuickSettingsOverlayService::class.java).apply {
                action = QuickSettingsOverlayService.ACTION_TOGGLE_EXPAND
            }
            context.startService(intent)
        }
    }

    private fun executeNotificationAction(context: Context, action: NotificationActionModel) {
        try {
            if (action.actionIntent != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val options = ActivityOptions.makeBasic().apply {
                        setPendingIntentBackgroundActivityStartMode(
                            ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                        )
                    }
                    action.actionIntent.send(context, 0, null, null, null, null, options.toBundle())
                } else {
                    action.actionIntent.send()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed executing action ${action.title}: ${e.message}")
        }
    }

    private fun launchNotificationTarget(context: Context, data: HeadsUpData) {
        try {
            if (data.fullScreenIntent != null) {
                val options = ActivityOptions.makeBasic()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    options.setPendingIntentBackgroundActivityStartMode(
                        ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                    )
                }
                data.fullScreenIntent.send(context, 0, null, null, null, null, options.toBundle())
            } else if (data.contentIntent != null) {
                val options = ActivityOptions.makeBasic()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    options.setPendingIntentBackgroundActivityStartMode(
                        ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                    )
                }
                data.contentIntent.send(context, 0, null, null, null, null, options.toBundle())
            } else {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(data.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch notification intent: ${e.message}")
        }
    }

    private fun dismissHeadsUpAnimated() {
        dismissAnimatedCallback?.invoke() ?: dismissExistingOverlay()
    }

    private fun dismissExistingOverlay() {
        // Note: Do not clear recentlyAlertedMap so active conversation alert history is preserved
        resetAlarmStateLock()
        _currentDataFlow.value = null
        currentComposeView?.let { view ->
            try {
                currentWindowManager?.removeView(view)
            } catch (e: Exception) {
                // Ignore removal exceptions
            }
        }
        currentComposeView = null
        currentLifecycleOwner?.onDestroy()
        currentLifecycleOwner = null
        currentWindowManager = null
        currentLayoutParams = null
        currentNotificationId = null
        dismissAnimatedCallback = null
        currentOnTriggerQs = null
        currentOnActionClick = null
        ScreenWakeHelper.releaseWakeLock()
    }

    /**
     * Remade realistic multi-layer shadow drawn via Canvas BlurMaskFilter.
     * Eliminates native RenderNode elevation artifacts (such as dark smudges on asymmetrical corners)
     * and continues rendering smoothly when translated out of bounds during gestures or in Edge-to-Edge mode.
     */
    private fun Modifier.headsUpCustomShadow(
        shape: Shape,
        elevation: Dp,
        isDark: Boolean
    ): Modifier = if (elevation > 0.dp) {
        this.drawBehind {
            val elevationPx = elevation.toPx()
            if (elevationPx <= 0.5f || size.width <= 0f || size.height <= 0f) return@drawBehind

            val outline = shape.createOutline(size, layoutDirection, this)

            val ambientBaseAlpha = if (isDark) 0.22f else 0.10f
            val keyBaseAlpha = if (isDark) 0.32f else 0.16f
            val elevationFactor = (elevation.value / 8f).coerceIn(0.6f, 1.8f)
            val ambientAlpha = (ambientBaseAlpha * elevationFactor).coerceIn(0.05f, 0.45f)
            val keyAlpha = (keyBaseAlpha * elevationFactor).coerceIn(0.08f, 0.55f)

            val ambientRadius = (elevationPx * 1.3f).coerceAtLeast(1f)
            val ambientOffsetY = elevationPx * 0.25f

            val keyRadius = (elevationPx * 0.75f).coerceAtLeast(1f)
            val keyOffsetY = elevationPx * 0.65f

            drawIntoCanvas { canvas ->
                val paint = Paint()
                val frameworkPaint = paint.asFrameworkPaint()
                frameworkPaint.isAntiAlias = true

                // 1. Ambient soft omnidirectional shadow
                frameworkPaint.color = android.graphics.Color.argb((ambientAlpha * 255).toInt(), 0, 0, 0)
                frameworkPaint.maskFilter = BlurMaskFilter(
                    ambientRadius,
                    BlurMaskFilter.Blur.NORMAL
                )
                canvas.save()
                canvas.translate(0f, ambientOffsetY)
                canvas.drawOutline(outline, paint)
                canvas.restore()

                // 2. Key directional drop shadow
                frameworkPaint.color = android.graphics.Color.argb((keyAlpha * 255).toInt(), 0, 0, 0)
                frameworkPaint.maskFilter = BlurMaskFilter(
                    keyRadius,
                    BlurMaskFilter.Blur.NORMAL
                )
                canvas.save()
                canvas.translate(0f, keyOffsetY)
                canvas.drawOutline(outline, paint)
                canvas.restore()
            }
        }
    } else {
        this
    }

    @Composable
    private fun HeadsUpBannerCard(
        data: HeadsUpData,
        targetContext: Context,
        onDismiss: () -> Unit,
        onOpen: () -> Unit,
        onSwipeDownToQs: () -> Unit,
        onActionTrigger: (NotificationActionModel) -> Unit
    ) {
        val coroutineScope = rememberCoroutineScope()
        val haptic = LocalHapticFeedback.current
        var isVisible by remember { mutableStateOf(false) }

        // Heads-Up Design Preferences
        val edgeToEdge by PanelPreferences.headsUpEdgeToEdge.collectAsState()
        val cornerRadius by PanelPreferences.headsUpCornerRadius.collectAsState()
        val elevation by PanelPreferences.headsUpElevation.collectAsState()
        val topMargin by PanelPreferences.headsUpTopMargin.collectAsState()

        // Interaction Action Preferences
        val swipeUpAction by PanelPreferences.headsUpSwipeUpAction.collectAsState()
        val swipeDownAction by PanelPreferences.headsUpSwipeDownAction.collectAsState()
        val swipeLeftAction by PanelPreferences.headsUpSwipeLeftAction.collectAsState()
        val swipeRightAction by PanelPreferences.headsUpSwipeRightAction.collectAsState()

        val singleTapAction by PanelPreferences.headsUpSingleTapAction.collectAsState()
        val doubleTapAction by PanelPreferences.headsUpDoubleTapAction.collectAsState()
        val longPressAction by PanelPreferences.headsUpLongPressAction.collectAsState()
        val snoozeDurationMinutes by PanelPreferences.headsUpSnoozeDurationMinutes.collectAsState()

        val configuredSeconds by PanelPreferences.headsUpTimeoutSeconds.collectAsState()

        // Physics Animatable offsets
        val animOffsetX = remember { Animatable(0f) }
        val animOffsetY = remember { Animatable(0f) }
        var cardWidthPx by remember { mutableFloatStateOf(1000f) }
        var cardHeightPx by remember { mutableFloatStateOf(300f) }
        var isExpanded by remember { mutableStateOf(false) }

        fun executeAction(action: HeadsUpAction) {
            when (action) {
                HeadsUpAction.DISMISS -> {
                    data.key?.let { key ->
                        QuickPanelNotificationListenerService.dismissNotification(key)
                    }
                    onDismiss()
                }
                HeadsUpAction.HIDE_BANNER -> {
                    onDismiss()
                }
                HeadsUpAction.OPEN_SHADE -> {
                    onSwipeDownToQs()
                }
                HeadsUpAction.OPEN_NOTIFICATION -> {
                    onOpen()
                }
                HeadsUpAction.OPEN_APP -> {
                    try {
                        val launchIntent = targetContext.packageManager.getLaunchIntentForPackage(data.packageName)
                        if (launchIntent != null) {
                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            targetContext.startActivity(launchIntent)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed launching app for ${data.packageName}: ${e.message}")
                    }
                    onDismiss()
                }
                HeadsUpAction.EXPAND_NOTIFICATION -> {
                    isExpanded = !isExpanded
                }
                HeadsUpAction.SNOOZE_APP -> {
                    val duration = snoozeDurationMinutes
                    HeadsUpSnoozeManager.snoozePackage(data.packageName, duration)
                    Toast.makeText(
                        targetContext,
                        "Snoozed ${data.appName} for $duration min",
                        Toast.LENGTH_SHORT
                    ).show()
                    onDismiss()
                }
                HeadsUpAction.SNOOZE_ALL -> {
                    val duration = snoozeDurationMinutes
                    HeadsUpSnoozeManager.snoozeAll(duration)
                    Toast.makeText(
                        targetContext,
                        "Snoozed all heads-up banners for $duration min",
                        Toast.LENGTH_SHORT
                    ).show()
                    onDismiss()
                }
                HeadsUpAction.NONE -> {
                    // Do nothing
                }
            }
        }

        // Monet dynamic theme colors
        val monetPrimary = MaterialTheme.colorScheme.primary
        val monetOnPrimary = MaterialTheme.colorScheme.onPrimary
        val monetPrimaryContainer = MaterialTheme.colorScheme.primaryContainer
        val monetOnPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer

        val monetSecondary = MaterialTheme.colorScheme.secondary
        val monetOnSecondary = MaterialTheme.colorScheme.onSecondary
        val monetSecondaryContainer = MaterialTheme.colorScheme.secondaryContainer
        val monetOnSecondaryContainer = MaterialTheme.colorScheme.onSecondaryContainer

        val monetTertiary = MaterialTheme.colorScheme.tertiary
        val monetOnTertiary = MaterialTheme.colorScheme.onTertiary
        val monetTertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer
        val monetOnTertiaryContainer = MaterialTheme.colorScheme.onTertiaryContainer

        val monetSurface = MaterialTheme.colorScheme.surfaceContainerHigh
        val monetOnSurface = MaterialTheme.colorScheme.onSurface
        val monetOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

        val isDark = LocalIsDarkTheme.current

        // Fully opaque container background
        val cardContainerColor = if (!isDark) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            DarkUiSurface
        }

        val dismissWithAnimation: () -> Unit = {
            if (isVisible) {
                coroutineScope.launch {
                    isVisible = false
                    delay(200)
                    dismissExistingOverlay()
                }
            } else {
                dismissExistingOverlay()
            }
        }

        dismissAnimatedCallback = dismissWithAnimation

        // Keep Alarm & Call Heads-Up Overlay PINNED until action
        LaunchedEffect(data.id, data.isAlarm, data.isCall) {
            isVisible = true
            if (!data.isAlarm && !data.isCall) {
                val autoDismissSeconds = configuredSeconds
                delay((autoDismissSeconds * 1000).toLong())
                if (isVisible) {
                    isVisible = false
                    delay(200)
                    dismissExistingOverlay()
                }
            }
        }

        // Card Shape calculation: Edge-to-Edge locks top corners to 0dp; Corner Radius applies strictly to bottom corners.
        // When Edge-to-Edge is OFF: Corner Radius applies symmetrically to all 4 corners.
        val cardShape = remember(edgeToEdge, cornerRadius) {
            if (edgeToEdge) {
                RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomStart = cornerRadius.dp,
                    bottomEnd = cornerRadius.dp
                )
            } else {
                RoundedCornerShape(cornerRadius.dp)
            }
        }

        val sidePaddingDp = if (edgeToEdge) 0.dp else 8.dp
        val bottomPaddingDp = maxOf(elevation * 3 + 32, 64).dp
        val topPaddingDp = topMargin.dp

        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy)
            ) { -it } + fadeIn(tween(200)),
            exit = slideOutVertically(
                animationSpec = tween(180)
            ) { -it } + fadeOut(tween(180))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(
                        start = sidePaddingDp,
                        end = sidePaddingDp,
                        top = topPaddingDp,
                        bottom = bottomPaddingDp
                    )
                    .testTag("heads_up_overlay_container"),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (edgeToEdge) Modifier else Modifier.widthIn(max = 440.dp))
                        .onSizeChanged {
                            cardWidthPx = it.width.toFloat()
                            cardHeightPx = it.height.toFloat()
                        }
                        .graphicsLayer {
                            translationX = animOffsetX.value
                            translationY = animOffsetY.value
                            val progress = if (cardWidthPx > 0f) (abs(animOffsetX.value) / cardWidthPx) else 0f
                            alpha = (1f - progress).coerceIn(0f, 1f)
                            clip = false
                        }
                        .headsUpCustomShadow(
                            shape = cardShape,
                            elevation = elevation.dp,
                            isDark = isDark
                        )
                        .animateContentSize()
                        .pointerInput(
                            swipeUpAction, swipeDownAction, swipeLeftAction, swipeRightAction,
                            singleTapAction, doubleTapAction, longPressAction, snoozeDurationMinutes
                        ) {
                            val velocityTracker = VelocityTracker()
                            var lastTapTime = 0L

                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val startPosition = down.position
                                val touchSlop = viewConfiguration.touchSlop
                                velocityTracker.resetTracking()
                                velocityTracker.addPosition(down.uptimeMillis, down.position)

                                var isDragging = false
                                var isLongPressed = false
                                var dragAxis = 0 // 1 = horizontal, 2 = vertical

                                val longPressJob = coroutineScope.launch {
                                    delay(viewConfiguration.longPressTimeoutMillis)
                                    if (!isDragging) {
                                        isLongPressed = true
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        executeAction(longPressAction)
                                    }
                                }

                                var pointer = down
                                while (pointer.pressed) {
                                    val event = awaitPointerEvent()
                                    val currentPointer = event.changes.firstOrNull { it.id == down.id } ?: break
                                    pointer = currentPointer
                                    velocityTracker.addPosition(currentPointer.uptimeMillis, currentPointer.position)

                                    val totalDeltaX = currentPointer.position.x - startPosition.x
                                    val totalDeltaY = currentPointer.position.y - startPosition.y

                                    if (!isDragging && (abs(totalDeltaX) > touchSlop || abs(totalDeltaY) > touchSlop)) {
                                        isDragging = true
                                        longPressJob.cancel()
                                        dragAxis = if (abs(totalDeltaX) >= abs(totalDeltaY)) 1 else 2
                                    }

                                    if (isDragging) {
                                        currentPointer.consume()
                                        coroutineScope.launch {
                                            if (dragAxis == 1) {
                                                animOffsetX.snapTo(totalDeltaX)
                                                animOffsetY.snapTo(0f)
                                            } else {
                                                animOffsetY.snapTo(totalDeltaY)
                                                animOffsetX.snapTo(0f)
                                            }
                                        }
                                    }
                                }

                                longPressJob.cancel()

                                if (isDragging) {
                                    val velocity = velocityTracker.calculateVelocity()
                                    val currentX = animOffsetX.value
                                    val currentY = animOffsetY.value
                                    val widthThreshold = cardWidthPx * 0.30f
                                    val heightThreshold = cardHeightPx * 0.30f
                                    val velThreshold = 800f

                                    val isHorizontalSwipe = dragAxis == 1 && (abs(currentX) > widthThreshold || abs(velocity.x) > velThreshold)
                                    val isVerticalSwipe = dragAxis == 2 && (abs(currentY) > heightThreshold || abs(velocity.y) > velThreshold)

                                    if (isHorizontalSwipe) {
                                        val toRight = if (abs(velocity.x) > velThreshold) velocity.x > 0 else currentX > 0
                                        val targetAction = if (toRight) swipeRightAction else swipeLeftAction
                                        coroutineScope.launch {
                                            animOffsetX.animateTo(if (toRight) cardWidthPx else -cardWidthPx, tween(150))
                                            executeAction(targetAction)
                                        }
                                    } else if (isVerticalSwipe) {
                                        val toDown = if (abs(velocity.y) > velThreshold) velocity.y > 0 else currentY > 0
                                        val targetAction = if (toDown) swipeDownAction else swipeUpAction
                                        coroutineScope.launch {
                                            animOffsetY.animateTo(if (toDown) cardHeightPx else -cardHeightPx, tween(150))
                                            executeAction(targetAction)
                                        }
                                    } else {
                                        // Spring back
                                        coroutineScope.launch {
                                            launch { animOffsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy)) }
                                            launch { animOffsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy)) }
                                        }
                                    }
                                } else if (!isLongPressed) {
                                    val tapTime = System.currentTimeMillis()
                                    if (tapTime - lastTapTime < 300L) {
                                        lastTapTime = 0L
                                        executeAction(doubleTapAction)
                                    } else {
                                        lastTapTime = tapTime
                                        coroutineScope.launch {
                                            delay(300L)
                                            if (lastTapTime == tapTime) {
                                                lastTapTime = 0L
                                                executeAction(singleTapAction)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        .testTag("heads_up_banner_card")
                ) {
                    Surface(
                        shape = cardShape,
                        color = cardContainerColor,
                        shadowElevation = 0.dp,
                        tonalElevation = 0.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                        // Top Header: App Icon, App Name, Badges, Time, and Close Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val appIconBitmap = remember(data.appIcon) {
                                    data.appIcon?.let { drawable ->
                                        try {
                                            if (drawable is android.graphics.drawable.BitmapDrawable && drawable.bitmap != null) {
                                                drawable.bitmap
                                            } else {
                                                val w = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
                                                val h = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
                                                val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                                                val canvas = android.graphics.Canvas(bmp)
                                                drawable.setBounds(0, 0, canvas.width, canvas.height)
                                                drawable.draw(canvas)
                                                bmp
                                            }
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }
                                }

                                if (appIconBitmap != null) {
                                    Image(
                                        bitmap = appIconBitmap.asImageBitmap(),
                                        contentDescription = data.appName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (!isDark) MaterialTheme.colorScheme.surfaceContainerHighest
                                                else MaterialTheme.colorScheme.surfaceContainerLow
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (data.isCall) Icons.Default.Call else if (data.isAlarm) Icons.Default.Alarm else Icons.Default.Notifications,
                                            contentDescription = null,
                                            tint = monetPrimary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = data.appName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = monetPrimary
                                )

                                if (data.isCall) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (!isDark) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainerLow,
                                        border = androidx.compose.foundation.BorderStroke(0.8.dp, monetPrimary.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = "INCOMING CALL",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.5.sp,
                                            color = monetPrimary,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                } else if (data.isAlarm) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (!isDark) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainerLow,
                                        border = androidx.compose.foundation.BorderStroke(0.8.dp, monetPrimary.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = "ALARM",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.5.sp,
                                            color = monetPrimary,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                if (!data.subText.isNullOrBlank()) {
                                    Text(
                                        text = "•",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = data.subText,
                                        fontSize = 11.sp,
                                        color = Color.LightGray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isExpanded) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = monetPrimary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "EXPANDED",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = monetPrimary,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "now",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )

                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .clickable(
                                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                            indication = null
                                        ) { onDismiss() }
                                        .testTag("dismiss_heads_up_btn"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        // Content Row: Large Avatar / Icon + Title & Body Text
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (data.avatarBitmap != null) {
                                Image(
                                    bitmap = data.avatarBitmap.asImageBitmap(),
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .border(
                                            1.dp,
                                            monetPrimary.copy(alpha = 0.3f),
                                            CircleShape
                                        )
                                )
                            }

                            AnimatedContent(
                                targetState = Triple(data.title, data.text, data.subText),
                                transitionSpec = {
                                    (fadeIn(animationSpec = tween(220)) + slideInVertically(animationSpec = tween(220)) { it / 4 })
                                        .togetherWith(fadeOut(animationSpec = tween(140)))
                                },
                                label = "HeadsUpMessageContentTransition",
                                modifier = Modifier.weight(1f)
                            ) { (currentTitle, currentText, _) ->
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    if (currentTitle.isNotBlank()) {
                                        Text(
                                            text = currentTitle,
                                            fontSize = if (data.isCall) 14.sp else 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = monetOnSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (currentText.isNotBlank()) {
                                        Text(
                                            text = currentText,
                                            fontSize = 12.sp,
                                            color = monetOnSurfaceVariant,
                                            maxLines = if (isExpanded) 16 else 2,
                                            overflow = TextOverflow.Ellipsis,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }

                        // DYNAMIC ACTION BUTTONS (Answer / Decline / Snooze / Stop / Custom Actions)
                        if (data.actions.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                data.actions.forEach { action ->
                                    val isAnswerAction = action.title.contains("Answer", ignoreCase = true) ||
                                            action.title.contains("Accept", ignoreCase = true) ||
                                            action.title.contains("Ответить", ignoreCase = true) ||
                                            action.title.contains("Responder", ignoreCase = true) ||
                                            action.title.contains("Répondre", ignoreCase = true) ||
                                            action.title.contains("Annehmen", ignoreCase = true) ||
                                            action.title.contains("Atender", ignoreCase = true) ||
                                            action.title.contains("Rispondi", ignoreCase = true) ||
                                            action.title.contains("接听", ignoreCase = true) ||
                                            action.title.contains("応答", ignoreCase = true) ||
                                            action.title.contains("通話", ignoreCase = true)

                                    val isDeclineAction = action.title.contains("Decline", ignoreCase = true) ||
                                            action.title.contains("Reject", ignoreCase = true) ||
                                            action.title.contains("Отклонить", ignoreCase = true) ||
                                            action.title.contains("Rechazar", ignoreCase = true) ||
                                            action.title.contains("Refuser", ignoreCase = true) ||
                                            action.title.contains("Ablehnen", ignoreCase = true) ||
                                            action.title.contains("Recusar", ignoreCase = true) ||
                                            action.title.contains("Rifiuta", ignoreCase = true) ||
                                            action.title.contains("挂断", ignoreCase = true) ||
                                            action.title.contains("拒否", ignoreCase = true) ||
                                            action.title.contains("Hang up", ignoreCase = true)

                                    val isStopAction = action.title.contains("Stop", ignoreCase = true) ||
                                            action.title.contains("Dismiss", ignoreCase = true) ||
                                            action.title.contains("Cancel", ignoreCase = true) ||
                                            action.title.contains("ストップ", ignoreCase = true) ||
                                            action.title.contains("解除", ignoreCase = true)

                                    val isSnoozeAction = action.title.contains("Snooze", ignoreCase = true) ||
                                            action.title.contains("Repeat", ignoreCase = true) ||
                                            action.title.contains("スヌーズ", ignoreCase = true)

                                    // Vibrant action button styling for incoming calls (green for answer, red for decline)
                                    val btnBgColor = when {
                                        isAnswerAction -> Color(0xFF2E7D32)
                                        isDeclineAction -> Color(0xFFC62828)
                                        !isDark -> MaterialTheme.colorScheme.surfaceContainerHighest
                                        else -> MaterialTheme.colorScheme.surfaceContainerHigh
                                    }

                                    val btnTextColor = when {
                                        isAnswerAction || isDeclineAction -> Color.White
                                        else -> monetOnSurface
                                    }

                                    val btnBorderColor = when {
                                        isAnswerAction || isDeclineAction -> Color.Transparent
                                        !isDark -> MaterialTheme.colorScheme.outlineVariant
                                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    }

                                    val iconVector = when {
                                        isAnswerAction -> Icons.Default.Call
                                        isDeclineAction -> Icons.Default.CallEnd
                                        isStopAction -> Icons.Default.Stop
                                        isSnoozeAction -> Icons.Default.Snooze
                                        else -> Icons.AutoMirrored.Filled.OpenInNew
                                    }

                                    Surface(
                                        onClick = { onActionTrigger(action) },
                                        shape = RoundedCornerShape(12.dp),
                                        color = btnBgColor,
                                        modifier = Modifier.testTag("action_btn_${action.title.filter { it.isLetterOrDigit() }}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = iconVector,
                                                contentDescription = action.title,
                                                tint = btnTextColor,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Text(
                                                text = action.title,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = btnTextColor
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom Gesture Hint & Open Pill
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Swipe down hint indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.clickable(
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ) { onSwipeDownToQs() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Swipe down for QS",
                                    tint = monetPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Swipe down for Quick Settings",
                                    fontSize = 10.sp,
                                    color = monetPrimary.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Open Button Pill (only shown if not an active alarm/call with explicit actions)
                            if (data.actions.isEmpty()) {
                                Surface(
                                    onClick = onOpen,
                                    shape = RoundedCornerShape(12.dp),
                                    color = monetPrimary.copy(alpha = 0.18f),
                                    modifier = Modifier.testTag("open_heads_up_action_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                            contentDescription = "Open",
                                            tint = monetPrimary,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = "Open",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = monetPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
