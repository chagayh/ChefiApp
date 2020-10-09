package com.example.chefi.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.Chefi
import com.example.chefi.LiveDataHolder
import com.example.chefi.ObserveWrapper
import com.example.chefi.R
import com.example.chefi.adapters.HomeAdapter
import com.example.chefi.database.AppRecipe
import com.example.chefi.database.DbUser


class HomeFragment : Fragment() {

    val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private lateinit var recyclerViewHome: RecyclerView
    private lateinit var homeAdapter: HomeAdapter
    private var appUser: DbUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        appUser = appContext.getCurrUser()
//        val progressBar: ProgressBar = view.findViewById(R.id.progressBarHome)
//        val draw: Drawable = view.resources.getDrawable(R.drawable.custum_progress_bar)
//        progressBar.progressDrawable = draw

        recyclerViewHome = view.findViewById(R.id.recyclerViewHome)
        homeAdapter = HomeAdapter(viewLifecycleOwner, view)
        homeAdapter.setItems(null)
        recyclerViewHome.adapter = homeAdapter
        recyclerViewHome.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
//        val items= appContext.getUserRecipes()
//        var items = ArrayList<AppRecipe>()
//        appContext.uploadFeed(true)
//        val observer = Observer<ObserveWrapper<MutableList<AppRecipe>>> { value ->
//            val content = value.getContentIfNotHandled()
//            if (content != null){
//                items = ArrayList(content)
////                Log.e("Profile Fragment", "${content[0]}")
////                    notifyDataSetChanged()
//            }
//        }
//        LiveDataHolder.getRecipeListLiveData().observe(viewLifecycleOwner, observer)
////        if (items != null) {
//        Log.e("Home fragment", items.size.toString())
//        homeAdapter.setItems(items)
//        }
        return view
    }

    override fun onResume() {
        Log.d("updateFeed", "in onResume of HomeFragment")
        super.onResume()
        var items = ArrayList<AppRecipe>()
        val notFeedToShow: TextView? = view?.findViewById(R.id.noFeedToShow)
        val constraintLayoutProgressBar: androidx.constraintlayout.widget.ConstraintLayout? = view?.findViewById(R.id.constrainLayoutProgressBar)
        if (appUser != null && appUser?.following != null){
            if(appUser?.following?.size!! > 0){
                constraintLayoutProgressBar?.visibility = View.VISIBLE
                notFeedToShow?.visibility = View.GONE
            }else{
                notFeedToShow?.visibility = View.VISIBLE
                constraintLayoutProgressBar?.visibility = View.GONE
            }
        }else{
            constraintLayoutProgressBar?.visibility = View.VISIBLE
            notFeedToShow?.visibility = View.GONE
        }

        val observer = Observer<ObserveWrapper<MutableList<AppRecipe>>> { value ->
            val content = value.getContentIfNotHandled()
            if (content != null){
                Log.d("updateFeed", "content size = ${content.size}")
                items = ArrayList(content)
                for(kas in content){
                    Log.d("updateFeed", "content name = ${kas.uid}")
                }
                homeAdapter.setItems(items)
                if (items.size <= 0){
                    notFeedToShow?.visibility = View.VISIBLE
                    constraintLayoutProgressBar?.visibility = View.GONE
                }
//                Log.e("Profile Fragment", "${content[0]}")
//                    notifyDataSetChanged()
            }
        }
        LiveDataHolder.getFeedListLiveData().observe(viewLifecycleOwner, observer)
        appContext.uploadFeed(true)
//        if (items != null) {
        Log.e("Home fragment", items.size.toString())
    }
}
