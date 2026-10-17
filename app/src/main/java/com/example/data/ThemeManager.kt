package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Represents a user-saved settings theme profile.
 * Strictly user-authored: no preset themes are packaged or injected by default.
 */
data class ThemePreviewData(
    val themeMode: String = "system",
    val enabledTileShapeId: String = "circle",
    val disabledTileShapeId: String = "circle",
    val gridLayout: String = "3x2",
    val wavySliders: Boolean = false,
    val progressBarStyle: String = "default",
    val panelOuterCorner: Float = 20f,
    val panelInnerCorner: Float = 12f,
    val panelGlobalCornerEnabled: Boolean = false,
    val tileOutlineEnabled: Boolean = false,
    val showMonetSurface: Boolean = false,
    val showTileGradient: Boolean = false,
    val showCustomHeaderImage: Boolean = false,
    val customHeaderImageUri: String = "",
    val showCustomHeaderMonetGradient: Boolean = false,
    val customHeaderMonetGradientHeight: Int = 120,
    val headerGradientColorTertiary: Boolean = false,
    val slidersColorTertiary: Boolean = false,
    val qsTilesColorTertiary: Boolean = false,
    val surfaceBgColorTertiary: Boolean = false,
    val panelContainerColorTertiary: Boolean = false
) {
    val themeModeLabel: String
        get() = when (themeMode.lowercase()) {
            "dark" -> "Dark"
            "light" -> "Light"
            else -> "System"
        }

    val shapeLabel: String
        get() = enabledTileShapeId.replace("_", " ").replaceFirstChar { it.uppercase() }
}

data class SavedTheme(
    val id: String,
    val name: String,
    val timestamp: Long,
    val settingsCount: Int,
    val jsonPayload: String
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val previewData: ThemePreviewData
        get() {
            return try {
                val json = JSONObject(jsonPayload)
                val shape = json.optString("tile_shape", "circle")
                val enabledShape = json.optString("enabled_tile_shape", shape)
                val disabledShape = json.optString("disabled_tile_shape", shape)
                val globalCorner = json.optBoolean("panel_global_corner_enabled", false)
                val outerCorner = json.optDouble("panel_outer_corner", 20.0).toFloat()
                val innerCorner = if (globalCorner) outerCorner else json.optDouble("panel_inner_corner", 12.0).toFloat()
                ThemePreviewData(
                    themeMode = json.optString("theme_mode", "system"),
                    enabledTileShapeId = enabledShape,
                    disabledTileShapeId = disabledShape,
                    gridLayout = json.optString("qs_grid_layout", "3x2"),
                    wavySliders = json.optBoolean("wavy_progress_bars", false),
                    progressBarStyle = json.optString("progress_bar_style", "default"),
                    panelOuterCorner = outerCorner,
                    panelInnerCorner = innerCorner,
                    panelGlobalCornerEnabled = globalCorner,
                    tileOutlineEnabled = json.optBoolean("tile_outline_enabled", false),
                    showMonetSurface = json.optBoolean("show_monet_surface_background", false),
                    showTileGradient = json.optBoolean("show_qs_tile_gradient", false),
                    showCustomHeaderImage = json.optBoolean("show_custom_header_image", false),
                    customHeaderImageUri = json.optString("custom_header_image_uri", ""),
                    showCustomHeaderMonetGradient = json.optBoolean("show_custom_header_monet_gradient", false),
                    customHeaderMonetGradientHeight = json.optInt("custom_header_monet_gradient_height", 120),
                    headerGradientColorTertiary = json.optBoolean("elem_header_gradient_tertiary", false),
                    slidersColorTertiary = json.optBoolean("elem_sliders_tertiary", false),
                    qsTilesColorTertiary = json.optBoolean("elem_qs_tiles_tertiary", false),
                    surfaceBgColorTertiary = json.optBoolean("elem_surface_bg_tertiary", false),
                    panelContainerColorTertiary = json.optBoolean("elem_panel_container_tertiary", false)
                )
            } catch (_: Exception) {
                ThemePreviewData()
            }
        }
}

object ThemeManager {
    private const val PREFS_THEMES_STORE = "quickpanel_saved_themes_store"
    private const val KEY_THEMES_ARRAY = "saved_themes_json_array"

    // Set of keys to ignore when capturing settings snapshots (device-level/auth state)
    private val EXCLUDED_KEYS = setOf(
        "initial_setup_completed",
        "privilege_mode",
        "firebase_appcheck_debug_token",
        "saved_themes_json_array"
    )

