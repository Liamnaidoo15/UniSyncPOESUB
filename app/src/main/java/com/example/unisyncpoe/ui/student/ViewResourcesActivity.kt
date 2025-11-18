package com.example.unisyncpoe.ui.student

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unisyncpoe.R
import com.example.unisyncpoe.databinding.ActivityViewResourcesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Student activity for viewing learning materials and resources
 */
@AndroidEntryPoint
class ViewResourcesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewResourcesBinding
    private val viewModel: ViewResourcesViewModel by viewModels()
    private lateinit var resourcesAdapter: ResourceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewResourcesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        loadResources()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.view_resources)
    }

    private fun setupRecyclerView() {
        resourcesAdapter = ResourceAdapter()
        binding.recyclerViewResources.apply {
            layoutManager = LinearLayoutManager(this@ViewResourcesActivity)
            adapter = resourcesAdapter
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.resources.collect { resources ->
                resourcesAdapter.submitList(resources)
                binding.tvNoResources.visibility = if (resources.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun loadResources() {
        viewModel.loadResources()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

