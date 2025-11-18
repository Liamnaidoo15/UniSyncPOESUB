package com.example.unisyncpoe.ui.messages

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.unisyncpoe.R
import com.example.unisyncpoe.databinding.ActivitySendMessageBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Activity for sending private messages (works for both students and lecturers)
 */
@AndroidEntryPoint
class SendMessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySendMessageBinding
    private val viewModel: SendMessageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val preSelectedUserId = intent.getStringExtra("toUserId")
        val preSelectedUserName = intent.getStringExtra("toUserName")

        setupToolbar()
        setupClickListeners()
        observeViewModel()
        loadRecipients()
        
        if (preSelectedUserId != null) {
            viewModel.setPreSelectedUser(preSelectedUserId)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.send_message)
    }

    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            val selectedRecipient = viewModel.recipients.value.getOrNull(binding.spinnerTo.selectedItemPosition)
            val subject = binding.etSubject.text.toString().trim()
            val message = binding.etMessage.text.toString().trim()

            if (selectedRecipient == null) {
                Toast.makeText(this, "Please select a recipient", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (subject.isEmpty()) {
                Toast.makeText(this, "Please enter a subject", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (message.isEmpty()) {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.sendMessage(
                toUserId = selectedRecipient.id,
                toUserName = selectedRecipient.name,
                subject = subject,
                content = message
            )
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.recipients.collect { recipients ->
                val recipientNames = recipients.map { it.name }
                val adapter = ArrayAdapter(this@SendMessageActivity, android.R.layout.simple_spinner_item, recipientNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerTo.adapter = adapter
                
                // Pre-select user if provided
                viewModel.preSelectedUserId.value?.let { userId ->
                    val index = recipients.indexOfFirst { it.id == userId }
                    if (index >= 0) {
                        binding.spinnerTo.setSelection(index)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is SendMessageUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnSend.isEnabled = false
                    }
                    is SendMessageUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSend.isEnabled = true
                        Toast.makeText(this@SendMessageActivity, state.message, Toast.LENGTH_SHORT).show()
                        // Clear form
                        binding.etSubject.text?.clear()
                        binding.etMessage.text?.clear()
                        // Finish activity to go back and refresh
                        finish()
                    }
                    is SendMessageUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSend.isEnabled = true
                        Toast.makeText(this@SendMessageActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                    is SendMessageUiState.Idle -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSend.isEnabled = true
                    }
                }
            }
        }
    }

    private fun loadRecipients() {
        viewModel.loadRecipients()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

