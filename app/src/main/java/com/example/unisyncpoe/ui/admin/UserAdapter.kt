package com.example.unisyncpoe.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.unisyncpoe.databinding.ItemUserBinding
import com.example.unisyncpoe.data.model.User

class UserAdapter(
    private val onEditClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit
) : ListAdapter<User, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(
        private val binding: ItemUserBinding,
        private val onEditClick: (User) -> Unit,
        private val onDeleteClick: (User) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.apply {
                tvUserName.text = user.name
                tvUserEmail.text = user.email
                tvUserRole.text = user.role.name.replace("_", " ")
                
                // Display ID based on role
                val idText = when (user.role) {
                    com.example.unisyncpoe.data.model.UserRole.STUDENT -> 
                        user.studentId ?: "N/A"
                    com.example.unisyncpoe.data.model.UserRole.LECTURER -> 
                        user.lecturerId ?: "N/A"
                    com.example.unisyncpoe.data.model.UserRole.PROGRAM_COORDINATOR -> 
                        user.coordinatorId ?: "N/A"
                    else -> "N/A"
                }
                tvUserId.text = "ID: $idText"
                
                btnEdit.setOnClickListener { onEditClick(user) }
                btnDelete.setOnClickListener { onDeleteClick(user) }
            }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}

