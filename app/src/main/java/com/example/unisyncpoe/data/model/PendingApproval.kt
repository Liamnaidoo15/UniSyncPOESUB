package com.example.unisyncpoe.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Pending Approval model for content awaiting coordinator approval
 */
@Entity(tableName = "pending_approvals")
data class PendingApproval(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("type")
    val type: ApprovalType,
    
    @SerializedName("uploadedBy")
    val uploadedBy: String, // User ID
    
    @SerializedName("uploadedByName")
    val uploadedByName: String,
    
    @SerializedName("moduleId")
    val moduleId: String? = null,
    
    @SerializedName("moduleCode")
    val moduleCode: String? = null,
    
    @SerializedName("fileUrl")
    val fileUrl: String? = null,
    
    @SerializedName("uploadedAt")
    val uploadedAt: Long = System.currentTimeMillis(),
    
    @SerializedName("status")
    val status: ApprovalStatus = ApprovalStatus.PENDING,
    
    @SerializedName("reviewedBy")
    val reviewedBy: String? = null, // Coordinator ID
    
    @SerializedName("reviewedAt")
    val reviewedAt: Long? = null,
    
    @SerializedName("reviewComments")
    val reviewComments: String? = null,
    
    @SerializedName("isSynced")
    val isSynced: Boolean = true
)

enum class ApprovalType {
    ANNOUNCEMENT,
    RESOURCE,
    MODULE_CONTENT,
    ASSESSMENT
}

enum class ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED
}

