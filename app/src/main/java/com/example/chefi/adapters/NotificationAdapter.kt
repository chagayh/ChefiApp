package com.example.chefi.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.database.AppNotification
import com.example.chefi.database.AppRecipe
import com.example.chefi.database.DbUser
import com.example.chefi.database.NotificationType
import com.example.chefi.fragment.NotificationFragmentDirections
import com.example.chefi.holders.NotificationHolder
import com.squareup.picasso.Picasso

class NotificationAdapter(private val fragmentView: View): RecyclerView.Adapter<NotificationHolder>() {

    private lateinit var appContext: Chefi
    private var _items: ArrayList<AppNotification> = ArrayList()

    fun setItems(items: ArrayList<AppNotification>?){
        val notNotificationToShow: TextView = fragmentView.findViewById(R.id.noNotificationToShow)
        if (items != null){
            if (items.size > 0){
                notNotificationToShow.visibility = View.GONE
            }
            _items.clear()
            _items.addAll(items)
            notifyDataSetChanged()
        }else{
            notNotificationToShow.visibility = View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  NotificationHolder {
        val context = parent.context
        val view: View
        appContext = context.applicationContext as Chefi
        // TODO: get list
        view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false)
        return NotificationHolder(view)
    }

    override fun getItemCount(): Int {
        return _items.size
//        return 35
    }


    override fun onBindViewHolder(holder: NotificationHolder, position: Int) {
        holder.profileImage.setImageResource(R.drawable.guypp)
    }

    fun setComponents(holder: NotificationHolder, position: Int){
        val item = _items[position]
        val user: DbUser? = item.creator
        val recipe: AppRecipe = item.recipe!!

        if(user?.imageUrl != null){
            Picasso.with(appContext)
                .load(user.imageUrl)
                .into(holder.profileImage)
        }
        else{
            holder.profileImage.setImageResource(R.drawable.defpp)
        }

        holder.profileImage.setOnClickListener {
            val action = NotificationFragmentDirections.actionNotificationToRecipe(recipe)
            it.findNavController().navigate(action)
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
                // TODO: nav graph and pop up msg
            }
            else ->{
                holder.recipeCardTwo.visibility = View.GONE
                holder.arrow.visibility = View.GONE
            }
        }
    }
}