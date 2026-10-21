package com.example.ui.customizer

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PanelPreferences
import com.example.system.NativeTileManager
import com.example.system.PrivilegeEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Miscellaneous section containing Performance, Gestures, Trigger, and Debug options
 * organized under individual collapsible ListSubheaders with proper corner shapes.
 * Supports pinning cards to the top of the section.
 */
@Composable
fun MiscellaneousSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Preferences states
    val disablePanelAnimations by PanelPreferences.disablePanelAnimations.collectAsState()
    val usePrimeCoreAffinity by PanelPreferences.usePrimeCoreAffinity.collectAsState()
    val enableCustomVolumePanel by PanelPreferences.enableCustomVolumePanel.collectAsState()

    val invisibleStatusBarTrigger by PanelPreferences.invisibleStatusBarTrigger.collectAsState()
    val autoSystemPanelClosing by PanelPreferences.autoSystemPanelClosing.collectAsState()
    val panelReplacementDelayMs by PanelPreferences.panelReplacementDelayMs.collectAsState()
    val collapseExpandOptions by PanelPreferences.collapseExpandOptions.collectAsState()
    var delayExpanded by rememberSaveable { mutableStateOf(false) }

    val overrideSystemQs by PanelPreferences.overrideSystemQs.collectAsState()
    val detectorCooldownMs by PanelPreferences.detectorCooldownMs.collectAsState()
    val stockShadeWhitelist by PanelPreferences.stockShadeWhitelist.collectAsState()
    val customBlacklistPackages by PanelPreferences.customBlacklistPackages.collectAsState()
    val customBlacklistClasses by PanelPreferences.customBlacklistClasses.collectAsState()

    val forceShowMediaPreview by PanelPreferences.forceShowMediaPreview.collectAsState()

    val pinnedIds by PinnedCardsManager.getPinnedCardIds(CustomizeCategory.MISCELLANEOUS, context).collectAsState()

    // 1. Disable Panel Animations Card
    val disablePanelAnimationsCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("disable_panel_animations_card", CustomizeCategory.MISCELLANEOUS, "Disable Panel Animations") {
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
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = "Disable Panel Animations",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 17.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Open, close, and expand Quick Settings instantly without transition animations or slide delays.",
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = disablePanelAnimations,
                        onCheckedChange = { PanelPreferences.setDisablePanelAnimations(context, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("disable_panel_animations_toggle")
                    )
                }
            }
        }
    }

    // 2. Prime Core Mode Card
    val primeCoreAffinityCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("prime_core_affinity_card", CustomizeCategory.MISCELLANEOUS, "Prime Core High Smoothness Mode") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("prime_core_affinity_card")
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
                                imageVector = Icons.Default.Memory,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = "Prime Core High Smoothness Mode",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 17.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Works with Root only. Pins CPU thread affinity to the prime core to completely eliminate swipe and scrolling micro-stutters.",
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = usePrimeCoreAffinity,
                        onCheckedChange = { enabled ->
                            coroutineScope.launch(Dispatchers.IO) {
                                val engine = NativeTileManager.instance?.privilegeEngine ?: PrivilegeEngine(context)
                                if (enabled) {
                                    if (engine.isRootGranted()) {
                                        val ok = engine.applyPrimeCoreAffinity(true)
                                        PanelPreferences.setUsePrimeCoreAffinity(context, true)
                                        withContext(Dispatchers.Main) {
                                            if (ok) {
                                                Toast.makeText(context, "High Smoothness Mode enabled via Root!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                val log = engine.lastExecutionLog.value
                                                Toast.makeText(context, "Taskset error: $log", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    } else {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Root permission required for High Smoothness mode", Toast.LENGTH_SHORT).show()
                                            engine.requestRootPermission()
                                        }
                                    }
                                } else {
                                    engine.applyPrimeCoreAffinity(false)
                                    PanelPreferences.setUsePrimeCoreAffinity(context, false)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Standard CPU scheduling restored", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("prime_core_affinity_toggle")
                    )
                }
            }
        }
    }

    // 3. Hardware Key Actions Card
    val customVolumePanelCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("custom_volume_panel_card", CustomizeCategory.MISCELLANEOUS, "Hide QS/QQS on Volume Key Press") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.fillMaxWidth()
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
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = "Hide QS/QQS on Volume Key Press",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 17.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Automatically close Quick Settings panel and place a transparent corner square when pressing hardware volume keys.",
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = enableCustomVolumePanel,
                        onCheckedChange = { PanelPreferences.setEnableCustomVolumePanel(context, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("custom_volume_panel_toggle")
                    )
                }
            }
        }
    }

    // 4. Invisible Status Bar Trigger Card
    val invisibleStatusBarTriggerCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("invisible_status_bar_trigger_card", CustomizeCategory.MISCELLANEOUS, "Invisible Status Bar Trigger") {
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
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = "Invisible Status Bar Trigger",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 17.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Make the status bar trigger totally invisible while keeping swipe gestures active.",
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = invisibleStatusBarTrigger,
                        onCheckedChange = { PanelPreferences.setInvisibleStatusBarTrigger(context, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("invisible_status_bar_trigger_toggle")
                    )
                }
            }
        }
    }

    // 5. Auto System Panel Closing Card
    val autoSystemPanelClosingCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("auto_system_panel_closing_card", CustomizeCategory.MISCELLANEOUS, "Use auto system panel closing") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("auto_system_panel_closing_card")
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
                                imageVector = Icons.Default.ExpandLess,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Use auto system panel closing",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 17.sp,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "A12-A15",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Actively forces the stock notification shade to collapse on Android 12 through 15 when opening the custom panel, resolving issues on devices where the system panel remains stuck open.",
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = autoSystemPanelClosing,
                        onCheckedChange = { PanelPreferences.setAutoSystemPanelClosing(context, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("auto_system_panel_closing_toggle")
                    )
                }
            }
        }
    }

    // 6. Panel Replacement Delay Card
    val panelReplacementDelayCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("panel_replacement_delay_card", CustomizeCategory.MISCELLANEOUS, "Panel Replacement Delay") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("panel_replacement_delay_card")
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (collapseExpandOptions) Modifier.clickable { delayExpanded = !delayExpanded } else Modifier
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
                                    imageVector = Icons.Default.Timelapse,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text(
                                    text = "Panel Replacement Delay",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 17.sp,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Set a custom delay before substituting the stock system notification shade with our custom panel.",
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
                            Text(
                                text = if (panelReplacementDelayMs == 0) "0 ms (Instant)" else "$panelReplacementDelayMs ms",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 8.dp)
                            )

                            if (collapseExpandOptions) {
                                IconButton(
                                    onClick = { delayExpanded = !delayExpanded },
                                    modifier = Modifier.size(36.dp).testTag("toggle_expand_delay")
                                ) {
                                    Icon(
                                        imageVector = if (delayExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (delayExpanded) "Collapse" else "Expand",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = !collapseExpandOptions || delayExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Slider(
                                value = panelReplacementDelayMs.toFloat(),
                                onValueChange = { PanelPreferences.setPanelReplacementDelayMs(context, (it / 10).toInt() * 10) },
                                valueRange = 0f..1000f,
                                steps = 99,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("panel_replacement_delay_slider"),
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
                                Text("0 ms (Instant)", fontSize = 10.sp, color = Color.Gray)
                                Text("Default (0 ms)", fontSize = 10.sp, color = Color.Gray)
                                Text("1000 ms (1.0s)", fontSize = 10.sp, color = Color.Gray)
                            }

                            // Quick Preset Buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val delayPresets = listOf(
                                    0 to "Instant (0 ms)",
                                    50 to "50 ms",
                                    150 to "150 ms",
                                    300 to "300 ms",
                                    500 to "500 ms",
                                    1000 to "1000 ms"
                                )
                                delayPresets.forEachIndexed { index, (delayVal, label) ->
                                    val isSelected = panelReplacementDelayMs == delayVal
                                    val presetBtnShape = if (isSelected) {
                                        CircleShape
                                    } else {
                                        when (index) {
                                            0 -> RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp, topEnd = 8.dp, bottomEnd = 8.dp)
                                            delayPresets.size - 1 -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = 50.dp, bottomEnd = 50.dp)
                                            else -> RoundedCornerShape(8.dp)
                                        }
                                    }

                                    Button(
                                        onClick = { PanelPreferences.setPanelReplacementDelayMs(context, delayVal) },
                                        shape = presetBtnShape,
                                        border = null,
                                        elevation = null,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else com.example.ui.theme.getSettingsScreenBackgroundColor(isSystemInDarkTheme()),
                                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        ),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("delay_preset_${delayVal}")
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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

    // 7. Stock Shade Auto-Override Card
    val stockShadeOverrideCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("stock_shade_override_card", CustomizeCategory.MISCELLANEOUS, "Stock Shade Auto-Override") {
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
                                    imageVector = Icons.Default.PublishedWithChanges,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text(
                                    text = "Stock Shade Auto-Override",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 17.sp,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Automatically intercepts native System UI notification shade expansion and substitutes it with the custom Quick Settings panel.",
                                    fontSize = 12.sp,
                                    lineHeight = 15.sp,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = overrideSystemQs,
                            onCheckedChange = { PanelPreferences.setOverrideSystemQs(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("stock_shade_override_toggle")
                        )
                    }

                    if (overrideSystemQs) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Detector Cooldown Slider Section
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Detector Cooldown",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$detectorCooldownMs ms",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = "Cooldown delay between consecutive interception triggers (10 ms to 1000 ms).",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Slider(
                                value = detectorCooldownMs.toFloat(),
                                onValueChange = { PanelPreferences.setDetectorCooldownMs(context, (it / 10).toInt() * 10) },
                                valueRange = 10f..1000f,
                                steps = 98,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("detector_cooldown_slider"),
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
                                Text("10 ms", fontSize = 10.sp, color = Color.Gray)
                                Text("Default (280 ms)", fontSize = 10.sp, color = Color.Gray)
                                Text("1000 ms", fontSize = 10.sp, color = Color.Gray)
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Target Whitelisted Packages
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Target Whitelisted Packages:",
                                fontSize = 12.sp,
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
                                stockShadeWhitelist.forEach { pkg ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.background,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                                        ) {
                                            Text(
                                                text = pkg,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (stockShadeWhitelist.size > 1) {
                                                IconButton(
                                                    onClick = {
                                                        PanelPreferences.removeStockShadeWhitelistPackage(context, pkg)
                                                    },
                                                    modifier = Modifier.size(20.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Remove $pkg",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            var newPackageInput by remember { mutableStateOf("") }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = newPackageInput,
                                    onValueChange = { newPackageInput = it },
                                    placeholder = { Text("e.g. com.vendor.systemui", fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("add_whitelist_pkg_input"),
                                    textStyle = TextStyle(fontSize = 12.sp)
                                )

                                Button(
                                    onClick = {
                                        if (newPackageInput.isNotBlank()) {
                                            PanelPreferences.addStockShadeWhitelistPackage(context, newPackageInput)
                                            newPackageInput = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.testTag("add_whitelist_pkg_btn")
                                ) {
                                    Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Custom Detection Blacklist Section
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Custom Detection Blacklist (Packages & Classes):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Events containing these packages or class names will be immediately rejected.",
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
                                customBlacklistPackages.forEach { pkg ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.background,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                                        ) {
                                            Text(
                                                text = pkg,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            IconButton(
                                                onClick = {
                                                    PanelPreferences.removeCustomBlacklistPackage(context, pkg)
                                                },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove $pkg",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                customBlacklistClasses.forEach { cls ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.background,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                                        ) {
                                            Text(
                                                text = cls,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            IconButton(
                                                onClick = {
                                                    PanelPreferences.removeCustomBlacklistClass(context, cls)
                                                },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove $cls",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            var newBlacklistInput by remember { mutableStateOf("") }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = newBlacklistInput,
                                    onValueChange = { newBlacklistInput = it },
                                    placeholder = { Text("e.g. com.example.app or Dialog", fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("add_blacklist_input"),
                                    textStyle = TextStyle(fontSize = 12.sp)
                                )

                                Button(
                                    onClick = {
                                        val input = newBlacklistInput.trim()
                                        if (input.isNotBlank()) {
                                            if (input.contains(".")) {
                                                PanelPreferences.addCustomBlacklistPackage(context, input)
                                            } else {
                                                PanelPreferences.addCustomBlacklistClass(context, input)
                                            }
                                            newBlacklistInput = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ),
                                    modifier = Modifier.testTag("add_blacklist_btn")
                                ) {
                                    Text("Blacklist", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 8. Force Show Media Preview Card
    val forceShowMediaPreviewCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("force_show_media_preview_card", CustomizeCategory.MISCELLANEOUS, "Force Show Preview Placeholder") {
            SettingsCard(
                shape = shape,
                modifier = Modifier.fillMaxWidth()
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
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = "Force Show Preview Placeholder",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 17.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Forces the Media Player card to remain visible with preview track data for testing when no music is playing.",
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = forceShowMediaPreview,
                        onCheckedChange = { PanelPreferences.setForceShowMediaPreview(context, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("force_show_media_preview_toggle")
                    )
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CustomizeCardShapes.CardSpacing)
    ) {
        // 1. Pinned Cards at the very top of Miscellaneous
        val pinnedCards = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
        if (pinnedIds.contains("disable_panel_animations_card")) pinnedCards.add(disablePanelAnimationsCard)
        if (pinnedIds.contains("prime_core_affinity_card")) pinnedCards.add(primeCoreAffinityCard)
        if (pinnedIds.contains("custom_volume_panel_card")) pinnedCards.add(customVolumePanelCard)
        if (pinnedIds.contains("invisible_status_bar_trigger_card")) pinnedCards.add(invisibleStatusBarTriggerCard)
        if (pinnedIds.contains("auto_system_panel_closing_card")) pinnedCards.add(autoSystemPanelClosingCard)
        if (pinnedIds.contains("panel_replacement_delay_card")) pinnedCards.add(panelReplacementDelayCard)
        if (pinnedIds.contains("stock_shade_override_card")) pinnedCards.add(stockShadeOverrideCard)
        if (pinnedIds.contains("force_show_media_preview_card")) pinnedCards.add(forceShowMediaPreviewCard)

        if (pinnedCards.isNotEmpty()) {
            ListSubheader(
                text = "Pinned",
                icon = Icons.Default.PushPin
            )
            pinnedCards.forEachIndexed { index, card ->
                card(CustomizeCardShapes.getShapeForIndex(index, pinnedCards.size))
            }
        }

        // 2. Unpinned Performance
        val unpinnedPerf = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
        if (!pinnedIds.contains("disable_panel_animations_card")) unpinnedPerf.add(disablePanelAnimationsCard)
        if (!pinnedIds.contains("prime_core_affinity_card")) unpinnedPerf.add(primeCoreAffinityCard)

        if (unpinnedPerf.isNotEmpty()) {
            CollapsibleSubheaderSection(
                title = "Performance",
                icon = Icons.Default.Speed
            ) {
                unpinnedPerf.forEachIndexed { index, card ->
                    card(CustomizeCardShapes.getShapeForIndex(index, unpinnedPerf.size))
                }
            }
        }

        // 3. Unpinned Gestures
        if (!pinnedIds.contains("custom_volume_panel_card")) {
            CollapsibleSubheaderSection(
                title = "Gestures",
                icon = Icons.Default.VolumeUp
            ) {
                customVolumePanelCard(CustomizeCardShapes.SingleCard)
            }
        }

        // 4. Unpinned Trigger
        val unpinnedTrigger = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
        if (!pinnedIds.contains("invisible_status_bar_trigger_card")) unpinnedTrigger.add(invisibleStatusBarTriggerCard)
        if (!pinnedIds.contains("auto_system_panel_closing_card")) unpinnedTrigger.add(autoSystemPanelClosingCard)
        if (!pinnedIds.contains("panel_replacement_delay_card")) unpinnedTrigger.add(panelReplacementDelayCard)
        if (!pinnedIds.contains("stock_shade_override_card")) unpinnedTrigger.add(stockShadeOverrideCard)

        if (unpinnedTrigger.isNotEmpty()) {
            CollapsibleSubheaderSection(
                title = "Trigger",
                icon = Icons.Default.TouchApp
            ) {
                unpinnedTrigger.forEachIndexed { index, card ->
                    card(CustomizeCardShapes.getShapeForIndex(index, unpinnedTrigger.size))
                }
            }
        }

        // 5. Unpinned Debug
        if (!pinnedIds.contains("force_show_media_preview_card")) {
            CollapsibleSubheaderSection(
                title = "Debug",
                icon = Icons.Default.BugReport
            ) {
                forceShowMediaPreviewCard(CustomizeCardShapes.SingleCard)
            }
        }
    }
}
