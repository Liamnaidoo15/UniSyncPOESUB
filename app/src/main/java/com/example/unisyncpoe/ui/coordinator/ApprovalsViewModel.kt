package com.example.unisyncpoe.ui.coordinator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unisyncpoe.data.local.dao.PendingApprovalDao
import com.example.unisyncpoe.data.model.ApprovalStatus
import com.example.unisyncpoe.data.model.ApprovalType
import com.example.unisyncpoe.data.model.PendingApproval
import com.example.unisyncpoe.util.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApprovalsViewModel @Inject constructor(
    private val pendingApprovalDao: PendingApprovalDao,
    private val authManager: AuthManager
) : ViewModel() {
    
    private val _approvals = MutableStateFlow<List<PendingApproval>>(emptyList())
    val approvals: StateFlow<List<PendingApproval>> = _approvals.asStateFlow()
    
    private var selectedApprovalIndex: Int = -1
    
    init {
        loadApprovals()
    }
    
    fun loadApprovals() {
        viewModelScope.launch {
            pendingApprovalDao.getPendingApprovals().collect { approvalsList ->
                _approvals.value = approvalsList
            }
        }
    }
    
    fun selectApproval(position: Int) {
        selectedApprovalIndex = position
    }
    
    fun getSelectedApproval(): PendingApproval? {
        return if (selectedApprovalIndex >= 0 && selectedApprovalIndex < _approvals.value.size) {
            _approvals.value[selectedApprovalIndex]
        } else {
            null
        }
    }
    
    fun approve(approvalId: String) {
        viewModelScope.launch {
            val approval = pendingApprovalDao.getApprovalById(approvalId)
            if (approval != null) {
                val updatedApproval = approval.copy(
                    status = ApprovalStatus.APPROVED,
                    reviewedBy = authManager.getUserId(),
                    reviewedAt = System.currentTimeMillis()
                )
                pendingApprovalDao.updateApproval(updatedApproval)
                loadApprovals()
            }
        }
    }
    
    fun reject(approvalId: String, reason: String) {
        viewModelScope.launch {
            val approval = pendingApprovalDao.getApprovalById(approvalId)
            if (approval != null) {
                val updatedApproval = approval.copy(
                    status = ApprovalStatus.REJECTED,
                    reviewedBy = authManager.getUserId(),
                    reviewedAt = System.currentTimeMillis(),
                    reviewComments = reason.ifEmpty { "Rejected by coordinator" }
                )
                pendingApprovalDao.updateApproval(updatedApproval)
                loadApprovals()
            }
        }
    }
    
    fun initializeDummyApprovals() {
        viewModelScope.launch {
            var checked = false
            pendingApprovalDao.getPendingApprovals().collect { approvals ->
                if (!checked) {
                    checked = true
                    if (approvals.isEmpty()) {
                        val dummyApprovals = listOf(
                            PendingApproval(
                                id = "approval_1",
                                title = "Introduction to Java Programming",
                                description = "New learning resource uploaded for CS101",
                                type = ApprovalType.RESOURCE,
                                uploadedBy = "lecturer_001",
                                uploadedByName = "Dr. Smith",
                                moduleId = "module_dummy_1",
                                moduleCode = "CS101",
                                status = ApprovalStatus.PENDING,
                                uploadedAt = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L)
                            ),
                            PendingApproval(
                                id = "approval_2",
                                title = "Midterm Exam Announcement",
                                description = "Announcement for upcoming midterm exam",
                                type = ApprovalType.ANNOUNCEMENT,
                                uploadedBy = "lecturer_002",
                                uploadedByName = "Prof. Johnson",
                                moduleId = "module_dummy_2",
                                moduleCode = "CS201",
                                status = ApprovalStatus.PENDING,
                                uploadedAt = System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000L)
                            ),
                            PendingApproval(
                                id = "approval_3",
                                title = "Data Structures Lecture Notes",
                                description = "Updated lecture notes for week 5",
                                type = ApprovalType.MODULE_CONTENT,
                                uploadedBy = "lecturer_001",
                                uploadedByName = "Dr. Smith",
                                moduleId = "module_dummy_2",
                                moduleCode = "CS201",
                                status = ApprovalStatus.PENDING,
                                uploadedAt = System.currentTimeMillis() - (12 * 60 * 60 * 1000L)
                            )
                        )
                        
                        dummyApprovals.forEach { approval ->
                            pendingApprovalDao.insertApproval(approval)
                        }
                        
                        loadApprovals()
                    }
                }
            }
        }
    }
}

