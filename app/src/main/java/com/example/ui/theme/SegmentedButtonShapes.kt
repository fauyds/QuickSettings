package com.example.ui.theme

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
 * - Selected buttons: corners rounded (pill / stadium shape).
 * - Non-selected buttons: outer corners round, inner corners 8dp.
 */
object SegmentedButtonShapes {
    val InnerCorner: Dp = 8.dp

    fun getShape(
        index: Int,
        count: Int,
        isSelected: Boolean,
        hasUnselectedNeighborLeft: Boolean,
        hasUnselectedNeighborRight: Boolean
    ): Shape {
        if (isSelected || count <= 1) {
            return CircleShape
        }

        val startCorner = if (hasUnselectedNeighborLeft) CornerSize(InnerCorner) else CornerSize(50)
        val endCorner = if (hasUnselectedNeighborRight) CornerSize(InnerCorner) else CornerSize(50)

        return RoundedCornerShape(
            topStart = startCorner,
            topEnd = endCorner,
            bottomEnd = endCorner,
            bottomStart = startCorner
        )
    }

    fun <T> getShape(
        item: T,
        items: List<T>,
        selectedItem: T?
    ): Shape {
        val index = items.indexOf(item)
        if (index == -1 || items.size <= 1) return CircleShape
        val isSelected = (item == selectedItem)
        if (isSelected) return CircleShape

        val hasUnselectedLeft = index > 0 && items[index - 1] != selectedItem
        val hasUnselectedRight = index < items.size - 1 && items[index + 1] != selectedItem

        return getShape(
            index = index,
            count = items.size,
            isSelected = false,
            hasUnselectedNeighborLeft = hasUnselectedLeft,
            hasUnselectedNeighborRight = hasUnselectedRight
        )
    }
}
