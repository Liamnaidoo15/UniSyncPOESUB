package com.example.unisyncpoe.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for managing app language/locale
 * Supports English, isiZulu, and Afrikaans
 */
@Singleton
class LanguageHelper @Inject constructor(
    private val context: Context,
    private val authManager: AuthManager
) {
    companion object {
        private const val TAG = "LanguageHelper"
        
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_ZULU = "zu"
        const val LANGUAGE_AFRIKAANS = "af"
        
        val SUPPORTED_LANGUAGES = listOf(
            LANGUAGE_ENGLISH,
            LANGUAGE_ZULU,
            LANGUAGE_AFRIKAANS
        )
    }
    
    /**
     * Get current language code
     */
    fun getCurrentLanguage(): String {
        return authManager.getLanguage() ?: LANGUAGE_ENGLISH
    }
    
    /**
     * Set app language
     */
    fun setLanguage(languageCode: String) {
        if (languageCode in SUPPORTED_LANGUAGES) {
            authManager.saveLanguage(languageCode)
            updateLocale(languageCode)
        }
    }
    
    /**
     * Update app locale
     */
    fun updateLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            val context = context.createConfigurationContext(configuration)
            // Context is updated, but we need to apply it at activity level
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
    }
    
    /**
     * Get language display name
     */
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            LANGUAGE_ENGLISH -> "English"
            LANGUAGE_ZULU -> "isiZulu"
            LANGUAGE_AFRIKAANS -> "Afrikaans"
            else -> "English"
        }
    }
    
    /**
     * Initialize language on app startup
     */
    fun initializeLanguage() {
        val savedLanguage = authManager.getLanguage()
        if (savedLanguage != null) {
            updateLocale(savedLanguage)
        }
    }
}

