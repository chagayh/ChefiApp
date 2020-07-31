package com.example.chefi

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class LoginAdapter(fm : FragmentManager) : FragmentPagerAdapter(fm) {

    companion object {
        val KEY_TAB = "keyTab"
        val TITLE_SIGN_IN = "Sign In"
        val TITLE_LOG_IN = "Log In"
    }

    override fun getItem(position: Int): Fragment {
        return if (position == 0) SignInFragment() else LogInFragment()
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence {
        return if (position == 0) "Sign In" else "Log In"
    }

}
