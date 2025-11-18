package com.example.unisyncpoe.ui.coordinator

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.unisyncpoe.R
import com.example.unisyncpoe.databinding.ActivityAssignLecturerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Coordinator activity for assigning lecturers to modules
 */
@AndroidEntryPoint
class AssignLecturerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAssignLecturerBinding
    private val viewModel: AssignLecturerViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssignLecturerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.assign_lecturer)
        
        binding.btnAssign.setOnClickListener {
            val lecturerPosition = binding.spinnerLecturer.selectedItemPosition
            val modulePosition = binding.spinnerModule.selectedItemPosition
            
            if (lecturerPosition < 0 || modulePosition < 0) {
                Toast.makeText(this, "Please select both lecturer and module", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            viewModel.assignLecturer(lecturerPosition, modulePosition)
        }
        
        binding.btnRefresh.setOnClickListener {
            viewModel.loadData()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.lecturers.collect { lecturers ->
                updateLecturersSpinner(lecturers.map { "${it.name} (${it.email})" })
            }
        }
        
        lifecycleScope.launch {
            viewModel.modules.collect { modules ->
                updateModulesSpinner(modules.map { "${it.code} - ${it.name}" })
            }
        }
        
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is AssignLecturerUiState.Success -> {
                        Toast.makeText(this@AssignLecturerActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    is AssignLecturerUiState.Error -> {
                        Toast.makeText(this@AssignLecturerActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            }
        }
    }
    
    private fun updateLecturersSpinner(lecturerNames: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, lecturerNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLecturer.adapter = adapter
    }
    
    private fun updateModulesSpinner(moduleNames: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, moduleNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerModule.adapter = adapter
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

