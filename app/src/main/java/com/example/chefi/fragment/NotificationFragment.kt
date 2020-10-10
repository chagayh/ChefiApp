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
import com.example.chefi.activities.MainActivity
import com.example.chefi.adapters.FollowersAdapter
import com.example.chefi.adapters.NotificationAdapter


/**
 * A simple [Fragment] subclass.
 * Use the [FavoritesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NotificationFragment : Fragment() {

    val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private lateinit var recyclerViewNotification: RecyclerView
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_notification, container, false)
        val myActivity = activity as MainActivity
        myActivity.setNotificationBadge(0)
        recyclerViewNotification = view.findViewById(R.id.recyclerViewNotification)
        notificationAdapter = NotificationAdapter(view)
        // TODO - get items from chagay
        // appContext.getUserNotifications()
        val notifications = appContext.getUserNotifications()
        Log.e("Notification", notifications?.size.toString())
        notificationAdapter.setItems(notifications)
        recyclerViewNotification.adapter = notificationAdapter
        recyclerViewNotification.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        return view
    }
}
