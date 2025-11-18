package com.example.unisyncpoe.data.local.dao

import androidx.room.*
import com.example.unisyncpoe.data.model.Assignment
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM assignments ORDER BY dueDate ASC")
    fun getAllAssignments(): Flow<List<Assignment>>
    
    @Query("SELECT * FROM assignments WHERE courseId = :courseId ORDER BY dueDate ASC")
    fun getAssignmentsByCourse(courseId: String): Flow<List<Assignment>>
    
    @Query("SELECT * FROM assignments WHERE id = :id")
    suspend fun getAssignmentById(id: String): Assignment?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: Assignment)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<Assignment>)
    
    @Update
    suspend fun updateAssignment(assignment: Assignment)
    
    @Delete
    suspend fun deleteAssignment(assignment: Assignment)
    
    @Query("SELECT * FROM assignments WHERE isSynced = 0")
    suspend fun getUnsyncedAssignments(): List<Assignment>
    
    @Query("UPDATE assignments SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
    
    // Statistics queries
    @Query("SELECT COUNT(*) FROM assignments WHERE submissionStatus IN ('SUBMITTED', 'GRADED')")
    suspend fun getSubmittedCount(): Int
    
    @Query("SELECT COUNT(*) FROM assignments")
    suspend fun getTotalAssignmentsCount(): Int
    
    @Query("SELECT * FROM assignments ORDER BY createdAt DESC LIMIT 100")
    suspend fun getRecentAssignments(): List<Assignment>
    
    @Query("SELECT * FROM assignments")
    suspend fun getAllAssignmentsList(): List<Assignment>
}

