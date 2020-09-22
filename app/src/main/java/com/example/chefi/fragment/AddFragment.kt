package com.example.chefi.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.navigation.findNavController
import com.example.chefi.Chefi
import com.example.chefi.R
import java.io.IOException
import java.util.*
import androidx.lifecycle.Observer


/**
 * A simple [Fragment] subclass.
 * Use the [AddFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddFragment : Fragment() {

    val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private lateinit var imageView: ImageView
    private lateinit var addBtn: Button
    private lateinit var displayBtn: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var imageUri: Uri

    companion object {
        private val PICK_IMAGE_REQUEST = 71
        private val TAG_ADD_FRAGMENT = "addFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add, container, false)
        imageView = view.findViewById(R.id.imageView)
        addBtn = view.findViewById(R.id.addBtn)
        displayBtn = view.findViewById(R.id.displayBtn)
        progressBar = view.findViewById(R.id.progressBar)
        setComponents()
//        loadImage()
        return view
    }

    private fun setComponents() {
        addBtn.setOnClickListener { v ->
            val intent = Intent()
            with (intent) {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
            }
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
//            v.findNavController().navigate(R.id.recipeFragment)
//            if (recipeTitleEditText.text.toString().trim().isEmpty()) {
//                Toast.makeText(activity, "missing field", Toast.LENGTH_SHORT)
//                    .show()
//            } else {
//                appContext.addRecipe(recipeTitleEditText.text.toString(), null) // TODO - change image uri
//            }
        }
        displayBtn.setOnClickListener { v ->
            v.findNavController().navigate(R.id.displayFragment)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST
            && resultCode == RESULT_OK
            && data != null && data.data != null){

            try {
                imageUri = data.data!!

                val bitmap = MediaStore.Images.Media.getBitmap(appContext.contentResolver, imageUri)
                imageView.setImageBitmap(bitmap)
                uploadImage()
                // TODO - if goes back delete the recipe document
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage() {
//        val progressBar = ProgressBar(activity)
        // get the extension of the file (e.g jpg)
        val contentResolver = activity?.contentResolver
        val mime = MimeTypeMap.getSingleton()
        val fileExtension = mime.getExtensionFromMimeType(contentResolver?.getType(imageUri))
        val workId = appContext.uploadImage(imageUri, fileExtension)
        setWorkObserver(workId)
    }

    private fun setWorkObserver(workId: UUID) {
        appContext.getWorkManager().getWorkInfoByIdLiveData(workId).observe(this
            , Observer { value ->
                Log.d(TAG_ADD_FRAGMENT, "value = ${value.outputData.getString(appContext.getString(R.string.keyUrl))}")
        })
    }
}
