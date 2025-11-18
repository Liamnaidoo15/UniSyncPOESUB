package com.example.unisyncpoe.di

import android.content.Context
import com.example.unisyncpoe.util.AuthManager
import com.example.unisyncpoe.util.BiometricHelper
import com.example.unisyncpoe.util.CredentialStorage
import com.example.unisyncpoe.util.DemoAccountsInitializer
import com.example.unisyncpoe.util.LanguageHelper
import com.example.unisyncpoe.util.NetworkChecker
import com.example.unisyncpoe.util.NotificationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilModule {
    
    @Provides
    @Singleton
    fun provideAuthManager(@ApplicationContext context: Context): AuthManager {
        return AuthManager(context)
    }
    
    @Provides
    @Singleton
    fun provideBiometricHelper(@ApplicationContext context: Context): BiometricHelper {
        return BiometricHelper(context)
    }
    
    @Provides
    @Singleton
    fun provideNetworkChecker(@ApplicationContext context: Context): NetworkChecker {
        return NetworkChecker(context)
    }
    
    @Provides
    @Singleton
    fun provideLanguageHelper(
        @ApplicationContext context: Context,
        authManager: AuthManager
    ): LanguageHelper {
        return LanguageHelper(context, authManager)
    }
    
    @Provides
    @Singleton
    fun provideCredentialStorage(@ApplicationContext context: Context): CredentialStorage {
        return CredentialStorage(context)
    }
    
    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper {
        return NotificationHelper(context)
    }
}
