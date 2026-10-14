package com.fauyds.quicksettings.ui.customizer

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.fauyds.quicksettings.ui.theme.SegmentedButtonShapes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fauyds.quicksettings.data.TileCommandRepository
import com.fauyds.quicksettings.model.CommandExecutionMode
import com.fauyds.quicksettings.model.TileCommandConfig
import com.fauyds.quicksettings.system.PrivilegeEngine
import com.fauyds.quicksettings.model.PrivilegeMode
import com.fauyds.quicksettings.ui.theme.ExpressiveCardShapes
import kotlinx.coroutines.launch
import androidx.compose.runtime.Immutable

@Immutable
data class TileInfo(
    val id: String,
    val title: String,
    val icon: ImageVector
)

private val KnownTilesList = listOf(
    TileInfo("wifi", "Wi-Fi", Icons.Default.Wifi),
    TileInfo("bluetooth", "Bluetooth", Icons.Default.Bluetooth),
    TileInfo("torch", "Flashlight", Icons.Default.FlashlightOn),
    TileInfo("cellular", "Mobile Data", Icons.Default.SignalCellular4Bar),
    TileInfo("nfc", "NFC", Icons.Default.Nfc),
    TileInfo("airplane", "Airplane Mode", Icons.Default.AirplanemodeActive),
    TileInfo("location", "Location", Icons.Default.LocationOn),
    TileInfo("autorotate", "Auto-Rotate", Icons.Default.RotateRight),
    TileInfo("hotspot", "Hotspot / Wi-Fi Tethering", Icons.Default.WifiTethering),
    TileInfo("battery_saver", "Battery Saver", Icons.Default.BatterySaver),
    TileInfo("sound_mode", "Sound Mode", Icons.Default.VolumeUp),
    TileInfo("dark_mode", "Dark Mode", Icons.Default.Brightness4),
    TileInfo("aod", "Always-On Display", Icons.Default.Wallpaper),
    TileInfo("ambient_display", "Ambient Display", Icons.Default.Visibility),
    TileInfo("caffeine", "Caffeine", Icons.Default.LocalCafe),
    TileInfo("heads_up", "Heads Up", Icons.Default.NotificationsActive),
    TileInfo("screenshot", "Screenshot", Icons.Default.CameraAlt),
    TileInfo("screenrecord", "Screen Record", Icons.Default.Videocam)
)

