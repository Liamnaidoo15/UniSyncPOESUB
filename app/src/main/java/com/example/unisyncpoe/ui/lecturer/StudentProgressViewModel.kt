package com.example.unisyncpoe.ui.lecturer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unisyncpoe.data.local.dao.AssignmentDao
import com.example.unisyncpoe.data.local.dao.AttendanceDao
import com.example.unisyncpoe.data.local.dao.ModuleDao
import com.example.unisyncpoe.data.local.dao.UserDao
import com.example.unisyncpoe.data.model.SubmissionStatus
import com.example.unisyncpoe.util.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentProgressItem(
    val studentId: String,
    val studentName: String,
    val lastSubmission: String?,
    val progressPercentage: Int
)

@HiltViewModel
class StudentProgressViewModel @Inject constructor(
    private val userDao: UserDao,
    private val moduleDao: ModuleDao,
    private val assignmentDao: AssignmentDao,
    private val attendanceDao: AttendanceDao,
    private val authManager: AuthManager
) : ViewModel() {

    private val _modules = MutableStateFlow<List<com.example.unisyncpoe.data.model.Module>>(emptyList())
    val modules: StateFlow<List<com.example.unisyncpoe.data.model.Module>> = _modules.asStateFlow()

    private val _selectedModuleId = MutableStateFlow<String?>(null)
    val selectedModuleId: StateFlow<String?> = _selectedModuleId.asStateFlow()

    private val _studentProgress = MutableStateFlow<List<StudentProgressItem>>(emptyList())
    val studentProgress: StateFlow<List<StudentProgressItem>> = _studentProgress.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadModules()
    }

    fun loadModules() {
        viewModelScope.launch {
            var modulesInitialized = false
            moduleDao.getAllModules().collect { modulesList ->
                _modules.value = modulesList
                // If no modules exist, initialize dummy modules (only once)
                if (modulesList.isEmpty() && !modulesInitialized) {
                    modulesInitialized = true
                    launch {
                        initializeDummyModules()
                    }
                }
                if (_selectedModuleId.value == null && modulesList.isNotEmpty()) {
                    _selectedModuleId.value = modulesList.first().id
                    loadProgress()
                }
            }
        }
    }

    private suspend fun initializeDummyModules() {
        val existingModule = moduleDao.getModuleById("module_dummy_1")
        if (existingModule == null) {
            val dummyModules = listOf(
                com.example.unisyncpoe.data.model.Module(
                    id = "module_dummy_1",
                    code = "CS101",
                    name = "Introduction to Computer Science",
                    credits = 12,
                    isActive = true
                ),
                com.example.unisyncpoe.data.model.Module(
                    id = "module_dummy_2",
                    code = "CS201",
                    name = "Data Structures and Algorithms",
                    credits = 15,
                    isActive = true
                ),
                com.example.unisyncpoe.data.model.Module(
                    id = "module_dummy_3",
                    code = "MATH101",
                    name = "Calculus I",
                    credits = 12,
                    isActive = true
                )
            )
            
            dummyModules.forEach { module ->
                moduleDao.insertModule(module)
            }
        }
    }

    fun selectModule(moduleId: String) {
        _selectedModuleId.value = moduleId
        loadProgress()
    }

    fun loadProgress() {
        viewModelScope.launch {
            val moduleId = _selectedModuleId.value
            if (moduleId == null) {
                _studentProgress.value = emptyList()
                return@launch
            }

            _isLoading.value = true
            try {
                // Get all students - use first() to get current value
                var usersLoaded = false
                userDao.getAllUsers().collect { users ->
                    if (!usersLoaded) {
                        usersLoaded = true
                        val students = users.filter { it.role == com.example.unisyncpoe.data.model.UserRole.STUDENT }
                    
                    // Get assignments for this module
                    val allAssignments = assignmentDao.getAllAssignmentsList()
                    val moduleAssignments = allAssignments.filter { 
                        // Extract module info from assignment - simplified for now
                        true // In real app, filter by moduleId
                    }

                    // Get attendance for this module
                    val allAttendance = attendanceDao.getAllAttendances()
                    val moduleAttendance = allAttendance.filter { it.courseId == moduleId }

                    val progressList = students.map { student ->
                        // Calculate progress based on assignments and attendance
                        val studentAssignments = moduleAssignments.filter { assignment ->
                            // Extract student ID from assignment ID
                            val parts = assignment.id.split("_")
                            parts.size >= 3 && parts[2] == student.id
                        }

                        val submittedCount = studentAssignments.count { 
                            it.submissionStatus in listOf(SubmissionStatus.SUBMITTED, SubmissionStatus.GRADED) 
                        }
                        val totalAssignments = studentAssignments.size
                        val assignmentProgress = if (totalAssignments > 0) {
                            (submittedCount * 100) / totalAssignments
                        } else {
                            0
                        }

                        // Attendance progress
                        val studentAttendance = moduleAttendance.filter { it.studentId == student.id }
                        val presentCount = studentAttendance.count { it.status == com.example.unisyncpoe.data.model.AttendanceStatus.PRESENT }
                        val totalAttendance = studentAttendance.size
                        val attendanceProgress = if (totalAttendance > 0) {
                            (presentCount * 100) / totalAttendance
                        } else {
                            0
                        }

                        // Overall progress (average of assignment and attendance)
                        val overallProgress = if (totalAssignments > 0 || totalAttendance > 0) {
                            (assignmentProgress + attendanceProgress) / 2
                        } else {
                            0
                        }

                        // Get last submission date
                        val lastSubmission = studentAssignments
                            .filter { it.submittedAt != null }
                            .maxByOrNull { it.submittedAt!! }
                            ?.submittedAt
                            ?.let { 
                                java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(it))
                            }

                        StudentProgressItem(
                            studentId = student.id,
                            studentName = student.name,
                            lastSubmission = lastSubmission ?: "No submissions",
                            progressPercentage = overallProgress
                        )
                    }

                        _studentProgress.value = progressList.sortedByDescending { it.progressPercentage }
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }
}

