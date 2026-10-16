package com.example.ui.customizer

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import com.example.data.PanelPreferences

enum class CustomizeCategory(
    val title: String,
    val icon: ImageVector,
    val description: String
) {
    HEADS_UP("Heads-Up", Icons.Default.NotificationsActive, "Notification banner overlays & styles"),
    BASIC("Basic Settings", Icons.Default.Settings, "Theme mode & basic appearance"),
    QS_DESIGN("Quick Settings Design", Icons.Default.Apps, "Tile shapes, sizes, carrier & sliders"),
    QS_TWEAKS("Quick Settings Tweaks", Icons.Default.Tune, "Grid layout, drag handle & header options"),
    DEBUG("Debug", Icons.Default.Terminal, "Diagnostics and developer toggles"),
    TRIGGER("Trigger", Icons.Default.TouchApp, "Status bar gestures & panel interception"),
    GESTURES("Gestures", Icons.Default.VolumeUp, "Hardware key actions & gestures"),
    PERFORMANCE("Performance", Icons.Default.Speed, "Animations, prime core & speed scales"),
    ABOUT("About", Icons.Default.Info, "Developer info, Telegram channel & app details")
}

object CustomizeCardShapes {
    val CardSpacing = 4.dp
    val OuterCorner = 16.dp
    val InsideCorner = 4.dp

    val TopCard = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
    val CenterCard = RoundedCornerShape(4.dp)
    val BottomCard = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    val SingleCard = RoundedCornerShape(16.dp)
}


@Composable
fun CategorySectionHeader(
    category: CustomizeCategory,
    itemCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp, start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(17.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.title.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = category.description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
