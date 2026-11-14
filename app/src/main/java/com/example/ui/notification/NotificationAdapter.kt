package com.example.ui.notification

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Path
import android.os.Build
import android.provider.Settings
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.model.NotificationActionModel
import com.example.model.NotificationItem
import com.example.service.QuickPanelNotificationListenerService
import java.util.Date

import android.graphics.drawable.GradientDrawable
import android.widget.ImageButton
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import com.example.data.NotificationCategoryManager
import com.example.data.PanelPreferences
import com.example.data.MediaNotificationStyle
import com.example.model.MediaState
import com.example.service.MediaSessionManagerHelper
import com.example.ui.overlay.extractAndroidQNotificationColors
import com.example.ui.overlay.trimTopBottomBlackLines
import com.example.ui.view.WavyProgressView

sealed class ShadeListRow {
    data class CategoryHeaderRow(
        val categoryId: String,
        val title: String,
        val iconRes: Int,
        val count: Int,
        val isExpanded: Boolean,
        val isUncategorized: Boolean,
        val childNotifications: List<NotificationItem>
    ) : ShadeListRow()

    data class NotificationRow(
        val item: NotificationItem,
        val isGrouped: Boolean,
        val isFirstInGroup: Boolean,
        val isLastInGroup: Boolean
    ) : ShadeListRow()
}

class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationItem>() {
    override fun areItemsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean {
        return oldItem.key == newItem.key
    }

    override fun areContentsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean {
        return oldItem == newItem
    }
}

