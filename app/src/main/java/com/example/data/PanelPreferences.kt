package com.example.data

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.model.QsGridLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ThemeMode(val id: String, val displayName: String) {
    SYSTEM("system", "System Default"),
    LIGHT("light", "Light"),
    DARK("dark", "Dark");

    companion object {
        fun fromId(id: String?): ThemeMode =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: SYSTEM
    }
}

enum class ScreenRecordResolution(val id: String, val title: String, val subtitle: String, val targetWidth: Int?) {
    NATIVE("native", "Native", "Device display resolution", null),
    RES_1080P("1080p", "1080p Full HD", "1080p (High clarity)", 1080),
    RES_720P("720p", "720p HD", "720p (Balanced)", 720),
    RES_480P("480p", "480p SD", "480p (Smaller size)", 480);

    companion object {
        fun fromId(id: String?): ScreenRecordResolution =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: NATIVE
    }
}

enum class ScreenRecordAudioSource(val id: String, val title: String, val subtitle: String) {
    MUTE("mute", "Mute", "No audio"),
    INTERNAL("internal", "Internal Audio", "System & app playback"),
    MIC("mic", "Microphone", "Device microphone"),
    INTERNAL_AND_MIC("internal_mic", "System + Mic", "Internal audio & microphone");

    companion object {
        fun fromId(id: String?): ScreenRecordAudioSource =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: MIC
    }
}

enum class ScreenRecordFps(val id: String, val fps: Int, val title: String) {
    FPS_60("60", 60, "60 FPS (Smoother)"),
    FPS_30("30", 30, "30 FPS (Standard)");

    companion object {
        fun fromId(id: String?): ScreenRecordFps =
            entries.firstOrNull { it.id == id } ?: FPS_60
    }
}

enum class EditButtonPosition(val id: String, val label: String) {
    FOOTER("footer", "Footer Only"),
    HEADER("header", "Header Only"),
    BOTH("both", "Header & Footer"),
    NONE("none", "Hidden");

    companion object {
        fun fromId(id: String?): EditButtonPosition =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: FOOTER
    }
}

enum class DatePosition(val id: String, val label: String) {
    NEXT_TO_TIME("inline", "Next to Time"),
    BELOW_TIME("below", "Below Time"),
    HEADER_RIGHT("right", "Header Right"),
    QQS_PANEL("qqs_panel", "QQS Panel"),
    HIDDEN("hidden", "Hidden");

    companion object {
        fun fromId(id: String?): DatePosition =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: BELOW_TIME
    }
}

enum class BatteryPercentageLocation(val id: String, val label: String) {
    HEADER("header", "In Header"),
    QQS_RIGHT("qqs_right", "In QQS (Right)"),
    HIDDEN("hidden", "Hidden");

    companion object {
        fun fromId(id: String?): BatteryPercentageLocation =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: HEADER
    }
}

enum class SettingsIconLocation(val id: String, val label: String) {
    HEADER("header", "In header"),
    EXPANDED_QS("expanded_qs", "In expanded QS alongside power icon"),
    CARRIER_RIGHT("carrier_right", "In carrier container at right side"),
    HIDDEN("hidden", "Hidden");

    companion object {
        fun fromId(id: String?): SettingsIconLocation =
            entries.firstOrNull { 
                it.id.equals(id, ignoreCase = true) ||
                (it == HEADER && id.equals("in header", ignoreCase = true)) ||
                (it == EXPANDED_QS && (id.equals("in expanded qs", ignoreCase = true) || id.equals("expanded", ignoreCase = true))) ||
                (it == CARRIER_RIGHT && id.equals("carrier right", ignoreCase = true))
            } ?: EXPANDED_QS
    }
}

enum class TileShape(val id: String, val label: String, val description: String) {
    CIRCLE("circle", "Circle", "Classic round tile shape"),
    SQUIRCLE("squircle", "Squircle", "Rounded square with 14dp corners"),
    SQUARE("square", "Square", "Sharp square with 4dp corners"),
    TEARDROP("teardrop", "Teardrop", "Asymmetric teardrop style"),
    PILL("pill", "12dp Square", "Square with 12dp rounded corners"),
    CUSTOM_1("custom_1", "Leaf", "Leaf shape with diagonal rounded corners"),
    CUSTOM_3("custom_3", "Arch", "Arch / dome with rounded top corners"),
    CUSTOM_8("custom_8", "Hexagon", "Six-sided regular hexagon"),
    CLOUD("cloud", "Cloud", "Fluffy cloud shape with rounded bumps"),
    OCTAGON("octagon", "Octagon", "Eight-sided symmetrical polygon"),
    STAMP("stamp", "Stamp", "Postage stamp style with scalloped edges");

    companion object {
        fun fromId(id: String?): TileShape =
            entries.firstOrNull { 
                it.id.equals(id, ignoreCase = true) || 
                (it == PILL && (
                    id.equals("12dp_square", ignoreCase = true) || 
                    id.equals("12dp square", ignoreCase = true) ||
                    id.equals("12dp", ignoreCase = true) ||
                    id.equals("8dp_square", ignoreCase = true) || 
                    id.equals("8dp square", ignoreCase = true) ||
                    id.equals("8dp", ignoreCase = true) ||
                    id.equals("16dp_square", ignoreCase = true) || 
                    id.equals("16dp square", ignoreCase = true)
                )) ||
                (it == CUSTOM_8 && id.equals("hexagon", ignoreCase = true))
            } ?: CIRCLE
    }
}

enum class ProgressBarStyle(val id: String, val label: String, val description: String) {
    DEFAULT("default", "M3", "Standard Material 3 slider"),
    STRAIGHT("straight", "Straight", "Clean minimal straight progress bar"),
    WAVY("wavy", "Wavy", "Android 13/14 squiggly wave progress bar"),
    ANDROID_12("android_12", "Android 12", "Android 12 thick pill style slider bar");

    companion object {
        fun fromId(id: String?): ProgressBarStyle =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
    }
}

enum class QsSliderTarget(val id: String, val label: String) {
    BRIGHTNESS("brightness", "Brightness"),
    VOLUME("volume", "Volume"),
    MEDIA("media", "Media Player");

    companion object {
        fun fromId(id: String?): QsSliderTarget =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: BRIGHTNESS
    }
}

enum class ClearAllFabPosition(val id: String, val label: String) {
    CENTER("center", "Centered"),
    LEFT("left", "Left Side"),
    RIGHT("right", "Right Side");

    companion object {
        fun fromId(id: String?): ClearAllFabPosition =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: CENTER
    }
}

enum class ClearAllFabShape(val id: String, val label: String) {
    CIRCLE("circle", "Circle"),
    PILL("pill", "Squircle / Pill"),
    ROUNDED("rounded", "Rounded Square"),
    CUT_CORNER("cut_corner", "Cut Corner / Diamond"),
    STAR("star", "Star / Tile");

    companion object {
        fun fromId(id: String?): ClearAllFabShape =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: CIRCLE
    }
}

enum class ClearAllFabIcon(val id: String, val label: String, val drawableRes: Int) {
    CLEAR_ALL("clear_all", "Clear All / Sweep", com.example.R.drawable.ic_notif_clear_all),
    BARS("bars", "Bars / Lines", com.example.R.drawable.ic_notif_bars),
    BROOM("broom", "Broom / Clean", com.example.R.drawable.ic_notif_broom),
    DELETE("delete", "Delete / Trash", com.example.R.drawable.ic_notif_trash),
    DONE_ALL("done_all", "Done All", com.example.R.drawable.ic_notif_done_all),
    CHECK_CIRCLE("check_circle", "Check Circle", com.example.R.drawable.ic_notif_check_circle),
    BELL_OFF("bell_off", "Bell Off", com.example.R.drawable.ic_notif_bell_off),
    SPARKLES("sparkles", "Sparkles", com.example.R.drawable.ic_notif_sparkles),
    ARCHIVE("archive", "Archive", com.example.R.drawable.ic_notif_archive),
    LAYERS_CLEAR("layers_clear", "Layers Clear", com.example.R.drawable.ic_notif_layers_clear),
    PLAYLIST_REMOVE("playlist_remove", "List Dismiss", com.example.R.drawable.ic_notif_playlist_remove),
    CANCEL("cancel", "Cancel Circle", com.example.R.drawable.ic_notif_cancel),
    CLOSE("close", "Close / X", com.example.R.drawable.ic_notif_close),
    BRUSH("brush", "Brush", com.example.R.drawable.ic_notif_brush),
    DOUBLE_ARROW("double_arrow", "Double Arrow", com.example.R.drawable.ic_notif_double_arrow),
    REFRESH("refresh", "Refresh", com.example.R.drawable.ic_notif_refresh);

    companion object {
        fun fromId(id: String?): ClearAllFabIcon =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: CLEAR_ALL
    }
}

object PanelPreferences {
    const val PREFS_NAME = "quickpanel_prefs"
    private const val KEY_DISABLE_CLEAR_ALL_BG = "disable_clear_all_bg"
    private const val KEY_ENABLE_PANEL_SHADOWS = "enable_panel_shadows"
    private const val KEY_PANEL_SHADOW_ELEVATION = "panel_shadow_elevation"
    private const val KEY_PANEL_SHADOW_OFFSET_Y = "panel_shadow_offset_y"
    private const val KEY_USE_CLEAR_ALL_FAB = "use_clear_all_fab"
    private const val KEY_CLEAR_ALL_FAB_POSITION = "clear_all_fab_position"
    private const val KEY_CLEAR_ALL_FAB_SHAPE = "clear_all_fab_shape"
    private const val KEY_CLEAR_ALL_FAB_SHAPE_ID = "clear_all_fab_shape_id"
    private const val KEY_CLEAR_ALL_FAB_ICON = "clear_all_fab_icon"
    private const val KEY_CLEAR_ALL_FAB_OFFSET_X = "clear_all_fab_offset_x"
    private const val KEY_CLEAR_ALL_FAB_OFFSET_Y = "clear_all_fab_offset_y"
    private const val KEY_CLEAR_ALL_FAB_SIZE = "clear_all_fab_size"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_GRID_LAYOUT = "qs_grid_layout"
    private const val KEY_QQS_TILE_SIZE = "qqs_tile_size"
    private const val KEY_QQS_TILE_COUNT = "qqs_tile_count"
    private const val KEY_EDIT_BUTTON_POSITION = "edit_button_position"
    private const val KEY_SHOW_DATE_IN_QQS_HEADER = "show_date_in_qqs_header"
    private const val KEY_DATE_POSITION = "date_position"
    private const val KEY_TILE_SHAPE = "tile_shape"
    private const val KEY_ENABLED_TILE_SHAPE = "enabled_tile_shape"
    private const val KEY_DISABLED_TILE_SHAPE = "disabled_tile_shape"
    private const val KEY_ACTIVE_SQUARE_SHAPE = "active_square_shape"
    private const val KEY_TILE_OUTLINE_ENABLED = "tile_outline_enabled"
    private const val KEY_HIDE_QQS_DRAG_HANDLE = "hide_qqs_drag_handle"
    private const val KEY_HIDE_QQS_EMPTY_NOTIF_LABELS = "hide_qqs_empty_notif_labels"
    private const val KEY_SHOW_CARRIER = "show_carrier"
    private const val KEY_SHOW_CARRIER_DIVIDER = "show_carrier_divider"
    private const val KEY_HIDE_CARRIER_IN_EXPANDED = "hide_carrier_in_expanded"
    private const val KEY_BRIGHTNESS_AT_BOTTOM = "brightness_at_bottom"
    private const val KEY_BRIGHTNESS_IN_QQS = "brightness_in_qqs"
    private const val KEY_HIDE_BRIGHTNESS_PERCENTAGE = "hide_brightness_percentage"
    private const val KEY_HIDE_BRIGHTNESS_ICON = "hide_brightness_icon"
    private const val KEY_SHOW_BRIGHTNESS_PLUS_MINUS = "show_brightness_plus_minus"
    private const val KEY_SHOW_BRIGHTNESS_GRADIENT = "show_brightness_gradient"
    private const val KEY_MIN_BRIGHTNESS = "min_brightness"
    private const val KEY_MAX_BRIGHTNESS = "max_brightness"
    private const val KEY_WAVY_PROGRESS_BARS = "wavy_progress_bars"
    private const val KEY_PROGRESS_BAR_STYLE = "progress_bar_style"
    private const val KEY_ENABLE_CONTAINER_BLUR = "enable_container_blur"
    private const val KEY_INVISIBLE_STATUS_BAR_TRIGGER = "invisible_status_bar_trigger"
    private const val KEY_FORCE_IMMERSIVE_TRIGGER = "force_immersive_trigger"
    private const val KEY_OVERRIDE_SYSTEM_QS = "override_system_qs"
    private const val KEY_AUTO_SYSTEM_PANEL_CLOSING = "auto_system_panel_closing"
    private const val KEY_STOCK_SHADE_WHITELIST = "stock_shade_whitelist"
    private const val KEY_CUSTOM_BLACKLIST_PACKAGES = "custom_blacklist_packages"
    private const val KEY_CUSTOM_BLACKLIST_CLASSES = "custom_blacklist_classes"
    private const val KEY_DETECTOR_COOLDOWN_MS = "detector_cooldown_ms"
    private const val KEY_PANEL_REPLACEMENT_DELAY_MS = "panel_replacement_delay_ms"
    private const val KEY_DRAG_HANDLE_VERTICAL_OFFSET = "drag_handle_vertical_offset"
    private const val KEY_DRAG_HANDLE_LENGTH = "drag_handle_length"
    private const val KEY_FORCE_SHOW_MEDIA_PREVIEW = "force_show_media_preview"
    private const val KEY_ENABLE_CUSTOM_VOLUME_PANEL = "enable_custom_volume_panel"
    private const val KEY_ANIMATION_SPEED_SCALE = "animation_speed_scale"
    private const val KEY_DISABLE_PANEL_ANIMATIONS = "disable_panel_animations"
    private const val KEY_USE_PRIME_CORE_AFFINITY = "use_prime_core_affinity"
    private const val KEY_SHOW_CUSTOM_HEADER_IMAGE = "show_custom_header_image"
    private const val KEY_CUSTOM_HEADER_IMAGE_URI = "custom_header_image_uri"
    private const val KEY_CUSTOM_HEADER_IMAGE_TITLE = "custom_header_image_title"
    private const val KEY_CUSTOM_HEADER_IMAGE_HEIGHT = "custom_header_image_height"
    private const val KEY_CUSTOM_HEADER_IMAGE_OPACITY = "custom_header_image_opacity"
    private const val KEY_SHOW_CUSTOM_HEADER_MONET_GRADIENT = "show_custom_header_monet_gradient"
    private const val KEY_CUSTOM_HEADER_MONET_GRADIENT_HEIGHT = "custom_header_monet_gradient_height"
    private const val KEY_SHOW_MONET_SURFACE_BACKGROUND = "show_monet_surface_background"
    private const val KEY_COLLAPSE_EXPAND_OPTIONS = "collapse_expand_options"
    private const val KEY_COLLAPSE_EXPAND_SUBHEADERS = "collapse_expand_subheaders"

    private val _showCustomHeaderImage = MutableStateFlow(false)
    val showCustomHeaderImage: StateFlow<Boolean> = _showCustomHeaderImage.asStateFlow()

    private val _collapseExpandOptions = MutableStateFlow(false)
    val collapseExpandOptions: StateFlow<Boolean> = _collapseExpandOptions.asStateFlow()

    private val _collapseExpandSubheaders = MutableStateFlow(false)
    val collapseExpandSubheaders: StateFlow<Boolean> = _collapseExpandSubheaders.asStateFlow()

    private val _customHeaderImageUri = MutableStateFlow("")
    val customHeaderImageUri: StateFlow<String> = _customHeaderImageUri.asStateFlow()

    private val _customHeaderImageTitle = MutableStateFlow("Header Banner")
    val customHeaderImageTitle: StateFlow<String> = _customHeaderImageTitle.asStateFlow()

    private val _customHeaderImageHeight = MutableStateFlow(85f)
    val customHeaderImageHeight: StateFlow<Float> = _customHeaderImageHeight.asStateFlow()

    private val _customHeaderImageOpacity = MutableStateFlow(1.0f)
    val customHeaderImageOpacity: StateFlow<Float> = _customHeaderImageOpacity.asStateFlow()

    private val _showCustomHeaderMonetGradient = MutableStateFlow(false)
    val showCustomHeaderMonetGradient: StateFlow<Boolean> = _showCustomHeaderMonetGradient.asStateFlow()

    private val _customHeaderMonetGradientHeight = MutableStateFlow(85f)
    val customHeaderMonetGradientHeight: StateFlow<Float> = _customHeaderMonetGradientHeight.asStateFlow()

    private val _showMonetSurfaceBackground = MutableStateFlow(false)
    val showMonetSurfaceBackground: StateFlow<Boolean> = _showMonetSurfaceBackground.asStateFlow()

    // Per-Element Monet Accent Color Preferences (false = Primary, true = Tertiary)
    private const val KEY_ELEM_SURFACE_BG_TERTIARY = "elem_surface_bg_tertiary"
    private const val KEY_ELEM_PANEL_CONTAINER_TERTIARY = "elem_panel_container_tertiary"
    private const val KEY_ELEM_NOTIF_CONTAINER_TERTIARY = "elem_notif_container_tertiary"
    private const val KEY_ELEM_HEADER_GRADIENT_TERTIARY = "elem_header_gradient_tertiary"
    private const val KEY_ELEM_QS_TILES_TERTIARY = "elem_qs_tiles_tertiary"
    private const val KEY_ELEM_INACTIVE_TILES_TERTIARY = "elem_inactive_tiles_tertiary"
    private const val KEY_ELEM_INACTIVE_TILES_PRIMARY = "elem_inactive_tiles_primary"
    private const val KEY_ELEM_SLIDERS_TERTIARY = "elem_sliders_tertiary"
    private const val KEY_ELEM_MISC_ELEMENTS_TERTIARY = "elem_misc_elements_tertiary"
    private const val KEY_DISABLE_MONET_NOTIF_TEXT = "disable_monet_notif_text"

    private const val KEY_ELEM_SURFACE_BG_HUE = "elem_surface_bg_hue"
    private const val KEY_ELEM_SURFACE_BG_SAT = "elem_surface_bg_sat"
    private const val KEY_ELEM_SURFACE_BG_VAL = "elem_surface_bg_val"

    private const val KEY_ELEM_PANEL_CONTAINER_HUE = "elem_panel_container_hue"
    private const val KEY_ELEM_PANEL_CONTAINER_SAT = "elem_panel_container_sat"
    private const val KEY_ELEM_PANEL_CONTAINER_VAL = "elem_panel_container_val"

    private const val KEY_ELEM_HEADER_GRADIENT_HUE = "elem_header_gradient_hue"
    private const val KEY_ELEM_HEADER_GRADIENT_SAT = "elem_header_gradient_sat"
    private const val KEY_ELEM_HEADER_GRADIENT_VAL = "elem_header_gradient_val"

    private const val KEY_ELEM_QS_TILES_HUE = "elem_qs_tiles_hue"
    private const val KEY_ELEM_QS_TILES_SAT = "elem_qs_tiles_sat"
    private const val KEY_ELEM_QS_TILES_VAL = "elem_qs_tiles_val"

