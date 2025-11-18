package com.example.unisyncpoe.ui.student

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unisyncpoe.R
import com.example.unisyncpoe.databinding.ActivityAnnouncementsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Student activity for viewing announcements
 */
@AndroidEntryPoint
class AnnouncementsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnnouncementsBinding
    private val viewModel: AnnouncementsViewModel by viewModels()
    private lateinit var announcementsAdapter: AnnouncementsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnnouncementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        loadAnnouncements()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.announcements)
    }

    private fun setupRecyclerView() {
        announcementsAdapter = AnnouncementsAdapter()
        binding.recyclerViewAnnouncements.apply {
            layoutManager = LinearLayoutManager(this@AnnouncementsActivity)
            adapter = announcementsAdapter
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.announcements.collect { announcements ->
                announcementsAdapter.submitList(announcements)
                binding.tvNoAnnouncements.visibility = if (announcements.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun loadAnnouncements() {
        viewModel.loadAnnouncements()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

