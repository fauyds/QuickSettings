package com.example.ui.customizer

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.TextButton
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.PanelPreferences
import com.example.data.ProgressBarStyle
import com.example.data.QsSliderTarget
import com.example.data.TileShape
import com.example.ui.components.WavySlider
import com.example.ui.components.QsProgressSlider
import com.example.ui.theme.ExpressiveCardShapes
import com.example.ui.theme.SegmentedButtonShapes
import com.example.ui.theme.MonetPrimary

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.shape.QsShapeItem
import com.example.ui.shape.QsShapeManager

@Composable
fun QsDesignSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val qqsTileSizeValue by PanelPreferences.qqsTileSize.collectAsState()
    val qqsTileCount by PanelPreferences.qqsTileCount.collectAsState()
    val currentEnabledTileShapeId by PanelPreferences.enabledTileShapeId.collectAsState()
    val currentDisabledTileShapeId by PanelPreferences.disabledTileShapeId.collectAsState()
    val tileOutlineEnabled by PanelPreferences.tileOutlineEnabled.collectAsState()
    val allShapes by QsShapeManager.allShapes.collectAsState()
    val showCarrier by PanelPreferences.showCarrier.collectAsState()
    val showCarrierDivider by PanelPreferences.showCarrierDivider.collectAsState()
    val hideCarrierInExpanded by PanelPreferences.hideCarrierInExpanded.collectAsState()
    val brightnessAtBottom by PanelPreferences.brightnessAtBottom.collectAsState()
    val brightnessInQqs by PanelPreferences.brightnessInQqs.collectAsState()
    val wavyProgressBars by PanelPreferences.wavyProgressBars.collectAsState()
    val wavyThickness by PanelPreferences.wavyThickness.collectAsState()
    val wavyWobbliness by PanelPreferences.wavyWobbliness.collectAsState()
    val wavyWavelength by PanelPreferences.wavyWavelength.collectAsState()
    val wavyThumbRadius by PanelPreferences.wavyThumbRadius.collectAsState()
    val wavySpeed by PanelPreferences.wavySpeed.collectAsState()
    val sliderInactiveTrackThickness by PanelPreferences.sliderInactiveTrackThickness.collectAsState()
    val sliderCornerRadius by PanelPreferences.sliderCornerRadius.collectAsState()
    val showCustomHeaderImage by PanelPreferences.showCustomHeaderImage.collectAsState()
    val customHeaderImageUri by PanelPreferences.customHeaderImageUri.collectAsState()
    val customHeaderImageHeight by PanelPreferences.customHeaderImageHeight.collectAsState()
    val customHeaderImageOpacity by PanelPreferences.customHeaderImageOpacity.collectAsState()
    val showCustomHeaderMonetGradient by PanelPreferences.showCustomHeaderMonetGradient.collectAsState()
    val customHeaderMonetGradientHeight by PanelPreferences.customHeaderMonetGradientHeight.collectAsState()
    val showMonetSurfaceBackground by PanelPreferences.showMonetSurfaceBackground.collectAsState()
    val showQsTileGradient by PanelPreferences.showQsTileGradient.collectAsState()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            PanelPreferences.setCustomHeaderImageUri(context, uri.toString())
            PanelPreferences.setShowCustomHeaderImage(context, true)
            Toast.makeText(context, "Header image selected!", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ExpressiveCardShapes.CardSpacing)
    ) {
        // 1. QQS Icon Size Card
        SettingsCard(
            shape = CustomizeCardShapes.TopCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QQS Icon Size",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 17.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$qqsTileSizeValue dp",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "Adjust the size of the circular icons in the compact Quick Quick Settings (QQS) panel.",
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Slider(
                    value = qqsTileSizeValue.toFloat(),
                    onValueChange = { PanelPreferences.setQqsTileSize(context, it.toInt()) },
                    valueRange = 32f..64f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("qqs_tile_size_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("32 dp", fontSize = 10.sp, color = Color.Gray)
                    Text("Default (48 dp)", fontSize = 10.sp, color = Color.Gray)
                    Text("64 dp", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

        // 2. QQS Icon Quantity Card
        SettingsCard(
            shape = CustomizeCardShapes.CenterCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QQS Icon Quantity",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 17.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$qqsTileCount icons",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "Set the number of quick toggle circular icons shown in the collapsed header row (from 2 to 6).",
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Slider(
                    value = qqsTileCount.toFloat(),
                    onValueChange = { PanelPreferences.setQqsTileCount(context, it.toInt()) },
                    valueRange = 2f..6f,
                    steps = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("qqs_tile_count_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("2 icons", fontSize = 10.sp, color = Color.Gray)
                    Text("Default (6 icons)", fontSize = 10.sp, color = Color.Gray)
                    Text("6 icons", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

        var showImportDialog by remember { mutableStateOf(false) }

        // 3. Tile Shape Customization (Enabled / Disabled) backed by XML drawables with path
        SettingsCard(
            shape = CustomizeCardShapes.CenterCard,
            modifier = Modifier.testTag("tile_shape_selection_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with Import Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Tile Shapes (XML Paths)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "All QS shapes are rendered via vector XML drawable paths. You can paste path data, XML, or import files.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = { showImportDialog = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("import_xml_shape_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Shape",
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Add Shape",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Enabled Tile Shape
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Enabled Tile Shape",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Choose the shape when a tile is active/ON",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        allShapes.forEach { shape ->
                            val selected = (currentEnabledTileShapeId == shape.id)
                            val chipShape = SegmentedButtonShapes.getShape(
                                item = shape,
                                items = allShapes,
                                selectedItem = allShapes.firstOrNull { it.id == currentEnabledTileShapeId }
                            )
                            val composeShape = remember(shape.id) {
                                QsShapeManager.getComposeShape(shape.id)
                            }
                            FilterChip(
                                selected = selected,
                                onClick = { PanelPreferences.setEnabledTileShapeId(context, shape.id) },
                                shape = chipShape,
                                border = null,
                                label = {
                                    Text(
                                        text = shape.label,
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(composeShape)
                                            .background(
                                                if (selected) MaterialTheme.colorScheme.onPrimary
                                                else MaterialTheme.colorScheme.primary
                                            )
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.background,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.testTag("enabled_tile_shape_${shape.id}")
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Disabled Tile Shape
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Disabled Tile Shape",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Choose the shape when a tile is inactive/OFF",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        allShapes.forEach { shape ->
                            val selected = (currentDisabledTileShapeId == shape.id)
                            val chipShape = SegmentedButtonShapes.getShape(
                                item = shape,
                                items = allShapes,
                                selectedItem = allShapes.firstOrNull { it.id == currentDisabledTileShapeId }
                            )
                            val composeShape = remember(shape.id) {
                                QsShapeManager.getComposeShape(shape.id)
                            }
                            FilterChip(
                                selected = selected,
                                onClick = { PanelPreferences.setDisabledTileShapeId(context, shape.id) },
                                shape = chipShape,
                                border = null,
                                label = {
                                    Text(
                                        text = shape.label,
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(composeShape)
                                            .background(
                                                if (selected) MaterialTheme.colorScheme.onPrimary
                                                else MaterialTheme.colorScheme.primary
                                            )
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.background,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.testTag("disabled_tile_shape_${shape.id}")
                            )
                        }
                    }
                }

                // Custom Imported Shapes Management
                val customShapes = allShapes.filter { it.isCustom }
                if (customShapes.isNotEmpty()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Imported Custom Shapes (${customShapes.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        customShapes.forEach { customShape ->
                            val customComposeShape = remember(customShape.id) {
                                QsShapeManager.getComposeShape(customShape.id)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(customComposeShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Column {
                                        Text(
                                            text = customShape.label,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Viewport: ${customShape.viewportWidth.toInt()}x${customShape.viewportHeight.toInt()}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        QsShapeManager.deleteImportedShape(context, customShape.id)
                                        if (currentEnabledTileShapeId == customShape.id) {
                                            PanelPreferences.setEnabledTileShapeId(context, "circle")
                                        }
                                        if (currentDisabledTileShapeId == customShape.id) {
                                            PanelPreferences.setDisabledTileShapeId(context, "circle")
                                        }
                                        Toast.makeText(context, "Removed shape \"${customShape.label}\"", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete custom shape",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showImportDialog) {
            ImportXmlShapeDialog(
                onDismiss = { showImportDialog = false },
                onShapeImported = { importedItem ->
                    PanelPreferences.setEnabledTileShapeId(context, importedItem.id)
                    showImportDialog = false
                }
            )
        }

        // 4. 4dp QS Tile Outline Card
        SettingsCard(
            shape = CustomizeCardShapes.CenterCard,
            modifier = Modifier.testTag("tile_outline_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "1.2dp Tile Outline",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Enable a 1.2dp stroke outline on Quick Settings tiles with the inner background hidden.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = tileOutlineEnabled,
                        onCheckedChange = { PanelPreferences.setTileOutlineEnabled(context, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("tile_outline_toggle")
                    )
                }

                AnimatedVisibility(
                    visible = tileOutlineEnabled,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    val enabledComposeShape = remember(currentEnabledTileShapeId) {
                        QsShapeManager.getComposeShape(currentEnabledTileShapeId)
                    }
                    val disabledComposeShape = remember(currentDisabledTileShapeId) {
                        QsShapeManager.getComposeShape(currentDisabledTileShapeId)
                    }
                    val isDark = isSystemInDarkTheme()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Active Tile Preview
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .border(1.2.dp, MaterialTheme.colorScheme.primary, enabledComposeShape)
                                    .clip(enabledComposeShape)
                                    .background(Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = "Active Tile Preview",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "Active (1.2dp)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Inactive Tile Preview
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val inactiveOutline = if (isDark) Color(0xFF262B30) else Color.White
                            val inactiveIconTint = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF1E293B)
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .border(1.2.dp, inactiveOutline, disabledComposeShape)
                                    .clip(disabledComposeShape)
                                    .background(Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = "Inactive Tile Preview",
                                    tint = inactiveIconTint,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "Inactive (1.2dp)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // QS Tile Diagonal Gradient Card
        SettingsCard(
            shape = CustomizeCardShapes.CenterCard,
            modifier = Modifier.testTag("qs_tile_gradient_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "QS Tile Diagonal Gradient",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Enable a smooth diagonal gradient from Primary to Tertiary Monet accent color across active tiles.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = showQsTileGradient,
                    onCheckedChange = { PanelPreferences.setShowQsTileGradient(context, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("qs_tile_gradient_toggle")
                )
            }
        }

        // 5. Show SIM Card Carrier Card
        SettingsCard(
            shape = CustomizeCardShapes.CenterCard
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Show SIM Card Carrier",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Show the dynamic SIM card network operator name on the bottom left of the QQS container.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = showCarrier,
                    onCheckedChange = { PanelPreferences.setShowCarrier(context, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("show_carrier_toggle")
                )
            }
        }

        // Custom Header Image Banner Card
        SettingsCard(
            shape = CustomizeCardShapes.CenterCard,
            modifier = Modifier.testTag("custom_header_image_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Custom Header Image Card",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Display a custom image banner card at the top of the Quick Settings panel.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = showCustomHeaderImage,
                        onCheckedChange = { PanelPreferences.setShowCustomHeaderImage(context, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("custom_header_image_toggle")
                    )
                }

                AnimatedVisibility(
                    visible = showCustomHeaderImage,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    imagePickerLauncher.launch("image/*")
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.UploadFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (customHeaderImageUri.isBlank()) "Select Image" else "Change Image")
                            }

                            if (customHeaderImageUri.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { PanelPreferences.setCustomHeaderImageUri(context, "") },
                                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        if (customHeaderImageUri.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                coil.compose.AsyncImage(
                                    model = customHeaderImageUri,
                                    contentDescription = "Header Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Header Height",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${customHeaderImageHeight.toInt()} dp",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Slider(
                                        value = customHeaderImageHeight,
                                        onValueChange = { PanelPreferences.setCustomHeaderImageHeight(context, it) },
                                        valueRange = 60f..180f,
                                        modifier = Modifier.testTag("custom_header_height_slider")
                                    )
                                }

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Image Opacity",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${(customHeaderImageOpacity * 100).toInt()}%",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Slider(
                                        value = customHeaderImageOpacity,
                                        onValueChange = { PanelPreferences.setCustomHeaderImageOpacity(context, it) },
                                        valueRange = 0.1f..1f,
                                        modifier = Modifier.testTag("custom_header_opacity_slider")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Header Monet Gradient Card
        SettingsCard(
            shape = CustomizeCardShapes.CenterCard,
            modifier = Modifier.testTag("header_monet_gradient_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Header Monet Gradient",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Add a dynamic Monet color gradient background to the header.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = showCustomHeaderMonetGradient,
                        onCheckedChange = { PanelPreferences.setShowCustomHeaderMonetGradient(context, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("custom_header_monet_gradient_toggle")
                    )
                }

                AnimatedVisibility(
                    visible = showCustomHeaderMonetGradient,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Gradient Height",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${customHeaderMonetGradientHeight.toInt()} dp",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Slider(
                            value = customHeaderMonetGradientHeight,
                            onValueChange = { PanelPreferences.setCustomHeaderMonetGradientHeight(context, it) },
                            valueRange = 40f..180f,
                            modifier = Modifier.testTag("custom_header_monet_gradient_height_slider")
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Solid Monet Surface",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Enables dynamic Monet color on the surface behind everything and removes transparency.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = showMonetSurfaceBackground,
                        onCheckedChange = { PanelPreferences.setShowMonetSurfaceBackground(context, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("monet_surface_background_toggle")
                    )
                }
            }
        }

        // Per-Element Monet Accent Colors Preference Card
        SettingsCard(
            shape = CustomizeCardShapes.CenterCard,
            onClick = {
                val intent = Intent(context, ElementColorsActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.testTag("element_colors_preference_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
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
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Per-Element Monet Accent Colors",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Configure Primary or Tertiary Monet accent color for each UI element individually",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // 5. Divider Between Carrier & QQS Tiles Card
        AnimatedVisibility(
            visible = showCarrier,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(ExpressiveCardShapes.CardSpacing)) {
                SettingsCard(
                    shape = CustomizeCardShapes.CenterCard,
                    modifier = Modifier.testTag("carrier_divider_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Divider Between Carrier & QQS Tiles",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Show a divider line separating the SIM card carrier name and QQS tiles",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = showCarrierDivider,
                            onCheckedChange = { PanelPreferences.setShowCarrierDivider(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("show_carrier_divider_toggle")
                        )
                    }
                }

                SettingsCard(
                    shape = CustomizeCardShapes.CenterCard,
                    modifier = Modifier.testTag("hide_carrier_in_expanded_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Hide Carrier in Expanded QS",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Hide the SIM card carrier label when Quick Settings is fully expanded",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = hideCarrierInExpanded,
                            onCheckedChange = { PanelPreferences.setHideCarrierInExpanded(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("hide_carrier_in_expanded_toggle")
                        )
                    }
                }
            }
        }

        // 6. Move Brightness to Bottom Card
        SettingsCard(
            shape = CustomizeCardShapes.CenterCard
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Move Brightness to Bottom",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Move the brightness slider bar to the bottom of the QS panel (just above the footer).",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = brightnessAtBottom,
                    onCheckedChange = { PanelPreferences.setBrightnessAtBottom(context, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("brightness_at_bottom_toggle")
                )
            }
        }

        // 7. Show Brightness Slider in QQS Card
        SettingsCard(
            shape = CustomizeCardShapes.CenterCard
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Show Brightness Slider in QQS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Show the brightness slider inside the compact QQS container view (always visible).",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = brightnessInQqs,
                    onCheckedChange = { PanelPreferences.setBrightnessInQqs(context, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("brightness_in_qqs_toggle")
                )
            }
        }

        // 8. Progress Bar Style Card (Previous M3, Straight, Wavy, Android 12)
        var previewSliderValue by remember { mutableFloatStateOf(0.65f) }
        var selectedSliderTarget by remember { mutableStateOf(QsSliderTarget.BRIGHTNESS) }

        val sliderSyncAll by PanelPreferences.sliderSyncAll.collectAsState()
        val globalProgressBarStyle by PanelPreferences.progressBarStyle.collectAsState()
        val brightnessSliderStyle by PanelPreferences.brightnessSliderStyle.collectAsState()
        val volumeSliderStyle by PanelPreferences.volumeSliderStyle.collectAsState()
        val mediaSliderStyle by PanelPreferences.mediaSliderStyle.collectAsState()

        val wavyThickness by PanelPreferences.wavyThickness.collectAsState()
        val wavyWobbliness by PanelPreferences.wavyWobbliness.collectAsState()
        val wavyWavelength by PanelPreferences.wavyWavelength.collectAsState()
        val wavyThumbRadius by PanelPreferences.wavyThumbRadius.collectAsState()
        val wavySpeed by PanelPreferences.wavySpeed.collectAsState()
        val wavyInactiveThickness by PanelPreferences.wavyInactiveTrackThickness.collectAsState()
        val wavyCornerRadius by PanelPreferences.wavyCornerRadius.collectAsState()

        val a12InactiveThickness by PanelPreferences.a12InactiveTrackThickness.collectAsState()
        val a12CornerRadius by PanelPreferences.a12CornerRadius.collectAsState()

        val straightThickness by PanelPreferences.straightThickness.collectAsState()
        val straightInactiveThickness by PanelPreferences.straightInactiveTrackThickness.collectAsState()
        val straightCornerRadius by PanelPreferences.straightCornerRadius.collectAsState()
        val straightThumbRadius by PanelPreferences.straightThumbRadius.collectAsState()

        val currentProgressBarStyle = when (selectedSliderTarget) {
            QsSliderTarget.BRIGHTNESS -> if (sliderSyncAll) globalProgressBarStyle else brightnessSliderStyle
            QsSliderTarget.VOLUME -> if (sliderSyncAll) globalProgressBarStyle else volumeSliderStyle
            QsSliderTarget.MEDIA -> if (sliderSyncAll) globalProgressBarStyle else mediaSliderStyle
        }

        SettingsCard(
            shape = CustomizeCardShapes.BottomCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Progress Bar & Slider Style",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Customize the progress bar and slider appearance for Brightness, Volume, and Media Player. Configure all sliders together or customize each slider separately.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Target Selector (All Sliders, Brightness, Volume, Media Player)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Slider Configuration Target",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!sliderSyncAll) {
                            TextButton(
                                onClick = {
                                    PanelPreferences.setSliderSyncAll(context, true)
                                    Toast.makeText(context, "All sliders synced together", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Sync All", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    val allTargets = QsSliderTarget.entries
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        allTargets.forEach { target ->
                            val isTargetSelected = (selectedSliderTarget == target)
                            val chipShape = SegmentedButtonShapes.getShape(
                                item = target,
                                items = allTargets,
                                selectedItem = selectedSliderTarget
                            )
                            FilterChip(
                                selected = isTargetSelected,
                                onClick = { selectedSliderTarget = target },
                                shape = chipShape,
                                border = null,
                                label = {
                                    Text(
                                        text = target.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isTargetSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (target) {
                                            QsSliderTarget.BRIGHTNESS -> Icons.Default.BrightnessMedium
                                            QsSliderTarget.VOLUME -> Icons.Default.VolumeUp
                                            QsSliderTarget.MEDIA -> Icons.Default.PlayArrow
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Text(
                        text = when (selectedSliderTarget) {
                            QsSliderTarget.BRIGHTNESS -> "Customizing Brightness Slider (separate style & settings)."
                            QsSliderTarget.VOLUME -> "Customizing Volume Slider (separate style & settings)."
                            QsSliderTarget.MEDIA -> "Customizing Media Player Progress Bar (separate style & settings)."
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                // Style Selector Row (M3, Straight, Wavy, Android 12)
                val allStyles = ProgressBarStyle.entries
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    allStyles.forEach { style ->
                        val selected = (currentProgressBarStyle == style)
                        val chipShape = SegmentedButtonShapes.getShape(
                            item = style,
                            items = allStyles,
                            selectedItem = currentProgressBarStyle
                        )
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (sliderSyncAll) {
                                    PanelPreferences.setSliderSyncAll(context, false)
                                }
                                PanelPreferences.setSliderStyleForTarget(context, selectedSliderTarget, style)
                            },
                            shape = chipShape,
                            border = null,
                            label = {
                                Text(
                                    text = style.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier.size(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val iconColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                    when (style) {
                                        ProgressBarStyle.DEFAULT -> {
                                            // Mini classic M3 rounded slider
                                            Canvas(modifier = Modifier.size(14.dp, 8.dp)) {
                                                drawRoundRect(
                                                    color = iconColor.copy(alpha = 0.35f),
                                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                                )
                                                drawRoundRect(
                                                    color = iconColor,
                                                    size = androidx.compose.ui.geometry.Size(size.width * 0.55f, size.height),
                                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                                )
                                                drawCircle(
                                                    color = iconColor,
                                                    radius = 3.8.dp.toPx(),
                                                    center = Offset(size.width * 0.55f, size.height / 2f)
                                                )
                                            }
                                        }
                                        ProgressBarStyle.STRAIGHT -> {
                                            // Mini straight line with thumb
                                            Canvas(modifier = Modifier.size(14.dp, 8.dp)) {
                                                drawLine(
                                                    color = iconColor.copy(alpha = 0.35f),
                                                    start = Offset(0f, size.height / 2f),
                                                    end = Offset(size.width, size.height / 2f),
                                                    strokeWidth = 2.2.dp.toPx(),
                                                    cap = StrokeCap.Round
                                                )
                                                drawLine(
                                                    color = iconColor,
                                                    start = Offset(0f, size.height / 2f),
                                                    end = Offset(size.width * 0.6f, size.height / 2f),
                                                    strokeWidth = 2.2.dp.toPx(),
                                                    cap = StrokeCap.Round
                                                )
                                                drawCircle(
                                                    color = iconColor,
                                                    radius = 3.dp.toPx(),
                                                    center = Offset(size.width * 0.6f, size.height / 2f)
                                                )
                                            }
                                        }
                                        ProgressBarStyle.WAVY -> {
                                            // Mini wavy path
                                            Canvas(modifier = Modifier.size(14.dp, 8.dp)) {
                                                val wavePath = Path()
                                                val cy = size.height / 2f
                                                wavePath.moveTo(0f, cy)
                                                val step = size.width / 4f
                                                wavePath.quadraticBezierTo(step * 0.5f, cy - 2.8.dp.toPx(), step, cy)
                                                wavePath.quadraticBezierTo(step * 1.5f, cy + 2.8.dp.toPx(), step * 2f, cy)
                                                wavePath.quadraticBezierTo(step * 2.5f, cy - 2.8.dp.toPx(), step * 3f, cy)
                                                wavePath.lineTo(size.width, cy)
                                                drawPath(
                                                    path = wavePath,
                                                    color = iconColor,
                                                    style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                                                )
                                            }
                                        }
                                        ProgressBarStyle.ANDROID_12 -> {
                                            // Mini Android 12 thick pill icon
                                            Canvas(modifier = Modifier.size(14.dp, 8.dp)) {
                                                drawRoundRect(
                                                    color = iconColor.copy(alpha = 0.35f),
                                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                                )
                                                drawRoundRect(
                                                    color = iconColor,
                                                    size = androidx.compose.ui.geometry.Size(size.width * 0.65f, size.height),
                                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.background,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .testTag("progress_bar_style_${style.id}")
                        )
                    }
                }

                // 3. Interactive live preview inside card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Interactive Preview",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${selectedSliderTarget.label} • " + when (currentProgressBarStyle) {
                                ProgressBarStyle.DEFAULT -> "M3 Slider"
                                ProgressBarStyle.STRAIGHT -> "Straight Progress Bar"
                                ProgressBarStyle.WAVY -> "Squiggly Wave Active"
                                ProgressBarStyle.ANDROID_12 -> "Android 12 Thick Pill"
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    QsProgressSlider(
                        value = previewSliderValue,
                        onValueChange = { previewSliderValue = it },
                        style = currentProgressBarStyle,
                        sliderTarget = selectedSliderTarget,
                        isPlaying = (currentProgressBarStyle == ProgressBarStyle.WAVY),
                        activeColor = MaterialTheme.colorScheme.primary,
                        inactiveColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thumbColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wavy_progress_bars_preview_slider")
                    )
                }

                // 4. Wave & Slider Customization Parameters
                AnimatedVisibility(
                    visible = (currentProgressBarStyle != ProgressBarStyle.DEFAULT),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        Text(
                            text = when (currentProgressBarStyle) {
                                ProgressBarStyle.ANDROID_12 -> "Android 12 Slider Parameters"
                                ProgressBarStyle.WAVY -> "Wave & Slider Parameters"
                                ProgressBarStyle.STRAIGHT -> "Straight Slider Parameters"
                                else -> "Slider Parameters"
                            },
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // ANDROID 12 SPECIFIC PARAMETERS
                        if (currentProgressBarStyle == ProgressBarStyle.ANDROID_12) {
                            // Inactive Track Thickness (2dp..38dp)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Inactive Track Thickness",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${String.format("%.1f", a12InactiveThickness)} dp",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = a12InactiveThickness.coerceIn(2.0f, 38.0f),
                                    onValueChange = { PanelPreferences.setA12InactiveTrackThickness(context, it) },
                                    valueRange = 2.0f..38.0f,
                                    steps = 35,
                                    modifier = Modifier.fillMaxWidth().testTag("a12_inactive_thickness_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("2 dp (Thin)", fontSize = 10.sp, color = Color.Gray)
                                    Text("Default (38 dp Full)", fontSize = 10.sp, color = Color.Gray)
                                    Text("38 dp (Pill)", fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            // Slider Corner Radius (0dp..24dp)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Slider Corner Radius",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${a12CornerRadius.toInt()} dp",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = a12CornerRadius,
                                    onValueChange = { PanelPreferences.setA12CornerRadius(context, it) },
                                    valueRange = 0.0f..24.0f,
                                    steps = 23,
                                    modifier = Modifier.fillMaxWidth().testTag("a12_corner_radius_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("0 dp (Square)", fontSize = 10.sp, color = Color.Gray)
                                    Text("10 dp (Rounded)", fontSize = 10.sp, color = Color.Gray)
                                    Text("19 dp (Pill)", fontSize = 10.sp, color = Color.Gray)
                                    Text("24 dp (Max)", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }

                        // WAVY SPECIFIC PARAMETERS
                        if (currentProgressBarStyle == ProgressBarStyle.WAVY) {
                            // Active Track Thickness
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Active Track Thickness",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${String.format("%.1f", wavyThickness)} dp",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = wavyThickness,
                                    onValueChange = { PanelPreferences.setWavyThickness(context, it) },
                                    valueRange = 1.0f..10.0f,
                                    steps = 17,
                                    modifier = Modifier.fillMaxWidth().testTag("wavy_thickness_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("1 dp (Thin)", fontSize = 10.sp, color = Color.Gray)
                                    Text("Default (4 dp)", fontSize = 10.sp, color = Color.Gray)
                                    Text("10 dp (Thick)", fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            // Inactive Track Thickness
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Inactive Track Thickness",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${String.format("%.1f", wavyInactiveThickness)} dp",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = wavyInactiveThickness.coerceIn(1.0f, 16.0f),
                                    onValueChange = { PanelPreferences.setWavyInactiveTrackThickness(context, it) },
                                    valueRange = 1.0f..16.0f,
                                    steps = 14,
                                    modifier = Modifier.fillMaxWidth().testTag("wavy_inactive_track_thickness_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("1 dp (Thin)", fontSize = 10.sp, color = Color.Gray)
                                    Text("Default (4 dp)", fontSize = 10.sp, color = Color.Gray)
                                    Text("16 dp (Thick)", fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            // Slider Corner Radius
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Slider Corner Radius",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${wavyCornerRadius.toInt()} dp",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = wavyCornerRadius,
                                    onValueChange = { PanelPreferences.setWavyCornerRadius(context, it) },
                                    valueRange = 0.0f..24.0f,
                                    steps = 23,
                                    modifier = Modifier.fillMaxWidth().testTag("wavy_corner_radius_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("0 dp (Square)", fontSize = 10.sp, color = Color.Gray)
                                    Text("4 dp (Default)", fontSize = 10.sp, color = Color.Gray)
                                    Text("19 dp (Pill)", fontSize = 10.sp, color = Color.Gray)
                                    Text("24 dp (Max)", fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            // Wobbliness (Wave Amplitude)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Wobbliness (Wave Amplitude)",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${String.format("%.1f", wavyWobbliness)} dp",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = wavyWobbliness,
                                    onValueChange = { PanelPreferences.setWavyWobbliness(context, it) },
                                    valueRange = 0.5f..10.0f,
                                    steps = 18,
                                    modifier = Modifier.fillMaxWidth().testTag("wavy_wobbliness_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("0.5 dp (Subtle)", fontSize = 10.sp, color = Color.Gray)
                                    Text("Default (3.5 dp)", fontSize = 10.sp, color = Color.Gray)
                                    Text("10 dp (Extreme)", fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            // Wavelength (Cycle Length)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Wavelength (Cycle Length)",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${String.format("%.1f", wavyWavelength)} dp",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = wavyWavelength,
                                    onValueChange = { PanelPreferences.setWavyWavelength(context, it) },
                                    valueRange = 10.0f..60.0f,
                                    steps = 24,
                                    modifier = Modifier.fillMaxWidth().testTag("wavy_wavelength_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("10 dp (Dense)", fontSize = 10.sp, color = Color.Gray)
                                    Text("Default (26 dp)", fontSize = 10.sp, color = Color.Gray)
                                    Text("60 dp (Wide)", fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            // Thumb Size (Radius)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Thumb Size (Radius)",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${String.format("%.1f", wavyThumbRadius)} dp",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = wavyThumbRadius,
                                    onValueChange = { PanelPreferences.setWavyThumbRadius(context, it) },
                                    valueRange = 2.0f..12.0f,
                                    steps = 19,
                                    modifier = Modifier.fillMaxWidth().testTag("wavy_thumb_radius_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("2 dp (Tiny)", fontSize = 10.sp, color = Color.Gray)
                                    Text("Default (6 dp)", fontSize = 10.sp, color = Color.Gray)
                                    Text("12 dp (Large)", fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            // Ripple Speed (Animation Period)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Wave Ripple Speed",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${wavySpeed} ms",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = wavySpeed.toFloat(),
                                    onValueChange = { PanelPreferences.setWavySpeed(context, it.toInt()) },
                                    valueRange = 300f..3000f,
                                    steps = 26,
                                    modifier = Modifier.fillMaxWidth().testTag("wavy_speed_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("300 ms (Fast)", fontSize = 10.sp, color = Color.Gray)
                                    Text("1300 ms (Default)", fontSize = 10.sp, color = Color.Gray)
                                    Text("3000 ms (Slow)", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }

                        // STRAIGHT SPECIFIC PARAMETERS
                        if (currentProgressBarStyle == ProgressBarStyle.STRAIGHT) {
                            // Active Track Thickness
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Active Track Thickness",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${String.format("%.1f", straightThickness)} dp",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = straightThickness,
                                    onValueChange = { PanelPreferences.setStraightThickness(context, it) },
                                    valueRange = 1.0f..12.0f,
                                    steps = 21,
                                    modifier = Modifier.fillMaxWidth().testTag("straight_thickness_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("1 dp (Thin)", fontSize = 10.sp, color = Color.Gray)
                                    Text("Default (4 dp)", fontSize = 10.sp, color = Color.Gray)
                                    Text("12 dp (Thick)", fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            // Inactive Track Thickness
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Inactive Track Thickness",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${String.format("%.1f", straightInactiveThickness)} dp",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = straightInactiveThickness.coerceIn(1.0f, 16.0f),
                                    onValueChange = { PanelPreferences.setStraightInactiveTrackThickness(context, it) },
                                    valueRange = 1.0f..16.0f,
                                    steps = 14,
                                    modifier = Modifier.fillMaxWidth().testTag("straight_inactive_thickness_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("1 dp (Thin)", fontSize = 10.sp, color = Color.Gray)
                                    Text("Default (4 dp)", fontSize = 10.sp, color = Color.Gray)
                                    Text("16 dp (Thick)", fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            // Corner Radius
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Slider Corner Radius",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${straightCornerRadius.toInt()} dp",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = straightCornerRadius,
                                    onValueChange = { PanelPreferences.setStraightCornerRadius(context, it) },
                                    valueRange = 0.0f..24.0f,
                                    steps = 23,
                                    modifier = Modifier.fillMaxWidth().testTag("straight_corner_radius_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("0 dp (Square)", fontSize = 10.sp, color = Color.Gray)
                                    Text("4 dp (Default)", fontSize = 10.sp, color = Color.Gray)
                                    Text("24 dp (Max)", fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            // Thumb Size (Radius)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Thumb Size (Radius)",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${String.format("%.1f", straightThumbRadius)} dp",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = straightThumbRadius,
                                    onValueChange = { PanelPreferences.setStraightThumbRadius(context, it) },
                                    valueRange = 2.0f..12.0f,
                                    steps = 19,
                                    modifier = Modifier.fillMaxWidth().testTag("straight_thumb_radius_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("2 dp (Tiny)", fontSize = 10.sp, color = Color.Gray)
                                    Text("Default (6 dp)", fontSize = 10.sp, color = Color.Gray)
                                    Text("12 dp (Large)", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }

                        // Reset to Defaults Button
                        TextButton(
                            onClick = {
                                PanelPreferences.resetWavyParamsToDefault(context)
                                Toast.makeText(context, "Slider settings reset to defaults", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("reset_wavy_params_button")
                        ) {
                            Text(
                                text = "Reset to Defaults",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 9. QS, QQS & Notifications Panel Corners Card
        val panelGlobalCornerEnabled by PanelPreferences.panelGlobalCornerEnabled.collectAsState()
        val panelOuterCorner by PanelPreferences.panelOuterCorner.collectAsState()
        val panelInnerCorner by PanelPreferences.panelInnerCorner.collectAsState()

        SettingsCard(
            shape = CustomizeCardShapes.SingleCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "QS, QQS & Notifications Panel Corners",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Customize inner and outer corner radii and toggle global panel corner sizing for Quick Settings, QQS and notification panels only.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Global Panel Corner Size Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Global Corner Size",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Sync inner corner size automatically with outer corner size",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = panelGlobalCornerEnabled,
                        onCheckedChange = { PanelPreferences.setPanelGlobalCornerEnabled(context, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("panel_global_corner_toggle")
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                    // Outer Corner Slider (0dp..36dp)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Outer Corner Size",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${panelOuterCorner.toInt()} dp",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = panelOuterCorner,
                            onValueChange = { PanelPreferences.setPanelOuterCorner(context, it) },
                            valueRange = 0.0f..36.0f,
                            steps = 36,
                            modifier = Modifier.fillMaxWidth().testTag("panel_outer_corner_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0 dp (Square)", fontSize = 10.sp, color = Color.Gray)
                            Text("20 dp (Default)", fontSize = 10.sp, color = Color.Gray)
                            Text("36 dp (Max)", fontSize = 10.sp, color = Color.Gray)
                        }
                    }

                    // Inner Corner Slider (0dp..36dp)
                    if (panelGlobalCornerEnabled) {
                        Text(
                            text = "Inner corner size is synced to outer corner size (${panelOuterCorner.toInt()} dp)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Inner Corner Size",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${panelInnerCorner.toInt()} dp",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = panelInnerCorner,
                                onValueChange = { PanelPreferences.setPanelInnerCorner(context, it) },
                                valueRange = 0.0f..36.0f,
                                steps = 36,
                                modifier = Modifier.fillMaxWidth().testTag("panel_inner_corner_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("0 dp (Square)", fontSize = 10.sp, color = Color.Gray)
                                Text("5 dp (Default)", fontSize = 10.sp, color = Color.Gray)
                                Text("36 dp (Max)", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImportXmlShapeDialog(
    onDismiss: () -> Unit,
    onShapeImported: (QsShapeItem) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Paste Path, 1: Paste XML, 2: Pick File
    var pathInput by remember { mutableStateOf("") }
    var xmlInput by remember { mutableStateOf("") }
    var shapeLabel by remember { mutableStateOf("") }
    var vpWidthInput by remember { mutableStateOf("100") }
    var vpHeightInput by remember { mutableStateOf("100") }
    var showVpSettings by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var pickedFileName by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val content = inputStream.bufferedReader().use { it.readText() }
                    xmlInput = content
                    var fileName: String? = null
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                            if (nameIdx >= 0) fileName = cursor.getString(nameIdx)
                        }
                    }
                    pickedFileName = fileName
                    if (shapeLabel.isBlank()) {
                        shapeLabel = fileName?.removeSuffix(".xml")?.replace('_', ' ')?.replace('-', ' ')?.trim()?.split(" ")?.joinToString(" ") { word ->
                            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        } ?: "Custom Shape"
                    }
                    errorMessage = null
                }
            } catch (e: Exception) {
                errorMessage = "Failed to read file: ${e.message}"
            }
        }
    }

    // Live parsed shape for preview
    val parsedShape: QsShapeItem? = remember(selectedTab, pathInput, xmlInput, vpWidthInput, vpHeightInput, shapeLabel) {
        val vpW = vpWidthInput.toFloatOrNull() ?: 100f
        val vpH = vpHeightInput.toFloatOrNull() ?: 100f
        when (selectedTab) {
            0 -> {
                if (pathInput.isNotBlank()) {
                    try {
                        errorMessage = null
                        QsShapeManager.parseShapeInput(pathInput, shapeLabel.ifBlank { "Preview" }, vpW, vpH)
                    } catch (e: Exception) {
                        errorMessage = e.message
                        null
                    }
                } else {
                    errorMessage = null
                    null
                }
            }
            else -> {
                if (xmlInput.isNotBlank()) {
                    try {
                        errorMessage = null
                        QsShapeManager.parseShapeInput(xmlInput, shapeLabel.ifBlank { "Preview" }, vpW, vpH)
                    } catch (e: Exception) {
                        errorMessage = e.message
                        null
                    }
                } else {
                    errorMessage = null
                    null
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .heightIn(max = 760.dp),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 8.dp,
                shadowElevation = 20.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 1. Material 3 Expressive Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 16.dp, top = 20.dp, bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Add Custom Tile Shape",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Vector path & XML drawable styling",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        thickness = 1.dp
                    )

                    // 2. Scrollable Body
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Expressive Segmented Pill Switcher
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val tabs = listOf(
                                    Triple(0, "Paste Path", Icons.Default.Draw),
                                    Triple(1, "Paste XML", Icons.Default.Code),
                                    Triple(2, "Pick File", Icons.Default.UploadFile)
                                )
                                val tabTags = listOf("tab_paste_path", "tab_paste_xml", "tab_pick_file")

                                tabs.forEachIndexed { index, (tabIndex, title, icon) ->
                                    val isSelected = selectedTab == tabIndex
                                    val bgCol by animateColorAsState(
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        label = "tab_bg"
                                    )
                                    val contentCol by animateColorAsState(
                                        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        label = "tab_content"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(50.dp))
                                            .background(bgCol)
                                            .clickable { selectedTab = tabIndex }
                                            .padding(vertical = 10.dp)
                                            .testTag(tabTags[index]),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = contentCol,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = title,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = contentCol
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Tab Content
                        when (selectedTab) {
                            0 -> {
                                // TAB 0: Paste Path
                                Card(
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Draw,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "Vector Path Data",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            // Viewport Chip toggle
                                            FilterChip(
                                                selected = showVpSettings,
                                                onClick = { showVpSettings = !showVpSettings },
                                                label = {
                                                    Text(
                                                        text = "Viewport: ${vpWidthInput}×${vpHeightInput}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.Tune,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                },
                                                shape = RoundedCornerShape(50.dp)
                                            )
                                        }

                                        // Expandable Viewport Settings
                                        AnimatedVisibility(visible = showVpSettings) {
                                            Surface(
                                                shape = RoundedCornerShape(14.dp),
                                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(10.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    OutlinedTextField(
                                                        value = vpWidthInput,
                                                        onValueChange = { vpWidthInput = it },
                                                        label = { Text("Viewport Width", fontSize = 11.sp) },
                                                        modifier = Modifier.weight(1f),
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(12.dp),
                                                        textStyle = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                                    )
                                                    OutlinedTextField(
                                                        value = vpHeightInput,
                                                        onValueChange = { vpHeightInput = it },
                                                        label = { Text("Viewport Height", fontSize = 11.sp) },
                                                        modifier = Modifier.weight(1f),
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(12.dp),
                                                        textStyle = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                                    )
                                                }
                                            }
                                        }

                                        OutlinedTextField(
                                            value = pathInput,
                                            onValueChange = { pathInput = it },
                                            placeholder = {
                                                Text(
                                                    "Paste raw SVG or Vector path data here...\n(e.g., M 50,0 C 77.6,0 100,22.4 100,50 ... Z)",
                                                    fontSize = 12.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                            },
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 100.dp, max = 150.dp)
                                                .testTag("shape_path_input"),
                                            textStyle = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                                            maxLines = 6
                                        )

                                        // Helper action buttons (Paste from clipboard & Clear)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (pathInput.isNotBlank()) {
                                                TextButton(
                                                    onClick = { pathInput = "" },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Clear,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Clear", fontSize = 11.sp)
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }

                                            FilledTonalButton(
                                                onClick = {
                                                    val clipText = clipboardManager.getText()?.text
                                                    if (!clipText.isNullOrBlank()) {
                                                        pathInput = clipText
                                                    } else {
                                                        Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                shape = RoundedCornerShape(50.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentPaste,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Paste Clipboard", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }
                            1 -> {
                                // TAB 1: Paste XML
                                Card(
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Code,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "Vector XML Code",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }

                                        OutlinedTextField(
                                            value = xmlInput,
                                            onValueChange = { xmlInput = it },
                                            placeholder = {
                                                Text(
                                                    "<vector xmlns:android=\"...\">\n  <path android:pathData=\"...\" />\n</vector>",
                                                    fontSize = 12.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                            },
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 120.dp, max = 160.dp)
                                                .testTag("shape_xml_input"),
                                            textStyle = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                                            maxLines = 8
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (xmlInput.isNotBlank()) {
                                                TextButton(
                                                    onClick = { xmlInput = "" },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Clear,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Clear", fontSize = 11.sp)
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }

                                            FilledTonalButton(
                                                onClick = {
                                                    val clipText = clipboardManager.getText()?.text
                                                    if (!clipText.isNullOrBlank()) {
                                                        xmlInput = clipText
                                                    } else {
                                                        Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                shape = RoundedCornerShape(50.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentPaste,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Paste Clipboard", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }
                            2 -> {
                                // TAB 2: Pick File
                                Card(
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(22.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(RoundedCornerShape(18.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.UploadFile,
                                                contentDescription = null,
                                                modifier = Modifier.size(28.dp),
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }

                                        Text(
                                            text = "Import Vector Drawable (.xml)",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Text(
                                            text = "Choose a vector drawable XML file exported from Android Studio or vector tools.",
                                            fontSize = 12.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Button(
                                            onClick = { filePickerLauncher.launch("*/*") },
                                            shape = RoundedCornerShape(50.dp),
                                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FileOpen,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Browse XML Files", fontWeight = FontWeight.SemiBold)
                                        }

                                        if (pickedFileName != null) {
                                            Surface(
                                                shape = RoundedCornerShape(50.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.padding(top = 4.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = pickedFileName ?: "file.xml",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Shape Label Input
                        OutlinedTextField(
                            value = shapeLabel,
                            onValueChange = { shapeLabel = it },
                            label = { Text("Shape Name") },
                            placeholder = { Text("e.g. Hexagon, Star, Diamond, Octagon") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Label,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("shape_name_input")
                        )

                        // Material 3 Expressive Live Preview Card
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Tile Shape Preview",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    if (parsedShape != null) {
                                        Surface(
                                            shape = RoundedCornerShape(50.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = "Valid (${parsedShape.viewportWidth.toInt()}×${parsedShape.viewportHeight.toInt()})",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    } else if (errorMessage != null) {
                                        Surface(
                                            shape = RoundedCornerShape(50.dp),
                                            color = MaterialTheme.colorScheme.errorContainer
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ErrorOutline,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = "Syntax Error",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            }
                                        }
                                    }
                                }

                                if (parsedShape != null) {
                                    // Side-by-side Quick Settings tile mockups
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val previewShape = remember(parsedShape.pathData, parsedShape.viewportWidth, parsedShape.viewportHeight) {
                                            com.example.ui.shape.VectorPathShape(
                                                parsedShape.pathData,
                                                parsedShape.viewportWidth,
                                                parsedShape.viewportHeight
                                            )
                                        }

                                        // Active tile mockup
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(54.dp)
                                                    .clip(previewShape)
                                                    .background(MaterialTheme.colorScheme.primary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Wifi,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Text(
                                                text = "Active Tile",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "On",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        // Inactive tile mockup
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(54.dp)
                                                    .clip(previewShape)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Wifi,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Text(
                                                text = "Inactive Tile",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Off",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                } else if (errorMessage != null) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ErrorOutline,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = errorMessage ?: "Invalid path syntax",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Enter a vector path or XML above to preview the tile shape in real-time.",
                                            fontSize = 12.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        thickness = 1.dp
                    )

                    // 3. Material 3 Expressive Action Footer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                val labelToUse = shapeLabel.ifBlank { "Custom Shape" }
                                val vpW = vpWidthInput.toFloatOrNull() ?: 100f
                                val vpH = vpHeightInput.toFloatOrNull() ?: 100f
                                val result = if (selectedTab == 0) {
                                    QsShapeManager.importShapeFromPathData(context, pathInput, labelToUse, vpW, vpH)
                                } else {
                                    QsShapeManager.importShape(context, xmlInput, labelToUse, vpW, vpH)
                                }
                                if (result.isSuccess) {
                                    val imported = result.getOrNull()!!
                                    Toast.makeText(context, "Shape \"${imported.label}\" saved & applied!", Toast.LENGTH_SHORT).show()
                                    onShapeImported(imported)
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to import shape"
                                }
                            },
                            enabled = parsedShape != null,
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("save_shape_confirm_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save & Apply", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
