package com.example.unisyncpoe.ui.student

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unisyncpoe.data.local.dao.AssignmentDao
import com.example.unisyncpoe.data.local.dao.ModuleDao
import com.example.unisyncpoe.data.model.SubmissionStatus
import com.example.unisyncpoe.util.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubmitAssignmentViewModel @Inject constructor(
    private val moduleDao: ModuleDao,
    private val assignmentDao: AssignmentDao,
    private val userDao: com.example.unisyncpoe.data.local.dao.UserDao,
    private val authManager: AuthManager
) : ViewModel() {

    private val _modules = MutableStateFlow<List<com.example.unisyncpoe.data.model.Module>>(emptyList())
    val modules: StateFlow<List<com.example.unisyncpoe.data.model.Module>> = _modules.asStateFlow()

    private val _selectedAssignmentId = MutableStateFlow<String?>(null)
    val selectedAssignmentId: StateFlow<String?> = _selectedAssignmentId.asStateFlow()

    private val _uiState = MutableStateFlow<SubmitAssignmentUiState>(SubmitAssignmentUiState.Idle)
    val uiState: StateFlow<SubmitAssignmentUiState> = _uiState.asStateFlow()

    fun loadModules() {
        viewModelScope.launch {
            moduleDao.getAllModules().collect {
                _modules.value = it
            }
        }
    }

    fun submitAssignment(
        assignmentId: String,
        moduleId: String,
        fileUri: Uri,
        comments: String
    ) {
        viewModelScope.launch {
            _uiState.value = SubmitAssignmentUiState.Loading
            try {
                val studentId = authManager.getUserId() ?: return@launch
                val student = userDao.getUserById(studentId)
                val studentName = student?.name ?: "Student"

                // Find the assignment
                val assignment = assignmentDao.getAssignmentById(assignmentId)
                if (assignment == null) {
                    // Create a new assignment entry for this submission
                    val module = moduleDao.getModuleById(moduleId)
                    val newAssignment = com.example.unisyncpoe.data.model.Assignment(
                        id = "assignment_${assignmentId}_${studentId}_${System.currentTimeMillis()}",
                        title = "Assignment Submission",
                        description = comments,
                        courseId = moduleId,
                        courseName = module?.name ?: "Course",
                        lecturerId = assignment?.lecturerId ?: "unknown",
                        lecturerName = assignment?.lecturerName ?: "Lecturer",
                        dueDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L), // 7 days from now
                        maxScore = 100,
                        submissionStatus = SubmissionStatus.SUBMITTED,
                        submittedAt = System.currentTimeMillis()
                    )
                    assignmentDao.insertAssignment(newAssignment)
                } else {
                    // Update existing assignment
                    val updatedAssignment = assignment.copy(
                        submissionStatus = SubmissionStatus.SUBMITTED,
                        submittedAt = System.currentTimeMillis()
                    )
                    assignmentDao.updateAssignment(updatedAssignment)
                }

                _uiState.value = SubmitAssignmentUiState.Success("Assignment submitted successfully!")
            } catch (e: Exception) {
                _uiState.value = SubmitAssignmentUiState.Error("Failed to submit assignment: ${e.message}")
            }
        }
    }
}

sealed class SubmitAssignmentUiState {
    object Idle : SubmitAssignmentUiState()
    object Loading : SubmitAssignmentUiState()
    data class Success(val message: String) : SubmitAssignmentUiState()
    data class Error(val message: String) : SubmitAssignmentUiState()
}

