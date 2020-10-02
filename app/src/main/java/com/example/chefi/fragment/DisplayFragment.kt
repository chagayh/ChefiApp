package com.example.chefi.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.Chefi
import com.example.chefi.LiveDataHolder
import com.example.chefi.R
import com.example.chefi.adapters.ImageItemAdapter
import androidx.lifecycle.Observer
import com.example.chefi.database.Recipe

/**
 * A simple [Fragment] subclass.
 * Use the [DisplayFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DisplayFragment : Fragment() {

    val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private lateinit var recyclerView: RecyclerView
    private lateinit var imageItemAdapter: ImageItemAdapter

    private val TAG_DISPLAY_FRAGMENT = "displayFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_display, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        imageItemAdapter = ImageItemAdapter()
        // Set an observer to upload new 'posts'
        recyclerView.adapter = imageItemAdapter
//        loadRecipes()

        val reverseLayout = false
        recyclerView.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, reverseLayout)
        recyclerView.setHasFixedSize(true)
        setObservers()
        return view
    }

    private fun setObservers() {
        // data class User observer
        LiveDataHolder.getRecipeListLiveData().observe(viewLifecycleOwner, Observer { value ->
            val content = value.getContentIfNotHandled()
            if (content == null) {
                Log.d(TAG_DISPLAY_FRAGMENT, "null recipe_profile, live data")
            } else {
                Log.d(TAG_DISPLAY_FRAGMENT, "new recipes, recipes.size = ${content.size}")
                imageItemAdapter.setItems(ArrayList<Recipe>(content))
            }
        })
    }
}
