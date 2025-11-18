package com.example.unisyncpoe.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure credential storage for biometric login
 * Uses EncryptedSharedPreferences to store passwords securely
 */
@Singleton
class CredentialStorage @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "encrypted_credentials"
        private const val KEY_SAVED_EMAIL = "saved_email"
        private const val KEY_SAVED_PASSWORD = "saved_password"
        private const val KEY_HAS_SAVED_CREDENTIALS = "has_saved_credentials"
    }
    
    private val encryptedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    /**
     * Save user credentials securely
     */
    fun saveCredentials(email: String, password: String) {
        encryptedPrefs.edit()
            .putString(KEY_SAVED_EMAIL, email)
            .putString(KEY_SAVED_PASSWORD, password)
            .putBoolean(KEY_HAS_SAVED_CREDENTIALS, true)
            .apply()
    }
    
    /**
     * Get saved email
     */
    fun getSavedEmail(): String? {
        return encryptedPrefs.getString(KEY_SAVED_EMAIL, null)
    }
    
    /**
     * Get saved password
     */
    fun getSavedPassword(): String? {
        return encryptedPrefs.getString(KEY_SAVED_PASSWORD, null)
    }
    
    /**
     * Check if credentials are saved
     */
    fun hasSavedCredentials(): Boolean {
        return encryptedPrefs.getBoolean(KEY_HAS_SAVED_CREDENTIALS, false)
    }
    
    /**
     * Clear saved credentials
     */
    fun clearCredentials() {
        encryptedPrefs.edit()
            .remove(KEY_SAVED_EMAIL)
            .remove(KEY_SAVED_PASSWORD)
            .putBoolean(KEY_HAS_SAVED_CREDENTIALS, false)
            .apply()
    }
}


