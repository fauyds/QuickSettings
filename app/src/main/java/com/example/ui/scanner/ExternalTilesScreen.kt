package com.example.ui.scanner

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TileCommandRepository
import com.example.model.CommandExecutionMode
import com.example.model.QsTileModel
import com.example.model.TileActionOverrideType
import com.example.model.TileCommandConfig
import com.example.system.ExternalTileScanner
import com.example.ui.theme.DarkUiBackground
import com.example.ui.theme.DarkUiSurface
import com.example.ui.theme.DarkUiSurfaceVariant
import com.example.ui.theme.ExpressiveCardShapes
import com.example.ui.theme.MonetOnPrimary
import com.example.ui.theme.MonetPrimary
import com.example.ui.theme.MonetTertiary
import kotlinx.coroutines.launch

@Composable
fun ExternalTilesScreen(
    scanner: ExternalTileScanner,
    scannedTiles: List<QsTileModel>,
    isScanning: Boolean,
    onAddTileToPanel: (QsTileModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val configs by TileCommandRepository.tileConfigs.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkUiBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(ExpressiveCardShapes.CardSpacing) // 4.dp
    ) {
        // TOP CARD: top corners 32dp, bottom corners 8dp
        Card(
            shape = ExpressiveCardShapes.TopCard,
            colors = CardDefaults.cardColors(containerColor = DarkUiSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "EXTERNAL TILE SCANNER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MonetPrimary
                        )
                        Text(
                            text = "Discovers third-party TileService integrations",
                            fontSize = 13.sp,
                            color = Color.LightGray
                        )
                    }

                    if (isScanning) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MonetPrimary, strokeWidth = 2.dp)
                    } else {
                        IconButton(
                            onClick = { scope.launch { scanner.scanThirdPartyTiles() } },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DarkUiSurfaceVariant)
                                .testTag("scan_tiles_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Scan", tint = MonetPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Text(
                    text = "Scans Android PackageManager for installed apps declaring TileService. You can override tile tap behavior to bind media commands (e.g. input keyevent 127) or direct intents without launching foreground app UI.",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }
        }

        // CENTER CARD: top corners 8dp, bottom corners 8dp
        Card(
            shape = ExpressiveCardShapes.CenterCard,
            colors = CardDefaults.cardColors(containerColor = DarkUiSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "DISCOVERED APPS & TILES (${scannedTiles.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MonetTertiary
                )

                if (scannedTiles.isEmpty() && !isScanning) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Apps, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                        Text(
                            text = "No third-party TileService found yet.",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                        Surface(
                            onClick = { scope.launch { scanner.scanThirdPartyTiles() } },
                            shape = CircleShape,
                            color = MonetPrimary.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MonetPrimary.copy(alpha = 0.4f))
                        ) {
                            Text(
                                "Scan Installed Apps",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MonetPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                } else {
                    scannedTiles.forEach { tile ->
                        val config = configs[tile.id] ?: TileCommandRepository.getConfig(tile.id)
                        ExternalTileCardItem(
                            tile = tile,
                            config = config,
                            onConfigChange = { updated ->
                                TileCommandRepository.saveConfig(context, updated)
                            },
                            onAdd = { onAddTileToPanel(tile) },
                            onLaunchApp = { scanner.dispatchTileClick(tile, config) },
                            onAppInfo = { scanner.dispatchTileLongClick(tile) }
                        )
                    }
                }
            }
        }

        // BOTTOM CARD: top corners 8dp, bottom corners 32dp
        Card(
            shape = ExpressiveCardShapes.BottomCard,
            colors = CardDefaults.cardColors(containerColor = DarkUiSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ACTION OVERRIDE & SILENT DISPATCH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MonetPrimary
                )

                Text(
                    text = "Third-party tiles can execute background keyevents (e.g. Media Pause 127) or direct system broadcasts under Standard, Shizuku, or Root modes silently without collapsing the Quick Settings panel.",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }
        }
    }
}

