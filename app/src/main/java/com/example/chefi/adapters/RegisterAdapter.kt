package com.example.chefi.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.chefi.fragment.LogInFragment
import com.example.chefi.fragment.SignInFragment

class RegisterAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        val fragment: Fragment = if (position == 0){
            SignInFragment()
        } else {
            LogInFragment()
        }
        fragment.arguments = Bundle().apply {
            // Our object is just an integer :-P
            putInt("object", position + 1)
        }
        return fragment
    }


}