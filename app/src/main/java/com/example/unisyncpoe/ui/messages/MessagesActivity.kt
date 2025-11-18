package com.example.unisyncpoe.ui.messages

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unisyncpoe.R
import com.example.unisyncpoe.databinding.ActivityMessagesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Activity for viewing conversation threads/messages inbox
 */
@AndroidEntryPoint
class MessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessagesBinding
    private val viewModel: MessagesViewModel by viewModels()
    private lateinit var conversationsAdapter: ConversationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        loadConversations()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh conversations when returning to this screen
        loadConversations()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.messages)
    }

    private fun setupRecyclerView() {
        conversationsAdapter = ConversationsAdapter(
            onConversationClick = { conversation ->
                val intent = Intent(this, ConversationActivity::class.java).apply {
                    putExtra("otherUserId", conversation.otherUserId)
                    putExtra("otherUserName", conversation.otherUserName)
                }
                startActivity(intent)
            }
        )
        binding.recyclerViewConversations.apply {
            layoutManager = LinearLayoutManager(this@MessagesActivity)
            adapter = conversationsAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fabNewMessage.setOnClickListener {
            startActivity(Intent(this, SendMessageActivity::class.java))
        }

        binding.btnRefresh.setOnClickListener {
            loadConversations()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.conversations.collect { conversations ->
                conversationsAdapter.submitList(conversations)
                binding.tvNoConversations.visibility = if (conversations.isEmpty()) View.VISIBLE else View.GONE
                binding.tvNoConversations.text = getString(R.string.no_conversations)
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun loadConversations() {
        viewModel.loadConversations()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

