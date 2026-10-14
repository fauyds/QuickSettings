package com.example

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.service.QuickSettingsOverlayService
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
            AppSettingsTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshServiceState()

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
        }
    }
}
