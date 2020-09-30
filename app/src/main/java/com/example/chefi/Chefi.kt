package com.example.chefi

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.work.*
import com.example.chefi.database.AppDb
import com.example.chefi.database.Recipe
import com.example.chefi.database.User
import com.example.chefi.workers.AddRecipeWorker
import com.example.chefi.workers.UploadImageWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import java.util.*
import kotlin.collections.ArrayList

class Chefi : Application() {
    private lateinit var appDb : AppDb
    private lateinit var workManager : WorkManager

    // TAGS
    private val TAG_LIVE_DATA: String = "userLiveData"
    private val TAG_CHEFI: String = "chefi"

    companion object {
        private lateinit var appCon: Context

        fun getCon(): Context {
            return appCon
        }
    }

    override fun onCreate() {
        super.onCreate()
        appCon = this
        workManager = WorkManager.getInstance(this)
        appDb = AppDb()
    }

    fun getWorkManager() : WorkManager {
        return workManager
    }

    fun signIn(email: String, password: String, userName: String){
        appDb.createUserWithEmailPassword(email, password, userName)
    }

    fun logIn(email: String, password: String){
        appDb.logIn(email, password)
    }

    fun checkCurrentUser() : User? {
        return appDb.getCurrUser()
    }

    fun uploadImageToStorage(uri: Uri) {
        val fileExtension = getFileExtension(uri)
        appDb.uploadImageToStorage(uri, fileExtension)
    }

    private fun getFileExtension(imageUri : Uri) : String? {
        val contentResolver = contentResolver
        val mime = MimeTypeMap.getSingleton()
        val fileExtension = mime.getExtensionFromMimeType(contentResolver?.getType(imageUri))
        Log.d(TAG_CHEFI, "getFileExtension fun, fileExtension = $fileExtension")
        return fileExtension
    }

    fun uploadImage(uri: Uri) : UUID {

        val workId = UUID.randomUUID()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val inputData = Data.Builder()
            .putString(getString(R.string.keyUri), uri.toString())
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

    fun addRecipeToDb(recipeName: String?,
                      imageUrl: String?,
                      direction: ArrayList<String>?,
                      ingredients: ArrayList<String>?,
                      status: Int?) {
        appDb.addRecipeToRecipesCollection(recipeName, imageUrl, direction, ingredients, status)
    }

    fun addRecipe(name: String?,
                  imageUrl: String?,
                  direction: ArrayList<String>?,
                  ingredients: ArrayList<String>?,
                  status: Int) : UUID {
        val workId = UUID.randomUUID()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val inputData = Data.Builder()
            .putString(getString(R.string.keyRecipeName), name)
            .putString(getString(R.string.keyRecipeImageUrl), imageUrl)
            .putString(getString(R.string.keyRecipeDirections), Gson().toJson(direction))
            .putString(getString(R.string.keyRecipeIngredients), Gson().toJson(ingredients))
            .putInt(getString(R.string.keyRecipeStatus), status)
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

    fun deleteRecipe(recipe : Recipe) {
        return appDb.deleteRecipe(recipe)
    }

    fun deleteImage(imageUrl: String?, recipe: Recipe?) {
        appDb.deleteImageFromStorage(imageUrl, null)
    }

    fun updateUserFields(fieldName: String, content: String){
        appDb.updateUserFields(fieldName, content)
    }

    fun loadRecipes(user: User?) {
        appDb.loadRecipes(user)
    }

    fun loadFavoritesFirstTime() {
        appDb.loadFavorites()
    }

    fun loadFollowingFirstTime(user: User?) {
        appDb.loadFollowing(user)
    }

    fun loadFollowersFirstTime(user: User?) {
        appDb.loadFollowers(user)
    }

    fun loadNotificationsFirstTime() {
        appDb.loadNotificationsFirstTime()
    }

    fun signOut(){
        appDb.signOut()
    }

    fun getUserRecipes() : ArrayList<Recipe>? {
        return appDb.getUserRecipes()
    }

    fun getFirebaseAuth() : FirebaseAuth {
        return appDb.getFirebaseAuth()
    }

    fun getUserFavorites() : ArrayList<Recipe>? {
        return appDb.getUserFavorites()
    }

    fun getUserFollowing() : ArrayList<User>? {
        return appDb.getUserFollowing()
    }

    fun getUserFollowers() : ArrayList<User>?  {
        return appDb.getUserFollowers()
    }

    fun follow(userToFollow : User) {
        appDb.follow(userToFollow)
    }

    fun unFollow(userToUnFollow : User) {
        appDb.unFollow(userToUnFollow)
    }

    fun addRecipeToFavorites(recipe: Recipe) {
        appDb.addRecipeToFavorites(recipe)
    }

    fun removeRecipeFromFavorites(recipe: Recipe) {
        appDb.removeRecipeFromFavorites(recipe)
    }

    fun addUserToFollowers(otherUser: User) {
        // adding currUser to followers list of otherUser
        appDb.addUserToFollowers(otherUser)
    }

    // TODO - delete, for debug only
//    fun loadSingleImage(imageId: String){
//        appDb.loadSingleImage(imageId)
//    }

    fun fireBaseSearchUsers(searchText: String) {
        appDb.fireBaseSearchUsers(searchText)
    }
}