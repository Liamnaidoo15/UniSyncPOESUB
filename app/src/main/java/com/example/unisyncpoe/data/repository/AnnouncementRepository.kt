package com.example.unisyncpoe.data.repository

import android.util.Log
import com.example.unisyncpoe.data.local.dao.AnnouncementDao
import com.example.unisyncpoe.data.local.dao.SyncQueueDao
import com.example.unisyncpoe.data.model.Announcement
import com.example.unisyncpoe.data.model.SyncOperation
import com.example.unisyncpoe.data.model.SyncQueue
import com.example.unisyncpoe.data.remote.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnnouncementRepository @Inject constructor(
    private val apiService: ApiService,
    private val announcementDao: AnnouncementDao,
    private val syncQueueDao: SyncQueueDao,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "AnnouncementRepository"
    }
    
    fun getAnnouncements(courseId: String? = null): Flow<List<Announcement>> {
        return if (courseId != null) {
            announcementDao.getAnnouncementsByCourse(courseId)
        } else {
            announcementDao.getAllAnnouncements()
        }
    }
    
    suspend fun getAnnouncementById(id: String): Announcement? {
        return announcementDao.getAnnouncementById(id)
    }
    
    suspend fun createAnnouncement(announcement: Announcement): Result<Announcement> {
        return try {
            val response = apiService.createAnnouncement(announcement)
            if (response.isSuccessful && response.body()?.success == true) {
                val created = response.body()!!.data!!
                announcementDao.insertAnnouncement(created.copy(isSynced = true))
                Log.d(TAG, "Announcement created: ${created.id}")
                Result.success(created)
            } else {
                // Save for offline sync
                val unsynced = announcement.copy(isSynced = false)
                announcementDao.insertAnnouncement(unsynced)
                syncQueueDao.insertSync(
                    SyncQueue(
                        operation = SyncOperation.CREATE,
                        entityType = "Announcement",
                        entityId = announcement.id,
                        entityData = gson.toJson(announcement)
                    )
                )
                Result.failure(Exception(response.body()?.error ?: "Failed to create announcement"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Create announcement error", e)
            // Save for offline sync
            val unsynced = announcement.copy(isSynced = false)
            announcementDao.insertAnnouncement(unsynced)
            syncQueueDao.insertSync(
                SyncQueue(
                    operation = SyncOperation.CREATE,
                    entityType = "Announcement",
                    entityId = announcement.id,
                    entityData = gson.toJson(announcement)
                )
            )
            Result.failure(e)
        }
    }
    
    suspend fun syncAnnouncements() {
        try {
            val response = apiService.getAnnouncements()
            if (response.isSuccessful && response.body()?.success == true) {
                val announcements = response.body()!!.data!!
                announcementDao.insertAnnouncements(announcements.map { it.copy(isSynced = true) })
                Log.d(TAG, "Synced ${announcements.size} announcements")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync announcements error", e)
        }
    }
    
    suspend fun markAsRead(id: String) {
        announcementDao.markAsRead(id)
    }
}

