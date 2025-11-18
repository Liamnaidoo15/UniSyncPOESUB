package com.example.unisyncpoe.ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.unisyncpoe.databinding.ItemMessageBinding
import com.example.unisyncpoe.data.model.Message
import java.text.SimpleDateFormat
import java.util.*

class MessagesAdapter(
    private val currentUserId: String?
) : ListAdapter<Message, MessagesAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), currentUserId)
    }

    inner class ViewHolder(
        private val binding: ItemMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        fun bind(message: Message, currentUserId: String?) {
            val isSent = message.fromUserId == currentUserId
            
            if (isSent) {
                // Sent message - align right
                binding.cardSentMessage.visibility = View.VISIBLE
                binding.cardReceivedMessage.visibility = View.GONE
                binding.tvSentSubject.text = message.subject
                binding.tvSentContent.text = message.content
                binding.tvSentTime.text = dateFormat.format(Date(message.sentAt))
            } else {
                // Received message - align left
                binding.cardSentMessage.visibility = View.GONE
                binding.cardReceivedMessage.visibility = View.VISIBLE
                binding.tvReceivedSubject.text = message.subject
                binding.tvReceivedContent.text = message.content
                binding.tvReceivedTime.text = dateFormat.format(Date(message.sentAt))
                binding.tvReceivedFrom.text = "From: ${message.fromUserName}"
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}

