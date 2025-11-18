package com.example.unisyncpoe.ui.coordinator

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.unisyncpoe.R
import com.example.unisyncpoe.databinding.ActivityMonitorActivityBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Coordinator activity for monitoring lecturer and student activity
 */
@AndroidEntryPoint
class MonitorActivityActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMonitorActivityBinding
    private val viewModel: MonitorActivityViewModel by viewModels()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonitorActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        observeViewModel()
        initializeDummyData()
    }
    
    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.monitor_activity)
        
        binding.btnRefresh.setOnClickListener {
            viewModel.loadActivity()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.lecturerActivity.collect { activities ->
                updateLecturerActivityList(activities)
            }
        }
        
        lifecycleScope.launch {
            viewModel.studentEngagement.collect { engagements ->
                updateStudentEngagementList(engagements)
            }
        }
        
        lifecycleScope.launch {
            viewModel.activityStats.collect { stats ->
                updateStatistics(stats)
            }
        }
    }
    
    private fun updateLecturerActivityList(activities: List<com.example.unisyncpoe.ui.coordinator.LecturerActivity>) {
        val activityStrings = activities.map { activity ->
            val date = dateFormat.format(Date(activity.timestamp))
            "[$date] ${activity.lecturerName}: ${activity.action}"
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, activityStrings)
        binding.listViewLecturerActivity.adapter = adapter
    }
    
    private fun updateStudentEngagementList(engagements: List<com.example.unisyncpoe.ui.coordinator.StudentEngagement>) {
        val engagementStrings = engagements.map { engagement ->
            "${engagement.studentName} - ${engagement.moduleCode}: ${engagement.metric} (${engagement.value})"
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, engagementStrings)
        binding.listViewStudentEngagement.adapter = adapter
    }
    
    private fun updateStatistics(stats: com.example.unisyncpoe.ui.coordinator.ActivityStatistics) {
        binding.tvTotalLecturers.text = "Total Lecturers: ${stats.totalLecturers}"
        binding.tvActiveLecturers.text = "Active This Week: ${stats.activeLecturers}"
        binding.tvTotalStudents.text = "Total Students: ${stats.totalStudents}"
        binding.tvEngagedStudents.text = "Engaged This Week: ${stats.engagedStudents}"
        binding.tvAverageAttendance.text = "Avg Attendance: ${stats.averageAttendance}%"
        binding.tvSubmissionRate.text = "Submission Rate: ${stats.submissionRate}%"
    }
    
    private fun initializeDummyData() {
        viewModel.initializeDummyData()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

