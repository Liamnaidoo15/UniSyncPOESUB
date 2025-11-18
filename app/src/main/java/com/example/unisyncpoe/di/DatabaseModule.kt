package com.example.unisyncpoe.di

import android.content.Context
import androidx.room.Room
import com.example.unisyncpoe.data.local.UniSyncDatabase
import com.example.unisyncpoe.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): UniSyncDatabase {
        return Room.databaseBuilder(
            context,
            UniSyncDatabase::class.java,
            "unisync_database"
        )
            .fallbackToDestructiveMigration() // For development - remove in production
            .build()
    }
    
    @Provides
    fun provideUserDao(database: UniSyncDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    fun provideAnnouncementDao(database: UniSyncDatabase): AnnouncementDao {
        return database.announcementDao()
    }
    
    @Provides
    fun provideAssignmentDao(database: UniSyncDatabase): AssignmentDao {
        return database.assignmentDao()
    }
    
    @Provides
    fun provideAttendanceDao(database: UniSyncDatabase): AttendanceDao {
        return database.attendanceDao()
    }
    
    @Provides
    fun provideTimetableDao(database: UniSyncDatabase): TimetableDao {
        return database.timetableDao()
    }
    
    @Provides
    fun provideSyncQueueDao(database: UniSyncDatabase): SyncQueueDao {
        return database.syncQueueDao()
    }
    
    @Provides
    fun provideAcademicYearDao(database: UniSyncDatabase): AcademicYearDao {
        return database.academicYearDao()
    }
    
    @Provides
    fun provideSemesterDao(database: UniSyncDatabase): SemesterDao {
        return database.semesterDao()
    }
    
    @Provides
    fun provideModuleDao(database: UniSyncDatabase): ModuleDao {
        return database.moduleDao()
    }
    
    @Provides
    fun provideSystemLogDao(database: UniSyncDatabase): SystemLogDao {
        return database.systemLogDao()
    }
    
    @Provides
    fun providePendingApprovalDao(database: UniSyncDatabase): PendingApprovalDao {
        return database.pendingApprovalDao()
    }
    
    @Provides
    fun provideMessageDao(database: UniSyncDatabase): MessageDao {
        return database.messageDao()
    }
}

