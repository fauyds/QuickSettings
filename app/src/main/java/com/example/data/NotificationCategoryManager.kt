package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.NotificationCategory
import com.example.model.NotificationCategoryIcon
import com.example.model.NotificationItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val Context.categoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_categories_datastore")

data class GroupedNotificationSection(
    val categoryId: String,
    val title: String,
    val icon: NotificationCategoryIcon,
    val isExpanded: Boolean,
    val isUncategorized: Boolean,
    val notifications: List<NotificationItem>
)

object NotificationCategoryManager {
    private const val PREFS_NAME = "notification_categories_prefs"
    private const val KEY_SORTING_ENABLED = "sorting_enabled"
    private const val KEY_CATEGORIES_JSON = "categories_json"
    private const val KEY_EXPANDED_STATES_JSON = "expanded_states_json"

    const val UNCATEGORIZED_ID = "__uncategorized__"

    private val DS_KEY_SORTING_ENABLED = booleanPreferencesKey(KEY_SORTING_ENABLED)
    private val DS_KEY_CATEGORIES_JSON = stringPreferencesKey(KEY_CATEGORIES_JSON)
    private val DS_KEY_EXPANDED_STATES_JSON = stringPreferencesKey(KEY_EXPANDED_STATES_JSON)

    private var prefs: SharedPreferences? = null
    private var appContext: Context? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isSortingEnabled = MutableStateFlow(true)
    val isSortingEnabled: StateFlow<Boolean> = _isSortingEnabled.asStateFlow()

    private val _categories = MutableStateFlow<List<NotificationCategory>>(emptyList())
    val categories: StateFlow<List<NotificationCategory>> = _categories.asStateFlow()

    private val _expandedStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val expandedStates: StateFlow<Map<String, Boolean>> = _expandedStates.asStateFlow()

