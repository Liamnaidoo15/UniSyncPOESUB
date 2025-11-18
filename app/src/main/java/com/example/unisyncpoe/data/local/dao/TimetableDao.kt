package com.example.unisyncpoe.data.local.dao

import androidx.room.*
import com.example.unisyncpoe.data.model.Timetable
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetables ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllTimetables(): Flow<List<Timetable>>
    
    @Query("SELECT * FROM timetables WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getTimetablesByDay(dayOfWeek: Int): Flow<List<Timetable>>
    
    @Query("SELECT * FROM timetables WHERE id = :id")
    suspend fun getTimetableById(id: String): Timetable?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetable(timetable: Timetable)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetables(timetables: List<Timetable>)
    
    @Update
    suspend fun updateTimetable(timetable: Timetable)
    
    @Delete
    suspend fun deleteTimetable(timetable: Timetable)
    
    @Query("SELECT * FROM timetables WHERE isSynced = 0")
    suspend fun getUnsyncedTimetables(): List<Timetable>
    
    @Query("UPDATE timetables SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}

