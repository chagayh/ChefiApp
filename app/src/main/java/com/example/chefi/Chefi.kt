package com.example.chefi

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseUser

class Chefi : Application() {
    private lateinit var appDb : AppDb
//    private lateinit var liveDataHolder : LiveDataHolder

    // TAGS
    private val TAG_LIVE_DATA: String = "userLiveData"

    override fun onCreate() {
        super.onCreate()
        appDb = AppDb()
//        liveDataHolder = LiveDataHolder()
    }

    fun signIn(email: String, password: String, provider: Int){
        when (provider){
            R.string.emailPasswordProvider -> appDb.createUserWithEmailPassword(email, password)
            R.string.googleProvider -> appDb.signInWithGoogle()
        }

    }

    fun logIn(email: String, password: String){
        appDb.logIn(email, password)
    }

    fun checkCurrentUser() : FirebaseUser? {
        return appDb.checkCurrentUser()
    }

//    fun getUserLiveData() : LiveData<FirebaseUser> {
//        return liveDataHolder.getUserLiveData()
//    }

}