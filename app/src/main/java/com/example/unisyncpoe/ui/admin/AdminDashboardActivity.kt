package com.example.unisyncpoe.ui.admin

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
import com.example.unisyncpoe.databinding.ActivityAdminDashboardBinding
import com.example.unisyncpoe.ui.auth.AuthViewModel
import com.example.unisyncpoe.ui.auth.LoginActivity
import com.example.unisyncpoe.ui.settings.SettingsActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Admin Dashboard Activity
 * Displays user statistics and provides access to admin features
 */
@AndroidEntryPoint
class AdminDashboardActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "AdminDashboardActivity"
    }
    
    private lateinit var binding: ActivityAdminDashboardBinding
    private val viewModel: AdminDashboardViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        Log.d(TAG, "AdminDashboardActivity created")
        
        setupToolbar()
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.admin_dashboard)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
    
    private fun setupClickListeners() {
        // User Management
        binding.cardUserManagement.setOnClickListener {
            startActivity(Intent(this, UserManagementActivity::class.java))
        }
        
        // Roles & Permissions
        binding.cardRolesPermissions.setOnClickListener {
            startActivity(Intent(this, AssignRolesActivity::class.java))
        }
        
        // System Settings
        binding.cardSystemSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        
        // Academic Calendar
        binding.cardAcademicCalendar.setOnClickListener {
            startActivity(Intent(this, AcademicSettingsActivity::class.java))
        }
        
        // Security & Data Integrity
        binding.cardSecurity.setOnClickListener {
            startActivity(Intent(this, SecurityLogsActivity::class.java))
        }
        
        // Refresh statistics
        binding.swipeRefresh.setOnRefreshListener {
            Log.d(TAG, "Swipe refresh triggered")
            viewModel.refreshStatistics()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.userStatistics.collect { stats ->
                stats?.let {
                    updateStatistics(it)
                } ?: run {
                    // If stats are null, show zeros
                    updateStatistics(com.example.unisyncpoe.data.repository.UserRepository.UserStatistics(0, 0, 0, 0, 0))
                }
            }
        }
        
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
    
    private fun updateStatistics(stats: com.example.unisyncpoe.data.repository.UserRepository.UserStatistics) {
        Log.d(TAG, "Updating statistics - Total: ${stats.totalUsers}, Students: ${stats.studentCount}, Lecturers: ${stats.lecturerCount}, Coordinators: ${stats.coordinatorCount}, Admins: ${stats.adminCount}")
        binding.tvTotalUsers.text = stats.totalUsers.toString()
        binding.tvStudentCount.text = stats.studentCount.toString()
        binding.tvLecturerCount.text = stats.lecturerCount.toString()
        binding.tvAdminCount.text = stats.adminCount.toString()
        // Note: Coordinator count is included in total but not shown separately in current layout
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.admin_dashboard_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
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

