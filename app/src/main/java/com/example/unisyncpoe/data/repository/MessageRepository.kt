package com.example.unisyncpoe.data.repository

import android.util.Log
import com.example.unisyncpoe.data.local.dao.MessageDao
import com.example.unisyncpoe.data.model.Message
import com.example.unisyncpoe.data.remote.FirestoreService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing messages with Firestore sync
 */
@Singleton
class MessageRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val firestoreService: FirestoreService
) {
    companion object {
        private const val TAG = "MessageRepository"
    }
    
    /**
     * Get messages for a user (from local DB, synced with Firestore)
     */
    fun getMessagesForUser(userId: String): Flow<List<Message>> {
        return messageDao.getMessagesForUser(userId)
    }
    
    /**
     * Get conversation messages between two users
     */
    fun getConversationMessages(user1Id: String, user2Id: String): Flow<List<Message>> {
        return messageDao.getConversationMessages(user1Id, user2Id)
    }
    
    /**
     * Sync messages from Firestore to local database
     */
    suspend fun syncMessages(userId: String) {
        try {
            Log.d(TAG, "Syncing messages from Firestore for user: $userId")
            
            // Get messages from Firestore
            val firestoreResult = firestoreService.getMessagesForUser(userId)
            
            firestoreResult.fold(
                onSuccess = { firestoreMessages ->
                    // Get current local messages
                    val localMessages = messageDao.getMessagesForUser(userId).first()
                    val localMessageIds = localMessages.map { it.id }.toSet()
                    
                    // Insert or update messages from Firestore
                    firestoreMessages.forEach { firestoreMessage ->
                        if (!localMessageIds.contains(firestoreMessage.id)) {
                            // New message from Firestore, insert it
                            messageDao.insertMessage(firestoreMessage.copy(isSynced = true))
                            Log.d(TAG, "Inserted new message from Firestore: ${firestoreMessage.id}")
                        } else {
                            // Message exists, update it (in case it was modified)
                            messageDao.updateMessage(firestoreMessage.copy(isSynced = true))
                        }
                    }
                    
                    // Sync unsynced local messages to Firestore
                    syncUnsyncedMessages()
                    
                    Log.d(TAG, "Message sync completed. ${firestoreMessages.size} messages synced")
                },
                onFailure = { error ->
                    Log.e(TAG, "Error syncing messages from Firestore: ${error.message}", error)
                    // Still try to sync unsynced messages
                    syncUnsyncedMessages()
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during message sync: ${e.message}", e)
        }
    }
    
    /**
     * Sync conversation messages from Firestore
     */
    suspend fun syncConversationMessages(user1Id: String, user2Id: String) {
        try {
            Log.d(TAG, "Syncing conversation messages from Firestore")
            
            val firestoreResult = firestoreService.getConversationMessages(user1Id, user2Id)
            
            firestoreResult.fold(
                onSuccess = { firestoreMessages ->
                    // Get current local messages
                    val localMessages = messageDao.getConversationMessages(user1Id, user2Id).first()
                    val localMessageIds = localMessages.map { it.id }.toSet()
                    
                    // Insert or update messages from Firestore
                    firestoreMessages.forEach { firestoreMessage ->
                        if (!localMessageIds.contains(firestoreMessage.id)) {
                            messageDao.insertMessage(firestoreMessage.copy(isSynced = true))
                            Log.d(TAG, "Inserted conversation message from Firestore: ${firestoreMessage.id}")
                        } else {
                            messageDao.updateMessage(firestoreMessage.copy(isSynced = true))
                        }
                    }
                    
                    Log.d(TAG, "Conversation sync completed. ${firestoreMessages.size} messages synced")
                },
                onFailure = { error ->
                    Log.e(TAG, "Error syncing conversation from Firestore: ${error.message}", error)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during conversation sync: ${e.message}", e)
        }
    }
    
    /**
     * Sync unsynced local messages to Firestore
     */
    private suspend fun syncUnsyncedMessages() {
        try {
            val unsyncedMessages = messageDao.getUnsyncedMessages()
            Log.d(TAG, "Found ${unsyncedMessages.size} unsynced messages to sync")
            
            unsyncedMessages.forEach { message ->
                firestoreService.saveMessage(message).fold(
                    onSuccess = {
                        messageDao.markAsSynced(message.id)
                        Log.d(TAG, "Synced unsynced message to Firestore: ${message.id}")
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to sync message to Firestore: ${message.id}, error: ${error.message}")
                    }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception syncing unsynced messages: ${e.message}", e)
        }
    }
    
    /**
     * Save message (to local DB and Firestore)
     */
    suspend fun saveMessage(message: Message) {
        try {
            // Save to local database first (this will trigger Flow update)
            // Use insertMessage with REPLACE strategy to ensure updates work
            val messageToSave = message.copy(isSynced = false)
            messageDao.insertMessage(messageToSave)
            Log.d(TAG, "Message saved to local DB: ${message.id}, from: ${message.fromUserId}, to: ${message.toUserId}")
            
            // Save to Firestore (synchronous, but won't block UI since this is called from coroutine)
            firestoreService.saveMessage(message).fold(
                onSuccess = {
                    // Update sync status using insertMessage with REPLACE to trigger Flow update
                    messageDao.insertMessage(message.copy(isSynced = true))
                    Log.d(TAG, "Message saved and synced to Firestore: ${message.id}")
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to save message to Firestore: ${error.message}")
                    // Message is still saved locally, will sync later
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception saving message: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Update message (mark as read, etc.)
     */
    suspend fun updateMessage(message: Message) {
        // Update local database
        messageDao.updateMessage(message)
        
        // Update Firestore
        firestoreService.updateMessage(message).fold(
            onSuccess = {
                Log.d(TAG, "Message updated in Firestore: ${message.id}")
            },
            onFailure = { error ->
                Log.e(TAG, "Failed to update message in Firestore: ${error.message}")
            }
        )
    }
    
    /**
     * Mark message as read
     */
    suspend fun markAsRead(messageId: String) {
        val message = messageDao.getMessageById(messageId)
        if (message != null && !message.isRead) {
            val updatedMessage = message.copy(isRead = true)
            messageDao.updateMessage(updatedMessage)
            
            // Update in Firestore
            firestoreService.updateMessage(updatedMessage).fold(
                onSuccess = {
                    Log.d(TAG, "Message marked as read in Firestore: $messageId")
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to mark message as read in Firestore: ${error.message}")
                }
            )
        }
    }
}

