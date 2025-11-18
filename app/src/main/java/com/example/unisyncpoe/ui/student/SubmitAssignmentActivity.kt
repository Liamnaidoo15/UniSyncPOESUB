package com.example.unisyncpoe.ui.student

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.unisyncpoe.R
import com.example.unisyncpoe.databinding.ActivitySubmitAssignmentBinding
import com.example.unisyncpoe.data.model.SubmissionStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Student activity for submitting assignments
 */
@AndroidEntryPoint
class SubmitAssignmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubmitAssignmentBinding
    private val viewModel: SubmitAssignmentViewModel by viewModels()
    private var selectedFileUri: Uri? = null

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            binding.tvSelectedFile.text = it.lastPathSegment ?: "File selected"
            binding.btnSelectFile.text = getString(R.string.change_file)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubmitAssignmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
        observeViewModel()
        loadData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.submit_assignment)
    }

    private fun setupClickListeners() {
        binding.btnSelectFile.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }

        binding.btnSubmit.setOnClickListener {
            val selectedModule = viewModel.modules.value.getOrNull(binding.spinnerModule.selectedItemPosition)
            val comments = binding.etComments.text.toString().trim()

            if (selectedModule == null) {
                Toast.makeText(this, "Please select a module", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedFileUri == null) {
                Toast.makeText(this, "Please select a file", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.submitAssignment(
                assignmentId = viewModel.selectedAssignmentId.value ?: "",
                moduleId = selectedModule.id,
                fileUri = selectedFileUri!!,
                comments = comments
            )
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.modules.collect { modules ->
                val moduleNames = modules.map { "${it.code} - ${it.name}" }
                val adapter = ArrayAdapter(this@SubmitAssignmentActivity, android.R.layout.simple_spinner_item, moduleNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerModule.adapter = adapter
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is SubmitAssignmentUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnSubmit.isEnabled = false
                    }
                    is SubmitAssignmentUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSubmit.isEnabled = true
                        Toast.makeText(this@SubmitAssignmentActivity, state.message, Toast.LENGTH_SHORT).show()
                        // Clear form
                        binding.etComments.text?.clear()
                        binding.tvSelectedFile.text = getString(R.string.no_file_selected)
                        binding.btnSelectFile.text = getString(R.string.select_file)
                        selectedFileUri = null
                    }
                    is SubmitAssignmentUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSubmit.isEnabled = true
                        Toast.makeText(this@SubmitAssignmentActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                    is SubmitAssignmentUiState.Idle -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSubmit.isEnabled = true
                    }
                }
            }
        }
    }

    private fun loadData() {
        viewModel.loadModules()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

