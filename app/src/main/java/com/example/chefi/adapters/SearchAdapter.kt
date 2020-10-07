package com.example.chefi.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.database.AppRecipe
import com.example.chefi.database.DbUser
import com.example.chefi.fragment.HomeFragmentDirections
import com.example.chefi.fragment.SearchFragmentDirections
import com.example.chefi.holders.SearchHolder
import com.squareup.picasso.Picasso


class SearchAdapter(): RecyclerView.Adapter<SearchHolder>() {

    private lateinit var appContext: Chefi
    private var _items: ArrayList<DbUser>? = ArrayList()

    // public method to show a new list of items
    fun setItems(items: ArrayList<DbUser>?){
        _items?.clear()
        if (items != null) {
            _items?.addAll(items)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  SearchHolder {
        val context = parent.context
        val view: View
        appContext = context.applicationContext as Chefi
        view = LayoutInflater.from(context).inflate(R.layout.item_search_result, parent, false)
        return SearchHolder(view)
    }

    override fun getItemCount(): Int {
        return _items?.size!!
    }


    override fun onBindViewHolder(holder: SearchHolder, position: Int) {
        val item = _items?.get(position)
        // details
        if (item != null) {
            holder.name.text = item.name
            holder.username.text = "@" + item.userName
            if(item.imageUrl != null) {
                Picasso.with(appContext)
                    .load(item.imageUrl)
                    .into(holder.image)
            }else{
                holder.image.setImageResource(R.drawable.defpp)
            }
        }
        // follow btn
        if (item?.myReference?.let { it1 -> appContext.isFollowedByMe(it1) }!!){
            holder.followButton.text = "UNFOLLOW"
        }else{
            holder.followButton.text = "FOLLOW"
        }

        if(item.myReference == appContext.getCurrUser()?.myReference) holder.followButton.visibility = View.GONE

        // on click listeners
        holder.image.setOnClickListener {
            val action = item.let { it1 -> SearchFragmentDirections.actionSearchToProfileOther(it1) }
            it.findNavController().navigate(action)
        }

        holder.followButton.setOnClickListener {
            if (item.myReference?.let { it1 -> appContext.isFollowedByMe(it1) }!!){
                appContext.unFollow(item)
                holder.followButton.text = "FOLLOW"
            }else{
                appContext.follow(item)
                holder.followButton.text = "UNFOLLOW"
            }
        }
    }
}