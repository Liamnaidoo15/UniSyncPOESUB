package com.example.unisyncpoe.data.local.dao

import androidx.room.*
import com.example.unisyncpoe.data.model.SyncQueue
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getPendingSyncs(limit: Int = 50): List<SyncQueue>
    
    @Query("SELECT * FROM sync_queue")
    fun getAllSyncs(): Flow<List<SyncQueue>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSync(sync: SyncQueue)
    
    @Delete
    suspend fun deleteSync(sync: SyncQueue)
    
    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteSyncById(id: Long)
    
    @Query("UPDATE sync_queue SET retryCount = retryCount + 1 WHERE id = :id")
    suspend fun incrementRetryCount(id: Long)
}

