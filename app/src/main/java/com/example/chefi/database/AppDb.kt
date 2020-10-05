package com.example.chefi.database

/*
https://github.com/firebase/snippets-android/blob/c2d8cfd95d996bd7c0c3e0bdf35a91430043f73d/firestore/app/src/main/java/com/google/example/firestore/kotlin/DocSnippets.kt#L462-L473
 */


import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import com.example.chefi.Chefi
import com.example.chefi.LiveDataHolder
import com.example.chefi.ObserveWrapper
import com.example.chefi.R
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("Registered")
class AppDb {

    // Declare an instance of FirebaseAuth
    private val auth: FirebaseAuth = Firebase.auth
//    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
//    private val firestore = FirebaseFirestore

    // Chefi.getCon().getString(R.string.imageUpload)
    private val storageRef = FirebaseStorage.getInstance().getReference("uploads")
    private val databaseRef = FirebaseDatabase.getInstance().getReference("uploads")

    // Users fields
    private var currDbUser: DbUser? = null
    private var userDbRecipes : ArrayList<DbRecipe>? = null
    private var dbUserFollowing : ArrayList<DbUser>? = null
    private var dbUserFollowers : ArrayList<DbUser>? = null
    private var userNotification : ArrayList<DbNotificationItem>? = null  // TODO CHANGE!!
    private var userFavorites : ArrayList<DbRecipe>? = null


    //App Objects
    private var userAppRecipes : ArrayList<AppRecipe>? = null
    private var userAppFavorites : ArrayList<AppRecipe>? = null
    private var userAppNotification : ArrayList<AppNotification>? = null

    companion object {
        // TAGS
        private const val TAG_APP_DB: String = "appDb"
    }

    init {
        updateCurrentUserField()
        createNotificationsLiveQuery()
    }

    fun getFirebaseAuth() : FirebaseAuth {
        return auth
    }

    fun getUserRecipes() : ArrayList<AppRecipe>? {
        return userAppRecipes
    }

    fun getUserFavorites() : ArrayList<AppRecipe>? {
        return userAppFavorites
    }

    fun getUserFollowing() : ArrayList<DbUser>? {
        return dbUserFollowing
    }

    fun getUserFollowers() : ArrayList<DbUser>? {
        return dbUserFollowers
    }

    fun getCurrUser(): DbUser? {
        return currDbUser
    }

    fun getFirebaseCurrUser(): FirebaseUser? {
        return auth.currentUser
    }

    private fun initCurrUser(){
        currDbUser = null
        userDbRecipes = null
        dbUserFollowers = null
        dbUserFollowing = null
        userNotification = null

        //
        userAppRecipes = null
        userAppFavorites = null
        userAppNotification = null
    }

