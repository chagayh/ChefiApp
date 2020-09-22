package com.example.chefi.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.chefi.Chefi
import com.example.chefi.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val appContext: Chefi
        get() = applicationContext as Chefi

    companion object{
        // TAGS
        private val TAG_LIVE_DATA: String = "userLiveData"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appContext.uploadRecipesFirstTime()
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navController = findNavController(R.id.navHostFragment)
        bottomNavigationView.setupWithNavController(navController)

//        val user = appContext.checkCurrentUser()
//        Log.d("mainActivity", "user = $user")
//        if (user == null){
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//        }
//        else {
//            Toast.makeText(this, "user = ${user.email}", Toast.LENGTH_SHORT)
//                .show()
////            updateUI(user)  // TODO
//        }
    }

    // TODO - BUG: endless loop
//    override fun onStart() {
//        super.onStart()
//        val user = appContext.checkCurrentUser()
//        Log.d("mainActivity", "user = $user")
//        if (user == null){
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//        }
//        else {
//            Toast.makeText(this, "user = ${user.email}", Toast.LENGTH_SHORT)
//                .show()
////            updateUI(user)  // TODO
//        }
//    }
}
