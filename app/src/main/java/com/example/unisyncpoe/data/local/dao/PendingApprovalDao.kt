package com.example.unisyncpoe.data.local.dao

import androidx.room.*
import com.example.unisyncpoe.data.model.ApprovalStatus
import com.example.unisyncpoe.data.model.PendingApproval
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingApprovalDao {
    @Query("SELECT * FROM pending_approvals WHERE status = 'PENDING' ORDER BY uploadedAt DESC")
    fun getPendingApprovals(): Flow<List<PendingApproval>>
    
    @Query("SELECT * FROM pending_approvals WHERE status = 'APPROVED' AND type = 'RESOURCE' ORDER BY uploadedAt DESC")
    suspend fun getApprovedResources(): List<PendingApproval>
    
    @Query("SELECT * FROM pending_approvals ORDER BY uploadedAt DESC")
    fun getAllApprovals(): Flow<List<PendingApproval>>
    
    @Query("SELECT * FROM pending_approvals WHERE id = :id")
    suspend fun getApprovalById(id: String): PendingApproval?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApproval(approval: PendingApproval)
    
    @Update
    suspend fun updateApproval(approval: PendingApproval)
    
    @Delete
    suspend fun deleteApproval(approval: PendingApproval)
}

