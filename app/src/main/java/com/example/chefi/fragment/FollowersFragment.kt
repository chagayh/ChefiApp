package com.example.chefi.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.R
import com.example.chefi.adapters.FollowersAdapter

class FollowersFragment : Fragment() {

    private lateinit var recyclerViewFollowers: RecyclerView
    private lateinit var followersAdapter: FollowersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_follwers, container, false)
        recyclerViewFollowers = view.findViewById(R.id.recyclerViewFollowers)
        followersAdapter = FollowersAdapter()
        followersAdapter.setItems(ArrayList())
        recyclerViewFollowers.adapter = followersAdapter
        recyclerViewFollowers.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        return view
    }
}