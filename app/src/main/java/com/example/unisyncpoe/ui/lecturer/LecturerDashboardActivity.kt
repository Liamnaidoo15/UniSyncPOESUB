package com.example.unisyncpoe.ui.lecturer

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
import com.example.unisyncpoe.databinding.ActivityLecturerDashboardBinding
import com.example.unisyncpoe.ui.auth.AuthViewModel
import com.example.unisyncpoe.ui.auth.LoginActivity
import com.example.unisyncpoe.ui.settings.SettingsActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Lecturer Dashboard Activity
 * Displays lecturer-specific features and tabs
 */
@AndroidEntryPoint
class LecturerDashboardActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "LecturerDashboard"
    }
    
    private lateinit var binding: ActivityLecturerDashboardBinding
    private val viewModel: LecturerDashboardViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLecturerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        Log.d(TAG, "LecturerDashboardActivity created")
        
        setupToolbar()
        setupViewPager()
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.lecturer_dashboard)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
    
    private fun setupViewPager() {
        // ViewPager removed - using feature cards instead
    }
    
    private fun setupClickListeners() {
        // Upload Learning Materials
        binding.cardUploadMaterials.setOnClickListener {
            startActivity(Intent(this, UploadResourcesActivity::class.java))
        }
        
        // Manage Attendance
        binding.cardManageAttendance.setOnClickListener {
            startActivity(Intent(this, LecturerAttendanceActivity::class.java))
        }
        
        // Mark Submissions
        binding.cardMarkSubmissions.setOnClickListener {
            startActivity(Intent(this, MarkAssignmentsActivity::class.java))
        }
        
        // Communicate with Students (Messages)
        binding.cardCommunicate.setOnClickListener {
            startActivity(Intent(this, com.example.unisyncpoe.ui.messages.MessagesActivity::class.java))
        }
        
        // Track Student Progress
        binding.cardTrackProgress.setOnClickListener {
            startActivity(Intent(this, StudentProgressActivity::class.java))
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
        menuInflater.inflate(R.menu.lecturer_dashboard_menu, menu)
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

