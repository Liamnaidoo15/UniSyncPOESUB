package com.example.unisyncpoe.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Semester model
 */
@Entity(tableName = "semesters")
data class Semester(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    
    @SerializedName("academicYearId")
    val academicYearId: String,
    
    @SerializedName("name")
    val name: String, // e.g., "Semester 1", "Semester 2"
    
    @SerializedName("startDate")
    val startDate: Long,
    
    @SerializedName("endDate")
    val endDate: Long,
    
    @SerializedName("examWeekStart")
    val examWeekStart: Long,
    
    @SerializedName("examWeekEnd")
    val examWeekEnd: Long,
    
    @SerializedName("isActive")
    val isActive: Boolean = false,
    
    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    
    @SerializedName("isSynced")
    val isSynced: Boolean = true
)

