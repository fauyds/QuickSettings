package com.example.ui.overlay

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
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
import com.example.data.PanelPreferences
import com.example.model.NotificationActionModel
import com.example.model.NotificationItem
import com.example.model.QsTileModel
import com.example.model.TileState
import com.example.ui.notification.MonetThemeHelper
import com.example.ui.notification.NotificationListView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProgressBarStyle
import com.example.data.QsSliderTarget
import com.example.model.TileCategory
import com.example.system.ExternalTileIcon
import com.example.ui.components.QsProgressSlider
import com.example.ui.theme.QuickPanelTheme
import com.example.ui.theme.getInactiveTilesElementColor
import com.example.ui.theme.getSlidersElementColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TYPE_KITKAT_HEADER = 0
private const val TYPE_KITKAT_TILE = 1

/**
 * Android 4.4 KitKat (Flat Holo) shade host view.
 * Dynamically inflates shade_layout_kitkat.xml and manages:
 * - Single-stage shade with smooth scrolling
 * - Universal closing (swipe up anywhere on bottom, header, or when scrolled to bottom) regardless of which screen is open
 * - 1:1 aspect ratio tiles (square cards: same height as width)
 * - Header screen switcher (Notifications <-> Quick Settings)
 * - Continuous horizontal brightness slider inside tiles container
 */
class KitKatShadeHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // Root & Header views
    private val rootLayout: FrameLayout
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
    private val vBottomDivider: View

    // Brightness Compose state
    private val brightnessState = mutableIntStateOf(128)
    private val isDarkState = mutableStateOf(true)
    private val showMonetSurfaceState = mutableStateOf(false)

    // Hidden stubs for View ID parity
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

        rootLayout = root.findViewById(R.id.shade_layout_kitkat_root)
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
        vBottomDivider = root.findViewById(R.id.v_kitkat_bottom_divider)

        ivBrightnessIcon = root.findViewById(R.id.iv_qs_top_brightness_icon)
        sbBrightness = root.findViewById(R.id.sb_qs_top_brightness)
        tvBrightnessPercentage = root.findViewById(R.id.tv_qs_top_brightness_percentage)

        rvTiles = root.findViewById(R.id.rv_qs_tiles)
        tileAdapter = KitKatTileAdapter(
            context = context,
            onTileClick = { tile -> onTileClick?.invoke(tile) },
            onTileLongClick = { tile -> onTileLongClick?.invoke(tile) }
        )

        val gridManager = GridLayoutManager(context, 3)
        gridManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (tileAdapter.getItemViewType(position) == TYPE_KITKAT_HEADER) 3 else 1
            }
        }
        rvTiles.layoutManager = gridManager
        rvTiles.isNestedScrollingEnabled = true
        rvTiles.overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
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
        var rvTilesStartedAtBottom = false
        var rvTilesDownY = 0f
        rvTiles.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        rvTilesDownY = e.rawY
                        rvTilesStartedAtBottom = !rv.canScrollVertically(1)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaY = e.rawY - rvTilesDownY
                        // Only close panel if touch started when tiles were ALREADY at the bottom
                        if (rvTilesStartedAtBottom && deltaY < -32f * density && !rv.canScrollVertically(1)) {
                            onClosePanel?.invoke()
                            return true
                        }
                    }
                }
                return false
            }
        })
    }

    private var initialTouchStartedAtBottom = false

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val density = context.resources.displayMetrics.density
        val threshold = 24f * density

        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchDownY = ev.rawY
                touchDownX = ev.rawX
                initialTouchStartedAtBottom = if (isShowingQuickSettings) {
                    !rvTiles.canScrollVertically(1)
                } else {
                    !notifListView.canScrollDown()
                }
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
                    val inHeaderZone = touchDownY < 60f * density
                    val inBottomCarrierBar = touchDownY > viewHeight - 48f * density

                    // Always intercept upward swipes on top header bar or bottom carrier bar
                    if (inHeaderZone || inBottomCarrierBar) {
                        return true
                    }
                    // On QS screen: intercept upward swipe ONLY if touch started when tiles were ALREADY at bottom
                    if (isShowingQuickSettings && initialTouchStartedAtBottom && !rvTiles.canScrollVertically(1)) {
                        return true
                    }
                    // On Notification screen: intercept upward swipe ONLY if touch started when notifications were ALREADY at bottom
                    if (!isShowingQuickSettings && initialTouchStartedAtBottom && !notifListView.canScrollDown()) {
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
        isDark: Boolean,
        showMonetSurface: Boolean = PanelPreferences.showMonetSurfaceBackground.value,
        showHeaderGradient: Boolean = PanelPreferences.showCustomHeaderMonetGradient.value
    ) {
        this.isDark = isDark
        this.isDarkState.value = isDark
        this.showMonetSurfaceState.value = showMonetSurface
        this.brightnessState.intValue = brightness
        this.currentNotifications = notifications

        updateDateTime()

        // 1. Resolve Monet Theme Dynamic Colors
        val primaryAccent = MonetThemeHelper.getPrimaryAccent(context, isDark)
        val textPrimary = MonetThemeHelper.getTextPrimary(context, isDark)
        val textSecondary = MonetThemeHelper.getTextSecondary(context, isDark)
        val cardOutline = MonetThemeHelper.getCardOutline(context, isDark)
        val tileAccent = MonetThemeHelper.getQsTilesAccent(context, isDark)
        val sliderAccent = MonetThemeHelper.getSlidersAccent(context, isDark)
        val clearAllAccent = MonetThemeHelper.getClearAllAccent(context, isDark)

        val customPanelBg = PanelPreferences.qsPanelBackgroundColor.value
        val panelSurfaceColor = when {
            customPanelBg != null -> customPanelBg
            showMonetSurface -> MonetThemeHelper.getSurfaceBackground(context, isDark)
            else -> if (isDark) Color.BLACK else MonetThemeHelper.getSurfaceBackground(context, isDark)
        }

        val tileCardBg = when {
            showMonetSurface -> MonetThemeHelper.getCardBackground(context, isDark)
            isDark -> Color.parseColor("#1E1E1E")
            else -> MonetThemeHelper.getCardBackground(context, isDark)
        }

        // Apply background to root panel
        rootLayout.setBackgroundColor(panelSurfaceColor)

        // Apply Monet Header Gradient if enabled
        if (showHeaderGradient) {
            val gradColor = MonetThemeHelper.getHeaderGradientColor(context, isDark)
            val gd = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(gradColor, Color.TRANSPARENT)
            )
            llHeader.background = gd
        } else {
            llHeader.setBackgroundColor(Color.TRANSPARENT)
        }

        // Themed header controls
        tvTime.setTextColor(textPrimary)
        tvDate.setTextColor(textSecondary)
        btnSwitchScreen.setColorFilter(primaryAccent)
        btnClearAll.setColorFilter(clearAllAccent)
        tvCarrier.setTextColor(textSecondary)

        // 2. Carrier display
        val resolvedCarrier = if (carrierName.isNotBlank()) carrierName.uppercase() else "EMERGENCY CALLS ONLY"
        tvCarrier.text = resolvedCarrier
        tvBottomCarrier.text = resolvedCarrier
        tvBottomCarrier.setTextColor(textSecondary)

        // Themed bottom divider
        val dividerColor = if (showMonetSurface) cardOutline else (if (isDark) Color.parseColor("#333333") else cardOutline)
        vBottomDivider.setBackgroundColor(dividerColor)

        // 3. Clear All visibility
        if (!isShowingQuickSettings) {
            btnClearAll.visibility = if (notifications.isNotEmpty()) View.VISIBLE else View.INVISIBLE
        }

        // 4. Brightness Slider Sync
        if (sbBrightness.progress != brightness) {
            sbBrightness.progress = brightness.coerceIn(sbBrightness.min, sbBrightness.max)
        }

        // 5. Quick Settings 3-column Grid (Brightness tile removed, position determined by preference)
        flQsContainer.setBackgroundColor(Color.TRANSPARENT)
        rvTiles.setBackgroundColor(Color.TRANSPARENT)
        val filteredTiles = tiles.filter { it.id != "brightness" }
        val isBrightnessAtBottom = PanelPreferences.brightnessAtBottom.value
        tileAdapter.submitList(filteredTiles, tileAccent, tileCardBg, isDark, isBrightnessAtBottom)

        // 6. Notifications List View
        val notifCardBg = if (showMonetSurface) {
            MonetThemeHelper.getCardBackground(context, isDark)
        } else {
            if (isDark) Color.parseColor("#1C1C1C") else MonetThemeHelper.getCardBackground(context, isDark)
        }
        notifListView.updateData(
            notifications = notifications,
            isPermissionGranted = isNotificationPermissionGranted,
            isDark = isDark,
            primaryColor = primaryAccent,
            panelCardBackgroundColor = notifCardBg,
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

    private inner class KitKatTileAdapter(
        private val context: Context,
        private val onTileClick: (QsTileModel) -> Unit,
        private val onTileLongClick: (QsTileModel) -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var items: List<QsTileModel> = emptyList()
        private var tileAccent: Int = Color.parseColor("#33B5E5")
        private var tileCardBg: Int = Color.parseColor("#1E1E1E")
        private var isDark: Boolean = true
        private var isBrightnessAtBottom: Boolean = false

        fun submitList(
            newItems: List<QsTileModel>,
            tileAccent: Int,
            tileCardBg: Int,
            isDark: Boolean,
            isBrightnessAtBottom: Boolean = PanelPreferences.brightnessAtBottom.value
        ) {
            this.items = newItems
            this.tileAccent = tileAccent
            this.tileCardBg = tileCardBg
            this.isDark = isDark
            this.isBrightnessAtBottom = isBrightnessAtBottom
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int): Int {
            return if (!isBrightnessAtBottom) {
                if (position == 0) TYPE_KITKAT_HEADER else TYPE_KITKAT_TILE
            } else {
                if (position == items.size) TYPE_KITKAT_HEADER else TYPE_KITKAT_TILE
            }
        }

        override fun getItemCount(): Int = items.size + 1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == TYPE_KITKAT_HEADER) {
                val composeView = ComposeView(parent.context).apply {
                    layoutParams = RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
                }
                KitKatHeaderViewHolder(composeView)
            } else {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_qs_tile_kitkat, parent, false)
                KitKatTileViewHolder(view)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is KitKatHeaderViewHolder) {
                holder.composeView.setContent {
                    val currentBrightness = brightnessState.intValue
                    val isDarkTheme = isDarkState.value
                    val showMonet = showMonetSurfaceState.value
                    val cardColor = if (showMonet) {
                        androidx.compose.ui.graphics.Color(MonetThemeHelper.getCardBackground(context, isDarkTheme))
                    } else {
                        if (isDarkTheme) androidx.compose.ui.graphics.Color(0xFF1E1E1E)
                        else androidx.compose.ui.graphics.Color(MonetThemeHelper.getCardBackground(context, isDarkTheme))
                    }

                    QuickPanelTheme(darkTheme = isDarkTheme) {
                        KitKatBrightnessSliderBox(
                            brightness = currentBrightness,
                            onBrightnessChange = { newB ->
                                brightnessState.intValue = newB
                                onBrightnessChange?.invoke(newB)
                            },
                            isDarkTheme = isDarkTheme,
                            cardColor = cardColor
                        )
                    }
                }
            } else if (holder is KitKatTileViewHolder) {
                val tileIndex = if (!isBrightnessAtBottom) position - 1 else position
                val tile = items.getOrNull(tileIndex) ?: return

                // Regular System Quick Settings Tile
                val isActive = tile.state == TileState.ACTIVE
                val textPrimary = MonetThemeHelper.getTextPrimary(context, isDark)
                val textSecondary = MonetThemeHelper.getTextSecondary(context, isDark)

                val inactiveIconColor = if (isDark) Color.parseColor("#999999") else textSecondary
                val inactiveTextColor = if (isDark) Color.parseColor("#AAAAAA") else textSecondary
                val iconTint = if (isActive) androidx.compose.ui.graphics.Color(tileAccent) else androidx.compose.ui.graphics.Color(inactiveIconColor)

                // 1. Jetpack Compose Tile Icon
                holder.composeIcon.setContent {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        KitKatTileIcon(
                            tile = tile,
                            tint = iconTint,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // 2. Tile Label & Subtitle
                holder.tvLabel.text = tile.label.uppercase()
                holder.tvLabel.setTextColor(if (isActive) (if (isDark) Color.WHITE else textPrimary) else inactiveTextColor)

                if (tile.subtitle.isNotBlank() && tile.subtitle != tile.label) {
                    holder.tvSubtitle.text = tile.subtitle.uppercase()
                    holder.tvSubtitle.setTextColor(if (isActive) tileAccent else textSecondary)
                    holder.tvSubtitle.visibility = View.VISIBLE
                } else {
                    holder.tvSubtitle.visibility = View.GONE
                }

                holder.cardRoot.setBackgroundColor(tileCardBg)
                holder.cardRoot.setOnClickListener { onTileClick(tile) }
                holder.cardRoot.setOnLongClickListener {
                    onTileLongClick(tile)
                    true
                }
            }
        }
    }

    private class KitKatHeaderViewHolder(val composeView: ComposeView) : RecyclerView.ViewHolder(composeView)

    private class KitKatTileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardRoot: View = itemView.findViewById(R.id.fl_kitkat_tile_card)
        val composeIcon: ComposeView = itemView.findViewById(R.id.compose_tile_icon)
        val tvLabel: TextView = itemView.findViewById(R.id.tv_tile_label)
        val tvSubtitle: TextView = itemView.findViewById(R.id.tv_tile_subtitle)

        init {
            composeIcon.setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
            )
        }
    }
}

