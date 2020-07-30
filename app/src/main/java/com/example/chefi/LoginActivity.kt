package com.example.chefi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Observer
import kotlin.math.sign

class LoginActivity : AppCompatActivity() {
    private val appContext: Chefi
        get() = applicationContext as Chefi

    private val userNameEditText: EditText
        get() = findViewById(R.id.userNameEditText)
    private val passwordEditText: EditText
        get() = findViewById(R.id.passwordEditText)
    private val signInBtn: Button
        get() = findViewById(R.id.signInBtn)
    private val logInBtn: Button
        get() = findViewById(R.id.logInBtn)

    // TAGS
    private val TAG_LIVE_DATA: String = "userLiveData"
    private val TAG_LOGIN: String = "loginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setObserver()
        setComponents()
    }

    private fun setComponents(){
        signInBtn.setOnClickListener {
            if (passwordEditText.text.toString().trim().isEmpty() || userNameEditText.text.toString().trim().isEmpty()) {
                Log.d(TAG_LOGIN, "one is empty")
                Toast.makeText(this, "missing fields", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val email = userNameEditText.text.toString()
                val password = passwordEditText.text.toString()
                appContext.signIn(email, password, R.string.emailPasswordProvider)
            }
        }
        logInBtn.setOnClickListener {
            if (passwordEditText.text.toString().trim().isEmpty() || userNameEditText.text.toString().trim().isEmpty()) {
                Log.d(TAG_LOGIN, "one is empty")
                Toast.makeText(this, "missing fields", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val email = userNameEditText.text.toString()
                val password = passwordEditText.text.toString()
                appContext.logIn(email, password)
            }
        }
    }

    private fun setObserver() {
        LiveDataHolder.getUserLiveData().observe (this, Observer { value ->
            if (value == null){
                Log.d(TAG_LIVE_DATA, "null user, live data")
            } else {
                Toast.makeText(this, "user ${value.email} created", Toast.LENGTH_SHORT)
                    .show()
                Log.d(TAG_LIVE_DATA, "new user, live data ${value.email}")
                val appIntent = Intent(this, MainActivity::class.java)
                startActivity(appIntent)
            }
        })
    }
}
