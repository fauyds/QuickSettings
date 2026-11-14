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

enum class NotificationCategoryIcon(
    val id: String,
    val label: String,
    val group: String,
    val imageVector: ImageVector,
    @param:DrawableRes val drawableRes: Int
) {
    LABEL("label", "General", "General", Icons.Default.Label, R.drawable.ic_cat_label),
    CHAT("chat", "Messages", "Communication", Icons.Default.Chat, R.drawable.ic_cat_chat),
    EMAIL("email", "Email", "Communication", Icons.Default.Email, R.drawable.ic_cat_email),
    GROUP("group", "Social", "Communication", Icons.Default.Group, R.drawable.ic_cat_group),
    WORK("work", "Work", "Productivity", Icons.Default.Work, R.drawable.ic_cat_work),
    CALENDAR("calendar", "Events", "Productivity", Icons.Default.Event, R.drawable.ic_cat_calendar),
    PRIORITY("priority", "Priority", "Alerts", Icons.Default.PriorityHigh, R.drawable.ic_cat_priority),
    ALERTS("alerts", "Alerts", "Alerts", Icons.Default.NotificationsActive, R.drawable.ic_notif_bell),
    STAR("star", "Favorites", "General", Icons.Default.Star, R.drawable.ic_cat_star),
    WALLET("wallet", "Finance", "Lifestyle", Icons.Default.AccountBalanceWallet, R.drawable.ic_cat_wallet),
    SHOPPING("shopping", "Shopping", "Lifestyle", Icons.Default.ShoppingBag, R.drawable.ic_cat_shopping),
    DELIVERY("delivery", "Delivery", "Lifestyle", Icons.Default.LocalShipping, R.drawable.ic_cat_delivery),
    FOOD("food", "Food", "Lifestyle", Icons.Default.Restaurant, R.drawable.ic_cat_food),
    FLIGHT("flight", "Travel", "Lifestyle", Icons.Default.Flight, R.drawable.ic_qs_flight),
    MUSIC("music", "Music", "Media", Icons.Default.MusicNote, R.drawable.ic_media_music_note),
    MOVIE("movie", "Media", "Media", Icons.Default.Movie, R.drawable.ic_cat_movie),
    GAMING("gaming", "Gaming", "Media", Icons.Default.SportsEsports, R.drawable.ic_cat_gaming),
    NEWS("news", "News", "Productivity", Icons.Default.Article, R.drawable.ic_cat_news),
    HEALTH("health", "Health", "Lifestyle", Icons.Default.Favorite, R.drawable.ic_heart_filled),
    SECURITY("security", "Security", "System", Icons.Default.Security, R.drawable.ic_cat_security),
    SETTINGS("settings", "System", "System", Icons.Default.Settings, R.drawable.ic_qs_settings),
    CLOUD("cloud", "Cloud", "System", Icons.Default.Cloud, R.drawable.ic_cat_cloud),
    SCHOOL("school", "Study", "Productivity", Icons.Default.School, R.drawable.ic_cat_school),
    HOME("home", "Home", "General", Icons.Default.Home, R.drawable.ic_cat_home),
    FOLDER("folder", "Other", "General", Icons.Default.Folder, R.drawable.ic_cat_folder);

    companion object {
        val groups: List<String> = listOf(
            "All",
            "Communication",
            "Productivity",
            "Lifestyle",
            "Media",
            "Alerts",
            "System",
            "General"
        )

        fun fromId(id: String?): NotificationCategoryIcon =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: LABEL
    }
}

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
