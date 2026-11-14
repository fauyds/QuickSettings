package com.example.ui.customizer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ActionChainStep
import com.example.model.CommandExecutionMode
import com.example.model.LongPressActionType
import com.example.model.TileActionOverrideType
import com.example.model.TileCommandConfig
import com.example.ui.theme.SegmentedButtonShapes
import com.example.ui.tile.GoogleIconEntry
import com.example.ui.tile.GoogleIconLibrary

@Composable
fun CustomQsTileEditorDialog(
    initialConfig: TileCommandConfig? = null,
    onDismiss: () -> Unit,
    onSave: (label: String, subtitle: String, config: TileCommandConfig) -> Unit
) {
    val isEditing = initialConfig != null
    var tileId by remember { mutableStateOf(initialConfig?.tileId ?: "") }
    var labelOn by remember { mutableStateOf(initialConfig?.labelOn ?: if (isEditing) "" else "My Custom Tile") }
    var labelOff by remember { mutableStateOf(initialConfig?.labelOff ?: "") }
    var subtitleOn by remember { mutableStateOf(initialConfig?.subtitleOn ?: "Tap to Action") }
    var subtitleOff by remember { mutableStateOf(initialConfig?.subtitleOff ?: "") }
    var iconName by remember { mutableStateOf(initialConfig?.iconName ?: "Settings") }
    var isStateToggle by remember { mutableStateOf(initialConfig?.isStateToggle ?: false) }
    var overrideType by remember { mutableStateOf(initialConfig?.overrideType ?: TileActionOverrideType.APP_LAUNCH) }
    var executionMode by remember { mutableStateOf(initialConfig?.executionMode ?: CommandExecutionMode.STANDARD) }

    // Action targets
    var appPackage by remember { mutableStateOf(initialConfig?.targetAppPackage ?: "") }
    var urlLink by remember { mutableStateOf(initialConfig?.urlOrDeepLink ?: "") }
    var cmdOn by remember { mutableStateOf(initialConfig?.commandOn ?: "") }
    var cmdOff by remember { mutableStateOf(initialConfig?.commandOff ?: "") }
    var intentAction by remember { mutableStateOf(initialConfig?.customIntentAction ?: "") }
    var intentPkg by remember { mutableStateOf(initialConfig?.customIntentPackage ?: "") }
    var intentComp by remember { mutableStateOf(initialConfig?.customIntentComponent ?: "") }

    // Long press
    var longPressType by remember { mutableStateOf(initialConfig?.longPressActionType ?: LongPressActionType.DEFAULT) }
    var longPressAppPkg by remember {
        mutableStateOf(if (initialConfig?.longPressActionType == LongPressActionType.APP_LAUNCH) initialConfig.longPressCommand else "")
    }
    var longPressUrl by remember {
        mutableStateOf(if (initialConfig?.longPressActionType == LongPressActionType.URL_DEEP_LINK) initialConfig.longPressCommand else "")
    }
    var longPressShellCmd by remember {
        mutableStateOf(if (initialConfig?.longPressActionType == LongPressActionType.SYSTEM_COMMAND) initialConfig.longPressCommand else "")
    }
    var longPressIntentAction by remember {
        mutableStateOf(if (initialConfig?.longPressActionType == LongPressActionType.DIRECT_INTENT) initialConfig.longPressCommand else "")
    }

    // Action chain
    var actionChainSteps by remember { mutableStateOf(initialConfig?.actionChainSteps ?: emptyList()) }

    // Behavior toggles
    var closePanel by remember { mutableStateOf(initialConfig?.closePanelOnPress ?: true) }
    var forceState by remember { mutableStateOf(initialConfig?.forceStateChangeAnyway ?: false) }
    var blockState by remember { mutableStateOf(initialConfig?.blockStateChanging ?: false) }
    var alwaysActive by remember { mutableStateOf(initialConfig?.alwaysActive ?: false) }

    var showIconPicker by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme
    val isDark = com.example.ui.theme.LocalIsDarkTheme.current
    val dialogBg = if (isDark) Color(0xFF303030) else Color(0xFFF5F5F5)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("custom_qs_tile_editor_dialog"),
            color = dialogBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Edit Custom Tile" else "Create Custom QS Tile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Icon Chooser & Basic Header Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF404040) else Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "ICON & TILE IDENTIFIER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = colorScheme.primary
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                val resolvedIcon = GoogleIconLibrary.findIconByName(iconName)
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(colorScheme.primary)
                                        .clickable { showIconPicker = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = resolvedIcon,
                                        contentDescription = iconName,
                                        tint = colorScheme.onPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Icon: $iconName",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    FilledTonalButton(
                                        onClick = { showIconPicker = true },
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier
                                            .height(34.dp)
                                            .testTag("btn_choose_icon")
                                    ) {
                                        Text("Choose Icon (500+ Icons)", fontSize = 11.5.sp)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = labelOn,
                                onValueChange = { labelOn = it },
                                label = { Text("Tile Label (ON)") },
                                placeholder = { Text("e.g. Work App") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("field_custom_label_on")
                            )

                            OutlinedTextField(
                                value = subtitleOn,
                                onValueChange = { subtitleOn = it },
                                label = { Text("Tile Subtitle (ON)") },
                                placeholder = { Text("e.g. Open Spotify") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("field_custom_subtitle_on")
                            )

                            // State Toggle Support Checkbox / Switch
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark) Color(0xFF2B2B2B) else Color(0xFFEBEBEB))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("State Toggle Support", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onSurface)
                                    Text("Supports separate ON and OFF labels/subtitles & toggles", fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = isStateToggle,
                                    onCheckedChange = { isStateToggle = it },
                                    modifier = Modifier.testTag("switch_is_state_toggle")
                                )
                            }

                            if (isStateToggle) {
                                OutlinedTextField(
                                    value = labelOff,
                                    onValueChange = { labelOff = it },
                                    label = { Text("Tile Label (OFF)") },
                                    placeholder = { Text("e.g. Work App Disabled") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("field_custom_label_off")
                                )

                                OutlinedTextField(
                                    value = subtitleOff,
                                    onValueChange = { subtitleOff = it },
                                    label = { Text("Tile Subtitle (OFF)") },
                                    placeholder = { Text("e.g. Off") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("field_custom_subtitle_off")
                                )
                            }
                        }
                    }

                    // Action Type Picker Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF404040) else Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "PRIMARY TILE ACTION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = colorScheme.primary
                            )

                            // Type Dropdown Menu / Buttons
                            var dropdownExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { dropdownExpanded = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(overrideType.displayName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }

                                DropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false }
                                ) {
                                    listOf(
                                        TileActionOverrideType.APP_LAUNCH,
                                        TileActionOverrideType.URL_DEEP_LINK,
                                        TileActionOverrideType.SYSTEM_COMMAND,
                                        TileActionOverrideType.DIRECT_INTENT,
                                        TileActionOverrideType.ACTION_CHAIN
                                    ).forEach { type ->
                                        DropdownMenuItem(
                                            text = { Text(type.displayName) },
                                            onClick = {
                                                overrideType = type
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Dynamic Fields based on Action Type
                            when (overrideType) {
                                TileActionOverrideType.APP_LAUNCH -> {
                                    OutlinedTextField(
                                        value = appPackage,
                                        onValueChange = { appPackage = it },
                                        label = { Text("Application Package Name") },
                                        placeholder = { Text("e.g. com.spotify.music or com.android.chrome") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("field_target_app_package")
                                    )
                                }
                                TileActionOverrideType.URL_DEEP_LINK -> {
                                    OutlinedTextField(
                                        value = urlLink,
                                        onValueChange = { urlLink = it },
                                        label = { Text("URL / Deep Link") },
                                        placeholder = { Text("e.g. https://google.com or spotify://") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("field_url_deep_link")
                                    )
                                }
                                TileActionOverrideType.SYSTEM_COMMAND -> {
                                    OutlinedTextField(
                                        value = cmdOn,
                                        onValueChange = { cmdOn = it },
                                        label = { Text("Shell Command ON") },
                                        placeholder = { Text("e.g. svc wifi enable") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("field_command_on")
                                    )
                                    if (isStateToggle) {
                                        OutlinedTextField(
                                            value = cmdOff,
                                            onValueChange = { cmdOff = it },
                                            label = { Text("Shell Command OFF") },
                                            placeholder = { Text("e.g. svc wifi disable") },
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("field_command_off")
                                        )
                                    }
                                }
                                TileActionOverrideType.DIRECT_INTENT -> {
                                    OutlinedTextField(
                                        value = intentAction,
                                        onValueChange = { intentAction = it },
                                        label = { Text("Intent Action") },
                                        placeholder = { Text("e.g. android.settings.SETTINGS") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("field_custom_intent_action")
                                    )
                                    OutlinedTextField(
                                        value = intentPkg,
                                        onValueChange = { intentPkg = it },
                                        label = { Text("Intent Target Package (Optional)") },
                                        placeholder = { Text("e.g. com.android.settings") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("field_custom_intent_pkg")
                                    )
                                    OutlinedTextField(
                                        value = intentComp,
                                        onValueChange = { intentComp = it },
                                        label = { Text("Intent Component (Optional)") },
                                        placeholder = { Text("e.g. .Settings") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("field_custom_intent_comp")
                                    )
                                }
                                TileActionOverrideType.ACTION_CHAIN -> {
                                    Text("Action Chaining (Sequential Steps)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Executes steps sequentially with configurable delays", fontSize = 11.sp, color = colorScheme.onSurfaceVariant)

                                    actionChainSteps.forEachIndexed { index, step ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isDark) Color(0xFF2B2B2B) else Color(0xFFEBEBEB))
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text("${index + 1}.", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            OutlinedTextField(
                                                value = step.target,
                                                onValueChange = { newTgt ->
                                                    actionChainSteps = actionChainSteps.toMutableList().apply {
                                                        this[index] = step.copy(target = newTgt)
                                                    }
                                                },
                                                placeholder = { Text("Target (Cmd/App/URL)") },
                                                singleLine = true,
                                                modifier = Modifier.weight(1f).height(50.dp)
                                            )
                                            OutlinedTextField(
                                                value = step.delayMs.toString(),
                                                onValueChange = { newDelay ->
                                                    actionChainSteps = actionChainSteps.toMutableList().apply {
                                                        this[index] = step.copy(delayMs = newDelay.toLongOrNull() ?: 0L)
                                                    }
                                                },
                                                label = { Text("Delay ms") },
                                                singleLine = true,
                                                modifier = Modifier.width(85.dp).height(50.dp)
                                            )
                                            IconButton(
                                                onClick = {
                                                    actionChainSteps = actionChainSteps.toMutableList().apply { removeAt(index) }
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete Step", tint = colorScheme.error)
                                            }
                                        }
                                    }

                                    FilledTonalButton(
                                        onClick = {
                                            actionChainSteps = actionChainSteps + ActionChainStep(type = "SHELL", target = "", delayMs = 500)
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("btn_add_chain_step")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Chain Step", fontSize = 12.sp)
                                    }
                                }
                                else -> {}
                            }
                        }
                    }

                    // Long Press Action Settings
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF404040) else Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "LONG PRESS ACTION SUPPORT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = colorScheme.primary
                            )

                            var longPressDropdown by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { longPressDropdown = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(longPressType.displayName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }

                                DropdownMenu(
                                    expanded = longPressDropdown,
                                    onDismissRequest = { longPressDropdown = false }
                                ) {
                                    LongPressActionType.entries.forEach { type ->
                                        DropdownMenuItem(
                                            text = { Text(type.displayName) },
                                            onClick = {
                                                longPressType = type
                                                longPressDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            when (longPressType) {
                                LongPressActionType.APP_LAUNCH -> {
                                    OutlinedTextField(
                                        value = longPressAppPkg,
                                        onValueChange = { longPressAppPkg = it },
                                        label = { Text("Long Press Application Package Name") },
                                        placeholder = { Text("e.g. com.spotify.music or com.android.chrome") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("field_long_press_cmd")
                                    )
                                }
                                LongPressActionType.URL_DEEP_LINK -> {
                                    OutlinedTextField(
                                        value = longPressUrl,
                                        onValueChange = { longPressUrl = it },
                                        label = { Text("Long Press URL / Deep Link") },
                                        placeholder = { Text("e.g. https://google.com or spotify://") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("field_long_press_cmd")
                                    )
                                }
                                LongPressActionType.SYSTEM_COMMAND -> {
                                    OutlinedTextField(
                                        value = longPressShellCmd,
                                        onValueChange = { longPressShellCmd = it },
                                        label = { Text("Long Press Shell Command") },
                                        placeholder = { Text("e.g. svc wifi enable or cmd statusbar") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("field_long_press_cmd")
                                    )
                                }
                                LongPressActionType.DIRECT_INTENT -> {
                                    OutlinedTextField(
                                        value = longPressIntentAction,
                                        onValueChange = { longPressIntentAction = it },
                                        label = { Text("Long Press Intent Action") },
                                        placeholder = { Text("e.g. android.settings.SETTINGS") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("field_long_press_cmd")
                                    )
                                }
                                LongPressActionType.DEFAULT -> {}
                            }
                        }
                    }

                    // Execution Mode & Behavior Options
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF404040) else Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "EXECUTION ENGINE & OPTIONS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = colorScheme.primary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val allModes = CommandExecutionMode.entries
                                allModes.forEach { mode ->
                                    val isSel = executionMode == mode
                                    val modeShape = SegmentedButtonShapes.getShape(
                                        item = mode,
                                        items = allModes,
                                        selectedItem = executionMode,
                                        innerCorner = 4.dp
                                    )
                                    Button(
                                        onClick = { executionMode = mode },
                                        shape = modeShape,
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSel) colorScheme.primary else colorScheme.surfaceContainerHigh,
                                            contentColor = if (isSel) colorScheme.onPrimary else colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                            .testTag("btn_exec_mode_${mode.id}")
                                    ) {
                                        Text(
                                            text = mode.displayName,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Close Panel on Press", fontSize = 12.5.sp, color = colorScheme.onSurface)
                                Switch(checked = closePanel, onCheckedChange = { closePanel = it })
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Force State Change", fontSize = 12.5.sp, color = colorScheme.onSurface)
                                Switch(checked = forceState, onCheckedChange = { forceState = it })
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Dialog Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.testTag("btn_cancel_custom_tile")) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val finalTileId = if (tileId.isNotBlank()) tileId else "custom_${System.currentTimeMillis()}"
                            val config = TileCommandConfig(
                                tileId = finalTileId,
                                isOverrideEnabled = true,
                                overrideType = overrideType,
                                executionMode = executionMode,
                                commandOn = cmdOn,
                                commandOff = cmdOff,
                                customIntentAction = intentAction,
                                customIntentPackage = intentPkg,
                                customIntentComponent = intentComp,
                                urlOrDeepLink = urlLink,
                                targetAppPackage = appPackage,
                                iconName = iconName,
                                labelOn = labelOn.ifBlank { "Custom Tile" },
                                labelOff = labelOff,
                                subtitleOn = subtitleOn.ifBlank { "Action" },
                                subtitleOff = subtitleOff,
                                isStateToggle = isStateToggle,
                                longPressActionType = longPressType,
                                longPressCommand = when (longPressType) {
                                    LongPressActionType.APP_LAUNCH -> longPressAppPkg
                                    LongPressActionType.URL_DEEP_LINK -> longPressUrl
                                    LongPressActionType.SYSTEM_COMMAND -> longPressShellCmd
                                    LongPressActionType.DIRECT_INTENT -> longPressIntentAction
                                    LongPressActionType.DEFAULT -> ""
                                },
                                actionChainSteps = actionChainSteps,
                                closePanelOnPress = closePanel,
                                forceStateChangeAnyway = forceState,
                                blockStateChanging = blockState,
                                alwaysActive = alwaysActive
                            )
                            onSave(labelOn.ifBlank { "Custom Tile" }, subtitleOn.ifBlank { "Action" }, config)
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("btn_save_custom_tile")
                    ) {
                        Text(if (isEditing) "Save Changes" else "Create Tile")
                    }
                }
            }
        }
    }

    if (showIconPicker) {
        GoogleIconPickerModal(
            onDismiss = { showIconPicker = false },
            onIconSelected = { selectedName ->
                iconName = selectedName
                showIconPicker = false
            }
        )
    }
}

@Composable
fun GoogleIconPickerModal(
    onDismiss: () -> Unit,
    onIconSelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val colorScheme = MaterialTheme.colorScheme
    val isDark = com.example.ui.theme.LocalIsDarkTheme.current
    val dialogBg = if (isDark) Color(0xFF2A2A2A) else Color(0xFFFAFAFA)

    val filteredIcons = remember(searchQuery, selectedCategory) {
        GoogleIconLibrary.allIcons.filter { icon ->
            val matchesCategory = (selectedCategory == "All" || icon.category == selectedCategory)
            val matchesQuery = searchQuery.isBlank() || icon.name.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("google_icon_picker_modal"),
            color = dialogBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Google Icon Library",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = "Choose from 500+ Material Design icons",
                            fontSize = 11.5.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search 500+ Google icons...", fontSize = 12.5.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) Color(0xFF383838) else Color.White,
                        unfocusedContainerColor = if (isDark) Color(0xFF383838) else Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("field_search_google_icons")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(GoogleIconLibrary.categories) { category ->
                        val isSel = selectedCategory == category
                        Surface(
                            shape = CircleShape,
                            color = if (isSel) colorScheme.primary else (if (isDark) Color(0xFF424242) else Color(0xFFE0E0E0)),
                            modifier = Modifier
                                .clickable { selectedCategory = category }
                                .testTag("chip_category_$category")
                        ) {
                            Text(
                                text = category,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) colorScheme.onPrimary else colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Icon Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredIcons, key = { it.name }) { iconEntry ->
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0xFF383838) else Color.White)
                                .clickable { onIconSelected(iconEntry.name) }
                                .padding(8.dp)
                                .testTag("icon_item_${iconEntry.name}"),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = iconEntry.iconVector,
                                contentDescription = iconEntry.name,
                                tint = colorScheme.onSurface,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = iconEntry.name.take(14),
                                fontSize = 9.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