    private fun updateCurrentUserField() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null){
            val usersCollectionPath = Chefi.getCon().getString(R.string.usersCollection)
            val userId = firebaseUser.uid
            val documentUser = firestore.collection(usersCollectionPath).document(userId)
            documentUser.get().addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject<DbUser>()
                Log.d(TAG_APP_DB, "user?.name = ${user?.name} in updateCurrentUser")
                if (user != null) {
                    currDbUser = user
                    userDbRecipes = null
                    dbUserFollowers = null
                    dbUserFollowing = null
                    userNotification = null

                    userAppRecipes = null
                    userAppFavorites = null
                    userAppNotification = null
                    postUser(currDbUser)
                } else {
                    Log.d("account", "user = null")
                }
            }
        }
    }

    fun createUserWithEmailPassword(email: String, password: String, userName: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG_APP_DB, "createUserWithEmail:success")
                    val user = auth.currentUser
                    val newUser = DbUser(
                        user?.uid,
                        email = user?.email,
                        userName = userName,
                        userName_lowerCase = userName.toLowerCase(Locale.ROOT)
                    )
                    addUserToCollection(newUser)
                    postUser(newUser)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG_APP_DB, "createUserWithEmail:failure", task.exception)
                }
            }
    }

    private fun addUserToCollection(dbUser: DbUser?) {
        Log.d(TAG_APP_DB, "in add to collection, uid = ${dbUser?.uid}")
        if (dbUser?.uid != null) {
            Log.d(TAG_APP_DB, "in add to collection")
            val myReference = firestore
                .collection(Chefi.getCon().getString(R.string.usersCollection))
                .document(dbUser.uid!!)
            dbUser.myReference = myReference
            myReference.set(dbUser)
                .addOnSuccessListener {
                updateCurrentUserField()
            }
        }
    }

    fun logIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    updateCurrentUserField()
                    Log.d(TAG_APP_DB, "logInWithEmail:success")
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG_APP_DB, "logInWithEmail:failure", task.exception)
                }
            }
    }

    private fun updateUserInUsersCollection(dbUser: DbUser?) {
        // update the user in the db
        if (dbUser == null) {
            val currUserId = currDbUser?.uid
            if (currUserId != null) {
                firestore.collection(Chefi.getCon().getString(R.string.usersCollection))
                    .document(currUserId)
                    .set(currDbUser!!, SetOptions.merge())
                //                .set(currUser!!)
            }
        } else {
            val userId = dbUser.uid
            if (userId != null) {
                firestore.collection(Chefi.getCon().getString(R.string.usersCollection))
                    .document(userId)
                    .set(dbUser, SetOptions.merge())
                //                .set(currUser!!)
            }
        }
    }

    private fun updateRecipeInRecipesCollection(dbRecipe: DbRecipe) {
        // update the user in the db
        val recipeId = dbRecipe.uid
        if (recipeId != null) {
            firestore.collection(Chefi.getCon().getString(R.string.recipesCollection))
                .document(recipeId)
                .set(dbRecipe, SetOptions.merge())
        }
    }

    private fun addRecipeToLocalCurrUserObject(document: DocumentReference) {
        if (currDbUser == null) {
            Log.e(TAG_APP_DB, "current user = null")
        } else if (currDbUser?.recipes == null) {
            currDbUser?.recipes = ArrayList()
        }
        Log.d(TAG_APP_DB, "currUser.recipe.size = ${currDbUser?.recipes?.size}")
        // TODO - add recipe to usersRecipeList
        currDbUser?.recipes?.add(document)
    }

    fun addRecipeToRecipesCollection(
        recipeName: String?,
        imageUrl: String?,
        direction: ArrayList<String>?,
        ingredients: ArrayList<String>?,
        status: Int?
    )  {
        val recipeCollectionPath = Chefi.getCon().getString(R.string.recipesCollection)
        val usersCollectionPath = Chefi.getCon().getString(R.string.usersCollection)

        val newDocument = firestore.collection(recipeCollectionPath).document()
        val userDocument = firestore.collection(usersCollectionPath)
            .document(currDbUser?.uid!!)
        Log.d(TAG_APP_DB, "userDocument = $userDocument")

        val dbRecipe = DbRecipe(
            uid = newDocument.id,
            description = recipeName,
            likes = 0,
            imageUrl = imageUrl,
            comments = ArrayList(),
            directions = direction,
            ingredients = ingredients,
            status = status,
            owner = userDocument
        )

        val appRecipe = AppRecipe(
            uid = newDocument.id,
            description = recipeName,
            likes = 0,
            imageUrl = imageUrl,
            comments = ArrayList(),
            directions = direction,
            ingredients = ingredients,
            status = status,
            owner = currDbUser
        )

        if (userDbRecipes == null) {
            userDbRecipes = ArrayList()
        }
        if (userAppRecipes == null) {
            userAppRecipes = ArrayList()
        }
        newDocument.set(dbRecipe)
            .addOnSuccessListener {
                Log.d(TAG_APP_DB, "in success of addRecipeToRecipesCollection")
                userDbRecipes?.add(dbRecipe)
                userAppRecipes?.add(appRecipe)
                addRecipeToLocalCurrUserObject(newDocument)
                updateUserInUsersCollection(null)
                postSingleRecipe(appRecipe)    // the worker observe to this post
//                Log.d(TAG_APP_DB, "in addRecipeToRecipesCollection userRecipes.size = ${userRecipes!!.size}")
            }
            .addOnFailureListener {
                Log.d(TAG_APP_DB, "in failure")
            }
    }

    private fun postSingleRecipe(appRecipe: AppRecipe) {
//        LiveDataHolder.getRecipeMutableLiveData().postValue(recipe)
        LiveDataHolder.getRecipeMutableLiveData().value = ObserveWrapper(appRecipe)
    }

    private fun postUser(dbUser: DbUser?) {
//        LiveDataHolder.getUserMutableLiveData().postValue(user)
        LiveDataHolder.getUserMutableLiveData().value = ObserveWrapper(dbUser)
    }

