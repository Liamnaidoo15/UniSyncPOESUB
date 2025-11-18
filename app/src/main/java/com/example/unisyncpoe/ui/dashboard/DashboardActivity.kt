package com.example.unisyncpoe.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.unisyncpoe.R
import com.example.unisyncpoe.databinding.ActivityDashboardBinding
import com.example.unisyncpoe.ui.admin.AdminDashboardActivity
import com.example.unisyncpoe.ui.admin.UserManagementActivity
import com.example.unisyncpoe.ui.coordinator.ProgramCoordinatorDashboardActivity
import com.example.unisyncpoe.ui.student.StudentDashboardActivity
import com.example.unisyncpoe.ui.lecturer.LecturerDashboardActivity
import com.example.unisyncpoe.ui.auth.AuthViewModel
import com.example.unisyncpoe.ui.auth.LoginActivity
import com.example.unisyncpoe.ui.settings.SettingsActivity
import com.example.unisyncpoe.util.AuthManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    
    @Inject
    lateinit var authManager: AuthManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check user role and redirect to appropriate dashboard
        val userRole = authManager.getUserRole()
        when (userRole) {
            "ADMIN" -> {
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                finish()
                return
            }
            "PROGRAM_COORDINATOR" -> {
                startActivity(Intent(this, ProgramCoordinatorDashboardActivity::class.java))
                finish()
                return
            }
            "STUDENT" -> {
                startActivity(Intent(this, StudentDashboardActivity::class.java))
                finish()
                return
            }
            "LECTURER" -> {
                startActivity(Intent(this, LecturerDashboardActivity::class.java))
                finish()
                return
            }
        }
        
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupViewPager()
        observeViewModel()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }
    
    private fun setupViewPager() {
        val adapter = DashboardPagerAdapter(this)
        binding.viewPager.adapter = adapter
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_timetable)
                1 -> getString(R.string.tab_announcements)
                2 -> getString(R.string.tab_assignments)
                3 -> getString(R.string.tab_attendance)
                4 -> getString(R.string.tab_network)
                else -> ""
            }
        }.attach()
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.syncState.collect { state ->
                when (state) {
                    is SyncState.Syncing -> {
                        // Show sync indicator
                        Toast.makeText(this@DashboardActivity, "Syncing data...", Toast.LENGTH_SHORT).show()
                    }
                    is SyncState.Success -> {
                        // Hide sync indicator, show success message
                        Toast.makeText(
                            this@DashboardActivity,
                            "Sync completed. ${state.syncedCount} items synced.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is SyncState.Error -> {
                        // Show error message
                        Toast.makeText(this@DashboardActivity, "Sync error: ${state.message}", Toast.LENGTH_LONG).show()
                    }
                    is SyncState.Idle -> {}
                }
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        
        // Show user management only for admins
        val userRole = authManager.getUserRole()
        val userManagementItem = menu.findItem(R.id.menu_user_management)
        userManagementItem?.isVisible = userRole == "ADMIN"
        
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.menu_user_management -> {
                startActivity(Intent(this, UserManagementActivity::class.java))
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

