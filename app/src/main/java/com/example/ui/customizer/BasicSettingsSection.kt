package com.example.ui.customizer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PanelPreferences
import com.example.data.ThemeMode

@Composable
fun BasicSettingsSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val themeMode by PanelPreferences.themeMode.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    val pinnedIds by PinnedCardsManager.getPinnedCardIds(CustomizeCategory.BASIC, context).collectAsState()

    val themeModeCard = @Composable {
        PinnableCardWrapper("theme_mode_card", CustomizeCategory.BASIC) {
            SettingsCard(
                shape = CustomizeCardShapes.TopCard,
                modifier = Modifier.testTag("theme_mode_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "App Settings Theme Mode",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp,
                                textAlign = TextAlign.Center,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Customize the theme for the app settings. The Quick Settings panel retains its authentic system dark appearance.",
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                textAlign = TextAlign.Center,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemeMode.entries.forEachIndexed { index, mode ->
                            val isSelected = themeMode == mode
                            val buttonShape = com.example.ui.theme.SegmentedButtonShapes.getShape(
                                index = index,
                                count = ThemeMode.entries.size,
                                isSelected = isSelected,
                                innerCorner = 8.dp
                            )
                            Surface(
                                onClick = { PanelPreferences.setThemeMode(context, mode) },
                                shape = buttonShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (mode) {
                                            ThemeMode.SYSTEM -> Icons.Default.SettingsSuggest
                                            ThemeMode.LIGHT -> Icons.Default.WbSunny
                                            ThemeMode.DARK -> Icons.Default.DarkMode
                                        },
                                        contentDescription = mode.displayName,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val collapseToggleCard = @Composable {
        PinnableCardWrapper("collapse_expand_options_card", CustomizeCategory.BASIC) {
            CollapseExpandToggleCard(shape = CustomizeCardShapes.CenterCard)
        }
    }

    val collapseSubheadersCard = @Composable {
        PinnableCardWrapper("collapse_expand_subheaders_card", CustomizeCategory.BASIC) {
            CollapseExpandSubheadersCard(shape = CustomizeCardShapes.BottomCard)
        }
    }

    val importExportCard = @Composable {
        PinnableCardWrapper("import_export_card", CustomizeCategory.BASIC) {
            SettingsCard(
                shape = CustomizeCardShapes.SingleCard,
                modifier = Modifier.testTag("import_export_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Import & Export Settings",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp,
                                textAlign = TextAlign.Center,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Backup your Quick Settings configurations to a JSON file or restore from a previously exported setup.",
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                textAlign = TextAlign.Center,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SyncAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { showExportDialog = true },
                            modifier = Modifier.weight(1f).testTag("export_settings_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Export", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { showImportDialog = true },
                            modifier = Modifier.weight(1f).testTag("import_settings_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Import", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CustomizeCardShapes.CardSpacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Pinned Cards at the very top of Basic Settings
        val pinnedCards = mutableListOf<@Composable () -> Unit>()
        if (pinnedIds.contains("theme_mode_card")) pinnedCards.add(themeModeCard)
        if (pinnedIds.contains("collapse_expand_options_card")) pinnedCards.add(collapseToggleCard)
        if (pinnedIds.contains("collapse_expand_subheaders_card")) pinnedCards.add(collapseSubheadersCard)
        if (pinnedIds.contains("import_export_card")) pinnedCards.add(importExportCard)

        if (pinnedCards.isNotEmpty()) {
            ListSubheader(
                text = "Pinned",
                icon = androidx.compose.material.icons.Icons.Default.PushPin
            )
            pinnedCards.forEach { card -> card() }
        }

        // 2. Unpinned Appearance & Theme Cards
        val unpinnedAppearance = mutableListOf<@Composable () -> Unit>()
        if (!pinnedIds.contains("theme_mode_card")) unpinnedAppearance.add(themeModeCard)
        if (!pinnedIds.contains("collapse_expand_options_card")) unpinnedAppearance.add(collapseToggleCard)
        if (!pinnedIds.contains("collapse_expand_subheaders_card")) unpinnedAppearance.add(collapseSubheadersCard)

        if (unpinnedAppearance.isNotEmpty()) {
            ListSubheader(
                text = "Appearance & Theme",
                icon = Icons.Default.Palette
            )
            unpinnedAppearance.forEach { card -> card() }
        }

        // 3. Unpinned Configuration Backup Card
        if (!pinnedIds.contains("import_export_card")) {
            CollapsibleSubheaderSection(
                title = "Configuration Backup",
                icon = Icons.Default.SyncAlt
            ) {
                importExportCard()
            }
        }
    }

    if (showExportDialog) {
        ExportSettingsDialog(
            onDismiss = { showExportDialog = false }
        )
    }

    if (showImportDialog) {
        ImportSettingsDialog(
            onDismiss = { showImportDialog = false }
        )
    }
}

@Composable
fun ExportSettingsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val jsonContent = remember { PanelPreferences.exportSettingsJson(context) }

    val fileSaverLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonContent.toByteArray())
                }
                Toast.makeText(context, "Settings exported successfully!", Toast.LENGTH_SHORT).show()
                onDismiss()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(text = "Export Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Copy the JSON content below or save it as a file to backup your configuration:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = jsonContent,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("QuickSettings_Backup", jsonContent)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, jsonContent)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Settings JSON"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    fileSaverLauncher.launch("quicksettings_config.json")
                }
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save File")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ImportSettingsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var importJsonText by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                    importJsonText = reader.readText()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(text = "Import Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Paste a JSON configuration string or choose a JSON file to restore your settings:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = importJsonText,
                    onValueChange = { importJsonText = it },
                    placeholder = { Text("Paste JSON config here...", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            filePickerLauncher.launch(arrayOf("application/json", "text/*"))
                        }
                    ) {
                        Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick JSON File", fontSize = 12.sp)
                    }

                    if (importJsonText.isNotEmpty()) {
                        TextButton(onClick = { importJsonText = "" }) {
                            Text("Clear", fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = importJsonText.isNotBlank(),
                onClick = {
                    val success = PanelPreferences.importSettingsJson(context, importJsonText.trim())
                    if (success) {
                        Toast.makeText(context, "Settings restored successfully!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    } else {
                        Toast.makeText(context, "Invalid JSON settings format", Toast.LENGTH_LONG).show()
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Restore Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
