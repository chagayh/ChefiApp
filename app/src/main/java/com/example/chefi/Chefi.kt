package com.example.chefi

import android.app.Application
import android.widget.Toast
import com.google.firebase.auth.FirebaseUser

class Chefi : Application() {
    private lateinit var appDb : AppDb

    // TAGS
    private val TAG_LIVE_DATA: String = "userLiveData"

    override fun onCreate() {
        super.onCreate()
        appDb = AppDb()
    }

    fun signIn(email: String, password: String){
        appDb.createUserWithEmailPassword(email, password)
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