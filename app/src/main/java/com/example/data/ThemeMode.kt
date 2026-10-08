package com.example.data

import android.content.Context
import com.example.model.QsGridLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    HIDDEN("hidden", "Hidden");

    companion object {
        fun fromId(id: String?): DatePosition =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: BELOW_TIME
    }
}

enum class TileShape(val id: String, val label: String, val description: String) {
    CIRCLE("circle", "Circle", "Classic round tile shape"),
    SQUIRCLE("squircle", "Squircle", "Rounded square with 14dp corners"),
    SQUARE("square", "Square", "Sharp square with 4dp corners"),
    TEARDROP("teardrop", "Teardrop", "Asymmetric teardrop style"),
    PILL("pill", "Pill", "Capsule pill shape");

    companion object {
        fun fromId(id: String?): TileShape =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: CIRCLE
    }
}

object PanelPreferences {
    private const val PREFS_NAME = "quickpanel_prefs"
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
    private const val KEY_HIDE_QQS_DRAG_HANDLE = "hide_qqs_drag_handle"
    private const val KEY_SHOW_CARRIER = "show_carrier"
    private const val KEY_SHOW_CARRIER_DIVIDER = "show_carrier_divider"
    private const val KEY_BRIGHTNESS_AT_BOTTOM = "brightness_at_bottom"
    private const val KEY_BRIGHTNESS_IN_QQS = "brightness_in_qqs"
    private const val KEY_ENABLE_CONTAINER_BLUR = "enable_container_blur"
    private const val KEY_INVISIBLE_STATUS_BAR_TRIGGER = "invisible_status_bar_trigger"
    private const val KEY_FORCE_IMMERSIVE_TRIGGER = "force_immersive_trigger"
    private const val KEY_OVERRIDE_SYSTEM_QS = "override_system_qs"
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

    // Heads-Up Overlay Preferences
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

    private val _datePosition = MutableStateFlow(DatePosition.BELOW_TIME)
    val datePosition: StateFlow<DatePosition> = _datePosition.asStateFlow()

    private val _enabledTileShape = MutableStateFlow(TileShape.CIRCLE)
    val enabledTileShape: StateFlow<TileShape> = _enabledTileShape.asStateFlow()

    private val _disabledTileShape = MutableStateFlow(TileShape.CIRCLE)
    val disabledTileShape: StateFlow<TileShape> = _disabledTileShape.asStateFlow()

    private val _tileShape = _enabledTileShape
    val tileShape: StateFlow<TileShape> = _tileShape.asStateFlow()

    private val _activeSquareShape = MutableStateFlow(false)
    val activeSquareShape: StateFlow<Boolean> = _activeSquareShape.asStateFlow()

    private val _hideQqsDragHandle = MutableStateFlow(false)
    val hideQqsDragHandle: StateFlow<Boolean> = _hideQqsDragHandle.asStateFlow()

    private val _dragHandleVerticalOffset = MutableStateFlow(4f)
    val dragHandleVerticalOffset: StateFlow<Float> = _dragHandleVerticalOffset.asStateFlow()

    private val _dragHandleLength = MutableStateFlow(36f)
    val dragHandleLength: StateFlow<Float> = _dragHandleLength.asStateFlow()

    private val _showCarrier = MutableStateFlow(false)
    val showCarrier: StateFlow<Boolean> = _showCarrier.asStateFlow()

    private val _showCarrierDivider = MutableStateFlow(false)
    val showCarrierDivider: StateFlow<Boolean> = _showCarrierDivider.asStateFlow()

    private val _brightnessAtBottom = MutableStateFlow(false)
    val brightnessAtBottom: StateFlow<Boolean> = _brightnessAtBottom.asStateFlow()

    private val _brightnessInQqs = MutableStateFlow(false)
    val brightnessInQqs: StateFlow<Boolean> = _brightnessInQqs.asStateFlow()

    private val _enableContainerBlur = MutableStateFlow(false)
    val enableContainerBlur: StateFlow<Boolean> = _enableContainerBlur.asStateFlow()

    private val _invisibleStatusBarTrigger = MutableStateFlow(false)
    val invisibleStatusBarTrigger: StateFlow<Boolean> = _invisibleStatusBarTrigger.asStateFlow()

    private val _forceImmersiveTrigger = MutableStateFlow(false)
    val forceImmersiveTrigger: StateFlow<Boolean> = _forceImmersiveTrigger.asStateFlow()