    private const val KEY_ELEM_INACTIVE_TILES_HUE = "elem_inactive_tiles_hue"
    private const val KEY_ELEM_INACTIVE_TILES_SAT = "elem_inactive_tiles_sat"
    private const val KEY_ELEM_INACTIVE_TILES_VAL = "elem_inactive_tiles_val"

    private const val KEY_ELEM_SLIDERS_HUE = "elem_sliders_hue"
    private const val KEY_ELEM_SLIDERS_SAT = "elem_sliders_sat"
    private const val KEY_ELEM_SLIDERS_VAL = "elem_sliders_val"

    private const val KEY_ELEM_MISC_ELEMENTS_HUE = "elem_misc_elements_hue"
    private const val KEY_ELEM_MISC_ELEMENTS_SAT = "elem_misc_elements_sat"
    private const val KEY_ELEM_MISC_ELEMENTS_VAL = "elem_misc_elements_val"

    // Per-mode keys: Light mode
    private const val KEY_ELEM_SURFACE_BG_TERTIARY_LIGHT = "elem_surface_bg_tertiary_light"
    private const val KEY_ELEM_PANEL_CONTAINER_TERTIARY_LIGHT = "elem_panel_container_tertiary_light"
    private const val KEY_ELEM_HEADER_GRADIENT_TERTIARY_LIGHT = "elem_header_gradient_tertiary_light"
    private const val KEY_ELEM_QS_TILES_TERTIARY_LIGHT = "elem_qs_tiles_tertiary_light"
    private const val KEY_ELEM_INACTIVE_TILES_TERTIARY_LIGHT = "elem_inactive_tiles_tertiary_light"
    private const val KEY_ELEM_INACTIVE_TILES_PRIMARY_LIGHT = "elem_inactive_tiles_primary_light"
    private const val KEY_ELEM_SLIDERS_TERTIARY_LIGHT = "elem_sliders_tertiary_light"
    private const val KEY_ELEM_MISC_ELEMENTS_TERTIARY_LIGHT = "elem_misc_elements_tertiary_light"

    private const val KEY_ELEM_SURFACE_BG_HUE_LIGHT = "elem_surface_bg_hue_light"
    private const val KEY_ELEM_SURFACE_BG_SAT_LIGHT = "elem_surface_bg_sat_light"
    private const val KEY_ELEM_SURFACE_BG_VAL_LIGHT = "elem_surface_bg_val_light"

    private const val KEY_ELEM_PANEL_CONTAINER_HUE_LIGHT = "elem_panel_container_hue_light"
    private const val KEY_ELEM_PANEL_CONTAINER_SAT_LIGHT = "elem_panel_container_sat_light"
    private const val KEY_ELEM_PANEL_CONTAINER_VAL_LIGHT = "elem_panel_container_val_light"

    private const val KEY_ELEM_HEADER_GRADIENT_HUE_LIGHT = "elem_header_gradient_hue_light"
    private const val KEY_ELEM_HEADER_GRADIENT_SAT_LIGHT = "elem_header_gradient_sat_light"
    private const val KEY_ELEM_HEADER_GRADIENT_VAL_LIGHT = "elem_header_gradient_val_light"

    private const val KEY_ELEM_QS_TILES_HUE_LIGHT = "elem_qs_tiles_hue_light"
    private const val KEY_ELEM_QS_TILES_SAT_LIGHT = "elem_qs_tiles_sat_light"
    private const val KEY_ELEM_QS_TILES_VAL_LIGHT = "elem_qs_tiles_val_light"

    private const val KEY_ELEM_INACTIVE_TILES_HUE_LIGHT = "elem_inactive_tiles_hue_light"
    private const val KEY_ELEM_INACTIVE_TILES_SAT_LIGHT = "elem_inactive_tiles_sat_light"
    private const val KEY_ELEM_INACTIVE_TILES_VAL_LIGHT = "elem_inactive_tiles_val_light"

    private const val KEY_ELEM_SLIDERS_HUE_LIGHT = "elem_sliders_hue_light"
    private const val KEY_ELEM_SLIDERS_SAT_LIGHT = "elem_sliders_sat_light"
    private const val KEY_ELEM_SLIDERS_VAL_LIGHT = "elem_sliders_val_light"

    private const val KEY_ELEM_MISC_ELEMENTS_HUE_LIGHT = "elem_misc_elements_hue_light"
    private const val KEY_ELEM_MISC_ELEMENTS_SAT_LIGHT = "elem_misc_elements_sat_light"
    private const val KEY_ELEM_MISC_ELEMENTS_VAL_LIGHT = "elem_misc_elements_val_light"

    // Per-mode keys: Dark mode
    private const val KEY_ELEM_SURFACE_BG_TERTIARY_DARK = "elem_surface_bg_tertiary_dark"
    private const val KEY_ELEM_PANEL_CONTAINER_TERTIARY_DARK = "elem_panel_container_tertiary_dark"
    private const val KEY_ELEM_HEADER_GRADIENT_TERTIARY_DARK = "elem_header_gradient_tertiary_dark"
    private const val KEY_ELEM_QS_TILES_TERTIARY_DARK = "elem_qs_tiles_tertiary_dark"
    private const val KEY_ELEM_INACTIVE_TILES_TERTIARY_DARK = "elem_inactive_tiles_tertiary_dark"
    private const val KEY_ELEM_INACTIVE_TILES_PRIMARY_DARK = "elem_inactive_tiles_primary_dark"
    private const val KEY_ELEM_SLIDERS_TERTIARY_DARK = "elem_sliders_tertiary_dark"
    private const val KEY_ELEM_MISC_ELEMENTS_TERTIARY_DARK = "elem_misc_elements_tertiary_dark"

    private const val KEY_ELEM_SURFACE_BG_HUE_DARK = "elem_surface_bg_hue_dark"
    private const val KEY_ELEM_SURFACE_BG_SAT_DARK = "elem_surface_bg_sat_dark"
    private const val KEY_ELEM_SURFACE_BG_VAL_DARK = "elem_surface_bg_val_dark"

    private const val KEY_ELEM_PANEL_CONTAINER_HUE_DARK = "elem_panel_container_hue_dark"
    private const val KEY_ELEM_PANEL_CONTAINER_SAT_DARK = "elem_panel_container_sat_dark"
    private const val KEY_ELEM_PANEL_CONTAINER_VAL_DARK = "elem_panel_container_val_dark"

    private const val KEY_ELEM_HEADER_GRADIENT_HUE_DARK = "elem_header_gradient_hue_dark"
    private const val KEY_ELEM_HEADER_GRADIENT_SAT_DARK = "elem_header_gradient_sat_dark"
    private const val KEY_ELEM_HEADER_GRADIENT_VAL_DARK = "elem_header_gradient_val_dark"

    private const val KEY_ELEM_QS_TILES_HUE_DARK = "elem_qs_tiles_hue_dark"
    private const val KEY_ELEM_QS_TILES_SAT_DARK = "elem_qs_tiles_sat_dark"
    private const val KEY_ELEM_QS_TILES_VAL_DARK = "elem_qs_tiles_val_dark"

    private const val KEY_ELEM_INACTIVE_TILES_HUE_DARK = "elem_inactive_tiles_hue_dark"
    private const val KEY_ELEM_INACTIVE_TILES_SAT_DARK = "elem_inactive_tiles_sat_dark"
    private const val KEY_ELEM_INACTIVE_TILES_VAL_DARK = "elem_inactive_tiles_val_dark"

    private const val KEY_ELEM_SLIDERS_HUE_DARK = "elem_sliders_hue_dark"
    private const val KEY_ELEM_SLIDERS_SAT_DARK = "elem_sliders_sat_dark"
    private const val KEY_ELEM_SLIDERS_VAL_DARK = "elem_sliders_val_dark"

    private const val KEY_ELEM_MISC_ELEMENTS_HUE_DARK = "elem_misc_elements_hue_dark"
    private const val KEY_ELEM_MISC_ELEMENTS_SAT_DARK = "elem_misc_elements_sat_dark"
    private const val KEY_ELEM_MISC_ELEMENTS_VAL_DARK = "elem_misc_elements_val_dark"
    private const val KEY_QS_BACKGROUND_COLOR = "qs_background_color"
    private const val KEY_QS_PANEL_BACKGROUND_COLOR = "qs_panel_background_color"
    private const val KEY_QS_BACKGROUND_CORNER = "qs_background_corner"
    private const val KEY_SHOW_QS_TILE_GRADIENT = "show_qs_tile_gradient"
    private const val KEY_QS_TILE_LABEL_FONT_SIZE = "qs_tile_label_font_size"
    private const val KEY_QS_TILE_SUBTITLE_FONT_SIZE = "qs_tile_subtitle_font_size"
    private const val KEY_HIDE_DATE_IN_EXPANDED = "hide_date_in_expanded"
    private const val KEY_DISABLE_LABELS_IN_EXPANDED = "disable_labels_in_expanded"
    private const val KEY_PINNED_FEATURES = "pinned_features"
    private const val KEY_WAVY_THICKNESS = "wavy_thickness"
    private const val KEY_WAVY_WOBBLINESS = "wavy_wobbliness"
    private const val KEY_WAVY_WAVELENGTH = "wavy_wavelength"
    private const val KEY_WAVY_THUMB_RADIUS = "wavy_thumb_radius"
    private const val KEY_WAVY_SPEED = "wavy_speed"
    private const val KEY_SLIDER_INACTIVE_TRACK_THICKNESS = "slider_inactive_track_thickness"
    private const val KEY_SLIDER_CORNER_RADIUS = "slider_corner_radius"
    private const val KEY_SLIDER_SYNC_ALL = "slider_sync_all"
    private const val KEY_BRIGHTNESS_SLIDER_STYLE = "brightness_slider_style"
    private const val KEY_VOLUME_SLIDER_STYLE = "volume_slider_style"
    private const val KEY_MEDIA_SLIDER_STYLE = "media_slider_style"
    private const val KEY_A12_INACTIVE_THICKNESS = "a12_inactive_thickness"
    private const val KEY_A12_CORNER_RADIUS = "a12_corner_radius"
    private const val KEY_WAVY_INACTIVE_THICKNESS = "wavy_inactive_thickness"
    private const val KEY_WAVY_CORNER_RADIUS = "wavy_corner_radius"
    private const val KEY_STRAIGHT_THICKNESS = "straight_thickness"
    private const val KEY_STRAIGHT_INACTIVE_THICKNESS = "straight_inactive_thickness"
    private const val KEY_STRAIGHT_CORNER_RADIUS = "straight_corner_radius"
    private const val KEY_STRAIGHT_THUMB_RADIUS = "straight_thumb_radius"
    private const val KEY_PANEL_GLOBAL_CORNER_ENABLED = "panel_global_corner_enabled"
    private const val KEY_PANEL_OUTER_CORNER = "panel_outer_corner"
    private const val KEY_PANEL_INNER_CORNER = "panel_inner_corner"

    private val _panelGlobalCornerEnabled = MutableStateFlow(false)
    val panelGlobalCornerEnabled: StateFlow<Boolean> = _panelGlobalCornerEnabled.asStateFlow()

    private val _panelOuterCorner = MutableStateFlow(20.0f)
    val panelOuterCorner: StateFlow<Float> = _panelOuterCorner.asStateFlow()

    private val _panelInnerCorner = MutableStateFlow(5.0f)
    val panelInnerCorner: StateFlow<Float> = _panelInnerCorner.asStateFlow()

    private val _qsBackgroundCorner = MutableStateFlow(20.0f)
    val qsBackgroundCorner: StateFlow<Float> = _qsBackgroundCorner.asStateFlow()

    private val _qsBackgroundColor = MutableStateFlow<Int?>(null)
    val qsBackgroundColor: StateFlow<Int?> = _qsBackgroundColor.asStateFlow()

    private val _qsPanelBackgroundColor = MutableStateFlow<Int?>(null)
    val qsPanelBackgroundColor: StateFlow<Int?> = _qsPanelBackgroundColor.asStateFlow()

    private val _surfaceBgColorTertiary = MutableStateFlow(false)
    val surfaceBgColorTertiary: StateFlow<Boolean> = _surfaceBgColorTertiary.asStateFlow()

    private val _panelContainerColorTertiary = MutableStateFlow(false)
    val panelContainerColorTertiary: StateFlow<Boolean> = _panelContainerColorTertiary.asStateFlow()

    private val _notifContainerColorTertiary = MutableStateFlow(false)
    val notifContainerColorTertiary: StateFlow<Boolean> = _notifContainerColorTertiary.asStateFlow()

    private val _headerGradientColorTertiary = MutableStateFlow(false)
    val headerGradientColorTertiary: StateFlow<Boolean> = _headerGradientColorTertiary.asStateFlow()

    private val _qsTilesColorTertiary = MutableStateFlow(false)
    val qsTilesColorTertiary: StateFlow<Boolean> = _qsTilesColorTertiary.asStateFlow()

    private val _inactiveTilesColorTertiary = MutableStateFlow(false)
    val inactiveTilesColorTertiary: StateFlow<Boolean> = _inactiveTilesColorTertiary.asStateFlow()

    private val _inactiveTilesColorPrimary = MutableStateFlow(false)
    val inactiveTilesColorPrimary: StateFlow<Boolean> = _inactiveTilesColorPrimary.asStateFlow()

    private val _slidersColorTertiary = MutableStateFlow(false)
    val slidersColorTertiary: StateFlow<Boolean> = _slidersColorTertiary.asStateFlow()

    private val _miscElementsColorTertiary = MutableStateFlow(false)
    val miscElementsColorTertiary: StateFlow<Boolean> = _miscElementsColorTertiary.asStateFlow()

    // Surface Bg HSB
    private val _surfaceBgHue = MutableStateFlow(0f)
    val surfaceBgHue: StateFlow<Float> = _surfaceBgHue.asStateFlow()
    private val _surfaceBgSat = MutableStateFlow(1f)
    val surfaceBgSat: StateFlow<Float> = _surfaceBgSat.asStateFlow()
    private val _surfaceBgVal = MutableStateFlow(1f)
    val surfaceBgVal: StateFlow<Float> = _surfaceBgVal.asStateFlow()

    // Panel Container HSB
    private val _panelContainerHue = MutableStateFlow(0f)
    val panelContainerHue: StateFlow<Float> = _panelContainerHue.asStateFlow()
    private val _panelContainerSat = MutableStateFlow(1f)
    val panelContainerSat: StateFlow<Float> = _panelContainerSat.asStateFlow()
    private val _panelContainerVal = MutableStateFlow(1f)
    val panelContainerVal: StateFlow<Float> = _panelContainerVal.asStateFlow()

    // Header Gradient HSB
    private val _headerGradientHue = MutableStateFlow(0f)
    val headerGradientHue: StateFlow<Float> = _headerGradientHue.asStateFlow()
    private val _headerGradientSat = MutableStateFlow(1f)
    val headerGradientSat: StateFlow<Float> = _headerGradientSat.asStateFlow()
    private val _headerGradientVal = MutableStateFlow(1f)
    val headerGradientVal: StateFlow<Float> = _headerGradientVal.asStateFlow()

    // QS Tiles HSB
    private val _qsTilesHue = MutableStateFlow(0f)
    val qsTilesHue: StateFlow<Float> = _qsTilesHue.asStateFlow()
    private val _qsTilesSat = MutableStateFlow(1f)
    val qsTilesSat: StateFlow<Float> = _qsTilesSat.asStateFlow()
    private val _qsTilesVal = MutableStateFlow(1f)
    val qsTilesVal: StateFlow<Float> = _qsTilesVal.asStateFlow()

    // Inactive Tiles HSB
    private val _inactiveTilesHue = MutableStateFlow(0f)
    val inactiveTilesHue: StateFlow<Float> = _inactiveTilesHue.asStateFlow()
    private val _inactiveTilesSat = MutableStateFlow(1f)
    val inactiveTilesSat: StateFlow<Float> = _inactiveTilesSat.asStateFlow()
    private val _inactiveTilesVal = MutableStateFlow(1f)
    val inactiveTilesVal: StateFlow<Float> = _inactiveTilesVal.asStateFlow()

    // Sliders HSB
    private val _slidersHue = MutableStateFlow(0f)
    val slidersHue: StateFlow<Float> = _slidersHue.asStateFlow()
    private val _slidersSat = MutableStateFlow(1f)
    val slidersSat: StateFlow<Float> = _slidersSat.asStateFlow()
    private val _slidersVal = MutableStateFlow(1f)
    val slidersVal: StateFlow<Float> = _slidersVal.asStateFlow()

    // Misc Elements HSB
    private val _miscElementsHue = MutableStateFlow(0f)
    val miscElementsHue: StateFlow<Float> = _miscElementsHue.asStateFlow()
    private val _miscElementsSat = MutableStateFlow(1f)
    val miscElementsSat: StateFlow<Float> = _miscElementsSat.asStateFlow()
    private val _miscElementsVal = MutableStateFlow(1f)
    val miscElementsVal: StateFlow<Float> = _miscElementsVal.asStateFlow()

    private val _disableMonetNotifText = MutableStateFlow(false)
    val disableMonetNotifText: StateFlow<Boolean> = _disableMonetNotifText.asStateFlow()

    // Per-mode state flows: Light Mode
    private val _surfaceBgColorTertiaryLight = MutableStateFlow(false)
    val surfaceBgColorTertiaryLight: StateFlow<Boolean> = _surfaceBgColorTertiaryLight.asStateFlow()
    private val _panelContainerColorTertiaryLight = MutableStateFlow(false)
    val panelContainerColorTertiaryLight: StateFlow<Boolean> = _panelContainerColorTertiaryLight.asStateFlow()
    private val _headerGradientColorTertiaryLight = MutableStateFlow(false)
    val headerGradientColorTertiaryLight: StateFlow<Boolean> = _headerGradientColorTertiaryLight.asStateFlow()
    private val _qsTilesColorTertiaryLight = MutableStateFlow(false)
    val qsTilesColorTertiaryLight: StateFlow<Boolean> = _qsTilesColorTertiaryLight.asStateFlow()
    private val _inactiveTilesColorTertiaryLight = MutableStateFlow(false)
    val inactiveTilesColorTertiaryLight: StateFlow<Boolean> = _inactiveTilesColorTertiaryLight.asStateFlow()
    private val _inactiveTilesColorPrimaryLight = MutableStateFlow(false)
    val inactiveTilesColorPrimaryLight: StateFlow<Boolean> = _inactiveTilesColorPrimaryLight.asStateFlow()
    private val _slidersColorTertiaryLight = MutableStateFlow(false)
    val slidersColorTertiaryLight: StateFlow<Boolean> = _slidersColorTertiaryLight.asStateFlow()
    private val _miscElementsColorTertiaryLight = MutableStateFlow(false)
    val miscElementsColorTertiaryLight: StateFlow<Boolean> = _miscElementsColorTertiaryLight.asStateFlow()

