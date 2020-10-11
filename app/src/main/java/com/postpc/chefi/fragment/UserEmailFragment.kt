package com.postpc.chefi.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.postpc.chefi.Chefi
import com.postpc.chefi.R
import com.postpc.chefi.adapters.RegisterAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

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
