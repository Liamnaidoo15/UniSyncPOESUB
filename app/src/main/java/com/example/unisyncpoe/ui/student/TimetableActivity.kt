package com.example.unisyncpoe.ui.student

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unisyncpoe.R
import com.example.unisyncpoe.databinding.ActivityTimetableBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Student activity for viewing timetable
 */
@AndroidEntryPoint
class TimetableActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimetableBinding
    private val viewModel: TimetableViewModel by viewModels()
    private lateinit var timetableAdapter: TimetableAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimetableBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        loadTimetable()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.timetable)
    }

    private fun setupRecyclerView() {
        timetableAdapter = TimetableAdapter()
        binding.recyclerViewTimetable.apply {
            layoutManager = LinearLayoutManager(this@TimetableActivity)
            adapter = timetableAdapter
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.timetableByDay.collect { timetableMap ->
                timetableAdapter.submitTimetable(timetableMap)
                binding.tvNoTimetable.visibility = if (timetableMap.values.all { it.isEmpty() }) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun loadTimetable() {
        viewModel.loadTimetable()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

