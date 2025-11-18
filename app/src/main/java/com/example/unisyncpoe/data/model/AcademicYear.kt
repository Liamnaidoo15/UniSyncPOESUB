package com.example.unisyncpoe.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Academic Year model
 */
@Entity(tableName = "academic_years")
data class AcademicYear(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    
    @SerializedName("year")
    val year: String, // e.g., "2024"
    
    @SerializedName("startDate")
    val startDate: Long,
    
    @SerializedName("endDate")
    val endDate: Long,
    
    @SerializedName("isActive")
    val isActive: Boolean = false,
    
    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    
    @SerializedName("isSynced")
    val isSynced: Boolean = true
)

