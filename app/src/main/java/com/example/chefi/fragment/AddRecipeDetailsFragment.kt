package com.example.chefi.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.chefi.Chefi
import com.example.chefi.R
import java.util.*
import androidx.navigation.fragment.findNavController
import com.example.chefi.database.AppRecipe
import com.example.chefi.database.DbUser
import com.google.firebase.firestore.DocumentReference
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 * Use the [AddRecipeDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddRecipeDetailsFragment : Fragment() {
    val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private val args: AddRecipeDetailsFragmentArgs by navArgs()
    private val TAG_RECIPE_FRAGMENT = "recipeFragment"
    private lateinit var textViewName: EditText
    private lateinit var textViewDirections: EditText
    private lateinit var textViewIngredients: EditText
    private lateinit var textViewStatus: EditText
    private lateinit var imageUrl: String
    private lateinit var addBtn: TextView
    private lateinit var addIngredientBtn: ImageButton
    private lateinit var linearLayoutIngredients: LinearLayout
    private lateinit var addDirectionsBtn: ImageButton
    private lateinit var linearLayoutDirections: LinearLayout
    private lateinit var user: DbUser

    private val ingredientsViewsList = ArrayList<View>()
    private val directionsViewsList = ArrayList<View>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setCustomOnBackPressed()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_recipe_details, container, false)
        imageUrl = args.imageUrl

        textViewName = view.findViewById(R.id.editTextDescription)
        textViewDirections = view.findViewById(R.id.editTextDirections)
        textViewIngredients = view.findViewById(R.id.editTextIngredients)
        textViewStatus = view.findViewById(R.id.editTextStatus)
        addBtn = view.findViewById(R.id.addRecipeBtn)

        addIngredientBtn = view.findViewById(R.id.plusIngredientsBtn)
        linearLayoutIngredients =  view.findViewById(R.id.LinearLayoutIngredients)
        addDirectionsBtn = view.findViewById(R.id.plusDirectionsBtn)
        linearLayoutDirections =  view.findViewById(R.id.LinearLayoutDirections)

        user = appContext.getCurrUser()!!
        addBtn.setOnClickListener {
            Log.d(TAG_RECIPE_FRAGMENT, "${arrayListOf(textViewDirections.text.toString())}")
            val workId = appContext.addRecipe(textViewName.text.toString(),
                                              imageUrl,
                                              convertViewArrayToStringsArray(false),
                                              convertViewArrayToStringsArray(true),
                                              Integer.parseInt(textViewStatus.text.toString()))
            setWorkObserver(workId)
        }


        addIngredientBtn.setOnClickListener {
            val tempEditText = inflater.inflate(R.layout.inflate_ingredient, container, false)
            ingredientsViewsList.add(tempEditText)
            linearLayoutIngredients.addView(tempEditText)
        }

        addDirectionsBtn.setOnClickListener {
            val tempEditText = inflater.inflate(R.layout.inflate_instruction, container, false)
            directionsViewsList.add(tempEditText)
            linearLayoutDirections.addView(tempEditText)
        }
        return view
    }

    @SuppressLint("RestrictedApi", "VisibleForTests")
    private fun setWorkObserver(workId: UUID) {
        appContext.getWorkManager().getWorkInfoByIdLiveData(workId).observe(viewLifecycleOwner,
            { value ->
                if (value.outputData.size() != 0) {
                    Log.d(TAG_RECIPE_FRAGMENT, "value.outputData = ${value.outputData}")
                    val gson = Gson()

                    val uid = value.outputData.getString(appContext.getString(R.string.keyUid))
                    val description = value.outputData.getString(appContext.getString(R.string.keyDescription))
                    val likes = value.outputData.getInt(appContext.getString(R.string.keyLikes), 0)
                    val imageUrl = value.outputData.getString(appContext.getString(R.string.keyImageUrl))
                    val commentsAsJson = value.outputData.getString(appContext.getString(R.string.keyComments))
                    val directionsAsJson = value.outputData.getString(appContext.getString(R.string.keyUid))
                    val ingredientsAsJson = value.outputData.getString(appContext.getString(R.string.keyUid))
                    val status = value.outputData.getInt(appContext.getString(R.string.keyStatus), 3)
                    val timeStampAsJson = value.outputData.getString(appContext.getString(R.string.keyTimestamp))

                    val stringArrayListType = object : TypeToken<ArrayList<String>>() {}.type
                    val referenceArrayListType = object : TypeToken<ArrayList<DocumentReference>>() {}.type
                    val dateType = object : TypeToken<Date>() {}.type

                    val appRecipeReturned = AppRecipe(uid=uid,
                                                      description=description,
                                                      likes=likes,
                                                      imageUrl=imageUrl,
                                                      comments=gson.fromJson(commentsAsJson, referenceArrayListType),
                                                      directions=arrayListOf(textViewDirections.text.toString()),
                                                      ingredients=arrayListOf(textViewIngredients.text.toString()),
                                                      status=status,
                                                      owner=null,   // curr user own the recipe
                                                      timestamp=gson.fromJson(timeStampAsJson, dateType))

//                    val recipeAsJson = value.outputData.getString(appContext.getString(R.string.keyRecipeType))
//                    val recipeType = object : TypeToken<AppRecipe>() {}.type
//
//                    val returnedRecipe = Gson().fromJson<AppRecipe>(recipeAsJson, recipeType)
                    Log.d("change_url", "in recipeFragment in setWorkObserver, recipe image url = ${appRecipeReturned.imageUrl}")
                    Toast.makeText(appContext, "Recipe was CREATED by @${user.userName}", Toast.LENGTH_SHORT)
                        .show()
                    // TODO - maybe add a preview page
                    val action = AddRecipeDetailsFragmentDirections.actionAddRecipeDetailsToRecipe(appRecipeReturned)
                    view?.findNavController()?.navigate(action)
                }
            })
    }

    private fun setCustomOnBackPressed() {
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Log.d(TAG_RECIPE_FRAGMENT, "delete the recipe_profile from storage and database")
                    Log.d("change_url", "in setCustomOnBackPressed of recipeFragment, imageUrl = $imageUrl")
                    appContext.deleteImage(imageUrl)
                    if (isEnabled) {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            })
    }

    private fun convertViewArrayToStringsArray(isIngredients: Boolean): ArrayList<String>{
        val result = ArrayList<String>()
        val iterArray = if(isIngredients) ingredientsViewsList else directionsViewsList
        if (isIngredients){
            result.add(textViewIngredients.text.toString())
        }else{
            result.add(textViewDirections.text.toString())
        }
        for (tempView in iterArray){
            val tempEditText: EditText = tempView.findViewById(R.id.editTextIngredientsTemp)
            result.add(tempEditText.text.toString())
        }
        return result
    }
}
