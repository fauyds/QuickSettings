package com.example.ui.notification

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup
import android.view.animation.PathInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.model.MediaState
import com.example.model.NotificationActionModel
import com.example.model.NotificationItem
import com.example.ui.overlay.QsCardBlurBounds
import com.example.ui.overlay.QsExpansionStage
import com.example.data.NotificationCategoryManager
import com.example.data.PanelPreferences
import kotlin.math.abs

class NotificationListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val rvNotifications: RecyclerView
    private val flPermissionCard: FrameLayout
    private val flEmptyCard: FrameLayout
    private val btnEnablePermission: TextView
    private val tvPermissionTitle: TextView
    private val tvPermissionSub: TextView
    private val ivPermissionIcon: ImageView
    private val tvEmptyText: TextView
    private val ivEmptyIcon: ImageView
    private val ivSettingsBtn: ImageView
    private val tvHeaderTitle: TextView
    private val ivHeaderIcon: ImageView
    private val llHeader: View
    private val flFloatingClearAllContainer: FrameLayout
    private val btnFloatingClearAllFab: FrameLayout
    private val ivFloatingClearAllIcon: ImageView

    private var adapter: NotificationAdapter? = null

    var onNotificationClick: ((NotificationItem) -> Unit)? = null
    var onNotificationDismiss: ((NotificationItem) -> Unit)? = null
    var onActionClick: ((NotificationActionModel) -> Unit)? = null
    var onClearAll: (() -> Unit)? = null
    var onRequestPermission: (() -> Unit)? = null
    var onOpenNotificationManager: (() -> Unit)? = null
    var rootPanelView: View? = null
    var onStageChange: ((QsExpansionStage) -> Unit)? = null
    var onExpandToFullQs: (() -> Unit)? = null
    var onDragInteraction: ((deltaY: Float, action: Int) -> Unit)? = null
    var onNotificationBlurRegionsChanged: ((List<QsCardBlurBounds>) -> Unit)? = null
    var composeHostView: View? = null
    var currentExpansionStage: QsExpansionStage = QsExpansionStage.QQS

    private var startY = 0f
    private var lastY = 0f
    private var lastTime = 0L
    private var calculatedVelocityY = 0f // pixels per millisecond
    private var isDraggingPanel = false
    private var activePointerId = MotionEvent.INVALID_POINTER_ID
    private var dragAnchorY = 0f
    private var dragActionType = DRAG_NONE
    private var velocityTracker: VelocityTracker? = null
    private var lastVisualSignature: Int? = null
    private val categoryItemAnimator = CategoryExpandItemAnimator()
    private var blurRegionReportingEnabled: Boolean = false
    private var isPreDrawListenerRegistered: Boolean = false
    private var lastReportedBlurRegions: List<QsCardBlurBounds> = emptyList()
    private var lastCardCornerRadiusPx: Float = 0f
    private val rootLocBuffer = IntArray(2)
    private val containerLocBuffer = IntArray(2)
    private val rvLocBuffer = IntArray(2)
    private val cardLocBuffer = IntArray(2)
    private val blurPreDrawListener = android.view.ViewTreeObserver.OnPreDrawListener {
        dispatchBlurRegionsIfNeeded()
        true
    }

    companion object {
        private const val DRAG_NONE = 0
        private const val DRAG_CLOSE_UP = 1
        private const val DRAG_EXPAND_DOWN = 2
    }

    fun getRecyclerView(): RecyclerView = rvNotifications

    fun canScrollUp(): Boolean {
        if (rvNotifications.visibility != View.VISIBLE) return false
        if (rvNotifications.canScrollVertically(-1)) return true
        val lm = rvNotifications.layoutManager as? LinearLayoutManager ?: return false
        val first = lm.findFirstVisibleItemPosition()
        return first > 0
    }

    fun canScrollDown(): Boolean {
        if (rvNotifications.visibility != View.VISIBLE) return false
        if (rvNotifications.canScrollVertically(1)) return true
        val lm = rvNotifications.layoutManager as? LinearLayoutManager ?: return false
        val total = adapter?.itemCount ?: 0
        if (total == 0) return false
        val last = lm.findLastVisibleItemPosition()
        return last in 0 until (total - 1)
    }

    init {
        NotificationCategoryManager.init(context)
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
        val root = LayoutInflater.from(context).inflate(R.layout.layout_notification_list, this, true)

        rvNotifications = root.findViewById(R.id.rv_notifications)
        rvNotifications.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        flPermissionCard = root.findViewById(R.id.fl_notif_permission_card)
        flEmptyCard = root.findViewById(R.id.fl_notif_empty_card)
        btnEnablePermission = root.findViewById(R.id.btn_notif_enable_permission)
        
        // Locate nested view references directly
        ivPermissionIcon = root.findViewById(R.id.iv_notif_permission_icon) ?: ImageView(context)
        tvPermissionTitle = root.findViewById(R.id.tv_notif_permission_title) ?: TextView(context)
        tvPermissionSub = root.findViewById(R.id.tv_notif_permission_sub) ?: TextView(context)

        ivEmptyIcon = root.findViewById(R.id.iv_notif_empty_icon) ?: ImageView(context)
        tvEmptyText = root.findViewById(R.id.tv_notif_empty_text) ?: TextView(context)

        ivSettingsBtn = root.findViewById(R.id.iv_notif_settings_btn)
        tvHeaderTitle = root.findViewById(R.id.tv_notif_header_title)
        ivHeaderIcon = root.findViewById(R.id.iv_notif_header_icon)
        llHeader = root.findViewById(R.id.ll_notif_header)

        flFloatingClearAllContainer = root.findViewById(R.id.fl_floating_clear_all_container)
        btnFloatingClearAllFab = root.findViewById(R.id.btn_floating_clear_all_fab)
        ivFloatingClearAllIcon = root.findViewById(R.id.iv_floating_clear_all_icon)

        // Enable completely free, unconstrained scrolling with native physics,
        // while strictly clipping scrolled items at the RecyclerView viewport edges.
        rvNotifications.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rvNotifications.isNestedScrollingEnabled = false
        rvNotifications.overScrollMode = View.OVER_SCROLL_NEVER
        rvNotifications.clipToPadding = true
        rvNotifications.clipChildren = true
        this.clipToPadding = true
        this.clipChildren = true
        flPermissionCard.clipChildren = false
        flPermissionCard.clipToPadding = false
        flEmptyCard.clipChildren = false
        flEmptyCard.clipToPadding = false
        root.findViewById<ViewGroup>(R.id.ll_notif_list_container)?.apply {
            clipChildren = true
            clipToPadding = true
            if (PanelPreferences.isAndroidOreoStyle(context) || PanelPreferences.isAndroidMarshmallowStyle(context) || PanelPreferences.isAndroidKitKatStyle(context)) {
                setPadding(0, 0, 0, 0)
            }
        }
        rvNotifications.setHasFixedSize(false)
        rvNotifications.descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS

        setupGestureInterception()

        rvNotifications.itemAnimator = categoryItemAnimator

        btnEnablePermission.setOnClickListener {
            onRequestPermission?.invoke()
        }

        ivSettingsBtn.setOnClickListener {
            onOpenNotificationManager?.invoke()
        }

        initAdapter()
        setupItemTouchHelper()
    }

    fun setBlurRegionReportingEnabled(enabled: Boolean) {
        if (blurRegionReportingEnabled == enabled) return
        blurRegionReportingEnabled = enabled
        updatePreDrawListenerRegistration()
        if (!enabled) {
            clearReportedBlurRegions()
        } else {
            post { dispatchBlurRegionsIfNeeded() }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updatePreDrawListenerRegistration()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isPreDrawListenerRegistered = false
        try {
            viewTreeObserver.removeOnPreDrawListener(blurPreDrawListener)
        } catch (_: Throwable) {}
        clearReportedBlurRegions()
    }

    private fun updatePreDrawListenerRegistration() {
        val shouldRegister = blurRegionReportingEnabled && isAttachedToWindow
        if (shouldRegister && !isPreDrawListenerRegistered) {
            try {
                viewTreeObserver.addOnPreDrawListener(blurPreDrawListener)
                isPreDrawListenerRegistered = true
            } catch (_: Throwable) {}
        } else if (!shouldRegister && isPreDrawListenerRegistered) {
            isPreDrawListenerRegistered = false
            try {
                viewTreeObserver.removeOnPreDrawListener(blurPreDrawListener)
            } catch (_: Throwable) {}
        }
    }

    private fun clearReportedBlurRegions() {
        if (lastReportedBlurRegions.isNotEmpty()) {
            lastReportedBlurRegions = emptyList()
            onNotificationBlurRegionsChanged?.invoke(emptyList())
        }
    }

    private fun collectVisibleBlurRegions(): List<QsCardBlurBounds> {
        if (!blurRegionReportingEnabled || !isAttachedToWindow || !isShown || width <= 1 || height <= 1) {
            return emptyList()
        }
        val rootRef = composeHostView ?: this.rootView ?: return emptyList()
        rootRef.getLocationOnScreen(rootLocBuffer)
        this.getLocationOnScreen(containerLocBuffer)

        val hostLeft = (containerLocBuffer[0] - rootLocBuffer[0]).toFloat()
        val hostTop = (containerLocBuffer[1] - rootLocBuffer[1]).toFloat()
        val hostRight = hostLeft + width * scaleX
        val hostBottom = hostTop + height * scaleY
        if (hostRight - hostLeft <= 2f || hostBottom - hostTop <= 2f) return emptyList()

        val result = ArrayList<QsCardBlurBounds>(rvNotifications.childCount + 2)

        fun addClampedCard(
            card: View,
            topCornerPx: Float,
            bottomCornerPx: Float,
            clipLeft: Float,
            clipTop: Float,
            clipRight: Float,
            clipBottom: Float,
            parentAlpha: Float = 1f
        ) {
            if (card.visibility != View.VISIBLE || !card.isShown) return
            if (parentAlpha * card.alpha <= 0.05f || card.width <= 1 || card.height <= 1) return
            card.getLocationOnScreen(cardLocBuffer)
            val rawLeft = (cardLocBuffer[0] - rootLocBuffer[0]).toFloat()
            val rawTop = (cardLocBuffer[1] - rootLocBuffer[1]).toFloat()
            val rawRight = rawLeft + card.width * card.scaleX
            val rawBottom = rawTop + card.height * card.scaleY

            val clampedLeft = maxOf(rawLeft, clipLeft)
            val clampedTop = maxOf(rawTop, clipTop)
            val clampedRight = minOf(rawRight, clipRight)
            val clampedBottom = minOf(rawBottom, clipBottom)
            if (clampedRight - clampedLeft <= 2f || clampedBottom - clampedTop <= 2f) return

            result.add(
                QsCardBlurBounds(
                    left = clampedLeft,
                    top = clampedTop,
                    right = clampedRight,
                    bottom = clampedBottom,
                    topCornerRadiusPx = topCornerPx,
                    bottomCornerRadiusPx = bottomCornerPx
                )
            )
        }

        if (flPermissionCard.visibility == View.VISIBLE) {
            addClampedCard(flPermissionCard, lastCardCornerRadiusPx, lastCardCornerRadiusPx, hostLeft, hostTop, hostRight, hostBottom)
        }
        if (flEmptyCard.visibility == View.VISIBLE) {
            addClampedCard(flEmptyCard, lastCardCornerRadiusPx, lastCardCornerRadiusPx, hostLeft, hostTop, hostRight, hostBottom)
        }
        if (rvNotifications.visibility == View.VISIBLE && rvNotifications.isShown) {
            rvNotifications.getLocationOnScreen(rvLocBuffer)
            val rvLeft = maxOf((rvLocBuffer[0] - rootLocBuffer[0]).toFloat(), hostLeft)
            val rvTop = maxOf((rvLocBuffer[1] - rootLocBuffer[1]).toFloat(), hostTop)
            val rvRight = minOf((rvLocBuffer[0] - rootLocBuffer[0]).toFloat() + rvNotifications.width * rvNotifications.scaleX, hostRight)
            val rvBottom = minOf((rvLocBuffer[1] - rootLocBuffer[1]).toFloat() + rvNotifications.height * rvNotifications.scaleY, hostBottom)
            if (rvRight - rvLeft > 2f && rvBottom - rvTop > 2f) {
                for (i in 0 until rvNotifications.childCount) {
                    val child = rvNotifications.getChildAt(i) ?: continue
                    val holder = rvNotifications.getChildViewHolder(child) as? NotificationAdapter.BlurRegionCardHolder ?: continue
                    val (topR, bottomR) = holder.blurCornerRadiiPx()
                    addClampedCard(
                        card = holder.blurTargetView(),
                        topCornerPx = topR,
                        bottomCornerPx = bottomR,
                        clipLeft = rvLeft,
                        clipTop = rvTop,
                        clipRight = rvRight,
                        clipBottom = rvBottom,
                        parentAlpha = child.alpha
                    )
                }
            }
        }
        return result
    }

    private fun blurRegionsMatch(a: List<QsCardBlurBounds>, b: List<QsCardBlurBounds>): Boolean {
        if (a.size != b.size) return false
        for (i in a.indices) {
            val ra = a[i]
            val rb = b[i]
            if (abs(ra.left - rb.left) >= 0.5f ||
                abs(ra.top - rb.top) >= 0.5f ||
                abs(ra.right - rb.right) >= 0.5f ||
                abs(ra.bottom - rb.bottom) >= 0.5f ||
                abs(ra.topCornerRadiusPx - rb.topCornerRadiusPx) >= 0.5f ||
                abs(ra.bottomCornerRadiusPx - rb.bottomCornerRadiusPx) >= 0.5f
            ) {
                return false
            }
        }
        return true
    }

    private fun dispatchBlurRegionsIfNeeded() {
        if (!blurRegionReportingEnabled) {
            clearReportedBlurRegions()
            return
        }
        val next = collectVisibleBlurRegions()
        if (!blurRegionsMatch(lastReportedBlurRegions, next)) {
            lastReportedBlurRegions = next
            onNotificationBlurRegionsChanged?.invoke(next)
        }
    }

    private fun handleInterceptTouch(e: MotionEvent): Boolean {
        if (com.example.service.MediaSessionManagerHelper.isSeekingMediaProgress.value) {
            return false
        }
        val isLandscape = resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        if (isLandscape) {
            // In landscape split mode, notifications have full independent scrolling without panel drag interception
            return false
        }
        val now = System.currentTimeMillis()
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = e.getPointerId(0)
                startY = e.rawY
                lastY = e.rawY
                lastTime = now
                calculatedVelocityY = 0f
                isDraggingPanel = false
                dragActionType = DRAG_NONE
                velocityTracker?.recycle()
                velocityTracker = VelocityTracker.obtain().apply { addMovement(e) }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val actionIndex = e.actionIndex
                activePointerId = e.getPointerId(actionIndex)
                startY = e.rawY
                lastY = e.rawY
                lastTime = now
                velocityTracker?.addMovement(e)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val actionIndex = e.actionIndex
                if (e.getPointerId(actionIndex) == activePointerId) {
                    val newIndex = if (actionIndex == 0) 1 else 0
                    if (newIndex < e.pointerCount) {
                        activePointerId = e.getPointerId(newIndex)
                        startY = e.rawY
                        lastY = e.rawY
                        lastTime = now
                    }
                }
                velocityTracker?.addMovement(e)
            }
            MotionEvent.ACTION_MOVE -> {
                velocityTracker?.addMovement(e)
                val currentY = e.rawY
                val deltaY = currentY - startY
                val timeDelta = now - lastTime
                if (timeDelta > 0) {
                    calculatedVelocityY = (currentY - lastY) / timeDelta
                }
                lastY = currentY
                lastTime = now

                val density = context.resources.displayMetrics.density
                val threshold = 16f * density

                // Swiping DOWN at top / empty state -> expand QQS to QS / FULL_QS
                if (deltaY > threshold) {
                    if (!canScrollUp() && !PanelPreferences.isAndroidKitKatStyle(context)) {
                        isDraggingPanel = true
                        dragActionType = DRAG_EXPAND_DOWN
                        dragAnchorY = currentY
                        if (currentExpansionStage == QsExpansionStage.QQS) {
                            onExpandToFullQs?.invoke() ?: onStageChange?.invoke(QsExpansionStage.FULL_QS)
                        }
                        return true
                    }
                }
                // Swiping UP at bottom / empty state -> collapse stage or close panel
                else if (deltaY < -threshold) {
                    if (!canScrollDown()) {
                        if (PanelPreferences.isAndroidKitKatStyle(context)) {
                            onStageChange?.invoke(QsExpansionStage.COLLAPSED)
                            return true
                        }
                        isDraggingPanel = true
                        dragActionType = DRAG_CLOSE_UP
                        dragAnchorY = currentY
                        if (currentExpansionStage == QsExpansionStage.FULL_QS) {
                            onStageChange?.invoke(QsExpansionStage.QQS)
                        } else {
                            onStageChange?.invoke(QsExpansionStage.COLLAPSED)
                        }
                        return true
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDraggingPanel = false
                dragActionType = DRAG_NONE
                activePointerId = MotionEvent.INVALID_POINTER_ID
            }
        }
        return false
    }

    private fun handleTouchAction(e: MotionEvent): Boolean {
        if (com.example.service.MediaSessionManagerHelper.isSeekingMediaProgress.value) {
            return false
        }
        val isLandscape = resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        if (isLandscape) {
            return false
        }
        velocityTracker?.addMovement(e)
        val now = System.currentTimeMillis()
        val density = context.resources.displayMetrics.density
        when (e.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                val currentY = e.rawY
                val timeDelta = now - lastTime
                if (timeDelta > 0) {
                    calculatedVelocityY = (currentY - lastY) / timeDelta
                }
                lastY = currentY
                lastTime = now

                if (isDraggingPanel) {
                    val dragDelta = currentY - dragAnchorY
                    if (dragActionType == DRAG_CLOSE_UP) {
                        // User is dragging UP to close panel - live hardware translation under finger!
                        if (dragDelta < 0) {
                            rootPanelView?.translationY = dragDelta * 0.85f
                        }
                    }
                    onDragInteraction?.invoke(dragDelta, MotionEvent.ACTION_MOVE)
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDraggingPanel) {
                    val panel = rootPanelView
                    velocityTracker?.computeCurrentVelocity(1000)
                    val vtVelocity = velocityTracker?.yVelocity ?: 0f
                    val effectiveVelocityY = if (vtVelocity != 0f) vtVelocity else (calculatedVelocityY * 1000f)

                    if (dragActionType == DRAG_CLOSE_UP) {
                        val dragDelta = e.rawY - dragAnchorY
                        val currentTrans = panel?.translationY ?: 0f
                        val panelHeight = panel?.height?.toFloat()?.coerceAtLeast(300f * density) ?: (context.resources.displayMetrics.heightPixels.toFloat())
                        val dismissThresholdPx = panelHeight * 0.15f // Require at least 15% drag height to dismiss
                        val draggedUpDistance = kotlin.math.max(
                            (-dragDelta).coerceAtLeast(0f),
                            (-currentTrans).coerceAtLeast(0f)
                        )
                        val isDraggedFifteenPercent = (PanelPreferences.isAndroidKitKatStyle(context) && (draggedUpDistance >= 24f * density || effectiveVelocityY < -300f)) || (draggedUpDistance >= dismissThresholdPx)

                        if (isDraggedFifteenPercent) {
                            if (panel != null) {
                                val screenHeight = panel.height.toFloat().coerceAtLeast(1000f)
                                val remainingDistance = kotlin.math.abs(-screenHeight - currentTrans)

                                // Dynamic duration based on velocity momentum: glides naturally matching finger speed
                                val animDuration = if (effectiveVelocityY < -350f) {
                                    (remainingDistance / kotlin.math.abs(effectiveVelocityY) * 1000f).toLong().coerceIn(160L, 280L)
                                } else {
                                    220L
                                }

                                // Physically animate/glide upward following finger velocity FIRST!
                                panel.animate()
                                    .translationY(-screenHeight)
                                    .setDuration(animDuration)
                                    .setInterpolator(android.view.animation.DecelerateInterpolator(1.6f))
                                    .withEndAction {
                                        // ONLY hide/close after the upward animation finishes
                                        com.example.service.QuickPanelAccessibilityService.hideCustomPanel()
                                        onStageChange?.invoke(QsExpansionStage.COLLAPSED)
                                        panel.post {
                                            panel.translationY = 0f
                                        }
                                    }
                                    .start()
                            } else {
                                com.example.service.QuickPanelAccessibilityService.hideCustomPanel()
                                onStageChange?.invoke(QsExpansionStage.COLLAPSED)
                            }
                        } else {
                            // Dragged less than 15%: Snap back open smoothly!
                            panel?.animate()
                                ?.translationY(0f)
                                ?.setDuration(200L)
                                ?.setInterpolator(android.view.animation.OvershootInterpolator(0.7f))
                                ?.start()
                        }
                    }
                    onDragInteraction?.invoke(0f, e.actionMasked)
                    isDraggingPanel = false
                    dragActionType = DRAG_NONE
                    activePointerId = MotionEvent.INVALID_POINTER_ID
                    velocityTracker?.recycle()
                    velocityTracker = null
                    return true
                }
                activePointerId = MotionEvent.INVALID_POINTER_ID
                dragActionType = DRAG_NONE
                isDraggingPanel = false
                velocityTracker?.recycle()
                velocityTracker = null
            }
        }
        return false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // When the RecyclerView is visible, allow it to receive touches and handle its own scrolling!
        if (rvNotifications.visibility == View.VISIBLE) {
            return false
        }
        // Only intercept if RecyclerView is GONE (e.g. empty state or permission banner)
        if (handleInterceptTouch(ev)) {
            return true
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (rvNotifications.visibility != View.VISIBLE && handleTouchAction(event)) {
            return true
        }
        return super.onTouchEvent(event)
    }

    private var hasTriggeredBoundaryAction = false

    private fun setupGestureInterception() {
        rvNotifications.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            private var touchStartY = 0f

            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                if (com.example.service.MediaSessionManagerHelper.isSeekingMediaProgress.value) {
                    return false
                }
                val density = context.resources.displayMetrics.density
                val threshold = 16f * density

                when (e.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        touchStartY = e.rawY
                        hasTriggeredBoundaryAction = false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (!hasTriggeredBoundaryAction) {
                            val deltaY = e.rawY - touchStartY
                            if (deltaY > threshold) {
                                // Swiping DOWN: Check if scrolled all the way UP (at top boundary)
                                if (!canScrollUp()) {
                                    if (currentExpansionStage == QsExpansionStage.QQS) {
                                        hasTriggeredBoundaryAction = true
                                        onExpandToFullQs?.invoke() ?: onStageChange?.invoke(QsExpansionStage.FULL_QS)
                                    }
                                }
                            } else if (deltaY < -threshold) {
                                // Swiping UP: Check if scrolled all the way DOWN (at bottom boundary)
                                if (!canScrollDown()) {
                                    hasTriggeredBoundaryAction = true
                                    if (PanelPreferences.isAndroidKitKatStyle(context)) {
                                        onStageChange?.invoke(QsExpansionStage.COLLAPSED)
                                    } else if (currentExpansionStage == QsExpansionStage.FULL_QS) {
                                        onStageChange?.invoke(QsExpansionStage.QQS)
                                    } else {
                                        onStageChange?.invoke(QsExpansionStage.COLLAPSED)
                                    }
                                }
                            }
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        hasTriggeredBoundaryAction = false
                    }
                }
                return false // Always allow RecyclerView to handle native touch scrolling
            }
        })
    }

    private fun initAdapter() {
        adapter = NotificationAdapter(
            onNotificationClick = { onNotificationClick?.invoke(it) },
            onNotificationDismiss = { onNotificationDismiss?.invoke(it) },
            onActionClick = { onActionClick?.invoke(it) },
            onClearAll = { animateAndClearAll() }
        ).also {
            it.categoryItemAnimator = categoryItemAnimator
        }
        adapter?.onRequestClearAnimation = { animateAndClearAll() }
        rvNotifications.adapter = adapter
    }

    private fun setupItemTouchHelper() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                // Media players (regardless of count) and clear-all footer must NEVER be swiped to dismiss
                if (viewHolder !is NotificationAdapter.NotificationViewHolder) {
                    return 0
                }
                // Strictly track by unique notification key from the ViewHolder's bound NotificationItem
                val item = viewHolder.notificationItem
                    ?: adapter?.getNotificationItem(viewHolder.bindingAdapterPosition)
                if (item != null && item.isClearable) {
                    return super.getSwipeDirs(recyclerView, viewHolder)
                }
                return 0
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Strictly dismiss the exact notification bound to this ViewHolder by unique key
                if (viewHolder is NotificationAdapter.NotificationViewHolder) {
                    val item = viewHolder.notificationItem
                        ?: adapter?.getNotificationItem(viewHolder.bindingAdapterPosition)
                    if (item != null && item.isClearable) {
                        onNotificationDismiss?.invoke(item)
                    }
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(rvNotifications)
    }

    fun updateData(
        notifications: List<NotificationItem>,
        isPermissionGranted: Boolean,
        isDark: Boolean,
        primaryColor: Int? = null,
        panelCardBackgroundColor: Int? = null,
        mediaSessions: List<MediaState> = emptyList()
    ) {
        if (com.example.service.MediaSessionManagerHelper.isSeekingMediaProgress.value) {
            return
        }

        val density = context.resources.displayMetrics.density

        val disableNotifMonet = PanelPreferences.disableMonetNotifText.value

        val showMonetSurface = PanelPreferences.showMonetSurfaceBackground.value

        // 1. Resolve Monet Dynamic Colors
        val primaryAccent = primaryColor ?: MonetThemeHelper.getPrimaryAccent(context, isDark)
        val textPrimary = if (disableNotifMonet) {
            if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
        } else {
            MonetThemeHelper.getTextPrimary(context, isDark)
        }
        val textSecondary = if (disableNotifMonet) {
            if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
        } else {
            MonetThemeHelper.getTextSecondary(context, isDark)
        }
        val onPrimary = MonetThemeHelper.getOnPrimaryText(context, isDark)

        val hasMediaCards = mediaSessions.isNotEmpty()

        adapter?.isDark = isDark
        adapter?.customPrimaryColor = primaryAccent
        adapter?.customCardBackgroundColor = panelCardBackgroundColor
        adapter?.disableMonetNotifText = disableNotifMonet
        adapter?.mediaSessions = mediaSessions
        adapter?.isMediaCardShown = hasMediaCards

        // 2. Style Header Row ("Notifications" section label)
        val defaultHeaderColor = if (disableNotifMonet) {
            if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
        } else {
            primaryAccent
        }

        val headerLabelColor = if (!showMonetSurface) {
            android.graphics.Color.WHITE
        } else {
            defaultHeaderColor
        }

        tvHeaderTitle.setTextColor(headerLabelColor)
        val iconHeaderColor = if (!isDark) headerLabelColor else defaultHeaderColor
        ivHeaderIcon.setColorFilter(iconHeaderColor)
        ivSettingsBtn.setColorFilter(iconHeaderColor)

        // 3. Style Permission Banner Card
        val enablePanelShadows = PanelPreferences.enablePanelShadows.value
        val shadowElevation = PanelPreferences.panelShadowElevation.value
        val shadowOffsetY = PanelPreferences.panelShadowOffsetY.value
        val panelShadowAlpha = PanelPreferences.panelShadowAlpha.value
        val cardRadius = if (PanelPreferences.isAndroidOreoStyle(context)) 0f else PanelPreferences.panelOuterCorner.value * density
        lastCardCornerRadiusPx = cardRadius
        val glassSupported = com.example.ui.overlay.isAndroid16FrostedGlassSupported()
        val rawContainerBlur = PanelPreferences.enableContainerBlur.value && glassSupported
        val enableNotificationBlur = PanelPreferences.enableNotificationBlur.value
        val enableContainerBlur = rawContainerBlur
        val containerBlurTransparency = PanelPreferences.containerBlurTransparency.value
        val frostedAlpha = 1f - containerBlurTransparency.coerceIn(0f, 0.95f)
        val rawCardBg = panelCardBackgroundColor ?: MonetThemeHelper.getCardBackground(context, isDark)
        val cardBg = if (enableContainerBlur) {
            ColorUtils.setAlphaComponent(rawCardBg, (frostedAlpha * 255).toInt())
        } else {
            rawCardBg
        }
        val permDrawable = MonetThemeHelper.createMonetCardDrawable(context, isDark, cardRadius, showStroke = false, customBgColor = cardBg)
        val enableGlassRefraction = PanelPreferences.isLiquidGlassActive()
        val glassRefractionSensitivity = PanelPreferences.glassRefractionSensitivity.value
        val enablePanelTopRim = PanelPreferences.enablePanelTopRim.value
        CustomShadowHelper.applyCustomShadowBackground(
            flPermissionCard,
            permDrawable,
            cornerRadius = cardRadius,
            elevation = if (enablePanelShadows) shadowElevation else 0f,
            isDark = isDark,
            customAlpha = panelShadowAlpha,
            customOffsetY = shadowOffsetY,
            enableTopRimBackground = enablePanelTopRim,
            enableFrostedGlass = rawContainerBlur && enableNotificationBlur,
            frostedTransparency = containerBlurTransparency,
            enableGlassRefraction = enableGlassRefraction,
            glassRefractionSensitivity = glassRefractionSensitivity
        )
        CustomShadowHelper.applyPanelTopRimForeground(
            flPermissionCard,
            enabled = enablePanelTopRim,
            cornerRadius = cardRadius
        )
        flPermissionCard.elevation = 0f
        flPermissionCard.translationY = 0f
        ivPermissionIcon.setColorFilter(MonetThemeHelper.getTertiaryAccent(context, isDark))
        tvPermissionTitle.setTextColor(textPrimary)
        tvPermissionSub.setTextColor(textSecondary)
        btnEnablePermission.background = MonetThemeHelper.createMonetPrimaryPillDrawable(context, isDark, 16f * density)
        btnEnablePermission.setTextColor(onPrimary)

        // 4. Style Empty State Card ("No notifications")
        val isOreoStyle = PanelPreferences.isAndroidOreoStyle(context)
        val isMarshmallowStyle = PanelPreferences.isAndroidMarshmallowStyle(context)
        val isKitKatStyle = PanelPreferences.isAndroidKitKatStyle(context)
        if (isOreoStyle || isMarshmallowStyle || isKitKatStyle) {
            flEmptyCard.background = null
            CustomShadowHelper.applyPanelTopRimForeground(flEmptyCard, enabled = false)
            flEmptyCard.elevation = 0f
            flEmptyCard.translationY = 0f
            tvEmptyText.text = "No notifications"
            val emptyColor = if (isKitKatStyle && !isDark) {
                android.graphics.Color.BLACK
            } else if (!showMonetSurface) {
                android.graphics.Color.WHITE
            } else {
                textSecondary
            }
            ivEmptyIcon.setColorFilter(emptyColor)
            tvEmptyText.setTextColor(emptyColor)
        } else {
            val emptyDrawable = MonetThemeHelper.createMonetCardDrawable(context, isDark, cardRadius, showStroke = false, customBgColor = cardBg)
            CustomShadowHelper.applyCustomShadowBackground(
                flEmptyCard,
                emptyDrawable,
                cornerRadius = cardRadius,
                elevation = if (enablePanelShadows) shadowElevation else 0f,
                isDark = isDark,
                customAlpha = panelShadowAlpha,
                customOffsetY = shadowOffsetY,
                enableTopRimBackground = enablePanelTopRim,
                enableFrostedGlass = rawContainerBlur && enableNotificationBlur,
                frostedTransparency = containerBlurTransparency,
                enableGlassRefraction = enableGlassRefraction,
                glassRefractionSensitivity = glassRefractionSensitivity
            )
            CustomShadowHelper.applyPanelTopRimForeground(
                flEmptyCard,
                enabled = enablePanelTopRim,
                cornerRadius = cardRadius
            )
            flEmptyCard.elevation = 0f
            flEmptyCard.translationY = 0f
            tvEmptyText.text = "No new notifications"
            ivEmptyIcon.setColorFilter(textSecondary)
            tvEmptyText.setTextColor(textSecondary)
        }

        // 5. Update Visibility and Data (Floating FAB vs Footer inside RecyclerView)
        val hideEmptyNotifLabels = PanelPreferences.hideQqsEmptyNotifLabels.value
        val useClearAllFab = PanelPreferences.useClearAllFab.value
        val disableClearAllBg = PanelPreferences.disableClearAllBackground.value
        val fabPos = PanelPreferences.clearAllFabPosition.value
        val fabShapeId = PanelPreferences.clearAllFabShapeId.value
        val fabIcon = PanelPreferences.clearAllFabIcon.value
        val fabSize = PanelPreferences.clearAllFabSize.value

        if (!isPermissionGranted) {
            llHeader.visibility = View.VISIBLE
            flPermissionCard.visibility = View.VISIBLE
            flEmptyCard.visibility = View.GONE
            rvNotifications.visibility = View.GONE
            flFloatingClearAllContainer.visibility = View.GONE
        } else if (notifications.isEmpty() && mediaSessions.isEmpty()) {
            flPermissionCard.visibility = View.GONE
            if (hideEmptyNotifLabels) {
                llHeader.visibility = View.GONE
                flEmptyCard.visibility = View.GONE
            } else {
                llHeader.visibility = View.VISIBLE
                flEmptyCard.visibility = View.VISIBLE
            }
            rvNotifications.visibility = View.GONE
            flFloatingClearAllContainer.visibility = View.GONE
        } else {
            llHeader.visibility = View.VISIBLE
            flPermissionCard.visibility = View.GONE
            flEmptyCard.visibility = View.GONE
            rvNotifications.visibility = View.VISIBLE
            
            val hasClearable = notifications.any { it.isClearable }
            val isKitKatStyle = PanelPreferences.isAndroidKitKatStyle(context)
            
            if (isKitKatStyle) {
                flFloatingClearAllContainer.visibility = View.GONE
                btnFloatingClearAllFab.translationX = 0f
                btnFloatingClearAllFab.translationY = 0f
                rvNotifications.setPadding(0, 0, 0, 0)
                adapter?.showFooter = false
            } else if (useClearAllFab && hasClearable) {
                flFloatingClearAllContainer.visibility = View.VISIBLE

                // Position and size floating FAB at the bottom (Left, Center, or Right)
                val lp = btnFloatingClearAllFab.layoutParams as FrameLayout.LayoutParams
                val sizePx = (fabSize * density).toInt()
                lp.width = sizePx
                lp.height = sizePx
                lp.gravity = when (fabPos) {
                    com.example.data.ClearAllFabPosition.LEFT -> android.view.Gravity.START or android.view.Gravity.BOTTOM
                    com.example.data.ClearAllFabPosition.RIGHT -> android.view.Gravity.END or android.view.Gravity.BOTTOM
                    com.example.data.ClearAllFabPosition.CENTER -> android.view.Gravity.CENTER_HORIZONTAL or android.view.Gravity.BOTTOM
                }
                btnFloatingClearAllFab.layoutParams = lp

                val iconLp = ivFloatingClearAllIcon.layoutParams as FrameLayout.LayoutParams
                val iconSizePx = (fabSize * (30f / 64f) * density).toInt()
                iconLp.width = iconSizePx
                iconLp.height = iconSizePx
                ivFloatingClearAllIcon.layoutParams = iconLp

                // Apply horizontal and vertical offset adjustments
                val fabOffsetX = PanelPreferences.clearAllFabOffsetX.value
                val fabOffsetY = PanelPreferences.clearAllFabOffsetY.value
                btnFloatingClearAllFab.translationX = fabOffsetX * density
                btnFloatingClearAllFab.translationY = -fabOffsetY * density

                ivFloatingClearAllIcon.setImageResource(fabIcon.drawableRes)

                val clearAllAccent = MonetThemeHelper.getClearAllAccent(context, isDark)
                val disableClearAllLabel = PanelPreferences.disableClearAllLabel.value
                val showMonetSurface = PanelPreferences.showMonetSurfaceBackground.value

                if (disableClearAllBg) {
                    btnFloatingClearAllFab.background = null
                    btnFloatingClearAllFab.elevation = 0f
                    val iconTint = if (disableClearAllLabel) {
                        if (showMonetSurface) clearAllAccent else android.graphics.Color.WHITE
                    } else {
                        clearAllAccent
                    }
                    ivFloatingClearAllIcon.setColorFilter(iconTint)
                } else {
                    btnFloatingClearAllFab.elevation = 0f
                    val shapeDrawable = com.example.ui.shape.QsShapeManager.createDrawable(fabShapeId, clearAllAccent)
                    val maskDrawable = com.example.ui.shape.QsShapeManager.createDrawable(fabShapeId, android.graphics.Color.WHITE)
                    val rippleColor = if (isDark) android.graphics.Color.parseColor("#44FFFFFF") else android.graphics.Color.parseColor("#33000000")
                    val rippleDrawable = android.graphics.drawable.RippleDrawable(
                        android.content.res.ColorStateList.valueOf(rippleColor),
                        shapeDrawable,
                        maskDrawable
                    )
                    val shapeItem = com.example.ui.shape.QsShapeManager.getShapeItem(fabShapeId)
                    val effectiveFabElevation = if (enablePanelShadows) (if (shadowElevation > 0f) shadowElevation else 6f) else 6f
                    val panelShadowAlpha = PanelPreferences.panelShadowAlpha.value
                    val panelShadowOffsetY = PanelPreferences.panelShadowOffsetY.value
                    CustomShadowHelper.applyCustomShadowBackground(
                        btnFloatingClearAllFab,
                        rippleDrawable,
                        pathData = shapeItem.pathData,
                        viewportWidth = shapeItem.viewportWidth,
                        viewportHeight = shapeItem.viewportHeight,
                        elevation = effectiveFabElevation,
                        isDark = isDark,
                        customAlpha = panelShadowAlpha,
                        customOffsetY = panelShadowOffsetY
                    )
                    val iconTint = if (disableClearAllLabel) {
                        if (showMonetSurface) clearAllAccent else android.graphics.Color.WHITE
                    } else {
                        if (isDark) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                    }
                    ivFloatingClearAllIcon.setColorFilter(iconTint)
                }

                btnFloatingClearAllFab.setOnClickListener {
                    animateAndClearAll()
                }

                // Generous bottom padding so notification list items are never obscured under the floating FAB
                val bottomPaddingDp = ((fabSize + 24f) + maxOf(0f, fabOffsetY)).toInt()
                rvNotifications.setPadding(0, 0, 0, (bottomPaddingDp * density).toInt())
                adapter?.showFooter = false
            } else {
                flFloatingClearAllContainer.visibility = View.GONE
                btnFloatingClearAllFab.translationX = 0f
                btnFloatingClearAllFab.translationY = 0f
                rvNotifications.setPadding(0, 0, 0, 0)
                adapter?.showFooter = hasClearable
            }

            val visualSignature = listOf(
                isDark,
                primaryAccent,
                panelCardBackgroundColor,
                disableNotifMonet,
                showMonetSurface,
                PanelPreferences.panelStyle.value,
                PanelPreferences.panelGlobalCornerEnabled.value,
                PanelPreferences.panelOuterCorner.value,
                PanelPreferences.panelInnerCorner.value,
                PanelPreferences.enablePanelShadows.value,
                PanelPreferences.enablePanelTopRim.value,
                PanelPreferences.topRimAlpha.value,
                PanelPreferences.panelShadowElevation.value,
                PanelPreferences.mediaNotificationStyle.value,
                PanelPreferences.disableClearAllBackground.value,
                PanelPreferences.useClearAllFab.value,
                PanelPreferences.disableClearAllLabel.value,
                PanelPreferences.clearAllFabShapeId.value,
                PanelPreferences.clearAllFabIcon.value,
                PanelPreferences.clearAllFabSize.value,
                rawContainerBlur,
                enableNotificationBlur,
                containerBlurTransparency,
                enableGlassRefraction,
                glassRefractionSensitivity
            ).hashCode()
            val visualChanged = lastVisualSignature != null && lastVisualSignature != visualSignature
            lastVisualSignature = visualSignature

            adapter?.submitNotifications(notifications.toList(), forceVisualRefresh = visualChanged)
            if (blurRegionReportingEnabled) {
                post { dispatchBlurRegionsIfNeeded() }
            }
        }
        if (PanelPreferences.isAndroidOreoStyle(context) || PanelPreferences.isAndroidMarshmallowStyle(context) || PanelPreferences.isAndroidKitKatStyle(context)) {
            llHeader.visibility = View.GONE
            findViewById<View>(R.id.ll_notif_list_container)?.setPadding(0, 0, 0, 0)
        }
    }

    fun animateAndClearAll() {
        val rvWidth = if (rvNotifications.width > 0) rvNotifications.width.toFloat() else 1000f
        var maxDelay = 0L
        var count = 0

        if (flFloatingClearAllContainer.visibility == View.VISIBLE) {
            val disableClearAllLabel = PanelPreferences.disableClearAllLabel.value
            val anim = btnFloatingClearAllFab.animate()
                .scaleX(0.85f)
                .scaleY(0.85f)
            if (!disableClearAllLabel) {
                anim.rotationBy(180f)
            }
            anim.setDuration(180)
                .withEndAction {
                    btnFloatingClearAllFab.scaleX = 1f
                    btnFloatingClearAllFab.scaleY = 1f
                    btnFloatingClearAllFab.rotation = 0f
                }
                .start()
        }

        val animatedViews = mutableListOf<View>()

        for (i in 0 until rvNotifications.childCount) {
            val child = rvNotifications.getChildAt(i)
            val viewHolder = rvNotifications.getChildViewHolder(child)
            if (viewHolder is NotificationAdapter.NotificationViewHolder) {
                val item = viewHolder.notificationItem ?: adapter?.getNotificationItem(viewHolder.bindingAdapterPosition)
                if (item != null && item.isClearable) {
                    animatedViews.add(child)
                    val delay = count * 35L
                    count++
                    if (delay > maxDelay) maxDelay = delay

                    child.animate()
                        .translationX(rvWidth)
                        .alpha(0f)
                        .setStartDelay(delay)
                        .setDuration(220)
                        .setInterpolator(android.view.animation.AccelerateInterpolator())
                        .start()
                }
            }
        }

        val totalDuration = if (animatedViews.isNotEmpty()) maxDelay + 240L else 0L
        if (totalDuration > 0L) {
            rvNotifications.postDelayed({
                for (v in animatedViews) {
                    v.translationX = 0f
                    v.alpha = 1f
                }
                onClearAll?.invoke()
            }, totalDuration)
        } else {
            onClearAll?.invoke()
        }
    }
}

