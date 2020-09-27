package com.example.chefi.holders

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.R

class NotificationHolder(view: View): RecyclerView.ViewHolder(view) {
    val profileImage: ImageView = view.findViewById(R.id.imageViewNotificationProfile)
    val profileCard: androidx.cardview.widget.CardView = view.findViewById(R.id.notificationCardProfile)
    val text: TextView = view.findViewById(R.id.textViewNotification)
    // options:
    val followButton: Button = view.findViewById(R.id.buttonNotification)
    val like: ImageView = view.findViewById(R.id.imageViewNotificationLike)
    val recipeImage: ImageView = view.findViewById(R.id.imageViewNotificationRecipe)
    val recipeCard: androidx.cardview.widget.CardView = view.findViewById(R.id.notificationCardRecipe)
}