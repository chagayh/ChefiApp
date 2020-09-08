package com.example.chefi.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.chefi.Chefi
import com.example.chefi.R
import com.google.firebase.auth.FirebaseUser

/**
 * A simple [Fragment] subclass.
 * Use the [UserEmailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserEmailFragment : Fragment() {
    private val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signInBtn: Button
    private lateinit var logInBtn: Button

    companion object {
        val TAG_SIGN_IN = "signInTag"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_email, container, false)

        emailEditText = view.findViewById(R.id.emailEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        signInBtn = view.findViewById(R.id.signInBtn)
        logInBtn = view.findViewById(R.id.logInBtn)

        setComponents()

        return view
    }

    private fun setComponents() {
        signInBtn.setOnClickListener {
            if (passwordEditText.text.toString().trim()
                    .isEmpty() || emailEditText.text.toString().trim().isEmpty()
            ) {
                Log.d(TAG_SIGN_IN, "one is empty")
                Toast.makeText(activity, "missing fields", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()
                appContext.signIn(email, password)
            }
        }

        logInBtn.setOnClickListener {
            if (passwordEditText.text.toString().trim()
                    .isEmpty() || emailEditText.text.toString().trim().isEmpty()
            ) {
                Log.d(TAG_SIGN_IN, "one is empty")
                Toast.makeText(activity, "missing fields", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()
                appContext.logIn(email, password)
            }
        }
    }
}
