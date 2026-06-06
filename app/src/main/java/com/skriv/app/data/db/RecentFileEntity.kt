package com.skriv.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_files")
data class RecentFileEntity(
    @PrimaryKey val uri: String,
    val displayName: String,
    val lastAccessedAt: Long,
    val lastModifiedAt: Long?,
    val cursorPosition: Int,
    val scrollOffset: Int,
    val isAvailable: Boolean = true
)
