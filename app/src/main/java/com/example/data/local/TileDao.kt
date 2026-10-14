package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TileDao {
    @Query("SELECT * FROM qs_tiles ORDER BY orderIndex ASC")
    fun getAllTiles(): Flow<List<TileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tiles: List<TileEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tile: TileEntity)

    @Update
    suspend fun update(tile: TileEntity)

    @Query("UPDATE qs_tiles SET orderIndex = :newIndex WHERE tileId = :id")
    suspend fun updateOrder(id: String, newIndex: Int)

    @Query("UPDATE qs_tiles SET isVisible = :isVisible WHERE tileId = :id")
    suspend fun setVisibility(id: String, isVisible: Boolean)

    @Query("UPDATE qs_tiles SET isQqsFavorite = :isFavorite WHERE tileId = :id")
    suspend fun setQqsFavorite(id: String, isFavorite: Boolean)

    @Query("SELECT value FROM app_preferences WHERE `key` = :key")
    fun getPreference(key: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPreference(entity: AppPreferenceEntity)
}
