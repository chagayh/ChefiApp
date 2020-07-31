package com.example.chefi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LogInFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LogInFragment : Fragment() {
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
        val view = inflater.inflate(R.layout.fragment_log_in, container, false)
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
