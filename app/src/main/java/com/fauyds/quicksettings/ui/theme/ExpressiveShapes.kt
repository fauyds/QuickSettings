package com.fauyds.quicksettings.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive Card Grouping Shapes
 * Updated to uniform 12dp corners as requested.
 * Distance between cards: 4dp
 */
object ExpressiveCardShapes {
    val TopCard = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 5.dp, bottomEnd = 5.dp)
    val CenterCard = RoundedCornerShape(5.dp)
    val BottomCard = RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    val SingleCard = RoundedCornerShape(16.dp)

    val CardSpacing = 4.dp
}
