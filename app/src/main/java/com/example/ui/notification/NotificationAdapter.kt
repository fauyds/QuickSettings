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
    var onClearAll: (() -> Unit)? = null
) : ListAdapter<NotificationItem, RecyclerView.ViewHolder>(NotificationDiffCallback()) {

    var isDark: Boolean = true
    var customPrimaryColor: Int? = null
    var customCardBackgroundColor: Int? = null
    var showFooter: Boolean = false
    var disableMonetNotifText: Boolean = false

    private val expandedStateMap = mutableMapOf<String, Boolean>()

    fun isItemExpanded(item: NotificationItem): Boolean {
        return expandedStateMap[item.key] ?: false
    }

    companion object {
        private const val TYPE_NOTIFICATION = 0
        private const val TYPE_FOOTER = 1
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
            if (currentList.isNotEmpty()) {
                notifyItemChanged(0)
                if (currentList.size > 1) {
                    notifyItemChanged(1)
                    notifyItemChanged(currentList.size - 1)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return currentList.size + if (showFooter && currentList.isNotEmpty()) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (showFooter && position == currentList.size) TYPE_FOOTER else TYPE_NOTIFICATION
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_FOOTER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification_footer, parent, false)
            FooterViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification_card, parent, false)
            NotificationViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NotificationViewHolder) {
            val item = getItem(position) ?: return
            holder.bind(item, position, currentList.size)
        } else if (holder is FooterViewHolder) {
            holder.bind()
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

            if (!useFab) {
                // When FAB button is disabled: "Clear All" button remains on right side anyway
                llFooterRoot.gravity = android.view.Gravity.END or android.view.Gravity.CENTER_VERTICAL

                tvClearAllText.visibility = View.VISIBLE
                val params = btnClearAll.layoutParams as ViewGroup.MarginLayoutParams
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                btnClearAll.layoutParams = params
                btnClearAll.setPadding((14 * density).toInt(), (6 * density).toInt(), (14 * density).toInt(), (6 * density).toInt())
                btnClearAll.gravity = android.view.Gravity.CENTER_VERTICAL

                val iconParams = ivClearAllIcon.layoutParams as ViewGroup.MarginLayoutParams
                iconParams.width = (16 * density).toInt()
                iconParams.height = (16 * density).toInt()
                iconParams.marginStart = 0
                ivClearAllIcon.layoutParams = iconParams
                ivClearAllIcon.setImageResource(R.drawable.ic_notif_clear_all)

                val clearAllTextColor = if (disableBg) {
                    MonetThemeHelper.getPrimaryAccent(context, isDarkTheme)
                } else {
                    if (isDarkTheme) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                }
                tvClearAllText.setTextColor(clearAllTextColor)
                ivClearAllIcon.setColorFilter(clearAllTextColor)

                val enablePanelShadows = com.example.data.PanelPreferences.enablePanelShadows.value
                val shadowElevation = com.example.data.PanelPreferences.panelShadowElevation.value
                if (disableBg) {
                    btnClearAll.background = null
                    btnClearAll.elevation = 0f
                    btnClearAll.translationY = 0f
                } else {
                    btnClearAll.elevation = if (enablePanelShadows) shadowElevation * density else 0f
                    btnClearAll.translationY = 0f
                    btnClearAll.background = MonetThemeHelper.createMonetPrimaryPillDrawable(context, isDarkTheme, 14f * density)
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

                val tintColor = if (disableBg) {
                    MonetThemeHelper.getPrimaryAccent(context, isDarkTheme)
                } else {
                    if (isDarkTheme) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                }
                ivClearAllIcon.setColorFilter(tintColor)

                val enablePanelShadows = com.example.data.PanelPreferences.enablePanelShadows.value
                val shadowElevation = com.example.data.PanelPreferences.panelShadowElevation.value
                if (disableBg) {
                    btnClearAll.background = null
                    btnClearAll.elevation = 0f
                    btnClearAll.translationY = 0f
                } else {
                    btnClearAll.elevation = if (enablePanelShadows) shadowElevation * density else 0f
                    btnClearAll.translationY = 0f
                    val primaryColor = MonetThemeHelper.getPrimaryAccent(context, isDarkTheme)
                    val shapeDrawable = com.example.ui.shape.QsShapeManager.createDrawable(fabShapeId, primaryColor)
                    val maskDrawable = com.example.ui.shape.QsShapeManager.createDrawable(fabShapeId, android.graphics.Color.WHITE)
                    val rippleColor = if (isDarkTheme) android.graphics.Color.parseColor("#44FFFFFF") else android.graphics.Color.parseColor("#33000000")
                    btnClearAll.background = android.graphics.drawable.RippleDrawable(
                        android.content.res.ColorStateList.valueOf(rippleColor),
                        shapeDrawable,
                        maskDrawable
                    )
                }
            }

            btnClearAll.setOnClickListener {
                onClearAll?.invoke()
            }
        }
    }

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

        fun bind(item: NotificationItem, position: Int, totalNotifCount: Int) {
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
            val textPrimaryColor = if (disableNotifMonet) {
                if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
            } else {
                MonetThemeHelper.getTextPrimary(context, isDark)
            }
            val textSecondaryColor = if (disableNotifMonet) {
                if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
            } else {
                MonetThemeHelper.getTextSecondary(context, isDark)
            }
            val headerColor = if (disableNotifMonet) {
                if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
            } else {
                when {
                    isCall -> primaryColor
                    isAlarm -> tertiaryColor
                    isOngoing -> secondaryColor
                    else -> tertiaryColor
                }
            }
            val subtextColor = if (disableNotifMonet) {
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

            val radii = when {
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

            val cardBg = customCardBackgroundColor ?: MonetThemeHelper.getCardBackground(context, isDark)
            flRoot.background = MonetThemeHelper.createMonetCardDrawable(context, isDark, radii, customBgColor = cardBg)
            val enablePanelShadows = com.example.data.PanelPreferences.enablePanelShadows.value
            val shadowElevation = com.example.data.PanelPreferences.panelShadowElevation.value
            flRoot.elevation = if (enablePanelShadows) shadowElevation * density else 0f
            flRoot.translationY = 0f

            // Outline provider for crisp clipping to the dynamic shape
            flRoot.outlineProvider = object : ViewOutlineProvider() {
                private val path = Path()
                override fun getOutline(view: View, outline: Outline) {
                    if (view.width > 0 && view.height > 0) {
                        path.reset()
                        path.addRoundRect(
                            0f, 0f, view.width.toFloat(), view.height.toFloat(),
                            radii,
                            Path.Direction.CW
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            outline.setPath(path)
                        } else {
                            outline.setConvexPath(path)
                        }
                    }
                }
            }
            flRoot.clipToOutline = true

            // Distance between notifications: 3dp
            val lp = flRoot.layoutParams as? ViewGroup.MarginLayoutParams
            if (lp != null) {
                val margin3dp = (3f * density).toInt()
                if (lp.bottomMargin != margin3dp) {
                    lp.bottomMargin = margin3dp
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
                    else -> false
                }
            }
            popup.show()
        }
    }
}
