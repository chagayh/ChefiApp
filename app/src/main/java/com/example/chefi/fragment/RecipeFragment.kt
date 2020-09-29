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
import com.example.chefi.database.Recipe
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
    private lateinit var imageDatabaseId: String
    private lateinit var addBtn: Button

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
        imageUrl = args.imageUrlAsString
        imageDatabaseId = args.databaseId

        textViewName = view.findViewById(R.id.textViewName)
        textViewDirections = view.findViewById(R.id.textViewDirections)
        textViewIngredients = view.findViewById(R.id.textViewIngredients)
        textViewStatus = view.findViewById(R.id.textViewStatus)
        addBtn = view.findViewById(R.id.addBtn)

        addBtn.setOnClickListener {
            val workId = appContext.addRecipe(textViewName.text.toString(),
                                              imageUrl,
                                              imageDatabaseId,
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
                    val recipeAsJson = value.outputData.getString(getString(R.string.keyRecipeType))
                    val recipeType = object : TypeToken<Recipe>() {}.type

                    val returnedRecipe = Gson().fromJson<Recipe>(recipeAsJson, recipeType)
                    Log.d(TAG_RECIPE_FRAGMENT, "recipe = $returnedRecipe")
                    Toast.makeText(appContext, "recipe ${returnedRecipe.name} CREATED", Toast.LENGTH_SHORT)
                        .show()
                    // TODO - maybe add a preview page
                    findNavController().navigate(R.id.addFragment)
                }
            })
    }

    private fun setCustomOnBackPressed() {
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(this, object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Log.d(TAG_RECIPE_FRAGMENT, "delete the recipe from storage and database")
                    // TODO delete the recipe from storage and database
                    if (isEnabled) {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }

            })
    }
}
