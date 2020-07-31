package com.example.chefi

/*
Can be replaced with
https://github.com/firebase/snippets-android/blob/c2d8cfd95d996bd7c0c3e0bdf35a91430043f73d/auth
https://www.youtube.com/watch?v=EO-_vwfVi7c&t=733s
 */

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer

class SignInFragment : Fragment() {

    private var pageTitle: String? = null

    // TAGS
    private val TAG_LIVE_DATA: String = "userLiveData"
    private val TAG_LOGIN: String = "loginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageTitle = arguments?.getString(LoginAdapter.KEY_TAB, null)

        activity?.supportFragmentManager
            ?.beginTransaction()
            ?.replace(R.id.user_email_frame, UserEmailFragment())
            ?.replace(R.id.quick_log_frame, QuickLogFragment())
            ?.commit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)
        // Inflate the layout for this fragment

        setObserver()

        return view
    }

    private fun setObserver() {
        LiveDataHolder.getUserLiveData().observe (viewLifecycleOwner, Observer { value ->
            if (value == null){
                Log.d(TAG_LIVE_DATA, "null user, live data")
            } else {
                Toast.makeText(activity, "user ${value.email} created", Toast.LENGTH_SHORT)
                    .show()
                Log.d(TAG_LIVE_DATA, "new user, live data ${value.email}")
                val appIntent = Intent(activity, MainActivity::class.java)
                startActivity(appIntent)
            }
        })
    }

}
