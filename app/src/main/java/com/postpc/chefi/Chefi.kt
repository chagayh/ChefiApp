package com.postpc.chefi

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.work.*
import com.postpc.chefi.database.*
import com.postpc.chefi.workers.AddRecipeWorker
import com.postpc.chefi.workers.UploadImageWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.gson.Gson
import java.text.DateFormat
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

    fun getUserNotifications() : ArrayList<AppNotification>? {
        return appDb.getUserNotification()
    }

    fun signIn(email: String, password: String, userName: String){
        appDb.createUserWithEmailPassword(email, password, userName)
    }

    fun logIn(email: String, password: String){
        appDb.logIn(email, password)
    }

    fun filterForTradeRecipesList() : ArrayList<AppRecipe> {
        return appDb.filterForTradeRecipesList()
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
        status: Boolean,
        location: String?
    ) {
        appDb.addRecipeToRecipesCollection(recipeName, imageUrl, direction, ingredients, status, location)
    }

    fun addRecipe(
        name: String?,
        imageUrl: String?,
        direction: ArrayList<String>?,
        ingredients: ArrayList<String>?,
        status: Boolean,
        location: String?
    ) : UUID {
        val workId = UUID.randomUUID()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val inputData = Data.Builder()
            .putString(getString(R.string.keyRecipeName), name)
            .putString(getString(R.string.keyRecipeImageUrl), imageUrl)
            .putString(getString(R.string.keyRecipeLocation), location)
            .putString(
                getString(R.string.keyRecipeTimeStamp),
                DateFormat.getDateTimeInstance().format(Date())
            )
            .putString(getString(R.string.keyRecipeDirections), Gson().toJson(direction))
            .putString(getString(R.string.keyRecipeIngredients), Gson().toJson(ingredients))
            .putBoolean(getString(R.string.keyRecipeStatus), status)
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
        appDb.follow(dbUserToFollow)
    }

    fun getCurrUser() : DbUser? {
        return appDb.getCurrUser()
    }

    fun deleteRecipe(recipe: AppRecipe) {
        appDb.deleteRecipe(recipe)
    }

    fun deleteRecipeFromFavorites(appRecipe: AppRecipe) {
        appDb.deleteRecipeFromFavorites(appRecipe)
    }

    fun deleteImage(imageUrl: String?) {
        appDb.deleteImageFromStorage(imageUrl, null)
    }

    fun updateUserFields(fieldName: String, content: String){
        appDb.updateUserFields(fieldName, content)
    }

    fun initLastSeenNotification() {
        appDb.initLastSeenNotification()
    }

    fun loadRecipes(user: DbUser?) {
        appDb.loadRecipes(user, "recipes")
    }

    fun loadFavorites() {
        appDb.loadRecipes(null, "favorites")
//        appDb.loadFavorites()
    }

    fun loadFollowing(dbUser: DbUser?) {
        appDb.loadFollowUnFollow(dbUser, "following")
    }

    fun loadFollowers(dbUser: DbUser?) {
        appDb.loadFollowUnFollow(dbUser, "followers")
    }

    fun loadNotifications() {
        appDb.loadNotifications()
    }

    fun signOut(){
        appDb.signOut()
    }

    fun deleteComment(comment: Comment, appRecipe: AppRecipe) {
        appDb.deleteComment(comment, appRecipe)
    }

    fun addToUserPermission(appRecipe: AppRecipe, userRef: DocumentReference) {
        appDb.addToRecipePermission(appRecipe, userRef)
    }

    fun addNotification(userDestRef: DocumentReference,
                        recipeRef: DocumentReference?,
                        offeredRecipeRef: DocumentReference?,
                        type: NotificationType) {
        appDb.addNotification(userDestRef, recipeRef, offeredRecipeRef, type)
    }

    fun addComment(content: String, recipeId: String) {
        appDb.addComment(content, recipeId)
    }

    fun updateRecipeFields(appRecipe: AppRecipe, fieldName: String) {
        appDb.updateRecipeFields(appRecipe, fieldName)
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

    fun unFollow(dbUserToUnFollow: DbUser) {
        appDb.unFollow(dbUserToUnFollow)
    }

    fun addRecipeToFavorites(appRecipe: AppRecipe) {
        Log.e("favoritesBug", "adds recipe ${appRecipe.uid} to favorites")
        appDb.addRecipeToFavorites(appRecipe)
    }

    fun uploadFeed() {
        appDb.uploadFeed()
        Log.d("updateFeed", "uploadFeed in Chefi")
    }

    fun getUnseenNotification() : Int {
        return appDb.getUnseenNotification()
    }

    fun addUserToFollowers(otherDbUser: DbUser) {
        // adding currUser to followers list of otherUser
        appDb.addUserToFollowers(otherDbUser)
    }

    fun deleteNotification(appNotificationItem: AppNotification) {
        appDb.deleteNotification(appNotificationItem)
    }

    fun fireBaseSearchUsers(searchText: String) {
        appDb.fireBaseSearchUsers(searchText)
    }

    fun isFollowedByMe(ref: DocumentReference): Boolean{
        return appDb.isFollowedByMe(ref)
    }
}