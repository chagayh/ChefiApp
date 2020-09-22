package com.example.chefi.holders

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.R

// we need to create a view holder that extends RecyclerView.ViewHolder
// and stores as fields all the views we would need to use in the adapter
class RecipeHolder(view: View): RecyclerView.ViewHolder(view) {
    val _image: ImageView = view.findViewById(R.id.imageViewRecipe)
    val _card: androidx.cardview.widget.CardView = view.findViewById(R.id.recipeCard)
}