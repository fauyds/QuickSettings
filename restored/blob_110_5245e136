package com.example.model

enum class QsGridLayout(
    val id: String,
    val label: String,
    val columns: Int,
    val rows: Int
) {
    GRID_3X1("3x1", "3x1", 3, 1),
    GRID_3X2("3x2", "3x2", 3, 2),
    GRID_3X3("3x3", "3x3", 3, 3),
    GRID_4X1("4x1", "4x1", 4, 1),
    GRID_4X2("4x2", "4x2", 4, 2),
    GRID_4X3("4x3", "4x3", 4, 3),
    GRID_4X4("4x4", "4x4", 4, 4);

    val maxTiles: Int get() = columns * rows

    companion object {
        val DEFAULT = GRID_4X3

        fun fromId(id: String?): QsGridLayout {
            return when (id?.lowercase()?.trim()) {
                "3x1", "3_1" -> GRID_3X1
                "3x2", "3_2" -> GRID_3X2
                "3x3", "3_3", "3" -> GRID_3X3
                "4x1", "4_1", "1" -> GRID_4X1
                "4x2", "4_2", "2" -> GRID_4X2
                "4x3", "4_3" -> GRID_4X3
                "4x4", "4_4", "4" -> GRID_4X4
                else -> DEFAULT
            }
        }
    }
}
