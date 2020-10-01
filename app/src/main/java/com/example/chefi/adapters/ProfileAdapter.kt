package com.example.chefi.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
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
import com.example.chefi.database.User
import com.example.chefi.fragment.ProfileFragmentDirections
import com.squareup.picasso.Picasso


// we need to create an adapter that extends RecyclerView.Adapter
// and is generic of our custom type of view holder
// the adapter needs to implement 3 methods:
//      - getItemCount() to tell the amount of items
//      - onCreateViewHolder() to create a new view holder
//      - onBindViewHolder() to customize a view holder with the position
//
// also, we created the interface OnToDoItemClickListener so that the adapter could
// tell anyone who wants to listen whenever a "person" view was clicked

class ProfileAdapter(private val user: User?): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var appContext: Chefi
    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1
    private var _items: ArrayList<Recipe>? = ArrayList()
    var recipesFlag:Boolean = true
    private val otherFlag = user != null
    private lateinit var tempUser: User

    // public method to show a new list of items
    fun setItems(items: ArrayList<Recipe>){
        _items?.clear()
        _items?.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  RecyclerView.ViewHolder {
        val context = parent.context
        val view: View
        appContext = context.applicationContext as Chefi
        tempUser = ((user ?: appContext.getCurrUser()) as User)
        if(viewType == TYPE_HEADER){
            view = LayoutInflater.from(context).inflate(R.layout.profile_header, parent, false)
            return ProfileHeaderHolder(view)
        }
        else{
            view = LayoutInflater.from(context).inflate(R.layout.recipe_profile, parent, false)
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
        val itemsSize = (_items?.size ?: 0)
        return 1 + itemsSize
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is RecipeHolder)
        {
            setRecipe(holder, position)
        }else if(holder is ProfileHeaderHolder){
            setProfileHeader(holder)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setProfileHeader(holder: ProfileHeaderHolder){
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
        if(!otherFlag){holder.followingButton.text = "Following"}
        // TODO: observer
        _items = if (otherFlag) ArrayList() else appContext.getUserRecipes()
        if(tempUser.imageUrl != null){
            Picasso.with(appContext)
                .load(tempUser.imageUrl)
                .into(holder.image)
        }
        else{
            holder.image.setImageResource(R.drawable.defpp)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setMenuButtons(holder: ProfileHeaderHolder){
        if(otherFlag){
            holder.menuLinear.visibility = View.GONE
            holder.followMenuLinear.visibility = View.VISIBLE
            holder.followButton.setOnClickListener(View.OnClickListener {
                if (user != null) {
                    appContext.follow(user)
                    holder.followButton.text = "UNFOLLOW"
                }
            })

        }
        else{
            holder.favoritesButton.setOnClickListener(View.OnClickListener {
                holder.favoritesButton.setTextColor(holder.blueColor)
                holder.recipesButton.setTextColor(holder.greyColor)
                recipesFlag = false
                _items = if (otherFlag) ArrayList() else appContext.getUserFavorites()
            })
            holder.recipesButton.setOnClickListener(View.OnClickListener {
                holder.favoritesButton.setTextColor(holder.greyColor)
                holder.recipesButton.setTextColor(holder.blueColor)
                recipesFlag = true
                _items = if (otherFlag) ArrayList() else appContext.getUserRecipes()
            })
        }
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
        holder.followingButton.setOnClickListener {
            if (user != null) {
                appContext.follow(user)
            }else{
                val action = ProfileFragmentDirections.actionProfileToFollowers(false, user)
                it.findNavController().navigate(action)
            }
        }
        holder.followersButton.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileToFollowers(true, user)
            it.findNavController().navigate(action)
        }
    }

    private fun setRecipe(holder: RecipeHolder, position: Int){
        val item = _items?.get(position-1)
        Picasso.with(appContext)
            .load(item?.imageUrl)
            .into(holder._image)
        setRecipeButtons(holder, position)
    }

    private fun setRecipeButtons(holder: RecipeHolder,  position: Int){
        val item = _items?.get(position - 1)
        // set a listener to know when view was clicked, and tell the listener if exists
        holder.itemView.setOnClickListener {
            // TODO: open recipe_profile page
        }
        // set a long listener to know when view was clicked, and tell the listener if exists
        if (!otherFlag){
            holder.itemView.setOnLongClickListener {
                if (item != null) {
                    if(recipesFlag){
                        val alertDialog = AlertDialog.Builder(it.context)
                        alertDialog.setTitle("Would you like to delete this recipe_profile?")
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
    }
}
