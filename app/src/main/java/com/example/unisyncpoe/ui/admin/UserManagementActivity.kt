package com.example.unisyncpoe.ui.admin

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unisyncpoe.R
import com.example.unisyncpoe.databinding.ActivityUserManagementBinding
import com.example.unisyncpoe.databinding.DialogEditUserBinding
import com.example.unisyncpoe.databinding.DialogRegisterUserBinding
import com.example.unisyncpoe.data.model.UserRole
import com.example.unisyncpoe.util.AuthManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Admin-only activity for managing users
 * Only accessible to users with ADMIN role
 */
@AndroidEntryPoint
class UserManagementActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityUserManagementBinding
    private val viewModel: UserManagementViewModel by viewModels()
    
    @Inject
    lateinit var authManager: AuthManager
    
    private lateinit var userAdapter: UserAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Check if user is admin
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
        supportActionBar?.title = "User Management"
        
        // Setup RecyclerView
        userAdapter = UserAdapter(
            onEditClick = { user -> showEditUserDialog(user) },
            onDeleteClick = { user -> showDeleteConfirmationDialog(user) }
        )
        binding.recyclerViewUsers.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewUsers.adapter = userAdapter
        
        // Setup filter spinner
        val filterOptions = arrayOf("All Roles", "Student", "Lecturer", "Program Coordinator", "Admin")
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilterRole.adapter = filterAdapter
        
        binding.spinnerFilterRole.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedRole = when (position) {
                    0 -> null // All Roles
                    1 -> UserRole.STUDENT
                    2 -> UserRole.LECTURER
                    3 -> UserRole.PROGRAM_COORDINATOR
                    4 -> UserRole.ADMIN
                    else -> null
                }
                viewModel.setFilterRole(selectedRole)
            }
            
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                viewModel.setFilterRole(null)
            }
        }
        
        // Register new user button
        binding.btnRegisterNewUser.setOnClickListener {
            showRegisterUserDialog()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.filteredUsers.collect { users ->
                userAdapter.submitList(users)
                binding.tvEmptyState.visibility = if (users.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
        }
        
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
            }
        }
        
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UserManagementUiState.Loading -> {
                        binding.progressBar.visibility = android.view.View.VISIBLE
                    }
                    is UserManagementUiState.Success -> {
                        binding.progressBar.visibility = android.view.View.GONE
                        Toast.makeText(this@UserManagementActivity, "User registered successfully!", Toast.LENGTH_SHORT).show()
                    }
                    is UserManagementUiState.UserUpdated -> {
                        binding.progressBar.visibility = android.view.View.GONE
                        Toast.makeText(this@UserManagementActivity, "User updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                    is UserManagementUiState.UserDeleted -> {
                        binding.progressBar.visibility = android.view.View.GONE
                        Toast.makeText(this@UserManagementActivity, "User deleted successfully!", Toast.LENGTH_SHORT).show()
                    }
                    is UserManagementUiState.Error -> {
                        binding.progressBar.visibility = android.view.View.GONE
                        Toast.makeText(this@UserManagementActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                    is UserManagementUiState.Idle -> {
                        binding.progressBar.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }
    
    private fun showRegisterUserDialog() {
        val dialogBinding = DialogRegisterUserBinding.inflate(LayoutInflater.from(this))
        
        // Setup role spinner
        val roles = arrayOf("Student", "Lecturer", "Program Coordinator", "Admin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerRole.adapter = adapter
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        
        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnRegister.setOnClickListener {
            val email = dialogBinding.etEmail.text.toString().trim()
            val password = dialogBinding.etPassword.text.toString()
            val confirmPassword = dialogBinding.etConfirmPassword.text.toString()
            val name = dialogBinding.etName.text.toString().trim()
            val rolePosition = dialogBinding.spinnerRole.selectedItemPosition
            val studentId = dialogBinding.etStudentId.text.toString().trim()
            val lecturerId = dialogBinding.etLecturerId.text.toString().trim()
            
            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val role = when (rolePosition) {
                0 -> UserRole.STUDENT
                1 -> UserRole.LECTURER
                2 -> UserRole.PROGRAM_COORDINATOR
                3 -> UserRole.ADMIN
                else -> UserRole.STUDENT
            }
            
            val coordinatorId = lecturerId // Reuse lecturerId field for coordinatorId
            
            viewModel.registerUser(
                email = email,
                password = password,
                name = name,
                role = role,
                studentId = if (role == UserRole.STUDENT && studentId.isNotEmpty()) studentId else null,
                lecturerId = if (role == UserRole.LECTURER && lecturerId.isNotEmpty()) lecturerId else null,
                coordinatorId = if (role == UserRole.PROGRAM_COORDINATOR && coordinatorId.isNotEmpty()) coordinatorId else null
            )
            
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun showEditUserDialog(user: com.example.unisyncpoe.data.model.User) {
        val dialogBinding = DialogEditUserBinding.inflate(LayoutInflater.from(this))
        
        // Pre-fill fields
        dialogBinding.etName.setText(user.name)
        dialogBinding.etEmail.setText(user.email)
        dialogBinding.etStudentId.setText(user.studentId ?: "")
        dialogBinding.etLecturerId.setText(user.lecturerId ?: user.coordinatorId ?: "")
        
        // Setup role spinner
        val roles = arrayOf("Student", "Lecturer", "Program Coordinator", "Admin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerRole.adapter = adapter
        
        // Set selected role
        val roleIndex = when (user.role) {
            UserRole.STUDENT -> 0
            UserRole.LECTURER -> 1
            UserRole.PROGRAM_COORDINATOR -> 2
            UserRole.ADMIN -> 3
        }
        dialogBinding.spinnerRole.setSelection(roleIndex)
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        
        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.etName.text.toString().trim()
            val rolePosition = dialogBinding.spinnerRole.selectedItemPosition
            val studentId = dialogBinding.etStudentId.text.toString().trim()
            val lecturerId = dialogBinding.etLecturerId.text.toString().trim()
            
            if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val role = when (rolePosition) {
                0 -> UserRole.STUDENT
                1 -> UserRole.LECTURER
                2 -> UserRole.PROGRAM_COORDINATOR
                3 -> UserRole.ADMIN
                else -> user.role
            }
            
            val coordinatorId = if (role == UserRole.PROGRAM_COORDINATOR && lecturerId.isNotEmpty()) lecturerId else null
            
            val updatedUser = user.copy(
                name = name,
                role = role,
                studentId = if (role == UserRole.STUDENT && studentId.isNotEmpty()) studentId else null,
                lecturerId = if (role == UserRole.LECTURER && lecturerId.isNotEmpty()) lecturerId else null,
                coordinatorId = coordinatorId
            )
            
            viewModel.updateUser(updatedUser)
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun showDeleteConfirmationDialog(user: com.example.unisyncpoe.data.model.User) {
        AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete ${user.name} (${user.email})? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteUser(user.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
