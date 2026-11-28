package com.example.ui.customizer

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.NotificationCategory
import com.example.model.NotificationCategoryIcon
import com.example.service.QuickPanelNotificationListenerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SelectableAppEntry(
    val packageName: String,
    val label: String,
    val iconBitmap: ImageBitmap?
)

private fun drawableToImageBitmap(drawable: Drawable?): ImageBitmap? {
    if (drawable == null) return null
    return try {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            drawable.bitmap.asImageBitmap()
        } else {
            val w = drawable.intrinsicWidth.coerceIn(24, 96)
            val h = drawable.intrinsicHeight.coerceIn(24, 96)
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, w, h)
            drawable.draw(canvas)
            bitmap.asImageBitmap()
        }
    } catch (_: Exception) {
        null
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotificationCategoryEditorDialog(
    initialCategory: NotificationCategory? = null,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        iconId: String,
        packageNames: Set<String>,
        keywords: List<String>,
        isExpandedByDefault: Boolean
    ) -> Unit
) {
    val context = LocalContext.current
    val isEditing = initialCategory != null

    var name by remember { mutableStateOf(initialCategory?.name ?: "") }
    var selectedIconId by remember { mutableStateOf(initialCategory?.iconId ?: NotificationCategoryIcon.LABEL.id) }
    var selectedPackages by remember { mutableStateOf(initialCategory?.packageNames ?: emptySet()) }
    var keywordsText by remember { mutableStateOf(initialCategory?.keywords?.joinToString(", ") ?: "") }
    var isExpandedByDefault by remember { mutableStateOf(initialCategory?.isExpandedByDefault ?: true) }

    var showIconPickerModal by remember { mutableStateOf(false) }
    var showAppPickerModal by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme
    val isDark = com.example.ui.theme.LocalIsDarkTheme.current
    val dialogBg = com.example.ui.theme.getSettingsScreenBackgroundColor(isDark)
    val cardBg = com.example.ui.theme.getSettingsCardBackgroundColor(isDark)
    val selectedIcon = NotificationCategoryIcon.fromId(selectedIconId)

    val appLabelMap = remember(context, selectedPackages) {
        val pm = context.packageManager
        selectedPackages.associateWith { pkg ->
            try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {
                pkg
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("category_editor_dialog"),
            color = dialogBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Edit Category" else "Create Category",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Name & Icon Card
                    Surface(
                        color = cardBg,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "CATEGORY DETAILS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = colorScheme.primary
                            )

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Category Name") },
                                placeholder = { Text("e.g. Work, Social, Finance") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("field_category_name")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(colorScheme.primary)
                                            .clickable { showIconPickerModal = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = selectedIcon.imageVector,
                                            contentDescription = selectedIcon.label,
                                            tint = colorScheme.onPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    SettingsTitleDescription(
                                        title = "Category Icon",
                                        description = "${selectedIcon.label} • ${selectedIcon.group}",
                                        titleFontSize = 13.5.sp,
                                        titleFontWeight = FontWeight.SemiBold,
                                        descriptionFontSize = 11.5.sp
                                    )
                                }

                                FilledTonalButton(
                                    onClick = { showIconPickerModal = true },
                                    shape = CircleShape,
                                    modifier = Modifier.testTag("btn_pick_category_icon")
                                ) {
                                    Text("Choose Icon", fontSize = 12.sp)
                                }
                            }

                            // Quick inline icon strip (pinned selected icon + curated defaults)
                            val quickStripIcons = remember(selectedIcon) {
                                (listOf(selectedIcon) + NotificationCategoryIcon.quickEntries).distinctBy { it.id }
                            }
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(quickStripIcons, key = { it.id }) { iconEntry ->
                                    val isSel = iconEntry.id == selectedIconId
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSel) colorScheme.primary
                                                else dialogBg
                                            )
                                            .border(
                                                width = if (isSel) 2.dp else 0.dp,
                                                color = if (isSel) colorScheme.primary else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable { selectedIconId = iconEntry.id }
                                            .testTag("quick_icon_${iconEntry.id}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = iconEntry.imageVector,
                                            contentDescription = iconEntry.label,
                                            tint = if (isSel) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Apps & Keywords Rules Card
                    Surface(
                        color = cardBg,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SettingsTitleDescription(
                                    title = "ASSIGNED APPS (${selectedPackages.size})",
                                    description = "Notifications from these apps will be grouped under this category.",
                                    titleFontSize = 11.sp,
                                    titleFontWeight = FontWeight.Bold,
                                    titleColor = colorScheme.primary,
                                    descriptionFontSize = 11.sp,
                                    modifier = Modifier.weight(1f)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                FilledTonalButton(
                                    onClick = { showAppPickerModal = true },
                                    shape = CircleShape,
                                    modifier = Modifier.testTag("btn_select_category_apps")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Apps,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Select Apps", fontSize = 12.sp)
                                }
                            }

                            if (selectedPackages.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(dialogBg)
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "No apps assigned yet. Tap \"Select Apps\" or use keywords below.",
                                        fontSize = 11.5.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    selectedPackages.sortedBy { appLabelMap[it] ?: it }.forEach { pkg ->
                                        val label = appLabelMap[pkg] ?: pkg
                                        Surface(
                                            shape = CircleShape,
                                            color = dialogBg,
                                            modifier = Modifier.clickable {
                                                selectedPackages = selectedPackages - pkg
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = label,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove $label",
                                                    tint = colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = keywordsText,
                                onValueChange = { keywordsText = it },
                                label = { Text("Keywords (Optional, comma-separated)") },
                                placeholder = { Text("e.g. OTP, invoice, delivery") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("field_category_keywords")
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(dialogBg)
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SettingsTitleDescription(
                                    title = "Expanded by default",
                                    description = "Show notifications automatically when opening the shade",
                                    titleFontSize = 13.sp,
                                    titleFontWeight = FontWeight.SemiBold,
                                    descriptionFontSize = 11.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(
                                    checked = isExpandedByDefault,
                                    onCheckedChange = { isExpandedByDefault = it },
                                    modifier = Modifier.testTag("switch_category_expanded_default")
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shape = CircleShape,
                        modifier = Modifier.testTag("btn_cancel_category")
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedKeywords = keywordsText
                                .split(',')
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                            onSave(
                                name.trim().ifBlank { selectedIcon.label },
                                selectedIconId,
                                selectedPackages,
                                parsedKeywords,
                                isExpandedByDefault
                            )
                        },
                        shape = CircleShape,
                        modifier = Modifier.testTag("btn_save_category")
                    ) {
                        Text(if (isEditing) "Save Category" else "Create Category")
                    }
                }
            }
        }
    }

    if (showIconPickerModal) {
        CategoryIconPickerModal(
            selectedIconId = selectedIconId,
            onDismiss = { showIconPickerModal = false },
            onSelectIcon = { icon ->
                selectedIconId = icon.id
                showIconPickerModal = false
            }
        )
    }

    if (showAppPickerModal) {
        CategoryAppPickerDialog(
            initialSelectedPackages = selectedPackages,
            onDismiss = { showAppPickerModal = false },
            onConfirm = { updated ->
                selectedPackages = updated
                showAppPickerModal = false
            }
        )
    }
}

@Composable
private fun CategoryIconPickerModal(
    selectedIconId: String,
    onDismiss: () -> Unit,
    onSelectIcon: (NotificationCategoryIcon) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf("All") }
    val colorScheme = MaterialTheme.colorScheme
    val isDark = com.example.ui.theme.LocalIsDarkTheme.current
    val dialogBg = com.example.ui.theme.getSettingsScreenBackgroundColor(isDark)
    val cardBg = com.example.ui.theme.getSettingsCardBackgroundColor(isDark)

    val allIcons = NotificationCategoryIcon.entries
    val filteredIcons = remember(searchQuery, selectedGroup, allIcons) {
        val query = searchQuery.trim()
        allIcons.filter { entry ->
            val matchesGroup = selectedGroup == "All" || entry.group == selectedGroup
            val matchesSearch = query.isEmpty() ||
                entry.label.contains(query, ignoreCase = true) ||
                entry.group.contains(query, ignoreCase = true) ||
                entry.keywords.contains(query, ignoreCase = true) ||
                entry.id.contains(query, ignoreCase = true)
            matchesGroup && matchesSearch
        }
    }

    val gridState = rememberLazyGridState()
    LaunchedEffect(selectedGroup, searchQuery) {
        if (filteredIcons.isNotEmpty()) {
            gridState.scrollToItem(0)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.84f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("category_icon_picker_modal"),
            color = dialogBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingsTitleDescription(
                        title = "Google Icon Library",
                        description = "${filteredIcons.size} of ${allIcons.size} icons",
                        titleFontSize = 18.sp,
                        titleFontWeight = FontWeight.Bold,
                        descriptionFontSize = 11.5.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search ${allIcons.size} icons (name, keyword, group)...",
                            fontSize = 12.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("field_search_category_icon")
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(NotificationCategoryIcon.groups) { group ->
                        val isSel = selectedGroup == group
                        Surface(
                            shape = CircleShape,
                            color = if (isSel) colorScheme.primary else cardBg,
                            modifier = Modifier
                                .clickable { selectedGroup = group }
                                .testTag("icon_group_${group.lowercase()}")
                        ) {
                            Text(
                                text = group,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) colorScheme.onPrimary else colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (filteredIcons.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "No icons match \"$searchQuery\"",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = {
                                    searchQuery = ""
                                    selectedGroup = "All"
                                }
                            ) {
                                Text("Reset Filters")
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredIcons, key = { it.id }) { entry ->
                            val isSel = entry.id == selectedIconId
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSel) colorScheme.primaryContainer else cardBg)
                                    .border(
                                        width = if (isSel) 1.5.dp else 0.dp,
                                        color = if (isSel) colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onSelectIcon(entry) }
                                    .padding(vertical = 10.dp, horizontal = 6.dp)
                                    .testTag("modal_icon_${entry.id}"),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = entry.imageVector,
                                    contentDescription = entry.label,
                                    tint = if (isSel) colorScheme.onPrimaryContainer else colorScheme.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = entry.label,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    color = if (isSel) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryAppPickerDialog(
    initialSelectedPackages: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(initialSelectedPackages) }
    var appsList by remember { mutableStateOf<List<SelectableAppEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val colorScheme = MaterialTheme.colorScheme
    val isDark = com.example.ui.theme.LocalIsDarkTheme.current
    val dialogBg = com.example.ui.theme.getSettingsScreenBackgroundColor(isDark)
    val cardBg = com.example.ui.theme.getSettingsCardBackgroundColor(isDark)

    LaunchedEffect(context) {
        val loaded = withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val activeNotifPkgs = QuickPanelNotificationListenerService.notifications.value
                .map { it.packageName }
                .toSet()

            val installed = try {
                pm.getInstalledApplications(PackageManager.GET_META_DATA)
            } catch (_: Exception) {
                emptyList()
            }

            installed.mapNotNull { info ->
                val isLaunchable = pm.getLaunchIntentForPackage(info.packageName) != null
                val isActive = info.packageName in activeNotifPkgs || info.packageName in initialSelectedPackages
                if (!isLaunchable && !isActive) return@mapNotNull null
                val label = try {
                    pm.getApplicationLabel(info).toString()
                } catch (_: Exception) {
                    info.packageName
                }
                val bmp = try {
                    drawableToImageBitmap(pm.getApplicationIcon(info))
                } catch (_: Exception) {
                    null
                }
                SelectableAppEntry(
                    packageName = info.packageName,
                    label = label,
                    iconBitmap = bmp
                )
            }.sortedWith(
                compareByDescending<SelectableAppEntry> { it.packageName in initialSelectedPackages }
                    .thenByDescending { it.packageName in activeNotifPkgs }
                    .thenBy { it.label.lowercase() }
            )
        }
        appsList = loaded
        isLoading = false
    }

    val filteredApps = remember(appsList, searchQuery) {
        if (searchQuery.isBlank()) appsList
        else appsList.filter {
            it.label.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.84f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("category_app_picker_dialog"),
            color = dialogBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingsTitleDescription(
                        title = "Select Apps (${selected.size})",
                        description = "Active notification & launchable apps",
                        titleFontSize = 18.sp,
                        titleFontWeight = FontWeight.Bold,
                        descriptionFontSize = 11.5.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search apps or package name...", fontSize = 12.5.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            val isChecked = app.packageName in selected
                            Surface(
                                color = cardBg,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selected = if (isChecked) {
                                            selected - app.packageName
                                        } else {
                                            selected + app.packageName
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (app.iconBitmap != null) {
                                        Image(
                                            bitmap = app.iconBitmap,
                                            contentDescription = app.label,
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Apps,
                                                contentDescription = app.label,
                                                tint = colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    SettingsTitleDescription(
                                        title = app.label,
                                        description = app.packageName,
                                        titleFontSize = 13.5.sp,
                                        titleFontWeight = FontWeight.SemiBold,
                                        titleMaxLines = 1,
                                        titleOverflow = TextOverflow.Ellipsis,
                                        descriptionFontSize = 11.sp,
                                        descriptionMaxLines = 1,
                                        descriptionOverflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            selected = if (checked) {
                                                selected + app.packageName
                                            } else {
                                                selected - app.packageName
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, shape = CircleShape) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(selected) },
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("btn_confirm_category_apps")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Done (${selected.size})")
                    }
                }
            }
        }
    }
}
