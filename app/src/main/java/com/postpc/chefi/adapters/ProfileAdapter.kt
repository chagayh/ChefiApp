package com.postpc.chefi.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.postpc.chefi.Chefi
import com.postpc.chefi.R
import com.postpc.chefi.holders.ProfileHeaderHolder
import com.postpc.chefi.database.AppRecipe
import com.postpc.chefi.holders.RecipeHolder
import com.postpc.chefi.database.DbUser
import com.postpc.chefi.database.NotificationType
import com.postpc.chefi.fragment.ProfileFragmentDirections
import com.postpc.chefi.fragment.ProfileOtherFragmentDirections
import com.squareup.picasso.Picasso


class ProfileAdapter(private val dbUser: DbUser?, viewLifecycleOwner: LifecycleOwner, val fragmentView: View): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
            view = LayoutInflater.from(context).inflate(R.layout.item_profile_header, parent, false)
            if(otherFlag){ //if not user
                val editAboutMe:TextView = view.findViewById(R.id.textViewAboutMeEdit)
                val editMainCard:TextView = view.findViewById(R.id.TextViewEditMainCard)
                val followButton:TextView = view.findViewById(R.id.followBtn)
                editAboutMe.visibility = View.GONE
                editMainCard.visibility = View.GONE
                if (dbUser != null) {
                    if (dbUser.myReference?.let { it1 -> appContext.isFollowedByMe(it1) }!!){
                        followButton.text = "UNFOLLOW"
                    }else{
                        followButton.text = "FOLLOW"
                    }
                }
            }
            return ProfileHeaderHolder(view)
        }
        else{
            view = LayoutInflater.from(context).inflate(R.layout.item_recipe_profile, parent, false)
//            val progressBar: androidx.constraintlayout.widget.ConstraintLayout = view.findViewById(R.id.constrainLayoutProgressBar)
//            progressBar.visibility = View.VISIBLE
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
                    if (dbUser.myReference?.let { it1 -> appContext.isFollowedByMe(it1) }!!){
                        appContext.unFollow(dbUser)
                        holder.followButton.text = "FOLLOW"
                    }else{
                        appContext.follow(dbUser)
                        appContext.addNotification(dbUser.myReference!!, null, null, NotificationType.FOLLOW)
                        holder.followButton.text = "UNFOLLOW"
                    }
                }
            })
            if(tempDbUser.myReference == appContext.getCurrUser()?.myReference) holder.followMenuLinear.visibility = View.GONE
        }
        else{
            holder.favoritesButton.setOnClickListener(View.OnClickListener {
                holder.favoritesButton.setTextColor(holder.blueColor)
                holder.recipesButton.setTextColor(holder.greyColor)
                recipesFlag = false
                setItems(appContext.getUserFavorites(), true)
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
                alertDialog.setNegativeButton("Cancel", DialogInterface.OnClickListener { _, _ ->
                })
                alertDialog.create().show()
            }

            holder.editMainCard.setOnClickListener{
                val alertDialog = AlertDialog.Builder(it.context)
                val view = LayoutInflater.from(it.context).inflate(R.layout.dialog_edit_main_card, null)
                alertDialog.setView(view)
                val fullName: EditText = view.findViewById(R.id.editTextFullName)
                alertDialog.setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                    if (fullName.text.toString().trim().isNotEmpty()){
                        holder.nameTextView.text = fullName.text
                        appContext.updateUserFields("name", fullName.text.toString())
                    }
                })
                alertDialog.setNegativeButton("Cancel", DialogInterface.OnClickListener { _, _ ->
                })
                alertDialog.create().show()
            }

            holder.image.setOnLongClickListener {
                val action = ProfileFragmentDirections.actionProfileToUploadPic()
                it.findNavController().navigate(action)
                true
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
        val item = if(recipesFlag){_recipesItems?.get(position-1)} else
            _favoritesItems?.get(position-1)
        Picasso.with(appContext)
            .load(item?.imageUrl)
            .into(holder._image)
//        holder._progressBar.visibility = View.GONE
        setRecipeButtons(holder, position)
    }

    private fun setRecipeButtons(holder: RecipeHolder,  position: Int){
        val item = if(recipesFlag){_recipesItems?.get(position-1)} else
            _favoritesItems?.get(position-1)
        // set a listener to know when view was clicked, and tell the listener if exists
        holder.itemView.setOnClickListener {
            if (item != null){
                val action: NavDirections = if (otherFlag) {
                    ProfileOtherFragmentDirections.actionProfileOtherToRecipe(item)
                }else {
                    ProfileFragmentDirections.actionProfileToRecipe(item)
                }
                it.findNavController().navigate(action)
            }
        }
        // set a long listener to know when view was clicked, and tell the listener if exists
        if (!otherFlag){
            holder.itemView.setOnLongClickListener {
                if (item != null) {
                    if(recipesFlag){
                        val alertDialog = AlertDialog.Builder(it.context)
                        val view = LayoutInflater.from(it.context).inflate(R.layout.dialog_delete_recipe, null)
                        alertDialog.setView(view)
                        alertDialog.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                            _recipesItems?.remove(item)
                            appContext.deleteRecipe(item)
//                            _recipesItems?.let { it1 -> setItems(it1, false) }
                            notifyDataSetChanged()
                        }
                        alertDialog.setNegativeButton("No"){ _: DialogInterface, _: Int -> }
                        alertDialog.show()
                    }else{
                        _favoritesItems?.remove(item)
                        appContext.deleteRecipeFromFavorites(item)
//                        _favoritesItems?.let { it1 -> setItems(it1, true) }
                        notifyDataSetChanged()
                    }
                }
                true
            }
        }
    }
}