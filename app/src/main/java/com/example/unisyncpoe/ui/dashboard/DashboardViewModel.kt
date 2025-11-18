package com.example.unisyncpoe.ui.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unisyncpoe.data.model.Announcement
import com.example.unisyncpoe.data.model.Assignment
import com.example.unisyncpoe.data.model.Attendance
import com.example.unisyncpoe.data.repository.AnnouncementRepository
import com.example.unisyncpoe.data.repository.AssignmentRepository
import com.example.unisyncpoe.data.repository.AttendanceRepository
import com.example.unisyncpoe.data.repository.SyncRepository
import com.example.unisyncpoe.util.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val announcementRepository: AnnouncementRepository,
    private val assignmentRepository: AssignmentRepository,
    private val attendanceRepository: AttendanceRepository,
    private val syncRepository: SyncRepository,
    private val authManager: AuthManager
) : ViewModel() {
    
    companion object {
        private const val TAG = "DashboardViewModel"
    }
    
    val announcements = announcementRepository.getAnnouncements()
    val assignments = assignmentRepository.getAssignments()
    
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            try {
                // Sync data from API
                announcementRepository.syncAnnouncements()
                assignmentRepository.syncAssignments()
                
                val userId = authManager.getUserId()
                if (userId != null) {
                    attendanceRepository.syncAttendance()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading data", e)
            }
        }
    }
    
    fun syncData() {
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing
            try {
                announcementRepository.syncAnnouncements()
                assignmentRepository.syncAssignments()
                
                val userId = authManager.getUserId()
                if (userId != null) {
                    attendanceRepository.syncAttendance()
                }
                
                // Sync pending offline operations
                val syncedCount = syncRepository.syncPendingOperations()
                _syncState.value = SyncState.Success(syncedCount)
            } catch (e: Exception) {
                Log.e(TAG, "Sync error", e)
                _syncState.value = SyncState.Error(e.message ?: "Sync failed")
            }
        }
    }
    
    fun getAttendanceByStudent(studentId: String) = attendanceRepository.getAttendanceByStudent(studentId)
}

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(val syncedCount: Int) : SyncState()
    data class Error(val message: String) : SyncState()
}

