package com.example.unisyncpoe.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unisyncpoe.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Admin Dashboard
 * Manages user statistics and admin dashboard state
 */
@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _userStatistics = MutableStateFlow<UserRepository.UserStatistics?>(
        UserRepository.UserStatistics(0, 0, 0, 0, 0)
    )
    val userStatistics: StateFlow<UserRepository.UserStatistics?> = _userStatistics.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadUserStatistics()
    }
    
    /**
     * Load user statistics from repository
     */
    fun loadUserStatistics() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val stats = userRepository.getUserStatistics()
                _userStatistics.value = stats
            } catch (e: Exception) {
                // Error already logged in repository
                _userStatistics.value = UserRepository.UserStatistics(0, 0, 0, 0, 0)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Refresh user statistics
     */
    fun refreshStatistics() {
        loadUserStatistics()
    }
}

