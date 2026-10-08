package com.example.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.model.PrivilegeMode
import com.example.system.PrivilegeEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScreenRecordPromptActivity : ComponentActivity() {

    companion object {
        private const val TAG = "ScreenRecordPrompt"

        fun start(context: Context) {
            val intent = Intent(context, ScreenRecordPromptActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            }
            context.startActivity(intent)
        }
    }

    private val activityScope = CoroutineScope(Dispatchers.Main)

    private val projectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            Log.d(TAG, "MediaProjection permission granted")
            ScreenRecordService.startRecording(this, result.resultCode, result.data!!)
        } else {
            Log.w(TAG, "MediaProjection permission denied or canceled: ${result.resultCode}")
        }
        finish()
        overridePendingTransition(0, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If recording is already in progress, stop it and finish
        if (ScreenRecordService.isRecording.value) {
            ScreenRecordService.stopRecording(this)
            finish()
            overridePendingTransition(0, 0)
            return
        }

        // Silent MediaProjection auto-grant under Root / Shizuku mode
        val privilegeEngine = PrivilegeEngine(this)
        val mode = privilegeEngine.currentMode.value
        if (mode == PrivilegeMode.ROOT || mode == PrivilegeMode.SHIZUKU) {
            activityScope.launch(Dispatchers.IO) {
                try {
                    val pkg = packageName
                    privilegeEngine.executeCommand("cmd appops set $pkg PROJECT_MEDIA allow")
                    privilegeEngine.executeCommand("appops set $pkg PROJECT_MEDIA allow")
                } catch (e: Exception) {
                    Log.w(TAG, "Silent appops grant error: ${e.message}")
                }
            }
        }

        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        if (projectionManager != null) {
            val captureIntent = projectionManager.createScreenCaptureIntent()
            projectionLauncher.launch(captureIntent)
        } else {
            Log.e(TAG, "MediaProjectionManager is null")
            finish()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}
