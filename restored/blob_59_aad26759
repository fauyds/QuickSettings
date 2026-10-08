package com.example.ui.notification

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.provider.Settings
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
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
    var showFooter: Boolean = false

    private val expandedStateMap = mutableMapOf<String, Boolean>()

    fun isItemExpanded(item: NotificationItem): Boolean {
        return expandedStateMap[item.key] ?: false
    }

    companion object {
        private const val TYPE_NOTIFICATION = 0
        private const val TYPE_FOOTER = 1
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
            holder.bind(item)
        } else if (holder is FooterViewHolder) {
            holder.bind()
        }
    }

    inner class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val btnClearAll: LinearLayout = itemView.findViewById(R.id.btn_notif_clear_all)
        private val tvClearAllText: TextView = itemView.findViewById(R.id.tv_notif_clear_all_text)
        private val ivClearAllIcon: ImageView = itemView.findViewById(R.id.iv_notif_clear_all_icon)

        fun bind() {
            val context = itemView.context
            val density = context.resources.displayMetrics.density
            val onPrimary = MonetThemeHelper.getOnPrimaryText(context, isDark)

            btnClearAll.background = MonetThemeHelper.createMonetPrimaryPillDrawable(context, isDark, 14f * density)
            tvClearAllText.setTextColor(onPrimary)
            ivClearAllIcon.setColorFilter(onPrimary)

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
        private val llActions: LinearLayout = itemView.findViewById(R.id.ll_notif_actions)

        fun bind(item: NotificationItem) {
            val context = itemView.context
            val density = context.resources.displayMetrics.density
            val isExpanded = isItemExpanded(item)
            val isExpandable = item.actions.isNotEmpty() || !item.subText.isNullOrBlank() || item.text.length > 40 || item.bigPictureBitmap != null

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
            val textPrimaryColor = MonetThemeHelper.getTextPrimary(context, isDark)
            val textSecondaryColor = MonetThemeHelper.getTextSecondary(context, isDark)

            // Dynamic Card Background with 20dp corner radius
            flRoot.background = MonetThemeHelper.createMonetCardDrawable(context, isDark, 20f * density)

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
                isOngoing -> " • ONGOING"
                else -> ""
            }
            tvAppName.text = "$resolvedAppName$typeBadge"
            val headerColor = when {
                isCall -> primaryColor
                isAlarm -> tertiaryColor
                isOngoing -> secondaryColor
                else -> tertiaryColor
            }
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
                tvSubtext.setTextColor(tertiaryColor)
            } else {
                tvSubtext.visibility = View.GONE
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
                        setTextColor(primaryColor)
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
