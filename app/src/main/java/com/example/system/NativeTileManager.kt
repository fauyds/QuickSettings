package com.example.system

import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.location.LocationManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.nfc.NfcAdapter
import android.telephony.TelephonyManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import com.example.model.PrivilegeMode
import com.example.model.QsTileModel
import com.example.model.TileState
import com.example.service.QuickPanelAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Native Tile Manager with Universal System Tile Interoperability Protocol:
 * 1. Universal Accessibility Click Simulation (Non-Root Engine).
 * 2. Direct Command & Intent Routing (Shizuku & Root Engine).
 * 3. Automatic Real-Time State Mirroring across hardware & system observers.
 * 4. Custom ROM & System-Specific Tiles Support (crDroid, LineageOS, PixelExperience, etc.).
 */
class NativeTileManager(
    private val context: Context,
    val privilegeEngine: PrivilegeEngine,
    private val scope: CoroutineScope
) {
    private val handler = Handler(Looper.getMainLooper())
    private val contentResolver = context.contentResolver

    // Native State Flows
    private val _isWifiEnabled = MutableStateFlow(false)
    val isWifiEnabled: StateFlow<Boolean> = _isWifiEnabled.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(false)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private val _isCellularEnabled = MutableStateFlow(false)
    val isCellularEnabled: StateFlow<Boolean> = _isCellularEnabled.asStateFlow()

    private val _isTorchEnabled = MutableStateFlow(false)
    val isTorchEnabled: StateFlow<Boolean> = _isTorchEnabled.asStateFlow()

    private val _isAutoRotateEnabled = MutableStateFlow(false)
    val isAutoRotateEnabled: StateFlow<Boolean> = _isAutoRotateEnabled.asStateFlow()

    private val _isDndEnabled = MutableStateFlow(false)
    val isDndEnabled: StateFlow<Boolean> = _isDndEnabled.asStateFlow()

    private val _isAirplaneModeEnabled = MutableStateFlow(false)
    val isAirplaneModeEnabled: StateFlow<Boolean> = _isAirplaneModeEnabled.asStateFlow()

    private val _isLocationEnabled = MutableStateFlow(false)
    val isLocationEnabled: StateFlow<Boolean> = _isLocationEnabled.asStateFlow()

    private val _isHotspotEnabled = MutableStateFlow(false)
    val isHotspotEnabled: StateFlow<Boolean> = _isHotspotEnabled.asStateFlow()

    private val _isBatterySaverEnabled = MutableStateFlow(false)
    val isBatterySaverEnabled: StateFlow<Boolean> = _isBatterySaverEnabled.asStateFlow()

    private val _isNfcEnabled = MutableStateFlow(false)
    val isNfcEnabled: StateFlow<Boolean> = _isNfcEnabled.asStateFlow()

    private val _isDarkThemeEnabled = MutableStateFlow(true)
    val isDarkThemeEnabled: StateFlow<Boolean> = _isDarkThemeEnabled.asStateFlow()

    private val _isAodEnabled = MutableStateFlow(false)
    val isAodEnabled: StateFlow<Boolean> = _isAodEnabled.asStateFlow()

    private val _isAmbientDisplayEnabled = MutableStateFlow(false)
    val isAmbientDisplayEnabled: StateFlow<Boolean> = _isAmbientDisplayEnabled.asStateFlow()

    private val _isCaffeineEnabled = MutableStateFlow(false)
    val isCaffeineEnabled: StateFlow<Boolean> = _isCaffeineEnabled.asStateFlow()

    private val _isHeadsUpEnabled = MutableStateFlow(true)
    val isHeadsUpEnabled: StateFlow<Boolean> = _isHeadsUpEnabled.asStateFlow()

    // Screen Recording State Flow from ScreenRecordService
    val isScreenRecording: StateFlow<Boolean> = com.example.service.ScreenRecordService.isRecording

    private var defaultCaffeineTimeout: Int
        get() = context.getSharedPreferences("quickpanel_prefs", Context.MODE_PRIVATE).getInt("caffeine_default_timeout", 30000)
        set(value) {
            context.getSharedPreferences("quickpanel_prefs", Context.MODE_PRIVATE).edit().putInt("caffeine_default_timeout", value).apply()
        }

    // Dynamic state map for Custom ROM / System-specific tiles
    private val _customRomTileStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val customRomTileStates: StateFlow<Map<String, Boolean>> = _customRomTileStates.asStateFlow()

    private var ignoreDarkModeObserverUntil = 0L

    // Brightness state (0..255)
    private val _brightness = MutableStateFlow(128)
    val brightness: StateFlow<Int> = _brightness.asStateFlow()

    // Volume states
    private val _mediaVolume = MutableStateFlow(0.5f)
    val mediaVolume: StateFlow<Float> = _mediaVolume.asStateFlow()

    private val _ringVolume = MutableStateFlow(0.5f)
    val ringVolume: StateFlow<Float> = _ringVolume.asStateFlow()

    private val _alarmVolume = MutableStateFlow(0.5f)
    val alarmVolume: StateFlow<Float> = _alarmVolume.asStateFlow()

    private val _notificationVolume = MutableStateFlow(0.5f)
    val notificationVolume: StateFlow<Float> = _notificationVolume.asStateFlow()

    // Services
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    private val nfcAdapter = NfcAdapter.getDefaultAdapter(context)

    private var torchCameraId: String? = null

    init {
        instance = this
        initTorch()
        refreshAllStates()
        registerObservers()
    }

    companion object {
        var instance: NativeTileManager? = null
            private set
    }

    private fun initTorch() {
        cameraManager?.let { cm ->
            try {
                for (id in cm.cameraIdList) {
                    val characteristics = cm.getCameraCharacteristics(id)
                    val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                    val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                    if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        torchCameraId = id
                        break
                    }
                }
                cm.registerTorchCallback(object : CameraManager.TorchCallback() {
                    override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                        if (cameraId == torchCameraId) {
                            _isTorchEnabled.value = enabled
                        }
                    }
                }, handler)
            } catch (e: Exception) {
                Log.w("NativeTileManager", "Torch setup failed: ${e.message}")
            }
        }
    }

    fun checkDarkThemeState(): Boolean {
        return try {
            val nightMode = Settings.Secure.getInt(contentResolver, "ui_night_mode", -1)
            if (nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                true
            } else if (nightMode == android.content.res.Configuration.UI_MODE_NIGHT_NO) {
                false
            } else {
                val currentUiMode = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                currentUiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        } catch (e: Exception) {
            val currentUiMode = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
            currentUiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
        }
    }

    fun isMobileDataActive(context: Context): Boolean {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return try {
            telephonyManager.isDataEnabled
        } catch (e: Exception) {
            Settings.Global.getInt(context.contentResolver, "mobile_data", 0) == 1
        }
    }

    fun isHotspotActive(context: Context): Boolean {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return try {
            val method = wifiManager.javaClass.getDeclaredMethod("isWifiApEnabled")
            method.isAccessible = true
            method.invoke(wifiManager) as Boolean
        } catch (e: Exception) {
            false
        }
    }

    @Volatile
    var isDraggingShade: Boolean = false

    /**
     * Automatic Real-Time State Mirroring:
     * Reflects live system states into StateFlows so the UI mirrors stock SystemUI immediately.
     */
    fun refreshAllStates() {
        if (isDraggingShade) return
        scope.launch(Dispatchers.IO) {
            val wifi = wifiManager?.isWifiEnabled == true
            val bt = bluetoothManager?.adapter?.isEnabled == true
            val mobile = isMobileDataActive(context)
            val hotspot = isHotspotActive(context)
            val autoRotate = Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) == 1
            val airplane = Settings.Global.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1
            val dndFilter = notificationManager?.currentInterruptionFilter ?: NotificationManager.INTERRUPTION_FILTER_ALL
            val dnd = dndFilter != NotificationManager.INTERRUPTION_FILTER_ALL
            val loc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                locationManager?.isLocationEnabled == true
            } else {
                locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
            }
            val powerSave = powerManager?.isPowerSaveMode == true ||
                    Settings.Global.getInt(contentResolver, "low_power", 0) == 1
            val nfc = nfcAdapter?.isEnabled == true
            val currentBrightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128)
            val darkTheme = if (System.currentTimeMillis() >= ignoreDarkModeObserverUntil) checkDarkThemeState() else _isDarkThemeEnabled.value
            val aod = Settings.Secure.getInt(contentResolver, "doze_always_on", 0) == 1
            val ambient = Settings.Secure.getInt(contentResolver, "doze_enabled", 0) == 1
            val timeout = Settings.System.getInt(contentResolver, "screen_off_timeout", 30000)
            val caffeine = timeout >= 600000 || timeout == 2147483647
            val headsUp = try {
                Settings.Global.getInt(contentResolver, "heads_up_notifications_enabled", 1) == 1
            } catch (e: Exception) { true }

            var curMedia = 0f
            var curRing = 0f
            var curAlarm = 0f
            var curNotif = 0f
            audioManager?.let { am ->
                val maxMedia = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
                curMedia = am.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxMedia

                val maxRing = am.getStreamMaxVolume(AudioManager.STREAM_RING).coerceAtLeast(1)
                curRing = am.getStreamVolume(AudioManager.STREAM_RING).toFloat() / maxRing

                val maxAlarm = am.getStreamMaxVolume(AudioManager.STREAM_ALARM).coerceAtLeast(1)
                curAlarm = am.getStreamVolume(AudioManager.STREAM_ALARM).toFloat() / maxAlarm

                val maxNotif = am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION).coerceAtLeast(1)
                curNotif = am.getStreamVolume(AudioManager.STREAM_NOTIFICATION).toFloat() / maxNotif
            }

            _isWifiEnabled.value = wifi
            _isBluetoothEnabled.value = bt
            _isCellularEnabled.value = mobile
            _isHotspotEnabled.value = hotspot
            _isAutoRotateEnabled.value = autoRotate
            _isAirplaneModeEnabled.value = airplane
            _isDndEnabled.value = dnd
            _isLocationEnabled.value = loc
            _isBatterySaverEnabled.value = powerSave
            _isNfcEnabled.value = nfc
            _brightness.value = currentBrightness
            if (System.currentTimeMillis() >= ignoreDarkModeObserverUntil) {
                _isDarkThemeEnabled.value = darkTheme
            }
            _isAodEnabled.value = aod
            _isAmbientDisplayEnabled.value = ambient
            _isCaffeineEnabled.value = caffeine
            _isHeadsUpEnabled.value = headsUp

            audioManager?.let {
                _mediaVolume.value = curMedia
                _ringVolume.value = curRing
                _alarmVolume.value = curAlarm
                _notificationVolume.value = curNotif
            }
        }
    }

    private fun registerObservers() {
        // Observer for Auto-Rotate
        contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
            true,
            object : ContentObserver(handler) {
                override fun onChange(selfChange: Boolean) {
                    val enabled = Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) == 1
                    _isAutoRotateEnabled.value = enabled
                }
            }
        )

        // Observer for Brightness
        contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
            true,
            object : ContentObserver(handler) {
                override fun onChange(selfChange: Boolean) {
                    val bright = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128)
                    _brightness.value = bright
                }
            }
        )

        // Observer for Airplane Mode
        contentResolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.AIRPLANE_MODE_ON),
            true,
            object : ContentObserver(handler) {
                override fun onChange(selfChange: Boolean) {
                    val enabled = Settings.Global.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1
                    _isAirplaneModeEnabled.value = enabled
                }
            }
        )

        // Observer for Mobile Data
        try {
            contentResolver.registerContentObserver(
                Settings.Global.getUriFor("mobile_data"),
                true,
                object : ContentObserver(handler) {
                    override fun onChange(selfChange: Boolean) {
                        _isCellularEnabled.value = isMobileDataActive(context)
                    }
                }
            )
        } catch (e: Exception) {
            Log.w("NativeTileManager", "mobile_data observer error: ${e.message}")
        }

        // Observer for Wi-Fi setting
        try {
            contentResolver.registerContentObserver(
                Settings.Global.getUriFor("wifi_on"),
                true,
                object : ContentObserver(handler) {
                    override fun onChange(selfChange: Boolean) {
                        _isWifiEnabled.value = wifiManager?.isWifiEnabled == true
                    }
                }
            )
        } catch (e: Exception) {}

        // Observer for Location Setting
        try {
            contentResolver.registerContentObserver(
                Settings.Secure.getUriFor("location_mode"),
                true,
                object : ContentObserver(handler) {
                    override fun onChange(selfChange: Boolean) {
                        _isLocationEnabled.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            locationManager?.isLocationEnabled == true
                        } else {
                            locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                            locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
                        }
                    }
                }
            )
        } catch (e: Exception) {}

        // Observer for Battery Saver
        try {
            contentResolver.registerContentObserver(
                Settings.Global.getUriFor("low_power"),
                true,
                object : ContentObserver(handler) {
                    override fun onChange(selfChange: Boolean) {
                        _isBatterySaverEnabled.value = powerManager?.isPowerSaveMode == true ||
                                Settings.Global.getInt(contentResolver, "low_power", 0) == 1
                    }
                }
            )
        } catch (e: Exception) {
            // Ignore if setting is restricted
        }

        // Observer for Dark Theme / Night Mode
        try {
            contentResolver.registerContentObserver(
                Settings.Secure.getUriFor("ui_night_mode"),
                true,
                object : ContentObserver(handler) {
                    override fun onChange(selfChange: Boolean) {
                        if (System.currentTimeMillis() < ignoreDarkModeObserverUntil) {
                            return
                        }
                        _isDarkThemeEnabled.value = checkDarkThemeState()
                    }
                }
            )
        } catch (e: Exception) {
            Log.w("NativeTileManager", "ui_night_mode observer error: ${e.message}")
        }

        // Observer for Always-On Display (AOD)
        try {
            contentResolver.registerContentObserver(
                Settings.Secure.getUriFor("doze_always_on"),
                true,
                object : ContentObserver(handler) {
                    override fun onChange(selfChange: Boolean) {
                        val enabled = Settings.Secure.getInt(contentResolver, "doze_always_on", 0) == 1
                        _isAodEnabled.value = enabled
                    }
                }
            )
        } catch (e: Exception) {
            Log.w("NativeTileManager", "doze_always_on observer error: ${e.message}")
        }

        // Observer for Ambient Display
        try {
            contentResolver.registerContentObserver(
                Settings.Secure.getUriFor("doze_enabled"),
                true,
                object : ContentObserver(handler) {
                    override fun onChange(selfChange: Boolean) {
                        val enabled = Settings.Secure.getInt(contentResolver, "doze_enabled", 0) == 1
                        _isAmbientDisplayEnabled.value = enabled
                    }
                }
            )
        } catch (e: Exception) {
            Log.w("NativeTileManager", "doze_enabled observer error: ${e.message}")
        }

        // Observer for Caffeine (Keep Screen Awake)
        try {
            contentResolver.registerContentObserver(
                Settings.System.getUriFor("screen_off_timeout"),
                true,
                object : ContentObserver(handler) {
                    override fun onChange(selfChange: Boolean) {
                        val t = Settings.System.getInt(contentResolver, "screen_off_timeout", 30000)
                        _isCaffeineEnabled.value = t >= 600000 || t == 2147483647
                    }
                }
            )
        } catch (e: Exception) {
            Log.w("NativeTileManager", "screen_off_timeout observer error: ${e.message}")
        }

        // Observer for Heads Up Notifications
        try {
            contentResolver.registerContentObserver(
                Settings.Global.getUriFor("heads_up_notifications_enabled"),
                true,
                object : ContentObserver(handler) {
                    override fun onChange(selfChange: Boolean) {
                        val enabled = Settings.Global.getInt(contentResolver, "heads_up_notifications_enabled", 1) == 1
                        _isHeadsUpEnabled.value = enabled
                    }
                }
            )
        } catch (e: Exception) {}

        // Network Callback
        connectivityManager?.let { cm ->
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    val caps = cm.getNetworkCapabilities(network)
                    if (caps != null) {
                        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            _isWifiEnabled.value = true
                        }
                        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            _isCellularEnabled.value = true
                        }
                    }
                }
                override fun onLost(network: Network) {
                    _isWifiEnabled.value = wifiManager?.isWifiEnabled == true
                }
            })
        }

        // Comprehensive BroadcastReceiver for real-time system state synchronization
        val filter = IntentFilter().apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
            addAction(NotificationManager.ACTION_NOTIFICATION_POLICY_CHANGED)
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            addAction("android.location.MODE_CHANGED")
            addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
            addAction(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            addAction(Intent.ACTION_CONFIGURATION_CHANGED)
            addAction("android.media.VOLUME_CHANGED_ACTION")
            addAction("android.media.RINGER_MODE_CHANGED")
            addAction("android.net.wifi.WIFI_AP_STATE_CHANGED")
            addAction("android.net.conn.TETHER_STATE_CHANGED")
            addAction("android.intent.action.ANY_DATA_STATE")
        }
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                when (intent?.action) {
                    WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                        val state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
                        if (state == WifiManager.WIFI_STATE_ENABLED) {
                            _isWifiEnabled.value = true
                        } else if (state == WifiManager.WIFI_STATE_DISABLED) {
                            _isWifiEnabled.value = false
                        } else {
                            _isWifiEnabled.value = wifiManager?.isWifiEnabled == true
                        }
                    }
                    WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
                        _isWifiEnabled.value = wifiManager?.isWifiEnabled == true
                    }
                    BluetoothAdapter.ACTION_STATE_CHANGED -> {
                        val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                        if (state == BluetoothAdapter.STATE_ON) {
                            _isBluetoothEnabled.value = true
                        } else if (state == BluetoothAdapter.STATE_OFF) {
                            _isBluetoothEnabled.value = false
                        } else {
                            _isBluetoothEnabled.value = bluetoothManager?.adapter?.isEnabled == true
                        }
                    }
                    NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED,
                    NotificationManager.ACTION_NOTIFICATION_POLICY_CHANGED -> {
                        val filterVal = notificationManager?.currentInterruptionFilter ?: NotificationManager.INTERRUPTION_FILTER_ALL
                        _isDndEnabled.value = filterVal != NotificationManager.INTERRUPTION_FILTER_ALL
                    }
                    LocationManager.PROVIDERS_CHANGED_ACTION,
                    "android.location.MODE_CHANGED" -> {
                        _isLocationEnabled.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            locationManager?.isLocationEnabled == true
                        } else {
                            locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                            locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
                        }
                    }
                    PowerManager.ACTION_POWER_SAVE_MODE_CHANGED -> {
                        _isBatterySaverEnabled.value = powerManager?.isPowerSaveMode == true ||
                                Settings.Global.getInt(contentResolver, "low_power", 0) == 1
                    }
                    NfcAdapter.ACTION_ADAPTER_STATE_CHANGED -> {
                        val state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF)
                        _isNfcEnabled.value = (state == NfcAdapter.STATE_ON) || nfcAdapter?.isEnabled == true
                    }
                    Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                        _isAirplaneModeEnabled.value = intent.getBooleanExtra("state", Settings.Global.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1)
                    }
                    Intent.ACTION_CONFIGURATION_CHANGED -> {
                        if (System.currentTimeMillis() >= ignoreDarkModeObserverUntil) {
                            _isDarkThemeEnabled.value = checkDarkThemeState()
                        }
                    }
                    "android.media.VOLUME_CHANGED_ACTION",
                    "android.media.RINGER_MODE_CHANGED" -> {
                        refreshAllStates()
                    }
                    "android.net.wifi.WIFI_AP_STATE_CHANGED" -> {
                        val apState = intent.getIntExtra("wifi_state", 11)
                        // 13 = WIFI_AP_STATE_ENABLED, 12 = WIFI_AP_STATE_ENABLING
                        _isHotspotEnabled.value = (apState == 13 || apState == 12)
                    }
                    "android.intent.action.ANY_DATA_STATE" -> {
                        _isCellularEnabled.value = isMobileDataActive(context)
                    }
                    else -> {
                        refreshAllStates()
                    }
                }
            }
        }, filter)
    }

    fun getTileActiveState(tileId: String): Boolean {
        return when (tileId) {
            "wifi" -> _isWifiEnabled.value
            "bluetooth" -> _isBluetoothEnabled.value
            "torch" -> _isTorchEnabled.value
            "autorotate" -> _isAutoRotateEnabled.value
            "dnd" -> _isDndEnabled.value
            "airplane" -> _isAirplaneModeEnabled.value
            "location" -> _isLocationEnabled.value
            "cellular" -> _isCellularEnabled.value
            "hotspot" -> _isHotspotEnabled.value
            "battery_saver" -> _isBatterySaverEnabled.value
            "nfc" -> _isNfcEnabled.value
            "dark_mode" -> _isDarkThemeEnabled.value
            "aod" -> _isAodEnabled.value
            "ambient_display" -> _isAmbientDisplayEnabled.value
            "caffeine" -> _isCaffeineEnabled.value
            "heads_up" -> _isHeadsUpEnabled.value
            "screenrecord" -> com.example.service.ScreenRecordService.isRecording.value
            else -> _customRomTileStates.value[tileId] ?: false
        }
    }

    /**
     * Universal System Tile Interoperability Execution Protocol:
     * 1. Universal Accessibility Click Simulation on stock SystemUI node.
     * 2. Direct Command & Intent Routing (Shizuku & Root Engine).
     * 3. Native SDK fallback execution.
     * 4. Automatic State Mirroring.
     */
    fun handleUniversalTileClick(tile: QsTileModel) {
        val specifier = getTileSpecifier(tile.id)

        scope.launch {
            // Check for tile custom command overrides
            val customConfig = com.example.data.TileCommandRepository.getConfig(tile.id)
            val fallbackIntent = getTileFallbackIntent(tile.id)

            if (customConfig.isOverrideEnabled) {
                val isCurrentlyActive = getTileActiveState(tile.id)
                val targetCmd = if (isCurrentlyActive) customConfig.commandOff else customConfig.commandOn

                if (customConfig.executionMode == com.example.model.CommandExecutionMode.STANDARD) {
                    if (customConfig.customIntentAction.isNotBlank() || customConfig.customIntentPackage.isNotBlank() || customConfig.customIntentComponent.isNotBlank()) {
                        dispatchDirectStandardIntent(customConfig.customIntentAction, customConfig.customIntentPackage, customConfig.customIntentComponent)
                    } else {
                        executeNativeSdkToggle(tile.id)
                        fallbackIntent?.let {
                            try {
                                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(it)
                            } catch (e: Exception) {}
                        }
                    }
                } else {
                    if (targetCmd.isNotBlank()) {
                        privilegeEngine.executeCommandWithMode(
                            shellCommand = targetCmd,
                            mode = customConfig.executionMode,
                            fallbackIntent = fallbackIntent
                        )
                    } else {
                        val shellCmd = getTileShellCommand(tile.id, specifier)
                        if (shellCmd != null) {
                            privilegeEngine.executeCommandWithMode(
                                shellCommand = shellCmd,
                                mode = customConfig.executionMode,
                                fallbackIntent = fallbackIntent
                            )
                        }
                    }
                }
                refreshAllStates()
                return@launch
            }

            // Standard vs Shizuku vs Root SU per-tile execution mode for native tiles:
            if (customConfig.executionMode == com.example.model.CommandExecutionMode.STANDARD) {
                executeNativeSdkToggle(tile.id)
            } else {
                val shellCmd = getTileShellCommand(tile.id, specifier)
                if (shellCmd != null) {
                    privilegeEngine.executeCommandWithMode(
                        shellCommand = shellCmd,
                        mode = customConfig.executionMode,
                        fallbackIntent = fallbackIntent
                    )
                } else {
                    executeNativeSdkToggle(tile.id)
                }
            }

            refreshAllStates()
        }
    }

    private fun dispatchDirectStandardIntent(action: String, targetPackage: String, targetComponent: String) {
        try {
            val intent = Intent().apply {
                if (action.isNotBlank()) this.action = action
                if (targetPackage.isNotBlank()) setPackage(targetPackage)
                if (targetComponent.isNotBlank()) {
                    val pkg = if (targetComponent.contains("/")) targetComponent.substringBefore('/') else targetPackage
                    val cls = if (targetComponent.contains("/")) targetComponent.substringAfter('/') else targetComponent
                    val fullCls = if (cls.startsWith(".")) "$pkg$cls" else cls
                    if (pkg.isNotBlank() && fullCls.isNotBlank()) {
                        setClassName(pkg, fullCls)
                    }
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val broadcastIntent = Intent().apply {
                    if (action.isNotBlank()) this.action = action
                    if (targetPackage.isNotBlank()) setPackage(targetPackage)
                }
                context.sendBroadcast(broadcastIntent)
            } catch (ex: Exception) {}
        }
    }

    private fun executeNativeSdkToggle(tileId: String) {
        when (tileId) {
            "wifi" -> toggleWifi()
            "bluetooth" -> toggleBluetooth()
            "torch" -> toggleTorch()
            "autorotate" -> toggleAutoRotate()
            "dnd" -> toggleDnd()
            "airplane" -> toggleAirplaneMode()
            "location" -> toggleLocation()
            "cellular" -> toggleCellular()
            "hotspot" -> toggleHotspot()
            "battery_saver" -> toggleBatterySaver()
            "nfc" -> toggleNfc()
            "dark_mode" -> toggleDarkMode()
            "aod" -> toggleAod()
            "ambient_display" -> toggleAmbientDisplay()
            "caffeine" -> toggleCaffeine()
            "heads_up" -> toggleHeadsUp()
            "screenrecord" -> toggleScreenRecord()
            else -> {
                // Custom ROM / System tile fallback: toggle local state & launch settings intent
                toggleCustomRomState(tileId)
                getTileFallbackIntent(tileId)?.let { intent ->
                    try {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
            }
        }
    }

    private fun toggleCustomRomState(tileId: String) {
        val current = _customRomTileStates.value.toMutableMap()
        val currentVal = current[tileId] ?: false
        current[tileId] = !currentVal
        _customRomTileStates.value = current
    }

    private fun getTileSearchKeywords(tileId: String): List<String> {
        return when (tileId) {
            "wifi" -> listOf("wifi", "wi-fi", "internet", "wlan", "network")
            "bluetooth" -> listOf("bluetooth", "bt", "devices")
            "torch" -> listOf("flashlight", "torch", "flash")
            "autorotate" -> listOf("auto-rotate", "autorotate", "rotation", "orientation")
            "dnd" -> listOf("do not disturb", "dnd", "zen", "priority")
            "airplane" -> listOf("airplane", "flight", "aeroplane")
            "location" -> listOf("location", "gps")
            "cellular" -> listOf("mobile data", "data", "cellular", "carrier")
            "hotspot" -> listOf("hotspot", "tethering", "portable hotspot")
            "battery_saver" -> listOf("battery saver", "power saving", "low power")
            "nfc" -> listOf("nfc", "contactless")
            "dark_mode" -> listOf("dark theme", "dark mode", "night mode")
            // Custom ROM keywords (crDroid, LineageOS, PixelExperience, etc.)
            "caffeine" -> listOf("caffeine", "stay awake", "screen timeout")
            "heads_up" -> listOf("heads up", "floating notifications", "peeking")
            "aod" -> listOf("always on", "aod", "ambient display")
            "sound_search" -> listOf("sound search", "music recognition", "identify music", "shazam")
            "cpu_info" -> listOf("cpu info", "cpu overlay", "processor info")
            "fps_info" -> listOf("fps info", "framerate", "fps overlay")
            "reading_mode" -> listOf("reading mode", "livedisplay", "night light", "eye comfort")
            "sound_profile" -> listOf("sound profile", "ringer", "vibrate", "silent")
            "screenshot" -> listOf("screenshot", "screen capture", "capture")
            "screenrecord" -> listOf("screen record", "screen recorder", "record screen")
            else -> listOf(tileId)
        }
    }

    private fun getTileSpecifier(tileId: String): String {
        return when (tileId) {
            "wifi" -> "wifi"
            "bluetooth" -> "bt"
            "torch" -> "flashlight"
            "autorotate" -> "rotation"
            "dnd" -> "dnd"
            "airplane" -> "airplane"
            "location" -> "location"
            "cellular" -> "cell"
            "hotspot" -> "hotspot"
            "battery_saver" -> "battery"
            "nfc" -> "nfc"
            "dark_mode" -> "dark"
            "caffeine" -> "caffeine"
            "heads_up" -> "heads_up"
            "aod" -> "ambient_display"
            "sound_search" -> "sound_search"
            "cpu_info" -> "cpuinfo"
            "fps_info" -> "fpsinfo"
            "reading_mode" -> "livedisplay"
            "sound_profile" -> "volume"
            "screenshot" -> "screenshot"
            "screenrecord" -> "screenrecord"
            else -> tileId
        }
    }

    private fun getTileShellCommand(tileId: String, specifier: String): String? {
        return when (tileId) {
            "wifi" -> if (!_isWifiEnabled.value) "svc wifi enable" else "svc wifi disable"
            "bluetooth" -> if (!_isBluetoothEnabled.value) "cmd bluetooth_manager enable" else "cmd bluetooth_manager disable"
            "cellular" -> if (!_isCellularEnabled.value) "svc data enable" else "svc data disable"
            "airplane" -> if (!_isAirplaneModeEnabled.value) "cmd connectivity airplane-mode enable" else "cmd connectivity airplane-mode disable"
            "battery_saver" -> if (!_isBatterySaverEnabled.value) "cmd power set-mode 1" else "cmd power set-mode 0"
            "nfc" -> if (!_isNfcEnabled.value) "svc nfc enable" else "svc nfc disable"
            "autorotate" -> "settings put system accelerometer_rotation ${if (_isAutoRotateEnabled.value) 0 else 1}"
            "location" -> "settings put secure location_mode ${if (_isLocationEnabled.value) 0 else 3}"
            "hotspot" -> if (!_isHotspotEnabled.value) "cmd connectivity tether startwifi" else "cmd connectivity tether stopwifi"
            "aod" -> if (!_isAodEnabled.value) "settings put secure doze_always_on 1" else "settings put secure doze_always_on 0"
            "ambient_display" -> if (!_isAmbientDisplayEnabled.value) "settings put secure doze_enabled 1" else "settings put secure doze_enabled 0"
            "caffeine" -> if (!_isCaffeineEnabled.value) "settings put system screen_off_timeout 2147483647" else "settings put system screen_off_timeout ${defaultCaffeineTimeout}"
            // Universal custom ROM & statusbar click-tile invocation
            else -> "cmd statusbar click-tile $specifier"
        }
    }

    private fun getTileFallbackIntent(tileId: String): Intent? {
        return when (tileId) {
            "wifi" -> Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
            "bluetooth" -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            "cellular" -> Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)
            "airplane" -> Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
            "location" -> Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            "hotspot" -> Intent("android.settings.TETHER_SETTINGS")
            "autorotate", "dark_mode" -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
            "dnd" -> Intent(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS)
            "battery_saver" -> Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
            "nfc" -> Intent(Settings.ACTION_NFC_SETTINGS)
            "reading_mode" -> Intent(Settings.ACTION_NIGHT_DISPLAY_SETTINGS)
            "sound_profile" -> Intent(Settings.ACTION_SOUND_SETTINGS)
            "aod" -> Intent("android.settings.LOCK_SCREEN_SETTINGS")
            "ambient_display", "caffeine" -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
            "heads_up" -> Intent("android.settings.NOTIFICATION_SETTINGS")
            else -> Intent(Settings.ACTION_SETTINGS)
        }
    }

    // Direct Toggles
    fun toggleWifi() {
        val target = !_isWifiEnabled.value
        _isWifiEnabled.value = target
        scope.launch {
            val cmd = if (target) "svc wifi enable" else "svc wifi disable"
            val fallback = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
            privilegeEngine.executeCommand(cmd, fallback)
        }
    }

    fun toggleBluetooth() {
        val target = !_isBluetoothEnabled.value
        _isBluetoothEnabled.value = target
        scope.launch {
            val cmd = if (target) "cmd bluetooth_manager enable" else "cmd bluetooth_manager disable"
            val fallback = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            privilegeEngine.executeCommand(cmd, fallback)
        }
    }

    fun toggleCellular() {
        val target = !_isCellularEnabled.value
        _isCellularEnabled.value = target
        scope.launch {
            val cmd = if (target) "svc data enable" else "svc data disable"
            val fallback = Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)
            privilegeEngine.executeCommand(cmd, fallback)
        }
    }

    fun toggleTorch() {
        val target = !_isTorchEnabled.value
        _isTorchEnabled.value = target
        try {
            torchCameraId?.let { id ->
                cameraManager?.setTorchMode(id, target)
            }
        } catch (e: Exception) {
            Log.w("NativeTileManager", "Torch toggle error: ${e.message}")
        }
    }

    fun toggleAutoRotate() {
        val target = if (_isAutoRotateEnabled.value) 0 else 1
        _isAutoRotateEnabled.value = target == 1
        if (privilegeEngine.hasWriteSettingsPermission()) {
            try {
                Settings.System.putInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, target)
            } catch (e: Exception) {
                // Ignore
            }
        } else {
            scope.launch {
                val cmd = "settings put system accelerometer_rotation $target"
                val fallback = Intent(Settings.ACTION_DISPLAY_SETTINGS)
                privilegeEngine.executeCommand(cmd, fallback)
            }
        }
    }

    fun toggleDnd() {
        val target = !_isDndEnabled.value
        _isDndEnabled.value = target
        if (privilegeEngine.hasNotificationPolicyPermission()) {
            val filterVal = if (target) {
                NotificationManager.INTERRUPTION_FILTER_PRIORITY
            } else {
                NotificationManager.INTERRUPTION_FILTER_ALL
            }
            try {
                notificationManager?.setInterruptionFilter(filterVal)
            } catch (e: Exception) {
                // Ignore
            }
        } else {
            privilegeEngine.requestNotificationPolicyPermission()
        }
    }

    fun toggleAirplaneMode() {
        val target = if (_isAirplaneModeEnabled.value) 0 else 1
        _isAirplaneModeEnabled.value = target == 1
        scope.launch {
            val cmd = if (target == 1) "cmd connectivity airplane-mode enable" else "cmd connectivity airplane-mode disable"
            val fallback = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
            privilegeEngine.executeCommand(cmd, fallback)
        }
    }

    fun toggleLocation() {
        val target = !_isLocationEnabled.value
        _isLocationEnabled.value = target
        scope.launch {
            val cmd = if (target) "settings put secure location_mode 3" else "settings put secure location_mode 0"
            val fallback = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            privilegeEngine.executeCommand(cmd, fallback)
        }
    }

    fun setHotspotEnabledAndroid16(enable: Boolean) {
        try {
            val tetheringManager = context.getSystemService("tethering")
            if (tetheringManager != null) {
                if (enable) {
                    // Use Reflection to call startTethering on TetheringManager
                    val requestClass = Class.forName("android.net.TetheringManager\$TetheringRequest\$Builder")
                    val builderConstructor = requestClass.getConstructor(Int::class.javaPrimitiveType)
                    val builder = builderConstructor.newInstance(0) // TETHERING_WIFI = 0
                    
                    val setShouldShowUiMethod = requestClass.getDeclaredMethod("setShouldShowUi", Boolean::class.javaPrimitiveType)
                    setShouldShowUiMethod.invoke(builder, false)
                    
                    val buildMethod = requestClass.getDeclaredMethod("build")
                    val request = buildMethod.invoke(builder)

                    val executor = java.util.concurrent.Executors.newSingleThreadExecutor()
                    
                    val callbackClass = Class.forName("android.net.TetheringManager\$StartTetheringCallback")
                    val proxyCallback = java.lang.reflect.Proxy.newProxyInstance(
                        callbackClass.classLoader,
                        arrayOf(callbackClass)
                    ) { _, _, _ -> null }

                    val startTetheringMethod = tetheringManager.javaClass.getDeclaredMethod(
                        "startTethering",
                        Class.forName("android.net.TetheringManager\$TetheringRequest"),
                        java.util.concurrent.Executor::class.java,
                        callbackClass
                    )
                    startTetheringMethod.invoke(tetheringManager, request, executor, proxyCallback)
                } else {
                    val stopTetheringMethod = tetheringManager.javaClass.getDeclaredMethod("stopTethering", Int::class.javaPrimitiveType)
                    stopTetheringMethod.invoke(tetheringManager, 0) // TETHERING_WIFI = 0
                }
            } else {
                throw Exception("TetheringManager is null")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to Shell command if Reflection fails
            val cmd = if (enable) "cmd tethering startTethering 0 false" else "cmd tethering stopTethering 0"
            scope.launch {
                privilegeEngine.executeCommand(cmd, Intent("android.settings.TETHER_SETTINGS"))
            }
        }
    }

    fun toggleHotspot() {
        val target = !_isHotspotEnabled.value
        _isHotspotEnabled.value = target
        scope.launch {
            val currentMode = privilegeEngine.currentMode.value
            if (currentMode == PrivilegeMode.ROOT || currentMode == PrivilegeMode.SHIZUKU) {
                setHotspotEnabledAndroid16(target)
            } else {
                val intent = Intent("android.settings.TETHER_SETTINGS").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    val fallbackIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try { context.startActivity(fallbackIntent) } catch (ex: Exception) {}
                }
            }
        }
    }

    fun toggleBatterySaver() {
        val target = !_isBatterySaverEnabled.value
        _isBatterySaverEnabled.value = target
        scope.launch {
            val cmd = if (target) "cmd power set-mode 1" else "cmd power set-mode 0"
            val fallback = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
            privilegeEngine.executeCommand(cmd, fallback)
        }
    }

    fun toggleNfc() {
        val target = !_isNfcEnabled.value
        _isNfcEnabled.value = target
        scope.launch {
            val cmd = if (target) "svc nfc enable" else "svc nfc disable"
            val fallback = Intent(Settings.ACTION_NFC_SETTINGS)
            privilegeEngine.executeCommand(cmd, fallback)
        }
    }

    fun toggleDarkMode() {
        val target = !_isDarkThemeEnabled.value
        _isDarkThemeEnabled.value = target
        ignoreDarkModeObserverUntil = System.currentTimeMillis() + 2500L
        scope.launch {
            val currentMode = privilegeEngine.currentMode.value
            if (currentMode == PrivilegeMode.ROOT || currentMode == PrivilegeMode.SHIZUKU) {
                val cmd = if (target) "cmd uimode night yes" else "cmd uimode night no"
                val fallback = Intent(Settings.ACTION_DISPLAY_SETTINGS)
                privilegeEngine.executeCommand(cmd, fallback)
            } else {
                val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? android.app.UiModeManager
                if (uiModeManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val mode = if (target) android.app.UiModeManager.MODE_NIGHT_YES else android.app.UiModeManager.MODE_NIGHT_NO
                    try {
                        uiModeManager.setApplicationNightMode(mode)
                    } catch (e: Exception) {
                        val fallback = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                        try { context.startActivity(fallback) } catch (ex: Exception) {}
                    }
                } else {
                    val fallback = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    try { context.startActivity(fallback) } catch (ex: Exception) {}
                }
            }
        }
    }

    fun toggleAod() {
        val target = !_isAodEnabled.value
        _isAodEnabled.value = target
        scope.launch {
            val currentMode = privilegeEngine.currentMode.value
            if (currentMode == PrivilegeMode.ROOT || currentMode == PrivilegeMode.SHIZUKU) {
                val cmd = if (target) "settings put secure doze_always_on 1" else "settings put secure doze_always_on 0"
                val fallback = Intent("android.settings.LOCK_SCREEN_SETTINGS")
                privilegeEngine.executeCommand(cmd, fallback)
            } else {
                val fallback = Intent("android.settings.LOCK_SCREEN_SETTINGS").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                try { context.startActivity(fallback) } catch (ex: Exception) {
                    val displayFallback = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    try { context.startActivity(displayFallback) } catch (ex2: Exception) {}
                }
            }
        }
    }

    fun toggleAmbientDisplay() {
        val target = !_isAmbientDisplayEnabled.value
        _isAmbientDisplayEnabled.value = target
        scope.launch {
            val currentMode = privilegeEngine.currentMode.value
            if (currentMode == PrivilegeMode.ROOT || currentMode == PrivilegeMode.SHIZUKU) {
                val cmd = if (target) "settings put secure doze_enabled 1" else "settings put secure doze_enabled 0"
                val fallback = Intent(Settings.ACTION_DISPLAY_SETTINGS)
                privilegeEngine.executeCommand(cmd, fallback)
            } else {
                val fallback = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                try { context.startActivity(fallback) } catch (ex: Exception) {}
            }
        }
    }

    fun toggleCaffeine() {
        val currentTimeout = Settings.System.getInt(contentResolver, "screen_off_timeout", 30000)
        val target = !_isCaffeineEnabled.value
        _isCaffeineEnabled.value = target
        scope.launch {
            if (target) {
                if (currentTimeout != 2147483647) {
                    defaultCaffeineTimeout = currentTimeout
                }
                val currentMode = privilegeEngine.currentMode.value
                if (currentMode == PrivilegeMode.ROOT || currentMode == PrivilegeMode.SHIZUKU) {
                    privilegeEngine.executeCommand("settings put system screen_off_timeout 2147483647")
                } else if (privilegeEngine.hasWriteSettingsPermission()) {
                    try {
                        Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 2147483647)
                    } catch (e: Exception) {
                        val fallback = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                        try { context.startActivity(fallback) } catch (ex: Exception) {}
                    }
                } else {
                    val fallback = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    try { context.startActivity(fallback) } catch (ex: Exception) {}
                }
            } else {
                val restoreVal = defaultCaffeineTimeout
                val currentMode = privilegeEngine.currentMode.value
                if (currentMode == PrivilegeMode.ROOT || currentMode == PrivilegeMode.SHIZUKU) {
                    privilegeEngine.executeCommand("settings put system screen_off_timeout $restoreVal")
                } else if (privilegeEngine.hasWriteSettingsPermission()) {
                    try {
                        Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, restoreVal)
                    } catch (e: Exception) {
                        val fallback = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                        try { context.startActivity(fallback) } catch (ex: Exception) {}
                    }
                } else {
                    val fallback = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    try { context.startActivity(fallback) } catch (ex: Exception) {}
                }
            }
        }
    }

    fun toggleHeadsUp() {
        val target = !_isHeadsUpEnabled.value
        setHeadsUpEnabled(target)
    }

    fun setHeadsUpEnabled(enabled: Boolean) {
        _isHeadsUpEnabled.value = enabled
        val valInt = if (enabled) 1 else 0
        try {
            Settings.Global.putInt(contentResolver, "heads_up_notifications_enabled", valInt)
        } catch (_: Exception) {}

        scope.launch {
            if (privilegeEngine.currentMode.value != PrivilegeMode.STANDARD) {
                val cmd = "settings put global heads_up_notifications_enabled $valInt"
                privilegeEngine.executeCommand(cmd)
            }
            kotlinx.coroutines.delay(300)
            refreshAllStates()
        }
    }

    fun setBrightness(value: Int) {
        val clamped = value.coerceIn(5, 255)
        _brightness.value = clamped
        if (privilegeEngine.hasWriteSettingsPermission()) {
            try {
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, clamped)
            } catch (e: Exception) {
                Log.w("NativeTileManager", "Set brightness failed: ${e.message}")
            }
        } else {
            scope.launch {
                privilegeEngine.executeCommand("settings put system screen_brightness $clamped")
            }
        }
    }

    fun setStreamVolume(streamType: Int, fraction: Float) {
        audioManager?.let { am ->
            val max = am.getStreamMaxVolume(streamType)
            val index = (fraction * max).toInt().coerceIn(0, max)
            am.setStreamVolume(streamType, index, 0)
            when (streamType) {
                AudioManager.STREAM_MUSIC -> _mediaVolume.value = fraction
                AudioManager.STREAM_RING -> _ringVolume.value = fraction
                AudioManager.STREAM_ALARM -> _alarmVolume.value = fraction
                AudioManager.STREAM_NOTIFICATION -> _notificationVolume.value = fraction
            }
        }
    }

    fun toggleScreenRecord() {
        if (com.example.service.ScreenRecordService.isRecording.value) {
            com.example.service.ScreenRecordService.stopRecording(context)
        } else {
            com.example.service.ScreenRecordPromptActivity.start(context)
        }
    }
}
