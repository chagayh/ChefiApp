package com.example.chefi.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.listeners.RecipeClickListener
import com.example.chefi.database.User
import com.example.chefi.holders.FollowerHolder
import com.squareup.picasso.Picasso

class FollowersAdapter(): RecyclerView.Adapter<FollowerHolder>() {

    private lateinit var appContext: Chefi
    private var _items: ArrayList<User> = ArrayList()

    // public method to show a new list of items
    fun setItems(items: ArrayList<User>){
        _items.clear()
        _items.addAll(items)
        notifyDataSetChanged()
    }

    // here we need to create a view holder
    // steps: 1. hold a LayoutInflater
    //        2. inflate a view
    //        3. wrap it with a view holder and return it
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  FollowerHolder {
        val context = parent.context
        val view: View
        appContext = context.applicationContext as Chefi
        // TODO: get list
//        _items = appContext.getUserFollowers()
        view = LayoutInflater.from(context).inflate(R.layout.follower, parent, false)
        return FollowerHolder(view)
    }

    override fun getItemCount(): Int {
//        return _items.size
        return 35
    }


    override fun onBindViewHolder(holder: FollowerHolder, position: Int) {
        holder.image.setImageResource(R.drawable.chagaipp)
//        val item = _items[position]
//        holder.name.text = item.name
//        Picasso.with(appContext) // ToDO: check with Chagai if appContext it's OK
//            .load(item.imageUrl)
//            .into(holder.image)
        holder.followButton.setOnClickListener {

        }
    }
}