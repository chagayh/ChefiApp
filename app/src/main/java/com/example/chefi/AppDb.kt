package com.example.chefi

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AppDb {

    private val liveDataHolder = LiveDataHolder()

    // Declare an instance of FirebaseAuth
    private var auth: FirebaseAuth = Firebase.auth

    // TAGS
    private val TAG_ACCOUNT: String = "account"

    init {

    }

    fun checkCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun createUserWithEmailPassword(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG_ACCOUNT, "createUserWithEmail:success")
                    val user = auth.currentUser
                    liveDataHolder.getUserMutableLiveData().postValue(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG_ACCOUNT, "createUserWithEmail:failure", task.exception)
                }
            }
    }
}