package com.postpc.chefi.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.postpc.chefi.Chefi
import com.postpc.chefi.R
import com.postpc.chefi.database.DbUser
import com.postpc.chefi.database.NotificationType
import com.postpc.chefi.fragment.HomeFragmentDirections
import com.postpc.chefi.fragment.SearchFragmentDirections
import com.postpc.chefi.holders.SearchHolder
import com.squareup.picasso.Picasso


class SearchAdapter(private val fragmentView: View): RecyclerView.Adapter<SearchHolder>() {

    private lateinit var appContext: Chefi
    private var _items: ArrayList<DbUser>? = ArrayList()

    // public method to show a new list of items
    fun setItems(items: ArrayList<DbUser>?){
        val notSearchResultToShow: TextView = fragmentView.findViewById(R.id.noSearchResultToShow)
        if (items != null) {
            if (items.size > 0){
                notSearchResultToShow.visibility = View.GONE
            }
            _items?.clear()
            _items?.addAll(items)
            notifyDataSetChanged()
        }else{
            notSearchResultToShow.visibility = View.VISIBLE
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
                appContext.addNotification(item.myReference!!, null, null, NotificationType.FOLLOW)
                holder.followButton.text = "UNFOLLOW"
            }
        }
    }
}