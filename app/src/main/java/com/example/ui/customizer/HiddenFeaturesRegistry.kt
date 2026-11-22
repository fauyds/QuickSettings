package com.example.ui.customizer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BorderStyle
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Gradient
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.PanelPreferences
import com.example.data.ThemeManager
import com.example.ui.theme.ExpressiveTheme

data class HiddenFeatureDescriptor(
    val id: String,
    val title: String,
    val categoryTitle: String,
    val icon: ImageVector
)

object HiddenFeatureCatalog {

    private data class CardMeta(
        val title: String,
        val category: CustomizeCategory,
        val icon: ImageVector,
        val linkedFavoriteId: String? = null
    )

    private val cardMetadata: Map<String, CardMeta> = mapOf(
        // BASIC
        "theme_mode_card" to CardMeta("App Settings Theme Mode", CustomizeCategory.BASIC, Icons.Default.Palette, FavoriteFeature.BASIC_THEME_MODE.id),
        "collapse_expand_options_card" to CardMeta("Collapse Options With Sliders & Buttons", CustomizeCategory.BASIC, Icons.Default.UnfoldLess, FavoriteFeature.BASIC_COLLAPSE_OPTIONS.id),
        "collapse_expand_subheaders_card" to CardMeta("Collapse & Expand List Subheaders", CustomizeCategory.BASIC, Icons.Default.MenuOpen, FavoriteFeature.BASIC_COLLAPSE_SUBHEADERS.id),
        "import_export_card" to CardMeta("Import & Export Settings", CustomizeCategory.BASIC, Icons.Default.SyncAlt, FavoriteFeature.BASIC_BACKUP_RESTORE.id),

        // HEADS UP
        "heads_up_overlay_engine_card" to CardMeta("Custom Heads-Up Overlay", CustomizeCategory.HEADS_UP, Icons.Default.NotificationsActive, FavoriteFeature.HEADS_UP_MASTER.id),
        "system_heads_up_card" to CardMeta("System Heads-Up Fallback", CustomizeCategory.HEADS_UP, Icons.Default.Notifications, FavoriteFeature.HEADS_UP_SYSTEM.id),
        "heads_up_permission_card" to CardMeta("Notification Listener Access", CustomizeCategory.HEADS_UP, Icons.Default.Security, FavoriteFeature.HEADS_UP_PERMISSION.id),
        "heads_up_test_card" to CardMeta("Test Heads-Up Triggers", CustomizeCategory.HEADS_UP, Icons.Default.Notifications, FavoriteFeature.HEADS_UP_TEST.id),
        "heads_up_timeout_card" to CardMeta("Auto-Dismiss Timeout", CustomizeCategory.HEADS_UP, Icons.Default.Timer, FavoriteFeature.HEADS_UP_TIMEOUT.id),
        "heads_up_swipe_actions_card" to CardMeta("Heads-Up Swipe Actions", CustomizeCategory.HEADS_UP, Icons.Default.TouchApp, FavoriteFeature.HEADS_UP_GESTURES.id),
        "heads_up_tap_actions_card" to CardMeta("Heads-Up Tap Actions", CustomizeCategory.HEADS_UP, Icons.Default.TouchApp, FavoriteFeature.HEADS_UP_TAP_ACTIONS.id),
        "heads_up_snooze_duration_card" to CardMeta("Heads-Up Snooze Duration", CustomizeCategory.HEADS_UP, Icons.Default.Timer, FavoriteFeature.HEADS_UP_SNOOZE_DURATION.id),

        // HEADS UP DESIGN
        "heads_up_design_test_card" to CardMeta("Heads-Up Appearance Preview", CustomizeCategory.HEADS_UP_DESIGN, Icons.Default.Notifications, FavoriteFeature.HEADS_UP_DESIGN_TEST.id),
        "heads_up_edge_to_edge_card" to CardMeta("Edge-to-Edge Banner", CustomizeCategory.HEADS_UP_DESIGN, Icons.Default.AspectRatio, FavoriteFeature.HEADS_UP_DESIGN_EDGE_TO_EDGE.id),
        "heads_up_corner_radius_card" to CardMeta("Banner Corner Radius", CustomizeCategory.HEADS_UP_DESIGN, Icons.Default.RoundedCorner, FavoriteFeature.HEADS_UP_DESIGN_CORNER_RADIUS.id),
        "heads_up_elevation_card" to CardMeta("Banner Elevation & Shadow", CustomizeCategory.HEADS_UP_DESIGN, Icons.Default.Layers, FavoriteFeature.HEADS_UP_DESIGN_ELEVATION.id),
        "heads_up_top_margin_card" to CardMeta("Banner Top Margin", CustomizeCategory.HEADS_UP_DESIGN, Icons.Default.VerticalAlignTop, FavoriteFeature.HEADS_UP_DESIGN_TOP_MARGIN.id),

        // QS DESIGN
        "panel_style_card" to CardMeta("Quick Settings Panel Style", CustomizeCategory.QS_DESIGN, Icons.Default.Style, FavoriteFeature.DESIGN_PANEL_STYLE.id),
        "android_oreo_style_card" to CardMeta("Android Oreo Style", CustomizeCategory.QS_DESIGN, Icons.Default.Style, FavoriteFeature.DESIGN_PANEL_STYLE.id),
        "qqs_icon_size_card" to CardMeta("QQS Icon Size", CustomizeCategory.QS_DESIGN, Icons.Default.FormatSize, FavoriteFeature.DESIGN_QQS_SIZE.id),
        "qqs_icon_quantity_card" to CardMeta("QQS Icon Quantity", CustomizeCategory.QS_DESIGN, Icons.Default.GridView, FavoriteFeature.DESIGN_QQS_QUANTITY.id),
        "qqs_complications_row_card" to CardMeta("QQS 2x1 Complications Row", CustomizeCategory.QS_DESIGN, Icons.Default.ViewAgenda, FavoriteFeature.DESIGN_QQS_2X1_COMPLICATIONS.id),
        "legacy_tile_shapes_support_card" to CardMeta("QS Tile Background Support", CustomizeCategory.QS_DESIGN, Icons.Default.CropSquare, FavoriteFeature.DESIGN_LEGACY_TILE_SHAPES.id),
        "tile_shape_selection_card" to CardMeta("Tile Shape Customization", CustomizeCategory.QS_DESIGN, Icons.Default.Star),
        "complication_arc_thickness_card" to CardMeta("Complication Arc Thickness", CustomizeCategory.QS_DESIGN, Icons.Default.LinearScale, FavoriteFeature.DESIGN_COMPLICATION_ARC_THICKNESS.id),
        "tile_outline_card" to CardMeta("QS Tile Outline", CustomizeCategory.QS_DESIGN, Icons.Default.BorderStyle, FavoriteFeature.DESIGN_TILE_STROKE.id),
        "qs_tile_gradient_card" to CardMeta("Tile Gradient", CustomizeCategory.QS_DESIGN, Icons.Default.Gradient, FavoriteFeature.DESIGN_TILE_GRADIENT.id),
        "tile_top_rim_card" to CardMeta("Tile & Complication Top Rim", CustomizeCategory.QS_DESIGN, Icons.Default.AutoAwesome, FavoriteFeature.DESIGN_TILE_TOP_RIM.id),
        "qs_page_indicator_style_card" to CardMeta("QS Page Indicator Style", CustomizeCategory.QS_DESIGN, Icons.Default.LinearScale, FavoriteFeature.DESIGN_PAGE_INDICATOR.id),
        "clear_all_fab_card" to CardMeta("Clear All FAB Button", CustomizeCategory.QS_DESIGN, Icons.Default.Clear, FavoriteFeature.DESIGN_CLEAR_ALL_FAB.id),
        "show_media_in_notif_container_card" to CardMeta("Music Player Notification", CustomizeCategory.QS_DESIGN, Icons.Default.Notifications, FavoriteFeature.DESIGN_MEDIA_STYLE.id),
        "compact_media_notification_card" to CardMeta("Compact Media Notification", CustomizeCategory.QS_DESIGN, Icons.Default.Notifications, FavoriteFeature.DESIGN_MEDIA_STYLE.id),
        "custom_header_image_card" to CardMeta("Custom Header Image", CustomizeCategory.QS_DESIGN, Icons.Default.Image, FavoriteFeature.DESIGN_HEADER_IMAGE.id),
        "custom_header_monet_gradient_card" to CardMeta("Header Monet Gradient", CustomizeCategory.QS_DESIGN, Icons.Default.AutoAwesome, FavoriteFeature.DESIGN_MONET_GRADIENT.id),
        "element_colors_preference_card" to CardMeta("Per-Element Accent Colors", CustomizeCategory.QS_DESIGN, Icons.Default.ColorLens, FavoriteFeature.DESIGN_ELEMENT_COLORS.id),
        "header_status_background_card" to CardMeta("Header & Status Backgrounds", CustomizeCategory.QS_DESIGN, Icons.Default.RoundedCorner, FavoriteFeature.DESIGN_HEADER_STATUS_BACKGROUNDS.id),
        "footer_icon_background_card" to CardMeta("Footer Icon Backgrounds", CustomizeCategory.QS_DESIGN, Icons.Default.CropSquare, FavoriteFeature.DESIGN_FOOTER_ICON_BACKGROUNDS.id),
        "frosted_glass_panel_card" to CardMeta("Frosted Glass Panel Backdrop", CustomizeCategory.QS_DESIGN, Icons.Default.AutoAwesome, FavoriteFeature.DESIGN_FROSTED_GLASS.id),
        "header_complication_tiles_card" to CardMeta("Complication QS Tiles", CustomizeCategory.QS_DESIGN, Icons.Default.Apps),
        "move_brightness_bottom_card" to CardMeta("Move Brightness to Bottom", CustomizeCategory.QS_DESIGN, Icons.Default.Tune, FavoriteFeature.DESIGN_BRIGHTNESS_BOTTOM.id),
        "brightness_in_qqs_card" to CardMeta("Brightness Slider in QQS", CustomizeCategory.QS_DESIGN, Icons.Default.Tune, FavoriteFeature.DESIGN_BRIGHTNESS_QQS.id),
        "brightness_gradient_card" to CardMeta("Brightness Bar Gradient", CustomizeCategory.QS_DESIGN, Icons.Default.Gradient, FavoriteFeature.DESIGN_BRIGHTNESS_GRADIENT.id),
        "show_brightness_controls_background_card" to CardMeta("Brightness Controls Background", CustomizeCategory.QS_DESIGN, Icons.Default.WbSunny, FavoriteFeature.DESIGN_BRIGHTNESS_CONTROLS_BG.id),
        "progress_bar_slider_style_card" to CardMeta("Progress Bar & Slider Style", CustomizeCategory.QS_DESIGN, Icons.Default.LinearScale, FavoriteFeature.DESIGN_PROGRESS_STYLE.id),
        "panel_corner_styling_card" to CardMeta("Panel Corners", CustomizeCategory.QS_DESIGN, Icons.Default.RoundedCorner, FavoriteFeature.DESIGN_PANEL_CORNERS.id),
        "panel_top_rim_card" to CardMeta("Panel Top Rim", CustomizeCategory.QS_DESIGN, Icons.Default.AutoAwesome, FavoriteFeature.DESIGN_PANEL_TOP_RIM.id),

        // QS TWEAKS
        "qs_grid_density_card" to CardMeta("QS Grid Layout Density", CustomizeCategory.QS_TWEAKS, Icons.Default.GridView, FavoriteFeature.TWEAKS_GRID_LAYOUT.id),
        "enable_qqs_horizontal_scroll_card" to CardMeta("QQS Horizontal Scroll", CustomizeCategory.QS_TWEAKS, Icons.Default.SyncAlt, FavoriteFeature.TWEAKS_QQS_HORIZONTAL_SCROLL.id),
        "qs_drag_handle_length_card" to CardMeta("Drag Handle Options", CustomizeCategory.QS_TWEAKS, Icons.Default.DragHandle, FavoriteFeature.TWEAKS_DRAG_HANDLE.id),
        "date_position_card" to CardMeta("Date Position in Header", CustomizeCategory.QS_TWEAKS, Icons.Default.Tune, FavoriteFeature.TWEAKS_DATE_POSITION.id),
        "edit_pencil_location_card" to CardMeta("Edit Pencil Icon Location", CustomizeCategory.QS_TWEAKS, Icons.Default.Tune, FavoriteFeature.TWEAKS_EDIT_POSITION.id),
        "move_collapse_to_qqs_footer_card" to CardMeta("Collapse Button in QQS Footer", CustomizeCategory.QS_TWEAKS, Icons.Default.UnfoldLess, FavoriteFeature.TWEAKS_COLLAPSE_QQS_FOOTER.id),
        "battery_percentage_location_card" to CardMeta("Battery Percentage Location", CustomizeCategory.QS_TWEAKS, Icons.Default.BatteryStd, FavoriteFeature.TWEAKS_BATTERY_PERCENT_LOCATION.id),
        "settings_icon_location_card" to CardMeta("Settings Icon Location", CustomizeCategory.QS_TWEAKS, Icons.Default.Settings, FavoriteFeature.TWEAKS_SETTINGS_ICON_LOCATION.id),
        "qs_tile_font_size_card" to CardMeta("Tile Label & Subtitle Font Size", CustomizeCategory.QS_TWEAKS, Icons.Default.FormatSize, FavoriteFeature.TWEAKS_FONT_SIZES.id),
        "hide_date_in_expanded_card" to CardMeta("Hide Date in Expanded QS", CustomizeCategory.QS_TWEAKS, Icons.Default.EventBusy, FavoriteFeature.TWEAKS_HIDE_DATE.id),
        "disable_labels_in_expanded_card" to CardMeta("Disable Labels in Expanded QS", CustomizeCategory.QS_TWEAKS, Icons.Default.VisibilityOff, FavoriteFeature.TWEAKS_DISABLE_LABELS.id),
        "hide_qqs_drag_handle_card" to CardMeta("Hide QQS Drag Handle", CustomizeCategory.QS_TWEAKS, Icons.Default.DragHandle, FavoriteFeature.TWEAKS_HIDE_QQS_DRAG.id),
        "hide_qqs_empty_notif_labels_card" to CardMeta("Hide Empty Notification Hint", CustomizeCategory.QS_TWEAKS, Icons.Default.VisibilityOff, FavoriteFeature.TWEAKS_HIDE_QQS_EMPTY_NOTIF.id),
        "hide_brightness_percentage_card" to CardMeta("Hide Brightness Percentage", CustomizeCategory.QS_TWEAKS, Icons.Default.BrightnessLow, FavoriteFeature.TWEAKS_HIDE_BRIGHTNESS_PERCENT.id),
        "hide_brightness_icon_card" to CardMeta("Hide Auto-Brightness Icon", CustomizeCategory.QS_TWEAKS, Icons.Default.WbSunny, FavoriteFeature.TWEAKS_HIDE_BRIGHTNESS_ICON.id),
        "show_brightness_plus_minus_card" to CardMeta("Show Brightness - / + Buttons", CustomizeCategory.QS_TWEAKS, Icons.Default.BrightnessHigh, FavoriteFeature.TWEAKS_BRIGHTNESS_PLUS_MINUS.id),
        "disable_clear_all_bg_card" to CardMeta("Disable Clear All Background", CustomizeCategory.QS_TWEAKS, Icons.Default.Clear, FavoriteFeature.TWEAKS_DISABLE_CLEAR_ALL_BG.id),
        "disable_clear_all_label_card" to CardMeta("Disable Clear All Label", CustomizeCategory.QS_TWEAKS, Icons.Default.Clear, FavoriteFeature.TWEAKS_DISABLE_CLEAR_ALL_LABEL.id),
        "show_carrier_card" to CardMeta("Show SIM Card Carrier", CustomizeCategory.QS_TWEAKS, Icons.Default.PhoneAndroid, FavoriteFeature.DESIGN_SIM_CARRIER.id),
        "carrier_divider_card" to CardMeta("Carrier Divider", CustomizeCategory.QS_TWEAKS, Icons.Default.LinearScale, FavoriteFeature.DESIGN_CARRIER_DIVIDER.id),
        "hide_carrier_in_expanded_card" to CardMeta("Hide Carrier in Expanded QS", CustomizeCategory.QS_TWEAKS, Icons.Default.VisibilityOff, FavoriteFeature.TWEAKS_HIDE_CARRIER_EXPANDED.id),
        "enable_panel_shadows_card" to CardMeta("Panel Depth Shadows", CustomizeCategory.QS_TWEAKS, Icons.Default.Layers, FavoriteFeature.TWEAKS_PANEL_SHADOWS.id),
        "clear_all_fab_offset_card" to CardMeta("Clear All FAB Offsets", CustomizeCategory.QS_TWEAKS, Icons.Default.Tune, FavoriteFeature.TWEAKS_CLEAR_ALL_FAB_OFFSETS.id),

        // THEMES
        "save_theme_card" to CardMeta("Save Current Settings", CustomizeCategory.THEMES, Icons.Default.Save, FavoriteFeature.THEMES_SAVE.id),
        "theme_transfer_card" to CardMeta("Import & Export Theme JSON", CustomizeCategory.THEMES, Icons.Default.SyncAlt, FavoriteFeature.THEMES_TRANSFER.id),
        "empty_themes_card" to CardMeta("No Saved Themes Card", CustomizeCategory.THEMES, Icons.Default.Palette),

        // MISCELLANEOUS
        "disable_panel_animations_card" to CardMeta("Disable Panel Animations", CustomizeCategory.MISCELLANEOUS, Icons.Default.Speed, FavoriteFeature.MISC_ANIMATION_SPEED.id),
        "prime_core_affinity_card" to CardMeta("Prime Core High Smoothness Mode", CustomizeCategory.MISCELLANEOUS, Icons.Default.Speed, FavoriteFeature.MISC_PRIME_CORE.id),
        "custom_volume_panel_card" to CardMeta("Hide QS/QQS on Volume Key Press", CustomizeCategory.MISCELLANEOUS, Icons.Default.VolumeUp, FavoriteFeature.MISC_VOLUME_PANEL.id),
        "invisible_status_bar_trigger_card" to CardMeta("Invisible Status Bar Trigger", CustomizeCategory.MISCELLANEOUS, Icons.Default.TouchApp, FavoriteFeature.MISC_STATUS_TRIGGER.id),
        "auto_system_panel_closing_card" to CardMeta("Use Auto System Panel Closing", CustomizeCategory.MISCELLANEOUS, Icons.Default.Close, FavoriteFeature.MISC_PANEL_AUTO_CLOSE.id),
        "panel_replacement_delay_card" to CardMeta("Panel Replacement Delay", CustomizeCategory.MISCELLANEOUS, Icons.Default.Timer, FavoriteFeature.MISC_REPLACEMENT_DELAY.id),
        "stock_shade_override_card" to CardMeta("Stock Shade Auto-Override", CustomizeCategory.MISCELLANEOUS, Icons.Default.AutoAwesome, FavoriteFeature.MISC_SYSTEM_OVERRIDE.id),
        "force_show_media_preview_card" to CardMeta("Force Show Preview Placeholder", CustomizeCategory.MISCELLANEOUS, Icons.Default.Image, FavoriteFeature.MISC_MEDIA_PREVIEW.id),
        "min_brightness_card" to CardMeta("Minimum Brightness", CustomizeCategory.MISCELLANEOUS, Icons.Default.BrightnessLow, FavoriteFeature.MISC_MIN_BRIGHTNESS.id),
        "max_brightness_card" to CardMeta("Maximum Brightness", CustomizeCategory.MISCELLANEOUS, Icons.Default.BrightnessHigh, FavoriteFeature.MISC_MAX_BRIGHTNESS.id),

        // ELEMENT COLORS
        "elem_color_surface_bg" to CardMeta("1. Surface Background", CustomizeCategory.QS_DESIGN, Icons.Default.ColorLens),
        "elem_color_panel_container" to CardMeta("2. Panel Containers & Notifications", CustomizeCategory.QS_DESIGN, Icons.Default.ColorLens),
        "elem_color_header_gradient" to CardMeta("3. Header Gradient", CustomizeCategory.QS_DESIGN, Icons.Default.ColorLens),
        "elem_color_qs_tiles" to CardMeta("4. Active QS Tiles", CustomizeCategory.QS_DESIGN, Icons.Default.ColorLens),
        "elem_color_inactive_tiles" to CardMeta("5. Inactive QS Tiles", CustomizeCategory.QS_DESIGN, Icons.Default.ColorLens),
        "elem_color_sliders" to CardMeta("6. Sliders / Progress Bars", CustomizeCategory.QS_DESIGN, Icons.Default.ColorLens),
        "elem_color_misc_elements" to CardMeta("7. Misc Elements", CustomizeCategory.QS_DESIGN, Icons.Default.ColorLens),
        "elem_color_clear_all" to CardMeta("8. Clear All Button / FAB", CustomizeCategory.QS_DESIGN, Icons.Default.ColorLens),
        "elem_color_active_comp_arc" to CardMeta("9. Active Complication Arcs", CustomizeCategory.QS_DESIGN, Icons.Default.ColorLens),
        "elem_color_inactive_comp_arc" to CardMeta("10. Inactive Complication Arcs", CustomizeCategory.QS_DESIGN, Icons.Default.ColorLens),
        "elem_color_comp_arc_track" to CardMeta("11. Complication Arc Tracks", CustomizeCategory.QS_DESIGN, Icons.Default.ColorLens),
        "disable_monet_notif_text_card" to CardMeta("Disable Monet for Notification Text", CustomizeCategory.QS_DESIGN, Icons.Default.ColorLens),

        // ABOUT
        "about_app_header_card" to CardMeta("QuickSettings App Info", CustomizeCategory.ABOUT, Icons.Default.Info, FavoriteFeature.ABOUT_INFO.id),
        "about_developer_card" to CardMeta("Developer Credits", CustomizeCategory.ABOUT, Icons.Default.Person, FavoriteFeature.ABOUT_DEVELOPER.id),
        "about_telegram_card" to CardMeta("Telegram Channel", CustomizeCategory.ABOUT, Icons.Default.Send, FavoriteFeature.ABOUT_TELEGRAM.id),
        "about_features_card" to CardMeta("Key Features Overview", CustomizeCategory.ABOUT, Icons.Default.Star, FavoriteFeature.ABOUT_KEY_FEATURES.id)
    )

