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
import com.example.chefi.R
import com.example.chefi.adapters.HomeAdapter

class HomeFragment : Fragment() {

    val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private lateinit var recyclerViewHome: RecyclerView
    private lateinit var homeAdapter: HomeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerViewHome = view.findViewById(R.id.recyclerViewHome)
        homeAdapter = HomeAdapter(viewLifecycleOwner)
        homeAdapter.setItems(ArrayList())
        recyclerViewHome.adapter = homeAdapter
        recyclerViewHome.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        val items= appContext.getUserRecipes()
        if (items != null) {
            Log.e("Home fragment", items.size.toString())
            homeAdapter.setItems(items)
        }
        return view
    }
}
