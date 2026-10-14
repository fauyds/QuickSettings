package com.fauyds.quicksettings.system

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Icon

object ExternalTileIconLoader {
    // In-memory LRU Cache for decoded ImageBitmap objects (max 50 icons to prevent memory overhead)
    private val iconCache = LruCache<String, ImageBitmap>(50)

    fun getCachedIcon(packageName: String, serviceClassName: String): ImageBitmap? {
        val cacheKey = "$packageName/$serviceClassName"
        return iconCache.get(cacheKey)
    }

    suspend fun loadIconAsync(context: Context, packageName: String, serviceClassName: String): ImageBitmap {
        val cacheKey = "$packageName/$serviceClassName"
        val cached = iconCache.get(cacheKey)
        if (cached != null) return cached

        return withContext(Dispatchers.IO) {
            val pm = context.packageManager
            try {
                val component = ComponentName(packageName, serviceClassName)
                val serviceInfo = pm.getServiceInfo(component, PackageManager.GET_META_DATA)
                val drawable = serviceInfo.loadIcon(pm)
                val bitmap = drawable.toBitmap(128, 128, Bitmap.Config.ARGB_8888)
                val imageBitmap = bitmap.asImageBitmap()
                iconCache.put(cacheKey, imageBitmap)
                imageBitmap
            } catch (e: Exception) {
                // Return fallback icon from the app icon or system default fallback
                try {
                    val appInfo = pm.getApplicationInfo(packageName, 0)
                    val appIcon = pm.getApplicationIcon(appInfo)
                    val bitmap = appIcon.toBitmap(128, 128, Bitmap.Config.ARGB_8888)
                    val imageBitmap = bitmap.asImageBitmap()
                    iconCache.put(cacheKey, imageBitmap)
                    imageBitmap
                } catch (ex: Exception) {
                    // Final system default fallback drawable (e.g. android.R.drawable.sym_def_app_icon)
                    val fallbackDrawable = context.resources.getDrawable(android.R.drawable.sym_def_app_icon, null)
                    val bitmap = fallbackDrawable.toBitmap(128, 128, Bitmap.Config.ARGB_8888)
                    val imageBitmap = bitmap.asImageBitmap()
                    iconCache.put(cacheKey, imageBitmap)
                    imageBitmap
                }
            }
        }
    }
}

@Composable
fun ExternalTileIcon(
    packageName: String?,
    serviceClassName: String?,
    tintColor: Color,
    contentDescription: String,
    modifier: Modifier = Modifier,
    fallbackDrawable: Drawable? = null
) {
    val context = LocalContext.current
    val pkg = packageName ?: ""
    val cls = serviceClassName ?: ""

    var iconBitmap by remember(pkg, cls) {
        mutableStateOf<ImageBitmap?>(
            if (pkg.isNotBlank() && cls.isNotBlank()) ExternalTileIconLoader.getCachedIcon(pkg, cls) else null
        )
    }

    LaunchedEffect(pkg, cls) {
        if (iconBitmap == null && pkg.isNotBlank() && cls.isNotBlank()) {
            iconBitmap = ExternalTileIconLoader.loadIconAsync(context, pkg, cls)
        }
    }

    val fallbackBitmap = remember(fallbackDrawable) {
        fallbackDrawable?.let {
            try {
                it.toBitmap(128, 128, Bitmap.Config.ARGB_8888).asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }

    val finalBitmap = iconBitmap ?: fallbackBitmap

    if (finalBitmap != null) {
        Image(
            bitmap = finalBitmap,
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(tintColor),
            modifier = modifier
        )
    } else {
        Icon(
            imageVector = Icons.Default.Apps,
            contentDescription = contentDescription,
            tint = tintColor,
            modifier = modifier
        )
    }
}
