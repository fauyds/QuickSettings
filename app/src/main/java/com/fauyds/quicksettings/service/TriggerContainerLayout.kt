package com.fauyds.quicksettings.service

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout

/**
 * TriggerContainerLayout
 *
 * Persistent Transparent Root Container View attached to WindowManager at status bar thickness.
 *
 * Combines Shizuku detection with OnApplyWindowInsetsListener:
 * - When insets.getInsets(WindowInsets.Type.statusBars()).top > 0 OR Shizuku detects revealed status bar:
 *   -> Set triggerView.visibility = View.VISIBLE on Main Thread.
 * - When top inset == 0 AND Shizuku confirms Immersive Mode:
 *   -> Set triggerView.visibility = View.GONE on Main Thread.
 *
 * Features:
 * 1. Instantaneous Swipe Detection: Zero hold delay required. As soon as the finger moves
 *    downward (diffY >= 5dp) or on a quick downward flick/tap, the custom Quick Settings panel opens immediately.
 * 2. Strict Immersive Gate: When the trigger is hidden (View.GONE), touch events pass through
 *    completely (returns false) so the custom panel never opens when the trigger is not appearing.
 */
class TriggerContainerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "ImmersiveCheck"
    }

    private var childTriggerView: View? = null
    var triggerView: View?
        get() = childTriggerView
        set(value) {
            if (value != null) setChildTriggerView(value)
        }

    private val mainHandler = Handler(Looper.getMainLooper())

    private var touchDownY = 0f
    private var touchDownX = 0f
    private var touchDownTime = 0L
    private var isTrackingSwipe = false

    private var isShizukuImmersive: Boolean = false
    private var lastWindowInsetsTop: Int? = null

    var onSwipeDownToOpen: ((isDeepSwipe: Boolean) -> Unit)? = null

    init {
        super.setVisibility(View.VISIBLE)
        fitsSystemWindows = false
        setOnApplyWindowInsetsListener { _, insets ->
            val top = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insets.getInsets(WindowInsets.Type.statusBars()).top
            } else {
                @Suppress("DEPRECATION")
                insets.systemWindowInsetTop
            }
            lastWindowInsetsTop = top
            Log.d(TAG, "OnApplyWindowInsets top statusBars: $top")
            evaluateVisibility()
            insets
        }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(View.VISIBLE)
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        val top = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            insets.getInsets(WindowInsets.Type.statusBars()).top
        } else {
            @Suppress("DEPRECATION")
            insets.systemWindowInsetTop
        }
        lastWindowInsetsTop = top
        evaluateVisibility()
        return super.onApplyWindowInsets(insets)
    }

    /**
     * Evaluates trigger visibility based on WindowInsets and Shizuku detection:
     * - insets top > 0 OR Shizuku detects revealed status bar (!isShizukuImmersive) -> View.VISIBLE
     * - insets top == 0 AND Shizuku confirms Immersive Mode (isShizukuImmersive) -> View.GONE
     */
    fun evaluateVisibility() {
        mainHandler.post {
            val trigger = childTriggerView ?: return@post
            val topInset = lastWindowInsetsTop

            val isImmersive = if (topInset != null && topInset > 0) {
                false
            } else if (topInset != null && topInset == 0 && isShizukuImmersive) {
                true
            } else {
                isShizukuImmersive
            }

            val targetVisibility = if (isImmersive) View.GONE else View.VISIBLE
            if (trigger.visibility != targetVisibility) {
                trigger.visibility = targetVisibility
                Log.d(TAG, "Applied UI visibility on triggerView: ${if (isImmersive) "GONE (Immersive Mode)" else "VISIBLE (Standard Mode)"}")
            }
        }
    }

    fun updateChildVisibility(isImmersive: Boolean) {
        isShizukuImmersive = isImmersive
        evaluateVisibility()
    }

    /**
     * Instantaneous touch processing:
     * - In Immersive Mode (child GONE): passes through completely (returns false).
     * - In Standard Mode (child VISIBLE): captures immediate downward movement (diffY >= 5dp)
     *   or quick flicks without requiring holding or waiting.
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val child = childTriggerView
        if (child == null || child.visibility != View.VISIBLE) {
            isTrackingSwipe = false
            return false
        }

        val density = resources.displayMetrics.density
        val immediateSwipeThresholdPx = 5f * density

        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchDownY = ev.rawY
                touchDownX = ev.rawX
                touchDownTime = System.currentTimeMillis()
                isTrackingSwipe = true
                QuickPanelAccessibilityService.collapseStockNotificationShade()
                super.dispatchTouchEvent(ev)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isTrackingSwipe) {
                    val diffY = ev.rawY - touchDownY
                    val diffX = Math.abs(ev.rawX - touchDownX)

                    if (diffY >= immediateSwipeThresholdPx && diffY > (diffX * 0.3f)) {
                        if (QuickPanelAccessibilityService.isRunning) {
                            QuickPanelAccessibilityService.instance?.let { service ->
                                if (!service.isPanelShowing) service.showOverlayPanel(com.fauyds.quicksettings.ui.overlay.QsExpansionStage.QQS)
                                service.updatePullDownOffset(diffY)
                            }
                        } else {
                            QuickSettingsOverlayService.instance?.let { service ->
                                if (!service.isPanelShowing) service.showOverlayPanel(com.fauyds.quicksettings.ui.overlay.QsExpansionStage.QQS)
                                service.updatePullDownOffset(diffY)
                            }
                        }
                        QuickPanelAccessibilityService.collapseStockNotificationShade()
                        return true
                    }
                }
                return super.dispatchTouchEvent(ev)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isTrackingSwipe) {
                    val diffY = ev.rawY - touchDownY
                    val diffX = Math.abs(ev.rawX - touchDownX)
                    val elapsed = System.currentTimeMillis() - touchDownTime
                    val isFlick = elapsed < 350 && diffY >= (8f * density)

                    if (QuickPanelAccessibilityService.isRunning) {
                        QuickPanelAccessibilityService.instance?.onPullDownReleased(diffY, isFlick)
                    } else {
                        QuickSettingsOverlayService.instance?.onPullDownReleased(diffY, isFlick)
                    }

                    isTrackingSwipe = false
                    if (diffY >= (8f * density) && diffY > (diffX * 0.3f)) {
                        Log.d(TAG, "[INSTANT FLICK OPEN] diffY=$diffY elapsed=$elapsed ms -> Opening panel!")
                        QuickPanelAccessibilityService.collapseStockNotificationShade()
                        onSwipeDownToOpen?.invoke(false)
                        return true
                    }
                }
                isTrackingSwipe = false
                return super.dispatchTouchEvent(ev)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    fun isTransientRevealActive(): Boolean = false

    fun cancelTransientReveal() {
        // No-op
    }

    fun setChildTriggerView(view: View) {
        removeAllViews()
        childTriggerView = view
        addView(view, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        evaluateVisibility()
    }

    fun getChildTriggerView(): View? = childTriggerView
}
