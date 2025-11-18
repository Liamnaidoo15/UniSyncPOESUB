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
import com.example.unisyncpoe.databinding.ActivityMarkAssignmentsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Lecturer activity for marking student assignment submissions
 */
@AndroidEntryPoint
class MarkAssignmentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMarkAssignmentsBinding
    private val viewModel: MarkAssignmentsViewModel by viewModels()
    private lateinit var submissionsAdapter: AssignmentSubmissionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarkAssignmentsBinding.inflate(layoutInflater)
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
        supportActionBar?.title = getString(R.string.mark_assignments)
    }

    private fun setupRecyclerView() {
        submissionsAdapter = AssignmentSubmissionAdapter(
            onScoreChanged = { submissionId, score ->
                viewModel.updateScore(submissionId, score)
            }
        )
        binding.recyclerViewSubmissions.apply {
            layoutManager = LinearLayoutManager(this@MarkAssignmentsActivity)
            adapter = submissionsAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnSubmitGrades.setOnClickListener {
            viewModel.submitGrades()
        }

        binding.btnRefresh.setOnClickListener {
            loadData()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.submissions.collect { submissions ->
                submissionsAdapter.submitList(submissions)
                binding.tvNoSubmissions.visibility = if (submissions.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is MarkAssignmentsUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnSubmitGrades.isEnabled = false
                    }
                    is MarkAssignmentsUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSubmitGrades.isEnabled = true
                        Toast.makeText(this@MarkAssignmentsActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    is MarkAssignmentsUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSubmitGrades.isEnabled = true
                        Toast.makeText(this@MarkAssignmentsActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                    is MarkAssignmentsUiState.Idle -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSubmitGrades.isEnabled = true
                    }
                }
            }
        }
    }

    private fun loadData() {
        viewModel.loadSubmissions()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

