package com.example.chefi.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.database.Recipe
import com.example.chefi.holders.ProfileHeaderHolder
import com.example.chefi.holders.RecipeHolder
import com.example.chefi.listeners.RecipeClickListener
import com.example.chefi.database.User
import com.squareup.picasso.Picasso
import kotlin.math.log


// we need to create an adapter that extends RecyclerView.Adapter
// and is generic of our custom type of view holder
// the adapter needs to implement 3 methods:
//      - getItemCount() to tell the amount of items
//      - onCreateViewHolder() to create a new view holder
//      - onBindViewHolder() to customize a view holder with the position
//
// also, we created the interface OnToDoItemClickListener so that the adapter could
// tell anyone who wants to listen whenever a "person" view was clicked

class RecipeAdapter(private val user: User?): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//    private val Context.app: Chefi
//        get() = applicationContext as Chefi

//        appContext.setUserFields(fieldName : String, content : String)
    private lateinit var appContext: Chefi
    private val TYPE_HEADER = 1
    private val TYPE_ITEM = 2
    private var _items: ArrayList<Recipe>? = ArrayList()
    var recipesFlag:Boolean = true
    var toDoItemClickCallback: RecipeClickListener? = null
    private val otherFlag = user != null
    private lateinit var tempUser: User

    // public method to show a new list of items
    fun setItems(items: ArrayList<Recipe>){
        _items?.clear()
        _items?.addAll(items)
        notifyDataSetChanged()
    }

    // here we need to create a view holder
    // steps: 1. hold a LayoutInflater
    //        2. inflate a view
    //        3. wrap it with a view holder and return it
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  RecyclerView.ViewHolder {
        val context = parent.context
        val view: View
        appContext = context.applicationContext as Chefi
        tempUser = (user ?: appContext.getCurrUser()) as User
        if(viewType == TYPE_HEADER){
            view = LayoutInflater.from(context).inflate(R.layout.profile_header, parent, false)
            return ProfileHeaderHolder(view)
        }
        else{
            view = LayoutInflater.from(context).inflate(R.layout.recipe, parent, false)
            return RecipeHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0){
            return TYPE_HEADER
        }
        return TYPE_ITEM
    }

    override fun getItemCount(): Int {
//        return 1 + (_items?.size ?: 0)
        return if(recipesFlag) 200 else 1
    }


    // here we need to customize the view holder at the needed position
    // we set the view fields based on the person
    // and set a ClickListener to know whenever the user taps on the view
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is RecipeHolder)
        {
            holder._image.setImageResource(R.drawable.dog)
            val item = _items?.get(position - 1)
            // set a listener to know when view was clicked, and tell the listener if exists
            holder.itemView.setOnClickListener {
                // TODO: open recipe page
            }
            // set a long listener to know when view was clicked, and tell the listener if exists
            if (!otherFlag){
                holder.itemView.setOnLongClickListener {
                    if (item != null) {
                        if(recipesFlag){
                            val alertDialog = AlertDialog.Builder(it.context)
                            alertDialog.setTitle("Would you like to delete this recipe?")
                            alertDialog.setPositiveButton("Confirm") { _: DialogInterface, _: Int ->
                                _items?.remove(item)
                                appContext.deleteRecipe(item)
                                _items?.let { it1 -> setItems(it1) }
                            }
                            alertDialog.setNegativeButton("Cancel"){ _: DialogInterface, _: Int -> }
                            alertDialog.show()
                        }else{
                            _items?.remove(item)
                            appContext.removeRecipeFromFavorites(item)
                            _items?.let { it1 -> setItems(it1) }
                        }
                    }
                    true
                }
            }
        }else if(holder is ProfileHeaderHolder){
            setProfileHeader(holder)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setProfileHeader(holder: ProfileHeaderHolder){
        holder.favorites.visibility = View.GONE
        holder.recipes.visibility = View.VISIBLE
        customizeComponents(holder)
        setMenuButtons(holder)
        if(!otherFlag){setEditButtons(holder)}
        setOtherButtons(holder)
    }

    @SuppressLint("SetTextI18n")
    private fun customizeComponents(holder: ProfileHeaderHolder){
        holder.aboutMeTextView.text = tempUser.aboutMe
        holder.nameTextView.text = tempUser.name
        holder.usernameTextView.text = "@" + tempUser.userName
        if(!otherFlag){holder.followButton.text = "Following"}
        _items = if (otherFlag) ArrayList() else appContext.getUserRecipes()
        if(tempUser.imageUrl != null){
            Picasso.with(appContext)
                .load(tempUser.imageUrl)
                .into(holder.image)
        }
        else{
            holder.image.setImageResource(R.drawable.chagaipp)
        }
    }

    private fun setMenuButtons(holder: ProfileHeaderHolder){
        holder.favoritesButton.setOnClickListener(View.OnClickListener {
            holder.favorites.visibility = View.VISIBLE
            holder.recipes.visibility = View.GONE
            holder.favoritesButton.setTextColor(holder.blueColor)
            holder.recipesButton.setTextColor(holder.greyColor)
            recipesFlag = false
            _items = if (otherFlag) ArrayList() else appContext.getUserFavorites()
//            notifyDataSetChanged()
        })
        holder.recipesButton.setOnClickListener(View.OnClickListener {
            holder.favorites.visibility = View.GONE
            holder.recipes.visibility = View.VISIBLE
            holder.favoritesButton.setTextColor(holder.greyColor)
            holder.recipesButton.setTextColor(holder.blueColor)
            recipesFlag = true
            _items = if (otherFlag) ArrayList() else appContext.getUserRecipes()
//            notifyDataSetChanged()
        })
    }

    private fun setEditButtons(holder: ProfileHeaderHolder) {
        if(otherFlag){ //if not user
            holder.editAboutMe.visibility = View.GONE
            holder.editMainCard.visibility = View.GONE
        }else{

            holder.editAboutMe.setOnClickListener(){
                val alertDialog = AlertDialog.Builder(it.context)
                val view = LayoutInflater.from(it.context).inflate(R.layout.dialog_edit_about_me, null)
                alertDialog.setView(view)
                val aboutMeDescription: EditText = view.findViewById(R.id.editTextAboutMe)
                alertDialog.setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                    if (aboutMeDescription.text.toString().trim().isNotEmpty()){
                        holder.aboutMeTextView.text = aboutMeDescription.text
                        appContext.updateUserFields("aboutMe", aboutMeDescription.text.toString())
                    }
                })
                alertDialog.create().show()
            }

            holder.editMainCard.setOnClickListener(){
                val alertDialog = AlertDialog.Builder(it.context)
                val view = LayoutInflater.from(it.context).inflate(R.layout.dialog_edit_main_card, null)
                alertDialog.setView(view)
                val fullName: EditText = view.findViewById(R.id.editTextFullName)
                val uploadProfilePictureBtn: Button = view.findViewById(R.id.uploadProfilePictureButton)
                uploadProfilePictureBtn.setOnClickListener(){
                    //TODO: Use Chagay API
                }
                alertDialog.setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                    if (fullName.text.toString().trim().isNotEmpty()){
                        holder.nameTextView.text = fullName.text
                        appContext.updateUserFields("name", fullName.text.toString())
                    }
                })
                alertDialog.create().show()
            }
        }
    }

    private fun setOtherButtons(holder: ProfileHeaderHolder){
        holder.followButton.setOnClickListener {
            if (user != null) {
                appContext.follow(user)
            }else{
                it.findNavController().navigate(R.id.followersFragment)
            }
        }
        holder.followersButton.setOnClickListener {
            it.findNavController().navigate(R.id.followersFragment)
        }
    }
}