package com.example.chefi

import android.app.Application
import android.content.Context
import android.net.Uri
import com.example.chefi.database.AppDb
import com.example.chefi.database.User
//import com.example.chefi.workers.FetchDataAsyncWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Chefi : Application() {
    private lateinit var appDb : AppDb


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
        appCon = this
        appDb = AppDb()
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

    fun uploadImage(uri: Uri, fileExtension: String?) {
        appDb.uploadImageToStorage(uri, fileExtension)
    }

//    fun loadRecipes() : UUID {
////        appDb.loadRecipes()
//        val workId = UUID.randomUUID()
//        val constraints = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.CONNECTED)
//            .build()
//        val inputData = Data.Builder()
//            .putString(getString(R.string.keyRecipeType), "Recipe")
//            .build()
//        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(FetchDataAsyncWorker::class.java)
//            .setConstraints(constraints)
//            .setInputData(inputData)
//            .addTag(workId.toString())
//            .build()
//        WorkManager.getInstance(this)
//            .enqueue(oneTimeWorkRequest)
//
//        return oneTimeWorkRequest.id
//    }

    fun loadRecipes_2() {
        appDb.loadRecipesFirstTime()
    }

//    fun loadRecipes_1() {
//        CoroutineScope(IO).launch {
//            appDb.loadRecipes_1()
//            sendRecipes()
//        }
//    }

    private suspend fun sendRecipes() {
        withContext (Dispatchers.Main) {
            appDb.postRecipes()
        }
    }

    fun addUserToCollection(user: User?){
        appDb.addUserToCollection(user)
    }

    fun postUser(user: User?) {
        appDb.postUser(user)
    }

    // TODO - for debug only
    fun getCurrUser() : User? {
        return appDb.getCurrUser()
    }

//    fun addRecipe(recipeTitle: String?) {
//        appDb.addRecipe(recipeTitle)
//    }

}