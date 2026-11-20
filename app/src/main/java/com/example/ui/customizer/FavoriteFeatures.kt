package com.example.ui.customizer

import com.example.data.*
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BorderStyle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Gradient
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PanelPreferences
import com.example.ui.shape.QsShapeManager
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.SegmentedButtonShapes
import com.example.ui.theme.getSettingsScreenBackgroundColor

/**
 * Registry of user-customizable features eligible for favoriting, pinning,
 * and searching from the Customize screen.
 */
enum class FavoriteFeature(
    val id: String,
    val title: String,
    val category: CustomizeCategory,
    val description: String,
    val icon: ImageVector,
    val keywords: String = "",
    val isSearchable: Boolean = true
) {
    // =====================================================
    // BASIC SETTINGS
    // =====================================================
    BASIC_THEME_MODE(
        id = "basic_theme_mode",
        title = "App Theme Mode",
        category = CustomizeCategory.BASIC,
        description = "Switch app settings theme between System, Light, and Dark mode",
        icon = Icons.Default.Palette,
        keywords = "dark light system appearance monet"
    ),
    BASIC_COLLAPSE_OPTIONS(
        id = "basic_collapse_options",
        title = "Collapse & Expand Options",
        category = CustomizeCategory.BASIC,
        description = "Toggle option cards between full view and compact chips",
        icon = Icons.Default.UnfoldLess,
        keywords = "compact view fold cards"
    ),
    BASIC_COLLAPSE_SUBHEADERS(
        id = "basic_collapse_subheaders",
        title = "Collapse & Expand Subheaders",
        category = CustomizeCategory.BASIC,
        description = "Quick toggle to collapse or expand all list subheaders",
        icon = Icons.Default.MenuOpen,
        keywords = "sections headers accordion"
    ),
    BASIC_BACKUP_RESTORE(
        id = "basic_backup_restore",
        title = "Backup & Restore Configuration",
        category = CustomizeCategory.BASIC,
        description = "Export your Quick Settings configurations to JSON or import a backup",
        icon = Icons.Default.SyncAlt,
        keywords = "export import json settings backup restore file"
    ),

    // =====================================================
    // QUICK SETTINGS TWEAKS
    // =====================================================
    TWEAKS_GRID_LAYOUT(
        id = "tweaks_grid_layout",
        title = "QS Grid Layout Density",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Choose column and row dimensions (3x1 to 4x4) for expanded Quick Settings tiles",
        icon = Icons.Default.GridView,
        keywords = "columns rows 3x3 4x3 4x4 grid tiles"
    ),
    TWEAKS_QQS_HORIZONTAL_SCROLL(
        id = "tweaks_qqs_horizontal_scroll",
        title = "Horizontal Scroll QS Tiles",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Scroll Quick Settings tiles horizontally in compact and expanded views",
        icon = Icons.Default.SyncAlt,
        keywords = "carousel pager scroll qqs swipe"
    ),
    TWEAKS_QQS_TILES(
        id = "tweaks_qqs_tiles",
        title = "Quick-Quick Settings (QQS) Tile Count",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Configure compact top-row QQS tile count",
        icon = Icons.Default.ViewAgenda,
        keywords = "qqs row toggle count tiles",
        isSearchable = false
    ),
    TWEAKS_DISABLE_LABELS(
        id = "tweaks_disable_labels",
        title = "Disable Labels in Expanded QS",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Hide text labels in expanded QS and enlarge tiles automatically",
        icon = Icons.Default.VisibilityOff,
        keywords = "hide text tile labels compact icons"
    ),
    TWEAKS_HIDE_DATE(
        id = "tweaks_hide_date",
        title = "Hide Date in Expanded QS",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Hide date display when Quick Settings panel is fully expanded",
        icon = Icons.Default.EventBusy,
        keywords = "clock header date visibility"
    ),
    TWEAKS_DRAG_HANDLE(
        id = "tweaks_drag_handle",
        title = "Drag Handle Options",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Customize drag handle pill length and dimensions",
        icon = Icons.Default.DragHandle,
        keywords = "pill width handle bar"
    ),
    TWEAKS_FONT_SIZES(
        id = "tweaks_font_sizes",
        title = "Tile Label & Subtitle Font Size",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Fine-tune font scaling for Quick Settings tile labels and subtitles",
        icon = Icons.Default.FormatSize,
        keywords = "text size scale typography label subtitle"
    ),
    TWEAKS_VIBRATION(
        id = "tweaks_vibration",
        title = "Tile Click Vibration",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Haptic feedback when pressing Quick Settings tiles",
        icon = Icons.Default.Vibration,
        isSearchable = false
    ),
    TWEAKS_DATE_POSITION(
        id = "tweaks_date_position",
        title = "Date Position in Header",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Set date location in Quick Settings header (Next to Time, Below Time, Right, or Hidden)",
        icon = Icons.Default.Tune,
        keywords = "clock date placement header qqs"
    ),
    TWEAKS_EDIT_POSITION(
        id = "tweaks_edit_position",
        title = "Edit Pencil Icon Location",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Set edit tile button location (Footer, Header, Both, or Hidden)",
        icon = Icons.Default.Tune,
        keywords = "pencil edit tiles footer header"
    ),
    TWEAKS_COLLAPSE_QQS_FOOTER(
        id = "tweaks_collapse_qqs_footer",
        title = "Move Collapse Arrow to QQS Footer",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Relocate the panel collapse chevron into the QQS footer bar",
        icon = Icons.Default.UnfoldLess,
        keywords = "collapse button arrow qqs footer"
    ),
    TWEAKS_BATTERY_PERCENT_LOCATION(
        id = "tweaks_battery_percent_location",
        title = "Battery Percentage Location",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Show battery percentage in Header, QQS Right, or Hidden",
        icon = Icons.Default.BatteryStd,
        keywords = "battery level indicator percent header qqs"
    ),
    TWEAKS_SETTINGS_ICON_LOCATION(
        id = "tweaks_settings_icon_location",
        title = "Settings Icon Location",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Choose where the settings gear button appears in the panel",
        icon = Icons.Default.Settings,
        keywords = "gear shortcut header expanded carrier"
    ),
    TWEAKS_HIDE_QQS_DRAG(
        id = "tweaks_hide_qqs_drag",
        title = "Hide QQS Drag Handle",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Hide the drag pill handle in compact Quick-Quick Settings",
        icon = Icons.Default.DragHandle,
        keywords = "drag handle pill qqs hide"
    ),
    TWEAKS_HIDE_QQS_EMPTY_NOTIF(
        id = "tweaks_hide_qqs_empty_notif",
        title = "Hide Empty Notification Hint",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Hide the 'No notifications' placeholder label below QQS",
        icon = Icons.Default.VisibilityOff,
        keywords = "empty notifications placeholder hide qqs"
    ),
    TWEAKS_HIDE_BRIGHTNESS_PERCENT(
        id = "tweaks_hide_brightness_percent",
        title = "Hide Brightness Percentage",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Hide the numeric brightness percentage label next to the slider",
        icon = Icons.Default.BrightnessLow,
        keywords = "slider brightness label percentage hide"
    ),
    TWEAKS_HIDE_BRIGHTNESS_ICON(
        id = "tweaks_hide_brightness_icon",
        title = "Hide Auto-Brightness Icon",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Hide the auto-brightness sun icon next to the brightness bar",
        icon = Icons.Default.WbSunny,
        keywords = "auto brightness sun button hide"
    ),
    TWEAKS_BRIGHTNESS_PLUS_MINUS(
        id = "tweaks_brightness_plus_minus",
        title = "Show Brightness - / + Buttons",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Display quick minus and plus step buttons beside the brightness slider",
        icon = Icons.Default.BrightnessHigh,
        keywords = "minus plus step buttons brightness slider"
    ),
    TWEAKS_DISABLE_CLEAR_ALL_BG(
        id = "tweaks_disable_clear_all_bg",
        title = "Disable Clear All Background",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Remove the pill background container behind the Clear All button",
        icon = Icons.Default.Clear,
        keywords = "clear all notifications transparent background"
    ),
    TWEAKS_DISABLE_CLEAR_ALL_LABEL(
        id = "tweaks_disable_clear_all_label",
        title = "Disable Clear All Label & Icon Size",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Hide Clear All text for an icon-only button and adjust icon size",
        icon = Icons.Default.Clear,
        keywords = "clear all icon only text hide size"
    ),
    DESIGN_SIM_CARRIER(
        id = "design_sim_carrier",
        title = "SIM Card Carrier Display",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Show or hide carrier network label in panel header",
        icon = Icons.Default.PhoneAndroid,
        keywords = "sim network carrier operator name"
    ),
    DESIGN_CARRIER_DIVIDER(
        id = "design_carrier_divider",
        title = "Carrier Divider Line",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Divider or solid line separating carrier name from quick settings tiles",
        icon = Icons.Default.LinearScale,
        keywords = "carrier divider line separator"
    ),
    TWEAKS_HIDE_CARRIER_EXPANDED(
        id = "tweaks_hide_carrier_expanded",
        title = "Hide Carrier in Expanded QS",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Hide the SIM carrier name when expanded into full Quick Settings",
        icon = Icons.Default.VisibilityOff,
        keywords = "sim carrier expanded qs hide"
    ),
    TWEAKS_PANEL_SHADOWS(
        id = "tweaks_panel_shadows",
        title = "Panel Depth Shadows",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Enable custom drop shadows and adjust elevation, offset Y, and opacity",
        icon = Icons.Default.Layers,
        keywords = "shadow elevation depth opacity offset"
    ),
    TWEAKS_CLEAR_ALL_FAB_OFFSETS(
        id = "tweaks_clear_all_fab_offsets",
        title = "Clear All FAB Offsets & Size",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Fine-tune Clear All FAB anchor, horizontal/vertical offset, and diameter",
        icon = Icons.Default.Tune,
        keywords = "clear all fab offset x y diameter size"
    ),

    // =====================================================
    // QUICK SETTINGS DESIGN
    // =====================================================
    DESIGN_PANEL_STYLE(
        id = "design_panel_style",
        title = "Quick Settings Panel Style",
        category = CustomizeCategory.QS_DESIGN,
        description = "Switch between Modern Compose, Android 8.0 Oreo, 6.0 Marshmallow, or 4.4 KitKat styles",
        icon = Icons.Default.Style,
        keywords = "oreo marshmallow kitkat classic theme style layout"
    ),
    DESIGN_QQS_SIZE(
        id = "design_qqs_size",
        title = "QQS Icon & Tile Size",
        category = CustomizeCategory.QS_DESIGN,
        description = "Adjust the diameter of compact Quick-Quick Settings tile icons",
        icon = Icons.Default.FormatSize,
        keywords = "qqs tile size diameter scale"
    ),
    DESIGN_QQS_QUANTITY(
        id = "design_qqs_quantity",
        title = "QQS Icon Quantity",
        category = CustomizeCategory.QS_DESIGN,
        description = "Choose how many quick setting icons are displayed in compact QQS row",
        icon = Icons.Default.GridView,
        keywords = "qqs count quantity compact toggles"
    ),
    DESIGN_TILE_SIZE(
        id = "design_tile_size",
        title = "QS Tile Size",
        category = CustomizeCategory.QS_DESIGN,
        description = "Scale the visual diameter of quick setting tiles",
        icon = Icons.Default.AspectRatio,
        isSearchable = false
    ),
    DESIGN_QQS_2X1_COMPLICATIONS(
        id = "design_qqs_2x1_complications",
        title = "QQS 2x1 Complications Row",
        category = CustomizeCategory.QS_DESIGN,
        description = "Show an additional QQS row of 2x1 live complication tiles with progress lines",
        icon = Icons.Default.ViewAgenda,
        keywords = "complication 2x1 qqs corners height progress line"
    ),
    DESIGN_LEGACY_TILE_SHAPES(
        id = "design_legacy_tile_shapes",
        title = "QS Tile Background Support",
        category = CustomizeCategory.QS_DESIGN,
        description = "Enable custom active and inactive tile background shape engine",
        icon = Icons.Default.CropSquare,
        keywords = "tile background shape support legacy shapes"
    ),
    DESIGN_ACTIVE_SHAPE(
        id = "design_active_shape",
        title = "Active QS Tile Shape",
        category = CustomizeCategory.QS_DESIGN,
        description = "Shape applied to active and connected Quick Settings tiles",
        icon = Icons.Default.Star,
        keywords = "circle squircle leaf arch octagon stamp cloud enabled tile shape"
    ),
    DESIGN_INACTIVE_SHAPE(
        id = "design_inactive_shape",
        title = "Inactive QS Tile Shape",
        category = CustomizeCategory.QS_DESIGN,
        description = "Shape applied to inactive Quick Settings tiles",
        icon = Icons.Default.CropSquare,
        keywords = "circle squircle square disabled tile shape"
    ),
    DESIGN_COMPLICATION_ACTIVE_SHAPE(
        id = "design_complication_active_shape",
        title = "Active Complication Tile Shape",
        category = CustomizeCategory.QS_DESIGN,
        description = "Choose shape applied to active live complication tiles",
        icon = Icons.Default.Star,
        keywords = "complication shape active ring"
    ),
    DESIGN_COMPLICATION_INACTIVE_SHAPE(
        id = "design_complication_inactive_shape",
        title = "Inactive Complication Tile Shape",
        category = CustomizeCategory.QS_DESIGN,
        description = "Choose shape applied to inactive complication tiles",
        icon = Icons.Default.CropSquare,
        keywords = "complication shape inactive"
    ),
    DESIGN_COMPLICATION_ARC_THICKNESS(
        id = "design_complication_arc_thickness",
        title = "Complication Arc Thickness",
        category = CustomizeCategory.QS_DESIGN,
        description = "Adjust stroke width of perimeter progress arcs around complication tiles",
        icon = Icons.Default.LinearScale,
        keywords = "complication arc progress stroke thickness"
    ),
    DESIGN_TILE_STROKE(
        id = "design_tile_stroke",
        title = "Tile & Complication Stroke Outline",
        category = CustomizeCategory.QS_DESIGN,
        description = "Toggle and resize subtle border outlines for QS and complication tiles",
        icon = Icons.Default.BorderStyle,
        keywords = "outline border stroke thickness tiles"
    ),
    DESIGN_TILE_GRADIENT(
        id = "design_tile_gradient",
        title = "Monet Tile Gradient",
        category = CustomizeCategory.QS_DESIGN,
        description = "Apply dynamic dual-tone Monet gradient on active tiles",
        icon = Icons.Default.Gradient,
        keywords = "monet gradient active tiles dual tone"
    ),
    DESIGN_PAGE_INDICATOR(
        id = "design_page_indicator",
        title = "QS Page Indicator Style",
        category = CustomizeCategory.QS_DESIGN,
        description = "Choose Dots, Lines, Dots + Line, or Sliding Line page indicators",
        icon = Icons.Default.LinearScale,
        keywords = "page indicator dots lines sliding pages"
    ),
    DESIGN_CLEAR_ALL_FAB(
        id = "design_clear_all_fab",
        title = "Clear All Icon FAB Style",
        category = CustomizeCategory.QS_DESIGN,
        description = "Enable floating Clear All button and customize anchor, shape, and icon",
        icon = Icons.Default.Clear,
        keywords = "clear all fab floating action button icon shape position"
    ),
    DESIGN_MEDIA_STYLE(
        id = "design_media_style",
        title = "Media Player Notification Style",
        category = CustomizeCategory.QS_DESIGN,
        description = "Switch media player card between Android 11, Android 10, and Android 6 styles",
        icon = Icons.Default.Notifications,
        keywords = "media music player style android 11 10 6 compact"
    ),
    DESIGN_PROGRESS_STYLE(
        id = "design_progress_style",
        title = "Sliders & Progress Bar Style",
        category = CustomizeCategory.QS_DESIGN,
        description = "Choose M3, Straight, Wavy, or Android 12 progress sliders",
        icon = Icons.Default.LinearScale,
        keywords = "wavy squiggly m3 android 12 straight slider brightness media volume"
    ),
    DESIGN_PANEL_CORNERS(
        id = "design_panel_corners",
        title = "Panel Corner Radii",
        category = CustomizeCategory.QS_DESIGN,
        description = "Adjust outer and inner adjacent corner rounding of panel surfaces",
        icon = Icons.Default.RoundedCorner,
        keywords = "corner radius rounding outer inner panel"
    ),
    DESIGN_HEADER_IMAGE(
        id = "design_header_image",
        title = "Custom Header Image",
        category = CustomizeCategory.QS_DESIGN,
        description = "Display custom image artwork in expanded header and adjust height & opacity",
        icon = Icons.Default.Image,
        keywords = "header banner artwork photo height opacity"
    ),
    DESIGN_MONET_GRADIENT(
        id = "design_monet_gradient",
        title = "Header Monet Gradient & Surface",
        category = CustomizeCategory.QS_DESIGN,
        description = "Configure header Monet atmospheric gradient height and Solid Monet Surface",
        icon = Icons.Default.AutoAwesome,
        keywords = "monet header gradient solid surface background"
    ),
    DESIGN_ELEMENT_COLORS(
        id = "design_element_colors",
        title = "Per-Element Accent Colors",
        category = CustomizeCategory.QS_DESIGN,
        description = "Custom colors and Monet accents for tiles, sliders, icons, and backgrounds",
        icon = Icons.Default.ColorLens,
        keywords = "colors tint custom accent palette inactive active slider background"
    ),
    DESIGN_HEADER_STATUS_BACKGROUNDS(
        id = "design_header_status_backgrounds",
        title = "Header & Status Backgrounds",
        category = CustomizeCategory.QS_DESIGN,
        description = "Add rounded pill backgrounds and corner radii to Date, Battery, Clock, and Carrier",
        icon = Icons.Default.RoundedCorner,
        keywords = "date battery clock carrier pill background corner"
    ),
    DESIGN_FOOTER_ICON_BACKGROUNDS(
        id = "design_footer_icon_backgrounds",
        title = "Footer Icon Backgrounds",
        category = CustomizeCategory.QS_DESIGN,
        description = "Customize pill backgrounds, corner radii, and spacing for footer action icons",
        icon = Icons.Default.CropSquare,
        keywords = "footer buttons power settings edit collapse background spacing"
    ),
    DESIGN_BRIGHTNESS_BOTTOM(
        id = "design_brightness_bottom",
        title = "Move Brightness to Bottom",
        category = CustomizeCategory.QS_DESIGN,
        description = "Position brightness slider bar at bottom of Quick Settings panel",
        icon = Icons.Default.Tune,
        keywords = "brightness bar bottom position"
    ),
    DESIGN_BRIGHTNESS_QQS(
        id = "design_brightness_qqs",
        title = "Show Brightness Slider in QQS",
        category = CustomizeCategory.QS_DESIGN,
        description = "Display brightness control in compact Quick-Quick Settings row",
        icon = Icons.Default.Tune,
        keywords = "brightness bar qqs compact"
    ),
    DESIGN_BRIGHTNESS_GRADIENT(
        id = "design_brightness_gradient",
        title = "Brightness Bar Gradient",
        category = CustomizeCategory.QS_DESIGN,
        description = "Apply subtle dynamic gradient fill along the active brightness slider track",
        icon = Icons.Default.Gradient,
        keywords = "brightness slider gradient fill"
    ),
    DESIGN_BRIGHTNESS_CONTROLS_BG(
        id = "design_brightness_controls_bg",
        title = "Brightness Controls Background",
        category = CustomizeCategory.QS_DESIGN,
        description = "Match background containers on percentage, +/- step, and auto-brightness icons",
        icon = Icons.Default.WbSunny,
        keywords = "brightness controls background auto icon percentage plus minus contrast"
    ),

    // =====================================================
    // HEADS-UP NOTIFICATIONS
    // =====================================================
    HEADS_UP_MASTER(
        id = "heads_up_master",
        title = "Custom Heads-Up Overlay",
        category = CustomizeCategory.HEADS_UP,
        description = "Enable custom floating notification banner overlay engine",
        icon = Icons.Default.NotificationsActive,
        keywords = "heads up banner popup floating overlay"
    ),
    HEADS_UP_SYSTEM(
        id = "heads_up_system",
        title = "System Heads-Up Fallback",
        category = CustomizeCategory.HEADS_UP,
        description = "Allow standard Android system heads-up notifications alongside or instead of custom",
        icon = Icons.Default.Notifications,
        keywords = "system heads up native fallback"
    ),
    HEADS_UP_PERMISSION(
        id = "heads_up_permission",
        title = "Notification Listener Access",
        category = CustomizeCategory.HEADS_UP,
        description = "Check and grant Notification Access required for floating heads-up banners",
        icon = Icons.Default.NotificationsActive,
        keywords = "permission listener access heads up"
    ),
    HEADS_UP_TEST(
        id = "heads_up_test",
        title = "Test Heads-Up Triggers",
        category = CustomizeCategory.HEADS_UP,
        description = "Preview floating Notification, Alarm clock, and Incoming Call banners",
        icon = Icons.Default.Notifications,
        keywords = "test preview demo notification alarm call banner"
    ),
    HEADS_UP_TIMEOUT(
        id = "heads_up_timeout",
        title = "Auto-Dismiss Timeout",
        category = CustomizeCategory.HEADS_UP,
        description = "Adjust seconds before incoming heads-up banners automatically hide",
        icon = Icons.Default.Timer,
        keywords = "duration timeout hide dismiss seconds"
    ),
    HEADS_UP_GESTURES(
        id = "heads_up_gestures",
        title = "Heads-Up Swipe Actions",
        category = CustomizeCategory.HEADS_UP,
        description = "Configure actions for Swipe Up, Down, Left, and Right on heads-up banners",
        icon = Icons.Default.TouchApp,
        keywords = "swipe up down left right dismiss snooze open shade heads up"
    ),
    HEADS_UP_TAP_ACTIONS(
        id = "heads_up_tap_actions",
        title = "Heads-Up Tap Actions",
        category = CustomizeCategory.HEADS_UP,
        description = "Configure actions for Single Tap, Double Tap, and Long Press on banners",
        icon = Icons.Default.TouchApp,
        keywords = "single tap double tap long press expand snooze open heads up"
    ),
    HEADS_UP_SNOOZE_DURATION(
        id = "heads_up_snooze_duration",
        title = "Heads-Up Snooze Duration",
        category = CustomizeCategory.HEADS_UP,
        description = "Set how many minutes apps stay muted when snoozed via banner gestures",
        icon = Icons.Default.Timer,
        keywords = "snooze mute duration minutes clear snooze"
    ),
    HEADS_UP_POSITION(
        id = "heads_up_position",
        title = "Banner Display Position",
        category = CustomizeCategory.HEADS_UP,
        description = "Show heads-up banners at the top or bottom of the screen",
        icon = Icons.Default.VerticalAlignTop,
        isSearchable = false
    ),
    HEADS_UP_COMPACT(
        id = "heads_up_compact",
        title = "Compact Banner Mode",
        category = CustomizeCategory.HEADS_UP,
        description = "Single-line minimalist heads-up notification pill style",
        icon = Icons.Default.Compress,
        isSearchable = false
    ),
    HEADS_UP_BLACKLIST(
        id = "heads_up_blacklist",
        title = "Blacklisted Apps",
        category = CustomizeCategory.HEADS_UP,
        description = "Exclude specific apps from generating heads-up overlays",
        icon = Icons.Default.Block,
        isSearchable = false
    ),

    // =====================================================
    // HEADS-UP DESIGN
    // =====================================================
    HEADS_UP_DESIGN_TEST(
        id = "heads_up_design_test",
        title = "Heads-Up Appearance Preview",
        category = CustomizeCategory.HEADS_UP_DESIGN,
        description = "Trigger a live test banner to preview heads-up corner radius, elevation, and margins",
        icon = Icons.Default.Notifications,
        keywords = "preview test design banner appearance"
    ),
    HEADS_UP_DESIGN_EDGE_TO_EDGE(
        id = "heads_up_design_edge_to_edge",
        title = "Edge-to-Edge Banner",
        category = CustomizeCategory.HEADS_UP_DESIGN,
        description = "Stretch floating heads-up notification banner across full screen width",
        icon = Icons.Default.AspectRatio,
        keywords = "edge to edge full width banner design"
    ),
    HEADS_UP_DESIGN_CORNER_RADIUS(
        id = "heads_up_design_corner_radius",
        title = "Banner Corner Radius",
        category = CustomizeCategory.HEADS_UP_DESIGN,
        description = "Customize corner rounding of floating heads-up cards (0–24 dp)",
        icon = Icons.Default.RoundedCorner,
        keywords = "corner radius heads up banner roundness"
    ),
    HEADS_UP_DESIGN_ELEVATION(
        id = "heads_up_design_elevation",
        title = "Banner Elevation & Shadow",
        category = CustomizeCategory.HEADS_UP_DESIGN,
        description = "Adjust shadow depth and elevation of heads-up cards (0–24 dp)",
        icon = Icons.Default.Layers,
        keywords = "shadow depth elevation banner heads up"
    ),
    HEADS_UP_DESIGN_TOP_MARGIN(
        id = "heads_up_design_top_margin",
        title = "Banner Top Margin",
        category = CustomizeCategory.HEADS_UP_DESIGN,
        description = "Adjust top spacing distance below status bar for floating banners (0–24 dp)",
        icon = Icons.Default.VerticalAlignTop,
        keywords = "top margin spacing offset heads up"
    ),

    // =====================================================
    // THEMES
    // =====================================================
    THEMES_SAVE(
        id = "themes_save",
        title = "Save Current Theme Profile",
        category = CustomizeCategory.THEMES,
        description = "Capture a complete snapshot of all settings into a reusable theme",
        icon = Icons.Default.Save,
        keywords = "save snapshot profile preset"
    ),
    THEMES_TRANSFER(
        id = "themes_transfer",
        title = "Import & Export Theme JSON",
        category = CustomizeCategory.THEMES,
        description = "Import shared theme JSON strings or copy your current theme snapshot",
        icon = Icons.Default.SyncAlt,
        keywords = "import export json copy share theme"
    ),
    THEMES_SAVED_LIST(
        id = "themes_saved_list",
        title = "Saved Theme Profiles",
        category = CustomizeCategory.THEMES,
        description = "Apply or remove your custom saved theme snapshots",
        icon = Icons.Default.Bookmark,
        keywords = "apply delete list saved themes"
    ),

    // =====================================================
    // MISCELLANEOUS
    // =====================================================
    MISC_ANIMATION_SPEED(
        id = "misc_animation_speed",
        title = "Disable Panel Animations",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Disable Quick Settings expand and collapse animations for instant transitions",
        icon = Icons.Default.Speed,
        keywords = "disable animations instant open transition speed"
    ),
    MISC_ANIMATION_DURATION(
        id = "misc_animation_duration",
        title = "Animation Duration Scale",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Fine-tune transition timing curve and millisecond durations",
        icon = Icons.Default.Timer,
        isSearchable = false
    ),
    MISC_STATUS_BAR_GESTURES(
        id = "misc_status_bar_gestures",
        title = "Invisible Status Bar Trigger",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Swipe down anywhere on the status bar to expand panel",
        icon = Icons.Default.TouchApp,
        isSearchable = false
    ),
    MISC_HARDWARE_BUTTONS(
        id = "misc_hardware_buttons",
        title = "Hide QS/QQS on Volume Key Press",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Use volume keys or shortcuts to trigger the Quick Settings panel",
        icon = Icons.Default.VolumeUp,
        isSearchable = false
    ),
    MISC_LOCKSCREEN_BYPASS(
        id = "misc_lockscreen_bypass",
        title = "Lockscreen Bypass & Security",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Configure quick setting tile access on locked device screen",
        icon = Icons.Default.Block,
        isSearchable = false
    ),
    MISC_DUAL_TONE_STATUS(
        id = "misc_dual_tone_status",
        title = "Dual Tone Status Bar",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Blend status bar tint with panel background Monet shades",
        icon = Icons.Default.Palette,
        isSearchable = false
    ),
    MISC_QUICK_PULLDOWN(
        id = "misc_quick_pulldown",
        title = "Quick Pulldown",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Expand QS panel directly when pulling down from specified edge",
        icon = Icons.Default.VerticalAlignTop,
        isSearchable = false
    ),
    MISC_SMART_PULLDOWN(
        id = "misc_smart_pulldown",
        title = "Smart Pulldown",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Automatically expand panel when there are no active notifications",
        icon = Icons.Default.AutoAwesome,
        isSearchable = false
    ),
    MISC_PRIME_CORE(
        id = "misc_prime_core",
        title = "Prime Core High Smoothness Mode",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Pin UI animation threads to prime performance CPU core (requires Root)",
        icon = Icons.Default.Speed,
        keywords = "cpu prime core thread root smoothness"
    ),
    MISC_VOLUME_PANEL(
        id = "misc_volume_panel",
        title = "Hide QS/QQS on Volume Key Press",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Automatically dismiss Quick Settings panel when hardware volume keys are pressed",
        icon = Icons.Default.VolumeUp,
        keywords = "volume key button close hide panel"
    ),
    MISC_STATUS_TRIGGER(
        id = "misc_status_trigger",
        title = "Invisible Status Bar Trigger",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Touch overlay bar along status bar to swipe down Quick Settings",
        icon = Icons.Default.TouchApp,
        keywords = "status bar pulldown swipe trigger touch"
    ),
    MISC_PANEL_AUTO_CLOSE(
        id = "misc_panel_auto_close",
        title = "Use Auto System Panel Closing",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Automatically collapse stock system shade when QuickSettings opens",
        icon = Icons.Default.Close,
        keywords = "system shade close dismiss automatic"
    ),
    MISC_REPLACEMENT_DELAY(
        id = "misc_replacement_delay",
        title = "Panel Replacement Delay",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Delay milliseconds (0–1000 ms) before overriding stock system shade",
        icon = Icons.Default.Timer,
        keywords = "delay timing ms override system panel"
    ),
    MISC_SYSTEM_OVERRIDE(
        id = "misc_system_override",
        title = "Stock Shade Auto-Override",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Intercept system status bar pulldowns with Shade Swipe Only option",
        icon = Icons.Default.AutoAwesome,
        keywords = "override intercept shade swipe accessibility detection"
    ),
    MISC_COOLDOWN(
        id = "misc_cooldown",
        title = "Detector Cooldown",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Cooldown delay between consecutive pulldown interceptions (10–1000 ms)",
        icon = Icons.Default.Timer,
        keywords = "cooldown throttle debounce trigger ms"
    ),
    MISC_MEDIA_PREVIEW(
        id = "misc_media_preview",
        title = "Force Media Player Preview",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Always display sample media playback card inside Quick Settings",
        icon = Icons.Default.Image,
        keywords = "media player music preview demo"
    ),
    MISC_MIN_BRIGHTNESS(
        id = "misc_min_brightness",
        title = "Minimum Hardware Brightness",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Calibrate the lowest hardware brightness value (1–2047)",
        icon = Icons.Default.BrightnessLow,
        keywords = "brightness calibrate min low dim slider range"
    ),
    MISC_MAX_BRIGHTNESS(
        id = "misc_max_brightness",
        title = "Maximum Hardware Brightness",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Calibrate the peak hardware brightness scale (255, 511, 1023, 2047)",
        icon = Icons.Default.BrightnessHigh,
        keywords = "brightness calibrate max high peak 255 1023 2047 slider range"
    ),

    // Hidden / Internal Category Aliases
    GESTURE_VOLUME_KEY(
        id = "gesture_volume_key",
        title = "Volume Key Gesture Action",
        category = CustomizeCategory.GESTURES,
        description = "Hardware volume button gesture panel dismiss toggle",
        icon = Icons.Default.VolumeUp,
        isSearchable = false
    ),
    TRIGGER_STATUS_BAR(
        id = "trigger_status_bar",
        title = "Invisible Status Bar Trigger",
        category = CustomizeCategory.TRIGGER,
        description = "Invisible touch bar at top of screen for opening panel",
        icon = Icons.Default.TouchApp,
        isSearchable = false
    ),
    TRIGGER_IMMERSIVE(
        id = "trigger_immersive",
        title = "Force Immersive Trigger",
        category = CustomizeCategory.TRIGGER,
        description = "Enable trigger overlay even in full-screen games and apps",
        icon = Icons.Default.AspectRatio,
        isSearchable = false
    ),
    TRIGGER_COOLDOWN(
        id = "trigger_cooldown",
        title = "Detector Cooldown",
        category = CustomizeCategory.TRIGGER,
        description = "Minimum milliseconds between pulldown gesture detections",
        icon = Icons.Default.Timer,
        isSearchable = false
    ),
    TRIGGER_DELAY(
        id = "trigger_delay",
        title = "Panel Replacement Delay",
        category = CustomizeCategory.TRIGGER,
        description = "Time delay before replacing system quick settings",
        icon = Icons.Default.Timer,
        isSearchable = false
    ),

    // =====================================================
    // ABOUT
    // =====================================================
    ABOUT_INFO(
        id = "about_info",
        title = "QuickSettings App Info",
        category = CustomizeCategory.ABOUT,
        description = "Version 2.0.1 — control center architecture and overview",
        icon = Icons.Default.Info,
        keywords = "version 2.0.1 about app info"
    ),
    ABOUT_DEVELOPER(
        id = "about_developer",
        title = "Developer Credits",
        category = CustomizeCategory.ABOUT,
        description = "Maxim Tarna — Creator & Lead Developer of QuickSettings",
        icon = Icons.Default.Person,
        keywords = "author creator maxim tarna developer"
    ),
    ABOUT_TELEGRAM(
        id = "about_telegram",
        title = "Telegram Channel",
        category = CustomizeCategory.ABOUT,
        description = "Join t.me/allphonecustomization for custom themes, guides, and updates",
        icon = Icons.Default.Send,
        keywords = "community link url updates support telegram"
    ),
    ABOUT_KEY_FEATURES(
        id = "about_key_features",
        title = "Key Features Overview",
        category = CustomizeCategory.ABOUT,
        description = "Summary of shapes, Monet theming, interactive sliders, and heads-up engine",
        icon = Icons.Default.Build,
        keywords = "highlights capabilities overview"
    ),
    ABOUT_PERMISSIONS(
        id = "about_permissions",
        title = "Permission Status",
        category = CustomizeCategory.ABOUT,
        description = "Accessibility and overlay permission statuses",
        icon = Icons.Default.Block,
        isSearchable = false
    );

    companion object {
        fun fromId(id: String): FavoriteFeature? = entries.firstOrNull { it.id == id }
        fun getByCategory(category: CustomizeCategory): List<FavoriteFeature> =
            entries.filter { it.category == category && it.isSearchable }
    }
}

