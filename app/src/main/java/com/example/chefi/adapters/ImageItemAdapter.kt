package com.example.chefi.adapters

import android.content.Context
import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.R
import com.example.chefi.database.MyImage
import com.example.chefi.database.Recipe
import com.example.chefi.holders.ImageHolder
import com.squareup.picasso.Picasso

class ImageItemAdapter : RecyclerView.Adapter<ImageHolder>() {
    private val items: MutableList<Recipe> = ArrayList()
    private lateinit var context: Context
    private val TAG_ADAPTER = "imageItemAdapter"

    fun setItems(items: MutableList<Recipe>) {
        items.clear()
        items.addAll(items)
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
        holder.name.text = item.name
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