    private val cardAliases: Map<String, List<String>> = mapOf(
        "panel_style_card" to listOf("android_oreo_style_card"),
        "android_oreo_style_card" to listOf("panel_style_card"),
        "show_media_in_notif_container_card" to listOf("compact_media_notification_card"),
        "compact_media_notification_card" to listOf("show_media_in_notif_container_card")
    )

    private val favoriteToPrimaryCardId: Map<String, String> = buildMap {
        cardMetadata.forEach { (cardId, meta) ->
            if (meta.linkedFavoriteId != null && cardId != "android_oreo_style_card" && cardId != "compact_media_notification_card") {
                put(meta.linkedFavoriteId, cardId)
            }
        }
    }

    private val multiSearchFeaturesToCard: Map<String, String> = mapOf(
        FavoriteFeature.DESIGN_ACTIVE_SHAPE.id to "tile_shape_selection_card",
        FavoriteFeature.DESIGN_INACTIVE_SHAPE.id to "tile_shape_selection_card",
        FavoriteFeature.DESIGN_COMPLICATION_ACTIVE_SHAPE.id to "tile_shape_selection_card",
        FavoriteFeature.DESIGN_COMPLICATION_INACTIVE_SHAPE.id to "tile_shape_selection_card"
    )

    fun isCategoryHidden(category: CustomizeCategory, hiddenIds: List<String>): Boolean {
        return hiddenIds.contains("cat_${category.name.lowercase()}")
    }

