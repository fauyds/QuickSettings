package com.example.ui.tile

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.example.model.ComplicationAction
import com.example.model.ComplicationLayoutMode
import com.example.model.ComplicationSublinePlacement
import com.example.model.ComplicationTileModel
import com.example.service.QuickPanelAccessibilityService
import com.example.system.NativeTileManager
import com.example.ui.overlay.rememberQqsBatteryState

/**
 * Dispatches standard ComplicationTile interactions:
 * - Short tap -> executes sealed [ComplicationAction] (Toggle, LaunchApp, Shortcut)
 * - Long press -> opens system/tile Settings screen
 */
object ComplicationTileController {

    fun executeAction(
        context: Context,
        action: ComplicationAction,
        collapsePanelOnLaunch: Boolean = true
    ) {
        when (action) {
            is ComplicationAction.Toggle -> {
                if (action.onToggle != null) {
                    action.onToggle.invoke(!action.isActive)
                    return
                }
                val manager = NativeTileManager.instance
                when (action.tileId.lowercase()) {
                    "battery_saver", "battery" -> manager?.toggleBatterySaver()
                    "wifi" -> manager?.toggleWifi()
                    "bluetooth" -> manager?.toggleBluetooth()
                    "cellular", "mobile_data" -> manager?.toggleCellular()
                    "torch", "flashlight" -> manager?.toggleTorch()
                    "autorotate" -> manager?.toggleAutoRotate()
                    "dnd", "sound", "mute" -> manager?.toggleDnd()
                    "volume_mute" -> {
                        manager?.let { mgr ->
                            val cur = mgr.mediaVolume.value
                            val next = if (cur > 0.05f) 0f else 0.65f
                            mgr.setStreamVolume(AudioManager.STREAM_MUSIC, next)
                        }
                    }
                    "airplane" -> manager?.toggleAirplaneMode()
                    "location" -> manager?.toggleLocation()
                    "hotspot" -> manager?.toggleHotspot()
                    "nfc" -> manager?.toggleNfc()
                    "dark_mode" -> manager?.toggleDarkMode()
                    else -> {
                        Toast.makeText(context, "Toggled ${action.tileId}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            is ComplicationAction.LaunchApp -> {
                val intent = if (!action.activityClassName.isNullOrBlank()) {
                    Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                        component = ComponentName(action.packageName, action.activityClassName)
                    }
                } else {
                    context.packageManager.getLaunchIntentForPackage(action.packageName)
                }
                if (intent != null) {
                    try {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        if (collapsePanelOnLaunch) {
                            QuickPanelAccessibilityService.hideCustomPanel()
                        }
                    } catch (_: Throwable) {
                        Toast.makeText(context, "Unable to open ${action.packageName}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "App not installed: ${action.packageName}", Toast.LENGTH_SHORT).show()
                }
            }

            is ComplicationAction.Shortcut -> {
                if (action.onInvoke != null) {
                    action.onInvoke.invoke()
                    return
                }
                if (action.shortcutId == "cycle_brightness") {
                    val manager = NativeTileManager.instance
                    if (manager != null) {
                        val current = manager.brightness.value
                        val next = when {
                            current < 80 -> 150
                            current < 200 -> 255
                            else -> 50
                        }
                        manager.setBrightness(next)
                    }
                    return
                }
                try {
                    val intent = if (!action.dataUri.isNullOrBlank()) {
                        Intent(action.intentAction ?: Intent.ACTION_VIEW, Uri.parse(action.dataUri))
                    } else if (!action.intentAction.isNullOrBlank()) {
                        Intent(action.intentAction)
                    } else {
                        null
                    }
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        if (collapsePanelOnLaunch) {
                            QuickPanelAccessibilityService.hideCustomPanel()
                        }
                    }
                } catch (_: Throwable) {
                    Toast.makeText(context, "Shortcut unavailable", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun openSettings(
        context: Context,
        settingsAction: String,
        collapsePanel: Boolean = true
    ) {
        val primaryAction = settingsAction.ifBlank { Settings.ACTION_SETTINGS }
        val candidates = listOf(primaryAction, Settings.ACTION_SETTINGS)
        for (action in candidates) {
            try {
                val intent = Intent(action).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                if (collapsePanel) {
                    QuickPanelAccessibilityService.hideCustomPanel()
                }
                return
            } catch (_: Throwable) {
            }
        }
    }

    fun readInternalStorageFraction(): Pair<Float, String> {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val total = stat.totalBytes.toDouble()
            val avail = stat.availableBytes.toDouble()
            if (total <= 0.0) return 0f to "0%"
            val usedFraction = ((total - avail) / total).toFloat().coerceIn(0f, 1f)
            val pct = (usedFraction * 100f).toInt()
            usedFraction to "$pct%"
        } catch (_: Throwable) {
            0.5f to "50%"
        }
    }
}

/**
 * Provides live device-backed [ComplicationTileModel] items (Battery, Brightness, Storage, Sound)
 * suitable for either a compact header row or a custom grid layout.
 */
@Composable
fun rememberLiveComplicationTiles(
    sublinePlacement: ComplicationSublinePlacement = ComplicationSublinePlacement.INSIDE_RING,
    layoutMode: ComplicationLayoutMode = ComplicationLayoutMode.HEADER_ROW
): List<ComplicationTileModel> {
    val batteryInfo = rememberQqsBatteryState()
    val nativeManager = NativeTileManager.instance
    val brightnessState = nativeManager?.brightness?.collectAsState()
    val mediaVolState = nativeManager?.mediaVolume?.collectAsState()
    val batterySaverState = nativeManager?.isBatterySaverEnabled?.collectAsState()
    val dndState = nativeManager?.isDndEnabled?.collectAsState()

    val brightnessVal = brightnessState?.value ?: 128
    val mediaVol = mediaVolState?.value ?: 0.5f
    val isBatterySaverOn = batterySaverState?.value ?: false
    val isDndOn = dndState?.value ?: false

    val storageStats = remember { ComplicationTileController.readInternalStorageFraction() }

    val batteryFraction = (batteryInfo.percentage / 100f).coerceIn(0f, 1f)
    val batteryAccent = when {
        batteryInfo.isCharging -> Color(0xFF4CAF50)
        batteryInfo.percentage <= 20 -> Color(0xFFFF5252)
        isBatterySaverOn -> Color(0xFFFF9800)
        else -> Color(0xFF26A69A)
    }
    val batteryIcon = when {
        batteryInfo.isCharging -> Icons.Default.BatteryChargingFull
        isBatterySaverOn -> Icons.Default.BatterySaver
        else -> Icons.Default.BatteryFull
    }

    val brightnessFraction = (brightnessVal / 255f).coerceIn(0f, 1f)
    val brightnessPct = (brightnessFraction * 100f).toInt()
    val brightnessIcon = when {
        brightnessPct >= 70 -> Icons.Default.BrightnessHigh
        brightnessPct >= 35 -> Icons.Default.BrightnessMedium
        else -> Icons.Default.BrightnessLow
    }

    val volumeFraction = mediaVol.coerceIn(0f, 1f)
    val volumePct = (volumeFraction * 100f).toInt()

    return remember(
        batteryInfo.percentage,
        batteryInfo.isCharging,
        isBatterySaverOn,
        brightnessVal,
        mediaVol,
        isDndOn,
        storageStats,
        sublinePlacement,
        layoutMode
    ) {
        listOf(
            ComplicationTileModel(
                id = "complication_battery",
                title = "Battery",
                subline = "${batteryInfo.percentage}%",
                progress = batteryFraction,
                icon = batteryIcon,
                accentColor = batteryAccent,
                isActive = batteryInfo.isCharging || isBatterySaverOn,
                sublinePlacement = sublinePlacement,
                layoutMode = layoutMode,
                tapAction = ComplicationAction.Toggle(
                    tileId = "battery_saver",
                    isActive = isBatterySaverOn
                ),
                settingsAction = Settings.ACTION_BATTERY_SAVER_SETTINGS
            ),
            ComplicationTileModel(
                id = "complication_brightness",
                title = "Brightness",
                subline = "$brightnessPct%",
                progress = brightnessFraction,
                icon = brightnessIcon,
                accentColor = Color(0xFFFFB300),
                isActive = brightnessPct >= 50,
                sublinePlacement = sublinePlacement,
                layoutMode = layoutMode,
                tapAction = ComplicationAction.Shortcut(
                    shortcutId = "cycle_brightness",
                    onInvoke = {
                        NativeTileManager.instance?.let { mgr ->
                            val cur = mgr.brightness.value
                            val next = when {
                                cur < 80 -> 150
                                cur < 200 -> 255
                                else -> 50
                            }
                            mgr.setBrightness(next)
                        }
                    }
                ),
                settingsAction = Settings.ACTION_DISPLAY_SETTINGS
            ),
            ComplicationTileModel(
                id = "complication_storage",
                title = "Storage",
                subline = storageStats.second,
                progress = storageStats.first,
                icon = Icons.Default.Storage,
                accentColor = Color(0xFF00BFA5),
                isActive = false,
                sublinePlacement = sublinePlacement,
                layoutMode = layoutMode,
                tapAction = ComplicationAction.Shortcut(
                    shortcutId = "open_storage",
                    intentAction = Settings.ACTION_INTERNAL_STORAGE_SETTINGS
                ),
                settingsAction = Settings.ACTION_INTERNAL_STORAGE_SETTINGS
            ),
            ComplicationTileModel(
                id = "complication_volume",
                title = "Volume",
                subline = if (isDndOn) "DND" else "$volumePct%",
                progress = volumeFraction,
                icon = if (isDndOn || volumePct == 0) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                accentColor = Color(0xFF7C4DFF),
                isActive = !isDndOn && volumePct > 0,
                sublinePlacement = sublinePlacement,
                layoutMode = layoutMode,
                tapAction = ComplicationAction.Toggle(
                    tileId = "dnd",
                    isActive = isDndOn,
                    onToggle = {
                        NativeTileManager.instance?.let { mgr ->
                            val cur = mgr.mediaVolume.value
                            val next = if (cur > 0.05f) 0f else 0.65f
                            mgr.setStreamVolume(AudioManager.STREAM_MUSIC, next)
                        }
                    }
                ),
                settingsAction = Settings.ACTION_SOUND_SETTINGS
            )
        )
    }
}
