package com.example.unisyncpoe.ui.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unisyncpoe.data.repository.MessageRepository
import com.example.unisyncpoe.util.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Conversation(
    val otherUserId: String,
    val otherUserName: String,
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadCount: Int
)

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val messageRepository: com.example.unisyncpoe.data.repository.MessageRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private var isCollecting = false
    private var collectionJob: kotlinx.coroutines.Job? = null

    private fun startMessageCollection(userId: String) {
        // Cancel existing collection if any
        collectionJob?.cancel()
        
        if (isCollecting) return
        isCollecting = true
        
        collectionJob = viewModelScope.launch {
            try {
                // Collect messages continuously (Flow will emit updates automatically)
                messageRepository.getMessagesForUser(userId).collect { messages ->
                    updateConversations(messages, userId)
                }
            } catch (e: Exception) {
                android.util.Log.e("MessagesViewModel", "Error collecting messages: ${e.message}", e)
                isCollecting = false
            }
        }
    }
    
    private fun updateConversations(messages: List<com.example.unisyncpoe.data.model.Message>, userId: String) {
        android.util.Log.d("MessagesViewModel", "Updating conversations with ${messages.size} messages")
        
        // Group messages by other user
        val conversationMap = mutableMapOf<String, Conversation>()
        
        messages.forEach { message ->
            val otherUserId = if (message.fromUserId == userId) {
                message.toUserId
            } else {
                message.fromUserId
            }
            
            val otherUserName = if (message.fromUserId == userId) {
                message.toUserName
            } else {
                message.fromUserName
            }
            
            val existing = conversationMap[otherUserId]
            if (existing == null || message.sentAt > existing.lastMessageTime) {
                conversationMap[otherUserId] = Conversation(
                    otherUserId = otherUserId,
                    otherUserName = otherUserName,
                    lastMessage = message.subject + ": " + message.content.take(50),
                    lastMessageTime = message.sentAt,
                    unreadCount = 0 // Will be calculated below
                )
            }
        }
        
        // Calculate unread counts for each conversation
        conversationMap.forEach { (otherUserId, conversation) ->
            val unreadCount = messages.count { 
                (it.fromUserId == otherUserId || it.toUserId == otherUserId) && 
                it.toUserId == userId && !it.isRead 
            }
            conversationMap[otherUserId] = conversation.copy(unreadCount = unreadCount)
        }
        
        _conversations.value = conversationMap.values.sortedByDescending { it.lastMessageTime }
    }

    fun loadConversations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = authManager.getUserId() ?: return@launch
                
                // Start message collection if not already started
                if (!isCollecting) {
                    startMessageCollection(userId)
                }
                
                // Sync messages from Firestore (this will trigger Flow update)
                messageRepository.syncMessages(userId)
                
                _isLoading.value = false
            } catch (e: Exception) {
                android.util.Log.e("MessagesViewModel", "Error loading conversations: ${e.message}", e)
                _isLoading.value = false
            }
        }
    }
}

