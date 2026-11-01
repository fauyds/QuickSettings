package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PanelPreferences
import com.example.data.local.QuickPanelDatabase
import com.example.data.repository.TileRepository
import com.example.model.NotificationActionModel
import com.example.model.NotificationItem
import com.example.model.PrivilegeMode
import com.example.model.QsGridLayout
import com.example.model.QsTileModel
import com.example.service.QuickPanelAccessibilityService
import com.example.service.QuickPanelNotificationListenerService
import com.example.service.QuickSettingsOverlayService
import com.example.system.ExternalTileScanner
import com.example.system.NativeTileManager
import com.example.system.PrivilegeEngine
import com.example.system.TileRegistry
import com.example.ui.overlay.QsExpansionStage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = QuickPanelDatabase.getInstance(application)
    private val repository = TileRepository(database.tileDao())
    private val prefs = application.getSharedPreferences("quickpanel_prefs", Context.MODE_PRIVATE)

    val privilegeEngine = PrivilegeEngine(application)
    val nativeTileManager = NativeTileManager(application, privilegeEngine, viewModelScope)
    val externalTileScanner = ExternalTileScanner(application)
    val tileRegistry = TileRegistry(
        context = application,
        repository = repository,
        nativeTileManager = nativeTileManager,
        externalTileScanner = externalTileScanner,
        scope = viewModelScope
    )

    // UI State
    val tiles: StateFlow<List<QsTileModel>> = kotlinx.coroutines.flow.combine(
        tileRegistry.allTiles,
        PanelPreferences.hiddenTileIds
    ) { all, hidden ->
        all.filter {
            it.isVisible && it.id !in hidden &&
            (if (it.id == "sound_profile") "sound_mode" !in hidden && "volume" !in hidden else true)
        }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    val allRawTiles: StateFlow<List<QsTileModel>> = tileRegistry.allTiles

    val hiddenTileIds: StateFlow<Set<String>> = PanelPreferences.hiddenTileIds

    fun setTileHidden(tileId: String, hidden: Boolean) {
        val canonicalId = if (tileId == "sound_mode" || tileId == "volume") "sound_profile" else tileId
        PanelPreferences.setTileHidden(getApplication(), canonicalId, hidden)
        tileRegistry.setTileVisibility(canonicalId, !hidden)
        if (canonicalId == "sound_profile") {
            PanelPreferences.setTileHidden(getApplication(), "sound_mode", hidden)
            PanelPreferences.setTileHidden(getApplication(), "volume", hidden)
        }
    }

    fun applySmartHideUnsupportedTiles() {
        val currentModeVal = privilegeEngine.currentMode.value
        val hiddenSet = mutableSetOf<String>()
        if (currentModeVal == PrivilegeMode.STANDARD) {
            // Hide advanced features that require Shell/ADB/Root in standard mode
            hiddenSet.addAll(listOf(
                "wifi", "cellular", "hotspot", "location", "airplane", "nfc", 
                "battery_saver", "dnd", "dark_mode", "aod", "ambient_display", 
                "screenshot", "cpu_info", "fps_info", "sound_search",
                "bluetooth", "caffeine", "heads_up", "reading_mode"
            ))
        }
        PanelPreferences.setAllTilesHidden(getApplication(), hiddenSet)
        tileRegistry.allTiles.value.forEach { tile ->
            val shouldHide = tile.id in hiddenSet
            tileRegistry.setTileVisibility(tile.id, !shouldHide)
        }
    }
    val notifications: StateFlow<List<NotificationItem>> = QuickPanelNotificationListenerService.notifications

    private val _expansionStage = MutableStateFlow(QsExpansionStage.FULL_QS)
    val expansionStage: StateFlow<QsExpansionStage> = _expansionStage.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    // Overlay ON / OFF Master Switch
    private val _isOverlayEnabled = MutableStateFlow(prefs.getBoolean("overlay_enabled", true))
    val isOverlayEnabled: StateFlow<Boolean> = _isOverlayEnabled.asStateFlow()

    private val _isOverlayServiceRunning = MutableStateFlow(
        (QuickSettingsOverlayService.isRunning || (QuickPanelAccessibilityService.isRunning && QuickPanelAccessibilityService.instance?.isInterceptionEnabled == true)) && _isOverlayEnabled.value
    )
    val isOverlayServiceRunning: StateFlow<Boolean> = _isOverlayServiceRunning.asStateFlow()

    val qsGridLayout: StateFlow<QsGridLayout> = PanelPreferences.gridLayout
    val qqsTileSize: StateFlow<Int> = PanelPreferences.qqsTileSize
    val qqsTileCount: StateFlow<Int> = PanelPreferences.qqsTileCount
    val editButtonPosition: StateFlow<com.example.data.EditButtonPosition> = PanelPreferences.editButtonPosition
    val showDateInQqsHeader: StateFlow<Boolean> = PanelPreferences.showDateInQqsHeader

    private val _showInitialSetup = MutableStateFlow(false)
    val showInitialSetup: StateFlow<Boolean> = _showInitialSetup.asStateFlow()

    init {
        PanelPreferences.init(application)
        privilegeEngine.syncWithPreferences()
        com.example.data.NotificationBlacklistManager.init(application)
        _showInitialSetup.value = !PanelPreferences.initialSetupCompleted.value
        refreshServiceState()
    }

    fun openInitialSetup() {
        _showInitialSetup.value = true
    }

    fun completeInitialSetup() {
        PanelPreferences.setInitialSetupCompleted(getApplication(), true)
        _showInitialSetup.value = false
    }

    fun dismissInitialSetup() {
        _showInitialSetup.value = false
    }

    fun setQsGridLayout(layout: QsGridLayout) {
        PanelPreferences.setGridLayout(getApplication(), layout)
    }

    fun setQqsTileSize(size: Int) {
        PanelPreferences.setQqsTileSize(getApplication(), size)
    }

    fun setQqsTileCount(count: Int) {
        PanelPreferences.setQqsTileCount(getApplication(), count)
    }

    fun setEditButtonPosition(position: com.example.data.EditButtonPosition) {
        PanelPreferences.setEditButtonPosition(getApplication(), position)
    }

    fun setShowDateInQqsHeader(enabled: Boolean) {
        PanelPreferences.setShowDateInQqsHeader(getApplication(), enabled)
    }

    fun refreshServiceState() {
        val enabled = _isOverlayEnabled.value
        val running = (QuickSettingsOverlayService.isRunning || (QuickPanelAccessibilityService.isRunning && QuickPanelAccessibilityService.instance?.isInterceptionEnabled == true))
        _isOverlayServiceRunning.value = enabled && running
        privilegeEngine.refreshPermissions()
    }

    fun setExpansionStage(stage: QsExpansionStage) {
        _expansionStage.value = stage
    }

    fun handleTileClick(tile: QsTileModel) {
        tileRegistry.handleTileClick(tile)
    }

    fun handleTileLongClick(tile: QsTileModel) {
        tileRegistry.handleTileLongClick(tile)
    }

    fun toggleTileVisibility(tileId: String, currentVisible: Boolean) {
        val canonicalId = if (tileId == "sound_mode" || tileId == "volume") "sound_profile" else tileId
        val newVisible = !currentVisible
        tileRegistry.setTileVisibility(canonicalId, newVisible)
        PanelPreferences.setTileHidden(getApplication(), canonicalId, !newVisible)
        if (canonicalId == "sound_profile") {
            PanelPreferences.setTileHidden(getApplication(), "sound_mode", !newVisible)
            PanelPreferences.setTileHidden(getApplication(), "volume", !newVisible)
        }
    }

    fun toggleQqsFavorite(tileId: String, currentFavorite: Boolean) {
        tileRegistry.toggleQqsFavorite(tileId, currentFavorite)
    }

    fun moveTile(fromIndex: Int, toIndex: Int) {
        tileRegistry.moveTile(fromIndex, toIndex, tiles.value)
    }

    fun updateTilesOrderAndVisibility(activeTiles: List<QsTileModel>, inactiveTiles: List<QsTileModel>) {
        tileRegistry.updateTilesOrderAndVisibility(activeTiles, inactiveTiles)
        val inactiveIds = inactiveTiles.map { it.id }.toSet()
        PanelPreferences.setAllTilesHidden(getApplication(), inactiveIds)
    }

    fun setBrightness(value: Int) {
        nativeTileManager.setBrightness(value)
    }

    fun setStreamVolume(streamType: Int, fraction: Float) {
        nativeTileManager.setStreamVolume(streamType, fraction)
    }

    fun setPrivilegeMode(mode: PrivilegeMode) {
        privilegeEngine.setMode(mode)
        PanelPreferences.setPrivilegeMode(getApplication(), mode)
    }

    fun addExternalTile(tile: QsTileModel) {
        tileRegistry.addExternalTile(tile)
    }

    fun isNotificationPermissionGranted(): Boolean {
        return QuickPanelNotificationListenerService.isPermissionGranted(getApplication())
    }

    fun openNotificationListenerSettings() {
        QuickPanelNotificationListenerService.openPermissionSettings(getApplication())
    }

    fun launchNotification(item: com.example.model.NotificationItem) {
        QuickPanelNotificationListenerService.launchNotification(item, getApplication())
    }

    fun dismissNotification(key: String) {
        QuickPanelNotificationListenerService.dismissNotification(key)
    }

    fun dismissAllNotifications() {
        QuickPanelNotificationListenerService.dismissAllNotifications()
    }

    fun executeNotificationAction(action: com.example.model.NotificationActionModel) {
        QuickPanelNotificationListenerService.executeAction(action)
    }

    /**
     * Master Overlay Off/On Toggle:
     * Starts or stops both the Accessibility Trigger Interceptor and the Foreground Overlay Window Service.
     */
    fun toggleOverlayService(targetState: Boolean? = null) {
        val app = getApplication<Application>()
        val nextState = targetState ?: !_isOverlayEnabled.value

        _isOverlayEnabled.value = nextState
        prefs.edit().putBoolean("overlay_enabled", nextState).apply()

        // 1. Accessibility Service Interception
        if (QuickPanelAccessibilityService.isRunning) {
            QuickPanelAccessibilityService.setInterceptionEnabled(nextState)
            if (!nextState) {
                QuickPanelAccessibilityService.hideCustomPanel()
            }
        }

        // 2. Standalone Overlay Service
        if (nextState) {
            if (Settings.canDrawOverlays(app)) {
                startOverlayServiceInternal()
            } else if (!QuickPanelAccessibilityService.isRunning) {
                privilegeEngine.requestOverlayPermission()
            }
        } else {
            if (QuickSettingsOverlayService.isRunning) {
                val stopIntent = Intent(app, QuickSettingsOverlayService::class.java).apply {
                    action = QuickSettingsOverlayService.ACTION_STOP
                }
                app.startService(stopIntent)
            }
        }

        _isOverlayServiceRunning.value = nextState
    }

    /**
     * Test / Trigger Floating Overlay Window
     */
    fun testFloatingOverlayWindow() {
        val app = getApplication<Application>()

        if (QuickPanelAccessibilityService.isRunning && _isOverlayEnabled.value) {
            QuickPanelAccessibilityService.showCustomPanel(QsExpansionStage.FULL_QS)
            refreshServiceState()
            return
        }

        if (!Settings.canDrawOverlays(app)) {
            privilegeEngine.requestOverlayPermission()
            return
        }

        if (!_isOverlayEnabled.value) {
            toggleOverlayService(true)
        }

        if (!QuickSettingsOverlayService.isRunning) {
            startOverlayServiceInternal()
        }

        val expandIntent = Intent(app, QuickSettingsOverlayService::class.java).apply {
            action = QuickSettingsOverlayService.ACTION_TOGGLE_EXPAND
        }
        app.startService(expandIntent)
        refreshServiceState()
    }

    private fun startOverlayServiceInternal() {
        val app = getApplication<Application>()
        val startIntent = Intent(app, QuickSettingsOverlayService::class.java).apply {
            action = QuickSettingsOverlayService.ACTION_START
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            app.startForegroundService(startIntent)
        } else {
            app.startService(startIntent)
        }
        _isOverlayServiceRunning.value = true
    }
}
