package com.example.chefi.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.chefi.Chefi
import com.example.chefi.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val appContext: Chefi
        get() = applicationContext as Chefi

    // TAGS
    private val TAG_LIVE_DATA: String = "userLiveData"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navController = findNavController(R.id.navHostFragment)
        bottomNavigationView.setupWithNavController(navController)
    }

    override fun onStart() {
        super.onStart()
        val user = appContext.checkCurrentUser()
        if (user == null){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        else {
            Toast.makeText(this, "user = null", Toast.LENGTH_SHORT)
                .show()
//            updateUI(user)  // TODO
        }
    }
}
