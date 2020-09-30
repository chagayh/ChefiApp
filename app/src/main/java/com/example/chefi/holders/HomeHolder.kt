package com.example.chefi.holders

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.R

class HomeHolder(view: View): RecyclerView.ViewHolder(view) {
    val userImage: ImageView = view.findViewById(R.id.recipeHomeUserImageView)
    val userTitleName: TextView = view.findViewById(R.id.textViewRecipeHome)
    val recipeImage: ImageView = view.findViewById(R.id.recipeHomeRecipeImageView)
    val likesTitle: TextView = view.findViewById(R.id.textViewLikes)
    val userNameSubTitle: TextView = view.findViewById(R.id.textViewUsername)
    val postContent: TextView = view.findViewById(R.id.textViewContent)
}