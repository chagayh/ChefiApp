package com.postpc.chefi.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.postpc.chefi.Chefi
import com.postpc.chefi.LiveDataHolder
import com.postpc.chefi.R
import com.postpc.chefi.activities.MainActivity
import com.postpc.chefi.adapters.NotificationAdapter
import com.postpc.chefi.database.AppNotification
import com.postpc.chefi.database.DbUser


/**
 * A simple [Fragment] subclass.
 * Use the [FavoritesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NotificationFragment : Fragment() {

    val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private lateinit var recyclerViewNotification: RecyclerView
    private var loadNotificationActive: Boolean = true
    private lateinit var notificationAdapter: NotificationAdapter
    private var notifications: ArrayList<AppNotification>? = null
    private lateinit var myActivity: MainActivity
    private var appUser: DbUser? = null
    private lateinit var notNotificationToShow: TextView
    private lateinit var constraintLayoutProgressBar: androidx.constraintlayout.widget.ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_notification, container, false)

        notNotificationToShow = view.findViewById(R.id.noNotificationToShow)
        constraintLayoutProgressBar = view.findViewById(R.id.constrainLayoutProgressBar)
        notNotificationToShow.visibility = View.GONE
        constraintLayoutProgressBar.visibility = View.VISIBLE

        myActivity = activity as MainActivity
        myActivity.setNotificationBadge(0)
        appUser = appContext.getCurrUser()
        recyclerViewNotification = view.findViewById(R.id.recyclerViewNotification)
        notificationAdapter = NotificationAdapter(view)
        Log.e("Notification", notifications?.size.toString())
        recyclerViewNotification.adapter = notificationAdapter
        recyclerViewNotification.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        setNotificationObserver()
        return view
    }

    private fun setNotificationObserver() {
        LiveDataHolder.getNotificationsLiveData().observe(viewLifecycleOwner,
            Observer { value ->
//                notNotificationToShow.visibility = View.GONE
//                constraintLayoutProgressBar.visibility = View.VISIBLE
                val content = value.getContentIfNotHandled()
                if (content != null) {
                    loadNotificationActive = false
                    if (content.size == 0) {
                        notNotificationToShow.visibility = View.VISIBLE
                        constraintLayoutProgressBar.visibility = View.GONE
                    } else {
                        if (content[0].destinationRef == appContext.getCurrUser()?.myReference) {
                            notNotificationToShow.visibility = View.GONE
                            constraintLayoutProgressBar.visibility = View.GONE
                            notifications = ArrayList(content)
                            notificationAdapter.setItems(notifications)
                            printNotification(notifications!!)
                        }
                    }
                } else {
                    if (notifications != null && notifications?.size!! == 0) {
                        notNotificationToShow.visibility = View.VISIBLE
                        constraintLayoutProgressBar.visibility = View.GONE
                    }
                }
            })
    }

    private fun printNotification(notificationsL: ArrayList<AppNotification>) {
        for (not in notificationsL) {
            Log.d("printNotification", "in notification fragment not = ${not.uid}")
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("notResume", "onResume of Notification Fragment")
        notifications = appContext.getUserNotifications()
        if (!loadNotificationActive) {
            if (notifications != null) {
                Log.d("notification", "onResume notifications?.size = ${notifications?.size}")
                if (notifications?.size != 0) {
                    constraintLayoutProgressBar.visibility = View.GONE
                    notNotificationToShow.visibility = View.INVISIBLE
                } else {
                    Log.d("notification", "onResume else notifications?.size = ${notifications?.size}")
                    constraintLayoutProgressBar.visibility = View.GONE
                    notNotificationToShow.visibility = View.VISIBLE
                }
            }
        }
        Log.d("notResume", "onResume of Notification Fragment size = ${notifications?.size}")
        notificationAdapter.setItems(notifications)
    }
}
