package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qs_tiles")
data class TileEntity(
    @PrimaryKey val tileId: String,
    val label: String,
    val subtitle: String,
    val categoryName: String,
    val orderIndex: Int,
    val isVisible: Boolean = true,
    val isQqsFavorite: Boolean = false,
    val isExternal: Boolean = false,
    val packageName: String? = null,
    val serviceClassName: String? = null,
    val settingsAction: String? = null
)

@Entity(tableName = "app_preferences")
data class AppPreferenceEntity(
    @PrimaryKey val key: String,
    val value: String
)
