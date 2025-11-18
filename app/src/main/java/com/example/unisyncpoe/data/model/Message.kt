package com.example.unisyncpoe.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Message model for private communication between users
 */
@Entity(tableName = "messages")
data class Message(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    
    @SerializedName("fromUserId")
    val fromUserId: String,
    
    @SerializedName("fromUserName")
    val fromUserName: String,
    
    @SerializedName("toUserId")
    val toUserId: String,
    
    @SerializedName("toUserName")
    val toUserName: String,
    
    @SerializedName("subject")
    val subject: String,
    
    @SerializedName("content")
    val content: String,
    
    @SerializedName("sentAt")
    val sentAt: Long = System.currentTimeMillis(),
    
    @SerializedName("isRead")
    val isRead: Boolean = false,
    
    @SerializedName("isSynced")
    val isSynced: Boolean = true
)

