package com.example.unisyncpoe.ui.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.unisyncpoe.databinding.ItemConversationBinding
import java.text.SimpleDateFormat
import java.util.*

class ConversationsAdapter(
    private val onConversationClick: (Conversation) -> Unit
) : ListAdapter<Conversation, ConversationsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConversationBinding.inflate(
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
        private val binding: ItemConversationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        fun bind(conversation: Conversation) {
            binding.tvUserName.text = conversation.otherUserName
            binding.tvLastMessage.text = conversation.lastMessage
            binding.tvTime.text = dateFormat.format(Date(conversation.lastMessageTime))
            
            if (conversation.unreadCount > 0) {
                binding.tvUnreadCount.text = conversation.unreadCount.toString()
                binding.tvUnreadCount.visibility = android.view.View.VISIBLE
            } else {
                binding.tvUnreadCount.visibility = android.view.View.GONE
            }

            binding.root.setOnClickListener {
                onConversationClick(conversation)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Conversation>() {
        override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem.otherUserId == newItem.otherUserId
        }

        override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem == newItem
        }
    }
}

