package com.example.chefi.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.R
import com.example.chefi.database.AppRecipe
import com.example.chefi.database.DbRecipe
import com.example.chefi.holders.ImageHolder
import com.squareup.picasso.Picasso

class ImageItemAdapter : RecyclerView.Adapter<ImageHolder>() {
    private val items: MutableList<AppRecipe> = ArrayList()
    private lateinit var context: Context
    private val TAG_ADAPTER = "imageItemAdapter"

    fun setItems(newItems: MutableList<AppRecipe>) {
        items.clear()
        items.addAll(newItems)
        Log.d(TAG_ADAPTER, "items size = ${items.size}")
//        Log.d(TAG_ADAPTER, "items size = ${items.size}")
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        context = parent.context
        val view = LayoutInflater.from(context)
            .inflate(R.layout.image_item, parent, false)
        return ImageHolder(view)
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        val item = items[position]
        Log.d(TAG_ADAPTER, "item url = ${item.imageUrl}")
        holder.name.text = item.description
        Picasso.with(context)
            .load(item.imageUrl)
            .fit()
            .centerCrop()
            .into(holder.image)
    }

    override fun getItemCount(): Int {
        return items.size
    }


}