class CategoryExpandItemAnimator : DefaultItemAnimator() {

    var pendingExpandingChildIndexByKey: Map<String, Int> = emptyMap()
    var pendingCollapsingKeys: Set<String> = emptySet()

    private val pendingCustomAdds = mutableListOf<Pair<RecyclerView.ViewHolder, Int>>()
    private val runningCustomAdds = mutableListOf<RecyclerView.ViewHolder>()
    private val pendingCustomRemoves = mutableListOf<RecyclerView.ViewHolder>()
    private val runningCustomRemoves = mutableListOf<RecyclerView.ViewHolder>()

    private val standardInterpolator = PathInterpolator(0.2f, 0f, 0f, 1f)
    private val exitInterpolator = PathInterpolator(0.4f, 0f, 1f, 1f)

    init {
        addDuration = 250L
        removeDuration = 190L
        moveDuration = 250L
        changeDuration = 220L
        supportsChangeAnimations = true
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        val key = (holder as? NotificationAdapter.NotificationViewHolder)?.notificationItem?.key
        val childIndex = if (key != null) pendingExpandingChildIndexByKey[key] else null
        if (childIndex != null) {
            endAnimation(holder)
            val density = holder.itemView.resources.displayMetrics.density
            holder.itemView.alpha = 0f
            holder.itemView.translationY = -22f * density
            holder.itemView.scaleX = 0.96f
            holder.itemView.scaleY = 0.90f
            pendingCustomAdds.add(holder to childIndex)
            return true
        }
        return super.animateAdd(holder)
    }

    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        val key = (holder as? NotificationAdapter.NotificationViewHolder)?.notificationItem?.key
        if (key != null && key in pendingCollapsingKeys) {
            endAnimation(holder)
            pendingCustomRemoves.add(holder)
            return true
        }
        return super.animateRemove(holder)
    }

    override fun runPendingAnimations() {
        super.runPendingAnimations()

        if (pendingCustomRemoves.isNotEmpty()) {
            val removes = pendingCustomRemoves.toList()
            pendingCustomRemoves.clear()
            for (holder in removes) {
                animateCategoryCollapseImpl(holder)
            }
        }

        if (pendingCustomAdds.isNotEmpty()) {
            val adds = pendingCustomAdds.toList()
            pendingCustomAdds.clear()
            for ((holder, childIndex) in adds) {
                animateCategoryExpandImpl(holder, childIndex)
            }
        }

        pendingExpandingChildIndexByKey = emptyMap()
        pendingCollapsingKeys = emptySet()
    }

    private fun animateCategoryExpandImpl(holder: RecyclerView.ViewHolder, childIndex: Int) {
        val view = holder.itemView
        runningCustomAdds.add(holder)
        view.pivotX = if (view.width > 0) view.width * 0.5f else 0f
        view.pivotY = 0f
        val animation = view.animate()
        dispatchAddStarting(holder)
        animation
            .alpha(1f)
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay((childIndex * 32L).coerceAtMost(128L))
            .setDuration(260L)
            .setInterpolator(standardInterpolator)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationCancel(animator: Animator) {
                    resetViewTransforms(view)
                }

                override fun onAnimationEnd(animator: Animator) {
                    animation.setListener(null)
                    animation.startDelay = 0L
                    resetViewTransforms(view)
                    runningCustomAdds.remove(holder)
                    dispatchAddFinished(holder)
                    if (!isRunning) dispatchAnimationsFinished()
                }
            })
            .start()
    }

    private fun animateCategoryCollapseImpl(holder: RecyclerView.ViewHolder) {
        val view = holder.itemView
        val density = view.resources.displayMetrics.density
        runningCustomRemoves.add(holder)
        view.pivotX = if (view.width > 0) view.width * 0.5f else 0f
        view.pivotY = 0f
        val animation = view.animate()
        dispatchRemoveStarting(holder)
        animation
            .alpha(0f)
            .translationY(-14f * density)
            .scaleY(0.92f)
            .setStartDelay(0L)
            .setDuration(180L)
            .setInterpolator(exitInterpolator)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationCancel(animator: Animator) {
                    resetViewTransforms(view)
                }

                override fun onAnimationEnd(animator: Animator) {
                    animation.setListener(null)
                    resetViewTransforms(view)
                    runningCustomRemoves.remove(holder)
                    dispatchRemoveFinished(holder)
                    if (!isRunning) dispatchAnimationsFinished()
                }
            })
            .start()
    }

    private fun resetViewTransforms(view: View) {
        view.alpha = 1f
        view.translationY = 0f
        view.scaleX = 1f
        view.scaleY = 1f
    }

    override fun endAnimation(item: RecyclerView.ViewHolder) {
        val view = item.itemView
        view.animate().cancel()
        if (pendingCustomAdds.removeAll { it.first == item }) {
            resetViewTransforms(view)
            dispatchAddFinished(item)
        }
        if (pendingCustomRemoves.remove(item)) {
            resetViewTransforms(view)
            dispatchRemoveFinished(item)
        }
        if (runningCustomAdds.remove(item)) {
            resetViewTransforms(view)
            dispatchAddFinished(item)
        }
        if (runningCustomRemoves.remove(item)) {
            resetViewTransforms(view)
            dispatchRemoveFinished(item)
        }
        super.endAnimation(item)
    }

    override fun endAnimations() {
        for (i in pendingCustomAdds.indices.reversed()) {
            val holder = pendingCustomAdds.removeAt(i).first
            resetViewTransforms(holder.itemView)
            dispatchAddFinished(holder)
        }
        for (i in pendingCustomRemoves.indices.reversed()) {
            val holder = pendingCustomRemoves.removeAt(i)
            resetViewTransforms(holder.itemView)
            dispatchRemoveFinished(holder)
        }
        for (holder in runningCustomAdds.toList()) {
            holder.itemView.animate().cancel()
        }
        for (holder in runningCustomRemoves.toList()) {
            holder.itemView.animate().cancel()
        }
        super.endAnimations()
    }

    override fun isRunning(): Boolean {
        return pendingCustomAdds.isNotEmpty() ||
            pendingCustomRemoves.isNotEmpty() ||
            runningCustomAdds.isNotEmpty() ||
            runningCustomRemoves.isNotEmpty() ||
            super.isRunning()
    }
}
