package com.example.unisyncpoe.data.local.dao

import androidx.room.*
import com.example.unisyncpoe.data.model.Attendance
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY classDate DESC")
    fun getAttendanceByStudent(studentId: String): Flow<List<Attendance>>
    
    @Query("SELECT * FROM attendance WHERE courseId = :courseId ORDER BY classDate DESC")
    fun getAttendanceByCourse(courseId: String): Flow<List<Attendance>>
    
    @Query("SELECT * FROM attendance WHERE id = :id")
    suspend fun getAttendanceById(id: String): Attendance?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendances(attendances: List<Attendance>)
    
    @Update
    suspend fun updateAttendance(attendance: Attendance)
    
    @Delete
    suspend fun deleteAttendance(attendance: Attendance)
    
    @Query("SELECT * FROM attendance WHERE isSynced = 0")
    suspend fun getUnsyncedAttendances(): List<Attendance>
    
    @Query("UPDATE attendance SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
    
    @Query("SELECT COUNT(*) FROM attendance WHERE studentId = :studentId AND courseId = :courseId AND status = 'PRESENT'")
    suspend fun getPresentCount(studentId: String, courseId: String): Int
    
    @Query("SELECT COUNT(*) FROM attendance WHERE studentId = :studentId AND courseId = :courseId")
    suspend fun getTotalCount(studentId: String, courseId: String): Int
    
    // Overall statistics queries
    @Query("SELECT COUNT(*) FROM attendance WHERE status = 'PRESENT'")
    suspend fun getTotalPresentCount(): Int
    
    @Query("SELECT COUNT(*) FROM attendance")
    suspend fun getTotalAttendanceCount(): Int
    
    @Query("SELECT COUNT(DISTINCT studentId) FROM attendance WHERE classDate >= :sinceTimestamp")
    suspend fun getEngagedStudentsCount(sinceTimestamp: Long): Int
    
    @Query("SELECT COUNT(DISTINCT lecturerId) FROM attendance WHERE markedAt >= :sinceTimestamp")
    suspend fun getActiveLecturersCount(sinceTimestamp: Long): Int
    
    @Query("SELECT * FROM attendance ORDER BY markedAt DESC LIMIT 100")
    suspend fun getRecentAttendances(): List<Attendance>
    
    @Query("SELECT * FROM attendance")
    suspend fun getAllAttendances(): List<Attendance>
}

