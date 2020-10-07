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
import com.example.chefi.LiveDataHolder
import com.example.chefi.R
import java.util.*
import androidx.lifecycle.Observer

/**
 * A simple [Fragment] subclass.
 * Use the [SignInFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignInFragment : Fragment() {
    private val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var signInBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)

        emailEditText = view.findViewById(R.id.emailEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        nameEditText = view.findViewById(R.id.userNameEditText)
        nameEditText.setText(nameEditText.text.toString().toLowerCase(Locale.ROOT))
        signInBtn = view.findViewById(R.id.signInBtn)

        setComponents()
        setObserver()
        return view
    }

    private fun setObserver() {
        LiveDataHolder.getStringLiveData().observe(viewLifecycleOwner, Observer { value ->
            val content = value.getContentIfNotHandled()
            if (content != null) {
                Toast.makeText(activity, content, Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    private fun setComponents() {
        signInBtn.setOnClickListener {
            if (passwordEditText.text.toString().trim()
                    .isEmpty() || emailEditText.text.toString().trim().isEmpty()
            ) {
                Log.d(UserEmailFragment.TAG_SIGN_IN, "one is empty")
                Toast.makeText(activity, "Missing fields", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val email = emailEditText.text.toString()
                val userName = nameEditText.text.toString()
                val password = passwordEditText.text.toString()
                appContext.signIn(email, password, userName)
            }
        }
    }
}