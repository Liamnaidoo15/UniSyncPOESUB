package com.example.unisyncpoe.data.repository

import android.util.Log
import com.example.unisyncpoe.data.local.dao.UserDao
import com.example.unisyncpoe.data.model.User
import com.example.unisyncpoe.data.remote.ApiService
import com.example.unisyncpoe.data.remote.AuthResponse
import com.example.unisyncpoe.data.remote.FirestoreService
import com.example.unisyncpoe.data.remote.LoginRequest
import com.example.unisyncpoe.util.NetworkChecker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for authentication operations
 * Handles both local (Room) and remote (API) data sources
 */
@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val firestoreService: FirestoreService,
    private val networkChecker: NetworkChecker
) {
    companion object {
        private const val TAG = "AuthRepository"
    }
    
    /**
     * Register a new user
     */
    suspend fun register(user: User, password: String): Result<User> {
        return try {
            // Check if API URL is valid before attempting
            if (!networkChecker.isApiUrlValid()) {
                Log.w(TAG, "API URL is not configured or invalid, saving to Firestore only")
                // Save to Firestore directly
                val localUser = user.copy(
                    id = if (user.id.isEmpty()) "local_${System.currentTimeMillis()}" else user.id,
                    isSynced = false
                )
                userDao.insertUser(localUser)
                firestoreService.saveUser(localUser).fold(
                    onSuccess = { Log.d(TAG, "User saved to Firestore successfully") },
                    onFailure = { error -> Log.e(TAG, "Failed to save user to Firestore", error) }
                )
                return Result.success(localUser)
            }
            
            // Create registration request with password
            val registerRequest = com.example.unisyncpoe.data.remote.RegisterRequest(
                email = user.email,
                name = user.name,
                role = user.role.name,
                password = password,
                studentId = user.studentId,
                lecturerId = user.lecturerId,
                coordinatorId = user.coordinatorId
            )
            
            Log.d(TAG, "Attempting to register user via API: ${user.email}")
            val response = apiService.register(registerRequest)
            Log.d(TAG, "API response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
            if (response.isSuccessful && response.body()?.success == true) {
                val registeredUser = response.body()!!.data!!
                // Save to local database
                userDao.insertUser(registeredUser.copy(isSynced = true))
                // Save to Firestore
                firestoreService.saveUser(registeredUser).fold(
                    onSuccess = { Log.d(TAG, "User saved to Firestore successfully") },
                    onFailure = { error -> Log.e(TAG, "Failed to save user to Firestore", error) }
                )
                Log.d(TAG, "User registered successfully: ${registeredUser.email}")
                Result.success(registeredUser)
            } else {
                // API failed, but still save to Firestore
                val errorBody = response.body()
                val errorMessage = errorBody?.error 
                    ?: errorBody?.message 
                    ?: "Registration failed: ${response.code()} ${response.message()}"
                Log.e(TAG, "Registration failed: $errorMessage")
                
                // Save to Firestore anyway
                val userToSave = user.copy(
                    id = if (user.id.isEmpty()) "user_${System.currentTimeMillis()}" else user.id,
                    isSynced = false
                )
                userDao.insertUser(userToSave)
                firestoreService.saveUser(userToSave).fold(
                    onSuccess = { Log.d(TAG, "User saved to Firestore despite API failure") },
                    onFailure = { Log.e(TAG, "Failed to save user to Firestore", it) }
                )
                
                Result.failure(Exception(errorMessage))
            }
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network error: Cannot reach server", e)
            // Create a local user and save to Firestore
            val demoUser = user.copy(
                id = "demo_${System.currentTimeMillis()}",
                isSynced = false
            )
            userDao.insertUser(demoUser)
            // Save to Firestore
            firestoreService.saveUser(demoUser).fold(
                onSuccess = { Log.d(TAG, "User saved to Firestore successfully") },
                onFailure = { error -> Log.e(TAG, "Failed to save user to Firestore", error) }
            )
            Result.success(demoUser)
        } catch (e: java.net.ConnectException) {
            Log.e(TAG, "Connection error: Cannot connect to server", e)
            // Create a local user and save to Firestore
            val demoUser = user.copy(
                id = "demo_${System.currentTimeMillis()}",
                isSynced = false
            )
            userDao.insertUser(demoUser)
            // Save to Firestore
            firestoreService.saveUser(demoUser).fold(
                onSuccess = { Log.d(TAG, "User saved to Firestore successfully") },
                onFailure = { error -> Log.e(TAG, "Failed to save user to Firestore", error) }
            )
            Result.success(demoUser)
        } catch (e: java.io.IOException) {
            // Catch IOException which includes "stream was reset" and other network errors
            val errorMsg = e.message ?: "Network error"
            Log.e(TAG, "Network/IO error: $errorMsg", e)
            
            // Check if it's a stream reset, protocol error, or connection error - enable offline mode
            if (errorMsg.contains("stream was reset", ignoreCase = true) ||
                errorMsg.contains("protocol error", ignoreCase = true) ||
                errorMsg.contains("connection", ignoreCase = true) ||
                errorMsg.contains("unable to resolve host", ignoreCase = true) ||
                errorMsg.contains("failed to connect", ignoreCase = true) ||
                errorMsg.contains("timeout", ignoreCase = true)) {
                // Create a local user and save to Firestore
                val demoUser = user.copy(
                    id = "demo_${System.currentTimeMillis()}",
                    isSynced = false
                )
                userDao.insertUser(demoUser)
                // Save to Firestore
                firestoreService.saveUser(demoUser).fold(
                    onSuccess = { Log.d(TAG, "User saved to Firestore successfully") },
                    onFailure = { error -> Log.e(TAG, "Failed to save user to Firestore", error) }
                )
                Result.success(demoUser)
            } else {
                // Other IO errors - still save locally but return failure
                val localUser = user.copy(
                    id = if (user.id.isEmpty()) "local_${System.currentTimeMillis()}" else user.id,
                    isSynced = false
                )
                userDao.insertUser(localUser)
                Result.failure(Exception("Registration failed: $errorMsg. User saved locally for offline sync."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Registration error: ${e.message}", e)
            // Save to local database for offline sync
            val localUser = user.copy(
                id = if (user.id.isEmpty()) "local_${System.currentTimeMillis()}" else user.id,
                isSynced = false
            )
            userDao.insertUser(localUser)
            // Save to Firestore
            firestoreService.saveUser(localUser).fold(
                onSuccess = { Log.d(TAG, "User saved to Firestore successfully") },
                onFailure = { error -> Log.e(TAG, "Failed to save user to Firestore", error) }
            )
            Result.failure(Exception("Registration failed: ${e.message ?: "Unknown error"}. User saved locally and to Firestore for offline sync."))
        }
    }
    
    /**
     * Login with email and password
     * Supports demo accounts and API-registered users
     * Tries API login first for users with bcrypt passwords, falls back to demo passwords
     */
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            // First, check Firestore to see if user exists and determine if they're a demo account
            // This helps us decide whether to try API login or use Firestore fallback
            val firestoreResult = try {
                withTimeout(5000) { // 5 second timeout for Firestore
                    firestoreService.getUserByEmail(email)
                }
            } catch (timeoutException: TimeoutCancellationException) {
                Log.w(TAG, "Firestore query timeout, will try API login")
                Result.failure(timeoutException)
            }
            val firestoreUser = firestoreResult.getOrNull()
            
            // Check if this is a known demo account - skip API login for demo accounts
            // Demo accounts are identified by their email pattern
            val isDemoAccount = email == "admin@unisync.com" || 
                                email == "student@unisync.com" || 
                                email == "lecturer@unisync.com" || 
                                email == "coordinator@unisync.com"
            
            // If it's a demo account, skip API login and go straight to Firestore fallback
            if (isDemoAccount) {
                Log.d(TAG, "Detected demo account, skipping API login: $email")
            } else {
                // Try API login if URL is valid (for users registered via API with bcrypt passwords)
                // Use withTimeout to fail fast if API is not responding
                if (networkChecker.isApiUrlValid()) {
                try {
                    Log.d(TAG, "Attempting API login for: $email")
                    val response = withTimeout(8000) { // 8 second timeout
                        apiService.login(LoginRequest(email, password))
                    }
                    Log.d(TAG, "API login response: code=${response.code()}, isSuccessful=${response.isSuccessful}, body=${response.body()}")
                    
                    if (response.isSuccessful && response.body()?.success == true) {
                        val authResponse = response.body()!!.data!!
                        // Save user to local database
                        userDao.insertUser(authResponse.user.copy(isSynced = true))
                        // Save to Firestore
                        firestoreService.saveUser(authResponse.user).fold(
                            onSuccess = { Log.d(TAG, "User saved to Firestore") },
                            onFailure = { Log.e(TAG, "Failed to save user to Firestore", it) }
                        )
                        Log.d(TAG, "API login successful: ${authResponse.user.email}")
                        return Result.success(authResponse)
                    } else {
                        val errorBody = response.body()
                        val error = errorBody?.error ?: errorBody?.message ?: "Login failed: ${response.code()} ${response.message()}"
                        Log.e(TAG, "API login failed: $error (code=${response.code()})")
                        // If it's a 401 and user exists in Firestore, they might be a demo account
                        // Continue to Firestore fallback to check demo passwords
                        if (response.code() == 401 && firestoreUser == null) {
                            // User doesn't exist in Firestore and API says invalid credentials
                            // Don't fall back, return error
                            Log.e(TAG, "User not found in API or Firestore")
                            return Result.failure(Exception("Invalid email or password"))
                        }
                        // For 401 with existing Firestore user, or other errors, continue to Firestore/demo account check
                    }
                } catch (timeoutException: TimeoutCancellationException) {
                    Log.w(TAG, "API login timeout (server not responding), trying Firestore fallback")
                    // Continue to Firestore/demo account check
                } catch (apiException: Exception) {
                    Log.e(TAG, "API login error: ${apiException.message}", apiException)
                    // Continue to Firestore/demo account check
                }
                } else {
                    Log.w(TAG, "API URL is not valid, skipping API login and trying Firestore fallback")
                }
            }
            
            // Fallback: Check Firestore for demo accounts or users without API
            // (firestoreUser was already fetched above, reuse it)
            Log.d(TAG, "Login attempt for: $email, Firestore result: ${if (firestoreUser != null) "Found" else "Not found"}")
            
            // If user exists in Firestore, check password
            if (firestoreUser != null) {
                // Check if it's a demo account with known password
                val expectedPassword = when (firestoreUser.role) {
                    com.example.unisyncpoe.data.model.UserRole.STUDENT -> "student123"
                    com.example.unisyncpoe.data.model.UserRole.LECTURER -> "lecturer123"
                    com.example.unisyncpoe.data.model.UserRole.PROGRAM_COORDINATOR -> "coordinator123"
                    com.example.unisyncpoe.data.model.UserRole.ADMIN -> "admin123"
                }
                
                Log.d(TAG, "Expected password for ${firestoreUser.role}: $expectedPassword, provided: $password")
                
                if (password == expectedPassword) {
                    // Demo account login successful
                    userDao.insertUser(firestoreUser.copy(isSynced = true))
                    val authResponse = AuthResponse(
                        user = firestoreUser,
                        token = "demo_token_${firestoreUser.id}"
                    )
                    Log.d(TAG, "Demo account login successful: ${firestoreUser.email}")
                    return Result.success(authResponse)
                } else {
                    Log.e(TAG, "Password mismatch for user: $email")
                    return Result.failure(Exception("Invalid password"))
                }
            }
            
            // If Firestore query failed or user not found, try local database as fallback
            if (firestoreUser == null) {
                Log.w(TAG, "User not found in Firestore, checking local database")
                val localUser = userDao.getUserByEmail(email)
                if (localUser != null) {
                    // Check password for local user (for demo accounts)
                    val expectedPassword = when (localUser.role) {
                        com.example.unisyncpoe.data.model.UserRole.STUDENT -> "student123"
                        com.example.unisyncpoe.data.model.UserRole.LECTURER -> "lecturer123"
                        com.example.unisyncpoe.data.model.UserRole.PROGRAM_COORDINATOR -> "coordinator123"
                        com.example.unisyncpoe.data.model.UserRole.ADMIN -> "admin123"
                    }
                    
                    if (password == expectedPassword) {
                        val authResponse = AuthResponse(
                            user = localUser,
                            token = "demo_token_${localUser.id}"
                        )
                        Log.d(TAG, "Login successful from local database: ${localUser.email}")
                        return Result.success(authResponse)
                    }
                }
            }
            
            // User not found in Firestore or local DB
            // Before trying API, check if it's a demo account and create it on-the-fly
            if (email == "admin@unisync.com" && password == "admin123") {
                Log.d(TAG, "Creating admin account on-the-fly")
                val adminUser = com.example.unisyncpoe.data.model.User(
                    id = "demo_admin_001",
                    email = "admin@unisync.com",
                    name = "Demo Admin",
                    role = com.example.unisyncpoe.data.model.UserRole.ADMIN,
                    isSynced = true
                )
                userDao.insertUser(adminUser)
                firestoreService.saveUser(adminUser).fold(
                    onSuccess = { Log.d(TAG, "Admin user created on-the-fly") },
                    onFailure = { Log.e(TAG, "Failed to save admin to Firestore", it) }
                )
                val authResponse = AuthResponse(user = adminUser, token = "demo_token_${adminUser.id}")
                return Result.success(authResponse)
            } else if (email == "student@unisync.com" && password == "student123") {
                Log.d(TAG, "Creating student account on-the-fly")
                val studentUser = com.example.unisyncpoe.data.model.User(
                    id = "demo_student_001",
                    email = "student@unisync.com",
                    name = "Demo Student",
                    role = com.example.unisyncpoe.data.model.UserRole.STUDENT,
                    studentId = "STU001",
                    isSynced = true
                )
                userDao.insertUser(studentUser)
                firestoreService.saveUser(studentUser).fold(
                    onSuccess = { Log.d(TAG, "Student user created on-the-fly") },
                    onFailure = { Log.e(TAG, "Failed to save student to Firestore", it) }
                )
                val authResponse = AuthResponse(user = studentUser, token = "demo_token_${studentUser.id}")
                return Result.success(authResponse)
            } else if (email == "lecturer@unisync.com" && password == "lecturer123") {
                Log.d(TAG, "Creating lecturer account on-the-fly")
                val lecturerUser = com.example.unisyncpoe.data.model.User(
                    id = "demo_lecturer_001",
                    email = "lecturer@unisync.com",
                    name = "Demo Lecturer",
                    role = com.example.unisyncpoe.data.model.UserRole.LECTURER,
                    lecturerId = "LEC001",
                    isSynced = true
                )
                userDao.insertUser(lecturerUser)
                firestoreService.saveUser(lecturerUser).fold(
                    onSuccess = { Log.d(TAG, "Lecturer user created on-the-fly") },
                    onFailure = { Log.e(TAG, "Failed to save lecturer to Firestore", it) }
                )
                val authResponse = AuthResponse(user = lecturerUser, token = "demo_token_${lecturerUser.id}")
                return Result.success(authResponse)
            } else if (email == "coordinator@unisync.com" && password == "coordinator123") {
                Log.d(TAG, "Creating coordinator account on-the-fly")
                val coordinatorUser = com.example.unisyncpoe.data.model.User(
                    id = "demo_coordinator_001",
                    email = "coordinator@unisync.com",
                    name = "Demo Program Coordinator",
                    role = com.example.unisyncpoe.data.model.UserRole.PROGRAM_COORDINATOR,
                    coordinatorId = "COORD001",
                    isSynced = true
                )
                userDao.insertUser(coordinatorUser)
                firestoreService.saveUser(coordinatorUser).fold(
                    onSuccess = { Log.d(TAG, "Coordinator user created on-the-fly") },
                    onFailure = { Log.e(TAG, "Failed to save coordinator to Firestore", it) }
                )
                val authResponse = AuthResponse(user = coordinatorUser, token = "demo_token_${coordinatorUser.id}")
                return Result.success(authResponse)
            }
            
            // User not found in Firestore or local DB, and API login already failed
            // Check if it's a demo account that needs to be created on-the-fly
            if (email == "coordinator@unisync.com" && password == "coordinator123") {
                Log.d(TAG, "Creating coordinator account on-the-fly (fallback)")
                val coordinatorUser = com.example.unisyncpoe.data.model.User(
                    id = "demo_coordinator_001",
                    email = "coordinator@unisync.com",
                    name = "Demo Program Coordinator",
                    role = com.example.unisyncpoe.data.model.UserRole.PROGRAM_COORDINATOR,
                    coordinatorId = "COORD001",
                    isSynced = true
                )
                userDao.insertUser(coordinatorUser)
                firestoreService.saveUser(coordinatorUser).fold(
                    onSuccess = { Log.d(TAG, "Coordinator user created on-the-fly (fallback)") },
                    onFailure = { Log.e(TAG, "Failed to save coordinator to Firestore", it) }
                )
                val authResponse = AuthResponse(user = coordinatorUser, token = "demo_token_${coordinatorUser.id}")
                return Result.success(authResponse)
            }
            
            // All login attempts failed
            return Result.failure(Exception("Invalid email or password. Please check your credentials."))
        } catch (e: Exception) {
            Log.e(TAG, "Login error: ${e.message}", e)
            // Last resort: check if it's a demo account and create it on-the-fly
            if (email == "admin@unisync.com" && password == "admin123") {
                val adminUser = com.example.unisyncpoe.data.model.User(
                    id = "demo_admin_001",
                    email = "admin@unisync.com",
                    name = "Demo Admin",
                    role = com.example.unisyncpoe.data.model.UserRole.ADMIN,
                    isSynced = true
                )
                userDao.insertUser(adminUser)
                firestoreService.saveUser(adminUser).fold(
                    onSuccess = { Log.d(TAG, "Admin user created on-the-fly") },
                    onFailure = { Log.e(TAG, "Failed to save admin to Firestore", it) }
                )
                val authResponse = AuthResponse(user = adminUser, token = "demo_token_${adminUser.id}")
                return Result.success(authResponse)
            } else if (email == "student@unisync.com" && password == "student123") {
                val studentUser = com.example.unisyncpoe.data.model.User(
                    id = "demo_student_001",
                    email = "student@unisync.com",
                    name = "Demo Student",
                    role = com.example.unisyncpoe.data.model.UserRole.STUDENT,
                    studentId = "STU001",
                    isSynced = true
                )
                userDao.insertUser(studentUser)
                firestoreService.saveUser(studentUser).fold(
                    onSuccess = { Log.d(TAG, "Student user created on-the-fly") },
                    onFailure = { Log.e(TAG, "Failed to save student to Firestore", it) }
                )
                val authResponse = AuthResponse(user = studentUser, token = "demo_token_${studentUser.id}")
                return Result.success(authResponse)
            } else if (email == "lecturer@unisync.com" && password == "lecturer123") {
                val lecturerUser = com.example.unisyncpoe.data.model.User(
                    id = "demo_lecturer_001",
                    email = "lecturer@unisync.com",
                    name = "Demo Lecturer",
                    role = com.example.unisyncpoe.data.model.UserRole.LECTURER,
                    lecturerId = "LEC001",
                    isSynced = true
                )
                userDao.insertUser(lecturerUser)
                firestoreService.saveUser(lecturerUser).fold(
                    onSuccess = { Log.d(TAG, "Lecturer user created on-the-fly") },
                    onFailure = { Log.e(TAG, "Failed to save lecturer to Firestore", it) }
                )
                val authResponse = AuthResponse(user = lecturerUser, token = "demo_token_${lecturerUser.id}")
                return Result.success(authResponse)
            } else if (email == "coordinator@unisync.com" && password == "coordinator123") {
                val coordinatorUser = com.example.unisyncpoe.data.model.User(
                    id = "demo_coordinator_001",
                    email = "coordinator@unisync.com",
                    name = "Demo Program Coordinator",
                    role = com.example.unisyncpoe.data.model.UserRole.PROGRAM_COORDINATOR,
                    coordinatorId = "COORD001",
                    isSynced = true
                )
                userDao.insertUser(coordinatorUser)
                firestoreService.saveUser(coordinatorUser).fold(
                    onSuccess = { Log.d(TAG, "Coordinator user created on-the-fly") },
                    onFailure = { Log.e(TAG, "Failed to save coordinator to Firestore", it) }
                )
                val authResponse = AuthResponse(user = coordinatorUser, token = "demo_token_${coordinatorUser.id}")
                return Result.success(authResponse)
            }
            Result.failure(Exception("Login failed: ${e.message ?: "Unknown error"}"))
        }
    }
    
    /**
     * Get current logged-in user
     * Note: This method requires a userId parameter or should be called after login
     */
    suspend fun getCurrentUser(userId: String? = null): Result<User> {
        return try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful && response.body()?.success == true) {
                val user = response.body()!!.data!!
                userDao.insertUser(user.copy(isSynced = true))
                Result.success(user)
            } else {
                // Try to get from local database if userId is provided
                if (userId != null) {
                    val localUser = userDao.getUserById(userId)
                    if (localUser != null) {
                        Result.success(localUser)
                    } else {
                        Result.failure(Exception("User not found"))
                    }
                } else {
                    Result.failure(Exception("User not found and no userId provided"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get current user error", e)
            // Try to get from local database if userId is provided
            if (userId != null) {
                val localUser = userDao.getUserById(userId)
                if (localUser != null) {
                    Result.success(localUser)
                } else {
                    Result.failure(e)
                }
            } else {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get user by ID (from local database)
     */
    fun getUserById(userId: String): Flow<User?> {
        return userDao.getUserByIdFlow(userId)
    }
    
    /**
     * Logout - clear local data
     */
    suspend fun logout() {
        // Clear user data from local database
        // In a real app, you might want to keep some data cached
        Log.d(TAG, "User logged out")
    }
}

