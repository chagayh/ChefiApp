package com.example.chefi

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

class AppDb : AppCompatActivity() {

    // Declare an instance of FirebaseAuth
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken((R.string.default_web_client_id.toString()))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth
    }

    companion object {
        // TAGS
        private const val TAG_ACCOUNT: String = "account"
        private const val TAG_GOOGLE = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }

    fun checkCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // sign in with google

    fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG_GOOGLE, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG_GOOGLE, "Google sign in failed", e)
                // ...
            }
        }
    }

    /*
    If the call to signInWithCredential succeeds you can use the getCurrentUser
    method to get the user's account data.
     */
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG_GOOGLE, "signInWithCredential:success")
                    val user = auth.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG_GOOGLE, "signInWithCredential:failure", task.exception)
                    // ...
//                    Snackbar.make(view, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                }

                // ...
            }
    }

    // end of sign in with google


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