package com.example.chefi.adapters


import android.app.AlertDialog
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.database.AppRecipe
import com.example.chefi.database.NotificationType
import com.example.chefi.fragment.HomeFragmentDirections
import com.example.chefi.holders.TradeHolder
import com.squareup.picasso.Picasso


class TradeAdapter(val recipe: AppRecipe?, private val fragmentView: View): RecyclerView.Adapter<TradeHolder>() {

    private lateinit var appContext: Chefi
    private var _items: ArrayList<AppRecipe> = ArrayList()

    // public method to show a new list of items
    fun setItems(items: ArrayList<AppRecipe>?){
        val notTradeToShow: TextView = fragmentView.findViewById(R.id.noTradeToShow)
        if (items != null){
            if (items.size > 0){
                notTradeToShow.visibility = View.GONE
            }
            _items.clear()
            _items.addAll(items)
            notifyDataSetChanged()
        }else{
            notTradeToShow.visibility = View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  TradeHolder {
        val context = parent.context
        val view: View
        appContext = context.applicationContext as Chefi
        view = LayoutInflater.from(context).inflate(R.layout.item_recipe_profile, parent, false)
        return TradeHolder(view)
    }

    override fun getItemCount(): Int {
        return _items.size
    }


    override fun onBindViewHolder(holder: TradeHolder, position: Int) {
        val item = _items[position]
        if(item.imageUrl != null) {
            Picasso.with(appContext)
                .load(item.imageUrl)
                .into(holder.image)
        }

        holder.image.setOnClickListener {
            val alertDialog = AlertDialog.Builder(it.context)
            val view = LayoutInflater.from(it.context).inflate(R.layout.dialog_offer_trade, null)
            alertDialog.setView(view)
            alertDialog.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                // TODO: create notification
                recipe?.owner?.myReference?.let { it1 -> recipe.myReference?.let { it2 ->
                    appContext.addNotification(it1, item.myReference, it2, NotificationType.TRADE) } }
                val unseen = appContext.getUnseenNotification()
                it.findNavController().popBackStack()
            }
            alertDialog.setNegativeButton("No"){ _: DialogInterface, _: Int -> }
            alertDialog.show()
        }
    }
}