@Composable
fun ExternalTileCardItem(
    tile: QsTileModel,
    config: TileCommandConfig,
    onConfigChange: (TileCommandConfig) -> Unit,
    onAdd: () -> Unit,
    onLaunchApp: () -> Unit,
    onAppInfo: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkUiSurfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MonetTertiary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    val pkg = tile.packageName ?: if (tile.id.contains("/")) tile.id.substringBefore('/') else null
                    val cls = tile.serviceClassName ?: if (tile.id.contains("/")) tile.id.substringAfter('/') else null
                    com.example.system.ExternalTileIcon(
                        packageName = pkg,
                        serviceClassName = cls,
                        tintColor = MonetTertiary,
                        contentDescription = tile.label,
                        fallbackDrawable = tile.externalDrawable,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = tile.label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = if (config.isOverrideEnabled) "Action Overridden (${config.overrideType.displayName})" else tile.subtitle,
                        fontSize = 11.sp,
                        color = if (config.isOverrideEnabled) MonetTertiary else Color.Gray
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(32.dp).testTag("override_btn_${tile.id}")
                ) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = "Action Override",
                        tint = if (config.isOverrideEnabled) MonetPrimary else Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(onClick = onAppInfo, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Info, contentDescription = "App Info", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }

                IconButton(onClick = onLaunchApp, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.OpenInNew, contentDescription = "Launch", tint = MonetPrimary, modifier = Modifier.size(16.dp))
                }

                Surface(
                    onClick = onAdd,
                    shape = CircleShape,
                    color = MonetPrimary,
                    modifier = Modifier.testTag("add_external_${tile.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF003732), modifier = Modifier.size(14.dp))
                        Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF003732))
                    }
                }
            }
        }

        // Action Override Details Engine
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkUiSurface.copy(alpha = 0.8f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "ACTION OVERRIDE ENGINE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = MonetPrimary
                )

                // Master override switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Override default tap action",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Text(
                            text = "Bypass app activity launch and execute direct command/intent",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = config.isOverrideEnabled,
                        onCheckedChange = { onConfigChange(config.copy(isOverrideEnabled = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MonetOnPrimary, checkedTrackColor = MonetPrimary),
                        modifier = Modifier.testTag("switch_ext_override_${tile.id}")
                    )
                }

                if (config.isOverrideEnabled) {
                    // Bypass app launch toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bypass parent app UI launch",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                            Text(
                                text = "Prevent opening the app interface in foreground",
                                fontSize = 10.5.sp,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = config.bypassAppLaunch,
                            onCheckedChange = { onConfigChange(config.copy(bypassAppLaunch = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = MonetOnPrimary, checkedTrackColor = MonetPrimary)
                        )
                    }

                    // Action Type Selection
                    Text("Select Action Binding", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MonetTertiary)

                    val actionTypes = listOf(
                        TileActionOverrideType.MEDIA_PLAY_PAUSE,
                        TileActionOverrideType.MEDIA_PAUSE,
                        TileActionOverrideType.MEDIA_PLAY,
                        TileActionOverrideType.MEDIA_NEXT,
                        TileActionOverrideType.MEDIA_PREV,
                        TileActionOverrideType.SYSTEM_COMMAND,
                        TileActionOverrideType.DIRECT_INTENT
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        actionTypes.forEach { type ->
                            val isSelected = config.overrideType == type
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MonetPrimary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f))
                                    .border(1.dp, if (isSelected) MonetPrimary else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable(
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                        indication = null
                                    ) { onConfigChange(config.copy(overrideType = type)) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = type.displayName,
                                    fontSize = 12.sp,
                                    color = if (isSelected) MonetPrimary else Color.White
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Tune, contentDescription = null, tint = MonetPrimary, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    // Execution Mode Selector
                    Text("Execution Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MonetTertiary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CommandExecutionMode.entries.forEach { mode ->
                            val isSelected = config.executionMode == mode
                            Surface(
                                onClick = { onConfigChange(config.copy(executionMode = mode)) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MonetPrimary else Color.White.copy(alpha = 0.05f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = mode.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MonetOnPrimary else Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Mode-Dependent Dynamic Fields
                    if (config.executionMode == CommandExecutionMode.STANDARD) {
                        if (config.overrideType == TileActionOverrideType.DIRECT_INTENT || config.overrideType == TileActionOverrideType.DEFAULT || config.overrideType == TileActionOverrideType.SYSTEM_COMMAND) {
                            Text("System API / Intent Settings", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MonetTertiary)

                            OutlinedTextField(
                                value = config.customIntentAction,
                                onValueChange = { onConfigChange(config.copy(customIntentAction = it)) },
                                label = { Text("Custom Intent / Action", fontSize = 11.sp) },
                                placeholder = { Text("e.g. android.settings.DISPLAY_SETTINGS or com.example.app/.MyActivity", fontSize = 10.5.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MonetPrimary,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            OutlinedTextField(
                                value = config.customIntentPackage,
                                onValueChange = { onConfigChange(config.copy(customIntentPackage = it)) },
                                label = { Text("Target Package Name", fontSize = 11.sp) },
                                placeholder = { Text("e.g. com.example.app", fontSize = 10.5.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MonetPrimary,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            OutlinedTextField(
                                value = config.customIntentComponent,
                                onValueChange = { onConfigChange(config.copy(customIntentComponent = it)) },
                                label = { Text("Activity / Service Component", fontSize = 11.sp) },
                                placeholder = { Text("e.g. .MyService or com.example.app.MyActivity", fontSize = 10.5.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MonetPrimary,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            if (config.overrideType == TileActionOverrideType.DIRECT_INTENT &&
                                config.customIntentAction.isBlank() && config.customIntentPackage.isBlank() && config.customIntentComponent.isBlank()) {
                                Text(
                                    text = "⚠️ Please enter an Action (e.g. android.settings.DISPLAY_SETTINGS) or Component string.",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFFFFB74D)
                                )
                            }
                        }
                    } else {
                        // Shizuku / Root Mode Advanced Shell Settings
                        if (config.overrideType == TileActionOverrideType.SYSTEM_COMMAND || config.overrideType == TileActionOverrideType.DEFAULT) {
                            Text("Advanced Shell / API Settings", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MonetTertiary)

                            OutlinedTextField(
                                value = config.commandOn,
                                onValueChange = { onConfigChange(config.copy(commandOn = it)) },
                                label = { Text("Shell Command ON (e.g. cmd statusbar ...)", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MonetPrimary,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            OutlinedTextField(
                                value = config.commandOff,
                                onValueChange = { onConfigChange(config.copy(commandOff = it)) },
                                label = { Text("Shell Command OFF (optional)", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MonetPrimary,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            if (config.commandOn.isBlank() && config.commandOff.isBlank()) {
                                Text(
                                    text = "⚠️ Enter a valid shell command (e.g. input keyevent 127 or cmd statusbar ...).",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFFFFB74D)
                                )
                            }
                        }
                    }

                    // Close Panel Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Close panel on press", fontSize = 12.sp, color = Color.White)
                        Switch(
                            checked = config.closePanelOnPress,
                            onCheckedChange = { onConfigChange(config.copy(closePanelOnPress = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = MonetOnPrimary, checkedTrackColor = MonetPrimary)
                        )
                    }

                    // Action buttons: Save & Done
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                isExpanded = false
                                Toast.makeText(context, "Action override saved for ${tile.label}", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MonetPrimary)
                        ) {
                            Text("Done", fontSize = 11.sp, color = MonetPrimary)
                        }
                    }
                }
            }
        }
    }
}

