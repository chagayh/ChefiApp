package com.example.chefi.fragment

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
/**
 * A simple [Fragment] subclass.
 * Use the [LogInFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LogInFragment : Fragment() {

    private val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var logInBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_log_in, container, false)

        emailEditText = view.findViewById(R.id.emailEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        logInBtn = view.findViewById(R.id.logInBtn)

        setComponents()

        return view
    }

    private fun setComponents() {

        logInBtn.setOnClickListener {
            if (passwordEditText.text.toString().trim()
                    .isEmpty() || emailEditText.text.toString().trim().isEmpty()
            ) {
                Log.d(UserEmailFragment.TAG_SIGN_IN, "one is empty")
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