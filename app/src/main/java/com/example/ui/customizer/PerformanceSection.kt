package com.example.ui.customizer

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.ui.theme.ExpressiveCardShapes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PerformanceSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val disablePanelAnimations by PanelPreferences.disablePanelAnimations.collectAsState()
    val usePrimeCoreAffinity by PanelPreferences.usePrimeCoreAffinity.collectAsState()
    val animationSpeedScale by PanelPreferences.animationSpeedScale.collectAsState()
    var localSliderValue by remember(animationSpeedScale) { mutableStateOf(animationSpeedScale) }

    // Debounce Settings.Global writes so continuous dragging does not flood IPC calls
    LaunchedEffect(localSliderValue) {
        if (localSliderValue != animationSpeedScale) {
            delay(150)
            PanelPreferences.setAnimationSpeedScale(context, localSliderValue)
            try {
                val engine = NativeTileManager.instance?.privilegeEngine ?: PrivilegeEngine(context)
                engine.applyAnimationScale(localSliderValue)
            } catch (e: Exception) {}
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ExpressiveCardShapes.CardSpacing)
    ) {
        // 1. Disable Panel Animations Toggle Card
        SettingsCard(
            shape = CustomizeCardShapes.TopCard
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

        // 2. Prime Core Mode (Root taskset) Card
        SettingsCard(
            shape = CustomizeCardShapes.BottomCard,
            modifier = Modifier.testTag("prime_core_affinity_card")
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

        // 3. System Animation Speed Controller Card - Hidden
        val showSystemAnimationSpeed = false
        if (showSystemAnimationSpeed) {
            SettingsCard(
                shape = CustomizeCardShapes.BottomCard,
                modifier = Modifier.testTag("system_animation_speed_card")
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
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = "System Animation Speed",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 17.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Set system animator duration scale via Settings.Global (requires Shizuku/Root/WRITE_SECURE_SETTINGS).",
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = String.format(java.util.Locale.US, "%.2fx", localSliderValue),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }

                    Slider(
                        value = localSliderValue,
                        onValueChange = { localSliderValue = it },
                        valueRange = 0f..2f,
                        colors = SliderDefaults.colors(
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                            thumbColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("animation_speed_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0.0x (Off)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("1.0x (Default)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("2.0x (Slow)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
