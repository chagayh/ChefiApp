package com.example.chefi.activities

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.database.DbUser
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.lifecycle.Observer
import com.example.chefi.LiveDataHolder
import com.example.chefi.ObserveWrapper

class MainActivity : AppCompatActivity() {

    private val appContext: Chefi
        get() = applicationContext as Chefi

    private var currentPhotoPath: String? = null
    private var capturedImageUri: Uri? = null
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var bottomMenu: Menu

    companion object{
        // TAGS
        private val TAG_MAIN_ACTIVITY: String = "mainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG_MAIN_ACTIVITY, "in onCreate of mainActivity user's name =${appContext.getCurrUser()?.name}")

        setObserver()

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        val navController = findNavController(R.id.navHostFragment)
        bottomNavigationView.setupWithNavController(navController)
        bottomMenu = bottomNavigationView.menu

    }

    private fun setObserver() {
        // data class User observer
        val observer = Observer<ObserveWrapper<DbUser>> { value ->
            val content = value.getContentIfNotHandled()
            if (content == null){
                Log.d(TAG_MAIN_ACTIVITY, "null user, live data")
            } else {
                Toast.makeText(this, "@${content.userName} was connected", Toast.LENGTH_LONG)
                    .show()
                appContext.loadRecipes(null)
                appContext.loadFavorites()
                appContext.loadFollowers(null)
                appContext.loadFollowing(null)
                appContext.loadNotifications()
                val notificationItemId = bottomMenu.getItem(3).itemId
                val badge = bottomNavigationView.getOrCreateBadge(notificationItemId)
                badge.isVisible = true
                if (content.lastSeenNotification != null) {
                    Log.d("badge", "${content.lastSeenNotification}")
                    badge.number = content.lastSeenNotification!!
                }
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
