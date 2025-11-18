package com.example.unisyncpoe

import com.example.unisyncpoe.data.model.User
import com.example.unisyncpoe.data.model.UserRole
import com.example.unisyncpoe.data.repository.AuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for AuthRepository
 */
class AuthRepositoryTest {
    
    @Test
    fun testUserModel() {
        val user = User(
            id = "123",
            email = "test@example.com",
            name = "Test User",
            role = UserRole.STUDENT
        )
        
        assertEquals("123", user.id)
        assertEquals("test@example.com", user.email)
        assertEquals("Test User", user.name)
        assertEquals(UserRole.STUDENT, user.role)
    }
    
    @Test
    fun testUserRoleEnum() {
        assertEquals(UserRole.STUDENT, UserRole.valueOf("STUDENT"))
        assertEquals(UserRole.LECTURER, UserRole.valueOf("LECTURER"))
        assertEquals(UserRole.ADMIN, UserRole.valueOf("ADMIN"))
    }
}

