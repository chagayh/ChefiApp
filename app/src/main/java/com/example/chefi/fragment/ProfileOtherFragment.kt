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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.activities.LoginActivity
import com.example.chefi.adapters.ProfileAdapter
import com.example.chefi.database.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


/**
 * A simple [Fragment] subclass.
 * Use the [ProfileOtherFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileOtherFragment : Fragment() {

    private lateinit var recyclerViewRecipes: RecyclerView
    private lateinit var recipesAdapter: ProfileAdapter
    private var user: User? = null
    private val args: ProfileOtherFragmentArgs by navArgs()
    private var SPAN_VALUE: Int = 3
    private val logTag = "ProfileOtherFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        user = args.curUser
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        recyclerViewRecipes = view.findViewById(R.id.recyclerViewRecipes)
        recipesAdapter = ProfileAdapter(user)
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

    private fun onCreateInstanceState(savedInstanceState: Bundle?)
    {
        if (savedInstanceState != null) {
            Log.i(logTag, "Load Bundle")
            val dataType = object : TypeToken<User>() {}.type
            val jasonData = savedInstanceState.getString(this.getString(R.string.profileBundleKey))
            if (jasonData != null){
                user = Gson().fromJson(jasonData, dataType)
                Log.i(logTag, "Load Jason successfully")
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.i(logTag, "Save Bundle")
        val jasonData = Gson().toJson(user)
        outState.putString(this.getString(R.string.profileBundleKey), jasonData)
    }
}