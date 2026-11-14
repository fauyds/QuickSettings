package com.example.system

import android.content.Context
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.example.model.LongPressActionType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.ui.graphics.Color
import com.example.data.local.TileEntity
import com.example.data.repository.TileRepository
import com.example.data.PanelPreferences
import com.example.data.TileCommandRepository
import android.media.AudioManager
import android.util.Log
import android.view.KeyEvent
import com.example.model.CommandExecutionMode
import com.example.model.ComplicationAction
import com.example.model.ComplicationLayoutMode
import com.example.model.ComplicationSublinePlacement
import com.example.model.ComplicationTileModel
import com.example.model.TileActionOverrideType
import com.example.model.TileCommandConfig
import com.example.model.QsTileModel
import com.example.model.TileCategory
import com.example.model.TileState
import com.example.ui.tile.ComplicationTileController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TileRegistry(
    private val context: Context,
    private val repository: TileRepository,
    val nativeTileManager: NativeTileManager,
    val externalTileScanner: ExternalTileScanner,
    private val scope: CoroutineScope
) {
    val forcedStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val customTileActiveStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    companion object {
        // Base system tile definition templates including Standard + Custom ROM Tiles
        val defaultSystemTiles = listOf(
            // Standard Android Core Tiles
            TileEntity("wifi", "Internet", "Wi-Fi", TileCategory.CONNECTIVITY.name, 0, isVisible = true, isQqsFavorite = true, settingsAction = Settings.Panel.ACTION_INTERNET_CONNECTIVITY),
            TileEntity("bluetooth", "Bluetooth", "Devices", TileCategory.CONNECTIVITY.name, 1, isVisible = true, isQqsFavorite = true, settingsAction = Settings.ACTION_BLUETOOTH_SETTINGS),
            TileEntity("torch", "Flashlight", "Torch", TileCategory.HARDWARE.name, 2, isVisible = true, isQqsFavorite = true),
            TileEntity("autorotate", "Auto-rotate", "Orientation", TileCategory.SYSTEM.name, 3, isVisible = true, isQqsFavorite = true, settingsAction = Settings.ACTION_DISPLAY_SETTINGS),
            TileEntity("dnd", "Do Not Disturb", "Sound Mode", TileCategory.SYSTEM.name, 4, isVisible = true, isQqsFavorite = true, settingsAction = Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS),
            TileEntity("airplane", "Airplane mode", "Offline", TileCategory.CONNECTIVITY.name, 5, isVisible = true, isQqsFavorite = true, settingsAction = Settings.ACTION_AIRPLANE_MODE_SETTINGS),
            TileEntity("location", "Location", "GPS", TileCategory.SYSTEM.name, 6, isVisible = true, isQqsFavorite = false, settingsAction = Settings.ACTION_LOCATION_SOURCE_SETTINGS),
            TileEntity("cellular", "Mobile Data", "Carrier", TileCategory.CONNECTIVITY.name, 7, isVisible = true, isQqsFavorite = false, settingsAction = Settings.ACTION_DATA_ROAMING_SETTINGS),
            TileEntity("hotspot", "Hotspot", "Tethering", TileCategory.CONNECTIVITY.name, 8, isVisible = true, isQqsFavorite = false, settingsAction = Settings.ACTION_WIRELESS_SETTINGS),
            TileEntity("dark_mode", "Dark theme", "Display", TileCategory.AUDIO_DISPLAY.name, 9, isVisible = true, isQqsFavorite = false, settingsAction = Settings.ACTION_DISPLAY_SETTINGS),
            TileEntity("battery_saver", "Battery Saver", "Power Mode", TileCategory.SYSTEM.name, 10, isVisible = true, isQqsFavorite = false, settingsAction = Settings.ACTION_BATTERY_SAVER_SETTINGS),
            TileEntity("nfc", "NFC", "Contactless", TileCategory.CONNECTIVITY.name, 11, isVisible = false, isQqsFavorite = false, settingsAction = Settings.ACTION_NFC_SETTINGS),

            // Custom ROM & System-Specific Tiles (crDroid, LineageOS, PixelExperience, etc.)
            TileEntity("caffeine", "Caffeine", "Keep Awake", TileCategory.SYSTEM.name, 12, isVisible = true, isQqsFavorite = false, settingsAction = Settings.ACTION_DISPLAY_SETTINGS),
            TileEntity("heads_up", "Heads Up", "Pop-up alerts", TileCategory.SYSTEM.name, 13, isVisible = true, isQqsFavorite = false),
            TileEntity("aod", "Always-On Display", "Ambient Display", TileCategory.AUDIO_DISPLAY.name, 14, isVisible = true, isQqsFavorite = false, settingsAction = "android.settings.LOCK_SCREEN_SETTINGS"),
            TileEntity("ambient_display", "Ambient Display", "Wake Screen", TileCategory.AUDIO_DISPLAY.name, 22, isVisible = true, isQqsFavorite = false, settingsAction = Settings.ACTION_DISPLAY_SETTINGS),
            TileEntity("sound_search", "Sound Search", "Music Identify", TileCategory.EXTERNAL.name, 15, isVisible = false, isQqsFavorite = false),
            TileEntity("cpu_info", "CPU Info", "Performance", TileCategory.HARDWARE.name, 16, isVisible = false, isQqsFavorite = false),
            TileEntity("fps_info", "FPS Info", "Overlay", TileCategory.HARDWARE.name, 17, isVisible = false, isQqsFavorite = false),
            TileEntity("reading_mode", "Reading Mode", "Eye Comfort", TileCategory.AUDIO_DISPLAY.name, 18, isVisible = false, isQqsFavorite = false),
            TileEntity("sound_profile", "Sound Profile", "Ringer Mode", TileCategory.SYSTEM.name, 19, isVisible = false, isQqsFavorite = false),
            TileEntity("screenshot", "Screenshot", "Capture", TileCategory.HARDWARE.name, 20, isVisible = false, isQqsFavorite = false),
            TileEntity("screenrecord", "Screen Record", "Recorder", TileCategory.HARDWARE.name, 21, isVisible = true, isQqsFavorite = false),
            TileEntity("settings", "Settings", "System", TileCategory.SYSTEM.name, 23, isVisible = true, isQqsFavorite = false, settingsAction = Settings.ACTION_SETTINGS),
            TileEntity("customize", "Customize", "Quick Panel", TileCategory.SYSTEM.name, 24, isVisible = true, isQqsFavorite = false),

            // Live Complication QS Tiles (Wear OS Ring + QS Hybrid, movable in QS Editor)
            TileEntity("comp_battery", "Battery", "Level", TileCategory.COMPLICATION.name, 25, isVisible = true, isQqsFavorite = false, settingsAction = Settings.ACTION_BATTERY_SAVER_SETTINGS),
            TileEntity("comp_storage", "Storage", "Internal", TileCategory.COMPLICATION.name, 26, isVisible = true, isQqsFavorite = false, settingsAction = Settings.ACTION_INTERNAL_STORAGE_SETTINGS),
            TileEntity("comp_brightness", "Brightness", "Display", TileCategory.COMPLICATION.name, 27, isVisible = false, isQqsFavorite = false, settingsAction = Settings.ACTION_DISPLAY_SETTINGS),
            TileEntity("comp_volume", "Volume", "Media", TileCategory.COMPLICATION.name, 28, isVisible = false, isQqsFavorite = false, settingsAction = Settings.ACTION_SOUND_SETTINGS)
        )

        internal fun resolveWifiSubtitle(
            wifiOn: Boolean,
            wifiConnected: Boolean,
            wifiSsidName: String?,
            panelStyle: com.example.data.QsPanelStyle = com.example.data.QsPanelStyle.DEFAULT
        ): String {
            if (!wifiOn) return "Off"
            if (!wifiConnected) return "On"
            val cleaned = NativeTileManager.cleanSsid(wifiSsidName)
            return if (!cleaned.isNullOrBlank()) cleaned else "Wi-Fi"
        }

        internal fun resolveBluetoothSubtitle(
            btOn: Boolean,
            btDeviceName: String?,
            panelStyle: com.example.data.QsPanelStyle = com.example.data.QsPanelStyle.DEFAULT
        ): String {
            if (!btOn) return "Off"
            val cleaned = btDeviceName?.trim()
            return if (!cleaned.isNullOrBlank()) cleaned else "On"
        }

        internal fun resolveMarshmallowTileLabel(
            tile: QsTileModel,
            wifiSsidName: String? = null,
            btDeviceName: String? = null
        ): String {
            if (tile.state != TileState.ACTIVE) {
                return tile.label
            }
            return when (tile.id) {
                "wifi" -> {
                    if (tile.subtitle == "On") {
                        "Wi-Fi"
                    } else {
                        val directSsid = NativeTileManager.cleanSsid(wifiSsidName)
                        val subtitleSsid = tile.subtitle.takeIf {
                            it.isNotBlank() && it !in setOf(
                                "On", "Off", "Connected", "Wi-Fi",
                                "Active", "Always Active", "Forced On", "Forced Off", "Toggle"
                            )
                        }
                        directSsid ?: subtitleSsid ?: "Wi-Fi"
                    }
                }
                "bluetooth" -> {
                    val directBt = btDeviceName?.trim()?.takeIf { it.isNotBlank() }
                    val subtitleBt = tile.subtitle.takeIf {
                        it.isNotBlank() && it !in setOf(
                            "On", "Off", "Devices",
                            "Active", "Always Active", "Forced On", "Forced Off", "Toggle"
                        )
                    }
                    directBt ?: subtitleBt ?: tile.label
                }
                else -> tile.label
            }
        }
    }

    val showScreenRecordSheet = MutableStateFlow(false)

    init {
        scope.launch {
            // Seed default tiles if database is empty
            repository.allTiles.collect { currentList ->
                if (currentList.isEmpty()) {
                    repository.insertAll(defaultSystemTiles)
                } else {
                    val hasAmbient = currentList.any { it.tileId == "ambient_display" }
                    if (!hasAmbient) {
                        repository.insert(TileEntity("ambient_display", "Ambient Display", "Wake Screen", TileCategory.AUDIO_DISPLAY.name, 22, isVisible = true, isQqsFavorite = false, settingsAction = Settings.ACTION_DISPLAY_SETTINGS))
                    }
                    val aodTile = currentList.find { it.tileId == "aod" }
                    if (aodTile != null && aodTile.label != "Always-On Display") {
                        repository.insert(aodTile.copy(label = "Always-On Display", subtitle = "Ambient Display", isVisible = true, settingsAction = "android.settings.LOCK_SCREEN_SETTINGS"))
                    }
                    val screenRecordTile = currentList.find { it.tileId == "screenrecord" }
                    if (screenRecordTile == null) {
                        repository.insert(TileEntity("screenrecord", "Screen Record", "Recorder", TileCategory.HARDWARE.name, 21, isVisible = true, isQqsFavorite = false))
                    }
                    val settingsTile = currentList.find { it.tileId == "settings" }
                    if (settingsTile == null) {
                        repository.insert(TileEntity("settings", "Settings", "System", TileCategory.SYSTEM.name, 23, isVisible = true, isQqsFavorite = false, settingsAction = Settings.ACTION_SETTINGS))
                    }
                    val customizeTile = currentList.find { it.tileId == "customize" }
                    if (customizeTile == null) {
                        repository.insert(TileEntity("customize", "Customize", "Quick Panel", TileCategory.SYSTEM.name, 24, isVisible = true, isQqsFavorite = false))
                    }
                    if (currentList.none { it.tileId == "comp_battery" }) {
                        repository.insert(TileEntity("comp_battery", "Battery", "Level", TileCategory.COMPLICATION.name, 25, isVisible = true, isQqsFavorite = false, settingsAction = Settings.ACTION_BATTERY_SAVER_SETTINGS))
                    }
                    if (currentList.none { it.tileId == "comp_storage" }) {
                        repository.insert(TileEntity("comp_storage", "Storage", "Internal", TileCategory.COMPLICATION.name, 26, isVisible = true, isQqsFavorite = false, settingsAction = Settings.ACTION_INTERNAL_STORAGE_SETTINGS))
                    }
                    if (currentList.none { it.tileId == "comp_brightness" }) {
                        repository.insert(TileEntity("comp_brightness", "Brightness", "Display", TileCategory.COMPLICATION.name, 27, isVisible = false, isQqsFavorite = false, settingsAction = Settings.ACTION_DISPLAY_SETTINGS))
                    }
                    if (currentList.none { it.tileId == "comp_volume" }) {
                        repository.insert(TileEntity("comp_volume", "Volume", "Media", TileCategory.COMPLICATION.name, 28, isVisible = false, isQqsFavorite = false, settingsAction = Settings.ACTION_SOUND_SETTINGS))
                    }
                }
            }
        }
    }

    // Combined flow producing list of all QsTileModels with live states
    val allTiles: StateFlow<List<QsTileModel>> = combine(
        repository.allTiles,
        nativeTileManager.isWifiEnabled,
        nativeTileManager.isBluetoothEnabled,
        nativeTileManager.wifiSsid,
        nativeTileManager.bluetoothDeviceName,
        nativeTileManager.isTorchEnabled,
        nativeTileManager.isAutoRotateEnabled,
        nativeTileManager.isDndEnabled,
        nativeTileManager.isAirplaneModeEnabled,
        nativeTileManager.isLocationEnabled,
        nativeTileManager.isCellularEnabled,
        nativeTileManager.isHotspotEnabled,
        nativeTileManager.isBatterySaverEnabled,
        nativeTileManager.isNfcEnabled,
        nativeTileManager.isDarkThemeEnabled,
        nativeTileManager.isAodEnabled,
        nativeTileManager.isAmbientDisplayEnabled,
        nativeTileManager.isCaffeineEnabled,
        nativeTileManager.isHeadsUpEnabled,
        nativeTileManager.isScreenRecording,
        nativeTileManager.customRomTileStates,
        externalTileScanner.scannedTiles,
        TileCommandRepository.tileConfigs,
        forcedStates,
        customTileActiveStates,
        nativeTileManager.isWifiConnected,
        PanelPreferences.panelStyle,
        nativeTileManager.batteryPercentage,
        nativeTileManager.isBatteryCharging,
        nativeTileManager.brightness,
        nativeTileManager.mediaVolume,
        PanelPreferences.complicationSublinePlacement
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val entities = args[0] as List<TileEntity>
        val wifiOn = args[1] as Boolean
        val btOn = args[2] as Boolean
        val wifiSsidName = args[3] as? String
        val btDeviceName = args[4] as? String
        val torchOn = args[5] as Boolean
        val rotateOn = args[6] as Boolean
        val dndOn = args[7] as Boolean
        val airplaneOn = args[8] as Boolean
        val locationOn = args[9] as Boolean
        val cellularOn = args[10] as Boolean
        val hotspotOn = args[11] as Boolean
        val batterySaverOn = args[12] as Boolean
        val nfcOn = args[13] as Boolean
        val darkThemeOn = args[14] as Boolean
        val aodOn = args[15] as Boolean
        val ambientDisplayOn = args[16] as Boolean
        val caffeineOn = args[17] as Boolean
        val headsUpOn = args[18] as Boolean
        val screenRecordOn = args[19] as Boolean
        @Suppress("UNCHECKED_CAST")
        val customStates = args[20] as Map<String, Boolean>
        @Suppress("UNCHECKED_CAST")
        val externalTiles = args[21] as List<QsTileModel>
        @Suppress("UNCHECKED_CAST")
        val configsMap = args[22] as Map<String, com.example.model.TileCommandConfig>
        @Suppress("UNCHECKED_CAST")
        val forced = args[23] as Map<String, Boolean>
        @Suppress("UNCHECKED_CAST")
        val customTileStates = args[24] as Map<String, Boolean>
        val wifiConnected = args[25] as Boolean
        val panelStyle = args[26] as com.example.data.QsPanelStyle
        val batteryPct = args[27] as Int
        val batteryCharging = args[28] as Boolean
        val brightnessVal = args[29] as Int
        val mediaVol = args[30] as Float
        val sublinePlacement = args[31] as ComplicationSublinePlacement
        val storageStats = ComplicationTileController.readInternalStorageFraction()

        val externalMap = externalTiles.associateBy { it.id }

        val dbTiles = entities.sortedBy { it.orderIndex }.map { entity ->
            val isThirdParty = entity.isExternal || 
                entity.categoryName.equals("THIRD_PARTY", ignoreCase = true) || 
                entity.categoryName.equals("EXTERNAL", ignoreCase = true) || 
                entity.tileId.contains("/") || 
                entity.packageName != null

            if (isThirdParty) {
                val ext = externalMap[entity.tileId] ?: externalMap.values.firstOrNull {
                    (entity.packageName != null && it.packageName == entity.packageName && it.serviceClassName == entity.serviceClassName) ||
                    (entity.tileId.contains("/") && it.packageName == entity.tileId.substringBefore('/') && it.serviceClassName == entity.tileId.substringAfter('/')) ||
                    (it.id == entity.tileId)
                }
                val pkg = entity.packageName ?: if (entity.tileId.contains("/")) entity.tileId.substringBefore('/') else ext?.packageName
                val cls = entity.serviceClassName ?: if (entity.tileId.contains("/")) entity.tileId.substringAfter('/') else ext?.serviceClassName
                val compId = if (pkg != null && cls != null) "$pkg/$cls" else entity.tileId

                val state = if (!entity.isVisible) {
                    TileState.UNAVAILABLE
                } else {
                    TileState.INACTIVE
                }
                val sub = ext?.subtitle ?: entity.subtitle.ifBlank { "" }

                QsTileModel(
                    id = compId,
                    label = ext?.label ?: entity.label,
                    subtitle = sub,
                    state = state,
                    category = TileCategory.THIRD_PARTY,
                    externalDrawable = ext?.externalDrawable,
                    isExternal = true,
                    packageName = pkg,
                    serviceClassName = cls,
                    settingsAction = entity.settingsAction,
                    orderIndex = entity.orderIndex,
                    isVisible = entity.isVisible,
                    isQqsFavorite = entity.isQqsFavorite
                )
            } else {
                val config = configsMap[entity.tileId] ?: com.example.model.TileCommandConfig(entity.tileId)
                val customOn = customStates[entity.tileId] == true
                var (state, vector, sub) = when (entity.tileId) {
                    "wifi" -> {
                        val wifiSubtitle = resolveWifiSubtitle(
                            wifiOn = wifiOn,
                            wifiConnected = wifiConnected,
                            wifiSsidName = wifiSsidName,
                            panelStyle = panelStyle
                        )
                        Triple(if (wifiOn) TileState.ACTIVE else TileState.INACTIVE, Icons.Default.Wifi, wifiSubtitle)
                    }
                    "bluetooth" -> {
                        val btSubtitle = resolveBluetoothSubtitle(
                            btOn = btOn,
                            btDeviceName = btDeviceName,
                            panelStyle = panelStyle
                        )
                        Triple(if (btOn) TileState.ACTIVE else TileState.INACTIVE, Icons.Default.Bluetooth, btSubtitle)
                    }
                    "torch" -> Triple(if (torchOn) TileState.ACTIVE else TileState.INACTIVE, if (torchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff, if (torchOn) "On" else "Off")
                    "autorotate" -> Triple(if (rotateOn) TileState.ACTIVE else TileState.INACTIVE, Icons.Default.ScreenRotation, if (rotateOn) "Auto" else "Locked")
                    "dnd" -> Triple(if (dndOn) TileState.ACTIVE else TileState.INACTIVE, Icons.Default.DoNotDisturb, if (dndOn) "Priority" else "Off")
                    "airplane" -> Triple(if (airplaneOn) TileState.ACTIVE else TileState.INACTIVE, Icons.Default.Flight, if (airplaneOn) "On" else "Off")
                    "location" -> Triple(if (locationOn) TileState.ACTIVE else TileState.INACTIVE, Icons.Default.LocationOn, if (locationOn) "High accuracy" else "Off")
                    "cellular" -> Triple(if (cellularOn) TileState.ACTIVE else TileState.INACTIVE, Icons.Default.CellTower, if (cellularOn) "LTE / 5G" else "Off")
                    "hotspot" -> Triple(if (hotspotOn) TileState.ACTIVE else TileState.INACTIVE, Icons.Default.WifiTethering, if (hotspotOn) "Active" else "Off")
                    "battery_saver" -> Triple(if (batterySaverOn) TileState.ACTIVE else TileState.INACTIVE, Icons.Default.BatterySaver, if (batterySaverOn) "On" else "Off")
                    "nfc" -> Triple(if (nfcOn) TileState.ACTIVE else TileState.INACTIVE, Icons.Default.Nfc, if (nfcOn) "On" else "Off")
                    "dark_mode" -> Triple(if (darkThemeOn) TileState.ACTIVE else TileState.INACTIVE, Icons.Default.DarkMode, if (darkThemeOn) "On" else "Off")

                    // Custom ROM / System Tiles
                    "caffeine" -> Triple(if (caffeineOn) TileState.ACTIVE else TileState.INACTIVE, Icons.Default.LocalCafe, if (caffeineOn) "Active" else "Off")
                    "heads_up" -> Triple(if (headsUpOn) TileState.ACTIVE else TileState.INACTIVE, Icons.Default.NotificationsActive, if (headsUpOn) "Enabled" else "Disabled")
                    "aod" -> Triple(if (aodOn) TileState.ACTIVE else TileState.INACTIVE, Icons.Default.Wallpaper, if (aodOn) "On" else "Off")
                    "ambient_display" -> Triple(if (ambientDisplayOn) TileState.ACTIVE else TileState.INACTIVE, Icons.Default.Visibility, if (ambientDisplayOn) "On" else "Off")
                    "sound_search" -> Triple(TileState.INACTIVE, Icons.Default.Headphones, "Identify")
                    "cpu_info" -> Triple(if (customOn) TileState.ACTIVE else TileState.INACTIVE, Icons.Default.Memory, if (customOn) "Overlay On" else "Off")
                    "fps_info" -> Triple(if (customOn) TileState.ACTIVE else TileState.INACTIVE, Icons.Default.Speed, if (customOn) "Overlay On" else "Off")
                    "reading_mode" -> Triple(if (customOn) TileState.ACTIVE else TileState.INACTIVE, Icons.Default.Visibility, if (customOn) "Warm" else "Off")
                    "sound_profile" -> Triple(if (customOn) TileState.ACTIVE else TileState.INACTIVE, Icons.Default.VolumeUp, if (customOn) "Ring" else "Vibrate")
                    "screenshot" -> Triple(TileState.INACTIVE, Icons.Default.CameraAlt, "Capture")
                    "screenrecord" -> Triple(if (screenRecordOn) TileState.ACTIVE else TileState.INACTIVE, Icons.Default.Videocam, if (screenRecordOn) "Recording..." else "Record")
                    "settings" -> Triple(TileState.INACTIVE, Icons.Default.Settings, "System")
                    "customize" -> Triple(TileState.INACTIVE, Icons.Default.Layers, "Quick Panel")
                    "comp_battery" -> {
                        val icon = when {
                            batteryCharging -> Icons.Default.BatteryChargingFull
                            batterySaverOn -> Icons.Default.BatterySaver
                            else -> Icons.Default.BatteryFull
                        }
                        val subText = when {
                            batteryCharging -> "Charging"
                            batterySaverOn -> "Saver On"
                            else -> "$batteryPct%"
                        }
                        Triple(if (batteryCharging || batterySaverOn) TileState.ACTIVE else TileState.INACTIVE, icon, subText)
                    }
                    "comp_storage" -> Triple(TileState.INACTIVE, Icons.Default.Storage, "${storageStats.second} used")
                    "comp_brightness" -> {
                        val pct = ((brightnessVal / 255f).coerceIn(0f, 1f) * 100f).toInt()
                        val icon = when {
                            pct >= 70 -> Icons.Default.BrightnessHigh
                            pct >= 35 -> Icons.Default.BrightnessMedium
                            else -> Icons.Default.BrightnessLow
                        }
                        Triple(if (pct >= 50) TileState.ACTIVE else TileState.INACTIVE, icon, "$pct%")
                    }
                    "comp_volume" -> {
                        val pct = (mediaVol.coerceIn(0f, 1f) * 100f).toInt()
                        val icon = if (dndOn || pct == 0) Icons.Default.VolumeOff else Icons.Default.VolumeUp
                        val subText = if (dndOn) "DND" else "$pct%"
                        Triple(if (!dndOn && pct > 0) TileState.ACTIVE else TileState.INACTIVE, icon, subText)
                    }

                    else -> Triple(if (customOn) TileState.ACTIVE else TileState.INACTIVE, Icons.Default.Wifi, "Toggle")
                }

                val complicationSpec: ComplicationTileModel? = when (entity.tileId) {
                    "comp_battery" -> {
                        ComplicationTileModel(
                            id = entity.tileId,
                            title = entity.label,
                            subline = "$batteryPct%",
                            progress = (batteryPct / 100f).coerceIn(0f, 1f),
                            icon = vector,
                            accentColor = null,
                            isActive = batteryCharging || batterySaverOn,
                            sublinePlacement = sublinePlacement,
                            layoutMode = ComplicationLayoutMode.ADAPTIVE,
                            tapAction = ComplicationAction.Toggle(
                                tileId = "battery_saver",
                                isActive = batterySaverOn
                            ),
                            settingsAction = entity.settingsAction ?: Settings.ACTION_BATTERY_SAVER_SETTINGS
                        )
                    }
                    "comp_storage" -> {
                        ComplicationTileModel(
                            id = entity.tileId,
                            title = entity.label,
                            subline = storageStats.second,
                            progress = storageStats.first,
                            icon = Icons.Default.Storage,
                            accentColor = null,
                            isActive = false,
                            sublinePlacement = sublinePlacement,
                            layoutMode = ComplicationLayoutMode.ADAPTIVE,
                            tapAction = ComplicationAction.Shortcut(
                                shortcutId = "open_storage",
                                intentAction = Settings.ACTION_INTERNAL_STORAGE_SETTINGS
                            ),
                            settingsAction = entity.settingsAction ?: Settings.ACTION_INTERNAL_STORAGE_SETTINGS
                        )
                    }
                    "comp_brightness" -> {
                        val brightFrac = (brightnessVal / 255f).coerceIn(0f, 1f)
                        val brightPct = (brightFrac * 100f).toInt()
                        ComplicationTileModel(
                            id = entity.tileId,
                            title = entity.label,
                            subline = "$brightPct%",
                            progress = brightFrac,
                            icon = vector,
                            accentColor = null,
                            isActive = brightPct >= 50,
                            sublinePlacement = sublinePlacement,
                            layoutMode = ComplicationLayoutMode.ADAPTIVE,
                            tapAction = ComplicationAction.Shortcut(shortcutId = "cycle_brightness"),
                            settingsAction = entity.settingsAction ?: Settings.ACTION_DISPLAY_SETTINGS
                        )
                    }
                    "comp_volume" -> {
                        val volFrac = mediaVol.coerceIn(0f, 1f)
                        val volPct = (volFrac * 100f).toInt()
                        ComplicationTileModel(
                            id = entity.tileId,
                            title = entity.label,
                            subline = if (dndOn) "DND" else "$volPct%",
                            progress = volFrac,
                            icon = vector,
                            accentColor = null,
                            isActive = !dndOn && volPct > 0,
                            sublinePlacement = sublinePlacement,
                            layoutMode = ComplicationLayoutMode.ADAPTIVE,
                            tapAction = ComplicationAction.Toggle(
                                tileId = "volume_mute",
                                isActive = !dndOn && volPct > 0
                            ),
                            settingsAction = entity.settingsAction ?: Settings.ACTION_SOUND_SETTINGS
                        )
                    }
                    else -> null
                }

                val isCustomActive = customTileStates[entity.tileId] ?: forced[entity.tileId]
                val isPureCustomTile = entity.tileId in listOf("sound_search", "cpu_info", "fps_info", "reading_mode", "sound_profile", "screenshot", "settings", "customize")
                if (isCustomActive != null && (isPureCustomTile || config.isOverrideEnabled || config.forceStateChangeAnyway)) {
                    state = if (isCustomActive) TileState.ACTIVE else TileState.INACTIVE
                    sub = if (isCustomActive) "Active" else "Off"
                }

                if (config.alwaysActive) {
                    state = TileState.ACTIVE
                    sub = "Always Active"
                } else if (config.forceStateChangeAnyway) {
                    val isForcedActive = forced[entity.tileId] ?: (state == TileState.ACTIVE)
                    state = if (isForcedActive) TileState.ACTIVE else TileState.INACTIVE
                    sub = if (isForcedActive) "Forced On" else "Forced Off"
                }

                QsTileModel(
                    id = entity.tileId,
                    label = entity.label,
                    subtitle = sub,
                    state = state,
                    category = TileCategory.fromString(entity.categoryName),
                    iconVector = vector,
                    orderIndex = entity.orderIndex,
                    isVisible = entity.isVisible,
                    isQqsFavorite = entity.isQqsFavorite,
                    settingsAction = entity.settingsAction,
                    complicationSpec = complicationSpec
                )
            }
        }

        val dbTileIds = entities.map { it.tileId }.toSet()
        val dbComponentSet = entities.mapNotNull { 
            if (it.packageName != null && it.serviceClassName != null) "${it.packageName}/${it.serviceClassName}" else null 
        }.toSet()

        val unusedExternalTiles = externalTiles.filter { ext ->
            ext.id !in dbTileIds && 
            (ext.packageName == null || "${ext.packageName}/${ext.serviceClassName}" !in dbComponentSet)
        }.map { ext ->
            ext.copy(isVisible = false, state = TileState.INACTIVE)
        }

        dbTiles + unusedExternalTiles
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Checks if a tile is an action/shortcut only tile (does not toggle on/off state)
     */
    fun isActionOnlyTile(tileId: String, config: TileCommandConfig): Boolean {
        if (config.forceStateChangeAnyway) return false
        if (config.isOverrideEnabled) {
            return when (config.overrideType) {
                TileActionOverrideType.MEDIA_PLAY_PAUSE,
                TileActionOverrideType.MEDIA_PAUSE,
                TileActionOverrideType.MEDIA_PLAY,
                TileActionOverrideType.MEDIA_NEXT,
                TileActionOverrideType.MEDIA_PREV,
                TileActionOverrideType.SCREENSHOT,
                TileActionOverrideType.MUTE_AUDIO,
                TileActionOverrideType.APP_LAUNCH,
                TileActionOverrideType.URL_DEEP_LINK,
                TileActionOverrideType.ACTION_CHAIN,
                TileActionOverrideType.DIRECT_INTENT -> !config.isStateToggle
                TileActionOverrideType.SYSTEM_COMMAND -> if (config.isStateToggle) false else config.commandOff.isBlank()
                TileActionOverrideType.DEFAULT -> false
            }
        }
        return tileId in listOf("screenshot", "sound_search", "settings", "customize")
    }

    /**
     * Handles click via Universal System Tile Interoperability Protocol & Action Override Engine
     */
    fun handleTileClick(tile: QsTileModel) {
        val config = TileCommandRepository.getConfig(tile.id)
        if (config.blockStateChanging) {
            return
        }

        val isThirdParty = tile.isExternal || tile.category == TileCategory.THIRD_PARTY || tile.id.contains("/")

        val isNativeSystemTile = tile.id in listOf(
            "wifi", "bluetooth", "torch", "autorotate", "dnd", "airplane", "location",
            "cellular", "hotspot", "battery_saver", "nfc", "dark_mode", "aod",
            "ambient_display", "caffeine", "heads_up", "screenrecord", "settings", "customize"
        )

        // 1. Only toggle local custom state for pure custom action overrides or non-system custom tiles
        if (!isNativeSystemTile && !isThirdParty && (config.forceStateChangeAnyway || tile.id in listOf("cpu_info", "fps_info", "reading_mode", "sound_profile"))) {
            val isCurrentlyActive = tile.state == TileState.ACTIVE
            val newActive = !isCurrentlyActive
            val currentCustom = customTileActiveStates.value.toMutableMap()
            currentCustom[tile.id] = newActive
            customTileActiveStates.value = currentCustom

            val currentForced = forcedStates.value.toMutableMap()
            currentForced[tile.id] = newActive
            forcedStates.value = currentForced
        }

        // 2. Route action execution through non-blocking Coroutines (Dispatchers.IO)
        scope.launch(Dispatchers.IO) {
            try {
                val spec = tile.complicationSpec
                if (config.isOverrideEnabled) {
                    executeOverriddenTileAction(tile, config)
                } else if (spec != null) {
                    withContext(Dispatchers.Main) {
                        ComplicationTileController.executeAction(context, spec.tapAction)
                    }
                } else if (tile.id == "sound_profile" || tile.id == "sound_mode" || tile.id == "volume") {
                    withContext(Dispatchers.Main) {
                        com.example.service.VolumePanelOverlay.show(context)
                    }
                } else if (isThirdParty) {
                    externalTileScanner.dispatchTileClick(tile, config, nativeTileManager.privilegeEngine)
                } else {
                    nativeTileManager.handleUniversalTileClick(tile)
                }
            } catch (e: Exception) {
                Log.w("TileRegistry", "Error executing tile click for ${tile.id}: ${e.message}")
            }

            // Query actual underlying system status to stay synchronized (non-blocking)
            queryUnderlyingSystemStatus(tile)
        }
    }

    suspend fun queryUnderlyingSystemStatus(tile: QsTileModel) {
        try {
            if (!tile.isExternal && tile.packageName == null && !tile.id.contains("/")) {
                kotlinx.coroutines.delay(300)
                nativeTileManager.refreshAllStates()
            }
        } catch (e: Exception) {
            Log.w("TileRegistry", "Error syncing system status for ${tile.id}: ${e.message}")
        }
    }

    suspend fun executeOverriddenTileAction(tile: QsTileModel, config: TileCommandConfig) {
        val isCurrentlyActive = tile.state == TileState.ACTIVE

        // Handle URL / Deep Link
        if (config.overrideType == TileActionOverrideType.URL_DEEP_LINK || config.urlOrDeepLink.isNotBlank()) {
            val url = config.urlOrDeepLink
            if (url.isNotBlank()) {
                withContext(Dispatchers.Main) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Log.w("TileRegistry", "Failed to launch URL/DeepLink: ${e.message}")
                    }
                }
            }
        }

        // Handle App Launching
        if (config.overrideType == TileActionOverrideType.APP_LAUNCH || config.targetAppPackage.isNotBlank()) {
            val pkg = config.targetAppPackage
            if (pkg.isNotBlank()) {
                withContext(Dispatchers.Main) {
                    try {
                        val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                        if (launchIntent != null) {
                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(launchIntent)
                        } else {
                            val intent = Intent(Intent.ACTION_MAIN).apply {
                                setPackage(pkg)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    } catch (e: Exception) {
                        Log.w("TileRegistry", "Failed to launch app: ${e.message}")
                    }
                }
            }
        }

        // Handle Action Chaining
        if (config.overrideType == TileActionOverrideType.ACTION_CHAIN || config.actionChainSteps.isNotEmpty()) {
            for (step in config.actionChainSteps) {
                if (step.delayMs > 0) {
                    kotlinx.coroutines.delay(step.delayMs)
                }
                when (step.type.uppercase()) {
                    "SHELL" -> {
                        if (step.target.isNotBlank()) {
                            nativeTileManager.privilegeEngine.executeCommandWithMode(step.target, config.executionMode, null)
                        }
                    }
                    "URL" -> {
                        if (step.target.isNotBlank()) {
                            withContext(Dispatchers.Main) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(step.target)).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        }
                    }
                    "APP" -> {
                        if (step.target.isNotBlank()) {
                            withContext(Dispatchers.Main) {
                                try {
                                    val launchIntent = context.packageManager.getLaunchIntentForPackage(step.target)
                                    launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    if (launchIntent != null) context.startActivity(launchIntent)
                                } catch (_: Exception) {}
                            }
                        }
                    }
                    "INTENT" -> {
                        if (step.target.isNotBlank()) {
                            dispatchDirectIntent(step.target, "", "", config.executionMode)
                        }
                    }
                }
            }
        }

        when (config.overrideType) {
            TileActionOverrideType.MEDIA_PLAY_PAUSE -> {
                dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, "input keyevent 85", config.executionMode)
            }
            TileActionOverrideType.MEDIA_PAUSE -> {
                dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PAUSE, "input keyevent 127", config.executionMode)
            }
            TileActionOverrideType.MEDIA_PLAY -> {
                dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY, "input keyevent 126", config.executionMode)
            }
            TileActionOverrideType.MEDIA_NEXT -> {
                dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT, "input keyevent 87", config.executionMode)
            }
            TileActionOverrideType.MEDIA_PREV -> {
                dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS, "input keyevent 88", config.executionMode)
            }
            TileActionOverrideType.SCREENSHOT -> {
                nativeTileManager.privilegeEngine.executeCommandWithMode("input keyevent 120", config.executionMode, null)
            }
            TileActionOverrideType.MUTE_AUDIO -> {
                dispatchMediaKeyEvent(KeyEvent.KEYCODE_VOLUME_MUTE, "input keyevent 164", config.executionMode)
            }
            TileActionOverrideType.DIRECT_INTENT -> {
                if (config.customIntentAction.isNotBlank() || config.customIntentPackage.isNotBlank() || config.customIntentComponent.isNotBlank()) {
                    dispatchDirectIntent(config.customIntentAction, config.customIntentPackage, config.customIntentComponent, config.executionMode)
                }
            }
            TileActionOverrideType.SYSTEM_COMMAND, TileActionOverrideType.DEFAULT, TileActionOverrideType.APP_LAUNCH, TileActionOverrideType.URL_DEEP_LINK, TileActionOverrideType.ACTION_CHAIN -> {
                val cmd = if (isCurrentlyActive && config.commandOff.isNotBlank()) config.commandOff else config.commandOn
                var ranCmd = false
                if (cmd.isNotBlank()) {
                    ranCmd = nativeTileManager.privilegeEngine.executeCommandWithMode(cmd, config.executionMode, null)
                }

                if (config.customIntentAction.isNotBlank() || config.customIntentPackage.isNotBlank() || config.customIntentComponent.isNotBlank()) {
                    dispatchDirectIntent(config.customIntentAction, config.customIntentPackage, config.customIntentComponent, config.executionMode)
                } else if (!ranCmd && tile.isExternal) {
                    externalTileScanner.dispatchTileClick(tile, config, nativeTileManager.privilegeEngine)
                }
            }
        }
    }

    private suspend fun dispatchMediaKeyEvent(keyCode: Int, shellCmd: String, mode: CommandExecutionMode) {
        if (mode == CommandExecutionMode.ROOT || mode == CommandExecutionMode.SHIZUKU) {
            // Broadcast the media/system intent directly at system level to bypass activity redirection
            nativeTileManager.privilegeEngine.executeCommandWithMode(shellCmd, mode, null)
        } else {
            // Standard mode: execute via AudioManager API
            try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                val eventDown = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
                val eventUp = KeyEvent(KeyEvent.ACTION_UP, keyCode)
                audioManager?.dispatchMediaKeyEvent(eventDown)
                audioManager?.dispatchMediaKeyEvent(eventUp)
            } catch (e: Exception) {
                Log.w("TileRegistry", "Media key event dispatch failed: ${e.message}")
            }
        }
    }

    private suspend fun dispatchDirectIntent(
        action: String,
        targetPackage: String,
        targetComponent: String = "",
        mode: CommandExecutionMode
    ) {
        withContext(Dispatchers.IO) {
            val compStr = if (targetComponent.isNotBlank()) targetComponent else if (action.contains("/")) action else ""

            if (mode == CommandExecutionMode.ROOT || mode == CommandExecutionMode.SHIZUKU) {
                val cmd = if (compStr.contains("/")) {
                    "am start -n $compStr"
                } else {
                    val pkgArg = if (targetPackage.isNotBlank()) "-p $targetPackage" else ""
                    val act = if (action.isNotBlank()) action else "android.intent.action.MAIN"
                    "am broadcast -a $act $pkgArg"
                }
                nativeTileManager.privilegeEngine.executeCommandWithMode(cmd, mode, null)
            } else {
                // Standard mode: dispatch via standard Context API
                try {
                    val intent = createExplicitOrActionIntent(action, targetPackage, targetComponent)
                    try {
                        context.sendBroadcast(intent)
                    } catch (e1: Exception) {
                        try {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e2: Exception) {
                            try {
                                context.startService(intent)
                            } catch (e3: Exception) {
                                Log.w("TileRegistry", "Failed direct intent triggers: ${e3.message}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w("TileRegistry", "Standard intent dispatch failed: ${e.message}")
                }
            }
        }
    }

    private fun createExplicitOrActionIntent(action: String, pkg: String, comp: String): Intent {
        val fullCompStr = if (comp.isNotBlank()) comp else if (action.contains("/")) action else ""
        return if (fullCompStr.contains("/")) {
            val parts = fullCompStr.split("/", limit = 2)
            val p = parts[0].trim()
            val cRaw = parts[1].trim()
            val c = if (cRaw.startsWith(".")) "$p$cRaw" else cRaw
            val act = if (action.isNotBlank() && !action.contains("/")) action else Intent.ACTION_MAIN
            Intent(act).apply {
                component = ComponentName(p, c)
                if (p.isNotBlank()) setPackage(p)
            }
        } else if (pkg.isNotBlank() && comp.isNotBlank()) {
            val c = if (comp.startsWith(".")) "$pkg$comp" else comp
            val act = if (action.isNotBlank()) action else Intent.ACTION_MAIN
            Intent(act).apply {
                component = ComponentName(pkg, c)
                setPackage(pkg)
            }
        } else {
            val act = if (action.isNotBlank()) action else Intent.ACTION_MAIN
            Intent(act).apply {
                if (pkg.isNotBlank()) setPackage(pkg)
            }
        }
    }

    fun handleTileLongClick(tile: QsTileModel) {
        val config = TileCommandRepository.getConfig(tile.id)
        if (config.longPressActionType != LongPressActionType.DEFAULT) {
            when (config.longPressActionType) {
                LongPressActionType.URL_DEEP_LINK -> {
                    if (config.longPressCommand.isNotBlank()) {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(config.longPressCommand)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                            return
                        } catch (e: Exception) {
                            Log.w("TileRegistry", "Long click URL error: ${e.message}")
                        }
                    }
                }
                LongPressActionType.APP_LAUNCH -> {
                    if (config.longPressCommand.isNotBlank()) {
                        try {
                            val launchIntent = context.packageManager.getLaunchIntentForPackage(config.longPressCommand)
                            launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            if (launchIntent != null) {
                                context.startActivity(launchIntent)
                                return
                            }
                        } catch (e: Exception) {
                            Log.w("TileRegistry", "Long click app launch error: ${e.message}")
                        }
                    }
                }
                LongPressActionType.SYSTEM_COMMAND -> {
                    if (config.longPressCommand.isNotBlank()) {
                        scope.launch(Dispatchers.IO) {
                            nativeTileManager.privilegeEngine.executeCommandWithMode(config.longPressCommand, config.executionMode)
                        }
                        return
                    }
                }
                LongPressActionType.DIRECT_INTENT -> {
                    if (config.longPressCommand.isNotBlank()) {
                        scope.launch(Dispatchers.IO) {
                            dispatchDirectIntent(config.longPressCommand, "", "", config.executionMode)
                        }
                        return
                    }
                }
                else -> {}
            }
        }

        if (tile.id == "screenrecord") {
            showScreenRecordSheet.value = true
            return
        }

        if (tile.isExternal) {
            externalTileScanner.dispatchTileLongClick(tile)
            return
        }

        tile.settingsAction?.let { action ->
            try {
                val intent = Intent(action).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback to main settings
                val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
            }
        } ?: run {
            // Default to main system settings
            try {
                val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun toggleTileVisibility(tileId: String, currentVisible: Boolean) {
        scope.launch(Dispatchers.IO) {
            val currentTile = allTiles.value.find { it.id == tileId }
            val isThirdParty = currentTile?.isExternal == true || currentTile?.category == TileCategory.THIRD_PARTY || tileId.contains("/")
            if (currentTile != null && isThirdParty) {
                val pkg = currentTile.packageName ?: if (tileId.contains("/")) tileId.substringBefore('/') else null
                val cls = currentTile.serviceClassName ?: if (tileId.contains("/")) tileId.substringAfter('/') else null
                val compId = if (pkg != null && cls != null) "$pkg/$cls" else tileId
                val entity = TileEntity(
                    tileId = compId,
                    label = currentTile.label,
                    subtitle = currentTile.subtitle,
                    categoryName = TileCategory.THIRD_PARTY.name,
                    orderIndex = currentTile.orderIndex,
                    isVisible = !currentVisible,
                    isQqsFavorite = currentTile.isQqsFavorite,
                    isExternal = true,
                    packageName = pkg,
                    serviceClassName = cls,
                    settingsAction = null
                )
                repository.insert(entity)
            } else {
                repository.setVisibility(tileId, !currentVisible)
            }
        }
    }

    fun setTileVisibility(tileId: String, isVisible: Boolean) {
        scope.launch(Dispatchers.IO) {
            val currentTile = allTiles.value.find { it.id == tileId }
            val isExt = currentTile?.isExternal == true || currentTile?.category == TileCategory.THIRD_PARTY || tileId.contains("/")
            if (isExt && currentTile != null) {
                val pkg = currentTile.packageName ?: if (tileId.contains("/")) tileId.substringBefore('/') else null
                val cls = currentTile.serviceClassName ?: if (tileId.contains("/")) tileId.substringAfter('/') else null
                val entity = TileEntity(
                    tileId = tileId,
                    label = currentTile.label,
                    subtitle = currentTile.subtitle,
                    categoryName = TileCategory.THIRD_PARTY.name,
                    orderIndex = currentTile.orderIndex,
                    isVisible = isVisible,
                    isQqsFavorite = currentTile.isQqsFavorite,
                    isExternal = true,
                    packageName = pkg,
                    serviceClassName = cls,
                    settingsAction = null
                )
                repository.insert(entity)
            } else {
                repository.setVisibility(tileId, isVisible)
            }
        }
    }

    fun toggleQqsFavorite(tileId: String, currentFavorite: Boolean) {
        scope.launch(Dispatchers.IO) {
            repository.setQqsFavorite(tileId, !currentFavorite)
        }
    }

    fun moveTile(fromIndex: Int, toIndex: Int, currentList: List<QsTileModel>) {
        if (fromIndex == toIndex || fromIndex !in currentList.indices || toIndex !in currentList.indices) return
        scope.launch(Dispatchers.IO) {
            val mutable = currentList.toMutableList()
            val item = mutable.removeAt(fromIndex)
            mutable.add(toIndex, item)
            mutable.forEachIndexed { index, model ->
                val isExt = model.isExternal || model.category == TileCategory.THIRD_PARTY || model.id.contains("/")
                if (isExt) {
                    val pkg = model.packageName ?: if (model.id.contains("/")) model.id.substringBefore('/') else null
                    val cls = model.serviceClassName ?: if (model.id.contains("/")) model.id.substringAfter('/') else null
                    val compId = if (pkg != null && cls != null) "$pkg/$cls" else model.id
                    val entity = TileEntity(
                        tileId = compId,
                        label = model.label,
                        subtitle = model.subtitle,
                        categoryName = TileCategory.THIRD_PARTY.name,
                        orderIndex = index,
                        isVisible = model.isVisible,
                        isQqsFavorite = model.isQqsFavorite,
                        isExternal = true,
                        packageName = pkg,
                        serviceClassName = cls,
                        settingsAction = model.settingsAction
                    )
                    repository.insert(entity)
                } else {
                    repository.updateOrder(model.id, index)
                }
            }
            PanelPreferences.saveActiveTileIds(context, mutable.filter { it.isVisible }.map { it.id })
        }
    }

    fun addExternalTile(tile: QsTileModel) {
        scope.launch(Dispatchers.IO) {
            val pkg = tile.packageName ?: if (tile.id.contains("/")) tile.id.substringBefore('/') else null
            val cls = tile.serviceClassName ?: if (tile.id.contains("/")) tile.id.substringAfter('/') else null
            val componentName = if (pkg != null && cls != null) "$pkg/$cls" else tile.id

            val entity = TileEntity(
                tileId = componentName,
                label = tile.label,
                subtitle = tile.subtitle,
                categoryName = TileCategory.THIRD_PARTY.name,
                orderIndex = 99,
                isVisible = true,
                isQqsFavorite = false,
                isExternal = true,
                packageName = pkg,
                serviceClassName = cls,
                settingsAction = null
            )
            repository.insert(entity)
            val currentSaved = PanelPreferences.getSavedActiveTileIds(context) ?: emptyList()
            if (componentName !in currentSaved) {
                PanelPreferences.saveActiveTileIds(context, currentSaved + componentName)
            }
        }
    }

    fun updateTilesOrderAndVisibility(activeTiles: List<QsTileModel>, inactiveTiles: List<QsTileModel>) {
        scope.launch(Dispatchers.IO) {
            var order = 0
            for (tile in activeTiles) {
                val isExt = tile.isExternal || tile.category == TileCategory.THIRD_PARTY || tile.category == TileCategory.EXTERNAL || tile.id.contains("/")
                val pkg = tile.packageName ?: if (tile.id.contains("/")) tile.id.substringBefore('/') else null
                val cls = tile.serviceClassName ?: if (tile.id.contains("/")) tile.id.substringAfter('/') else null
                val compId = if (isExt && pkg != null && cls != null) "$pkg/$cls" else tile.id

                val entity = TileEntity(
                    tileId = if (isExt) compId else tile.id,
                    label = tile.label,
                    subtitle = tile.subtitle,
                    categoryName = if (isExt) TileCategory.THIRD_PARTY.name else tile.category.name,
                    orderIndex = order,
                    isVisible = true,
                    isQqsFavorite = tile.isQqsFavorite,
                    isExternal = isExt,
                    packageName = pkg,
                    serviceClassName = cls,
                    settingsAction = tile.settingsAction
                )
                repository.insert(entity)
                order++
            }
            for (tile in inactiveTiles) {
                val isExt = tile.isExternal || tile.category == TileCategory.THIRD_PARTY || tile.category == TileCategory.EXTERNAL || tile.id.contains("/")
                val pkg = tile.packageName ?: if (tile.id.contains("/")) tile.id.substringBefore('/') else null
                val cls = tile.serviceClassName ?: if (tile.id.contains("/")) tile.id.substringAfter('/') else null
                val compId = if (isExt && pkg != null && cls != null) "$pkg/$cls" else tile.id

                val entity = TileEntity(
                    tileId = if (isExt) compId else tile.id,
                    label = tile.label,
                    subtitle = tile.subtitle,
                    categoryName = if (isExt) TileCategory.THIRD_PARTY.name else tile.category.name,
                    orderIndex = order,
                    isVisible = false,
                    isQqsFavorite = tile.isQqsFavorite,
                    isExternal = isExt,
                    packageName = pkg,
                    serviceClassName = cls,
                    settingsAction = tile.settingsAction
                )
                repository.insert(entity)
                order++
            }
            PanelPreferences.saveActiveTileIds(context, activeTiles.map { it.id })
        }
    }
}
