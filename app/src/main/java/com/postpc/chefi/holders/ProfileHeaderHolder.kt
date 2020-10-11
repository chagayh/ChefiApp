package com.postpc.chefi.holders

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.postpc.chefi.R

class ProfileHeaderHolder(view: View): RecyclerView.ViewHolder(view) {
    val aboutMeTextView:TextView = view.findViewById(R.id.aboutMeTextView)
    val nameTextView: TextView = view.findViewById(R.id.nameTextView)
    val usernameTextView: TextView = view.findViewById(R.id.usernameTextView)
    val menuLinear: LinearLayout = view.findViewById(R.id.menuLinearLayout)
    val followMenuLinear: LinearLayout = view.findViewById(R.id.followMenuLinearLayout)
    val favoritesButton: TextView = view.findViewById(R.id.favoritesbtn)
    val recipesButton: TextView = view.findViewById(R.id.recipesbtn)
    val greyColor = view.resources.getColor(R.color.grey)
    val blueColor = view.resources.getColor(R.color.blue)
    val image: ImageView = view.findViewById(R.id.profileImageView)
    // Buttons:
    val editAboutMe:TextView = view.findViewById(R.id.textViewAboutMeEdit)
    val editMainCard:TextView = view.findViewById(R.id.TextViewEditMainCard)
    val followingButton:TextView = view.findViewById(R.id.ButtonFollowing)
    val followersButton:TextView = view.findViewById(R.id.ButtonFollowers)
    val followButton:TextView = view.findViewById(R.id.followBtn)
}