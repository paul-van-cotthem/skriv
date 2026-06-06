package com.skriv.app.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentFileDao {
    @Query("SELECT * FROM recent_files ORDER BY lastAccessedAt DESC LIMIT 20")
    fun observeAll(): Flow<List<RecentFileEntity>>

    @Query("SELECT * FROM recent_files WHERE uri = :uri LIMIT 1")
    suspend fun getByUri(uri: String): RecentFileEntity?

    @Upsert
    suspend fun upsert(file: RecentFileEntity)

    @Query("DELETE FROM recent_files WHERE uri = :uri")
    suspend fun delete(uri: String)

    @Query("DELETE FROM recent_files")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM recent_files")
    suspend fun count(): Int

    @Query("DELETE FROM recent_files WHERE uri = (SELECT uri FROM recent_files ORDER BY lastAccessedAt ASC LIMIT 1)")
    suspend fun deleteOldest()

    @Query("UPDATE recent_files SET cursorPosition = :pos, scrollOffset = :offset WHERE uri = :uri")
    suspend fun updateScrollState(uri: String, pos: Int, offset: Int)

    @Query("UPDATE recent_files SET isAvailable = :available WHERE uri = :uri")
    suspend fun updateAvailability(uri: String, available: Boolean)
}
