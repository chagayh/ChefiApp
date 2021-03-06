package com.postpc.chefi.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.postpc.chefi.R

class FollowerHolder(view: View): RecyclerView.ViewHolder(view) {
    val image: ImageView = view.findViewById(R.id.imageViewFollower)
    val unfollowButton: TextView = view.findViewById(R.id.buttonFollower)
    val name: TextView = view.findViewById(R.id.textViewFollower)
    val username: TextView = view.findViewById(R.id.textViewFollowerSub)
}