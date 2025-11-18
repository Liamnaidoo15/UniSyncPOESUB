package com.example.unisyncpoe.ui.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.unisyncpoe.data.local.dao.UserDao
import com.example.unisyncpoe.data.model.Message
import com.example.unisyncpoe.data.model.UserRole
import com.example.unisyncpoe.data.repository.MessageRepository
import com.example.unisyncpoe.util.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SendMessageViewModel @Inject constructor(
    private val userDao: UserDao,
    private val messageRepository: MessageRepository,
    private val authManager: AuthManager
) : ViewModel() {
    
    companion object {
        private const val TAG = "SendMessageViewModel"
    }

    private val _recipients = MutableStateFlow<List<com.example.unisyncpoe.data.model.User>>(emptyList())
    val recipients: StateFlow<List<com.example.unisyncpoe.data.model.User>> = _recipients.asStateFlow()

    private val _preSelectedUserId = MutableStateFlow<String?>(null)
    val preSelectedUserId: StateFlow<String?> = _preSelectedUserId.asStateFlow()

    private val _uiState = MutableStateFlow<SendMessageUiState>(SendMessageUiState.Idle)
    val uiState: StateFlow<SendMessageUiState> = _uiState.asStateFlow()

    fun loadRecipients() {
        viewModelScope.launch {
            val currentUserRole = authManager.getUserRole()
            userDao.getAllUsers().collect { users ->
                val recipientUsers = when (currentUserRole) {
                    "STUDENT" -> users.filter { it.role == UserRole.LECTURER }
                    "LECTURER" -> users.filter { it.role == UserRole.STUDENT }
                    else -> emptyList()
                }
                _recipients.value = recipientUsers
            }
        }
    }

    fun setPreSelectedUser(userId: String?) {
        _preSelectedUserId.value = userId
    }

    fun sendMessage(
        toUserId: String,
        toUserName: String,
        subject: String,
        content: String
    ) {
        viewModelScope.launch {
            _uiState.value = SendMessageUiState.Loading
            try {
                val fromUserId = authManager.getUserId() ?: return@launch
                val fromUser = userDao.getUserById(fromUserId)
                val fromUserName = fromUser?.name ?: "Student"

                val message = Message(
                    id = "message_${fromUserId}_${toUserId}_${System.currentTimeMillis()}",
                    fromUserId = fromUserId,
                    fromUserName = fromUserName,
                    toUserId = toUserId,
                    toUserName = toUserName,
                    subject = subject,
                    content = content,
                    sentAt = System.currentTimeMillis(),
                    isRead = false,
                    isSynced = false // Will be set to true after Firestore save
                )

                // Save message (handles both local DB and Firestore sync)
                messageRepository.saveMessage(message)
                Log.d(TAG, "Message saved via repository: ${message.id}")
                
                _uiState.value = SendMessageUiState.Success("Message sent successfully!")
            } catch (e: Exception) {
                _uiState.value = SendMessageUiState.Error("Failed to send message: ${e.message}")
            }
        }
    }
}

sealed class SendMessageUiState {
    object Idle : SendMessageUiState()
    object Loading : SendMessageUiState()
    data class Success(val message: String) : SendMessageUiState()
    data class Error(val message: String) : SendMessageUiState()
}

