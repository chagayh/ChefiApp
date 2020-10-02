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
import com.example.chefi.Chefi
import com.example.chefi.LiveDataHolder
import com.example.chefi.R

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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchBtn = view.findViewById(R.id.searchBtn)
        searchBtn = view.findViewById(R.id.searchBtn)
        barEditText = view.findViewById(R.id.barEditText)

        setSearchBtn()
        setUsersObserver()
        return view
    }

    private fun setSearchBtn() {
        searchBtn.setOnClickListener {
            val searchText = barEditText.text.toString()
            if (searchText.trim().isEmpty()) {
                Toast.makeText(activity, "empty search bar", Toast.LENGTH_SHORT)
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
                    for (user in content) {
                        Log.d(TAG_SEARCH_FRAGMENT, "name = ${user.name}")
                    }
                }
            })
    }
}
