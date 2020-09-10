package com.example.chefi.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Observer
import com.example.chefi.*
import com.example.chefi.database.User
import com.example.chefi.fragment.*
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    private val appContext: Chefi
        get() = applicationContext as Chefi

    companion object {
        private val TAG_LIVE_DATA: String = "userLiveData"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setObservers()

        this.supportFragmentManager
            .beginTransaction()
            .replace(R.id.user_email_frame, UserEmailFragment())
            .replace(R.id.quick_log_frame, QuickLogFragment())
            .commit()


    }

    private fun setObservers() {
        // firebase user observer
        LiveDataHolder.getFirebaseUserLiveData().observe (this, Observer { value ->
            if (value == null){
                Log.d(TAG_LIVE_DATA, "null user, live data")
            } else {
                Toast.makeText(this, "user ${value.email} created", Toast.LENGTH_SHORT)
                    .show()
                Log.d(TAG_LIVE_DATA, "new user, live data ${value.email}")
                startSignInDialog(value)
//                val appIntent = Intent(this, MainActivity::class.java)
//                startActivity(appIntent)
//                finish()
            }
        })

        // data class User observer
        LiveDataHolder.getUserLiveData().observe (this, Observer { value ->
            if (value == null){
                Log.d(TAG_LIVE_DATA, "null user, live data")
            } else {
                Toast.makeText(this, "user ${value.name} connected", Toast.LENGTH_SHORT)
                    .show()
                Log.d(TAG_LIVE_DATA, "new user, live data ${value.email}")
                val appIntent = Intent(this, MainActivity::class.java)
                startActivity(appIntent)
                finish()
            }
        })
    }

    @SuppressLint("InflateParams")
    private fun startSignInDialog(user: FirebaseUser?){
        var newUser: User?
        val dialog = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.sign_in_dialog, null)
        dialog.setView(view)
        val fullName = view?.findViewById<EditText>(R.id.fullNameEditText)
        val description = view?.findViewById<EditText>(R.id.descriptionEditText)
        dialog.setPositiveButton("ok", DialogInterface.OnClickListener { dialog, id ->
            if (fullName?.text.toString().trim().isEmpty()
                || description?.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "missing fields", Toast.LENGTH_SHORT)
                    .show()
                startSignInDialog(user)
            } else {
                newUser = User(user?.uid,
                    user?.email,
                    fullName?.text.toString(),
                    description?.text.toString())
                Log.d("account", "in else of start sign in dialog ${newUser?.name}")
                appContext.addUserToCollection(newUser)
                appContext.postUser(newUser)
            }
        })
        dialog.setNegativeButton("cancel", DialogInterface.OnClickListener { dialo, id ->
            appContext.deleteUser()
            dialo.cancel()
        })
        dialog.create()
            .show()
    }
}
