package com.example.chefi

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

/**
 * A simple [Fragment] subclass.
 * Use the [UserEmailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserEmailFragment : Fragment() {
    private val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private var btnText: String? = null

    private lateinit var userNameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signInBtn: Button

    companion object {
        val TAG_SIGN_IN = "signInTag"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            btnText = it.getString(LoginAdapter.KEY_TAB)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_email, container, false)

        userNameEditText = view.findViewById(R.id.userNameEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        signInBtn = view.findViewById(R.id.btn)

        setComponents()

        return view
    }

    private fun setComponents() {
        signInBtn.setOnClickListener {
            if (passwordEditText.text.toString().trim()
                    .isEmpty() || userNameEditText.text.toString().trim().isEmpty()
            ) {
                Log.d(TAG_SIGN_IN, "one is empty")
                Toast.makeText(activity, "missing fields", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val email = userNameEditText.text.toString()
                val password = passwordEditText.text.toString()
                appContext.signIn(email, password)
            }
        }
    }
}
