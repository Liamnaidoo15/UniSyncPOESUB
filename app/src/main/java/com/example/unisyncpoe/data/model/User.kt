package com.example.unisyncpoe.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName

/**
 * User data model representing students, lecturers, and administrators
 * Supports offline storage via RoomDB and Firestore
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @SerializedName("id")
    @PropertyName("id")
    val id: String,
    
    @SerializedName("email")
    @PropertyName("email")
    val email: String,
    
    @SerializedName("name")
    @PropertyName("name")
    val name: String,
    
    @SerializedName("role")
    @PropertyName("role")
    val role: UserRole,
    
    @SerializedName("studentId")
    @PropertyName("studentId")
    val studentId: String? = null,
    
    @SerializedName("lecturerId")
    @PropertyName("lecturerId")
    val lecturerId: String? = null,
    
    @SerializedName("coordinatorId")
    @PropertyName("coordinatorId")
    val coordinatorId: String? = null,
    
    @SerializedName("profileImageUrl")
    @PropertyName("profileImageUrl")
    val profileImageUrl: String? = null,
    
    @SerializedName("createdAt")
    @PropertyName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    
    @SerializedName("lastSyncTime")
    @PropertyName("lastSyncTime")
    val lastSyncTime: Long = System.currentTimeMillis(),
    
    // Offline sync flag
    @SerializedName("isSynced")
    @PropertyName("isSynced")
    val isSynced: Boolean = true
)

enum class UserRole {
    STUDENT,
    LECTURER,
    PROGRAM_COORDINATOR,
    ADMIN
}

