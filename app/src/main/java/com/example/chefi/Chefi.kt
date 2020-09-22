package com.example.chefi

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.work.*
import com.example.chefi.database.AppDb
import com.example.chefi.database.User
import com.example.chefi.workers.AddRecipeWorker
import com.example.chefi.workers.UploadImageWorker
import com.google.gson.Gson
//import com.example.chefi.workers.FetchDataAsyncWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class Chefi : Application() {
    private lateinit var appDb : AppDb
    private lateinit var workManager : WorkManager

    // TAGS
    private val TAG_LIVE_DATA: String = "userLiveData"

    companion object {
        private lateinit var appCon: Context

        fun getCon(): Context {
            return appCon
        }
    }

    override fun onCreate() {
        super.onCreate()
        workManager = WorkManager.getInstance(this)
        appCon = this
        appDb = AppDb()
    }

    fun getWorkManager() : WorkManager {
        return workManager
    }

    fun signIn(email: String, password: String, name: String){
        appDb.createUserWithEmailPassword(email, password, name)
    }

    fun logIn(email: String, password: String){
        appDb.logIn(email, password)
    }

    fun checkCurrentUser() : User? {
        return appDb.getCurrUser()
    }

    fun uploadImageToStorage(uri: Uri, fileExtension: String?) {
        appDb.uploadImageToStorage(uri, fileExtension)
    }

    fun uploadImage(uri: Uri, fileExtension: String?) : UUID {

        val workId = UUID.randomUUID()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val inputData = Data.Builder()
            .putString(getString(R.string.keyUri), uri.toString())
            .putString(getString(R.string.keyFileExtension), fileExtension)
            .build()
        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(UploadImageWorker::class.java)
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag(workId.toString())
            .build()
        WorkManager.getInstance(this)
            .enqueue(oneTimeWorkRequest)

        return oneTimeWorkRequest.id

    }

    fun addRecipeToDb(recipeTitle: String?, imageUrl: String?) {
        appDb.addRecipeToRecipesCollection(recipeTitle, imageUrl)
    }

    fun addRecipe(recipeTitle: String?, imageUrl: String?) : UUID {
        val workId = UUID.randomUUID()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val inputData = Data.Builder()
            .putString(getString(R.string.keyRecipeTitle), recipeTitle)
            .putString(getString(R.string.keyRecipeImageUrl), imageUrl)
            .build()
        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(AddRecipeWorker::class.java)
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag(workId.toString())
            .build()
        WorkManager.getInstance(this)
            .enqueue(oneTimeWorkRequest)

        return oneTimeWorkRequest.id
    }

    fun getCurrUser() : User? {
        return appDb.getCurrUser()
    }
}