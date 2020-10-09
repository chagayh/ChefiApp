package com.example.chefi.adapters


import android.app.AlertDialog
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.Chefi
import com.example.chefi.LiveDataHolder
import com.example.chefi.ObserveWrapper
import com.example.chefi.R
import com.example.chefi.database.AppRecipe
import com.example.chefi.database.DbUser
import com.example.chefi.fragment.HomeFragmentDirections
import com.example.chefi.holders.HomeHolder
import com.squareup.picasso.Picasso
import androidx.lifecycle.Observer
import com.example.chefi.database.Comment
import com.example.chefi.database.NotificationType
import java.util.*
import kotlin.collections.ArrayList

class HomeAdapter(val viewLifecycleOwner: LifecycleOwner, private val fragmentView: View) : RecyclerView.Adapter<HomeHolder>() {

    private lateinit var appContext: Chefi
    private lateinit var observer: Observer<ObserveWrapper<MutableList<AppRecipe>>>
    private var _items: ArrayList<AppRecipe> = ArrayList()
    private val likeMsg = "%s people likes this recipe"
    private val commentMsg = "View all %s comments"
    private lateinit var appUser: DbUser

    // public method to show a new list of items
    fun setItems(items: ArrayList<AppRecipe>?){
        val notFeedToShow: TextView = fragmentView.findViewById(R.id.noFeedToShow)
        val constraintLayoutProgressBar: androidx.constraintlayout.widget.ConstraintLayout = fragmentView.findViewById(R.id.constrainLayoutProgressBar)
        //s
        if (items != null){
            Log.e("visibility", constraintLayoutProgressBar.visibility.toString())
            constraintLayoutProgressBar.visibility = View.VISIBLE
            if (items.size > 0){
                notFeedToShow.visibility = View.GONE
            }
            _items.clear()
            _items.addAll(items)
            notifyDataSetChanged()
        }else{
            notFeedToShow.visibility = View.VISIBLE
            constraintLayoutProgressBar.visibility = View.GONE
        }
    }

    // here we need to create a view holder
    // steps: 1. hold a LayoutInflater
    //        2. inflate a view
    //        3. wrap it with a view holder and return it
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  HomeHolder {
        val context = parent.context
        val view: View
        appContext = context.applicationContext as Chefi
        appUser = appContext.getCurrUser()!!
        view = LayoutInflater.from(context).inflate(R.layout.item_recipe_home, parent, false)

        observer = Observer<ObserveWrapper<MutableList<AppRecipe>>> { value ->
            val items = ArrayList<AppRecipe>(_items)
            val content = value.getContentIfNotHandled()
            if (content != null){
                Log.d("HomeAdapterObserver", "content size = ${content.size}")
                items.union(ArrayList(content))
                for(kas in content){
                    Log.d("HomeAdapterObserver", "content name = ${kas.uid}")
                }
                Log.d("HomeAdapterObserver", "items size = ${items.size}")
                setItems(items)
            }
        }


        return HomeHolder(view)
    }

    override fun getItemCount(): Int {
        return _items.size
    }


    override fun onBindViewHolder(holder: HomeHolder, position: Int) {
        customizeComponents(holder, position)
//        updateFeedRoutine(position)
    }

    private fun updateFeedRoutine(position: Int){
        Log.e("Position", "$position")
        if(position % 5 == 2){
            appContext.uploadFeed(false)
            LiveDataHolder.getFeedListLiveData().observe(viewLifecycleOwner, observer)
            Log.e("Home fragment", _items.size.toString())
        }
    }

    private fun customizeComponents(holder:HomeHolder, position: Int){
        val item = _items[position]
        val curUser: DbUser? = item.owner
        Log.e("HomeAdapter", curUser?.name.toString())
        // user details:
        if(curUser != null){
            holder.userNameUp.text = curUser.name
            holder.userNameDown.text = curUser.userName
            if(curUser.imageUrl != null){
                Picasso.with(appContext)
                    .load(curUser.imageUrl)
                    .into(holder.userImage)
            }
            else{
                holder.userImage.setImageResource(R.drawable.defpp)
            }
        }
        // recipe details
        if(item.imageUrl != null){
            Picasso.with(appContext)
                .load(item.imageUrl)
                .into(holder.recipeImage)
        }
        else{
            holder.userImage.setImageResource(R.drawable.pasta3)
        }
        holder.postDescription.text = item.description
        holder.likesTitle.text = String.format(holder.likesTitle.text.toString(), item.likes)
        if (item.status != item.TRADE_STATUS) holder.forTrade.visibility = View.GONE
        if (appUser.favorites != null){
            if(appUser.favorites?.contains(item.myReference)!!) holder.favoritesImage.visibility = View.VISIBLE
        }

        // comment details
        if (item.comments != null){
            holder.commentTitle.text = String.format(holder.commentTitle.text.toString(), item.comments!!.size)
            setNavigateToCommentComponents(holder, item)
            if(item.comments!!.size > 0)
            {
                var tempComment = item.comments!![0]
                holder.commentOneUsername.text = tempComment.userName
                holder.commentOneContent.text = tempComment.commentContent
                if (item.comments!!.size > 1){
                    tempComment = item.comments!![1]
                    holder.commentTwoUsername.text = tempComment.userName
                    holder.commentTwoContent.text = tempComment.commentContent
                }
            }
        }
        setAddCommentButton(holder, item, position)
        if (curUser != null) {
            setNavigateToProfileComponents(holder, curUser)
        }
        setOtherClicks(holder, position)
    }

