package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TileEntity::class, AppPreferenceEntity::class], version = 1, exportSchema = false)
abstract class QuickPanelDatabase : RoomDatabase() {
    abstract fun tileDao(): TileDao

    companion object {
        @Volatile
        private var INSTANCE: QuickPanelDatabase? = null

        fun getInstance(context: Context): QuickPanelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuickPanelDatabase::class.java,
                    "quickpanel_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
