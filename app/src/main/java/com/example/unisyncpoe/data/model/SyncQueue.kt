package com.example.unisyncpoe.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Sync queue entity for offline operations
 */
@Entity(tableName = "sync_queue")
data class SyncQueue(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val operation: SyncOperation,
    val entityType: String, // "User", "Announcement", "Attendance", etc.
    val entityId: String,
    val entityData: String, // JSON string of the entity
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
)

enum class SyncOperation {
    CREATE,
    UPDATE,
    DELETE
}

