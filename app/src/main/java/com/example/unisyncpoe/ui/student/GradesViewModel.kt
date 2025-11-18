package com.example.unisyncpoe.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unisyncpoe.data.local.dao.AssignmentDao
import com.example.unisyncpoe.data.model.SubmissionStatus
import com.example.unisyncpoe.util.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GradeItem(
    val module: String,
    val assessment: String,
    val mark: Int?,
    val maxMark: Int,
    val status: String
)

@HiltViewModel
class GradesViewModel @Inject constructor(
    private val assignmentDao: AssignmentDao,
    private val authManager: AuthManager
) : ViewModel() {

    private val _grades = MutableStateFlow<List<GradeItem>>(emptyList())
    val grades: StateFlow<List<GradeItem>> = _grades.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadGrades() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val studentId = authManager.getUserId() ?: return@launch
                
                var assignmentsLoaded = false
                assignmentDao.getAllAssignments().collect { allAssignments ->
                    if (!assignmentsLoaded) {
                        assignmentsLoaded = true
                        // Filter assignments for this student (extract student ID from assignment ID pattern)
                        val studentAssignments = allAssignments.filter { assignment ->
                            val parts = assignment.id.split("_")
                            parts.size >= 3 && parts[2] == studentId
                        }

                val gradeList = studentAssignments.map { assignment ->
                    val status = when (assignment.submissionStatus) {
                        SubmissionStatus.GRADED -> "Released"
                        SubmissionStatus.SUBMITTED -> "Pending"
                        SubmissionStatus.NOT_SUBMITTED -> "Not Submitted"
                        SubmissionStatus.LATE -> "Late"
                    }

                    GradeItem(
                        module = assignment.courseName,
                        assessment = assignment.title,
                        mark = assignment.score,
                        maxMark = assignment.maxScore,
                        status = status
                    )
                }

                        _grades.value = gradeList.sortedByDescending { it.status == "Released" }
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }
}

