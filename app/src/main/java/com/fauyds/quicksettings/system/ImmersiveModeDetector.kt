package com.fauyds.quicksettings.system

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ImmersiveModeDetector (Shizuku & Shell Variant)
 *
 * Targeted lightweight detection based on dumpsys window displays / visible-apps mContentInsets parsing.
 */
class ImmersiveModeDetector(
    private val privilegeEngine: PrivilegeEngine,
    private val scope: CoroutineScope
) {
    companion object {
        const val TAG = "ImmersiveCheck"
        private const val POLL_INTERVAL_MS = 200L
        private const val CMD = "dumpsys window displays | grep mContentInsets || dumpsys window visible-apps | grep mContentInsets"

        @Volatile
        var latestShizukuImmersiveState: Boolean = false
            private set
    }

    private val _isImmersiveModeActive = MutableStateFlow(false)
    val isImmersiveModeActive: StateFlow<Boolean> = _isImmersiveModeActive.asStateFlow()

    private var monitoringJob: Job? = null
    private var stateChangeCallback: ((Boolean) -> Unit)? = null

    fun startMonitoring(onStateChanged: ((Boolean) -> Unit)? = null) {
        stateChangeCallback = onStateChanged
        if (monitoringJob?.isActive == true) return

        monitoringJob = scope.launch(Dispatchers.IO) {
            Log.d(TAG, "Started targeted Shizuku interval loop (${POLL_INTERVAL_MS}ms) for Immersive Mode detection.")
            var lastState: Boolean? = null

            while (isActive) {
                val isHidden = checkStatusBarHiddenViaShizuku()
                if (lastState == null || _isImmersiveModeActive.value != isHidden) {
                    lastState = isHidden
                    _isImmersiveModeActive.value = isHidden
                    withContext(Dispatchers.Main) {
                        stateChangeCallback?.invoke(isHidden)
                    }
                }

                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        _isImmersiveModeActive.value = false
        latestShizukuImmersiveState = false
        stateChangeCallback = null
    }

    /**
     * Immediate trigger evaluation when accessibility or window transition events fire.
     */
    fun checkNow() {
        scope.launch(Dispatchers.IO) {
            val isHidden = checkStatusBarHiddenViaShizuku()
            if (_isImmersiveModeActive.value != isHidden) {
                _isImmersiveModeActive.value = isHidden
                withContext(Dispatchers.Main) {
                    stateChangeCallback?.invoke(isHidden)
                }
            }
        }
    }

    private fun checkStatusBarHiddenViaShizuku(): Boolean {
        return try {
            val isShizukuReady = privilegeEngine.isShizukuGranted()
            val isRootReady = privilegeEngine.isRootAvailable.value

            var rawOutput = ""

            if (isShizukuReady) {
                rawOutput = privilegeEngine.executeShizukuShell(CMD).second
            } else if (isRootReady) {
                val sb = StringBuilder()
                try {
                    val process = Runtime.getRuntime().exec(arrayOf("su", "-c", CMD))
                    val reader = process.inputStream.bufferedReader()
                    reader.forEachLine { line -> sb.appendLine(line) }
                    process.waitFor()
                } catch (e: Exception) {
                    Log.w(TAG, "Root fallback error: ${e.message}")
                }
                rawOutput = sb.toString()
            } else {
                val sb = StringBuilder()
                try {
                    val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", CMD))
                    val reader = process.inputStream.bufferedReader()
                    reader.forEachLine { line -> sb.appendLine(line) }
                    process.waitFor()
                } catch (e: Exception) {
                    // ignore
                }
                rawOutput = sb.toString()
            }

            if (rawOutput.isBlank()) {
                val defaultVal = false
                Log.d(TAG, "Parsed top inset: null, isImmersive: $defaultVal")
                latestShizukuImmersiveState = defaultVal
                return defaultVal
            }

            val topInset = parseTopContentInset(rawOutput)
            val isImmersive = if (topInset != null) {
                topInset == 0
            } else {
                false
            }

            Log.d(TAG, "Parsed top inset: $topInset, isImmersive: $isImmersive")
            latestShizukuImmersiveState = isImmersive
            isImmersive
        } catch (e: Exception) {
            Log.e(TAG, "Error checking status bar insets via Shizuku: ${e.message}", e)
            false
        }
    }

    /**
     * Parses the top value of `mContentInsets` from dumpsys window output.
     * Examples:
     * - `mContentInsets=[0,54][0,0]` -> left=0, top=54
     * - `mContentInsets=[0,0][0,0]` -> left=0, top=0
     * - `mContentInsets=Rect(0, 54 - 0, 0)` -> left=0, top=54
     * - `mContentInsets=[0, 136][0, 0]` -> left=0, top=136
     */
    private fun parseTopContentInset(output: String): Int? {
        val bracketRegex = Regex("""(?:mContentInsets|contentInsets).*?\[\s*-?\d+\s*,\s*(-?\d+)\s*\]""", RegexOption.IGNORE_CASE)
        val rectRegex = Regex("""(?:mContentInsets|contentInsets).*?Rect\(\s*-?\d+\s*,\s*(-?\d+)""", RegexOption.IGNORE_CASE)

        for (line in output.lines()) {
            val bracketMatch = bracketRegex.find(line)
            if (bracketMatch != null) {
                val topVal = bracketMatch.groupValues[1].toIntOrNull()
                if (topVal != null) return topVal
            }

            val rectMatch = rectRegex.find(line)
            if (rectMatch != null) {
                val topVal = rectMatch.groupValues[1].toIntOrNull()
                if (topVal != null) return topVal
            }
        }
        return null
    }
}
