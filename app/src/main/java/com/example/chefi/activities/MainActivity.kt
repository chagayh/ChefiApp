package com.example.chefi.activities

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.database.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.lifecycle.Observer
import com.example.chefi.LiveDataHolder

class MainActivity : AppCompatActivity() {

    private val appContext: Chefi
        get() = applicationContext as Chefi

    private var currentPhotoPath: String? = null
    private var capturedImageUri: Uri? = null

    companion object{
        // TAGS
        private val TAG_MAIN_ACTIVITY: String = "mainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG_MAIN_ACTIVITY, "in onCreate of mainActivity user's name =${appContext.getCurrUser()?.name}")

        setObserver()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navController = findNavController(R.id.navHostFragment)
        bottomNavigationView.setupWithNavController(navController)

    }

    private fun setObserver() {
        // data class User observer
        val observer = Observer<User> { value ->
            if (value == null){
                Log.d(TAG_MAIN_ACTIVITY, "null user, live data")
            } else {
                Toast.makeText(this, "user ${value.name} connected", Toast.LENGTH_SHORT)
                    .show()
                appContext.loadRecipes(null)
                appContext.loadFavoritesFirstTime()
                appContext.loadFollowersFirstTime(null)
                appContext.loadFollowingFirstTime(null)
                appContext.loadNotificationsFirstTime()
            }
        }
        LiveDataHolder.getUserLiveData().observe (this, observer)
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
