package com.example.unisyncpoe.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * QR Code model for attendance scanning
 */
@Entity(tableName = "qr_codes")
data class QRCode(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    
    @SerializedName("courseId")
    val courseId: String,
    
    @SerializedName("lecturerId")
    val lecturerId: String,
    
    @SerializedName("classDate")
    val classDate: Long,
    
    @SerializedName("expiresAt")
    val expiresAt: Long,
    
    @SerializedName("qrData")
    val qrData: String, // JSON string containing QR code data
    
    @SerializedName("isActive")
    val isActive: Boolean = true,
    
    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis()
)

