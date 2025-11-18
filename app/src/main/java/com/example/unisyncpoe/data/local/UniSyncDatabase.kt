package com.example.unisyncpoe.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.unisyncpoe.data.local.dao.*
import com.example.unisyncpoe.data.model.*

/**
 * Room Database for UniSync app
 * Handles offline data storage and synchronization
 */
@Database(
    entities = [
        User::class,
        Announcement::class,
        Assignment::class,
        Attendance::class,
        Timetable::class,
        QRCode::class,
        NetworkPost::class,
        SyncQueue::class,
        AcademicYear::class,
        Semester::class,
        Module::class,
        SystemLog::class,
        PendingApproval::class,
        Message::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class UniSyncDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun timetableDao(): TimetableDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun academicYearDao(): AcademicYearDao
    abstract fun semesterDao(): SemesterDao
    abstract fun moduleDao(): ModuleDao
    abstract fun systemLogDao(): SystemLogDao
    abstract fun pendingApprovalDao(): PendingApprovalDao
    abstract fun messageDao(): MessageDao
}

