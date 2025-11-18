package com.example.unisyncpoe.data.remote

import com.example.unisyncpoe.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Retrofit module for dependency injection
 * Provides API service instance with authentication and logging
 */
object RetrofitModule {
    
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // Enable logging for debug and staging builds
            val enableLogging = try {
                BuildConfig::class.java.getField("ENABLE_LOGGING").getBoolean(null)
            } catch (e: Exception) {
                BuildConfig.DEBUG // Fallback to DEBUG flag if ENABLE_LOGGING doesn't exist
            }
            
            level = if (enableLogging) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(5, TimeUnit.SECONDS) // Reduced from 30 to 5 seconds
            .readTimeout(5, TimeUnit.SECONDS) // Reduced from 30 to 5 seconds
            .writeTimeout(5, TimeUnit.SECONDS) // Reduced from 30 to 5 seconds
            .retryOnConnectionFailure(false) // Disable retry to fail faster
            // Use HTTP/1.1 only to avoid HTTP/2 protocol errors with invalid/unreachable servers
            .protocols(listOf(Protocol.HTTP_1_1))
            .build()
    }
    
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}

/**
 * Interceptor to add authentication token to requests
 */
class AuthInterceptor : Interceptor {
    private var authToken: String? = null
    
    fun setAuthToken(token: String?) {
        this.authToken = token
    }
    
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val originalRequest = chain.request()
        
        val newRequest = if (authToken != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $authToken")
                .build()
        } else {
            originalRequest
        }
        
        return chain.proceed(newRequest)
    }
}

