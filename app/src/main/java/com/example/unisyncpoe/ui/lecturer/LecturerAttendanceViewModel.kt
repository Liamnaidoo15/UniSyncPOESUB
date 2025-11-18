package com.example.unisyncpoe.ui.lecturer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unisyncpoe.data.local.dao.AttendanceDao
import com.example.unisyncpoe.data.local.dao.ModuleDao
import com.example.unisyncpoe.data.local.dao.UserDao
import com.example.unisyncpoe.data.model.Attendance
import com.example.unisyncpoe.data.model.AttendanceStatus
import com.example.unisyncpoe.util.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentAttendanceItem(
    val studentId: String,
    val studentName: String,
    val isPresent: Boolean = true
)

@HiltViewModel
class LecturerAttendanceViewModel @Inject constructor(
    private val userDao: UserDao,
    private val moduleDao: ModuleDao,
    private val attendanceDao: AttendanceDao,
    private val authManager: AuthManager
) : ViewModel() {

    private val _students = MutableStateFlow<List<StudentAttendanceItem>>(emptyList())
    val students: StateFlow<List<StudentAttendanceItem>> = _students.asStateFlow()

    private val _modules = MutableStateFlow<List<com.example.unisyncpoe.data.model.Module>>(emptyList())
    val modules: StateFlow<List<com.example.unisyncpoe.data.model.Module>> = _modules.asStateFlow()

    private val _selectedModuleId = MutableStateFlow<String?>(null)
    val selectedModuleId: StateFlow<String?> = _selectedModuleId.asStateFlow()

    private val _uiState = MutableStateFlow<LecturerAttendanceUiState>(LecturerAttendanceUiState.Idle)
    val uiState: StateFlow<LecturerAttendanceUiState> = _uiState.asStateFlow()

    private val attendanceMap = mutableMapOf<String, Boolean>()

    init {
        loadModules()
    }

    fun loadModules() {
        viewModelScope.launch {
            moduleDao.getAllModules().collect {
                _modules.value = it
                // Select first module by default
                if (_selectedModuleId.value == null && it.isNotEmpty()) {
                    _selectedModuleId.value = it.first().id
                    loadStudents()
                }
            }
        }
    }

    fun selectModule(moduleId: String) {
        _selectedModuleId.value = moduleId
        loadStudents()
    }

    fun loadStudents() {
        viewModelScope.launch {
            _uiState.value = LecturerAttendanceUiState.Loading
            try {
                val moduleId = _selectedModuleId.value
                if (moduleId == null) {
                    _students.value = emptyList()
                    _uiState.value = LecturerAttendanceUiState.Idle
                    return@launch
                }

                // Get all students - use first() to get current value
                var usersLoaded = false
                userDao.getAllUsers().collect { users ->
                    if (!usersLoaded) {
                        usersLoaded = true
                        val studentUsers = users.filter { it.role == com.example.unisyncpoe.data.model.UserRole.STUDENT }
                        
                        val studentList = studentUsers.map { student ->
                            StudentAttendanceItem(
                                studentId = student.id,
                                studentName = student.name,
                                isPresent = attendanceMap[student.id] ?: true
                            )
                        }

                        _students.value = studentList
                        _uiState.value = LecturerAttendanceUiState.Idle
                    }
                }
            } catch (e: Exception) {
                _uiState.value = LecturerAttendanceUiState.Error("Failed to load students: ${e.message}")
            }
        }
    }

    fun updateAttendance(studentId: String, isPresent: Boolean) {
        attendanceMap[studentId] = isPresent
    }

    fun submitAttendance() {
        viewModelScope.launch {
            val moduleId = _selectedModuleId.value
            val lecturerId = authManager.getUserId()
            val lecturer = lecturerId?.let { userDao.getUserById(it) }
            val lecturerName = lecturer?.name ?: authManager.getUserEmail() ?: "Lecturer"

            if (moduleId == null) {
                _uiState.value = LecturerAttendanceUiState.Error("Please select a module")
                return@launch
            }

            if (lecturerId == null) {
                _uiState.value = LecturerAttendanceUiState.Error("Lecturer not found")
                return@launch
            }

            val module = moduleDao.getModuleById(moduleId)
            if (module == null) {
                _uiState.value = LecturerAttendanceUiState.Error("Module not found")
                return@launch
            }

            _uiState.value = LecturerAttendanceUiState.Loading

            try {
                val now = System.currentTimeMillis()
                val attendances = _students.value.map { student ->
                    Attendance(
                        id = "attendance_${lecturerId}_${student.studentId}_${now}",
                        studentId = student.studentId,
                        studentName = student.studentName,
                        courseId = moduleId,
                        courseName = module.name,
                        lecturerId = lecturerId,
                        classDate = now,
                        markedAt = now,
                        status = if (attendanceMap[student.studentId] ?: true) {
                            AttendanceStatus.PRESENT
                        } else {
                            AttendanceStatus.ABSENT
                        }
                    )
                }

                attendanceDao.insertAttendances(attendances)
                attendanceMap.clear()
                _uiState.value = LecturerAttendanceUiState.Success("Attendance marked for ${attendances.size} students")
            } catch (e: Exception) {
                _uiState.value = LecturerAttendanceUiState.Error("Failed to submit attendance: ${e.message}")
            }
        }
    }
}

sealed class LecturerAttendanceUiState {
    object Idle : LecturerAttendanceUiState()
    object Loading : LecturerAttendanceUiState()
    data class Success(val message: String) : LecturerAttendanceUiState()
    data class Error(val message: String) : LecturerAttendanceUiState()
}

