package com.fauyds.quicksettings.ui.nativecontrols

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhoneIphone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.fauyds.quicksettings.model.MediaState
import com.fauyds.quicksettings.ui.overlay.CompactMediaTile
import com.fauyds.quicksettings.ui.overlay.ExpandedMediaCard
import com.fauyds.quicksettings.system.NativeTileManager
import com.fauyds.quicksettings.ui.theme.ExpressiveCardShapes
import com.fauyds.quicksettings.ui.theme.MonetOnPrimary
import com.fauyds.quicksettings.ui.theme.MonetPrimary
import com.fauyds.quicksettings.ui.theme.MonetTertiary

@Composable
fun NativeControlsScreen(
    nativeManager: NativeTileManager,
    isWifi: Boolean,
    isBt: Boolean,
    isCellular: Boolean,
    isTorch: Boolean,
    isRotate: Boolean,
    isDnd: Boolean,
    isAirplane: Boolean,
    isLocation: Boolean,
    isHotspot: Boolean,
    brightness: Int,
    mediaVolume: Float,
    ringVolume: Float,
    alarmVolume: Float,
    notifVolume: Float,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(ExpressiveCardShapes.CardSpacing) // 4.dp
    ) {
        // TOP CARD: top corners 32dp, bottom corners 8dp
        Card(
            shape = ExpressiveCardShapes.TopCard,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = "NATIVE SYSTEM CONTROLS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp,
                            lineHeight = 14.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MonetPrimary
                        )
                        Text(
                            text = "Live telemetry & direct hardware observers",
                            fontSize = 13.sp,
                            lineHeight = 16.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { nativeManager.refreshAllStates() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .testTag("refresh_hardware_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MonetPrimary, modifier = Modifier.size(18.dp))
                    }
                }

                // Brightness Controller with Live Observer
                Text(
                    text = "Display Brightness: ${((brightness / 255f) * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.BrightnessMedium, contentDescription = null, tint = MonetPrimary, modifier = Modifier.size(20.dp))
                    Slider(
                        value = brightness.toFloat(),
                        onValueChange = { nativeManager.setBrightness(it.toInt()) },
                        valueRange = 5f..255f,
                        modifier = Modifier.weight(1f).testTag("hw_brightness_slider"),
                        colors = SliderDefaults.colors(thumbColor = MonetPrimary, activeTrackColor = MonetPrimary, inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }
        }

        // CENTER CARD: top corners 8dp, bottom corners 8dp
        Card(
            shape = ExpressiveCardShapes.CenterCard,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "HARDWARE & RADIO TOGGLES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MonetTertiary
                )

                ToggleRow(label = "Wi-Fi Network", state = isWifi, icon = Icons.Default.Wifi, onToggle = { nativeManager.toggleWifi() })
                ToggleRow(label = "Bluetooth Radio", state = isBt, icon = Icons.Default.Bluetooth, onToggle = { nativeManager.toggleBluetooth() })
                ToggleRow(label = "Cellular Mobile Data", state = isCellular, icon = Icons.Default.CellTower, onToggle = { nativeManager.toggleCellular() })
                ToggleRow(label = "Flashlight / Torch", state = isTorch, icon = Icons.Default.FlashlightOn, onToggle = { nativeManager.toggleTorch() })
                ToggleRow(label = "Auto-Rotate Screen", state = isRotate, icon = Icons.Default.ScreenRotation, onToggle = { nativeManager.toggleAutoRotate() })
                ToggleRow(label = "Do Not Disturb (DND)", state = isDnd, icon = Icons.Default.DoNotDisturb, onToggle = { nativeManager.toggleDnd() })
                ToggleRow(label = "Airplane Mode", state = isAirplane, icon = Icons.Default.Flight, onToggle = { nativeManager.toggleAirplaneMode() })
                ToggleRow(label = "Location / GPS", state = isLocation, icon = Icons.Default.LocationOn, onToggle = { nativeManager.toggleLocation() })
                ToggleRow(label = "Mobile Hotspot", state = isHotspot, icon = Icons.Default.WifiTethering, onToggle = { nativeManager.toggleHotspot() })
            }
        }

        // BOTTOM CARD: top corners 8dp, bottom corners 32dp
        Card(
            shape = ExpressiveCardShapes.BottomCard,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "AUDIO STREAMS CONTROLLER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MonetPrimary
                )

                AudioStreamRow(
                    label = "Media Playback",
                    icon = Icons.Outlined.MusicNote,
                    value = mediaVolume,
                    onValueChange = { nativeManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, it) },
                    accent = MonetPrimary
                )

                AudioStreamRow(
                    label = "Phone Ringtone",
                    icon = Icons.Outlined.PhoneIphone,
                    value = ringVolume,
                    onValueChange = { nativeManager.setStreamVolume(android.media.AudioManager.STREAM_RING, it) },
                    accent = MonetTertiary
                )

                AudioStreamRow(
                    label = "System Alarm",
                    icon = Icons.Outlined.Alarm,
                    value = alarmVolume,
                    onValueChange = { nativeManager.setStreamVolume(android.media.AudioManager.STREAM_ALARM, it) },
                    accent = MonetPrimary
                )

                AudioStreamRow(
                    label = "App Notifications",
                    icon = Icons.Outlined.Notifications,
                    value = notifVolume,
                    onValueChange = { nativeManager.setStreamVolume(android.media.AudioManager.STREAM_NOTIFICATION, it) },
                    accent = MonetTertiary
                )
            }
        }

        // MEDIA PLAYER PLACEHOLDER SHOWCASE (Connected to MediaSessions with Preview fallback)
        val liveMediaState by com.fauyds.quicksettings.service.MediaSessionManagerHelper.mediaState.collectAsState()
        var sampleMedia by remember(liveMediaState) { mutableStateOf(liveMediaState ?: MediaState.Preview) }

        Card(
            shape = ExpressiveCardShapes.SingleCard,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MEDIA PLAYER (QS / NOTIFICATION UI)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MonetPrimary
                    )
                    Text(
                        text = "Android 10/11",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "1. Compact Media Tile (QS / QQS Layout):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                CompactMediaTile(
                    mediaState = sampleMedia,
                    cornerRadius = 16.dp,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    accentColor = MonetPrimary,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    onPlayPauseClick = {
                        sampleMedia = sampleMedia.copy(isPlaying = !sampleMedia.isPlaying)
                    },
                    onNextClick = {
                        sampleMedia = sampleMedia.copy(
                            title = "Blinding Lights",
                            artist = "The Weeknd",
                            progress = 0.15f,
                            currentPositionMs = 30000L
                        )
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "2. Expanded Media Card (Notification Panel Layout):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                ExpandedMediaCard(
                    mediaState = sampleMedia,
                    cornerRadius = 20.dp,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    accentColor = MonetPrimary,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    onPlayPauseClick = {
                        sampleMedia = sampleMedia.copy(isPlaying = !sampleMedia.isPlaying)
                    },
                    onPreviousClick = {
                        sampleMedia = sampleMedia.copy(
                            title = "Never Let You Go",
                            artist = "Kygo",
                            progress = 0.02f,
                            currentPositionMs = 4000L
                        )
                    },
                    onNextClick = {
                        sampleMedia = sampleMedia.copy(
                            title = "Higher Love",
                            artist = "Kygo & Whitney Houston",
                            progress = 0.28f,
                            currentPositionMs = 58000L
                        )
                    },
                    onFavoriteClick = {
                        sampleMedia = sampleMedia.copy(isFavorite = !sampleMedia.isFavorite)
                    },
                    onSeek = { newProgress ->
                        sampleMedia = sampleMedia.copy(
                            progress = newProgress,
                            currentPositionMs = (newProgress * sampleMedia.totalDurationMs).toLong()
                        )
                    },
                    onOutputDeviceClick = {
                        val nextDevice = if (sampleMedia.outputDevice == "Phone speaker") "Pixel Buds Pro" else "Phone speaker"
                        sampleMedia = sampleMedia.copy(outputDevice = nextDevice)
                    }
                )
            }
        }
    }
}

@Composable
fun ToggleRow(
    label: String,
    state: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (state) MonetPrimary else Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (state) MonetOnPrimary else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (state) "Enabled" else "Disabled",
                    fontSize = 11.sp,
                    color = if (state) MonetPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = state,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MonetOnPrimary,
                checkedTrackColor = MonetPrimary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier.testTag("toggle_${label.lowercase().replace(" ", "_")}")
        )
    }
}

@Composable
fun AudioStreamRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: Float,
    onValueChange: (Float) -> Unit,
    accent: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
        }

        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(96.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent, inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant)
        )

        Text(
            text = "${(value * 100).toInt()}%",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(36.dp)
        )
    }
}
