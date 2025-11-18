package com.example.unisyncpoe.ui.coordinator

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.unisyncpoe.R
import com.example.unisyncpoe.databinding.ActivityApprovalsBinding
import com.example.unisyncpoe.data.model.PendingApproval
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Coordinator activity for approving/rejecting pending uploads
 */
@AndroidEntryPoint
class ApprovalsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityApprovalsBinding
    private val viewModel: ApprovalsViewModel by viewModels()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApprovalsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        observeViewModel()
        initializeDummyApprovals()
    }
    
    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.approve_content)
        
        binding.listViewApprovals.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectApproval(position)
            showApprovalDetails(viewModel.getSelectedApproval())
        }
        
        binding.btnRefresh.setOnClickListener {
            viewModel.loadApprovals()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.approvals.collect { approvals ->
                updateApprovalsList(approvals)
            }
        }
    }
    
    private fun updateApprovalsList(approvals: List<PendingApproval>) {
        val approvalStrings = approvals.map { approval ->
            val date = dateFormat.format(Date(approval.uploadedAt))
            "[$date] ${approval.type.name}: ${approval.title} - ${approval.uploadedByName}"
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, approvalStrings)
        binding.listViewApprovals.adapter = adapter
    }
    
    private fun showApprovalDetails(approval: PendingApproval?) {
        if (approval == null) return
        
        val date = dateFormat.format(Date(approval.uploadedAt))
        val message = """
            Type: ${approval.type.name}
            Title: ${approval.title}
            Description: ${approval.description}
            Uploaded by: ${approval.uploadedByName}
            Date: $date
            Module: ${approval.moduleCode ?: "N/A"}
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle("Approval Details")
            .setMessage(message)
            .setPositiveButton("Approve") { _, _ ->
                viewModel.approve(approval.id)
            }
            .setNegativeButton("Reject") { _, _ ->
                showRejectDialog(approval)
            }
            .setNeutralButton("Cancel", null)
            .show()
    }
    
    private fun showRejectDialog(approval: PendingApproval) {
        val input = android.widget.EditText(this)
        input.hint = "Rejection reason (optional)"
        
        AlertDialog.Builder(this)
            .setTitle("Reject Approval")
            .setMessage("Are you sure you want to reject this ${approval.type.name.lowercase()}?")
            .setView(input)
            .setPositiveButton("Reject") { _, _ ->
                val reason = input.text.toString().trim()
                viewModel.reject(approval.id, reason)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun initializeDummyApprovals() {
        viewModel.initializeDummyApprovals()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

