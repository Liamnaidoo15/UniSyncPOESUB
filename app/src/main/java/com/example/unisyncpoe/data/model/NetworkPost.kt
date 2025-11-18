package com.example.unisyncpoe.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Network post model for discussion/networking hub
 */
@Entity(tableName = "network_posts")
data class NetworkPost(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    
    @SerializedName("authorId")
    val authorId: String,
    
    @SerializedName("authorName")
    val authorName: String,
    
    @SerializedName("authorRole")
    val authorRole: UserRole,
    
    @SerializedName("content")
    val content: String,
    
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    
    @SerializedName("likes")
    val likes: Int = 0,
    
    @SerializedName("comments")
    val comments: Int = 0,
    
    @SerializedName("isLiked")
    val isLiked: Boolean = false,
    
    @SerializedName("isSynced")
    val isSynced: Boolean = true
)

