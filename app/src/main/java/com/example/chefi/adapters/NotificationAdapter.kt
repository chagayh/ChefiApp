package com.example.chefi.adapters


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
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
        val constraintLayoutProgressBar: androidx.constraintlayout.widget.ConstraintLayout = fragmentView.findViewById(R.id.constrainLayoutProgressBar)

        if (items != null){
            Log.e("visibility", constraintLayoutProgressBar.visibility.toString())
            constraintLayoutProgressBar.visibility = View.VISIBLE
            if (items.size > 0){
                notNotificationToShow.visibility = View.GONE
            }else{
                notNotificationToShow.visibility = View.VISIBLE
                constraintLayoutProgressBar.visibility = View.GONE
            }
            _items.clear()
            _items.addAll(items)
            notifyDataSetChanged()
        }else{
            notNotificationToShow.visibility = View.VISIBLE
            constraintLayoutProgressBar.visibility = View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  NotificationHolder {
        val context = parent.context
        val view: View
        appContext = context.applicationContext as Chefi
        // TODO: get list
        // appContext.get
        view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false)
        return NotificationHolder(view)
    }

    override fun getItemCount(): Int {
        return _items.size
    }


    override fun onBindViewHolder(holder: NotificationHolder, position: Int) {
        holder.profileImage.setImageResource(R.drawable.guypp)
        setComponents(holder, position)
    }

    @SuppressLint("SetTextI18n")
    fun setComponents(holder: NotificationHolder, position: Int){
        val item = _items[position]
        val user: DbUser? = item.creator
        val recipe: AppRecipe? = item.recipe
        val recipeTrade: AppRecipe? = item.offeredRecipe

        Log.e("Claire1", recipe?.myReference.toString())
        Log.e("Claire2", recipeTrade?.myReference.toString())
        //images:
        if(user?.imageUrl != null){
            Picasso.with(appContext)
                .load(user.imageUrl)
                .into(holder.profileImage)
        }
        else{
            holder.profileImage.setImageResource(R.drawable.defpp)
        }

        if(recipe?.imageUrl != null){
            Picasso.with(appContext)
                .load(recipe.imageUrl)
                .into(holder.recipeImageOne)
        }
        if(recipeTrade?.imageUrl != null){
            Picasso.with(appContext)
                .load(recipeTrade.imageUrl)
                .into(holder.recipeImageTwo)
        }

        // content:
        holder.username.text =  "@${user?.userName}"
        holder.content.text = item.notificationType.toString()

        // Clicks
        holder.profileImage.setOnClickListener {
            val action = user?.let { it1 ->
                NotificationFragmentDirections.actionNotificationToProfileOther(it1)
            }
            if (action != null) {
                it.findNavController().navigate(action)
            }
        }

        if(item.notificationType != NotificationType.TRADE && item.notificationType != NotificationType.DEFAULT) {
            holder.recipeImageOne.setOnClickListener {
                if (recipe != null) {
                    val action = NotificationFragmentDirections.actionNotificationToRecipe(recipe)
                    it.findNavController().navigate(action)
                }
            }
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
                        appContext.addNotification(user.myReference!!, null, null, NotificationType.FOLLOW)
                        holder.followButton.text = "UNFOLLOW"
                    }
                }
            }
            NotificationType.TRADE -> {
                holder.linearLayout.setOnClickListener {
                    val alertDialog = AlertDialog.Builder(it.context)
                    val view = LayoutInflater.from(it.context).inflate(R.layout.dialog_trade_layout, null)
                    val imageTradeOne: ImageView = view.findViewById(R.id.imageViewTradeRecipeOne)
                    val imageTradeTwo: ImageView = view.findViewById(R.id.imageViewTradeRecipeTwo)
                    if(recipe?.imageUrl != null){
                        Picasso.with(appContext)
                            .load(recipe.imageUrl)
                            .into(imageTradeOne)
                    }
                    if(recipeTrade?.imageUrl != null){
                        Picasso.with(appContext)
                            .load(recipeTrade.imageUrl)
                            .into(imageTradeTwo)
                    }
                    alertDialog.setView(view)
                    alertDialog.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                        if (recipe != null) {
                            appContext.getCurrUser()?.myReference?.let { it1 -> appContext.addToUserPermission(recipe, it1) }
                        }
                        if (recipeTrade != null) {
                            user?.myReference?.let { it1 -> appContext.addToUserPermission(recipeTrade, it1) }
                        }
                        val action = recipe?.let {
                            NotificationFragmentDirections.actionNotificationToRecipe(it)
                        }
                        if (action != null) {
                            it.findNavController().navigate(action)
                        }
                        if (user != null) {
                            user.myReference?.let { it1 -> appContext.addNotification(it1, recipe?.myReference, recipeTrade?.myReference, NotificationType.DEFAULT) }
                        }
                    }
                    alertDialog.setNegativeButton("No"){ _: DialogInterface, _: Int -> }
                    alertDialog.show()
                }
            }

            NotificationType.DEFAULT->{
                holder.linearLayout.setOnClickListener {
                    if (recipeTrade != null) {
                        val action = NotificationFragmentDirections.actionNotificationToRecipe(recipeTrade)
                        it.findNavController().navigate(action)
                    }
                }
            }
            else ->{
                holder.recipeCardTwo.visibility = View.GONE
                holder.arrow.visibility = View.GONE
            }
        }
        holder.linearLayoutOverall.setOnLongClickListener(){
            Log.e("notificationDelete", "in setOnLongClickListener")
            _items.remove(item)
            appContext.deleteNotification(item)
            notifyDataSetChanged()
            true
        }
    }
}