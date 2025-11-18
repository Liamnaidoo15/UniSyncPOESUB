package com.example.unisyncpoe.ui.lecturer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unisyncpoe.data.local.dao.AssignmentDao
import com.example.unisyncpoe.data.model.Assignment
import com.example.unisyncpoe.data.model.SubmissionStatus
import com.example.unisyncpoe.util.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssignmentSubmission(
    val id: String,
    val assignmentId: String,
    val assignmentTitle: String,
    val studentId: String,
    val studentName: String,
    val courseName: String,
    val submittedAt: Long?,
    val currentScore: Int?,
    val maxScore: Int,
    val status: SubmissionStatus
)

@HiltViewModel
class MarkAssignmentsViewModel @Inject constructor(
    private val assignmentDao: AssignmentDao,
    private val authManager: AuthManager
) : ViewModel() {

    private val _submissions = MutableStateFlow<List<AssignmentSubmission>>(emptyList())
    val submissions: StateFlow<List<AssignmentSubmission>> = _submissions.asStateFlow()

    private val _uiState = MutableStateFlow<MarkAssignmentsUiState>(MarkAssignmentsUiState.Idle)
    val uiState: StateFlow<MarkAssignmentsUiState> = _uiState.asStateFlow()

    private val scoreMap = mutableMapOf<String, Int>()

    fun loadSubmissions() {
        viewModelScope.launch {
            _uiState.value = MarkAssignmentsUiState.Loading
            try {
                val lecturerId = authManager.getUserId() ?: return@launch
                
                val allAssignments = assignmentDao.getAllAssignmentsList()
                // Filter assignments for this lecturer that have been submitted
                val submittedAssignments = allAssignments.filter { assignment ->
                    assignment.lecturerId == lecturerId &&
                    assignment.submissionStatus in listOf(SubmissionStatus.SUBMITTED, SubmissionStatus.GRADED)
                }

                // Convert to AssignmentSubmission format
                val submissionList = submittedAssignments.map { assignment ->
                    // Extract student ID from assignment ID pattern: "assignment_lecturerId_studentId_i"
                    val parts = assignment.id.split("_")
                    val studentId = if (parts.size >= 3) parts[2] else "unknown"
                    
                    AssignmentSubmission(
                        id = assignment.id,
                        assignmentId = assignment.id,
                        assignmentTitle = assignment.title,
                        studentId = studentId,
                        studentName = "Student $studentId", // Will be resolved from User table if needed
                        courseName = assignment.courseName,
                        submittedAt = assignment.submittedAt,
                        currentScore = assignment.score,
                        maxScore = assignment.maxScore,
                        status = assignment.submissionStatus
                    )
                }

                _submissions.value = submissionList.sortedByDescending { it.submittedAt ?: 0L }
                _uiState.value = MarkAssignmentsUiState.Idle
            } catch (e: Exception) {
                _uiState.value = MarkAssignmentsUiState.Error("Failed to load submissions: ${e.message}")
            }
        }
    }

    fun updateScore(submissionId: String, score: Int) {
        scoreMap[submissionId] = score
    }

    fun submitGrades() {
        viewModelScope.launch {
            _uiState.value = MarkAssignmentsUiState.Loading
            try {
                var gradedCount = 0
                scoreMap.forEach { (submissionId, score) ->
                    val assignment = assignmentDao.getAssignmentById(submissionId)
                    if (assignment != null) {
                        val updatedAssignment = assignment.copy(
                            score = score,
                            submissionStatus = SubmissionStatus.GRADED
                        )
                        assignmentDao.updateAssignment(updatedAssignment)
                        gradedCount++
                    }
                }

                scoreMap.clear()
                _uiState.value = MarkAssignmentsUiState.Success("Successfully graded $gradedCount submission(s)")
                loadSubmissions() // Refresh the list
            } catch (e: Exception) {
                _uiState.value = MarkAssignmentsUiState.Error("Failed to submit grades: ${e.message}")
            }
        }
    }
}

sealed class MarkAssignmentsUiState {
    object Idle : MarkAssignmentsUiState()
    object Loading : MarkAssignmentsUiState()
    data class Success(val message: String) : MarkAssignmentsUiState()
    data class Error(val message: String) : MarkAssignmentsUiState()
}

