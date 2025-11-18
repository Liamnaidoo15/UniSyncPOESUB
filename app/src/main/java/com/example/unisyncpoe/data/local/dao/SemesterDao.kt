package com.example.unisyncpoe.data.local.dao

import androidx.room.*
import com.example.unisyncpoe.data.model.Semester
import kotlinx.coroutines.flow.Flow

@Dao
interface SemesterDao {
    @Query("SELECT * FROM semesters WHERE academicYearId = :academicYearId ORDER BY startDate ASC")
    fun getSemestersByAcademicYear(academicYearId: String): Flow<List<Semester>>
    
    @Query("SELECT * FROM semesters ORDER BY startDate DESC")
    fun getAllSemesters(): Flow<List<Semester>>
    
    @Query("SELECT * FROM semesters WHERE id = :id")
    suspend fun getSemesterById(id: String): Semester?
    
    @Query("SELECT * FROM semesters WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveSemester(): Semester?
    
    @Query("SELECT * FROM semesters WHERE isActive = 1")
    suspend fun getAllActiveSemesters(): List<Semester>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSemester(semester: Semester)
    
    @Update
    suspend fun updateSemester(semester: Semester)
    
    @Delete
    suspend fun deleteSemester(semester: Semester)
}

