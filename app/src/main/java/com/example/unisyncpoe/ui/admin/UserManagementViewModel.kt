package com.example.unisyncpoe.ui.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unisyncpoe.data.local.dao.UserDao
import com.example.unisyncpoe.data.model.User
import com.example.unisyncpoe.data.model.UserRole
import com.example.unisyncpoe.data.remote.FirestoreService
import com.example.unisyncpoe.data.repository.AuthRepository
import com.example.unisyncpoe.util.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val authManager: AuthManager,
    private val firestoreService: FirestoreService,
    private val userDao: UserDao
) : ViewModel() {
    
    companion object {
        private const val TAG = "UserManagementViewModel"
    }
    
    private val _uiState = MutableStateFlow<UserManagementUiState>(UserManagementUiState.Idle)
    val uiState: StateFlow<UserManagementUiState> = _uiState.asStateFlow()
    
    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()
    
    private val _selectedFilterRole = MutableStateFlow<UserRole?>(null)
    val selectedFilterRole: StateFlow<UserRole?> = _selectedFilterRole.asStateFlow()
    
    // Filtered users based on selected role
    val filteredUsers: StateFlow<List<User>> = combine(
        _allUsers,
        _selectedFilterRole
    ) { users: List<User>, filterRole: UserRole? ->
        if (filterRole == null) {
            users.sortedBy { it.name }
        } else {
            users.filter { it.role == filterRole }.sortedBy { it.name }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadUsers()
    }
    
    fun setFilterRole(role: UserRole?) {
        _selectedFilterRole.value = role
    }
    
    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Load users from Firestore
                firestoreService.getAllUsers().fold(
                    onSuccess = { firestoreUsers ->
                        // Also sync to local database
                        firestoreUsers.forEach { user ->
                            userDao.insertUser(user.copy(isSynced = true))
                        }
                        _allUsers.value = firestoreUsers
                        _isLoading.value = false
                        Log.d(TAG, "Loaded ${firestoreUsers.size} users from Firestore")
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error loading users from Firestore: ${error.message}")
                        // Fallback to local database - get first value from Flow
                        try {
                            val localUsers = userDao.getAllUsers().first()
                            _allUsers.value = localUsers
                        } catch (ex: Exception) {
                            Log.e(TAG, "Error loading from local database", ex)
                            _allUsers.value = emptyList()
                        }
                        _isLoading.value = false
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error loading users", e)
                // Fallback to local database
                try {
                    val localUsers = userDao.getAllUsers().first()
                    _allUsers.value = localUsers
                } catch (ex: Exception) {
                    Log.e(TAG, "Error loading from local database", ex)
                    _allUsers.value = emptyList()
                }
                _isLoading.value = false
            }
        }
    }
    
    fun registerUser(
        email: String,
        password: String,
        name: String,
        role: UserRole,
        studentId: String? = null,
        lecturerId: String? = null,
        coordinatorId: String? = null
    ) {
        viewModelScope.launch {
            // Check if current user is admin
            val currentUserRole = authManager.getUserRole()
            if (currentUserRole != UserRole.ADMIN.name) {
                _uiState.value = UserManagementUiState.Error("Only administrators can register users")
                return@launch
            }
            
            _uiState.value = UserManagementUiState.Loading
            
            val user = User(
                id = "", // Will be generated by backend or Firestore
                email = email,
                name = name,
                role = role,
                studentId = studentId,
                lecturerId = lecturerId,
                coordinatorId = coordinatorId
            )
            
            authRepository.register(user, password).fold(
                onSuccess = { registeredUser ->
                    _uiState.value = UserManagementUiState.Success(registeredUser)
                    Log.d(TAG, "User registered successfully: ${registeredUser.email}")
                    // Reload users list
                    loadUsers()
                },
                onFailure = { exception ->
                    _uiState.value = UserManagementUiState.Error(exception.message ?: "Registration failed")
                    Log.e(TAG, "Registration failed", exception)
                }
            )
        }
    }
    
    fun updateUser(user: User) {
        viewModelScope.launch {
            // Check if current user is admin
            val currentUserRole = authManager.getUserRole()
            if (currentUserRole != UserRole.ADMIN.name) {
                _uiState.value = UserManagementUiState.Error("Only administrators can update users")
                return@launch
            }
            
            _isLoading.value = true
            try {
                // Update in Firestore
                firestoreService.updateUser(user).fold(
                    onSuccess = {
                        // Update in local database
                        userDao.insertUser(user.copy(isSynced = true))
                        Log.d(TAG, "User updated successfully: ${user.email}")
                        // Reload users list
                        loadUsers()
                        _uiState.value = UserManagementUiState.UserUpdated(user)
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error updating user: ${error.message}")
                        _uiState.value = UserManagementUiState.Error("Failed to update user: ${error.message}")
                        _isLoading.value = false
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error updating user", e)
                _uiState.value = UserManagementUiState.Error("Failed to update user: ${e.message}")
                _isLoading.value = false
            }
        }
    }
    
    fun deleteUser(userId: String) {
        viewModelScope.launch {
            // Check if current user is admin
            val currentUserRole = authManager.getUserRole()
            if (currentUserRole != UserRole.ADMIN.name) {
                _uiState.value = UserManagementUiState.Error("Only administrators can delete users")
                return@launch
            }
            
            _isLoading.value = true
            try {
                // Get user to check if it's the current user
                val currentUserId = authManager.getUserId()
                if (currentUserId == userId) {
                    _uiState.value = UserManagementUiState.Error("Cannot delete your own account")
                    _isLoading.value = false
                    return@launch
                }
                
                // Delete from Firestore
                val user = _allUsers.value.find { it.id == userId }
                if (user != null) {
                    firestoreService.deleteUser(userId).fold(
                        onSuccess = {
                            // Delete from local database
                            userDao.deleteUser(user)
                            Log.d(TAG, "User deleted: ${user.email}")
                            
                            // Reload users list
                            loadUsers()
                            _uiState.value = UserManagementUiState.UserDeleted(user)
                        },
                        onFailure = { error ->
                            Log.e(TAG, "Error deleting user from Firestore: ${error.message}")
                            _uiState.value = UserManagementUiState.Error("Failed to delete user: ${error.message}")
                            _isLoading.value = false
                        }
                    )
                } else {
                    _uiState.value = UserManagementUiState.Error("User not found")
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting user", e)
                _uiState.value = UserManagementUiState.Error("Failed to delete user: ${e.message}")
                _isLoading.value = false
            }
        }
    }
}

sealed class UserManagementUiState {
    object Idle : UserManagementUiState()
    object Loading : UserManagementUiState()
    data class Success(val user: User) : UserManagementUiState()
    data class UserUpdated(val user: User) : UserManagementUiState()
    data class UserDeleted(val user: User) : UserManagementUiState()
    data class Error(val message: String) : UserManagementUiState()
}
