package com.example.chefi.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.fragment.navArgs
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.database.AppRecipe

class RecipeDetailsFragment : Fragment() {

    val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private val args: CommentFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_recipe_details, container, false)
        val curRecipe: AppRecipe? = args.curRecipe
        val descriptionEditText: TextView =  view.findViewById(R.id.editTextDescriptionDetails)
        val firstIngredientEditText: TextView =  view.findViewById(R.id.editTextIngredientsDetails)
        val firstDirectionEditText: TextView =  view.findViewById(R.id.editTextDirections)
        val linearLayoutIngredients: LinearLayout =  view.findViewById(R.id.LinearLayoutIngredientsDetails)
        val linearLayoutDirections: LinearLayout =  view.findViewById(R.id.LinearLayoutDirectionsDetails)
        val ingredientsArray = curRecipe?.ingredients
        val directionsArray = curRecipe?.directions

        descriptionEditText.setText(curRecipe?.description)

        for (i in 0 until ingredientsArray!!.size){
            if(i == 0) {
                firstIngredientEditText.setText(ingredientsArray[i])
            }else{
                val tempView = inflater.inflate(R.layout.inflate_ingredient, container, false)
                val tempEditText: TextView = tempView.findViewById(R.id.editTextIngredientsTemp)
                tempEditText.setText((ingredientsArray[i]))
                linearLayoutIngredients.addView(tempView)
            }
        }

        for (i in 0 until directionsArray!!.size){
            if(i == 0) {
                firstDirectionEditText.setText(directionsArray[i])
            }else{
                val tempView = inflater.inflate(R.layout.inflate_instruction, container, false)
                val tempEditText: TextView = tempView.findViewById(R.id.editTextIngredientsTemp)
                tempEditText.setText((directionsArray[i]))
                linearLayoutDirections.addView(tempView)
            }
        }

        return view
    }
}