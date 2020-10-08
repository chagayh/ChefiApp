package com.example.chefi.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.navigation.findNavController
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.activities.MainActivity
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.Observer

/**
 * A simple [Fragment] subclass.
 * Use the [UploadPicFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UploadPicFragment : Fragment() {

    val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private var currentPhotoPath: String? = null
    private var imageUri: Uri? = null
    private lateinit var cameraBtn: Button
    private lateinit var galleryBtn: Button

    private val REQUEST_PICK_IMAGE_FROM_GALLERY = 3242
    private val REQUEST_IMAGE_FROM_CAMERA = 44
    private val REQUEST_CODE_PERMISSION_GENERAL = 2034
    private val TAG_UPLOAD_PIC_FRAGMENT = "uploadPicFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_upload_pic, container, false)
        cameraBtn = view.findViewById(R.id.searchBtn)
        galleryBtn = view.findViewById(R.id.galleryBtn)
        setButtons()
        checkForPermissions()
        return view
    }

    private fun launchGallery() {
        val intent = Intent()
        with(intent) {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            REQUEST_PICK_IMAGE_FROM_GALLERY
        )
    }

    private fun setButtons() {
        cameraBtn.setOnClickListener {
            showUploadPicOptions()
        }
        galleryBtn.setOnClickListener {
            launchGallery()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG_UPLOAD_PIC_FRAGMENT, "onActivityResult, requestCode = $requestCode")
        when {
            (requestCode == REQUEST_PICK_IMAGE_FROM_GALLERY
                    && resultCode == Activity.RESULT_OK
                    && data != null && data.data != null) -> {
                try {
                    imageUri = data.data!!
                    Log.d(TAG_UPLOAD_PIC_FRAGMENT, "data = $imageUri")
                    val workId = appContext.uploadImage(imageUri!!)
                    setWorkObserver(workId)
                    // TODO - if goes back delete the recipe_profile document
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            (requestCode == REQUEST_IMAGE_FROM_CAMERA
                    && resultCode == Activity.RESULT_OK) -> {
                Log.d(TAG_UPLOAD_PIC_FRAGMENT, "data = $data")
                try {
                    val myActivity = activity as MainActivity
                    imageUri = myActivity.getCapturedImageUri()
                    Log.d(TAG_UPLOAD_PIC_FRAGMENT, "photoUri = $imageUri")
                    val workId = appContext.uploadImage(imageUri!!)
                    setWorkObserver(workId)
                    // TODO - if goes back delete the recipe_profile document
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun showUploadPicOptions() {
        val myActivity: MainActivity = activity as MainActivity
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(myActivity.packageManager!!)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    Log.d(TAG_UPLOAD_PIC_FRAGMENT, "${ex.stackTrace}")
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        appContext,
                        "com.example.android.chefi",
                        it
                    )
                    imageUri = photoURI
                    myActivity.setCapturedImageUri(imageUri!!)
                    myActivity.setCurrentPhotoPath(currentPhotoPath!!)
                    Log.d(TAG_UPLOAD_PIC_FRAGMENT, "imageUri?.path = ${imageUri?.path}, currentPhotoPath = $currentPhotoPath")
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_FROM_CAMERA)
                }
            }
        }
    }

    private fun checkForPermissions() {

        val permissionsArray = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

        val cameraPer = ActivityCompat.checkSelfPermission(appContext, permissionsArray[0])
        val readPer = ActivityCompat.checkSelfPermission(appContext, permissionsArray[1])
        val writePer = ActivityCompat.checkSelfPermission(appContext, permissionsArray[2])

        if (cameraPer != PackageManager.PERMISSION_GRANTED
            || readPer != PackageManager.PERMISSION_GRANTED
            || writePer != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                permissionsArray,
                REQUEST_CODE_PERMISSION_GENERAL
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_PERMISSION_GENERAL -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    checkForPermissions()
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = appContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    @SuppressLint("RestrictedApi", "VisibleForTests")
    private fun setWorkObserver(workId: UUID) {
        appContext.getWorkManager().getWorkInfoByIdLiveData(workId).observe(this,
            Observer { value ->
                Log.d(
                    TAG_UPLOAD_PIC_FRAGMENT,
                    "value = ${value.outputData.getString(getString(R.string.keyUrl))}"
                )
                Log.d("change_url", "in setWorkObserver image url = ${value.outputData.getString(getString(R.string.keyUrl))}")
                if (value.outputData.size() != 0) {
                    Log.d("change_url", "value.outputData.size() == 0")
                    val imageUrl =
                        value.outputData.getString(getString(R.string.keyUrl))
                    val action = AddFragmentDirections.actionAddToAddRecipeDetails(imageUrl!!)
                    view?.findNavController()?.navigate(action)
                }
            })
    }
}