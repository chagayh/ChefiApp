package com.example.chefi.fragment

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.activities.LoginActivity
import com.example.chefi.adapters.RecipeAdapter


/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {

    private lateinit var recyclerViewRecipes: RecyclerView
    private lateinit var recipesAdapter: RecipeAdapter
    private var SPAN_VALUE: Int = 3

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
        recipesAdapter = RecipeAdapter(false, null)
        recipesAdapter.setItems(ArrayList())
        recyclerViewRecipes.adapter = recipesAdapter
//        recyclerViewRecipes.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
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
            val appIntent = Intent(it.context, LoginActivity::class.java)
            startActivity(it.context, appIntent, null)
        }
        return view
    }
}