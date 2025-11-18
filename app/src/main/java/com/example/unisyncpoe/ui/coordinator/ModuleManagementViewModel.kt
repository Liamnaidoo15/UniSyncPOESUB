package com.example.unisyncpoe.ui.coordinator

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unisyncpoe.data.local.dao.ModuleDao
import com.example.unisyncpoe.data.model.Module
import com.example.unisyncpoe.data.remote.FirestoreService
import com.example.unisyncpoe.util.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModuleManagementViewModel @Inject constructor(
    private val moduleDao: ModuleDao,
    private val authManager: AuthManager,
    private val firestoreService: FirestoreService
) : ViewModel() {
    
    companion object {
        private const val TAG = "ModuleManagementViewModel"
    }

    private val _modules = MutableStateFlow<List<Module>>(emptyList())
    val modules: StateFlow<List<Module>> = _modules.asStateFlow()

    private var selectedModuleIndex: Int = -1

    init {
        loadModules()
    }

    fun loadModules() {
        viewModelScope.launch {
            moduleDao.getAllModules().collect { modulesList ->
                _modules.value = modulesList
            }
        }
    }

    fun selectModule(position: Int) {
        selectedModuleIndex = position
    }

    fun getSelectedModule(): Module? {
        return if (selectedModuleIndex >= 0 && selectedModuleIndex < _modules.value.size) {
            _modules.value[selectedModuleIndex]
        } else {
            null
        }
    }

    fun addModule(code: String, name: String, credits: Int) {
        viewModelScope.launch {
            val module = Module(
                id = "module_${System.currentTimeMillis()}",
                code = code,
                name = name,
                credits = credits,
                coordinatorId = authManager.getUserId(),
                coordinatorName = authManager.getUserEmail() ?: "Coordinator",
                isActive = true,
                isSynced = false // Will be set to true after Firestore save
            )
            
            // Save to local database first
            moduleDao.insertModule(module)
            
            // Save to Firestore
            firestoreService.saveModule(module).fold(
                onSuccess = {
                    // Update sync status in local database
                    moduleDao.updateModule(module.copy(isSynced = true))
                    Log.d(TAG, "Module saved to Firestore successfully: ${module.code}")
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to save module to Firestore: ${error.message}")
                    // Module is still saved locally, will sync later
                }
            )
            
            loadModules()
        }
    }

    fun updateModule(moduleId: String, code: String, name: String, credits: Int) {
        viewModelScope.launch {
            val existingModule = moduleDao.getModuleById(moduleId)
            if (existingModule != null) {
                val updatedModule = existingModule.copy(
                    code = code,
                    name = name,
                    credits = credits,
                    isSynced = false // Will be set to true after Firestore save
                )
                
                // Update in local database
                moduleDao.updateModule(updatedModule)
                
                // Update in Firestore
                firestoreService.updateModule(updatedModule).fold(
                    onSuccess = {
                        // Update sync status in local database
                        moduleDao.updateModule(updatedModule.copy(isSynced = true))
                        Log.d(TAG, "Module updated in Firestore successfully: ${updatedModule.code}")
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to update module in Firestore: ${error.message}")
                    }
                )
                
                loadModules()
            }
        }
    }

    fun deleteModule(moduleId: String) {
        viewModelScope.launch {
            val module = moduleDao.getModuleById(moduleId)
            if (module != null) {
                // Delete from local database
                moduleDao.deleteModule(module)
                
                // Delete from Firestore
                firestoreService.deleteModule(moduleId).fold(
                    onSuccess = {
                        Log.d(TAG, "Module deleted from Firestore successfully: $moduleId")
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to delete module from Firestore: ${error.message}")
                    }
                )
                
                loadModules()
            }
        }
    }

    fun initializeDummyModules() {
        viewModelScope.launch {
            var checked = false
            moduleDao.getAllModules().collect { modules ->
                if (!checked) {
                    checked = true
                    if (modules.isEmpty()) {
                        val dummyModules = listOf(
                            Module(
                                id = "module_dummy_1",
                                code = "CS101",
                                name = "Introduction to Computer Science",
                                credits = 12,
                                coordinatorId = authManager.getUserId(),
                                coordinatorName = "Coordinator",
                                isActive = true
                            ),
                            Module(
                                id = "module_dummy_2",
                                code = "CS201",
                                name = "Data Structures and Algorithms",
                                credits = 15,
                                coordinatorId = authManager.getUserId(),
                                coordinatorName = "Coordinator",
                                isActive = true
                            ),
                            Module(
                                id = "module_dummy_3",
                                code = "MATH101",
                                name = "Calculus I",
                                credits = 12,
                                coordinatorId = authManager.getUserId(),
                                coordinatorName = "Coordinator",
                                isActive = true
                            )
                        )

                        dummyModules.forEach { module ->
                            moduleDao.insertModule(module)
                        }

                        loadModules()
                    }
                }
            }
        }
    }
}

