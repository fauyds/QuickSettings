package com.example.system

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext

/**
 * ImmersiveModeDetector (Shizuku Variant)
 *
 * Targeted lightweight detection based on dumpsys window displays / visible-apps mContentInsets parsing.
 * Strictly uses Shizuku when authorized and never invokes `su` in the background, preventing
 * repeated root permission requests and device lag.
 */
class ImmersiveModeDetector(
    private val privilegeEngine: PrivilegeEngine,
    private val scope: CoroutineScope,
    private val shouldPauseMonitoring: () -> Boolean = { false }
) {
    companion object {
        const val TAG = "ImmersiveCheck"
        private const val POLL_INTERVAL_MS = 1000L
        private const val CHECK_DEBOUNCE_MS = 400L
        private const val CMD = "dumpsys window displays | grep mContentInsets || dumpsys window visible-apps | grep mContentInsets"

        @Volatile
        var latestShizukuImmersiveState: Boolean = false
            private set
    }

    private val _isImmersiveModeActive = MutableStateFlow(false)
    val isImmersiveModeActive: StateFlow<Boolean> = _isImmersiveModeActive.asStateFlow()

    private var monitoringJob: Job? = null
    private var stateChangeCallback: ((Boolean) -> Unit)? = null
    private val checkMutex = Mutex()
    @Volatile
    private var lastCheckTimeMs: Long = 0L

    fun startMonitoring(onStateChanged: ((Boolean) -> Unit)? = null) {
        stateChangeCallback = onStateChanged
        if (monitoringJob?.isActive == true) return

        monitoringJob = scope.launch(Dispatchers.IO) {
            Log.d(TAG, "Started Shizuku-only Immersive Mode monitor (interval=${POLL_INTERVAL_MS}ms).")
            privilegeEngine.isShizukuAvailable.collectLatest { shizukuAvailable ->
                if (!shizukuAvailable) {
                    latestShizukuImmersiveState = false
                    if (_isImmersiveModeActive.value) {
                        _isImmersiveModeActive.value = false
                        withContext(Dispatchers.Main) {
                            stateChangeCallback?.invoke(false)
                        }
                    }
                    return@collectLatest
                }

                var lastState: Boolean? = null
                while (isActive) {
                    if (!shouldPauseMonitoring()) {
                        val isHidden = evaluateImmersiveStateSafe()
                        if (lastState == null || _isImmersiveModeActive.value != isHidden) {
                            lastState = isHidden
                            _isImmersiveModeActive.value = isHidden
                            withContext(Dispatchers.Main) {
                                stateChangeCallback?.invoke(isHidden)
                            }
                        }
                    }
                    delay(POLL_INTERVAL_MS)
                }
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
     * Debounced trigger evaluation when accessibility or window transition events fire.
     * Only executes when Shizuku is actively granted.
     */
    fun checkNow() {
        if (!privilegeEngine.isShizukuGranted() || shouldPauseMonitoring()) return
        val now = System.currentTimeMillis()
        if (now - lastCheckTimeMs < CHECK_DEBOUNCE_MS) return

        scope.launch(Dispatchers.IO) {
            if (!checkMutex.tryLock()) return@launch
            try {
                val nowInner = System.currentTimeMillis()
                if (nowInner - lastCheckTimeMs < CHECK_DEBOUNCE_MS) return@launch
                lastCheckTimeMs = nowInner
                val isHidden = checkStatusBarHiddenViaShizuku()
                if (_isImmersiveModeActive.value != isHidden) {
                    _isImmersiveModeActive.value = isHidden
                    withContext(Dispatchers.Main) {
                        stateChangeCallback?.invoke(isHidden)
                    }
                }
            } finally {
                checkMutex.unlock()
            }
        }
    }

    private suspend fun evaluateImmersiveStateSafe(): Boolean {
        if (!checkMutex.tryLock()) return _isImmersiveModeActive.value
        return try {
            lastCheckTimeMs = System.currentTimeMillis()
            checkStatusBarHiddenViaShizuku()
        } finally {
            checkMutex.unlock()
        }
    }

    private fun checkStatusBarHiddenViaShizuku(): Boolean {
        return try {
            if (!privilegeEngine.isShizukuGranted()) {
                latestShizukuImmersiveState = false
                return false
            }

            val rawOutput = privilegeEngine.executeShizukuShell(CMD).second
            if (rawOutput.isBlank()) {
                latestShizukuImmersiveState = false
                return false
            }

            val topInset = parseTopContentInset(rawOutput)
            val isImmersive = if (topInset != null) {
                topInset == 0
            } else {
                false
            }

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
