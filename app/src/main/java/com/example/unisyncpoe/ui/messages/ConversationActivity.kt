package com.example.unisyncpoe.ui.messages

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unisyncpoe.R
import com.example.unisyncpoe.databinding.ActivityConversationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Activity for viewing conversation history between two users
 */
@AndroidEntryPoint
class ConversationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConversationBinding
    private val viewModel: ConversationViewModel by viewModels()
    private lateinit var messagesAdapter: MessagesAdapter

    private var otherUserId: String? = null
    private var otherUserName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConversationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        otherUserId = intent.getStringExtra("otherUserId")
        otherUserName = intent.getStringExtra("otherUserName")

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        loadMessages()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh messages when returning to this screen
        otherUserId?.let { viewModel.loadMessages(it) }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = otherUserName ?: getString(R.string.conversation)
    }

    private fun setupRecyclerView() {
        val currentUserId = viewModel.getCurrentUserId()
        messagesAdapter = MessagesAdapter(currentUserId)
        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(this@ConversationActivity).apply {
                stackFromEnd = true
            }
            adapter = messagesAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fabNewMessage.setOnClickListener {
            val intent = Intent(this, SendMessageActivity::class.java).apply {
                otherUserId?.let { putExtra("toUserId", it) }
                otherUserName?.let { putExtra("toUserName", it) }
            }
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.messages.collect { messages ->
                messagesAdapter.submitList(messages)
                binding.tvNoMessages.visibility = if (messages.isEmpty()) View.VISIBLE else View.GONE
                binding.tvNoMessages.text = getString(R.string.no_messages)
                // Scroll to bottom
                if (messages.isNotEmpty()) {
                    binding.recyclerViewMessages.post {
                        binding.recyclerViewMessages.smoothScrollToPosition(messages.size - 1)
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

    private fun loadMessages() {
        otherUserId?.let { viewModel.loadMessages(it) }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

