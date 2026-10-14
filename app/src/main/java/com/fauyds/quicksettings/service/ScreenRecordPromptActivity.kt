package com.fauyds.quicksettings.service

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.fauyds.quicksettings.data.PanelPreferences
import com.fauyds.quicksettings.data.ScreenRecordAudioSource
import com.fauyds.quicksettings.model.PrivilegeMode
import com.fauyds.quicksettings.system.PrivilegeEngine
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
    }

    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d(TAG, "RECORD_AUDIO permission result: $isGranted")
        launchMediaProjectionPrompt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PanelPreferences.init(this)

        // If recording is already in progress, stop it and finish
        if (ScreenRecordService.isRecording.value) {
            ScreenRecordService.stopRecording(this)
            finish()
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

        // Check if audio recording is requested and request permission if missing
        val audioSource = PanelPreferences.screenRecordAudioSource.value
        val needsAudio = audioSource != ScreenRecordAudioSource.MUTE
        val hasAudioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

        if (needsAudio && !hasAudioPermission) {
            try {
                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch audio permission request: ${e.message}")
                launchMediaProjectionPrompt()
            }
        } else {
            launchMediaProjectionPrompt()
        }
    }

    private fun launchMediaProjectionPrompt() {
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        if (projectionManager != null) {
            try {
                val captureIntent = projectionManager.createScreenCaptureIntent()
                projectionLauncher.launch(captureIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Error launching screen capture intent: ${e.message}", e)
                finish()
            }
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
