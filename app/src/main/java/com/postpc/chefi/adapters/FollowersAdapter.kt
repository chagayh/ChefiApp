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
import com.postpc.chefi.fragment.FollowersFragmentDirections
import com.postpc.chefi.holders.FollowerHolder
import com.squareup.picasso.Picasso


class FollowersAdapter(val isFollowers: Boolean, val curDbUser:DbUser?, private val fragmentView: View): RecyclerView.Adapter<FollowerHolder>() {

    private lateinit var appContext: Chefi
    private var _items: ArrayList<DbUser> = ArrayList()

    // public method to show a new list of items
    fun setItems(items: ArrayList<DbUser>?){
        val notFollowersToShow: TextView = fragmentView.findViewById(R.id.noFollowersToShow)
        val notFollowingToShow: TextView = fragmentView.findViewById(R.id.noFollowingToShow)
        if (items != null){
            if (items.size > 0){
                notFollowersToShow.visibility = View.GONE
                notFollowingToShow.visibility = View.GONE
            }else{
                if(isFollowers){
                    notFollowersToShow.visibility = View.VISIBLE
                    notFollowingToShow.visibility = View.GONE
                }else{
                    notFollowersToShow.visibility = View.GONE
                    notFollowingToShow.visibility = View.VISIBLE
                }
            }
            _items.clear()
            _items.addAll(items)
            notifyDataSetChanged()
        }else{
            if(isFollowers){
                notFollowersToShow.visibility = View.VISIBLE
                notFollowingToShow.visibility = View.GONE
            }else{
                notFollowersToShow.visibility = View.GONE
                notFollowingToShow.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  FollowerHolder {
        val context = parent.context
        val view: View
        appContext = context.applicationContext as Chefi
        view = LayoutInflater.from(context).inflate(R.layout.item_follower, parent, false)
        return FollowerHolder(view)
    }

    override fun getItemCount(): Int {
        return _items.size
    }


    override fun onBindViewHolder(holder: FollowerHolder, position: Int) {
        val item = _items[position]
        // details:
        holder.name.text = item.name
        holder.username.text = "@" + item.userName
        if(item.imageUrl != null) {
            Picasso.with(appContext)
                .load(item.imageUrl)
                .into(holder.image)
        }else{
            holder.image.setImageResource(R.drawable.defpp)
        }
        // Follow btn:
        if (item.myReference?.let { it1 -> appContext.isFollowedByMe(it1) }!!){
            holder.unfollowButton.text = "UNFOLLOW"
        }else{
            holder.unfollowButton.text = "FOLLOW"
        }
        // On click Listeners:
        holder.image.setOnClickListener {
            val action = FollowersFragmentDirections.actionFollowersToProfileOther(item)
            it.findNavController().navigate(action)
        }

        holder.unfollowButton.setOnClickListener {
            if (item.myReference?.let { it1 -> appContext.isFollowedByMe(it1) }!!){
                appContext.unFollow(item)
                holder.unfollowButton.text = "FOLLOW"
            }else{
                appContext.follow(item)
                appContext.addNotification(item.myReference!!, null, null, NotificationType.FOLLOW)
                holder.unfollowButton.text = "UNFOLLOW"
            }
        }
    }
}