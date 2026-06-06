package com.kalid.ms.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.kalid.ms.database.KalidMsDatabase
import com.kalid.ms.databinding.ActivityBusinessProfileBinding
import com.kalid.ms.repository.UserRepository
import com.kalid.ms.ui.viewmodel.AuthViewModel
import com.kalid.ms.utils.SharedPreferencesManager
import com.kalid.ms.utils.ValidationUtils

class BusinessProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBusinessProfileBinding
    private lateinit var authViewModel: AuthViewModel
    private lateinit var prefsManager: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBusinessProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = SharedPreferencesManager(this)
        setupViewModel()
        setupListeners()
        loadBusinessProfile()
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
        binding.saveButton.setOnClickListener { saveBusinessProfile() }
    }

    private fun loadBusinessProfile() {
        val businessName = prefsManager.getBusinessName()
        val pricePerSqm = prefsManager.getPricePerSquareMeter()
        
        binding.businessNameInput.setText(businessName)
        binding.pricePerSqmInput.setText(pricePerSqm.toString())
    }

    private fun saveBusinessProfile() {
        val businessName = binding.businessNameInput.text.toString().trim()
        val ownerName = binding.ownerNameInput.text.toString().trim()
        val phone = binding.phoneInput.text.toString().trim()
        val address = binding.addressInput.text.toString().trim()
        val pricePerSqmStr = binding.pricePerSqmInput.text.toString()

        if (!ValidationUtils.isNotEmpty(businessName, ownerName, phone, address, pricePerSqmStr)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!ValidationUtils.isValidPhoneNumber(phone)) {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show()
            return
        }

        if (!ValidationUtils.isValidDouble(pricePerSqmStr)) {
            Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = prefsManager.getUserId()
        if (userId == -1L) return

        authViewModel.updateBusinessProfile(
            userId,
            businessName,
            ownerName,
            phone,
            address,
            ""
        ).observe(this) { result ->
            result.onSuccess {
                prefsManager.saveBusinessName(businessName)
                prefsManager.savePricePerSquareMeter(pricePerSqmStr.toDouble())
                Toast.makeText(this, "Business profile updated", Toast.LENGTH_SHORT).show()
                finish()
            }
            result.onFailure { error ->
                Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
