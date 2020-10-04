package com.example.chefi.database

/*
https://github.com/firebase/snippets-android/blob/c2d8cfd95d996bd7c0c3e0bdf35a91430043f73d/firestore/app/src/main/java/com/google/example/firestore/kotlin/DocSnippets.kt#L462-L473
 */


import android.annotation.SuppressLint
import android.app.Notification
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
    private var userNotification : ArrayList<Notification>? = null  // TODO CHANGE!!
    private var userFavorites : ArrayList<DbRecipe>? = null


    //App Objects
    private var userAppRecipes : ArrayList<AppRecipe>? = null
    private var userAppFavorites : ArrayList<AppRecipe>? = null
    private var userAppNotification : ArrayList<Notification>? = null

    companion object {
        // TAGS
        private const val TAG_APP_DB: String = "appDb"
    }

    init {
        updateCurrentUserField()
    }

    fun getFirebaseAuth() : FirebaseAuth {
        return auth
    }

    fun getUserRecipes() : ArrayList<AppRecipe>? {
        return userAppRecipes
    }

    fun getUserFavorites() : ArrayList<DbRecipe>? {
        return userFavorites
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
        userFavorites = null
        dbUserFollowers = null
        dbUserFollowing = null
        userNotification = null
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
                    userFavorites = null
                    dbUserFollowers = null
                    dbUserFollowing = null
                    userNotification = null
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
            firestore.collection(Chefi.getCon().getString(R.string.usersCollection))
                .document(dbUser.uid!!)
                .set(dbUser)
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
        val newDocument = firestore.collection(recipeCollectionPath).document()
        val recipe = DbRecipe(
            uid = newDocument.id,
            description = recipeName,
            likes = 0,
            imageUrl = imageUrl,
            comments = ArrayList(),
            directions = direction,
            ingredients = ingredients,
            status = status,
            owner = currDbUser?.uid
        )

        if (userDbRecipes == null) {
            userDbRecipes = ArrayList()
        }
        newDocument.set(recipe)
            .addOnSuccessListener {
                Log.d(TAG_APP_DB, "in success of addRecipeToRecipesCollection")
                userDbRecipes?.add(recipe)
                addRecipeToLocalCurrUserObject(newDocument)
                updateUserInUsersCollection(null)
                postSingleRecipe(recipe)    // the worker observe to this post
//                Log.d(TAG_APP_DB, "in addRecipeToRecipesCollection userRecipes.size = ${userRecipes!!.size}")
            }
            .addOnFailureListener {
                Log.d(TAG_APP_DB, "in failure")
            }
    }

    private fun postSingleRecipe(dbRecipe: DbRecipe) {
//        LiveDataHolder.getRecipeMutableLiveData().postValue(recipe)
        LiveDataHolder.getRecipeMutableLiveData().value = ObserveWrapper(dbRecipe)
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

    private fun postNotificationList(notificationList: ArrayList<Notification>) {
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

    fun deleteRecipe(dbRecipe: DbRecipe){
        // delete from local array
        if (userDbRecipes?.contains(dbRecipe)!!) {
            userDbRecipes?.remove(dbRecipe)
        } else {
            Log.d(TAG_APP_DB, "in deleteRecipe, local recipe list not contains")
        }
        // delete from storage and data base
        val imageUrl = dbRecipe.imageUrl
        deleteImageFromStorage(imageUrl, dbRecipe)
    }

    fun deleteImageFromStorage(imageUrl: String?, dbRecipe: DbRecipe?){
        FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl!!).delete()
            .addOnSuccessListener {
                if (dbRecipe != null) {
                    deleteRecipeFromCollection(dbRecipe)
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG_APP_DB, "can't delete recipe $imageUrl, ${exception.message}")
            }
    }

    private fun deleteRecipeFromUserDocument(dbRecipe: DbRecipe) {
        firestore.collection(Chefi.getCon().getString(R.string.recipesCollection))
            .document(dbRecipe.uid!!)
            .get()
            .addOnSuccessListener { documentSnapShot ->
                if (documentSnapShot != null) {
                    val recipeRef = documentSnapShot.reference
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
            }
    }

    private fun deleteRecipeFromCollection(dbRecipe: DbRecipe) {
        firestore.collection(Chefi.getCon().getString(R.string.recipesCollection))
            .document(dbRecipe.uid!!)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG_APP_DB, "recipe deleted")
                deleteRecipeFromUserDocument(dbRecipe)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG_APP_DB, "exception ${exception.message}")
            }
    }

    fun uploadImageToStorage(uri: Uri, fileExtension: String?) {
        val imageStorageId = System.currentTimeMillis().toString() + "." + fileExtension
        val fileRef = storageRef.child(imageStorageId)
//        val progressBar = ProgressBar(appContext)
//        progressBar.display     // TODO - check to switch to visibility

        // upload the file to firebase storage
        fileRef.putFile(uri)
            .addOnSuccessListener { uploadTask ->
//                Toast.makeText(appContext, "File uploaded", Toast.LENGTH_SHORT)
//                    .show()
//                progressBar.visibility = View.GONE
                val downloadUri = uploadTask.storage.downloadUrl
                downloadUri.addOnSuccessListener {
                    if (downloadUri.isSuccessful) {
                        val url = downloadUri.result.toString()
                        Log.d(TAG_APP_DB, "url upload image - $url")
                        Log.d("change_url", "in appDb in uploadImageToStorage image url = $url")
//                        LiveDataHolder.getStringMutableLiveData().postValue(url)
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

    fun loadRecipes(dbUser: DbUser?){

        if (dbUser == null) {
            val recipesRefList = currDbUser?.recipes
            loadRecipesFromReferenceList(recipesRefList, "recipes", true)
        } else {
            firestore.collection(Chefi.getCon().getString(R.string.usersCollection))
                .document(dbUser.uid!!)
                .get()
                .addOnSuccessListener { documentSnapShot ->
                    if (documentSnapShot != null) {
                        val otherUser = documentSnapShot.toObject<DbUser>()
                        loadRecipesFromReferenceList(otherUser?.recipes, "recipes", false)
                    }
                }
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
                                   isCurrUser: Boolean) {
        val tasks = ArrayList<Task<DocumentSnapshot>>()
        val userAppRecipesList = ArrayList<AppRecipe>()
        for (recipe in userDbRecipesList) {
            val docTask = firestore
                .collection(Chefi.getCon().getString(R.string.usersCollection))
                .document(recipe.owner!!)
                .get()
            tasks.add(docTask)
        }
        Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
            .addOnSuccessListener { value ->
                for (docSnapShot in value) {
                    val user = docSnapShot?.toObject<DbUser>()
                    for (recipe in userDbRecipesList) {
                        if (recipe.owner == user?.uid) {
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
                }
            }
        if (isCurrUser) {
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
        }
        postAppRecipes(userAppRecipesList)
    }

    private fun loadRecipesFromReferenceList(recipesList: ArrayList<DocumentReference>?,
                                             type: String?,
                                             isCurrUser: Boolean) {

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
        loadRecipesFromReferenceList(recipesFavoritesList, "favorites", true)

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
                    Log.e(TAG_APP_DB, "followingList.size = ${followingList.size} last")
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

//    fun loadFollowers_1(user: User?){
//        val userFollowersList = currUser?.followers
//        val tasks = ArrayList<Task<DocumentSnapshot>>()
//        val followersList = ArrayList<User>()
//        if (userFollowersList != null) {
////            userFollowers = ArrayList()
//            for (userRef in userFollowersList) {
//                val docTask = userRef.get()
//                tasks.add(docTask)
//            }
//            Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
//                .addOnSuccessListener { value ->
//                    for (userDoc in value) {
//                        val currUser = userDoc.toObject<User>()
//                        if (currUser != null) {
//                            followersList.add(currUser)
//                        }
//                    }
//                    Log.e(TAG_APP_DB, "followingList.size = ${followersList.size} last")
//                    if (user == null) {
//                        userFollowers = ArrayList()
//                        userFollowers = followersList
//                    }
//                    postUsersList(followersList)
//                }
//        }
//    }

    fun loadNotificationsFirstTime(){
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
                        val notification = notificationDoc.toObject<Notification>()
                        if (notification != null) {
                            userNotification?.add(notification)
                        }
                    }
                    postNotificationList(userNotification!!)   // TODO - check if needed
                    Log.e(TAG_APP_DB, "userNotification.size = ${userNotification?.size} last")
                }
        }
    }

    fun loadRecipesComments(dbRecipe: DbRecipe) {
        val recipeCommentsList = dbRecipe.comments
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

    fun updateRecipeFields(dbRecipe: DbRecipe, fieldName: String, content: String) {
        // TODO - updates options - like, comment per recipe
        when (fieldName) {
            "likes" -> dbRecipe.likes = dbRecipe.likes?.plus(1)
            "name" -> {
                dbRecipe.description = content
                currDbUser?.name_lowerCase = content.toLowerCase(Locale.ROOT)
            }
            "imageUrl" -> currDbUser?.imageUrl = content  // TODO - if changed maybe delete the old one ?
        }
//        updateRecipeInRecipesCollection(recipe)
    }

    fun addUserToFollowers(otherDbUser: DbUser) {
        val currUserId = currDbUser?.uid
        val otherUserId = otherDbUser.uid

        if (currUserId != null && otherUserId != null) {
            firestore.collection(
                Chefi.getCon()
                    .getString(R.string.usersCollection)
            )
                .document(currUserId)
                .get()
                .addOnSuccessListener { documentSnapShot ->
                    if (documentSnapShot != null) {
                        val currUserRef = documentSnapShot.reference
                        if (otherDbUser.followers == null) {
                            otherDbUser.followers = ArrayList()
                        }
                        if (!otherDbUser.followers!!.contains(currUserRef)) {
                            otherDbUser.followers!!.add(currUserRef)
                        }
                    }
                    updateUserInUsersCollection(otherDbUser)
                }
        }
    }

    fun follow(dbUserToFollow: DbUser) {
        // TODO: check duplicate, write to DB
        Log.e("appDb", dbUserToFollow.name.toString())
        // add user to the firestore

        firestore.collection(Chefi.getCon().getString(R.string.usersCollection))
            .document(dbUserToFollow.uid!!)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {
                    if (currDbUser?.following != null) {
                        if (!currDbUser?.following?.contains(documentSnapshot.reference)!!){
                            currDbUser?.following?.add(documentSnapshot.reference)   // TODO check
                        }
                    } else {
                        currDbUser?.following = ArrayList()
                        currDbUser?.following?.add(documentSnapshot.reference)
                    }
                    updateUserInUsersCollection(null)
                    addUserToFollowers(dbUserToFollow)
                } else {
                    Log.d(TAG_APP_DB, "in follow value data: null")
                }
            }

        if (dbUserFollowing == null) {
            dbUserFollowing = ArrayList()
        }
        if (!(dbUserFollowing?.contains(dbUserToFollow))!!) {
            dbUserFollowing!!.add(dbUserToFollow)
        }
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
            firestore.collection(
                Chefi.getCon()
                    .getString(R.string.usersCollection)
            )
                .document(otherUserId)
                .get()
                .addOnSuccessListener { documentSnapShot ->
                    if (documentSnapShot != null) {
                        val userRef = documentSnapShot.reference
                        if (currDbUser?.following == null) {
                            Log.d(TAG_APP_DB, "in unFollow currUser.following == null")
                        }
                        if (currDbUser?.following!!.contains(userRef)) {
                            currDbUser?.following!!.remove(userRef)
                        } else {
                            Log.d(TAG_APP_DB, "in unFollow currUser.following not contains")
                        }
                    }
                    // remove currUser from other user followers
                    firestore.collection(
                        Chefi.getCon()
                            .getString(R.string.usersCollection)
                    )
                        .document(currUserId)
                        .get()
                        .addOnSuccessListener {
                            val userRef = documentSnapShot.reference
                            if (dbUserToUnFollow.followers == null) {
                                Log.d(TAG_APP_DB, "in unFollow userToUnFollow.followers == null")
                            }
                            if (dbUserToUnFollow.followers!!.contains(userRef)) {
                                dbUserToUnFollow.followers!!.remove(userRef)
                            } else {
                                Log.d(
                                    TAG_APP_DB,
                                    "in unFollow userToUnFollow.followers not contains"
                                )
                            }
                        }
                    updateUserInUsersCollection(dbUserToUnFollow)
                    updateUserInUsersCollection(currDbUser)
                }
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

    fun addRecipeToFavorites(dbRecipe: DbRecipe) {
        val recipeId = dbRecipe.uid

        if (recipeId != null) {
            firestore.collection(
                Chefi.getCon()
                    .getString(R.string.recipesCollection)
            )
                .document(recipeId)
                .get()
                .addOnSuccessListener { documentSnapShot ->
                    if (documentSnapShot != null) {
                        val recipeRef = documentSnapShot.reference
                        if (currDbUser?.favorites == null) {
                            currDbUser?.favorites = ArrayList()
                        }
                        currDbUser?.favorites!!.add(recipeRef)
                    }
                    updateUserInUsersCollection(null)
                }
        }
        if (userFavorites == null) {
            userFavorites = ArrayList()
        }
        userFavorites!!.add(dbRecipe)
    }

    fun removeRecipeFromFavorites(dbRecipe: DbRecipe) {
        val recipeId = dbRecipe.uid

        if (recipeId != null) {
            firestore.collection(
                Chefi.getCon()
                    .getString(R.string.recipesCollection)
            )
                .document(recipeId)
                .get()
                .addOnSuccessListener { documentSnapShot ->
                    if (documentSnapShot != null) {
                        val recipeRef = documentSnapShot.reference
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
                    }
                    updateUserInUsersCollection(null)
                }
        }
        if (userFavorites == null) {
            Log.d(TAG_APP_DB, "in addRecipeToFavorites local favorites list = null")
        }
        if (userFavorites?.contains(dbRecipe)!!) {
            userFavorites!!.remove(dbRecipe)
        } else {
            Log.d(TAG_APP_DB, "in addRecipeToFavorites local favorites list not contains")
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
}

// TODO - add on single event listener
