package com.example.unisyncpoe.ui.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unisyncpoe.data.local.dao.*
import com.example.unisyncpoe.data.local.dao.SystemLogDao
import com.example.unisyncpoe.data.model.*
import com.example.unisyncpoe.data.remote.FirestoreService
import com.example.unisyncpoe.util.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AcademicSettingsViewModel @Inject constructor(
    private val academicYearDao: AcademicYearDao,
    private val semesterDao: SemesterDao,
    private val moduleDao: ModuleDao,
    private val systemLogDao: SystemLogDao,
    private val authManager: AuthManager,
    private val firestoreService: FirestoreService
) : ViewModel() {
    
    companion object {
        private const val TAG = "AcademicSettingsViewModel"
    }
    
    private val _uiState = MutableStateFlow<AcademicSettingsUiState>(AcademicSettingsUiState.Idle)
    val uiState: StateFlow<AcademicSettingsUiState> = _uiState.asStateFlow()
    
    private var semesterStartDate: Long? = null
    private var semesterEndDate: Long? = null
    private var examWeekStart: Long? = null
    private var examWeekEnd: Long? = null
    private var selectedAcademicYearId: String? = null
    
    fun addAcademicYear(year: String) {
        viewModelScope.launch {
            _uiState.value = AcademicSettingsUiState.Loading
            try {
                // Deactivate all other active years
                val activeYears = academicYearDao.getAllActiveAcademicYears()
                activeYears.forEach { yearEntity ->
                    academicYearDao.updateAcademicYear(yearEntity.copy(isActive = false))
                }
                
                val academicYear = AcademicYear(
                    id = "year_${System.currentTimeMillis()}",
                    year = year,
                    startDate = System.currentTimeMillis(),
                    endDate = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000), // 1 year from now
                    isActive = true
                )
                academicYearDao.insertAcademicYear(academicYear)
                selectedAcademicYearId = academicYear.id
                
                // Log action
                logAction("Academic Year Added", "Academic year $year added")
                
                _uiState.value = AcademicSettingsUiState.Success("Academic year added successfully", true)
            } catch (e: Exception) {
                _uiState.value = AcademicSettingsUiState.Error("Failed to add academic year: ${e.message}")
            }
        }
    }
    
    fun setSemesterStartDate(timestamp: Long) {
        semesterStartDate = timestamp
    }
    
    fun setSemesterEndDate(timestamp: Long) {
        semesterEndDate = timestamp
    }
    
    fun setExamWeekStart(timestamp: Long) {
        examWeekStart = timestamp
    }
    
    fun setExamWeekEnd(timestamp: Long) {
        examWeekEnd = timestamp
    }
    
    fun saveSemester(name: String) {
        viewModelScope.launch {
            if (semesterStartDate == null || semesterEndDate == null || examWeekStart == null || examWeekEnd == null) {
                _uiState.value = AcademicSettingsUiState.Error("Please select all dates")
                return@launch
            }
            
            if (selectedAcademicYearId == null) {
                val activeYear = academicYearDao.getActiveAcademicYear()
                if (activeYear == null) {
                    _uiState.value = AcademicSettingsUiState.Error("Please add an academic year first")
                    return@launch
                }
                selectedAcademicYearId = activeYear.id
            }
            
            _uiState.value = AcademicSettingsUiState.Loading
            
            try {
                // Deactivate all other active semesters
                val activeSemesters = semesterDao.getAllActiveSemesters()
                activeSemesters.forEach { semester ->
                    semesterDao.updateSemester(semester.copy(isActive = false))
                }
                
                val semester = Semester(
                    id = "semester_${System.currentTimeMillis()}",
                    academicYearId = selectedAcademicYearId!!,
                    name = name,
                    startDate = semesterStartDate!!,
                    endDate = semesterEndDate!!,
                    examWeekStart = examWeekStart!!,
                    examWeekEnd = examWeekEnd!!,
                    isActive = true
                )
                semesterDao.insertSemester(semester)
                
                // Log action
                logAction("Semester Added", "Semester $name added")
                
                _uiState.value = AcademicSettingsUiState.Success("Semester saved successfully", true)
                
                // Reset dates
                semesterStartDate = null
                semesterEndDate = null
                examWeekStart = null
                examWeekEnd = null
            } catch (e: Exception) {
                _uiState.value = AcademicSettingsUiState.Error("Failed to save semester: ${e.message}")
            }
        }
    }
    
    fun addModule(code: String, name: String, credits: Int) {
        viewModelScope.launch {
            _uiState.value = AcademicSettingsUiState.Loading
            try {
                val activeSemester = semesterDao.getActiveSemester()
                val module = Module(
                    id = "module_${System.currentTimeMillis()}",
                    code = code,
                    name = name,
                    credits = credits,
                    semesterId = activeSemester?.id,
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
                
                // Log action
                logAction("Module Added", "Module $code - $name added")
                
                _uiState.value = AcademicSettingsUiState.Success("Module added successfully", true)
            } catch (e: Exception) {
                _uiState.value = AcademicSettingsUiState.Error("Failed to add module: ${e.message}")
            }
        }
    }
    
    private fun logAction(action: String, description: String) {
        viewModelScope.launch {
            val log = SystemLog(
                id = "log_${System.currentTimeMillis()}",
                action = action,
                description = description,
                performedBy = authManager.getUserId() ?: "unknown",
                performedByName = authManager.getUserEmail() ?: "Admin",
                logType = LogType.SUCCESS
            )
            systemLogDao.insertLog(log)
        }
    }
}

sealed class AcademicSettingsUiState {
    object Idle : AcademicSettingsUiState()
    object Loading : AcademicSettingsUiState()
    data class Success(val message: String, val clearForm: Boolean = false) : AcademicSettingsUiState()
    data class Error(val message: String) : AcademicSettingsUiState()
}