    fun init(context: Context) {
        if (appContext != null) return
        val app = context.applicationContext
        appContext = app
        val p = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = p

        _isSortingEnabled.value = p.getBoolean(KEY_SORTING_ENABLED, true)
        _categories.value = decodeCategories(p.getString(KEY_CATEGORIES_JSON, null))
        _expandedStates.value = decodeExpandedStates(p.getString(KEY_EXPANDED_STATES_JSON, null))

        scope.launch {
            try {
                val preferences = app.categoryDataStore.data.firstOrNull()
                if (preferences != null) {
                    preferences[DS_KEY_SORTING_ENABLED]?.let { enabled ->
                        _isSortingEnabled.value = enabled
                        p.edit().putBoolean(KEY_SORTING_ENABLED, enabled).apply()
                    }
                    preferences[DS_KEY_CATEGORIES_JSON]?.let { json ->
                        _categories.value = decodeCategories(json)
                        p.edit().putString(KEY_CATEGORIES_JSON, json).apply()
                    }
                    preferences[DS_KEY_EXPANDED_STATES_JSON]?.let { json ->
                        _expandedStates.value = decodeExpandedStates(json)
                        p.edit().putString(KEY_EXPANDED_STATES_JSON, json).apply()
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun setSortingEnabled(context: Context, enabled: Boolean) {
        init(context)
        _isSortingEnabled.value = enabled
        prefs?.edit()?.putBoolean(KEY_SORTING_ENABLED, enabled)?.apply()
        val app = context.applicationContext
        scope.launch {
            try {
                app.categoryDataStore.edit { it[DS_KEY_SORTING_ENABLED] = enabled }
            } catch (_: Exception) {
            }
        }
    }

    fun addCategory(
        context: Context,
        name: String,
        iconId: String,
        packageNames: Set<String> = emptySet(),
        keywords: List<String> = emptyList(),
        isExpandedByDefault: Boolean = true
    ): NotificationCategory {
        init(context)
        val current = _categories.value.toMutableList()
        val newCat = NotificationCategory(
            id = "cat_${UUID.randomUUID().toString().take(8)}",
            name = name.trim().ifBlank { "Category ${current.size + 1}" },
            iconId = iconId,
            packageNames = packageNames,
            keywords = keywords.map { it.trim() }.filter { it.isNotEmpty() },
            isExpandedByDefault = isExpandedByDefault,
            sortOrder = current.size
        )
        current.add(newCat)
        saveCategories(context, current)

        // Seed initial expanded state for the new category
        val states = _expandedStates.value.toMutableMap()
        states[newCat.id] = isExpandedByDefault
        saveExpandedStates(context, states)
        return newCat
    }

    fun updateCategory(context: Context, updated: NotificationCategory) {
        init(context)
        val current = _categories.value.toMutableList()
        val index = current.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            current[index] = updated.copy(
                name = updated.name.trim().ifBlank { current[index].name },
                keywords = updated.keywords.map { it.trim() }.filter { it.isNotEmpty() }
            )
            saveCategories(context, current)
        }
    }

    fun deleteCategory(context: Context, categoryId: String) {
        init(context)
        val current = _categories.value
            .filterNot { it.id == categoryId }
            .mapIndexed { idx, cat -> cat.copy(sortOrder = idx) }
        saveCategories(context, current)

        val states = _expandedStates.value.toMutableMap()
        if (states.remove(categoryId) != null) {
            saveExpandedStates(context, states)
        }
    }

    fun moveCategory(context: Context, fromIndex: Int, toIndex: Int) {
        init(context)
        val current = _categories.value.toMutableList()
        if (fromIndex !in current.indices || toIndex !in current.indices || fromIndex == toIndex) return
        val item = current.removeAt(fromIndex)
        current.add(toIndex, item)
        val reindexed = current.mapIndexed { idx, cat -> cat.copy(sortOrder = idx) }
        saveCategories(context, reindexed)
    }

    fun assignPackageToCategory(context: Context, packageName: String, targetCategoryId: String?) {
        init(context)
        val updated = _categories.value.map { cat ->
            val nextPkgs = cat.packageNames.toMutableSet()
            if (cat.id == targetCategoryId) {
                nextPkgs.add(packageName)
            } else {
                nextPkgs.remove(packageName)
            }
            cat.copy(packageNames = nextPkgs)
        }
        saveCategories(context, updated)
    }

    fun findCategoryForPackage(packageName: String): NotificationCategory? {
        return _categories.value.firstOrNull { packageName in it.packageNames }
    }

    fun isCategoryExpanded(categoryId: String, defaultExpanded: Boolean = true): Boolean {
        return _expandedStates.value[categoryId] ?: defaultExpanded
    }

    fun toggleCategoryExpanded(context: Context? = null, categoryId: String, defaultExpanded: Boolean = true): Boolean {
        if (context != null) init(context)
        val currentStates = _expandedStates.value.toMutableMap()
        val next = !(currentStates[categoryId] ?: defaultExpanded)
        currentStates[categoryId] = next
        saveExpandedStates(context ?: appContext, currentStates)
        return next
    }

    fun setAllCategoriesExpanded(context: Context, expanded: Boolean) {
        init(context)
        val states = _expandedStates.value.toMutableMap()
        _categories.value.forEach { states[it.id] = expanded }
        states[UNCATEGORIZED_ID] = expanded
        saveExpandedStates(context, states)
    }

    fun buildGroupedSections(notifications: List<NotificationItem>): List<GroupedNotificationSection> {
        val cats = _categories.value.sortedBy { it.sortOrder }
        if (!_isSortingEnabled.value || cats.isEmpty() || notifications.isEmpty()) {
            return emptyList()
        }

        val buckets = LinkedHashMap<String, MutableList<NotificationItem>>()
        cats.forEach { buckets[it.id] = mutableListOf() }
        val uncategorized = mutableListOf<NotificationItem>()

        for (notif in notifications) {
            val matched = cats.firstOrNull { it.matches(notif) }
            if (matched != null) {
                buckets[matched.id]?.add(notif)
            } else {
                uncategorized.add(notif)
            }
        }

        val sections = mutableListOf<GroupedNotificationSection>()
        cats.forEach { cat ->
            val items = buckets[cat.id].orEmpty()
            if (items.isNotEmpty()) {
                sections.add(
                    GroupedNotificationSection(
                        categoryId = cat.id,
                        title = cat.name,
                        icon = cat.resolveIcon(),
                        isExpanded = isCategoryExpanded(cat.id, cat.isExpandedByDefault),
                        isUncategorized = false,
                        notifications = items
                    )
                )
            }
        }

        if (uncategorized.isNotEmpty()) {
            sections.add(
                GroupedNotificationSection(
                    categoryId = UNCATEGORIZED_ID,
                    title = "Other",
                    icon = NotificationCategoryIcon.FOLDER,
                    isExpanded = isCategoryExpanded(UNCATEGORIZED_ID, true),
                    isUncategorized = true,
                    notifications = uncategorized
                )
            )
        }

        return sections
    }

    private fun saveCategories(context: Context, list: List<NotificationCategory>) {
        val sorted = list.sortedBy { it.sortOrder }.mapIndexed { index, cat -> cat.copy(sortOrder = index) }
        _categories.value = sorted
        val json = encodeCategories(sorted)
        prefs?.edit()?.putString(KEY_CATEGORIES_JSON, json)?.apply()
        val app = context.applicationContext
        scope.launch {
            try {
                app.categoryDataStore.edit { it[DS_KEY_CATEGORIES_JSON] = json }
            } catch (_: Exception) {
            }
        }
    }

    private fun saveExpandedStates(context: Context?, states: Map<String, Boolean>) {
        _expandedStates.value = states
        val json = encodeExpandedStates(states)
        prefs?.edit()?.putString(KEY_EXPANDED_STATES_JSON, json)?.apply()
        context?.applicationContext?.let { app ->
            scope.launch {
                try {
                    app.categoryDataStore.edit { it[DS_KEY_EXPANDED_STATES_JSON] = json }
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun encodeCategories(list: List<NotificationCategory>): String {
        val arr = JSONArray()
        list.forEach { cat ->
            val obj = JSONObject().apply {
                put("id", cat.id)
                put("name", cat.name)
                put("iconId", cat.iconId)
                put("packageNames", JSONArray(cat.packageNames.toList()))
                put("keywords", JSONArray(cat.keywords))
                put("isExpandedByDefault", cat.isExpandedByDefault)
                put("sortOrder", cat.sortOrder)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun decodeCategories(raw: String?): List<NotificationCategory> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            val result = mutableListOf<NotificationCategory>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val id = obj.optString("id", "").ifBlank { continue }
                val name = obj.optString("name", "").ifBlank { "Category" }
                val iconId = obj.optString("iconId", NotificationCategoryIcon.LABEL.id)
                val pkgArr = obj.optJSONArray("packageNames")
                val pkgs = mutableSetOf<String>()
                if (pkgArr != null) {
                    for (j in 0 until pkgArr.length()) {
                        val pkg = pkgArr.optString(j, "")
                        if (pkg.isNotBlank()) pkgs.add(pkg)
                    }
                }
                val kwArr = obj.optJSONArray("keywords")
                val kws = mutableListOf<String>()
                if (kwArr != null) {
                    for (j in 0 until kwArr.length()) {
                        val kw = kwArr.optString(j, "")
                        if (kw.isNotBlank()) kws.add(kw)
                    }
                }
                val expandedDefault = obj.optBoolean("isExpandedByDefault", true)
                val sortOrder = obj.optInt("sortOrder", i)
                result.add(
                    NotificationCategory(
                        id = id,
                        name = name,
                        iconId = iconId,
                        packageNames = pkgs,
                        keywords = kws,
                        isExpandedByDefault = expandedDefault,
                        sortOrder = sortOrder
                    )
                )
            }
            result.sortedBy { it.sortOrder }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun encodeExpandedStates(states: Map<String, Boolean>): String {
        val obj = JSONObject()
        states.forEach { (k, v) -> obj.put(k, v) }
        return obj.toString()
    }

    private fun decodeExpandedStates(raw: String?): Map<String, Boolean> {
        if (raw.isNullOrBlank()) return emptyMap()
        return try {
            val obj = JSONObject(raw)
            val map = mutableMapOf<String, Boolean>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = obj.optBoolean(key, true)
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }
}
