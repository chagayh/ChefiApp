package com.postpc.chefi.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.postpc.chefi.R

class ImageHolder(view: View) : RecyclerView.ViewHolder(view) {

    var name: TextView = view.findViewById(R.id.name)
    val image: ImageView = view.findViewById(R.id.image)

}