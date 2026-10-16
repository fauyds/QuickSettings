package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import com.example.data.PanelPreferences

/**
 * Material 3 Expressive Card Grouping Shapes
 * Supports dynamic global, inner, and outer corner sizes from PanelPreferences.
 * Distance between cards: 4dp
 */
object ExpressiveCardShapes {
    val CardSpacing = 4.dp
    val OuterCorner = 16.dp
    val InsideCorner = 4.dp

    val TopCard = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
    val CenterCard = RoundedCornerShape(4.dp)
    val BottomCard = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    val SingleCard = RoundedCornerShape(16.dp)
}

