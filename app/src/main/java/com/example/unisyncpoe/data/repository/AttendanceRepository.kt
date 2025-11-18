package com.example.unisyncpoe.data.repository

import android.util.Log
import com.example.unisyncpoe.data.local.dao.AttendanceDao
import com.example.unisyncpoe.data.local.dao.SyncQueueDao
import com.example.unisyncpoe.data.model.Attendance
import com.example.unisyncpoe.data.model.SyncOperation
import com.example.unisyncpoe.data.model.SyncQueue
import com.example.unisyncpoe.data.remote.ApiService
import com.example.unisyncpoe.data.remote.AttendanceStats
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val apiService: ApiService,
    private val attendanceDao: AttendanceDao,
    private val syncQueueDao: SyncQueueDao,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "AttendanceRepository"
    }
    
    fun getAttendanceByStudent(studentId: String): Flow<List<Attendance>> {
        return attendanceDao.getAttendanceByStudent(studentId)
    }
    
    fun getAttendanceByCourse(courseId: String): Flow<List<Attendance>> {
        return attendanceDao.getAttendanceByCourse(courseId)
    }
    
    suspend fun markAttendance(attendance: Attendance): Result<Attendance> {
        return try {
            val response = apiService.markAttendance(attendance)
            if (response.isSuccessful && response.body()?.success == true) {
                val marked = response.body()!!.data!!
                attendanceDao.insertAttendance(marked.copy(isSynced = true))
                Log.d(TAG, "Attendance marked: ${marked.id}")
                Result.success(marked)
            } else {
                // Save for offline sync
                val unsynced = attendance.copy(isSynced = false)
                attendanceDao.insertAttendance(unsynced)
                syncQueueDao.insertSync(
                    SyncQueue(
                        operation = SyncOperation.CREATE,
                        entityType = "Attendance",
                        entityId = attendance.id,
                        entityData = gson.toJson(attendance)
                    )
                )
                Result.failure(Exception(response.body()?.error ?: "Failed to mark attendance"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Mark attendance error", e)
            // Save for offline sync
            val unsynced = attendance.copy(isSynced = false)
            attendanceDao.insertAttendance(unsynced)
            syncQueueDao.insertSync(
                SyncQueue(
                    operation = SyncOperation.CREATE,
                    entityType = "Attendance",
                    entityId = attendance.id,
                    entityData = gson.toJson(attendance)
                )
            )
            Result.failure(e)
        }
    }
    
    suspend fun getAttendanceStats(studentId: String, courseId: String): Result<AttendanceStats> {
        return try {
            val response = apiService.getAttendanceStats(studentId, courseId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                // Calculate from local data
                val presentCount = attendanceDao.getPresentCount(studentId, courseId)
                val totalCount = attendanceDao.getTotalCount(studentId, courseId)
                val percentage = if (totalCount > 0) (presentCount.toDouble() / totalCount) * 100 else 0.0
                val stats = AttendanceStats(
                    totalClasses = totalCount,
                    presentCount = presentCount,
                    absentCount = totalCount - presentCount,
                    lateCount = 0,
                    attendancePercentage = percentage
                )
                Result.success(stats)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get attendance stats error", e)
            // Calculate from local data
            val presentCount = attendanceDao.getPresentCount(studentId, courseId)
            val totalCount = attendanceDao.getTotalCount(studentId, courseId)
            val percentage = if (totalCount > 0) (presentCount.toDouble() / totalCount) * 100 else 0.0
            val stats = AttendanceStats(
                totalClasses = totalCount,
                presentCount = presentCount,
                absentCount = totalCount - presentCount,
                lateCount = 0,
                attendancePercentage = percentage
            )
            Result.success(stats)
        }
    }
    
    suspend fun syncAttendance() {
        try {
            val response = apiService.getAttendance()
            if (response.isSuccessful && response.body()?.success == true) {
                val attendances = response.body()!!.data!!
                attendanceDao.insertAttendances(attendances.map { it.copy(isSynced = true) })
                Log.d(TAG, "Synced ${attendances.size} attendance records")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync attendance error", e)
        }
    }
}

