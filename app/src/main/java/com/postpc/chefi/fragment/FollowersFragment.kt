package com.postpc.chefi.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.postpc.chefi.Chefi
import com.postpc.chefi.R
import com.postpc.chefi.adapters.FollowersAdapter
import com.postpc.chefi.database.DbUser
import kotlin.collections.ArrayList
import androidx.lifecycle.Observer
import com.postpc.chefi.LiveDataHolder
import com.postpc.chefi.ObserveWrapper


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
        followersAdapter = FollowersAdapter(isFollowers, curUser, view)
        recyclerViewFollowers.adapter = followersAdapter
        recyclerViewFollowers.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        if (curUser == null){
            if(isFollowers) {
                followersAdapter.setItems(appContext.getUserFollowers())
            }else{
                followersAdapter.setItems(appContext.getUserFollowing())
            }
        }else{
            if(isFollowers) {
                appContext.loadFollowers(curUser)
            }else{
                appContext.loadFollowing(curUser)
            }
            val observer = Observer<ObserveWrapper<MutableList<DbUser>>> { value ->
                val content = value.getContentIfNotHandled()
                if (content != null){
                    followersAdapter.setItems(ArrayList(content))
                }
                else{
                    followersAdapter.setItems(null)
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