    private val _surfaceBgHueLight = MutableStateFlow(0f)
    val surfaceBgHueLight: StateFlow<Float> = _surfaceBgHueLight.asStateFlow()
    private val _surfaceBgSatLight = MutableStateFlow(1f)
    val surfaceBgSatLight: StateFlow<Float> = _surfaceBgSatLight.asStateFlow()
    private val _surfaceBgValLight = MutableStateFlow(1f)
    val surfaceBgValLight: StateFlow<Float> = _surfaceBgValLight.asStateFlow()

    private val _panelContainerHueLight = MutableStateFlow(0f)
    val panelContainerHueLight: StateFlow<Float> = _panelContainerHueLight.asStateFlow()
    private val _panelContainerSatLight = MutableStateFlow(1f)
    val panelContainerSatLight: StateFlow<Float> = _panelContainerSatLight.asStateFlow()
    private val _panelContainerValLight = MutableStateFlow(1f)
    val panelContainerValLight: StateFlow<Float> = _panelContainerValLight.asStateFlow()

    private val _headerGradientHueLight = MutableStateFlow(0f)
    val headerGradientHueLight: StateFlow<Float> = _headerGradientHueLight.asStateFlow()
    private val _headerGradientSatLight = MutableStateFlow(1f)
    val headerGradientSatLight: StateFlow<Float> = _headerGradientSatLight.asStateFlow()
    private val _headerGradientValLight = MutableStateFlow(1f)
    val headerGradientValLight: StateFlow<Float> = _headerGradientValLight.asStateFlow()

    private val _qsTilesHueLight = MutableStateFlow(0f)
    val qsTilesHueLight: StateFlow<Float> = _qsTilesHueLight.asStateFlow()
    private val _qsTilesSatLight = MutableStateFlow(1f)
    val qsTilesSatLight: StateFlow<Float> = _qsTilesSatLight.asStateFlow()
    private val _qsTilesValLight = MutableStateFlow(1f)
    val qsTilesValLight: StateFlow<Float> = _qsTilesValLight.asStateFlow()

    private val _inactiveTilesHueLight = MutableStateFlow(0f)
    val inactiveTilesHueLight: StateFlow<Float> = _inactiveTilesHueLight.asStateFlow()
    private val _inactiveTilesSatLight = MutableStateFlow(1f)
    val inactiveTilesSatLight: StateFlow<Float> = _inactiveTilesSatLight.asStateFlow()
    private val _inactiveTilesValLight = MutableStateFlow(1f)
    val inactiveTilesValLight: StateFlow<Float> = _inactiveTilesValLight.asStateFlow()

    private val _slidersHueLight = MutableStateFlow(0f)
    val slidersHueLight: StateFlow<Float> = _slidersHueLight.asStateFlow()
    private val _slidersSatLight = MutableStateFlow(1f)
    val slidersSatLight: StateFlow<Float> = _slidersSatLight.asStateFlow()
    private val _slidersValLight = MutableStateFlow(1f)
    val slidersValLight: StateFlow<Float> = _slidersValLight.asStateFlow()

    private val _miscElementsHueLight = MutableStateFlow(0f)
    val miscElementsHueLight: StateFlow<Float> = _miscElementsHueLight.asStateFlow()
    private val _miscElementsSatLight = MutableStateFlow(1f)
    val miscElementsSatLight: StateFlow<Float> = _miscElementsSatLight.asStateFlow()
    private val _miscElementsValLight = MutableStateFlow(1f)
    val miscElementsValLight: StateFlow<Float> = _miscElementsValLight.asStateFlow()

    // Per-mode state flows: Dark Mode
    private val _surfaceBgColorTertiaryDark = MutableStateFlow(false)
    val surfaceBgColorTertiaryDark: StateFlow<Boolean> = _surfaceBgColorTertiaryDark.asStateFlow()
    private val _panelContainerColorTertiaryDark = MutableStateFlow(false)
    val panelContainerColorTertiaryDark: StateFlow<Boolean> = _panelContainerColorTertiaryDark.asStateFlow()
    private val _headerGradientColorTertiaryDark = MutableStateFlow(false)
    val headerGradientColorTertiaryDark: StateFlow<Boolean> = _headerGradientColorTertiaryDark.asStateFlow()
    private val _qsTilesColorTertiaryDark = MutableStateFlow(false)
    val qsTilesColorTertiaryDark: StateFlow<Boolean> = _qsTilesColorTertiaryDark.asStateFlow()
    private val _inactiveTilesColorTertiaryDark = MutableStateFlow(false)
    val inactiveTilesColorTertiaryDark: StateFlow<Boolean> = _inactiveTilesColorTertiaryDark.asStateFlow()
    private val _inactiveTilesColorPrimaryDark = MutableStateFlow(false)
    val inactiveTilesColorPrimaryDark: StateFlow<Boolean> = _inactiveTilesColorPrimaryDark.asStateFlow()
    private val _slidersColorTertiaryDark = MutableStateFlow(false)
    val slidersColorTertiaryDark: StateFlow<Boolean> = _slidersColorTertiaryDark.asStateFlow()
    private val _miscElementsColorTertiaryDark = MutableStateFlow(false)
    val miscElementsColorTertiaryDark: StateFlow<Boolean> = _miscElementsColorTertiaryDark.asStateFlow()

    private val _surfaceBgHueDark = MutableStateFlow(0f)
    val surfaceBgHueDark: StateFlow<Float> = _surfaceBgHueDark.asStateFlow()
    private val _surfaceBgSatDark = MutableStateFlow(1f)
    val surfaceBgSatDark: StateFlow<Float> = _surfaceBgSatDark.asStateFlow()
    private val _surfaceBgValDark = MutableStateFlow(1f)
    val surfaceBgValDark: StateFlow<Float> = _surfaceBgValDark.asStateFlow()

    private val _panelContainerHueDark = MutableStateFlow(0f)
    val panelContainerHueDark: StateFlow<Float> = _panelContainerHueDark.asStateFlow()
    private val _panelContainerSatDark = MutableStateFlow(1f)
    val panelContainerSatDark: StateFlow<Float> = _panelContainerSatDark.asStateFlow()
    private val _panelContainerValDark = MutableStateFlow(1f)
    val panelContainerValDark: StateFlow<Float> = _panelContainerValDark.asStateFlow()

    private val _headerGradientHueDark = MutableStateFlow(0f)
    val headerGradientHueDark: StateFlow<Float> = _headerGradientHueDark.asStateFlow()
    private val _headerGradientSatDark = MutableStateFlow(1f)
    val headerGradientSatDark: StateFlow<Float> = _headerGradientSatDark.asStateFlow()
    private val _headerGradientValDark = MutableStateFlow(1f)
    val headerGradientValDark: StateFlow<Float> = _headerGradientValDark.asStateFlow()

    private val _qsTilesHueDark = MutableStateFlow(0f)
    val qsTilesHueDark: StateFlow<Float> = _qsTilesHueDark.asStateFlow()
    private val _qsTilesSatDark = MutableStateFlow(1f)
    val qsTilesSatDark: StateFlow<Float> = _qsTilesSatDark.asStateFlow()
    private val _qsTilesValDark = MutableStateFlow(1f)
    val qsTilesValDark: StateFlow<Float> = _qsTilesValDark.asStateFlow()

    private val _inactiveTilesHueDark = MutableStateFlow(0f)
    val inactiveTilesHueDark: StateFlow<Float> = _inactiveTilesHueDark.asStateFlow()
    private val _inactiveTilesSatDark = MutableStateFlow(1f)
    val inactiveTilesSatDark: StateFlow<Float> = _inactiveTilesSatDark.asStateFlow()
    private val _inactiveTilesValDark = MutableStateFlow(1f)
    val inactiveTilesValDark: StateFlow<Float> = _inactiveTilesValDark.asStateFlow()

    private val _slidersHueDark = MutableStateFlow(0f)
    val slidersHueDark: StateFlow<Float> = _slidersHueDark.asStateFlow()
    private val _slidersSatDark = MutableStateFlow(1f)
    val slidersSatDark: StateFlow<Float> = _slidersSatDark.asStateFlow()
    private val _slidersValDark = MutableStateFlow(1f)
    val slidersValDark: StateFlow<Float> = _slidersValDark.asStateFlow()

    private val _miscElementsHueDark = MutableStateFlow(0f)
    val miscElementsHueDark: StateFlow<Float> = _miscElementsHueDark.asStateFlow()
    private val _miscElementsSatDark = MutableStateFlow(1f)
    val miscElementsSatDark: StateFlow<Float> = _miscElementsSatDark.asStateFlow()
    private val _miscElementsValDark = MutableStateFlow(1f)
    val miscElementsValDark: StateFlow<Float> = _miscElementsValDark.asStateFlow()

    private val _showQsTileGradient = MutableStateFlow(false)
    val showQsTileGradient: StateFlow<Boolean> = _showQsTileGradient.asStateFlow()

    private val _showBrightnessGradient = MutableStateFlow(false)
    val showBrightnessGradient: StateFlow<Boolean> = _showBrightnessGradient.asStateFlow()

    private val _qsTileLabelFontSize = MutableStateFlow(12.0f)
    val qsTileLabelFontSize: StateFlow<Float> = _qsTileLabelFontSize.asStateFlow()

    private val _qsTileSubtitleFontSize = MutableStateFlow(9.0f)
    val qsTileSubtitleFontSize: StateFlow<Float> = _qsTileSubtitleFontSize.asStateFlow()

    private val _wavyThickness = MutableStateFlow(4.0f)
    val wavyThickness: StateFlow<Float> = _wavyThickness.asStateFlow()

    private val _wavyWobbliness = MutableStateFlow(3.5f)
    val wavyWobbliness: StateFlow<Float> = _wavyWobbliness.asStateFlow()

    private val _wavyWavelength = MutableStateFlow(26.0f)
    val wavyWavelength: StateFlow<Float> = _wavyWavelength.asStateFlow()

    private val _wavyThumbRadius = MutableStateFlow(6.0f)
    val wavyThumbRadius: StateFlow<Float> = _wavyThumbRadius.asStateFlow()

    private val _wavySpeed = MutableStateFlow(1300)
    val wavySpeed: StateFlow<Int> = _wavySpeed.asStateFlow()

    private val _sliderInactiveTrackThickness = MutableStateFlow<Float?>(null)
    val sliderInactiveTrackThickness: StateFlow<Float?> = _sliderInactiveTrackThickness.asStateFlow()

    private val _sliderCornerRadius = MutableStateFlow(19.0f)
    val sliderCornerRadius: StateFlow<Float> = _sliderCornerRadius.asStateFlow()

    private val _sliderSyncAll = MutableStateFlow(true)
    val sliderSyncAll: StateFlow<Boolean> = _sliderSyncAll.asStateFlow()

    private val _brightnessSliderStyle = MutableStateFlow(ProgressBarStyle.DEFAULT)
    val brightnessSliderStyle: StateFlow<ProgressBarStyle> = _brightnessSliderStyle.asStateFlow()

    private val _volumeSliderStyle = MutableStateFlow(ProgressBarStyle.DEFAULT)
    val volumeSliderStyle: StateFlow<ProgressBarStyle> = _volumeSliderStyle.asStateFlow()

    private val _mediaSliderStyle = MutableStateFlow(ProgressBarStyle.DEFAULT)
    val mediaSliderStyle: StateFlow<ProgressBarStyle> = _mediaSliderStyle.asStateFlow()

    private val _a12InactiveTrackThickness = MutableStateFlow(38.0f)
    val a12InactiveTrackThickness: StateFlow<Float> = _a12InactiveTrackThickness.asStateFlow()

    private val _a12CornerRadius = MutableStateFlow(19.0f)
    val a12CornerRadius: StateFlow<Float> = _a12CornerRadius.asStateFlow()

    private val _wavyInactiveTrackThickness = MutableStateFlow(4.0f)
    val wavyInactiveTrackThickness: StateFlow<Float> = _wavyInactiveTrackThickness.asStateFlow()

    private val _wavyCornerRadius = MutableStateFlow(4.0f)
    val wavyCornerRadius: StateFlow<Float> = _wavyCornerRadius.asStateFlow()

    private val _straightThickness = MutableStateFlow(4.0f)
    val straightThickness: StateFlow<Float> = _straightThickness.asStateFlow()

    private val _straightInactiveTrackThickness = MutableStateFlow(4.0f)
    val straightInactiveTrackThickness: StateFlow<Float> = _straightInactiveTrackThickness.asStateFlow()

    private val _straightCornerRadius = MutableStateFlow(4.0f)
    val straightCornerRadius: StateFlow<Float> = _straightCornerRadius.asStateFlow()

    private val _straightThumbRadius = MutableStateFlow(6.0f)
    val straightThumbRadius: StateFlow<Float> = _straightThumbRadius.asStateFlow()

    private val _usePrimeCoreAffinity = MutableStateFlow(false)
    val usePrimeCoreAffinity: StateFlow<Boolean> = _usePrimeCoreAffinity.asStateFlow()

    private val _disablePanelAnimations = MutableStateFlow(false)
    val disablePanelAnimations: StateFlow<Boolean> = _disablePanelAnimations.asStateFlow()

    private val _animationSpeedScale = MutableStateFlow(1.0f)
    val animationSpeedScale: StateFlow<Float> = _animationSpeedScale.asStateFlow()

    private val _enableCustomVolumePanel = MutableStateFlow(true)
    val enableCustomVolumePanel: StateFlow<Boolean> = _enableCustomVolumePanel.asStateFlow()
    private const val KEY_HIDDEN_TILE_IDS = "hidden_tile_ids"
    private const val KEY_ENABLE_HEADS_UP_OVERLAY = "enable_heads_up_overlay"
    private const val KEY_HEADS_UP_TIMEOUT_SECONDS = "heads_up_timeout_seconds"
    private const val KEY_INITIAL_SETUP_COMPLETED = "initial_setup_completed"
    private val _initialSetupCompleted = MutableStateFlow(false)
    val initialSetupCompleted: StateFlow<Boolean> = _initialSetupCompleted.asStateFlow()

    // Privilege Engine Mode Preference
    private const val KEY_PRIVILEGE_MODE = "privilege_mode"
    private val _privilegeMode = MutableStateFlow(com.example.model.PrivilegeMode.STANDARD)
    val privilegeMode: StateFlow<com.example.model.PrivilegeMode> = _privilegeMode.asStateFlow()

    // Heads-Up Overlay Preferences
    private const val KEY_ENABLE_SYSTEM_HEADS_UP = "enable_system_heads_up"
    private val _enableSystemHeadsUp = MutableStateFlow(true)
    val enableSystemHeadsUp: StateFlow<Boolean> = _enableSystemHeadsUp.asStateFlow()

    private val _enableHeadsUpOverlay = MutableStateFlow(true)
    val enableHeadsUpOverlay: StateFlow<Boolean> = _enableHeadsUpOverlay.asStateFlow()

    private val _headsUpTimeoutSeconds = MutableStateFlow(4)
    val headsUpTimeoutSeconds: StateFlow<Int> = _headsUpTimeoutSeconds.asStateFlow()

    // Screen Recording Preferences
    private const val KEY_SCREEN_RECORD_RESOLUTION = "screen_record_resolution"
    private const val KEY_SCREEN_RECORD_AUDIO_SOURCE = "screen_record_audio_source"
    private const val KEY_SCREEN_RECORD_FPS = "screen_record_fps"
    private const val KEY_SCREEN_RECORD_SHOW_TOUCHES = "screen_record_show_touches"

    private val _screenRecordResolution = MutableStateFlow(ScreenRecordResolution.NATIVE)
    val screenRecordResolution: StateFlow<ScreenRecordResolution> = _screenRecordResolution.asStateFlow()

    private val _screenRecordAudioSource = MutableStateFlow(ScreenRecordAudioSource.MIC)
    val screenRecordAudioSource: StateFlow<ScreenRecordAudioSource> = _screenRecordAudioSource.asStateFlow()

    private val _screenRecordFps = MutableStateFlow(ScreenRecordFps.FPS_60)
    val screenRecordFps: StateFlow<ScreenRecordFps> = _screenRecordFps.asStateFlow()

    private val _screenRecordShowTouches = MutableStateFlow(false)
    val screenRecordShowTouches: StateFlow<Boolean> = _screenRecordShowTouches.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _hiddenTileIds = MutableStateFlow<Set<String>>(emptySet())
    val hiddenTileIds: StateFlow<Set<String>> = _hiddenTileIds.asStateFlow()

    private val _gridLayout = MutableStateFlow(QsGridLayout.DEFAULT)
    val gridLayout: StateFlow<QsGridLayout> = _gridLayout.asStateFlow()

    private val _qqsTileSize = MutableStateFlow(48)
    val qqsTileSize: StateFlow<Int> = _qqsTileSize.asStateFlow()

    private val _qqsTileCount = MutableStateFlow(6)
    val qqsTileCount: StateFlow<Int> = _qqsTileCount.asStateFlow()

    private val _editButtonPosition = MutableStateFlow(EditButtonPosition.FOOTER)
    val editButtonPosition: StateFlow<EditButtonPosition> = _editButtonPosition.asStateFlow()

    private val _showDateInQqsHeader = MutableStateFlow(false)
    val showDateInQqsHeader: StateFlow<Boolean> = _showDateInQqsHeader.asStateFlow()

    private val _hideDateInExpanded = MutableStateFlow(false)
    val hideDateInExpanded: StateFlow<Boolean> = _hideDateInExpanded.asStateFlow()

    private val _disableLabelsInExpanded = MutableStateFlow(false)
    val disableLabelsInExpanded: StateFlow<Boolean> = _disableLabelsInExpanded.asStateFlow()

    private val _pinnedFeatures = MutableStateFlow<List<String>>(emptyList())
    val pinnedFeatures: StateFlow<List<String>> = _pinnedFeatures.asStateFlow()

    private val _datePosition = MutableStateFlow(DatePosition.BELOW_TIME)
    val datePosition: StateFlow<DatePosition> = _datePosition.asStateFlow()

    private val _batteryPercentageLocation = MutableStateFlow(BatteryPercentageLocation.HEADER)
    val batteryPercentageLocation: StateFlow<BatteryPercentageLocation> = _batteryPercentageLocation.asStateFlow()

    private val _settingsIconLocation = MutableStateFlow(SettingsIconLocation.EXPANDED_QS)
    val settingsIconLocation: StateFlow<SettingsIconLocation> = _settingsIconLocation.asStateFlow()

    private val _disableClearAllBackground = MutableStateFlow(false)
    val disableClearAllBackground: StateFlow<Boolean> = _disableClearAllBackground.asStateFlow()

    private val _enablePanelShadows = MutableStateFlow(false)
    val enablePanelShadows: StateFlow<Boolean> = _enablePanelShadows.asStateFlow()

    private val _panelShadowElevation = MutableStateFlow(8f)
    val panelShadowElevation: StateFlow<Float> = _panelShadowElevation.asStateFlow()

    private val _panelShadowOffsetY = MutableStateFlow(0f)
    val panelShadowOffsetY: StateFlow<Float> = _panelShadowOffsetY.asStateFlow()

    private val _useClearAllFab = MutableStateFlow(false)
    val useClearAllFab: StateFlow<Boolean> = _useClearAllFab.asStateFlow()

