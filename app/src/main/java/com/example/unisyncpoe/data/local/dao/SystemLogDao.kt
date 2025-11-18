package com.example.unisyncpoe.data.local.dao

import androidx.room.*
import com.example.unisyncpoe.data.model.SystemLog
import kotlinx.coroutines.flow.Flow

@Dao
interface SystemLogDao {
    @Query("SELECT * FROM system_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<SystemLog>>
    
    @Query("SELECT * FROM system_logs WHERE logType = :logType ORDER BY timestamp DESC")
    fun getLogsByType(logType: String): Flow<List<SystemLog>>
    
    @Query("SELECT * FROM system_logs WHERE id = :id")
    suspend fun getLogById(id: String): SystemLog?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SystemLog)
    
    @Query("DELETE FROM system_logs WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOldLogs(cutoffTimestamp: Long)
}

