package com.example.unisyncpoe.data.local

import androidx.room.TypeConverter
import com.example.unisyncpoe.data.model.*

/**
 * Type converters for Room database
 */
class Converters {
    @TypeConverter
    fun fromUserRole(role: UserRole): String {
        return role.name
    }
    
    @TypeConverter
    fun toUserRole(role: String): UserRole {
        return UserRole.valueOf(role)
    }
    
    @TypeConverter
    fun fromAnnouncementPriority(priority: AnnouncementPriority): String {
        return priority.name
    }
    
    @TypeConverter
    fun toAnnouncementPriority(priority: String): AnnouncementPriority {
        return AnnouncementPriority.valueOf(priority)
    }
    
    @TypeConverter
    fun fromSubmissionStatus(status: SubmissionStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toSubmissionStatus(status: String): SubmissionStatus {
        return SubmissionStatus.valueOf(status)
    }
    
    @TypeConverter
    fun fromAttendanceStatus(status: AttendanceStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toAttendanceStatus(status: String): AttendanceStatus {
        return AttendanceStatus.valueOf(status)
    }
    
    @TypeConverter
    fun fromSyncOperation(operation: SyncOperation): String {
        return operation.name
    }
    
    @TypeConverter
    fun toSyncOperation(operation: String): SyncOperation {
        return SyncOperation.valueOf(operation)
    }
    
    @TypeConverter
    fun fromLogType(logType: LogType): String {
        return logType.name
    }
    
    @TypeConverter
    fun toLogType(logType: String): LogType {
        return LogType.valueOf(logType)
    }
    
    @TypeConverter
    fun fromApprovalType(type: ApprovalType): String {
        return type.name
    }
    
    @TypeConverter
    fun toApprovalType(type: String): ApprovalType {
        return ApprovalType.valueOf(type)
    }
    
    @TypeConverter
    fun fromApprovalStatus(status: ApprovalStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toApprovalStatus(status: String): ApprovalStatus {
        return ApprovalStatus.valueOf(status)
    }
}

