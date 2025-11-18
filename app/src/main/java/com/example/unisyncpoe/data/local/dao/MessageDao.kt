package com.example.unisyncpoe.data.local.dao

import androidx.room.*
import com.example.unisyncpoe.data.model.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE fromUserId = :userId OR toUserId = :userId ORDER BY sentAt DESC")
    fun getMessagesForUser(userId: String): Flow<List<Message>>
    
    @Query("SELECT * FROM messages WHERE toUserId = :userId ORDER BY sentAt DESC")
    fun getInboxMessages(userId: String): Flow<List<Message>>
    
    @Query("SELECT * FROM messages WHERE fromUserId = :userId ORDER BY sentAt DESC")
    fun getSentMessages(userId: String): Flow<List<Message>>
    
    @Query("SELECT * FROM messages WHERE (fromUserId = :user1Id AND toUserId = :user2Id) OR (fromUserId = :user2Id AND toUserId = :user1Id) ORDER BY sentAt ASC")
    fun getConversationMessages(user1Id: String, user2Id: String): Flow<List<Message>>
    
    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessageById(id: String): Message?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<Message>)
    
    @Update
    suspend fun updateMessage(message: Message)
    
    @Delete
    suspend fun deleteMessage(message: Message)
    
    @Query("UPDATE messages SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)
    
    @Query("SELECT * FROM messages WHERE isSynced = 0")
    suspend fun getUnsyncedMessages(): List<Message>
    
    @Query("UPDATE messages SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}

