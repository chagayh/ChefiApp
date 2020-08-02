package com.example.chefi

import android.annotation.SuppressLint
import android.content.res.Resources
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@SuppressLint("Registered")
class AppDb {

    // Declare an instance of FirebaseAuth
    private val auth: FirebaseAuth = Firebase.auth

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
                    LiveDataHolder.getFirebaseUserMutableLiveData().postValue(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG_ACCOUNT, "createUserWithEmail:failure", task.exception)
                }
            }
    }

    /*
    * set without merge will overwrite a document or create it if it doesn't exist yet
    * set with merge will update fields in the document or create it if it doesn't exists
     */
    fun addUserToCollection(user: User?) {
        Log.d(TAG_ACCOUNT, "in add to collection, uid = ${user?.uid}")
        if (user?.uid != null){
            Log.d(TAG_ACCOUNT, "in add to collection")
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection(Chefi.getCon().getString(R.string.usersCollection))
                .document(user.uid!!)
                .set(user)
        }
    }

    fun logIn(email: String, password: String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    val newUser = User(user?.uid, user?.email)
                    Log.d(TAG_ACCOUNT, "logInWithEmail:success")
                    postUser(newUser)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG_ACCOUNT, "logInWithEmail:failure", task.exception)
                }
            }
    }

    fun postUser(user: User?) {
        LiveDataHolder.getUserMutableLiveData().postValue(user)
    }

    fun signOut(){
        auth.signOut()
    }

    fun deleteUser() {
        val currUser = auth.currentUser
        currUser?.delete()
            ?.addOnCompleteListener {
                Log.d(TAG_ACCOUNT, "deleted user")
            }
    }

}