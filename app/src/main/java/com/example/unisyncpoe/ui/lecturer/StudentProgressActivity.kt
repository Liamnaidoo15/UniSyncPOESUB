package com.example.unisyncpoe.ui.lecturer

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unisyncpoe.R
import com.example.unisyncpoe.databinding.ActivityStudentProgressBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Lecturer activity for tracking student progress and performance
 */
@AndroidEntryPoint
class StudentProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentProgressBinding
    private val viewModel: StudentProgressViewModel by viewModels()
    private lateinit var progressAdapter: StudentProgressAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        loadData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.student_progress)
    }

    private fun setupRecyclerView() {
        progressAdapter = StudentProgressAdapter()
        binding.recyclerViewProgress.apply {
            layoutManager = LinearLayoutManager(this@StudentProgressActivity)
            adapter = progressAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnRefresh.setOnClickListener {
            loadData()
        }

        binding.spinnerModule.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val module = viewModel.modules.value.getOrNull(position)
                module?.let {
                    viewModel.selectModule(it.id)
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.studentProgress.collect { progress ->
                progressAdapter.submitList(progress)
                binding.tvNoProgress.visibility = if (progress.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.modules.collect { modules ->
                if (modules.isNotEmpty()) {
                    val moduleNames = modules.map { "${it.code} - ${it.name}" }
                    val adapter = ArrayAdapter(this@StudentProgressActivity, android.R.layout.simple_spinner_item, moduleNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerModule.adapter = adapter
                    
                    // Set the selected position to match the selected module
                    viewModel.selectedModuleId.value?.let { selectedId ->
                        val selectedIndex = modules.indexOfFirst { it.id == selectedId }
                        if (selectedIndex >= 0 && selectedIndex != binding.spinnerModule.selectedItemPosition) {
                            binding.spinnerModule.setSelection(selectedIndex, false)
                        }
                    }
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.selectedModuleId.collect { selectedId ->
                selectedId?.let {
                    val modules = viewModel.modules.value
                    val selectedIndex = modules.indexOfFirst { it.id == selectedId }
                    if (selectedIndex >= 0 && selectedIndex != binding.spinnerModule.selectedItemPosition) {
                        binding.spinnerModule.setSelection(selectedIndex, false)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun loadData() {
        viewModel.loadProgress()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

