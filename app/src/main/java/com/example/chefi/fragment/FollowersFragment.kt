package com.example.chefi.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.adapters.FollowersAdapter
import com.example.chefi.database.User
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates
import androidx.lifecycle.Observer
import com.example.chefi.LiveDataHolder


class FollowersFragment : Fragment() {

    val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private val args: FollowersFragmentArgs by navArgs()
    private lateinit var recyclerViewFollowers: RecyclerView
    private lateinit var followersAdapter: FollowersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_follwers, container, false)
        val isFollowers = args.isFollowers
        val curUser = args.curUser
        recyclerViewFollowers = view.findViewById(R.id.recyclerViewFollowers)
        followersAdapter = FollowersAdapter(isFollowers, curUser)
        recyclerViewFollowers.adapter = followersAdapter
        recyclerViewFollowers.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        if (curUser == null){
            if(isFollowers) {
                followersAdapter.setItems(appContext.getUserFollowers())
            }else{
                followersAdapter.setItems(appContext.getUserFollowing())
            }
        }else{
            // TODO: observer
            Log.e("Amnon1", curUser.name.toString())
            Log.e("Amnon2", isFollowers.toString())
            if(isFollowers) {
                appContext.loadFollowers(curUser)
            }else{
                appContext.loadFollowing(curUser)
            }
            val observer = Observer<MutableList<User>> { value ->
                if (value != null){
                    Log.e("Amnon3", value.size.toString())
                    followersAdapter.setItems(ArrayList(value))
                }
                else{
                    followersAdapter.setItems(ArrayList())
                }
            }
            LiveDataHolder.getUsersListLiveData().observe(viewLifecycleOwner, observer)
        }
        return view
    }

    override fun onPause() {
        super.onPause()
        followersAdapter.setItems(ArrayList())
    }


}