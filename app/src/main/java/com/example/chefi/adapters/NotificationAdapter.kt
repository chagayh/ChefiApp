package com.example.chefi.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.database.AppNotification
import com.example.chefi.database.DbUser
import com.example.chefi.database.NotificationType
import com.example.chefi.holders.NotificationHolder
import com.squareup.picasso.Picasso

class NotificationAdapter(): RecyclerView.Adapter<NotificationHolder>() {

    private lateinit var appContext: Chefi
    private var _items: ArrayList<AppNotification> = ArrayList()

    fun setItems(items: ArrayList<AppNotification>){
        _items.clear()
        _items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  NotificationHolder {
        val context = parent.context
        val view: View
        appContext = context.applicationContext as Chefi
        // TODO: get list
//        _items = appContext.getUserFollowers()
        view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false)
        return NotificationHolder(view)
    }

    override fun getItemCount(): Int {
//        return _items.size
        return 35
    }


    override fun onBindViewHolder(holder: NotificationHolder, position: Int) {
        holder.profileImage.setImageResource(R.drawable.guypp)
    }

    fun setComponents(holder: NotificationHolder, position: Int){
        val item = _items[position]
        val user: DbUser? = item.creator

        if(user?.imageUrl != null){
            Picasso.with(appContext)
                .load(user.imageUrl)
                .into(holder.profileImage)
        }
        else{
            holder.profileImage.setImageResource(R.drawable.defpp)
        }

        when(item.notificationType){
            NotificationType.FOLLOW -> {
                holder.followButton.visibility = View.VISIBLE
                holder.linearLayout.visibility = View.GONE
                if (user?.myReference?.let { it1 -> appContext.isFollowedByMe(it1) }!!){
                    holder.followButton.text = "UNFOLLOW"
                }else{
                    holder.followButton.text = "FOLLOW"
                }
                holder.followButton.setOnClickListener {
                    if (user.myReference?.let { it1 -> appContext.isFollowedByMe(it1) }!!){
                        appContext.unFollow(user)
                        holder.followButton.text = "FOLLOW"
                    }else{
                        appContext.follow(user)
                        holder.followButton.text = "UNFOLLOW"
                    }
                }
            }
            NotificationType.TRADE -> {
                // TODO: nav graph
            }
            else ->{
                holder.recipeCardTwo.visibility = View.GONE
                holder.arrow.visibility = View.GONE
                // TODO: nav graph
            }
        }
    }
}