class NotificationAdapter(
    private val onNotificationClick: (NotificationItem) -> Unit,
    private val onNotificationDismiss: (NotificationItem) -> Unit,
    private val onActionClick: (NotificationActionModel) -> Unit,
    var onClearAll: (() -> Unit)? = null,
    var onCategoryToggled: (() -> Unit)? = null
) : ListAdapter<NotificationItem, RecyclerView.ViewHolder>(NotificationDiffCallback()) {

    var isDark: Boolean = true
    var customPrimaryColor: Int? = null
    var customCardBackgroundColor: Int? = null
    var showFooter: Boolean = false
    var disableMonetNotifText: Boolean = false
    var onRequestClearAnimation: (() -> Unit)? = null
    var isMediaCardShown: Boolean = false
    var mediaSessions: List<MediaState> = emptyList()

    private var rawNotifications: List<NotificationItem> = emptyList()
    private var displayRows: List<ShadeListRow> = emptyList()
    private val expandedStateMap = mutableMapOf<String, Boolean>()

    fun submitNotifications(list: List<NotificationItem>) {
        rawNotifications = list
        rebuildDisplayRows()
        submitList(list)
    }

    fun rebuildDisplayRows() {
        val sections = NotificationCategoryManager.buildGroupedSections(rawNotifications)
        displayRows = if (sections.isNotEmpty()) {
            buildList {
                sections.forEach { section ->
                    add(
                        ShadeListRow.CategoryHeaderRow(
                            categoryId = section.categoryId,
                            title = section.title,
                            iconRes = section.icon.drawableRes,
                            count = section.notifications.size,
                            isExpanded = section.isExpanded,
                            isUncategorized = section.isUncategorized,
                            childNotifications = section.notifications
                        )
                    )
                    if (section.isExpanded) {
                        val children = section.notifications
                        children.forEachIndexed { idx, notif ->
                            add(
                                ShadeListRow.NotificationRow(
                                    item = notif,
                                    isGrouped = true,
                                    isFirstInGroup = (idx == 0),
                                    isLastInGroup = (idx == children.lastIndex)
                                )
                            )
                        }
                    }
                }
            }
        } else {
            rawNotifications.mapIndexed { idx, notif ->
                ShadeListRow.NotificationRow(
                    item = notif,
                    isGrouped = false,
                    isFirstInGroup = (idx == 0),
                    isLastInGroup = (idx == rawNotifications.lastIndex)
                )
            }
        }
    }

    fun isItemExpanded(item: NotificationItem): Boolean {
        return expandedStateMap[item.key] ?: false
    }

    /**
     * Safely retrieves the [NotificationItem] at the given adapter position,
     * strictly accounting for media player cards and category headers.
     */
    fun getNotificationItem(position: Int): NotificationItem? {
        if (position == RecyclerView.NO_POSITION) return null
        val rowIndex = position - mediaSessions.size
        return (displayRows.getOrNull(rowIndex) as? ShadeListRow.NotificationRow)?.item
    }

    /**
     * Safely retrieves the [NotificationItem] by its unique notification key ID.
     */
    fun getNotificationItemByKey(key: String): NotificationItem? {
        return rawNotifications.firstOrNull { it.key == key }
    }

    companion object {
        private const val TYPE_MEDIA_PLAYER = 0
        private const val TYPE_NOTIFICATION = 1
        private const val TYPE_FOOTER = 2
        private const val TYPE_MEDIA_PLAYER_COMPACT = 3
        private const val TYPE_CATEGORY_HEADER = 4
    }

    private var attachedRecyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        attachedRecyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        attachedRecyclerView = null
    }

    override fun onCurrentListChanged(
        previousList: MutableList<NotificationItem>,
        currentList: MutableList<NotificationItem>
    ) {
        super.onCurrentListChanged(previousList, currentList)
        attachedRecyclerView?.post {
            rebuildDisplayRows()
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        val mediaCount = mediaSessions.size
        val rowCount = displayRows.size
        val isKitKatStyle = attachedRecyclerView?.context?.let { PanelPreferences.isAndroidKitKatStyle(it) } ?: false
        val footerCount = if (showFooter && rawNotifications.isNotEmpty() && !isKitKatStyle) 1 else 0
        return mediaCount + rowCount + footerCount
    }

    override fun getItemViewType(position: Int): Int {
        val mediaCount = mediaSessions.size
        val rowCount = displayRows.size
        return when {
            position < mediaCount -> {
                if (PanelPreferences.mediaNotificationStyle.value == MediaNotificationStyle.COMPACT) {
                    TYPE_MEDIA_PLAYER_COMPACT
                } else {
                    TYPE_MEDIA_PLAYER
                }
            }
            position < mediaCount + rowCount -> {
                when (displayRows[position - mediaCount]) {
                    is ShadeListRow.CategoryHeaderRow -> TYPE_CATEGORY_HEADER
                    is ShadeListRow.NotificationRow -> TYPE_NOTIFICATION
                }
            }
            else -> TYPE_FOOTER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_MEDIA_PLAYER_COMPACT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media_player_compact, parent, false)
                CompactMediaPlayerViewHolder(view)
            }
            TYPE_MEDIA_PLAYER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media_player_android_q, parent, false)
                MediaPlayerViewHolder(view)
            }
            TYPE_CATEGORY_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification_category_header, parent, false)
                CategoryHeaderViewHolder(view)
            }
            TYPE_FOOTER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification_footer, parent, false)
                FooterViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification_card, parent, false)
                NotificationViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val totalCount = mediaSessions.size + displayRows.size
        if (holder is MediaPlayerViewHolder) {
            val mediaState = mediaSessions.getOrNull(position) ?: return
            holder.bind(mediaState, position, totalCount)
        } else if (holder is CompactMediaPlayerViewHolder) {
            val mediaState = mediaSessions.getOrNull(position) ?: return
            holder.bind(mediaState, position, totalCount)
        } else if (holder is CategoryHeaderViewHolder) {
            val row = displayRows.getOrNull(position - mediaSessions.size) as? ShadeListRow.CategoryHeaderRow ?: return
            holder.bind(row)
        } else if (holder is NotificationViewHolder) {
            val row = displayRows.getOrNull(position - mediaSessions.size) as? ShadeListRow.NotificationRow ?: return
            holder.bind(row, position, totalCount)
        } else if (holder is FooterViewHolder) {
            holder.bind()
        }
    }

    inner class CategoryHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val flRoot: FrameLayout = itemView.findViewById(R.id.fl_cat_header_root)
        private val flIconBg: FrameLayout = itemView.findViewById(R.id.fl_cat_icon_bg)
        private val ivIcon: ImageView = itemView.findViewById(R.id.iv_cat_icon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_cat_title)
        private val tvCount: TextView = itemView.findViewById(R.id.tv_cat_count)
        private val ivExpand: ImageView = itemView.findViewById(R.id.iv_cat_expand)

        fun bind(row: ShadeListRow.CategoryHeaderRow) {
            val context = itemView.context
            val density = context.resources.displayMetrics.density
            val isOreoStyle = PanelPreferences.isAndroidOreoStyle(context)
            val isKitKatStyle = PanelPreferences.isAndroidKitKatStyle(context)
            val disableMonet = disableMonetNotifText

            val primaryColor = customPrimaryColor ?: MonetThemeHelper.getPrimaryAccent(context, isDark)
            val textPrimary = if (disableMonet) {
                if (isDark) Color.WHITE else Color.BLACK
            } else {
                MonetThemeHelper.getTextPrimary(context, isDark)
            }
            val textSecondary = if (disableMonet) {
                if (isDark) Color.parseColor("#CCCCCC") else Color.parseColor("#555555")
            } else {
                MonetThemeHelper.getTextSecondary(context, isDark)
            }

            val panelGlobalCornerEnabled = PanelPreferences.panelGlobalCornerEnabled.value
            val panelOuterCorner = PanelPreferences.panelOuterCorner.value
            val panelInnerCorner = PanelPreferences.panelInnerCorner.value
            val outerCornerPx = panelOuterCorner * density
            val innerCornerPx = (if (panelGlobalCornerEnabled) panelOuterCorner else panelInnerCorner) * density

            val radii = when {
                isOreoStyle -> FloatArray(8) { 0f }
                row.isExpanded -> floatArrayOf(
                    outerCornerPx, outerCornerPx,
                    outerCornerPx, outerCornerPx,
                    innerCornerPx, innerCornerPx,
                    innerCornerPx, innerCornerPx
                )
                else -> FloatArray(8) { outerCornerPx }
            }

            val cardBg = if (isOreoStyle) {
                if (isDark) Color.parseColor("#282828") else Color.parseColor("#F5F5F5")
            } else {
                customCardBackgroundColor ?: MonetThemeHelper.getCardBackground(context, isDark)
            }

            val contentDrawable = if (isOreoStyle) {
                val strokeColor = if (isDark) Color.parseColor("#33FFFFFF") else Color.parseColor("#33000000")
                val strokePx = (1 * density).toInt().coerceAtLeast(1)
                val baseDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(cardBg)
                }
                val strokeDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setStroke(strokePx, strokeColor)
                    setColor(Color.TRANSPARENT)
                }
                android.graphics.drawable.LayerDrawable(arrayOf(baseDrawable, strokeDrawable)).apply {
                    setLayerInset(1, -strokePx, 0, -strokePx, 0)
                }
            } else {
                MonetThemeHelper.createMonetCardDrawable(context, isDark, radii, customBgColor = cardBg)
            }

            val enablePanelShadows = PanelPreferences.enablePanelShadows.value
            val shadowElevation = PanelPreferences.panelShadowElevation.value
            val panelShadowAlpha = PanelPreferences.panelShadowAlpha.value
            val panelShadowOffsetY = PanelPreferences.panelShadowOffsetY.value

            CustomShadowHelper.applyCustomShadowBackground(
                view = flRoot,
                contentDrawable = contentDrawable,
                radii = radii,
                elevation = if (enablePanelShadows && !isOreoStyle && !isKitKatStyle) shadowElevation else 0f,
                isDark = isDark,
                customAlpha = panelShadowAlpha,
                customOffsetY = panelShadowOffsetY
            )
            flRoot.elevation = 0f
            flRoot.translationY = 0f

            val lp = flRoot.layoutParams as? ViewGroup.MarginLayoutParams
            if (lp != null) {
                val targetBottomMargin = if (isOreoStyle) 0 else (4f * density).toInt()
                if (lp.bottomMargin != targetBottomMargin) {
                    lp.bottomMargin = targetBottomMargin
                    flRoot.layoutParams = lp
                }
            }

            // Icon circle badge
            val badgeBgColor = if (row.isUncategorized) {
                ColorUtils.setAlphaComponent(textSecondary, 36)
            } else {
                ColorUtils.setAlphaComponent(primaryColor, 42)
            }
            flIconBg.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(badgeBgColor)
            }
            ivIcon.setImageResource(row.iconRes)
            ivIcon.setColorFilter(if (row.isUncategorized) textSecondary else primaryColor)

            tvTitle.text = row.title
            tvTitle.setTextColor(textPrimary)

            tvCount.text = row.count.toString()
            tvCount.setTextColor(if (row.isUncategorized) textSecondary else primaryColor)
            tvCount.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 999f * density
                setColor(
                    if (row.isUncategorized) ColorUtils.setAlphaComponent(textSecondary, 30)
                    else ColorUtils.setAlphaComponent(primaryColor, 36)
                )
            }

            ivExpand.setImageResource(
                if (row.isExpanded) R.drawable.ic_notif_chevron_up else R.drawable.ic_notif_chevron_down
            )
            ivExpand.setColorFilter(textSecondary)
            ivExpand.contentDescription = if (row.isExpanded) "Collapse ${row.title}" else "Expand ${row.title}"

            flRoot.setOnClickListener {
                NotificationCategoryManager.toggleCategoryExpanded(
                    context = context,
                    categoryId = row.categoryId,
                    defaultExpanded = true
                )
                rebuildDisplayRows()
                notifyDataSetChanged()
                onCategoryToggled?.invoke()
            }
        }
    }

    inner class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val llFooterRoot: LinearLayout = itemView.findViewById(R.id.ll_notif_footer_root)
        private val btnClearAll: LinearLayout = itemView.findViewById(R.id.btn_notif_clear_all)
        private val tvClearAllText: TextView = itemView.findViewById(R.id.tv_notif_clear_all_text)
        private val ivClearAllIcon: ImageView = itemView.findViewById(R.id.iv_notif_clear_all_icon)

        fun bind() {
            val context = itemView.context
            val density = context.resources.displayMetrics.density
            val isDarkTheme = isDark

            val disableBg = com.example.data.PanelPreferences.disableClearAllBackground.value
            val useFab = com.example.data.PanelPreferences.useClearAllFab.value
            val fabPos = com.example.data.PanelPreferences.clearAllFabPosition.value
            val fabShapeId = com.example.data.PanelPreferences.clearAllFabShapeId.value
            val fabIcon = com.example.data.PanelPreferences.clearAllFabIcon.value
            val disableClearAllLabel = com.example.data.PanelPreferences.disableClearAllLabel.value
            val clearAllIconSize = com.example.data.PanelPreferences.clearAllIconSize.value
            val showMonetSurface = com.example.data.PanelPreferences.showMonetSurfaceBackground.value

            llFooterRoot.clipChildren = false
            llFooterRoot.clipToPadding = false
            btnClearAll.clipChildren = false
            btnClearAll.clipToPadding = false

            if (!useFab) {
                // When FAB button is disabled: "Clear All" button remains on right side anyway
                llFooterRoot.gravity = android.view.Gravity.END or android.view.Gravity.CENTER_VERTICAL

                val params = btnClearAll.layoutParams as ViewGroup.MarginLayoutParams
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                btnClearAll.layoutParams = params

                if (disableClearAllLabel) {
                    tvClearAllText.visibility = View.GONE
                    llFooterRoot.setPadding(0, (6 * density).toInt(), (12 * density).toInt(), (12 * density).toInt())
                    btnClearAll.setPadding((8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt())
                } else {
                    tvClearAllText.visibility = View.VISIBLE
                    llFooterRoot.setPadding(0, (10 * density).toInt(), (12 * density).toInt(), (20 * density).toInt())
                    btnClearAll.setPadding((16 * density).toInt(), (8 * density).toInt(), (16 * density).toInt(), (8 * density).toInt())
                }
                btnClearAll.gravity = android.view.Gravity.CENTER_VERTICAL

                val iconParams = ivClearAllIcon.layoutParams as ViewGroup.MarginLayoutParams
                val iconSizePx = if (disableClearAllLabel) (clearAllIconSize * density).toInt() else (16 * density).toInt()
                iconParams.width = iconSizePx
                iconParams.height = iconSizePx
                iconParams.marginStart = 0
                ivClearAllIcon.layoutParams = iconParams
                ivClearAllIcon.setImageResource(fabIcon.drawableRes)

                val clearAllAccent = MonetThemeHelper.getClearAllAccent(context, isDarkTheme)
                val clearAllTextColor = if (disableClearAllLabel) {
                    if (showMonetSurface) {
                        clearAllAccent
                    } else {
                        android.graphics.Color.WHITE
                    }
                } else if (disableBg) {
                    clearAllAccent
                } else {
                    if (isDarkTheme) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                }
                tvClearAllText.setTextColor(clearAllTextColor)
                ivClearAllIcon.setColorFilter(clearAllTextColor)

                val enablePanelShadows = com.example.data.PanelPreferences.enablePanelShadows.value
                val shadowElevation = com.example.data.PanelPreferences.panelShadowElevation.value
                val panelShadowAlpha = com.example.data.PanelPreferences.panelShadowAlpha.value
                val panelShadowOffsetY = com.example.data.PanelPreferences.panelShadowOffsetY.value
                val effectiveButtonElevation = if (enablePanelShadows) (if (shadowElevation > 0f) shadowElevation else 6f) else 6f

                if (disableClearAllLabel || disableBg) {
                    btnClearAll.background = null
                    btnClearAll.elevation = 0f
                    btnClearAll.translationY = 0f
                } else {
                    btnClearAll.elevation = 0f
                    btnClearAll.translationY = 0f
                    val pillDrawable = MonetThemeHelper.createMonetPrimaryPillDrawable(context, isDarkTheme, 999f * density, customBgColor = clearAllAccent)
                    CustomShadowHelper.applyCustomShadowBackground(
                        view = btnClearAll,
                        contentDrawable = pillDrawable,
                        isPill = true,
                        elevation = effectiveButtonElevation,
                        isDark = isDarkTheme,
                        customAlpha = panelShadowAlpha,
                        customOffsetY = panelShadowOffsetY
                    )
                }
            } else {
                // When FAB button is enabled: positioned according to fabPos and bigger (64dp)
                llFooterRoot.gravity = when (fabPos) {
                    com.example.data.ClearAllFabPosition.LEFT -> android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL
                    com.example.data.ClearAllFabPosition.RIGHT -> android.view.Gravity.END or android.view.Gravity.CENTER_VERTICAL
                    com.example.data.ClearAllFabPosition.CENTER -> android.view.Gravity.CENTER
                }

                tvClearAllText.visibility = View.GONE
                val fabSize = com.example.data.PanelPreferences.clearAllFabSize.value
                val params = btnClearAll.layoutParams as ViewGroup.MarginLayoutParams
                params.width = (fabSize * density).toInt()
                params.height = (fabSize * density).toInt()
                btnClearAll.layoutParams = params
                btnClearAll.setPadding(0, 0, 0, 0)
                btnClearAll.gravity = android.view.Gravity.CENTER

                val iconParams = ivClearAllIcon.layoutParams as ViewGroup.MarginLayoutParams
                val iconSize = (fabSize * (30f / 64f) * density).toInt()
                iconParams.width = iconSize
                iconParams.height = iconSize
                iconParams.marginStart = 0
                ivClearAllIcon.layoutParams = iconParams
                ivClearAllIcon.setImageResource(fabIcon.drawableRes)

                val clearAllAccent = MonetThemeHelper.getClearAllAccent(context, isDarkTheme)
                val tintColor = if (disableClearAllLabel) {
                    if (showMonetSurface) clearAllAccent else android.graphics.Color.WHITE
                } else if (disableBg) {
                    MonetThemeHelper.getPrimaryAccent(context, isDarkTheme)
                } else {
                    if (isDarkTheme) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                }
                ivClearAllIcon.setColorFilter(tintColor)

                val enablePanelShadows = com.example.data.PanelPreferences.enablePanelShadows.value
                val shadowElevation = com.example.data.PanelPreferences.panelShadowElevation.value
                val panelShadowAlpha = com.example.data.PanelPreferences.panelShadowAlpha.value
                val panelShadowOffsetY = com.example.data.PanelPreferences.panelShadowOffsetY.value
                if (disableBg) {
                    btnClearAll.background = null
                    btnClearAll.elevation = 0f
                    btnClearAll.translationY = 0f
                } else {
                    btnClearAll.elevation = 0f
                    btnClearAll.translationY = 0f
                    val primaryColor = MonetThemeHelper.getPrimaryAccent(context, isDarkTheme)
                    val shapeDrawable = com.example.ui.shape.QsShapeManager.createDrawable(fabShapeId, primaryColor)
                    val maskDrawable = com.example.ui.shape.QsShapeManager.createDrawable(fabShapeId, android.graphics.Color.WHITE)
                    val rippleColor = if (isDarkTheme) android.graphics.Color.parseColor("#44FFFFFF") else android.graphics.Color.parseColor("#33000000")
                    val rippleDrawable = android.graphics.drawable.RippleDrawable(
                        android.content.res.ColorStateList.valueOf(rippleColor),
                        shapeDrawable,
                        maskDrawable
                    )
                    val shapeItem = com.example.ui.shape.QsShapeManager.getShapeItem(fabShapeId)
                    val effectiveFabElevation = if (enablePanelShadows) (if (shadowElevation > 0f) shadowElevation else 6f) else 6f
                    CustomShadowHelper.applyCustomShadowBackground(
                        btnClearAll,
                        rippleDrawable,
                        pathData = shapeItem.pathData,
                        viewportWidth = shapeItem.viewportWidth,
                        viewportHeight = shapeItem.viewportHeight,
                        elevation = effectiveFabElevation,
                        isDark = isDarkTheme,
                        customAlpha = panelShadowAlpha,
                        customOffsetY = panelShadowOffsetY
                    )
                }
            }

            btnClearAll.setOnClickListener {
                val shouldRotate = useFab && !disableClearAllLabel
                val anim = btnClearAll.animate()
                    .scaleX(0.85f)
                    .scaleY(0.85f)
                if (shouldRotate) {
                    anim.rotationBy(180f)
                }
                anim.setDuration(180)
                    .withEndAction {
                        btnClearAll.scaleX = 1f
                        btnClearAll.scaleY = 1f
                        btnClearAll.rotation = 0f
                    }
                    .start()
                if (onRequestClearAnimation != null) {
                    onRequestClearAnimation?.invoke()
                } else {
                    onClearAll?.invoke()
                }
            }
        }
    }

    inner class MediaPlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val flRoot: FrameLayout = itemView.findViewById(R.id.media_card_root)
        private val ivArtwork: ImageView = itemView.findViewById(R.id.iv_media_artwork)
        private val vArtworkGradient: View = itemView.findViewById(R.id.v_media_artwork_gradient)
        private val flArtworkFallback: View = itemView.findViewById(R.id.fl_media_artwork_fallback)
        private val ivArtworkFallback: ImageView = itemView.findViewById(R.id.iv_media_artwork_fallback)
        private val ivAppIcon: ImageView = itemView.findViewById(R.id.iv_media_app_icon)
        private val tvAppName: TextView = itemView.findViewById(R.id.tv_media_app_name)
        private val btnToggleSeekBar: ImageButton = itemView.findViewById(R.id.btn_media_toggle_seekbar)
        private val btnClose: ImageButton = itemView.findViewById(R.id.btn_media_close)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_media_title)
        private val tvArtist: TextView = itemView.findViewById(R.id.tv_media_artist)
        private val btnShuffle: ImageButton = itemView.findViewById(R.id.btn_media_shuffle)
        private val btnPrevious: ImageButton = itemView.findViewById(R.id.btn_media_previous)
        private val btnPlayPause: ImageButton = itemView.findViewById(R.id.btn_media_play_pause)
        private val btnNext: ImageButton = itemView.findViewById(R.id.btn_media_next)
        private val btnRepeat: ImageButton = itemView.findViewById(R.id.btn_media_repeat)
        private val btnFavorite: ImageButton = itemView.findViewById(R.id.btn_media_favorite)
        private val llSeekbarContainer: LinearLayout = itemView.findViewById(R.id.ll_media_seekbar_container)
        private val wavySeekBar: WavyProgressView = itemView.findViewById(R.id.v_media_wavy_seekbar)
        private val tvCurrTime: TextView = itemView.findViewById(R.id.tv_media_curr_time)
        private val tvTotalTime: TextView = itemView.findViewById(R.id.tv_media_total_time)

        private var isSeekBarVisible = true

        fun bind(mediaState: MediaState, position: Int, totalCount: Int) {
            val context = itemView.context
            val density = context.resources.displayMetrics.density

            val artwork = mediaState.artworkBitmap
            val palette = artwork?.let {
                Palette.from(it)
                    .maximumColorCount(32)
                    .clearFilters()
                    .generate()
            }
            val useMediaArtworkColors = PanelPreferences.useMediaArtworkColors.value
            val isPanelCustomized = PanelPreferences.qsPanelBackgroundColor.value != null ||
                PanelPreferences.getPanelContainerColorTertiary(isDark).value ||
                PanelPreferences.getPanelContainerHue(isDark).value != 0f ||
                PanelPreferences.getPanelContainerSat(isDark).value != 1f ||
                PanelPreferences.getPanelContainerVal(isDark).value != 1f ||
                PanelPreferences.showMonetSurfaceBackground.value

            val fallbackBg = customCardBackgroundColor ?: if (isDark) Color.parseColor("#25262B") else Color.parseColor("#EAEBEF")
            val fallbackAccent = customPrimaryColor ?: MonetThemeHelper.getPrimaryAccent(context, isDark)

            val cardBgColorInt: Int
            val accentColorInt: Int
            if (useMediaArtworkColors && artwork != null && !isPanelCustomized) {
                val extractedColors = extractAndroidQNotificationColors(artwork, palette)
                cardBgColorInt = extractedColors.backgroundColor.toArgb()
                accentColorInt = extractedColors.accentColor.toArgb()
            } else {
                cardBgColorInt = fallbackBg
                accentColorInt = fallbackAccent
            }
            val subTextColorInt = ColorUtils.setAlphaComponent(accentColorInt, (255 * 0.80f).toInt())
            val dimAccentColorInt = ColorUtils.setAlphaComponent(accentColorInt, (255 * 0.45f).toInt())
            val inactiveAccentColorInt = ColorUtils.setAlphaComponent(accentColorInt, (255 * 0.25f).toInt())

            // 1. Dynamic Shape & Corner Radii following panel preferences
            val panelGlobalCornerEnabled = PanelPreferences.panelGlobalCornerEnabled.value
            val panelOuterCorner = PanelPreferences.panelOuterCorner.value
            val panelInnerCorner = PanelPreferences.panelInnerCorner.value

            val isOreoStyle = PanelPreferences.isAndroidOreoStyle(context)
            val isMarshmallowStyle = PanelPreferences.isAndroidMarshmallowStyle(context)
            val isKitKatStyle = PanelPreferences.isAndroidKitKatStyle(context)
            val outerCornerPx = panelOuterCorner * density
            val innerCornerPx = (if (panelGlobalCornerEnabled) panelOuterCorner else panelInnerCorner) * density

            val radii = if (isOreoStyle) {
                FloatArray(8) { 0f }
            } else {
                when {
                    totalCount <= 1 -> FloatArray(8) { outerCornerPx }
                    position == 0 -> floatArrayOf(
                        outerCornerPx, outerCornerPx,
                        outerCornerPx, outerCornerPx,
                        innerCornerPx, innerCornerPx,
                        innerCornerPx, innerCornerPx
                    )
                    position == totalCount - 1 -> floatArrayOf(
                        innerCornerPx, innerCornerPx,
                        innerCornerPx, innerCornerPx,
                        outerCornerPx, outerCornerPx,
                        outerCornerPx, outerCornerPx
                    )
                    else -> FloatArray(8) { innerCornerPx }
                }
            }

            val contentDrawable = if (isOreoStyle) {
                val strokeColor = if (isDark) Color.parseColor("#33FFFFFF") else Color.parseColor("#33000000")
                val strokePx = (1 * density).toInt().coerceAtLeast(1)
                val baseDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(cardBgColorInt)
                }
                val strokeDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setStroke(strokePx, strokeColor)
                    setColor(Color.TRANSPARENT)
                }
                android.graphics.drawable.LayerDrawable(arrayOf(baseDrawable, strokeDrawable)).apply {
                    setLayerInset(1, -strokePx, 0, -strokePx, 0)
                }
            } else {
                GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadii = radii
                    setColor(cardBgColorInt)
                }
            }

            val enablePanelShadows = PanelPreferences.enablePanelShadows.value
            val shadowElevation = PanelPreferences.panelShadowElevation.value
            val panelShadowAlpha = PanelPreferences.panelShadowAlpha.value
            val panelShadowOffsetY = PanelPreferences.panelShadowOffsetY.value

            CustomShadowHelper.applyCustomShadowBackground(
                view = flRoot,
                contentDrawable = contentDrawable,
                radii = radii,
                elevation = if (enablePanelShadows && !isOreoStyle) shadowElevation else 0f,
                isDark = isDark,
                customAlpha = panelShadowAlpha,
                customOffsetY = panelShadowOffsetY
            )
            flRoot.elevation = 0f
            flRoot.translationY = 0f
            flRoot.clipChildren = false
            flRoot.clipToPadding = false
            flRoot.clipToOutline = false
            flRoot.outlineProvider = null

            // Distance between notification & media items: 4dp (0dp in Oreo style)
            val lp = flRoot.layoutParams as? ViewGroup.MarginLayoutParams
            if (lp != null) {
                val targetBottomMargin = if (isOreoStyle) 0 else (4f * density).toInt()
                val targetTopMargin = if (isOreoStyle) 0 else lp.topMargin
                if (lp.bottomMargin != targetBottomMargin || lp.topMargin != targetTopMargin) {
                    lp.bottomMargin = targetBottomMargin
                    lp.topMargin = targetTopMargin
                    flRoot.layoutParams = lp
                }
            }

            // 2. Right-side Artwork & Gradient
            if (artwork != null) {
                val displayBitmap = trimTopBottomBlackLines(artwork)
                ivArtwork.visibility = View.VISIBLE
                ivArtwork.setImageBitmap(displayBitmap)
                flArtworkFallback.visibility = View.GONE

                val gradient = GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    intArrayOf(
                        cardBgColorInt,
                        ColorUtils.setAlphaComponent(cardBgColorInt, (255 * 0.92f).toInt()),
                        ColorUtils.setAlphaComponent(cardBgColorInt, (255 * 0.50f).toInt()),
                        Color.TRANSPARENT
                    )
                )
                vArtworkGradient.background = gradient
                vArtworkGradient.visibility = View.VISIBLE

                val artworkOutlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: Outline) {
                        val path = Path().apply {
                            addRoundRect(
                                0f, 0f, view.width.toFloat(), view.height.toFloat(),
                                radii,
                                Path.Direction.CW
                            )
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            outline.setPath(path)
                        } else {
                            @Suppress("DEPRECATION")
                            outline.setConvexPath(path)
                        }
                    }
                }
                ivArtwork.outlineProvider = artworkOutlineProvider
                ivArtwork.clipToOutline = true
                vArtworkGradient.outlineProvider = artworkOutlineProvider
                vArtworkGradient.clipToOutline = true
            } else {
                ivArtwork.visibility = View.GONE
                vArtworkGradient.visibility = View.GONE
                flArtworkFallback.visibility = View.VISIBLE

                val neutralBadgeColor = if (isDark) Color.parseColor("#34363C") else Color.parseColor("#DCDFE6")
                val badgeDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(neutralBadgeColor)
                }
                ivArtworkFallback.background = badgeDrawable
                val pad = (10 * density).toInt()
                ivArtworkFallback.setPadding(pad, pad, pad, pad)
                ivArtworkFallback.setColorFilter(accentColorInt)
            }

            // 3. App Title & Icon
            val displayAppName = if (mediaState.appName.isNotBlank() && mediaState.appName != "MUSER") {
                mediaState.appName
            } else if (mediaState.packageName.isNotBlank()) {
                try {
                    val pm = context.packageManager
                    val info = pm.getApplicationInfo(mediaState.packageName, 0)
                    pm.getApplicationLabel(info).toString()
                } catch (e: Exception) {
                    "Music Player"
                }
            } else {
                "Music Player"
            }
            tvAppName.text = displayAppName
            tvAppName.setTextColor(accentColorInt)

            if (mediaState.packageName.isNotBlank()) {
                try {
                    val pm = context.packageManager
                    val icon = pm.getApplicationIcon(mediaState.packageName)
                    ivAppIcon.setImageDrawable(icon)
                    ivAppIcon.colorFilter = null
                    ivAppIcon.visibility = View.VISIBLE
                } catch (e: Exception) {
                    ivAppIcon.setImageResource(R.drawable.ic_media_play)
                    ivAppIcon.setColorFilter(accentColorInt)
                    ivAppIcon.visibility = View.VISIBLE
                }
            } else {
                ivAppIcon.setImageResource(R.drawable.ic_media_play)
                ivAppIcon.setColorFilter(accentColorInt)
                ivAppIcon.visibility = View.VISIBLE
            }

            btnToggleSeekBar.setColorFilter(accentColorInt)
            btnToggleSeekBar.setImageResource(
                if (isSeekBarVisible) R.drawable.ic_notif_chevron_up else R.drawable.ic_notif_chevron_down
            )
            btnToggleSeekBar.setOnClickListener {
                isSeekBarVisible = !isSeekBarVisible
                llSeekbarContainer.visibility = if (isSeekBarVisible) View.VISIBLE else View.GONE
                btnToggleSeekBar.setImageResource(
                    if (isSeekBarVisible) R.drawable.ic_notif_chevron_up else R.drawable.ic_notif_chevron_down
                )
            }

            btnClose.setColorFilter(accentColorInt)
            btnClose.setOnClickListener {
                MediaSessionManagerHelper.stopAndDismissSession(mediaState.sessionIndex)
            }

            // 4. Track Info
            tvTitle.text = mediaState.title.ifBlank { "Track Title" }
            tvTitle.setTextColor(accentColorInt)

            val artist = mediaState.artist.ifBlank { "" }
            if (artist.isNotBlank()) {
                tvArtist.text = artist
                tvArtist.setTextColor(subTextColorInt)
                tvArtist.visibility = View.VISIBLE
            } else {
                tvArtist.visibility = View.GONE
            }

            // 5. Playback Buttons
            if (mediaState.supportsShuffle) {
                btnShuffle.visibility = View.VISIBLE
                val isShuffleActive = mediaState.shuffleMode != 0 && mediaState.shuffleMode != -1
                btnShuffle.setColorFilter(if (isShuffleActive) accentColorInt else dimAccentColorInt)
                btnShuffle.setOnClickListener {
                    MediaSessionManagerHelper.toggleShuffleForSession(mediaState.sessionIndex)
                }
            } else {
                btnShuffle.visibility = View.GONE
            }

            btnPrevious.setColorFilter(accentColorInt)
            btnPrevious.setOnClickListener {
                MediaSessionManagerHelper.previousForSession(mediaState.sessionIndex)
            }

            btnPlayPause.setColorFilter(accentColorInt)
            btnPlayPause.setImageResource(
                if (mediaState.isPlaying) R.drawable.ic_media_pause else R.drawable.ic_media_play
            )
            btnPlayPause.setOnClickListener {
                MediaSessionManagerHelper.playPauseForSession(mediaState.sessionIndex)
            }

            btnNext.setColorFilter(accentColorInt)
            btnNext.setOnClickListener {
                MediaSessionManagerHelper.nextForSession(mediaState.sessionIndex)
            }

            if (mediaState.supportsRepeat) {
                btnRepeat.visibility = View.VISIBLE
                val isRepeatActive = mediaState.repeatMode != 0 && mediaState.repeatMode != -1
                btnRepeat.setImageResource(
                    if (mediaState.repeatMode == 1) R.drawable.ic_media_repeat_one else R.drawable.ic_media_repeat
                )
                btnRepeat.setColorFilter(if (isRepeatActive) accentColorInt else dimAccentColorInt)
                btnRepeat.setOnClickListener {
                    MediaSessionManagerHelper.toggleRepeatForSession(mediaState.sessionIndex)
                }
            } else {
                btnRepeat.visibility = View.GONE
            }

            if (mediaState.supportsFavorite) {
                btnFavorite.visibility = View.VISIBLE
                btnFavorite.setImageResource(
                    if (mediaState.isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
                )
                btnFavorite.setColorFilter(if (mediaState.isFavorite) accentColorInt else dimAccentColorInt)
                btnFavorite.setOnClickListener {
                    MediaSessionManagerHelper.toggleFavoriteForSession(mediaState.sessionIndex)
                }
            } else {
                btnFavorite.visibility = View.GONE
            }

            flRoot.setOnClickListener {
                MediaSessionManagerHelper.openActivePlayerAppForSession(context, mediaState.sessionIndex)
            }

            // 6. Custom Wavy Progress Bar
            val sliderSyncAll = PanelPreferences.sliderSyncAll.value
            val resolvedStyle = if (sliderSyncAll) {
                PanelPreferences.progressBarStyle.value
            } else {
                PanelPreferences.mediaSliderStyle.value
            }
            wavySeekBar.sliderStyle = resolvedStyle
            wavySeekBar.isPlaying = mediaState.isPlaying
            wavySeekBar.activeColor = accentColorInt
            wavySeekBar.thumbColor = accentColorInt
            wavySeekBar.inactiveColor = dimAccentColorInt
            if (!wavySeekBar.isCurrentlyDragging) {
                wavySeekBar.progress = mediaState.progress.coerceIn(0f, 1f)
                tvCurrTime.text = MediaState.formatDuration(mediaState.currentPositionMs)
            }

            wavySeekBar.onSeekListener = { progress ->
                val currMs = (progress * mediaState.totalDurationMs).toLong()
                tvCurrTime.text = MediaState.formatDuration(currMs)
            }

            wavySeekBar.onSeekFinalizedListener = { progress ->
                MediaSessionManagerHelper.seekToForSession(mediaState.sessionIndex, progress)
            }

            tvCurrTime.setTextColor(accentColorInt)
            tvTotalTime.setTextColor(accentColorInt)
            tvTotalTime.text = if (mediaState.totalDurationMs > 0) mediaState.formattedTotalTime else "--:--"

            llSeekbarContainer.visibility = if (isSeekBarVisible) View.VISIBLE else View.GONE
        }
    }

    inner class CompactMediaPlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val flRoot: FrameLayout = itemView.findViewById(R.id.compact_media_card_root)
        private val ivArtwork: ImageView = itemView.findViewById(R.id.iv_compact_media_artwork)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_compact_media_title)
        private val tvArtist: TextView = itemView.findViewById(R.id.tv_compact_media_artist)
        private val tvAlbum: TextView = itemView.findViewById(R.id.tv_compact_media_album)
        private val btnPrevious: ImageButton = itemView.findViewById(R.id.btn_compact_media_previous)
        private val btnPlayPause: ImageButton = itemView.findViewById(R.id.btn_compact_media_play_pause)
        private val btnNext: ImageButton = itemView.findViewById(R.id.btn_compact_media_next)

        fun bind(mediaState: MediaState, position: Int, totalCount: Int) {
            val context = itemView.context
            val density = context.resources.displayMetrics.density

            val isOreoStyle = PanelPreferences.isAndroidOreoStyle(context)
            val isDarkTheme = isDark

            // 1. Metadata: Song name, Artist name, Album name
            tvTitle.text = mediaState.title.ifBlank { "Track Title" }
            tvArtist.text = mediaState.artist.ifBlank { "Unknown Artist" }
            val albumText = mediaState.album.ifBlank {
                if (mediaState.appName.isNotBlank()) mediaState.appName else "Media Player"
            }
            tvAlbum.text = albumText

            // Colors
            val primaryTextColor = if (isDarkTheme) Color.WHITE else Color.parseColor("#1C1B1F")
            val secondaryTextColor = if (isDarkTheme) Color.parseColor("#B0B0B0") else Color.parseColor("#49454F")
            val tertiaryTextColor = if (isDarkTheme) Color.parseColor("#8E8E93") else Color.parseColor("#79747E")

            tvTitle.setTextColor(primaryTextColor)
            tvArtist.setTextColor(secondaryTextColor)
            tvAlbum.setTextColor(tertiaryTextColor)

            // 2. Play/Pause state and control buttons
            val playPauseRes = if (mediaState.isPlaying) R.drawable.ic_media_pause else R.drawable.ic_media_play
            btnPlayPause.setImageResource(playPauseRes)
            btnPlayPause.setColorFilter(primaryTextColor)
            btnPrevious.setColorFilter(primaryTextColor)
            btnNext.setColorFilter(primaryTextColor)

            // 3. Media Artwork (44x44dp matching screenshot, using outer corner radius slider)
            val panelGlobalCornerEnabled = PanelPreferences.panelGlobalCornerEnabled.value
            val panelOuterCorner = PanelPreferences.panelOuterCorner.value
            val panelInnerCorner = PanelPreferences.panelInnerCorner.value

            val outerCornerPx = panelOuterCorner * density
            val innerCornerPx = (if (panelGlobalCornerEnabled) panelOuterCorner else panelInnerCorner) * density

            ivArtwork.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, outerCornerPx)
                }
            }
            ivArtwork.clipToOutline = true

            val artwork = mediaState.artworkBitmap
            if (artwork != null) {
                ivArtwork.setImageBitmap(artwork)
                ivArtwork.background = null
                ivArtwork.clearColorFilter()
                ivArtwork.setPadding(0, 0, 0, 0)
            } else {
                ivArtwork.setImageResource(R.drawable.ic_media_music_note)
                val fallbackBg = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = outerCornerPx
                    setColor(if (isDarkTheme) Color.parseColor("#424242") else Color.parseColor("#E0E0E0"))
                }
                ivArtwork.background = fallbackBg
                ivArtwork.setColorFilter(primaryTextColor)
                val pad = (8 * density).toInt()
                ivArtwork.setPadding(pad, pad, pad, pad)
            }

            // 4. Click Actions
            btnPrevious.setOnClickListener {
                MediaSessionManagerHelper.previousForSession(mediaState.sessionIndex)
            }
            btnPlayPause.setOnClickListener {
                MediaSessionManagerHelper.playPauseForSession(mediaState.sessionIndex)
            }
            btnNext.setOnClickListener {
                MediaSessionManagerHelper.nextForSession(mediaState.sessionIndex)
            }
            flRoot.setOnClickListener {
                MediaSessionManagerHelper.openActivePlayerApp(context)
            }

            // 5. Card Background & Dynamic Corner Radii (Corner Radius Slider)

            val radii = if (isOreoStyle) {
                FloatArray(8) { 0f }
            } else {
                when {
                    totalCount <= 1 -> FloatArray(8) { outerCornerPx }
                    position == 0 -> floatArrayOf(
                        outerCornerPx, outerCornerPx,
                        outerCornerPx, outerCornerPx,
                        innerCornerPx, innerCornerPx,
                        innerCornerPx, innerCornerPx
                    )
                    position == totalCount - 1 -> floatArrayOf(
                        innerCornerPx, innerCornerPx,
                        innerCornerPx, innerCornerPx,
                        outerCornerPx, outerCornerPx,
                        outerCornerPx, outerCornerPx
                    )
                    else -> FloatArray(8) { innerCornerPx }
                }
            }

            val cardBg = if (isOreoStyle) {
                if (isDarkTheme) Color.parseColor("#212121") else Color.WHITE
            } else {
                customCardBackgroundColor ?: MonetThemeHelper.getCardBackground(context, isDarkTheme)
            }

            val contentDrawable = if (isOreoStyle) {
                val strokeColor = if (isDarkTheme) Color.parseColor("#33FFFFFF") else Color.parseColor("#33000000")
                val strokePx = (1 * density).toInt().coerceAtLeast(1)
                val solidOreoBg = if (isDarkTheme) Color.parseColor("#212121") else Color.parseColor("#FFFFFF")
                val baseDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(solidOreoBg)
                }
                val strokeDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setStroke(strokePx, strokeColor)
                    setColor(Color.TRANSPARENT)
                }
                android.graphics.drawable.LayerDrawable(arrayOf(baseDrawable, strokeDrawable)).apply {
                    setLayerInset(1, -strokePx, 0, -strokePx, 0)
                }
            } else {
                GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadii = radii
                    setColor(cardBg)
                }
            }

            val enablePanelShadows = PanelPreferences.enablePanelShadows.value
            val shadowElevation = PanelPreferences.panelShadowElevation.value
            val panelShadowAlpha = PanelPreferences.panelShadowAlpha.value
            val panelShadowOffsetY = PanelPreferences.panelShadowOffsetY.value

            CustomShadowHelper.applyCustomShadowBackground(
                view = flRoot,
                contentDrawable = contentDrawable,
                radii = radii,
                elevation = if (enablePanelShadows && !isOreoStyle) shadowElevation else 0f,
                isDark = isDarkTheme,
                customAlpha = panelShadowAlpha,
                customOffsetY = panelShadowOffsetY
            )
            flRoot.elevation = 0f
            flRoot.translationY = 0f
            flRoot.clipChildren = false
            flRoot.clipToPadding = false
            flRoot.clipToOutline = false
            flRoot.outlineProvider = null

            val lp = flRoot.layoutParams as? ViewGroup.MarginLayoutParams
            if (lp != null) {
                val targetBottomMargin = if (isOreoStyle) 0 else (4f * density).toInt()
                val targetTopMargin = if (isOreoStyle) 0 else lp.topMargin
                if (lp.bottomMargin != targetBottomMargin || lp.topMargin != targetTopMargin) {
                    lp.bottomMargin = targetBottomMargin
                    lp.topMargin = targetTopMargin
                    flRoot.layoutParams = lp
                }
            }
        }
    }

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var notificationItem: NotificationItem? = null
            private set
        val itemKey: String? get() = notificationItem?.key

        private val flRoot: FrameLayout = itemView.findViewById(R.id.fl_notif_root)
        private val ivAvatar: ImageView = itemView.findViewById(R.id.iv_notif_avatar)
        private val ivSubAppIcon: ImageView = itemView.findViewById(R.id.iv_notif_sub_app_icon)
        private val tvAppName: TextView = itemView.findViewById(R.id.tv_notif_app_name)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_notif_time)
        private val ivExpand: ImageView = itemView.findViewById(R.id.iv_notif_expand)
        private val ivDismiss: ImageView = itemView.findViewById(R.id.iv_notif_dismiss)
        private val ivMore: ImageView = itemView.findViewById(R.id.iv_notif_more)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_notif_title)
        private val tvText: TextView = itemView.findViewById(R.id.tv_notif_text)
        private val tvSubtext: TextView = itemView.findViewById(R.id.tv_notif_subtext)
        private val ivBigPicture: ImageView = itemView.findViewById(R.id.iv_notif_big_picture)
        private val pbProgress: ProgressBar = itemView.findViewById(R.id.pb_notif_progress)
        private val llActions: LinearLayout = itemView.findViewById(R.id.ll_notif_actions)

        fun bind(row: ShadeListRow.NotificationRow, position: Int, totalNotifCount: Int) {
            val item = row.item
            this.notificationItem = item
            val context = itemView.context
            val density = context.resources.displayMetrics.density
            val isExpanded = isItemExpanded(item)
            val isExpandable = item.actions.isNotEmpty() || !item.subText.isNullOrBlank() || item.text.length > 40 || item.bigPictureBitmap != null || item.hasProgress

            val isCall = item.channelId?.contains("call", ignoreCase = true) == true ||
                    item.packageName.contains("dialer", ignoreCase = true) ||
                    item.packageName.contains("telecom", ignoreCase = true) ||
                    item.actions.any { it.title.contains("Answer", ignoreCase = true) || it.title.contains("Decline", ignoreCase = true) }

            val isAlarm = item.channelId?.contains("alarm", ignoreCase = true) == true ||
                    item.channelId?.contains("timer", ignoreCase = true) == true ||
                    item.packageName.contains("clock", ignoreCase = true) ||
                    item.packageName.contains("deskclock", ignoreCase = true)

            val isOngoing = item.isOngoing

            // 1. Dynamic Monet Theme Colors
            val primaryColor = customPrimaryColor ?: MonetThemeHelper.getPrimaryAccent(context, isDark)
            val secondaryColor = MonetThemeHelper.getSecondaryAccent(context, isDark)
            val tertiaryColor = MonetThemeHelper.getTertiaryAccent(context, isDark)

            val disableNotifMonet = disableMonetNotifText
            val isOreoStyle = PanelPreferences.isAndroidOreoStyle(context)
            val isMarshmallowStyle = PanelPreferences.isAndroidMarshmallowStyle(context)
            val isKitKatStyle = PanelPreferences.isAndroidKitKatStyle(context)
            val isFlatStyle = isOreoStyle || isMarshmallowStyle || isKitKatStyle

            val textPrimaryColor = if (isFlatStyle) {
                if (isDark) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#222222")
            } else if (disableNotifMonet) {
                if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
            } else {
                MonetThemeHelper.getTextPrimary(context, isDark)
            }

            val textSecondaryColor = if (isFlatStyle) {
                if (isDark) android.graphics.Color.parseColor("#CCCCCC") else android.graphics.Color.parseColor("#555555")
            } else if (disableNotifMonet) {
                if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
            } else {
                MonetThemeHelper.getTextSecondary(context, isDark)
            }

            val headerColor = if (isOreoStyle) {
                if (isDark) android.graphics.Color.parseColor("#AAAAAA") else android.graphics.Color.parseColor("#666666")
            } else if (disableNotifMonet) {
                if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
            } else {
                when {
                    isCall -> primaryColor
                    isAlarm -> tertiaryColor
                    isOngoing -> secondaryColor
                    else -> tertiaryColor
                }
            }

            val subtextColor = if (isOreoStyle) {
                if (isDark) android.graphics.Color.parseColor("#AAAAAA") else android.graphics.Color.parseColor("#666666")
            } else if (disableNotifMonet) {
                if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
            } else {
                tertiaryColor
            }

            val actionTextColor = if (disableNotifMonet) {
                if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
            } else {
                primaryColor
            }

            val panelGlobalCornerEnabled = com.example.data.PanelPreferences.panelGlobalCornerEnabled.value
            val panelOuterCorner = com.example.data.PanelPreferences.panelOuterCorner.value
            val panelInnerCorner = com.example.data.PanelPreferences.panelInnerCorner.value

            val outerCornerPx = panelOuterCorner * density
            val innerCornerPx = (if (panelGlobalCornerEnabled) panelOuterCorner else panelInnerCorner) * density

            val radii = if (isOreoStyle) {
                FloatArray(8) { 0f }
            } else if (row.isGrouped) {
                val topRadius = innerCornerPx
                val bottomRadius = if (row.isLastInGroup) outerCornerPx else innerCornerPx
                floatArrayOf(
                    topRadius, topRadius,
                    topRadius, topRadius,
                    bottomRadius, bottomRadius,
                    bottomRadius, bottomRadius
                )
            } else {
                when {
                    totalNotifCount <= 1 -> {
                        FloatArray(8) { outerCornerPx }
                    }
                    position == 0 -> {
                        floatArrayOf(
                            outerCornerPx, outerCornerPx, // top-left
                            outerCornerPx, outerCornerPx, // top-right
                            innerCornerPx, innerCornerPx, // bottom-right
                            innerCornerPx, innerCornerPx  // bottom-left
                        )
                    }
                    position == totalNotifCount - 1 -> {
                        floatArrayOf(
                            innerCornerPx, innerCornerPx, // top-left
                            innerCornerPx, innerCornerPx, // top-right
                            outerCornerPx, outerCornerPx, // bottom-right
                            outerCornerPx, outerCornerPx  // bottom-left
                        )
                    }
                    else -> {
                        FloatArray(8) { innerCornerPx }
                    }
                }
            }

            val cardBg = if (isOreoStyle) {
                if (isDark) Color.parseColor("#212121") else Color.WHITE
            } else {
                customCardBackgroundColor ?: MonetThemeHelper.getCardBackground(context, isDark)
            }
            val contentDrawable = if (isOreoStyle) {
                val strokeColor = if (isDark) Color.parseColor("#33FFFFFF") else Color.parseColor("#33000000")
                val strokePx = (1 * density).toInt().coerceAtLeast(1)
                val baseDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(cardBg)
                }
                val strokeDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setStroke(strokePx, strokeColor)
                    setColor(Color.TRANSPARENT)
                }
                android.graphics.drawable.LayerDrawable(arrayOf(baseDrawable, strokeDrawable)).apply {
                    setLayerInset(1, -strokePx, 0, -strokePx, 0)
                }
            } else {
                MonetThemeHelper.createMonetCardDrawable(context, isDark, radii, customBgColor = cardBg)
            }

            val enablePanelShadows = com.example.data.PanelPreferences.enablePanelShadows.value
            val shadowElevation = com.example.data.PanelPreferences.panelShadowElevation.value
            val panelShadowAlpha = com.example.data.PanelPreferences.panelShadowAlpha.value
            val panelShadowOffsetY = com.example.data.PanelPreferences.panelShadowOffsetY.value

            CustomShadowHelper.applyCustomShadowBackground(
                view = flRoot,
                contentDrawable = contentDrawable,
                radii = radii,
                elevation = if (enablePanelShadows && !isOreoStyle && !isKitKatStyle) shadowElevation else 0f,
                isDark = isDark,
                customAlpha = panelShadowAlpha,
                customOffsetY = panelShadowOffsetY
            )
            flRoot.elevation = 0f
            flRoot.translationY = 0f
            flRoot.clipChildren = false
            flRoot.clipToPadding = false
            flRoot.clipToOutline = false
            flRoot.outlineProvider = null

            // Distance between notifications & media cards: 4dp (0dp in Oreo style)
            val lp = flRoot.layoutParams as? ViewGroup.MarginLayoutParams
            if (lp != null) {
                val targetBottomMargin = if (isOreoStyle) 0 else (4f * density).toInt()
                val targetTopMargin = if (isOreoStyle) 0 else lp.topMargin
                if (lp.bottomMargin != targetBottomMargin || lp.topMargin != targetTopMargin) {
                    lp.bottomMargin = targetBottomMargin
                    lp.topMargin = targetTopMargin
                    flRoot.layoutParams = lp
                }
            }

            // 2. Root Click & Long-Click
            flRoot.setOnClickListener {
                onNotificationClick(item)
            }
            flRoot.setOnLongClickListener {
                showOptionsMenu(ivMore, item, context)
                true
            }

            // 3. Avatar / App Icon
            val avatar = item.avatarBitmap
            if (avatar != null && !avatar.isRecycled) {
                val safeBmp = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && avatar.config == Bitmap.Config.HARDWARE) {
                        avatar.copy(Bitmap.Config.ARGB_8888, false)
                    } else avatar
                } catch (_: Throwable) {
                    null
                }
                if (safeBmp != null) {
                    ivAvatar.setImageBitmap(safeBmp)
                    if (item.appIcon != null) {
                        ivSubAppIcon.visibility = View.VISIBLE
                        ivSubAppIcon.setImageDrawable(item.appIcon)
                    } else {
                        ivSubAppIcon.visibility = View.GONE
                    }
                } else if (item.appIcon != null) {
                    ivAvatar.setImageDrawable(item.appIcon)
                    ivSubAppIcon.visibility = View.GONE
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_notif_bell)
                    ivAvatar.setColorFilter(if (isCall) primaryColor else if (isAlarm) tertiaryColor else primaryColor)
                    ivSubAppIcon.visibility = View.GONE
                }
            } else if (item.appIcon != null) {
                ivAvatar.setImageDrawable(item.appIcon)
                ivSubAppIcon.visibility = View.GONE
            } else {
                ivAvatar.setImageResource(R.drawable.ic_notif_bell)
                ivAvatar.setColorFilter(if (isCall) primaryColor else if (isAlarm) tertiaryColor else primaryColor)
                ivSubAppIcon.visibility = View.GONE
            }

            // 4. Header Text & Time with Monet Colors per Notification Type
            val resolvedAppName = item.appName.ifBlank { item.packageName }
            val typeBadge = when {
                isCall -> " • CALL"
                isAlarm -> " • ALARM"
                item.hasProgress -> " • PROGRESS"
                isOngoing -> " • ONGOING"
                else -> ""
            }
            tvAppName.text = "$resolvedAppName$typeBadge"
            tvAppName.setTextColor(headerColor)

            val timeFormat = DateFormat.getTimeFormat(context)
            tvTime.text = "• ${timeFormat.format(Date(item.postTime))}"
            tvTime.setTextColor(textSecondaryColor)

            // 5. Header Icons with Monet Theme tint
            ivDismiss.setColorFilter(textSecondaryColor)
            ivMore.setColorFilter(textSecondaryColor)
            ivExpand.setColorFilter(textSecondaryColor)

            // 6. Expand / Collapse Icon
            if (isExpandable) {
                ivExpand.visibility = View.VISIBLE
                ivExpand.setImageResource(
                    if (isExpanded) R.drawable.ic_notif_chevron_up else R.drawable.ic_notif_chevron_down
                )
                ivExpand.contentDescription = if (isExpanded) "Collapse" else "Expand"
                ivExpand.setOnClickListener {
                    val currentlyExpanded = isItemExpanded(item)
                    expandedStateMap[item.key] = !currentlyExpanded
                    notifyItemChanged(bindingAdapterPosition)
                }
            } else {
                ivExpand.visibility = View.GONE
            }

            // 7. Dismiss Icon
            if (item.isClearable) {
                ivDismiss.visibility = View.VISIBLE
                ivDismiss.setOnClickListener {
                    onNotificationDismiss(item)
                }
            } else {
                ivDismiss.visibility = View.GONE
            }

            // 8. Options Menu
            ivMore.setOnClickListener {
                showOptionsMenu(ivMore, item, context)
            }

            // 9. Title & Text with Monet Typographic Colors
            if (item.title.isNotBlank()) {
                tvTitle.visibility = View.VISIBLE
                tvTitle.text = item.title
                tvTitle.setTextColor(textPrimaryColor)
                tvTitle.maxLines = if (isExpanded) Int.MAX_VALUE else 1
            } else {
                tvTitle.visibility = View.GONE
            }

            if (item.text.isNotBlank()) {
                tvText.visibility = View.VISIBLE
                tvText.text = item.text
                tvText.setTextColor(textSecondaryColor)
                tvText.maxLines = if (isExpanded) Int.MAX_VALUE else 2
            } else {
                tvText.visibility = View.GONE
            }

            if (isExpanded && !item.subText.isNullOrBlank()) {
                tvSubtext.visibility = View.VISIBLE
                tvSubtext.text = item.subText
                tvSubtext.setTextColor(subtextColor)
            } else {
                tvSubtext.visibility = View.GONE
            }

            // 9.5 Download / Ongoing Task Progress Bar
            if (item.hasProgress) {
                pbProgress.visibility = View.VISIBLE
                if (item.isIndeterminateProgress) {
                    pbProgress.isIndeterminate = true
                } else {
                    pbProgress.isIndeterminate = false
                    pbProgress.max = if (item.progressMax > 0) item.progressMax else 100
                    pbProgress.progress = item.progress.coerceAtLeast(0)
                }
                pbProgress.progressTintList = ColorStateList.valueOf(primaryColor)
                pbProgress.indeterminateTintList = ColorStateList.valueOf(primaryColor)
            } else {
                pbProgress.visibility = View.GONE
            }

            // 10. BigPicture Attachment (Visible ONLY when expanded)
            val bigPicture = item.bigPictureBitmap
            if (isExpanded && bigPicture != null && !bigPicture.isRecycled) {
                val safePic = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bigPicture.config == Bitmap.Config.HARDWARE) {
                        bigPicture.copy(Bitmap.Config.ARGB_8888, false)
                    } else bigPicture
                } catch (_: Throwable) {
                    null
                }
                if (safePic != null) {
                    ivBigPicture.visibility = View.VISIBLE
                    ivBigPicture.setImageBitmap(safePic)
                } else {
                    ivBigPicture.visibility = View.GONE
                }
            } else {
                ivBigPicture.visibility = View.GONE
            }

            // 11. Dynamic Action Buttons (Shown when expanded, hidden when collapsed)
            llActions.removeAllViews()
            val actionsToShow = if (isExpanded) item.actions else emptyList()
            if (actionsToShow.isNotEmpty()) {
                llActions.visibility = View.VISIBLE
                actionsToShow.forEach { action ->
                    val btn = TextView(context).apply {
                        text = action.title
                        textSize = 11f
                        setTextColor(actionTextColor)
                        background = MonetThemeHelper.createMonetActionButtonDrawable(context, isDark, 8f * density)
                        setPadding(
                            (12 * density).toInt(),
                            (6 * density).toInt(),
                            (12 * density).toInt(),
                            (6 * density).toInt()
                        )
                        isClickable = true
                        isFocusable = true
                        setOnClickListener {
                            onActionClick(action)
                        }
                    }
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginEnd = (6 * density).toInt()
                    }
                    llActions.addView(btn, params)
                }
            } else {
                llActions.visibility = View.GONE
            }
        }

        private fun showOptionsMenu(anchor: View, item: NotificationItem, context: Context) {
            val popup = PopupMenu(context, anchor)
            popup.menu.add(0, 1, 0, "Hide app: ${item.appName}")
            if (!item.channelId.isNullOrBlank()) {
                popup.menu.add(0, 2, 1, "Hide channel: ${item.channelId}")
            }
            popup.menu.add(0, 3, 2, "Notification settings")

            val categories = NotificationCategoryManager.categories.value
            val catIdMap = mutableMapOf<Int, String?>()
            if (categories.isNotEmpty()) {
                val subMenu = popup.menu.addSubMenu(0, 100, 3, "Assign to category")
                val currentCat = NotificationCategoryManager.findCategoryForPackage(item.packageName)
                var nextItemId = 1000
                categories.forEach { cat ->
                    val id = nextItemId++
                    catIdMap[id] = cat.id
                    val prefix = if (currentCat?.id == cat.id) "✓ " else ""
                    subMenu.add(1, id, id, "$prefix${cat.name}")
                }
                val uncatId = nextItemId
                catIdMap[uncatId] = null
                val uncatPrefix = if (currentCat == null) "✓ " else ""
                subMenu.add(1, uncatId, uncatId, "${uncatPrefix}Other (Uncategorized)")
            }

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    1 -> {
                        QuickPanelNotificationListenerService.hidePackage(item.packageName, context)
                        true
                    }
                    2 -> {
                        if (!item.channelId.isNullOrBlank()) {
                            QuickPanelNotificationListenerService.hideChannel(item.channelId, context)
                        }
                        true
                    }
                    3 -> {
                        try {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, item.packageName)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                        true
                    }
                    else -> {
                        if (catIdMap.containsKey(menuItem.itemId)) {
                            NotificationCategoryManager.assignPackageToCategory(
                                context = context,
                                packageName = item.packageName,
                                targetCategoryId = catIdMap[menuItem.itemId]
                            )
                            rebuildDisplayRows()
                            notifyDataSetChanged()
                            true
                        } else {
                            false
                        }
                    }
                }
            }
            popup.show()
        }
    }
}
