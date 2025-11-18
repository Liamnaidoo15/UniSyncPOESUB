package com.example.unisyncpoe.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * System Log model for admin oversight
 */
@Entity(tableName = "system_logs")
data class SystemLog(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    
    @SerializedName("action")
    val action: String, // e.g., "User created", "Role updated"
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("userId")
    val userId: String? = null,
    
    @SerializedName("userEmail")
    val userEmail: String? = null,
    
    @SerializedName("performedBy")
    val performedBy: String, // Admin user ID who performed the action
    
    @SerializedName("performedByName")
    val performedByName: String,
    
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    
    @SerializedName("logType")
    val logType: LogType = LogType.INFO,
    
    @SerializedName("isSynced")
    val isSynced: Boolean = true
)

enum class LogType {
    INFO,
    WARNING,
    ERROR,
    SUCCESS
}

