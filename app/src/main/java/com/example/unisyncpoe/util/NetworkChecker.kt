package com.example.unisyncpoe.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility to check network connectivity and API availability
 */
@Singleton
class NetworkChecker @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "NetworkChecker"
    }
    
    /**
     * Check if device has internet connection
     */
    fun hasInternetConnection(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Check if API URL is valid (not placeholder)
     */
    fun isApiUrlValid(): Boolean {
        val apiUrl = com.example.unisyncpoe.BuildConfig.API_BASE_URL
        return apiUrl.isNotEmpty() && 
               !apiUrl.contains("your-api-url", ignoreCase = true) &&
               (apiUrl.startsWith("http://") || apiUrl.startsWith("https://"))
    }
}