    // Flow of saved themes - starts EMPTY, strictly NO preset themes
    private val _savedThemes = MutableStateFlow<List<SavedTheme>>(emptyList())
    val savedThemes: StateFlow<List<SavedTheme>> = _savedThemes.asStateFlow()

    private const val KEY_ACTIVE_THEME_ID = "active_theme_id"
    private val _activeThemeId = MutableStateFlow<String?>(null)
    val activeThemeId: StateFlow<String?> = _activeThemeId.asStateFlow()

    private var isInitialized = false

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_THEMES_STORE, Context.MODE_PRIVATE)
        val rawJson = prefs.getString(KEY_THEMES_ARRAY, null)
        _activeThemeId.value = prefs.getString(KEY_ACTIVE_THEME_ID, null)

        val list = mutableListOf<SavedTheme>()
        if (!rawJson.isNullOrBlank()) {
            try {
                val array = JSONArray(rawJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.optString("id", UUID.randomUUID().toString())
                    val name = obj.optString("name", "Untitled Theme")
                    val timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    val payload = obj.optString("payload", "{}")
                    val count = obj.optInt("count", 0)
                    list.add(
                        SavedTheme(
                            id = id,
                            name = name,
                            timestamp = timestamp,
                            settingsCount = count,
                            jsonPayload = payload
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _savedThemes.value = list
        isInitialized = true
    }

    /**
     * Saves the current customization settings snapshot into a new user theme.
     * No preset themes are created.
     */
    fun saveCurrentSettingsAsTheme(context: Context, themeName: String): SavedTheme {
        if (!isInitialized) init(context)

        val panelPrefs = context.getSharedPreferences(PanelPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val allSettings = panelPrefs.all

        val settingsJson = JSONObject()
        var count = 0

        for ((key, value) in allSettings) {
            if (key in EXCLUDED_KEYS) continue
            when (value) {
                is Boolean -> { settingsJson.put(key, value); count++ }
                is Int -> { settingsJson.put(key, value); count++ }
                is Long -> { settingsJson.put(key, value); count++ }
                is Float -> { settingsJson.put(key, value.toDouble()); count++ }
                is String -> { settingsJson.put(key, value); count++ }
                is Set<*> -> {
                    val arr = JSONArray()
                    for (item in value) {
                        arr.put(item.toString())
                    }
                    settingsJson.put(key, arr)
                    count++
                }
            }
        }

        val newTheme = SavedTheme(
            id = UUID.randomUUID().toString(),
            name = themeName.trim().ifEmpty { "Saved Theme" },
            timestamp = System.currentTimeMillis(),
            settingsCount = count,
            jsonPayload = settingsJson.toString()
        )

        val updated = _savedThemes.value.toMutableList()
        updated.add(0, newTheme)
        _savedThemes.value = updated
        persistThemes(context, updated)

        return newTheme
    }

    /**
     * Applies a saved theme's settings to PanelPreferences and triggers a reload.
     */
    fun applyTheme(context: Context, theme: SavedTheme): Boolean {
        return try {
            val settingsJson = JSONObject(theme.jsonPayload)
            val panelPrefs = context.getSharedPreferences(PanelPreferences.PREFS_NAME, Context.MODE_PRIVATE)
            val editor = panelPrefs.edit()

            val keys = settingsJson.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                if (key in EXCLUDED_KEYS) continue
                when (val value = settingsJson.get(key)) {
                    is Boolean -> editor.putBoolean(key, value)
                    is Number -> {
                        // Handle numbers robustly whether stored as Int, Long, or Float
                        val floatKeys = setOf(
                            "drag_handle_vertical_offset", "drag_handle_length", "animation_speed_scale",
                            "custom_header_image_height", "custom_header_image_opacity", "custom_header_monet_gradient_height",
                            "qs_tile_label_font_size", "qs_tile_subtitle_font_size", "wavy_thickness", "wavy_wobbliness",
                            "wavy_wavelength", "wavy_thumb_radius", "slider_inactive_track_thickness", "slider_corner_radius",
                            "a12_inactive_thickness", "a12_corner_radius", "wavy_inactive_thickness", "wavy_corner_radius",
                            "straight_thickness", "straight_inactive_thickness", "straight_corner_radius", "straight_thumb_radius",
                            "panel_outer_corner", "panel_inner_corner"
                        )
                        if (key in floatKeys || value is Double) {
                            editor.putFloat(key, value.toFloat())
                        } else if (value is Long) {
                            editor.putLong(key, value.toLong())
                        } else {
                            editor.putInt(key, value.toInt())
                        }
                    }
                    is String -> editor.putString(key, value)
                    is JSONArray -> {
                        val set = mutableSetOf<String>()
                        for (i in 0 until value.length()) {
                            set.add(value.getString(i))
                        }
                        editor.putStringSet(key, set)
                    }
                }
            }
            editor.apply()

            val prefs = context.getSharedPreferences(PREFS_THEMES_STORE, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_ACTIVE_THEME_ID, theme.id).apply()
            _activeThemeId.value = theme.id

            // Reload into all MutableStateFlow properties across the app
            PanelPreferences.init(context)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Updates an existing theme with current settings.
     */
    fun updateThemeWithCurrentSettings(context: Context, themeId: String): Boolean {
        val currentList = _savedThemes.value
        val index = currentList.indexOfFirst { it.id == themeId }
        if (index == -1) return false

        val existingTheme = currentList[index]
        val panelPrefs = context.getSharedPreferences(PanelPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val allSettings = panelPrefs.all
        val settingsJson = JSONObject()
        var count = 0

        for ((key, value) in allSettings) {
            if (key in EXCLUDED_KEYS) continue
            when (value) {
                is Boolean -> { settingsJson.put(key, value); count++ }
                is Int -> { settingsJson.put(key, value); count++ }
                is Long -> { settingsJson.put(key, value); count++ }
                is Float -> { settingsJson.put(key, value.toDouble()); count++ }
                is String -> { settingsJson.put(key, value); count++ }
                is Set<*> -> {
                    val arr = JSONArray()
                    for (item in value) {
                        arr.put(item.toString())
                    }
                    settingsJson.put(key, arr)
                    count++
                }
            }
        }

        val updatedTheme = existingTheme.copy(
            timestamp = System.currentTimeMillis(),
            settingsCount = count,
            jsonPayload = settingsJson.toString()
        )

        val updatedList = currentList.toMutableList()
        updatedList[index] = updatedTheme
        _savedThemes.value = updatedList
        persistThemes(context, updatedList)
        return true
    }

    /**
     * Renames an existing saved theme.
     */
    fun renameTheme(context: Context, themeId: String, newName: String): Boolean {
        val currentList = _savedThemes.value
        val index = currentList.indexOfFirst { it.id == themeId }
        if (index == -1) return false

        val updatedTheme = currentList[index].copy(name = newName.trim().ifEmpty { "Saved Theme" })
        val updatedList = currentList.toMutableList()
        updatedList[index] = updatedTheme
        _savedThemes.value = updatedList
        persistThemes(context, updatedList)
        return true
    }

    /**
     * Deletes a saved theme.
     */
    fun deleteTheme(context: Context, themeId: String): Boolean {
        val currentList = _savedThemes.value
        val updatedList = currentList.filter { it.id != themeId }
        if (updatedList.size == currentList.size) return false

        _savedThemes.value = updatedList
        persistThemes(context, updatedList)
        return true
    }

    /**
     * Exports a theme to a JSON export string.
     */
    fun exportThemeToJson(theme: SavedTheme): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("name", theme.name)
        root.put("timestamp", theme.timestamp)
        root.put("settings", JSONObject(theme.jsonPayload))
        return root.toString(2)
    }

    /**
     * Imports a theme from a JSON string.
     */
    fun importThemeFromJson(context: Context, jsonString: String): Result<SavedTheme> {
        return try {
            val root = JSONObject(jsonString)
            val name = root.optString("name", "Imported Theme")
            val settings = root.getJSONObject("settings")
            var count = 0
            val keys = settings.keys()
            while (keys.hasNext()) {
                keys.next()
                count++
            }

            val imported = SavedTheme(
                id = UUID.randomUUID().toString(),
                name = name,
                timestamp = System.currentTimeMillis(),
                settingsCount = count,
                jsonPayload = settings.toString()
            )

            val updated = _savedThemes.value.toMutableList()
            updated.add(0, imported)
            _savedThemes.value = updated
            persistThemes(context, updated)

            Result.success(imported)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun persistThemes(context: Context, list: List<SavedTheme>) {
        val array = JSONArray()
        for (theme in list) {
            val obj = JSONObject()
            obj.put("id", theme.id)
            obj.put("name", theme.name)
            obj.put("timestamp", theme.timestamp)
            obj.put("count", theme.settingsCount)
            obj.put("payload", theme.jsonPayload)
            array.put(obj)
        }
        val prefs = context.getSharedPreferences(PREFS_THEMES_STORE, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEMES_ARRAY, array.toString()).apply()
    }
}