    private fun setNavigateToProfileComponents(holder: HomeHolder, dbUser: DbUser){
        holder.userImage.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeToProfileOther(dbUser)
            it.findNavController().navigate(action)
        }
        holder.userNameUp.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeToProfileOther(dbUser)
            it.findNavController().navigate(action)
        }
        holder.userNameDown.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeToProfileOther(dbUser)
            it.findNavController().navigate(action)
        }
    }

    private fun setNavigateToCommentComponents(holder: HomeHolder, appRecipe: AppRecipe?){
        holder.commentTitle.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeToComment(appRecipe)
            it.findNavController().navigate(action)
        }
    }

    private fun setAddCommentButton(holder: HomeHolder, appRecipe: AppRecipe?, position: Int){
        holder.commentPostBtn.setOnClickListener {
            val inputText = holder.commentContent.text.toString()
            if (inputText.trim().isNotEmpty()){
                appContext.addComment(inputText, appRecipe?.uid!!)
                holder.commentContent.text.clear()
                _items[position].comments?.add(Comment(appUser.userName,
                    appUser.name, inputText, null, Calendar.getInstance().time))
                holder.commentTitle.text = String.format(commentMsg, _items[position].comments?.size)
                _items[position].myReference?.let { it1 ->
                    _items[position].owner?.myReference?.let { it2 ->
                        appContext.addNotification(it2, it1, null, NotificationType.COMMENT)
                    }
                }
                notifyItemChanged(position)
            }
        }
    }

    private fun setOtherClicks(holder: HomeHolder, position: Int){
        holder.likeImage.setOnClickListener {
            Log.e("Home Adapter: likes", _items[position].likes.toString())
            _items[position].likes = _items[position].likes?.plus(1)
            Log.e("Home Adapter: likes", _items[position].likes.toString())
            appContext.updateRecipeFields(_items[position], "likes", null)
            holder.likesTitle.text = String.format(likeMsg, _items[position].likes)
            _items[position].myReference?.let { it1 ->
                _items[position].owner?.myReference?.let { it2 ->
                    appContext.addNotification(it2, it1, null, NotificationType.LIKE)
                }
            }
        }

        holder.recipeImage.setOnLongClickListener {
            val item = _items[position]
            if (appUser.favorites != null){
                if(!appUser.favorites?.contains(item.myReference)!!) {
                    appContext.addRecipeToFavorites(_items[position])
                    holder.favoritesImage.visibility = View.VISIBLE
                    Toast.makeText(it.context, "Recipe was added to favorites", Toast.LENGTH_LONG)
                        .show()
                }else{
                    appContext.deleteRecipeFromFavorites(item)
                    holder.favoritesImage.visibility = View.GONE
                }
            }else{
                appContext.addRecipeToFavorites(_items[position])
                holder.favoritesImage.visibility = View.VISIBLE
                Toast.makeText(it.context, "Recipe was added to favorites", Toast.LENGTH_LONG)
                    .show()
            }

            true
        }

        holder.recipeImage.setOnClickListener{
            if((_items[position].status == _items[position].TRADE_STATUS) && (_items[position].allowedUsers != null) && (_items[position].allowedUsers?.contains(appUser.myReference)) == false){
                val alertDialog = AlertDialog.Builder(it.context)
                val view = LayoutInflater.from(it.context)
                    .inflate(R.layout.dialog_move_offer_trade, null)
                alertDialog.setView(view)
                alertDialog.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                    val action = HomeFragmentDirections.actionHomeToTrade(_items[position])
                    it.findNavController().navigate(action)
                }
                alertDialog.setNegativeButton("No") { _: DialogInterface, _: Int -> }
                alertDialog.show()
            }else{
                val action = HomeFragmentDirections.actionHomeToRecipeDetails(_items[position])
                it.findNavController().navigate(action)
            }
        }
    }

}