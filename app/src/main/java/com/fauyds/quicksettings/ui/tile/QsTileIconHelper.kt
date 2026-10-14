package com.fauyds.quicksettings.ui.tile

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.fauyds.quicksettings.R
import com.fauyds.quicksettings.model.QsTileModel
import com.fauyds.quicksettings.model.TileCategory
import com.fauyds.quicksettings.model.TileState

object QsTileIconHelper {

    fun getTileDrawable(context: Context, tile: QsTileModel): Drawable? {
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

        val resId = when (tile.id) {
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
            "heads_up" -> R.drawable.ic_qs_notifications
            "aod" -> R.drawable.ic_qs_wallpaper
            "ambient_display" -> R.drawable.ic_qs_visibility
            "sound_search" -> R.drawable.ic_qs_headphones
            "cpu_info" -> R.drawable.ic_qs_memory
            "fps_info" -> R.drawable.ic_qs_speed
            "reading_mode" -> R.drawable.ic_qs_visibility
            "sound_profile" -> R.drawable.ic_qs_volume
            "screenshot" -> R.drawable.ic_qs_screenshot
            "screenrecord" -> R.drawable.ic_qs_videocam
            else -> R.drawable.ic_qs_wifi
        }

        return ContextCompat.getDrawable(context, resId)
    }
}
