package com.example.ui.customizer

import com.example.data.*
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Gradient
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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

/**
 * Registry of user-customizable features eligible for favoriting and pinning
 * to the top of their respective customization sections.
 */
enum class FavoriteFeature(
    val id: String,
    val title: String,
    val category: CustomizeCategory,
    val description: String,
    val icon: ImageVector
) {
    // Basic Settings
    BASIC_THEME_MODE(
        id = "basic_theme_mode",
        title = "App Theme Mode",
        category = CustomizeCategory.BASIC,
        description = "Switch settings theme between System, Light, and Dark mode",
        icon = Icons.Default.Palette
    ),
    BASIC_COLLAPSE_OPTIONS(
        id = "basic_collapse_options",
        title = "Collapse & Expand Options",
        category = CustomizeCategory.BASIC,
        description = "Toggle option cards between full view and compact chips",
        icon = Icons.Default.UnfoldLess
    ),
    BASIC_COLLAPSE_SUBHEADERS(
        id = "basic_collapse_subheaders",
        title = "Collapse & Expand Subheaders",
        category = CustomizeCategory.BASIC,
        description = "Quick toggle to collapse or expand all list subheaders",
        icon = Icons.Default.MenuOpen
    ),
    BASIC_BACKUP_RESTORE(
        id = "basic_backup_restore",
        title = "Configuration Backup",
        category = CustomizeCategory.BASIC,
        description = "Backup your Quick Settings configurations to JSON or restore",
        icon = Icons.Default.SyncAlt
    ),

    // Quick Settings Tweaks
    TWEAKS_GRID_LAYOUT(
        id = "tweaks_grid_layout",
        title = "QS Grid Layout Density",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Choose column and row dimensions for expanded Quick Settings tiles",
        icon = Icons.Default.GridView
    ),
    TWEAKS_DISABLE_LABELS(
        id = "tweaks_disable_labels",
        title = "Disable Labels in Expanded QS",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Hide text labels in expanded QS and enlarge tiles automatically",
        icon = Icons.Default.VisibilityOff
    ),
    TWEAKS_HIDE_DATE(
        id = "tweaks_hide_date",
        title = "Hide Date in Expanded QS",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Hide date display when Quick Settings panel is fully expanded",
        icon = Icons.Default.EventBusy
    ),
    TWEAKS_DRAG_HANDLE(
        id = "tweaks_drag_handle",
        title = "Drag Handle Options",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Customize drag handle pill length, offset, and visibility",
        icon = Icons.Default.DragHandle
    ),
    TWEAKS_FONT_SIZES(
        id = "tweaks_font_sizes",
        title = "Tile Label & Subtitle Font Size",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Fine-tune font scaling for Quick Settings labels and subtitles",
        icon = Icons.Default.FormatSize
    ),
    TWEAKS_QQS_TILES(
        id = "tweaks_qqs_tiles",
        title = "Quick-Quick Settings (QQS) Tiles",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Configure top-row tile count and dimensions",
        icon = Icons.Default.ViewAgenda
    ),
    TWEAKS_VIBRATION(
        id = "tweaks_vibration",
        title = "Tile Click Vibration",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Haptic feedback when pressing Quick Settings tiles",
        icon = Icons.Default.Vibration
    ),
    TWEAKS_DATE_POSITION(
        id = "tweaks_date_position",
        title = "Date Position in Header",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Set date location in Quick Settings header (Left, Center, or Right)",
        icon = Icons.Default.Tune
    ),
    TWEAKS_EDIT_POSITION(
        id = "tweaks_edit_position",
        title = "Edit Pencil Icon Location",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Set edit icon location (Top Bar or Bottom Row)",
        icon = Icons.Default.Tune
    ),
    TWEAKS_HIDE_QQS_DRAG(
        id = "tweaks_hide_qqs_drag",
        title = "Hide QQS Drag Handle",
        category = CustomizeCategory.QS_TWEAKS,
        description = "Hide the drag pill handle in compact Quick-Quick Settings",
        icon = Icons.Default.DragHandle
    ),

    // Quick Settings Design
    DESIGN_QQS_SIZE(
        id = "design_qqs_size",
        title = "QQS Icon Size",
        category = CustomizeCategory.QS_DESIGN,
        description = "Adjust the size of the circular icons in the compact QQS panel",
        icon = Icons.Default.FormatSize
    ),
    DESIGN_QQS_QUANTITY(
        id = "design_qqs_quantity",
        title = "QQS Icon Quantity",
        category = CustomizeCategory.QS_DESIGN,
        description = "Choose how many quick setting icons are displayed in compact QQS row",
        icon = Icons.Default.GridView
    ),
    DESIGN_ACTIVE_SHAPE(
        id = "design_active_shape",
        title = "Active QS Tile Shape",
        category = CustomizeCategory.QS_DESIGN,
        description = "Shape applied to active and connected Quick Settings tiles",
        icon = Icons.Default.Star
    ),
    DESIGN_INACTIVE_SHAPE(
        id = "design_inactive_shape",
        title = "Inactive QS Tile Shape",
        category = CustomizeCategory.QS_DESIGN,
        description = "Shape applied to inactive Quick Settings tiles",
        icon = Icons.Default.CropSquare
    ),
    DESIGN_TILE_SIZE(
        id = "design_tile_size",
        title = "QS Tile Size",
        category = CustomizeCategory.QS_DESIGN,
        description = "Scale the visual diameter of quick setting tiles",
        icon = Icons.Default.AspectRatio
    ),
    DESIGN_TILE_STROKE(
        id = "design_tile_stroke",
        title = "Tile Stroke / Outline",
        category = CustomizeCategory.QS_DESIGN,
        description = "Toggle subtle border outlines for tiles",
        icon = Icons.Default.BorderStyle
    ),
    DESIGN_TILE_GRADIENT(
        id = "design_tile_gradient",
        title = "Monet Tile Gradient",
        category = CustomizeCategory.QS_DESIGN,
        description = "Apply dynamic dual-tone Monet gradient on active tiles",
        icon = Icons.Default.Gradient
    ),
    DESIGN_PROGRESS_STYLE(
        id = "design_progress_style",
        title = "Sliders & Progress Bar Style",
        category = CustomizeCategory.QS_DESIGN,
        description = "Choose Wavy, Android 12, Straight, or Default progress sliders",
        icon = Icons.Default.LinearScale
    ),
    DESIGN_PANEL_CORNERS(
        id = "design_panel_corners",
        title = "Panel Corner Radii",
        category = CustomizeCategory.QS_DESIGN,
        description = "Adjust outer and inner corner rounding of the panel surface",
        icon = Icons.Default.RoundedCorner
    ),
    DESIGN_HEADER_IMAGE(
        id = "design_header_image",
        title = "Custom Header Image",
        category = CustomizeCategory.QS_DESIGN,
        description = "Display custom image artwork or banner in expanded panel header",
        icon = Icons.Default.Image
    ),
    DESIGN_MONET_GRADIENT(
        id = "design_monet_gradient",
        title = "Header Monet Gradient",
        category = CustomizeCategory.QS_DESIGN,
        description = "Atmospheric Monet color gradient background at the panel top",
        icon = Icons.Default.AutoAwesome
    ),
    DESIGN_ELEMENT_COLORS(
        id = "design_element_colors",
        title = "Per-Element Monet Accent Colors",
        category = CustomizeCategory.QS_DESIGN,
        description = "Detailed Hue, Saturation & Brightness slider coloring for panel elements",
        icon = Icons.Default.ColorLens
    ),

    // Heads-Up
    HEADS_UP_MASTER(
        id = "heads_up_master",
        title = "Custom Heads-Up Overlay",
        category = CustomizeCategory.HEADS_UP,
        description = "Enable floating notification banner overlay engine",
        icon = Icons.Default.NotificationsActive
    ),
    HEADS_UP_TEST(
        id = "heads_up_test",
        title = "Independent Preview & Test",
        category = CustomizeCategory.HEADS_UP,
        description = "Inflates lightweight Compose layout preview at top of screen",
        icon = Icons.Default.Notifications
    ),
    HEADS_UP_TIMEOUT(
        id = "heads_up_timeout",
        title = "Auto-Dismiss Timeout",
        category = CustomizeCategory.HEADS_UP,
        description = "Adjust seconds before incoming heads-up banners automatically hide",
        icon = Icons.Default.Timer
    ),
    HEADS_UP_POSITION(
        id = "heads_up_position",
        title = "Banner Display Position",
        category = CustomizeCategory.HEADS_UP,
        description = "Show heads-up banners at the top or bottom of the screen",
        icon = Icons.Default.VerticalAlignTop
    ),
    HEADS_UP_COMPACT(
        id = "heads_up_compact",
        title = "Compact Banner Mode",
        category = CustomizeCategory.HEADS_UP,
        description = "Single-line minimalist heads-up notification pill style",
        icon = Icons.Default.Compress
    ),
    HEADS_UP_GESTURES(
        id = "heads_up_gestures",
        title = "Gesture Hints",
        category = CustomizeCategory.HEADS_UP,
        description = "Swipe left or right on floating banners to dismiss or mute",
        icon = Icons.Default.TouchApp
    ),
    HEADS_UP_BLACKLIST(
        id = "heads_up_blacklist",
        title = "Blacklisted Apps",
        category = CustomizeCategory.HEADS_UP,
        description = "Exclude specific apps from generating heads-up overlays",
        icon = Icons.Default.Block
    ),

    // Themes
    THEMES_SAVE(
        id = "themes_save",
        title = "Save Current Theme Profile",
        category = CustomizeCategory.THEMES,
        description = "Capture a complete snapshot of all settings into a user theme",
        icon = Icons.Default.Save
    ),
    THEMES_SAVED_LIST(
        id = "themes_saved_list",
        title = "Saved Theme Profiles",
        category = CustomizeCategory.THEMES,
        description = "Manage, apply, update, and export custom saved themes",
        icon = Icons.Default.Bookmark
    ),

    // Miscellaneous
    MISC_ANIMATION_SPEED(
        id = "misc_animation_speed",
        title = "Disable Panel Animations",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Adjust opening, expanding, and closing animation speeds",
        icon = Icons.Default.Speed
    ),
    MISC_ANIMATION_DURATION(
        id = "misc_animation_duration",
        title = "Animation Duration Scale",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Fine-tune transition timing curve and millisecond durations",
        icon = Icons.Default.Timer
    ),
    MISC_STATUS_BAR_GESTURES(
        id = "misc_status_bar_gestures",
        title = "Invisible Status Bar Trigger",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Swipe down anywhere on the status bar to expand panel",
        icon = Icons.Default.TouchApp
    ),
    MISC_HARDWARE_BUTTONS(
        id = "misc_hardware_buttons",
        title = "Hide QS/QQS on Volume Key Press",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Use volume keys or shortcuts to trigger the Quick Settings panel",
        icon = Icons.Default.VolumeUp
    ),
    MISC_LOCKSCREEN_BYPASS(
        id = "misc_lockscreen_bypass",
        title = "Lockscreen Bypass & Security",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Configure quick setting tile access on locked device screen",
        icon = Icons.Default.Block
    ),
    MISC_DUAL_TONE_STATUS(
        id = "misc_dual_tone_status",
        title = "Dual Tone Status Bar",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Blend status bar tint with panel background Monet shades",
        icon = Icons.Default.Palette
    ),
    MISC_QUICK_PULLDOWN(
        id = "misc_quick_pulldown",
        title = "Quick Pulldown",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Expand QS panel directly when pulling down from specified edge",
        icon = Icons.Default.VerticalAlignTop
    ),
    MISC_SMART_PULLDOWN(
        id = "misc_smart_pulldown",
        title = "Smart Pulldown",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Automatically expand panel when there are no active notifications",
        icon = Icons.Default.AutoAwesome
    ),

    // Additional Quick Settings Design
    DESIGN_SIM_CARRIER(
        id = "design_sim_carrier",
        title = "SIM Card Carrier Display",
        category = CustomizeCategory.QS_DESIGN,
        description = "Show or hide carrier label name in panel header",
        icon = Icons.Default.PhoneAndroid
    ),
    DESIGN_CARRIER_DIVIDER(
        id = "design_carrier_divider",
        title = "Carrier Divider Line",
        category = CustomizeCategory.QS_DESIGN,
        description = "Divider separating carrier name from quick settings tiles",
        icon = Icons.Default.LinearScale
    ),
    DESIGN_BRIGHTNESS_BOTTOM(
        id = "design_brightness_bottom",
        title = "Move Brightness to Bottom",
        category = CustomizeCategory.QS_DESIGN,
        description = "Position brightness slider bar at bottom of Quick Settings panel",
        icon = Icons.Default.Tune
    ),
    DESIGN_BRIGHTNESS_QQS(
        id = "design_brightness_qqs",
        title = "Show Brightness Slider in QQS",
        category = CustomizeCategory.QS_DESIGN,
        description = "Display brightness control in compact Quick-Quick Settings row",
        icon = Icons.Default.Tune
    ),

    // Additional Miscellaneous
    MISC_PRIME_CORE(
        id = "misc_prime_core",
        title = "Prime Core High Smoothness Mode",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Pin UI animation threads to prime performance core",
        icon = Icons.Default.Speed
    ),
    MISC_VOLUME_PANEL(
        id = "misc_volume_panel",
        title = "Hide QS on Volume Key Press",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Automatically close panel when pressing volume buttons",
        icon = Icons.Default.VolumeUp
    ),
    MISC_STATUS_TRIGGER(
        id = "misc_status_trigger",
        title = "Invisible Status Bar Trigger",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Tap status bar corner to expand Quick Settings panel",
        icon = Icons.Default.TouchApp
    ),
    MISC_PANEL_AUTO_CLOSE(
        id = "misc_panel_auto_close",
        title = "Use auto system panel closing",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Automatically dismiss stock system shade when custom panel opens",
        icon = Icons.Default.Close
    ),
    MISC_REPLACEMENT_DELAY(
        id = "misc_replacement_delay",
        title = "Panel Replacement Delay",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Delay milliseconds before overriding system shade",
        icon = Icons.Default.Timer
    ),
    MISC_SYSTEM_OVERRIDE(
        id = "misc_system_override",
        title = "Stock Shade Auto-Override",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Full system status bar pulldown interception service",
        icon = Icons.Default.AutoAwesome
    ),
    MISC_COOLDOWN(
        id = "misc_cooldown",
        title = "Detector Cooldown",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Cooldown timer between status bar pulldown triggers",
        icon = Icons.Default.Timer
    ),
    MISC_MEDIA_PREVIEW(
        id = "misc_media_preview",
        title = "Force Media Player Preview",
        category = CustomizeCategory.MISCELLANEOUS,
        description = "Always display media playback widget in panel",
        icon = Icons.Default.Image
    ),

    // Gestures
    GESTURE_VOLUME_KEY(
        id = "gesture_volume_key",
        title = "Volume Key Gesture Action",
        category = CustomizeCategory.GESTURES,
        description = "Hardware volume button gesture panel dismiss toggle",
        icon = Icons.Default.VolumeUp
    ),

    // Trigger
    TRIGGER_STATUS_BAR(
        id = "trigger_status_bar",
        title = "Invisible Status Bar Trigger",
        category = CustomizeCategory.TRIGGER,
        description = "Invisible touch bar at top of screen for opening panel",
        icon = Icons.Default.TouchApp
    ),
    TRIGGER_IMMERSIVE(
        id = "trigger_immersive",
        title = "Force Immersive Trigger",
        category = CustomizeCategory.TRIGGER,
        description = "Enable trigger overlay even in full-screen games and apps",
        icon = Icons.Default.AspectRatio
    ),
    TRIGGER_COOLDOWN(
        id = "trigger_cooldown",
        title = "Detector Cooldown",
        category = CustomizeCategory.TRIGGER,
        description = "Minimum milliseconds between pulldown gesture detections",
        icon = Icons.Default.Timer
    ),
    TRIGGER_DELAY(
        id = "trigger_delay",
        title = "Panel Replacement Delay",
        category = CustomizeCategory.TRIGGER,
        description = "Time delay before replacing system quick settings",
        icon = Icons.Default.Timer
    ),

    // About
    ABOUT_INFO(
        id = "about_info",
        title = "App Architecture Info",
        category = CustomizeCategory.ABOUT,
        description = "Build version, system status, and overlay engine details",
        icon = Icons.Default.AutoAwesome
    ),
    ABOUT_PERMISSIONS(
        id = "about_permissions",
        title = "Permission Status",
        category = CustomizeCategory.ABOUT,
        description = "Accessibility and overlay permission statuses",
        icon = Icons.Default.Block
    );

    companion object {
        fun fromId(id: String): FavoriteFeature? = entries.firstOrNull { it.id == id }
        fun getByCategory(category: CustomizeCategory): List<FavoriteFeature> = entries.filter { it.category == category }
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
            CustomizeCategory.BASIC to "Basic",
            CustomizeCategory.THEMES to "Themes",
            CustomizeCategory.MISCELLANEOUS to "Misc"
        )
    }

    val filteredFeatures = remember(searchQuery, selectedCategoryFilter) {
        FavoriteFeature.entries.filter { feat ->
            val matchesCategory = (selectedCategoryFilter == null || feat.category == selectedCategoryFilter)
            val matchesQuery = searchQuery.isBlank() ||
                feat.title.contains(searchQuery, ignoreCase = true) ||
                feat.description.contains(searchQuery, ignoreCase = true) ||
                feat.category.title.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
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
    actionContent: (@Composable () -> Unit)? = null
) {
    SettingsCard(
        shape = shape,
        modifier = modifier.testTag("pinned_feature_${feature.id}"),
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
    val context = LocalContext.current

    when (feature) {
        // --- BASIC ---
        FavoriteFeature.BASIC_THEME_MODE -> {
            val themeMode by PanelPreferences.themeMode.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = themeMode == mode,
                        onClick = { PanelPreferences.setThemeMode(context, mode) },
                        label = { Text(mode.displayName, fontSize = 12.sp) }
                    )
                }
            }
        }
        FavoriteFeature.BASIC_COLLAPSE_OPTIONS -> {
            val collapseExpandOptions by PanelPreferences.collapseExpandOptions.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Compact Chips View", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = collapseExpandOptions,
                    onCheckedChange = { PanelPreferences.setCollapseExpandOptions(context, it) }
                )
            }
        }
        FavoriteFeature.BASIC_COLLAPSE_SUBHEADERS -> {
            val collapseExpandSubheaders by PanelPreferences.collapseExpandSubheaders.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Collapse Subheaders", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = collapseExpandSubheaders,
                    onCheckedChange = { PanelPreferences.setCollapseExpandSubheaders(context, it) }
                )
            }
        }
        FavoriteFeature.BASIC_BACKUP_RESTORE -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        android.widget.Toast.makeText(context, "Backup functionality available in Basic Settings", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Export Config", fontSize = 12.sp)
                }
            }
        }

        // --- QS TWEAKS ---
        FavoriteFeature.TWEAKS_GRID_LAYOUT,
        FavoriteFeature.TWEAKS_QQS_TILES,
        FavoriteFeature.DESIGN_QQS_QUANTITY -> {
            val qqsTileCount by PanelPreferences.qqsTileCount.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("QQS Tile Count ($qqsTileCount)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Slider(
                    value = qqsTileCount.toFloat(),
                    onValueChange = { PanelPreferences.setQqsTileCount(context, it.toInt()) },
                    valueRange = 2f..12f,
                    steps = 9,
                    modifier = Modifier.width(160.dp)
                )
            }
        }
        FavoriteFeature.TWEAKS_DISABLE_LABELS -> {
            val disableLabelsInExpanded by PanelPreferences.disableLabelsInExpanded.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Disable Tile Labels", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = disableLabelsInExpanded,
                    onCheckedChange = { PanelPreferences.setDisableLabelsInExpanded(context, it) }
                )
            }
        }
        FavoriteFeature.TWEAKS_HIDE_DATE -> {
            val hideDateInExpanded by PanelPreferences.hideDateInExpanded.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Hide Date Display", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = hideDateInExpanded,
                    onCheckedChange = { PanelPreferences.setHideDateInExpanded(context, it) }
                )
            }
        }
        FavoriteFeature.TWEAKS_DRAG_HANDLE -> {
            val handleLength by PanelPreferences.dragHandleLength.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Handle Width (${handleLength.toInt()}dp)", fontSize = 12.sp)
                Slider(
                    value = handleLength,
                    onValueChange = { PanelPreferences.setDragHandleLength(context, it) },
                    valueRange = 20f..120f,
                    modifier = Modifier.width(160.dp)
                )
            }
        }
        FavoriteFeature.TWEAKS_FONT_SIZES,
        FavoriteFeature.DESIGN_TILE_SIZE,
        FavoriteFeature.DESIGN_QQS_SIZE -> {
            val qqsTileSize by PanelPreferences.qqsTileSize.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tile Size (${qqsTileSize} dp)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Slider(
                    value = qqsTileSize.toFloat(),
                    onValueChange = { PanelPreferences.setQqsTileSize(context, it.toInt()) },
                    valueRange = 24f..64f,
                    modifier = Modifier.width(160.dp)
                )
            }
        }
        FavoriteFeature.TWEAKS_VIBRATION -> {
            Text("Haptic Feedback enabled for Quick Setting tiles", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        FavoriteFeature.TWEAKS_DATE_POSITION -> {
            val datePosition by PanelPreferences.datePosition.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DatePosition.entries.forEach { pos ->
                    FilterChip(
                        selected = datePosition == pos,
                        onClick = { PanelPreferences.setDatePosition(context, pos) },
                        label = { Text(pos.label, fontSize = 11.sp) }
                    )
                }
            }
        }
        FavoriteFeature.TWEAKS_EDIT_POSITION -> {
            val editButtonPosition by PanelPreferences.editButtonPosition.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EditButtonPosition.entries.forEach { pos ->
                    FilterChip(
                        selected = editButtonPosition == pos,
                        onClick = { PanelPreferences.setEditButtonPosition(context, pos) },
                        label = { Text(pos.label, fontSize = 11.sp) }
                    )
                }
            }
        }
        FavoriteFeature.TWEAKS_HIDE_QQS_DRAG -> {
            val hideQqsDragHandle by PanelPreferences.hideQqsDragHandle.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Hide QQS Drag Handle", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = hideQqsDragHandle,
                    onCheckedChange = { PanelPreferences.setHideQqsDragHandle(context, it) }
                )
            }
        }

        // --- QS DESIGN ---
        FavoriteFeature.DESIGN_ACTIVE_SHAPE -> {
            val shapeId by PanelPreferences.enabledTileShapeId.collectAsState()
            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Active Shape", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("circle" to "Circle", "squircle" to "Squircle", "rounded_square" to "Square", "teardrop" to "Teardrop", "hexagon" to "Hexagon").forEach { (id, name) ->
                        FilterChip(
                            selected = shapeId == id,
                            onClick = { PanelPreferences.setEnabledTileShapeId(context, id) },
                            label = { Text(name, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }
        FavoriteFeature.DESIGN_INACTIVE_SHAPE -> {
            val shapeId by PanelPreferences.disabledTileShapeId.collectAsState()
            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Inactive Shape", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("circle" to "Circle", "squircle" to "Squircle", "rounded_square" to "Square", "teardrop" to "Teardrop", "hexagon" to "Hexagon").forEach { (id, name) ->
                        FilterChip(
                            selected = shapeId == id,
                            onClick = { PanelPreferences.setDisabledTileShapeId(context, id) },
                            label = { Text(name, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }
        FavoriteFeature.DESIGN_TILE_STROKE -> {
            val stroke by PanelPreferences.tileOutlineEnabled.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tile Stroke / Outline", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = stroke,
                    onCheckedChange = { PanelPreferences.setTileOutlineEnabled(context, it) }
                )
            }
        }
        FavoriteFeature.DESIGN_TILE_GRADIENT -> {
            val gradient by PanelPreferences.showQsTileGradient.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Monet Tile Gradient", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = gradient,
                    onCheckedChange = { PanelPreferences.setShowQsTileGradient(context, it) }
                )
            }
        }
        FavoriteFeature.DESIGN_PROGRESS_STYLE -> {
            val progressStyle by PanelPreferences.progressBarStyle.collectAsState()
            Text("Progress Style: ${progressStyle.label}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        FavoriteFeature.DESIGN_PANEL_CORNERS -> {
            Text("Configure outer/inner corner radii in Panel Corner section", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        FavoriteFeature.DESIGN_HEADER_IMAGE -> {
            val headerImg by PanelPreferences.showCustomHeaderImage.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Custom Header Image", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = headerImg,
                    onCheckedChange = { PanelPreferences.setShowCustomHeaderImage(context, it) }
                )
            }
        }
        FavoriteFeature.DESIGN_MONET_GRADIENT -> {
            val monetGradient by PanelPreferences.showCustomHeaderMonetGradient.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Header Monet Gradient", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = monetGradient,
                    onCheckedChange = { PanelPreferences.setShowCustomHeaderMonetGradient(context, it) }
                )
            }
        }
        FavoriteFeature.DESIGN_ELEMENT_COLORS -> {
            Button(
                onClick = {
                    val intent = android.content.Intent(context, ElementColorsActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = modifier.fillMaxWidth()
            ) {
                Text("Open Element Accent Colors", fontSize = 12.sp)
            }
        }
        FavoriteFeature.DESIGN_SIM_CARRIER -> {
            val showCarrier by PanelPreferences.showCarrier.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SIM Carrier Label", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = showCarrier,
                    onCheckedChange = { PanelPreferences.setShowCarrier(context, it) }
                )
            }
        }
        FavoriteFeature.DESIGN_CARRIER_DIVIDER -> {
            val showDivider by PanelPreferences.showCarrierDivider.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Carrier Divider Line", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = showDivider,
                    onCheckedChange = { PanelPreferences.setShowCarrierDivider(context, it) }
                )
            }
        }
        FavoriteFeature.DESIGN_BRIGHTNESS_BOTTOM -> {
            val moveBottom by PanelPreferences.brightnessAtBottom.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Brightness Bar at Bottom", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = moveBottom,
                    onCheckedChange = { PanelPreferences.setBrightnessAtBottom(context, it) }
                )
            }
        }
        FavoriteFeature.DESIGN_BRIGHTNESS_QQS -> {
            val showQqsBrightness by PanelPreferences.brightnessInQqs.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Brightness Bar in QQS", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = showQqsBrightness,
                    onCheckedChange = { PanelPreferences.setBrightnessInQqs(context, it) }
                )
            }
        }

        // --- HEADS UP ---
        FavoriteFeature.HEADS_UP_MASTER -> {
            val enableHeadsUp by PanelPreferences.enableHeadsUpOverlay.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Custom Heads-Up Banner Engine", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = enableHeadsUp,
                    onCheckedChange = { PanelPreferences.setEnableHeadsUpOverlay(context, it) }
                )
            }
        }
        FavoriteFeature.HEADS_UP_TEST -> {
            Button(
                onClick = {
                    com.example.service.HeadsUpOverlayManager.showTestHeadsUp(context)
                    android.widget.Toast.makeText(context, "Testing Heads-Up Banner...", android.widget.Toast.LENGTH_SHORT).show()
                },
                modifier = modifier.fillMaxWidth()
            ) {
                Text("Trigger Test Banner Notification", fontSize = 12.sp)
            }
        }
        FavoriteFeature.HEADS_UP_TIMEOUT -> {
            val timeout by PanelPreferences.headsUpTimeoutSeconds.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Auto-Dismiss Timeout (${timeout}s)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Slider(
                    value = timeout.toFloat(),
                    onValueChange = { PanelPreferences.setHeadsUpTimeoutSeconds(context, it.toInt()) },
                    valueRange = 1f..15f,
                    steps = 13,
                    modifier = Modifier.width(160.dp)
                )
            }
        }
        FavoriteFeature.HEADS_UP_POSITION,
        FavoriteFeature.HEADS_UP_COMPACT,
        FavoriteFeature.HEADS_UP_GESTURES,
        FavoriteFeature.HEADS_UP_BLACKLIST -> {
            Text("Access detailed options in section below", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // --- MISCELLANEOUS ---
        FavoriteFeature.MISC_ANIMATION_SPEED,
        FavoriteFeature.MISC_ANIMATION_DURATION -> {
            val speed by PanelPreferences.animationSpeedScale.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Anim Speed (${String.format("%.1fx", speed)})", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Slider(
                    value = speed,
                    onValueChange = { PanelPreferences.setAnimationSpeedScale(context, it) },
                    valueRange = 0.5f..2.5f,
                    modifier = Modifier.width(160.dp)
                )
            }
        }
        FavoriteFeature.MISC_STATUS_BAR_GESTURES,
        FavoriteFeature.MISC_QUICK_PULLDOWN,
        FavoriteFeature.MISC_STATUS_TRIGGER,
        FavoriteFeature.TRIGGER_STATUS_BAR -> {
            val gestures by PanelPreferences.invisibleStatusBarTrigger.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Invisible Status Bar Trigger", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = gestures,
                    onCheckedChange = { PanelPreferences.setInvisibleStatusBarTrigger(context, it) }
                )
            }
        }
        FavoriteFeature.MISC_PANEL_AUTO_CLOSE -> {
            val autoClose by PanelPreferences.autoSystemPanelClosing.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Auto-Close Stock System Panel", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = autoClose,
                    onCheckedChange = { PanelPreferences.setAutoSystemPanelClosing(context, it) }
                )
            }
        }
        FavoriteFeature.MISC_REPLACEMENT_DELAY,
        FavoriteFeature.TRIGGER_DELAY -> {
            val delayMs by PanelPreferences.panelReplacementDelayMs.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Replacement Delay (${delayMs}ms)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Slider(
                    value = delayMs.toFloat(),
                    onValueChange = { PanelPreferences.setPanelReplacementDelayMs(context, it.toInt()) },
                    valueRange = 0f..500f,
                    steps = 10,
                    modifier = Modifier.width(160.dp)
                )
            }
        }
        FavoriteFeature.MISC_SYSTEM_OVERRIDE -> {
            val sysOverride by PanelPreferences.overrideSystemQs.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("System QS Override Engine", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = sysOverride,
                    onCheckedChange = { PanelPreferences.setOverrideSystemQs(context, it) }
                )
            }
        }
        FavoriteFeature.MISC_COOLDOWN,
        FavoriteFeature.TRIGGER_COOLDOWN -> {
            val cooldown by PanelPreferences.detectorCooldownMs.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Detector Cooldown (${cooldown}ms)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Slider(
                    value = cooldown.toFloat(),
                    onValueChange = { PanelPreferences.setDetectorCooldownMs(context, it.toInt()) },
                    valueRange = 100f..1000f,
                    modifier = Modifier.width(160.dp)
                )
            }
        }
        FavoriteFeature.MISC_MEDIA_PREVIEW -> {
            val mediaPreview by PanelPreferences.forceShowMediaPreview.collectAsState()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Force Media Player Preview", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = mediaPreview,
                    onCheckedChange = { PanelPreferences.setForceShowMediaPreview(context, it) }
                )
            }
        }

        // --- GESTURES & OTHER ---
        FavoriteFeature.GESTURE_VOLUME_KEY,
        FavoriteFeature.MISC_HARDWARE_BUTTONS,
        FavoriteFeature.MISC_LOCKSCREEN_BYPASS,
        FavoriteFeature.MISC_DUAL_TONE_STATUS,
        FavoriteFeature.MISC_SMART_PULLDOWN,
        FavoriteFeature.MISC_PRIME_CORE,
        FavoriteFeature.MISC_VOLUME_PANEL,
        FavoriteFeature.TRIGGER_IMMERSIVE,
        FavoriteFeature.THEMES_SAVE,
        FavoriteFeature.THEMES_SAVED_LIST,
        FavoriteFeature.ABOUT_INFO,
        FavoriteFeature.ABOUT_PERMISSIONS -> {
            Text("Access options in section body below", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}



