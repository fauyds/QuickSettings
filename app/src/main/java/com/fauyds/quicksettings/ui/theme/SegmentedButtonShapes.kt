package com.fauyds.quicksettings.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Segmented Button Shapes for selector button rows.
 *
 * Requirements:
 * - Selected buttons: round corners (CircleShape / pill shape).
 * - Non-selected buttons: 8dp corners for both outer and inner corners.
 */
object SegmentedButtonShapes {
    val UnselectedCorner: Dp = 8.dp

    fun getShape(
        index: Int,
        count: Int,
        isSelected: Boolean,
        hasUnselectedNeighborLeft: Boolean = false,
        hasUnselectedNeighborRight: Boolean = false
    ): Shape {
        if (isSelected || count <= 1) {
            return CircleShape
        }

        return RoundedCornerShape(UnselectedCorner)
    }

    fun <T> getShape(
        item: T,
        items: List<T>,
        selectedItem: T?
    ): Shape {
        val isSelected = (item == selectedItem)
        if (isSelected || items.size <= 1) return CircleShape

        return RoundedCornerShape(UnselectedCorner)
    }
}
