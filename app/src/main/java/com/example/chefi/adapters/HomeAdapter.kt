package com.example.chefi.adapters


import android.app.AlertDialog
import android.content.DialogInterface
import android.opengl.Visibility
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.database.AppRecipe
import com.example.chefi.database.DbUser
import com.example.chefi.fragment.HomeFragmentDirections
import com.example.chefi.holders.HomeHolder
import com.squareup.picasso.Picasso

class HomeAdapter(viewLifecycleOwner: LifecycleOwner) : RecyclerView.Adapter<HomeHolder>() {

    private lateinit var appContext: Chefi
    private var _items: ArrayList<AppRecipe> = ArrayList()

    // public method to show a new list of items
    fun setItems(items: ArrayList<AppRecipe>){
        _items.clear()
        _items.addAll(items)
        notifyDataSetChanged()
    }

    // here we need to create a view holder
    // steps: 1. hold a LayoutInflater
    //        2. inflate a view
    //        3. wrap it with a view holder and return it
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  HomeHolder {
        val context = parent.context
        val view: View
        appContext = context.applicationContext as Chefi
        view = LayoutInflater.from(context).inflate(R.layout.item_recipe_home, parent, false)
        return HomeHolder(view)
    }

    override fun getItemCount(): Int {
        return _items.size
    }


    override fun onBindViewHolder(holder: HomeHolder, position: Int) {
        customizeComponents(holder, position)
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
        // comment details
//        holder.commentTitle.text = String.format(holder.commentTitle.text.toString(), 0)
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
        setOtherClicks(holder, item, position)
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
            if (appRecipe?.comments?.size!! > 0){
                val action = HomeFragmentDirections.actionHomeToComment(appRecipe)
                it.findNavController().navigate(action)
            }
        }
    }

    private fun setAddCommentButton(holder: HomeHolder, appRecipe: AppRecipe?, position: Int){
        holder.commentPostBtn.setOnClickListener {
            val inputText = holder.commentContent.text
            if (inputText.toString().trim().isNotEmpty()){
                appContext.addComment(inputText.toString(), appRecipe?.uid!!)
                holder.commentContent.text.clear()
                notifyItemChanged(position)
            }
        }
    }

    private fun setOtherClicks(holder: HomeHolder, appRecipe: AppRecipe?, position: Int){
        holder.likeImage.setOnClickListener {
            if (appRecipe != null) {
                Log.e("Home Adapter: likes", appRecipe.likes.toString())
                appRecipe.likes = appRecipe.likes?.plus(1)
                Log.e("Home Adapter: likes", appRecipe.likes.toString())
                appContext.updateRecipeFields(appRecipe, "likes", null)
                holder.likesTitle.text = String.format(holder.likesTitle.text.toString(), appRecipe.likes)
                notifyItemChanged(position)
            }
        }

        holder.recipeImage.setOnLongClickListener {
            if (appRecipe != null) {
                appContext.addRecipeToFavorites(appRecipe)
                holder.favoritesImage.visibility = View.VISIBLE
            }
            true
        }

        holder.recipeImage.setOnClickListener{
            if(appRecipe?.status == appRecipe?.TRADE_STATUS){
                val alertDialog = AlertDialog.Builder(it.context)
                val view = LayoutInflater.from(it.context).inflate(R.layout.dialog_offer_trade_nav, null)
                alertDialog.setView(view)
                alertDialog.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                    val action = HomeFragmentDirections.actionHomeToTrade(appRecipe?.uid)
                    it.findNavController().navigate(action)
                }
                alertDialog.setNegativeButton("No"){ _: DialogInterface, _: Int -> }
                alertDialog.show()
            }else{
                val action = HomeFragmentDirections.actionHomeToRecipeDetails(appRecipe)
                it.findNavController().navigate(action)
            }
        }
    }

}