package com.example.unisyncpoe

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.unisyncpoe.ui.auth.LoginActivity
import com.example.unisyncpoe.ui.dashboard.DashboardActivity
import com.example.unisyncpoe.util.AuthManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main Activity - Entry point of the app
 * Routes to Login or Dashboard based on auth state
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    @Inject
    lateinit var authManager: AuthManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Always show login screen first (no auto-login)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}