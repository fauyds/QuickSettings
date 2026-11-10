package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.service.QuickSettingsOverlayService
import com.example.system.NativeTileManager
import com.example.ui.MainScreen
import com.example.ui.MainViewModel
import com.example.ui.overlay.QsExpansionStage
import com.example.ui.theme.AppSettingsTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private fun bypassHiddenApiRestrictions() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val classArrayType = Class.forName("[Ljava.lang.Class;")
                val getDeclaredMethod = Class::class.java.getDeclaredMethod(
                    "getDeclaredMethod",
                    String::class.java,
                    classArrayType
                )
                val vmRuntimeClass = Class.forName("dalvik.system.VMRuntime")
                val getRuntimeMethod = getDeclaredMethod.invoke(
                    vmRuntimeClass,
                    "getRuntime",
                    null
                ) as java.lang.reflect.Method
                val vmRuntime = getRuntimeMethod.invoke(null)
                val setHiddenApiExemptionsMethod = getDeclaredMethod.invoke(
                    vmRuntimeClass,
                    "setHiddenApiExemptions",
                    classArrayType
                ) as java.lang.reflect.Method
                setHiddenApiExemptionsMethod.invoke(vmRuntime, arrayOf("L"))
            }
        } catch (e: Throwable) {
            // Safe fallback
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bypassHiddenApiRestrictions()
        enableEdgeToEdge()

        com.example.data.PanelPreferences.init(this)
        com.example.data.TileCommandRepository.init(this)
        com.example.service.MediaSessionManagerHelper.init(this)
        com.example.system.SystemThemeObserver.init(this)

        handleTriggerIntent(intent)

        setContent {
            val showInitialSetup by viewModel.showInitialSetup.collectAsState()
            var requestedConnectivityPerms by rememberSaveable { mutableStateOf(false) }
            val connectivityPermissionsLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) {
                NativeTileManager.notifyAllInstancesPrivilegeOrPermissionsUpdated()
            }

            LaunchedEffect(showInitialSetup) {
                if (!showInitialSetup && !requestedConnectivityPerms) {
                    requestedConnectivityPerms = true
                    val missing = buildList {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            add(Manifest.permission.ACCESS_COARSE_LOCATION)
                        }
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            add(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            add(Manifest.permission.BLUETOOTH_CONNECT)
                        }
                    }
                    if (missing.isNotEmpty()) {
                        connectivityPermissionsLauncher.launch(missing.toTypedArray())
                    }
                }
            }

            AppSettingsTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshServiceState()
        NativeTileManager.notifyAllInstancesPrivilegeOrPermissionsUpdated()

        // Auto-start overlay service if permission is granted
        if (Settings.canDrawOverlays(this) && !QuickSettingsOverlayService.isRunning) {
            val startIntent = Intent(this, QuickSettingsOverlayService::class.java).apply {
                action = QuickSettingsOverlayService.ACTION_START
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(startIntent)
            } else {
                startService(startIntent)
            }
            viewModel.refreshServiceState()
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        com.example.system.SystemThemeObserver.onConfigurationChanged(this, newConfig)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleTriggerIntent(intent)
    }

    private fun handleTriggerIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("EXPAND_OVERLAY_TRIGGER", false) == true) {
            viewModel.setExpansionStage(QsExpansionStage.FULL_QS)
        }
        val targetTab = intent?.getIntExtra("TARGET_TAB", -1) ?: -1
        if (targetTab >= 0) {
            viewModel.setSelectedTab(targetTab)
        } else if (intent?.getStringExtra("screen") == "tile_customizer") {
            viewModel.setSelectedTab(1)
        }
    }
}
