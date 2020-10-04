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
import com.example.chefi.holders.HomeHolder

class HomeAdapter(): RecyclerView.Adapter<HomeHolder>() {

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
        // TODO: get list
//        _items = appContext.getUserFollowers()
        view = LayoutInflater.from(context).inflate(R.layout.recipe_home, parent, false)
        return HomeHolder(view)
    }

    override fun getItemCount(): Int {
//        return _items.size
        return 35
    }


    override fun onBindViewHolder(holder: HomeHolder, position: Int) {
//        holder.image.setImageResource(R.drawable.chagaipp)
//        val item = _items[position]
//        holder.name.text = item.name
//        Picasso.with(appContext) // ToDO: check with Chagai if appContext it's OK
//            .load(item.imageUrl)
//            .into(holder.image)
//        setNavigateToProfileComponents(holder, itemCount.user)
        setNavigateToCommentComponents(holder, null)
    }

    fun setNavigateToProfileComponents(holder: HomeHolder, dbUser: DbUser){
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

    fun setNavigateToCommentComponents(holder: HomeHolder, appRecipe: AppRecipe?){
        holder.commentTitle.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeToComment(appRecipe)
            it.findNavController().navigate(action)
        }
    }
}