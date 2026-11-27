package com.example.system

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.model.PrivilegeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicLong

enum class ShizukuStatusState(val label: String) {
    RUNNING_GRANTED("Shizuku Running / Permission Granted"),
    PERMISSION_REQUIRED("Permission Required"),
    SERVICE_NOT_RUNNING("Shizuku Service Not Running")
}

class PrivilegeEngine(context: Context) {

    private val context: Context = context.applicationContext

    private val _currentMode = MutableStateFlow(
        try {
            val prefs = this.context.getSharedPreferences("quickpanel_prefs", Context.MODE_PRIVATE)
            val saved = prefs.getString("privilege_mode", PrivilegeMode.STANDARD.name)
            PrivilegeMode.valueOf(saved ?: PrivilegeMode.STANDARD.name)
        } catch (_: Exception) {
            com.example.data.PanelPreferences.privilegeMode.value
        }
    )
    val currentMode: StateFlow<PrivilegeMode> = _currentMode.asStateFlow()

    fun syncWithPreferences() {
        val savedMode = com.example.data.PanelPreferences.privilegeMode.value
        if (_currentMode.value != savedMode) {
            _currentMode.value = savedMode
            _lastExecutionLog.value = "Active privilege mode synced to: ${savedMode.title}"
        }
    }

    private val _isRootAvailable = MutableStateFlow(false)
    val isRootAvailable: StateFlow<Boolean> = _isRootAvailable.asStateFlow()

    private val _isShizukuAvailable = MutableStateFlow(false)
    val isShizukuAvailable: StateFlow<Boolean> = _isShizukuAvailable.asStateFlow()

    private val _shizukuStatus = MutableStateFlow(ShizukuStatusState.SERVICE_NOT_RUNNING)
    val shizukuStatus: StateFlow<ShizukuStatusState> = _shizukuStatus.asStateFlow()

    private val _lastExecutionLog = MutableStateFlow<String>("Engine initialized in Standard mode.")
    val lastExecutionLog: StateFlow<String> = _lastExecutionLog.asStateFlow()

    private val _hasOverlay = MutableStateFlow(hasOverlayPermission())
    val hasOverlay: StateFlow<Boolean> = _hasOverlay.asStateFlow()

    private val _hasWriteSettings = MutableStateFlow(hasWriteSettingsPermission())
    val hasWriteSettings: StateFlow<Boolean> = _hasWriteSettings.asStateFlow()

    private val _hasNotificationPolicy = MutableStateFlow(hasNotificationPolicyPermission())
    val hasNotificationPolicy: StateFlow<Boolean> = _hasNotificationPolicy.asStateFlow()

    private val _hasAccessibility = MutableStateFlow(hasAccessibilityPermission())
    val hasAccessibility: StateFlow<Boolean> = _hasAccessibility.asStateFlow()

    private val _hasLocation = MutableStateFlow(hasLocationPermission())
    val hasLocation: StateFlow<Boolean> = _hasLocation.asStateFlow()

    private val _hasNearbyDevices = MutableStateFlow(hasNearbyDevicesPermission())
    val hasNearbyDevices: StateFlow<Boolean> = _hasNearbyDevices.asStateFlow()

    init {
        registerSharedShizukuListeners()
        synchronized(sharedRootLock) {
            activeEngines[this] = true
        }
        detectCapabilities()
        updateShizukuStatus()
    }

    fun destroy() {
        synchronized(sharedRootLock) {
            activeEngines.remove(this)
        }
    }

