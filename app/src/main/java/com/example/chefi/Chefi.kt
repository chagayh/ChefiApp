package com.example.chefi

import android.app.Application
import android.content.Context
import android.net.Uri
import com.example.chefi.database.AppDb
import com.example.chefi.database.User

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

    fun signIn(email: String, password: String, name: String){
        appDb.createUserWithEmailPassword(email, password, name)
    }

    fun logIn(email: String, password: String){
        appDb.logIn(email, password)
    }

    fun checkCurrentUser() : User? {
        return appDb.getCurrUser()
    }

    fun uploadImage(uri: Uri, fileExtension: String?) {
        appDb.uploadImage(uri, fileExtension)
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

    fun addRecipe(recipeTitle: String, imageUri: Uri?) {
        appDb.addRecipe(recipeTitle, imageUri)
    }

}