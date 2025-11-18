package com.example.unisyncpoe.ui.lecturer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.unisyncpoe.databinding.ItemStudentAttendanceBinding

class StudentAttendanceAdapter(
    private val onAttendanceChanged: (String, Boolean) -> Unit
) : ListAdapter<StudentAttendanceItem, StudentAttendanceAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStudentAttendanceBinding.inflate(
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
        private val binding: ItemStudentAttendanceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(student: StudentAttendanceItem) {
            binding.tvStudentName.text = student.studentName
            binding.checkboxPresent.isChecked = student.isPresent

            binding.checkboxPresent.setOnCheckedChangeListener { _, isChecked ->
                onAttendanceChanged(student.studentId, isChecked)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<StudentAttendanceItem>() {
        override fun areItemsTheSame(oldItem: StudentAttendanceItem, newItem: StudentAttendanceItem): Boolean {
            return oldItem.studentId == newItem.studentId
        }

        override fun areContentsTheSame(oldItem: StudentAttendanceItem, newItem: StudentAttendanceItem): Boolean {
            return oldItem == newItem
        }
    }
}

