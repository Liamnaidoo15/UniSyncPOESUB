package com.example.unisyncpoe.ui.coordinator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unisyncpoe.data.local.dao.ModuleDao
import com.example.unisyncpoe.data.model.Module
import com.example.unisyncpoe.data.model.User
import com.example.unisyncpoe.data.model.UserRole
import com.example.unisyncpoe.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssignLecturerViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val moduleDao: ModuleDao
) : ViewModel() {
    
    private val _lecturers = MutableStateFlow<List<User>>(emptyList())
    val lecturers: StateFlow<List<User>> = _lecturers.asStateFlow()
    
    private val _modules = MutableStateFlow<List<Module>>(emptyList())
    val modules: StateFlow<List<Module>> = _modules.asStateFlow()
    
    private val _uiState = MutableStateFlow<AssignLecturerUiState>(AssignLecturerUiState.Idle)
    val uiState: StateFlow<AssignLecturerUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    fun loadData() {
        viewModelScope.launch {
            // First, sync users from Firestore to ensure we have lecturers
            try {
                // Trigger sync by getting statistics (which syncs users)
                userRepository.getUserStatistics()
            } catch (e: Exception) {
                // Ignore errors, continue with local data
            }
            
            // Load lecturers in separate coroutine
            launch {
                userRepository.getAllUsers().collect { users ->
                    val lecturers = users.filter { it.role == UserRole.LECTURER }
                    _lecturers.value = lecturers
                }
            }
            
            // Load modules in separate coroutine
            launch {
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
                }
            }
        }
    }
    
    private suspend fun initializeDummyModules() {
        // Check if modules already exist by trying to get one
        val existingModule = moduleDao.getModuleById("module_dummy_1")
        if (existingModule == null) {
            val dummyModules = listOf(
                Module(
                    id = "module_dummy_1",
                    code = "CS101",
                    name = "Introduction to Computer Science",
                    credits = 12,
                    isActive = true
                ),
                Module(
                    id = "module_dummy_2",
                    code = "CS201",
                    name = "Data Structures and Algorithms",
                    credits = 15,
                    isActive = true
                ),
                Module(
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
    
    fun assignLecturer(lecturerIndex: Int, moduleIndex: Int) {
        viewModelScope.launch {
            if (lecturerIndex < 0 || lecturerIndex >= _lecturers.value.size ||
                moduleIndex < 0 || moduleIndex >= _modules.value.size) {
                _uiState.value = AssignLecturerUiState.Error("Invalid selection")
                return@launch
            }
            
            val lecturer = _lecturers.value[lecturerIndex]
            val module = _modules.value[moduleIndex]
            
            // Update module with lecturer assignment (UI only - no actual logic)
            // In a real app, you would update the module's lecturerId field
            _uiState.value = AssignLecturerUiState.Success(
                "Lecturer ${lecturer.name} assigned to ${module.code} - ${module.name}"
            )
        }
    }
}

sealed class AssignLecturerUiState {
    object Idle : AssignLecturerUiState()
    data class Success(val message: String) : AssignLecturerUiState()
    data class Error(val message: String) : AssignLecturerUiState()
}

