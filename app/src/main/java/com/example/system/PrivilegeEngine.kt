package com.example.system

import android.accessibilityservice.AccessibilityService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.example.model.PrivilegeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import rikka.shizuku.Shizuku

import android.Manifest
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

enum class ShizukuStatusState(val label: String) {
    RUNNING_GRANTED("Shizuku Running / Permission Granted"),
    PERMISSION_REQUIRED("Permission Required"),
    SERVICE_NOT_RUNNING("Shizuku Service Not Running")
}

class PrivilegeEngine(private val context: Context) {

    private val _currentMode = MutableStateFlow(
        try {
            val prefs = context.getSharedPreferences("quickpanel_prefs", Context.MODE_PRIVATE)
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

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        _lastExecutionLog.value = "Shizuku binder received!"
        updateShizukuStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        _isShizukuAvailable.value = false
        _shizukuStatus.value = ShizukuStatusState.SERVICE_NOT_RUNNING
        _lastExecutionLog.value = "Shizuku binder died."
    }

    private val shizukuPermissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            _isShizukuAvailable.value = true
            _shizukuStatus.value = ShizukuStatusState.RUNNING_GRANTED
            _lastExecutionLog.value = "Shizuku permission granted!"
            NativeTileManager.instance?.onPrivilegeOrPermissionsUpdated()
        } else {
            _isShizukuAvailable.value = false
            _shizukuStatus.value = ShizukuStatusState.PERMISSION_REQUIRED
            _lastExecutionLog.value = "Shizuku permission denied by user."
        }
    }

    init {
        try {
            Shizuku.addBinderReceivedListener(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(shizukuPermissionListener)
        } catch (e: Exception) {
            Log.d("PrivilegeEngine", "Error registering Shizuku listeners: ${e.message}")
        }
        detectCapabilities()
        updateShizukuStatus()
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

    fun detectCapabilities() {
        val prefs = try {
            context.getSharedPreferences("quickpanel_prefs", Context.MODE_PRIVATE)
        } catch (_: Exception) { null }
        val savedRoot = prefs?.getBoolean("root_access_granted", false) == true

        isRootGrantedMemory = savedRoot
        _isRootAvailable.value = savedRoot
        updateShizukuStatus()
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
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "which su"))
            val stdout = process.inputStream.bufferedReader().readText().trim()
            val exitCode = process.waitFor()
            exitCode == 0 && stdout.isNotBlank()
        } catch (e: Exception) {
            false
        }
    }

    fun testRootAccess(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val stdout = process.inputStream.bufferedReader().use { it.readText().trim() }
            try { process.errorStream.close() } catch (_: Exception) {}
            try { process.outputStream.close() } catch (_: Exception) {}
            val finished = process.waitFor(5, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                return false
            }
            process.exitValue() == 0 && (stdout.contains("uid=0") || stdout.contains("root"))
        } catch (e: Exception) {
            false
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
                    } catch (e: Exception) {
                        // ignore
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
        val granted = testRootAccess()
        isRootGrantedMemory = granted
        _isRootAvailable.value = granted
        try {
            val prefs = context.getSharedPreferences("quickpanel_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("root_access_granted", granted).apply()
        } catch (_: Exception) {}
        if (granted) {
            NativeTileManager.instance?.onPrivilegeOrPermissionsUpdated()
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
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            try { process.inputStream.close() } catch (_: Exception) {}
            try { process.errorStream.close() } catch (_: Exception) {}
            try { process.outputStream.close() } catch (_: Exception) {}
            val finished = process.waitFor(5, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                return false
            }
            process.exitValue() == 0
        } catch (e: Exception) {
            Log.w("PrivilegeEngine", "Root command failed gracefully: ${e.message}")
            false
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
            val proc = newProcessMethod.invoke(null, arrayOf("sh", "-c", command), null, null) as java.lang.Process

            val stdout = proc.inputStream.bufferedReader().readText().trim()
            val stderr = proc.errorStream.bufferedReader().readText().trim()
            val exitCode = proc.waitFor()

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
        }
    }

    fun runShizukuOrAdbCommand(command: String): Boolean {
        if (isShizukuGranted()) {
            return executeShizukuShell(command).first
        }
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val exit = process.waitFor()
            exit == 0
        } catch (e: Exception) {
            Log.w("PrivilegeEngine", "Shell command failed: ${e.message}")
            false
        }
    }

    /**
     * Executes a read-only shell command via Root or Shizuku and returns clean stdout,
     * or null if neither Root nor Shizuku is granted or command produces no output.
     */
    fun executeShellWithOutput(command: String): String? {
        if (isShizukuGranted()) {
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
                    val proc = newProcessMethod.invoke(null, arrayOf("sh", "-c", command), null, null) as java.lang.Process
                    val stdout = proc.inputStream.bufferedReader().use { it.readText().trim() }
                    try { proc.errorStream.close() } catch (_: Exception) {}
                    try { proc.outputStream.close() } catch (_: Exception) {}
                    val finished = proc.waitFor(4, TimeUnit.SECONDS)
                    if (!finished) {
                        proc.destroyForcibly()
                    } else if (proc.exitValue() == 0 && stdout.isNotBlank()) {
                        return stdout
                    }
                }
            } catch (_: Exception) {}
        }

        if (isRootGranted() && _currentMode.value == PrivilegeMode.ROOT) {
            try {
                val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
                val stdout = proc.inputStream.bufferedReader().use { it.readText().trim() }
                try { proc.errorStream.close() } catch (_: Exception) {}
                try { proc.outputStream.close() } catch (_: Exception) {}
                val finished = proc.waitFor(4, TimeUnit.SECONDS)
                if (!finished) {
                    proc.destroyForcibly()
                    return null
                }
                if (proc.exitValue() == 0 && stdout.isNotBlank()) {
                    return stdout
                }
            } catch (_: Exception) {}
        }

        return null
    }
}
