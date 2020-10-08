package com.example.chefi.adapters


import android.app.AlertDialog
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.database.AppRecipe
import com.example.chefi.database.NotificationType
import com.example.chefi.fragment.HomeFragmentDirections
import com.example.chefi.holders.TradeHolder
import com.squareup.picasso.Picasso


class TradeAdapter(val recipe: AppRecipe?): RecyclerView.Adapter<TradeHolder>() {

    private lateinit var appContext: Chefi
    private var _items: ArrayList<AppRecipe> = ArrayList()

    // public method to show a new list of items
    fun setItems(items: ArrayList<AppRecipe>?){
        _items.clear()
        if (items != null) {
            _items.addAll(items)
            notifyDataSetChanged()
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
                    appContext.addNotification(it1, it2, item.myReference, NotificationType.TRADE) } }
                val unseen = appContext.getUnseenNotification()
                appContext.setUnseenNotificationNumber(unseen + 1)
                it.findNavController().popBackStack()
            }
            alertDialog.setNegativeButton("No"){ _: DialogInterface, _: Int -> }
            alertDialog.show()
        }
    }

}