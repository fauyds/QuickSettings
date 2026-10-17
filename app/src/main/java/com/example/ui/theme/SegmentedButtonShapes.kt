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
 * - Selected buttons: round corners (CircleShape / pill shape).
 * - Non-selected buttons: 8dp corners for both outer and inner corners.
 */
object SegmentedButtonShapes {
    val InnerCorner: Dp = 5.dp
    val OuterCorner: Dp = 50.dp

    fun getShape(
        index: Int,
        count: Int,
        isSelected: Boolean,
        hasUnselectedNeighborLeft: Boolean = false,
        hasUnselectedNeighborRight: Boolean = false,
        innerCorner: Dp = InnerCorner,
        outerCorner: Dp = OuterCorner
    ): Shape {
        if (isSelected || count <= 1) {
            return CircleShape
        }

        val isFirst = (index == 0)
        val isLast = (index == count - 1)

        return when {
            isFirst -> RoundedCornerShape(
                topStart = outerCorner,
                bottomStart = outerCorner,
                topEnd = innerCorner,
                bottomEnd = innerCorner
            )
            isLast -> RoundedCornerShape(
                topStart = innerCorner,
                bottomStart = innerCorner,
                topEnd = outerCorner,
                bottomEnd = outerCorner
            )
            else -> RoundedCornerShape(innerCorner)
        }
    }

    fun <T> getShape(
        item: T,
        items: List<T>,
        selectedItem: T?,
        innerCorner: Dp = InnerCorner,
        outerCorner: Dp = OuterCorner
    ): Shape {
        val isSelected = (item == selectedItem)
        if (isSelected || items.size <= 1) return CircleShape

        val index = items.indexOf(item)
        val count = items.size
        if (index == -1) return CircleShape

        val isFirst = (index == 0)
        val isLast = (index == count - 1)

        return when {
            isFirst -> RoundedCornerShape(
                topStart = outerCorner,
                bottomStart = outerCorner,
                topEnd = innerCorner,
                bottomEnd = innerCorner
            )
            isLast -> RoundedCornerShape(
                topStart = innerCorner,
                bottomStart = innerCorner,
                topEnd = outerCorner,
                bottomEnd = outerCorner
            )
            else -> RoundedCornerShape(innerCorner)
        }
    }
}
