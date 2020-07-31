package com.example.chefi

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import com.google.android.material.snackbar.Snackbar
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@SuppressLint("Registered")
class AppDb {

    // Declare an instance of FirebaseAuth
    private val auth: FirebaseAuth = Firebase.auth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        // TAGS
        private const val TAG_ACCOUNT: String = "account"
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
//                    liveDataHolder.getUserMutableLiveData().postValue(user)
                    LiveDataHolder.getUserMutableLiveData().postValue(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG_ACCOUNT, "createUserWithEmail:failure", task.exception)
                }
            }
    }

    fun logIn(email: String, password: String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    Log.d(TAG_ACCOUNT, "logInWithEmail:success")
//                    liveDataHolder.getUserMutableLiveData().postValue(user)
                    LiveDataHolder.getUserMutableLiveData().postValue(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG_ACCOUNT, "logInWithEmail:failure", task.exception)
                }
            }
    }

    fun signOut(){
        auth.signOut()
    }

}