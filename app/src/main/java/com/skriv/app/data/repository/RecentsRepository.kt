package com.skriv.app.data.repository

import android.net.Uri
import com.skriv.app.data.db.RecentFileDao
import com.skriv.app.data.db.RecentFileEntity
import kotlinx.coroutines.flow.Flow

class RecentsRepository(private val dao: RecentFileDao) {

    val recentFiles: Flow<List<RecentFileEntity>> = dao.observeAll()

    suspend fun recordOpen(uri: Uri, displayName: String, lastModifiedAt: Long?) {
        val existing = dao.getByUri(uri.toString())
        if (existing == null && dao.count() >= 20) dao.deleteOldest()
        dao.upsert(
            RecentFileEntity(
                uri = uri.toString(),
                displayName = displayName,
                lastAccessedAt = System.currentTimeMillis(),
                lastModifiedAt = lastModifiedAt,
                cursorPosition = existing?.cursorPosition ?: 0,
                scrollOffset = existing?.scrollOffset ?: 0,
                isAvailable = true
            )
        )
    }

    suspend fun reconnect(oldUri: Uri, newUri: Uri, displayName: String, lastModifiedAt: Long?) {
        val existing = dao.getByUri(oldUri.toString())
        dao.delete(oldUri.toString())
        dao.upsert(
            RecentFileEntity(
                uri = newUri.toString(),
                displayName = displayName,
                lastAccessedAt = System.currentTimeMillis(),
                lastModifiedAt = lastModifiedAt,
                cursorPosition = existing?.cursorPosition ?: 0,
                scrollOffset = existing?.scrollOffset ?: 0,
                isAvailable = true
            )
        )
    }

    suspend fun getEntry(uri: Uri): RecentFileEntity? = dao.getByUri(uri.toString())

    suspend fun updateScrollState(uri: Uri, cursorPos: Int, scrollOffset: Int) =
        dao.updateScrollState(uri.toString(), cursorPos, scrollOffset)

    suspend fun markUnavailable(uri: Uri) = dao.updateAvailability(uri.toString(), false)
    suspend fun remove(uri: Uri) = dao.delete(uri.toString())
    suspend fun clearAll() = dao.deleteAll()
}
