package com.example.ui.customizer

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PanelPreferences

@Composable
fun VolumeKeyGestureCard(
    modifier: Modifier = Modifier,
    shape: Shape = CustomizeCardShapes.SingleCard
) {
    val context = LocalContext.current
    val enableCustomVolumePanel by PanelPreferences.enableCustomVolumePanel.collectAsState()

    SettingsCard(
        shape = shape,
        modifier = modifier.fillMaxWidth(),
        featureId = FavoriteFeature.GESTURE_VOLUME_KEY.id
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
                        text = "Automatically close Quick Settings panel when pressing hardware volume keys.",
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

@Composable
fun GesturesSection(modifier: Modifier = Modifier) {
    val pinnedIds by PanelPreferences.pinnedFeatures.collectAsState()
    val pinnedInGestures = remember(pinnedIds) {
        pinnedIds.mapNotNull { FavoriteFeature.fromId(it) }.filter { it.category == CustomizeCategory.GESTURES }
    }

    val isVolumePinned = pinnedIds.contains(FavoriteFeature.GESTURE_VOLUME_KEY.id)

    Column(
        modifier = modifier.fillMaxWidth().animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(CustomizeCardShapes.CardSpacing)
    ) {
        if (pinnedInGestures.isNotEmpty()) {
            PinnedSectionHeader(pinnedCount = pinnedInGestures.size)
            pinnedInGestures.forEachIndexed { index, feat ->
                val cardShape = CustomizeCardShapes.getShapeForIndex(index, pinnedInGestures.size)
                when (feat) {
                    FavoriteFeature.GESTURE_VOLUME_KEY -> VolumeKeyGestureCard(shape = cardShape)
                    else -> GenericPinnedFeatureCard(feature = feat, shape = cardShape)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (!isVolumePinned) {
            CollapsibleSubheaderSection(
                title = "Hardware Key Actions",
                icon = Icons.Default.VolumeUp
            ) {
                VolumeKeyGestureCard(shape = CustomizeCardShapes.SingleCard)
            }
        }
    }
}