    private val _clearAllFabPosition = MutableStateFlow(ClearAllFabPosition.CENTER)
    val clearAllFabPosition: StateFlow<ClearAllFabPosition> = _clearAllFabPosition.asStateFlow()

    private val _clearAllFabShape = MutableStateFlow(ClearAllFabShape.CIRCLE)
    val clearAllFabShape: StateFlow<ClearAllFabShape> = _clearAllFabShape.asStateFlow()

    private val _clearAllFabShapeId = MutableStateFlow("circle")
    val clearAllFabShapeId: StateFlow<String> = _clearAllFabShapeId.asStateFlow()

    private val _clearAllFabIcon = MutableStateFlow(ClearAllFabIcon.CLEAR_ALL)
    val clearAllFabIcon: StateFlow<ClearAllFabIcon> = _clearAllFabIcon.asStateFlow()

    private val _clearAllFabOffsetX = MutableStateFlow(0f)
    val clearAllFabOffsetX: StateFlow<Float> = _clearAllFabOffsetX.asStateFlow()

    private val _clearAllFabOffsetY = MutableStateFlow(0f)
    val clearAllFabOffsetY: StateFlow<Float> = _clearAllFabOffsetY.asStateFlow()

    private val _clearAllFabSize = MutableStateFlow(64)
    val clearAllFabSize: StateFlow<Int> = _clearAllFabSize.asStateFlow()

    private val _enabledTileShapeId = MutableStateFlow("circle")
    val enabledTileShapeId: StateFlow<String> = _enabledTileShapeId.asStateFlow()

    private val _disabledTileShapeId = MutableStateFlow("circle")
    val disabledTileShapeId: StateFlow<String> = _disabledTileShapeId.asStateFlow()

    private val _enabledTileShape = MutableStateFlow(TileShape.CIRCLE)
    val enabledTileShape: StateFlow<TileShape> = _enabledTileShape.asStateFlow()

    private val _disabledTileShape = MutableStateFlow(TileShape.CIRCLE)
    val disabledTileShape: StateFlow<TileShape> = _disabledTileShape.asStateFlow()

    private val _tileShape = _enabledTileShape
    val tileShape: StateFlow<TileShape> = _tileShape.asStateFlow()

    private val _activeSquareShape = MutableStateFlow(false)
    val activeSquareShape: StateFlow<Boolean> = _activeSquareShape.asStateFlow()

    private val _tileOutlineEnabled = MutableStateFlow(false)
    val tileOutlineEnabled: StateFlow<Boolean> = _tileOutlineEnabled.asStateFlow()

    private val _hideQqsDragHandle = MutableStateFlow(false)
    val hideQqsDragHandle: StateFlow<Boolean> = _hideQqsDragHandle.asStateFlow()

    private val _hideQqsEmptyNotifLabels = MutableStateFlow(false)
    val hideQqsEmptyNotifLabels: StateFlow<Boolean> = _hideQqsEmptyNotifLabels.asStateFlow()

    private val _dragHandleVerticalOffset = MutableStateFlow(4f)
    val dragHandleVerticalOffset: StateFlow<Float> = _dragHandleVerticalOffset.asStateFlow()

    private val _dragHandleLength = MutableStateFlow(36f)
    val dragHandleLength: StateFlow<Float> = _dragHandleLength.asStateFlow()

    private val _showCarrier = MutableStateFlow(true)
    val showCarrier: StateFlow<Boolean> = _showCarrier.asStateFlow()

    private val _showCarrierDivider = MutableStateFlow(true)
    val showCarrierDivider: StateFlow<Boolean> = _showCarrierDivider.asStateFlow()

    private val _hideCarrierInExpanded = MutableStateFlow(false)
    val hideCarrierInExpanded: StateFlow<Boolean> = _hideCarrierInExpanded.asStateFlow()

    private val _brightnessAtBottom = MutableStateFlow(false)
    val brightnessAtBottom: StateFlow<Boolean> = _brightnessAtBottom.asStateFlow()

    private val _brightnessInQqs = MutableStateFlow(false)
    val brightnessInQqs: StateFlow<Boolean> = _brightnessInQqs.asStateFlow()

    private val _hideBrightnessPercentage = MutableStateFlow(false)
    val hideBrightnessPercentage: StateFlow<Boolean> = _hideBrightnessPercentage.asStateFlow()

    private val _hideBrightnessIcon = MutableStateFlow(false)
    val hideBrightnessIcon: StateFlow<Boolean> = _hideBrightnessIcon.asStateFlow()

    private val _showBrightnessPlusMinus = MutableStateFlow(false)
    val showBrightnessPlusMinus: StateFlow<Boolean> = _showBrightnessPlusMinus.asStateFlow()

    private val _minBrightness = MutableStateFlow(1)
    val minBrightness: StateFlow<Int> = _minBrightness.asStateFlow()

    private val _maxBrightness = MutableStateFlow(2047)
    val maxBrightness: StateFlow<Int> = _maxBrightness.asStateFlow()

    private val _progressBarStyle = MutableStateFlow(ProgressBarStyle.DEFAULT)
    val progressBarStyle: StateFlow<ProgressBarStyle> = _progressBarStyle.asStateFlow()

    private val _wavyProgressBars = MutableStateFlow(false)
    val wavyProgressBars: StateFlow<Boolean> = _wavyProgressBars.asStateFlow()

    private val _enableContainerBlur = MutableStateFlow(false)
    val enableContainerBlur: StateFlow<Boolean> = _enableContainerBlur.asStateFlow()

    private val _invisibleStatusBarTrigger = MutableStateFlow(false)
    val invisibleStatusBarTrigger: StateFlow<Boolean> = _invisibleStatusBarTrigger.asStateFlow()

    private val _forceImmersiveTrigger = MutableStateFlow(false)
    val forceImmersiveTrigger: StateFlow<Boolean> = _forceImmersiveTrigger.asStateFlow()

    private val _overrideSystemQs = MutableStateFlow(false)
    val overrideSystemQs: StateFlow<Boolean> = _overrideSystemQs.asStateFlow()

    private val _autoSystemPanelClosing = MutableStateFlow(android.os.Build.VERSION.SDK_INT in 31..35)
    val autoSystemPanelClosing: StateFlow<Boolean> = _autoSystemPanelClosing.asStateFlow()

    private val _detectorCooldownMs = MutableStateFlow(280)
    val detectorCooldownMs: StateFlow<Int> = _detectorCooldownMs.asStateFlow()

    private val _panelReplacementDelayMs = MutableStateFlow(0)
    val panelReplacementDelayMs: StateFlow<Int> = _panelReplacementDelayMs.asStateFlow()

    private val defaultWhitelist = setOf(
        "com.android.systemui",
        "com.google.android.systemui",
        "com.miui.systemui",
        "com.samsung.android.systemui",
        "com.nothing.systemui",
        "com.oneplus.systemui",
        "com.oppo.systemui",
        "com.vivo.systemui",
        "com.motorola.systemui",
        "com.transsion.systemui",
        "com.realme.systemui",
        "com.asus.systemui",
        "com.huawei.systemui",
        "com.honor.systemui",
        "com.sony.systemui",
        "com.lge.systemui"
    )

    private val _stockShadeWhitelist = MutableStateFlow<Set<String>>(defaultWhitelist)
    val stockShadeWhitelist: StateFlow<Set<String>> = _stockShadeWhitelist.asStateFlow()

    private val _customBlacklistPackages = MutableStateFlow<Set<String>>(emptySet())
    val customBlacklistPackages: StateFlow<Set<String>> = _customBlacklistPackages.asStateFlow()

    private val _customBlacklistClasses = MutableStateFlow<Set<String>>(emptySet())
    val customBlacklistClasses: StateFlow<Set<String>> = _customBlacklistClasses.asStateFlow()

