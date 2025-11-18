package com.example.unisyncpoe.ui.lecturer

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.unisyncpoe.databinding.ItemAssignmentSubmissionBinding
import java.text.SimpleDateFormat
import java.util.*

class AssignmentSubmissionAdapter(
    private val onScoreChanged: (String, Int) -> Unit
) : ListAdapter<AssignmentSubmission, AssignmentSubmissionAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAssignmentSubmissionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemAssignmentSubmissionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        fun bind(submission: AssignmentSubmission) {
            binding.tvStudentName.text = submission.studentName
            binding.tvAssignmentTitle.text = submission.assignmentTitle
            binding.tvCourseName.text = submission.courseName
            binding.tvMaxScore.text = "/ ${submission.maxScore}"

            submission.submittedAt?.let {
                binding.tvSubmittedDate.text = dateFormat.format(Date(it))
            } ?: run {
                binding.tvSubmittedDate.text = "Not submitted"
            }

            submission.currentScore?.let {
                binding.etScore.setText(it.toString())
            } ?: run {
                binding.etScore.setText("")
            }

            binding.etScore.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val scoreText = binding.etScore.text.toString()
                    val score = scoreText.toIntOrNull()
                    if (score != null && score >= 0 && score <= submission.maxScore) {
                        onScoreChanged(submission.id, score)
                    } else if (scoreText.isNotEmpty()) {
                        Toast.makeText(
                            binding.root.context,
                            "Score must be between 0 and ${submission.maxScore}",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.etScore.setText(submission.currentScore?.toString() ?: "")
                    }
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AssignmentSubmission>() {
        override fun areItemsTheSame(oldItem: AssignmentSubmission, newItem: AssignmentSubmission): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AssignmentSubmission, newItem: AssignmentSubmission): Boolean {
            return oldItem == newItem
        }
    }
}