/**
 * Settings card placed under Customize → Basic Settings.
 * Displays the current count of pinned favorites and allows opening the manager dialog.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FavoriteFeaturesSettingsCard(
    onOpenManager: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CustomizeCardShapes.SingleCard
) {
    val pinnedIds by PanelPreferences.pinnedFeatures.collectAsState()
    val pinnedFeatures = remember(pinnedIds) {
        pinnedIds.mapNotNull { FavoriteFeature.fromId(it) }
    }

    SettingsCard(
        shape = shape,
        canPin = false,
        modifier = modifier.testTag("favorite_features_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Favorite Features",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 18.sp,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Choose favorite features to pin at the top of their respective customization sections for instant access.",
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Summary of pinned items
            if (pinnedFeatures.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    pinnedFeatures.take(5).forEach { feat ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = feat.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = feat.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if (pinnedFeatures.size > 5) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "+${pinnedFeatures.size - 5} more",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onOpenManager,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manage_favorites_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (pinnedFeatures.isEmpty()) "Choose Favorite Features" else "Manage Favorites (${pinnedFeatures.size} pinned)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Interactive dialog to browse, search, and toggle pinned favorite features.
 */
@Composable
fun ManageFavoriteFeaturesDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val pinnedIds by PanelPreferences.pinnedFeatures.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<CustomizeCategory?>(null) }

    val categories = remember {
        listOf(
            null to "All",
            CustomizeCategory.QS_TWEAKS to "QS Tweaks",
            CustomizeCategory.QS_DESIGN to "QS Design",
            CustomizeCategory.HEADS_UP to "Heads-Up",
            CustomizeCategory.HEADS_UP_DESIGN to "Heads-Up Design",
            CustomizeCategory.BASIC to "Basic",
            CustomizeCategory.THEMES to "Themes",
            CustomizeCategory.MISCELLANEOUS to "Misc",
            CustomizeCategory.ABOUT to "About"
        )
    }

    val filteredFeatures = remember(searchQuery, selectedCategoryFilter) {
        val tokens = searchQuery.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        FavoriteFeature.entries.filter { feat ->
            feat.isSearchable &&
                (selectedCategoryFilter == null || feat.category == selectedCategoryFilter) &&
                (tokens.isEmpty() || tokens.all { token ->
                    feat.title.contains(token, ignoreCase = true) ||
                        feat.description.contains(token, ignoreCase = true) ||
                        feat.category.title.contains(token, ignoreCase = true) ||
                        feat.keywords.contains(token, ignoreCase = true)
                })
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "Choose Favorite Features",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${pinnedIds.size} feature${if (pinnedIds.size == 1) "" else "s"} pinned to section tops",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search settings & features...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_features_input")
                )

                // Category filter chips row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categories.forEach { (cat, label) ->
                        val isSelected = selectedCategoryFilter == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(label, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp),
                            border = null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }

                // Features list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filteredFeatures.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No matching features found",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(filteredFeatures, key = { it.id }) { feat ->
                            val isPinned = pinnedIds.contains(feat.id)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isPinned) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        PanelPreferences.toggleFeaturePinned(context, feat.id)
                                    }
                                    .testTag("feature_row_${feat.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isPinned) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = feat.icon,
                                            contentDescription = null,
                                            tint = if (isPinned) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = feat.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                            ) {
                                                Text(
                                                    text = feat.category.title,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = feat.description,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    IconButton(
                                        onClick = { PanelPreferences.toggleFeaturePinned(context, feat.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPinned) Icons.Default.Star else Icons.Default.StarOutline,
                                            contentDescription = if (isPinned) "Unpin feature" else "Pin feature",
                                            tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.testTag("done_favorites_btn")
            ) {
                Text("Done")
            }
        },
        dismissButton = {
            if (pinnedIds.isNotEmpty()) {
                TextButton(
                    onClick = {
                        PanelPreferences.setPinnedFeatures(context, emptyList())
                    },
                    modifier = Modifier.testTag("clear_all_favorites_btn")
                ) {
                    Text("Clear All")
                }
            }
        }
    )
}

