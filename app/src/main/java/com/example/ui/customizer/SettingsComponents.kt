package com.example.ui.customizer

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PanelPreferences
import com.example.ui.customizer.CustomizeCategory

val LocalPinnableCardId = staticCompositionLocalOf<String?> { null }
val LocalPinnableCategory = staticCompositionLocalOf<CustomizeCategory?> { null }
val LocalPinnableCardTitle = staticCompositionLocalOf<String?> { null }
val LocalPinnableCardOnLongClick = staticCompositionLocalOf<(() -> Unit)?> { null }

/**
 * Dedicated Card Composable for Settings Sections in ui/customizer.
 *
 * Requirements:
 * - Container color is pure white (Color.White) in light theme (!isSystemInDarkTheme()).
 * - Container color is default surface (MaterialTheme.colorScheme.surface) in dark theme.
 * - By holding (long-pressing) any option card, a drop-down menu appears with a "Pin" (or "Unpin") button.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    shape: Shape = CustomizeCardShapes.SingleCard,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = com.example.ui.theme.getSettingsCardBackgroundColor(isSystemInDarkTheme())
    ),
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    feature: FavoriteFeature? = null,
    featureId: String? = feature?.id,
    featureTitle: String? = feature?.title,
    canPin: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showDropdown by remember { mutableStateOf(false) }

    val localCardId = LocalPinnableCardId.current
    val localLongClick = LocalPinnableCardOnLongClick.current

    val resolvedId = featureId ?: localCardId ?: feature?.id ?: featureTitle?.let { title ->
        FavoriteFeature.entries.firstOrNull { it.title.equals(title, ignoreCase = true) }?.id
            ?: ("feature_" + title.lowercase().replace("[^a-z0-9]+".toRegex(), "_").trim('_'))
    } ?: rememberSaveable { "option_" + java.util.UUID.randomUUID().toString().take(8) }

    val effectiveFeatureId = resolvedId

    val pinnedIds by PanelPreferences.pinnedFeatures.collectAsState()
    val isPinned = pinnedIds.contains(effectiveFeatureId)

    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onClick?.invoke() },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick?.invoke()
                        if (localLongClick != null) {
                            localLongClick.invoke()
                        } else if (canPin) {
                            showDropdown = true
                        }
                    }
                ),
            shape = shape,
            colors = colors,
            content = content
        )



        if (canPin) {
            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false },
                modifier = Modifier.testTag("dropdown_menu_$effectiveFeatureId")
            ) {
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isPinned) "Unpin" else "Pin",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            if (isPinned) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "PINNED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = if (isPinned) "Unpin" else "Pin",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = {
                        showDropdown = false
                        val isNowPinned = PanelPreferences.toggleFeaturePinned(context, effectiveFeatureId)
                        val title = featureTitle ?: FavoriteFeature.fromId(effectiveFeatureId)?.title ?: "Option"
                        val msg = if (isNowPinned) "$title moved to top (Pinned)" else "$title unpinned"
                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("dropdown_pin_button")
                )
            }
        }
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PinnableCardWrapper(
    cardId: String,
    category: CustomizeCategory,
    cardTitle: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showDropdown by remember { mutableStateOf(false) }

    val pinnedIds by PinnedCardsManager.getPinnedCardIds(category, context).collectAsState()
    val isPinned = pinnedIds.contains(cardId)

    val triggerLongPress: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        showDropdown = true
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(cardId) {
                detectTapGestures(
                    onLongPress = {
                        triggerLongPress()
                    }
                )
            }
    ) {
        CompositionLocalProvider(
            LocalPinnableCardId provides cardId,
            LocalPinnableCategory provides category,
            LocalPinnableCardTitle provides cardTitle,
            LocalPinnableCardOnLongClick provides triggerLongPress
        ) {
            content()
        }

        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false },
            modifier = Modifier.testTag("dropdown_menu_$cardId")
        ) {
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isPinned) "Unpin" else "Pin",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        if (isPinned) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "PINNED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = if (isPinned) "Unpin" else "Pin",
                        tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    showDropdown = false
                    val isNowPinned = PinnedCardsManager.togglePin(cardId, category, context)
                    val displayTitle = cardTitle ?: cardId.replace("qs_", "").replace("_card", "")
                        .replace("_", " ").split(" ")
                        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                    val msg = if (isNowPinned) "$displayTitle moved to top (Pinned)" else "$displayTitle unpinned"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.testTag("dropdown_pin_button")
            )
        }
    }
}

