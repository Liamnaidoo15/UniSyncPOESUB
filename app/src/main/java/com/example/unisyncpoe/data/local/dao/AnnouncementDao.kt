package com.example.unisyncpoe.data.local.dao

import androidx.room.*
import com.example.unisyncpoe.data.model.Announcement
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY createdAt DESC")
    fun getAllAnnouncements(): Flow<List<Announcement>>
    
    @Query("SELECT * FROM announcements WHERE courseId = :courseId ORDER BY createdAt DESC")
    fun getAnnouncementsByCourse(courseId: String): Flow<List<Announcement>>
    
    @Query("SELECT * FROM announcements WHERE id = :id")
    suspend fun getAnnouncementById(id: String): Announcement?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: Announcement)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncements(announcements: List<Announcement>)
    
    @Update
    suspend fun updateAnnouncement(announcement: Announcement)
    
    @Delete
    suspend fun deleteAnnouncement(announcement: Announcement)
    
    @Query("SELECT * FROM announcements WHERE isSynced = 0")
    suspend fun getUnsyncedAnnouncements(): List<Announcement>
    
    @Query("UPDATE announcements SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
    
    @Query("UPDATE announcements SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)
    
    @Query("SELECT * FROM announcements ORDER BY createdAt DESC LIMIT 50")
    suspend fun getRecentAnnouncements(): List<Announcement>
    
    @Query("SELECT * FROM announcements")
    suspend fun getAllAnnouncementsList(): List<Announcement>
}

