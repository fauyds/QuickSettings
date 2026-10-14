package com.fauyds.quicksettings.system

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.service.quicksettings.TileService
import android.util.Log
import com.fauyds.quicksettings.model.CommandExecutionMode
import com.fauyds.quicksettings.model.QsTileModel
import com.fauyds.quicksettings.model.TileCategory
import com.fauyds.quicksettings.model.TileState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

class ExternalTileScanner(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val _scannedTiles = MutableStateFlow<List<QsTileModel>>(emptyList())
    val scannedTiles: StateFlow<List<QsTileModel>> = _scannedTiles.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.Main + kotlinx.coroutines.SupervisorJob())

    init {
        scope.launch {
            scanThirdPartyTiles()
        }
        registerPackageReceiver()
    }

    private fun registerPackageReceiver() {
        val filter = android.content.IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        }
        context.registerReceiver(object : android.content.BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                scope.launch {
                    scanThirdPartyTiles()
                }
            }
        }, filter)
    }

    suspend fun scanThirdPartyTiles(): List<QsTileModel> {
        return withContext(Dispatchers.IO) {
            _isScanning.value = true
            val results = mutableListOf<QsTileModel>()

            try {
                val intent = Intent(TileService.ACTION_QS_TILE)
                val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.queryIntentServices(
                        intent,
                        PackageManager.ResolveInfoFlags.of(PackageManager.GET_META_DATA.toLong())
                    )
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.queryIntentServices(intent, PackageManager.GET_META_DATA)
                }

                var index = 100
                for (info in resolveInfos) {
                    val serviceInfo = info.serviceInfo ?: continue
                    val pkgName = serviceInfo.packageName
                    val className = serviceInfo.name

                    // Don't include our own package if we register one
                    if (pkgName == context.packageName) continue

                    val label = try {
                        serviceInfo.loadLabel(packageManager).toString()
                    } catch (e: Exception) {
                        serviceInfo.name.substringAfterLast('.')
                    }

                    val icon: Drawable? = try {
                        serviceInfo.loadIcon(packageManager)
                    } catch (e: Exception) {
                        null
                    }

                    val appName = try {
                        val appInfo = packageManager.getApplicationInfo(pkgName, 0)
                        packageManager.getApplicationLabel(appInfo).toString()
                    } catch (e: Exception) {
                        pkgName
                    }

                    val componentNameStr = "$pkgName/$className"
                    val tile = QsTileModel(
                        id = componentNameStr,
                        label = label.ifBlank { "Third-Party Tile" },
                        subtitle = appName,
                        state = TileState.INACTIVE,
                        category = TileCategory.THIRD_PARTY,
                        externalDrawable = icon,
                        isExternal = true,
                        packageName = pkgName,
                        serviceClassName = className,
                        orderIndex = index++,
                        isVisible = false,
                        settingsAction = null
                    )
                    results.add(tile)
                }
            } catch (e: Exception) {
                Log.w("ExternalTileScanner", "Error scanning external tiles: ${e.message}")
            } finally {
                _isScanning.value = false
            }

            _scannedTiles.value = results
            results
        }
    }

    fun dispatchTileClick(
        tile: QsTileModel,
        config: com.fauyds.quicksettings.model.TileCommandConfig? = null,
        engine: PrivilegeEngine? = null
    ) {
        val effectiveConfig = config ?: com.fauyds.quicksettings.data.TileCommandRepository.getConfig(tile.id)
        val privilegeEngine = engine ?: PrivilegeEngine(context)

        val pkg = tile.packageName ?: if (tile.id.contains("/")) tile.id.substringBefore('/') else null ?: return
        val rawCls = tile.serviceClassName ?: if (tile.id.contains("/")) tile.id.substringAfter('/') else null
        val cls = if (!rawCls.isNullOrBlank()) {
            if (rawCls.startsWith(".")) "$pkg$rawCls" else rawCls
        } else null

        val targetCls = cls ?: if (tile.id.contains("/")) tile.id.substringAfter('/') else ""
        val componentStr = if (targetCls.isNotBlank()) "$pkg/$targetCls" else pkg
        val componentName = if (targetCls.isNotBlank()) ComponentName(pkg, targetCls) else null

        scope.launch(Dispatchers.IO) {
            try {
                val mode = effectiveConfig.executionMode
                if (mode == CommandExecutionMode.SHIZUKU || mode == CommandExecutionMode.ROOT) {
                    Log.d("ExternalTileScanner", "Executing explicit direct shell dispatch for $componentStr (mode: $mode)")

                    // Command 1: am start-service -n $pkg/$cls
                    try {
                        privilegeEngine.executeCommandWithMode(
                            "am start-service -n $componentStr",
                            mode,
                            null
                        )
                    } catch (e: Exception) {}

                    // Command 2: am broadcast -a android.service.quicksettings.action.QS_TILE -n $pkg/$cls --receiver-foreground
                    try {
                        privilegeEngine.executeCommandWithMode(
                            "am broadcast -a android.service.quicksettings.action.QS_TILE -n $componentStr --receiver-foreground",
                            mode,
                            null
                        )
                    } catch (e: Exception) {}

                    // Statusbar click-tile fallback trigger
                    try {
                        privilegeEngine.executeCommandWithMode(
                            "cmd statusbar click-tile $componentStr",
                            mode,
                            null
                        )
                    } catch (e: Exception) {}

                    // Command 3: am start -n $pkg/$cls (as a fallback if it's an Activity-based tile)
                    try {
                        privilegeEngine.executeCommandWithMode(
                            "am start -n $componentStr",
                            mode,
                            null
                        )
                    } catch (e: Exception) {}

                    // Shell Tile State Re-Sync
                    try {
                        privilegeEngine.executeCommandWithMode(
                            "cmd statusbar refresh-tile $componentStr",
                            mode,
                            null
                        )
                    } catch (e: Exception) {}
                } else {
                    // 2. BINDING & FALLBACK (STANDARD EXPLICIT INTENT DISPATCHING)
                    val intent = Intent("android.service.quicksettings.action.QS_TILE").apply {
                        if (componentName != null) {
                            component = componentName
                        } else {
                            setPackage(pkg)
                        }
                        addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                    }

                    try {
                        context.sendBroadcast(intent)
                    } catch (e: Exception) {}

                    try {
                        context.startService(intent)
                    } catch (e: Exception) {}
                }

                // Tile State Re-Sync
                if (componentName != null) {
                    try {
                        TileService.requestListeningState(context, componentName)
                    } catch (e: Exception) {}
                }
            } catch (e: Exception) {
                Log.w("ExternalTileScanner", "Error dispatching tile click: ${e.message}")
            }
        }
    }

    fun dispatchTileLongClick(tile: QsTileModel) {
        val pkg = tile.packageName ?: if (tile.id.contains("/")) tile.id.substringBefore('/') else null ?: return
        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:$pkg")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.w("ExternalTileScanner", "Failed to open app info: ${e.message}")
        }
    }
}
