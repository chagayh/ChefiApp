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
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.holders.ProfileHeaderHolder
import com.example.chefi.database.AppRecipe
import com.example.chefi.holders.RecipeHolder
import com.example.chefi.database.DbUser
import com.example.chefi.fragment.ProfileFragmentDirections
import com.example.chefi.fragment.ProfileOtherFragmentDirections
import com.squareup.picasso.Picasso
import androidx.lifecycle.Observer
import com.example.chefi.LiveDataHolder
import com.example.chefi.ObserveWrapper


class ProfileAdapter(private val dbUser: DbUser?, viewLifecycleOwner: LifecycleOwner): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var appContext: Chefi
    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1
    var _recipesItems: ArrayList<AppRecipe>? = ArrayList()
    private var _favoritesItems: ArrayList<AppRecipe>? = ArrayList()
    private val _viewLifecycleOwner = viewLifecycleOwner
    var recipesFlag:Boolean = true
    private val otherFlag = dbUser != null
    private lateinit var tempDbUser: DbUser

    // public method to show a new list of items
    fun setItems(items: ArrayList<AppRecipe>?, isFavorites: Boolean){
        if (isFavorites){
            _favoritesItems?.clear()
            if (items != null) {
                _favoritesItems?.addAll(items)
            }
        }else{
            _recipesItems?.clear()
            if (items != null) {
                _recipesItems?.addAll(items)
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  RecyclerView.ViewHolder {
        val context = parent.context
        val view: View
        appContext = context.applicationContext as Chefi
        tempDbUser = ((dbUser ?: appContext.getCurrUser()) as DbUser)
        if(viewType == TYPE_HEADER){
            view = LayoutInflater.from(context).inflate(R.layout.profile_header, parent, false)
            if(otherFlag){ //if not user
                val editAboutMe:TextView = view.findViewById(R.id.textViewAboutMeEdit)
                val editMainCard:TextView = view.findViewById(R.id.TextViewEditMainCard)
                editAboutMe.visibility = View.GONE
                editMainCard.visibility = View.GONE
            }
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

        val itemsSize =  if(recipesFlag){(_recipesItems?.size ?: 0)} else {(_favoritesItems?.size ?: 0)}
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

    @SuppressLint("SetTextI18n", "LongLogTag")
    private fun customizeComponents(holder: ProfileHeaderHolder){
        holder.aboutMeTextView.text = tempDbUser.aboutMe
        holder.nameTextView.text = tempDbUser.name
        holder.usernameTextView.text = "@" + tempDbUser.userName

        if(tempDbUser.imageUrl != null){
            Picasso.with(appContext)
                .load(tempDbUser.imageUrl)
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
                if (dbUser != null) {
                    appContext.follow(dbUser)
                    holder.followButton.text = "UNFOLLOW"
                }
            })
        }
        else{
            holder.favoritesButton.setOnClickListener(View.OnClickListener {
                holder.favoritesButton.setTextColor(holder.blueColor)
                holder.recipesButton.setTextColor(holder.greyColor)
                recipesFlag = false
                _favoritesItems = appContext.getUserFavorites()
                setItems(_favoritesItems, true)
            })
            holder.recipesButton.setOnClickListener(View.OnClickListener {
                holder.favoritesButton.setTextColor(holder.greyColor)
                holder.recipesButton.setTextColor(holder.blueColor)
                recipesFlag = true
                setItems(appContext.getUserRecipes(), false)
            })
        }
    }

    private fun setEditButtons(holder: ProfileHeaderHolder) {
        if(!otherFlag){

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
            val action = if (otherFlag) {
                ProfileOtherFragmentDirections.actionProfileOtherToFollowers(false, dbUser)
            }else {
                ProfileFragmentDirections.actionProfileToFollowers(false, dbUser)
            }
            it.findNavController().navigate(action)
        }
        holder.followersButton.setOnClickListener {
            val action = if (otherFlag) {
                ProfileOtherFragmentDirections.actionProfileOtherToFollowers(true, dbUser)
            }else {
                ProfileFragmentDirections.actionProfileToFollowers(true, dbUser)
            }
            it.findNavController().navigate(action)
        }
    }

    private fun setRecipe(holder: RecipeHolder, position: Int){
        val item = _recipesItems?.get(position-1)
        Picasso.with(appContext)
            .load(item?.imageUrl)
            .into(holder._image)
//        setRecipeButtons(holder, position)
    }

    private fun setRecipeButtons(holder: RecipeHolder,  position: Int){
        val item = _recipesItems?.get(position - 1)
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
                            _recipesItems?.remove(item)
                            appContext.deleteRecipe(item)
                            _recipesItems?.let { it1 -> setItems(it1, false) }
                        }
                        alertDialog.setNegativeButton("Cancel"){ _: DialogInterface, _: Int -> }
                        alertDialog.show()
                    }else{
                        _favoritesItems?.remove(item)
                        appContext.removeRecipeFromFavorites(item)
                        _favoritesItems?.let { it1 -> setItems(it1, true) }
                    }
                }
                true
            }
        }
}
