package com.example.ui.tile

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import com.example.R
import com.example.model.QsTileModel
import com.example.model.TileCategory
import com.example.model.TileState

object QsTileIconHelper {

    fun getTileImageVector(tile: QsTileModel): ImageVector {
        if (tile.id.startsWith("custom_")) {
            val config = com.example.data.TileCommandRepository.getConfig(tile.id)
            if (config.iconName.isNotBlank()) {
                return GoogleIconLibrary.findIconByName(config.iconName)
            }
        }
        tile.iconVector?.let { return it }

        val canonicalId = if (tile.id == "sound_profile") "sound_mode" else tile.id

        return when (canonicalId) {
            "wifi" -> Icons.Default.Wifi
            "bluetooth" -> Icons.Default.Bluetooth
            "torch" -> if (tile.state == TileState.ACTIVE) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff
            "autorotate" -> Icons.Default.ScreenRotation
            "dnd" -> Icons.Default.DoNotDisturb
            "airplane" -> Icons.Default.Flight
            "location" -> Icons.Default.LocationOn
            "cellular" -> Icons.Default.CellTower
            "hotspot" -> Icons.Default.WifiTethering
            "battery_saver" -> Icons.Default.BatterySaver
            "nfc" -> Icons.Default.Nfc
            "dark_mode" -> Icons.Default.DarkMode
            "caffeine" -> Icons.Default.LocalCafe
            "heads_up", "notifications" -> Icons.Default.NotificationsActive
            "aod" -> Icons.Default.Wallpaper
            "ambient_display" -> Icons.Default.Visibility
            "sound_search" -> Icons.Default.Headphones
            "cpu_info" -> Icons.Default.Memory
            "fps_info" -> Icons.Default.Speed
            "reading_mode" -> Icons.Default.Visibility
            "sound_mode" -> Icons.Default.VolumeUp
            "screenshot" -> Icons.Default.CameraAlt
            "screenrecord" -> Icons.Default.Videocam
            "settings" -> Icons.Default.Settings
            "customize" -> Icons.Default.Layers
            "comp_battery" -> Icons.Default.BatteryFull
            "comp_storage" -> Icons.Default.Storage
            "comp_brightness" -> Icons.Default.BrightnessMedium
            "comp_volume" -> Icons.Default.VolumeUp
            else -> {
                val config = com.example.data.TileCommandRepository.getConfig(tile.id)
                if (config.iconName.isNotBlank()) GoogleIconLibrary.findIconByName(config.iconName) else Icons.Default.Settings
            }
        }
    }

    fun getTileIconResId(tile: QsTileModel): Int? {
        val isThirdParty = tile.isExternal || 
                tile.category == TileCategory.THIRD_PARTY || 
                tile.category == TileCategory.EXTERNAL || 
                tile.id.contains("/")

        if (isThirdParty) return null

        val canonicalId = if (tile.id == "sound_profile") "sound_mode" else tile.id

        return when (canonicalId) {
            "wifi" -> R.drawable.ic_qs_wifi
            "bluetooth" -> R.drawable.ic_qs_bluetooth
            "torch" -> if (tile.state == TileState.ACTIVE) R.drawable.ic_qs_flashlight_on else R.drawable.ic_qs_flashlight_off
            "autorotate" -> R.drawable.ic_qs_rotate
            "dnd" -> R.drawable.ic_qs_dnd
            "airplane" -> R.drawable.ic_qs_flight
            "location" -> R.drawable.ic_qs_location
            "cellular" -> R.drawable.ic_qs_cellular
            "hotspot" -> R.drawable.ic_qs_hotspot
            "battery_saver" -> R.drawable.ic_qs_battery
            "nfc" -> R.drawable.ic_qs_nfc
            "dark_mode" -> R.drawable.ic_qs_dark_mode
            "caffeine" -> R.drawable.ic_qs_caffeine
            "heads_up", "notifications" -> R.drawable.ic_qs_notifications
            "aod" -> R.drawable.ic_qs_wallpaper
            "ambient_display" -> R.drawable.ic_qs_visibility
            "sound_search" -> R.drawable.ic_qs_headphones
            "cpu_info" -> R.drawable.ic_qs_memory
            "fps_info" -> R.drawable.ic_qs_speed
            "reading_mode" -> R.drawable.ic_qs_visibility
            "sound_mode" -> R.drawable.ic_qs_volume
            "screenshot" -> R.drawable.ic_qs_screenshot
            "screenrecord" -> R.drawable.ic_qs_videocam
            "settings" -> R.drawable.ic_qs_settings
            "customize" -> R.drawable.ic_qs_customize
            "comp_battery" -> R.drawable.ic_qs_battery
            "comp_storage" -> R.drawable.ic_qs_memory
            "comp_brightness" -> R.drawable.ic_qs_visibility
            "comp_volume" -> R.drawable.ic_qs_volume
            else -> R.drawable.ic_qs_wifi
        }
    }

    fun getTileDrawable(context: Context, tile: QsTileModel): Drawable? {
        val resId = getTileIconResId(tile)
        if (resId != null) {
            return ContextCompat.getDrawable(context, resId)
        }

        val isThirdParty = tile.isExternal || 
                tile.category == TileCategory.THIRD_PARTY || 
                tile.category == TileCategory.EXTERNAL || 
                tile.id.contains("/")

        if (isThirdParty) {
            if (tile.externalDrawable != null) {
                return tile.externalDrawable
            }
            val pkg = tile.packageName ?: if (tile.id.contains("/")) tile.id.substringBefore('/') else null
            val cls = tile.serviceClassName ?: if (tile.id.contains("/")) tile.id.substringAfter('/') else null
            if (!pkg.isNullOrBlank()) {
                val pm = context.packageManager
                if (!cls.isNullOrBlank()) {
                    try {
                        val component = ComponentName(pkg, cls)
                        val serviceInfo = pm.getServiceInfo(component, PackageManager.GET_META_DATA)
                        return serviceInfo.loadIcon(pm)
                    } catch (_: Throwable) {}
                }
                try {
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    return pm.getApplicationIcon(appInfo)
                } catch (_: Throwable) {}
            }
            return ContextCompat.getDrawable(context, R.drawable.ic_notif_bell)
        }

        return ContextCompat.getDrawable(context, R.drawable.ic_qs_wifi)
    }
}
