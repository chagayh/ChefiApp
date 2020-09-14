package com.example.chefi.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import com.example.chefi.Chefi
import com.example.chefi.R
import java.io.IOException


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
    private lateinit var progressBar: ProgressBar
    private lateinit var imageUri: Uri

    companion object {
        private val PICK_IMAGE_REQUEST = 71
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add, container, false)
        imageView = view.findViewById(R.id.imageView)
        addBtn = view.findViewById(R.id.addBtn)
        progressBar = view.findViewById(R.id.progressBar)
        setComponents()
        return view
    }

    private fun setComponents() {
        addBtn.setOnClickListener {
            val intent = Intent()
            with (intent) {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
            }
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
//            if (recipeTitleEditText.text.toString().trim().isEmpty()) {
//                Toast.makeText(activity, "missing field", Toast.LENGTH_SHORT)
//                    .show()
//            } else {
//                appContext.addRecipe(recipeTitleEditText.text.toString(), null) // TODO - change image uri
//            }
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
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage() {
        val progressBar = ProgressBar(activity)
        // get the extension of the file (e.g jpg)
        val contentResolver = activity?.contentResolver
        val mime = MimeTypeMap.getSingleton()
        val fileExtension = mime.getExtensionFromMimeType(contentResolver?.getType(imageUri))
        appContext.uploadImage(imageUri, fileExtension)
    }


}
