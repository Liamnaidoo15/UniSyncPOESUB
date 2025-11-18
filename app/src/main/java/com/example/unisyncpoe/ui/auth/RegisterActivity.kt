package com.example.unisyncpoe.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.unisyncpoe.R
import com.example.unisyncpoe.databinding.ActivityRegisterBinding
import com.example.unisyncpoe.data.model.UserRole
import com.example.unisyncpoe.ui.dashboard.DashboardActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        // Setup role spinner
        val roles = arrayOf("Student", "Lecturer")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRole.adapter = adapter
        
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            val name = binding.etName.text.toString().trim()
            val rolePosition = binding.spinnerRole.selectedItemPosition
            val studentId = binding.etStudentId.text.toString().trim()
            val lecturerId = binding.etLecturerId.text.toString().trim()
            
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
            
            val role = if (rolePosition == 0) UserRole.STUDENT else UserRole.LECTURER
            
            viewModel.register(
                email = email,
                password = password,
                name = name,
                role = role,
                studentId = if (role == UserRole.STUDENT && studentId.isNotEmpty()) studentId else null,
                lecturerId = if (role == UserRole.LECTURER && lecturerId.isNotEmpty()) lecturerId else null
            )
        }
        
        binding.tvLogin.setOnClickListener {
            finish()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is AuthUiState.Loading -> {
                        binding.btnRegister.isEnabled = false
                        binding.progressBar.visibility = android.view.View.VISIBLE
                    }
                    is AuthUiState.Success -> {
                        binding.btnRegister.isEnabled = true
                        binding.progressBar.visibility = android.view.View.GONE
                        val message = if (state.user.isSynced) {
                            "Registration successful!"
                        } else {
                            "Account created in offline mode. Data will sync when online."
                        }
                        Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@RegisterActivity, DashboardActivity::class.java))
                        finish()
                    }
                    is AuthUiState.Error -> {
                        binding.btnRegister.isEnabled = true
                        binding.progressBar.visibility = android.view.View.GONE
                        Toast.makeText(this@RegisterActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                    is AuthUiState.Idle -> {
                        binding.btnRegister.isEnabled = true
                        binding.progressBar.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }
}