    private val _overrideSystemQs = MutableStateFlow(false)
    val overrideSystemQs: StateFlow<Boolean> = _overrideSystemQs.asStateFlow()

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
        val savedEnabledTileShape = prefs.getString(KEY_ENABLED_TILE_SHAPE, null) ?: prefs.getString(KEY_TILE_SHAPE, null)
        if (savedEnabledTileShape != null) {
            val shape = TileShape.fromId(savedEnabledTileShape)
            _enabledTileShape.value = shape
            _activeSquareShape.value = (shape == TileShape.SQUIRCLE || shape == TileShape.SQUARE)
        } else {
            val legacySquare = prefs.getBoolean(KEY_ACTIVE_SQUARE_SHAPE, false)
            _activeSquareShape.value = legacySquare
            _enabledTileShape.value = if (legacySquare) TileShape.SQUIRCLE else TileShape.CIRCLE
        }
        val savedDisabledTileShape = prefs.getString(KEY_DISABLED_TILE_SHAPE, null)
        if (savedDisabledTileShape != null) {
            _disabledTileShape.value = TileShape.fromId(savedDisabledTileShape)
        }
        _hideQqsDragHandle.value = prefs.getBoolean(KEY_HIDE_QQS_DRAG_HANDLE, false)
        _dragHandleVerticalOffset.value = prefs.getFloat(KEY_DRAG_HANDLE_VERTICAL_OFFSET, 4f)
        _dragHandleLength.value = prefs.getFloat(KEY_DRAG_HANDLE_LENGTH, 36f)
        _showCarrier.value = prefs.getBoolean(KEY_SHOW_CARRIER, false)
        _showCarrierDivider.value = prefs.getBoolean(KEY_SHOW_CARRIER_DIVIDER, false)
        _brightnessAtBottom.value = prefs.getBoolean(KEY_BRIGHTNESS_AT_BOTTOM, false)
        _brightnessInQqs.value = prefs.getBoolean(KEY_BRIGHTNESS_IN_QQS, false)
        _enableContainerBlur.value = prefs.getBoolean(KEY_ENABLE_CONTAINER_BLUR, false)
        _invisibleStatusBarTrigger.value = prefs.getBoolean(KEY_INVISIBLE_STATUS_BAR_TRIGGER, false)
        _forceImmersiveTrigger.value = prefs.getBoolean(KEY_FORCE_IMMERSIVE_TRIGGER, false)
        _overrideSystemQs.value = prefs.getBoolean(KEY_OVERRIDE_SYSTEM_QS, false)
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

    fun setShowDateInQqsHeader(context: Context, enabled: Boolean) {
        val pos = if (enabled) DatePosition.NEXT_TO_TIME else DatePosition.BELOW_TIME
        setDatePosition(context, pos)
    }

    fun setEnabledTileShape(context: Context, shape: TileShape) {
        _enabledTileShape.value = shape
        _activeSquareShape.value = (shape == TileShape.SQUIRCLE || shape == TileShape.SQUARE)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_ENABLED_TILE_SHAPE, shape.id)
            .putString(KEY_TILE_SHAPE, shape.id)
            .putBoolean(KEY_ACTIVE_SQUARE_SHAPE, shape == TileShape.SQUIRCLE || shape == TileShape.SQUARE)
            .apply()
    }

    fun setDisabledTileShape(context: Context, shape: TileShape) {
        _disabledTileShape.value = shape
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_DISABLED_TILE_SHAPE, shape.id)
            .apply()
    }

    fun setTileShape(context: Context, shape: TileShape) {
        setEnabledTileShape(context, shape)
    }

    fun setActiveSquareShape(context: Context, enabled: Boolean) {
        val shape = if (enabled) TileShape.SQUIRCLE else TileShape.CIRCLE
        setTileShape(context, shape)
    }

    fun setHideQqsDragHandle(context: Context, hide: Boolean) {
        _hideQqsDragHandle.value = hide
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_HIDE_QQS_DRAG_HANDLE, hide).apply()
    }

    fun setShowCarrier(context: Context, show: Boolean) {
        _showCarrier.value = show
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SHOW_CARRIER, show).apply()
    }

    fun setShowCarrierDivider(context: Context, show: Boolean) {
        _showCarrierDivider.value = show
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SHOW_CARRIER_DIVIDER, show).apply()
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
}
