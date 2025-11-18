package com.example.unisyncpoe.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.unisyncpoe.databinding.ItemGradeBinding

class GradesAdapter : ListAdapter<GradeItem, GradesAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGradeBinding.inflate(
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
        private val binding: ItemGradeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(grade: GradeItem) {
            binding.tvModule.text = grade.module
            binding.tvAssessment.text = grade.assessment
            binding.tvStatus.text = grade.status
            
            if (grade.mark != null) {
                binding.tvMark.text = "${grade.mark}/${grade.maxMark}"
            } else {
                binding.tvMark.text = "-"
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<GradeItem>() {
        override fun areItemsTheSame(oldItem: GradeItem, newItem: GradeItem): Boolean {
            return oldItem.module == newItem.module && oldItem.assessment == newItem.assessment
        }

        override fun areContentsTheSame(oldItem: GradeItem, newItem: GradeItem): Boolean {
            return oldItem == newItem
        }
    }
}

