package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_blacklist_datastore")

object NotificationBlacklistManager {
    private const val PREFS_NAME = "notification_blacklist_prefs"
    private const val KEY_HIDDEN_PACKAGES_PREF = "hidden_packages"
    private const val KEY_HIDDEN_CHANNELS_PREF = "hidden_channels"

    private val DS_KEY_HIDDEN_PACKAGES = stringSetPreferencesKey("hidden_packages")
    private val DS_KEY_HIDDEN_CHANNELS = stringSetPreferencesKey("hidden_channels")

    private var prefs: SharedPreferences? = null
    private var appContext: Context? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _hiddenPackages = MutableStateFlow<Set<String>>(emptySet())
    val hiddenPackages: StateFlow<Set<String>> = _hiddenPackages.asStateFlow()

    private val _hiddenChannels = MutableStateFlow<Set<String>>(emptySet())
    val hiddenChannels: StateFlow<Set<String>> = _hiddenChannels.asStateFlow()

    fun init(context: Context) {
        if (appContext == null) {
            val app = context.applicationContext
            appContext = app
            val p = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs = p
            
            // Sync initial state from SharedPreferences (fast synchronous fallback)
            _hiddenPackages.value = p.getStringSet(KEY_HIDDEN_PACKAGES_PREF, emptySet()) ?: emptySet()
            _hiddenChannels.value = p.getStringSet(KEY_HIDDEN_CHANNELS_PREF, emptySet()) ?: emptySet()

            // Asynchronously load and synchronize from Jetpack DataStore
            scope.launch {
                try {
                    val preferences = app.dataStore.data.firstOrNull()
                    if (preferences != null) {
                        val dsPkgs = preferences[DS_KEY_HIDDEN_PACKAGES]
                        val dsChs = preferences[DS_KEY_HIDDEN_CHANNELS]
                        
                        if (dsPkgs != null) {
                            _hiddenPackages.value = dsPkgs
                            // Sync back to SharedPreferences
                            p.edit().putStringSet(KEY_HIDDEN_PACKAGES_PREF, dsPkgs).apply()
                        }
                        if (dsChs != null) {
                            _hiddenChannels.value = dsChs
                            // Sync back to SharedPreferences
                            p.edit().putStringSet(KEY_HIDDEN_CHANNELS_PREF, dsChs).apply()
                        }
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    fun isBlacklisted(packageName: String, channelId: String? = null): Boolean {
        if (_hiddenPackages.value.contains(packageName)) return true
        if (channelId != null && _hiddenChannels.value.contains(channelId)) return true
        return false
    }

    fun hidePackage(packageName: String, context: Context? = null) {
        val ctx = context?.applicationContext ?: appContext
        if (prefs == null && ctx != null) init(ctx)
        
        val current = _hiddenPackages.value.toMutableSet()
        if (current.add(packageName)) {
            _hiddenPackages.value = current
            prefs?.edit()?.putStringSet(KEY_HIDDEN_PACKAGES_PREF, current)?.apply()
            
            ctx?.let { c ->
                scope.launch {
                    try {
                        c.dataStore.edit { preferences ->
                            preferences[DS_KEY_HIDDEN_PACKAGES] = current
                        }
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
            }
        }
    }

    fun unhidePackage(packageName: String, context: Context? = null) {
        val ctx = context?.applicationContext ?: appContext
        if (prefs == null && ctx != null) init(ctx)
        
        val current = _hiddenPackages.value.toMutableSet()
        if (current.remove(packageName)) {
            _hiddenPackages.value = current
            prefs?.edit()?.putStringSet(KEY_HIDDEN_PACKAGES_PREF, current)?.apply()
            
            ctx?.let { c ->
                scope.launch {
                    try {
                        c.dataStore.edit { preferences ->
                            preferences[DS_KEY_HIDDEN_PACKAGES] = current
                        }
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
            }
        }
    }

    fun hideChannel(channelId: String, context: Context? = null) {
        val ctx = context?.applicationContext ?: appContext
        if (prefs == null && ctx != null) init(ctx)
        
        val current = _hiddenChannels.value.toMutableSet()
        if (current.add(channelId)) {
            _hiddenChannels.value = current
            prefs?.edit()?.putStringSet(KEY_HIDDEN_CHANNELS_PREF, current)?.apply()
            
            ctx?.let { c ->
                scope.launch {
                    try {
                        c.dataStore.edit { preferences ->
                            preferences[DS_KEY_HIDDEN_CHANNELS] = current
                        }
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
            }
        }
    }

    fun unhideChannel(channelId: String, context: Context? = null) {
        val ctx = context?.applicationContext ?: appContext
        if (prefs == null && ctx != null) init(ctx)
        
        val current = _hiddenChannels.value.toMutableSet()
        if (current.remove(channelId)) {
            _hiddenChannels.value = current
            prefs?.edit()?.putStringSet(KEY_HIDDEN_CHANNELS_PREF, current)?.apply()
            
            ctx?.let { c ->
                scope.launch {
                    try {
                        c.dataStore.edit { preferences ->
                            preferences[DS_KEY_HIDDEN_CHANNELS] = current
                        }
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
            }
        }
    }

    fun clearAllBlacklist() {
        _hiddenPackages.value = emptySet()
        _hiddenChannels.value = emptySet()
        prefs?.edit()?.clear()?.apply()
        
        appContext?.let { c ->
            scope.launch {
                try {
                    c.dataStore.edit { preferences ->
                        preferences.clear()
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }
}
