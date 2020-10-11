package com.postpc.chefi.fragment

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.postpc.chefi.Chefi
import com.postpc.chefi.R
import com.postpc.chefi.activities.LoginActivity
import com.postpc.chefi.adapters.ProfileAdapter
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
        recipesAdapter = ProfileAdapter(null, viewLifecycleOwner, view)
        recyclerViewRecipes.adapter = recipesAdapter
//        val lm = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
//        recyclerViewRecipes.layoutManager = lm
        if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            SPAN_VALUE = 6
        }
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
        val appContext: Chefi = view.context.applicationContext as Chefi
        val signOutButton: TextView = view.findViewById(R.id.signOut)
        signOutButton.setOnClickListener {
            appContext.signOut()
        }
        recipesAdapter.setItems(appContext.getUserRecipes(), false)
        Log.e("Profile Fragment", appContext.getUserRecipes()?.size.toString())
        return view
    }

    override fun onStart() {
        super.onStart()
        authStateListener = FirebaseAuth.AuthStateListener {
            if (it.currentUser == null) {
                Log.d(TAG_PROFILE_FRAGMENT, "user = ${appContext.getCurrUser()?.name}")
                val loginIntent = Intent(context, LoginActivity::class.java)
                startActivity(requireContext(), loginIntent, null)
                loginIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                activity?.finish()
            }
        }

        appContext.getFirebaseAuth()
            .addAuthStateListener(authStateListener!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG_PROFILE_FRAGMENT, "in onDestroy")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG_PROFILE_FRAGMENT, "in onStop")
        if (authStateListener != null){
            appContext.getFirebaseAuth().removeAuthStateListener(authStateListener!!)
        }
    }
}
