package com.example.unisyncpoe.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unisyncpoe.data.model.User
import com.example.unisyncpoe.data.model.UserRole
import com.example.unisyncpoe.data.repository.UserRepository
import com.example.unisyncpoe.data.local.dao.SystemLogDao
import com.example.unisyncpoe.data.model.SystemLog
import com.example.unisyncpoe.data.model.LogType
import com.example.unisyncpoe.util.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssignRolesViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val systemLogDao: SystemLogDao,
    private val authManager: AuthManager
) : ViewModel() {
    
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()
    
    private val _uiState = MutableStateFlow<AssignRolesUiState>(AssignRolesUiState.Idle)
    val uiState: StateFlow<AssignRolesUiState> = _uiState.asStateFlow()
    
    init {
        loadUsers()
    }
    
    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = AssignRolesUiState.Loading
            try {
                userRepository.getAllUsers().collect { users ->
                    _users.value = users
                    if (_uiState.value is AssignRolesUiState.Loading) {
                        _uiState.value = AssignRolesUiState.Idle
                    }
                }
            } catch (e: Exception) {
                _uiState.value = AssignRolesUiState.Error("Failed to load users: ${e.message}")
            }
        }
    }
    
    fun selectUser(position: Int) {
        // User selection handled in UI
    }
    
    fun assignRole(userIndex: Int, newRole: UserRole) {
        viewModelScope.launch {
            if (userIndex < 0 || userIndex >= _users.value.size) {
                _uiState.value = AssignRolesUiState.Error("Invalid user selection")
                return@launch
            }
            
            _uiState.value = AssignRolesUiState.Loading
            
            val user = _users.value[userIndex]
            val oldRole = user.role
            
            if (oldRole == newRole) {
                _uiState.value = AssignRolesUiState.Error("User already has this role")
                return@launch
            }
            
            try {
                // Update user role
                val updatedUser = user.copy(role = newRole)
                // TODO: Update user in repository/database
                
                // Log the action
                val log = SystemLog(
                    id = "log_${System.currentTimeMillis()}",
                    action = "Role Updated",
                    description = "User ${user.email} role changed from ${oldRole.name} to ${newRole.name}",
                    userId = user.id,
                    userEmail = user.email,
                    performedBy = authManager.getUserId() ?: "unknown",
                    performedByName = authManager.getUserEmail() ?: "Admin",
                    logType = LogType.SUCCESS
                )
                systemLogDao.insertLog(log)
                
                _uiState.value = AssignRolesUiState.Success("Role updated successfully")
                loadUsers() // Refresh list
            } catch (e: Exception) {
                _uiState.value = AssignRolesUiState.Error("Failed to update role: ${e.message}")
            }
        }
    }
}

sealed class AssignRolesUiState {
    object Idle : AssignRolesUiState()
    object Loading : AssignRolesUiState()
    data class Success(val message: String) : AssignRolesUiState()
    data class Error(val message: String) : AssignRolesUiState()
}

