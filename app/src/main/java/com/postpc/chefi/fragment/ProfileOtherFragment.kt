package com.postpc.chefi.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.postpc.chefi.Chefi
import com.postpc.chefi.R
import com.postpc.chefi.adapters.ProfileAdapter
import com.postpc.chefi.database.AppRecipe
import com.postpc.chefi.database.DbUser
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.lifecycle.Observer
import com.postpc.chefi.LiveDataHolder
import com.postpc.chefi.ObserveWrapper


/**
 * A simple [Fragment] subclass.
 * Use the [ProfileOtherFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileOtherFragment : Fragment() {

    private lateinit var recyclerViewRecipes: RecyclerView
    private lateinit var recipesAdapter: ProfileAdapter
    private var dbUser: DbUser? = null
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
        dbUser = args.curUser
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        recyclerViewRecipes = view.findViewById(R.id.recyclerViewRecipes)
        recipesAdapter = ProfileAdapter(dbUser, viewLifecycleOwner, view)
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
        signOutButton.visibility = View.GONE

        appContext.loadRecipes(dbUser)
        Log.e("Profile Fragment", dbUser?.name.toString())
        val observer = Observer<ObserveWrapper<MutableList<AppRecipe>>> { value ->
            val content = value.getContentIfNotHandled()
            if (content != null){
                Log.e("Profile Fragment", content.size.toString())
                recipesAdapter.setItems(ArrayList(content), false)
//                Log.e("Profile Fragment", "${content[0]}")
//                    notifyDataSetChanged()
            }
        }
        LiveDataHolder.getRecipeListLiveData().observe(viewLifecycleOwner, observer)
//        recipesAdapter.setItems(recipesAdapter._recipesItems, false)
        return view
    }

    private fun onCreateInstanceState(savedInstanceState: Bundle?)
    {
        if (savedInstanceState != null) {
            Log.i(logTag, "Load Bundle")
            val dataType = object : TypeToken<DbUser>() {}.type
            val jasonData = savedInstanceState.getString(this.getString(R.string.profileBundleKey))
            if (jasonData != null){
                dbUser = Gson().fromJson(jasonData, dataType)
                Log.i(logTag, "Load Jason successfully")
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.i(logTag, "Save Bundle")
        val jasonData = Gson().toJson(dbUser)
        outState.putString(this.getString(R.string.profileBundleKey), jasonData)
    }
}