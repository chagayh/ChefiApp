package com.example.chefi.holders

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.R

class NotificationHolder(view: View): RecyclerView.ViewHolder(view) {
    val profileImage: ImageView = view.findViewById(R.id.imageViewNotificationProfile)
    val profileCard: androidx.cardview.widget.CardView = view.findViewById(R.id.notificationCardProfile)
    val content: TextView = view.findViewById(R.id.textViewNotificationContent)
    val username: TextView = view.findViewById(R.id.textViewNotificationUsername)
    // options:
    val followButton: Button = view.findViewById(R.id.buttonNotification)
    val recipeCardOne: androidx.cardview.widget.CardView = view.findViewById(R.id.notificationCardRecipeOne)
    val recipeCardTwo: androidx.cardview.widget.CardView = view.findViewById(R.id.notificationCardRecipeTwo)
    val recipeImageOne: ImageView = view.findViewById(R.id.imageViewNotificationRecipeOne)
    val recipeImageTwo: ImageView = view.findViewById(R.id.imageViewNotificationRecipeTwo)
    val arrow: ImageView = view.findViewById(R.id.imageViewArrow)
    val linearLayout: LinearLayout = view.findViewById(R.id.linearLayoutNotificationTrade)
    val linearLayoutOverall: androidx.constraintlayout.widget.ConstraintLayout = view.findViewById(R.id.innerConstraintNotifiaction)
}