package com.example.ui.customizer

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
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
import com.example.data.QsPageIndicatorStyle
import com.example.data.QsSliderTarget
import com.example.data.TileShape
import com.example.data.ClearAllFabPosition
import com.example.data.ClearAllFabShape
import com.example.data.ClearAllFabIcon
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.NotificationsOff
import com.example.ui.components.QsProgressSlider
import com.example.ui.overlay.QsPageIndicator
import com.example.ui.theme.ExpressiveCardShapes
import com.example.ui.theme.SegmentedButtonShapes
import com.example.ui.theme.MonetPrimary

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Gradient
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Layers
import com.example.data.ComplicationCommandRepository
import com.example.data.MediaNotificationStyle
import com.example.ui.theme.ExpressiveTheme
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
import androidx.compose.material.icons.filled.Speed
import com.example.ui.shape.QsShapeItem
import com.example.ui.shape.QsShapeManager
import com.example.ui.shape.insideShapeBorder

@Composable
fun QsDesignSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val qqsTileSizeValue by PanelPreferences.qqsTileSize.collectAsState()
    val qqsTileCount by PanelPreferences.qqsTileCount.collectAsState()
    val enableQqsComplicationsRow by PanelPreferences.enableQqsComplicationsRow.collectAsState()
    val qqs2x1ComplicationIds by PanelPreferences.qqs2x1ComplicationIds.collectAsState()
    val qqs2x1HorizontalLine by PanelPreferences.qqs2x1HorizontalLine.collectAsState()
    val qqs2x1PerimeterProgress by PanelPreferences.qqs2x1PerimeterProgress.collectAsState()
    val qqs2x1ActiveCornerDp by PanelPreferences.qqs2x1ActiveCornerDp.collectAsState()
    val qqs2x1InactiveCornerDp by PanelPreferences.qqs2x1InactiveCornerDp.collectAsState()
    val qqs2x1TileHeightDp by PanelPreferences.qqs2x1TileHeightDp.collectAsState()
    val currentEnabledTileShapeId by PanelPreferences.enabledTileShapeId.collectAsState()
    val currentDisabledTileShapeId by PanelPreferences.disabledTileShapeId.collectAsState()
    val tileOutlineEnabled by PanelPreferences.tileOutlineEnabled.collectAsState()
    val tileOutlineThicknessDp by PanelPreferences.tileOutlineThicknessDp.collectAsState()
    val complicationOutlineEnabled by PanelPreferences.complicationOutlineEnabled.collectAsState()
    val allShapes by QsShapeManager.allShapes.collectAsState()
    val pageIndicatorStyle by PanelPreferences.qsPageIndicatorStyle.collectAsState()
    val brightnessAtBottom by PanelPreferences.brightnessAtBottom.collectAsState()
    val brightnessInQqs by PanelPreferences.brightnessInQqs.collectAsState()
    val showBrightnessGradient by PanelPreferences.showBrightnessGradient.collectAsState()
    val showBrightnessControlsBackground by PanelPreferences.showBrightnessControlsBackground.collectAsState()
    val showCustomHeaderImage by PanelPreferences.showCustomHeaderImage.collectAsState()
    val customHeaderImageUri by PanelPreferences.customHeaderImageUri.collectAsState()
    val customHeaderImageHeight by PanelPreferences.customHeaderImageHeight.collectAsState()
    val customHeaderImageOpacity by PanelPreferences.customHeaderImageOpacity.collectAsState()
    val showCustomHeaderMonetGradient by PanelPreferences.showCustomHeaderMonetGradient.collectAsState()
    val customHeaderMonetGradientHeight by PanelPreferences.customHeaderMonetGradientHeight.collectAsState()
    val showMonetSurfaceBackground by PanelPreferences.showMonetSurfaceBackground.collectAsState()
    val showQsTileGradient by PanelPreferences.showQsTileGradient.collectAsState()
    val panelGlobalCornerEnabled by PanelPreferences.panelGlobalCornerEnabled.collectAsState()
    val panelOuterCorner by PanelPreferences.panelOuterCorner.collectAsState()
    val panelInnerCorner by PanelPreferences.panelInnerCorner.collectAsState()
    val enablePanelTopRim by PanelPreferences.enablePanelTopRim.collectAsState()
    val enableTileTopRim by PanelPreferences.enableTileTopRim.collectAsState()
    val topRimAlpha by PanelPreferences.topRimAlpha.collectAsState()
    val topRimPercent = kotlin.math.round(topRimAlpha * 100f).toInt().coerceIn(5, 30)
    val collapseExpandOptions by PanelPreferences.collapseExpandOptions.collectAsState()
    val useClearAllFab by PanelPreferences.useClearAllFab.collectAsState()
    val clearAllFabPosition by PanelPreferences.clearAllFabPosition.collectAsState()
    val clearAllFabShapeId by PanelPreferences.clearAllFabShapeId.collectAsState()
    val clearAllFabShape by PanelPreferences.clearAllFabShape.collectAsState()
    val clearAllFabIcon by PanelPreferences.clearAllFabIcon.collectAsState()
    val showMediaInNotifContainer by PanelPreferences.showMediaInNotificationContainer.collectAsState()
    val mediaNotificationStyle by PanelPreferences.mediaNotificationStyle.collectAsState()
    val currentPanelStyle by PanelPreferences.panelStyle.collectAsState()
    val isAndroidOreoStyle by PanelPreferences.isAndroidOreoStyle.collectAsState()
    val isAndroidMarshmallowStyle by PanelPreferences.isAndroidMarshmallowStyle.collectAsState()
    val isAndroidKitKatStyle by PanelPreferences.isAndroidKitKatStyle.collectAsState()
    val enableLegacyTileShapes by PanelPreferences.enableLegacyTileShapes.collectAsState()

    var tileShapesExpanded by rememberSaveable { mutableStateOf(false) }
    var progressBarSliderExpanded by rememberSaveable { mutableStateOf(false) }
    var panelCornersExpanded by rememberSaveable { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                try {
                    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                } catch (_: Exception) {}

                val headerDir = java.io.File(context.filesDir, "header_images").apply { mkdirs() }
                val destFile = java.io.File(headerDir, "custom_header_image.jpg")
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    java.io.FileOutputStream(destFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                val localUriStr = Uri.fromFile(destFile).toString()
                PanelPreferences.setCustomHeaderImageUri(context, localUriStr)
                PanelPreferences.setShowCustomHeaderImage(context, true)
                Toast.makeText(context, "Header image selected!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                PanelPreferences.setCustomHeaderImageUri(context, uri.toString())
                PanelPreferences.setShowCustomHeaderImage(context, true)
                Toast.makeText(context, "Header image selected!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var showImportDialog by remember { mutableStateOf(false) }
    var showFabImportDialog by remember { mutableStateOf(false) }

    val pinnedIds by PinnedCardsManager.getPinnedCardIds(CustomizeCategory.QS_DESIGN, context).collectAsState()
    val hiddenIds by PinnedCardsManager.getHiddenCardIds().collectAsState()
    val isUserHidden = { id: String -> HiddenFeatureCatalog.isCardHidden(id, hiddenIds) }

    // --- 1. TILE SHAPES & LAYOUT CARDS ---

    // 0. Quick Settings Style Selector Card
    val panelStyleCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("panel_style_card", CustomizeCategory.QS_DESIGN, "Quick Settings Panel Style") {
            SettingsCard(shape = shape) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
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
                                imageVector = Icons.Default.Apps,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Quick Settings Style",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 17.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Select layout style: Modern, Android 8.0 Oreo, Android 6.0 Marshmallow, or Android 4.4 KitKat.",
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    val allStyles = com.example.data.QsPanelStyle.entries
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        allStyles.forEach { style ->
                            val isSelected = (currentPanelStyle == style)
                            val chipShape = SegmentedButtonShapes.getShape(
                                item = style,
                                items = allStyles,
                                selectedItem = currentPanelStyle
                            )
                            FilterChip(
                                selected = isSelected,
                                onClick = { PanelPreferences.setPanelStyle(context, style) },
                                shape = chipShape,
                                border = null,
                                label = {
                                    Text(
                                        text = style.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        style = TextStyle(
                                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                                            lineHeightStyle = LineHeightStyle(
                                                alignment = LineHeightStyle.Alignment.Center,
                                                trim = LineHeightStyle.Trim.Both
                                            )
                                        )
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = com.example.ui.theme.getSettingsScreenBackgroundColor(isSystemInDarkTheme()),
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    // Chips row ends here
                }
            }
        }
    }

    val androidOreoStyleCard = panelStyleCard

    // 1. QQS Icon Size Card
    val qqsIconSizeCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("qqs_icon_size_card", CustomizeCategory.QS_DESIGN, "QQS Icon Size") {
            SettingsCard(
                shape = shape
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 36.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-16).dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
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
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "QQS Icon Size",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 17.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
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
                }
            }
        }
    }

    // 2. QQS Icon Quantity Card
    val qqsIconQuantityCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("qqs_icon_quantity_card", CustomizeCategory.QS_DESIGN, "QQS Icon Quantity") {
            SettingsCard(
                shape = shape
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 36.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-16).dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
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
                                    imageVector = Icons.Default.Apps,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "QQS Icon Quantity",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 17.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
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
            }
        }
    }
    }

    // 2.4b. Additional 2x1 Complications QQS Row Card
    val qqsComplicationsRowCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("qqs_complications_row_card", CustomizeCategory.QS_DESIGN, "QQS 2x1 Complications Row") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("qqs_complications_row_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ViewAgenda,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "2x1 Complications Row",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                                        lineHeightStyle = LineHeightStyle(
                                            alignment = LineHeightStyle.Alignment.Center,
                                            trim = LineHeightStyle.Trim.Both
                                        )
                                    )
                                )
                                Text(
                                    text = "Show an additional QQS row with 2x1 live complication tiles beneath quick toggles.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                                        lineHeightStyle = LineHeightStyle(
                                            alignment = LineHeightStyle.Alignment.Center,
                                            trim = LineHeightStyle.Trim.Both
                                        )
                                    )
                                )
                            }
                        }
                        Switch(
                            checked = enableQqsComplicationsRow,
                            onCheckedChange = { PanelPreferences.setEnableQqsComplicationsRow(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("enable_qqs_complications_row_switch")
                        )
                    }

                    AnimatedVisibility(
                        visible = enableQqsComplicationsRow,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(ExpressiveTheme.colors.nestedElementSurface)
                                    .pinnableClickable { PanelPreferences.setQqs2x1HorizontalLine(context, !qqs2x1HorizontalLine) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "Horizontal Progress Line",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                    )
                                    Text(
                                        text = "Replace the circular progress arc with a horizontal line in the 2x1 complication row.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                    )
                                }
                                Switch(
                                    checked = qqs2x1HorizontalLine,
                                    onCheckedChange = { PanelPreferences.setQqs2x1HorizontalLine(context, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier.testTag("qqs_2x1_horizontal_line_switch")
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(ExpressiveTheme.colors.nestedElementSurface)
                                    .pinnableClickable { PanelPreferences.setQqs2x1PerimeterProgress(context, !qqs2x1PerimeterProgress) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "Progress Bar Around 2x1 Tiles",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                    )
                                    Text(
                                        text = "Draw a progress bar tracing the outer perimeter of each 2x1 complication card.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                    )
                                }
                                Switch(
                                    checked = qqs2x1PerimeterProgress,
                                    onCheckedChange = { PanelPreferences.setQqs2x1PerimeterProgress(context, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier.testTag("qqs_2x1_perimeter_progress_switch")
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(ExpressiveTheme.colors.nestedElementSurface)
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "2x1 Tile Height",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                        )
                                        Text(
                                            text = "$qqs2x1TileHeightDp dp",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                        )
                                    }
                                    Slider(
                                        value = qqs2x1TileHeightDp.toFloat(),
                                        onValueChange = {
                                            PanelPreferences.setQqs2x1TileHeightDp(context, kotlin.math.round(it).toInt())
                                        },
                                        valueRange = 40f..84f,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("qqs_2x1_tile_height_slider"),
                                        colors = SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.primary,
                                            activeTrackColor = MaterialTheme.colorScheme.primary,
                                            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                        )
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Active 2x1 Corner Radius",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                        )
                                        Text(
                                            text = "${kotlin.math.round(qqs2x1ActiveCornerDp).toInt()} dp",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                        )
                                    }
                                    Slider(
                                        value = qqs2x1ActiveCornerDp,
                                        onValueChange = {
                                            PanelPreferences.setQqs2x1ActiveCornerDp(context, kotlin.math.round(it))
                                        },
                                        valueRange = 0f..40f,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("qqs_2x1_active_corner_slider"),
                                        colors = SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.primary,
                                            activeTrackColor = MaterialTheme.colorScheme.primary,
                                            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                        )
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Inactive 2x1 Corner Radius",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                        )
                                        Text(
                                            text = "${kotlin.math.round(qqs2x1InactiveCornerDp).toInt()} dp",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                        )
                                    }
                                    Slider(
                                        value = qqs2x1InactiveCornerDp,
                                        onValueChange = {
                                            PanelPreferences.setQqs2x1InactiveCornerDp(context, kotlin.math.round(it))
                                        },
                                        valueRange = 0f..40f,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("qqs_2x1_inactive_corner_slider"),
                                        colors = SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.primary,
                                            activeTrackColor = MaterialTheme.colorScheme.primary,
                                            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                        )
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "2x1 ROW COMPLICATIONS",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                )
                                Text(
                                    text = "${qqs2x1ComplicationIds.size} selected",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                )
                            }
                            Text(
                                text = "Two 2x1 tiles appear side by side; selecting more scrolls horizontally in the same row.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                            @OptIn(ExperimentalLayoutApi::class)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ComplicationCommandRepository.knownComplications.forEach { def ->
                                    val isSelected = qqs2x1ComplicationIds.contains(def.id)
                                    val pillBg = if (isSelected) {
                                        ExpressiveTheme.colors.selectorSelected
                                    } else {
                                        ExpressiveTheme.colors.nestedElementSurface
                                    }
                                    val pillFg = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                    Row(
                                        modifier = Modifier
                                            .heightIn(min = 38.dp)
                                            .clip(RoundedCornerShape(percent = 50))
                                            .background(pillBg)
                                            .clickable {
                                                PanelPreferences.toggleQqs2x1ComplicationId(context, def.id)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            .testTag("qqs_2x1_picker_${def.id}"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = def.icon,
                                            contentDescription = def.title,
                                            tint = pillFg,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = def.title,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = pillFg,
                                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = pillFg,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 2.5. Legacy Tile Background / Shape Support Card
    val legacyTileShapesSupportCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("legacy_tile_shapes_support_card", CustomizeCategory.QS_DESIGN, "QS Tile Background Support") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("legacy_tile_shapes_support_card")
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "QS Tile Background Support",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                )
                            )
                            Text(
                                text = "Enable tile shapes & backgrounds for Android 8, Android 6, and Android 4.4.4 styles.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                )
                            )
                        }
                    }
                    Switch(
                        checked = enableLegacyTileShapes,
                        onCheckedChange = { PanelPreferences.setEnableLegacyTileShapes(context, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("legacy_tile_shapes_support_toggle")
                    )
                }
            }
        }
    }

    // 3. Tile Shape Customization (XML Paths)
    val tileShapeSelectionCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("tile_shape_selection_card", CustomizeCategory.QS_DESIGN, "Tile Shape Customization") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("tile_shape_selection_card")
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (collapseExpandOptions) Modifier.pinnableClickable { tileShapesExpanded = !tileShapesExpanded } else Modifier
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
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
                                text = "Tile Shapes (XML Paths)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "All QS shapes are rendered via vector XML drawable paths. You can paste path data, XML, or import files.",
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { showImportDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(com.example.ui.theme.getSettingsScreenBackgroundColor(isSystemInDarkTheme()))
                                .testTag("import_xml_shape_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Shape",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (collapseExpandOptions) {
                            IconButton(
                                onClick = { tileShapesExpanded = !tileShapesExpanded },
                                modifier = Modifier.size(36.dp).testTag("toggle_expand_tile_shapes")
                            ) {
                                Icon(
                                    imageVector = if (tileShapesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (tileShapesExpanded) "Collapse" else "Expand",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = !collapseExpandOptions || tileShapesExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Enabled Tile Shape
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Enabled Tile Shape",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Choose the shape when a tile is active/ON",
                            fontSize = 12.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
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
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Choose the shape when a tile is inactive/OFF",
                            fontSize = 12.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
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

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Complication Tile Shape
                val currentComplicationShapeId by PanelPreferences.complicationTileShapeId.collectAsState()
                val currentInactiveComplicationShapeId by PanelPreferences.inactiveComplicationTileShapeId.collectAsState()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Complication Tile Shape",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Choose the shape when a complication is active/ON",
                            fontSize = 12.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
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
                            val selected = (currentComplicationShapeId == shape.id)
                            val chipShape = SegmentedButtonShapes.getShape(
                                item = shape,
                                items = allShapes,
                                selectedItem = allShapes.firstOrNull { it.id == currentComplicationShapeId }
                            )
                            val composeShape = remember(shape.id) {
                                QsShapeManager.getComposeShape(shape.id)
                            }
                            FilterChip(
                                selected = selected,
                                onClick = { PanelPreferences.setComplicationTileShapeId(context, shape.id) },
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
                                modifier = Modifier.testTag("complication_tile_shape_${shape.id}")
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Inactive Complication Shape
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Inactive Complication Shape",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Choose the shape when a complication is inactive/OFF",
                            fontSize = 12.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
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
                            val selected = (currentInactiveComplicationShapeId == shape.id)
                            val chipShape = SegmentedButtonShapes.getShape(
                                item = shape,
                                items = allShapes,
                                selectedItem = allShapes.firstOrNull { it.id == currentInactiveComplicationShapeId }
                            )
                            val composeShape = remember(shape.id) {
                                QsShapeManager.getComposeShape(shape.id)
                            }
                            FilterChip(
                                selected = selected,
                                onClick = { PanelPreferences.setInactiveComplicationTileShapeId(context, shape.id) },
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
                                modifier = Modifier.testTag("inactive_complication_tile_shape_${shape.id}")
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
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
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
                                        if (currentComplicationShapeId == customShape.id) {
                                            PanelPreferences.setComplicationTileShapeId(context, "circle")
                                        }
                                        if (currentInactiveComplicationShapeId == customShape.id) {
                                            PanelPreferences.setInactiveComplicationTileShapeId(context, "circle")
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
        }
    }
    }
    }

    // 3.5. Complication Arc Thickness Card
    val complicationArcThicknessCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        val complicationArcThicknessDp by PanelPreferences.complicationArcThicknessDp.collectAsState()
        val formattedThickness = remember(complicationArcThicknessDp) {
            String.format(java.util.Locale.US, "%.1f dp", complicationArcThicknessDp)
        }
        PinnableCardWrapper("complication_arc_thickness_card", CustomizeCategory.QS_DESIGN, "Complication Arc Thickness") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("complication_arc_thickness_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 36.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-16).dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val badgeTrackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.22f)
                                val badgeArcColor = MaterialTheme.colorScheme.onPrimaryContainer
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.size(22.dp)) {
                                        val strokePx = (complicationArcThicknessDp * 0.65f).coerceIn(1f, 5.5f).dp.toPx()
                                        val inset = strokePx / 2f
                                        val arcSize = androidx.compose.ui.geometry.Size(
                                            width = size.width - inset * 2f,
                                            height = size.height - inset * 2f
                                        )
                                        val topLeft = Offset(inset, inset)
                                        drawArc(
                                            color = badgeTrackColor,
                                            startAngle = 0f,
                                            sweepAngle = 360f,
                                            useCenter = false,
                                            topLeft = topLeft,
                                            size = arcSize,
                                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                                        )
                                        drawArc(
                                            color = badgeArcColor,
                                            startAngle = -90f,
                                            sweepAngle = 250f,
                                            useCenter = false,
                                            topLeft = topLeft,
                                            size = arcSize,
                                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                                        )
                                    }
                                }
                                Text(
                                    text = "Complication Arc Thickness",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 17.sp,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = formattedThickness,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 16.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "Adjust the stroke width of progress rings on complication tiles.",
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Slider(
                        value = complicationArcThicknessDp,
                        onValueChange = { PanelPreferences.setComplicationArcThicknessDp(context, it) },
                        valueRange = 1.0f..8.0f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("complication_arc_thickness_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    }

    // 4. Tile Outline Card
    val tileOutlineCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        val complicationShapeId by PanelPreferences.complicationTileShapeId.collectAsState()
        val formattedThickness = remember(tileOutlineThicknessDp) {
            String.format(java.util.Locale.US, "%.1f dp", tileOutlineThicknessDp)
        }
        PinnableCardWrapper("tile_outline_card", CustomizeCategory.QS_DESIGN, "QS Tile Outline") {
            SettingsCard(
                shape = shape,
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
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CropFree,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Tile Outline",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Enable a stroke outline on Quick Settings tiles with the inner background hidden.",
                                fontSize = 12.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Separate Complication Outline toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Complication Outline",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Show an inside stroke outline instead of a filled background on complications.",
                                fontSize = 12.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = complicationOutlineEnabled,
                        onCheckedChange = { PanelPreferences.setComplicationOutlineEnabled(context, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("complication_outline_toggle")
                    )
                }

                AnimatedVisibility(
                    visible = tileOutlineEnabled || complicationOutlineEnabled,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    val enabledComposeShape = remember(currentEnabledTileShapeId) {
                        QsShapeManager.getComposeShape(currentEnabledTileShapeId)
                    }
                    val disabledComposeShape = remember(currentDisabledTileShapeId) {
                        QsShapeManager.getComposeShape(currentDisabledTileShapeId)
                    }
                    val complicationComposeShape = remember(complicationShapeId) {
                        QsShapeManager.getComposeShape(complicationShapeId)
                    }
                    val isDark = isSystemInDarkTheme()

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Outline Thickness",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = formattedThickness,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = tileOutlineThicknessDp,
                                onValueChange = { PanelPreferences.setTileOutlineThicknessDp(context, it) },
                                valueRange = 0.5f..6.0f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("tile_outline_thickness_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                )
                            )
                        }

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
                            if (tileOutlineEnabled) {
                                // Active Tile Preview
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(enabledComposeShape)
                                            .background(Color.Transparent)
                                            .insideShapeBorder(
                                                tileOutlineThicknessDp.dp,
                                                MaterialTheme.colorScheme.primary,
                                                enabledComposeShape
                                            ),
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
                                        text = "Active ($formattedThickness)",
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
                                            .clip(disabledComposeShape)
                                            .background(Color.Transparent)
                                            .insideShapeBorder(
                                                tileOutlineThicknessDp.dp,
                                                inactiveOutline,
                                                disabledComposeShape
                                            ),
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
                                        text = "Inactive ($formattedThickness)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (complicationOutlineEnabled) {
                                // Complication Outline Preview
                                val previewPrimary = MaterialTheme.colorScheme.primary
                                val previewTrack = previewPrimary.copy(alpha = 0.22f)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(complicationComposeShape)
                                            .background(Color.Transparent)
                                            .insideShapeBorder(
                                                tileOutlineThicknessDp.dp,
                                                previewPrimary,
                                                complicationComposeShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val insetFromOutline = tileOutlineThicknessDp.dp.toPx() + 1.5.dp.toPx()
                                            val arcStrokePx = 3.dp.toPx()
                                            val totalInset = insetFromOutline + arcStrokePx / 2f
                                            val arcSide = (minOf(size.width, size.height) - totalInset * 2f).coerceAtLeast(4f)
                                            val topLeft = Offset((size.width - arcSide) / 2f, (size.height - arcSide) / 2f)
                                            val arcSize = androidx.compose.ui.geometry.Size(arcSide, arcSide)
                                            drawArc(
                                                color = previewTrack,
                                                startAngle = 0f,
                                                sweepAngle = 360f,
                                                useCenter = false,
                                                topLeft = topLeft,
                                                size = arcSize,
                                                style = Stroke(width = arcStrokePx, cap = StrokeCap.Round)
                                            )
                                            drawArc(
                                                color = previewPrimary,
                                                startAngle = -90f,
                                                sweepAngle = 240f,
                                                useCenter = false,
                                                topLeft = topLeft,
                                                size = arcSize,
                                                style = Stroke(width = arcStrokePx, cap = StrokeCap.Round)
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.Speed,
                                            contentDescription = "Complication Outline Preview",
                                            tint = previewPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = "Complication ($formattedThickness)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = previewPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }

    // 5. QS Tile Diagonal Gradient Card
    val qsTileGradientCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("qs_tile_gradient_card", CustomizeCategory.QS_DESIGN, "Tile Gradient") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("qs_tile_gradient_card")
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gradient,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "QS Tile Diagonal Gradient",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Enable a smooth diagonal gradient from Primary to Tertiary Monet accent color across active tiles.",
                            fontSize = 12.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
    }
    }

    // --- 2. HEADER & BACKGROUNDS CARDS ---

    // Custom Header Image Banner Card
    val customHeaderImageCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("custom_header_image_card", CustomizeCategory.QS_DESIGN, "Custom Header Image") {
            SettingsCard(
                shape = shape,
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
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Custom Header Image Card",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                )
                            )
                            Text(
                                text = "Display a custom image banner card at the top of the Quick Settings panel.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                )
                            )
                        }
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
                                    onClick = {
                                        PanelPreferences.setCustomHeaderImageUri(context, "")
                                        try {
                                            val destFile = java.io.File(context.filesDir, "header_images/custom_header_image.jpg")
                                            if (destFile.exists()) destFile.delete()
                                        } catch (_: Exception) {}
                                    },
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
    }
    }

    // Header Monet Gradient Card
    val customHeaderMonetGradientCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("custom_header_monet_gradient_card", CustomizeCategory.QS_DESIGN, "Header Monet Gradient") {
            SettingsCard(
                shape = shape,
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
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Header Monet Gradient",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                )
                            )
                            Text(
                                text = "Add a dynamic Monet color gradient background to the header.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                )
                            )
                        }
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
                                color = MaterialTheme.colorScheme.onSurface,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                )
                            )
                            Text(
                                text = "${customHeaderMonetGradientHeight.toInt()} dp",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                )
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Solid Monet Surface",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                )
                            )
                            Text(
                                text = "Enables dynamic Monet color on the surface behind everything and removes transparency.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                )
                            )
                        }
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

                val monetSurfaceTransparency by PanelPreferences.monetSurfaceTransparency.collectAsState()
                val monetSurfaceTransparencyPercent = kotlin.math.round(monetSurfaceTransparency * 100f).toInt().coerceIn(0, 95)

                AnimatedVisibility(
                    visible = showMonetSurfaceBackground,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ExpressiveTheme.colors.nestedElementSurface)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Solid Monet Transparency",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "See-through amount for Monet background surface (0% – 95%)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "$monetSurfaceTransparencyPercent%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("monet_surface_transparency_value")
                            )
                        }
                        Slider(
                            value = monetSurfaceTransparencyPercent.toFloat(),
                            onValueChange = {
                                PanelPreferences.setMonetSurfaceTransparency(
                                    context,
                                    kotlin.math.round(it).coerceIn(0f, 95f) / 100f
                                )
                            },
                            valueRange = 0f..95f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("monet_surface_transparency_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    }
    }

    // Per-Element Accent Colors Preference Card
    val elementColorsPreferenceCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("element_colors_preference_card", CustomizeCategory.QS_DESIGN, "Per-Element Accent Colors") {
            SettingsCard(
                shape = shape,
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
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Per-Element Accent Colors",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            )
                        )
                        Text(
                            text = "Configure Primary or Tertiary Monet accent color for each UI element individually",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            )
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
    }
    }

    // Complication QS Tiles Card (Wear OS Complication + QS Hybrid integrated inside QS Tiles)
    val headerComplicationTilesCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        val complicationSublinePlacement by PanelPreferences.complicationSublinePlacement.collectAsState()
        var previewInGridMode by rememberSaveable { mutableStateOf(false) }
        var simulatedStepsProgress by rememberSaveable { mutableFloatStateOf(0.75f) }
        var simulatedWeatherProgress by rememberSaveable { mutableFloatStateOf(0.62f) }
        var expandedConfig by rememberSaveable { mutableStateOf(true) }

        PinnableCardWrapper("header_complication_tiles_card", CustomizeCategory.QS_DESIGN, "Complication QS Tiles") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("header_complication_tiles_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pinnableClickable { expandedConfig = !expandedConfig },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "Complication QS Tiles",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                )
                                Text(
                                    text = "Live circular Wear OS progress rings movable inside the QS grid & editor",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                )
                            }
                        }

                        Icon(
                            imageVector = if (expandedConfig) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expandedConfig) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(
                        visible = expandedConfig,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Battery, Storage, Brightness, and Volume complication tiles can be added, removed, or reordered directly in the Quick Settings Editor.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )

                            // Subline placement selector (Inside Ring vs Below Ring)
                            Text(
                                text = "SUBLINE PLACEMENT",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            val placements = com.example.model.ComplicationSublinePlacement.entries
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                placements.forEachIndexed { idx, placement ->
                                    val isSelected = complicationSublinePlacement == placement
                                    val segShape = SegmentedButtonShapes.getShape(idx, placements.size, isSelected)
                                    val segBg = if (isSelected) MaterialTheme.colorScheme.primary else com.example.ui.theme.getSettingsScreenBackgroundColor(com.example.ui.theme.LocalIsDarkTheme.current)
                                    val segFg = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(segShape)
                                            .background(segBg)
                                            .clickable {
                                                PanelPreferences.setComplicationSublinePlacement(context, placement)
                                            }
                                            .padding(vertical = 10.dp)
                                            .testTag("seg_complication_${placement.id}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = placement.label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = segFg
                                        )
                                    }
                                }
                            }

                            // Adaptive preview mode switcher (Header Row vs Grid Tile)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "LIVE PREVIEW (TAP TO ACTIVATE • HOLD FOR SETTINGS)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = previewInGridMode,
                                    onClick = { previewInGridMode = !previewInGridMode },
                                    label = {
                                        Text(
                                            text = if (previewInGridMode) "Grid Layout" else "Header Row",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    },
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.testTag("chip_complication_preview_mode")
                                )
                            }

                            val previewBg = com.example.ui.theme.getSettingsScreenBackgroundColor(com.example.ui.theme.LocalIsDarkTheme.current)
                            val liveItems = com.example.ui.tile.rememberLiveComplicationTiles(
                                sublinePlacement = complicationSublinePlacement,
                                layoutMode = if (previewInGridMode) com.example.model.ComplicationLayoutMode.GRID_TILE else com.example.model.ComplicationLayoutMode.HEADER_ROW
                            )
                            val stepK = (simulatedStepsProgress * 16f).toInt().coerceAtLeast(1)
                            val tempDeg = (12 + simulatedWeatherProgress * 22f).toInt()
                            val showcaseItems = remember(liveItems, simulatedStepsProgress, simulatedWeatherProgress, previewInGridMode, complicationSublinePlacement) {
                                if (!previewInGridMode) {
                                    liveItems
                                } else {
                                    listOf(
                                        liveItems[0],
                                        com.example.model.ComplicationTileModel(
                                            id = "preview_steps",
                                            title = "Step Goal",
                                            subline = "${stepK}k",
                                            progress = simulatedStepsProgress,
                                            icon = Icons.Default.AutoAwesome,
                                            accentColor = Color(0xFF00BFA5),
                                            isActive = simulatedStepsProgress >= 0.8f,
                                            sublinePlacement = complicationSublinePlacement,
                                            layoutMode = com.example.model.ComplicationLayoutMode.GRID_TILE,
                                            tapAction = com.example.model.ComplicationAction.Shortcut(
                                                shortcutId = "step_boost",
                                                onInvoke = {
                                                    simulatedStepsProgress = if (simulatedStepsProgress >= 0.95f) 0.25f else (simulatedStepsProgress + 0.2f).coerceAtMost(1f)
                                                }
                                            ),
                                            settingsAction = android.provider.Settings.ACTION_SETTINGS
                                        ),
                                        com.example.model.ComplicationTileModel(
                                            id = "preview_weather",
                                            title = "Weather",
                                            subline = "${tempDeg}°",
                                            progress = simulatedWeatherProgress,
                                            icon = Icons.Default.BrightnessMedium,
                                            accentColor = Color(0xFFFF7043),
                                            isActive = true,
                                            sublinePlacement = complicationSublinePlacement,
                                            layoutMode = com.example.model.ComplicationLayoutMode.GRID_TILE,
                                            tapAction = com.example.model.ComplicationAction.Shortcut(
                                                shortcutId = "weather_cycle",
                                                onInvoke = {
                                                    simulatedWeatherProgress = if (simulatedWeatherProgress >= 0.92f) 0.2f else (simulatedWeatherProgress + 0.18f).coerceAtMost(1f)
                                                }
                                            ),
                                            settingsAction = android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
                                        ),
                                        liveItems[1]
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(previewBg)
                                    .padding(10.dp)
                            ) {
                                if (!previewInGridMode) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        showcaseItems.forEach { item ->
                                            com.example.ui.tile.ComplicationTileView(
                                                model = item,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        showcaseItems.chunked(2).forEach { pair ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                pair.forEach { item ->
                                                    com.example.ui.tile.ComplicationTileView(
                                                        model = item,
                                                        showGridCardContainer = true,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- 4. BRIGHTNESS & SLIDERS CARDS ---

    // Move Brightness to Bottom Card
    val moveBrightnessBottomCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("move_brightness_bottom_card", CustomizeCategory.QS_DESIGN, "Move Brightness to Bottom") {
            SettingsCard(
                shape = shape
            ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 16.dp)
                    .offset(y = (-16).dp),
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerticalAlignBottom,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Move Brightness to Bottom",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            )
                        )
                        Text(
                            text = "Move the brightness slider bar to the bottom of the QS panel (just above the footer).",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            )
                        )
                    }
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
    }
    }

    // Show Brightness Slider in QQS Card
    val brightnessInQqsCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("brightness_in_qqs_card", CustomizeCategory.QS_DESIGN, "Brightness Slider in QQS") {
            SettingsCard(
                shape = shape
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BrightnessMedium,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Show Brightness Slider in QQS",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAndroidOreoStyle) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            )
                        )
                        Text(
                            text = if (isAndroidOreoStyle) "Disabled for Android 8.0 Oreo QS panel style." else "Show the brightness slider inside the compact QQS container view (always visible).",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            )
                        )
                    }
                }

                Switch(
                    checked = if (isAndroidOreoStyle) false else brightnessInQqs,
                    enabled = !isAndroidOreoStyle,
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
    }
    }

    // Brightness Bar Gradient Card
    val brightnessGradientCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("brightness_gradient_card", CustomizeCategory.QS_DESIGN, "Brightness Bar Gradient") {
            SettingsCard(
                shape = shape
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gradient,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Brightness Bar Gradient",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            )
                        )
                        Text(
                            text = "Apply a smooth horizontal Monet accent gradient across the active track of the brightness slider.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            )
                        )
                    }
                }

                Switch(
                    checked = showBrightnessGradient,
                    onCheckedChange = { PanelPreferences.setShowBrightnessGradient(context, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("brightness_gradient_toggle")
                )
            }
        }
    }
    }

    // Progress Bar & Slider Style Card
    val progressBarSliderStyleCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
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

        val m3TrackHeight by PanelPreferences.m3TrackHeight.collectAsState()
        val m3InactiveTrackHeight by PanelPreferences.m3InactiveTrackHeight.collectAsState()
        val m3OuterCornerRadius by PanelPreferences.m3OuterCornerRadius.collectAsState()
        val m3InnerCornerRadius by PanelPreferences.m3InnerCornerRadius.collectAsState()
        val m3ThumbWidth by PanelPreferences.m3ThumbWidth.collectAsState()
        val m3ThumbHeight by PanelPreferences.m3ThumbHeight.collectAsState()
        val m3ThumbGap by PanelPreferences.m3ThumbGap.collectAsState()
        val m3ShowStopIndicator by PanelPreferences.m3ShowStopIndicator.collectAsState()

        val currentProgressBarStyle = when (selectedSliderTarget) {
            QsSliderTarget.BRIGHTNESS -> if (sliderSyncAll) globalProgressBarStyle else brightnessSliderStyle
            QsSliderTarget.VOLUME -> if (sliderSyncAll) globalProgressBarStyle else volumeSliderStyle
            QsSliderTarget.MEDIA -> if (sliderSyncAll) globalProgressBarStyle else mediaSliderStyle
        }

        PinnableCardWrapper("progress_bar_slider_style_card", CustomizeCategory.QS_DESIGN, "Progress Bar & Slider Style") {
            SettingsCard(
                shape = shape
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (collapseExpandOptions) Modifier.pinnableClickable { progressBarSliderExpanded = !progressBarSliderExpanded } else Modifier
                            ),
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
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Progress Bar & Slider Style",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                )
                            )
                            Text(
                                text = "Customize the progress bar and slider appearance for Brightness, Volume, and Media Player. Configure all sliders together or customize each slider separately.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                )
                            )
                        }
                    }

                    if (collapseExpandOptions) {
                        IconButton(
                            onClick = { progressBarSliderExpanded = !progressBarSliderExpanded },
                            modifier = Modifier.size(36.dp).testTag("toggle_expand_progress_bar_slider")
                        ) {
                            Icon(
                                imageVector = if (progressBarSliderExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (progressBarSliderExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = !collapseExpandOptions || progressBarSliderExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Target Selector
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
                            color = MaterialTheme.colorScheme.onSurface,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            )
                        )
                        if (!sliderSyncAll) {
                            TextButton(
                                onClick = {
                                    PanelPreferences.setSliderSyncAll(context, true)
                                    Toast.makeText(context, "All sliders synced together", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "Sync All",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                                        lineHeightStyle = LineHeightStyle(
                                            alignment = LineHeightStyle.Alignment.Center,
                                            trim = LineHeightStyle.Trim.Both
                                        )
                                    )
                                )
                            }
                        }
                    }

                    val allTargets = QsSliderTarget.entries.filter { it != QsSliderTarget.VOLUME }
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
                                        fontWeight = if (isTargetSelected) FontWeight.Bold else FontWeight.Medium,
                                        style = TextStyle(
                                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                                            lineHeightStyle = LineHeightStyle(
                                                alignment = LineHeightStyle.Alignment.Center,
                                                trim = LineHeightStyle.Trim.Both
                                            )
                                        )
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
                                    containerColor = com.example.ui.theme.getSettingsScreenBackgroundColor(isSystemInDarkTheme()),
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onSurface
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both
                            )
                        )
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                // Style Selector Row
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
                            modifier = Modifier.testTag("progress_bar_style_${style.id}")
                        )
                    }
                }

                // Interactive live preview
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

                // Wave & Slider Customization Parameters
                AnimatedVisibility(
                    visible = true,
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
                                ProgressBarStyle.DEFAULT -> "M3 Slider Parameters"
                                ProgressBarStyle.ANDROID_12 -> "Android 12 Slider Parameters"
                                ProgressBarStyle.WAVY -> "Wave & Slider Parameters"
                                ProgressBarStyle.STRAIGHT -> "Straight Slider Parameters"
                            },
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (currentProgressBarStyle == ProgressBarStyle.DEFAULT) {
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
                                        text = "${String.format("%.1f", m3TrackHeight)} dp",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = m3TrackHeight.coerceIn(4.0f, 32.0f),
                                    onValueChange = { PanelPreferences.setM3TrackHeight(context, it) },
                                    valueRange = 4.0f..32.0f,
                                    steps = 27,
                                    modifier = Modifier.fillMaxWidth().testTag("m3_track_height_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

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
                                        text = "${String.format("%.1f", m3InactiveTrackHeight)} dp",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = m3InactiveTrackHeight.coerceIn(4.0f, 32.0f),
                                    onValueChange = { PanelPreferences.setM3InactiveTrackHeight(context, it) },
                                    valueRange = 4.0f..32.0f,
                                    steps = 27,
                                    modifier = Modifier.fillMaxWidth().testTag("m3_inactive_track_height_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Outer Corner Radius",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${m3OuterCornerRadius.toInt()} dp",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = m3OuterCornerRadius.coerceIn(0.0f, 16.0f),
                                    onValueChange = { PanelPreferences.setM3OuterCornerRadius(context, it) },
                                    valueRange = 0.0f..16.0f,
                                    steps = 15,
                                    modifier = Modifier.fillMaxWidth().testTag("m3_outer_corner_radius_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Inner Corner Radius",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${m3InnerCornerRadius.toInt()} dp",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = m3InnerCornerRadius.coerceIn(0.0f, 16.0f),
                                    onValueChange = { PanelPreferences.setM3InnerCornerRadius(context, it) },
                                    valueRange = 0.0f..16.0f,
                                    steps = 15,
                                    modifier = Modifier.fillMaxWidth().testTag("m3_inner_corner_radius_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Drag Handle Width",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${String.format("%.1f", m3ThumbWidth)} dp",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = m3ThumbWidth.coerceIn(2.0f, 12.0f),
                                    onValueChange = { PanelPreferences.setM3ThumbWidth(context, it) },
                                    valueRange = 2.0f..12.0f,
                                    steps = 19,
                                    modifier = Modifier.fillMaxWidth().testTag("m3_thumb_width_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Drag Handle Height",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${String.format("%.1f", m3ThumbHeight)} dp",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = m3ThumbHeight.coerceIn(14.0f, 40.0f),
                                    onValueChange = { PanelPreferences.setM3ThumbHeight(context, it) },
                                    valueRange = 14.0f..40.0f,
                                    steps = 25,
                                    modifier = Modifier.fillMaxWidth().testTag("m3_thumb_height_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Handle-Track Gap",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${String.format("%.1f", m3ThumbGap)} dp",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = m3ThumbGap.coerceIn(0.0f, 12.0f),
                                    onValueChange = { PanelPreferences.setM3ThumbGap(context, it) },
                                    valueRange = 0.0f..12.0f,
                                    steps = 23,
                                    modifier = Modifier.fillMaxWidth().testTag("m3_thumb_gap_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f).padding(end = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "Show End Stop Indicator",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Display a subtle dot at the far end of the inactive track.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = m3ShowStopIndicator,
                                    onCheckedChange = { PanelPreferences.setM3ShowStopIndicator(context, it) },
                                    modifier = Modifier.testTag("m3_show_stop_indicator_toggle")
                                )
                            }
                        }

                        if (currentProgressBarStyle == ProgressBarStyle.ANDROID_12) {
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
                            }

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
                            }
                        }

                        if (currentProgressBarStyle == ProgressBarStyle.WAVY) {
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
                            }

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
                            }

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
                            }

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
                            }

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
                            }

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
                            }

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
                            }
                        }

                        if (currentProgressBarStyle == ProgressBarStyle.STRAIGHT) {
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
                            }

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
                            }

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
                            }

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
                            }
                        }

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
        }
    }
    }
    }

    // --- 5. PANEL CORNER STYLING CARDS ---

    // QS, QQS & Notifications Panel Corners Card
    val panelCornerStylingCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("panel_corner_styling_card", CustomizeCategory.QS_DESIGN, "Panel Corners") {
            SettingsCard(
                shape = shape
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (collapseExpandOptions) Modifier.pinnableClickable { panelCornersExpanded = !panelCornersExpanded } else Modifier
                            ),
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
                                imageVector = Icons.Default.RoundedCorner,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "QS, QQS & Notifications Panel Corners",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                )
                            )
                            Text(
                                text = "Customize inner and outer corner radii and toggle global panel corner sizing for Quick Settings, QQS and notification panels only.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                )
                            )
                        }
                    }

                    if (collapseExpandOptions) {
                        IconButton(
                            onClick = { panelCornersExpanded = !panelCornersExpanded },
                            modifier = Modifier.size(36.dp).testTag("toggle_expand_panel_corners")
                        ) {
                            Icon(
                                imageVector = if (panelCornersExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (panelCornersExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = !collapseExpandOptions || panelCornersExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

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
                            text = "Global Corner Size",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            )
                        )
                        Text(
                            text = "Sync inner corner size automatically with outer corner size",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            )
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
                                color = MaterialTheme.colorScheme.onSurface,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                )
                            )
                            Text(
                                text = "${panelOuterCorner.toInt()} dp",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                )
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
                    }

                    if (panelGlobalCornerEnabled) {
                        Text(
                            text = "Inner corner size is synced to outer corner size (${panelOuterCorner.toInt()} dp)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            )
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
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                                        lineHeightStyle = LineHeightStyle(
                                            alignment = LineHeightStyle.Alignment.Center,
                                            trim = LineHeightStyle.Trim.Both
                                        )
                                    )
                                )
                                Text(
                                    text = "${panelInnerCorner.toInt()} dp",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                                        lineHeightStyle = LineHeightStyle(
                                            alignment = LineHeightStyle.Alignment.Center,
                                            trim = LineHeightStyle.Trim.Both
                                        )
                                    )
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
                        }
                    }
                }
                    }
                }
            }
        }
    }
    }

    val panelTopRimCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("panel_top_rim_card", CustomizeCategory.QS_DESIGN, "Panel Top Rim") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("panel_top_rim_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "Panel Top Rim",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 17.sp,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Draw a rim highlight and top-to-bottom panel background gradient fading from top to bottom.",
                                    fontSize = 12.sp,
                                    lineHeight = 15.sp,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = enablePanelTopRim,
                            onCheckedChange = { PanelPreferences.setEnablePanelTopRim(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("panel_top_rim_toggle")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Rim Sensitivity",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Top highlight opacity (5% – 30%)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${topRimPercent}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("rim_sensitivity_value_text")
                            )
                        }
                        Slider(
                            value = topRimPercent.toFloat(),
                            onValueChange = {
                                PanelPreferences.setTopRimAlpha(context, kotlin.math.round(it).coerceIn(5f, 30f) / 100f)
                            },
                            valueRange = 5f..30f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("rim_sensitivity_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    }

    val tileTopRimCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("tile_top_rim_card", CustomizeCategory.QS_DESIGN, "Tile & Complication Top Rim") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("tile_top_rim_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "Tile & Complication Top Rim",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 17.sp,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Draw a rim highlight and top-to-bottom background gradient on QS tiles and complications.",
                                    fontSize = 12.sp,
                                    lineHeight = 15.sp,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = enableTileTopRim,
                            onCheckedChange = { PanelPreferences.setEnableTileTopRim(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("tile_top_rim_toggle")
                        )
                    }

                    AnimatedVisibility(visible = enableTileTopRim) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Rim Sensitivity",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Top highlight opacity (5% – 30%)",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${topRimPercent}%",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = topRimPercent.toFloat(),
                                    onValueChange = {
                                        PanelPreferences.setTopRimAlpha(context, kotlin.math.round(it).coerceIn(5f, 30f) / 100f)
                                    },
                                    valueRange = 5f..30f,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("tile_rim_sensitivity_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    val clearAllFabCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        var clearAllFabExpanded by rememberSaveable { mutableStateOf(false) }
        PinnableCardWrapper("clear_all_fab_card", CustomizeCategory.QS_DESIGN, "Clear All FAB Button") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("clear_all_fab_card")
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (collapseExpandOptions) Modifier.pinnableClickable { clearAllFabExpanded = !clearAllFabExpanded } else Modifier
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text(
                                    text = "Clear All FAB Button",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 17.sp,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Replace the text pill clear all button with a customizable FAB button.",
                                    fontSize = 12.sp,
                                    lineHeight = 15.sp,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (collapseExpandOptions) {
                                IconButton(
                                    onClick = { clearAllFabExpanded = !clearAllFabExpanded },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("toggle_expand_clear_all_fab")
                                ) {
                                    Icon(
                                        imageVector = if (clearAllFabExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (clearAllFabExpanded) "Collapse" else "Expand",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = useClearAllFab,
                                onCheckedChange = { PanelPreferences.setUseClearAllFab(context, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.testTag("use_clear_all_fab_toggle")
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = !collapseExpandOptions || clearAllFabExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (useClearAllFab) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                // 1. FAB Alignment Position
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "FAB Position",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val allPositions = ClearAllFabPosition.entries
                                        allPositions.forEach { pos ->
                                            val isSelected = clearAllFabPosition == pos
                                            val chipShape = SegmentedButtonShapes.getShape(
                                                item = pos,
                                                items = allPositions,
                                                selectedItem = clearAllFabPosition
                                            )
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { PanelPreferences.setClearAllFabPosition(context, pos) },
                                                label = { Text(pos.label, fontSize = 11.sp) },
                                                shape = chipShape,
                                                border = null,
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                    containerColor = MaterialTheme.colorScheme.background,
                                                    labelColor = MaterialTheme.colorScheme.onSurface
                                                ),
                                                modifier = Modifier.testTag("clear_all_fab_pos_${pos.id}")
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                // 2. Custom FAB Shape Selection (XML Paths from QsShapeManager)
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(2.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "FAB Shape (XML Paths)",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Tile-style vector XML shapes applied to the FAB",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { showFabImportDialog = true },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.testTag("import_fab_xml_shape_button"),
                                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                                containerColor = com.example.ui.theme.getSettingsScreenBackgroundColor(isSystemInDarkTheme())
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Add Shape",
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Add Shape",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(start = 4.dp)
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        allShapes.forEach { shape ->
                                            val isSelected = (clearAllFabShapeId == shape.id)
                                            val chipShape = SegmentedButtonShapes.getShape(
                                                item = shape,
                                                items = allShapes,
                                                selectedItem = allShapes.firstOrNull { it.id == clearAllFabShapeId }
                                            )
                                            val composeShape = remember(shape.id) {
                                                QsShapeManager.getComposeShape(shape.id)
                                            }
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { PanelPreferences.setClearAllFabShapeId(context, shape.id) },
                                                label = {
                                                    Text(
                                                        text = shape.label,
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                    )
                                                },
                                                leadingIcon = {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clip(composeShape)
                                                            .background(
                                                                if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                                else MaterialTheme.colorScheme.primary
                                                            )
                                                    )
                                                },
                                                shape = chipShape,
                                                border = null,
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                                                    containerColor = MaterialTheme.colorScheme.background,
                                                    labelColor = MaterialTheme.colorScheme.onSurface
                                                ),
                                                modifier = Modifier.testTag("clear_all_fab_shape_${shape.id}")
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            // Single Unified Clear All Icon Selection
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Clear All Icon",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Choose the icon displayed on the Clear All button",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val allIcons = ClearAllFabIcon.entries
                                    allIcons.forEach { iconItem ->
                                        val isSelected = clearAllFabIcon == iconItem
                                        val chipShape = SegmentedButtonShapes.getShape(
                                            item = iconItem,
                                            items = allIcons,
                                            selectedItem = clearAllFabIcon
                                        )
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { PanelPreferences.setClearAllFabIcon(context, iconItem) },
                                            leadingIcon = {
                                                Icon(
                                                    painter = androidx.compose.ui.res.painterResource(id = iconItem.drawableRes),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                                )
                                            },
                                            label = { Text(iconItem.label, fontSize = 11.sp) },
                                            shape = chipShape,
                                            border = null,
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                                                containerColor = MaterialTheme.colorScheme.background,
                                                labelColor = MaterialTheme.colorScheme.onSurface,
                                                iconColor = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier.testTag("clear_all_icon_choice_${iconItem.id}")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val showMediaInNotifContainerCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("show_media_in_notif_container_card", CustomizeCategory.QS_DESIGN, "Music Player Notification") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("show_media_in_notif_container_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
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
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Media Player Style",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 17.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Select media player notification style",
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    val currentOption = when {
                        !showMediaInNotifContainer -> "Android 11"
                        mediaNotificationStyle == MediaNotificationStyle.COMPACT -> "Android 6"
                        else -> "Android 10"
                    }

                    val allOptions = listOf("Android 11", "Android 10", "Android 6")

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        allOptions.forEach { option ->
                            val isSelected = (currentOption == option)
                            val chipShape = SegmentedButtonShapes.getShape(
                                item = option,
                                items = allOptions,
                                selectedItem = currentOption
                            )
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    when (option) {
                                        "Android 11" -> {
                                            PanelPreferences.setShowMediaInNotificationContainer(context, false)
                                            PanelPreferences.setMediaNotificationStyle(context, MediaNotificationStyle.ANDROID_10)
                                        }
                                        "Android 10" -> {
                                            PanelPreferences.setShowMediaInNotificationContainer(context, true)
                                            PanelPreferences.setMediaNotificationStyle(context, MediaNotificationStyle.ANDROID_10)
                                        }
                                        "Android 6" -> {
                                            PanelPreferences.setShowMediaInNotificationContainer(context, true)
                                            PanelPreferences.setMediaNotificationStyle(context, MediaNotificationStyle.COMPACT)
                                        }
                                    }
                                },
                                shape = chipShape,
                                border = null,
                                label = {
                                    Text(
                                        text = option,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        style = TextStyle(
                                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                                            lineHeightStyle = LineHeightStyle(
                                                alignment = LineHeightStyle.Alignment.Center,
                                                trim = LineHeightStyle.Trim.Both
                                            )
                                        )
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = com.example.ui.theme.getSettingsScreenBackgroundColor(isSystemInDarkTheme()),
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    val compactMediaNotificationCard = showMediaInNotifContainerCard

    val footerIconBackgroundCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("footer_icon_background_card", CustomizeCategory.QS_DESIGN, "Footer Icon Backgrounds") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("footer_icon_background_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = "Footer Icon Backgrounds",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 17.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Enable M3 rounded backgrounds for QS footer action buttons with 10dp outer and 3dp inner corners.",
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    val footerIconBgEnabled by PanelPreferences.footerIconBackground.collectAsState()
                    val footerOuterSpacingEnabled by PanelPreferences.footerOuterSpacing.collectAsState()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enable Footer Icon Backgrounds",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = footerIconBgEnabled,
                            onCheckedChange = { PanelPreferences.setFooterIconBackground(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("footer_icon_bg_toggle")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Outer 3dp Spacing Between Buttons",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Adds 3dp spacing with 3dp inner corners. When disabled, spacing and inner corners are 0dp.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = footerOuterSpacingEnabled,
                            onCheckedChange = { PanelPreferences.setFooterOuterSpacing(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("footer_outer_spacing_toggle")
                        )
                    }

                    if (footerIconBgEnabled) {
                        val footerOuterCorner by PanelPreferences.footerOuterCorner.collectAsState()
                        val footerInnerCorner by PanelPreferences.footerInnerCorner.collectAsState()
                        val footerSpacing by PanelPreferences.footerSpacing.collectAsState()

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // 1. Outer Corner Radius Slider
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Outer Corner Radius",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Exposed ends corner curvature",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${footerOuterCorner.toInt()} dp",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = footerOuterCorner,
                                onValueChange = { PanelPreferences.setFooterOuterCorner(context, Math.round(it).toFloat()) },
                                valueRange = 0f..24f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("footer_outer_corner_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                )
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // 2. Inner Corner Radius Slider
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Inner Corner Radius",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Adjacent inner edges curvature",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${footerInnerCorner.toInt()} dp",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = footerInnerCorner,
                                onValueChange = { PanelPreferences.setFooterInnerCorner(context, Math.round(it).toFloat()) },
                                valueRange = 0f..20f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("footer_inner_corner_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                )
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // 3. Spacing Between Buttons Slider
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Button Spacing",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Gap between footer button backgrounds",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${footerSpacing.toInt()} dp",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = footerSpacing,
                                onValueChange = { PanelPreferences.setFooterSpacing(context, Math.round(it).toFloat()) },
                                valueRange = 0f..20f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("footer_spacing_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    val frostedGlassCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("frosted_glass_panel_card", CustomizeCategory.QS_DESIGN, "Frosted Glass Panel Backdrop") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("frosted_glass_panel_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = "Frosted Glass Panel Backdrop",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 17.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Enable container blur and frosted glass visual effect for QS panel background containers, notifications, and surface cards.",
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    val containerBlurEnabled by PanelPreferences.enableContainerBlur.collectAsState()
                    val blurOnlyBehindQsCard by PanelPreferences.blurOnlyBehindQsCard.collectAsState()
                    val notificationBlurEnabled by PanelPreferences.enableNotificationBlur.collectAsState()
                    val containerBlurRadiusDp by PanelPreferences.containerBlurRadiusDp.collectAsState()
                    val containerBlurTransparency by PanelPreferences.containerBlurTransparency.collectAsState()
                    val transparencyPercent = kotlin.math.round(containerBlurTransparency * 100f).toInt().coerceIn(0, 95)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enable Frosted Glass Blur",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = containerBlurEnabled,
                            onCheckedChange = { PanelPreferences.setEnableContainerBlur(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("frosted_glass_toggle")
                        )
                    }

                    AnimatedVisibility(
                        visible = containerBlurEnabled,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(ExpressiveTheme.colors.nestedElementSurface)
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Blur Sensitivity",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Backdrop blur intensity (4 – 60 dp)",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${kotlin.math.round(containerBlurRadiusDp).toInt()} dp",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.testTag("frosted_glass_blur_sensitivity_value")
                                    )
                                }
                                Slider(
                                    value = containerBlurRadiusDp,
                                    onValueChange = {
                                        PanelPreferences.setContainerBlurRadiusDp(
                                            context,
                                            kotlin.math.round(it).coerceIn(4f, 60f)
                                        )
                                    },
                                    valueRange = 4f..60f,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("frosted_glass_blur_sensitivity_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                    )
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(ExpressiveTheme.colors.nestedElementSurface)
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Transparency",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Container glass see-through amount (0% – 95%)",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "$transparencyPercent%",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.testTag("frosted_glass_transparency_value")
                                    )
                                }
                                Slider(
                                    value = transparencyPercent.toFloat(),
                                    onValueChange = {
                                        PanelPreferences.setContainerBlurTransparency(
                                            context,
                                            kotlin.math.round(it).coerceIn(0f, 95f) / 100f
                                        )
                                    },
                                    valueRange = 0f..95f,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("frosted_glass_transparency_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                    )
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "Blur Behind QS/QQS Only",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Keep the rest of the screen sharp and apply blur only directly underneath the QS/QQS panel.",
                                        fontSize = 11.5.sp,
                                        lineHeight = 14.sp,
                                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = blurOnlyBehindQsCard,
                                    onCheckedChange = { PanelPreferences.setBlurOnlyBehindQsCard(context, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier.testTag("frosted_glass_qs_only_toggle")
                                )
                            }

                            AnimatedVisibility(
                                visible = blurOnlyBehindQsCard,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 12.dp),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = "Notification Card Blur",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Apply frosted glass and bounded backdrop blur directly behind individual notification cards.",
                                                fontSize = 11.5.sp,
                                                lineHeight = 14.sp,
                                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Switch(
                                            checked = notificationBlurEnabled,
                                            onCheckedChange = { PanelPreferences.setEnableNotificationBlur(context, it) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                                            ),
                                            modifier = Modifier.testTag("notification_card_blur_toggle")
                                        )
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                    val enableRefraction by PanelPreferences.enableGlassRefraction.collectAsState()
                                    val refractionSensitivity by PanelPreferences.glassRefractionSensitivity.collectAsState()
                                    val refractionPercent = kotlin.math.round(refractionSensitivity * 100f).toInt().coerceIn(10, 100)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 12.dp),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = "Glass Edge Refraction",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Real-time AGSL optical glass lens refraction and chromatic edge dispersion.",
                                                fontSize = 11.5.sp,
                                                lineHeight = 14.sp,
                                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Switch(
                                            checked = enableRefraction,
                                            onCheckedChange = { PanelPreferences.setEnableGlassRefraction(context, it) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                                            ),
                                            modifier = Modifier.testTag("glass_refraction_toggle")
                                        )
                                    }

                                    AnimatedVisibility(
                                        visible = enableRefraction,
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut() + shrinkVertically()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(ExpressiveTheme.colors.nestedElementSurface)
                                                .padding(horizontal = 14.dp, vertical = 12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Refraction Sensitivity",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "Optical lens warp & edge dispersion strength (10% – 100%)",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Text(
                                                    text = "$refractionPercent%",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.testTag("glass_refraction_sensitivity_value")
                                                )
                                            }
                                            Slider(
                                                value = refractionPercent.toFloat(),
                                                onValueChange = {
                                                    PanelPreferences.setGlassRefractionSensitivity(
                                                        context,
                                                        kotlin.math.round(it).coerceIn(10f, 100f) / 100f
                                                    )
                                                },
                                                valueRange = 10f..100f,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("glass_refraction_sensitivity_slider"),
                                                colors = SliderDefaults.colors(
                                                    thumbColor = MaterialTheme.colorScheme.primary,
                                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val headerStatusBackgroundCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("header_status_background_card", CustomizeCategory.QS_DESIGN, "Header & Status Backgrounds") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("header_status_background_card")
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
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = "Header & Status Backgrounds",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 17.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Toggle rounded backgrounds and corner radius separately for date, battery, clock, and carrier.",
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    val dateBgEnabled by PanelPreferences.dateBackgroundEnabled.collectAsState()
                    val dateBgCorner by PanelPreferences.dateBackgroundCorner.collectAsState()
                    val batteryBgEnabled by PanelPreferences.batteryBackgroundEnabled.collectAsState()
                    val batteryBgCorner by PanelPreferences.batteryBackgroundCorner.collectAsState()
                    val clockBgEnabled by PanelPreferences.clockBackgroundEnabled.collectAsState()
                    val clockBgCorner by PanelPreferences.clockBackgroundCorner.collectAsState()
                    val carrierBgEnabled by PanelPreferences.carrierBackgroundEnabled.collectAsState()
                    val carrierBgCorner by PanelPreferences.carrierBackgroundCorner.collectAsState()

                    // 1. Date Background & Corner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Date Background",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = dateBgEnabled,
                            onCheckedChange = { PanelPreferences.setDateBackgroundEnabled(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("date_bg_toggle")
                        )
                    }
                    AnimatedVisibility(visible = dateBgEnabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Date Corner Radius",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${dateBgCorner.toInt()} dp",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = dateBgCorner,
                                onValueChange = { PanelPreferences.setDateBackgroundCorner(context, Math.round(it).toFloat()) },
                                valueRange = 0f..24f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("date_bg_corner_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // 2. Battery Background & Corner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Battery Background",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = batteryBgEnabled,
                            onCheckedChange = { PanelPreferences.setBatteryBackgroundEnabled(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("battery_bg_toggle")
                        )
                    }
                    AnimatedVisibility(visible = batteryBgEnabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Battery Corner Radius",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${batteryBgCorner.toInt()} dp",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = batteryBgCorner,
                                onValueChange = { PanelPreferences.setBatteryBackgroundCorner(context, Math.round(it).toFloat()) },
                                valueRange = 0f..24f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("battery_bg_corner_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // 3. Clock Background & Corner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Clock Background",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = clockBgEnabled,
                            onCheckedChange = { PanelPreferences.setClockBackgroundEnabled(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("clock_bg_toggle")
                        )
                    }
                    AnimatedVisibility(visible = clockBgEnabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Clock Corner Radius",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${clockBgCorner.toInt()} dp",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = clockBgCorner,
                                onValueChange = { PanelPreferences.setClockBackgroundCorner(context, Math.round(it).toFloat()) },
                                valueRange = 0f..24f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("clock_bg_corner_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // 4. Carrier Label Background & Corner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Carrier Label Background",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = carrierBgEnabled,
                            onCheckedChange = { PanelPreferences.setCarrierBackgroundEnabled(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("carrier_bg_toggle")
                        )
                    }
                    AnimatedVisibility(visible = carrierBgEnabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Carrier Label Corner Radius",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${carrierBgCorner.toInt()} dp",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = carrierBgCorner,
                                onValueChange = { PanelPreferences.setCarrierBackgroundCorner(context, Math.round(it).toFloat()) },
                                valueRange = 0f..24f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("carrier_bg_corner_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    val pageIndicatorStyleCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("qs_page_indicator_style_card", CustomizeCategory.QS_DESIGN, "QS Page Indicator Style") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("qs_page_indicator_style_card")
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
                                imageVector = Icons.Default.DragHandle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = "QS Page Indicator Style",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 17.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = pageIndicatorStyle.description,
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    val piActiveLineThickness by PanelPreferences.piActiveLineThickness.collectAsState()
                    val piInactiveLineThickness by PanelPreferences.piInactiveLineThickness.collectAsState()
                    val piActiveLineWidth by PanelPreferences.piActiveLineWidth.collectAsState()
                    val piInactiveLineWidth by PanelPreferences.piInactiveLineWidth.collectAsState()
                    val piActiveDotRadius by PanelPreferences.piActiveDotRadius.collectAsState()
                    val piInactiveDotRadius by PanelPreferences.piInactiveDotRadius.collectAsState()
                    val piActiveSlidingThickness by PanelPreferences.piActiveSlidingThickness.collectAsState()
                    val piInactiveSlidingThickness by PanelPreferences.piInactiveSlidingThickness.collectAsState()

                    // Live interactive preview of the selected indicator style
                    var previewPage by remember { mutableIntStateOf(0) }
                    val animatedPreviewPos by animateFloatAsState(
                        targetValue = previewPage.toFloat(),
                        animationSpec = tween(durationMillis = 240),
                        label = "preview_page_indicator"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .pinnableClickable { previewPage = (previewPage + 1) % 3 }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Preview (tap)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        QsPageIndicator(
                            pageCount = 3,
                            scrollPosition = animatedPreviewPos,
                            style = pageIndicatorStyle,
                            activeColor = MaterialTheme.colorScheme.primary,
                            inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f),
                            onPageClick = { previewPage = it },
                            modifier = Modifier.testTag("page_indicator_preview"),
                            activeLineThickness = piActiveLineThickness,
                            inactiveLineThickness = piInactiveLineThickness,
                            activeLineWidth = piActiveLineWidth,
                            inactiveLineWidth = piInactiveLineWidth,
                            activeDotRadius = piActiveDotRadius,
                            inactiveDotRadius = piInactiveDotRadius,
                            activeSlidingThickness = piActiveSlidingThickness,
                            inactiveSlidingThickness = piInactiveSlidingThickness
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val allIndicatorStyles = QsPageIndicatorStyle.entries
                        allIndicatorStyles.forEach { style ->
                            val isSelected = pageIndicatorStyle == style
                            val chipShape = SegmentedButtonShapes.getShape(
                                item = style,
                                items = allIndicatorStyles,
                                selectedItem = pageIndicatorStyle
                            )
                            FilterChip(
                                selected = isSelected,
                                onClick = { PanelPreferences.setQsPageIndicatorStyle(context, style) },
                                label = { Text(style.label, fontSize = 11.sp) },
                                shape = chipShape,
                                border = null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.background,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.testTag("page_indicator_style_${style.id}")
                            )
                        }
                    }

                    @Composable
                    fun indicatorSlider(
                        title: String,
                        value: Float,
                        valueRange: ClosedFloatingPointRange<Float>,
                        stepHalfDp: Boolean,
                        testTagName: String,
                        onValueChange: (Float) -> Unit
                    ) {
                        val displayVal = if (value % 1f == 0f) {
                            "${value.toInt()} dp"
                        } else {
                            "${String.format(java.util.Locale.US, "%.1f", value)} dp"
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = displayVal,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = value,
                                onValueChange = { raw ->
                                    val snapped = if (stepHalfDp) {
                                        kotlin.math.round(raw * 2f) / 2f
                                    } else {
                                        kotlin.math.round(raw)
                                    }
                                    onValueChange(snapped)
                                },
                                valueRange = valueRange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag(testTagName),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    when (pageIndicatorStyle) {
                        QsPageIndicatorStyle.DOTS -> {
                            indicatorSlider(
                                title = "Active Dot Radius",
                                value = piActiveDotRadius,
                                valueRange = 1.5f..8f,
                                stepHalfDp = true,
                                testTagName = "pi_active_dot_radius_slider"
                            ) { PanelPreferences.setPiActiveDotRadius(context, it) }

                            indicatorSlider(
                                title = "Inactive Dot Radius",
                                value = piInactiveDotRadius,
                                valueRange = 1.0f..6f,
                                stepHalfDp = true,
                                testTagName = "pi_inactive_dot_radius_slider"
                            ) { PanelPreferences.setPiInactiveDotRadius(context, it) }
                        }
                        QsPageIndicatorStyle.HORIZONTAL_LINES -> {
                            indicatorSlider(
                                title = "Active Line Thickness",
                                value = piActiveLineThickness,
                                valueRange = 1f..10f,
                                stepHalfDp = true,
                                testTagName = "pi_active_line_thickness_slider"
                            ) { PanelPreferences.setPiActiveLineThickness(context, it) }

                            indicatorSlider(
                                title = "Inactive Line Thickness",
                                value = piInactiveLineThickness,
                                valueRange = 1f..10f,
                                stepHalfDp = true,
                                testTagName = "pi_inactive_line_thickness_slider"
                            ) { PanelPreferences.setPiInactiveLineThickness(context, it) }

                            indicatorSlider(
                                title = "Active Line Width",
                                value = piActiveLineWidth,
                                valueRange = 8f..48f,
                                stepHalfDp = false,
                                testTagName = "pi_active_line_width_slider"
                            ) { PanelPreferences.setPiActiveLineWidth(context, it) }

                            indicatorSlider(
                                title = "Inactive Line Width",
                                value = piInactiveLineWidth,
                                valueRange = 4f..32f,
                                stepHalfDp = false,
                                testTagName = "pi_inactive_line_width_slider"
                            ) { PanelPreferences.setPiInactiveLineWidth(context, it) }
                        }
                        QsPageIndicatorStyle.DOTS_WITH_ACTIVE_LINE -> {
                            indicatorSlider(
                                title = "Active Line Thickness",
                                value = piActiveLineThickness,
                                valueRange = 1f..10f,
                                stepHalfDp = true,
                                testTagName = "pi_active_line_thickness_slider"
                            ) { PanelPreferences.setPiActiveLineThickness(context, it) }

                            indicatorSlider(
                                title = "Active Line Width",
                                value = piActiveLineWidth,
                                valueRange = 8f..48f,
                                stepHalfDp = false,
                                testTagName = "pi_active_line_width_slider"
                            ) { PanelPreferences.setPiActiveLineWidth(context, it) }

                            indicatorSlider(
                                title = "Inactive Dot Radius",
                                value = piInactiveDotRadius,
                                valueRange = 1.0f..6f,
                                stepHalfDp = true,
                                testTagName = "pi_inactive_dot_radius_slider"
                            ) { PanelPreferences.setPiInactiveDotRadius(context, it) }
                        }
                        QsPageIndicatorStyle.SLIDING_TRACK -> {
                            indicatorSlider(
                                title = "Active Sliding Line Thickness",
                                value = piActiveSlidingThickness,
                                valueRange = 1f..12f,
                                stepHalfDp = true,
                                testTagName = "pi_active_sliding_thickness_slider"
                            ) { PanelPreferences.setPiActiveSlidingThickness(context, it) }

                            indicatorSlider(
                                title = "Inactive Sliding Line Thickness",
                                value = piInactiveSlidingThickness,
                                valueRange = 1f..12f,
                                stepHalfDp = true,
                                testTagName = "pi_inactive_sliding_thickness_slider"
                            ) { PanelPreferences.setPiInactiveSlidingThickness(context, it) }
                        }
                    }
                }
            }
        }
    }

    val showBrightnessControlsBackgroundCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("show_brightness_controls_background_card", CustomizeCategory.QS_DESIGN, "Brightness Controls Background") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("show_brightness_controls_background_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BrightnessMedium,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = "Brightness Controls Background",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 17.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Place the brightness icon, -/+ buttons, and percentage inside pill backgrounds.",
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = showBrightnessControlsBackground,
                        onCheckedChange = { PanelPreferences.setShowBrightnessControlsBackground(context, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("show_brightness_controls_background_toggle")
                    )
                }
            }
        }
    }

    val pinnedCardsMap = mapOf<String, @Composable (androidx.compose.ui.graphics.Shape) -> Unit>(
        "panel_style_card" to panelStyleCard,
        "android_oreo_style_card" to androidOreoStyleCard,
        "qqs_icon_size_card" to qqsIconSizeCard,
        "qqs_icon_quantity_card" to qqsIconQuantityCard,
        "qqs_complications_row_card" to qqsComplicationsRowCard,
        "legacy_tile_shapes_support_card" to legacyTileShapesSupportCard,
        "tile_shape_selection_card" to tileShapeSelectionCard,
        "complication_arc_thickness_card" to complicationArcThicknessCard,
        "tile_outline_card" to tileOutlineCard,
        "qs_tile_gradient_card" to qsTileGradientCard,
        "qs_page_indicator_style_card" to pageIndicatorStyleCard,
        "clear_all_fab_card" to clearAllFabCard,
        "show_media_in_notif_container_card" to showMediaInNotifContainerCard,
        "compact_media_notification_card" to compactMediaNotificationCard,
        "custom_header_image_card" to customHeaderImageCard,
        "custom_header_monet_gradient_card" to customHeaderMonetGradientCard,
        "element_colors_preference_card" to elementColorsPreferenceCard,
        "header_status_background_card" to headerStatusBackgroundCard,
        "footer_icon_background_card" to footerIconBackgroundCard,
        "frosted_glass_panel_card" to frostedGlassCard,
        "header_complication_tiles_card" to headerComplicationTilesCard,
        "move_brightness_bottom_card" to moveBrightnessBottomCard,
        "brightness_in_qqs_card" to brightnessInQqsCard,
        "brightness_gradient_card" to brightnessGradientCard,
        "show_brightness_controls_background_card" to showBrightnessControlsBackgroundCard,
        "progress_bar_slider_style_card" to progressBarSliderStyleCard,
        "panel_corner_styling_card" to panelCornerStylingCard,
        "panel_top_rim_card" to panelTopRimCard,
        "tile_top_rim_card" to tileTopRimCard
    )
    val hiddenDesignIds = buildSet {
        add("header_complication_tiles_card")
        if (isAndroidOreoStyle) {
            val list = mutableListOf(
                "tile_outline_card",
                "qs_tile_gradient_card",
                "panel_corner_styling_card"
            )
            if (!enableLegacyTileShapes) {
                list.add("tile_shape_selection_card")
            }
            addAll(list)
        }
        if (isAndroidMarshmallowStyle) {
            val list = mutableListOf(
                "tile_outline_card",
                "qs_tile_gradient_card",
                "qqs_icon_size_card",
                "qqs_icon_quantity_card",
                "qqs_complications_row_card",
                "qs_page_indicator_style_card"
            )
            if (!enableLegacyTileShapes) {
                list.add("tile_shape_selection_card")
            }
            addAll(list)
        }
        if (isAndroidKitKatStyle) {
            val list = mutableListOf(
                "tile_outline_card",
                "qs_tile_gradient_card",
                "qqs_icon_size_card",
                "qqs_icon_quantity_card",
                "qqs_complications_row_card",
                "qs_page_indicator_style_card",
                "clear_all_fab_card",
                "custom_header_monet_gradient_card",
                "element_colors_preference_card"
            )
            if (!enableLegacyTileShapes) {
                list.add("tile_shape_selection_card")
            }
            addAll(list)
        }
    }
    val canonicalPinKey: (String) -> String = { id ->
        when (id) {
            "android_oreo_style_card" -> "panel_style_card"
            "compact_media_notification_card" -> "show_media_in_notif_container_card"
            else -> id
        }
    }
    val pinnedCards = pinnedIds
        .filter { !hiddenDesignIds.contains(it) && !isUserHidden(it) }
        .distinctBy(canonicalPinKey)
        .mapNotNull { pinnedCardsMap[it] }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ExpressiveCardShapes.CardSpacing)
    ) {
        // 1. Pinned Cards at the very top of Quick Settings Design
        if (pinnedCards.isNotEmpty()) {
            ListSubheader(
                text = "Pinned",
                icon = Icons.Default.PushPin
            )
            pinnedCards.forEachIndexed { index, card ->
                card(CustomizeCardShapes.getShapeForIndex(index, pinnedCards.size))
            }
        }

        // 2. Tile Shapes & Layout
        val unpinnedTileShapes = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
        if (!hiddenDesignIds.contains("panel_style_card") &&
            !hiddenDesignIds.contains("android_oreo_style_card") &&
            !isUserHidden("panel_style_card") &&
            !pinnedIds.contains("panel_style_card") &&
            !pinnedIds.contains("android_oreo_style_card")
        ) unpinnedTileShapes.add(panelStyleCard)
        if (!hiddenDesignIds.contains("qqs_icon_size_card") && !isUserHidden("qqs_icon_size_card") && !pinnedIds.contains("qqs_icon_size_card")) unpinnedTileShapes.add(qqsIconSizeCard)
        if (!hiddenDesignIds.contains("qqs_icon_quantity_card") && !isUserHidden("qqs_icon_quantity_card") && !pinnedIds.contains("qqs_icon_quantity_card")) unpinnedTileShapes.add(qqsIconQuantityCard)
        if (!hiddenDesignIds.contains("qqs_complications_row_card") && !isUserHidden("qqs_complications_row_card") && !pinnedIds.contains("qqs_complications_row_card")) unpinnedTileShapes.add(qqsComplicationsRowCard)
        if (!hiddenDesignIds.contains("legacy_tile_shapes_support_card") && !isUserHidden("legacy_tile_shapes_support_card") && !pinnedIds.contains("legacy_tile_shapes_support_card")) unpinnedTileShapes.add(legacyTileShapesSupportCard)
        if (!hiddenDesignIds.contains("tile_shape_selection_card") && !isUserHidden("tile_shape_selection_card") && !pinnedIds.contains("tile_shape_selection_card")) unpinnedTileShapes.add(tileShapeSelectionCard)
        if (!hiddenDesignIds.contains("complication_arc_thickness_card") && !isUserHidden("complication_arc_thickness_card") && !pinnedIds.contains("complication_arc_thickness_card")) unpinnedTileShapes.add(complicationArcThicknessCard)
        if (!hiddenDesignIds.contains("tile_outline_card") && !isUserHidden("tile_outline_card") && !pinnedIds.contains("tile_outline_card")) unpinnedTileShapes.add(tileOutlineCard)
        if (!hiddenDesignIds.contains("qs_tile_gradient_card") && !isUserHidden("qs_tile_gradient_card") && !pinnedIds.contains("qs_tile_gradient_card")) unpinnedTileShapes.add(qsTileGradientCard)
        if (!hiddenDesignIds.contains("tile_top_rim_card") && !isUserHidden("tile_top_rim_card") && !pinnedIds.contains("tile_top_rim_card")) unpinnedTileShapes.add(tileTopRimCard)
        if (!hiddenDesignIds.contains("qs_page_indicator_style_card") && !isUserHidden("qs_page_indicator_style_card") && !pinnedIds.contains("qs_page_indicator_style_card")) unpinnedTileShapes.add(pageIndicatorStyleCard)
        if (!hiddenDesignIds.contains("clear_all_fab_card") && !isUserHidden("clear_all_fab_card") && !pinnedIds.contains("clear_all_fab_card")) unpinnedTileShapes.add(clearAllFabCard)
        if (!hiddenDesignIds.contains("show_media_in_notif_container_card") &&
            !hiddenDesignIds.contains("compact_media_notification_card") &&
            !isUserHidden("show_media_in_notif_container_card") &&
            !pinnedIds.contains("show_media_in_notif_container_card") &&
            !pinnedIds.contains("compact_media_notification_card")
        ) unpinnedTileShapes.add(showMediaInNotifContainerCard)

        if (unpinnedTileShapes.isNotEmpty()) {
            CollapsibleSubheaderSection(
                title = "Tile Shapes & Layout",
                icon = Icons.Default.Apps
            ) {
                unpinnedTileShapes.forEachIndexed { index, card ->
                    card(CustomizeCardShapes.getShapeForIndex(index, unpinnedTileShapes.size))
                }
            }
        }

        // 3. Header & Backgrounds
        val unpinnedHeader = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
        if (!hiddenDesignIds.contains("custom_header_image_card") && !isUserHidden("custom_header_image_card") && !pinnedIds.contains("custom_header_image_card")) unpinnedHeader.add(customHeaderImageCard)
        if (!hiddenDesignIds.contains("custom_header_monet_gradient_card") && !isUserHidden("custom_header_monet_gradient_card") && !pinnedIds.contains("custom_header_monet_gradient_card")) unpinnedHeader.add(customHeaderMonetGradientCard)
        if (!hiddenDesignIds.contains("element_colors_preference_card") && !isUserHidden("element_colors_preference_card") && !pinnedIds.contains("element_colors_preference_card")) unpinnedHeader.add(elementColorsPreferenceCard)
        if (!hiddenDesignIds.contains("header_status_background_card") && !isUserHidden("header_status_background_card") && !pinnedIds.contains("header_status_background_card")) unpinnedHeader.add(headerStatusBackgroundCard)
        if (!hiddenDesignIds.contains("footer_icon_background_card") && !isUserHidden("footer_icon_background_card") && !pinnedIds.contains("footer_icon_background_card")) unpinnedHeader.add(footerIconBackgroundCard)
        if (!hiddenDesignIds.contains("frosted_glass_panel_card") && !isUserHidden("frosted_glass_panel_card") && !pinnedIds.contains("frosted_glass_panel_card")) unpinnedHeader.add(frostedGlassCard)
        if (!hiddenDesignIds.contains("header_complication_tiles_card") && !isUserHidden("header_complication_tiles_card") && !pinnedIds.contains("header_complication_tiles_card")) unpinnedHeader.add(headerComplicationTilesCard)

        if (unpinnedHeader.isNotEmpty()) {
            CollapsibleSubheaderSection(
                title = "Header & Backgrounds",
                icon = Icons.Default.Image
            ) {
                unpinnedHeader.forEachIndexed { index, card ->
                    card(CustomizeCardShapes.getShapeForIndex(index, unpinnedHeader.size))
                }
            }
        }

        // 4. Brightness & Sliders
        val unpinnedBrightness = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
        if (!isUserHidden("move_brightness_bottom_card") && !pinnedIds.contains("move_brightness_bottom_card")) unpinnedBrightness.add(moveBrightnessBottomCard)
        if (!isUserHidden("brightness_in_qqs_card") && !pinnedIds.contains("brightness_in_qqs_card")) unpinnedBrightness.add(brightnessInQqsCard)
        if (!isUserHidden("brightness_gradient_card") && !pinnedIds.contains("brightness_gradient_card")) unpinnedBrightness.add(brightnessGradientCard)
        if (!isUserHidden("show_brightness_controls_background_card") && !pinnedIds.contains("show_brightness_controls_background_card")) unpinnedBrightness.add(showBrightnessControlsBackgroundCard)
        if (!isUserHidden("progress_bar_slider_style_card") && !pinnedIds.contains("progress_bar_slider_style_card")) unpinnedBrightness.add(progressBarSliderStyleCard)

        if (unpinnedBrightness.isNotEmpty()) {
            CollapsibleSubheaderSection(
                title = "Brightness & Sliders",
                icon = Icons.Default.BrightnessMedium
            ) {
                unpinnedBrightness.forEachIndexed { index, card ->
                    card(CustomizeCardShapes.getShapeForIndex(index, unpinnedBrightness.size))
                }
            }
        }

        // 5. Panel Corner Styling
        val unpinnedCorners = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
        if (!isAndroidOreoStyle && !isUserHidden("panel_corner_styling_card") && !pinnedIds.contains("panel_corner_styling_card")) unpinnedCorners.add(panelCornerStylingCard)
        if (!isUserHidden("panel_top_rim_card") && !pinnedIds.contains("panel_top_rim_card")) unpinnedCorners.add(panelTopRimCard)

        if (unpinnedCorners.isNotEmpty()) {
            CollapsibleSubheaderSection(
                title = "Panel Corner Styling",
                icon = Icons.Default.RoundedCorner
            ) {
                unpinnedCorners.forEachIndexed { index, card ->
                    card(CustomizeCardShapes.getShapeForIndex(index, unpinnedCorners.size))
                }
            }
        }

        if (pinnedCards.isEmpty() &&
            unpinnedTileShapes.isEmpty() &&
            unpinnedHeader.isEmpty() &&
            unpinnedBrightness.isEmpty() &&
            unpinnedCorners.isEmpty()
        ) {
            AllSectionFeaturesHiddenCard(sectionTitle = "Quick Settings Design")
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

    if (showFabImportDialog) {
        ImportXmlShapeDialog(
            onDismiss = { showFabImportDialog = false },
            onShapeImported = { importedItem ->
                PanelPreferences.setClearAllFabShapeId(context, importedItem.id)
                showFabImportDialog = false
            }
        )
    }
}

@Composable
fun ImportXmlShapeDialog(
    onDismiss: () -> Unit,
    onShapeImported: (QsShapeItem) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableIntStateOf(0) }
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

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
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

                        when (selectedTab) {
                            0 -> {
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
                                                    "Paste full <vector> XML or <path android:pathData=\"...\" /> here...",
                                                    fontSize = 12.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                            },
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 100.dp, max = 150.dp)
                                                .testTag("shape_xml_input"),
                                            textStyle = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                                            maxLines = 6
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
                            else -> {
                                Card(
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.UploadFile,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "Import XML Drawable File",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = { filePickerLauncher.launch("*/*") },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("pick_xml_file_button"),
                                            shape = RoundedCornerShape(14.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.UploadFile,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(pickedFileName ?: "Choose XML File (.xml)")
                                        }

                                        if (xmlInput.isNotBlank()) {
                                            Text(
                                                text = "Loaded ${xmlInput.length} characters of XML data.",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Shape Label Input
                        OutlinedTextField(
                            value = shapeLabel,
                            onValueChange = { shapeLabel = it },
                            label = { Text("Shape Name / Label") },
                            placeholder = { Text("e.g. Hexagon, Shield, Star") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("shape_label_input"),
                            textStyle = TextStyle(fontSize = 13.sp)
                        )

                        // Live Preview Card
                        if (parsedShape != null) {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val previewComposeShape = remember(parsedShape.id) {
                                            QsShapeManager.getComposeShape(parsedShape.id)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(previewComposeShape)
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = shapeLabel.ifBlank { "Custom Shape" },
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Valid Path • Viewport ${parsedShape.viewportWidth.toInt()}×${parsedShape.viewportHeight.toInt()}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: "",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        thickness = 1.dp
                    )

                    // Footer actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("cancel_import_shape_button")
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (parsedShape != null) {
                                    val result = QsShapeManager.importShape(
                                        context,
                                        parsedShape.pathData,
                                        parsedShape.label,
                                        parsedShape.viewportWidth,
                                        parsedShape.viewportHeight
                                    )
                                    if (result.isSuccess) {
                                        val importedItem = result.getOrNull()!!
                                        onShapeImported(importedItem)
                                        Toast.makeText(context, "Successfully added custom shape \"${importedItem.label}\"", Toast.LENGTH_SHORT).show()
                                    } else {
                                        errorMessage = "Failed to save shape: ${result.exceptionOrNull()?.message}"
                                    }
                                } else {
                                    errorMessage = "Please enter valid path data or XML first."
                                }
                            },
                            enabled = (parsedShape != null),
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.testTag("save_custom_shape_button")
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Shape")
                        }
                    }
                }
            }
        }
    }
}