    fun isCardHidden(cardId: String, hiddenIds: List<String>): Boolean {
        if (hiddenIds.contains(cardId)) return true
        val aliases = cardAliases[cardId]
        if (aliases != null && aliases.any { hiddenIds.contains(it) }) return true
        if ((cardId == "empty_themes_card" || cardId.startsWith("saved_theme_")) &&
            hiddenIds.contains(FavoriteFeature.THEMES_SAVED_LIST.id)
        ) {
            return true
        }
        val linkedFav = cardMetadata[cardId]?.linkedFavoriteId
        return linkedFav != null && hiddenIds.contains(linkedFav)
    }

    fun isSearchFeatureHidden(feature: FavoriteFeature, hiddenIds: List<String>): Boolean {
        if (hiddenIds.contains(feature.id)) return true
        if (isCategoryHidden(feature.category, hiddenIds)) return true
        val directCard = favoriteToPrimaryCardId[feature.id]
        if (directCard != null && isCardHidden(directCard, hiddenIds)) return true
        val containerCard = multiSearchFeaturesToCard[feature.id]
        if (containerCard != null && isCardHidden(containerCard, hiddenIds)) return true
        if (feature.id == FavoriteFeature.DESIGN_RIM_SENSITIVITY.id &&
            isCardHidden("panel_top_rim_card", hiddenIds) &&
            isCardHidden("tile_top_rim_card", hiddenIds)
        ) {
            return true
        }
        return false
    }

