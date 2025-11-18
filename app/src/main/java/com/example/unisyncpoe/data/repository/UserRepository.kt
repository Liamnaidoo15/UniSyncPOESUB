package com.example.unisyncpoe.data.repository

import android.util.Log
import com.example.unisyncpoe.data.local.dao.UserDao
import com.example.unisyncpoe.data.model.User
import com.example.unisyncpoe.data.remote.FirestoreService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for user management operations
 * Handles user statistics and user data retrieval
 */
@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val firestoreService: FirestoreService
) {
    companion object {
        private const val TAG = "UserRepository"
    }
    
    /**
     * Data class for user statistics
     */
    data class UserStatistics(
        val totalUsers: Int,
        val studentCount: Int,
        val lecturerCount: Int,
        val coordinatorCount: Int,
        val adminCount: Int
    )
    
    /**
     * Get user statistics from local database and Firestore
     * Syncs Firestore users to local database for accurate statistics
     */
    suspend fun getUserStatistics(): UserStatistics {
        return try {
            // First, sync users from Firestore to local database
            syncUsersFromFirestore()
            
            // Then get statistics from local database
            val totalUsers = userDao.getTotalUserCount()
            val studentCount = userDao.getStudentCount()
            val lecturerCount = userDao.getLecturerCount()
            val coordinatorCount = userDao.getCoordinatorCount()
            val adminCount = userDao.getAdminCount()
            
            Log.d(TAG, "User statistics - Total: $totalUsers, Students: $studentCount, Lecturers: $lecturerCount, Coordinators: $coordinatorCount, Admins: $adminCount")
            
            UserStatistics(
                totalUsers = totalUsers,
                studentCount = studentCount,
                lecturerCount = lecturerCount,
                coordinatorCount = coordinatorCount,
                adminCount = adminCount
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user statistics", e)
            UserStatistics(0, 0, 0, 0, 0)
        }
    }
    
    /**
     * Sync users from Firestore to local database
     */
    private suspend fun syncUsersFromFirestore() {
        try {
            // Get all users from Firestore
            val result = firestoreService.getAllUsers()
            result.fold(
                onSuccess = { users ->
                    users.forEach { user ->
                        try {
                            // Insert or update user in local database
                            userDao.insertUser(user.copy(isSynced = true))
                            Log.d(TAG, "Synced user ${user.email} to local database")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error syncing user ${user.email} to local database", e)
                        }
                    }
                    Log.d(TAG, "Synced ${users.size} users from Firestore to local database")
                },
                onFailure = { error ->
                    Log.e(TAG, "Error getting users from Firestore for sync", error)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing users from Firestore", e)
        }
    }
    
    /**
     * Get all users as Flow
     */
    fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers()
    }
    
    /**
     * Get user by ID
     */
    suspend fun getUserById(userId: String): User? {
        return try {
            userDao.getUserById(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by ID", e)
            null
        }
    }
}

