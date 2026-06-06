package com.kalid.ms.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.kalid.ms.databinding.ActivitySettingsBinding
import com.kalid.ms.ui.auth.LoginActivity
import com.kalid.ms.utils.SharedPreferencesManager

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefsManager: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = SharedPreferencesManager(this)
        setupListeners()
        loadSettings()
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener { finish() }
        
        binding.changePasswordButton?.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }
        
        binding.businessInfoButton?.setOnClickListener {
            startActivity(Intent(this, BusinessProfileActivity::class.java))
        }
        
        binding.darkModeSwitch?.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.saveThemeMode(isChecked)
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
        
        binding.logoutButton?.setOnClickListener { logout() }
    }

    private fun loadSettings() {
        val isDarkMode = prefsManager.isDarkMode()
        binding.darkModeSwitch?.isChecked = isDarkMode
    }

    private fun logout() {
        prefsManager.logout()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
