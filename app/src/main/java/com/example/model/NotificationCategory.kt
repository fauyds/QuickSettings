package com.example.model

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R

@Immutable
class NotificationCategoryIcon(
    val id: String,
    val label: String,
    val group: String,
    private val vectorProvider: () -> ImageVector,
    @param:DrawableRes val drawableRes: Int? = null,
    val keywords: String = ""
) {
    val imageVector: ImageVector
        get() = vectorProvider()

    override fun equals(other: Any?): Boolean =
        this === other || (other is NotificationCategoryIcon && id == other.id)

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "NotificationCategoryIcon(id=$id, label=$label, group=$group)"

    companion object {
        val LABEL = NotificationCategoryIcon("label", "General", "General", { Icons.Filled.Label }, R.drawable.ic_cat_label, "tag category default")
        val CHAT = NotificationCategoryIcon("chat", "Messages", "Communication", { Icons.Filled.Chat }, R.drawable.ic_cat_chat, "sms text talk")
        val EMAIL = NotificationCategoryIcon("email", "Email", "Communication", { Icons.Filled.Email }, R.drawable.ic_cat_email, "mail inbox letter")
        val GROUP = NotificationCategoryIcon("group", "Social", "Social", { Icons.Filled.Group }, R.drawable.ic_cat_group, "people friends community")
        val WORK = NotificationCategoryIcon("work", "Work", "Work", { Icons.Filled.Work }, R.drawable.ic_cat_work, "office job business briefcase")
        val CALENDAR = NotificationCategoryIcon("calendar", "Events", "Work", { Icons.Filled.Event }, R.drawable.ic_cat_calendar, "date schedule meeting")
        val PRIORITY = NotificationCategoryIcon("priority", "Priority", "Alerts", { Icons.Filled.PriorityHigh }, R.drawable.ic_cat_priority, "urgent important")
        val ALERTS = NotificationCategoryIcon("alerts", "Alerts", "Alerts", { Icons.Filled.NotificationsActive }, R.drawable.ic_notif_bell, "bell alarm ring")
        val STAR = NotificationCategoryIcon("star", "Favorites", "General", { Icons.Filled.Star }, R.drawable.ic_cat_star, "favorite bookmark")
        val WALLET = NotificationCategoryIcon("wallet", "Finance", "Finance", { Icons.Filled.AccountBalanceWallet }, R.drawable.ic_cat_wallet, "money cash pay")
        val SHOPPING = NotificationCategoryIcon("shopping", "Shopping", "Shopping", { Icons.Filled.ShoppingBag }, R.drawable.ic_cat_shopping, "store retail buy")
        val DELIVERY = NotificationCategoryIcon("delivery", "Delivery", "Shopping", { Icons.Filled.LocalShipping }, R.drawable.ic_cat_delivery, "package truck parcel")
        val FOOD = NotificationCategoryIcon("food", "Food", "Food", { Icons.Filled.Restaurant }, R.drawable.ic_cat_food, "eat dining meal")
        val FLIGHT = NotificationCategoryIcon("flight", "Travel", "Travel", { Icons.Filled.Flight }, R.drawable.ic_qs_flight, "airplane trip airport")
        val MUSIC = NotificationCategoryIcon("music", "Music", "Media", { Icons.Filled.MusicNote }, R.drawable.ic_media_music_note, "audio song tune")
        val MOVIE = NotificationCategoryIcon("movie", "Media", "Media", { Icons.Filled.Movie }, R.drawable.ic_cat_movie, "film video cinema")
        val GAMING = NotificationCategoryIcon("gaming", "Gaming", "Gaming", { Icons.Filled.SportsEsports }, R.drawable.ic_cat_gaming, "controller game play")
        val NEWS = NotificationCategoryIcon("news", "News", "Work", { Icons.Filled.Article }, R.drawable.ic_cat_news, "press blog reading")
        val HEALTH = NotificationCategoryIcon("health", "Health", "Health", { Icons.Filled.Favorite }, R.drawable.ic_heart_filled, "heart wellness fitness")
        val SECURITY = NotificationCategoryIcon("security", "Security", "Alerts", { Icons.Filled.Security }, R.drawable.ic_cat_security, "shield protect")
        val SETTINGS = NotificationCategoryIcon("settings", "System", "System", { Icons.Filled.Settings }, R.drawable.ic_qs_settings, "gear config prefs")
        val CLOUD = NotificationCategoryIcon("cloud", "Cloud", "System", { Icons.Filled.Cloud }, R.drawable.ic_cat_cloud, "sync backup drive")
        val SCHOOL = NotificationCategoryIcon("school", "Study", "Education", { Icons.Filled.School }, R.drawable.ic_cat_school, "learn college university")
        val HOME = NotificationCategoryIcon("home", "Home", "Home", { Icons.Filled.Home }, R.drawable.ic_cat_home, "house smart living")
        val FOLDER = NotificationCategoryIcon("folder", "Other", "General", { Icons.Filled.Folder }, R.drawable.ic_cat_folder, "directory files")

        val quickEntries: List<NotificationCategoryIcon> = listOf(
            LABEL, CHAT, EMAIL, GROUP, WORK, CALENDAR, PRIORITY, ALERTS, STAR,
            WALLET, SHOPPING, DELIVERY, FOOD, FLIGHT, MUSIC, MOVIE, GAMING,
            NEWS, HEALTH, SECURITY, SETTINGS, CLOUD, SCHOOL, HOME, FOLDER
        )

        val entries: List<NotificationCategoryIcon> by lazy {
            buildList {
                addAll(quickEntries)
                addAll(notificationCategoryIconsPart1())
                addAll(notificationCategoryIconsPart2())
                addAll(notificationCategoryIconsPart3())
            }.distinctBy { it.id }
        }

        private val byId: Map<String, NotificationCategoryIcon> by lazy {
            entries.associateBy { it.id.lowercase() }
        }

        val groups: List<String> = listOf(
            "All",
            "Communication",
            "Social",
            "Work",
            "Finance",
            "Shopping",
            "Food",
            "Travel",
            "Media",
            "Gaming",
            "Health",
            "Home",
            "Alerts",
            "System",
            "Education",
            "Weather",
            "General"
        )

        fun fromId(id: String?): NotificationCategoryIcon =
            if (id.isNullOrBlank()) LABEL else byId[id.lowercase()] ?: LABEL
    }
}

internal fun googleCatIcon(
    id: String,
    label: String,
    group: String,
    keywords: String = "",
    vectorProvider: () -> ImageVector
): NotificationCategoryIcon = NotificationCategoryIcon(
    id = id,
    label = label,
    group = group,
    vectorProvider = vectorProvider,
    drawableRes = null,
    keywords = keywords
)

@Immutable
data class NotificationCategory(
    val id: String,
    val name: String,
    val iconId: String = NotificationCategoryIcon.LABEL.id,
    val packageNames: Set<String> = emptySet(),
    val keywords: List<String> = emptyList(),
    val isExpandedByDefault: Boolean = true,
    val sortOrder: Int = 0
) {
    fun resolveIcon(): NotificationCategoryIcon = NotificationCategoryIcon.fromId(iconId)

    fun matches(item: NotificationItem): Boolean {
        if (packageNames.contains(item.packageName)) return true
        if (keywords.isNotEmpty()) {
            val haystack = buildString {
                append(item.appName).append(' ')
                append(item.title).append(' ')
                append(item.text).append(' ')
                append(item.subText ?: "").append(' ')
                append(item.channelId ?: "")
            }
            return keywords.any { kw -> kw.isNotBlank() && haystack.contains(kw.trim(), ignoreCase = true) }
        }
        return false
    }
}