    fun resolveTitle(id: String, fallbackTitle: String? = null): String {
        if (!fallbackTitle.isNullOrBlank()) return fallbackTitle
        return resolve(id).title
    }

    fun resolve(id: String): HiddenFeatureDescriptor {
        if (id.startsWith("cat_")) {
            val cat = CustomizeCategory.entries.firstOrNull { "cat_${it.name.lowercase()}" == id }
            if (cat != null) {
                return HiddenFeatureDescriptor(
                    id = id,
                    title = cat.title,
                    categoryTitle = "Category",
                    icon = cat.icon
                )
            }
        }

        if (id.startsWith("saved_theme_")) {
            val themeId = id.removePrefix("saved_theme_")
            val theme = ThemeManager.savedThemes.value.firstOrNull { it.id == themeId }
            return HiddenFeatureDescriptor(
                id = id,
                title = theme?.name ?: "Saved Theme",
                categoryTitle = CustomizeCategory.THEMES.title,
                icon = Icons.Default.Palette
            )
        }

        cardMetadata[id]?.let { meta ->
            return HiddenFeatureDescriptor(
                id = id,
                title = meta.title,
                categoryTitle = meta.category.title,
                icon = meta.icon
            )
        }

        FavoriteFeature.fromId(id)?.let { feat ->
            return HiddenFeatureDescriptor(
                id = id,
                title = feat.title,
                categoryTitle = feat.category.title,
                icon = feat.icon
            )
        }

        val prettyName = id
            .removePrefix("qs_")
            .removePrefix("heads_up_")
            .removeSuffix("_card")
            .replace("_", " ")
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
            .ifBlank { id }

        return HiddenFeatureDescriptor(
            id = id,
            title = prettyName,
            categoryTitle = "Feature",
            icon = Icons.Default.Tune
        )
    }
}

