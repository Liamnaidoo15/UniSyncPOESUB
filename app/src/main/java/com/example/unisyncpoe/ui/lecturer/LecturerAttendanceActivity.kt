package com.example.unisyncpoe.ui.lecturer

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unisyncpoe.R
import com.example.unisyncpoe.databinding.ActivityLecturerAttendanceBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Lecturer activity for marking student attendance
 */
@AndroidEntryPoint
class LecturerAttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLecturerAttendanceBinding
    private val viewModel: LecturerAttendanceViewModel by viewModels()
    private lateinit var attendanceAdapter: StudentAttendanceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLecturerAttendanceBinding.inflate(layoutInflater)
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
        supportActionBar?.title = getString(R.string.lecturer_attendance)
    }

    private fun setupRecyclerView() {
        attendanceAdapter = StudentAttendanceAdapter(
            onAttendanceChanged = { studentId, isPresent ->
                viewModel.updateAttendance(studentId, isPresent)
            }
        )
        binding.recyclerViewStudents.apply {
            layoutManager = LinearLayoutManager(this@LecturerAttendanceActivity)
            adapter = attendanceAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnSubmitAttendance.setOnClickListener {
            viewModel.submitAttendance()
        }

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
            viewModel.students.collect { students ->
                attendanceAdapter.submitList(students)
                binding.tvNoStudents.visibility = if (students.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.modules.collect { modules ->
                val moduleNames = modules.map { "${it.code} - ${it.name}" }
                val adapter = ArrayAdapter(this@LecturerAttendanceActivity, android.R.layout.simple_spinner_item, moduleNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerModule.adapter = adapter
                
                // Select first module if none selected
                if (binding.spinnerModule.selectedItemPosition == -1 && modules.isNotEmpty()) {
                    binding.spinnerModule.setSelection(0)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is LecturerAttendanceUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnSubmitAttendance.isEnabled = false
                    }
                    is LecturerAttendanceUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSubmitAttendance.isEnabled = true
                        Toast.makeText(this@LecturerAttendanceActivity, state.message, Toast.LENGTH_SHORT).show()
                        loadData() // Refresh the list
                    }
                    is LecturerAttendanceUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSubmitAttendance.isEnabled = true
                        Toast.makeText(this@LecturerAttendanceActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                    is LecturerAttendanceUiState.Idle -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSubmitAttendance.isEnabled = true
                    }
                }
            }
        }
    }

    private fun loadData() {
        viewModel.loadStudents()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

