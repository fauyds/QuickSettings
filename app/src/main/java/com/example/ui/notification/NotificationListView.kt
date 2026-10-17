package com.example.ui.notification

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.model.NotificationActionModel
import com.example.model.NotificationItem
import com.example.ui.overlay.QsExpansionStage
import com.example.data.PanelPreferences

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

        // Enable completely free, unconstrained scrolling with native physics
        rvNotifications.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rvNotifications.isNestedScrollingEnabled = false
        rvNotifications.overScrollMode = View.OVER_SCROLL_NEVER
        rvNotifications.clipToPadding = false
        rvNotifications.setHasFixedSize(false)
        rvNotifications.descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS

        setupGestureInterception()

        rvNotifications.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator().apply {
            addDuration = 180
            removeDuration = 180
            moveDuration = 180
            changeDuration = 180
        }

        btnEnablePermission.setOnClickListener {
            onRequestPermission?.invoke()
        }

        ivSettingsBtn.setOnClickListener {
            onOpenNotificationManager?.invoke()
        }

        initAdapter()
        setupItemTouchHelper()
    }

    private fun handleInterceptTouch(e: MotionEvent): Boolean {
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
                    if (!canScrollUp()) {
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
                        val isDraggedFifteenPercent = draggedUpDistance >= dismissThresholdPx

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
                                    if (currentExpansionStage == QsExpansionStage.FULL_QS) {
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
            onClearAll = { onClearAll?.invoke() }
        )
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
                val position = viewHolder.bindingAdapterPosition
                if (position in 0 until (adapter?.currentList?.size ?: 0)) {
                    val item = adapter?.currentList?.get(position)
                    if (item?.isClearable == true) {
                        return super.getSwipeDirs(recyclerView, viewHolder)
                    }
                }
                return 0
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position in 0 until (adapter?.currentList?.size ?: 0)) {
                    val item = adapter?.currentList?.get(position)
                    if (item != null) {
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
        primaryColor: Int? = null
    ) {
        val density = context.resources.displayMetrics.density

        // 1. Resolve Monet Dynamic Colors
        val primaryAccent = primaryColor ?: MonetThemeHelper.getPrimaryAccent(context, isDark)
        val textPrimary = MonetThemeHelper.getTextPrimary(context, isDark)
        val textSecondary = MonetThemeHelper.getTextSecondary(context, isDark)
        val onPrimary = MonetThemeHelper.getOnPrimaryText(context, isDark)

        adapter?.isDark = isDark
        adapter?.customPrimaryColor = primaryAccent

        // 2. Style Header Row
        tvHeaderTitle.setTextColor(primaryAccent)
        ivHeaderIcon.setColorFilter(primaryAccent)
        ivSettingsBtn.setColorFilter(primaryAccent)

        // 3. Style Permission Banner Card
        flPermissionCard.background = MonetThemeHelper.createMonetCardDrawable(context, isDark, PanelPreferences.qsBackgroundCorner.value * density)
        ivPermissionIcon.setColorFilter(MonetThemeHelper.getTertiaryAccent(context, isDark))
        tvPermissionTitle.setTextColor(textPrimary)
        tvPermissionSub.setTextColor(textSecondary)
        btnEnablePermission.background = MonetThemeHelper.createMonetPrimaryPillDrawable(context, isDark, 16f * density)
        btnEnablePermission.setTextColor(onPrimary)

        // 4. Style Empty State Card
        flEmptyCard.background = MonetThemeHelper.createMonetCardDrawable(context, isDark, PanelPreferences.qsBackgroundCorner.value * density)
        // Apply custom color to background cards if set
        PanelPreferences.qsBackgroundColor.value?.let { customColor ->
            (flPermissionCard.background as? android.graphics.drawable.RippleDrawable)?.setColor(android.content.res.ColorStateList.valueOf(customColor))
            (flEmptyCard.background as? android.graphics.drawable.RippleDrawable)?.setColor(android.content.res.ColorStateList.valueOf(customColor))
        }
        ivEmptyIcon.setColorFilter(textSecondary)
        tvEmptyText.setTextColor(textSecondary)

        // 5. Update Visibility and Data (Footer inside RecyclerView)
        if (!isPermissionGranted) {
            flPermissionCard.visibility = View.VISIBLE
            flEmptyCard.visibility = View.GONE
            rvNotifications.visibility = View.GONE
        } else if (notifications.isEmpty()) {
            flPermissionCard.visibility = View.GONE
            flEmptyCard.visibility = View.VISIBLE
            rvNotifications.visibility = View.GONE
        } else {
            flPermissionCard.visibility = View.GONE
            flEmptyCard.visibility = View.GONE
            rvNotifications.visibility = View.VISIBLE
            
            val hasClearable = notifications.any { it.isClearable }
            adapter?.showFooter = hasClearable
            adapter?.submitList(notifications)
        }
    }
}
