package com.example.unisyncpoe.data.repository

import android.util.Log
import com.example.unisyncpoe.data.local.dao.SyncQueueDao
import com.example.unisyncpoe.data.model.SyncQueue
import com.example.unisyncpoe.data.remote.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling offline sync operations
 */
@Singleton
class SyncRepository @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
    private val apiService: ApiService,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "SyncRepository"
        private const val MAX_RETRY_COUNT = 3
    }
    
    /**
     * Get all pending sync operations
     */
    suspend fun getPendingSyncs(): List<SyncQueue> {
        return syncQueueDao.getPendingSyncs()
    }
    
    /**
     * Get all sync operations as Flow
     */
    fun getAllSyncs(): Flow<List<SyncQueue>> {
        return syncQueueDao.getAllSyncs()
    }
    
    /**
     * Process pending sync operations
     */
    suspend fun syncPendingOperations(): Int {
        val pendingSyncs = getPendingSyncs()
        var successCount = 0
        
        for (sync in pendingSyncs) {
            if (sync.retryCount >= MAX_RETRY_COUNT) {
                // Skip if max retries reached
                Log.w(TAG, "Skipping sync ${sync.id} - max retries reached")
                continue
            }
            
            try {
                when (sync.operation) {
                    com.example.unisyncpoe.data.model.SyncOperation.CREATE -> {
                        // Parse entity and create via API
                        when (sync.entityType) {
                            "Announcement" -> {
                                val announcement = gson.fromJson(sync.entityData, com.example.unisyncpoe.data.model.Announcement::class.java)
                                val response = apiService.createAnnouncement(announcement)
                                if (response.isSuccessful && response.body()?.success == true) {
                                    syncQueueDao.deleteSyncById(sync.id)
                                    successCount++
                                } else {
                                    syncQueueDao.incrementRetryCount(sync.id)
                                }
                            }
                            "Attendance" -> {
                                val attendance = gson.fromJson(sync.entityData, com.example.unisyncpoe.data.model.Attendance::class.java)
                                val response = apiService.markAttendance(attendance)
                                if (response.isSuccessful && response.body()?.success == true) {
                                    syncQueueDao.deleteSyncById(sync.id)
                                    successCount++
                                } else {
                                    syncQueueDao.incrementRetryCount(sync.id)
                                }
                            }
                            // Add more entity types as needed
                        }
                    }
                    com.example.unisyncpoe.data.model.SyncOperation.UPDATE -> {
                        // Handle update operations
                        syncQueueDao.incrementRetryCount(sync.id)
                    }
                    com.example.unisyncpoe.data.model.SyncOperation.DELETE -> {
                        // Handle delete operations
                        syncQueueDao.incrementRetryCount(sync.id)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing operation ${sync.id}", e)
                syncQueueDao.incrementRetryCount(sync.id)
            }
        }
        
        Log.d(TAG, "Synced $successCount out of ${pendingSyncs.size} operations")
        return successCount
    }
}

