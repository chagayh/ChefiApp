package com.postpc.chefi.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import com.postpc.chefi.Chefi
import com.postpc.chefi.R
import java.io.IOException
import java.util.*
import androidx.lifecycle.Observer
import android.Manifest
import android.annotation.SuppressLint
import android.os.Environment
import android.provider.MediaStore
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import com.postpc.chefi.activities.MainActivity
import java.io.File
import java.text.SimpleDateFormat

/**
 * A simple [Fragment] subclass.
 * Use the [AddFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddFragment : Fragment() {

    val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private var currentPhotoPath: String? = null
    private var imageUri: Uri? = null
    private lateinit var galleryBtn: TextView
    private lateinit var cameraBtn: TextView
    private lateinit var progressBar: ConstraintLayout
    private lateinit var mainBts: LinearLayout

    companion object {
        private val REQUEST_PICK_IMAGE_FROM_GALLERY = 71
        private val REQUEST_IMAGE_FROM_CAMERA = 70
        private val TAG_ADD_FRAGMENT = "addFragment"
        private val REQUEST_CODE_PERMISSION_GENERAL = 2000
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG_ADD_FRAGMENT, "bundle = $savedInstanceState")
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add, container, false)
        galleryBtn = view.findViewById(R.id.galleryBtn)
        cameraBtn = view.findViewById(R.id.cameraBtn)
        progressBar = view.findViewById(R.id.constrainLayoutProgressBar)
        mainBts = view.findViewById(R.id.mainLinearLayoutAdd)
        setComponents()
        checkForPermissions()
        return view
    }

    private fun checkForPermissions() {

        val permissionsArray = arrayOf(Manifest.permission.CAMERA,
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

    private fun setComponents() {

        galleryBtn.setOnClickListener { v ->
            launchGallery()
        }
        cameraBtn.setOnClickListener {
            showUploadPicOptions()
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG_ADD_FRAGMENT, "onActivityResult, requestCode = $requestCode")
        // TODO - add Progress BAR

        when {
            (requestCode == REQUEST_PICK_IMAGE_FROM_GALLERY
                    && resultCode == RESULT_OK
                    && data != null && data.data != null) -> {
                try {
                    imageUri = data.data!!
                    Log.d(TAG_ADD_FRAGMENT, "data = $imageUri")
                    val workId = appContext.uploadImage(imageUri!!)
                    setWorkObserver(workId)
                    // TODO - if goes back delete the recipe_profile document
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            (requestCode == REQUEST_IMAGE_FROM_CAMERA
                    && resultCode == RESULT_OK) -> {
                Log.d(TAG_ADD_FRAGMENT, "data = $data")
                try {
                    val myActivity = activity as MainActivity
                    imageUri = myActivity.getCapturedImageUri()
                    Log.d(TAG_ADD_FRAGMENT, "photoUri = $imageUri")
                    val workId = appContext.uploadImage(imageUri!!)
                    setWorkObserver(workId)
                    // TODO - if goes back delete the recipe_profile document
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @SuppressLint("RestrictedApi", "VisibleForTests")
    private fun setWorkObserver(workId: UUID) {
        appContext.getWorkManager().getWorkInfoByIdLiveData(workId).observe(this,
            Observer { value ->
                Log.d(
                    TAG_ADD_FRAGMENT,
                    "value = ${value.outputData.getString(getString(R.string.keyUrl))}"
                )
                Log.d("change_url", "in setWorkObserver image url = ${value.outputData.getString(getString(R.string.keyUrl))}")
                if (value.outputData.size() != 0) {
                    // TODO - STOP Progress BAR
                    Log.d("change_url", "value.outputData.size() == 0")
                    val imageUrl =
                        value.outputData.getString(getString(R.string.keyUrl))
                    val action = AddFragmentDirections.actionAddToAddRecipeDetails(imageUrl!!)
                    view?.findNavController()?.navigate(action)
                }
            })
        mainBts.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
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
                    Log.d(TAG_ADD_FRAGMENT, "${ex.stackTrace}")
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        appContext,
                        "com.postpc.android.chefi",
                        it
                    )
                    imageUri = photoURI
                    myActivity.setCapturedImageUri(imageUri!!)
                    myActivity.setCurrentPhotoPath(currentPhotoPath!!)
                    Log.d(TAG_ADD_FRAGMENT, "imageUri?.path = ${imageUri?.path}, currentPhotoPath = $currentPhotoPath")
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_FROM_CAMERA)
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
}
