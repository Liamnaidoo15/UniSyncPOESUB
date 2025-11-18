package com.example.unisyncpoe.ui.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unisyncpoe.data.repository.MessageRepository
import com.example.unisyncpoe.data.model.Message
import com.example.unisyncpoe.util.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val messageRepository: com.example.unisyncpoe.data.repository.MessageRepository,
    private val authManager: AuthManager,
    private val userDao: com.example.unisyncpoe.data.local.dao.UserDao
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private var currentOtherUserId: String? = null
    private var isCollecting = false
    private var collectionJob: kotlinx.coroutines.Job? = null

    fun getCurrentUserId(): String? {
        return authManager.getUserId()
    }

    fun loadMessages(otherUserId: String) {
        // If already collecting for this conversation, just sync
        if (isCollecting && currentOtherUserId == otherUserId) {
            viewModelScope.launch {
                val userId = authManager.getUserId() ?: return@launch
                messageRepository.syncConversationMessages(userId, otherUserId)
            }
            return
        }
        
        // Cancel existing collection if switching conversations
        collectionJob?.cancel()
        isCollecting = false
        
        // Start new collection
        currentOtherUserId = otherUserId
        isCollecting = true
        
        collectionJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = authManager.getUserId() ?: return@launch
                
                // Sync conversation messages from Firestore first
                messageRepository.syncConversationMessages(userId, otherUserId)
                
                // Collect messages continuously (Flow will emit updates automatically)
                messageRepository.getConversationMessages(userId, otherUserId).collect { conversationMessages ->
                    android.util.Log.d("ConversationViewModel", "Received ${conversationMessages.size} messages in Flow")
                    
                    // Mark messages as read
                    conversationMessages.forEach { message ->
                        if (message.toUserId == userId && !message.isRead) {
                            messageRepository.markAsRead(message.id)
                        }
                    }
                    
                    _messages.value = conversationMessages
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                android.util.Log.e("ConversationViewModel", "Error loading messages: ${e.message}", e)
                _isLoading.value = false
                isCollecting = false
            }
        }
    }
    
    /**
     * Send a new message in the conversation
     */
    fun sendMessage(otherUserId: String, otherUserName: String, subject: String, content: String) {
        viewModelScope.launch {
            try {
                val userId = authManager.getUserId() ?: return@launch
                val fromUser = userDao.getUserById(userId)
                val fromUserName = fromUser?.name ?: "User"
                
                val message = Message(
                    id = "message_${userId}_${otherUserId}_${System.currentTimeMillis()}",
                    fromUserId = userId,
                    fromUserName = fromUserName,
                    toUserId = otherUserId,
                    toUserName = otherUserName,
                    subject = subject,
                    content = content,
                    sentAt = System.currentTimeMillis(),
                    isRead = false,
                    isSynced = false
                )
                
                messageRepository.saveMessage(message)
            } catch (e: Exception) {
                android.util.Log.e("ConversationViewModel", "Error sending message: ${e.message}", e)
            }
        }
    }
}

