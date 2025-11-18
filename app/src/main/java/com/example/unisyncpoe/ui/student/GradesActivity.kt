package com.example.unisyncpoe.ui.student

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unisyncpoe.R
import com.example.unisyncpoe.databinding.ActivityGradesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Student activity for viewing grades
 */
@AndroidEntryPoint
class GradesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGradesBinding
    private val viewModel: GradesViewModel by viewModels()
    private lateinit var gradesAdapter: GradesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGradesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        loadGrades()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.grades)
    }

    private fun setupRecyclerView() {
        gradesAdapter = GradesAdapter()
        binding.recyclerViewGrades.apply {
            layoutManager = LinearLayoutManager(this@GradesActivity)
            adapter = gradesAdapter
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.grades.collect { grades ->
                gradesAdapter.submitList(grades)
                binding.tvNoGrades.visibility = if (grades.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun loadGrades() {
        viewModel.loadGrades()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

