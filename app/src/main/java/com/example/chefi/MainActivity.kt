package com.example.chefi

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseUser
import javax.sql.StatementEvent
import kotlin.math.sign

class MainActivity : AppCompatActivity() {

    private val appContext: Chefi
        get() = applicationContext as Chefi

    // TAGS
    private val TAG_LIVE_DATA: String = "userLiveData"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navController = findNavController(R.id.navHostFragment)
        bottomNavigationView.setupWithNavController(navController)
        setObserver()
    }

    private fun setObserver() {
        appContext.getUserLiveData().observe (this, Observer { value ->
            if (value == null){
                Log.d(TAG_LIVE_DATA, "null user, live data")
            } else {
                Toast.makeText(this, "user ${value.email} created", Toast.LENGTH_SHORT)
                    .show()
                Log.d(TAG_LIVE_DATA, "new user, live data ${value.email}")
            }
        })
    }

    override fun onStart() {
        super.onStart()
        val user = appContext.checkCurrentUser()
        if (user == null){
            startLogInDialog()
        }
        else {
            Toast.makeText(this, "user = ${user.email}", Toast.LENGTH_SHORT)
                .show()
//            updateUI(user)  // TODO
        }
    }

    @SuppressLint("InflateParams")
    private fun startLogInDialog() {
        // build the dialog
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.register_dialog, null)

        with (builder) {
            builder.setTitle("Register")
            setView(view)
//            val logInBtn = view.findViewById<Button>(R.id.logInBtn)
//            val signInBtn = view.findViewById<Button>(R.id.signInBtn)
            val userNameEditText = view.findViewById<EditText>(R.id.userNameEditText)
            val passwordEditText = view.findViewById<EditText>(R.id.passwordEditText)
            setPositiveButton("Sign In") { dialog, id ->
                if (passwordEditText.text.toString().trim().isEmpty() || userNameEditText.text.toString().trim().isEmpty()) {
                    Log.d("dialog", "one is empty")
                    Toast.makeText(this@MainActivity, "missing fields", Toast.LENGTH_SHORT)
                        .show()
                    startLogInDialog()
                } else {
                    val email = userNameEditText.text.toString()
                    val password = passwordEditText.text.toString()
                    appContext.signIn(email, password)
                    // TODO only if succeeded cancel dialog
                    dialog.cancel()
                }
            // TODO - set negative button
            }
        }
        val dialog = builder.create()

//        setDialogComponents(view, dialog)

        builder.show()
    }

    private fun setDialogComponents(view: View, dialog: DialogInterface){
//        val logInBtn = view.findViewById<Button>(R.id.logInBtn)
//        val signInBtn = view.findViewById<Button>(R.id.signInBtn)
        val userNameEditText = view.findViewById<EditText>(R.id.userNameEditText)
        val passwordEditText = view.findViewById<EditText>(R.id.passwordEditText)

//        signInBtn.setOnClickListener {
//            if (passwordEditText.text.toString().trim().isEmpty() || userNameEditText.text.toString().trim().isEmpty()) {
//                Log.d("dialog", "one is empty")
//                Toast.makeText(this, "missing fields", Toast.LENGTH_SHORT)
//                    .show()
//            } else {
//                val email = userNameEditText.text.toString()
//                val password = passwordEditText.text.toString()
//                appContext.signIn(email, password)
//                dialog.cancel()
//            }
//        }
    }
}
