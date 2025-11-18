package com.example.unisyncpoe.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Assignment model for course assignments
 */
@Entity(tableName = "assignments")
data class Assignment(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("courseId")
    val courseId: String,
    
    @SerializedName("courseName")
    val courseName: String,
    
    @SerializedName("lecturerId")
    val lecturerId: String,
    
    @SerializedName("lecturerName")
    val lecturerName: String,
    
    @SerializedName("dueDate")
    val dueDate: Long,
    
    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    
    @SerializedName("maxScore")
    val maxScore: Int = 100,
    
    @SerializedName("submissionStatus")
    val submissionStatus: SubmissionStatus = SubmissionStatus.NOT_SUBMITTED,
    
    @SerializedName("submittedAt")
    val submittedAt: Long? = null,
    
    @SerializedName("score")
    val score: Int? = null,
    
    @SerializedName("isSynced")
    val isSynced: Boolean = true
)

enum class SubmissionStatus {
    NOT_SUBMITTED,
    SUBMITTED,
    GRADED,
    LATE
}

