package com.example.chefi.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.activities.LoginActivity
import com.example.chefi.adapters.ProfileAdapter
import com.google.firebase.auth.FirebaseAuth


/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private lateinit var recyclerViewRecipes: RecyclerView
    private lateinit var recipesAdapter: ProfileAdapter
    private var SPAN_VALUE: Int = 3
    private val TAG_PROFILE_FRAGMENT = "profileFragment"
    private var authStateListener : FirebaseAuth.AuthStateListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        recyclerViewRecipes = view.findViewById(R.id.recyclerViewRecipes)
        recipesAdapter = ProfileAdapter(null)
        recipesAdapter.setItems(ArrayList())
        recyclerViewRecipes.adapter = recipesAdapter
//        val lm = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
//        recyclerViewRecipes.layoutManager = lm
        val gridLayoutManager: GridLayoutManager = GridLayoutManager(activity, SPAN_VALUE)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (position) {
                    0 -> SPAN_VALUE
                    else -> 1
                }
            }
        }
        recyclerViewRecipes.layoutManager = gridLayoutManager
//        recyclerViewRecipes.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))
//        recyclerViewRecipes.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.HORIZONTAL))
//        recipesAdapter.notifyDataSetChanged()

        // set sign out button:
        val signOutButton: TextView = view.findViewById(R.id.signOut)
        val appContext: Chefi = view.context.applicationContext as Chefi
        signOutButton.setOnClickListener {
            appContext.signOut()
        }
        return view
    }

    override fun onStart() {
        super.onStart()
//        authStateListener = FirebaseAuth.AuthStateListener {
//            if (FirebaseAuth.getInstance().currentUser != null) {
//                val appIntent = Intent(context, LoginActivity::class.java)
//                startActivity(requireContext(), appIntent, null)
//                activity?.finish()
//            }
//        }
//
//        appContext.getFirebaseAuth()
//            .addAuthStateListener(authStateListener!!)
    }

    override fun onDestroy() {
        super.onDestroy()
//        Log.d(TAG_PROFILE_FRAGMENT, "in onDestroy")
//        Log.d(TAG_PROFILE_FRAGMENT, "in onDestroy")
    }

    override fun onStop() {
//        super.onStop()
//        Log.d(TAG_PROFILE_FRAGMENT, "in onStop")
//        if (authStateListener != null){
//            appContext.getFirebaseAuth().removeAuthStateListener(authStateListener!!)
//        }
    }
}
