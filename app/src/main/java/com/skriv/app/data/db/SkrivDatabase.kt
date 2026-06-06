package com.skriv.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RecentFileEntity::class], version = 1, exportSchema = false)
abstract class SkrivDatabase : RoomDatabase() {
    abstract fun recentFileDao(): RecentFileDao

    companion object {
        @Volatile private var INSTANCE: SkrivDatabase? = null
        fun getInstance(context: Context): SkrivDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext, SkrivDatabase::class.java, "skriv.db"
                ).build().also { INSTANCE = it }
            }
    }
}
