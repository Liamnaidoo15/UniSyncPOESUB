package com.example.unisyncpoe.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unisyncpoe.data.local.dao.TimetableDao
import com.example.unisyncpoe.data.model.Timetable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val timetableDao: TimetableDao
) : ViewModel() {

    private val _timetableByDay = MutableStateFlow<Map<Int, List<Timetable>>>(emptyMap())
    val timetableByDay: StateFlow<Map<Int, List<Timetable>>> = _timetableByDay.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadTimetable() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                timetableDao.getAllTimetables().collect { timetables ->
                    val groupedByDay = timetables.groupBy { it.dayOfWeek }
                    // Ensure all days 1-7 are present
                    val completeMap = (1..7).associateWith { day ->
                        groupedByDay[day] ?: emptyList()
                    }
                    _timetableByDay.value = completeMap
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }
}

