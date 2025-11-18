package com.example.unisyncpoe.ui.coordinator

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
import com.example.unisyncpoe.databinding.ActivityCoordinatorDashboardBinding
import com.example.unisyncpoe.ui.auth.AuthViewModel
import com.example.unisyncpoe.ui.auth.LoginActivity
import com.example.unisyncpoe.ui.settings.SettingsActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Program Coordinator Dashboard Activity
 * Displays coordinator-specific features and statistics
 */
@AndroidEntryPoint
class ProgramCoordinatorDashboardActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "CoordinatorDashboard"
    }
    
    private lateinit var binding: ActivityCoordinatorDashboardBinding
    private val viewModel: CoordinatorDashboardViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoordinatorDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        Log.d(TAG, "ProgramCoordinatorDashboardActivity created")
        
        setupToolbar()
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.coordinator_dashboard)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
    
    private fun setupClickListeners() {
        // Manage Modules
        binding.cardManageModules.setOnClickListener {
            startActivity(Intent(this, ModuleManagementActivity::class.java))
        }
        
        // Allocate Lecturers
        binding.cardAllocateLecturers.setOnClickListener {
            startActivity(Intent(this, AssignLecturerActivity::class.java))
        }
        
        // Monitor Activity
        binding.cardMonitorActivity.setOnClickListener {
            startActivity(Intent(this, MonitorActivityActivity::class.java))
        }
        
        // Approve Content
        binding.cardApproveContent.setOnClickListener {
            startActivity(Intent(this, ApprovalsActivity::class.java))
        }
        
        // Generate Reports
        binding.cardGenerateReports.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
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
        menuInflater.inflate(R.menu.coordinator_dashboard_menu, menu)
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

