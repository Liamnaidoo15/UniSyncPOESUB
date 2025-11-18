package com.example.unisyncpoe.util

import android.util.Log
import com.example.unisyncpoe.data.model.User
import com.example.unisyncpoe.data.model.UserRole
import com.example.unisyncpoe.data.remote.FirestoreService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Initializes demo accounts for testing
 * Creates student, lecturer, and admin accounts
 */
@Singleton
class DemoAccountsInitializer @Inject constructor(
    private val firestoreService: FirestoreService
) {
    companion object {
        private const val TAG = "DemoAccountsInitializer"
        
        // Demo account credentials
        const val DEMO_STUDENT_EMAIL = "student@unisync.com"
        const val DEMO_STUDENT_PASSWORD = "student123"
        const val DEMO_LECTURER_EMAIL = "lecturer@unisync.com"
        const val DEMO_LECTURER_PASSWORD = "lecturer123"
        const val DEMO_COORDINATOR_EMAIL = "coordinator@unisync.com"
        const val DEMO_COORDINATOR_PASSWORD = "coordinator123"
        const val DEMO_ADMIN_EMAIL = "admin@unisync.com"
        const val DEMO_ADMIN_PASSWORD = "admin123"
    }
    
    /**
     * Initialize demo accounts in Firestore
     * This should be called once on app startup
     */
    fun initializeDemoAccounts() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check if demo accounts already exist
                val studentExists = firestoreService.getUserByEmail(DEMO_STUDENT_EMAIL).getOrNull() != null
                val lecturerExists = firestoreService.getUserByEmail(DEMO_LECTURER_EMAIL).getOrNull() != null
                val coordinatorExists = firestoreService.getUserByEmail(DEMO_COORDINATOR_EMAIL).getOrNull() != null
                val adminExists = firestoreService.getUserByEmail(DEMO_ADMIN_EMAIL).getOrNull() != null
                
                if (!studentExists) {
                    val student = User(
                        id = "demo_student_001",
                        email = DEMO_STUDENT_EMAIL,
                        name = "Demo Student",
                        role = UserRole.STUDENT,
                        studentId = "STU001",
                        isSynced = true
                    )
                    firestoreService.saveUser(student).fold(
                        onSuccess = { Log.d(TAG, "Demo student account created") },
                        onFailure = { Log.e(TAG, "Failed to create demo student", it) }
                    )
                }
                
                if (!lecturerExists) {
                    val lecturer = User(
                        id = "demo_lecturer_001",
                        email = DEMO_LECTURER_EMAIL,
                        name = "Demo Lecturer",
                        role = UserRole.LECTURER,
                        lecturerId = "LEC001",
                        isSynced = true
                    )
                    firestoreService.saveUser(lecturer).fold(
                        onSuccess = { Log.d(TAG, "Demo lecturer account created") },
                        onFailure = { Log.e(TAG, "Failed to create demo lecturer", it) }
                    )
                }
                
                if (!coordinatorExists) {
                    val coordinator = User(
                        id = "demo_coordinator_001",
                        email = DEMO_COORDINATOR_EMAIL,
                        name = "Demo Program Coordinator",
                        role = UserRole.PROGRAM_COORDINATOR,
                        coordinatorId = "COORD001",
                        isSynced = true
                    )
                    firestoreService.saveUser(coordinator).fold(
                        onSuccess = { Log.d(TAG, "Demo coordinator account created") },
                        onFailure = { Log.e(TAG, "Failed to create demo coordinator", it) }
                    )
                }
                
                if (!adminExists) {
                    val admin = User(
                        id = "demo_admin_001",
                        email = DEMO_ADMIN_EMAIL,
                        name = "Demo Admin",
                        role = UserRole.ADMIN,
                        isSynced = true
                    )
                    firestoreService.saveUser(admin).fold(
                        onSuccess = { Log.d(TAG, "Demo admin account created") },
                        onFailure = { Log.e(TAG, "Failed to create demo admin", it) }
                    )
                }
                
                Log.d(TAG, "Demo accounts initialization completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing demo accounts", e)
            }
        }
    }
}

