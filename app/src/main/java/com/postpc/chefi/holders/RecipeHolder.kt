package com.postpc.chefi.holders

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.postpc.chefi.R

// we need to create a view holder that extends RecyclerView.ViewHolder
// and stores as fields all the views we would need to use in the adapter
class RecipeHolder(view: View): RecyclerView.ViewHolder(view) {
    val _image: ImageView = view.findViewById(R.id.imageViewRecipe)
    val _progressBar: androidx.constraintlayout.widget.ConstraintLayout = view.findViewById(R.id.constrainLayoutProgressBar)
}