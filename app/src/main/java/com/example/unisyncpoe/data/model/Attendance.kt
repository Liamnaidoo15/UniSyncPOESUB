package com.example.unisyncpoe.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Attendance model for tracking student attendance
 */
@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    
    @SerializedName("studentId")
    val studentId: String,
    
    @SerializedName("studentName")
    val studentName: String,
    
    @SerializedName("courseId")
    val courseId: String,
    
    @SerializedName("courseName")
    val courseName: String,
    
    @SerializedName("lecturerId")
    val lecturerId: String,
    
    @SerializedName("classDate")
    val classDate: Long,
    
    @SerializedName("markedAt")
    val markedAt: Long = System.currentTimeMillis(),
    
    @SerializedName("status")
    val status: AttendanceStatus,
    
    @SerializedName("qrCodeId")
    val qrCodeId: String? = null,
    
    @SerializedName("latitude")
    val latitude: Double? = null,
    
    @SerializedName("longitude")
    val longitude: Double? = null,
    
    @SerializedName("isSynced")
    val isSynced: Boolean = true
)

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    EXCUSED
}

