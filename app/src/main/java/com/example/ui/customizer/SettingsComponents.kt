package com.example.ui.customizer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PanelPreferences

/**
 * Dedicated Card Composable for Settings Sections in ui/customizer.
 *
 * Requirements:
 * - Container color is pure white (Color.White) in light theme (!isSystemInDarkTheme()).
 * - Container color is default surface (MaterialTheme.colorScheme.surface) in dark theme.
 */
@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    shape: Shape = CustomizeCardShapes.SingleCard,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = com.example.ui.theme.getSettingsCardBackgroundColor(isSystemInDarkTheme())
    ),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            colors = colors,
            content = content
        )
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            colors = colors,
            content = content
        )
    }
}

@Composable
fun settingsChipColors(
    containerColor: Color = Color.Transparent,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
): SelectableChipColors {
    return FilterChipDefaults.filterChipColors(
        containerColor = containerColor,
        labelColor = labelColor,
        selectedContainerColor = selectedContainerColor,
        selectedLabelColor = selectedLabelColor
    )
}

/**
 * ListSubheader component for sorting and organizing features into sub-sections.
 */
@Composable
fun ListSubheader(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    color: Color = MaterialTheme.colorScheme.primary,
    isCollapsible: Boolean = false,
    isExpanded: Boolean = true,
    onToggleExpand: (() -> Unit)? = null
) {
    val collapseExpandSubheaders by PanelPreferences.collapseExpandSubheaders.collectAsState()
    val showExpandControls = (isCollapsible || collapseExpandSubheaders) && onToggleExpand != null

    val tag = "toggle_subheader_" + text.lowercase()
        .replace("&", "")
        .replace(",", "")
        .trim()
        .replace("\\s+".toRegex(), "_")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (showExpandControls) {
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = { onToggleExpand() })
                } else Modifier
            )
            .padding(start = 12.dp, end = 12.dp, top = 14.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = text.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                lineHeight = 15.sp,
                color = color,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
            )
        }

        if (showExpandControls) {
            IconButton(
                onClick = { onToggleExpand() },
                modifier = Modifier
                    .size(28.dp)
                    .testTag(tag)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse $text" else "Expand $text",
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Wraps a ListSubheader and its child cards with collapsible AnimatedVisibility
 * controlled by PanelPreferences.collapseExpandSubheaders.
 */
@Composable
fun CollapsibleSubheaderSection(
    title: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    defaultExpanded: Boolean = true,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val collapseExpandSubheaders by PanelPreferences.collapseExpandSubheaders.collectAsState()
    var isExpanded by rememberSaveable(title) { mutableStateOf(defaultExpanded) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CustomizeCardShapes.CardSpacing)
    ) {
        ListSubheader(
            text = title,
            icon = icon,
            color = color,
            isCollapsible = collapseExpandSubheaders,
            isExpanded = isExpanded,
            onToggleExpand = { isExpanded = !isExpanded }
        )

        AnimatedVisibility(
            visible = !collapseExpandSubheaders || isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CustomizeCardShapes.CardSpacing),
                content = content
            )
        }
    }
}