    fun updateShizukuStatus(): ShizukuStatusState {
        val state = try {
            if (Shizuku.pingBinder()) {
                if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                    _isShizukuAvailable.value = true
                    ShizukuStatusState.RUNNING_GRANTED
                } else {
                    _isShizukuAvailable.value = false
                    ShizukuStatusState.PERMISSION_REQUIRED
                }
            } else {
                _isShizukuAvailable.value = false
                ShizukuStatusState.SERVICE_NOT_RUNNING
            }
        } catch (e: Exception) {
            _isShizukuAvailable.value = false
            ShizukuStatusState.SERVICE_NOT_RUNNING
        }
        _shizukuStatus.value = state
        return state
    }

    fun setMode(mode: PrivilegeMode) {
        _currentMode.value = mode
        _lastExecutionLog.value = "Active privilege mode set to: ${mode.title}"
        com.example.data.PanelPreferences.setPrivilegeMode(context, mode)
        val execMode = when (mode) {
            PrivilegeMode.ROOT -> com.example.model.CommandExecutionMode.ROOT
            PrivilegeMode.SHIZUKU -> com.example.model.CommandExecutionMode.SHIZUKU
            PrivilegeMode.STANDARD -> com.example.model.CommandExecutionMode.STANDARD
        }
        com.example.data.TileCommandRepository.setGlobalExecutionMode(context, execMode, enableOverrides = true)
        if (mode == PrivilegeMode.SHIZUKU) {
            requestShizukuPermission()
        }
    }

    private var isRootGrantedMemory = false

    fun isEndlessRootActive(): Boolean {
        return com.example.data.PanelPreferences.endlessRootMode.value
    }

    fun detectCapabilities() {
        val prefs = try {
            context.getSharedPreferences("quickpanel_prefs", Context.MODE_PRIVATE)
        } catch (_: Exception) { null }
        val savedRoot = prefs?.getBoolean("root_access_granted", false) == true || isEndlessRootActive()

        isRootGrantedMemory = savedRoot
        _isRootAvailable.value = savedRoot
        updateShizukuStatus()
        if (isEndlessRootActive()) {
            _currentMode.value = PrivilegeMode.ROOT
        }
    }

    private fun checkRootBinary(): Boolean {
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/su",
            "/system/bin/.ext/.su",
            "/system/usr/we-need-root/su-backup",
            "/data/adb/ksu/bin/su",
            "/data/adb/ap/bin/su",
            "/magisk/.core/bin/su",
            "/system/app/Superuser.apk",
            "/system/app/Magisk.apk"
        )
        return paths.any { File(it).exists() }
    }

    private fun checkSuInPath(): Boolean {
        var process: java.lang.Process? = null
        return try {
            process = ProcessBuilder(listOf("sh", "-c", "which su")).redirectErrorStream(true).start()
            try { process.outputStream.close() } catch (_: Exception) {}
            val stdout = process.inputStream.bufferedReader().use { it.readText().trim() }
            val finished = process.waitFor(3, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                false
            } else {
                process.exitValue() == 0 && stdout.isNotBlank()
            }
        } catch (_: Exception) {
            false
        } finally {
            process?.let { p ->
                try { p.inputStream.close() } catch (_: Exception) {}
                try { p.errorStream.close() } catch (_: Exception) {}
                try { p.outputStream.close() } catch (_: Exception) {}
                if (isProcessAlive(p)) {
                    try { p.destroyForcibly() } catch (_: Exception) {}
                }
            }
        }
    }

    fun testRootAccess(): Boolean {
        if (isEndlessRootActive()) {
            if (ensurePersistentRootSession()) return true
        }
        var process: java.lang.Process? = null
        return try {
            process = ProcessBuilder(listOf("su", "-c", "id")).redirectErrorStream(true).start()
            try { process.outputStream.close() } catch (_: Exception) {}
            val stdout = process.inputStream.bufferedReader().use { it.readText().trim() }
            val finished = process.waitFor(5, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                return false
            }
            process.exitValue() == 0 && (stdout.contains("uid=0") || stdout.contains("root"))
        } catch (_: Exception) {
            false
        } finally {
            process?.let { p ->
                try { p.inputStream.close() } catch (_: Exception) {}
                try { p.errorStream.close() } catch (_: Exception) {}
                try { p.outputStream.close() } catch (_: Exception) {}
                if (isProcessAlive(p)) {
                    try { p.destroyForcibly() } catch (_: Exception) {}
                }
            }
        }
    }

    private fun checkShizukuInstalled(): Boolean {
        return updateShizukuStatus() != ShizukuStatusState.SERVICE_NOT_RUNNING
    }

    fun requestShizukuPermission(requestCode: Int = 1001): String {
        return try {
            if (Shizuku.pingBinder()) {
                if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                    _isShizukuAvailable.value = true
                    _shizukuStatus.value = ShizukuStatusState.RUNNING_GRANTED
                    _lastExecutionLog.value = "Shizuku already authorized."
                    "Shizuku permission is already granted!"
                } else {
                    try {
                        if (Shizuku.shouldShowRequestPermissionRationale()) {
                            _lastExecutionLog.value = "Shizuku permission rationale suggested."
                        }
                    } catch (_: Exception) {
                    }
                    Shizuku.requestPermission(requestCode)
                    _lastExecutionLog.value = "Requested Shizuku permission (code $requestCode)..."
                    "Requesting Shizuku permission..."
                }
            } else {
                _shizukuStatus.value = ShizukuStatusState.SERVICE_NOT_RUNNING
                _lastExecutionLog.value = "Shizuku service binder is not running. Please start Shizuku app first."
                "Shizuku service is not running. Please start the Shizuku app first."
            }
        } catch (e: Exception) {
            Log.e("PrivilegeEngine", "Error requesting Shizuku permission", e)
            _lastExecutionLog.value = "Shizuku error: ${e.message}"
            "Shizuku error: ${e.message}"
        }
    }

    fun requestRootPermission(): Boolean {
        val granted = testRootAccess() || isEndlessRootActive()
        isRootGrantedMemory = granted
        _isRootAvailable.value = granted
        try {
            val prefs = context.getSharedPreferences("quickpanel_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("root_access_granted", granted).apply()
        } catch (_: Exception) {}
        if (granted) {
            NativeTileManager.notifyAllInstancesPrivilegeOrPermissionsUpdated()
        }
        _lastExecutionLog.value = if (granted) "Root access granted!" else "Root access denied or su unavailable."
        return granted
    }

    fun hasOverlayPermission(): Boolean = Settings.canDrawOverlays(context)
    fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(intent)
    }

    fun hasWriteSettingsPermission(): Boolean = Settings.System.canWrite(context)
    fun requestWriteSettingsPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:${context.packageName}")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(intent)
    }

    fun hasNotificationPolicyPermission(): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        return nm?.isNotificationPolicyAccessGranted == true
    }
    fun requestNotificationPolicyPermission() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(intent)
    }

    fun hasAccessibilityPermission(): Boolean {
        if (com.example.service.QuickPanelAccessibilityService.isRunning) return true
        return isAccessibilityServiceEnabled(context)
    }

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun requestLocationPermission() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun hasNearbyDevicesPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val connect = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            val scan = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            return connect || scan
        }
        return true
    }

    fun requestNearbyDevicesPermission() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun isAccessibilityServiceEnabled(ctx: Context): Boolean {
        return try {
            val enabledServices = Settings.Secure.getString(
                ctx.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            val colonSplitter = android.text.TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServices)
            val myComponentName = "${ctx.packageName}/${com.example.service.QuickPanelAccessibilityService::class.java.name}"
            val shortComponentName = "${ctx.packageName}/.service.QuickPanelAccessibilityService"
            while (colonSplitter.hasNext()) {
                val componentName = colonSplitter.next()
                if (componentName.equals(myComponentName, ignoreCase = true) ||
                    componentName.equals(shortComponentName, ignoreCase = true) ||
                    componentName.contains("QuickPanelAccessibilityService", ignoreCase = true)) {
                    return true
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(intent)
    }

    fun refreshPermissions() {
        _hasOverlay.value = hasOverlayPermission()
        _hasWriteSettings.value = hasWriteSettingsPermission()
        _hasNotificationPolicy.value = hasNotificationPolicyPermission()
        _hasAccessibility.value = hasAccessibilityPermission()
        _hasLocation.value = hasLocationPermission()
        _hasNearbyDevices.value = hasNearbyDevicesPermission()
        detectCapabilities()
        updateShizukuStatus()
    }

    suspend fun executeCommand(shellCommand: String, fallbackIntent: Intent? = null): Boolean {
        return withContext(Dispatchers.IO) {
            when (_currentMode.value) {
                PrivilegeMode.ROOT -> {
                    val success = runRootCommand(shellCommand)
                    if (success) {
                        _lastExecutionLog.value = "[ROOT SUCCESS] $shellCommand"
                        true
                    } else {
                        _lastExecutionLog.value = "[ROOT FAILED] $shellCommand (Falling back)"
                        fallbackIntent?.let {
                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(it)
                            true
                        } ?: false
                    }
                }
                PrivilegeMode.SHIZUKU -> {
                    _lastExecutionLog.value = "[SHIZUKU DISPATCH] $shellCommand"
                    val success = runShizukuOrAdbCommand(shellCommand)
                    if (success) {
                        _lastExecutionLog.value = "[SHIZUKU SUCCESS] $shellCommand"
                        true
                    } else {
                        fallbackIntent?.let {
                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(it)
                            true
                        } ?: false
                    }
                }
                PrivilegeMode.STANDARD -> {
                    _lastExecutionLog.value = "[STANDARD ROUTE] Opening intent"
                    fallbackIntent?.let {
                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(it)
                        true
                    } ?: false
                }
            }
        }
    }

    suspend fun applyAnimationScale(scale: Float): Boolean {
        return withContext(Dispatchers.IO) {
            var success = false
            try {
                Settings.Global.putFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, scale)
                Settings.Global.putFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, scale)
                Settings.Global.putFloat(context.contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, scale)
                _lastExecutionLog.value = "[SETTINGS.GLOBAL SUCCESS] Animation scales set to ${scale}x"
                success = true
            } catch (e: SecurityException) {
                _lastExecutionLog.value = "[SETTINGS.GLOBAL WRITE RESTRICTED] Trying shell fallback..."
            }

            if (!success) {
                val cmd = "settings put global animator_duration_scale $scale && " +
                          "settings put global window_animation_scale $scale && " +
                          "settings put global transition_animation_scale $scale"
                success = executeCommand(cmd)
            }
            success
        }
    }

    suspend fun executeCommandWithMode(
        shellCommand: String,
        mode: com.example.model.CommandExecutionMode,
        fallbackIntent: Intent? = null
    ): Boolean {
        return withContext(Dispatchers.IO) {
            when (mode) {
                com.example.model.CommandExecutionMode.ROOT -> {
                    val success = runRootCommand(shellCommand)
                    if (success) {
                        _lastExecutionLog.value = "[ROOT SUCCESS] $shellCommand"
                        true
                    } else {
                        _lastExecutionLog.value = "[ROOT FAILED] $shellCommand"
                        fallbackIntent?.let {
                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            try {
                                context.startActivity(it)
                                true
                            } catch (e: Exception) {
                                false
                            }
                        } ?: false
                    }
                }
                com.example.model.CommandExecutionMode.SHIZUKU -> {
                    val success = runShizukuOrAdbCommand(shellCommand)
                    if (success) {
                        _lastExecutionLog.value = "[SHIZUKU SUCCESS] $shellCommand"
                        true
                    } else {
                        _lastExecutionLog.value = "[SHIZUKU FAILED] $shellCommand"
                        fallbackIntent?.let {
                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            try {
                                context.startActivity(it)
                                true
                            } catch (e: Exception) {
                                false
                            }
                        } ?: false
                    }
                }
                com.example.model.CommandExecutionMode.STANDARD -> {
                    _lastExecutionLog.value = "[STANDARD EXEC] Executing via Standard API / Intent"
                    var launched = false
                    if (fallbackIntent != null) {
                        try {
                            fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(fallbackIntent)
                            launched = true
                        } catch (e: Exception) {
                            Log.w("PrivilegeEngine", "Standard fallback intent launch failed: ${e.message}")
                        }
                    }
                    if (!launched && shellCommand.isNotBlank()) {
                        try {
                            val intent = when {
                                shellCommand.contains('/') -> {
                                    val pkg = shellCommand.substringBefore('/')
                                    val cls = shellCommand.substringAfter('/')
                                    val fullCls = if (cls.startsWith(".")) "$pkg$cls" else cls
                                    Intent().apply {
                                        setClassName(pkg, fullCls)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                }
                                shellCommand.contains('.') -> {
                                    Intent(shellCommand).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                }
                                else -> null
                            }
                            if (intent != null) {
                                context.startActivity(intent)
                                launched = true
                            }
                        } catch (e: Exception) {
                            Log.w("PrivilegeEngine", "Standard intent launch failed: ${e.message}")
                        }
                    }
                    launched
                }
            }
        }
    }

    private fun runRootCommand(command: String): Boolean {
        if (isEndlessRootActive()) {
            return when (executeEndlessRootCommandInternal(command).status) {
                EndlessExecStatus.RAN_SUCCESS -> true
                EndlessExecStatus.RAN_NON_ZERO -> false
                EndlessExecStatus.SESSION_UNAVAILABLE -> runStandaloneSuCommand(command)
            }
        }
        return runStandaloneSuCommand(command)
    }

    private fun runStandaloneSuCommand(command: String): Boolean {
        var process: java.lang.Process? = null
        return try {
            process = ProcessBuilder(listOf("su", "-c", command)).redirectErrorStream(true).start()
            try { process.outputStream.close() } catch (_: Exception) {}
            // Drain stdout briefly so the child process cannot block on a full pipe before exiting
            try { process.inputStream.bufferedReader().use { reader ->
                val buf = CharArray(1024)
                var readTotal = 0
                while (readTotal < 16_384) {
                    val n = reader.read(buf)
                    if (n <= 0) break
                    readTotal += n
                }
            } } catch (_: Exception) {}
            val finished = process.waitFor(5, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                return false
            }
            process.exitValue() == 0
        } catch (e: Exception) {
            Log.w("PrivilegeEngine", "Root command failed gracefully: ${e.message}")
            false
        } finally {
            process?.let { p ->
                try { p.inputStream.close() } catch (_: Exception) {}
                try { p.errorStream.close() } catch (_: Exception) {}
                try { p.outputStream.close() } catch (_: Exception) {}
                if (isProcessAlive(p)) {
                    try { p.destroyForcibly() } catch (_: Exception) {}
                }
            }
        }
    }

    fun isShizukuGranted(): Boolean {
        return try {
            Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) {
            false
        }
    }

    fun isRootGranted(): Boolean {
        if (isEndlessRootActive()) {
            if (!_isRootAvailable.value) _isRootAvailable.value = true
            if (_currentMode.value != PrivilegeMode.ROOT) _currentMode.value = PrivilegeMode.ROOT
            return true
        }
        return _isRootAvailable.value
    }

    /**
     * Applies or resets CPU core affinity to the Prime Core (Cortex-X / 7th core).
     * Executed via Root shell privileges (or Shizuku if root is absent).
     */
    fun applyPrimeCoreAffinity(enable: Boolean): Boolean {
        val pid = android.os.Process.myPid()
        val command = if (enable) {
            "taskset -p 80 $pid || taskset -ap 7 $pid || taskset -p 0x80 $pid"
        } else {
            "taskset -p ff $pid || taskset -ap 0-7 $pid || taskset -p 0xff $pid"
        }

        if (isRootGranted()) {
            val success = runRootCommand(command)
            Log.i("PrivilegeEngine", "applyPrimeCoreAffinity via Root(enable=$enable, pid=$pid) -> success: $success")
            return success
        }

        if (!isShizukuGranted()) {
            val errorMsg = "Root permission required for High Smoothness mode"
            _lastExecutionLog.value = errorMsg
            Log.w("PrivilegeEngine", errorMsg)
            return false
        }

        val (success, output) = executeShizukuShell(command)
        Log.i("PrivilegeEngine", "applyPrimeCoreAffinity(enable=$enable, pid=$pid) -> success: $success (out: $output)")
        return success
    }

    /**
     * Executes a command strictly via Shizuku's remote process with system shell privileges.
     * Returns Pair(success: Boolean, output: String).
     */
    fun executeShizukuShell(command: String): Pair<Boolean, String> {
        if (!isShizukuGranted()) {
            val errorMsg = "Shizuku permission required for Prime Core mode"
            _lastExecutionLog.value = errorMsg
            return Pair(false, errorMsg)
        }

        var proc: java.lang.Process? = null
        return try {
            val newProcessMethod = try {
                Shizuku::class.java.getDeclaredMethod(
                    "newProcess",
                    Array<String>::class.java,
                    Array<String>::class.java,
                    String::class.java
                )
            } catch (_: NoSuchMethodException) {
                Shizuku::class.java.methods.firstOrNull { it.name == "newProcess" }
                    ?: throw IllegalStateException("Shizuku newProcess method not found")
            }
            newProcessMethod.isAccessible = true
            proc = newProcessMethod.invoke(null, arrayOf("sh", "-c", command), null, null) as java.lang.Process

            try { proc.outputStream.close() } catch (_: Exception) {}
            val stdout = proc.inputStream.bufferedReader().use { it.readText().trim().take(MAX_SHELL_OUTPUT_CHARS) }
            val stderr = proc.errorStream.bufferedReader().use { it.readText().trim().take(4096) }
            val finished = proc.waitFor(5, TimeUnit.SECONDS)
            if (!finished) {
                proc.destroyForcibly()
                return Pair(false, "Shizuku command timed out")
            }
            val exitCode = proc.exitValue()

            val combined = when {
                stdout.isNotBlank() && stderr.isNotBlank() -> "$stdout | $stderr"
                stdout.isNotBlank() -> stdout
                stderr.isNotBlank() -> stderr
                else -> if (exitCode == 0) "Success (Exit code: 0)" else "Failed (Exit code: $exitCode)"
            }
            _lastExecutionLog.value = combined
            Log.d("PrivilegeEngine", "Shizuku shell executed [exit: $exitCode]: $combined")
            Pair(exitCode == 0, combined)
        } catch (e: Exception) {
            val error = "Shizuku process runner failed: ${e.message}"
            Log.e("PrivilegeEngine", error, e)
            _lastExecutionLog.value = error
            Pair(false, error)
        } finally {
            proc?.let { p ->
                try { p.inputStream.close() } catch (_: Exception) {}
                try { p.errorStream.close() } catch (_: Exception) {}
                try { p.outputStream.close() } catch (_: Exception) {}
                if (isProcessAlive(p)) {
                    try { p.destroyForcibly() } catch (_: Exception) {}
                }
            }
        }
    }

    fun runShizukuOrAdbCommand(command: String): Boolean {
        if (isShizukuGranted()) {
            return executeShizukuShell(command).first
        }
        var process: java.lang.Process? = null
        return try {
            process = ProcessBuilder(listOf("sh", "-c", command)).redirectErrorStream(true).start()
            try { process.outputStream.close() } catch (_: Exception) {}
            try { process.inputStream.bufferedReader().use { reader ->
                val buf = CharArray(1024)
                var readTotal = 0
                while (readTotal < 16_384) {
                    val n = reader.read(buf)
                    if (n <= 0) break
                    readTotal += n
                }
            } } catch (_: Exception) {}
            val finished = process.waitFor(4, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                false
            } else {
                process.exitValue() == 0
            }
        } catch (e: Exception) {
            Log.w("PrivilegeEngine", "Shell command failed: ${e.message}")
            false
        } finally {
            process?.let { p ->
                try { p.inputStream.close() } catch (_: Exception) {}
                try { p.errorStream.close() } catch (_: Exception) {}
                try { p.outputStream.close() } catch (_: Exception) {}
                if (isProcessAlive(p)) {
                    try { p.destroyForcibly() } catch (_: Exception) {}
                }
            }
        }
    }

    /**
     * Executes a read-only shell command via Root or Shizuku and returns clean stdout,
     * or null if neither Root nor Shizuku is granted or command produces no output.
     */
    fun executeShellWithOutput(command: String): String? {
        if (isShizukuGranted()) {
            var proc: java.lang.Process? = null
            try {
                val newProcessMethod = try {
                    Shizuku::class.java.getDeclaredMethod(
                        "newProcess",
                        Array<String>::class.java,
                        Array<String>::class.java,
                        String::class.java
                    )
                } catch (_: NoSuchMethodException) {
                    Shizuku::class.java.methods.firstOrNull { it.name == "newProcess" }
                }
                if (newProcessMethod != null) {
                    newProcessMethod.isAccessible = true
                    proc = newProcessMethod.invoke(null, arrayOf("sh", "-c", command), null, null) as java.lang.Process
                    try { proc.outputStream.close() } catch (_: Exception) {}
                    val stdout = proc.inputStream.bufferedReader().use { it.readText().trim().take(MAX_SHELL_OUTPUT_CHARS) }
                    val finished = proc.waitFor(4, TimeUnit.SECONDS)
                    if (!finished) {
                        proc.destroyForcibly()
                    } else if (proc.exitValue() == 0 && stdout.isNotBlank()) {
                        return stdout
                    }
                }
            } catch (_: Exception) {
            } finally {
                proc?.let { p ->
                    try { p.inputStream.close() } catch (_: Exception) {}
                    try { p.errorStream.close() } catch (_: Exception) {}
                    try { p.outputStream.close() } catch (_: Exception) {}
                    if (isProcessAlive(p)) {
                        try { p.destroyForcibly() } catch (_: Exception) {}
                    }
                }
            }
        }

        if (isEndlessRootActive()) {
            val outcome = executeEndlessRootCommandInternal(command)
            when (outcome.status) {
                EndlessExecStatus.RAN_SUCCESS -> return outcome.output?.takeIf { it.isNotBlank() }
                EndlessExecStatus.RAN_NON_ZERO -> return null
                EndlessExecStatus.SESSION_UNAVAILABLE -> {
                    // Fall through only if persistent session failed to launch
                }
            }
        }

        if (isRootGranted() && _currentMode.value == PrivilegeMode.ROOT) {
            var proc: java.lang.Process? = null
            try {
                proc = ProcessBuilder(listOf("su", "-c", command)).redirectErrorStream(true).start()
                try { proc.outputStream.close() } catch (_: Exception) {}
                val stdout = proc.inputStream.bufferedReader().use { it.readText().trim().take(MAX_SHELL_OUTPUT_CHARS) }
                val finished = proc.waitFor(4, TimeUnit.SECONDS)
                if (!finished) {
                    proc.destroyForcibly()
                    return null
                }
                if (proc.exitValue() == 0 && stdout.isNotBlank()) {
                    return stdout
                }
            } catch (_: Exception) {
            } finally {
                proc?.let { p ->
                    try { p.inputStream.close() } catch (_: Exception) {}
                    try { p.errorStream.close() } catch (_: Exception) {}
                    try { p.outputStream.close() } catch (_: Exception) {}
                    if (isProcessAlive(p)) {
                        try { p.destroyForcibly() } catch (_: Exception) {}
                    }
                }
            }
        }

        return null
    }

    fun ensurePersistentRootSession(): Boolean {
        if (!isEndlessRootActive()) return false
        synchronized(sharedRootLock) {
            val proc = sharedRootProcess
            if (proc != null) {
                val alive = isProcessAlive(proc)
                val needsRecycle = endlessCommandCount >= MAX_COMMANDS_BEFORE_RECYCLE
                if (alive && !needsRecycle && sharedRootWriter != null && sharedRootReader != null) {
                    return true
                }
            }
            closePersistentRootSessionLocked(gracefulExit = true)
            return try {
                // Merge stderr -> stdout so stderr pipe never fills (64KB deadlock) or leaks
                val newProc = ProcessBuilder(listOf("su"))
                    .redirectErrorStream(true)
                    .start()
                sharedRootProcess = newProc
                sharedRootWriter = newProc.outputStream.bufferedWriter()
                sharedRootReader = newProc.inputStream.bufferedReader()
                endlessCommandCount = 0
                lastEndlessActivityMs = System.currentTimeMillis()
                scheduleIdleRootSessionTrimLocked()
                _isRootAvailable.value = true
                _lastExecutionLog.value = "Endless Root daemon session established (PID active)."
                true
            } catch (e: Exception) {
                closePersistentRootSessionLocked(gracefulExit = false)
                Log.w("PrivilegeEngine", "Failed to start endless root session: ${e.message}")
                false
            }
        }
    }

    fun closePersistentRootSession() {
        synchronized(sharedRootLock) {
            closePersistentRootSessionLocked(gracefulExit = true)
        }
    }

    private enum class EndlessExecStatus {
        RAN_SUCCESS,
        RAN_NON_ZERO,
        SESSION_UNAVAILABLE
    }

    private data class EndlessExecOutcome(
        val status: EndlessExecStatus,
        val output: String?
    )

    fun executeEndlessRootCommand(command: String): Pair<Boolean, String?> {
        val outcome = executeEndlessRootCommandInternal(command)
        return Pair(outcome.status == EndlessExecStatus.RAN_SUCCESS, outcome.output)
    }

    private fun executeEndlessRootCommandInternal(command: String): EndlessExecOutcome {
        if (command.isBlank()) return EndlessExecOutcome(EndlessExecStatus.RAN_NON_ZERO, null)
        synchronized(sharedRootLock) {
            if (!ensurePersistentRootSession()) {
                return EndlessExecOutcome(EndlessExecStatus.SESSION_UNAVAILABLE, null)
            }
            val writer = sharedRootWriter ?: return EndlessExecOutcome(EndlessExecStatus.SESSION_UNAVAILABLE, null)
            val reader = sharedRootReader ?: return EndlessExecOutcome(EndlessExecStatus.SESSION_UNAVAILABLE, null)
            val seq = markerSeq.incrementAndGet()
            val marker = "__ENDLESS_ROOT_${seq}_${System.nanoTime()}__"
            return try {
                val sanitized = command.trimEnd()
                writer.write("$sanitized\necho $marker $?\n")
                writer.flush()

                val future = endlessReaderExecutor.submit(Callable {
                    val output = StringBuilder()
                    var success = false
                    var truncated = false
                    while (true) {
                        val line = reader.readLine() ?: throw java.io.EOFException("su stream closed")
                        if (line.startsWith(marker)) {
                            val exitCodeStr = line.substringAfter(marker).trim()
                            val exitCode = exitCodeStr.toIntOrNull() ?: 0
                            success = (exitCode == 0)
                            break
                        } else if (!truncated) {
                            if (output.length + line.length + 1 <= MAX_SHELL_OUTPUT_CHARS) {
                                if (output.isNotEmpty()) output.append('\n')
                                output.append(line)
                            } else {
                                truncated = true
                            }
                        }
                    }
                    Pair(success, output.toString())
                })

                val result = try {
                    future.get(ENDLESS_COMMAND_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                } catch (te: TimeoutException) {
                    future.cancel(true)
                    Log.w("PrivilegeEngine", "Endless root command timed out (${ENDLESS_COMMAND_TIMEOUT_MS}ms); resetting su daemon.")
                    closePersistentRootSessionLocked(gracefulExit = false)
                    return EndlessExecOutcome(EndlessExecStatus.SESSION_UNAVAILABLE, null)
                }

                endlessCommandCount++
                lastEndlessActivityMs = System.currentTimeMillis()
                if (endlessCommandCount >= MAX_COMMANDS_BEFORE_RECYCLE) {
                    closePersistentRootSessionLocked(gracefulExit = true)
                } else {
                    scheduleIdleRootSessionTrimLocked()
                }
                if (result.first) {
                    EndlessExecOutcome(EndlessExecStatus.RAN_SUCCESS, result.second)
                } else {
                    EndlessExecOutcome(EndlessExecStatus.RAN_NON_ZERO, result.second)
                }
            } catch (e: Exception) {
                Log.w("PrivilegeEngine", "Error executing endless root command: ${e.message}")
                closePersistentRootSessionLocked(gracefulExit = false)
                EndlessExecOutcome(EndlessExecStatus.SESSION_UNAVAILABLE, null)
            }
        }
    }

    fun setEndlessRootMode(enabled: Boolean) {
        if (enabled) {
            try {
                val prefs = context.getSharedPreferences("quickpanel_prefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("root_access_granted", true).apply()
            } catch (_: Exception) {}
            synchronized(sharedRootLock) {
                activeEngines.keys.forEach { engine ->
                    engine._isRootAvailable.value = true
                    engine._currentMode.value = PrivilegeMode.ROOT
                    engine._lastExecutionLog.value = "Endless Root Mode enabled: Persistent daemon active."
                }
            }
            if (Looper.myLooper() != Looper.getMainLooper()) {
                ensurePersistentRootSession()
            } else {
                rootDaemonScope.launch { ensurePersistentRootSession() }
            }
            NativeTileManager.notifyAllInstancesPrivilegeOrPermissionsUpdated()
        } else {
            closePersistentRootSession()
            synchronized(sharedRootLock) {
                activeEngines.keys.forEach { engine ->
                    engine._lastExecutionLog.value = "Endless Root Mode disabled."
                }
            }
        }
    }

    companion object {
        private const val MAX_SHELL_OUTPUT_CHARS = 128 * 1024
        private const val ENDLESS_COMMAND_TIMEOUT_MS = 4000L
        private const val MAX_COMMANDS_BEFORE_RECYCLE = 25
        private const val IDLE_ROOT_SESSION_CLEANUP_MS = 25_000L
        private const val IDLE_ENDLESS_ROOT_SESSION_CLEANUP_MS = 45_000L

        @Volatile
        private var sharedRootProcess: java.lang.Process? = null
        private var sharedRootWriter: java.io.BufferedWriter? = null
        private var sharedRootReader: java.io.BufferedReader? = null
        private var endlessCommandCount: Int = 0
        @Volatile
        private var lastEndlessActivityMs: Long = 0L
        private val sharedRootLock = Any()
        private val activeEngines = java.util.WeakHashMap<PrivilegeEngine, Boolean>()
        private val markerSeq = AtomicLong(0L)
        private val rootDaemonScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private var idleTrimJob: Job? = null
        private val endlessReaderExecutor = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "EndlessRootReader").apply { isDaemon = true }
        }

        @Volatile
        private var sharedFallbackEngine: PrivilegeEngine? = null

        @Volatile
        private var shizukuListenersRegistered = false

        private val sharedBinderReceivedListener = Shizuku.OnBinderReceivedListener {
            snapshotEngines().forEach { engine ->
                engine._lastExecutionLog.value = "Shizuku binder received!"
                engine.updateShizukuStatus()
            }
        }

        private val sharedBinderDeadListener = Shizuku.OnBinderDeadListener {
            snapshotEngines().forEach { engine ->
                engine._isShizukuAvailable.value = false
                engine._shizukuStatus.value = ShizukuStatusState.SERVICE_NOT_RUNNING
                engine._lastExecutionLog.value = "Shizuku binder died."
            }
        }

        private val sharedShizukuPermissionListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
            val granted = grantResult == PackageManager.PERMISSION_GRANTED
            snapshotEngines().forEach { engine ->
                if (granted) {
                    engine._isShizukuAvailable.value = true
                    engine._shizukuStatus.value = ShizukuStatusState.RUNNING_GRANTED
                    engine._lastExecutionLog.value = "Shizuku permission granted!"
                } else {
                    engine._isShizukuAvailable.value = false
                    engine._shizukuStatus.value = ShizukuStatusState.PERMISSION_REQUIRED
                    engine._lastExecutionLog.value = "Shizuku permission denied by user."
                }
            }
            if (granted) {
                NativeTileManager.notifyAllInstancesPrivilegeOrPermissionsUpdated()
            }
        }

        private fun snapshotEngines(): List<PrivilegeEngine> = synchronized(sharedRootLock) {
            activeEngines.keys.toList()
        }

        private fun registerSharedShizukuListeners() {
            if (shizukuListenersRegistered) return
            synchronized(sharedRootLock) {
                if (shizukuListenersRegistered) return
                try {
                    Shizuku.addBinderReceivedListener(sharedBinderReceivedListener)
                    Shizuku.addBinderDeadListener(sharedBinderDeadListener)
                    Shizuku.addRequestPermissionResultListener(sharedShizukuPermissionListener)
                    shizukuListenersRegistered = true
                } catch (e: Exception) {
                    Log.d("PrivilegeEngine", "Error registering Shizuku listeners: ${e.message}")
                }
            }
        }

        private fun isProcessAlive(proc: java.lang.Process): Boolean {
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    proc.isAlive
                } else {
                    try {
                        proc.exitValue()
                        false
                    } catch (_: IllegalThreadStateException) {
                        true
                    }
                }
            } catch (_: Exception) {
                false
            }
        }

        private fun scheduleIdleRootSessionTrimLocked() {
            idleTrimJob?.cancel()
            idleTrimJob = null
            val timeoutMs = if (com.example.data.PanelPreferences.endlessRootMode.value) {
                IDLE_ENDLESS_ROOT_SESSION_CLEANUP_MS
            } else {
                IDLE_ROOT_SESSION_CLEANUP_MS
            }
            idleTrimJob = rootDaemonScope.launch {
                delay(timeoutMs)
                synchronized(sharedRootLock) {
                    val elapsed = System.currentTimeMillis() - lastEndlessActivityMs
                    if (sharedRootProcess != null && elapsed >= timeoutMs) {
                        // Trim idle su child process & buffers while preserving Endless Root Mode grants
                        // and state so the next root command seamlessly respawns su without re-prompting.
                        closePersistentRootSessionLocked(gracefulExit = true)
                    }
                }
            }
        }

        private fun closePersistentRootSessionLocked(gracefulExit: Boolean = true) {
            idleTrimJob?.cancel()
            idleTrimJob = null
            val proc = sharedRootProcess
            if (gracefulExit && proc != null && isProcessAlive(proc)) {
                try {
                    sharedRootWriter?.write("exit\n")
                    sharedRootWriter?.flush()
                } catch (_: Exception) {}
            }
            try { sharedRootWriter?.close() } catch (_: Exception) {}
            try { sharedRootReader?.close() } catch (_: Exception) {}
            if (proc != null) {
                try {
                    val exited = if (gracefulExit && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        proc.waitFor(150, TimeUnit.MILLISECONDS)
                    } else {
                        false
                    }
                    if (!exited || isProcessAlive(proc)) {
                        proc.destroyForcibly()
                    }
                } catch (_: Exception) {
                    try { proc.destroyForcibly() } catch (_: Exception) {}
                }
            }
            sharedRootWriter = null
            sharedRootReader = null
            sharedRootProcess = null
            endlessCommandCount = 0
        }

        fun getShared(context: Context): PrivilegeEngine {
            NativeTileManager.instance?.privilegeEngine?.let { return it }
            return sharedFallbackEngine ?: synchronized(sharedRootLock) {
                sharedFallbackEngine ?: PrivilegeEngine(context.applicationContext).also {
                    sharedFallbackEngine = it
                }
            }
        }

        internal fun activeEngineCount(): Int = synchronized(sharedRootLock) {
            activeEngines.size
        }
    }
}
