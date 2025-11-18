package com.example.unisyncpoe.ui.lecturer

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
import com.example.unisyncpoe.databinding.ActivityUploadResourcesBinding
import com.example.unisyncpoe.data.model.ApprovalType
import com.example.unisyncpoe.data.model.PendingApproval
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Lecturer activity for uploading learning materials and resources
 */
@AndroidEntryPoint
class UploadResourcesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadResourcesBinding
    private val viewModel: UploadResourcesViewModel by viewModels()
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
        binding = ActivityUploadResourcesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
        observeViewModel()
        loadModules()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.upload_resources)
    }

    private fun setupClickListeners() {
        binding.btnSelectFile.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }

        binding.btnUpload.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val selectedModule = viewModel.modules.value.getOrNull(binding.spinnerModule.selectedItemPosition)

            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedModule == null) {
                Toast.makeText(this, "Please select a module", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedFileUri == null) {
                Toast.makeText(this, "Please select a file", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.uploadResource(
                title = title,
                description = description,
                moduleId = selectedModule.id,
                moduleCode = selectedModule.code,
                fileUri = selectedFileUri!!
            )
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UploadResourcesUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnUpload.isEnabled = false
                    }
                    is UploadResourcesUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnUpload.isEnabled = true
                        Toast.makeText(this@UploadResourcesActivity, state.message, Toast.LENGTH_SHORT).show()
                        // Clear form
                        binding.etTitle.text?.clear()
                        binding.etDescription.text?.clear()
                        binding.tvSelectedFile.text = getString(R.string.no_file_selected)
                        binding.btnSelectFile.text = getString(R.string.select_file)
                        selectedFileUri = null
                    }
                    is UploadResourcesUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnUpload.isEnabled = true
                        Toast.makeText(this@UploadResourcesActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                    is UploadResourcesUiState.Idle -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnUpload.isEnabled = true
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.modules.collect { modules ->
                val moduleNames = modules.map { "${it.code} - ${it.name}" }
                val adapter = ArrayAdapter(this@UploadResourcesActivity, android.R.layout.simple_spinner_item, moduleNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerModule.adapter = adapter
            }
        }
    }

    private fun loadModules() {
        viewModel.loadModules()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

