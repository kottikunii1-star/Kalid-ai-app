package com.kalid.ms.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kalid.ms.ui.auth.LoginActivity
import com.kalid.ms.ui.home.HomeActivity
import com.kalid.ms.utils.SharedPreferencesManager

class MainActivity : AppCompatActivity() {
    private lateinit var prefsManager: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        prefsManager = SharedPreferencesManager(this)
        
        // Check if user is logged in
        val userId = prefsManager.getUserId()
        
        if (userId != -1L) {
            // User is logged in, go to home
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            // User is not logged in, go to login
            startActivity(Intent(this, LoginActivity::class.java))
        }
        
        finish()
    }
}