    private val _forceShowMediaPreview = MutableStateFlow(false)
    val forceShowMediaPreview: StateFlow<Boolean> = _forceShowMediaPreview.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val themeSaved = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.id)
        _themeMode.value = ThemeMode.fromId(themeSaved)
        val saved = prefs.getString(KEY_GRID_LAYOUT, QsGridLayout.DEFAULT.id)
        _gridLayout.value = QsGridLayout.fromId(saved)
        val savedTileSize = prefs.getInt(KEY_QQS_TILE_SIZE, 48)
        _qqsTileSize.value = if (savedTileSize in 32..72) savedTileSize else 48
        _qqsTileCount.value = prefs.getInt(KEY_QQS_TILE_COUNT, 6)
        val editPosSaved = prefs.getString(KEY_EDIT_BUTTON_POSITION, EditButtonPosition.FOOTER.id)
        _editButtonPosition.value = EditButtonPosition.fromId(editPosSaved)

        val savedDatePos = prefs.getString(KEY_DATE_POSITION, null)
        if (savedDatePos != null) {
            _datePosition.value = DatePosition.fromId(savedDatePos)
            _showDateInQqsHeader.value = (_datePosition.value == DatePosition.NEXT_TO_TIME)
        } else {
            val legacyInline = prefs.getBoolean(KEY_SHOW_DATE_IN_QQS_HEADER, false)
            _datePosition.value = if (legacyInline) DatePosition.NEXT_TO_TIME else DatePosition.BELOW_TIME
            _showDateInQqsHeader.value = legacyInline
        }

        val savedBatteryLoc = prefs.getString("battery_percentage_location", BatteryPercentageLocation.HEADER.id)
        _batteryPercentageLocation.value = BatteryPercentageLocation.fromId(savedBatteryLoc)

        val savedSettingsLoc = prefs.getString("settings_icon_location", SettingsIconLocation.EXPANDED_QS.id)
        _settingsIconLocation.value = SettingsIconLocation.fromId(savedSettingsLoc)

        _disableClearAllBackground.value = prefs.getBoolean(KEY_DISABLE_CLEAR_ALL_BG, false)
        _enablePanelShadows.value = prefs.getBoolean(KEY_ENABLE_PANEL_SHADOWS, false)
        _panelShadowElevation.value = prefs.getFloat(KEY_PANEL_SHADOW_ELEVATION, 8f)
        _panelShadowOffsetY.value = prefs.getFloat(KEY_PANEL_SHADOW_OFFSET_Y, 0f)
        _useClearAllFab.value = prefs.getBoolean(KEY_USE_CLEAR_ALL_FAB, false)
        _clearAllFabPosition.value = ClearAllFabPosition.fromId(prefs.getString(KEY_CLEAR_ALL_FAB_POSITION, null))
        _clearAllFabShape.value = ClearAllFabShape.fromId(prefs.getString(KEY_CLEAR_ALL_FAB_SHAPE, null))
        val savedFabShapeId = prefs.getString(KEY_CLEAR_ALL_FAB_SHAPE_ID, null)
            ?: prefs.getString(KEY_CLEAR_ALL_FAB_SHAPE, "circle") ?: "circle"
        _clearAllFabShapeId.value = savedFabShapeId
        _clearAllFabIcon.value = ClearAllFabIcon.fromId(prefs.getString(KEY_CLEAR_ALL_FAB_ICON, null))
        _clearAllFabOffsetX.value = prefs.getFloat(KEY_CLEAR_ALL_FAB_OFFSET_X, 0f)
        _clearAllFabOffsetY.value = prefs.getFloat(KEY_CLEAR_ALL_FAB_OFFSET_Y, 0f)
        _clearAllFabSize.value = prefs.getInt(KEY_CLEAR_ALL_FAB_SIZE, 64)
        com.example.ui.shape.QsShapeManager.init(context)
        val savedEnabledTileShape = prefs.getString(KEY_ENABLED_TILE_SHAPE, null) ?: prefs.getString(KEY_TILE_SHAPE, null)
        if (savedEnabledTileShape != null) {
            _enabledTileShapeId.value = savedEnabledTileShape
            val shape = TileShape.fromId(savedEnabledTileShape)
            _enabledTileShape.value = shape
            _activeSquareShape.value = (shape == TileShape.SQUIRCLE || shape == TileShape.SQUARE)
        } else {
            val legacySquare = prefs.getBoolean(KEY_ACTIVE_SQUARE_SHAPE, false)
            _activeSquareShape.value = legacySquare
            val shape = if (legacySquare) TileShape.SQUIRCLE else TileShape.CIRCLE
            _enabledTileShape.value = shape
            _enabledTileShapeId.value = shape.id
        }
        val savedDisabledTileShape = prefs.getString(KEY_DISABLED_TILE_SHAPE, null)
        if (savedDisabledTileShape != null) {
            _disabledTileShapeId.value = savedDisabledTileShape
            _disabledTileShape.value = TileShape.fromId(savedDisabledTileShape)
        } else {
            _disabledTileShapeId.value = "circle"
            _disabledTileShape.value = TileShape.CIRCLE
        }
        _tileOutlineEnabled.value = prefs.getBoolean(KEY_TILE_OUTLINE_ENABLED, false)
        _hideQqsDragHandle.value = prefs.getBoolean(KEY_HIDE_QQS_DRAG_HANDLE, false)
        _hideQqsEmptyNotifLabels.value = prefs.getBoolean(KEY_HIDE_QQS_EMPTY_NOTIF_LABELS, false)
        _dragHandleVerticalOffset.value = prefs.getFloat(KEY_DRAG_HANDLE_VERTICAL_OFFSET, 4f)
        _dragHandleLength.value = prefs.getFloat(KEY_DRAG_HANDLE_LENGTH, 36f)
        _showCarrier.value = prefs.getBoolean(KEY_SHOW_CARRIER, true)
        _showCarrierDivider.value = prefs.getBoolean(KEY_SHOW_CARRIER_DIVIDER, true)
        _hideCarrierInExpanded.value = prefs.getBoolean(KEY_HIDE_CARRIER_IN_EXPANDED, false)
        _brightnessAtBottom.value = prefs.getBoolean(KEY_BRIGHTNESS_AT_BOTTOM, false)
        _brightnessInQqs.value = prefs.getBoolean(KEY_BRIGHTNESS_IN_QQS, false)
        _hideBrightnessPercentage.value = prefs.getBoolean(KEY_HIDE_BRIGHTNESS_PERCENTAGE, false)
        _hideBrightnessIcon.value = prefs.getBoolean(KEY_HIDE_BRIGHTNESS_ICON, false)
        _showBrightnessPlusMinus.value = prefs.getBoolean(KEY_SHOW_BRIGHTNESS_PLUS_MINUS, false)
        _showBrightnessGradient.value = prefs.getBoolean(KEY_SHOW_BRIGHTNESS_GRADIENT, false)
        _minBrightness.value = prefs.getInt(KEY_MIN_BRIGHTNESS, 1)
        _maxBrightness.value = prefs.getInt(KEY_MAX_BRIGHTNESS, 2047)
        val savedStyleId = prefs.getString(KEY_PROGRESS_BAR_STYLE, null)
        val resolvedStyle = if (savedStyleId != null) {
            ProgressBarStyle.fromId(savedStyleId)
        } else {
            if (prefs.contains(KEY_WAVY_PROGRESS_BARS)) {
                if (prefs.getBoolean(KEY_WAVY_PROGRESS_BARS, false)) ProgressBarStyle.WAVY else ProgressBarStyle.DEFAULT
            } else {
                ProgressBarStyle.DEFAULT
            }
        }
        _progressBarStyle.value = resolvedStyle
        _wavyProgressBars.value = (resolvedStyle == ProgressBarStyle.WAVY)
        _enableContainerBlur.value = prefs.getBoolean(KEY_ENABLE_CONTAINER_BLUR, false)
        _invisibleStatusBarTrigger.value = prefs.getBoolean(KEY_INVISIBLE_STATUS_BAR_TRIGGER, false)
        _forceImmersiveTrigger.value = prefs.getBoolean(KEY_FORCE_IMMERSIVE_TRIGGER, false)
        _overrideSystemQs.value = prefs.getBoolean(KEY_OVERRIDE_SYSTEM_QS, false)
        _autoSystemPanelClosing.value = prefs.getBoolean(
            KEY_AUTO_SYSTEM_PANEL_CLOSING,
            android.os.Build.VERSION.SDK_INT in 31..35
        )
        val savedCooldown = prefs.getInt(KEY_DETECTOR_COOLDOWN_MS, 280)
        _detectorCooldownMs.value = savedCooldown.coerceIn(10, 1000)
        val savedReplacementDelay = prefs.getInt(KEY_PANEL_REPLACEMENT_DELAY_MS, 0)
        _panelReplacementDelayMs.value = savedReplacementDelay.coerceIn(0, 1000)
        val savedWhitelist = prefs.getStringSet(KEY_STOCK_SHADE_WHITELIST, null)
        if (!savedWhitelist.isNullOrEmpty()) {
            _stockShadeWhitelist.value = savedWhitelist
        } else {
            _stockShadeWhitelist.value = defaultWhitelist
        }
        val savedBlacklistPkgs = prefs.getStringSet(KEY_CUSTOM_BLACKLIST_PACKAGES, null)
        if (!savedBlacklistPkgs.isNullOrEmpty()) {
            _customBlacklistPackages.value = savedBlacklistPkgs
        }
        val savedBlacklistClasses = prefs.getStringSet(KEY_CUSTOM_BLACKLIST_CLASSES, null)
        if (!savedBlacklistClasses.isNullOrEmpty()) {
            _customBlacklistClasses.value = savedBlacklistClasses
        }
        _forceShowMediaPreview.value = prefs.getBoolean(KEY_FORCE_SHOW_MEDIA_PREVIEW, false)
        _enableCustomVolumePanel.value = prefs.getBoolean(KEY_ENABLE_CUSTOM_VOLUME_PANEL, true)
        _enableSystemHeadsUp.value = prefs.getBoolean(KEY_ENABLE_SYSTEM_HEADS_UP, true)
        _enableHeadsUpOverlay.value = prefs.getBoolean(KEY_ENABLE_HEADS_UP_OVERLAY, true)
        _headsUpTimeoutSeconds.value = prefs.getInt(KEY_HEADS_UP_TIMEOUT_SECONDS, 4)
        val hiddenSaved = prefs.getString(KEY_HIDDEN_TILE_IDS, "") ?: ""
        _hiddenTileIds.value = hiddenSaved.split(",").filter { it.isNotBlank() }.toSet()

        // Screen Record
        val resSaved = prefs.getString(KEY_SCREEN_RECORD_RESOLUTION, ScreenRecordResolution.NATIVE.id)
        _screenRecordResolution.value = ScreenRecordResolution.fromId(resSaved)
        val audioSaved = prefs.getString(KEY_SCREEN_RECORD_AUDIO_SOURCE, ScreenRecordAudioSource.MIC.id)
        _screenRecordAudioSource.value = ScreenRecordAudioSource.fromId(audioSaved)
        val fpsSaved = prefs.getString(KEY_SCREEN_RECORD_FPS, ScreenRecordFps.FPS_60.id)
        _screenRecordFps.value = ScreenRecordFps.fromId(fpsSaved)
        _screenRecordShowTouches.value = prefs.getBoolean(KEY_SCREEN_RECORD_SHOW_TOUCHES, false)

        val sysAnimatorScale = try {
            android.provider.Settings.Global.getFloat(context.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f)
        } catch (e: Exception) {
            1.0f
        }
        _animationSpeedScale.value = prefs.getFloat(KEY_ANIMATION_SPEED_SCALE, sysAnimatorScale)
        _disablePanelAnimations.value = prefs.getBoolean(KEY_DISABLE_PANEL_ANIMATIONS, false)
        _usePrimeCoreAffinity.value = prefs.getBoolean(KEY_USE_PRIME_CORE_AFFINITY, false)
        _showCustomHeaderImage.value = prefs.getBoolean(KEY_SHOW_CUSTOM_HEADER_IMAGE, false)
        _customHeaderImageUri.value = prefs.getString(KEY_CUSTOM_HEADER_IMAGE_URI, "") ?: ""
        _customHeaderImageTitle.value = prefs.getString(KEY_CUSTOM_HEADER_IMAGE_TITLE, "Header Banner") ?: "Header Banner"
        _customHeaderImageHeight.value = prefs.getFloat(KEY_CUSTOM_HEADER_IMAGE_HEIGHT, 85f)
        _customHeaderImageOpacity.value = prefs.getFloat(KEY_CUSTOM_HEADER_IMAGE_OPACITY, 1.0f)
        _showCustomHeaderMonetGradient.value = prefs.getBoolean(KEY_SHOW_CUSTOM_HEADER_MONET_GRADIENT, false)
        _customHeaderMonetGradientHeight.value = prefs.getFloat(KEY_CUSTOM_HEADER_MONET_GRADIENT_HEIGHT, 85f)
        _showMonetSurfaceBackground.value = prefs.getBoolean(KEY_SHOW_MONET_SURFACE_BACKGROUND, false)
        _collapseExpandOptions.value = prefs.getBoolean(KEY_COLLAPSE_EXPAND_OPTIONS, false)
        _collapseExpandSubheaders.value = prefs.getBoolean(KEY_COLLAPSE_EXPAND_SUBHEADERS, false)
        _surfaceBgColorTertiary.value = prefs.getBoolean(KEY_ELEM_SURFACE_BG_TERTIARY, false)
        _panelContainerColorTertiary.value = prefs.getBoolean(KEY_ELEM_PANEL_CONTAINER_TERTIARY, false)
        _notifContainerColorTertiary.value = prefs.getBoolean(KEY_ELEM_NOTIF_CONTAINER_TERTIARY, false)
        _headerGradientColorTertiary.value = prefs.getBoolean(KEY_ELEM_HEADER_GRADIENT_TERTIARY, false)
        _qsTilesColorTertiary.value = prefs.getBoolean(KEY_ELEM_QS_TILES_TERTIARY, false)
        _inactiveTilesColorTertiary.value = prefs.getBoolean(KEY_ELEM_INACTIVE_TILES_TERTIARY, false)
        _inactiveTilesColorPrimary.value = prefs.getBoolean(KEY_ELEM_INACTIVE_TILES_PRIMARY, false)
        _slidersColorTertiary.value = prefs.getBoolean(KEY_ELEM_SLIDERS_TERTIARY, false)
        _miscElementsColorTertiary.value = prefs.getBoolean(KEY_ELEM_MISC_ELEMENTS_TERTIARY, false)

        _surfaceBgHue.value = prefs.getFloat(KEY_ELEM_SURFACE_BG_HUE, 0f)
        _surfaceBgSat.value = prefs.getFloat(KEY_ELEM_SURFACE_BG_SAT, 1f)
        _surfaceBgVal.value = prefs.getFloat(KEY_ELEM_SURFACE_BG_VAL, 1f)

        _panelContainerHue.value = prefs.getFloat(KEY_ELEM_PANEL_CONTAINER_HUE, 0f)
        _panelContainerSat.value = prefs.getFloat(KEY_ELEM_PANEL_CONTAINER_SAT, 1f)
        _panelContainerVal.value = prefs.getFloat(KEY_ELEM_PANEL_CONTAINER_VAL, 1f)

        _headerGradientHue.value = prefs.getFloat(KEY_ELEM_HEADER_GRADIENT_HUE, 0f)
        _headerGradientSat.value = prefs.getFloat(KEY_ELEM_HEADER_GRADIENT_SAT, 1f)
        _headerGradientVal.value = prefs.getFloat(KEY_ELEM_HEADER_GRADIENT_VAL, 1f)

        _qsTilesHue.value = prefs.getFloat(KEY_ELEM_QS_TILES_HUE, 0f)
        _qsTilesSat.value = prefs.getFloat(KEY_ELEM_QS_TILES_SAT, 1f)
        _qsTilesVal.value = prefs.getFloat(KEY_ELEM_QS_TILES_VAL, 1f)

        _inactiveTilesHue.value = prefs.getFloat(KEY_ELEM_INACTIVE_TILES_HUE, 0f)
        _inactiveTilesSat.value = prefs.getFloat(KEY_ELEM_INACTIVE_TILES_SAT, 1f)
        _inactiveTilesVal.value = prefs.getFloat(KEY_ELEM_INACTIVE_TILES_VAL, 1f)

        _slidersHue.value = prefs.getFloat(KEY_ELEM_SLIDERS_HUE, 0f)
        _slidersSat.value = prefs.getFloat(KEY_ELEM_SLIDERS_SAT, 1f)
        _slidersVal.value = prefs.getFloat(KEY_ELEM_SLIDERS_VAL, 1f)

        _miscElementsHue.value = prefs.getFloat(KEY_ELEM_MISC_ELEMENTS_HUE, 0f)
        _miscElementsSat.value = prefs.getFloat(KEY_ELEM_MISC_ELEMENTS_SAT, 1f)
        _miscElementsVal.value = prefs.getFloat(KEY_ELEM_MISC_ELEMENTS_VAL, 1f)

        // Initialize Light & Dark mode settings
        val defSurfaceBgTertiary = _surfaceBgColorTertiary.value
        _surfaceBgColorTertiaryLight.value = prefs.getBoolean(KEY_ELEM_SURFACE_BG_TERTIARY_LIGHT, defSurfaceBgTertiary)
        _surfaceBgColorTertiaryDark.value = prefs.getBoolean(KEY_ELEM_SURFACE_BG_TERTIARY_DARK, defSurfaceBgTertiary)

        val defPanelContainerTertiary = _panelContainerColorTertiary.value
        _panelContainerColorTertiaryLight.value = prefs.getBoolean(KEY_ELEM_PANEL_CONTAINER_TERTIARY_LIGHT, defPanelContainerTertiary)
        _panelContainerColorTertiaryDark.value = prefs.getBoolean(KEY_ELEM_PANEL_CONTAINER_TERTIARY_DARK, defPanelContainerTertiary)

        val defHeaderGradientTertiary = _headerGradientColorTertiary.value
        _headerGradientColorTertiaryLight.value = prefs.getBoolean(KEY_ELEM_HEADER_GRADIENT_TERTIARY_LIGHT, defHeaderGradientTertiary)
        _headerGradientColorTertiaryDark.value = prefs.getBoolean(KEY_ELEM_HEADER_GRADIENT_TERTIARY_DARK, defHeaderGradientTertiary)

        val defQsTilesTertiary = _qsTilesColorTertiary.value
        _qsTilesColorTertiaryLight.value = prefs.getBoolean(KEY_ELEM_QS_TILES_TERTIARY_LIGHT, defQsTilesTertiary)
        _qsTilesColorTertiaryDark.value = prefs.getBoolean(KEY_ELEM_QS_TILES_TERTIARY_DARK, defQsTilesTertiary)

        val defInactiveTilesTertiary = _inactiveTilesColorTertiary.value
        _inactiveTilesColorTertiaryLight.value = prefs.getBoolean(KEY_ELEM_INACTIVE_TILES_TERTIARY_LIGHT, defInactiveTilesTertiary)
        _inactiveTilesColorTertiaryDark.value = prefs.getBoolean(KEY_ELEM_INACTIVE_TILES_TERTIARY_DARK, defInactiveTilesTertiary)

        val defInactiveTilesPrimary = _inactiveTilesColorPrimary.value
        _inactiveTilesColorPrimaryLight.value = prefs.getBoolean(KEY_ELEM_INACTIVE_TILES_PRIMARY_LIGHT, defInactiveTilesPrimary)
        _inactiveTilesColorPrimaryDark.value = prefs.getBoolean(KEY_ELEM_INACTIVE_TILES_PRIMARY_DARK, defInactiveTilesPrimary)

        val defSlidersTertiary = _slidersColorTertiary.value
        _slidersColorTertiaryLight.value = prefs.getBoolean(KEY_ELEM_SLIDERS_TERTIARY_LIGHT, defSlidersTertiary)
        _slidersColorTertiaryDark.value = prefs.getBoolean(KEY_ELEM_SLIDERS_TERTIARY_DARK, defSlidersTertiary)

        val defMiscElementsTertiary = _miscElementsColorTertiary.value
        _miscElementsColorTertiaryLight.value = prefs.getBoolean(KEY_ELEM_MISC_ELEMENTS_TERTIARY_LIGHT, defMiscElementsTertiary)
        _miscElementsColorTertiaryDark.value = prefs.getBoolean(KEY_ELEM_MISC_ELEMENTS_TERTIARY_DARK, defMiscElementsTertiary)

        val defSurfaceBgHue = _surfaceBgHue.value
        val defSurfaceBgSat = _surfaceBgSat.value
        val defSurfaceBgVal = _surfaceBgVal.value
        _surfaceBgHueLight.value = prefs.getFloat(KEY_ELEM_SURFACE_BG_HUE_LIGHT, defSurfaceBgHue)
        _surfaceBgSatLight.value = prefs.getFloat(KEY_ELEM_SURFACE_BG_SAT_LIGHT, defSurfaceBgSat)
        _surfaceBgValLight.value = prefs.getFloat(KEY_ELEM_SURFACE_BG_VAL_LIGHT, defSurfaceBgVal)
        _surfaceBgHueDark.value = prefs.getFloat(KEY_ELEM_SURFACE_BG_HUE_DARK, defSurfaceBgHue)
        _surfaceBgSatDark.value = prefs.getFloat(KEY_ELEM_SURFACE_BG_SAT_DARK, defSurfaceBgSat)
        _surfaceBgValDark.value = prefs.getFloat(KEY_ELEM_SURFACE_BG_VAL_DARK, defSurfaceBgVal)

        val defPanelContainerHue = _panelContainerHue.value
        val defPanelContainerSat = _panelContainerSat.value
        val defPanelContainerVal = _panelContainerVal.value
        _panelContainerHueLight.value = prefs.getFloat(KEY_ELEM_PANEL_CONTAINER_HUE_LIGHT, defPanelContainerHue)
        _panelContainerSatLight.value = prefs.getFloat(KEY_ELEM_PANEL_CONTAINER_SAT_LIGHT, defPanelContainerSat)
        _panelContainerValLight.value = prefs.getFloat(KEY_ELEM_PANEL_CONTAINER_VAL_LIGHT, defPanelContainerVal)
        _panelContainerHueDark.value = prefs.getFloat(KEY_ELEM_PANEL_CONTAINER_HUE_DARK, defPanelContainerHue)
        _panelContainerSatDark.value = prefs.getFloat(KEY_ELEM_PANEL_CONTAINER_SAT_DARK, defPanelContainerSat)
        _panelContainerValDark.value = prefs.getFloat(KEY_ELEM_PANEL_CONTAINER_VAL_DARK, defPanelContainerVal)

        val defHeaderGradientHue = _headerGradientHue.value
        val defHeaderGradientSat = _headerGradientSat.value
        val defHeaderGradientVal = _headerGradientVal.value
        _headerGradientHueLight.value = prefs.getFloat(KEY_ELEM_HEADER_GRADIENT_HUE_LIGHT, defHeaderGradientHue)
        _headerGradientSatLight.value = prefs.getFloat(KEY_ELEM_HEADER_GRADIENT_SAT_LIGHT, defHeaderGradientSat)
        _headerGradientValLight.value = prefs.getFloat(KEY_ELEM_HEADER_GRADIENT_VAL_LIGHT, defHeaderGradientVal)
        _headerGradientHueDark.value = prefs.getFloat(KEY_ELEM_HEADER_GRADIENT_HUE_DARK, defHeaderGradientHue)
        _headerGradientSatDark.value = prefs.getFloat(KEY_ELEM_HEADER_GRADIENT_SAT_DARK, defHeaderGradientSat)
        _headerGradientValDark.value = prefs.getFloat(KEY_ELEM_HEADER_GRADIENT_VAL_DARK, defHeaderGradientVal)

        val defQsTilesHue = _qsTilesHue.value
        val defQsTilesSat = _qsTilesSat.value
        val defQsTilesVal = _qsTilesVal.value
        _qsTilesHueLight.value = prefs.getFloat(KEY_ELEM_QS_TILES_HUE_LIGHT, defQsTilesHue)
        _qsTilesSatLight.value = prefs.getFloat(KEY_ELEM_QS_TILES_SAT_LIGHT, defQsTilesSat)
        _qsTilesValLight.value = prefs.getFloat(KEY_ELEM_QS_TILES_VAL_LIGHT, defQsTilesVal)
        _qsTilesHueDark.value = prefs.getFloat(KEY_ELEM_QS_TILES_HUE_DARK, defQsTilesHue)
        _qsTilesSatDark.value = prefs.getFloat(KEY_ELEM_QS_TILES_SAT_DARK, defQsTilesSat)
        _qsTilesValDark.value = prefs.getFloat(KEY_ELEM_QS_TILES_VAL_DARK, defQsTilesVal)

        val defInactiveTilesHue = _inactiveTilesHue.value
        val defInactiveTilesSat = _inactiveTilesSat.value
        val defInactiveTilesVal = _inactiveTilesVal.value
        _inactiveTilesHueLight.value = prefs.getFloat(KEY_ELEM_INACTIVE_TILES_HUE_LIGHT, defInactiveTilesHue)
        _inactiveTilesSatLight.value = prefs.getFloat(KEY_ELEM_INACTIVE_TILES_SAT_LIGHT, defInactiveTilesSat)
        _inactiveTilesValLight.value = prefs.getFloat(KEY_ELEM_INACTIVE_TILES_VAL_LIGHT, defInactiveTilesVal)
        _inactiveTilesHueDark.value = prefs.getFloat(KEY_ELEM_INACTIVE_TILES_HUE_DARK, defInactiveTilesHue)
        _inactiveTilesSatDark.value = prefs.getFloat(KEY_ELEM_INACTIVE_TILES_SAT_DARK, defInactiveTilesSat)
        _inactiveTilesValDark.value = prefs.getFloat(KEY_ELEM_INACTIVE_TILES_VAL_DARK, defInactiveTilesVal)

        val defSlidersHue = _slidersHue.value
        val defSlidersSat = _slidersSat.value
        val defSlidersVal = _slidersVal.value
        _slidersHueLight.value = prefs.getFloat(KEY_ELEM_SLIDERS_HUE_LIGHT, defSlidersHue)
        _slidersSatLight.value = prefs.getFloat(KEY_ELEM_SLIDERS_SAT_LIGHT, defSlidersSat)
        _slidersValLight.value = prefs.getFloat(KEY_ELEM_SLIDERS_VAL_LIGHT, defSlidersVal)
        _slidersHueDark.value = prefs.getFloat(KEY_ELEM_SLIDERS_HUE_DARK, defSlidersHue)
        _slidersSatDark.value = prefs.getFloat(KEY_ELEM_SLIDERS_SAT_DARK, defSlidersSat)
        _slidersValDark.value = prefs.getFloat(KEY_ELEM_SLIDERS_VAL_DARK, defSlidersVal)

        val defMiscElementsHue = _miscElementsHue.value
        val defMiscElementsSat = _miscElementsSat.value
        val defMiscElementsVal = _miscElementsVal.value
        _miscElementsHueLight.value = prefs.getFloat(KEY_ELEM_MISC_ELEMENTS_HUE_LIGHT, defMiscElementsHue)
        _miscElementsSatLight.value = prefs.getFloat(KEY_ELEM_MISC_ELEMENTS_SAT_LIGHT, defMiscElementsSat)
        _miscElementsValLight.value = prefs.getFloat(KEY_ELEM_MISC_ELEMENTS_VAL_LIGHT, defMiscElementsVal)
        _miscElementsHueDark.value = prefs.getFloat(KEY_ELEM_MISC_ELEMENTS_HUE_DARK, defMiscElementsHue)
        _miscElementsSatDark.value = prefs.getFloat(KEY_ELEM_MISC_ELEMENTS_SAT_DARK, defMiscElementsSat)
        _miscElementsValDark.value = prefs.getFloat(KEY_ELEM_MISC_ELEMENTS_VAL_DARK, defMiscElementsVal)
        _disableMonetNotifText.value = prefs.getBoolean(KEY_DISABLE_MONET_NOTIF_TEXT, false)
        _showQsTileGradient.value = prefs.getBoolean(KEY_SHOW_QS_TILE_GRADIENT, false)
        _qsTileLabelFontSize.value = prefs.getFloat(KEY_QS_TILE_LABEL_FONT_SIZE, 12.0f)
        _qsTileSubtitleFontSize.value = prefs.getFloat(KEY_QS_TILE_SUBTITLE_FONT_SIZE, 9.0f)
        _hideDateInExpanded.value = prefs.getBoolean(KEY_HIDE_DATE_IN_EXPANDED, false)
        _disableLabelsInExpanded.value = prefs.getBoolean(KEY_DISABLE_LABELS_IN_EXPANDED, false)
        _pinnedFeatures.value = (prefs.getStringSet(KEY_PINNED_FEATURES, emptySet()) ?: emptySet()).toList()
        _wavyThickness.value = prefs.getFloat(KEY_WAVY_THICKNESS, 4.0f)
        _wavyWobbliness.value = prefs.getFloat(KEY_WAVY_WOBBLINESS, 3.5f)
        _wavyWavelength.value = prefs.getFloat(KEY_WAVY_WAVELENGTH, 26.0f)
        _wavyThumbRadius.value = prefs.getFloat(KEY_WAVY_THUMB_RADIUS, 6.0f)
        _wavySpeed.value = prefs.getInt(KEY_WAVY_SPEED, 1300)
        _sliderInactiveTrackThickness.value = if (prefs.contains(KEY_SLIDER_INACTIVE_TRACK_THICKNESS)) {
            prefs.getFloat(KEY_SLIDER_INACTIVE_TRACK_THICKNESS, 4.0f)
        } else null
        _sliderCornerRadius.value = prefs.getFloat(KEY_SLIDER_CORNER_RADIUS, 19.0f)
        _sliderSyncAll.value = prefs.getBoolean(KEY_SLIDER_SYNC_ALL, true)
        val savedBrightnessStyle = prefs.getString(KEY_BRIGHTNESS_SLIDER_STYLE, null)
        _brightnessSliderStyle.value = if (savedBrightnessStyle != null) ProgressBarStyle.fromId(savedBrightnessStyle) else resolvedStyle
        val savedVolumeStyle = prefs.getString(KEY_VOLUME_SLIDER_STYLE, null)
        _volumeSliderStyle.value = if (savedVolumeStyle != null) ProgressBarStyle.fromId(savedVolumeStyle) else resolvedStyle
        val savedMediaStyle = prefs.getString(KEY_MEDIA_SLIDER_STYLE, null)
        _mediaSliderStyle.value = if (savedMediaStyle != null) ProgressBarStyle.fromId(savedMediaStyle) else resolvedStyle

        _a12InactiveTrackThickness.value = prefs.getFloat(KEY_A12_INACTIVE_THICKNESS, 38.0f)
        _a12CornerRadius.value = prefs.getFloat(KEY_A12_CORNER_RADIUS, 19.0f)
        _wavyInactiveTrackThickness.value = prefs.getFloat(KEY_WAVY_INACTIVE_THICKNESS, 4.0f)
        _wavyCornerRadius.value = prefs.getFloat(KEY_WAVY_CORNER_RADIUS, 4.0f)
        _straightThickness.value = prefs.getFloat(KEY_STRAIGHT_THICKNESS, 4.0f)
        _straightInactiveTrackThickness.value = prefs.getFloat(KEY_STRAIGHT_INACTIVE_THICKNESS, 4.0f)
        _straightCornerRadius.value = prefs.getFloat(KEY_STRAIGHT_CORNER_RADIUS, 4.0f)
        _straightThumbRadius.value = prefs.getFloat(KEY_STRAIGHT_THUMB_RADIUS, 6.0f)
        _panelGlobalCornerEnabled.value = prefs.getBoolean(KEY_PANEL_GLOBAL_CORNER_ENABLED, false)
        _panelOuterCorner.value = prefs.getFloat(KEY_PANEL_OUTER_CORNER, 20.0f)
        _panelInnerCorner.value = prefs.getFloat(KEY_PANEL_INNER_CORNER, 5.0f)
        _qsBackgroundCorner.value = prefs.getFloat(KEY_QS_BACKGROUND_CORNER, 20.0f)
        _qsBackgroundColor.value = if (prefs.contains(KEY_QS_BACKGROUND_COLOR)) prefs.getInt(KEY_QS_BACKGROUND_COLOR, 0) else null
        _qsPanelBackgroundColor.value = if (prefs.contains(KEY_QS_PANEL_BACKGROUND_COLOR)) prefs.getInt(KEY_QS_PANEL_BACKGROUND_COLOR, 0) else null
        _initialSetupCompleted.value = prefs.getBoolean(KEY_INITIAL_SETUP_COMPLETED, false)
        val savedMode = prefs.getString(KEY_PRIVILEGE_MODE, com.example.model.PrivilegeMode.STANDARD.name)
        _privilegeMode.value = try {
            com.example.model.PrivilegeMode.valueOf(savedMode ?: com.example.model.PrivilegeMode.STANDARD.name)
        } catch (_: Exception) {
            com.example.model.PrivilegeMode.STANDARD
        }
        ThemeManager.init(context)
    }

    fun setInitialSetupCompleted(context: Context, completed: Boolean) {
        _initialSetupCompleted.value = completed
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_INITIAL_SETUP_COMPLETED, completed).apply()
    }

    fun setPrivilegeMode(context: Context, mode: com.example.model.PrivilegeMode) {
        _privilegeMode.value = mode
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PRIVILEGE_MODE, mode.name).apply()
    }

    fun setUsePrimeCoreAffinity(context: Context, enabled: Boolean) {
        _usePrimeCoreAffinity.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_USE_PRIME_CORE_AFFINITY, enabled).apply()
    }

    fun setDisablePanelAnimations(context: Context, disable: Boolean) {
        _disablePanelAnimations.value = disable
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DISABLE_PANEL_ANIMATIONS, disable).apply()
    }

    fun setScreenRecordResolution(context: Context, res: ScreenRecordResolution) {
        _screenRecordResolution.value = res
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SCREEN_RECORD_RESOLUTION, res.id).apply()
    }

    fun setScreenRecordAudioSource(context: Context, source: ScreenRecordAudioSource) {
        _screenRecordAudioSource.value = source
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SCREEN_RECORD_AUDIO_SOURCE, source.id).apply()
    }

    fun setScreenRecordFps(context: Context, fps: ScreenRecordFps) {
        _screenRecordFps.value = fps
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SCREEN_RECORD_FPS, fps.id).apply()
    }

    fun setScreenRecordShowTouches(context: Context, enabled: Boolean) {
        _screenRecordShowTouches.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SCREEN_RECORD_SHOW_TOUCHES, enabled).apply()
    }

    fun setEnableCustomVolumePanel(context: Context, enabled: Boolean) {
        _enableCustomVolumePanel.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ENABLE_CUSTOM_VOLUME_PANEL, enabled).apply()
    }

    fun setTileHidden(context: Context, tileId: String, hidden: Boolean) {
        val current = _hiddenTileIds.value.toMutableSet()
        if (hidden) {
            current.add(tileId)
        } else {
            current.remove(tileId)
        }
        _hiddenTileIds.value = current
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_HIDDEN_TILE_IDS, current.joinToString(",")).apply()
    }

    fun setAllTilesHidden(context: Context, hiddenIds: Set<String>) {
        _hiddenTileIds.value = hiddenIds
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_HIDDEN_TILE_IDS, hiddenIds.joinToString(",")).apply()
    }

    fun setForceShowMediaPreview(context: Context, force: Boolean) {
        _forceShowMediaPreview.value = force
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_FORCE_SHOW_MEDIA_PREVIEW, force).apply()
    }

    fun setAnimationSpeedScale(context: Context, value: Float) {
        _animationSpeedScale.value = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_ANIMATION_SPEED_SCALE, value).apply()
    }

    fun setThemeMode(context: Context, mode: ThemeMode) {
        _themeMode.value = mode
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME_MODE, mode.id).apply()
    }

    fun setDragHandleVerticalOffset(context: Context, offset: Float) {
        _dragHandleVerticalOffset.value = offset
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_DRAG_HANDLE_VERTICAL_OFFSET, offset).apply()
    }

    fun setDragHandleLength(context: Context, length: Float) {
        _dragHandleLength.value = length
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_DRAG_HANDLE_LENGTH, length).apply()
    }

    fun setGridLayout(context: Context, layout: QsGridLayout) {
        _gridLayout.value = layout
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_GRID_LAYOUT, layout.id).apply()
    }

    fun setQqsTileSize(context: Context, size: Int) {
        _qqsTileSize.value = size
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_QQS_TILE_SIZE, size).apply()
    }

    fun setQqsTileCount(context: Context, count: Int) {
        _qqsTileCount.value = count
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_QQS_TILE_COUNT, count).apply()
    }

    fun setEditButtonPosition(context: Context, position: EditButtonPosition) {
        _editButtonPosition.value = position
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_EDIT_BUTTON_POSITION, position.id).apply()
    }

    fun setDatePosition(context: Context, position: DatePosition) {
        _datePosition.value = position
        _showDateInQqsHeader.value = (position == DatePosition.NEXT_TO_TIME)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_DATE_POSITION, position.id)
            .putBoolean(KEY_SHOW_DATE_IN_QQS_HEADER, position == DatePosition.NEXT_TO_TIME)
            .apply()
    }

    fun setBatteryPercentageLocation(context: Context, location: BatteryPercentageLocation) {
        _batteryPercentageLocation.value = location
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("battery_percentage_location", location.id).apply()
    }

    fun setSettingsIconLocation(context: Context, location: SettingsIconLocation) {
        _settingsIconLocation.value = location
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("settings_icon_location", location.id).apply()
    }

    fun setDisableClearAllBackground(context: Context, disable: Boolean) {
        _disableClearAllBackground.value = disable
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DISABLE_CLEAR_ALL_BG, disable).apply()
    }

    fun setEnablePanelShadows(context: Context, enable: Boolean) {
        _enablePanelShadows.value = enable
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ENABLE_PANEL_SHADOWS, enable).apply()
    }

    fun setPanelShadowElevation(context: Context, elevation: Float) {
        _panelShadowElevation.value = elevation
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_PANEL_SHADOW_ELEVATION, elevation).apply()
    }

    fun setPanelShadowOffsetY(context: Context, offset: Float) {
        _panelShadowOffsetY.value = offset
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_PANEL_SHADOW_OFFSET_Y, offset).apply()
    }

    fun resetPanelShadowParams(context: Context) {
        _panelShadowElevation.value = 8f
        _panelShadowOffsetY.value = 0f
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat(KEY_PANEL_SHADOW_ELEVATION, 8f)
            .putFloat(KEY_PANEL_SHADOW_OFFSET_Y, 0f)
            .apply()
    }

    fun setUseClearAllFab(context: Context, use: Boolean) {
        _useClearAllFab.value = use
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_USE_CLEAR_ALL_FAB, use).apply()
    }

    fun setClearAllFabPosition(context: Context, pos: ClearAllFabPosition) {
        _clearAllFabPosition.value = pos
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CLEAR_ALL_FAB_POSITION, pos.id).apply()
    }

    fun setClearAllFabShape(context: Context, shape: ClearAllFabShape) {
        _clearAllFabShape.value = shape
        _clearAllFabShapeId.value = shape.id
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_CLEAR_ALL_FAB_SHAPE, shape.id)
            .putString(KEY_CLEAR_ALL_FAB_SHAPE_ID, shape.id)
            .apply()
    }

    fun setClearAllFabShapeId(context: Context, shapeId: String) {
        _clearAllFabShapeId.value = shapeId
        val shape = ClearAllFabShape.fromId(shapeId)
        _clearAllFabShape.value = shape
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_CLEAR_ALL_FAB_SHAPE_ID, shapeId)
            .putString(KEY_CLEAR_ALL_FAB_SHAPE, shapeId)
            .apply()
    }

    fun setClearAllFabIcon(context: Context, icon: ClearAllFabIcon) {
        _clearAllFabIcon.value = icon
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CLEAR_ALL_FAB_ICON, icon.id).apply()
    }

    fun setClearAllFabOffsetX(context: Context, offset: Float) {
        _clearAllFabOffsetX.value = offset
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_CLEAR_ALL_FAB_OFFSET_X, offset).apply()
    }

    fun setClearAllFabOffsetY(context: Context, offset: Float) {
        _clearAllFabOffsetY.value = offset
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_CLEAR_ALL_FAB_OFFSET_Y, offset).apply()
    }

    fun setClearAllFabSize(context: Context, size: Int) {
        _clearAllFabSize.value = size
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_CLEAR_ALL_FAB_SIZE, size).apply()
    }

    fun resetClearAllFabOffsets(context: Context) {
        _clearAllFabOffsetX.value = 0f
        _clearAllFabOffsetY.value = 0f
        _clearAllFabSize.value = 64
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat(KEY_CLEAR_ALL_FAB_OFFSET_X, 0f)
            .putFloat(KEY_CLEAR_ALL_FAB_OFFSET_Y, 0f)
            .putInt(KEY_CLEAR_ALL_FAB_SIZE, 64)
            .apply()
    }

    fun setShowDateInQqsHeader(context: Context, enabled: Boolean) {
        val pos = if (enabled) DatePosition.NEXT_TO_TIME else DatePosition.BELOW_TIME
        setDatePosition(context, pos)
    }

    fun setEnabledTileShapeId(context: Context, shapeId: String) {
        _enabledTileShapeId.value = shapeId
        val shape = TileShape.fromId(shapeId)
        _enabledTileShape.value = shape
        _activeSquareShape.value = (shape == TileShape.SQUIRCLE || shape == TileShape.SQUARE)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_ENABLED_TILE_SHAPE, shapeId)
            .putString(KEY_TILE_SHAPE, shapeId)
            .putBoolean(KEY_ACTIVE_SQUARE_SHAPE, shape == TileShape.SQUIRCLE || shape == TileShape.SQUARE)
            .apply()
    }

    fun setDisabledTileShapeId(context: Context, shapeId: String) {
        _disabledTileShapeId.value = shapeId
        val shape = TileShape.fromId(shapeId)
        _disabledTileShape.value = shape
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_DISABLED_TILE_SHAPE, shapeId)
            .apply()
    }

    fun setEnabledTileShape(context: Context, shape: TileShape) {
        setEnabledTileShapeId(context, shape.id)
    }

    fun setDisabledTileShape(context: Context, shape: TileShape) {
        setDisabledTileShapeId(context, shape.id)
    }

    fun setTileShape(context: Context, shape: TileShape) {
        setEnabledTileShape(context, shape)
    }

    fun setActiveSquareShape(context: Context, enabled: Boolean) {
        val shape = if (enabled) TileShape.SQUIRCLE else TileShape.CIRCLE
        setTileShape(context, shape)
    }

    fun setTileOutlineEnabled(context: Context, enabled: Boolean) {
        _tileOutlineEnabled.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_TILE_OUTLINE_ENABLED, enabled).apply()
    }

    fun setHideQqsDragHandle(context: Context, hide: Boolean) {
        _hideQqsDragHandle.value = hide
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_HIDE_QQS_DRAG_HANDLE, hide).apply()
    }

    fun setHideQqsEmptyNotifLabels(context: Context, hide: Boolean) {
        _hideQqsEmptyNotifLabels.value = hide
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_HIDE_QQS_EMPTY_NOTIF_LABELS, hide).apply()
    }

    fun setShowCarrier(context: Context, show: Boolean) {
        _showCarrier.value = show
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SHOW_CARRIER, show).apply()
        if (show && !_showCarrierDivider.value) {
            setShowCarrierDivider(context, true)
        }
    }

    fun setShowCarrierDivider(context: Context, show: Boolean) {
        _showCarrierDivider.value = show
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SHOW_CARRIER_DIVIDER, show).apply()
    }

    fun setHideCarrierInExpanded(context: Context, hide: Boolean) {
        _hideCarrierInExpanded.value = hide
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_HIDE_CARRIER_IN_EXPANDED, hide).apply()
    }

    fun setBrightnessAtBottom(context: Context, value: Boolean) {
        _brightnessAtBottom.value = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_BRIGHTNESS_AT_BOTTOM, value).apply()
    }

    fun setBrightnessInQqs(context: Context, value: Boolean) {
        _brightnessInQqs.value = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_BRIGHTNESS_IN_QQS, value).apply()
    }

    fun setHideBrightnessPercentage(context: Context, value: Boolean) {
        _hideBrightnessPercentage.value = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_HIDE_BRIGHTNESS_PERCENTAGE, value).apply()
    }

    fun setHideBrightnessIcon(context: Context, value: Boolean) {
        _hideBrightnessIcon.value = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_HIDE_BRIGHTNESS_ICON, value).apply()
    }

    fun setShowBrightnessPlusMinus(context: Context, value: Boolean) {
        _showBrightnessPlusMinus.value = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SHOW_BRIGHTNESS_PLUS_MINUS, value).apply()
    }

    fun setShowBrightnessGradient(context: Context, value: Boolean) {
        _showBrightnessGradient.value = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SHOW_BRIGHTNESS_GRADIENT, value).apply()
    }

    fun setMinBrightness(context: Context, value: Int) {
        val clampedMin = value.coerceIn(1, 2047)
        val finalMin = if (clampedMin > _maxBrightness.value) _maxBrightness.value else clampedMin
        _minBrightness.value = finalMin
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_MIN_BRIGHTNESS, finalMin).apply()
    }

    fun setMaxBrightness(context: Context, value: Int) {
        val clampedMax = value.coerceIn(1, 2047)
        val finalMax = if (clampedMax < _minBrightness.value) _minBrightness.value else clampedMax
        _maxBrightness.value = finalMax
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_MAX_BRIGHTNESS, finalMax).apply()
    }

    fun setProgressBarStyle(context: Context, style: ProgressBarStyle) {
        _progressBarStyle.value = style
        _wavyProgressBars.value = (style == ProgressBarStyle.WAVY)
        if (_sliderSyncAll.value) {
            _brightnessSliderStyle.value = style
            _volumeSliderStyle.value = style
            _mediaSliderStyle.value = style
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
            .putString(KEY_PROGRESS_BAR_STYLE, style.id)
            .putBoolean(KEY_WAVY_PROGRESS_BARS, style == ProgressBarStyle.WAVY)
        if (_sliderSyncAll.value) {
            editor.putString(KEY_BRIGHTNESS_SLIDER_STYLE, style.id)
                .putString(KEY_VOLUME_SLIDER_STYLE, style.id)
                .putString(KEY_MEDIA_SLIDER_STYLE, style.id)
        }
        editor.apply()
    }

    fun setSliderSyncAll(context: Context, sync: Boolean) {
        _sliderSyncAll.value = sync
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit().putBoolean(KEY_SLIDER_SYNC_ALL, sync)
        if (sync) {
            val style = _progressBarStyle.value
            _brightnessSliderStyle.value = style
            _volumeSliderStyle.value = style
            _mediaSliderStyle.value = style
            editor.putString(KEY_BRIGHTNESS_SLIDER_STYLE, style.id)
                .putString(KEY_VOLUME_SLIDER_STYLE, style.id)
                .putString(KEY_MEDIA_SLIDER_STYLE, style.id)
        }
        editor.apply()
    }

    fun setSliderStyleForTarget(context: Context, target: QsSliderTarget, style: ProgressBarStyle) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        when (target) {
            QsSliderTarget.BRIGHTNESS -> {
                _brightnessSliderStyle.value = style
                editor.putString(KEY_BRIGHTNESS_SLIDER_STYLE, style.id)
            }
            QsSliderTarget.VOLUME -> {
                _volumeSliderStyle.value = style
                editor.putString(KEY_VOLUME_SLIDER_STYLE, style.id)
            }
            QsSliderTarget.MEDIA -> {
                _mediaSliderStyle.value = style
                editor.putString(KEY_MEDIA_SLIDER_STYLE, style.id)
            }
        }
        editor.apply()
    }

    fun setWavyProgressBars(context: Context, enabled: Boolean) {
        val style = if (enabled) ProgressBarStyle.WAVY else ProgressBarStyle.DEFAULT
        setProgressBarStyle(context, style)
    }

    fun setEnableContainerBlur(context: Context, enabled: Boolean) {
        _enableContainerBlur.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ENABLE_CONTAINER_BLUR, enabled).apply()
    }

    fun setInvisibleStatusBarTrigger(context: Context, enabled: Boolean) {
        _invisibleStatusBarTrigger.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_INVISIBLE_STATUS_BAR_TRIGGER, enabled).apply()
    }

    fun setForceImmersiveTrigger(context: Context, enabled: Boolean) {
        _forceImmersiveTrigger.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_FORCE_IMMERSIVE_TRIGGER, enabled).apply()
    }

    fun setOverrideSystemQs(context: Context, enabled: Boolean) {
        _overrideSystemQs.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_OVERRIDE_SYSTEM_QS, enabled).apply()
    }

    fun setAutoSystemPanelClosing(context: Context, enabled: Boolean) {
        _autoSystemPanelClosing.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_SYSTEM_PANEL_CLOSING, enabled).apply()
    }

    fun setDetectorCooldownMs(context: Context, ms: Int) {
        val clamped = ms.coerceIn(10, 1000)
        _detectorCooldownMs.value = clamped
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_DETECTOR_COOLDOWN_MS, clamped).apply()
    }

    fun setPanelReplacementDelayMs(context: Context, ms: Int) {
        val clamped = ms.coerceIn(0, 1000)
        _panelReplacementDelayMs.value = clamped
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_PANEL_REPLACEMENT_DELAY_MS, clamped).apply()
    }

    fun addStockShadeWhitelistPackage(context: Context, pkg: String) {
        val trimmed = pkg.trim().lowercase()
        if (trimmed.isEmpty()) return
        val newSet = _stockShadeWhitelist.value + trimmed
        _stockShadeWhitelist.value = newSet
        saveWhitelist(context, newSet)
    }

    fun removeStockShadeWhitelistPackage(context: Context, pkg: String) {
        val newSet = _stockShadeWhitelist.value - pkg
        _stockShadeWhitelist.value = newSet
        saveWhitelist(context, newSet)
    }

    fun addCustomBlacklistPackage(context: Context, pkg: String) {
        val trimmed = pkg.trim()
        if (trimmed.isEmpty()) return
        val newSet = _customBlacklistPackages.value + trimmed
        _customBlacklistPackages.value = newSet
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_CUSTOM_BLACKLIST_PACKAGES, newSet).apply()
    }

    fun removeCustomBlacklistPackage(context: Context, pkg: String) {
        val newSet = _customBlacklistPackages.value - pkg
        _customBlacklistPackages.value = newSet
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_CUSTOM_BLACKLIST_PACKAGES, newSet).apply()
    }

    fun addCustomBlacklistClass(context: Context, cls: String) {
        val trimmed = cls.trim()
        if (trimmed.isEmpty()) return
        val newSet = _customBlacklistClasses.value + trimmed
        _customBlacklistClasses.value = newSet
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_CUSTOM_BLACKLIST_CLASSES, newSet).apply()
    }

    fun removeCustomBlacklistClass(context: Context, cls: String) {
        val newSet = _customBlacklistClasses.value - cls
        _customBlacklistClasses.value = newSet
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_CUSTOM_BLACKLIST_CLASSES, newSet).apply()
    }

    fun setStockShadeWhitelist(context: Context, packages: Set<String>) {
        _stockShadeWhitelist.value = packages
        saveWhitelist(context, packages)
    }

    private fun saveWhitelist(context: Context, packages: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_STOCK_SHADE_WHITELIST, packages).apply()
    }

    fun setEnableHeadsUpOverlay(context: Context, enabled: Boolean) {
        _enableHeadsUpOverlay.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ENABLE_HEADS_UP_OVERLAY, enabled).apply()

        // When custom heads-up overlay is turned off, if system heads-up is enabled in prefs, ensure system heads-up is active
        if (!enabled && _enableSystemHeadsUp.value) {
            setEnableSystemHeadsUp(context, true)
        }
    }

    fun setEnableSystemHeadsUp(context: Context, enabled: Boolean) {
        _enableSystemHeadsUp.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ENABLE_SYSTEM_HEADS_UP, enabled).apply()

        val nativeMgr = com.example.system.NativeTileManager.instance
        if (nativeMgr != null) {
            nativeMgr.setHeadsUpEnabled(enabled)
        } else {
            val valInt = if (enabled) 1 else 0
            try {
                Settings.Global.putInt(context.contentResolver, "heads_up_notifications_enabled", valInt)
            } catch (_: Exception) {}

            val engine = com.example.system.PrivilegeEngine(context)
            if (engine.currentMode.value != com.example.model.PrivilegeMode.STANDARD) {
                val cmd = "settings put global heads_up_notifications_enabled $valInt"
                CoroutineScope(Dispatchers.IO).launch {
                    engine.executeCommand(cmd)
                }
            }
        }
    }

    fun setHeadsUpTimeoutSeconds(context: Context, seconds: Int) {
        val clamped = seconds.coerceIn(2, 10)
        _headsUpTimeoutSeconds.value = clamped
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_HEADS_UP_TIMEOUT_SECONDS, clamped).apply()
    }

    private const val KEY_SAVED_ACTIVE_TILE_IDS = "saved_active_tile_ids"

    fun saveActiveTileIds(context: Context, ids: List<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SAVED_ACTIVE_TILE_IDS, ids.joinToString(",")).apply()
    }

    fun getSavedActiveTileIds(context: Context): List<String>? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_SAVED_ACTIVE_TILE_IDS, null) ?: return null
        return saved.split(",").filter { it.isNotBlank() }
    }

    fun setShowCustomHeaderImage(context: Context, enabled: Boolean) {
        _showCustomHeaderImage.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SHOW_CUSTOM_HEADER_IMAGE, enabled).apply()
    }

    fun setCustomHeaderImageUri(context: Context, uri: String) {
        _customHeaderImageUri.value = uri
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CUSTOM_HEADER_IMAGE_URI, uri).apply()
    }

    fun setCustomHeaderImageTitle(context: Context, title: String) {
        _customHeaderImageTitle.value = title
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CUSTOM_HEADER_IMAGE_TITLE, title).apply()
    }

    fun setCustomHeaderImageHeight(context: Context, height: Float) {
        _customHeaderImageHeight.value = height
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_CUSTOM_HEADER_IMAGE_HEIGHT, height).apply()
    }

    fun setCustomHeaderImageOpacity(context: Context, opacity: Float) {
        _customHeaderImageOpacity.value = opacity
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_CUSTOM_HEADER_IMAGE_OPACITY, opacity).apply()
    }

    fun setShowCustomHeaderMonetGradient(context: Context, enabled: Boolean) {
        _showCustomHeaderMonetGradient.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SHOW_CUSTOM_HEADER_MONET_GRADIENT, enabled).apply()
    }

    fun setCustomHeaderMonetGradientHeight(context: Context, height: Float) {
        _customHeaderMonetGradientHeight.value = height
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_CUSTOM_HEADER_MONET_GRADIENT_HEIGHT, height).apply()
    }

    fun setShowMonetSurfaceBackground(context: Context, enabled: Boolean) {
        _showMonetSurfaceBackground.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SHOW_MONET_SURFACE_BACKGROUND, enabled).apply()
    }

    fun setCollapseExpandOptions(context: Context, enabled: Boolean) {
        _collapseExpandOptions.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_COLLAPSE_EXPAND_OPTIONS, enabled).apply()
    }

    fun setCollapseExpandSubheaders(context: Context, enabled: Boolean) {
        _collapseExpandSubheaders.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_COLLAPSE_EXPAND_SUBHEADERS, enabled).apply()
    }

    fun setSurfaceBgColorTertiary(context: Context, enabled: Boolean) {
        _surfaceBgColorTertiary.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ELEM_SURFACE_BG_TERTIARY, enabled).apply()
    }

    fun setPanelContainerColorTertiary(context: Context, enabled: Boolean) {
        _panelContainerColorTertiary.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ELEM_PANEL_CONTAINER_TERTIARY, enabled).apply()
    }

    fun setNotifContainerColorTertiary(context: Context, enabled: Boolean) {
        _notifContainerColorTertiary.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ELEM_NOTIF_CONTAINER_TERTIARY, enabled).apply()
    }

    fun setQsTilesColorTertiary(context: Context, enabled: Boolean) {
        _qsTilesColorTertiary.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ELEM_QS_TILES_TERTIARY, enabled).apply()
    }

    fun setSlidersColorTertiary(context: Context, enabled: Boolean) {
        _slidersColorTertiary.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ELEM_SLIDERS_TERTIARY, enabled).apply()
    }

    fun setMiscElementsColorTertiary(context: Context, enabled: Boolean) {
        _miscElementsColorTertiary.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ELEM_MISC_ELEMENTS_TERTIARY, enabled).apply()
    }

    fun setHeaderGradientColorTertiary(context: Context, enabled: Boolean) {
        _headerGradientColorTertiary.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ELEM_HEADER_GRADIENT_TERTIARY, enabled).apply()
    }

    fun setSurfaceBgHsb(context: Context, hue: Float, sat: Float, valFactor: Float) {
        _surfaceBgHue.value = hue
        _surfaceBgSat.value = sat
        _surfaceBgVal.value = valFactor
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_ELEM_SURFACE_BG_HUE, hue).putFloat(KEY_ELEM_SURFACE_BG_SAT, sat).putFloat(KEY_ELEM_SURFACE_BG_VAL, valFactor).apply()
    }

    fun setPanelContainerHsb(context: Context, hue: Float, sat: Float, valFactor: Float) {
        _panelContainerHue.value = hue
        _panelContainerSat.value = sat
        _panelContainerVal.value = valFactor
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_ELEM_PANEL_CONTAINER_HUE, hue).putFloat(KEY_ELEM_PANEL_CONTAINER_SAT, sat).putFloat(KEY_ELEM_PANEL_CONTAINER_VAL, valFactor).apply()
    }

    fun setHeaderGradientHsb(context: Context, hue: Float, sat: Float, valFactor: Float) {
        _headerGradientHue.value = hue
        _headerGradientSat.value = sat
        _headerGradientVal.value = valFactor
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_ELEM_HEADER_GRADIENT_HUE, hue).putFloat(KEY_ELEM_HEADER_GRADIENT_SAT, sat).putFloat(KEY_ELEM_HEADER_GRADIENT_VAL, valFactor).apply()
    }

    fun setQsTilesHsb(context: Context, hue: Float, sat: Float, valFactor: Float) {
        _qsTilesHue.value = hue
        _qsTilesSat.value = sat
        _qsTilesVal.value = valFactor
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_ELEM_QS_TILES_HUE, hue).putFloat(KEY_ELEM_QS_TILES_SAT, sat).putFloat(KEY_ELEM_QS_TILES_VAL, valFactor).apply()
    }

    fun setSlidersHsb(context: Context, hue: Float, sat: Float, valFactor: Float) {
        _slidersHue.value = hue
        _slidersSat.value = sat
        _slidersVal.value = valFactor
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_ELEM_SLIDERS_HUE, hue).putFloat(KEY_ELEM_SLIDERS_SAT, sat).putFloat(KEY_ELEM_SLIDERS_VAL, valFactor).apply()
    }

    fun setMiscElementsHsb(context: Context, hue: Float, sat: Float, valFactor: Float) {
        _miscElementsHue.value = hue
        _miscElementsSat.value = sat
        _miscElementsVal.value = valFactor
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_ELEM_MISC_ELEMENTS_HUE, hue).putFloat(KEY_ELEM_MISC_ELEMENTS_SAT, sat).putFloat(KEY_ELEM_MISC_ELEMENTS_VAL, valFactor).apply()
    }

    fun setDisableMonetNotifText(context: Context, enabled: Boolean) {
        _disableMonetNotifText.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DISABLE_MONET_NOTIF_TEXT, enabled).apply()
    }

    fun setSurfaceBgColorTertiary(context: Context, isDark: Boolean, enabled: Boolean) {
        if (isDark) {
            _surfaceBgColorTertiaryDark.value = enabled
        } else {
            _surfaceBgColorTertiaryLight.value = enabled
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(
            if (isDark) KEY_ELEM_SURFACE_BG_TERTIARY_DARK else KEY_ELEM_SURFACE_BG_TERTIARY_LIGHT,
            enabled
        ).apply()
    }

    fun setPanelContainerColorTertiary(context: Context, isDark: Boolean, enabled: Boolean) {
        if (isDark) {
            _panelContainerColorTertiaryDark.value = enabled
        } else {
            _panelContainerColorTertiaryLight.value = enabled
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(
            if (isDark) KEY_ELEM_PANEL_CONTAINER_TERTIARY_DARK else KEY_ELEM_PANEL_CONTAINER_TERTIARY_LIGHT,
            enabled
        ).apply()
    }

    fun setHeaderGradientColorTertiary(context: Context, isDark: Boolean, enabled: Boolean) {
        _headerGradientColorTertiary.value = enabled
        if (isDark) {
            _headerGradientColorTertiaryDark.value = enabled
        } else {
            _headerGradientColorTertiaryLight.value = enabled
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ELEM_HEADER_GRADIENT_TERTIARY, enabled).putBoolean(
            if (isDark) KEY_ELEM_HEADER_GRADIENT_TERTIARY_DARK else KEY_ELEM_HEADER_GRADIENT_TERTIARY_LIGHT,
            enabled
        ).apply()
    }

    fun setQsTilesColorTertiary(context: Context, isDark: Boolean, enabled: Boolean) {
        if (isDark) {
            _qsTilesColorTertiaryDark.value = enabled
        } else {
            _qsTilesColorTertiaryLight.value = enabled
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(
            if (isDark) KEY_ELEM_QS_TILES_TERTIARY_DARK else KEY_ELEM_QS_TILES_TERTIARY_LIGHT,
            enabled
        ).apply()
    }

    fun setInactiveTilesColorTertiary(context: Context, isDark: Boolean, enabled: Boolean) {
        if (isDark) {
            _inactiveTilesColorTertiaryDark.value = enabled
        } else {
            _inactiveTilesColorTertiaryLight.value = enabled
        }
        _inactiveTilesColorTertiary.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(
            if (isDark) KEY_ELEM_INACTIVE_TILES_TERTIARY_DARK else KEY_ELEM_INACTIVE_TILES_TERTIARY_LIGHT,
            enabled
        ).apply()
    }

    fun setInactiveTilesColorPrimary(context: Context, isDark: Boolean, enabled: Boolean) {
        if (isDark) {
            _inactiveTilesColorPrimaryDark.value = enabled
        } else {
            _inactiveTilesColorPrimaryLight.value = enabled
        }
        _inactiveTilesColorPrimary.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(
            if (isDark) KEY_ELEM_INACTIVE_TILES_PRIMARY_DARK else KEY_ELEM_INACTIVE_TILES_PRIMARY_LIGHT,
            enabled
        ).apply()
    }

    fun setSlidersColorTertiary(context: Context, isDark: Boolean, enabled: Boolean) {
        if (isDark) {
            _slidersColorTertiaryDark.value = enabled
        } else {
            _slidersColorTertiaryLight.value = enabled
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(
            if (isDark) KEY_ELEM_SLIDERS_TERTIARY_DARK else KEY_ELEM_SLIDERS_TERTIARY_LIGHT,
            enabled
        ).apply()
    }

    fun setMiscElementsColorTertiary(context: Context, isDark: Boolean, enabled: Boolean) {
        _miscElementsColorTertiary.value = enabled
        if (isDark) {
            _miscElementsColorTertiaryDark.value = enabled
        } else {
            _miscElementsColorTertiaryLight.value = enabled
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ELEM_MISC_ELEMENTS_TERTIARY, enabled).putBoolean(
            if (isDark) KEY_ELEM_MISC_ELEMENTS_TERTIARY_DARK else KEY_ELEM_MISC_ELEMENTS_TERTIARY_LIGHT,
            enabled
        ).apply()
    }

    fun setSurfaceBgHsb(context: Context, isDark: Boolean, hue: Float, sat: Float, valFactor: Float) {
        if (isDark) {
            _surfaceBgHueDark.value = hue
            _surfaceBgSatDark.value = sat
            _surfaceBgValDark.value = valFactor
        } else {
            _surfaceBgHueLight.value = hue
            _surfaceBgSatLight.value = sat
            _surfaceBgValLight.value = valFactor
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat(if (isDark) KEY_ELEM_SURFACE_BG_HUE_DARK else KEY_ELEM_SURFACE_BG_HUE_LIGHT, hue)
            .putFloat(if (isDark) KEY_ELEM_SURFACE_BG_SAT_DARK else KEY_ELEM_SURFACE_BG_SAT_LIGHT, sat)
            .putFloat(if (isDark) KEY_ELEM_SURFACE_BG_VAL_DARK else KEY_ELEM_SURFACE_BG_VAL_LIGHT, valFactor)
            .apply()
    }

    fun setPanelContainerHsb(context: Context, isDark: Boolean, hue: Float, sat: Float, valFactor: Float) {
        if (isDark) {
            _panelContainerHueDark.value = hue
            _panelContainerSatDark.value = sat
            _panelContainerValDark.value = valFactor
        } else {
            _panelContainerHueLight.value = hue
            _panelContainerSatLight.value = sat
            _panelContainerValLight.value = valFactor
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat(if (isDark) KEY_ELEM_PANEL_CONTAINER_HUE_DARK else KEY_ELEM_PANEL_CONTAINER_HUE_LIGHT, hue)
            .putFloat(if (isDark) KEY_ELEM_PANEL_CONTAINER_SAT_DARK else KEY_ELEM_PANEL_CONTAINER_SAT_LIGHT, sat)
            .putFloat(if (isDark) KEY_ELEM_PANEL_CONTAINER_VAL_DARK else KEY_ELEM_PANEL_CONTAINER_VAL_LIGHT, valFactor)
            .apply()
    }

    fun setHeaderGradientHsb(context: Context, isDark: Boolean, hue: Float, sat: Float, valFactor: Float) {
        if (isDark) {
            _headerGradientHueDark.value = hue
            _headerGradientSatDark.value = sat
            _headerGradientValDark.value = valFactor
        } else {
            _headerGradientHueLight.value = hue
            _headerGradientSatLight.value = sat
            _headerGradientValLight.value = valFactor
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat(if (isDark) KEY_ELEM_HEADER_GRADIENT_HUE_DARK else KEY_ELEM_HEADER_GRADIENT_HUE_LIGHT, hue)
            .putFloat(if (isDark) KEY_ELEM_HEADER_GRADIENT_SAT_DARK else KEY_ELEM_HEADER_GRADIENT_SAT_LIGHT, sat)
            .putFloat(if (isDark) KEY_ELEM_HEADER_GRADIENT_VAL_DARK else KEY_ELEM_HEADER_GRADIENT_VAL_LIGHT, valFactor)
            .apply()
    }

    fun setQsTilesHsb(context: Context, isDark: Boolean, hue: Float, sat: Float, valFactor: Float) {
        if (isDark) {
            _qsTilesHueDark.value = hue
            _qsTilesSatDark.value = sat
            _qsTilesValDark.value = valFactor
        } else {
            _qsTilesHueLight.value = hue
            _qsTilesSatLight.value = sat
            _qsTilesValLight.value = valFactor
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat(if (isDark) KEY_ELEM_QS_TILES_HUE_DARK else KEY_ELEM_QS_TILES_HUE_LIGHT, hue)
            .putFloat(if (isDark) KEY_ELEM_QS_TILES_SAT_DARK else KEY_ELEM_QS_TILES_SAT_LIGHT, sat)
            .putFloat(if (isDark) KEY_ELEM_QS_TILES_VAL_DARK else KEY_ELEM_QS_TILES_VAL_LIGHT, valFactor)
            .apply()
    }

    fun setInactiveTilesHsb(context: Context, isDark: Boolean, hue: Float, sat: Float, valFactor: Float) {
        if (isDark) {
            _inactiveTilesHueDark.value = hue
            _inactiveTilesSatDark.value = sat
            _inactiveTilesValDark.value = valFactor
        } else {
            _inactiveTilesHueLight.value = hue
            _inactiveTilesSatLight.value = sat
            _inactiveTilesValLight.value = valFactor
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat(if (isDark) KEY_ELEM_INACTIVE_TILES_HUE_DARK else KEY_ELEM_INACTIVE_TILES_HUE_LIGHT, hue)
            .putFloat(if (isDark) KEY_ELEM_INACTIVE_TILES_SAT_DARK else KEY_ELEM_INACTIVE_TILES_SAT_LIGHT, sat)
            .putFloat(if (isDark) KEY_ELEM_INACTIVE_TILES_VAL_DARK else KEY_ELEM_INACTIVE_TILES_VAL_LIGHT, valFactor)
            .apply()
    }

    fun setSlidersHsb(context: Context, isDark: Boolean, hue: Float, sat: Float, valFactor: Float) {
        if (isDark) {
            _slidersHueDark.value = hue
            _slidersSatDark.value = sat
            _slidersValDark.value = valFactor
        } else {
            _slidersHueLight.value = hue
            _slidersSatLight.value = sat
            _slidersValLight.value = valFactor
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat(if (isDark) KEY_ELEM_SLIDERS_HUE_DARK else KEY_ELEM_SLIDERS_HUE_LIGHT, hue)
            .putFloat(if (isDark) KEY_ELEM_SLIDERS_SAT_DARK else KEY_ELEM_SLIDERS_SAT_LIGHT, sat)
            .putFloat(if (isDark) KEY_ELEM_SLIDERS_VAL_DARK else KEY_ELEM_SLIDERS_VAL_LIGHT, valFactor)
            .apply()
    }

    fun setMiscElementsHsb(context: Context, isDark: Boolean, hue: Float, sat: Float, valFactor: Float) {
        if (isDark) {
            _miscElementsHueDark.value = hue
            _miscElementsSatDark.value = sat
            _miscElementsValDark.value = valFactor
        } else {
            _miscElementsHueLight.value = hue
            _miscElementsSatLight.value = sat
            _miscElementsValLight.value = valFactor
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat(if (isDark) KEY_ELEM_MISC_ELEMENTS_HUE_DARK else KEY_ELEM_MISC_ELEMENTS_HUE_LIGHT, hue)
            .putFloat(if (isDark) KEY_ELEM_MISC_ELEMENTS_SAT_DARK else KEY_ELEM_MISC_ELEMENTS_SAT_LIGHT, sat)
            .putFloat(if (isDark) KEY_ELEM_MISC_ELEMENTS_VAL_DARK else KEY_ELEM_MISC_ELEMENTS_VAL_LIGHT, valFactor)
            .apply()
    }

    fun getSurfaceBgColorTertiary(isDark: Boolean): StateFlow<Boolean> = if (isDark) surfaceBgColorTertiaryDark else surfaceBgColorTertiaryLight
    fun getPanelContainerColorTertiary(isDark: Boolean): StateFlow<Boolean> = if (isDark) panelContainerColorTertiaryDark else panelContainerColorTertiaryLight
    fun getHeaderGradientColorTertiary(isDark: Boolean): StateFlow<Boolean> = if (isDark) headerGradientColorTertiaryDark else headerGradientColorTertiaryLight
    fun getQsTilesColorTertiary(isDark: Boolean): StateFlow<Boolean> = if (isDark) qsTilesColorTertiaryDark else qsTilesColorTertiaryLight
    fun getInactiveTilesColorTertiary(isDark: Boolean): StateFlow<Boolean> = if (isDark) inactiveTilesColorTertiaryDark else inactiveTilesColorTertiaryLight
    fun getInactiveTilesColorPrimary(isDark: Boolean): StateFlow<Boolean> = if (isDark) inactiveTilesColorPrimaryDark else inactiveTilesColorPrimaryLight
    fun getSlidersColorTertiary(isDark: Boolean): StateFlow<Boolean> = if (isDark) slidersColorTertiaryDark else slidersColorTertiaryLight
    fun getMiscElementsColorTertiary(isDark: Boolean): StateFlow<Boolean> = if (isDark) miscElementsColorTertiaryDark else miscElementsColorTertiaryLight

    fun getSurfaceBgHue(isDark: Boolean): StateFlow<Float> = if (isDark) surfaceBgHueDark else surfaceBgHueLight
    fun getSurfaceBgSat(isDark: Boolean): StateFlow<Float> = if (isDark) surfaceBgSatDark else surfaceBgSatLight
    fun getSurfaceBgVal(isDark: Boolean): StateFlow<Float> = if (isDark) surfaceBgValDark else surfaceBgValLight

    fun getPanelContainerHue(isDark: Boolean): StateFlow<Float> = if (isDark) panelContainerHueDark else panelContainerHueLight
    fun getPanelContainerSat(isDark: Boolean): StateFlow<Float> = if (isDark) panelContainerSatDark else panelContainerSatLight
    fun getPanelContainerVal(isDark: Boolean): StateFlow<Float> = if (isDark) panelContainerValDark else panelContainerValLight

    fun getHeaderGradientHue(isDark: Boolean): StateFlow<Float> = if (isDark) headerGradientHueDark else headerGradientHueLight
    fun getHeaderGradientSat(isDark: Boolean): StateFlow<Float> = if (isDark) headerGradientSatDark else headerGradientSatLight
    fun getHeaderGradientVal(isDark: Boolean): StateFlow<Float> = if (isDark) headerGradientValDark else headerGradientValLight

    fun getQsTilesHue(isDark: Boolean): StateFlow<Float> = if (isDark) qsTilesHueDark else qsTilesHueLight
    fun getQsTilesSat(isDark: Boolean): StateFlow<Float> = if (isDark) qsTilesSatDark else qsTilesSatLight
    fun getQsTilesVal(isDark: Boolean): StateFlow<Float> = if (isDark) qsTilesValDark else qsTilesValLight

    fun getInactiveTilesHue(isDark: Boolean): StateFlow<Float> = if (isDark) inactiveTilesHueDark else inactiveTilesHueLight
    fun getInactiveTilesSat(isDark: Boolean): StateFlow<Float> = if (isDark) inactiveTilesSatDark else inactiveTilesSatLight
    fun getInactiveTilesVal(isDark: Boolean): StateFlow<Float> = if (isDark) inactiveTilesValDark else inactiveTilesValLight

    fun getSlidersHue(isDark: Boolean): StateFlow<Float> = if (isDark) slidersHueDark else slidersHueLight
    fun getSlidersSat(isDark: Boolean): StateFlow<Float> = if (isDark) slidersSatDark else slidersSatLight
    fun getSlidersVal(isDark: Boolean): StateFlow<Float> = if (isDark) slidersValDark else slidersValLight

    fun getMiscElementsHue(isDark: Boolean): StateFlow<Float> = if (isDark) miscElementsHueDark else miscElementsHueLight
    fun getMiscElementsSat(isDark: Boolean): StateFlow<Float> = if (isDark) miscElementsSatDark else miscElementsSatLight
    fun getMiscElementsVal(isDark: Boolean): StateFlow<Float> = if (isDark) miscElementsValDark else miscElementsValLight

    fun syncElementColors(context: Context, fromDark: Boolean) {
        val toDark = !fromDark
        setSurfaceBgColorTertiary(context, toDark, getSurfaceBgColorTertiary(fromDark).value)
        setPanelContainerColorTertiary(context, toDark, getPanelContainerColorTertiary(fromDark).value)
        setHeaderGradientColorTertiary(context, toDark, getHeaderGradientColorTertiary(fromDark).value)
        setQsTilesColorTertiary(context, toDark, getQsTilesColorTertiary(fromDark).value)
        setInactiveTilesColorTertiary(context, toDark, getInactiveTilesColorTertiary(fromDark).value)
        setInactiveTilesColorPrimary(context, toDark, getInactiveTilesColorPrimary(fromDark).value)
        setSlidersColorTertiary(context, toDark, getSlidersColorTertiary(fromDark).value)
        setMiscElementsColorTertiary(context, toDark, getMiscElementsColorTertiary(fromDark).value)

        setSurfaceBgHsb(context, toDark, getSurfaceBgHue(fromDark).value, getSurfaceBgSat(fromDark).value, getSurfaceBgVal(fromDark).value)
        setPanelContainerHsb(context, toDark, getPanelContainerHue(fromDark).value, getPanelContainerSat(fromDark).value, getPanelContainerVal(fromDark).value)
        setHeaderGradientHsb(context, toDark, getHeaderGradientHue(fromDark).value, getHeaderGradientSat(fromDark).value, getHeaderGradientVal(fromDark).value)
        setQsTilesHsb(context, toDark, getQsTilesHue(fromDark).value, getQsTilesSat(fromDark).value, getQsTilesVal(fromDark).value)
        setInactiveTilesHsb(context, toDark, getInactiveTilesHue(fromDark).value, getInactiveTilesSat(fromDark).value, getInactiveTilesVal(fromDark).value)
        setSlidersHsb(context, toDark, getSlidersHue(fromDark).value, getSlidersSat(fromDark).value, getSlidersVal(fromDark).value)
        setMiscElementsHsb(context, toDark, getMiscElementsHue(fromDark).value, getMiscElementsSat(fromDark).value, getMiscElementsVal(fromDark).value)
    }

    fun setShowQsTileGradient(context: Context, enabled: Boolean) {
        _showQsTileGradient.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SHOW_QS_TILE_GRADIENT, enabled).apply()
    }

    fun setQsTileLabelFontSize(context: Context, size: Float) {
        _qsTileLabelFontSize.value = size
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_QS_TILE_LABEL_FONT_SIZE, size).apply()
    }

    fun setQsTileSubtitleFontSize(context: Context, size: Float) {
        _qsTileSubtitleFontSize.value = size
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_QS_TILE_SUBTITLE_FONT_SIZE, size).apply()
    }

    fun setHideDateInExpanded(context: Context, hide: Boolean) {
        _hideDateInExpanded.value = hide
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_HIDE_DATE_IN_EXPANDED, hide).apply()
    }

    fun setDisableLabelsInExpanded(context: Context, disabled: Boolean) {
        _disableLabelsInExpanded.value = disabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DISABLE_LABELS_IN_EXPANDED, disabled).apply()
    }

    fun isFeaturePinned(featureId: String): Boolean = _pinnedFeatures.value.contains(featureId)

    fun toggleFeaturePinned(context: Context, featureId: String): Boolean {
        val current = _pinnedFeatures.value.toMutableList()
        val isNowPinned = if (current.contains(featureId)) {
            current.remove(featureId)
            false
        } else {
            current.add(0, featureId)
            true
        }
        android.util.Log.d("PanelPreferences", "Feature $featureId pinned: $isNowPinned")
        _pinnedFeatures.value = current.toList()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_PINNED_FEATURES, current.toSet()).apply()
        android.util.Log.d("PanelPreferences", "New pinned list: $current")
        return isNowPinned
    }

    fun setPinnedFeatures(context: Context, featureIds: List<String>) {
        _pinnedFeatures.value = featureIds
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_PINNED_FEATURES, featureIds.toSet()).apply()
    }

    fun setWavyThickness(context: Context, value: Float) {
        _wavyThickness.value = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_WAVY_THICKNESS, value).apply()
    }

    fun setWavyWobbliness(context: Context, value: Float) {
        _wavyWobbliness.value = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_WAVY_WOBBLINESS, value).apply()
    }

    fun setWavyWavelength(context: Context, value: Float) {
        _wavyWavelength.value = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_WAVY_WAVELENGTH, value).apply()
    }

    fun setWavyThumbRadius(context: Context, value: Float) {
        _wavyThumbRadius.value = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_WAVY_THUMB_RADIUS, value).apply()
    }

    fun setWavySpeed(context: Context, value: Int) {
        _wavySpeed.value = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_WAVY_SPEED, value).apply()
    }

    fun setSliderInactiveTrackThickness(context: Context, value: Float) {
        _sliderInactiveTrackThickness.value = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_SLIDER_INACTIVE_TRACK_THICKNESS, value).apply()
    }

    fun setSliderCornerRadius(context: Context, value: Float) {
        _sliderCornerRadius.value = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_SLIDER_CORNER_RADIUS, value).apply()
    }

    fun setA12InactiveTrackThickness(context: Context, value: Float) {
        _a12InactiveTrackThickness.value = value
        _sliderInactiveTrackThickness.value = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat(KEY_A12_INACTIVE_THICKNESS, value)
            .putFloat(KEY_SLIDER_INACTIVE_TRACK_THICKNESS, value)
            .apply()
    }

    fun setA12CornerRadius(context: Context, value: Float) {
        _a12CornerRadius.value = value
        _sliderCornerRadius.value = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat(KEY_A12_CORNER_RADIUS, value)
            .putFloat(KEY_SLIDER_CORNER_RADIUS, value)
            .apply()
    }

    fun setWavyInactiveTrackThickness(context: Context, value: Float) {
        _wavyInactiveTrackThickness.value = value
        _sliderInactiveTrackThickness.value = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat(KEY_WAVY_INACTIVE_THICKNESS, value)
            .putFloat(KEY_SLIDER_INACTIVE_TRACK_THICKNESS, value)
            .apply()
    }

    fun setWavyCornerRadius(context: Context, value: Float) {
        _wavyCornerRadius.value = value
        _sliderCornerRadius.value = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat(KEY_WAVY_CORNER_RADIUS, value)
            .putFloat(KEY_SLIDER_CORNER_RADIUS, value)
            .apply()
    }

    fun setStraightThickness(context: Context, value: Float) {
        _straightThickness.value = value
        _wavyThickness.value = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat(KEY_STRAIGHT_THICKNESS, value)
            .putFloat(KEY_WAVY_THICKNESS, value)
            .apply()
    }

    fun setStraightInactiveTrackThickness(context: Context, value: Float) {
        _straightInactiveTrackThickness.value = value
        _sliderInactiveTrackThickness.value = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat(KEY_STRAIGHT_INACTIVE_THICKNESS, value)
            .putFloat(KEY_SLIDER_INACTIVE_TRACK_THICKNESS, value)
            .apply()
    }

    fun setStraightCornerRadius(context: Context, value: Float) {
        _straightCornerRadius.value = value
        _sliderCornerRadius.value = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat(KEY_STRAIGHT_CORNER_RADIUS, value)
            .putFloat(KEY_SLIDER_CORNER_RADIUS, value)
            .apply()
    }

    fun setStraightThumbRadius(context: Context, value: Float) {
        _straightThumbRadius.value = value
        _wavyThumbRadius.value = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat(KEY_STRAIGHT_THUMB_RADIUS, value)
            .putFloat(KEY_WAVY_THUMB_RADIUS, value)
            .apply()
    }

    fun resetWavyParamsToDefault(context: Context) {
        _wavyThickness.value = 4.0f
        _wavyWobbliness.value = 3.5f
        _wavyWavelength.value = 26.0f
        _wavyThumbRadius.value = 6.0f
        _wavySpeed.value = 1300
        _sliderInactiveTrackThickness.value = null
        _sliderCornerRadius.value = 19.0f
        _a12InactiveTrackThickness.value = 38.0f
        _a12CornerRadius.value = 19.0f
        _wavyInactiveTrackThickness.value = 4.0f
        _wavyCornerRadius.value = 4.0f
        _straightThickness.value = 4.0f
        _straightInactiveTrackThickness.value = 4.0f
        _straightCornerRadius.value = 4.0f
        _straightThumbRadius.value = 6.0f
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat(KEY_WAVY_THICKNESS, 4.0f)
            .putFloat(KEY_WAVY_WOBBLINESS, 3.5f)
            .putFloat(KEY_WAVY_WAVELENGTH, 26.0f)
            .putFloat(KEY_WAVY_THUMB_RADIUS, 6.0f)
            .putInt(KEY_WAVY_SPEED, 1300)
            .remove(KEY_SLIDER_INACTIVE_TRACK_THICKNESS)
            .putFloat(KEY_SLIDER_CORNER_RADIUS, 19.0f)
            .putFloat(KEY_A12_INACTIVE_THICKNESS, 38.0f)
            .putFloat(KEY_A12_CORNER_RADIUS, 19.0f)
            .putFloat(KEY_WAVY_INACTIVE_THICKNESS, 4.0f)
            .putFloat(KEY_WAVY_CORNER_RADIUS, 4.0f)
            .putFloat(KEY_STRAIGHT_THICKNESS, 4.0f)
            .putFloat(KEY_STRAIGHT_INACTIVE_THICKNESS, 4.0f)
            .putFloat(KEY_STRAIGHT_CORNER_RADIUS, 4.0f)
            .putFloat(KEY_STRAIGHT_THUMB_RADIUS, 6.0f)
            .apply()
    }

    fun setPanelGlobalCornerEnabled(context: Context, enabled: Boolean) {
        _panelGlobalCornerEnabled.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_PANEL_GLOBAL_CORNER_ENABLED, enabled).apply()
        if (enabled) {
            setPanelInnerCorner(context, _panelOuterCorner.value)
        }
    }

    fun setPanelOuterCorner(context: Context, value: Float) {
        _panelOuterCorner.value = value
        _qsBackgroundCorner.value = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_PANEL_OUTER_CORNER, value).apply()
        prefs.edit().putFloat(KEY_QS_BACKGROUND_CORNER, value).apply()
        if (_panelGlobalCornerEnabled.value) {
            _panelInnerCorner.value = value
            prefs.edit().putFloat(KEY_PANEL_INNER_CORNER, value).apply()
        }
    }

    fun setPanelInnerCorner(context: Context, value: Float) {
        _panelInnerCorner.value = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_PANEL_INNER_CORNER, value).apply()
    }

    fun exportSettingsJson(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = org.json.JSONObject()
        val all = prefs.all
        for ((key, value) in all) {
            when (value) {
                is Boolean -> json.put(key, value)
                is Int -> json.put(key, value)
                is Long -> json.put(key, value)
                is Float -> json.put(key, value.toDouble())
                is String -> json.put(key, value)
                is Set<*> -> {
                    val array = org.json.JSONArray()
                    value.forEach { array.put(it) }
                    json.put(key, array)
                }
            }
        }
        return json.toString(2)
    }

    fun importSettingsJson(context: Context, jsonString: String): Boolean {
        return try {
            val json = org.json.JSONObject(jsonString)
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                if (json.isNull(key)) continue
                val value = json.get(key)
                when (value) {
                    is Boolean -> editor.putBoolean(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Double -> editor.putFloat(key, value.toFloat())
                    is String -> editor.putString(key, value)
                    is org.json.JSONArray -> {
                        val set = mutableSetOf<String>()
                        for (i in 0 until value.length()) {
                            set.add(value.getString(i))
                        }
                        editor.putStringSet(key, set)
                    }
                }
            }
            editor.apply()
            init(context)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun setQsBackgroundColor(context: Context, color: Int?) {
        _qsBackgroundColor.value = color
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        if (color != null) {
            editor.putInt(KEY_QS_BACKGROUND_COLOR, color)
        } else {
            editor.remove(KEY_QS_BACKGROUND_COLOR)
        }
        editor.apply()
    }

    fun setQsPanelBackgroundColor(context: Context, color: Int?) {
        _qsPanelBackgroundColor.value = color
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        if (color != null) {
            editor.putInt(KEY_QS_PANEL_BACKGROUND_COLOR, color)
        } else {
            editor.remove(KEY_QS_PANEL_BACKGROUND_COLOR)
        }
        editor.apply()
    }

    fun setQsBackgroundCorner(context: Context, corner: Float) {
        _qsBackgroundCorner.value = corner
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_QS_BACKGROUND_CORNER, corner).apply()
    }
}
