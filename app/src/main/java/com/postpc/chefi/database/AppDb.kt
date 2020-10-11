package com.postpc.chefi.database

/*
https://github.com/firebase/snippets-android/blob/c2d8cfd95d996bd7c0c3e0bdf35a91430043f73d/firestore/app/src/main/java/com/google/example/firestore/kotlin/DocSnippets.kt#L462-L473
 */


import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import com.postpc.chefi.Chefi
import com.postpc.chefi.LiveDataHolder
import com.postpc.chefi.ObserveWrapper
import com.postpc.chefi.R
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("Registered")
class AppDb {

    // Declare an instance of FirebaseAuth
    private val auth: FirebaseAuth = Firebase.auth

    // Private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Chefi.getCon().getString(R.string.imageUpload)
    private val storageRef = FirebaseStorage.getInstance().getReference("uploads")

    // Users fields

    private var currDbUser: DbUser? = null
    private var dbUserFollowing: ArrayList<DbUser>? = null
    private var dbUserFollowers: ArrayList<DbUser>? = null
    private var unseenNotification: Int = 0
    private var lastVisible: DocumentSnapshot? = null


    // App Objects

    private var userAppRecipes: ArrayList<AppRecipe>? = null
    private var userAppFavorites: ArrayList<AppRecipe>? = null
    private var userAppNotification: ArrayList<AppNotification>? = null

    companion object {
        // TAGS
        private const val TAG_APP_DB: String = "appDb"
        private const val TAG_UPDATE_FEED: String = "updateFeed"
    }

    init {
        initCurrentUserFields()
//        createNotificationsLiveQuery()
    }

    // set and get

    fun getUnseenNotification() : Int {
        return unseenNotification
    }

    fun initLastSeenNotification() {
        currDbUser?.lastSeenNotification = 0
        currDbUser?.myReference
            ?.update("lastSeenNotification", 0)
    }

    fun addToRecipePermission(appRecipe: AppRecipe, userRef: DocumentReference) {
        if (appRecipe.status != null && appRecipe.status == true) {
            if (appRecipe.allowedUsers == null) {
                appRecipe.allowedUsers = ArrayList()
            }

            if (!appRecipe.allowedUsers!!.contains(userRef)) {
                appRecipe.allowedUsers!!.add(userRef)
                firestore
                    .collection(Chefi.getCon().getString(R.string.recipesCollection))
                    .document(appRecipe.uid!!)
                    .get()
                    .addOnSuccessListener {
                        val dbRecipe = it.toObject<DbRecipe>()
                        if (dbRecipe?.allowedUsers == null) {
                            dbRecipe?.allowedUsers = ArrayList()
                        }
                        if (!dbRecipe?.allowedUsers?.contains(userRef)!!) {
                            dbRecipe.allowedUsers!!.add(userRef)
                        }
                        updateRecipeInRecipesCollection(dbRecipe)
                    }
            } else {
                Log.w(
                    TAG_APP_DB,
                    "in setUserPermission appRecipe.allowedUsers already contains the user ref"
                )
            }
        } else {
            Log.w(TAG_APP_DB, "in setUserPermission appRecipe.status != 3")
        }
    }

    fun getFirebaseAuth(): FirebaseAuth {
        return auth
    }

    fun getUserRecipes(): ArrayList<AppRecipe>? {
        return userAppRecipes
    }

    fun getUserFavorites(): ArrayList<AppRecipe>? {
        return userAppFavorites
    }

    fun getUserFollowing(): ArrayList<DbUser>? {
        return dbUserFollowing
    }

    fun getUserFollowers(): ArrayList<DbUser>? {
        return dbUserFollowers
    }

    fun getUserNotification() : ArrayList<AppNotification>? {
        printNotificationToLog()
        return userAppNotification
    }

    private fun printNotificationToLog() {
        if (userAppNotification != null) {
            for (not in userAppNotification!!) {
                Log.d("notificationPrint", "notification uid = ${not.uid}")
            }
        }
    }

    fun getCurrUser(): DbUser? {
        return currDbUser
    }

    //

    private fun initCurrUser() {
        currDbUser = null
        dbUserFollowers = null
        dbUserFollowing = null
        userAppRecipes = null
        userAppFavorites = null
        userAppNotification = null
    }

