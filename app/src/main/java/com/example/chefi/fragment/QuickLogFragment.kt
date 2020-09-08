package com.example.chefi.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.chefi.Chefi
import com.example.chefi.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * A simple [Fragment] subclass.
 * Use the [QuickLogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class QuickLogFragment : Fragment() {

    private val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private lateinit var googleSignInBtn: com.google.android.gms.common.SignInButton
    private val auth: FirebaseAuth = Firebase.auth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object{
        private const val RC_SIGN_IN = 9001
        private const val TAG_GOOGLE = "GoogleActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken((R.string.default_web_client_id.toString()))
            .requestEmail()
            .build()
        googleSignInClient = activity?.let { GoogleSignIn.getClient(it, gso) }!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_quick_log, container, false)
        googleSignInBtn = view.findViewById(R.id.googleSignInButton)
//        setComponents()
        return view
    }

//    private fun setComponents(){
//        googleSignInBtn.setOnClickListener {
//            signInWithGoogle()
//        }
//    }

//    private fun signInWithGoogle() {
//        val signInIntent = googleSignInClient.signInIntent
//        startActivityForResult(signInIntent, RC_SIGN_IN)
//    }
//
//
//    // sign in with google
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
//        if (requestCode == RC_SIGN_IN) {
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            try {
//                // Google Sign In was successful, authenticate with Firebase
//                val account = task.getResult(ApiException::class.java)!!
//                Log.d(TAG_GOOGLE, "firebaseAuthWithGoogle:" + account.id)
//                firebaseAuthWithGoogle(account.idToken!!)
//            } catch (e: ApiException) {
//                // Google Sign In failed, update UI appropriately
//                Log.w(TAG_GOOGLE, "Google sign in failed", e)
//                // ...
//            }
//        }
//    }
//
//    /*
//    If the call to signInWithCredential succeeds you can use the getCurrentUser
//    method to get the user's account data.
//     */
//    private fun firebaseAuthWithGoogle(idToken: String) {
//            val credential = GoogleAuthProvider.getCredential(idToken, null)
//            activity?.let {
//                auth.signInWithCredential(credential)
//                    .addOnCompleteListener(it) { task ->
//                        if (task.isSuccessful) {
//                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG_GOOGLE, "signInWithCredential:success")
//                            val user = auth.currentUser
//                            appContext.addUserToCollection(user)
//                            appContext.postUser(user)
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            Log.w(TAG_GOOGLE, "signInWithCredential:failure", task.exception)
//                        }
//                    }
//            }
//
//    }
}
