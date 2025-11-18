package com.example.unisyncpoe.ui.coordinator

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.unisyncpoe.R
import com.example.unisyncpoe.databinding.ActivityReportsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Coordinator activity for viewing program-level reports
 */
@AndroidEntryPoint
class ReportsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityReportsBinding
    private val viewModel: ReportsViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.generate_program_reports)
        
        // These are static placeholders - in a real app, you would use a charting library
        binding.tvStudentPerformance.text = "Student Performance Chart\n(Placeholder)\n\nAverage Score: 75%\nPass Rate: 85%"
        binding.tvSubmissionRate.text = "Submission Completion Rate\n(Placeholder)\n\nOn Time: 78%\nLate: 15%\nMissing: 7%"
        binding.tvModuleStats.text = "Module Statistics\n(Placeholder)\n\nActive Modules: 12\nTotal Students: 450\nTotal Lecturers: 8"
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            // ViewModel can be used for future data loading
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

