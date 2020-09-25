package com.example.chefi.holders

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.R

class ProfileHeaderHolder(view: View): RecyclerView.ViewHolder(view) {
    val _view = view
    val linearLayoutAboutMe: LinearLayout = view.findViewById(R.id.aboutme)
    val aboutMeTextView:TextView = view.findViewById(R.id.aboutMeTextView)
    val nameTextView: TextView = view.findViewById(R.id.nameTextView)
    val favorites: LinearLayout = view.findViewById(R.id.favorites)
    val recipes: LinearLayout = view.findViewById(R.id.recipes)
    val favoritesButton: TextView = view.findViewById(R.id.favoritesbtn)
    val recipesButton: TextView = view.findViewById(R.id.recipesbtn)
    val greyColor = view.resources.getColor(R.color.grey)
    val blueColor = view.resources.getColor(R.color.blue)
    // Buttons:
    val editAboutMe:TextView = view.findViewById(R.id.textViewAboutMeEdit)
    val editMainCard:TextView = view.findViewById(R.id.TextViewEditMainCard)
    val followButton:TextView = view.findViewById(R.id.ButtonFollow)
    val followersButton:TextView = view.findViewById(R.id.ButtonFollowers)

//    val aboutMeEdit: TextView = view.findViewById(Te)

//    var wrapSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//    var chagaipp = view.measure(wrapSpec, wrapSpec)
    val l = view.resources.displayMetrics
    val kas = linearLayoutAboutMe.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    val factor = view.resources.displayMetrics.density
    val k = linearLayoutAboutMe.measuredWidth / factor
}