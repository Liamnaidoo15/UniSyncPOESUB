package com.example.unisyncpoe.data.local.dao

import androidx.room.*
import com.example.unisyncpoe.data.model.AcademicYear
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademicYearDao {
    @Query("SELECT * FROM academic_years ORDER BY year DESC")
    fun getAllAcademicYears(): Flow<List<AcademicYear>>
    
    @Query("SELECT * FROM academic_years WHERE id = :id")
    suspend fun getAcademicYearById(id: String): AcademicYear?
    
    @Query("SELECT * FROM academic_years WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveAcademicYear(): AcademicYear?
    
    @Query("SELECT * FROM academic_years WHERE isActive = 1")
    suspend fun getAllActiveAcademicYears(): List<AcademicYear>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAcademicYear(academicYear: AcademicYear)
    
    @Update
    suspend fun updateAcademicYear(academicYear: AcademicYear)
    
    @Delete
    suspend fun deleteAcademicYear(academicYear: AcademicYear)
}

