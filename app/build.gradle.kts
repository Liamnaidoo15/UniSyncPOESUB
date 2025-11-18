plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.unisyncpoe"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.unisyncpoe"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // API Base URL - Development default (can be overridden per build type)
        // For local development, use: http://10.0.2.2:3000/api (Android emulator)
        // For physical device, use: http://YOUR_COMPUTER_IP:3000/api
        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:3000/api/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Production API URL - Render hosting
            buildConfigField("String", "API_BASE_URL", "\"https://unisyncapi.onrender.com/api/\"")
            // Disable logging in production
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
        }
        create("staging") {
            // Pre-release build for testing - production-like but debuggable
            initWith(getByName("release"))
            isDebuggable = true
            // Note: No applicationIdSuffix to avoid Google Services validation issues
            // Staging builds will use base package name: com.example.unisyncpoe
            // (will overwrite release if both installed, which is fine for testing)
            versionNameSuffix = "-staging"
            matchingFallbacks += listOf("release", "debug")
            
            // Staging API URL - using production API for testing
            // For local testing, use: http://10.0.2.2:3000/api
            // Using production API for staging builds
            buildConfigField("String", "API_BASE_URL", "\"https://unisyncapi.onrender.com/api/\"")
            
            // Enable logging for staging to help with debugging
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
        }
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            // Note: No applicationIdSuffix to avoid Google Services validation issues
            // Debug builds will use base package name: com.example.unisyncpoe
            // Development API URL
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:3000/api/\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}


dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    
    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.runtime)
    
    // Navigation
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    
    // Retrofit & Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    
    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)
    implementation(libs.play.services.auth)
    
    // Biometric
    implementation(libs.androidx.biometric)
    
    // QR Code
    implementation(libs.zxing.android.embedded)
    
    // JSON
    implementation(libs.gson)
    
    // Image Loading
    implementation(libs.glide)
    
    // Work Manager
    implementation(libs.androidx.work.runtime)
    
    // Location Services
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    
    // SwipeRefreshLayout
    implementation(libs.androidx.swiperefreshlayout)
    
    // Security Crypto (for EncryptedSharedPreferences)
    implementation(libs.androidx.security.crypto)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}