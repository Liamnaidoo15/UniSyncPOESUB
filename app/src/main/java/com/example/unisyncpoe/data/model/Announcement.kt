package com.example.unisyncpoe.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Announcement model for course and general announcements
 */
@Entity(tableName = "announcements")
data class Announcement(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("content")
    val content: String,
    
    @SerializedName("authorId")
    val authorId: String,
    
    @SerializedName("authorName")
    val authorName: String,
    
    @SerializedName("courseId")
    val courseId: String? = null,
    
    @SerializedName("courseName")
    val courseName: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    
    @SerializedName("priority")
    val priority: AnnouncementPriority = AnnouncementPriority.NORMAL,
    
    @SerializedName("isRead")
    val isRead: Boolean = false,
    
    @SerializedName("isSynced")
    val isSynced: Boolean = true
)

enum class AnnouncementPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}

