package com.example.unisyncpoe.ui.admin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.unisyncpoe.R
import com.example.unisyncpoe.databinding.ActivityAssignRolesBinding
import com.example.unisyncpoe.data.model.User
import com.example.unisyncpoe.data.model.UserRole
import com.example.unisyncpoe.util.AuthManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Admin activity for assigning roles to users
 */
@AndroidEntryPoint
class AssignRolesActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAssignRolesBinding
    private val viewModel: AssignRolesViewModel by viewModels()
    
    @Inject
    lateinit var authManager: AuthManager
    
    private lateinit var usersAdapter: ArrayAdapter<String>
    private var selectedUser: User? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssignRolesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        checkAdminAccess()
        setupUI()
        observeViewModel()
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
        supportActionBar?.title = getString(R.string.assign_roles_permissions)
        
        // Setup role spinner
        val roles = arrayOf("Student", "Lecturer", "Program Coordinator", "Admin")
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRole.adapter = roleAdapter
        
        // Setup users list
        usersAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        binding.listViewUsers.adapter = usersAdapter
        
        binding.listViewUsers.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectUser(position)
        }
        
        binding.btnAssignRole.setOnClickListener {
            val selectedPosition = binding.listViewUsers.selectedItemPosition
            val rolePosition = binding.spinnerRole.selectedItemPosition
            
            if (selectedPosition < 0) {
                Toast.makeText(this, "Please select a user", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val role = when (rolePosition) {
                0 -> UserRole.STUDENT
                1 -> UserRole.LECTURER
                2 -> UserRole.PROGRAM_COORDINATOR
                3 -> UserRole.ADMIN
                else -> UserRole.STUDENT
            }
            
            viewModel.assignRole(selectedPosition, role)
        }
        
        binding.btnRefresh.setOnClickListener {
            viewModel.loadUsers()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.users.collect { users ->
                updateUsersList(users)
            }
        }
        
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is AssignRolesUiState.Loading -> {
                        binding.progressBar.visibility = android.view.View.VISIBLE
                        binding.btnAssignRole.isEnabled = false
                    }
                    is AssignRolesUiState.Success -> {
                        binding.progressBar.visibility = android.view.View.GONE
                        binding.btnAssignRole.isEnabled = true
                        Toast.makeText(this@AssignRolesActivity, state.message, Toast.LENGTH_SHORT).show()
                        viewModel.loadUsers() // Refresh list
                    }
                    is AssignRolesUiState.Error -> {
                        binding.progressBar.visibility = android.view.View.GONE
                        binding.btnAssignRole.isEnabled = true
                        Toast.makeText(this@AssignRolesActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                    is AssignRolesUiState.Idle -> {
                        binding.progressBar.visibility = android.view.View.GONE
                        binding.btnAssignRole.isEnabled = true
                    }
                }
            }
        }
    }
    
    private fun updateUsersList(users: List<User>) {
        val userStrings = users.map { "${it.name} (${it.email}) - ${it.role.name}" }
        usersAdapter.clear()
        usersAdapter.addAll(userStrings)
        usersAdapter.notifyDataSetChanged()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

