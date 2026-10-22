package com.example.ui.customizer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import org.json.JSONObject
import com.example.model.QsGridLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ProgressBarStyle
import com.example.data.QsSliderTarget
import com.example.data.SavedTheme
import com.example.data.ThemeManager
import com.example.data.ThemePreviewData
import com.example.ui.components.QsProgressSlider
import com.example.ui.shape.QsShapeManager

/**
 * Dedicated Themes section for saving and restoring user settings configurations.
 * Strictly user-authored: no preset themes are included.
 * Preview features strictly what the user requested: one enabled tile, one disabled tile,
 * one slider, one card under tiles, background, gradient, and image header (without outlines).
 */
@Composable
fun ThemesSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val savedThemes by ThemeManager.savedThemes.collectAsState()
    val activeThemeId by ThemeManager.activeThemeId.collectAsState()

    var newThemeName by remember { mutableStateOf("") }
    var renameThemeTarget by remember { mutableStateOf<SavedTheme?>(null) }
    var renameInput by remember { mutableStateOf("") }

    var deleteThemeTarget by remember { mutableStateOf<SavedTheme?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }

    val pinnedIds by PinnedCardsManager.getPinnedCardIds(CustomizeCategory.THEMES, context).collectAsState()

    val saveThemeCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("save_theme_card", CustomizeCategory.THEMES) {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("save_theme_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = "Save Current Settings",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 17.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Capture all active tile shapes, sliders, colors, triggers, and layout as a custom theme profile.",
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextField(
                            value = newThemeName,
                            onValueChange = { newThemeName = it },
                            placeholder = { Text("Name", fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                errorIndicatorColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_name_input"),
                            textStyle = TextStyle(fontSize = 13.sp, platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )

                        Button(
                            onClick = {
                                val name = newThemeName.trim().ifEmpty { "My Custom Theme" }
                                val saved = ThemeManager.saveCurrentSettingsAsTheme(context, name)
                                newThemeName = ""
                                Toast.makeText(context, "Theme \"${saved.name}\" saved!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("save_theme_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Save",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    val themeTransferCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("theme_transfer_card", CustomizeCategory.THEMES) {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("theme_transfer_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = "Transfer & Clipboard",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 17.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Export your configuration to clipboard or import a saved theme JSON.",
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                val tempTheme = ThemeManager.saveCurrentSettingsAsTheme(context, "Current Snapshot")
                                val json = ThemeManager.exportThemeToJson(tempTheme)
                                ThemeManager.deleteTheme(context, tempTheme.id)

                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("QuickSettings Theme", json)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Configuration copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_clipboard_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy JSON", fontSize = 12.sp)
                        }

                        FilledTonalButton(
                            onClick = {
                                importJsonText = ""
                                showImportDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("import_clipboard_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import JSON", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    var collapseAllTrigger by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CustomizeCardShapes.CardSpacing)
    ) {
        // 1. Pinned Cards at the very top of Themes
        val pinnedCards = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
        if (pinnedIds.contains("save_theme_card")) pinnedCards.add(saveThemeCard)
        if (pinnedIds.contains("theme_transfer_card")) pinnedCards.add(themeTransferCard)
        savedThemes.forEach { theme ->
            if (pinnedIds.contains("saved_theme_${theme.id}")) {
                pinnedCards.add { cardShape ->
                    SavedThemeCard(
                        theme = theme,
                        shape = cardShape,
                        activeThemeId = activeThemeId,
                        collapseAllTrigger = collapseAllTrigger,
                        onApply = {
                            val ok = ThemeManager.applyTheme(context, theme)
                            if (ok) {
                                Toast.makeText(context, "Applied \"${theme.name}\"!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to apply theme", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onUpdate = {
                            ThemeManager.updateThemeWithCurrentSettings(context, theme.id)
                            Toast.makeText(context, "Updated \"${theme.name}\" with current settings!", Toast.LENGTH_SHORT).show()
                        },
                        onRename = {
                            renameThemeTarget = theme
                            renameInput = theme.name
                        },
                        onExport = {
                            val json = ThemeManager.exportThemeToJson(theme)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("QuickSettings Theme", json)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied \"${theme.name}\" JSON to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = {
                            deleteThemeTarget = theme
                        }
                    )
                }
            }
        }

        if (pinnedCards.isNotEmpty()) {
            ListSubheader(
                text = "Pinned",
                icon = androidx.compose.material.icons.Icons.Default.PushPin
            )
            pinnedCards.forEachIndexed { index, card ->
                card(CustomizeCardShapes.getShapeForIndex(index, pinnedCards.size))
            }
        }

        // Unpinned Save Configuration subheader
        val unpinnedSaveConfig = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
        if (!pinnedIds.contains("save_theme_card")) unpinnedSaveConfig.add(saveThemeCard)
        if (!pinnedIds.contains("theme_transfer_card")) unpinnedSaveConfig.add(themeTransferCard)

        if (unpinnedSaveConfig.isNotEmpty()) {
            CollapsibleSubheaderSection(
                title = "Save Configuration",
                icon = Icons.Default.BookmarkAdd
            ) {
                unpinnedSaveConfig.forEachIndexed { index, card ->
                    card(CustomizeCardShapes.getShapeForIndex(index, unpinnedSaveConfig.size))
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CollectionsBookmark,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "SAVED THEMES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                    color = MaterialTheme.colorScheme.primary,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                )
            }

            TextButton(
                onClick = { collapseAllTrigger = !collapseAllTrigger },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp).testTag("collapse_all_themes_btn")
            ) {
                Text(
                    text = "Collapse All",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(CustomizeCardShapes.CardSpacing)
        ) {
            if (savedThemes.isEmpty()) {
                // Empty state card (SingleCard)
                PinnableCardWrapper("empty_themes_card", CustomizeCategory.THEMES) {
                    SettingsCard(
                        shape = CustomizeCardShapes.SingleCard,
                        modifier = Modifier.testTag("empty_themes_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Text(
                                text = "No Saved Themes",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "You haven't saved any themes yet. Use the Save Configuration card above to save your current settings profile. Preset themes are not included.",
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                val unpinnedThemes = savedThemes.filter { !pinnedIds.contains("saved_theme_${it.id}") }
                // List of user-saved themes with custom preview and proper card corners
                unpinnedThemes.forEachIndexed { index, theme ->
                    val shape = if (unpinnedThemes.size == 1) {
                        CustomizeCardShapes.SingleCard
                    } else when (index) {
                        0 -> CustomizeCardShapes.TopCard
                        unpinnedThemes.size - 1 -> CustomizeCardShapes.BottomCard
                        else -> CustomizeCardShapes.CenterCard
                    }

                    SavedThemeCard(
                        theme = theme,
                        shape = shape,
                        activeThemeId = activeThemeId,
                        collapseAllTrigger = collapseAllTrigger,
                        onApply = {
                            val ok = ThemeManager.applyTheme(context, theme)
                            if (ok) {
                                Toast.makeText(context, "Applied \"${theme.name}\"!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to apply theme", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onUpdate = {
                            ThemeManager.updateThemeWithCurrentSettings(context, theme.id)
                            Toast.makeText(context, "Updated \"${theme.name}\" with current settings!", Toast.LENGTH_SHORT).show()
                        },
                        onRename = {
                            renameThemeTarget = theme
                            renameInput = theme.name
                        },
                        onExport = {
                            val json = ThemeManager.exportThemeToJson(theme)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("QuickSettings Theme", json)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied \"${theme.name}\" JSON to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = {
                            deleteThemeTarget = theme
                        }
                    )
                }
            }
        }
    }

    // Rename Dialog
    if (renameThemeTarget != null) {
        val target = renameThemeTarget!!
        AlertDialog(
            onDismissRequest = { renameThemeTarget = null },
            title = { Text("Rename Theme") },
            text = {
                TextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("Theme Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("rename_theme_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            ThemeManager.renameTheme(context, target.id, renameInput)
                        }
                        renameThemeTarget = null
                    },
                    modifier = Modifier.testTag("confirm_rename_btn")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { renameThemeTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (deleteThemeTarget != null) {
        val target = deleteThemeTarget!!
        AlertDialog(
            onDismissRequest = { deleteThemeTarget = null },
            title = { Text("Delete Theme") },
            text = { Text("Are you sure you want to delete \"${target.name}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        ThemeManager.deleteTheme(context, target.id)
                        deleteThemeTarget = null
                        Toast.makeText(context, "Theme deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_theme_btn")
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteThemeTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Theme JSON") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Paste settings configuration JSON below:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        placeholder = { Text("Paste JSON payload here...", fontSize = 11.sp) },
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("import_theme_json_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonText.isNotBlank()) {
                            val result = ThemeManager.importThemeFromJson(context, importJsonText)
                            if (result.isSuccess) {
                                Toast.makeText(context, "Imported theme \"${result.getOrNull()?.name}\"!", Toast.LENGTH_SHORT).show()
                                showImportDialog = false
                            } else {
                                Toast.makeText(context, "Invalid theme JSON format", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_import_theme_btn")
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Individual saved theme card containing header, requested preview, and management actions.
 */
@Composable
private fun SavedThemeCard(
    theme: SavedTheme,
    shape: Shape,
    activeThemeId: String?,
    collapseAllTrigger: Boolean,
    onApply: () -> Unit,
    onUpdate: () -> Unit,
    onRename: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    PinnableCardWrapper(
        cardId = "saved_theme_${theme.id}",
        category = CustomizeCategory.THEMES
    ) {
        SettingsCard(
            shape = shape,
            modifier = Modifier.fillMaxWidth().testTag("saved_theme_card_${theme.id}")
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            var isExpanded by remember { mutableStateOf(false) }

            LaunchedEffect(collapseAllTrigger) {
                isExpanded = false
            }

            // Header: Title, Date, Settings Count, Expand/Collapse Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = theme.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                        Text(
                            text = "${theme.formattedDate} • ${theme.settingsCount} settings",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse theme" else "Expand theme",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Requested Preview & Chips (Collapsible content)
            if (isExpanded) {
                ThemeOverlayPreview(theme = theme)
                ThemeTweakChipsRow(theme = theme)
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            // Secondary Action Row: Apply, Update, Rename, Share, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Apply Button on Left Side (Minimalist Rounded Tonal, shows "Applied" if active)
                // We determine if active by checking if current preferences match theme or tracking state
                val context = LocalContext.current
                val isApplied = (activeThemeId == theme.id)

                FilledTonalButton(
                    onClick = onApply,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isApplied) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = if (isApplied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.height(30.dp).testTag("apply_theme_btn_${theme.id}")
                ) {
                    Icon(
                        imageVector = if (isApplied) Icons.Default.Check else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isApplied) "Applied" else "Apply",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                }

                // Right Side: Minimalist Round Circular Buttons (Update, Rename, Share, Delete)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Update Button
                    FilledTonalIconButton(
                        onClick = onUpdate,
                        shape = CircleShape,
                        modifier = Modifier.size(34.dp).testTag("update_theme_btn_${theme.id}"),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Update", modifier = Modifier.size(16.dp))
                    }

                    // Rename Button
                    FilledTonalIconButton(
                        onClick = onRename,
                        shape = CircleShape,
                        modifier = Modifier.size(34.dp).testTag("rename_theme_btn_${theme.id}"),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Rename", modifier = Modifier.size(16.dp))
                    }

                    // Share Button
                    FilledTonalIconButton(
                        onClick = onExport,
                        shape = CircleShape,
                        modifier = Modifier.size(34.dp).testTag("export_theme_btn_${theme.id}"),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                    }

                    // Delete Button
                    FilledTonalIconButton(
                        onClick = onDelete,
                        shape = CircleShape,
                        modifier = Modifier.size(34.dp).testTag("delete_theme_btn_${theme.id}"),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
}

@Composable
private fun ThemeTweakChipsRow(
    theme: SavedTheme,
    modifier: Modifier = Modifier
) {
    val tweaks = remember(theme.jsonPayload) { getThemeQsTweaks(theme) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tweaks.forEach { tweak ->
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tweak,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
            }
        }
    }
}

private fun getThemeQsTweaks(theme: SavedTheme): List<String> {
    val tweaks = mutableListOf<String>()
    val json = try { JSONObject(theme.jsonPayload) } catch (_: Exception) { JSONObject() }

    // 1. QS Grid Layout Density (Real Default: QsGridLayout.DEFAULT = "3x2")
    val gridLayoutSaved = json.optString("qs_grid_layout", "3x2")
    if (!gridLayoutSaved.equals("3x2", ignoreCase = true)) {
        val grid = QsGridLayout.fromId(gridLayoutSaved)
        tweaks.add("Grid Size: ${grid.label}")
    }

    // 2. Brightness Slider Position (Real Default: false = Top)
    val brightnessAtBottom = json.optBoolean("brightness_at_bottom", false)
    if (brightnessAtBottom) {
        tweaks.add("Slider on Bottom")
    }

    // 3. Brightness in QQS (Real Default: false)
    val brightnessInQqs = json.optBoolean("brightness_in_qqs", false)
    if (brightnessInQqs) {
        tweaks.add("Brightness in QQS")
    }

    // 4. Carrier Settings (Real Defaults: show_carrier=true, show_carrier_divider=true, hide_carrier_in_expanded=false)
    val showCarrier = json.optBoolean("show_carrier", true)
    if (!showCarrier) {
        tweaks.add("Carrier Hidden")
    } else {
        val showCarrierDivider = json.optBoolean("show_carrier_divider", true)
        if (!showCarrierDivider) {
            tweaks.add("Carrier Divider Off")
        }
        val hideCarrierExpanded = json.optBoolean("hide_carrier_in_expanded", false)
        if (hideCarrierExpanded) {
            tweaks.add("Carrier Hidden in Expanded")
        }
    }

    // 5. Date Position in Header (Real Default: DatePosition.BELOW_TIME = "below")
    val datePosSaved = json.optString("date_position", "below")
    val datePos = com.example.data.DatePosition.fromId(datePosSaved)
    if (datePos != com.example.data.DatePosition.BELOW_TIME) {
        tweaks.add("Date: ${datePos.label}")
    }

    // 6. Edit Pencil Icon Location (Real Default: EditButtonPosition.FOOTER = "footer")
    val editPosSaved = json.optString("edit_button_position", "footer")
    val editPos = com.example.data.EditButtonPosition.fromId(editPosSaved)
    if (editPos != com.example.data.EditButtonPosition.FOOTER) {
        tweaks.add("Edit Pencil: ${editPos.label}")
    }

    // 7. Drag Handle Options (Real Defaults: hide_qqs_drag_handle=false, drag_handle_length=36f)
    val hideQqsDragHandle = json.optBoolean("hide_qqs_drag_handle", false)
    if (hideQqsDragHandle) {
        tweaks.add("Drag Handle Hidden")
    } else {
        val dragLength = json.optDouble("drag_handle_length", 36.0).toInt()
        if (dragLength != 36) {
            tweaks.add("Drag Handle: ${dragLength}dp")
        }
    }

    // 8. Hide Date in Expanded (Real Default: false)
    val hideDateInExpanded = json.optBoolean("hide_date_in_expanded", false)
    if (hideDateInExpanded) {
        tweaks.add("Hide Date in Expanded")
    }

    // 9. Font Sizes (Real Defaults: label=12.0f, subtitle=9.0f)
    val labelFontSize = json.optDouble("qs_tile_label_font_size", 12.0).toFloat()
    if (Math.abs(labelFontSize - 12.0f) > 0.1f) {
        tweaks.add("Label Font: ${String.format("%.1f", labelFontSize)}sp")
    }

    val subtitleFontSize = json.optDouble("qs_tile_subtitle_font_size", 9.0).toFloat()
    if (Math.abs(subtitleFontSize - 9.0f) > 0.1f) {
        tweaks.add("Sub Font: ${String.format("%.1f", subtitleFontSize)}sp")
    }

    // 10. QQS Tile Count (Real Default: 6) & Size (Real Default: 48)
    val qqsCount = json.optInt("qqs_tile_count", 6)
    if (qqsCount != 6) {
        tweaks.add("QQS Tiles: $qqsCount")
    }

    val qqsSize = json.optDouble("qqs_tile_size", 48.0).toInt()
    if (qqsSize != 48) {
        tweaks.add("QQS Tile Size: ${qqsSize}dp")
    }

    if (tweaks.isEmpty()) {
        tweaks.add("Default QS Tweaks")
    }

    return tweaks
}

/**
 * Strictly follows the user's explicit prompt:
 * - One enabled tile
 * - One disabled tile
 * - One slider
 * - One card under the tiles
 * - Without outlines
 * - Background, gradient, and image header shown
 */
@Composable
fun ThemeOverlayPreview(
    theme: SavedTheme,
    modifier: Modifier = Modifier
) {
    val config = com.example.ui.overlay.QsOverlaySurfaceConfig.fromTheme(theme)
    com.example.ui.overlay.QsOverlaySurface(
        config = config,
        headerImageAsCard = true,
        modifier = modifier,
        testTagPrefix = "theme_preview_${theme.id}"
    )
}

