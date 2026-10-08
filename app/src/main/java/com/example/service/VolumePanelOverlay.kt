package com.example.service

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.system.NativeTileManager
import com.example.ui.theme.DarkUiBackground
import com.example.ui.theme.DarkUiSurface
import com.example.ui.theme.DarkUiSurfaceVariant
import com.example.ui.theme.MonetPrimary
import com.example.ui.theme.QuickPanelTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages the custom Volume Panel layout attached directly to WindowManager
 * with high window layer priority (TYPE_ACCESSIBILITY_OVERLAY or TYPE_APPLICATION_OVERLAY)
 * to ensure it always renders on top of the Quick Settings panel.
 */
object VolumePanelOverlay {

    private const val TAG = "VolumePanelOverlay"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var volumeView: View? = null
    private var currentWm: WindowManager? = null
    private var currentParams: WindowManager.LayoutParams? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null

    private var autoDismissJob: Job? = null

    private val _isShowingFlow = MutableStateFlow(false)
    val isShowingFlow = _isShowingFlow.asStateFlow()

    val isShowing: Boolean
        get() = _isShowingFlow.value

    private val _activeStreamFlow = MutableStateFlow(AudioManager.STREAM_MUSIC)

    /**
     * Show or bring to front the custom Volume Panel.
     * When already showing, invokes windowManager.updateViewLayout() and bringToFront()
     * to keep its Z-order strictly above the Quick Settings panel.
     */
    fun show(context: Context, activeStream: Int = AudioManager.STREAM_MUSIC) {
        try {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY)
            com.example.system.CpuPerformanceManager.getInstance(context).onInteractionStart()
        } catch (_: Exception) {}
        scope.launch(Dispatchers.Main) {
            _activeStreamFlow.value = activeStream
            val (wm, windowType) = resolveWindowManagerAndType(context) ?: return@launch

            // If already attached to the same WindowManager instance, re-assert Z-order priority
            if (isShowing && volumeView != null && currentWm == wm) {
                try {
                    volumeView?.bringToFront()
                    currentParams?.let { params ->
                        wm.updateViewLayout(volumeView, params)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "updateViewLayout failed: ${e.message}")
                }
                resetAutoDismissTimer()
                return@launch
            }

            // Remove prior view if switching window manager or recreating
            if (isShowing && volumeView != null) {
                removeViewSafely()
            }

            createAndAttachView(context, wm, windowType)
        }
    }

    /**
     * Brings the volume panel to the front and asserts its top Z-order over the active QS panel.
     */
    fun bringToFront(context: Context? = null) {
        scope.launch(Dispatchers.Main) {
            val ctx = context ?: volumeView?.context ?: return@launch
            val (wm, windowType) = resolveWindowManagerAndType(ctx) ?: return@launch
            val view = volumeView
            if (view != null && currentWm == wm && currentParams != null) {
                try {
                    view.bringToFront()
                    currentParams?.let { params ->
                        wm.updateViewLayout(view, params)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "bringToFront updateViewLayout failed: ${e.message}")
                }
            } else if (isShowing) {
                removeViewSafely()
                createAndAttachView(ctx, wm, windowType)
            }
        }
    }

    /**
     * Dismiss the volume panel overlay.
     */
    fun hide() {
        scope.launch(Dispatchers.Main) {
            autoDismissJob?.cancel()
            autoDismissJob = null
            removeViewSafely()
        }
    }

    /**
     * Called when hardware volume keys (Volume Up / Down) are detected.
     */
    fun onVolumeKey(context: Context, isUp: Boolean) {
        if (!com.example.data.PanelPreferences.enableCustomVolumePanel.value) return
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val direction = if (isUp) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        try {
            am?.adjustSuggestedStreamVolume(direction, AudioManager.USE_DEFAULT_STREAM_TYPE, 0)
        } catch (e: Exception) {
            Log.e(TAG, "adjustSuggestedStreamVolume failed: ${e.message}")
        }
        NativeTileManager.instance?.refreshAllStates()
        QuickPanelAccessibilityService.hideCustomPanel()
        QuickSettingsOverlayService.instance?.hideOverlayPanelSmoothly()
        show(context, activeStream = AudioManager.STREAM_MUSIC)
    }

    /**
     * Called when hardware volume mute key is detected.
     */
    fun onVolumeMuteKey(context: Context) {
        if (!com.example.data.PanelPreferences.enableCustomVolumePanel.value) return
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        try {
            am?.adjustSuggestedStreamVolume(AudioManager.ADJUST_TOGGLE_MUTE, AudioManager.USE_DEFAULT_STREAM_TYPE, 0)
        } catch (e: Exception) {
            Log.e(TAG, "adjustSuggestedStreamVolume mute failed: ${e.message}")
        }
        NativeTileManager.instance?.refreshAllStates()
        QuickPanelAccessibilityService.hideCustomPanel()
        QuickSettingsOverlayService.instance?.hideOverlayPanelSmoothly()
        show(context, activeStream = AudioManager.STREAM_MUSIC)
    }

    private fun resetAutoDismissTimer() {
        autoDismissJob?.cancel()
        autoDismissJob = scope.launch {
            delay(3500)
            hide()
        }
    }

    private fun resolveWindowManagerAndType(context: Context): Pair<WindowManager, Int>? {
        // Priority 1: If QuickSettingsOverlayService currently has its panel showing, or is the calling context,
        // attach to QuickSettingsOverlayService's WindowManager with high-priority TYPE_APPLICATION_OVERLAY
        QuickSettingsOverlayService.instance?.let { service ->
            if (service.isPanelShowing || context is QuickSettingsOverlayService) {
                val wm = service.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                if (wm != null) {
                    val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        @Suppress("DEPRECATION")
                        WindowManager.LayoutParams.TYPE_PHONE
                    }
                    return Pair(wm, type)
                }
            }
        }

        // Priority 2: If QuickPanelAccessibilityService currently has its panel showing, or is the calling context,
        // attach to QuickPanelAccessibilityService's WindowManager with TYPE_ACCESSIBILITY_OVERLAY
        QuickPanelAccessibilityService.instance?.let { service ->
            if (service.isPanelShowing || context is QuickPanelAccessibilityService) {
                val wm = service.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                if (wm != null) {
                    return Pair(wm, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
                }
            }
        }

        // Priority 3: QuickPanelAccessibilityService available (TYPE_ACCESSIBILITY_OVERLAY)
        QuickPanelAccessibilityService.instance?.let { service ->
            val wm = service.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            if (wm != null) {
                return Pair(wm, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
            }
        }

        // Priority 4: QuickSettingsOverlayService available (TYPE_APPLICATION_OVERLAY)
        QuickSettingsOverlayService.instance?.let { service ->
            val wm = service.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            if (wm != null) {
                val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }
                return Pair(wm, type)
            }
        }

        // Priority 5: Fallback to context window manager with overlay permissions
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        if (wm != null && (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context))) {
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            return Pair(wm, type)
        }

        return null
    }

    private fun createAndAttachView(context: Context, wm: WindowManager, windowType: Int) {
        val density = context.resources.displayMetrics.density

        // WindowManager LayoutParams configured with high layer priority and outside touch detection
        val panelParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.BOTTOM or Gravity.START
            x = 0
            y = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        }

        val owner = OverlayLifecycleOwner()
        owner.onCreate()
        lifecycleOwner = owner

        val composeView = ComposeView(context).apply {
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            owner.attachTo(this)
            setContent {
                QuickPanelTheme {
                    VolumePanelContent(
                        context = context,
                        onInteraction = { resetAutoDismissTimer() },
                        onDismiss = { hide() },
                        onLayoutChange = {
                            // Update WindowManager layout when expanding/collapsing
                            try {
                                volumeView?.bringToFront()
                                currentWm?.updateViewLayout(volumeView, panelParams)
                            } catch (e: Exception) {
                                Log.e(TAG, "updateViewLayout on expand failed: ${e.message}")
                            }
                        }
                    )
                }
            }
        }

        // Tapping outside dismisses only the volume UI without closing or breaking interaction with underlying QS panel
        composeView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_OUTSIDE) {
                hide()
                false // Allow underlying window (e.g. Quick Settings panel) to process the touch
            } else {
                false
            }
        }

        try {
            wm.addView(composeView, panelParams)
            composeView.bringToFront()
            volumeView = composeView
            currentWm = wm
            currentParams = panelParams
            _isShowingFlow.value = true
            resetAutoDismissTimer()
            Log.d(TAG, "Volume panel overlay attached on top of Quick Settings window (Type: $windowType).")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach volume panel overlay: ${e.message}")
            _isShowingFlow.value = false
        }
    }

    private fun removeViewSafely() {
        val view = volumeView
        val wm = currentWm
        if (view != null && wm != null) {
            try {
                wm.removeView(view)
            } catch (e: Exception) {
                Log.e(TAG, "removeView failed: ${e.message}")
            }
        }
        lifecycleOwner?.onDestroy()
        lifecycleOwner = null
        volumeView = null
        currentWm = null
        currentParams = null
        _isShowingFlow.value = false
    }
}

/**
 * Transparent square overlay element rendered at the bottom-left screen corner when volume keys are pressed.
 */
@Composable
fun VolumePanelContent(
    context: Context,
    onInteraction: () -> Unit,
    onDismiss: () -> Unit,
    onLayoutChange: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(Color.Transparent)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) { onDismiss() }
    )
}
