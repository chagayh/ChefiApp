package com.example.chefi.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.navArgs
import com.example.chefi.Chefi
import com.example.chefi.R
import java.util.*
import androidx.navigation.fragment.findNavController
import com.example.chefi.database.AppRecipe
import com.google.firebase.firestore.DocumentReference
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 * Use the [RecipeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecipeFragment : Fragment() {
    val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private val args: RecipeFragmentArgs by navArgs()
    private val TAG_RECIPE_FRAGMENT = "recipeFragment"
    private lateinit var textViewName: EditText
    private lateinit var textViewDirections: EditText
    private lateinit var textViewIngredients: EditText
    private lateinit var textViewStatus: EditText
    private lateinit var imageUrl: String
    private lateinit var addBtn: EditText

    // TODO - can't have empty fields
    // TODO - add progress bar

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setCustomOnBackPressed()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recipe, container, false)
        imageUrl = args.imageUrl

        textViewName = view.findViewById(R.id.editTextDescription)
        textViewDirections = view.findViewById(R.id.editTextDirections)
        textViewIngredients = view.findViewById(R.id.editTextIngredients)
        textViewStatus = view.findViewById(R.id.textViewStatus)
        addBtn = view.findViewById(R.id.addBtn)

        addBtn.setOnClickListener {
            Log.d(TAG_RECIPE_FRAGMENT, "${arrayListOf(textViewDirections.text.toString())}")
            val workId = appContext.addRecipe(textViewName.text.toString(),
                                              imageUrl,
                                              arrayListOf(textViewDirections.text.toString()),
                                              arrayListOf(textViewIngredients.text.toString()),
                                              Integer.parseInt(textViewStatus.text.toString()))
            setWorkObserver(workId)
        }
//        Log.d(TAG_RECIPE_FRAGMENT, "imgeUrl = $url, databaseId= $imageDatabaseId")
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
                    Toast.makeText(appContext, "recipe_profile ${appRecipeReturned.description} CREATED", Toast.LENGTH_SHORT)
                        .show()
                    // TODO - maybe add a preview page
                    findNavController().navigate(R.id.addFragment)
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
                    appContext.deleteImage(imageUrl, null)
                    if (isEnabled) {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            })
    }
}