@Composable
fun HiddenFeaturesDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val hiddenIds by PanelPreferences.hiddenFeatures.collectAsState()
    val hiddenItems = remember(hiddenIds) {
        hiddenIds.map { HiddenFeatureCatalog.resolve(it) }
    }
    val colors = ExpressiveTheme.colors

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .testTag("hidden_features_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = colors.appBackground,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Hidden Features",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                            Text(
                                text = if (hiddenItems.isEmpty()) {
                                    "No features are currently hidden"
                                } else {
                                    "${hiddenItems.size} hidden • Tap Show to restore"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_hidden_features_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close hidden features dialog",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (hiddenItems.isEmpty()) {
                    Surface(
                        shape = CustomizeCardShapes.SingleCard,
                        color = colors.cardSurface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(colors.nestedElementSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "Everything is visible",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Long-press any category or option card and tap \"Hide\" in the Pin menu to hide unwanted features.",
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp)
                            .testTag("hidden_features_list"),
                        verticalArrangement = Arrangement.spacedBy(CustomizeCardShapes.CardSpacing)
                    ) {
                        itemsIndexed(hiddenItems, key = { _, item -> item.id }) { index, item ->
                            val shape = CustomizeCardShapes.getShapeForIndex(index, hiddenItems.size)
                            Surface(
                                shape = shape,
                                color = colors.cardSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("hidden_feature_item_${item.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(colors.nestedElementSurface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = item.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = colors.nestedElementSurface
                                        ) {
                                            Text(
                                                text = item.categoryTitle,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            PanelPreferences.unhideFeature(context, item.id)
                                            Toast.makeText(context, "${item.title} shown", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = CircleShape,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colors.buttonTertiarySubtle,
                                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                        ),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                        modifier = Modifier
                                            .defaultMinSize(minHeight = 48.dp)
                                            .testTag("unhide_feature_btn_${item.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = "Show ${item.title}",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Show",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.cardSurface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("done_hidden_features_btn")
                    ) {
                        Text(
                            text = "Close",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (hiddenItems.isNotEmpty()) {
                        Button(
                            onClick = {
                                PanelPreferences.unhideAllFeatures(context)
                                Toast.makeText(context, "All hidden features shown", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.buttonPrimary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .weight(1.3f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("unhide_all_features_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Show All (${hiddenItems.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AllSectionFeaturesHiddenCard(
    sectionTitle: String,
    modifier: Modifier = Modifier
) {
    var showHiddenDialog by remember { mutableStateOf(false) }
    val colors = ExpressiveTheme.colors

    SettingsCard(
        shape = CustomizeCardShapes.SingleCard,
        canPin = false,
        modifier = modifier
            .fillMaxWidth()
            .testTag("all_section_features_hidden_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colors.nestedElementSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "All $sectionTitle features are hidden",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Tap the button below to view and restore hidden features.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = { showHiddenDialog = true },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.buttonPrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("empty_section_show_hidden_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Show Hidden Features",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (showHiddenDialog) {
        HiddenFeaturesDialog(onDismiss = { showHiddenDialog = false })
    }
}
