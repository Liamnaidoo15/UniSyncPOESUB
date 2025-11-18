package com.example.unisyncpoe.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.unisyncpoe.databinding.ItemResourceBinding
import java.text.SimpleDateFormat
import java.util.*

class ResourceAdapter : ListAdapter<ResourceItem, ResourceAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemResourceBinding.inflate(
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
        private val binding: ItemResourceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun bind(resource: ResourceItem) {
            binding.tvTitle.text = resource.title
            binding.tvDescription.text = resource.description
            binding.tvModuleCode.text = resource.moduleCode ?: "General"
            binding.tvUploadedBy.text = "Uploaded by: ${resource.uploadedByName}"
            binding.tvUploadDate.text = dateFormat.format(Date(resource.uploadedAt))

            // Set a placeholder icon (in a real app, you'd load an image from fileUrl)
            binding.ivResourceImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ResourceItem>() {
        override fun areItemsTheSame(oldItem: ResourceItem, newItem: ResourceItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ResourceItem, newItem: ResourceItem): Boolean {
            return oldItem == newItem
        }
    }
}

