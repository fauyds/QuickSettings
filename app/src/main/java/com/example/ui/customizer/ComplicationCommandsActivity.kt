package com.example.ui.customizer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ComplicationCommandRepository
import com.example.data.KnownComplicationDefinition
import com.example.data.PanelPreferences
import com.example.model.CommandExecutionMode
import com.example.model.ComplicationCommandConfig
import com.example.model.ComplicationTriggerBehavior
import com.example.model.PrivilegeMode
import com.example.system.PrivilegeEngine
import com.example.ui.theme.AppSettingsTheme
import com.example.ui.theme.ExpressiveCardShapes
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.SegmentedButtonShapes
import com.example.ui.theme.getSettingsCardBackgroundColor
import com.example.ui.theme.getSettingsScreenBackgroundColor
import kotlinx.coroutines.launch

class ComplicationCommandsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        PanelPreferences.init(this)
        ComplicationCommandRepository.init(this)

        setContent {
            AppSettingsTheme {
                ComplicationCommandsScreen(
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplicationCommandsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val privilegeEngine = remember(context) { PrivilegeEngine(context.applicationContext) }
    val activeMode by privilegeEngine.currentMode.collectAsStateWithLifecycle()
    val configs by ComplicationCommandRepository.configs.collectAsStateWithLifecycle()
    val complications = ComplicationCommandRepository.knownComplications

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                ComplicationCommandRepository.init(context)
                privilegeEngine.syncWithPreferences()
                privilegeEngine.refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BackHandler { onBackClick() }

    val colorScheme = MaterialTheme.colorScheme
    val isDark = LocalIsDarkTheme.current
    val screenBg = getSettingsScreenBackgroundColor(isDark)
    val cardBg = getSettingsCardBackgroundColor(isDark)

    val customizedCount = remember(configs) {
        configs.values.count {
            it.tapBehavior != ComplicationTriggerBehavior.DEFAULT ||
                it.longPressBehavior != ComplicationTriggerBehavior.DEFAULT
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                expandedHeight = 52.dp,
                title = {
                    Text(
                        text = "Complication Actions",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("complication_commands_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            ComplicationCommandRepository.resetAll(context)
                            Toast.makeText(context, "Reset all complication actions to default", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("complication_commands_reset_all_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset All Complication Actions"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cardBg,
                    titleContentColor = colorScheme.onSurface,
                    navigationIconContentColor = colorScheme.onSurface,
                    actionIconContentColor = colorScheme.onSurfaceVariant
                )
            )
        },
        containerColor = screenBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(screenBg)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .testTag("complication_commands_list"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Overview Card (mirrors Tile Commands header card)
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    SettingsCard(
                        shape = ExpressiveCardShapes.SingleCard,
                        canPin = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp)
                            .testTag("complication_commands_intro_card")
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
                                        imageVector = Icons.Default.TouchApp,
                                        contentDescription = "Complication Actions",
                                        tint = colorScheme.onPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(1.dp)
                                ) {
                                    Text(
                                        text = "Complication Actions Customization",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 21.sp,
                                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                        color = colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Customize single tap and long press behavior for each complication: open complication menus, launch intents, or run Standard / Shizuku / Root commands.",
                                        fontSize = 12.sp,
                                        lineHeight = 15.sp,
                                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            when (activeMode) {
                                PrivilegeMode.STANDARD -> Unit
                                PrivilegeMode.SHIZUKU -> {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val shizukuStatus by privilegeEngine.shizukuStatus.collectAsStateWithLifecycle()
                                    com.example.ui.onboarding.ShizukuPermissionRow(
                                        statusState = shizukuStatus,
                                        onRequestAccess = {
                                            val msg = privilegeEngine.requestShizukuPermission()
                                            privilegeEngine.refreshPermissions()
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        },
                                        showBorder = false
                                    )
                                }
                                PrivilegeMode.ROOT -> {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val isRootActive by privilegeEngine.isRootAvailable.collectAsStateWithLifecycle()
                                    com.example.ui.onboarding.RootPermissionRow(
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
            }

            // Target Execution Engine Selector Card (mirrors Tile Commands engine card)
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    SettingsCard(
                        shape = ExpressiveCardShapes.SingleCard,
                        canPin = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp)
                            .testTag("comp_cmd_engine_card")
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
                                text = "Choose the device privilege engine for shell execution. Each complication can independently pick Default, Menu, Standard, Shizuku, or Root for Single Tap and Long Press.",
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
                                        onClick = { privilegeEngine.setMode(mode) },
                                        shape = shape,
                                        border = null,
                                        elevation = null,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) colorScheme.primary else colorScheme.surfaceContainerHigh,
                                            contentColor = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("comp_cmd_mode_btn_${mode.name.lowercase()}")
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
            }

            // Summary & Quick Reset Card (mirrors Tile Commands master status card)
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    SettingsCard(
                        shape = ExpressiveCardShapes.SingleCard,
                        canPin = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp)
                            .testTag("comp_cmd_summary_card")
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
                                        text = "Configured Complications",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (customizedCount > 0) {
                                            "$customizedCount of ${complications.size} complications customized"
                                        } else {
                                            "All ${complications.size} complications using smart defaults"
                                        },
                                        fontSize = 11.5.sp,
                                        lineHeight = 15.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            FilledTonalButton(
                                onClick = {
                                    ComplicationCommandRepository.resetAll(context)
                                    Toast.makeText(context, "Reset all complication actions to default", Toast.LENGTH_SHORT).show()
                                },
                                enabled = customizedCount > 0,
                                shape = CircleShape,
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_reset_all_complications")
                            ) {
                                Text("Reset All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Per-Complication Command Cards (same style as TileCommandCardItem)
            items(
                items = complications,
                key = { def -> def.id },
                contentType = { "complication_command_item" }
            ) { def ->
                val cfg = configs[def.id] ?: ComplicationCommandRepository.getConfig(def.id, context)

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    ComplicationCommandCardItem(
                        definition = def,
                        config = cfg,
                        modifier = Modifier.widthIn(max = 600.dp),
                        onConfigChange = { updated ->
                            ComplicationCommandRepository.saveConfig(context, updated)
                        },
                        onResetTap = {
                            ComplicationCommandRepository.saveConfig(
                                context,
                                cfg.copy(
                                    tapBehavior = ComplicationTriggerBehavior.DEFAULT,
                                    tapShellCommand = def.defaultShellOn,
                                    tapShellCommandOff = def.defaultShellOff,
                                    tapIntentAction = def.defaultIntentAction,
                                    tapIntentPackage = "",
                                    tapIntentComponent = ""
                                )
                            )
                            Toast.makeText(context, "Reset ${def.title} single tap to default", Toast.LENGTH_SHORT).show()
                        },
                        onResetLongPress = {
                            ComplicationCommandRepository.saveConfig(
                                context,
                                cfg.copy(
                                    longPressBehavior = ComplicationTriggerBehavior.DEFAULT,
                                    longPressShellCommand = def.defaultShellOn,
                                    longPressIntentAction = def.defaultIntentAction,
                                    longPressIntentPackage = "",
                                    longPressIntentComponent = ""
                                )
                            )
                            Toast.makeText(context, "Reset ${def.title} long press to default", Toast.LENGTH_SHORT).show()
                        },
                        onTestShell = { cmd, mode ->
                            coroutineScope.launch {
                                if (cmd.isBlank()) {
                                    Toast.makeText(context, "Enter a command first", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                Toast.makeText(context, "Executing via ${mode.displayName}...", Toast.LENGTH_SHORT).show()
                                val ok = privilegeEngine.executeCommandWithMode(cmd, mode, null)
                                Toast.makeText(
                                    context,
                                    if (ok) "Command executed successfully!" else "Command failed or permission missing",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onTestStandardIntent = { action, pkg, comp ->
                            val launched = launchTestStandardIntent(context, action, pkg, comp)
                            Toast.makeText(
                                context,
                                if (launched) "Intent launched successfully!" else "Unable to resolve Intent target",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ComplicationCommandCardItem(
    definition: KnownComplicationDefinition,
    config: ComplicationCommandConfig,
    modifier: Modifier = Modifier,
    onConfigChange: (ComplicationCommandConfig) -> Unit,
    onResetTap: () -> Unit,
    onResetLongPress: () -> Unit,
    onTestShell: (String, CommandExecutionMode) -> Unit,
    onTestStandardIntent: (String, String, String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = LocalIsDarkTheme.current
    var isExpanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "comp_expand_rotation_${definition.id}"
    )

    val innerContainerBg = getSettingsScreenBackgroundColor(isDark)
    val hasCustomAction = config.tapBehavior != ComplicationTriggerBehavior.DEFAULT ||
        config.longPressBehavior != ComplicationTriggerBehavior.DEFAULT

    SettingsCard(
        shape = ExpressiveCardShapes.SingleCard,
        canPin = false,
        modifier = modifier
            .fillMaxWidth()
            .testTag("complication_cmd_card_${definition.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Icon, Title, Subtitle Status, Expand Chevron (mirrors TileCommandCardItem)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
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
                                if (hasCustomAction) colorScheme.primary
                                else colorScheme.surfaceContainerHigh
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = definition.icon,
                            contentDescription = definition.title,
                            tint = if (hasCustomAction) colorScheme.onPrimary
                            else colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = definition.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = if (hasCustomAction) {
                                "Tap: ${config.tapBehavior.label} • Long: ${config.longPressBehavior.label}"
                            } else {
                                "Default complication action"
                            },
                            fontSize = 11.5.sp,
                            lineHeight = 14.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = if (hasCustomAction) colorScheme.primary else colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { isExpanded = !isExpanded },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse ${definition.title}" else "Expand ${definition.title}",
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(rotationAngle)
                    )
                }
            }

            // Expandable Options Section (mirrors TileCommandCardItem's inner mode & command containers)
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Spacer(modifier = Modifier.height(2.dp))

                    // 1. Single Tap Action Block
                    TriggerActionEditorBlock(
                        title = "Single Tap Action:",
                        defaultDescription = definition.defaultTapDescription,
                        selectedBehavior = config.tapBehavior,
                        onBehaviorChange = { behavior ->
                            onConfigChange(config.copy(tapBehavior = behavior))
                        },
                        shellCommand = config.tapShellCommand,
                        onShellCommandChange = { onConfigChange(config.copy(tapShellCommand = it)) },
                        shellCommandOff = config.tapShellCommandOff,
                        onShellCommandOffChange = { onConfigChange(config.copy(tapShellCommandOff = it)) },
                        showOffShellField = true,
                        intentAction = config.tapIntentAction,
                        onIntentActionChange = { onConfigChange(config.copy(tapIntentAction = it)) },
                        intentPackage = config.tapIntentPackage,
                        onIntentPackageChange = { onConfigChange(config.copy(tapIntentPackage = it)) },
                        intentComponent = config.tapIntentComponent,
                        onIntentComponentChange = { onConfigChange(config.copy(tapIntentComponent = it)) },
                        onTestShell = onTestShell,
                        onTestStandardIntent = onTestStandardIntent,
                        onReset = onResetTap,
                        innerContainerBg = innerContainerBg,
                        fieldContainerBg = getSettingsCardBackgroundColor(isDark),
                        testTagPrefix = "tap_${definition.id}"
                    )

                    // 2. Long Press Action Block
                    TriggerActionEditorBlock(
                        title = "Long Press Action:",
                        defaultDescription = definition.defaultLongPressDescription,
                        selectedBehavior = config.longPressBehavior,
                        onBehaviorChange = { behavior ->
                            onConfigChange(config.copy(longPressBehavior = behavior))
                        },
                        shellCommand = config.longPressShellCommand,
                        onShellCommandChange = { onConfigChange(config.copy(longPressShellCommand = it)) },
                        shellCommandOff = "",
                        onShellCommandOffChange = {},
                        showOffShellField = false,
                        intentAction = config.longPressIntentAction,
                        onIntentActionChange = { onConfigChange(config.copy(longPressIntentAction = it)) },
                        intentPackage = config.longPressIntentPackage,
                        onIntentPackageChange = { onConfigChange(config.copy(longPressIntentPackage = it)) },
                        intentComponent = config.longPressIntentComponent,
                        onIntentComponentChange = { onConfigChange(config.copy(longPressIntentComponent = it)) },
                        onTestShell = onTestShell,
                        onTestStandardIntent = onTestStandardIntent,
                        onReset = onResetLongPress,
                        innerContainerBg = innerContainerBg,
                        fieldContainerBg = getSettingsCardBackgroundColor(isDark),
                        testTagPrefix = "long_press_${definition.id}"
                    )
                }
            }
        }
    }
}

@Composable
private fun TriggerActionEditorBlock(
    title: String,
    defaultDescription: String,
    selectedBehavior: ComplicationTriggerBehavior,
    onBehaviorChange: (ComplicationTriggerBehavior) -> Unit,
    shellCommand: String,
    onShellCommandChange: (String) -> Unit,
    shellCommandOff: String,
    onShellCommandOffChange: (String) -> Unit,
    showOffShellField: Boolean,
    intentAction: String,
    onIntentActionChange: (String) -> Unit,
    intentPackage: String,
    onIntentPackageChange: (String) -> Unit,
    intentComponent: String,
    onIntentComponentChange: (String) -> Unit,
    onTestShell: (String, CommandExecutionMode) -> Unit,
    onTestStandardIntent: (String, String, String) -> Unit,
    onReset: () -> Unit,
    innerContainerBg: Color,
    fieldContainerBg: Color,
    testTagPrefix: String
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ExpressiveCardShapes.SingleCard)
            .background(innerContainerBg)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurfaceVariant
        )

        // Full-width segmented selector matching TileCommandCardItem's Execution Mode selector
        val behaviors = ComplicationTriggerBehavior.entries
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            behaviors.forEach { behavior ->
                val isSelected = selectedBehavior == behavior
                val modeShape = SegmentedButtonShapes.getShape(
                    item = behavior,
                    items = behaviors,
                    selectedItem = selectedBehavior
                )
                val itemWeight = when (behavior) {
                    ComplicationTriggerBehavior.STANDARD_COMMAND -> 1.18f
                    ComplicationTriggerBehavior.SHIZUKU_COMMAND -> 1.05f
                    ComplicationTriggerBehavior.DEFAULT -> 1.02f
                    ComplicationTriggerBehavior.COMPLICATION_MENU -> 0.85f
                    ComplicationTriggerBehavior.ROOT_COMMAND -> 0.82f
                }
                Box(
                    modifier = Modifier
                        .weight(itemWeight)
                        .heightIn(min = 48.dp)
                        .clip(modeShape)
                        .background(
                            if (isSelected) colorScheme.primaryContainer
                            else fieldContainerBg
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onBehaviorChange(behavior)
                        }
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                        .testTag("${testTagPrefix}_mode_${behavior.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = behavior.label,
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        val textFieldColors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedContainerColor = fieldContainerBg,
            unfocusedContainerColor = fieldContainerBg,
            focusedLabelColor = colorScheme.primary,
            unfocusedLabelColor = colorScheme.onSurfaceVariant,
            focusedTextColor = colorScheme.onSurface,
            unfocusedTextColor = colorScheme.onSurface
        )

        when (selectedBehavior) {
            ComplicationTriggerBehavior.DEFAULT -> {
                Text(
                    text = "DEFAULT COMPLICATION ACTION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = colorScheme.tertiary
                )
                Text(
                    text = defaultDescription,
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }

            ComplicationTriggerBehavior.COMPLICATION_MENU -> {
                Text(
                    text = "COMPLICATION MENU OVERLAY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = colorScheme.tertiary
                )
                Text(
                    text = "Opens this complication's interactive menu micro-window when triggered.",
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }

            ComplicationTriggerBehavior.STANDARD_COMMAND -> {
                Text(
                    text = "SYSTEM API / INTENT ACTION SETTINGS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = colorScheme.tertiary
                )

                TextField(
                    value = intentAction,
                    onValueChange = onIntentActionChange,
                    label = { Text("Custom Intent / Action", fontSize = 12.sp) },
                    placeholder = { Text("e.g. android.settings.DISPLAY_SETTINGS or com.example.app/.MyActivity", fontSize = 11.5.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("${testTagPrefix}_intent_action")
                )

                TextField(
                    value = intentPackage,
                    onValueChange = onIntentPackageChange,
                    label = { Text("Target Package Name", fontSize = 12.sp) },
                    placeholder = { Text("e.g. com.android.settings", fontSize = 11.5.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("${testTagPrefix}_intent_package")
                )

                TextField(
                    value = intentComponent,
                    onValueChange = onIntentComponentChange,
                    label = { Text("Activity / Service Component", fontSize = 12.sp) },
                    placeholder = { Text("e.g. .DisplaySettings or com.example.app.MyActivity", fontSize = 11.5.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("${testTagPrefix}_intent_component")
                )

                if (intentAction.isBlank() && intentPackage.isBlank() && intentComponent.isBlank()) {
                    Text(
                        text = "⚠️ Standard mode requires a valid Intent Action (e.g. android.settings.DISPLAY_SETTINGS) or Component string.",
                        fontSize = 11.sp,
                        color = colorScheme.error
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = { onTestStandardIntent(intentAction, intentPackage, intentComponent) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("${testTagPrefix}_test_standard"),
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

                    FilledTonalButton(
                        onClick = onReset,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("${testTagPrefix}_reset"),
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

            ComplicationTriggerBehavior.SHIZUKU_COMMAND,
            ComplicationTriggerBehavior.ROOT_COMMAND -> {
                val execMode = if (selectedBehavior == ComplicationTriggerBehavior.ROOT_COMMAND) {
                    CommandExecutionMode.ROOT
                } else {
                    CommandExecutionMode.SHIZUKU
                }

                Text(
                    text = "ADVANCED SHELL / API COMMAND SETTINGS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = colorScheme.tertiary
                )

                TextField(
                    value = shellCommand,
                    onValueChange = onShellCommandChange,
                    label = {
                        Text(
                            if (showOffShellField) "Custom ON Shell Command" else "Custom Shell Command",
                            fontSize = 12.sp
                        )
                    },
                    placeholder = { Text("e.g. cmd statusbar expand-notifications or svc wifi enable", fontSize = 11.5.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("${testTagPrefix}_shell_cmd")
                )

                if (showOffShellField) {
                    TextField(
                        value = shellCommandOff,
                        onValueChange = onShellCommandOffChange,
                        label = { Text("Custom OFF Shell Command", fontSize = 12.sp) },
                        placeholder = { Text("e.g. svc wifi disable", fontSize = 11.5.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = textFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("${testTagPrefix}_shell_cmd_off")
                    )
                }

                if (shellCommand.isBlank() && (!showOffShellField || shellCommandOff.isBlank())) {
                    Text(
                        text = "⚠️ Shell mode requires a valid ON or OFF shell command (e.g. cmd statusbar ...).",
                        fontSize = 11.sp,
                        color = colorScheme.error
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = { onTestShell(shellCommand, execMode) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("${testTagPrefix}_test_shell"),
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

                    if (showOffShellField) {
                        FilledTonalButton(
                            onClick = { onTestShell(shellCommandOff, execMode) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("${testTagPrefix}_test_shell_off"),
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
                            .heightIn(min = 48.dp)
                            .testTag("${testTagPrefix}_reset"),
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

private fun launchTestStandardIntent(
    context: Context,
    action: String,
    pkg: String,
    comp: String
): Boolean {
    return try {
        val fullComp = if (comp.isNotBlank()) comp else if (action.contains("/")) action else ""
        val intent = if (fullComp.contains("/")) {
            val p = fullComp.substringBefore('/').trim()
            val cRaw = fullComp.substringAfter('/').trim()
            val c = if (cRaw.startsWith(".")) "$p$cRaw" else cRaw
            val act = if (action.isNotBlank() && !action.contains("/")) action else Intent.ACTION_MAIN
            Intent(act).apply {
                component = ComponentName(p, c)
                if (p.isNotBlank()) setPackage(p)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else if (pkg.isNotBlank() && comp.isNotBlank()) {
            val c = if (comp.startsWith(".")) "$pkg$comp" else comp
            Intent(action.ifBlank { Intent.ACTION_MAIN }).apply {
                component = ComponentName(pkg, c)
                setPackage(pkg)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else if (action.isBlank() && pkg.isNotBlank()) {
            context.packageManager.getLaunchIntentForPackage(pkg)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            } ?: Intent(Intent.ACTION_MAIN).apply {
                setPackage(pkg)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(action.ifBlank { Intent.ACTION_MAIN }).apply {
                if (pkg.isNotBlank()) setPackage(pkg)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(intent)
        true
    } catch (_: Throwable) {
        false
    }
}
