package com.example.chefi

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.work.*
import com.example.chefi.database.*
import com.example.chefi.workers.AddRecipeWorker
import com.example.chefi.workers.UpdateCurrUserFeedWorker
import com.example.chefi.workers.UpdateFollowersFeedWorker
import com.example.chefi.workers.UploadImageWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import java.text.DateFormat
import java.util.*

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

    fun uploadImageToStorage(uri: Uri) {
        val fileExtension = getFileExtension(uri)
        appDb.uploadImageToStorage(uri, fileExtension)
    }

    private fun getFileExtension(imageUri: Uri) : String? {
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

    fun updateFeedWithRecipe(type: String, recipeUid: String) {
        appDb.updateFeedWithRecipe(type, recipeUid)
    }

    fun addRecipeToDb(
        recipeName: String?,
        imageUrl: String?,
        direction: ArrayList<String>?,
        ingredients: ArrayList<String>?,
        status: Int?
    ) {
        appDb.addRecipeToRecipesCollection(recipeName, imageUrl, direction, ingredients, status)
    }

    fun addWorkerUpdateFeedToFollowers(type: String, recipeId: String) {
        val workId = UUID.randomUUID()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val inputData = Data.Builder()
            .putString("type", type)
            .putString("recipeUid", recipeId)
            .build()
        val oneTimeWorkRequest = OneTimeWorkRequest
            .Builder(UpdateFollowersFeedWorker::class.java)
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag(workId.toString())
            .build()
        WorkManager.getInstance(this)
            .enqueue(oneTimeWorkRequest)
    }

    fun addRecipe(
        name: String?,
        imageUrl: String?,
        direction: ArrayList<String>?,
        ingredients: ArrayList<String>?,
        status: Int
    ) : UUID {
        val workId = UUID.randomUUID()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val inputData = Data.Builder()
            .putString(getString(R.string.keyRecipeName), name)
            .putString(getString(R.string.keyRecipeImageUrl), imageUrl)
            .putString(
                getString(R.string.keyRecipeTimeStamp),
                DateFormat.getDateTimeInstance().format(Date())
            )
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

    fun follow(dbUserToFollow: DbUser) {
        appDb.follow(dbUserToFollow, this)
    }

    fun getFirebaseCurrUser() : FirebaseUser? {
        return appDb.getFirebaseCurrUser()
    }

    fun getCurrUser() : DbUser? {
        return appDb.getCurrUser()
    }

    fun deleteRecipe(recipe: AppRecipe) {
        if (recipe.uid != null) {
            addWorkerUpdateFeedToFollowers("delete", recipe.uid!!)
        } else {
            Log.w(TAG_CHEFI, "recipe uid = null")
        }
        appDb.deleteRecipe(recipe)
    }

    fun deleteRecipeFromFavorites(recipeId : String) {
        appDb.deleteRecipeFromFavorites(recipeId)
    }

    fun deleteImage(imageUrl: String?, dbRecipe: DbRecipe?) {
        appDb.deleteImageFromStorage(imageUrl, null)
    }

    fun updateUserFields(fieldName: String, content: String){
        appDb.updateUserFields(fieldName, content)
    }

    fun loadRecipes(user: DbUser?) {
        appDb.loadRecipes(user)
    }

    fun loadFavorites() {
        appDb.loadFavorites()
    }

    fun loadFollowing(dbUser: DbUser?) {
        appDb.loadFollowing(dbUser)
    }

    fun loadFollowers(dbUser: DbUser?) {
        appDb.loadFollowers(dbUser)
    }

    fun loadNotifications() {
        appDb.loadNotifications()
    }

    fun signOut(){
        appDb.signOut()
    }

    fun getUser(userId: String?) {
        appDb.getUser(userId)
    }

    fun deleteComment(comment: Comment, appRecipe: AppRecipe) {
        appDb.deleteComment(comment, appRecipe)
    }

    fun setUnseenNotificationNumber(num : Int) {
        appDb.setUnseenNotification(num)
    }

    fun setUserPermission(appRecipe: AppRecipe, userId: String) {
        appDb.setUserPermission(appRecipe, userId)
    }

    fun addNotification(userDestId: String, content: String, type : NotificationType) {
        appDb.addNotification(userDestId, content, type)
    }

    fun addComment(content: String, recipeId: String) {
        appDb.addComment(content, recipeId, this, "add")
    }

    fun updateRecipeFields(appRecipe: AppRecipe, fieldName: String, content: String?) {
        appDb.updateRecipeFields(appRecipe, fieldName, content)
    }

    fun getUserRecipes() : ArrayList<AppRecipe>? {
        return appDb.getUserRecipes()
    }

    fun getFirebaseAuth() : FirebaseAuth {
        return appDb.getFirebaseAuth()
    }

    fun getUserFavorites() : ArrayList<AppRecipe>? {
        return appDb.getUserFavorites()
    }

    fun getUserFollowing() : ArrayList<DbUser>? {
        return appDb.getUserFollowing()
    }

    fun getUserFollowers() : ArrayList<DbUser>?  {
        return appDb.getUserFollowers()
    }

    fun updatePostsToCurrUserFeed(type: String, userIdTo: String) {
        appDb.updatePostsToCurrUserFeed(type, userIdTo)
    }

    fun unFollow(dbUserToUnFollow: DbUser) {
        appDb.unFollow(dbUserToUnFollow, this)
    }

    fun addRecipeToFavorites(appRecipe: AppRecipe) {
        appDb.addRecipeToFavorites(appRecipe)
    }

    fun removeRecipeFromFavorites(appRecipe: AppRecipe) {
        appDb.removeRecipeFromFavorites(appRecipe)
    }

    fun uploadFeed(fromBeginning: Boolean) {
        appDb.uploadFeed(fromBeginning)
    }

    fun addUserToFollowers(otherDbUser: DbUser) {
        // adding currUser to followers list of otherUser
        appDb.addUserToFollowers(otherDbUser)
    }

    // TODO - delete, for debug only
//    fun loadSingleImage(imageId: String){
//        appDb.loadSingleImage(imageId)
//    }

    fun fireBaseSearchUsers(searchText: String) {
        appDb.fireBaseSearchUsers(searchText)
    }
}