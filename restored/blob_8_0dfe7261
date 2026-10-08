package com.example.data.repository

import com.example.data.local.AppPreferenceEntity
import com.example.data.local.TileDao
import com.example.data.local.TileEntity
import kotlinx.coroutines.flow.Flow

class TileRepository(private val tileDao: TileDao) {

    val allTiles: Flow<List<TileEntity>> = tileDao.getAllTiles()

    suspend fun insertAll(tiles: List<TileEntity>) = tileDao.insertAll(tiles)

    suspend fun insert(tile: TileEntity) = tileDao.insert(tile)

    suspend fun update(tile: TileEntity) = tileDao.update(tile)

    suspend fun updateOrder(id: String, newIndex: Int) = tileDao.updateOrder(id, newIndex)

    suspend fun setVisibility(id: String, isVisible: Boolean) = tileDao.setVisibility(id, isVisible)

    suspend fun setQqsFavorite(id: String, isFavorite: Boolean) = tileDao.setQqsFavorite(id, isFavorite)

    fun getPreference(key: String): Flow<String?> = tileDao.getPreference(key)

    suspend fun setPreference(key: String, value: String) {
        tileDao.setPreference(AppPreferenceEntity(key, value))
    }
}
