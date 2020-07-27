package com.example.chefi

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser

class Chefi : Application() {
    private lateinit var appDb : AppDb
    private val liveDataHolder = LiveDataHolder()

    override fun onCreate() {
        super.onCreate()
        appDb = AppDb()
    }

    fun checkCurrentUser() : FirebaseUser? {
        return appDb.checkCurrentUser()
    }

    fun getUserLiveData() : LiveData<FirebaseUser> {
        return liveDataHolder.getUserLiveData()
    }

}