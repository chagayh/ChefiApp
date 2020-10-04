package com.example.chefi.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import com.example.chefi.*
import com.example.chefi.database.DbUser
import com.example.chefi.fragment.*
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private val appContext: Chefi
        get() = applicationContext as Chefi

    private lateinit var observer: Observer<DbUser>
    private lateinit var authStateListener : FirebaseAuth.AuthStateListener

    companion object {
        private val TAG_LOGIN_ACTIVITY: String = "loginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkUserConnection()
        setContentView(R.layout.activity_login)
        this.supportFragmentManager
            .beginTransaction()
            .replace(R.id.user_email_frame, UserEmailFragment())
//            .replace(R.id.quick_log_frame, QuickLogFragment())
            .commit()
    }

//    private fun setObservers() {
//        // data class User observer
//        observer = Observer<User> { value ->
//            if (value == null){
//                Log.d(TAG_LOGIN_ACTIVITY, "null user, live data")
//            } else {
//                Toast.makeText(this, "user ${value.name} connected", Toast.LENGTH_SHORT)
//                    .show()
//                Log.d(TAG_LOGIN_ACTIVITY, "new user, live data ${value.email}")
//                val appIntent = Intent(this, MainActivity::class.java)
//                appIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//                startActivity(appIntent)
//                finish()
//            }
//        }
//        LiveDataHolder.getUserLiveData().observe (this, observer)
//    }

    override fun onStop() {
        super.onStop()
        appContext.getFirebaseAuth().removeAuthStateListener(authStateListener)
    }

    private fun checkUserConnection() {
        authStateListener = FirebaseAuth.AuthStateListener {
            val user = it.currentUser
            if (user != null) {
//                Toast.makeText(this, "user ${user.email} connected", Toast.LENGTH_SHORT)
//                    .show()
                Log.d(TAG_LOGIN_ACTIVITY, "in LoginActivity checkUser")
                val appIntent = Intent(this, MainActivity::class.java)
                appIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(appIntent)
                finish()
            }
        }
        appContext.getFirebaseAuth()
            .addAuthStateListener(authStateListener)
    }
}
