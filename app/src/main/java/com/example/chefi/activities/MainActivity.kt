package com.example.chefi.activities

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import android.speech.tts.TextToSpeech
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.chefi.Chefi
import com.example.chefi.LiveDataHolder
import com.example.chefi.ObserveWrapper
import com.example.chefi.R
import com.example.chefi.database.DbUser
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener{

    private val appContext: Chefi
        get() = applicationContext as Chefi

    private var currentPhotoPath: String? = null
    private var capturedImageUri: Uri? = null
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var bottomMenu: Menu
    private lateinit var notificationBadge: BadgeDrawable
    private var tts: TextToSpeech? = null
    private var ttsFlag: Boolean = false

    companion object{
        // TAGS
        private val TAG_MAIN_ACTIVITY: String = "mainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(
            TAG_MAIN_ACTIVITY,
            "in onCreate of mainActivity user's name =${appContext.getCurrUser()?.name}"
        )
        tts = TextToSpeech(this, this)
        tts?.setSpeechRate(0.6F)


//        setUserObserver()

        setNotificationBadgeObserver()
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        val navController = findNavController(R.id.navHostFragment)
        bottomNavigationView.setupWithNavController(navController)
        bottomMenu = bottomNavigationView.menu
        val notificationItemId = bottomMenu.getItem(3).itemId
        notificationBadge = bottomNavigationView.getOrCreateBadge(notificationItemId)
        notificationBadge.isVisible = false
        val user = appContext.getCurrUser()
        if (user?.lastSeenNotification != null) {
            setNotificationBadge(user.lastSeenNotification!!)
        }
    }

    private fun setUserObserver() {
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
                if (content.lastSeenNotification != null) {
                    setNotificationBadge(content.lastSeenNotification!!)
                }
            }
        }
        LiveDataHolder.getUserLiveData().observe(this, observer)
    }

    private fun setNotificationBadgeObserver() {
        LiveDataHolder.getNotificationIntLiveData().observe(this,
            Observer { value ->
                val content = value.getContentIfNotHandled()
                if (content != null){
                    setNotificationBadge(content)
                }
            })
    }

    fun setNotificationBadge(num: Int) {
        if (num > 0) {
            notificationBadge.isVisible = true
            notificationBadge.number = num
        } else if (num == 0) {
            notificationBadge.isVisible = false
            notificationBadge.number = 0
            appContext.initLastSeenNotification()
        }
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

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS)
        {
            Log.e("speecher" ,"kas")
            val result = tts!!.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA ||
                result != TextToSpeech.LANG_NOT_SUPPORTED)
            {
                Log.e("speecher" ,"kas2")
                ttsFlag = true
            }
        }
    }

    fun speak(text: String){
        if (ttsFlag){
            tts!!.speak(text, TextToSpeech.QUEUE_ADD, null, null)
        }
    }
    override fun onDestroy()
    {
        if(tts != null)
        {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }
}
