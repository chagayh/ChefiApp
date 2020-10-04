package com.example.chefi.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.database.AppRecipe
import com.example.chefi.database.Comment
import com.example.chefi.holders.CommentHolder
import com.example.chefi.holders.FollowerHolder
import com.squareup.picasso.Picasso


class CommentAdapter(curRecipe: AppRecipe?): RecyclerView.Adapter<CommentHolder>() {

    private lateinit var appContext: Chefi
    private var _items: ArrayList<Comment> = ArrayList()
    private val _curRecipe = curRecipe

    // public method to show a new list of items
    fun setItems(items: ArrayList<Comment>?){
        _items.clear()
        if (items != null) {
            _items.addAll(items)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  CommentHolder {
        val context = parent.context
        val view: View
        appContext = context.applicationContext as Chefi
        view = LayoutInflater.from(context).inflate(R.layout.comment, parent, false)
        return CommentHolder(view)
    }

    override fun getItemCount(): Int {
//        return _items.size
        return 15
    }


    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
//        val item = _items[position]
//        holder.name.text = item.name
//        holder.username.text = "@" + item.userName
//        holder.content.text = item.commentContent
//        holder.timestamp.text = item.timestamp.toString()
    }
}