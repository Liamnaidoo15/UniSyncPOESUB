package com.example.unisyncpoe.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unisyncpoe.data.local.dao.PendingApprovalDao
import com.example.unisyncpoe.data.model.ApprovalStatus
import com.example.unisyncpoe.data.model.PendingApproval
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResourceItem(
    val id: String,
    val title: String,
    val description: String,
    val moduleCode: String?,
    val fileUrl: String?,
    val uploadedAt: Long,
    val uploadedByName: String
)

@HiltViewModel
class ViewResourcesViewModel @Inject constructor(
    private val pendingApprovalDao: PendingApprovalDao
) : ViewModel() {

    private val _resources = MutableStateFlow<List<ResourceItem>>(emptyList())
    val resources: StateFlow<List<ResourceItem>> = _resources.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadResources() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val approvedResources = pendingApprovalDao.getApprovedResources()
                val resourceList = approvedResources.map { approval ->
                    ResourceItem(
                        id = approval.id,
                        title = approval.title,
                        description = approval.description,
                        moduleCode = approval.moduleCode,
                        fileUrl = approval.fileUrl,
                        uploadedAt = approval.uploadedAt,
                        uploadedByName = approval.uploadedByName
                    )
                }
                _resources.value = resourceList.sortedByDescending { it.uploadedAt }
            } catch (e: Exception) {
                _resources.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