    private fun initCurrentUserFields() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            val usersCollectionPath = Chefi.getCon().getString(R.string.usersCollection)
            val userId = firebaseUser.uid
            val documentUser = firestore.collection(usersCollectionPath).document(userId)
            documentUser.get().addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject<DbUser>()
                Log.d("initCurrentUserFields", "user?.ref = ${user?.uid} in updateCurrentUser")
                if (user != null) {
                    currDbUser = user
                    dbUserFollowers = null
                    dbUserFollowing = null

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

    fun signOut() {
        initCurrUser()
        auth.signOut()
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
                        userName_lowerCase = userName.toLowerCase(Locale.ROOT),
                        lastSeenNotification = 0
                    )
                    addUserToUsersCollection(newUser)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG_APP_DB, "createUserWithEmail:failure ${task.exception?.message}")
                    postString(task.exception?.message.toString())
                }
            }
    }

    fun logIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    initCurrentUserFields()
                    Log.d(TAG_APP_DB, "logInWithEmail:success")
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG_APP_DB, "logInWithEmail:failure", task.exception)
                    postString(task.exception?.message.toString())
                }
            }
    }

    private fun updateUserInUsersCollection(dbUser: DbUser?) {
        // update the user in the db
        val currUserId = dbUser?.uid
        if (currUserId != null) {
            firestore.collection(Chefi.getCon().getString(R.string.usersCollection))
                .document(currUserId)
                .set(dbUser, SetOptions.merge())
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

    private fun addUserToUsersCollection(dbUser: DbUser?) {
        Log.d(TAG_APP_DB, "in add to collection, uid = ${dbUser?.uid}")
        if (dbUser?.uid != null) {
            Log.d(TAG_APP_DB, "in add to collection")
            val myReference = firestore
                .collection(Chefi.getCon().getString(R.string.usersCollection))
                .document(dbUser.uid!!)
            Log.d(TAG_APP_DB, "in addUserToCollection myReference = $myReference")
            dbUser.myReference = myReference

            myReference.set(dbUser)
                .addOnSuccessListener {
                    initCurrentUserFields()
                }
        }
    }

    fun addRecipeToRecipesCollection(
        recipeName: String?,
        imageUrl: String?,
        direction: ArrayList<String>?,
        ingredients: ArrayList<String>?,
        status: Boolean,
        location: String?
    ) {
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
            owner = userDocument,
            myReference = newDocument,
            allowedUsers = ArrayList(),
            location = location
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
            owner = currDbUser,
            myReference = newDocument,
            allowedUsers = ArrayList(),
            location = location
        )
        dbRecipe.allowedUsers?.add(userDocument)
        appRecipe.allowedUsers?.add(userDocument)
//        if (userDbRecipes == null) {
//            userDbRecipes = ArrayList()
//        }
        if (userAppRecipes == null) {
            userAppRecipes = ArrayList()
        }
        newDocument.set(dbRecipe)
            .addOnSuccessListener {
                Log.d(TAG_APP_DB, "in success of addRecipeToRecipesCollection")
//                userDbRecipes?.add(dbRecipe)
                userAppRecipes?.add(appRecipe)
                addRecipeToLocalCurrUserObject(newDocument)
                updateUserInUsersCollection(currDbUser)
                postSingleRecipe(appRecipe)    // the worker observe to this post
//                Log.d(TAG_APP_DB, "in addRecipeToRecipesCollection userRecipes.size = ${userRecipes!!.size}")
            }
            .addOnFailureListener {
                Log.d(TAG_APP_DB, "in failure")
            }
    }

    // post Live Data

    private fun postString(str: String) {
        LiveDataHolder.getStringMutableLiveData().value = ObserveWrapper(str)
    }

    private fun postSingleRecipe(appRecipe: AppRecipe) {
//        LiveDataHolder.getRecipeMutableLiveData().postValue(recipe)
        LiveDataHolder.getRecipeMutableLiveData().value = ObserveWrapper(appRecipe)
    }

    private fun postUser(dbUser: DbUser?) {
//        LiveDataHolder.getUserMutableLiveData().postValue(user)
        LiveDataHolder.getUserMutableLiveData().value = ObserveWrapper(dbUser)
    }

    private fun postAppRecipes(recipesList: ArrayList<AppRecipe>) {
        Log.d("coUpload", "in postAppRecipes recipesList.size = ${recipesList.size}")
        LiveDataHolder.getRecipeListMutableLiveData().value = ObserveWrapper(recipesList)
    }

    private fun postFeedRecipes(recipesList: ArrayList<AppRecipe>) {
        Log.d("coUpload", "in postAppRecipes recipesList.size = ${recipesList.size}")
        LiveDataHolder.getFeedMutableLiveData().value = ObserveWrapper(recipesList)
    }

    private fun postNotificationInt(num: Int) {
        Log.d("notificationCounter", "in postNotificationInt num = $num")
        LiveDataHolder.getNotificationIntMutableLiveData().value = ObserveWrapper(num)
    }

    private fun postUsersList(usersList: ArrayList<DbUser>) {
//        LiveDataHolder.getUsersListMutableLiveData().postValue(usersList)
        LiveDataHolder.getUsersListMutableLiveData().value = ObserveWrapper(usersList)
    }

    private fun postNotificationList(notificationList: ArrayList<AppNotification>) {
        Log.d("setNotificationObserver", "content size = ${notificationList.size}")
//        LiveDataHolder.getNotificationsMutableLiveData().postValue(notificationList)
        LiveDataHolder.getNotificationsMutableLiveData().value = ObserveWrapper(notificationList)
    }

    private fun postCommentsList(commentsList: ArrayList<Comment>) {
        LiveDataHolder.getCommentsMutableLiveData().value = ObserveWrapper(commentsList)
    }

    // delete and add

    fun deleteRecipe(recipe: AppRecipe) {
        // delete from local array
        Log.d("favoritesBug", "in deleteRecipe in appDb recipe uid = ${recipe.uid}")
        if (userAppRecipes != null) {
            if (userAppRecipes?.contains(recipe)!!) {
                userAppRecipes?.remove(recipe)

            } else {
                Log.d(TAG_APP_DB, "in deleteRecipe, local recipe list not contains")
            }
        }
        // delete from storage and data base
        val imageUrl = recipe.imageUrl
        if (currDbUser?.favorites != null) {
            if (currDbUser?.favorites?.contains(recipe.myReference)!!) {
                deleteRecipeFromFavorites(recipe)
            }
        }
        deleteImageFromStorage(imageUrl, recipe)
    }

    fun deleteComment(comment: Comment, appRecipe: AppRecipe) {
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
                if (dbRecipe != null) {
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
                        Log.w(
                            TAG_APP_DB,
                            "in deleteRecipe dbRecipe.comments does not contain the comment"
                        )
                    }
                }
            }
    }

    fun deleteImageFromStorage(imageUrl: String?, recipe: AppRecipe?) {
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
        updateUserInUsersCollection(currDbUser)
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

    fun deleteRecipeFromFavorites(appRecipe: AppRecipe) {
        if (userAppFavorites != null) {
            val recipe = userAppFavorites?.find { it.uid == appRecipe.uid }
            if (recipe != null) {
                userAppFavorites?.remove(recipe)
                val recipeRef = appRecipe.myReference
                if (currDbUser?.favorites != null) {
                    currDbUser?.favorites!!.remove(recipeRef)
                    updateUserInUsersCollection(currDbUser!!)
                } else {
                    Log.w(TAG_APP_DB, "currDbUser?.favorites = null")
                }
            }
        } else {
            Log.w(TAG_APP_DB, "userAppFavorites = null")
        }
    }

    fun deleteNotification(appNotificationItem: AppNotification) {
        Log.d("deleteNotification", "app notification uid = ${appNotificationItem.uid}")
        val notificationRef = appNotificationItem.uid?.let {
            firestore
                .collection(Chefi.getCon().getString(R.string.notificationsCollection))
                .document(it)
        }
        if (notificationRef != null) {
            if (currDbUser?.notifications?.contains(notificationRef)!!) {
                val notification = userAppNotification?.find { it.uid == appNotificationItem.uid }
                userAppNotification?.remove(notification)

                currDbUser?.notifications?.remove(notificationRef)
                updateUserInUsersCollection(currDbUser)
                notificationRef.delete()
            }
        } else {
            Log.e(TAG_APP_DB, "notification uid wrong = ${appNotificationItem.uid}")
        }
    }

    fun addNotification(userDestRef: DocumentReference,
                        recipeRef: DocumentReference?,
                        offeredRecipeRef: DocumentReference?,
                        type: NotificationType) {

        val notificationRef = firestore
            .collection(Chefi.getCon().getString(R.string.notificationsCollection))
            .document()
        val dbNotificationItem =
            DbNotificationItem(
                notificationRef.id,
                currDbUser?.myReference,
                userDestRef,
                recipeRef,
                offeredRecipeRef,
                type)

        userDestRef
            .get()
            .addOnSuccessListener { documentSnapShot ->
                val user = documentSnapShot.toObject<DbUser>()
                Log.d("addNotification", "in listener, user name = ${user?.name}, " +
                        "notification uid = ${notificationRef.id}")
                if (user?.notifications == null) {
                    user?.notifications = ArrayList()
                }
                user?.notifications?.add(notificationRef)
                Log.d("addNotification", "lastSeenNotification = ${user?.lastSeenNotification}")
                if (user?.lastSeenNotification == null) {
                    user?.lastSeenNotification = 1

                } else {
                    user.lastSeenNotification = user.lastSeenNotification!!.plus(1)
                    Log.d("addNotification", "in else lastSeenNotification = ${user.lastSeenNotification}")
                }
                Log.d("addNotification", "user?.notifications size = ${user?.notifications?.size}")
                updateUserInUsersCollection(user)

                notificationRef
                    .set(dbNotificationItem)
            }

//        notificationRef
//            .set(dbNotificationItem)
//            .addOnSuccessListener {
//                Log.d("addNotification", "notification added")
//                // add the notification to the user dest notification list
//                Log.d("addNotification", "in listener, user ref = $userDestRef")
//                userDestRef
//                    .get()
//                    .addOnSuccessListener { documentSnapShot ->
//                        val user = documentSnapShot.toObject<DbUser>()
//                        Log.d("addNotification", "in listener, user name = ${user?.name}, " +
//                                "notification uid = ${notificationRef.id}")
//                        if (user?.notifications == null) {
//                            user?.notifications = ArrayList()
//                        }
//                        user?.notifications?.add(notificationRef)
//                        Log.d("addNotification", "lastSeenNotification = ${user?.lastSeenNotification}")
//                        if (user?.lastSeenNotification == null) {
//                            user?.lastSeenNotification = 1
//                        } else {
//                            user.lastSeenNotification = user.lastSeenNotification!!.plus(1)
//                            Log.d("addNotification", "in else lastSeenNotification = ${user.lastSeenNotification}")
//                        }
//                        Log.d("addNotification", "user?.notifications size = ${user?.notifications?.size}")
//                        updateUserInUsersCollection(user)
//                        Log.d("addNotification", "user?.notifications size = ${user?.notifications?.size}")
//                    }
//            }
    }

    fun addComment(content: String, recipeId: String, context: Context, type: String) {
        Log.d("addComment", "start of addComment")
        firestore.collection(Chefi.getCon().getString(R.string.recipesCollection))
            .document(recipeId)
            .get()
            .addOnSuccessListener { documentSnapShot ->
                if (documentSnapShot != null) {
                    val dbRecipe = documentSnapShot.toObject<DbRecipe>()
                    if (dbRecipe != null) {
                        Log.d("addComment", "dbRecipe = ${dbRecipe.description}")
                        val newCommentRef = firestore
                            .collection(Chefi.getCon().getString(R.string.commentsCollection))
                            .document()

                        val comment = Comment(
                            currDbUser?.userName,
                            currDbUser?.name,
                            content,
                            newCommentRef.id
                        )

                        newCommentRef
                            .set(comment)
                            .addOnSuccessListener {
                                Log.d(TAG_APP_DB, "in addComment, comment added")
                                dbRecipe.comments?.add(newCommentRef)
                                updateRecipeInRecipesCollection(dbRecipe)
                            }
                    }
                } else {
                    Log.d(TAG_APP_DB, "in addComment, can't load recipe")
                }
            }
    }

    fun addRecipeToFavorites(appRecipe: AppRecipe) {
        Log.e("favoritesBug", "in addRecipeToFavorites ref = ${appRecipe.myReference}")
        val recipeId = appRecipe.uid
        if (userAppFavorites == null) {
            userAppFavorites = ArrayList()
        }
        if (currDbUser?.favorites == null) {
            currDbUser?.favorites = ArrayList()
        }
        if (!currDbUser?.favorites?.contains(appRecipe.myReference)!!) {
            if (recipeId != null) {
                val recipeRef =
                    firestore.collection(Chefi.getCon().getString(R.string.recipesCollection))
                        .document(recipeId)
                if (currDbUser?.favorites == null) {
                    currDbUser?.favorites = ArrayList()
                }
                currDbUser?.favorites!!.add(recipeRef)
                updateUserInUsersCollection(currDbUser)

                userAppFavorites!!.add(appRecipe)
            }

        }
    }

    fun addUserToFollowers(otherDbUser: DbUser) {
        val currUserId = currDbUser?.uid
        val otherUserId = otherDbUser.uid

        if (currUserId != null && otherUserId != null) {
            if (otherDbUser.followers == null) {
                otherDbUser.followers = ArrayList()
            }
            if (!otherDbUser.followers!!.contains(currDbUser?.myReference)) {
                otherDbUser.followers!!.add(currDbUser?.myReference!!)
            }
            updateUserInUsersCollection(otherDbUser)
        }
    }

    // load and uploads

    fun loadNotifications() {
        // TODO - add live query on the notification collection
        val userNotificationsList = currDbUser?.notifications
        val tasks = ArrayList<Task<DocumentSnapshot>>()
        val dbNotificationsList = ArrayList<DbNotificationItem>()
        val appNotificationsList = ArrayList<AppNotification>()
        if (userNotificationsList != null) {
            for (notificationRef in userNotificationsList) {
                val docTask = notificationRef.get()
                tasks.add(docTask)
            }
            Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                .addOnSuccessListener { value ->
                    for (notificationDoc in value) {
                        val notification = notificationDoc.toObject<DbNotificationItem>()
                        if (notification != null) {
                            dbNotificationsList.add(notification)
                        }
                    }
                    val mainJob = CoroutineScope(IO).launch {
                        for (dbNotification in dbNotificationsList) {
                            val job = buildNotificationFlow(dbNotification)
                            job.collect { appNot -> appNotificationsList.add(appNot) }
                            Log.d(
                                TAG_UPDATE_FEED,
                                "in loadNotification for notification flow appNotificationsList size = ${appNotificationsList.size}"
                            )
                        }
                    }
                    mainJob.invokeOnCompletion {
                        Log.d(TAG_UPDATE_FEED, "in loadNotification invokeOnCompletion appNotificationsList size = ${appNotificationsList.size}")
                        CoroutineScope(Main).launch {
                            appNotificationsList.sortByDescending { it.timestamp }
                            userAppNotification = appNotificationsList
                            postNotificationList(ArrayList(appNotificationsList))
                            createNotificationsLiveQuery()
                        }
                    }
                }
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
                        postString(url)
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

    fun loadRecipes(user: DbUser?, type: String) {

        if (user == null) {
            var recipesRefList: ArrayList<DocumentReference>? = null
            when (type) {
                "favorites" -> recipesRefList = currDbUser?.favorites
                "recipes" -> recipesRefList = currDbUser?.recipes
                else -> Log.e(TAG_APP_DB, "wrong type = $type")
            }
            loadRecipesFromReferenceList(recipesRefList, type, null)
        } else {
            Log.d(TAG_APP_DB, "in loadRecipes user?.name = ${user.name}")
            Log.d(TAG_APP_DB, "in loadRecipes user?.recipes = ${user.recipes?.size}")
            var recipesRefList: ArrayList<DocumentReference>? = null
            when (type) {
                "favorites" -> recipesRefList = user.favorites
                "recipes" -> recipesRefList = user.recipes
                else -> Log.e(TAG_APP_DB, "wrong type = $type")
            }
            loadRecipesFromReferenceList(recipesRefList, type, user)
        }
    }

    private fun loadRecipesFromReferenceList(
        recipesList: ArrayList<DocumentReference>?,
        type: String,
        dbUser: DbUser?
    ) {
        val recipeTasks = ArrayList<Task<DocumentSnapshot>>()
        if (recipesList != null) {
            for (recipeRef in recipesList) {
                val docTask = recipeRef.get()
                recipeTasks.add(docTask)
            }
            Tasks.whenAllSuccess<DocumentSnapshot>(recipeTasks)
                .addOnSuccessListener { value ->
                    val dbRecipesList = ArrayList<DbRecipe>()
                    for (recipeDoc in value) {
                        val recipe = recipeDoc.toObject<DbRecipe>()
                        if (recipe != null) {
                            dbRecipesList.add(recipe)
                        }
                    }
                    parseDbRecipesToAppRecipes(dbRecipesList, dbUser, type)
                }
        }
    }

    private fun loadUsersListFromReferenceList(
        list: ArrayList<DocumentReference>?,
        type: String?,
        isCurrUser: Boolean
    ) {

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
                    if (!isCurrUser) {
                        postUsersList(followingList)
                    }
                }
        }
    }

    fun loadFollowUnFollow(dbUser: DbUser?, type: String) {
        var userDocRefList : ArrayList<DocumentReference>? = null
        when (type) {
            "followers" -> userDocRefList = currDbUser?.followers
            "following" -> userDocRefList = currDbUser?.following
            else -> Log.e(TAG_APP_DB, "wrong type - $type")
        }
        if (dbUser == null) {
            loadUsersListFromReferenceList(userDocRefList, type, true)
        } else {
            dbUser.myReference?.get()
                ?.addOnSuccessListener { documentSnapShot ->
                    if (documentSnapShot != null) {
                        val otherUser = documentSnapShot.toObject<DbUser>()
                        val refList =
                            if (type == "followers") otherUser?.followers
                            else otherUser?.following
                        loadUsersListFromReferenceList(refList, type, false)
                    }
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
            "imageUrl" -> {
                val temp = currDbUser?.imageUrl
                if (temp != null) {
                    deleteImageFromStorage(temp, null)
                }
                currDbUser?.imageUrl = content
            }
        }
        updateUserInUsersCollection(currDbUser)
    }

    fun updateRecipeFields(appRecipe: AppRecipe, fieldName: String, content: String?) {
        // TODO - updates options - like, comment per recipe
        val recipeId = appRecipe.uid
        firestore.collection(Chefi.getCon().getString(R.string.recipesCollection))
            .document(recipeId!!)
            .get()
            .addOnSuccessListener { documentSnapShot ->
                val recipe = documentSnapShot.toObject<DbRecipe>()
                if (recipe != null) {
                    when (fieldName) {
                        "likes" -> recipe.likes = appRecipe.likes
                    }
                    updateRecipeInRecipesCollection(recipe)
                }
            }
    }

    //

    fun follow(dbUserToFollow: DbUser, context: Context) {
        // TODO: check duplicate, write to DB
        Log.e(TAG_APP_DB, dbUserToFollow.name.toString())
        // add user to the firestore
        if (currDbUser?.following != null) {
            if (!currDbUser?.following?.contains(dbUserToFollow.myReference)!!) {
                currDbUser?.following?.add(dbUserToFollow.myReference!!)   // TODO check
            }
        } else {
            currDbUser?.following = ArrayList()
            currDbUser?.following?.add(dbUserToFollow.myReference!!)
        }
        addUserToFollowers(dbUserToFollow)
        firestore.collection(Chefi.getCon().getString(R.string.usersCollection))
            .document(currDbUser?.uid!!)
            .set(currDbUser!!, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG_APP_DB, "in follow adding another worker")
//                addWorkerUpdateCurrUserFeed("follow", dbUserToFollow.uid!!, context)
            }

        if (dbUserFollowing == null) {
            dbUserFollowing = ArrayList()
        }
        if (!(dbUserFollowing?.contains(dbUserToFollow))!!) {
            dbUserFollowing!!.add(dbUserToFollow)
        }
    }

    fun unFollow(dbUserToUnFollow: DbUser, context: Context) {
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

            firestore.collection(Chefi.getCon().getString(R.string.usersCollection))
                .document(currDbUser?.uid!!)
                .set(currDbUser!!, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG_APP_DB, "in unFollow adding another worker")
//                    addWorkerUpdateCurrUserFeed("unfollow", dbUserToUnFollow.uid!!, context)
                }

            if (dbUserFollowing == null) {
                return
            }
            if (dbUserFollowing!!.contains(dbUserToUnFollow)) {
                dbUserFollowing!!.remove(dbUserToUnFollow)
            } else {
                Log.d(TAG_APP_DB, "in unFollow local followers list not contains")
            }
        }
    }

    fun fireBaseSearchUsers(searchText: String) {
        val usersCollectionPath = Chefi.getCon().getString(R.string.usersCollection)
        val usersRef = firestore.collection(usersCollectionPath)
        val query = usersRef
            .whereEqualTo(
                "userName_lowerCase",
                searchText.toLowerCase(Locale.ROOT)
            )

        query.get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    val ansUsers = ArrayList<DbUser>()
                    for (document in documents.documents) {
                        val user = document.toObject<DbUser>()
                        if (user != null) {
                            ansUsers.add(user)
                            Log.d(TAG_APP_DB, "userName = ${user.userName}")
                        }
                    }
                    postUsersList(ansUsers)
                }
            }
    }

    // TODO - delete if not needed
    fun updateFeedWithRecipe(type: String, recipeUid: String) {
        Log.e(TAG_UPDATE_FEED, "in updateFeedWithRecipe, recipeId = $recipeUid")
        if (dbUserFollowers != null) {
            val feedDeleteTasks = ArrayList<Task<QuerySnapshot>>()
            val feedAddTasks = ArrayList<Task<Void>>()
            firestore
                .collection(Chefi.getCon().getString(R.string.recipesCollection))
                .document(recipeUid)
                .get()
                .addOnSuccessListener {
                    // We have the recipe that's needs to be added ot deleted
                    val dbRecipe = it.toObject<DbRecipe>()
                    for (user in dbUserFollowers!!) {
                        when (type) {
                            "add" -> {
                                Log.e(TAG_UPDATE_FEED, "in updateFeedWithRecipe add")
                                val feedDocRef = firestore.collection(user.uid!!).document()
                                val feedRecipe = FeedPost(
                                    feedDocRef.id,
                                    dbRecipe?.myReference,
                                    dbRecipe?.timestamp,
                                    dbRecipe?.uid
                                )
                                val task = feedDocRef.set(feedRecipe)
                                feedAddTasks.add(task)
                            }
                            "delete" -> {
                                Log.e(TAG_UPDATE_FEED, "in updateFeedWithRecipe delete")
                                val feedDocTask = firestore.collection(user.uid!!)
                                    .whereEqualTo("dbRecipe", dbRecipe)
                                    .get()
                                    .addOnSuccessListener {
                                        for (feedPostSnap in it) {
                                            val feedPost = feedPostSnap.toObject<FeedPost>()
                                            firestore
                                                .collection(user.uid!!)
                                                .document(feedPost.uid!!)
                                                .delete()
                                        }
                                    }
                                feedDeleteTasks.add(feedDocTask)
                            }
                        }
                    }
                }
        }
    }

    private fun parseDbRecipesToAppRecipes(
        dbRecipesList: ArrayList<DbRecipe>,
        dbUser: DbUser?,
        type: String) {
        val otherAppRecipes = ArrayList<AppRecipe>()

        val mainJob = CoroutineScope(Default).launch {
            for (dbRecipe in dbRecipesList) {
                val job = buildSingleRecipeFlow(dbRecipe)
                job.collect { appRecipe -> otherAppRecipes.add(appRecipe) }
            }
        }
        mainJob.invokeOnCompletion {
            Log.d(TAG_UPDATE_FEED, "in uploadFeed1 invokeOnCompletion appRecipesList size = ${otherAppRecipes.size}")
            if (dbUser == null) {
                when (type) {
                    "recipes" -> {
                        Log.d("coLoadTest", "appRecipesList size = ${otherAppRecipes.size}")
                        userAppRecipes = ArrayList()
                        userAppRecipes = otherAppRecipes
                    }
                    "favorites" -> {
                        userAppFavorites = ArrayList()
                        userAppFavorites = otherAppRecipes
                    }
                }
            }
            CoroutineScope(Main).launch {
                postAppRecipes(otherAppRecipes)
            }
        }
    }

    private suspend fun getSingleUser(dbUserRef: DocumentReference?) =
        dbUserRef
            ?.get()
            ?.await()

    private suspend fun getSingleRecipe(dbUserRef: DocumentReference?) =
        dbUserRef
            ?.get()
            ?.await()

    private fun buildNotificationFlow(dbNotificationItem: DbNotificationItem):
            Flow<AppNotification> = flow {

        // creator
        val notificationCreatorSnapShot = getSingleUser(dbNotificationItem.creatorRef)
        val userNotificationCreator = notificationCreatorSnapShot?.toObject<DbUser>()

        // appRecipe 1
        val recipeSnapShot = getSingleRecipe(dbNotificationItem.recipeRef)
        val dbRecipe = recipeSnapShot?.toObject<DbRecipe>()
        val recipeJob = dbRecipe?.let { buildSingleRecipeFlow(it) }
        var appRecipe = AppRecipe()
        recipeJob?.collect { value ->
            appRecipe = value
        }

        // appRecipe 2 if exist
        var offeredRecipe: AppRecipe? = null
        if (dbNotificationItem.offeredRecipeRef != null) {
            val offeredRecipeSnapShot = getSingleRecipe(dbNotificationItem.offeredRecipeRef)
            val dbOfferedRecipe = offeredRecipeSnapShot?.toObject<DbRecipe>()
            val offeredRecipeJob = dbOfferedRecipe?.let { buildSingleRecipeFlow(it) }
            offeredRecipeJob?.collect { value ->
                offeredRecipe = value
            }
        }

        val appNotification = AppNotification(
            uid = dbNotificationItem.uid,
            creator = userNotificationCreator,
            destinationRef = dbNotificationItem.destinationRef,
            recipe = appRecipe,
            offeredRecipe = offeredRecipe,
            notificationType = dbNotificationItem.notificationType,
            timestamp = dbNotificationItem.timestamp
        )
        Log.d("notificationFlow",
            "creator uid = ${userNotificationCreator?.uid}, recipe1 uid = ${appRecipe.uid}, " +
                    "recipe2 uid = ${offeredRecipe?.uid}")
        emit(appNotification)
    }

    fun filterForTradeRecipesList(): ArrayList<AppRecipe> {
        val filteredList = userAppRecipes?.filter { it.status == true }
        val forTradeList = ArrayList<AppRecipe>()
        if (filteredList != null) {
            for (recipe in filteredList) {
                forTradeList.add(recipe)
            }
        }
        return forTradeList
    }

    private fun buildSingleRecipeFlow(dbRecipe: DbRecipe):
            Flow<AppRecipe> = flow {

        val commentsList = ArrayList<Comment>()
        val recipeCommentsList = dbRecipe.comments
        val commentsTasks = ArrayList<Task<DocumentSnapshot>>()
        if (recipeCommentsList != null) {
            for (commentRef in recipeCommentsList) {
                val docTask = commentRef.get()
                commentsTasks.add(docTask)
            }
            Tasks.whenAllSuccess<DocumentSnapshot>(commentsTasks)
                .addOnSuccessListener { value ->
                    for (commentDoc in value) {
                        val comment = commentDoc.toObject<Comment>()
                        if (comment != null) {
                            commentsList.add(comment)
                        }
                    }
                }
            val recipeOwnerSnapshot = getSingleUser(dbRecipe.owner)
            val recipeOwner = recipeOwnerSnapshot?.toObject<DbUser>()

            val appRecipe = AppRecipe(
                dbRecipe.uid,
                dbRecipe.description,
                dbRecipe.likes,
                dbRecipe.imageUrl,
                commentsList,
                dbRecipe.directions,
                dbRecipe.ingredients,
                dbRecipe.status,
                recipeOwner,
                dbRecipe.timestamp,
                dbRecipe.myReference,
                dbRecipe.allowedUsers,
                dbRecipe.location
            )
            Log.d("flow", "comments size = ${commentsList.size}, owner = ${recipeOwner?.uid}")
            emit(appRecipe)
        }
    }

    private fun mainFeedFlow(dbRecipesFeed: ArrayList<DbRecipe>):
            Flow<ArrayList<AppRecipe>> = flow {
        val appRecipesList = ArrayList<AppRecipe>()
        for (dbRecipe in dbRecipesFeed) {
            val job = buildSingleRecipeFlow(dbRecipe)
            job.collect { appRecipe -> appRecipesList.add(appRecipe) }
            Log.d(TAG_UPDATE_FEED, "in uploadFeed1 for flow appRecipesList size = ${appRecipesList.size}")
        }
        emit(appRecipesList)
    }

    fun uploadFeed() {
        // TODO - add limit and lastVisited...
        val query =
            firestore
                .collection(Chefi.getCon().getString(R.string.recipesCollection))
                .orderBy("timestamp", Query.Direction.DESCENDING)
        val dbRecipesFeed = ArrayList<DbRecipe>()
        val appRecipesList = ArrayList<AppRecipe>()
        query.get()
            .addOnSuccessListener { snapShotList ->
                for (snapShot in snapShotList) {
                    val dbRecipe = snapShot.toObject<DbRecipe>()
                    if (currDbUser?.following != null
                        && currDbUser?.following?.contains(dbRecipe.owner)!!)
                    {
                        Log.d(TAG_UPDATE_FEED, "currDbUser?.following?.contains = ${dbRecipe.owner}, recipe id = ${dbRecipe.uid}")
                        dbRecipesFeed.add(dbRecipe)
                    }
                }
                Log.d(TAG_UPDATE_FEED, "in uploadFeed1 dbRecipesFeed size = ${dbRecipesFeed.size}")
                val mainJob = CoroutineScope(Default).launch {
//                    for (dbRecipe in dbRecipesFeed) {
//                        val job = buildRecipeFlow(dbRecipe)
//                        job.collect { appRecipe -> appRecipesList.add(appRecipe) }
//                        Log.d(TAG_UPDATE_FEED, "in uploadFeed1 for flow appRecipesList size = ${appRecipesList.size}")
//                    }
                    val job = mainFeedFlow(dbRecipesFeed)
                    job.collect { value ->
                        withContext(Main) {
                            Log.d(TAG_UPDATE_FEED, "in collect value size = ${value.size}")
                            postFeedRecipes(value)
                        }
                    }
                }

//                mainJob.invokeOnCompletion {
//                    Log.d(TAG_UPDATE_FEED, "it = ${mainJob.isCancelled}")
//                    Log.d(TAG_UPDATE_FEED, "in uploadFeed1 invokeOnCompletion appRecipesList size = ${appRecipesList.size}")
//                    CoroutineScope(Main).launch {
//                        postFeedRecipes(appRecipesList)
//                    }
//                }
            }
    }

    private fun updateLocalAppNotificationList(dbNotificationItemList: ArrayList<DbNotificationItem>) {
        val mainJob = CoroutineScope(IO).launch {
            for (notification in dbNotificationItemList) {
                val job = buildNotificationFlow(notification)
                if (userAppNotification == null) {
                    userAppNotification = ArrayList()
                }
                job.collect { appNot -> userAppNotification?.add(appNot) }
            }
        }
        mainJob.invokeOnCompletion {
            CoroutineScope(Main).launch {
                userAppNotification?.sortByDescending { it.timestamp }
                val userSnapShot = firestore
                    .collection(Chefi.getCon().getString(R.string.usersCollection))
                    .document(currDbUser?.uid!!)
                    .get()
                    .await()
                currDbUser = userSnapShot.toObject<DbUser>()
                postNotificationInt(currDbUser?.lastSeenNotification!!)
                if (userAppNotification != null) {
                    postNotificationList(userAppNotification!!)
                }
            }
        }
    }

    // live Query

    // after calling this method, there will be a live query that firestore will trigger
    // every time the collection "pets" is changed
    // (e.g. when a document in this collection gets created, deleted, or getting its data changed)
    private fun createNotificationsLiveQuery() {

        val referenceToCollection =
            firestore.collection(Chefi.getCon().getString(R.string.notificationsCollection))
        val userDbNotificationItem = ArrayList<DbNotificationItem>()
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

            val dbNotificationItemList = ArrayList<DbNotificationItem>()

            for (document: QueryDocumentSnapshot in value) {
                val dbNotification =
                    document.toObject<DbNotificationItem>()
                userDbNotificationItem.add(dbNotification)
                val currRef = auth.currentUser?.uid?.let {
                    firestore
                        .collection(Chefi.getCon().getString(R.string.usersCollection))
                        .document(it)
                }
                if (dbNotification.destinationRef == currRef) {
                    val dbNot = userAppNotification?.find {
                        Log.d("queryNotification", "dbNotification uid = ${dbNotification.uid}, it.uid = ${it.uid}")
                        it.uid == dbNotification.uid
                        }
                    if (dbNot == null) {
                        Log.d("queryNotification", "KASSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS")
                        Log.d("queryNotification", "dbNot uid = ${dbNot?.uid}")
                        dbNotificationItemList.add(dbNotification)
                    }
                }
            }
            updateLocalAppNotificationList(dbNotificationItemList)
        }

        referenceToCollection.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("queryNotification", "task.isSuccessful KASSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSssss")
                Log.d("queryNotification", "task.isSuccessful, userDbNotificationItem size = ${userDbNotificationItem.size}")
                val dbNotificationItemList = ArrayList<DbNotificationItem>()
                for (notification in userDbNotificationItem) {
                    val currRef = auth.currentUser?.uid?.let {
                        firestore
                            .collection(Chefi.getCon().getString(R.string.usersCollection))
                            .document(it)
                    }
//                    Log.d("queryNotification", "currDbUser?.myReference uid = $currRef")
                    if (notification.destinationRef == currRef) {
                        val dbNot = userAppNotification?.find { it.uid == notification.uid }
                        if (dbNot == null) {
                            Log.d("queryNotification", "in SUCCESS KASSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS")
                            Log.d("queryNotification", "dbNot uid = ${dbNot?.uid}")
                            dbNotificationItemList.add(notification)
                        }
                    }
                }
                updateLocalAppNotificationList(dbNotificationItemList)
            }
        }

        // NOTICE: the live-query (also called "snapshot") stored in the variable "liveQuery",
        // will continue to listen until you will call "liveQuery.remove()"
        // you can just ignore the variable and the live-query will continue listening forever
        // (or at least until your application process will be killed by the OS)
    }

    fun isFollowedByMe(ref: DocumentReference): Boolean {
        Log.e("search", "user ref = $ref")
        return if (currDbUser?.following != null)
            currDbUser?.following?.contains(ref)!! else false

    }
}
