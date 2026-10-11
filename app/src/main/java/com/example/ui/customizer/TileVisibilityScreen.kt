package com.example.ui.customizer

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PrivilegeMode
import com.example.system.ExternalTileIcon
import com.example.ui.MainViewModel

/**
 * Card shapes for Tile Visibility screen.
 * Follows the 4dp padding between cards rule, with inner corners strictly 5dp.
 */
private object VisibilityCardShapes {
    val OuterCorner = 16.dp
    val InsideCorner = 5.dp
    val CardSpacing = 4.dp

    val TopCard = RoundedCornerShape(
        topStart = OuterCorner,
        topEnd = OuterCorner,
        bottomStart = InsideCorner,
        bottomEnd = InsideCorner
    )
    val CenterCard = RoundedCornerShape(InsideCorner)
    val BottomCard = RoundedCornerShape(
        topStart = InsideCorner,
        topEnd = InsideCorner,
        bottomStart = OuterCorner,
        bottomEnd = OuterCorner
    )
    val SingleCard = RoundedCornerShape(OuterCorner)
}

@Composable
fun TileVisibilityScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allTiles by viewModel.allRawTiles.collectAsState()
    val hiddenTileIds by viewModel.hiddenTileIds.collectAsState()
    val privilegeMode by viewModel.privilegeEngine.currentMode.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    // Definition of ADB/Root required tiles
    val requiresAdbRootSet = remember {
        setOf(
            "wifi", "cellular", "hotspot", "location", "airplane", "nfc",
            "battery_saver", "dnd", "dark_mode", "aod", "ambient_display",
            "screenshot", "screenrecord", "cpu_info", "fps_info", "sound_search"
        )
    }

    val filteredTiles = remember(allTiles, searchQuery) {
        if (searchQuery.isBlank()) {
            allTiles
        } else {
            val q = searchQuery.trim().lowercase()
            allTiles.filter {
                it.label.lowercase().contains(q) ||
                it.subtitle.lowercase().contains(q) ||
                it.id.lowercase().contains(q)
            }
        }
    }

    val colorScheme = MaterialTheme.colorScheme
    val isDark = com.example.ui.theme.LocalIsDarkTheme.current
    val screenBg = com.example.ui.theme.getSettingsScreenBackgroundColor(isDark)
    val cardBg = com.example.ui.theme.getSettingsCardBackgroundColor(isDark)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("tile_visibility_screen"),
        verticalArrangement = Arrangement.spacedBy(VisibilityCardShapes.CardSpacing)
    ) {
        // Section Header with Monet Pill Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "TILE VISIBILITY & BLACKLIST",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                color = colorScheme.primary
            )

            Surface(
                color = colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${allTiles.size - hiddenTileIds.size}/${allTiles.size} Active",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        val isDark = com.example.ui.theme.LocalIsDarkTheme.current
        val cardBg = com.example.ui.theme.getSettingsCardBackgroundColor(isDark)

        // Search Bar with Monet Styling
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Search tiles",
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = cardBg,
                unfocusedContainerColor = cardBg,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = colorScheme.onSurface,
                unfocusedTextColor = colorScheme.onSurface
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Smart Suggestion Action Card
        SettingsCard(
            shape = VisibilityCardShapes.SingleCard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = "Compatibility Auto-Hide",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 17.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = "Mode: ${privilegeMode.badge}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 14.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                            color = colorScheme.primary
                        )
                    }
                }

                Text(
                    text = "Automatically hide tiles that are unsupported in your current operational mode (${privilegeMode.badge}) to declutter your Quick Settings panel.",
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                    color = colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = { viewModel.applySmartHideUnsupportedTiles() },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Smart Hide Unsupported Tiles",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // List of Tiles Heading
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "TILES (${filteredTiles.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = colorScheme.onSurfaceVariant
            )

            if (hiddenTileIds.isNotEmpty()) {
                Surface(
                    color = colorScheme.errorContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${hiddenTileIds.size} Hidden",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Render each Tile as a Monet Card with 5dp inside corners and 4dp spacing
        filteredTiles.forEachIndexed { index, tile ->
            val isHidden = hiddenTileIds.contains(tile.id)
            val requiresAdbRoot = requiresAdbRootSet.contains(tile.id)
            val isUnsupported = requiresAdbRoot && privilegeMode == PrivilegeMode.STANDARD

            // Card shape with 5dp inner corners
            val cardShape = when {
                filteredTiles.size == 1 -> VisibilityCardShapes.SingleCard
                index == 0 -> VisibilityCardShapes.TopCard
                index == filteredTiles.size - 1 -> VisibilityCardShapes.BottomCard
                else -> VisibilityCardShapes.CenterCard
            }

            SettingsCard(
                shape = cardShape,
                colors = CardDefaults.cardColors(
                    containerColor = if (isHidden) screenBg else cardBg
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("visibility_tile_${tile.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Tile Details & Icon
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Tile Icon with Active QS Tile primary background and white icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isHidden) {
                                        colorScheme.primary.copy(alpha = 0.35f)
                                    } else {
                                        colorScheme.primary
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            val isThirdParty = tile.isExternal || tile.category == com.example.model.TileCategory.THIRD_PARTY || tile.id.contains("/")
                            val iconTint = colorScheme.onPrimary

                            if (isThirdParty) {
                                val pkg = tile.packageName ?: if (tile.id.contains("/")) tile.id.substringBefore('/') else null
                                val cls = tile.serviceClassName ?: if (tile.id.contains("/")) tile.id.substringAfter('/') else null
                                ExternalTileIcon(
                                    packageName = pkg,
                                    serviceClassName = cls,
                                    tintColor = iconTint,
                                    contentDescription = tile.label,
                                    fallbackDrawable = tile.externalDrawable,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                tile.iconVector?.let { vector ->
                                    Icon(
                                        imageVector = vector,
                                        contentDescription = tile.label,
                                        tint = iconTint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } ?: Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = tile.label,
                                    tint = iconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Label, Subtitle & Badges
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = tile.label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false),
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    color = if (isHidden) {
                                        colorScheme.onSurface.copy(alpha = 0.5f)
                                    } else {
                                        colorScheme.onSurface
                                    }
                                )

                                if (tile.isExternal) {
                                    Surface(
                                        color = colorScheme.tertiaryContainer,
                                        contentColor = colorScheme.onTertiaryContainer,
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = "3rd Party",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = tile.subtitle,
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                color = colorScheme.onSurfaceVariant.copy(alpha = if (isHidden) 0.5f else 0.75f)
                            )

                            // Privilege / Support Badges with Monet styling
                            Row(
                                modifier = Modifier.padding(top = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isUnsupported) {
                                    Surface(
                                        color = colorScheme.errorContainer,
                                        contentColor = colorScheme.onErrorContainer,
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = "Unsupported in Standard Mode",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                } else {
                                    val (badgeBg, badgeTextCol, badgeText) = if (requiresAdbRoot) {
                                        Triple(colorScheme.primaryContainer, colorScheme.onPrimaryContainer, "Requires ADB/Root")
                                    } else {
                                        Triple(colorScheme.secondaryContainer, colorScheme.onSecondaryContainer, "Standard API")
                                    }
                                    Surface(
                                        color = badgeBg,
                                        contentColor = badgeTextCol,
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = badgeText,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Show / Hide Switch with Monet colors
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isHidden) "Hidden" else "Visible",
                            tint = if (isHidden) colorScheme.outline else colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Switch(
                            checked = !isHidden,
                            onCheckedChange = { isVisible ->
                                viewModel.setTileHidden(tile.id, !isVisible)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colorScheme.onPrimary,
                                checkedTrackColor = colorScheme.primary,
                                uncheckedThumbColor = colorScheme.outline,
                                uncheckedTrackColor = colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
