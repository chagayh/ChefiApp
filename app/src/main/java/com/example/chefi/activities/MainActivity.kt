package com.example.chefi.activities

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.chefi.Chefi
import com.example.chefi.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val appContext: Chefi
        get() = applicationContext as Chefi

    private var currentPhotoPath: String? = null
    private var capturedImageUri: Uri? = null

    companion object{
        // TAGS
        private val TAG_LIVE_DATA: String = "userLiveData"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appContext.loadRecipes(null)
        appContext.loadFavoritesFirstTime()
        appContext.loadFollowersFirstTime(null)
        appContext.loadFollowingFirstTime(null)
        appContext.loadNotificationsFirstTime()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navController = findNavController(R.id.navHostFragment)
        bottomNavigationView.setupWithNavController(navController)

    }

    fun getCurrentPhotoPath() : String?{
        return currentPhotoPath
    }

    fun getCapturedImageUri() : Uri?{
        return capturedImageUri
    }

    fun setCurrentPhotoPath(imagePath: String){
        currentPhotoPath = imagePath
    }

    fun setCapturedImageUri(uri: Uri){
        capturedImageUri = uri
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (capturedImageUri != null) {
            outState.putString(getString(R.string.keyCapturedPhotoUri), capturedImageUri.toString());
        }

        if (currentPhotoPath != null) {
            outState.putString(getString(R.string.keyPathPhoto), currentPhotoPath);
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        if (savedInstanceState.containsKey(getString(R.string.keyCapturedPhotoUri))){
            capturedImageUri = Uri.parse(savedInstanceState.getString(getString(R.string.keyCapturedPhotoUri)))
        }
        if (savedInstanceState.containsKey(getString(R.string.keyPathPhoto))){
            currentPhotoPath = savedInstanceState.getString(getString(R.string.keyPathPhoto))
        }
        super.onRestoreInstanceState(savedInstanceState)
    }
}
