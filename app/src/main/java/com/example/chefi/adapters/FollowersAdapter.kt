package com.example.chefi.adapters


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.database.DbUser
import com.example.chefi.fragment.FollowersFragmentDirections
import com.example.chefi.holders.FollowerHolder
import com.squareup.picasso.Picasso


class FollowersAdapter(isFollowers: Boolean, curDbUser:DbUser?): RecyclerView.Adapter<FollowerHolder>() {

    private lateinit var appContext: Chefi
    private var _items: ArrayList<DbUser> = ArrayList()
    private val _curUser = curDbUser
    private  val _isFollowers = isFollowers

    // public method to show a new list of items
    fun setItems(items: ArrayList<DbUser>?){
        Log.e("FA", items?.size.toString())
        _items.clear()
        if (items != null) {
            _items.addAll(items)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  FollowerHolder {
        val context = parent.context
        val view: View
        appContext = context.applicationContext as Chefi
        view = LayoutInflater.from(context).inflate(R.layout.follower, parent, false)
        return FollowerHolder(view)
    }

    override fun getItemCount(): Int {
        return _items.size
    }


    override fun onBindViewHolder(holder: FollowerHolder, position: Int) {
        if (_isFollowers){
            holder.unfollowButton.text = "Follow"
        }
        val item = _items[position]
        holder.name.text = item.name
        holder.username.text = "@" + item.userName
        if(item.imageUrl != null) {
            Picasso.with(appContext)
                .load(item.imageUrl)
                .into(holder.image)
        }else{
            holder.image.setImageResource(R.drawable.defpp)
        }
        holder.image.setOnClickListener {
            val action = FollowersFragmentDirections.actionFollowersToProfileOther(item)
            it.findNavController().navigate(action)
        }
    }
}