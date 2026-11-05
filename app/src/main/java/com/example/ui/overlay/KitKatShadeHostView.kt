package com.example.ui.overlay

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.provider.Settings
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.model.NotificationActionModel
import com.example.model.NotificationItem
import com.example.model.QsTileModel
import com.example.model.TileState
import com.example.ui.notification.MonetThemeHelper
import com.example.ui.notification.NotificationListView
import com.example.ui.tile.QsTileIconHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Android 4.4 KitKat (Flat Holo) shade host view.
 * Dynamically inflates shade_layout_kitkat.xml and manages:
 * - Single-stage shade with smooth scrolling
 * - Universal closing (swipe up anywhere on bottom, header, or when scrolled to bottom) regardless of which screen is open
 * - 1:1 aspect ratio tiles (square cards: same height as width)
 * - Header screen switcher (Notifications <-> Quick Settings)
 * - Continuous horizontal brightness slider
 */
class KitKatShadeHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // Header views
    private val llHeader: LinearLayout
    private val tvTime: TextView
    private val tvDate: TextView
    private val tvCarrier: TextView
    private val btnClearAll: ImageView
    private val btnSwitchScreen: ImageView

    // Container views
    private val flNotifContainer: FrameLayout
    private val notifListView: NotificationListView
    private val flQsContainer: LinearLayout

    // Persistent bottom closing bar
    private val llBottomBar: LinearLayout
    private val tvBottomCarrier: TextView

    // Brightness views
    private val ivBrightnessIcon: ImageView
    private val sbBrightness: SeekBar
    private val tvBrightnessPercentage: TextView

    // Quick Settings Grid
    private val rvTiles: RecyclerView
    private val tileAdapter: KitKatTileAdapter

    // State
    private var isShowingQuickSettings: Boolean = false
    private var currentNotifications: List<NotificationItem> = emptyList()
    private var isDark: Boolean = true

    // Touch tracking for universal dismiss
    private var touchDownY: Float = 0f
    private var touchDownX: Float = 0f
    private var velocityTracker: VelocityTracker? = null

    // Callbacks
    var onTileClick: ((QsTileModel) -> Unit)? = null
    var onTileLongClick: ((QsTileModel) -> Unit)? = null
    var onBrightnessChange: ((Int) -> Unit)? = null
    var onNotificationClick: ((NotificationItem) -> Unit)? = null
    var onNotificationDismiss: ((NotificationItem) -> Unit)? = null
    var onNotificationActionClick: ((NotificationActionModel) -> Unit)? = null
    var onDismissAllNotifications: (() -> Unit)? = null
    var onRequestNotificationAccess: (() -> Unit)? = null
    var onOpenNotificationManager: (() -> Unit)? = null
    var onClosePanel: (() -> Unit)? = null

    init {
        val root = LayoutInflater.from(context).inflate(R.layout.shade_layout_kitkat, this, true)

        llHeader = root.findViewById(R.id.ll_kitkat_header)
        tvTime = root.findViewById(R.id.tv_oreo_top_time)
        tvDate = root.findViewById(R.id.tv_qs_oreo_header_date)
        tvCarrier = root.findViewById(R.id.tv_oreo_top_carrier)
        btnClearAll = root.findViewById(R.id.btn_kitkat_clear_all)
        btnSwitchScreen = root.findViewById(R.id.btn_kitkat_switch_screen)

        flNotifContainer = root.findViewById(R.id.fl_kitkat_notif_container)
        notifListView = root.findViewById(R.id.view_kitkat_notification_list)
        flQsContainer = root.findViewById(R.id.fl_kitkat_qs_container)

        llBottomBar = root.findViewById(R.id.ll_kitkat_bottom_carrier_bar)
        tvBottomCarrier = root.findViewById(R.id.tv_kitkat_bottom_carrier)

        ivBrightnessIcon = root.findViewById(R.id.iv_qs_top_brightness_icon)
        sbBrightness = root.findViewById(R.id.sb_qs_top_brightness)
        tvBrightnessPercentage = root.findViewById(R.id.tv_qs_top_brightness_percentage)

        rvTiles = root.findViewById(R.id.rv_qs_tiles)
        rvTiles.layoutManager = GridLayoutManager(context, 3)
        rvTiles.isNestedScrollingEnabled = true
        rvTiles.overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS

        tileAdapter = KitKatTileAdapter(
            context = context,
            onTileClick = { tile ->
                if (tile.id == "kitkat_profile") {
                    openUserSettings()
                } else {
                    onTileClick?.invoke(tile)
                }
            },
            onTileLongClick = { tile ->
                if (tile.id != "kitkat_profile") {
                    onTileLongClick?.invoke(tile)
                }
            }
        )
        rvTiles.adapter = tileAdapter

        setupListeners()
        updateDateTime()
        updateViewVisibility()
    }

    private fun setupListeners() {
        val density = context.resources.displayMetrics.density

        btnSwitchScreen.setOnClickListener {
            isShowingQuickSettings = !isShowingQuickSettings
            updateViewVisibility()
        }

        btnClearAll.setOnClickListener {
            onDismissAllNotifications?.invoke()
        }

        tvTime.setOnClickListener {
            launchClockApp()
        }

        tvDate.setOnClickListener {
            launchCalendarApp()
        }

        // Tapping or swiping up on the bottom bar closes the panel immediately on ANY screen
        llBottomBar.setOnClickListener {
            onClosePanel?.invoke()
        }

        sbBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    onBrightnessChange?.invoke(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        notifListView.apply {
            this.rootPanelView = this@KitKatShadeHostView
            this.onNotificationClick = { this@KitKatShadeHostView.onNotificationClick?.invoke(it) }
            this.onNotificationDismiss = { this@KitKatShadeHostView.onNotificationDismiss?.invoke(it) }
            this.onActionClick = { this@KitKatShadeHostView.onNotificationActionClick?.invoke(it) }
            this.onClearAll = { this@KitKatShadeHostView.onDismissAllNotifications?.invoke() }
            this.onRequestPermission = { this@KitKatShadeHostView.onRequestNotificationAccess?.invoke() }
            this.onOpenNotificationManager = { this@KitKatShadeHostView.onOpenNotificationManager?.invoke() }
            this.onStageChange = { stage ->
                if (stage == QsExpansionStage.COLLAPSED) {
                    this@KitKatShadeHostView.onClosePanel?.invoke()
                }
            }
            this.currentExpansionStage = QsExpansionStage.FULL_QS
        }

        // rvTiles swipe-up listener when tiles reach bottom or all fit on screen
        var rvTilesDownY = 0f
        rvTiles.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        rvTilesDownY = e.rawY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaY = e.rawY - rvTilesDownY
                        // When swiping UP and rvTiles cannot scroll down any further: trigger close!
                        if (deltaY < -24f * density && !rv.canScrollVertically(1)) {
                            onClosePanel?.invoke()
                            return true
                        }
                    }
                }
                return false
            }
        })
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val density = context.resources.displayMetrics.density
        val threshold = 20f * density

        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchDownY = ev.rawY
                touchDownX = ev.rawX
                velocityTracker?.recycle()
                velocityTracker = VelocityTracker.obtain()
                velocityTracker?.addMovement(ev)
            }
            MotionEvent.ACTION_MOVE -> {
                velocityTracker?.addMovement(ev)
                val deltaY = ev.rawY - touchDownY
                val deltaX = ev.rawX - touchDownX

                // Vertical upward swipe detected
                if (deltaY < -threshold && kotlin.math.abs(deltaY) > kotlin.math.abs(deltaX)) {
                    val viewHeight = height.toFloat().coerceAtLeast(1000f)
                    val inBottomZone = touchDownY > viewHeight * 0.70f
                    val inHeaderZone = touchDownY < 120f * density

                    // Always intercept upward swipes in header or bottom zone
                    if (inBottomZone || inHeaderZone) {
                        return true
                    }
                    // On QS screen: intercept upward swipe if tiles cannot scroll further down
                    if (isShowingQuickSettings && !rvTiles.canScrollVertically(1)) {
                        return true
                    }
                    // On Notification screen: intercept upward swipe if notifications cannot scroll further down
                    if (!isShowingQuickSettings && !notifListView.canScrollDown()) {
                        return true
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                velocityTracker?.recycle()
                velocityTracker = null
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val density = context.resources.displayMetrics.density
        velocityTracker?.addMovement(ev)

        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchDownY = ev.rawY
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaY = ev.rawY - touchDownY
                if (deltaY < -24f * density) {
                    onClosePanel?.invoke()
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                velocityTracker?.computeCurrentVelocity(1000)
                val vy = velocityTracker?.yVelocity ?: 0f
                val deltaY = ev.rawY - touchDownY
                if (deltaY < -16f * density || vy < -300f) {
                    onClosePanel?.invoke()
                    return true
                }
                velocityTracker?.recycle()
                velocityTracker = null
            }
        }
        return super.onTouchEvent(ev)
    }

    private fun updateViewVisibility() {
        if (isShowingQuickSettings) {
            flNotifContainer.visibility = View.GONE
            flQsContainer.visibility = View.VISIBLE
            btnSwitchScreen.setImageResource(R.drawable.ic_kitkat_notif_switch)
            btnSwitchScreen.contentDescription = "Switch to Notifications"
            tvCarrier.visibility = View.VISIBLE
            btnClearAll.visibility = View.GONE
        } else {
            flNotifContainer.visibility = View.VISIBLE
            flQsContainer.visibility = View.GONE
            btnSwitchScreen.setImageResource(R.drawable.ic_kitkat_qs_switch)
            btnSwitchScreen.contentDescription = "Switch to Quick Settings"
            tvCarrier.visibility = View.GONE
            btnClearAll.visibility = if (currentNotifications.isNotEmpty()) View.VISIBLE else View.INVISIBLE
        }
    }

    fun updateData(
        tiles: List<QsTileModel>,
        brightness: Int,
        notifications: List<NotificationItem>,
        isNotificationPermissionGranted: Boolean,
        carrierName: String,
        isDark: Boolean
    ) {
        this.isDark = isDark
        this.currentNotifications = notifications

        updateDateTime()

        // 1. Carrier display
        val resolvedCarrier = if (carrierName.isNotBlank()) carrierName.uppercase() else "EMERGENCY CALLS ONLY"
        tvCarrier.text = resolvedCarrier
        tvBottomCarrier.text = resolvedCarrier

        // 2. Clear All visibility
        if (!isShowingQuickSettings) {
            btnClearAll.visibility = if (notifications.isNotEmpty()) View.VISIBLE else View.INVISIBLE
        }

        // 3. Brightness Slider Styling & Progress
        val primaryAccent = MonetThemeHelper.getPrimaryAccent(context, isDark)
        val mutedSurface = Color.parseColor("#33FFFFFF")
        sbBrightness.thumbTintList = ColorStateList.valueOf(primaryAccent)
        sbBrightness.progressTintList = ColorStateList.valueOf(primaryAccent)
        sbBrightness.progressBackgroundTintList = ColorStateList.valueOf(mutedSurface)
        ivBrightnessIcon.setColorFilter(primaryAccent)

        if (sbBrightness.progress != brightness) {
            sbBrightness.progress = brightness.coerceIn(sbBrightness.min, sbBrightness.max)
        }

        // 4. Quick Settings 3-column Grid
        // Position 0 is User Profile Avatar tile, followed by tiles with Brightness tile removed
        val filteredTiles = tiles.filter { it.id != "brightness" }
        val kitKatTiles = mutableListOf<QsTileModel>()

        // Add Profile Avatar Tile at top-left
        kitKatTiles.add(
            QsTileModel(
                id = "kitkat_profile",
                label = "OWNER",
                subtitle = "",
                state = TileState.INACTIVE,
                category = com.example.model.TileCategory.SYSTEM,
                isVisible = true
            )
        )
        kitKatTiles.addAll(filteredTiles)
        tileAdapter.submitList(kitKatTiles, primaryAccent, isDark)

        // 5. Notifications List View
        notifListView.updateData(
            notifications = notifications,
            isPermissionGranted = isNotificationPermissionGranted,
            isDark = isDark,
            primaryColor = primaryAccent,
            panelCardBackgroundColor = Color.parseColor("#1C1C1C"),
            mediaSessions = emptyList()
        )
    }

    private fun updateDateTime() {
        val now = Date()
        val timeFormat = SimpleDateFormat("H:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEE, MMMM d", Locale.getDefault())
        tvTime.text = timeFormat.format(now)
        tvDate.text = dateFormat.format(now).uppercase()
    }

    private fun openUserSettings() {
        try {
            val intent = Intent("android.settings.USER_SETTINGS").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
        onClosePanel?.invoke()
    }

    private fun launchClockApp() {
        try {
            val intent = Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onClosePanel?.invoke()
        } catch (_: Exception) {
            try {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_CALENDAR)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                onClosePanel?.invoke()
            } catch (_: Exception) {}
        }
    }

    private fun launchCalendarApp() {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_CALENDAR)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onClosePanel?.invoke()
        } catch (_: Exception) {}
    }

    private class KitKatTileAdapter(
        private val context: Context,
        private val onTileClick: (QsTileModel) -> Unit,
        private val onTileLongClick: (QsTileModel) -> Unit
    ) : RecyclerView.Adapter<KitKatTileViewHolder>() {

        private var items: List<QsTileModel> = emptyList()
        private var primaryAccent: Int = Color.parseColor("#33B5E5")
        private var isDark: Boolean = true

        fun submitList(newItems: List<QsTileModel>, primaryAccent: Int, isDark: Boolean) {
            this.items = newItems
            this.primaryAccent = primaryAccent
            this.isDark = isDark
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KitKatTileViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_qs_tile_kitkat, parent, false)
            return KitKatTileViewHolder(view)
        }

        override fun onBindViewHolder(holder: KitKatTileViewHolder, position: Int) {
            val tile = items.getOrNull(position) ?: return

            if (tile.id == "kitkat_profile") {
                // Profile Avatar tile (top-left)
                holder.ivIcon.setImageResource(R.drawable.ic_kitkat_user_avatar)
                holder.ivIcon.clearColorFilter()
                holder.tvLabel.text = "OWNER"
                holder.tvLabel.setTextColor(Color.parseColor("#CCCCCC"))
                holder.tvSubtitle.visibility = View.GONE
                holder.cardRoot.setBackgroundColor(Color.parseColor("#1E1E1E"))
                holder.cardRoot.setOnClickListener { onTileClick(tile) }
                holder.cardRoot.setOnLongClickListener { false }
                return
            }

            // Regular System Quick Settings Tile
            val isActive = tile.state == TileState.ACTIVE
            val drawable = QsTileIconHelper.getTileDrawable(context, tile)
            holder.ivIcon.setImageDrawable(drawable)

            if (isActive) {
                // Active state: Monet primary accent
                holder.ivIcon.setColorFilter(primaryAccent)
                holder.tvLabel.setTextColor(Color.WHITE)
            } else {
                // Inactive state: muted/dimmed vector icons on dark flat tile backgrounds
                holder.ivIcon.setColorFilter(Color.parseColor("#999999"))
                holder.tvLabel.setTextColor(Color.parseColor("#AAAAAA"))
            }

            holder.tvLabel.text = tile.label.uppercase()

            if (tile.subtitle.isNotBlank() && tile.subtitle != tile.label) {
                holder.tvSubtitle.text = tile.subtitle.uppercase()
                holder.tvSubtitle.visibility = View.VISIBLE
            } else {
                holder.tvSubtitle.visibility = View.GONE
            }

            holder.cardRoot.setBackgroundColor(Color.parseColor("#1E1E1E"))
            holder.cardRoot.setOnClickListener { onTileClick(tile) }
            holder.cardRoot.setOnLongClickListener {
                onTileLongClick(tile)
                true
            }
        }

        override fun getItemCount(): Int = items.size
    }

    private class KitKatTileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardRoot: View = itemView.findViewById(R.id.fl_kitkat_tile_card)
        val ivIcon: ImageView = itemView.findViewById(R.id.iv_tile_icon)
        val tvLabel: TextView = itemView.findViewById(R.id.tv_tile_label)
        val tvSubtitle: TextView = itemView.findViewById(R.id.tv_tile_subtitle)
    }
}
