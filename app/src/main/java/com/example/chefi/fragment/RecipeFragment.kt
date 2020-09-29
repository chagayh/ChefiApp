package com.example.chefi.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import com.example.chefi.R
/**
 * A simple [Fragment] subclass.
 * Use the [RecipeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecipeFragment : Fragment() {
    private val args: RecipeFragmentArgs by navArgs()
    private val TAG_RECIPE_FRAGMENT = "recipeFragment"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recipe, container, false)
        val url = args.imageUrlAsString
        val imageDatabaseId = args.databaseId
        Log.d(TAG_RECIPE_FRAGMENT, "imgeUrl = $url, databaseId= $imageDatabaseId")
        return view
    }
}
