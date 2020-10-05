package com.example.chefi.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.Chefi
import com.example.chefi.LiveDataHolder
import com.example.chefi.R
import com.example.chefi.adapters.FollowersAdapter
import com.example.chefi.adapters.SearchAdapter
import com.example.chefi.database.DbUser

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {

    val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private lateinit var searchBtn: ImageButton
    private lateinit var barEditText: EditText
    private val TAG_SEARCH_FRAGMENT: String = "searchFragment"
    private var usersList : ArrayList<DbUser>? = null
    private lateinit var recyclerViewSearch: RecyclerView
    private lateinit var searchAdapter: SearchAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        searchBtn = view.findViewById(R.id.searchBtn)
        barEditText = view.findViewById(R.id.barEditText)
        setUsersObserver()
        setSearchBtn()
        recyclerViewSearch = view.findViewById(R.id.recyclerViewSearch)
        searchAdapter = SearchAdapter()
        recyclerViewSearch.adapter = searchAdapter
        recyclerViewSearch.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        searchAdapter.setItems(usersList)
        return view
    }

    private fun setSearchBtn() {
        searchBtn.setOnClickListener {
            val searchText = barEditText.text.toString()
            if (searchText.trim().isEmpty()) {
                Toast.makeText(activity, "Empty search bar", Toast.LENGTH_SHORT)
                    .show()
            } else {
                appContext.fireBaseSearchUsers(searchText)
            }
        }
    }

    private fun setUsersObserver() {
        LiveDataHolder.getUsersListLiveData().observe(viewLifecycleOwner,
            Observer { usersListWrapper ->
                val content = usersListWrapper.getContentIfNotHandled()
                if (content != null) {
                    usersList = ArrayList(content)
                    for (user in content) {
                        Log.d(TAG_SEARCH_FRAGMENT, "name = ${user.name}")
                    }
                    searchAdapter.setItems(usersList)
                }
            })
    }
}
