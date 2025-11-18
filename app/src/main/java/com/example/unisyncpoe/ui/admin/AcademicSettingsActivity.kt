package com.example.unisyncpoe.ui.admin

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.unisyncpoe.R
import com.example.unisyncpoe.databinding.ActivityAcademicSettingsBinding
import com.example.unisyncpoe.data.model.UserRole
import com.example.unisyncpoe.util.AuthManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Admin activity for configuring academic settings
 */
@AndroidEntryPoint
class AcademicSettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAcademicSettingsBinding
    private val viewModel: AcademicSettingsViewModel by viewModels()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    @Inject
    lateinit var authManager: AuthManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAcademicSettingsBinding.inflate(layoutInflater)
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
        supportActionBar?.title = getString(R.string.academic_settings)
        
        // Academic Year Section
        binding.btnAddAcademicYear.setOnClickListener {
            val year = binding.etAcademicYear.text.toString().trim()
            if (year.isEmpty()) {
                Toast.makeText(this, "Please enter academic year", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.addAcademicYear(year)
        }
        
        // Semester Section - Start Date
        binding.etSemesterStartDate.setOnClickListener {
            showDatePicker { date ->
                binding.etSemesterStartDate.setText(dateFormat.format(date))
                viewModel.setSemesterStartDate(date.time)
            }
        }
        
        // Semester Section - End Date
        binding.etSemesterEndDate.setOnClickListener {
            showDatePicker { date ->
                binding.etSemesterEndDate.setText(dateFormat.format(date))
                viewModel.setSemesterEndDate(date.time)
            }
        }
        
        // Semester Section - Exam Week Start
        binding.etExamWeekStart.setOnClickListener {
            showDatePicker { date ->
                binding.etExamWeekStart.setText(dateFormat.format(date))
                viewModel.setExamWeekStart(date.time)
            }
        }
        
        // Semester Section - Exam Week End
        binding.etExamWeekEnd.setOnClickListener {
            showDatePicker { date ->
                binding.etExamWeekEnd.setText(dateFormat.format(date))
                viewModel.setExamWeekEnd(date.time)
            }
        }
        
        binding.btnSaveSemester.setOnClickListener {
            val semesterName = binding.etSemesterName.text.toString().trim()
            if (semesterName.isEmpty()) {
                Toast.makeText(this, "Please enter semester name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.saveSemester(semesterName)
        }
        
        // Module Section
        binding.btnAddModule.setOnClickListener {
            val code = binding.etModuleCode.text.toString().trim()
            val name = binding.etModuleName.text.toString().trim()
            if (code.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please fill all module fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val credits = binding.etModuleCredits.text.toString().toIntOrNull() ?: 0
            viewModel.addModule(code, name, credits)
        }
    }
    
    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                onDateSelected(selectedDate.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is AcademicSettingsUiState.Loading -> {
                        binding.progressBar.visibility = android.view.View.VISIBLE
                    }
                    is AcademicSettingsUiState.Success -> {
                        binding.progressBar.visibility = android.view.View.GONE
                        Toast.makeText(this@AcademicSettingsActivity, state.message, Toast.LENGTH_SHORT).show()
                        // Clear form if needed
                        if (state.clearForm) {
                            clearForm()
                        }
                    }
                    is AcademicSettingsUiState.Error -> {
                        binding.progressBar.visibility = android.view.View.GONE
                        Toast.makeText(this@AcademicSettingsActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                    is AcademicSettingsUiState.Idle -> {
                        binding.progressBar.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }
    
    private fun clearForm() {
        binding.etAcademicYear.text?.clear()
        binding.etSemesterName.text?.clear()
        binding.etSemesterStartDate.text?.clear()
        binding.etSemesterEndDate.text?.clear()
        binding.etExamWeekStart.text?.clear()
        binding.etExamWeekEnd.text?.clear()
        binding.etModuleCode.text?.clear()
        binding.etModuleName.text?.clear()
        binding.etModuleCredits.text?.clear()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

