package com.example.chefi.holders

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.R

class HomeHolder(view: View): RecyclerView.ViewHolder(view) {
    val userImage: ImageView = view.findViewById(R.id.recipeHomeUserImageView)
    val userNameUp: TextView = view.findViewById(R.id.textViewRecipeHome)
    val recipeImage: ImageView = view.findViewById(R.id.recipeHomeRecipeImageView)
    val likesTitle: TextView = view.findViewById(R.id.textViewLikes)
    val userNameDown: TextView = view.findViewById(R.id.textViewUsername)
    val postDescription: TextView = view.findViewById(R.id.textViewContent)
    val commentTitle: TextView = view.findViewById(R.id.textViewCommentTitle)
    val commentOneUsername: TextView = view.findViewById(R.id.textViewCommentOneUsername)
    val commentOneContent: TextView = view.findViewById(R.id.textViewCommentOneContent)
    val commentTwoUsername: TextView = view.findViewById(R.id.textViewCommentTwoUsername)
    val commentTwoContent: TextView = view.findViewById(R.id.textViewCommentTwoContent)
    val commentPostBtn: TextView = view.findViewById(R.id.textViewPostCommentBtn)
}