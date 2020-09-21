package com.example.chefi.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import com.example.chefi.*
import com.example.chefi.fragment.*

class LoginActivity : AppCompatActivity() {

    private val appContext: Chefi
        get() = applicationContext as Chefi

    companion object {
        private val TAG_LOGIN_ACTIVITY: String = "loginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setObservers()
        this.supportFragmentManager
            .beginTransaction()
            .replace(R.id.user_email_frame, UserEmailFragment())
            .replace(R.id.quick_log_frame, QuickLogFragment())
            .commit()
        appContext.checkCurrentUser()
    }

    private fun setObservers() {
        // data class User observer
        LiveDataHolder.getUserLiveData().observe (this, Observer { value ->
            if (value == null){
                Log.d(TAG_LOGIN_ACTIVITY, "null user, live data")
            } else {
                Toast.makeText(this, "user ${value.name} connected", Toast.LENGTH_SHORT)
                    .show()
                Log.d(TAG_LOGIN_ACTIVITY, "new user, live data ${value.email}")
                val appIntent = Intent(this, MainActivity::class.java)
                startActivity(appIntent)
                finish()
            }
        })
    }

    private fun checkUserConnected(){
        val user = appContext.checkCurrentUser()
        Log.d(TAG_LOGIN_ACTIVITY, "user = $user")
        if (user != null){
            val appIntent = Intent(this, MainActivity::class.java)
            startActivity(appIntent)
            finish()
        }
    }
}
