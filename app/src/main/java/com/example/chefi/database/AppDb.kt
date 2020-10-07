package com.example.chefi.database

/*
https://github.com/firebase/snippets-android/blob/c2d8cfd95d996bd7c0c3e0bdf35a91430043f73d/firestore/app/src/main/java/com/google/example/firestore/kotlin/DocSnippets.kt#L462-L473
 */


import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.reflect.TypeToken
import android.net.Uri
import android.util.Log
import androidx.work.*
import com.example.chefi.Chefi
import com.example.chefi.LiveDataHolder
import com.example.chefi.ObserveWrapper
import com.example.chefi.R
import com.example.chefi.workers.UpdateCurrUserFeedWorker
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
import com.google.gson.Gson
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

    private var userNotification: ArrayList<DbNotificationItem>? = null  // TODO CHANGE!!
    private var userFavorites: ArrayList<DbRecipe>? = null
    private var userDbRecipes: ArrayList<DbRecipe>? = null

    private var currDbUser: DbUser? = null
    private var dbUserFollowing: ArrayList<DbUser>? = null
    private var dbUserFollowers: ArrayList<DbUser>? = null
    private var unseenNotification: Int = 0
    private var lastVisible: DocumentSnapshot? = null


    //App Objects
    private var userAppRecipes: ArrayList<AppRecipe>? = null
    private var userAppFavorites: ArrayList<AppRecipe>? = null
    private var userAppNotification: ArrayList<AppNotification>? = null

    companion object {
        // TAGS
        private const val TAG_APP_DB: String = "appDb"
    }

    init {
        updateCurrentUserField()
        createNotificationsLiveQuery()
    }

    fun setUnseenNotification(num: Int) {
        unseenNotification = num
    }

    fun setUserPermission(appRecipe: AppRecipe, userId: String) {
        if (appRecipe.status == 3) {
            if (appRecipe.allowedUsers == null) {
                appRecipe.allowedUsers = ArrayList()
            }
            val userRef = firestore
                .collection(Chefi.getCon().getString(R.string.usersCollection))
                .document(userId)
            if (!appRecipe.allowedUsers!!.contains(userRef)) {
                appRecipe.allowedUsers!!.add(userRef)
                firestore
                    .collection(Chefi.getCon().getString(R.string.recipesCollection))
                    .document(appRecipe.uid!!)
                    .get()
                    .addOnSuccessListener {
                        val dbUser = it.toObject<DbRecipe>()
                        if (dbUser != null){
                            updateRecipeInRecipesCollection(dbUser)
                        }
                    }
            } else {
                Log.w(TAG_APP_DB, "in setUserPermission appRecipe.allowedUsers already contains the user ref")
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

    fun getCurrUser(): DbUser? {
        return currDbUser
    }

    fun getFirebaseCurrUser(): FirebaseUser? {
        return auth.currentUser
    }

    private fun initCurrUser() {
        currDbUser = null
//        userDbRecipes = null
        dbUserFollowers = null
        dbUserFollowing = null
//        userNotification = null

        //
        userAppRecipes = null
        userAppFavorites = null
        userAppNotification = null
    }

    private fun updateCurrentUserField() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            val usersCollectionPath = Chefi.getCon().getString(R.string.usersCollection)
            val userId = firebaseUser.uid
            val documentUser = firestore.collection(usersCollectionPath).document(userId)
            documentUser.get().addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject<DbUser>()
                Log.d(TAG_APP_DB, "user?.name = ${user?.name} in updateCurrentUser")
                if (user != null) {
                    currDbUser = user
//                    userDbRecipes = null
                    dbUserFollowers = null
                    dbUserFollowing = null
//                    userNotification = null

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
                    Log.w(TAG_APP_DB, "createUserWithEmail:failure ${task.exception?.message}")
                    postString(task.exception?.message.toString())
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
            Log.d(TAG_APP_DB, "in addUserToCollection myReference = $myReference")
            dbUser.myReference = myReference

            val emptyRecipe = DbRecipe()
            val collectionPath = dbUser.uid!!
            val emptyPostRef = firestore
                .collection(collectionPath)
                .document()

            emptyRecipe.uid = emptyPostRef.id
            val feedPost = FeedPost(uid = emptyRecipe.uid, null, null, null)

            emptyPostRef.set(feedPost)
                .addOnSuccessListener {
                    myReference.set(dbUser)
                        .addOnSuccessListener {
                            updateCurrentUserField()
                        }
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
        val currUserId = dbUser?.uid
        if (currUserId != null) {
            firestore.collection(Chefi.getCon().getString(R.string.usersCollection))
                .document(currUserId)
                .set(dbUser, SetOptions.merge())
        }
    }

    fun updatePostsToCurrUserFeed(type: String, userIdTo: String) {
        firestore
            .collection(Chefi.getCon().getString(R.string.usersCollection))
            .document(userIdTo)
            .get()
            .addOnSuccessListener { documentSnapShot ->
                if (documentSnapShot != null) {
                    val userTo = documentSnapShot.toObject<DbUser>()
                    val feedTasks = ArrayList<Task<Void>>()
                    if (userTo?.recipes != null) {
                        for (recipeRef in userTo.recipes!!) {
                            when (type) {
                                "follow" -> {
                                    recipeRef
                                        .get()
                                        .addOnSuccessListener {
                                            val recipe = it.toObject<DbRecipe>()
                                            val ref = firestore.collection(currDbUser?.uid!!)
                                                .document()
                                            Log.e(TAG_APP_DB, "(after add task)in updatePostsToCurrUserFeed docRef = $ref")
                                            val feedRecipe = FeedPost(ref.id, recipe?.myReference, recipe?.timestamp, recipe?.uid)
                                            val singleTask = ref.set(feedRecipe)
                                            Log.e(TAG_APP_DB, "(after add task)in updatePostsToCurrUserFeed task = $singleTask")
                                            feedTasks.add(singleTask)
                                            Log.e(TAG_APP_DB, "(after add task)in updatePostsToCurrUserFeed dbRecipe status = ${recipe?.status}")
                                        }
                                }
                                "unfollow" -> {
                                    firestore.collection(currDbUser?.uid!!)
                                        .whereEqualTo("myReference", recipeRef)
                                        .get()
                                        .addOnSuccessListener {
                                            for (doc in it) {
                                                val feedPost = doc.toObject<FeedPost>()
                                                val singleTask = firestore
                                                    .collection(currDbUser?.uid!!)
                                                    .document(feedPost.uid!!)
                                                    .delete()
                                                feedTasks.add(singleTask)
                                            }
                                        }
                                }
                            }
                        }
                        Tasks.whenAllSuccess<Void>(feedTasks)
                            .addOnSuccessListener {
                                postInt(1)
                            }
                    } else {
                        Log.e(TAG_APP_DB, "feed contain recipe")
                    }
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
            owner = userDocument,
            myReference = newDocument
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
            myReference = newDocument
        )

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

    fun deleteRecipeFromFavorites(recipeId: String) {
        if (userAppFavorites != null) {
            val recipe = userAppFavorites?.find { it.uid == recipeId }
            if (recipe != null) {
                userAppRecipes?.remove(recipe)
                val recipeRef = firestore
                    .collection(Chefi.getCon().getString(R.string.recipesCollection))
                    .document(recipeId)
                if (currDbUser?.favorites != null) {
                    if (currDbUser != null) {
                        currDbUser?.favorites!!.remove(recipeRef)
                        updateUserInUsersCollection(currDbUser!!)
                    } else {
                        Log.w(TAG_APP_DB, "currDbUser = null")
                    }
                } else {
                    Log.w(TAG_APP_DB, "currDbUser?.favorites = null")
                }
            }
        } else {
            Log.w(TAG_APP_DB, "userAppFavorites = null")
        }
    }

    private fun postString(str: String) {
        LiveDataHolder.getStringMutableLiveData().value = ObserveWrapper(str)
    }

    private fun postInt(num: Int) {
        LiveDataHolder.getIntMutableLiveData().value = ObserveWrapper(num)
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

//    private fun loadOwnersToRecipe(userDbRecipesList : ArrayList<DbRecipe>,
//                                   type: String?,
//                                   user: DbUser?) {
//        val userAppRecipesList = ArrayList<AppRecipe>()
//
//        if (user == null) {
//            for (recipe in userDbRecipesList) {
//                val appRecipe = AppRecipe(recipe.uid,
//                    recipe.description,
//                    recipe.likes,
//                    recipe.imageUrl,
//                    recipe.comments,
//                    recipe.directions,
//                    recipe.ingredients,
//                    recipe.status,
//                    null,
//                    recipe.timestamp)
//                userAppRecipesList.add(appRecipe)
//            }
//            when (type!!) {
//                "recipes" -> {
//                    userAppRecipes = ArrayList()
//                    userAppRecipes = userAppRecipesList
//                }
//                "favorites" -> {
//                    userAppFavorites = ArrayList()
//                    userAppFavorites = userAppRecipesList
//                }
//            }
//        } else {
//            for (recipe in userDbRecipesList) {
//                val appRecipe = AppRecipe(recipe.uid,
//                    recipe.description,
//                    recipe.likes,
//                    recipe.imageUrl,
//                    recipe.comments,
//                    recipe.directions,
//                    recipe.ingredients,
//                    recipe.status,
//                    user,
//                    recipe.timestamp)
//                userAppRecipesList.add(appRecipe)
//            }
//        }
//        postAppRecipes(userAppRecipesList)
//    }

    private fun loadRecipesFromReferenceList(recipesList: ArrayList<DocumentReference>?,
                                             type: String?,
                                             isCurrUser: DbUser?) {

        val recipeTasks = ArrayList<Task<DocumentSnapshot>>()
        val userRecipesList = ArrayList<AppRecipe>()
        if (recipesList != null) {
            for (recipeRef in recipesList) {
                val docTask = recipeRef.get()
                recipeTasks.add(docTask)
            }
            Tasks.whenAllSuccess<DocumentSnapshot>(recipeTasks)
                .addOnSuccessListener { value ->
                    for (recipeDoc in value) {
                        val recipe = recipeDoc.toObject<DbRecipe>()
                        if (recipe != null) {
                            val recipeCommentsList = recipe.comments
                            val commentsTasks = ArrayList<Task<DocumentSnapshot>>()
                            if (recipeCommentsList != null) {
                                for (commentRef in recipeCommentsList) {
                                    val docTask = commentRef.get()
                                    commentsTasks.add(docTask)
                                }
                                Tasks.whenAllSuccess<DocumentSnapshot>(commentsTasks)
                                    .addOnSuccessListener { value ->
                                        val commentsList = ArrayList<Comment>()
                                        for (commentDoc in value) {
                                            val comment = commentDoc.toObject<Comment>()
                                            if (comment != null) {
                                                commentsList.add(comment)
                                            }
                                        }
                                        val appRecipe = AppRecipe(recipe.uid,
                                            recipe.description,
                                            recipe.likes,
                                            recipe.imageUrl,
                                            commentsList,
                                            recipe.directions,
                                            recipe.ingredients,
                                            recipe.status,
                                            isCurrUser,
                                            recipe.timestamp,
                                            recipe.myReference,
                                            recipe.allowedUsers)
                                        userRecipesList.add(appRecipe)

                                        // TODO - add post somewhere
                                    }
                            }
                        }
                    }
                    Log.d(TAG_APP_DB, "in loadRecipesFromReferenceList userRecipesList = ${userRecipesList.size}")
                    if (isCurrUser == null) {
                        when (type!!) {
                            "recipes" -> {
                                userAppRecipes = ArrayList()
                                userAppRecipes = userRecipesList
                            }
                            "favorites" -> {
                                userAppFavorites = ArrayList()
                                userAppFavorites = userRecipesList
                            }
                        }
                    }
                    postAppRecipes(userRecipesList)
                }
        }
    }

    private fun loadCommentsAndOwnersOfRecipes(userDbRecipesList : ArrayList<DbRecipe>,
                                   type: String?,
                                   user: DbUser?) {
        val userAppRecipesList = ArrayList<AppRecipe>()

        for (recipe in userDbRecipesList) {
            val recipeCommentsList = recipe.comments
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
                        val appRecipe = AppRecipe(recipe.uid,
                            recipe.description,
                            recipe.likes,
                            recipe.imageUrl,
                            commentsList,
                            recipe.directions,
                            recipe.ingredients,
                            recipe.status,
                            user,
                            recipe.timestamp,
                            recipe.myReference,
                            recipe.allowedUsers)
                        userAppRecipesList.add(appRecipe)
                    }
            }
        }




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
        Log.e(TAG_APP_DB, "in loadNotifications userNotificationsList = ${currDbUser?.notifications}")
        val tasks = ArrayList<Task<DocumentSnapshot>>()
        val notificationsList = ArrayList<DbNotificationItem>()
        if (userNotificationsList != null && userNotificationsList.size != 0) {
            Log.e(TAG_APP_DB, "in loadNotifications userNotificationsList = ${currDbUser?.notifications}")
//            userNotification = ArrayList()
            for (notificationRef in userNotificationsList) {
                val docTask = notificationRef.get()
                tasks.add(docTask)
            }
            Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                .addOnSuccessListener { value ->
                    for (notificationDoc in value) {
                        val notification = notificationDoc.toObject<DbNotificationItem>()
                        if (notification != null) {
                            notificationsList.add(notification)
                        }
                    }
                    loadOwnersToNotifications(notificationsList)
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

    private fun loadOwnersToNotifications(notificationList: ArrayList<DbNotificationItem>?) {
        if (userAppNotification == null) {
            userAppNotification = ArrayList()
        }
        Log.d("notification",
            "in loadOwnersToNotifications notification content = $notificationList")
        if (notificationList != null) {
            val tasks = ArrayList<Task<DocumentSnapshot>>()
            for (notification in notificationList) {
                Log.d("notification",
                    "in loadOwnersToNotifications notification content = ${notification.notificationContent}")
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
                            val notification = notificationList.find{ it.userId == user?.uid }
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
                        if (userAppNotification != null) {
                            postNotificationList(userAppNotification!!)
                        }
                        Log.e(TAG_APP_DB, "in loadOwnersToNotifications userAppNotification = $userAppNotification")
                    }
                }
        }
    }

//    fun updateCommentInFollowersFeed(typeOp: String, recipeId: String, commentAsJson: String) {
//        val commentType = object : TypeToken<Comment>() {}.type
//        val comment = Gson().fromJson<Comment>(commentAsJson, commentType)
//        val commentRef = firestore
//            .collection(Chefi.getCon().getString(R.string.commentsCollection))
//            .document(comment.uid!!)
//        firestore
//            .collection(Chefi.getCon().getString(R.string.recipesCollection))
//            .document(recipeId)
//            .get()
//            .addOnSuccessListener { documentSnapShot ->
//                val recipe = documentSnapShot.toObject<DbRecipe>()
//                val ownerRef = recipe?.owner
//                ownerRef?.get()
//                    ?.addOnSuccessListener { userSnapShot ->
//                        val recipeUser = userSnapShot.toObject<DbUser>()
//                        val followersRefList = recipeUser?.followers
//                        val followersUsersTasks = ArrayList<Task<DocumentSnapshot>>()
//                        if (followersRefList != null) {
//                            for (userRef in followersRefList) {
//                                followersUsersTasks.add(userRef.get())
//                            }
//                            Tasks.whenAllSuccess<DocumentSnapshot>(followersUsersTasks)
//                                .addOnSuccessListener { followersSnapShotList ->
//                                    val usersFeedTasks = ArrayList<Task<QuerySnapshot>>()
//                                    for (snapShot in followersSnapShotList) {
//                                        val user = userSnapShot.toObject<DbUser>()
//                                        val sinTask = firestore.collection(user?.uid!!)
//                                            .whereEqualTo("recipeId", recipeId)
//                                            .get()
//                                        usersFeedTasks.add(sinTask)
//
//                                    }
//                                    Tasks.whenAllSuccess<QuerySnapshot>(usersFeedTasks)
//                                        .addOnSuccessListener { querySnapShot ->
//                                            val updateTasksList = ArrayList<Task<Void>>()
//                                            for (querySnapShotList in querySnapShot) {
//                                                for (postDoc in querySnapShotList) {
//                                                    val recipeToCom = postDoc.toObject<FeedPost>()
//                                                    when (typeOp) {
//                                                        "add" -> {
//                                                            recipeToCom.dbRecipe?.comments?.add(
//                                                                commentRef
//                                                            )
//                                                            recipeToCom.dbRecipe?.comments?.remove(
//                                                                commentRef
//                                                            )
//                                                            val task = postDoc
//                                                                .reference
//                                                                .set(
//                                                                    recipeToCom,
//                                                                    SetOptions.merge()
//                                                                )
//                                                            updateTasksList.add(task)
//                                                        }
//                                                        "delete" -> {
//                                                            if (recipeToCom.dbRecipe?.comments?.contains(
//                                                                    commentRef
//                                                                )!!
//                                                            ) {
//                                                                recipeToCom.dbRecipe?.comments?.remove(
//                                                                    commentRef
//                                                                )
//                                                                val task = postDoc
//                                                                    .reference
//                                                                    .set(
//                                                                        recipeToCom,
//                                                                        SetOptions.merge()
//                                                                    )
//                                                                updateTasksList.add(task)
//                                                            }
//                                                        }
//                                                    }
//
//                                                }
//                                                Tasks.whenAllSuccess<Void>(updateTasksList)
//                                                    .addOnSuccessListener {
//                                                        postInt(1)
//                                                    }
//                                            }
//
//                                        }
//                                }
//                        }
//                    }
//            }
//    }

    fun addComment(content: String, recipeId: String, context: Context, type: String) {
        // TODO add worker to update comment in all followers feed collections
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

                        val comment = Comment(currDbUser?.userName,
                            currDbUser?.name,
                            content,
                            newCommentRef.id)

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

//    fun loadRecipesComments(appRecipe: AppRecipe) {
//        val recipeCommentsList = appRecipe.comments
//        val tasks = ArrayList<Task<DocumentSnapshot>>()
//        if (recipeCommentsList != null) {
//            val commentsList = ArrayList<Comment>()
//            for (commentRef in recipeCommentsList) {
//                val docTask = commentRef.get()
//                tasks.add(docTask)
//            }
//            Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
//                .addOnSuccessListener { value ->
//                    for (commentDoc in value) {
//                        val comment = commentDoc.toObject<Comment>()
//                        if (comment != null) {
//                            commentsList.add(comment)
//                        }
//                    }
//                    postCommentsList(commentsList)
//                }
//        }
//    }

    fun updateUserFields(fieldName: String, content: String) {
        when (fieldName) {
            "aboutMe" -> currDbUser?.aboutMe = content
            "name" -> {
                currDbUser?.name = content
                currDbUser?.name_lowerCase = content.toLowerCase(Locale.ROOT)
            }
            "imageUrl" -> currDbUser?.imageUrl = content  // TODO - if changed maybe delete the old one ?
        }
        updateUserInUsersCollection(currDbUser)
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
                if (recipe != null) {
//                    val dbRecipeFromList = userDbRecipes?.find { it.uid == recipe.uid }
                    val appRecipeFromList = userAppRecipes?.find { it.uid == recipe.uid }
//                    dbRecipeFromList?.likes = appRecipe.likes
                    appRecipeFromList?.likes = appRecipe.likes
                    when (fieldName) {
                        "likes" -> recipe.likes = appRecipe.likes
                    }
                    // TODO - check likes correct
                    updateRecipeInRecipesCollection(recipe)
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

    private fun addWorkerUpdateCurrUserFeed(type: String, userToId: String, context: Context) {
        val workId = UUID.randomUUID()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val inputData = Data.Builder()
            .putString("type", type)
            .putString("userIdTo", userToId)
            .build()
        val oneTimeWorkRequest = OneTimeWorkRequest
            .Builder(UpdateCurrUserFeedWorker::class.java)
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag(workId.toString())
            .build()
        WorkManager.getInstance(context)
            .enqueue(oneTimeWorkRequest)
    }

    fun follow(dbUserToFollow: DbUser, context: Context) {
        // TODO: check duplicate, write to DB
        Log.e(TAG_APP_DB, dbUserToFollow.name.toString())
        // add user to the firestore
        if (currDbUser?.following != null) {
            if (!currDbUser?.following?.contains(dbUserToFollow.myReference)!!){
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
                addWorkerUpdateCurrUserFeed("follow", dbUserToFollow.uid!!, context)
            }

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
                    addWorkerUpdateCurrUserFeed("unfollow", dbUserToUnFollow.uid!!, context)
                }

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
        if (userAppFavorites != null && !userAppFavorites?.contains(appRecipe)!!)
        {
            if (recipeId != null) {
                val recipeRef =
                    firestore.collection(Chefi.getCon().getString(R.string.recipesCollection))
                        .document(recipeId)
                if (currDbUser?.favorites == null) {
                    currDbUser?.favorites = ArrayList()
                }
                currDbUser?.favorites!!.add(recipeRef)
                updateUserInUsersCollection(currDbUser)
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
            updateUserInUsersCollection(currDbUser)
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
                "userName_lowerCase",
                searchText.toLowerCase(Locale.ROOT)
            )

        query.get()
            .addOnSuccessListener { documents->
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

    fun updateFeedWithRecipe(type: String, recipeUid: String) {
        if (dbUserFollowers != null) {
            val feedDeleteTasks = ArrayList<Task<QuerySnapshot>>()
            val feedAddTasks = ArrayList<Task<Void>>()
            firestore
                .collection(Chefi.getCon().getString(R.string.recipesCollection))
                .document(recipeUid)
                .get()
                .addOnSuccessListener {
                    val dbRecipe = it.toObject<DbRecipe>()
                    for (user in dbUserFollowers!!) {
                        when (type) {
                            "add" -> {
                                val feedDocRef = firestore.collection(user.uid!!).document()
                                val feedRecipe = FeedPost(feedDocRef.id, dbRecipe?.myReference, dbRecipe?.timestamp, dbRecipe?.uid)
                                val task = feedDocRef.set(feedRecipe)
                                feedAddTasks.add(task)
                            }
                            "delete" -> {
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
//                    when (type) {
//                        "add" -> {
//                            Tasks.whenAllSuccess<Void>(feedAddTasks)
//                                .addOnSuccessListener {
//                                    postString(recipeUid)
//                                }
//                        }
//                        "delete" -> {
//                            Tasks.whenAllSuccess<Void>(feedDeleteTasks)
//                                .addOnSuccessListener {
//                                    for (query in it) {
//
//                                    }
//                                    postString(recipeUid)
//                                }
//                        }
//                    }

                }
        }
    }

    fun uploadFeed(fromBeginning: Boolean) {
        Log.d("updateFeed", "first line")
        val limit = 3
        val query = if (fromBeginning)
            firestore
                .collection(auth.currentUser?.uid!!)
                .orderBy("date", Query.Direction.ASCENDING)
                .limit(limit.toLong())
        else
            firestore
                .collection(auth.currentUser?.uid!!)
                .orderBy("date", Query.Direction.ASCENDING)
                .startAfter(lastVisible)
                .limit(limit.toLong())

        val feedPostsListDbRecipe = ArrayList<DbRecipe>()
        query.get()
            .addOnSuccessListener { querySnapShot ->
                val dbRecipesTasks = ArrayList<Task<DocumentSnapshot>>()
                lastVisible = querySnapShot.documents[querySnapShot.size() - 1]
                for (docSnapShot in querySnapShot) {
                    Log.d("updateFeed", "docSnapShot = $docSnapShot")
                    val feedPost = docSnapShot.toObject<FeedPost>()
                    Log.d("updateFeed", "date of post = ${feedPost.date}")
                    if (feedPost.dbRecipe != null){
                        val dbRecRefTask = feedPost.dbRecipe?.get()
                        if (dbRecRefTask != null) {
                            dbRecipesTasks.add(dbRecRefTask)
                        }
                    }
                }
                Log.d("updateFeed", "after first for")
                Tasks.whenAllSuccess<DocumentSnapshot>(dbRecipesTasks)
                    .addOnSuccessListener { recipeSnapShotList ->
                        Log.d("updateFeed", "after first for")
                        for (recipeSnapShot in recipeSnapShotList) {
                            val dbRecipe = recipeSnapShot.toObject<DbRecipe>()
                            if (dbRecipe != null) {
                                feedPostsListDbRecipe.add(dbRecipe)
                            }
                        }
                        val visitedOwners = ArrayList<DocumentReference>()
                        val recipeOwnerTasks = ArrayList<Task<DocumentSnapshot>>()
                        for (dbRecipe in feedPostsListDbRecipe) {
                            if (!visitedOwners.contains(dbRecipe.owner)) {
                                val task = dbRecipe.owner?.get()
                                if (task != null) {
                                    recipeOwnerTasks.add(task)
                                }
                                visitedOwners.add(dbRecipe.owner!!)
                            }
                        }
                        Tasks.whenAllSuccess<DocumentSnapshot>(recipeOwnerTasks)
                            .addOnSuccessListener { ownersList ->
                                val appRecipesList = ArrayList<AppRecipe>()
                                for (userDoc in ownersList) {
                                    val user = userDoc.toObject<DbUser>()

                                    val userRef = firestore
                                        .collection(Chefi.getCon().getString(R.string.usersCollection))
                                        .document(user?.uid!!)
                                    val recipeFilteredList =
                                        feedPostsListDbRecipe.filter { it.owner == userRef }
                                    for (recipe in recipeFilteredList) {
                                        val recipeCommentsList = recipe.comments
                                        val commentsTasks = ArrayList<Task<DocumentSnapshot>>()
                                        if (recipeCommentsList != null) {
                                            val commentsList = ArrayList<Comment>()
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
                                                    val appRecipe = AppRecipe(
                                                        recipe.uid,
                                                        recipe.description,
                                                        recipe.likes,
                                                        recipe.imageUrl,
                                                        commentsList,
                                                        recipe.directions,
                                                        recipe.ingredients,
                                                        recipe.status,
                                                        user,
                                                        recipe.timestamp,
                                                        user.myReference,
                                                        recipe.allowedUsers
                                                    )
                                                    appRecipesList.add(appRecipe)
                                                    postAppRecipes(appRecipesList)
                                                }
                                        }
                                    }
                                }
                            }
                }
            }
    }

    // after calling this method, there will be a live query that firestore will trigger
    // every time the collection "pets" is changed
    // (e.g. when a document in this collection gets created, deleted, or getting its data changed)
    private fun createNotificationsLiveQuery(){

        val referenceToCollection = firestore.collection(Chefi.getCon().getString(R.string.notificationsCollection))
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

            for (document: QueryDocumentSnapshot in value) {
                val dbNotification = document.toObject(DbNotificationItem::class.java) // convert to item
                userDbNotificationItem.add(dbNotification)
//                if ((userNotification != null) && !userNotification?.contains(dbNotification)!!) {
//                    userNotification?.add(dbNotification)
//                }
            }
        }
        referenceToCollection.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                loadOwnersToNotifications(userDbNotificationItem)
            }
        }

        // NOTICE: the live-query (also called "snapshot") stored in the variable "liveQuery",
        // will continue to listen until you will call "liveQuery.remove()"
        // you can just ignore the variable and the live-query will continue listening forever
        // (or at least until your application process will be killed by the OS)
    }

    fun isFollowedByMe(ref: DocumentReference): Boolean{
        Log.e("search", "user ref = $ref")
        return if (currDbUser?.following != null)
            currDbUser?.following?.contains(ref)!! else false

    }
}


// TODO - add on single event listener
