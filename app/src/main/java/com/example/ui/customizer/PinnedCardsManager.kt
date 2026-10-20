package com.example.ui.customizer

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import com.example.data.PanelPreferences

/**
 * Manages the "Pin to top" state for settings cards in Customize categories.
 * Pinned card IDs persist in PanelPreferences.pinnedFeatures.
 */
object PinnedCardsManager {
    fun getPinnedCardIds(category: CustomizeCategory, context: Context? = null): StateFlow<Set<String>> {
        return PanelPreferences.pinnedFeatures
    }

    fun isPinned(category: CustomizeCategory, cardId: String): Boolean {
        return PanelPreferences.pinnedFeatures.value.contains(cardId)
    }

    fun togglePin(context: Context, category: CustomizeCategory, cardId: String) {
        PanelPreferences.toggleFeaturePinned(context, cardId)
    }

    fun init(context: Context) {
        // Handled by PanelPreferences
    }
}

/**
 * Wraps a settings card to handle long-press gestures, display a DropdownMenu with
 * Pin/Unpin options, and display a pinned indicator.
 * Strict rule compliance: Leaves the internal UI, code, and parameters of the wrapped card
 * completely untouched.
 */
@Composable
fun PinnableCardWrapper(
    cardId: String,
    category: CustomizeCategory,
    modifier: Modifier = Modifier,
    isPinned: Boolean? = null,
    onTogglePin: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val pinnedIds by PinnedCardsManager.getPinnedCardIds(category, context).collectAsState()
    val pinned = isPinned ?: pinnedIds.contains(cardId)
    val toggleAction = onTogglePin ?: { PinnedCardsManager.togglePin(context, category, cardId) }

    var showMenu by remember { mutableStateOf(false) }
    var pressOffset by remember { mutableStateOf(DpOffset.Zero) }
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(cardId, pinned) {
                detectTapGestures(
                    onLongPress = { offset ->
                        with(density) {
                            pressOffset = DpOffset(offset.x.toDp(), offset.y.toDp())
                        }
                        showMenu = true
                    }
                )
            }
    ) {
        content()

        if (pinned) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 12.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = "Pinned to top",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            offset = pressOffset,
            modifier = Modifier.testTag("pin_dropdown_menu_$cardId")
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = if (pinned) "Unpin" else "Pin to top",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (pinned) Icons.Outlined.PushPin else Icons.Default.PushPin,
                        contentDescription = null,
                        tint = if (pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = {
                    showMenu = false
                    toggleAction()
                },
                modifier = Modifier.testTag(if (pinned) "menu_item_unpin" else "menu_item_pin")
            )
        }
    }
}
