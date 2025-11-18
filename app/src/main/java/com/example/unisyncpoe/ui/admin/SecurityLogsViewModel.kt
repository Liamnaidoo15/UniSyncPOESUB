package com.example.unisyncpoe.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class SecurityLogsViewModel @Inject constructor(
    private val systemLogDao: SystemLogDao,
    private val authManager: AuthManager
) : ViewModel() {
    
    private val _logs = MutableStateFlow<List<SystemLog>>(emptyList())
    val logs: StateFlow<List<SystemLog>> = _logs.asStateFlow()
    
    init {
        loadLogs()
    }
    
    fun loadLogs() {
        viewModelScope.launch {
            systemLogDao.getRecentLogs(100).collect { logsList ->
                _logs.value = logsList
            }
        }
    }
    
    fun initializeDummyLogs() {
        viewModelScope.launch {
            // Check if logs already exist
            var hasLogs = false
            systemLogDao.getRecentLogs(1).collect { logs ->
                if (!hasLogs) {
                    hasLogs = true
                    if (logs.isEmpty()) {
                        // Create dummy logs
                        val dummyLogs = listOf(
                            SystemLog(
                                id = "log_1",
                                action = "User created",
                                description = "New user student@unisync.com was created",
                                userEmail = "student@unisync.com",
                                performedBy = authManager.getUserId() ?: "admin_001",
                                performedByName = "System Admin",
                                timestamp = System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000L),
                                logType = LogType.SUCCESS
                            ),
                            SystemLog(
                                id = "log_2",
                                action = "Role updated",
                                description = "User lecturer@unisync.com role changed to LECTURER",
                                userEmail = "lecturer@unisync.com",
                                performedBy = authManager.getUserId() ?: "admin_001",
                                performedByName = "System Admin",
                                timestamp = System.currentTimeMillis() - (4 * 24 * 60 * 60 * 1000L),
                                logType = LogType.INFO
                            ),
                            SystemLog(
                                id = "log_3",
                                action = "Module added",
                                description = "Module CS101 - Introduction to Computer Science was added",
                                performedBy = authManager.getUserId() ?: "admin_001",
                                performedByName = "System Admin",
                                timestamp = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000L),
                                logType = LogType.SUCCESS
                            ),
                            SystemLog(
                                id = "log_4",
                                action = "Academic Year Added",
                                description = "Academic year 2024 was added and activated",
                                performedBy = authManager.getUserId() ?: "admin_001",
                                performedByName = "System Admin",
                                timestamp = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L),
                                logType = LogType.SUCCESS
                            ),
                            SystemLog(
                                id = "log_5",
                                action = "Semester Added",
                                description = "Semester 1 for academic year 2024 was added",
                                performedBy = authManager.getUserId() ?: "admin_001",
                                performedByName = "System Admin",
                                timestamp = System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000L),
                                logType = LogType.SUCCESS
                            ),
                            SystemLog(
                                id = "log_6",
                                action = "User deleted",
                                description = "User test@example.com was deleted from the system",
                                userEmail = "test@example.com",
                                performedBy = authManager.getUserId() ?: "admin_001",
                                performedByName = "System Admin",
                                timestamp = System.currentTimeMillis() - (12 * 60 * 60 * 1000L),
                                logType = LogType.WARNING
                            ),
                            SystemLog(
                                id = "log_7",
                                action = "Security Check",
                                description = "System security audit completed successfully",
                                performedBy = authManager.getUserId() ?: "admin_001",
                                performedByName = "System Admin",
                                timestamp = System.currentTimeMillis() - (6 * 60 * 60 * 1000L),
                                logType = LogType.INFO
                            )
                        )
                        
                        dummyLogs.forEach { log ->
                            systemLogDao.insertLog(log)
                        }
                        
                        loadLogs()
                    }
                }
            }
        }
    }
}

