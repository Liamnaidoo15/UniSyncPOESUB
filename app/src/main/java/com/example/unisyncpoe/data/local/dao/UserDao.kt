package com.example.unisyncpoe.data.local.dao

import androidx.room.*
import com.example.unisyncpoe.data.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for User entity
 */
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?
    
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?
    
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserByIdFlow(userId: String): Flow<User?>
    
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Update
    suspend fun updateUser(user: User)
    
    @Delete
    suspend fun deleteUser(user: User)
    
    @Query("SELECT * FROM users WHERE isSynced = 0")
    suspend fun getUnsyncedUsers(): List<User>
    
    @Query("UPDATE users SET isSynced = 1 WHERE id = :userId")
    suspend fun markAsSynced(userId: String)
    
    // Statistics queries
    @Query("SELECT COUNT(*) FROM users WHERE role = 'STUDENT'")
    suspend fun getStudentCount(): Int
    
    @Query("SELECT COUNT(*) FROM users WHERE role = 'LECTURER'")
    suspend fun getLecturerCount(): Int
    
    @Query("SELECT COUNT(*) FROM users WHERE role = 'ADMIN'")
    suspend fun getAdminCount(): Int
    
    @Query("SELECT COUNT(*) FROM users WHERE role = 'PROGRAM_COORDINATOR'")
    suspend fun getCoordinatorCount(): Int
    
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getTotalUserCount(): Int
}

