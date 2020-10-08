package com.example.chefi.adapters


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.database.AppRecipe
import com.example.chefi.database.Comment
import com.example.chefi.holders.CommentHolder
import com.example.chefi.holders.FollowerHolder
import com.squareup.picasso.Picasso


class CommentAdapter(curRecipe: AppRecipe?, private val fragmentView: View): RecyclerView.Adapter<CommentHolder>() {

    private lateinit var appContext: Chefi
    private var _items: ArrayList<Comment> = ArrayList()
    private val _curRecipe = curRecipe

    // public method to show a new list of items
    fun setItems(items: ArrayList<Comment>?){
        val notCommentsToShow: TextView = fragmentView.findViewById(R.id.noCommentsToShow)
        if (items != null){
            if (items.size > 0){
                notCommentsToShow.visibility = View.GONE
            }
            _items.clear()
            _items.addAll(items)
            notifyDataSetChanged()
        }else{
            notCommentsToShow.visibility = View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  CommentHolder {
        val context = parent.context
        val view: View
        appContext = context.applicationContext as Chefi
        view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false)
        return CommentHolder(view)
    }

    override fun getItemCount(): Int {
        return _items.size
    }


    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        val item = _items[position]
        holder.name.text = item.name
        holder.username.text = "@" + item.userName
        holder.content.text = item.commentContent
        holder.timestamp.text = item.timestamp.toString()
    }
}