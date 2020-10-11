package com.postpc.chefi.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import com.postpc.chefi.*
import com.postpc.chefi.database.AppRecipe
import com.postpc.chefi.database.DbUser
import com.postpc.chefi.fragment.*
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private val appContext: Chefi
        get() = applicationContext as Chefi

    private lateinit var observer: Observer<AppRecipe>
    private lateinit var authStateListener : FirebaseAuth.AuthStateListener

    companion object {
        private val TAG_LOGIN_ACTIVITY: String = "loginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkUserConnection()
        setContentView(R.layout.activity_login)
        setUserObserver()
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
            Log.d(TAG_LOGIN_ACTIVITY, "in LoginActivity usr.email = ${user?.email}")
            if (user != null) {
                Log.d(TAG_LOGIN_ACTIVITY, "in LoginActivity checkUser")
            }
        }
        appContext.getFirebaseAuth()
            .addAuthStateListener(authStateListener)
    }

    private fun setUserObserver() {
        // data class User observer
        val observer = Observer<ObserveWrapper<DbUser>> { value ->
            val content = value.getContentIfNotHandled()
            if (content == null){
                Log.d("LoginActivity", "null user, live data")
            } else {
                Toast.makeText(this, "@${content.userName} was connected", Toast.LENGTH_LONG)
                    .show()
                appContext.loadRecipes(null)
                appContext.loadFavorites()
                appContext.loadFollowers(null)
                appContext.loadFollowing(null)
                appContext.loadNotifications()
                val appIntent = Intent(this, MainActivity::class.java)
                appIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(appIntent)
                finish()
            }
        }
        LiveDataHolder.getUserLiveData().observe(this, observer)
    }
}