/**
 * Pinned subheader displayed at the very top of each section when one or more
 * features in that section have been favorited by the user.
 */
@Composable
fun PinnedSectionHeader(
    pinnedCount: Int,
    onManageClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = "Pinned Features",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = pinnedCount.toString(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        if (onManageClick != null) {
            TextButton(
                onClick = onManageClick,
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text(
                    text = "Manage",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Pin toggle button - deprecated in favor of holding option for dropdown menu.
 * Retained as empty composable for binary/code compatibility.
 */
@Composable
fun CardPinButton(
    featureId: String,
    modifier: Modifier = Modifier
) {
    // Pin buttons removed in favor of holding option to reveal drop-down with Pin button
}

/**
 * Clean card representation of a pinned feature at the top of its section.
 * Holding this card opens a drop-down menu with an "Unpin" button.
 */
@Composable
fun GenericPinnedFeatureCard(
    feature: FavoriteFeature,
    modifier: Modifier = Modifier,
    shape: Shape = CustomizeCardShapes.SingleCard,
    showPinnedBadge: Boolean = true,
    actionContent: (@Composable () -> Unit)? = null
) {
    SettingsCard(
        shape = shape,
        modifier = modifier.then(
            if (showPinnedBadge) Modifier.testTag("pinned_feature_${feature.id}") else Modifier
        ),
        featureId = feature.id
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = feature.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = feature.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (showPinnedBadge) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = "PINNED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = feature.description,
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (actionContent != null) {
                actionContent()
            } else {
                FavoriteFeatureActionContent(feature = feature)
            }
        }
    }
}

@Composable
fun FavoriteFeatureActionContent(
    feature: FavoriteFeature,
    modifier: Modifier = Modifier
) {
    when (feature.category) {
        CustomizeCategory.BASIC -> RenderBasicFeatureActions(feature = feature, modifier = modifier)
        CustomizeCategory.HEADS_UP,
        CustomizeCategory.HEADS_UP_DESIGN -> RenderHeadsUpFeatureActions(feature = feature, modifier = modifier)
        CustomizeCategory.QS_DESIGN -> RenderQsDesignFeatureActions(feature = feature, modifier = modifier)
        CustomizeCategory.QS_TWEAKS -> RenderQsTweaksFeatureActions(feature = feature, modifier = modifier)
        CustomizeCategory.THEMES,
        CustomizeCategory.MISCELLANEOUS,
        CustomizeCategory.ABOUT -> RenderThemesAndMiscAndAboutActions(feature = feature, modifier = modifier)
        else -> Unit
    }
}



