package com.example.ui.customizer

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

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

