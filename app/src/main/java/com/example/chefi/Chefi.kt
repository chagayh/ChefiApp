package com.example.chefi

import android.app.Application
import android.content.Context
import android.content.res.Resources
import com.google.firebase.auth.FirebaseUser

class Chefi : Application() {
    private lateinit var appDb : AppDb


    // TAGS
    private val TAG_LIVE_DATA: String = "userLiveData"

    companion object {
        private lateinit var appCon: Context

        fun getCon(): Context {
            return appCon
        }
    }

    override fun onCreate() {
        super.onCreate()
        appDb = AppDb()
        appCon = this
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

    fun addUserToCollection(user: User?){
        appDb.addUserToCollection(user)
    }

    fun postUser(user: User?) {
        appDb.postUser(user)
    }

    fun deleteUser(){
        appDb.deleteUser()
    }

}