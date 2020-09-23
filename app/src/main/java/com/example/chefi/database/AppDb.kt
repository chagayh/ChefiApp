package com.example.chefi.database

/*
https://github.com/firebase/snippets-android/blob/c2d8cfd95d996bd7c0c3e0bdf35a91430043f73d/firestore/app/src/main/java/com/google/example/firestore/kotlin/DocSnippets.kt#L462-L473
 */


import android.annotation.SuppressLint
import android.app.Application
import android.app.Notification
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.example.chefi.Chefi
import com.example.chefi.LiveDataHolder
import com.example.chefi.R
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlin.collections.ArrayList

@SuppressLint("Registered")
class AppDb : Application() {

    // Declare an instance of FirebaseAuth
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore = FirebaseFirestore.getInstance()

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
        updateCurrentUser()
    }

    fun getUserRecipes() : ArrayList<Recipe>? {
        return userRecipes
    }

    fun getCurrUser(): User? {
        return currUser
    }

    private fun initCurrUser(){
        currUser = null
        userRecipes = null
        userFavorites = null
        userFollowers = null
        userFollowing = null
        userNotification = null
    }

    private fun updateCurrentUser() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null){
            Log.d(TAG_APP_DB, "begin of update curr user = $currUser")
            val usersCollectionPath = Chefi.getCon().getString(R.string.usersCollection)
            val userId = firebaseUser.uid
            val documentUser = firestore.collection(usersCollectionPath).document(userId)
            Log.d(TAG_APP_DB, "after docUser curr user = $currUser")
            documentUser.get().addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject<User>()
                if (user != null) {
                    Log.d(TAG_APP_DB, "user name = ${user.name}")
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

    fun createUserWithEmailPassword(email: String, password: String, name: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG_APP_DB, "createUserWithEmail:success")
                    val user = auth.currentUser
                    val newUser = User(
                        user?.uid, email=user?.email, name=name
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
                    updateCurrentUser()
                }
        }
    }

    fun logIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    updateCurrentUser()
                    Log.d(TAG_APP_DB, "logInWithEmail:success")
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG_APP_DB, "logInWithEmail:failure", task.exception)
                }
            }
    }

    private fun updateUserInUsersCollection() {
        // update the user in the db
        val currUserId = currUser?.uid
        if (currUserId != null) {
            firestore.collection(Chefi.getCon().getString(R.string.usersCollection))
                .document(currUserId)
                .set(currUser!!, SetOptions.merge())
        }
    }

    private fun addRecipeToLocalCurrUserObject(document: DocumentReference) {
        if (currUser == null) {
            Log.e(TAG_APP_DB, "current user = null")
        } else if (currUser?.recipes == null) {
            currUser?.recipes = ArrayList()
        }
        Log.d(TAG_APP_DB, "currUser.recipe.size = ${currUser?.recipes?.size}")
        currUser?.recipes?.add(document)
    }

    fun addRecipeToRecipesCollection(recipeTitle: String?, imageUrl: String?) {
        val recipeCollectionPath = Chefi.getCon().getString(R.string.recipesCollection)
        val document = firestore.collection(recipeCollectionPath).document()
        val recipe = Recipe(uid=document.id, name=recipeTitle, likes=0, imageUrl=imageUrl)

        if (userRecipes == null) {
            userRecipes = ArrayList()
        }
        document.set(recipe)
            .addOnSuccessListener {
                Log.d(TAG_APP_DB, "in success")
                userRecipes?.add(recipe)
                addRecipeToLocalCurrUserObject(document)
                updateUserInUsersCollection()
                postSingleRecipe(recipe)    // the worker observe to this post
            }
            .addOnFailureListener {
                Log.d(TAG_APP_DB, "in failure")
            }
    }

    private fun postSingleRecipe(recipe : Recipe) {
        LiveDataHolder.getRecipeMutableLiveData().postValue(recipe)
    }

    private fun postUser(user: User?) {
        LiveDataHolder.getUserMutableLiveData().postValue(user)
    }

    private fun postRecipes() {
        Log.d(TAG_APP_DB, "usersRecipes.size = ${userRecipes?.size}")
        LiveDataHolder.getRecipeListMutableLiveData().postValue(userRecipes)
    }

    // add on success listener and set observer
    fun signOut() {
        initCurrUser()
        auth.signOut()
    }

    fun deleteRecipe(recipe : Recipe){
        // delete from local array
        userRecipes?.remove(recipe)

        // delete from storage and data base
        val imageUrl = recipe.imageUrl

        FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl!!).delete()
            .addOnSuccessListener {
                Log.d(TAG_APP_DB, "recipe ${recipe.name} deleted")
                deleteRecipeFromDatabase(recipe)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG_APP_DB, "can't delete recipe ${recipe.name}, ${exception.message}")
            }
    }

    private fun deleteRecipeFromDatabase(recipe: Recipe) {
        val query = databaseRef.child(recipe.databaseImageId!!)
        query.addListenerForSingleValueEvent( object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.ref.removeValue()
                Log.d(TAG_APP_DB, "database doc $snapshot removed")
                deleteRecipeFromCollection(recipe)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG_APP_DB, "can't remove database doc ${error.message}")
            }
        })
    }

    private fun deleteRecipeFromCollection(recipe: Recipe) {
        firestore.collection(Chefi.getCon().getString(R.string.recipesCollection))
            .document(recipe.uid!!)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG_APP_DB, "recipe deleted")
            }
            .addOnFailureListener { exception ->
                Log.d(TAG_APP_DB, "exception ${exception.message}")
            }
    }

    private fun uploadImageToDatabase(imageUrl: String) {
        // TODO - add databaseId to recipe fields
        val imageDatabaseId = databaseRef.push().key
        val image = DatabaseImage(imageDatabaseId!!, imageUrl)
        databaseRef.child(imageDatabaseId).setValue(image)
            .addOnSuccessListener {
            LiveDataHolder.getDatabaseImageMutableLiveData().postValue(image)
        }
            .addOnFailureListener{ exception ->
                Log.d(TAG_APP_DB, "exception upload to database = ${exception.message}")
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
                        uploadImageToDatabase(url)
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
                val progress = (100 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
//                progressBar.progress = progress.toInt()
            }
        Log.d(TAG_APP_DB, "image url = ${fileRef.downloadUrl}")
    }

    fun loadRecipesFirstTime(){
        val recipesRefList = currUser?.recipes
        val tasks = ArrayList<Task<DocumentSnapshot>>()

        if (recipesRefList != null) {
            userRecipes = ArrayList()
            for (recipeRef in recipesRefList) {
                val docTask = recipeRef.get()
                tasks.add(docTask)
            }
            Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                .addOnSuccessListener { value ->
                    for (recipeDoc in value) {
                        val recipe = recipeDoc.toObject<Recipe>()
                        if (recipe != null) {
                            userRecipes?.add(recipe)
                        }
                    }
                    postRecipes()
                    Log.e(TAG_APP_DB, "usersRecipes.size = ${userRecipes?.size} last")
                }
        }
    }

    // TODO - delete, for debug only
    fun loadSingleImage(imageId : String){
        databaseRef.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (image in snapshot.children){
                        val databaseImage = image.getValue(DatabaseImage::class.java)
                        Log.d(TAG_APP_DB, "image = $image, imageId = $imageId")
                        LiveDataHolder.getDatabaseImageMutableLiveData().postValue(databaseImage)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    fun updateUserFields(fieldName: String, content: String) {
        when (fieldName) {
            "aboutMe" -> currUser?.aboutMe = content
            "name" -> currUser?.name = content
            "imageUrl" -> currUser?.imageUrl = content  // TODO - if changed maybe delete the old one ?
            "databaseImageId" -> currUser?.databaseImageId = content
        }
        updateUserInUsersCollection()
    }

    fun follow(userToFollow: User) {
        if (userFollowing == null) {
            userFollowing = ArrayList()
        }
        userFollowing!!.add(userToFollow)
        updateUserInUsersCollection()
    }

    fun unFollow(userToUnFollow: User) {
        if (userFollowing == null) {
            return
        }
        userFollowing!!.remove(userToUnFollow)
        updateUserInUsersCollection()
    }

    fun addRecipeToFavorites(recipe: Recipe) {
        if (userFavorites == null) {
            userFavorites = ArrayList()
        }
        userFavorites!!.add(recipe)
        updateUserInUsersCollection()
    }

    fun removeRecipeToFavorites(recipe: Recipe) {
        if (userFavorites == null) {
            return
        }
        userFavorites!!.remove(recipe)
        updateUserInUsersCollection()
    }
}

// TODO - add on single event listener
