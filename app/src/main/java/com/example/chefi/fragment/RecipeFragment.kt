package com.example.chefi.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.database.AppRecipe
import com.example.chefi.database.DbUser
import com.squareup.picasso.Picasso


class RecipeFragment : Fragment() {
    val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private val args: RecipeFragmentArgs by navArgs()
    private lateinit var appRecipe: AppRecipe

    private lateinit var userImage: ImageView
    private lateinit var userNameUp: TextView
    private lateinit var recipeImage: ImageView
    private lateinit var likesTitle: TextView
    private lateinit var likeImage: ImageView
    private lateinit var favoritesImage: ImageView
    private lateinit var userNameDown: TextView
    private lateinit var postDescription: TextView
    private lateinit var commentTitle: TextView
    private lateinit var commentOneUsername: TextView
    private lateinit var commentOneContent: TextView
    private lateinit var commentTwoUsername: TextView
    private lateinit var commentTwoContent: TextView
    private lateinit var commentPostBtn: TextView
    private lateinit var commentContent: EditText
    private lateinit var forTrade: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view= inflater.inflate(R.layout.fragment_recipe, container, false)
        appRecipe = args.curRecipe
        userImage = view.findViewById(R.id.recipeHomeUserImageView)
        userNameUp = view.findViewById(R.id.textViewRecipeHome)
        recipeImage = view.findViewById(R.id.recipeHomeRecipeImageView)
        likesTitle = view.findViewById(R.id.textViewLikes)
        likeImage = view.findViewById(R.id.imageViewHomeRecipeLike)
        favoritesImage = view.findViewById(R.id.imageViewHomeFavorites)
        userNameDown = view.findViewById(R.id.textViewUsername)
        postDescription = view.findViewById(R.id.textViewContent)
        commentTitle = view.findViewById(R.id.textViewCommentTitle)
        commentOneUsername = view.findViewById(R.id.textViewCommentOneUsername)
        commentOneContent = view.findViewById(R.id.textViewCommentOneContent)
        commentTwoUsername = view.findViewById(R.id.textViewCommentTwoUsername)
        commentTwoContent = view.findViewById(R.id.textViewCommentTwoContent)
        commentPostBtn = view.findViewById(R.id.textViewPostCommentBtn)
        commentContent = view.findViewById(R.id.editTextPostComment)
        forTrade = view.findViewById(R.id.textViewForTrade)
        customizeComponents()
        return view
    }

    private fun customizeComponents(){
        val item = appRecipe
        val curUser: DbUser? = item.owner
        Log.e("Recipe Fragment", curUser?.name.toString())
        // user details:
        if(curUser != null){
            userNameUp.text = curUser.name
            userNameDown.text = curUser.userName
            if(curUser.imageUrl != null){
                Picasso.with(appContext)
                    .load(curUser.imageUrl)
                    .into(userImage)
            }
            else{
                userImage.setImageResource(R.drawable.defpp)
            }
        }
        // recipe details
        if(item.imageUrl != null){
            Picasso.with(appContext)
                .load(item.imageUrl)
                .into(recipeImage)
        }
        else{
            userImage.setImageResource(R.drawable.pasta3)
        }
        postDescription.text = item.description
        likesTitle.text = String.format(likesTitle.text.toString(), item.likes)
        if (item.status != item.TRADE_STATUS) forTrade.visibility = View.GONE
        // comment details
        if (item.comments != null){
            commentTitle.text = String.format(commentTitle.text.toString(), item.comments!!.size)
            setNavigateToCommentComponents()
            if(item.comments!!.size > 0)
            {
                var tempComment = item.comments!![0]
                commentOneUsername.text = tempComment.userName
                commentOneContent.text = tempComment.commentContent
                if (item.comments!!.size > 1){
                    tempComment = item.comments!![1]
                    commentTwoUsername.text = tempComment.userName
                    commentTwoContent.text = tempComment.commentContent
                }
            }
        }
        setAddCommentButton()
        if (curUser != null) {
            setNavigateToProfileComponents(curUser)
        }
        setOtherClicks()
    }

    private fun setNavigateToProfileComponents(dbUser: DbUser){
        userImage.setOnClickListener {
            val action = RecipeFragmentDirections.actionRecipeToProfileOther(dbUser)
            it.findNavController().navigate(action)
        }
        userNameUp.setOnClickListener {
            val action = RecipeFragmentDirections.actionRecipeToProfileOther(dbUser)
            it.findNavController().navigate(action)
        }
        userNameDown.setOnClickListener {
            val action = RecipeFragmentDirections.actionRecipeToProfileOther(dbUser)
            it.findNavController().navigate(action)
        }
    }

    private fun setNavigateToCommentComponents(){
        commentTitle.setOnClickListener {
            if (appRecipe.comments?.size!! > 0){
                val action = RecipeFragmentDirections.actionRecipeToComment(appRecipe)
                it.findNavController().navigate(action)
            }
        }
    }

    private fun setAddCommentButton(){
        commentPostBtn.setOnClickListener {
            val inputText = commentContent.text
            if (inputText.toString().trim().isNotEmpty()){
                appContext.addComment(inputText.toString(), appRecipe.uid!!)
                commentContent.text.clear()
            }
        }
    }

    private fun setOtherClicks(){
        likeImage.setOnClickListener {
            Log.e("Home Adapter: likes", appRecipe.likes.toString())
            appRecipe.likes = appRecipe.likes?.plus(1)
            Log.e("Home Adapter: likes", appRecipe.likes.toString())
            appContext.updateRecipeFields(appRecipe, "likes", null)
            likesTitle.text = String.format(likesTitle.text.toString(), appRecipe.likes)
        }

        recipeImage.setOnLongClickListener {
            appContext.addRecipeToFavorites(appRecipe)
            favoritesImage.visibility = View.VISIBLE
            true
        }

        recipeImage.setOnClickListener{
            val action = RecipeFragmentDirections.actionRecipeToRecipeDetails(appRecipe)
            it.findNavController().navigate(action)
        }
    }
}