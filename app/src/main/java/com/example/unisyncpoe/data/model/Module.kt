package com.example.unisyncpoe.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Module model for academic modules/courses
 */
@Entity(tableName = "modules")
data class Module(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    
    @SerializedName("code")
    val code: String, // e.g., "CS101"
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("credits")
    val credits: Int = 0,
    
    @SerializedName("semesterId")
    val semesterId: String? = null,
    
    @SerializedName("coordinatorId")
    val coordinatorId: String? = null,
    
    @SerializedName("coordinatorName")
    val coordinatorName: String? = null,
    
    @SerializedName("isActive")
    val isActive: Boolean = true,
    
    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    
    @SerializedName("isSynced")
    val isSynced: Boolean = true
)

