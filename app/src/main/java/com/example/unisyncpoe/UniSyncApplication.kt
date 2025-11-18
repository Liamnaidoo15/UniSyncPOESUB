package com.example.unisyncpoe

import android.app.Application
import android.util.Log
import com.example.unisyncpoe.util.DemoAccountsInitializer
import com.example.unisyncpoe.util.LanguageHelper
import com.example.unisyncpoe.util.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class for UniSync
 * Initializes Hilt dependency injection, demo accounts, and language
 */
@HiltAndroidApp
class UniSyncApplication : Application() {
    
    @Inject
    lateinit var demoAccountsInitializer: DemoAccountsInitializer
    
    @Inject
    lateinit var languageHelper: LanguageHelper
    
    @Inject
    lateinit var notificationHelper: NotificationHelper
    
    override fun onCreate() {
        super.onCreate()
        // Initialize language on app startup
        languageHelper.initializeLanguage()
        // Initialize demo accounts on app startup
        demoAccountsInitializer.initializeDemoAccounts()
        // Initialize FCM token retrieval
        initializeFCM()
    }
    
    private fun initializeFCM() {
        // Get FCM token on app start
        notificationHelper.getFCMToken { token ->
            if (token != null) {
                Log.d("UniSyncApplication", "FCM Token retrieved: $token")
                // Token is automatically sent to server via FirebaseMessagingService.onNewToken()
            } else {
                Log.w("UniSyncApplication", "Failed to retrieve FCM token")
            }
        }
    }
}

