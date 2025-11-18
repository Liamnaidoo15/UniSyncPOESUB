package com.example.unisyncpoe.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.unisyncpoe.R
import com.example.unisyncpoe.databinding.ActivitySettingsBinding
import com.example.unisyncpoe.util.AuthManager
import com.example.unisyncpoe.util.BiometricHelper
import com.example.unisyncpoe.util.LanguageHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    
    @Inject
    lateinit var authManager: AuthManager
    
    @Inject
    lateinit var biometricHelper: BiometricHelper
    
    @Inject
    lateinit var languageHelper: LanguageHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupSettings()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
    }
    
    private fun setupSettings() {
        // Theme toggle
        val isDarkMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        binding.switchDarkMode.isChecked = isDarkMode
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        
        // Biometric toggle
        if (biometricHelper.isBiometricAvailable()) {
            binding.switchBiometric.isChecked = authManager.isBiometricEnabled()
            binding.switchBiometric.setOnCheckedChangeListener { _, isChecked ->
                authManager.setBiometricEnabled(isChecked)
                Toast.makeText(this, "Biometric authentication ${if (isChecked) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
            }
        } else {
            binding.switchBiometric.isEnabled = false
            binding.switchBiometric.text = "Biometric not available"
        }
        
        // Language selection
        updateLanguageButtonText()
        binding.btnLanguage.setOnClickListener {
            showLanguageSelectionDialog()
        }
        
        // About
        binding.btnAbout.setOnClickListener {
            Toast.makeText(this, "UniSync v1.0\nUniversity Management System", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun updateLanguageButtonText() {
        val currentLanguage = languageHelper.getCurrentLanguage()
        val languageName = languageHelper.getLanguageDisplayName(currentLanguage)
        binding.btnLanguage.text = "${getString(R.string.language)}: $languageName"
    }
    
    private fun showLanguageSelectionDialog() {
        val languages = listOf(
            LanguageHelper.LANGUAGE_ENGLISH,
            LanguageHelper.LANGUAGE_ZULU,
            LanguageHelper.LANGUAGE_AFRIKAANS
        )
        
        val languageNames = languages.map { languageHelper.getLanguageDisplayName(it) }
        val currentLanguage = languageHelper.getCurrentLanguage()
        val selectedIndex = languages.indexOf(currentLanguage).takeIf { it >= 0 } ?: 0
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_language))
            .setSingleChoiceItems(
                languageNames.toTypedArray(),
                selectedIndex
            ) { dialog, which ->
                val selectedLanguage = languages[which]
                if (selectedLanguage != currentLanguage) {
                    languageHelper.setLanguage(selectedLanguage)
                    updateLanguageButtonText()
                    Toast.makeText(
                        this,
                        getString(R.string.language_changed),
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Restart activity to apply language change
                    recreate()
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

