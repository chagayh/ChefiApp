package com.example.chefi

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast


/**
 * A simple [Fragment] subclass.
 * Use the [AddFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddFragment : Fragment() {

    val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private lateinit var recipeTitleEditText: EditText
    private lateinit var addBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add, container, false)
        recipeTitleEditText = view.findViewById(R.id.recipeTitleEditText)
        addBtn = view.findViewById(R.id.addBtn)
        setComponents()
        return view
    }

    private fun setComponents() {
        addBtn.setOnClickListener {
            if (recipeTitleEditText.text.toString().trim().isEmpty()) {
                Toast.makeText(activity, "missing field", Toast.LENGTH_SHORT)
                    .show()
            } else {
                appContext.addRecipe(recipeTitleEditText.text.toString(), null) // TODO - change image uri
            }
        }
    }


}
