package com.example.unisyncpoe.ui.student

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.unisyncpoe.R
import com.example.unisyncpoe.databinding.ActivityStudentDashboardBinding
import com.example.unisyncpoe.ui.auth.AuthViewModel
import com.example.unisyncpoe.ui.auth.LoginActivity
import com.example.unisyncpoe.ui.settings.SettingsActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Student Dashboard Activity
 * Displays student-specific features and tabs
 */
@AndroidEntryPoint
class StudentDashboardActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "StudentDashboard"
    }
    
    private lateinit var binding: ActivityStudentDashboardBinding
    private val viewModel: StudentDashboardViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        Log.d(TAG, "StudentDashboardActivity created")
        
        setupToolbar()
        setupViewPager()
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.student_dashboard)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
    
    private fun setupViewPager() {
        // ViewPager removed - using feature cards instead
    }
    
    private fun setupClickListeners() {
        // View Modules & Timetable
        binding.cardViewModules.setOnClickListener {
            startActivity(Intent(this, TimetableActivity::class.java))
        }
        
        // Submit Assignments
        binding.cardSubmitAssignments.setOnClickListener {
            startActivity(Intent(this, SubmitAssignmentActivity::class.java))
        }
        
        // Access Course Resources
        binding.cardAccessResources.setOnClickListener {
            startActivity(Intent(this, ViewResourcesActivity::class.java))
        }
        
        // Communicate with Lecturers (Messages)
        binding.cardCommunicate.setOnClickListener {
            startActivity(Intent(this, com.example.unisyncpoe.ui.messages.MessagesActivity::class.java))
        }
        
        // Track Progress (Grades)
        binding.cardTrackProgress.setOnClickListener {
            startActivity(Intent(this, GradesActivity::class.java))
        }
        
        // Refresh statistics
        binding.swipeRefresh.setOnRefreshListener {
            Log.d(TAG, "Swipe refresh triggered")
            viewModel.refreshStatistics()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.swipeRefresh.isRefreshing = isLoading
                if (isLoading) {
                    binding.progressBar.visibility = View.VISIBLE
                } else {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.student_dashboard_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.menu_sync -> {
                viewModel.syncData()
                true
            }
            R.id.menu_logout -> {
                authViewModel.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

