package com.example.unisyncpoe.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Timetable model for class schedules
 */
@Entity(tableName = "timetables")
data class Timetable(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    
    @SerializedName("courseId")
    val courseId: String,
    
    @SerializedName("courseName")
    val courseName: String,
    
    @SerializedName("lecturerId")
    val lecturerId: String,
    
    @SerializedName("lecturerName")
    val lecturerName: String,
    
    @SerializedName("dayOfWeek")
    val dayOfWeek: Int, // 1-7 (Monday-Sunday)
    
    @SerializedName("startTime")
    val startTime: String, // Format: "HH:mm"
    
    @SerializedName("endTime")
    val endTime: String, // Format: "HH:mm"
    
    @SerializedName("venue")
    val venue: String,
    
    @SerializedName("roomNumber")
    val roomNumber: String? = null,
    
    @SerializedName("isSynced")
    val isSynced: Boolean = true
)

