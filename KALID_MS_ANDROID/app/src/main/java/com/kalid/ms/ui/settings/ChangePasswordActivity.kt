package com.kalid.ms.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.kalid.ms.database.KalidMsDatabase
import com.kalid.ms.databinding.ActivityChangePasswordBinding
import com.kalid.ms.repository.UserRepository
import com.kalid.ms.ui.viewmodel.AuthViewModel
import com.kalid.ms.utils.SharedPreferencesManager
import com.kalid.ms.utils.ValidationUtils

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var authViewModel: AuthViewModel
    private lateinit var prefsManager: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = SharedPreferencesManager(this)
        setupViewModel()
        setupListeners()
    }

    private fun setupViewModel() {
        val database = KalidMsDatabase.getDatabase(this)
        val userRepository = UserRepository(database.userDao())
        authViewModel = ViewModelProvider(
            this,
            AuthViewModel.Factory(userRepository)
        )[AuthViewModel::class.java]
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener { finish() }
        binding.updateButton.setOnClickListener { changePassword() }
    }

    private fun changePassword() {
        val oldPassword = binding.oldPasswordInput.text.toString()
        val newPassword = binding.newPasswordInput.text.toString()
        val confirmPassword = binding.confirmPasswordInput.text.toString()

        if (!ValidationUtils.isNotEmpty(oldPassword, newPassword, confirmPassword)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!ValidationUtils.isValidPassword(newPassword)) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = prefsManager.getUserId()
        if (userId == -1L) return

        authViewModel.updatePassword(userId, oldPassword, newPassword).observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            result.onFailure { error ->
                Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
