package com.example.unisyncpoe.data.remote

import android.util.Log
import com.example.unisyncpoe.data.model.User
import com.example.unisyncpoe.data.model.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for Firestore operations
 * Handles saving and retrieving data from Firestore
 */
@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "FirestoreService"
        private const val USERS_COLLECTION = "users"
        private const val MESSAGES_COLLECTION = "messages"
        private const val MODULES_COLLECTION = "modules"
    }
    
    /**
     * Save user to Firestore 'users' collection
     */
    suspend fun saveUser(user: User): Result<User> {
        return try {
            // Convert User to Map for Firestore (handles enum serialization)
            val userMap = mapOf(
                "id" to user.id,
                "email" to user.email,
                "name" to user.name,
                "role" to user.role.name, // Convert enum to string
                "studentId" to user.studentId,
                "lecturerId" to user.lecturerId,
                "coordinatorId" to user.coordinatorId,
                "profileImageUrl" to user.profileImageUrl,
                "createdAt" to user.createdAt,
                "lastSyncTime" to user.lastSyncTime,
                "isSynced" to user.isSynced
            )
            
            firestore.collection(USERS_COLLECTION)
                .document(user.id)
                .set(userMap)
                .await()
            
            Log.d(TAG, "User saved to Firestore: ${user.email} (ID: ${user.id})")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get user from Firestore by ID
     */
    suspend fun getUserById(userId: String): Result<User?> {
        return try {
            val document = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            if (document.exists()) {
                val data = document.data
                val user = data?.let { mapToUser(it) }
                Log.d(TAG, "User retrieved from Firestore: $userId")
                Result.success(user)
            } else {
                Log.d(TAG, "User not found in Firestore: $userId")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get user from Firestore by email
     */
    suspend fun getUserByEmail(email: String): Result<User?> {
        return try {
            val querySnapshot = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()
            
            if (!querySnapshot.isEmpty) {
                val data = querySnapshot.documents[0].data
                val user = data?.let { mapToUser(it) }
                Log.d(TAG, "User retrieved from Firestore by email: $email")
                Result.success(user)
            } else {
                Log.d(TAG, "User not found in Firestore by email: $email")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user from Firestore by email: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Convert Firestore Map to User object
     */
    private fun mapToUser(data: Map<String, Any?>): User {
        return User(
            id = data["id"] as? String ?: "",
            email = data["email"] as? String ?: "",
            name = data["name"] as? String ?: "",
            role = try {
                UserRole.valueOf(data["role"] as? String ?: "STUDENT")
            } catch (e: Exception) {
                UserRole.STUDENT
            },
            studentId = data["studentId"] as? String,
            lecturerId = data["lecturerId"] as? String,
            coordinatorId = data["coordinatorId"] as? String,
            profileImageUrl = data["profileImageUrl"] as? String,
            createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis(),
            lastSyncTime = (data["lastSyncTime"] as? Long) ?: System.currentTimeMillis(),
            isSynced = (data["isSynced"] as? Boolean) ?: true
        )
    }
    
    /**
     * Get all users from Firestore
     */
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val querySnapshot = firestore.collection(USERS_COLLECTION)
                .get()
                .await()
            
            val users = querySnapshot.documents.mapNotNull { document ->
                document.data?.let { mapToUser(it) }
            }
            
            Log.d(TAG, "Retrieved ${users.size} users from Firestore")
            Result.success(users)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all users from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update user in Firestore
     */
    suspend fun updateUser(user: User): Result<User> {
        return try {
            // Convert User to Map for Firestore
            val userMap = mapOf(
                "id" to user.id,
                "email" to user.email,
                "name" to user.name,
                "role" to user.role.name,
                "studentId" to user.studentId,
                "lecturerId" to user.lecturerId,
                "coordinatorId" to user.coordinatorId,
                "profileImageUrl" to user.profileImageUrl,
                "createdAt" to user.createdAt,
                "lastSyncTime" to user.lastSyncTime,
                "isSynced" to user.isSynced
            )
            
            firestore.collection(USERS_COLLECTION)
                .document(user.id)
                .set(userMap)
                .await()
            
            Log.d(TAG, "User updated in Firestore: ${user.email}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user in Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete user from Firestore
     */
    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .delete()
                .await()
            
            Log.d(TAG, "User deleted from Firestore: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== MESSAGES COLLECTION ====================
    
    /**
     * Save message to Firestore 'messages' collection
     */
    suspend fun saveMessage(message: com.example.unisyncpoe.data.model.Message): Result<com.example.unisyncpoe.data.model.Message> {
        return try {
            val messageMap = mapOf(
                "id" to message.id,
                "fromUserId" to message.fromUserId,
                "fromUserName" to message.fromUserName,
                "toUserId" to message.toUserId,
                "toUserName" to message.toUserName,
                "subject" to message.subject,
                "content" to message.content,
                "sentAt" to message.sentAt,
                "isRead" to message.isRead,
                "isSynced" to message.isSynced
            )
            
            firestore.collection(MESSAGES_COLLECTION)
                .document(message.id)
                .set(messageMap)
                .await()
            
            Log.d(TAG, "Message saved to Firestore: ${message.id}")
            Result.success(message)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving message to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get messages for a user from Firestore
     * Firestore doesn't support OR queries directly, so we do two separate queries
     */
    suspend fun getMessagesForUser(userId: String): Result<List<com.example.unisyncpoe.data.model.Message>> {
        return try {
            // Get messages where user is the sender
            val fromMessages = firestore.collection(MESSAGES_COLLECTION)
                .whereEqualTo("fromUserId", userId)
                .get()
                .await()
            
            // Get messages where user is the recipient
            val toMessages = firestore.collection(MESSAGES_COLLECTION)
                .whereEqualTo("toUserId", userId)
                .get()
                .await()
            
            val allMessages = mutableSetOf<String>()
            val messages = mutableListOf<com.example.unisyncpoe.data.model.Message>()
            
            fromMessages.documents.forEach { doc ->
                val data = doc.data
                data?.let {
                    val message = mapToMessage(it)
                    if (!allMessages.contains(message.id)) {
                        allMessages.add(message.id)
                        messages.add(message)
                    }
                }
            }
            
            toMessages.documents.forEach { doc ->
                val data = doc.data
                data?.let {
                    val message = mapToMessage(it)
                    if (!allMessages.contains(message.id)) {
                        allMessages.add(message.id)
                        messages.add(message)
                    }
                }
            }
            
            Log.d(TAG, "Retrieved ${messages.size} messages from Firestore for user: $userId")
            Result.success(messages.sortedByDescending { it.sentAt })
        } catch (e: Exception) {
            Log.e(TAG, "Error getting messages from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get conversation messages between two users
     */
    suspend fun getConversationMessages(user1Id: String, user2Id: String): Result<List<com.example.unisyncpoe.data.model.Message>> {
        return try {
            // Get messages where user1 sent to user2
            val messages1 = firestore.collection(MESSAGES_COLLECTION)
                .whereEqualTo("fromUserId", user1Id)
                .whereEqualTo("toUserId", user2Id)
                .get()
                .await()
            
            // Get messages where user2 sent to user1
            val messages2 = firestore.collection(MESSAGES_COLLECTION)
                .whereEqualTo("fromUserId", user2Id)
                .whereEqualTo("toUserId", user1Id)
                .get()
                .await()
            
            val allMessages = mutableListOf<com.example.unisyncpoe.data.model.Message>()
            
            messages1.documents.forEach { doc ->
                doc.data?.let { allMessages.add(mapToMessage(it)) }
            }
            
            messages2.documents.forEach { doc ->
                doc.data?.let { allMessages.add(mapToMessage(it)) }
            }
            
            Log.d(TAG, "Retrieved ${allMessages.size} conversation messages from Firestore")
            Result.success(allMessages.sortedBy { it.sentAt })
        } catch (e: Exception) {
            Log.e(TAG, "Error getting conversation messages from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update message in Firestore (e.g., mark as read)
     */
    suspend fun updateMessage(message: com.example.unisyncpoe.data.model.Message): Result<com.example.unisyncpoe.data.model.Message> {
        return try {
            val messageMap = mapOf(
                "id" to message.id,
                "fromUserId" to message.fromUserId,
                "fromUserName" to message.fromUserName,
                "toUserId" to message.toUserId,
                "toUserName" to message.toUserName,
                "subject" to message.subject,
                "content" to message.content,
                "sentAt" to message.sentAt,
                "isRead" to message.isRead,
                "isSynced" to message.isSynced
            )
            
            firestore.collection(MESSAGES_COLLECTION)
                .document(message.id)
                .set(messageMap)
                .await()
            
            Log.d(TAG, "Message updated in Firestore: ${message.id}")
            Result.success(message)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating message in Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get all messages from Firestore
     */
    suspend fun getAllMessages(): Result<List<com.example.unisyncpoe.data.model.Message>> {
        return try {
            val querySnapshot = firestore.collection(MESSAGES_COLLECTION)
                .get()
                .await()
            
            val messages = querySnapshot.documents.mapNotNull { document ->
                document.data?.let { mapToMessage(it) }
            }
            
            Log.d(TAG, "Retrieved ${messages.size} messages from Firestore")
            Result.success(messages)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all messages from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Convert Firestore Map to Message object
     */
    private fun mapToMessage(data: Map<String, Any?>): com.example.unisyncpoe.data.model.Message {
        return com.example.unisyncpoe.data.model.Message(
            id = data["id"] as? String ?: "",
            fromUserId = data["fromUserId"] as? String ?: "",
            fromUserName = data["fromUserName"] as? String ?: "",
            toUserId = data["toUserId"] as? String ?: "",
            toUserName = data["toUserName"] as? String ?: "",
            subject = data["subject"] as? String ?: "",
            content = data["content"] as? String ?: "",
            sentAt = (data["sentAt"] as? Long) ?: System.currentTimeMillis(),
            isRead = (data["isRead"] as? Boolean) ?: false,
            isSynced = (data["isSynced"] as? Boolean) ?: true
        )
    }
    
    // ==================== MODULES COLLECTION ====================
    
    /**
     * Save module to Firestore 'modules' collection
     */
    suspend fun saveModule(module: com.example.unisyncpoe.data.model.Module): Result<com.example.unisyncpoe.data.model.Module> {
        return try {
            val moduleMap = mapOf(
                "id" to module.id,
                "code" to module.code,
                "name" to module.name,
                "description" to module.description,
                "credits" to module.credits,
                "semesterId" to module.semesterId,
                "coordinatorId" to module.coordinatorId,
                "coordinatorName" to module.coordinatorName,
                "isActive" to module.isActive,
                "createdAt" to module.createdAt,
                "isSynced" to module.isSynced
            )
            
            firestore.collection(MODULES_COLLECTION)
                .document(module.id)
                .set(moduleMap)
                .await()
            
            Log.d(TAG, "Module saved to Firestore: ${module.code} (ID: ${module.id})")
            Result.success(module)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving module to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get module from Firestore by ID
     */
    suspend fun getModuleById(moduleId: String): Result<com.example.unisyncpoe.data.model.Module?> {
        return try {
            val document = firestore.collection(MODULES_COLLECTION)
                .document(moduleId)
                .get()
                .await()
            
            if (document.exists()) {
                val data = document.data
                val module = data?.let { mapToModule(it) }
                Log.d(TAG, "Module retrieved from Firestore: $moduleId")
                Result.success(module)
            } else {
                Log.d(TAG, "Module not found in Firestore: $moduleId")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting module from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get all modules from Firestore
     */
    suspend fun getAllModules(): Result<List<com.example.unisyncpoe.data.model.Module>> {
        return try {
            val querySnapshot = firestore.collection(MODULES_COLLECTION)
                .get()
                .await()
            
            val modules = querySnapshot.documents.mapNotNull { document ->
                document.data?.let { mapToModule(it) }
            }
            
            Log.d(TAG, "Retrieved ${modules.size} modules from Firestore")
            Result.success(modules)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all modules from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update module in Firestore
     */
    suspend fun updateModule(module: com.example.unisyncpoe.data.model.Module): Result<com.example.unisyncpoe.data.model.Module> {
        return try {
            val moduleMap = mapOf(
                "id" to module.id,
                "code" to module.code,
                "name" to module.name,
                "description" to module.description,
                "credits" to module.credits,
                "semesterId" to module.semesterId,
                "coordinatorId" to module.coordinatorId,
                "coordinatorName" to module.coordinatorName,
                "isActive" to module.isActive,
                "createdAt" to module.createdAt,
                "isSynced" to module.isSynced
            )
            
            firestore.collection(MODULES_COLLECTION)
                .document(module.id)
                .set(moduleMap)
                .await()
            
            Log.d(TAG, "Module updated in Firestore: ${module.code}")
            Result.success(module)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating module in Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete module from Firestore
     */
    suspend fun deleteModule(moduleId: String): Result<Unit> {
        return try {
            firestore.collection(MODULES_COLLECTION)
                .document(moduleId)
                .delete()
                .await()
            
            Log.d(TAG, "Module deleted from Firestore: $moduleId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting module from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Convert Firestore Map to Module object
     */
    private fun mapToModule(data: Map<String, Any?>): com.example.unisyncpoe.data.model.Module {
        return com.example.unisyncpoe.data.model.Module(
            id = data["id"] as? String ?: "",
            code = data["code"] as? String ?: "",
            name = data["name"] as? String ?: "",
            description = data["description"] as? String,
            credits = (data["credits"] as? Long)?.toInt() ?: (data["credits"] as? Int) ?: 0,
            semesterId = data["semesterId"] as? String,
            coordinatorId = data["coordinatorId"] as? String,
            coordinatorName = data["coordinatorName"] as? String,
            isActive = (data["isActive"] as? Boolean) ?: true,
            createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis(),
            isSynced = (data["isSynced"] as? Boolean) ?: true
        )
    }
}

