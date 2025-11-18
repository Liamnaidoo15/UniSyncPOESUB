package com.example.unisyncpoe.ui.admin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.unisyncpoe.R
import com.example.unisyncpoe.databinding.ActivitySecurityLogsBinding
import com.example.unisyncpoe.data.model.UserRole
import com.example.unisyncpoe.util.AuthManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Admin activity for viewing system logs
 */
@AndroidEntryPoint
class SecurityLogsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySecurityLogsBinding
    private val viewModel: SecurityLogsViewModel by viewModels()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    @Inject
    lateinit var authManager: AuthManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecurityLogsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        checkAdminAccess()
        setupUI()
        observeViewModel()
        initializeDummyLogs()
    }
    
    private fun checkAdminAccess() {
        val userRole = authManager.getUserRole()
        if (userRole != UserRole.ADMIN.name) {
            Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
    }
    
    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.view_system_logs)
        
        binding.btnRefresh.setOnClickListener {
            viewModel.loadLogs()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.logs.collect { logs ->
                updateLogsList(logs)
            }
        }
    }
    
    private fun updateLogsList(logs: List<com.example.unisyncpoe.data.model.SystemLog>) {
        val logStrings = logs.map { log ->
            val timestamp = dateFormat.format(Date(log.timestamp))
            "[$timestamp] ${log.action}: ${log.description}"
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, logStrings)
        binding.listViewLogs.adapter = adapter
    }
    
    private fun initializeDummyLogs() {
        viewModel.initializeDummyLogs()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

