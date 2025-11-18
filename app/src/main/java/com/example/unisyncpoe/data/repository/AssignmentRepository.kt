package com.example.unisyncpoe.data.repository

import android.util.Log
import com.example.unisyncpoe.data.local.dao.AssignmentDao
import com.example.unisyncpoe.data.local.dao.SyncQueueDao
import com.example.unisyncpoe.data.model.Assignment
import com.example.unisyncpoe.data.model.SyncOperation
import com.example.unisyncpoe.data.model.SyncQueue
import com.example.unisyncpoe.data.remote.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssignmentRepository @Inject constructor(
    private val apiService: ApiService,
    private val assignmentDao: AssignmentDao,
    private val syncQueueDao: SyncQueueDao,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "AssignmentRepository"
    }
    
    fun getAssignments(courseId: String? = null): Flow<List<Assignment>> {
        return if (courseId != null) {
            assignmentDao.getAssignmentsByCourse(courseId)
        } else {
            assignmentDao.getAllAssignments()
        }
    }
    
    suspend fun getAssignmentById(id: String): Assignment? {
        return assignmentDao.getAssignmentById(id)
    }
    
    suspend fun createAssignment(assignment: Assignment): Result<Assignment> {
        return try {
            val response = apiService.createAssignment(assignment)
            if (response.isSuccessful && response.body()?.success == true) {
                val created = response.body()!!.data!!
                assignmentDao.insertAssignment(created.copy(isSynced = true))
                Log.d(TAG, "Assignment created: ${created.id}")
                Result.success(created)
            } else {
                // Save for offline sync
                val unsynced = assignment.copy(isSynced = false)
                assignmentDao.insertAssignment(unsynced)
                syncQueueDao.insertSync(
                    SyncQueue(
                        operation = SyncOperation.CREATE,
                        entityType = "Assignment",
                        entityId = assignment.id,
                        entityData = gson.toJson(assignment)
                    )
                )
                Result.failure(Exception(response.body()?.error ?: "Failed to create assignment"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Create assignment error", e)
            val unsynced = assignment.copy(isSynced = false)
            assignmentDao.insertAssignment(unsynced)
            syncQueueDao.insertSync(
                SyncQueue(
                    operation = SyncOperation.CREATE,
                    entityType = "Assignment",
                    entityId = assignment.id,
                    entityData = gson.toJson(assignment)
                )
            )
            Result.failure(e)
        }
    }
    
    suspend fun submitAssignment(id: String): Result<Assignment> {
        return try {
            val response = apiService.submitAssignment(id)
            if (response.isSuccessful && response.body()?.success == true) {
                val updated = response.body()!!.data!!
                assignmentDao.updateAssignment(updated.copy(isSynced = true))
                Result.success(updated)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to submit assignment"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Submit assignment error", e)
            Result.failure(e)
        }
    }
    
    suspend fun syncAssignments() {
        try {
            val response = apiService.getAssignments()
            if (response.isSuccessful && response.body()?.success == true) {
                val assignments = response.body()!!.data!!
                assignmentDao.insertAssignments(assignments.map { it.copy(isSynced = true) })
                Log.d(TAG, "Synced ${assignments.size} assignments")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync assignments error", e)
        }
    }
}

