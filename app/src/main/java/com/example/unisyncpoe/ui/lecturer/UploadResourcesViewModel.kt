package com.example.unisyncpoe.ui.lecturer

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unisyncpoe.data.local.dao.ModuleDao
import com.example.unisyncpoe.data.local.dao.PendingApprovalDao
import com.example.unisyncpoe.data.local.dao.UserDao
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
class UploadResourcesViewModel @Inject constructor(
    private val moduleDao: ModuleDao,
    private val pendingApprovalDao: PendingApprovalDao,
    private val userDao: UserDao,
    private val authManager: AuthManager
) : ViewModel() {

    private val _modules = MutableStateFlow<List<com.example.unisyncpoe.data.model.Module>>(emptyList())
    val modules: StateFlow<List<com.example.unisyncpoe.data.model.Module>> = _modules.asStateFlow()

    private val _uiState = MutableStateFlow<UploadResourcesUiState>(UploadResourcesUiState.Idle)
    val uiState: StateFlow<UploadResourcesUiState> = _uiState.asStateFlow()

    fun loadModules() {
        viewModelScope.launch {
            moduleDao.getAllModules().collect {
                _modules.value = it
            }
        }
    }

    fun uploadResource(
        title: String,
        description: String,
        moduleId: String,
        moduleCode: String,
        fileUri: Uri
    ) {
        viewModelScope.launch {
            _uiState.value = UploadResourcesUiState.Loading
            try {
                val lecturerId = authManager.getUserId() ?: return@launch
                val lecturer = userDao.getUserById(lecturerId)
                val lecturerName = lecturer?.name ?: authManager.getUserEmail() ?: "Lecturer"

                // In a real app, you would upload the file to a server and get a URL
                // For now, we'll use the URI as a placeholder
                val fileUrl = fileUri.toString()

                val pendingApproval = PendingApproval(
                    id = "resource_${System.currentTimeMillis()}",
                    title = title,
                    description = description,
                    type = ApprovalType.RESOURCE,
                    uploadedBy = lecturerId,
                    uploadedByName = lecturerName,
                    moduleId = moduleId,
                    moduleCode = moduleCode,
                    fileUrl = fileUrl,
                    uploadedAt = System.currentTimeMillis(),
                    status = com.example.unisyncpoe.data.model.ApprovalStatus.PENDING
                )

                pendingApprovalDao.insertApproval(pendingApproval)
                _uiState.value = UploadResourcesUiState.Success("Resource uploaded successfully! Awaiting coordinator approval.")
            } catch (e: Exception) {
                _uiState.value = UploadResourcesUiState.Error("Failed to upload resource: ${e.message}")
            }
        }
    }
}

sealed class UploadResourcesUiState {
    object Idle : UploadResourcesUiState()
    object Loading : UploadResourcesUiState()
    data class Success(val message: String) : UploadResourcesUiState()
    data class Error(val message: String) : UploadResourcesUiState()
}