@Composable
fun TileCommandsScreen(
    privilegeEngine: PrivilegeEngine
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val configs by TileCommandRepository.tileConfigs.collectAsStateWithLifecycle()

    // Also include custom / third-party tiles with configurations
    val allDisplayTiles = remember(configs) {
        val customTileIds = configs.keys.filter { key -> KnownTilesList.none { it.id == key } }
        KnownTilesList + customTileIds.map { id ->
            TileInfo(id, id.substringAfterLast('.').ifBlank { id }, Icons.Default.Terminal)
        }
    }

    val activeMode by privilegeEngine.currentMode.collectAsStateWithLifecycle()
    val areAllOverridesEnabled = remember(configs, allDisplayTiles) {
        allDisplayTiles.all { tile -> configs[tile.id]?.isOverrideEnabled == true }
    }

    val colorScheme = MaterialTheme.colorScheme
    val isDark = com.fauyds.quicksettings.ui.theme.LocalIsDarkTheme.current
    val screenBg = com.fauyds.quicksettings.ui.theme.getSettingsScreenBackgroundColor(isDark)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("tile_commands_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SettingsCard(
                shape = ExpressiveCardShapes.SingleCard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = "Tile Commands",
                                tint = colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = "Tile Commands Customization",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 21.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "Override default system toggles and third-party tile launches with custom shell commands, media keys, or direct broadcast intents.",
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    when (activeMode) {
                        PrivilegeMode.STANDARD -> {
                            // Standard API mode requires no ADB or Root daemon
                        }
                        PrivilegeMode.SHIZUKU -> {
                            val shizukuStatus by privilegeEngine.shizukuStatus.collectAsStateWithLifecycle()
                            com.fauyds.quicksettings.ui.onboarding.ShizukuPermissionRow(
                                statusState = shizukuStatus,
                                onRequestAccess = {
                                    val msg = privilegeEngine.requestShizukuPermission()
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                },
                                showBorder = false
                            )
                        }
                        PrivilegeMode.ROOT -> {
                            val isRootActive by privilegeEngine.isRootAvailable.collectAsStateWithLifecycle()
                            com.fauyds.quicksettings.ui.onboarding.RootPermissionRow(
                                isRootGranted = isRootActive,
                                onRequestAccess = {
                                    val granted = privilegeEngine.requestRootPermission()
                                    privilegeEngine.refreshPermissions()
                                    val msg = if (granted) "Root access granted!" else "Root request failed. Please check root manager."
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }

        // Target Execution Engine Mode Selector Card
        item {
            SettingsCard(
                shape = ExpressiveCardShapes.SingleCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tile_cmd_engine_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TARGET EXECUTION ENGINE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp,
                            color = colorScheme.primary
                        )
                        Surface(
                            color = colorScheme.primaryContainer,
                            shape = CircleShape
                        ) {
                            Text(
                                text = activeMode.title,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = if (activeMode == PrivilegeMode.STANDARD)
                            "Standard option shows Intent action settings for launching system activities."
                        else
                            "${activeMode.title} option shows promptless shell commands for ON and OFF execution.",
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        color = colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val modes = PrivilegeMode.entries
                        modes.forEachIndexed { index, mode ->
                            val isSelected = activeMode == mode
                            val shape = if (isSelected) {
                                CircleShape
                            } else {
                                when (index) {
                                    0 -> RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp, topEnd = 8.dp, bottomEnd = 8.dp)
                                    modes.size - 1 -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = 24.dp, bottomEnd = 24.dp)
                                    else -> RoundedCornerShape(8.dp)
                                }
                            }

                            Button(
                                onClick = {
                                    privilegeEngine.setMode(mode)
                                },
                                shape = shape,
                                border = null,
                                elevation = null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) colorScheme.primary else colorScheme.surfaceContainerHigh,
                                    contentColor = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("tile_cmd_mode_btn_${mode.name.lowercase()}")
                            ) {
                                Text(
                                    text = mode.title,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Master Enable All Overrides Card
        item {
            SettingsCard(
                shape = ExpressiveCardShapes.SingleCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tile_cmd_enable_all_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
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
                                .background(colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = "Enable All Tile Overrides",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = if (areAllOverridesEnabled) "All tile overrides are active" else "Turn on custom command overrides for all tiles",
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = areAllOverridesEnabled,
                        onCheckedChange = { enable ->
                            val currentExecMode = when (activeMode) {
                                PrivilegeMode.ROOT -> CommandExecutionMode.ROOT
                                PrivilegeMode.SHIZUKU -> CommandExecutionMode.SHIZUKU
                                PrivilegeMode.STANDARD -> CommandExecutionMode.STANDARD
                            }
                            TileCommandRepository.setAllOverridesEnabled(context, enable, currentExecMode)
                        },
                        modifier = Modifier.testTag("tile_cmd_toggle_all_commands")
                    )
                }
            }
        }

        itemsIndexed(
            items = allDisplayTiles,
            key = { _, tile -> tile.id },
            contentType = { _, _ -> "tile_command_item" }
        ) { _, tile ->
            val config = configs[tile.id] ?: remember(tile.id) { TileCommandConfig(tileId = tile.id) }
            val onConfigChange = remember(tile.id, context) {
                { updatedConfig: TileCommandConfig ->
                    TileCommandRepository.saveConfig(context, updatedConfig)
                }
            }
            val onReset = remember(tile.id, tile.title, context) {
                {
                    TileCommandRepository.resetConfig(context, tile.id)
                    Toast.makeText(context, "Reset ${tile.title} to default commands", Toast.LENGTH_SHORT).show()
                }
            }
            val onTestCommand = remember(tile.id, context, privilegeEngine, coroutineScope) {
                { cmd: String, mode: CommandExecutionMode ->
                    coroutineScope.launch {
                        Toast.makeText(context, "Executing via ${mode.displayName}...", Toast.LENGTH_SHORT).show()
                        val ok = privilegeEngine.executeCommandWithMode(cmd, mode, null)
                        if (ok) {
                            Toast.makeText(context, "Command executed successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Command failed or permission missing", Toast.LENGTH_SHORT).show()
                        }
                    }
                    Unit
                }
            }
            TileCommandCardItem(
                tile = tile,
                config = config,
                shape = ExpressiveCardShapes.SingleCard,
                onConfigChange = onConfigChange,
                onReset = onReset,
                onTestCommand = onTestCommand
            )
        }
    }
}

@Composable
fun TileCommandCardItem(
    tile: TileInfo,
    config: TileCommandConfig,
    shape: androidx.compose.ui.graphics.Shape = ExpressiveCardShapes.SingleCard,
    onConfigChange: (TileCommandConfig) -> Unit,
    onReset: () -> Unit,
    onTestCommand: (String, CommandExecutionMode) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = com.fauyds.quicksettings.ui.theme.LocalIsDarkTheme.current
    var isExpanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "expand_rotation"
    )

    val innerContainerBg = com.fauyds.quicksettings.ui.theme.getSettingsScreenBackgroundColor(isDark)

    SettingsCard(
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tile_cmd_card_${tile.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Icon, Title, Master Override Switch, Expand Chevron
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (config.isOverrideEnabled) colorScheme.primary
                                else colorScheme.surfaceContainerHigh
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tile.icon,
                            contentDescription = tile.title,
                            tint = if (config.isOverrideEnabled) colorScheme.onPrimary
                            else colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = tile.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = if (config.isOverrideEnabled) "Custom command enabled" else "Standard system action",
                            fontSize = 11.5.sp,
                            lineHeight = 14.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = if (config.isOverrideEnabled) colorScheme.primary else colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Switch(
                        checked = config.isOverrideEnabled,
                        onCheckedChange = { isChecked ->
                            onConfigChange(config.copy(isOverrideEnabled = isChecked))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colorScheme.onPrimary,
                            checkedTrackColor = colorScheme.primary,
                            uncheckedThumbColor = colorScheme.outline,
                            uncheckedTrackColor = colorScheme.surfaceContainerHighest
                        ),
                        modifier = Modifier.testTag("switch_override_${tile.id}")
                    )

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable { isExpanded = !isExpanded },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(22.dp)
                                .rotate(rotationAngle)
                        )
                    }
                }
            }

            // Expandable Options Section
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Spacer(modifier = Modifier.height(2.dp))

                    // Inner Group of Toggles with Inner and Outer Corners
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        // Close panel on press toggle (Top Card in Group)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(ExpressiveCardShapes.TopCard)
                                .background(innerContainerBg)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text(
                                    text = "Close panel on press",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 16.sp,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = "Dismisses the overlay after clicking this tile",
                                    fontSize = 11.sp,
                                    lineHeight = 13.sp,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = config.closePanelOnPress,
                                onCheckedChange = { isChecked ->
                                    onConfigChange(config.copy(closePanelOnPress = isChecked))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colorScheme.onPrimary,
                                    checkedTrackColor = colorScheme.primary,
                                    uncheckedThumbColor = colorScheme.outline,
                                    uncheckedTrackColor = colorScheme.surfaceContainerHighest
                                ),
                                modifier = Modifier.testTag("switch_close_panel_${tile.id}")
                            )
                        }

                        // Force state change toggle (Center Card in Group)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(ExpressiveCardShapes.CenterCard)
                                .background(innerContainerBg)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text(
                                    text = "Force state change anyway",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 16.sp,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = "Instantly flips the visual state locally on tap",
                                    fontSize = 11.sp,
                                    lineHeight = 13.sp,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = config.forceStateChangeAnyway,
                                onCheckedChange = { isChecked ->
                                    onConfigChange(config.copy(forceStateChangeAnyway = isChecked))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colorScheme.onPrimary,
                                    checkedTrackColor = colorScheme.primary,
                                    uncheckedThumbColor = colorScheme.outline,
                                    uncheckedTrackColor = colorScheme.surfaceContainerHighest
                                ),
                                modifier = Modifier.testTag("switch_force_state_${tile.id}")
                            )
                        }

                        // Block state changing toggle (Center Card in Group)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(ExpressiveCardShapes.CenterCard)
                                .background(innerContainerBg)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text(
                                    text = "Block state changing",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 16.sp,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = "Freezes the current state and ignores clicks",
                                    fontSize = 11.sp,
                                    lineHeight = 13.sp,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = config.blockStateChanging,
                                onCheckedChange = { isChecked ->
                                    onConfigChange(config.copy(blockStateChanging = isChecked))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colorScheme.onPrimary,
                                    checkedTrackColor = colorScheme.primary,
                                    uncheckedThumbColor = colorScheme.outline,
                                    uncheckedTrackColor = colorScheme.surfaceContainerHighest
                                ),
                                modifier = Modifier.testTag("switch_block_state_${tile.id}")
                            )
                        }

                        // Always Active toggle (Bottom Card in Group)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(ExpressiveCardShapes.BottomCard)
                                .background(innerContainerBg)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text(
                                    text = "Always Active",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 16.sp,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = "Locks the tile state to ACTIVE permanently",
                                    fontSize = 11.sp,
                                    lineHeight = 13.sp,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = config.alwaysActive,
                                onCheckedChange = { isChecked ->
                                    onConfigChange(config.copy(alwaysActive = isChecked))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colorScheme.onPrimary,
                                    checkedTrackColor = colorScheme.primary,
                                    uncheckedThumbColor = colorScheme.outline,
                                    uncheckedTrackColor = colorScheme.surfaceContainerHighest
                                ),
                                modifier = Modifier.testTag("switch_always_active_${tile.id}")
                            )
                        }
                    }

                    // Mode & Custom Commands Card Container
                    AnimatedVisibility(visible = config.isOverrideEnabled) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(ExpressiveCardShapes.SingleCard)
                                .background(innerContainerBg)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Execution Mode:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurfaceVariant
                            )

                            // Execution Mode Selector (Standard API / Shizuku / Root)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val allModes = CommandExecutionMode.entries
                                allModes.forEach { mode ->
                                    val isSelected = config.executionMode == mode
                                    val modeShape = SegmentedButtonShapes.getShape(
                                        item = mode,
                                        items = allModes,
                                        selectedItem = config.executionMode
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(modeShape)
                                            .background(
                                                if (isSelected) colorScheme.primaryContainer
                                                else com.fauyds.quicksettings.ui.theme.getSettingsCardBackgroundColor(isDark)
                                            )
                                            .clickable(
                                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                onConfigChange(config.copy(executionMode = mode))
                                            }
                                            .padding(vertical = 8.dp)
                                            .testTag("mode_${mode.id}_${tile.id}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = mode.displayName,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Mode-Dependent Dynamic Settings Fields
                            if (config.executionMode == CommandExecutionMode.STANDARD) {
                                Text(
                                    text = "SYSTEM API / INTENT ACTION SETTINGS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = colorScheme.tertiary
                                )

                                // Custom Intent / Action
                                TextField(
                                    value = config.customIntentAction,
                                    onValueChange = { onConfigChange(config.copy(customIntentAction = it)) },
                                    label = { Text("Custom Intent / Action", fontSize = 12.sp) },
                                    placeholder = { Text("e.g. android.settings.DISPLAY_SETTINGS or com.fauyds.quicksettings.app/.MyActivity", fontSize = 11.5.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent,
                                        focusedContainerColor = com.fauyds.quicksettings.ui.theme.getSettingsCardBackgroundColor(isDark),
                                        unfocusedContainerColor = com.fauyds.quicksettings.ui.theme.getSettingsCardBackgroundColor(isDark),
                                        focusedLabelColor = colorScheme.primary,
                                        unfocusedLabelColor = colorScheme.onSurfaceVariant,
                                        focusedTextColor = colorScheme.onSurface,
                                        unfocusedTextColor = colorScheme.onSurface
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("field_intent_action_${tile.id}")
                                )

                                // Target Package Name
                                TextField(
                                    value = config.customIntentPackage,
                                    onValueChange = { onConfigChange(config.copy(customIntentPackage = it)) },
                                    label = { Text("Target Package Name", fontSize = 12.sp) },
                                    placeholder = { Text("e.g. com.android.settings", fontSize = 11.5.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent,
                                        focusedContainerColor = com.fauyds.quicksettings.ui.theme.getSettingsCardBackgroundColor(isDark),
                                        unfocusedContainerColor = com.fauyds.quicksettings.ui.theme.getSettingsCardBackgroundColor(isDark),
                                        focusedLabelColor = colorScheme.primary,
                                        unfocusedLabelColor = colorScheme.onSurfaceVariant,
                                        focusedTextColor = colorScheme.onSurface,
                                        unfocusedTextColor = colorScheme.onSurface
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("field_intent_package_${tile.id}")
                                )

                                // Activity / Service Component
                                TextField(
                                    value = config.customIntentComponent,
                                    onValueChange = { onConfigChange(config.copy(customIntentComponent = it)) },
                                    label = { Text("Activity / Service Component", fontSize = 12.sp) },
                                    placeholder = { Text("e.g. .DisplaySettings or com.fauyds.quicksettings.app.MyService", fontSize = 11.5.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent,
                                        focusedContainerColor = com.fauyds.quicksettings.ui.theme.getSettingsCardBackgroundColor(isDark),
                                        unfocusedContainerColor = com.fauyds.quicksettings.ui.theme.getSettingsCardBackgroundColor(isDark),
                                        focusedLabelColor = colorScheme.primary,
                                        unfocusedLabelColor = colorScheme.onSurfaceVariant,
                                        focusedTextColor = colorScheme.onSurface,
                                        unfocusedTextColor = colorScheme.onSurface
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("field_intent_component_${tile.id}")
                                )

                                // Input validation warning
                                if (config.customIntentAction.isBlank() && config.customIntentPackage.isBlank() && config.customIntentComponent.isBlank()) {
                                    Text(
                                        text = "⚠️ Standard mode requires a valid Intent Action (e.g. android.settings.DISPLAY_SETTINGS) or Component string.",
                                        fontSize = 11.sp,
                                        color = colorScheme.error
                                    )
                                }
                            } else {
                                Text(
                                    text = "ADVANCED SHELL / API COMMAND SETTINGS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = colorScheme.tertiary
                                )

                                // Custom ON Command Text Field
                                TextField(
                                    value = config.commandOn,
                                    onValueChange = { onConfigChange(config.copy(commandOn = it)) },
                                    label = { Text("Custom ON Shell Command", fontSize = 12.sp) },
                                    placeholder = { Text("e.g. cmd statusbar expand-notifications or svc wifi enable", fontSize = 11.5.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent,
                                        focusedContainerColor = com.fauyds.quicksettings.ui.theme.getSettingsCardBackgroundColor(isDark),
                                        unfocusedContainerColor = com.fauyds.quicksettings.ui.theme.getSettingsCardBackgroundColor(isDark),
                                        focusedLabelColor = colorScheme.primary,
                                        unfocusedLabelColor = colorScheme.onSurfaceVariant,
                                        focusedTextColor = colorScheme.onSurface,
                                        unfocusedTextColor = colorScheme.onSurface
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("field_cmd_on_${tile.id}")
                                )

                                // Custom OFF Command Text Field
                                TextField(
                                    value = config.commandOff,
                                    onValueChange = { onConfigChange(config.copy(commandOff = it)) },
                                    label = { Text("Custom OFF Shell Command", fontSize = 12.sp) },
                                    placeholder = { Text("e.g. svc wifi disable", fontSize = 11.5.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent,
                                        focusedContainerColor = com.fauyds.quicksettings.ui.theme.getSettingsCardBackgroundColor(isDark),
                                        unfocusedContainerColor = com.fauyds.quicksettings.ui.theme.getSettingsCardBackgroundColor(isDark),
                                        focusedLabelColor = colorScheme.primary,
                                        unfocusedLabelColor = colorScheme.onSurfaceVariant,
                                        focusedTextColor = colorScheme.onSurface,
                                        unfocusedTextColor = colorScheme.onSurface
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("field_cmd_off_${tile.id}")
                                )

                                // Input validation warning
                                if (config.commandOn.isBlank() && config.commandOff.isBlank()) {
                                    Text(
                                        text = "⚠️ Shell mode requires a valid ON or OFF shell command (e.g. cmd statusbar ...).",
                                        fontSize = 11.sp,
                                        color = colorScheme.error
                                    )
                                }
                            }

                            // Test Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = {
                                        if (config.executionMode == CommandExecutionMode.STANDARD) {
                                            if (config.customIntentAction.isNotBlank() || config.customIntentPackage.isNotBlank() || config.customIntentComponent.isNotBlank()) {
                                                onTestCommand(config.customIntentAction.ifBlank { config.customIntentComponent }, config.executionMode)
                                            }
                                        } else {
                                            if (config.commandOn.isNotBlank()) {
                                                onTestCommand(config.commandOn, config.executionMode)
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_test_on_${tile.id}"),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = colorScheme.surfaceContainerHigh,
                                        contentColor = colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Test Action",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Test Action", fontSize = 11.5.sp)
                                }

                                if (config.executionMode != CommandExecutionMode.STANDARD) {
                                    FilledTonalButton(
                                        onClick = {
                                            if (config.commandOff.isNotBlank()) {
                                                onTestCommand(config.commandOff, config.executionMode)
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("btn_test_off_${tile.id}"),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = colorScheme.surfaceContainerHigh,
                                            contentColor = colorScheme.primary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Test OFF",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Test OFF", fontSize = 11.5.sp)
                                    }
                                }

                                FilledTonalButton(
                                    onClick = onReset,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_reset_${tile.id}"),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = colorScheme.surfaceContainerHigh,
                                        contentColor = colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RestartAlt,
                                        contentDescription = "Reset",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reset", fontSize = 11.5.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
