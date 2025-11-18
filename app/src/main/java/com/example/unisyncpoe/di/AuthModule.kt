package com.example.unisyncpoe.di

import com.example.unisyncpoe.data.remote.AuthInterceptor
import com.example.unisyncpoe.util.AuthManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    
    @Provides
    @Singleton
    fun provideAuthInterceptor(authManager: AuthManager): AuthInterceptor {
        val interceptor = AuthInterceptor()
        authManager.getAuthToken()?.let {
            interceptor.setAuthToken(it)
        }
        return interceptor
    }
}

