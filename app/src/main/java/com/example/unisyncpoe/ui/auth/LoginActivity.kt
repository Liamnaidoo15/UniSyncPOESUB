package com.example.unisyncpoe.ui.auth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.unisyncpoe.R
import com.example.unisyncpoe.databinding.ActivityLoginBinding
import com.example.unisyncpoe.ui.dashboard.DashboardActivity
import com.example.unisyncpoe.util.AuthManager
import com.example.unisyncpoe.util.BiometricHelper
import com.example.unisyncpoe.util.CredentialStorage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Login Activity with SSO and Biometric support
 */
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    
    @Inject
    lateinit var authManager: AuthManager
    
    @Inject
    lateinit var biometricHelper: BiometricHelper
    
    @Inject
    lateinit var credentialStorage: CredentialStorage
    
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, notifications will work
        } else {
            // Permission denied, show message
            Toast.makeText(this, "Notification permission is required for push notifications", Toast.LENGTH_LONG).show()
        }
    }
    
    companion object {
        private const val RC_GOOGLE_SIGN_IN = 9001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupFirebaseAuth()
        setupGoogleSignIn()
        setupUI()
        observeViewModel()
        requestNotificationPermission()
        
        // Check if user is already logged in
        if (authManager.isLoggedIn()) {
            navigateToDashboard()
        }
    }
    
    private fun requestNotificationPermission() {
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    
    private fun setupFirebaseAuth() {
        firebaseAuth = FirebaseAuth.getInstance()
    }
    
    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }
    
    private fun setupUI() {
        binding.apply {
            btnLogin.setOnClickListener {
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString()
                
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this@LoginActivity, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                viewModel.login(email, password)
            }
            
            btnBiometric.setOnClickListener {
                if (biometricHelper.isBiometricAvailable()) {
                    performBiometricLogin()
                } else {
                    Toast.makeText(this@LoginActivity, "Biometric authentication not available", Toast.LENGTH_SHORT).show()
                }
            }
            
            // Registration removed - only admins can register users
        }
        
        // Show biometric button only if available, enabled, and credentials are saved
        val shouldShowBiometric = biometricHelper.isBiometricAvailable() && 
                                   authManager.isBiometricEnabled() && 
                                   credentialStorage.hasSavedCredentials()
        if (!shouldShowBiometric) {
            binding.btnBiometric.visibility = android.view.View.GONE
        } else {
            binding.btnBiometric.text = "Login with Fingerprint"
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is AuthUiState.Loading -> {
                        binding.btnLogin.isEnabled = false
                        binding.progressBar.visibility = android.view.View.VISIBLE
                    }
                    is AuthUiState.Success -> {
                        binding.btnLogin.isEnabled = true
                        binding.progressBar.visibility = android.view.View.GONE
                        navigateToDashboard()
                    }
                    is AuthUiState.Error -> {
                        binding.btnLogin.isEnabled = true
                        binding.progressBar.visibility = android.view.View.GONE
                        Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                    is AuthUiState.Idle -> {
                        binding.btnLogin.isEnabled = true
                        binding.progressBar.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }
    
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        // Create user object and register/login
                        val uniSyncUser = com.example.unisyncpoe.data.model.User(
                            id = user.uid,
                            email = user.email ?: "",
                            name = user.displayName ?: "",
                            role = com.example.unisyncpoe.data.model.UserRole.STUDENT // Default role
                        )
                        // Save to local and navigate
                        authManager.saveUserId(uniSyncUser.id)
                        authManager.saveUserEmail(uniSyncUser.email)
                        authManager.setLoggedIn(true)
                        navigateToDashboard()
                    }
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
    
    private fun performBiometricLogin() {
        // Check if credentials are saved
        if (!credentialStorage.hasSavedCredentials()) {
            Toast.makeText(this, "No saved credentials found. Please login with email and password first.", Toast.LENGTH_LONG).show()
            return
        }
        
        val savedEmail = credentialStorage.getSavedEmail()
        val savedPassword = credentialStorage.getSavedPassword()
        
        if (savedEmail == null || savedPassword == null) {
            Toast.makeText(this, "No saved credentials found. Please login with email and password first.", Toast.LENGTH_LONG).show()
            return
        }
        
        // Show biometric prompt
        biometricHelper.showBiometricPrompt(
            activity = this,
            title = "Biometric Login",
            subtitle = "Use your fingerprint or face to login",
            onSuccess = {
                // Biometric authentication successful, now login with saved credentials
                viewModel.login(savedEmail, savedPassword)
            },
            onError = { error ->
                Toast.makeText(this, "Biometric error: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }
    
    private fun navigateToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }
}

