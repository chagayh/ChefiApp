package com.example.chefi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
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
            Toast.makeText(this, "user = ${user.email}", Toast.LENGTH_SHORT)
                .show()
//            updateUI(user)  // TODO
        }
    }
}