//    private fun postRecipes(recipesList: ArrayList<DbRecipe>) {
////        LiveDataHolder.getRecipeListMutableLiveData().postValue(recipesList)
//        LiveDataHolder.getRecipeListMutableLiveData().value = ObserveWrapper(recipesList)
//    }

    private fun postAppRecipes(recipesList: ArrayList<AppRecipe>) {
        Log.d(TAG_APP_DB, "in postAppRecipes recipesAppList.size = ${recipesList.size}")
        Log.d(TAG_APP_DB, "in postAppRecipes recipesDbList.size = ${userDbRecipes?.size}")
        LiveDataHolder.getRecipeListMutableLiveData().value = ObserveWrapper(recipesList)
    }

    private fun postUsersList(usersList: ArrayList<DbUser>) {
        Log.d(TAG_APP_DB, "usersList.size = ${usersList.size}")
//        LiveDataHolder.getUsersListMutableLiveData().postValue(usersList)
        LiveDataHolder.getUsersListMutableLiveData().value = ObserveWrapper(usersList)
    }

    private fun postNotificationList(notificationList: ArrayList<AppNotification>) {
        Log.d(TAG_APP_DB, "usersList.size = ${notificationList.size}")
//        LiveDataHolder.getNotificationsMutableLiveData().postValue(notificationList)
        LiveDataHolder.getNotificationsMutableLiveData().value = ObserveWrapper(notificationList)
    }

    private fun postCommentsList(commentsList: ArrayList<Comment>) {
        Log.d(TAG_APP_DB, "usersList.size = ${commentsList.size}")
//        LiveDataHolder.getNotificationsMutableLiveData().postValue(notificationList)
        LiveDataHolder.getCommentsMutableLiveData().value = ObserveWrapper(commentsList)
    }

    // TODO add on success listener and set observer
    fun signOut() {
        initCurrUser()
        auth.signOut()
    }

    fun deleteRecipe(recipe: AppRecipe){
        // delete from local array
        if (userAppRecipes?.contains(recipe)!!) {
            userAppRecipes?.remove(recipe)
        } else {
            Log.d(TAG_APP_DB, "in deleteRecipe, local recipe list not contains")
        }
        // delete from storage and data base
        val imageUrl = recipe.imageUrl
        deleteImageFromStorage(imageUrl, recipe)
    }

    fun deleteComment(comment: Comment, appRecipe: AppRecipe){
        val commentRef = firestore
            .collection(Chefi.getCon().getString(R.string.commentsCollection))
            .document(comment.uid!!)
        if (appRecipe.comments?.contains(commentRef)!!) {
            appRecipe.comments?.remove(commentRef)
        } else {
            Log.w(TAG_APP_DB, "in deleteRecipe appRecipe.comments does not contain the comment")
        }
        firestore
            .collection(Chefi.getCon().getString(R.string.recipesCollection))
            .document(appRecipe.uid!!)
            .get()
            .addOnSuccessListener { documentSnapShot ->
                val dbRecipe = documentSnapShot.toObject<DbRecipe>()
                if (dbRecipe != null){
                    if (dbRecipe.comments?.contains(commentRef)!!) {
                        dbRecipe.comments?.remove(commentRef)
                        updateRecipeInRecipesCollection(dbRecipe)
                        commentRef
                            .delete()
                            .addOnSuccessListener {
                                Log.d(TAG_APP_DB, "comment deleted")
                            }
                            .addOnFailureListener { exception ->
                                Log.d(TAG_APP_DB, "exception ${exception.message}")
                            }
                    } else {
                        Log.w(TAG_APP_DB, "in deleteRecipe dbRecipe.comments does not contain the comment")
                    }
                }
            }
    }

    fun deleteImageFromStorage(imageUrl: String?, recipe: AppRecipe?){
        FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl!!).delete()
            .addOnSuccessListener {
                if (recipe != null) {
                    deleteRecipeFromCollection(recipe)
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG_APP_DB, "can't delete recipe $imageUrl, ${exception.message}")
            }
    }

    private fun deleteRecipeFromUserDocument(recipe: AppRecipe) {
        val recipeRef = firestore
            .collection(Chefi.getCon().getString(R.string.recipesCollection))
            .document(recipe.uid!!)
        if (currDbUser?.recipes?.contains(recipeRef)!!) {
            currDbUser?.recipes?.remove(recipeRef)
        } else {
            Log.d(
                TAG_APP_DB,
                "in deleteRecipeFromUserDocument currUser?.recipes? not contains recipe"
            )
        }
        updateUserInUsersCollection(null)
    }

    private fun deleteRecipeFromCollection(recipe: AppRecipe) {
        firestore.collection(Chefi.getCon().getString(R.string.recipesCollection))
            .document(recipe.uid!!)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG_APP_DB, "recipe deleted")
                deleteRecipeFromUserDocument(recipe)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG_APP_DB, "exception ${exception.message}")
            }
    }

    fun uploadImageToStorage(uri: Uri, fileExtension: String?) {
        val imageStorageId = System.currentTimeMillis().toString() + "." + fileExtension
        val fileRef = storageRef.child(imageStorageId)

        // upload the file to firebase storage
        fileRef.putFile(uri)
            .addOnSuccessListener { uploadTask ->
                val downloadUri = uploadTask.storage.downloadUrl
                downloadUri.addOnSuccessListener {
                    if (downloadUri.isSuccessful) {
                        val url = downloadUri.result.toString()
                        Log.d(TAG_APP_DB, "url upload image - $url")
                        Log.d("change_url", "in appDb in uploadImageToStorage image url = $url")
                        LiveDataHolder
                            .getStringMutableLiveData()
                            .value = ObserveWrapper(url)   // = .postValue(url)
                    } else {
                        Log.d(TAG_APP_DB, "downloadUri.isSuccessful = false")
                    }
                }
            }
            .addOnFailureListener { exeption ->
                Log.d("AppDb", "fail upload image - ${exeption.message}")
                // TODO
            }
            .addOnProgressListener { taskSnapshot ->
//                val progress = (100 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
//                progressBar.progress = progress.toInt()
            }
        Log.d(TAG_APP_DB, "image url = ${fileRef.downloadUrl}")
    }

    fun loadRecipes(user: DbUser?){

        if (user == null) {
            val recipesRefList = currDbUser?.recipes
            loadRecipesFromReferenceList(recipesRefList, "recipes", null)
        } else {
            Log.d(TAG_APP_DB, "in loadRecipes user?.name = ${user.name}")
            Log.d(TAG_APP_DB, "in loadRecipes user?.recipes = ${user.recipes?.size}")
            loadRecipesFromReferenceList(user.recipes, "recipes", user)
//            firestore.collection(Chefi.getCon().getString(R.string.usersCollection))
//                .document(user.uid!!)
//                .get()
//                .addOnSuccessListener { documentSnapShot ->
//                    if (documentSnapShot != null) {
//                        val otherUser = documentSnapShot.toObject<DbUser>()
//                        Log.d(TAG_APP_DB, "in loadRecipes otherUser?.name = ${otherUser?.name}")
//                        Log.d(TAG_APP_DB, "in loadRecipes otherUser?.recipes = ${otherUser?.recipes?.size}")
//                        loadRecipesFromReferenceList(otherUser?.recipes, "recipes", user)
//                    }
//                }
        }

//        val tasks = ArrayList<Task<DocumentSnapshot>>()
//        val userRecipesList = ArrayList<Recipe>()
//
//        if (recipesRefList != null) {
//            for (recipeRef in recipesRefList) {
//                val docTask = recipeRef.get()
//                tasks.add(docTask)
//            }
//            Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
//                .addOnSuccessListener { value ->
//                    for (recipeDoc in value) {
//                        val recipe = recipeDoc.toObject<Recipe>()
//                        if (recipe != null) {
//                            userRecipesList.add(recipe)
//                        }
//                    }
//                    if (user == null) {
//                        userRecipes = ArrayList()
//                        userRecipes = userRecipesList
//                    }
//                    postRecipes(userRecipesList)   // TODO - check if needed
//                }
//        }
    }

    private fun loadOwnersToRecipe(userDbRecipesList : ArrayList<DbRecipe>,
                                   type: String?,
                                   user: DbUser?) {
        val userAppRecipesList = ArrayList<AppRecipe>()

        if (user == null) {
            for (recipe in userDbRecipesList) {
                val appRecipe = AppRecipe(recipe.uid,
                    recipe.description,
                    recipe.likes,
                    recipe.imageUrl,
                    recipe.comments,
                    recipe.directions,
                    recipe.ingredients,
                    recipe.status,
                    null,
                    recipe.timestamp)
                userAppRecipesList.add(appRecipe)
            }
            when (type!!) {
                "recipes" -> {
                    userAppRecipes = ArrayList()
                    userAppRecipes = userAppRecipesList
                }
                "favorites" -> {
                    userAppFavorites = ArrayList()
                    userAppFavorites = userAppRecipesList
                }
            }
        } else {
            for (recipe in userDbRecipesList) {
                val appRecipe = AppRecipe(recipe.uid,
                    recipe.description,
                    recipe.likes,
                    recipe.imageUrl,
                    recipe.comments,
                    recipe.directions,
                    recipe.ingredients,
                    recipe.status,
                    user,
                    recipe.timestamp)
                userAppRecipesList.add(appRecipe)
            }
        }
        postAppRecipes(userAppRecipesList)
    }

    private fun loadRecipesFromReferenceList(recipesList: ArrayList<DocumentReference>?,
                                             type: String?,
                                             isCurrUser: DbUser?) {

        val tasks = ArrayList<Task<DocumentSnapshot>>()
        val userRecipesList = ArrayList<DbRecipe>()
        if (recipesList != null) {
            for (recipeRef in recipesList) {
                val docTask = recipeRef.get()
                tasks.add(docTask)
            }
            Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                .addOnSuccessListener { value ->
                    for (recipeDoc in value) {
                        val recipe = recipeDoc.toObject<DbRecipe>()
                        if (recipe != null) {
                            userRecipesList.add(recipe)
                        }
                    }
                    Log.d(TAG_APP_DB, "in loadRecipesFromReferenceList userRecipesList = ${userRecipesList.size}")
                    loadOwnersToRecipe(userRecipesList,
                    type,
                    isCurrUser)
                }
        }
//        if (isCurrUser) {
//            when (type!!) {
//                "recipes" -> {
//                    userDbRecipes = ArrayList()
//                    userDbRecipes = userRecipesList
//                }
//                "favorites" -> {
//                    userFavorites = ArrayList()
//                    userFavorites = userRecipesList
//                }
//            }
//        }
//        postRecipes(userRecipesList)
    }

    fun loadFavorites(){
        val recipesFavoritesList = currDbUser?.favorites
        loadRecipesFromReferenceList(recipesFavoritesList, "favorites", null)

//        val tasks = ArrayList<Task<DocumentSnapshot>>()
//        if (recipesFavoritesList != null) {
//            userFavorites = ArrayList()
//            for (recipeRef in recipesFavoritesList) {
//                val docTask = recipeRef.get()
//                tasks.add(docTask)
//            }
//            Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
//                .addOnSuccessListener { value ->
//                    for (recipeDoc in value) {
//                        val recipe = recipeDoc.toObject<Recipe>()
//                        if (recipe != null) {
//                            userFavorites?.add(recipe)
//                        }
//                    }
//                    postRecipes(userFavorites!!)
//                    Log.e(TAG_APP_DB, "userFavorites.size = ${userFavorites?.size} last")
//                }
//        }
    }

    private fun loadUsersListFromReferenceList(list: ArrayList<DocumentReference>?,
                                       type: String?,
                                       isCurrUser: Boolean) {

        val tasks = ArrayList<Task<DocumentSnapshot>>()
        val followingList = ArrayList<DbUser>()
        if (list != null) {
            for (userRef in list) {
                val docTask = userRef.get()
                tasks.add(docTask)
            }
            Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                .addOnSuccessListener { value ->
                    for (userDoc in value) {
                        val currUser = userDoc.toObject<DbUser>()
                        if (currUser != null) {
                            followingList.add(currUser)
                        }
                    }
                    if (isCurrUser) {
                        when (type) {
                            "following" -> {
                                dbUserFollowing = ArrayList()
                                dbUserFollowing = followingList
                            }
                            "followers" -> {
                                dbUserFollowers = ArrayList()
                                dbUserFollowers = followingList
                            }
                        }
                    }
                    postUsersList(followingList)   // TODO - check if needed
                }
        }
    }

    fun loadFollowing(dbUser: DbUser?){
        if (dbUser == null) {
            val userFollowingList = currDbUser?.following
            loadUsersListFromReferenceList(userFollowingList, "following", true)
        } else {
            firestore.collection(Chefi.getCon().getString(R.string.usersCollection))
                .document(dbUser.uid!!)
                .get()
                .addOnSuccessListener { documentSnapShot ->
                    if (documentSnapShot != null) {
                        val otherUser = documentSnapShot.toObject<DbUser>()
                        loadUsersListFromReferenceList(otherUser?.following, "following", false)
                    }
                }
        }
    }

    fun loadFollowers(dbUser: DbUser?){
        if (dbUser == null) {
            val userFollowingList = currDbUser?.followers
            loadUsersListFromReferenceList(userFollowingList, "followers", true)
        } else {
            firestore.collection(Chefi.getCon().getString(R.string.usersCollection))
                .document(dbUser.uid!!)
                .get()
                .addOnSuccessListener { documentSnapShot ->
                    if (documentSnapShot != null) {
                        val otherUser = documentSnapShot.toObject<DbUser>()
                        loadUsersListFromReferenceList(otherUser?.followers, "followers", false)
                    }
                }
        }
    }

    fun loadNotifications(){
        // TODO - add live query on the notification collection
        val userNotificationsList = currDbUser?.notifications
        val tasks = ArrayList<Task<DocumentSnapshot>>()

        if (userNotificationsList != null) {
            userNotification = ArrayList()
            for (notificationRef in userNotificationsList) {
                val docTask = notificationRef.get()
                tasks.add(docTask)
            }
            Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                .addOnSuccessListener { value ->
                    for (notificationDoc in value) {
                        val notification = notificationDoc.toObject<DbNotificationItem>()
                        if (notification != null) {
                            userNotification?.add(notification)
                        }
                    }
                    loadOwnersToNotifications()

                    Log.e(TAG_APP_DB, "userNotification.size = ${userNotification?.size} last")
                }
        }
    }

    fun addNotification(userDestId: String, content: String, type : NotificationType) {
        val notificationRef = firestore
            .collection(Chefi.getCon().getString(R.string.notificationsCollection))
            .document()
        val dbNotificationItem = DbNotificationItem(notificationRef.id, currDbUser?.uid, content, type)
        notificationRef
            .set(dbNotificationItem)
            .addOnSuccessListener {
                Log.d(TAG_APP_DB, "notification added")
                // add the notification to the user dest notification list
                firestore
                    .collection(Chefi.getCon().getString(R.string.usersCollection))
                    .document(userDestId)
                    .get()
                    .addOnSuccessListener { documentSnapShot ->
                        val user = documentSnapShot.toObject<DbUser>()
                        user?.notifications?.add(notificationRef)
                    }
            }
    }

    private fun loadOwnersToNotifications() {
        if (userAppNotification == null) {
            userAppNotification = ArrayList()
        }
        if (userNotification != null) {
            val tasks = ArrayList<Task<DocumentSnapshot>>()
            for (notification in userNotification!!) {
                val notTask = firestore
                    .collection(Chefi.getCon().getString(R.string.usersCollection))
                    .document(notification.userId!!)
                    .get()
                tasks.add(notTask)
            }
            Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                .addOnSuccessListener { value ->
                    if (value != null) {
                        for (userDoc in value) {
                            val user = userDoc.toObject<DbUser>()
                            val notification = userNotification!!.find{ it.userId == user?.uid }
                            if (notification != null) {
                                val appNotificationItem = AppNotification(user,
                                    notification.notificationContent,
                                    notification.notificationType,
                                    notification.timestamp)
                                userAppNotification?.add(appNotificationItem)
                            } else {
                                Log.w(TAG_APP_DB, "in loadOwnersToNotifications notification = null")
                            }
                        }
                    }
                    postNotificationList(userAppNotification!!)
                }

        } else {
            Log.w(TAG_APP_DB, "in loadOwnersToNotifications userNotification = null")
        }
    }

    fun addComment(content: String, recipeId: String) {
        firestore.collection(Chefi.getCon().getString(R.string.usersCollection))
            .document(recipeId)
            .get()
            .addOnSuccessListener { documentSnapShot ->
                if (documentSnapShot != null) {
                    val dbRecipe = documentSnapShot.toObject<DbRecipe>()
                    if (dbRecipe != null) {
                        val newCommentRef = firestore
                            .collection(Chefi.getCon().getString(R.string.commentsCollection))
                            .document()

                        val comment = Comment(currDbUser?.userName,
                            currDbUser?.name,
                            content,
                            newCommentRef.id)

                        newCommentRef
                            .set(comment)
                            .addOnSuccessListener {
                                Log.d(TAG_APP_DB, "in addComment, comment added")
                                dbRecipe.comments?.add(newCommentRef)
                            }
                    }
                } else {
                    Log.d(TAG_APP_DB, "in addComment, can't load recipe")
                }
            }
    }

    fun loadRecipesComments(appRecipe: AppRecipe) {
        val recipeCommentsList = appRecipe.comments
        val tasks = ArrayList<Task<DocumentSnapshot>>()
        if (recipeCommentsList != null) {
            val commentsList = ArrayList<Comment>()
            for (commentRef in recipeCommentsList) {
                val docTask = commentRef.get()
                tasks.add(docTask)
            }
            Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                .addOnSuccessListener { value ->
                    for (commentDoc in value) {
                        val comment = commentDoc.toObject<Comment>()
                        if (comment != null) {
                            commentsList.add(comment)
                        }
                    }
                    postCommentsList(commentsList)
                }
        }
    }

    fun updateUserFields(fieldName: String, content: String) {
        when (fieldName) {
            "aboutMe" -> currDbUser?.aboutMe = content
            "name" -> {
                currDbUser?.name = content
                currDbUser?.name_lowerCase = content.toLowerCase(Locale.ROOT)
            }
            "imageUrl" -> currDbUser?.imageUrl = content  // TODO - if changed maybe delete the old one ?
        }
        updateUserInUsersCollection(null)
    }

    fun updateRecipeFields(appRecipe: AppRecipe, fieldName: String, content: String?) {
        val recipeFromList = userAppRecipes?.find { it.uid == appRecipe.uid }
        recipeFromList?.likes = recipeFromList?.likes?.plus(1)
        // TODO - updates options - like, comment per recipe
        val recipeId = appRecipe.uid
        firestore.collection(Chefi.getCon().getString(R.string.recipesCollection))
            .document(recipeId!!)
            .get()
            .addOnSuccessListener { documentSnapShot ->
                val recipe = documentSnapShot.toObject<DbRecipe>()
                val dbRecipeFromList = userDbRecipes?.find { it.uid == recipe?.uid }
                dbRecipeFromList?.likes = dbRecipeFromList?.likes?.plus(1)
                when (fieldName) {
                    "likes" -> recipe?.likes = appRecipe.likes
                }
                if (recipe != null) {
                    updateRecipeInRecipesCollection(recipe)
                }
            }
    }

    fun addUserToFollowers(otherDbUser: DbUser) {
        val currUserId = currDbUser?.uid
        val otherUserId = otherDbUser.uid

        if (currUserId != null && otherUserId != null) {
            val currUserRef = firestore
                .collection(Chefi.getCon().getString(R.string.usersCollection))
                .document(currUserId)
            if (otherDbUser.followers == null) {
                otherDbUser.followers = ArrayList()
            }
            if (!otherDbUser.followers!!.contains(currUserRef)) {
                otherDbUser.followers!!.add(currUserRef)
            }
            updateUserInUsersCollection(otherDbUser)


//            firestore.collection(Chefi.getCon()
//                    .getString(R.string.usersCollection))
//                .document(currUserId)
//                .get()
//                .addOnSuccessListener { documentSnapShot ->
//                    if (documentSnapShot != null) {
//                        val currUserRef = documentSnapShot.reference
//                        if (otherDbUser.followers == null) {
//                            otherDbUser.followers = ArrayList()
//                        }
//                        if (!otherDbUser.followers!!.contains(currUserRef)) {
//                            otherDbUser.followers!!.add(currUserRef)
//                        }
//                    }
//                    updateUserInUsersCollection(otherDbUser)
//                }
        }
    }

    fun follow(dbUserToFollow: DbUser) {
        // TODO: check duplicate, write to DB
        Log.e("appDb", dbUserToFollow.name.toString())
        // add user to the firestore
        val userToFollowRef = firestore.collection(Chefi.getCon().getString(R.string.usersCollection))
            .document(dbUserToFollow.uid!!)
        if (currDbUser?.following != null) {
            if (!currDbUser?.following?.contains(userToFollowRef)!!){
                currDbUser?.following?.add(userToFollowRef)   // TODO check
            }
        } else {
            currDbUser?.following = ArrayList()
            currDbUser?.following?.add(userToFollowRef)
        }
        updateUserInUsersCollection(null)
        addUserToFollowers(dbUserToFollow)

        if (dbUserFollowing == null) {
            dbUserFollowing = ArrayList()
        }
        if (!(dbUserFollowing?.contains(dbUserToFollow))!!) {
            dbUserFollowing!!.add(dbUserToFollow)
        }

//        firestore.collection(Chefi.getCon().getString(R.string.usersCollection))
//            .document(dbUserToFollow.uid!!)
//            .get()
//            .addOnSuccessListener { documentSnapshot ->
//                if (documentSnapshot != null) {
//                    if (currDbUser?.following != null) {
//                        if (!currDbUser?.following?.contains(documentSnapshot.reference)!!){
//                            currDbUser?.following?.add(documentSnapshot.reference)   // TODO check
//                        }
//                    } else {
//                        currDbUser?.following = ArrayList()
//                        currDbUser?.following?.add(documentSnapshot.reference)
//                    }
//                    updateUserInUsersCollection(null)
//                    addUserToFollowers(dbUserToFollow)
//                } else {
//                    Log.d(TAG_APP_DB, "in follow value data: null")
//                }
//            }
//
//        if (dbUserFollowing == null) {
//            dbUserFollowing = ArrayList()
//        }
//        if (!(dbUserFollowing?.contains(dbUserToFollow))!!) {
//            dbUserFollowing!!.add(dbUserToFollow)
//        }
    }

    fun getUser(userId: String?) {
        if (userId != null) {
            firestore.collection(Chefi.getCon().getString(R.string.usersCollection))
                .document(userId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot != null) {
                        val user = documentSnapshot.toObject<DbUser>()
                        postUser(user)
                    }
                }
        }
    }

    fun unFollow(dbUserToUnFollow: DbUser) {
        val currUserId = currDbUser?.uid
        val otherUserId = dbUserToUnFollow.uid

        if (currUserId != null && otherUserId != null) {
            val userRef = firestore
                .collection(Chefi.getCon().getString(R.string.usersCollection))
                .document(otherUserId)
            if (currDbUser?.following == null) {
                Log.d(TAG_APP_DB, "in unFollow currUser.following == null")
            }
            if (currDbUser?.following!!.contains(userRef)) {
                currDbUser?.following!!.remove(userRef)
            } else {
                Log.d(TAG_APP_DB, "in unFollow currUser.following not contains")
            }

            val currUserRef = firestore
                .collection(Chefi.getCon().getString(R.string.usersCollection))
                .document(currUserId)
            if (dbUserToUnFollow.followers == null) {
                Log.d(TAG_APP_DB, "in unFollow userToUnFollow.followers == null")
            }
            if (dbUserToUnFollow.followers!!.contains(currUserRef)) {
                dbUserToUnFollow.followers!!.remove(currUserRef)
            } else {
                Log.d(
                    TAG_APP_DB,
                    "in unFollow userToUnFollow.followers not contains"
                )
            }
            updateUserInUsersCollection(dbUserToUnFollow)
            updateUserInUsersCollection(currDbUser)

            if (dbUserFollowing == null) {
                return
            }
            if (dbUserFollowing!!.contains(dbUserToUnFollow)) {
                dbUserFollowing!!.remove(dbUserToUnFollow)
            } else {
                Log.d(TAG_APP_DB, "in unFollow local followers list not contains")
            }

//            firestore.collection(
//                Chefi.getCon()
//                    .getString(R.string.usersCollection)
//            )
//                .document(otherUserId)
//                .get()
//                .addOnSuccessListener { documentSnapShot ->
//                    if (documentSnapShot != null) {
//                        val userRef = documentSnapShot.reference
//                        if (currDbUser?.following == null) {
//                            Log.d(TAG_APP_DB, "in unFollow currUser.following == null")
//                        }
//                        if (currDbUser?.following!!.contains(userRef)) {
//                            currDbUser?.following!!.remove(userRef)
//                        } else {
//                            Log.d(TAG_APP_DB, "in unFollow currUser.following not contains")
//                        }
//                    }
//                    // remove currUser from other user followers
//                    firestore.collection(
//                        Chefi.getCon()
//                            .getString(R.string.usersCollection)
//                    )
//                        .document(currUserId)
//                        .get()
//                        .addOnSuccessListener {
//                            val userRef = documentSnapShot.reference
//                            if (dbUserToUnFollow.followers == null) {
//                                Log.d(TAG_APP_DB, "in unFollow userToUnFollow.followers == null")
//                            }
//                            if (dbUserToUnFollow.followers!!.contains(userRef)) {
//                                dbUserToUnFollow.followers!!.remove(userRef)
//                            } else {
//                                Log.d(
//                                    TAG_APP_DB,
//                                    "in unFollow userToUnFollow.followers not contains"
//                                )
//                            }
//                        }
//                    updateUserInUsersCollection(dbUserToUnFollow)
//                    updateUserInUsersCollection(currDbUser)
//                }
//        }
//        if (dbUserFollowing == null) {
//            return
//        }
//        if (dbUserFollowing!!.contains(dbUserToUnFollow)) {
//            dbUserFollowing!!.remove(dbUserToUnFollow)
//        } else {
//            Log.d(TAG_APP_DB, "in unFollow local followers list not contains")
//        }
        }
    }

    fun addRecipeToFavorites(appRecipe: AppRecipe) {
        val recipeId = appRecipe.uid

        if (recipeId != null) {
            val recipeRef =
                firestore.collection(Chefi.getCon().getString(R.string.recipesCollection))
                    .document(recipeId)
            if (currDbUser?.favorites == null) {
                currDbUser?.favorites = ArrayList()
            }
            currDbUser?.favorites!!.add(recipeRef)
            updateUserInUsersCollection(null)
            if (userAppFavorites == null) {
                userAppFavorites = ArrayList()
            }
            userAppFavorites!!.add(appRecipe)

//            firestore.collection(Chefi.getCon()
//                    .getString(R.string.recipesCollection)).document(recipeId)
//                .get()
//                .addOnSuccessListener { documentSnapShot ->
//                    if (documentSnapShot != null) {
//                        val recipeRef = documentSnapShot.reference
//                        if (currDbUser?.favorites == null) {
//                            currDbUser?.favorites = ArrayList()
//                        }
//                        currDbUser?.favorites!!.add(recipeRef)
//                    }
//                    updateUserInUsersCollection(null)
//                }
//        }
//        if (userFavorites == null) {
//            userFavorites = ArrayList()
//        }
//        userFavorites!!.add(dbRecipe)
        }
    }

    fun removeRecipeFromFavorites(appRecipe: AppRecipe) {
        val recipeId = appRecipe.uid

        if (recipeId != null) {
            val recipeRef = firestore.collection(Chefi.getCon().getString(R.string.recipesCollection)).document(recipeId)
            if (currDbUser?.favorites == null) {
                Log.d(TAG_APP_DB, "in addRecipeToFavorites currUser?.favorites == null")
            }
            if (currDbUser?.favorites!!.contains(recipeRef)) {
                currDbUser?.favorites!!.remove(recipeRef)
            } else {
                Log.d(
                    TAG_APP_DB,
                    "in addRecipeToFavorites currUser?.favorites not contains"
                )
            }
            updateUserInUsersCollection(null)
            if (userAppFavorites == null) {
                Log.d(TAG_APP_DB, "in addRecipeToFavorites local favorites list = null")
            }
            if (userAppFavorites?.contains(appRecipe)!!) {
                userAppFavorites!!.remove(appRecipe)
            } else {
                Log.d(TAG_APP_DB, "in addRecipeToFavorites local favorites list not contains")
            }
//            firestore.collection(
//                Chefi.getCon()
//                    .getString(R.string.recipesCollection)
//            )
//                .document(recipeId)
//                .get()
//                .addOnSuccessListener { documentSnapShot ->
//                    if (documentSnapShot != null) {
//                        val recipeRef = documentSnapShot.reference
//                        if (currDbUser?.favorites == null) {
//                            Log.d(TAG_APP_DB, "in addRecipeToFavorites currUser?.favorites == null")
//                        }
//                        if (currDbUser?.favorites!!.contains(recipeRef)) {
//                            currDbUser?.favorites!!.remove(recipeRef)
//                        } else {
//                            Log.d(
//                                TAG_APP_DB,
//                                "in addRecipeToFavorites currUser?.favorites not contains"
//                            )
//                        }
//                    }
//                    updateUserInUsersCollection(null)
//                }
        }
    }

    fun fireBaseSearchUsers(searchText: String) {
        val usersCollectionPath = Chefi.getCon().getString(R.string.usersCollection)
        val usersRef = firestore.collection(usersCollectionPath)
        val query = usersRef
            .whereGreaterThanOrEqualTo(
                "name_lowerCase",
                searchText.toLowerCase(Locale.ROOT)
            )
        query
            .get()
            .addOnSuccessListener { documents->
                if (documents != null) {
                    val ansUsers = ArrayList<DbUser>()
                    for (document in documents.documents) {
                        val user = document.toObject<DbUser>()
                        if (user != null) {
                            ansUsers.add(user)
                            Log.d(TAG_APP_DB, "user's name = ${user.name}")
                        }
                    }
                    postUsersList(ansUsers)
                }
            }
    }

    // after calling this method, there will be a live query that firestore will trigger
    // every time the collection "pets" is changed
    // (e.g. when a document in this collection gets created, deleted, or getting its data changed)
    private fun createNotificationsLiveQuery(){

        val referenceToCollection = firestore.collection(Chefi.getCon().getString(R.string.notificationsCollection))
        // we could also add "constraints" to this reference
        // for example, if we wanted a live query for {all the items in collection "pets" that
        // have their string field "animalType" equals "dog"}
        // we could have written this:
        // {val referenceToCollection = firestore.collection("pets").whereEqualTo("animalType", "dog")}
        // but we want to get all the documents in this collection without any constraint


        // the code in ".addSnapshotListener {}" will execute in the future, first time when firestore
        // will finish downloading the collection to the phone,
        // and then each time when documents in the collection get changed
        val liveQuery = referenceToCollection.addSnapshotListener { value, exception ->
            if (exception != null) {
                // problems...
                Log.d(TAG_APP_DB, "exception in snapshot :(" + exception.message)
                return@addSnapshotListener
            }

            if (value == null) {
                // no data...
                Log.d(TAG_APP_DB, "value is null :(")
                return@addSnapshotListener
            }

            Log.e(TAG_APP_DB, "reached here? we got data! yay :)")
            // reached here? we got data! yay :)
            // let's refresh the local arrayList
            for (document: QueryDocumentSnapshot in value) {
                val dbNotification = document.toObject(DbNotificationItem::class.java) // convert to item
                if (!userNotification?.contains(dbNotification)!!) {
                    userNotification?.add(dbNotification)
                }
            }
        }
        referenceToCollection.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                loadOwnersToNotifications()
            }
        }

        // NOTICE: the live-query (also called "snapshot") stored in the variable "liveQuery",
        // will continue to listen until you will call "liveQuery.remove()"
        // you can just ignore the variable and the live-query will continue listening forever
        // (or at least until your application process will be killed by the OS)
    }
}

// TODO - add on single event listener
