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
    private val liveDataHolder = LiveDataHolder()

    // TAGS
    private val TAG_LIVE_DATA: String = "userLiveData"

    override fun onCreate() {
        super.onCreate()
        appDb = AppDb()
        setObserver()
    }

    private fun setObserver() {
        getUserLiveData().observeForever ( Observer { value ->
            if (value == null){
                Log.d(TAG_LIVE_DATA, "null user, live data")
            } else {
                Toast.makeText(this, "user ${value.email} created", Toast.LENGTH_SHORT)
                    .show()
                Log.d(TAG_LIVE_DATA, "new user, live data ${value.email}")
            }
        })
    }

    fun signIn(email: String, password: String){
        appDb.createUserWithEmailPassword(email, password)
    }

    fun checkCurrentUser() : FirebaseUser? {
        return appDb.checkCurrentUser()
    }

    fun getUserLiveData() : LiveData<FirebaseUser> {
        return liveDataHolder.getUserLiveData()
    }

}