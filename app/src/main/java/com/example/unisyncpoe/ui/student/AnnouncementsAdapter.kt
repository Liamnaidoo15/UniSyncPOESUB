package com.example.unisyncpoe.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.unisyncpoe.databinding.ItemAnnouncementBinding
import com.example.unisyncpoe.data.model.Announcement
import java.text.SimpleDateFormat
import java.util.*

class AnnouncementsAdapter : ListAdapter<Announcement, AnnouncementsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAnnouncementBinding.inflate(
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
        private val binding: ItemAnnouncementBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        fun bind(announcement: Announcement) {
            binding.tvTitle.text = announcement.title
            binding.tvContent.text = announcement.content
            binding.tvAuthor.text = announcement.authorName
            binding.tvCourseName.text = announcement.courseName ?: "General"
            binding.tvDate.text = dateFormat.format(Date(announcement.createdAt))

            // Set priority indicator color
            val priorityColor = when (announcement.priority) {
                com.example.unisyncpoe.data.model.AnnouncementPriority.URGENT -> android.graphics.Color.RED
                com.example.unisyncpoe.data.model.AnnouncementPriority.HIGH -> android.graphics.Color.parseColor("#FF9800")
                com.example.unisyncpoe.data.model.AnnouncementPriority.NORMAL -> android.graphics.Color.parseColor("#2196F3")
                com.example.unisyncpoe.data.model.AnnouncementPriority.LOW -> android.graphics.Color.parseColor("#4CAF50")
            }
            binding.viewPriorityIndicator.setBackgroundColor(priorityColor)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Announcement>() {
        override fun areItemsTheSame(oldItem: Announcement, newItem: Announcement): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Announcement, newItem: Announcement): Boolean {
            return oldItem == newItem
        }
    }
}

