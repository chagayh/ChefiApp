package com.example.chefi

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseUser
import javax.sql.StatementEvent

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

        setObserver()
    }

    private fun setObserver() {
        appContext.getUserLiveData().observe(this, Observer { value ->
            if (value == null){
                Log.d(TAG_LIVE_DATA, "null user, live data")
            } else {
                Log.d(TAG_LIVE_DATA, "new user, live data ${value.email}")
            }
        })
    }

    override fun onStart() {
        super.onStart()
        val user = appContext.checkCurrentUser()
        if (user == null){
            startLogInDialog()
        }
        else {
            Toast.makeText(this, "user = ${user.email}", Toast.LENGTH_SHORT)
                .show()
//            updateUI(user)  // TODO
        }
    }

    private fun startLogInDialog() {
        val registerDialog = RegisterDialog()
        registerDialog.show(supportFragmentManager, "register dialog")
    }

}
