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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
    private var currUser: User? = null
    private var userRecipes : ArrayList<Recipe>? = null
    private var userFollowing : ArrayList<User>? = null
    private var userFollowers : ArrayList<User>? = null
    private var userNotification : ArrayList<Notification>? = null
    private var userFavorites : ArrayList<Recipe>? = null

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

    fun getUserRecipes() : ArrayList<Recipe>? {
        return userRecipes
    }

    fun getUserFavorites() : ArrayList<Recipe>? {
        return userFavorites
    }

    fun getUserFollowing() : ArrayList<User>? {
        return userFollowing
    }

    fun getUserFollowers() : ArrayList<User>? {
        return userFollowers
    }

    fun getCurrUser(): User? {
        return currUser
    }

    fun getFirebaseCurrUser(): FirebaseUser? {
        return auth.currentUser
    }

    private fun initCurrUser(){
        currUser = null
        userRecipes = null
        userFavorites = null
        userFollowers = null
        userFollowing = null
        userNotification = null
    }

    private fun updateCurrentUserField() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null){
            val usersCollectionPath = Chefi.getCon().getString(R.string.usersCollection)
            val userId = firebaseUser.uid
            val documentUser = firestore.collection(usersCollectionPath).document(userId)
            documentUser.get().addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject<User>()
                Log.d(TAG_APP_DB, "user?.name = ${user?.name} in updateCurrentUser")
                if (user != null) {
                    currUser = user
                    userRecipes = null
                    userFavorites = null
                    userFollowers = null
                    userFollowing = null
                    userNotification = null
                    postUser(currUser)
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
                    val newUser = User(
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

    private fun addUserToCollection(user: User?) {
        Log.d(TAG_APP_DB, "in add to collection, uid = ${user?.uid}")
        if (user?.uid != null) {
            Log.d(TAG_APP_DB, "in add to collection")
            firestore.collection(Chefi.getCon().getString(R.string.usersCollection))
                .document(user.uid!!)
                .set(user)
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

    private fun updateUserInUsersCollection(user: User?) {
        // update the user in the db
        if (user == null) {
            val currUserId = currUser?.uid
            if (currUserId != null) {
                firestore.collection(Chefi.getCon().getString(R.string.usersCollection))
                    .document(currUserId)
                    .set(currUser!!, SetOptions.merge())
                //                .set(currUser!!)
            }
        } else {
            val userId = user.uid
            if (userId != null) {
                firestore.collection(Chefi.getCon().getString(R.string.usersCollection))
                    .document(userId)
                    .set(user, SetOptions.merge())
                //                .set(currUser!!)
            }
        }
    }

    private fun addRecipeToLocalCurrUserObject(document: DocumentReference) {
        if (currUser == null) {
            Log.e(TAG_APP_DB, "current user = null")
        } else if (currUser?.recipes == null) {
            currUser?.recipes = ArrayList()
        }
        Log.d(TAG_APP_DB, "currUser.recipe.size = ${currUser?.recipes?.size}")
        // TODO - add recipe to usersRecipeList
        currUser?.recipes?.add(document)
    }

    fun addRecipeToRecipesCollection(
        recipeName: String?,
        imageUrl: String?,
        direction: ArrayList<String>?,
        ingredients: ArrayList<String>?,
        status: Int?
    )  {
        val recipeCollectionPath = Chefi.getCon().getString(R.string.recipesCollection)
        val document = firestore.collection(recipeCollectionPath).document()
        val recipe = Recipe(
            uid = document.id,
            name = recipeName,
            likes = 0,
            imageUrl = imageUrl,
            comments = ArrayList(),
            directions = direction,
            ingredients = ingredients,
            status = status
        )

        if (userRecipes == null) {
            userRecipes = ArrayList()
        }
        document.set(recipe)
            .addOnSuccessListener {
                Log.d(TAG_APP_DB, "in success of addRecipeToRecipesCollection")
                userRecipes?.add(recipe)
                addRecipeToLocalCurrUserObject(document)
                updateUserInUsersCollection(null)
                postSingleRecipe(recipe)    // the worker observe to this post
//                Log.d(TAG_APP_DB, "in addRecipeToRecipesCollection userRecipes.size = ${userRecipes!!.size}")
            }
            .addOnFailureListener {
                Log.d(TAG_APP_DB, "in failure")
            }
    }

    private fun postSingleRecipe(recipe: Recipe) {
//        LiveDataHolder.getRecipeMutableLiveData().postValue(recipe)
        LiveDataHolder.getRecipeMutableLiveData().value = ObserveWrapper(recipe)
    }

    private fun postUser(user: User?) {
//        LiveDataHolder.getUserMutableLiveData().postValue(user)
        LiveDataHolder.getUserMutableLiveData().value = ObserveWrapper(user)
    }

    private fun postRecipes(recipesList: ArrayList<Recipe>) {
//        LiveDataHolder.getRecipeListMutableLiveData().postValue(recipesList)
        LiveDataHolder.getRecipeListMutableLiveData().value = ObserveWrapper(recipesList)
    }

    private fun postUsersList(usersList: ArrayList<User>) {
        Log.d(TAG_APP_DB, "usersList.size = ${usersList.size}")
//        LiveDataHolder.getUsersListMutableLiveData().postValue(usersList)
        LiveDataHolder.getUsersListMutableLiveData().value = ObserveWrapper(usersList)
    }

    private fun postNotificationList(notificationList: ArrayList<Notification>) {
        Log.d(TAG_APP_DB, "usersList.size = ${notificationList.size}")
//        LiveDataHolder.getNotificationsMutableLiveData().postValue(notificationList)
        LiveDataHolder.getNotificationsMutableLiveData().value = ObserveWrapper(notificationList)
    }

    // TODO add on success listener and set observer
    fun signOut() {
        initCurrUser()
        auth.signOut()
    }

    fun deleteRecipe(recipe: Recipe){
        // delete from local array
        if (userRecipes?.contains(recipe)!!) {
            userRecipes?.remove(recipe)
        } else {
            Log.d(TAG_APP_DB, "in deleteRecipe, local recipe list not contains")
        }
        // delete from storage and data base
        val imageUrl = recipe.imageUrl
        deleteImageFromStorage(imageUrl, recipe)
    }

    fun deleteImageFromStorage(imageUrl: String?, recipe: Recipe?){
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

    private fun deleteRecipeFromUserDocument(recipe: Recipe) {
        firestore.collection(Chefi.getCon().getString(R.string.recipesCollection))
            .document(recipe.uid!!)
            .get()
            .addOnSuccessListener { documentSnapShot ->
                if (documentSnapShot != null) {
                    val recipeRef = documentSnapShot.reference
                    if (currUser?.recipes?.contains(recipeRef)!!) {
                        currUser?.recipes?.remove(recipeRef)
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

    private fun deleteRecipeFromCollection(recipe: Recipe) {
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

//    private fun uploadImageToDatabase(imageUrl: String) {
//        // TODO - add databaseId to recipe fields
//        val imageDatabaseId = databaseRef.push().key
//        val image = DatabaseImage(imageDatabaseId!!, imageUrl)
//        databaseRef.child(imageDatabaseId).setValue(image)
//            .addOnSuccessListener {
//            LiveDataHolder.getStringMutableLiveData().postValue(image)
//        }
//            .addOnFailureListener{ exception ->
//                Log.d(TAG_APP_DB, "exception upload to database = ${exception.message}")
//            }
//    }

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

    fun loadRecipes(user: User?){
        val recipesRefList = currUser?.recipes
        val tasks = ArrayList<Task<DocumentSnapshot>>()
        val userRecipesList = ArrayList<Recipe>()
        if (recipesRefList != null) {
            for (recipeRef in recipesRefList) {
                val docTask = recipeRef.get()
                tasks.add(docTask)
            }
            Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                .addOnSuccessListener { value ->
                    for (recipeDoc in value) {
                        val recipe = recipeDoc.toObject<Recipe>()
                        if (recipe != null) {
                            userRecipesList.add(recipe)
                        }
                    }
                    if (user == null) {
                        userRecipes = ArrayList()
                        userRecipes = userRecipesList
                    }
                    postRecipes(userRecipesList)   // TODO - check if needed
                }
        }
    }

    fun loadFavorites(){
        val recipesFavoritesList = currUser?.favorites
        val tasks = ArrayList<Task<DocumentSnapshot>>()

        if (recipesFavoritesList != null) {
            userFavorites = ArrayList()
            for (recipeRef in recipesFavoritesList) {
                val docTask = recipeRef.get()
                tasks.add(docTask)
            }
            Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                .addOnSuccessListener { value ->
                    for (recipeDoc in value) {
                        val recipe = recipeDoc.toObject<Recipe>()
                        if (recipe != null) {
                            userFavorites?.add(recipe)
                        }
                    }
                    postRecipes(userFavorites!!)   // TODO - check if needed
                    Log.e(TAG_APP_DB, "userFavorites.size = ${userFavorites?.size} last")
                }
        }
    }

    fun loadFollowing(user: User?){
        val userFollowingList = currUser?.following
        val tasks = ArrayList<Task<DocumentSnapshot>>()
        val followingList = ArrayList<User>()
        if (userFollowingList != null) {
            for (userRef in userFollowingList) {
                val docTask = userRef.get()
                tasks.add(docTask)
            }
            Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                .addOnSuccessListener { value ->
                    for (userDoc in value) {
                        val currUser = userDoc.toObject<User>()
                        if (currUser != null) {
                            followingList.add(currUser)
                        }
                    }
                    Log.e(TAG_APP_DB, "followingList.size = ${followingList.size} last")
                    if (user == null) {
                        userFollowing = ArrayList()
                        userFollowing = followingList
                    }
                    postUsersList(followingList)   // TODO - check if needed
                }
        }
    }

    fun loadFollowers(user: User?){
        val userFollowersList = currUser?.followers
        val tasks = ArrayList<Task<DocumentSnapshot>>()
        val followersList = ArrayList<User>()
        if (userFollowersList != null) {
//            userFollowers = ArrayList()
            for (userRef in userFollowersList) {
                val docTask = userRef.get()
                tasks.add(docTask)
            }
            Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                .addOnSuccessListener { value ->
                    for (userDoc in value) {
                        val currUser = userDoc.toObject<User>()
                        if (currUser != null) {
                            followersList.add(currUser)
                        }
                    }
                    Log.e(TAG_APP_DB, "followingList.size = ${followersList.size} last")
                    if (user == null) {
                        userFollowers = ArrayList()
                        userFollowers = followersList
                    }
                    postUsersList(followersList)
                }
        }
    }

    fun loadNotificationsFirstTime(){
        val userNotificationsList = currUser?.notifications
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

    fun updateUserFields(fieldName: String, content: String) {
        when (fieldName) {
            "aboutMe" -> currUser?.aboutMe = content
            "name" -> {
                currUser?.name = content
                currUser?.name_lowerCase = content.toLowerCase(Locale.ROOT)
            }
            "imageUrl" -> currUser?.imageUrl = content  // TODO - if changed maybe delete the old one ?
        }
        updateUserInUsersCollection(null)
    }

    fun addUserToFollowers(otherUser: User) {
        val currUserId = currUser?.uid
        val otherUserId = otherUser.uid

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
                        if (otherUser.followers == null) {
                            otherUser.followers = ArrayList()
                        }
                        if (!otherUser.followers!!.contains(currUserRef)) {
                            otherUser.followers!!.add(currUserRef)
                        }
                    }
                    updateUserInUsersCollection(otherUser)
                }
        }
    }

    fun follow(userToFollow: User) {
        // TODO: check duplicate, write to DB
        Log.e("appDb", userToFollow.name.toString())
        // add user to the firestore

        firestore.collection(Chefi.getCon().getString(R.string.usersCollection))
            .document(userToFollow.uid!!)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {
                    if (currUser?.following != null) {
                        if (!currUser?.following?.contains(documentSnapshot.reference)!!){
                            currUser?.following?.add(documentSnapshot.reference)   // TODO check
                        }
                    } else {
                        currUser?.following = ArrayList()
                        currUser?.following?.add(documentSnapshot.reference)
                    }
                    updateUserInUsersCollection(null)
                    addUserToFollowers(userToFollow)
                } else {
                    Log.d(TAG_APP_DB, "in follow value data: null")
                }
            }

        if (userFollowing == null) {
            userFollowing = ArrayList()
        }
        if (!(userFollowing?.contains(userToFollow))!!) {
            userFollowing!!.add(userToFollow)
        }
    }

    fun unFollow(userToUnFollow: User) {
        val currUserId = currUser?.uid
        val otherUserId = userToUnFollow.uid

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
                        if (currUser?.following == null) {
                            Log.d(TAG_APP_DB, "in unFollow currUser.following == null")
                        }
                        if (currUser?.following!!.contains(userRef)) {
                            currUser?.following!!.remove(userRef)
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
                            if (userToUnFollow.followers == null) {
                                Log.d(TAG_APP_DB, "in unFollow userToUnFollow.followers == null")
                            }
                            if (userToUnFollow.followers!!.contains(userRef)) {
                                userToUnFollow.followers!!.remove(userRef)
                            } else {
                                Log.d(
                                    TAG_APP_DB,
                                    "in unFollow userToUnFollow.followers not contains"
                                )
                            }
                        }
                    updateUserInUsersCollection(userToUnFollow)
                    updateUserInUsersCollection(currUser)
                }
        }
        if (userFollowing == null) {
            return
        }
        if (userFollowing!!.contains(userToUnFollow)) {
            userFollowing!!.remove(userToUnFollow)
        } else {
            Log.d(TAG_APP_DB, "in unFollow local followers list not contains")
        }
    }

    fun addRecipeToFavorites(recipe: Recipe) {
        val recipeId = recipe.uid

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
                        if (currUser?.favorites == null) {
                            currUser?.favorites = ArrayList()
                        }
                        currUser?.favorites!!.add(recipeRef)
                    }
                    updateUserInUsersCollection(null)
                }
        }
        if (userFavorites == null) {
            userFavorites = ArrayList()
        }
        userFavorites!!.add(recipe)
    }

    fun removeRecipeFromFavorites(recipe: Recipe) {
        val recipeId = recipe.uid

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
                        if (currUser?.favorites == null) {
                            Log.d(TAG_APP_DB, "in addRecipeToFavorites currUser?.favorites == null")
                        }
                        if (currUser?.favorites!!.contains(recipeRef)) {
                            currUser?.favorites!!.remove(recipeRef)
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
        if (userFavorites?.contains(recipe)!!) {
            userFavorites!!.remove(recipe)
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
                    val ansUsers = ArrayList<User>()
                    for (document in documents.documents) {
                        val user = document.toObject<User>()
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