@Composable
fun KitKatTileIcon(
    tile: QsTileModel,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier.size(28.dp)
) {
    val isThirdParty = tile.isExternal || tile.category == TileCategory.THIRD_PARTY || tile.category == TileCategory.EXTERNAL || tile.id.contains("/")
    if (isThirdParty) {
        val pkg = tile.packageName ?: if (tile.id.contains("/")) tile.id.substringBefore('/') else null
        val cls = tile.serviceClassName ?: if (tile.id.contains("/")) tile.id.substringAfter('/') else null
        ExternalTileIcon(
            packageName = pkg,
            serviceClassName = cls,
            tintColor = tint,
            contentDescription = tile.label,
            fallbackDrawable = tile.externalDrawable,
            modifier = modifier
        )
    } else {
        val vector = tile.iconVector ?: com.example.ui.tile.QsTileIconHelper.getTileImageVector(tile)
        Icon(
            imageVector = vector,
            contentDescription = tile.label,
            tint = tint,
            modifier = modifier
        )
    }
}

@Composable
fun KitKatBrightnessSliderBox(
    brightness: Int,
    onBrightnessChange: (Int) -> Unit,
    isDarkTheme: Boolean,
    cardColor: androidx.compose.ui.graphics.Color
) {
    val sliderSyncAll by PanelPreferences.sliderSyncAll.collectAsState()
    val globalProgressStyle by PanelPreferences.progressBarStyle.collectAsState()
    val brightnessStyle by PanelPreferences.brightnessSliderStyle.collectAsState()
    val resolvedStyle = if (sliderSyncAll) globalProgressStyle else brightnessStyle

    // Box is 4dp taller for M3 (DEFAULT) and Android 12 styles (56.dp vs 48.dp)
    val isM3OrAndroid12 = resolvedStyle == ProgressBarStyle.DEFAULT ||
            resolvedStyle == ProgressBarStyle.ANDROID_12
    val boxHeight = if (isM3OrAndroid12) 56.dp else 48.dp

    val hideBrightnessPercentage by PanelPreferences.hideBrightnessPercentage.collectAsState()
    val hideBrightnessIcon by PanelPreferences.hideBrightnessIcon.collectAsState()
    val showBrightnessPlusMinus by PanelPreferences.showBrightnessPlusMinus.collectAsState()
    val showBrightnessGradient by PanelPreferences.showBrightnessGradient.collectAsState()
    val minBrightness by PanelPreferences.minBrightness.collectAsState()
    val maxBrightness by PanelPreferences.maxBrightness.collectAsState()

    val sliderAccentColor = getSlidersElementColor(isDarkTheme)
    val tertiaryThemeColor = androidx.compose.material3.MaterialTheme.colorScheme.tertiary
    val brightnessGradientBrush = remember(sliderAccentColor, tertiaryThemeColor) {
        Brush.horizontalGradient(listOf(sliderAccentColor, tertiaryThemeColor))
    }
    val inactiveTrackColor = getInactiveTilesElementColor(isDarkTheme)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 1.5.dp, vertical = 1.5.dp)
            .height(boxHeight)
            .background(cardColor)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!hideBrightnessIcon) {
                Icon(
                    imageVector = if (brightness > (minBrightness + maxBrightness) / 2)
                        Icons.Default.BrightnessMedium
                    else
                        Icons.Default.BrightnessLow,
                    contentDescription = "Brightness",
                    tint = sliderAccentColor,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(22.dp)
                )
            }

            if (showBrightnessPlusMinus) {
                IconButton(
                    onClick = { onBrightnessChange((brightness - 15).coerceAtLeast(minBrightness)) },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("brightness_minus_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease brightness",
                        tint = sliderAccentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            QsProgressSlider(
                value = brightness.toFloat().coerceIn(minBrightness.toFloat(), maxBrightness.toFloat()),
                onValueChange = { onBrightnessChange(it.toInt()) },
                valueRange = minBrightness.toFloat()..maxBrightness.toFloat(),
                sliderTarget = QsSliderTarget.BRIGHTNESS,
                activeColor = sliderAccentColor,
                activeBrush = if (showBrightnessGradient) brightnessGradientBrush else null,
                inactiveColor = inactiveTrackColor,
                thumbColor = sliderAccentColor,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
                    .testTag("brightness_slider")
            )

            if (showBrightnessPlusMinus) {
                IconButton(
                    onClick = { onBrightnessChange((brightness + 15).coerceAtMost(maxBrightness)) },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("brightness_plus_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase brightness",
                        tint = sliderAccentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (!hideBrightnessPercentage) {
                val percentageColor = if (isDarkTheme) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color(0xFF333333)
                val clampedB = brightness.coerceIn(minBrightness, maxBrightness)
                val range = (maxBrightness - minBrightness).coerceAtLeast(1)
                val pct = (((clampedB - minBrightness).toFloat() / range) * 100).toInt().coerceIn(0, 100)

                Text(
                    text = "$pct%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = percentageColor
                )
            }
        }
    }
}

