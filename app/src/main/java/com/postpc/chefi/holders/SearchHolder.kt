package com.postpc.chefi.holders

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.postpc.chefi.R

class SearchHolder(view: View): RecyclerView.ViewHolder(view) {
    val image: ImageView = view.findViewById(R.id.imageViewSearch)
    val followButton: Button = view.findViewById(R.id.buttonSearch)
    val name: TextView = view.findViewById(R.id.textViewSearch)
    val username: TextView = view.findViewById(R.id.textViewSearchSub)
}