package com.example.unisyncpoe.ui.lecturer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.unisyncpoe.databinding.ItemStudentProgressBinding

class StudentProgressAdapter : ListAdapter<StudentProgressItem, StudentProgressAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStudentProgressBinding.inflate(
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
        private val binding: ItemStudentProgressBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(progress: StudentProgressItem) {
            binding.tvStudentName.text = progress.studentName
            binding.tvLastSubmission.text = progress.lastSubmission
            binding.tvProgressPercentage.text = "${progress.progressPercentage}%"
            
            // Set progress bar
            binding.progressBar.progress = progress.progressPercentage
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<StudentProgressItem>() {
        override fun areItemsTheSame(oldItem: StudentProgressItem, newItem: StudentProgressItem): Boolean {
            return oldItem.studentId == newItem.studentId
        }

        override fun areContentsTheSame(oldItem: StudentProgressItem, newItem: StudentProgressItem): Boolean {
            return oldItem == newItem
        }
    }
}

