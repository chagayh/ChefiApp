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
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.adapters.RegisterAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_user_email.view.*

/**
 * A simple [Fragment] subclass.
 * Use the [UserEmailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserEmailFragment : Fragment() {
    private val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private lateinit var registerAdapter : RegisterAdapter
    private lateinit var viewPager : ViewPager2
    private lateinit var tabLayout : TabLayout

    companion object {
        val TAG_SIGN_IN = "signInTag"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_email, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerAdapter = RegisterAdapter(this)
        viewPager = view.findViewById(R.id.pager)
        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager.adapter = registerAdapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.view.isClickable = true
            tab.text = if (position == 0) {
                "Sign In"
            } else {
                "Log In"
            }
        }.attach()
    }
}
