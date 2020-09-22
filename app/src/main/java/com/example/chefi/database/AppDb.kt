package com.example.chefi.database

/*
https://github.com/firebase/snippets-android/blob/c2d8cfd95d996bd7c0c3e0bdf35a91430043f73d/firestore/app/src/main/java/com/google/example/firestore/kotlin/DocSnippets.kt#L462-L473
 */


import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import androidx.work.*
import com.example.chefi.Chefi
import com.example.chefi.LiveDataHolder
import com.example.chefi.R
import com.example.chefi.workers.AddRecipeWorker
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.*
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
    private var usersRecipes : ArrayList<Recipe>? = null

    companion object {
        // TAGS
        private const val TAG_APP_DB: String = "appDb"
    }

    init {
        updateCurrentUser()
    }

    fun getCurrUser(): User? {
        return currUser
    }

    private fun initCurrUser(){
        currUser = null
        usersRecipes = null
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
                    usersRecipes = ArrayList()
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
                        user?.uid, user?.email, name
                    )
                    addUserToCollection(newUser)
                    postUser(newUser)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG_APP_DB, "createUserWithEmail:failure", task.exception)
                }
            }
    }

    fun addUserToCollection(user: User?) {
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

//    private fun addRecipe(recipeTitle: String?, imageUrl: String?) {
//        val recipeCollectionPath = Chefi.getCon().getString(R.string.recipesCollection)
//        val document = firestore.collection(recipeCollectionPath).document()
//        val recipe = Recipe(document.id, recipeTitle, 0, imageUrl)
//
//        addRecipeToRecipesCollection(document, recipe)
//        addRecipeToLocalCurrUserObject(document)
//        updateUserInUsersCollection()
//
//        // if we want to also add a comments collection - use next line
//        // firestore.document(document.path).collection(commentsCollectionPath).add("comment" to "amazing")
//    }

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
        val recipe = Recipe(document.id, recipeTitle, 0, imageUrl)

        document.set(recipe)
            .addOnSuccessListener {
                Log.d(TAG_APP_DB, "in success")
                usersRecipes?.add(recipe)
                addRecipeToLocalCurrUserObject(document)
                updateUserInUsersCollection()
                postSingleRecipe(recipe)
            }
            .addOnFailureListener {
                Log.d(TAG_APP_DB, "in failure")
            }
    }

    private fun postSingleRecipe(recipe : Recipe) {
        LiveDataHolder.getRecipeMutableLiveData().postValue(recipe)
    }

    fun postUser(user: User?) {
        LiveDataHolder.getUserMutableLiveData().postValue(user)
    }

    fun postRecipes() {
        Log.d(TAG_APP_DB, "usersRecipes.size = ${usersRecipes?.size}")
        LiveDataHolder.getRecipeListMutableLiveData().postValue(usersRecipes)
    }

    fun signOut() {
        initCurrUser()
        auth.signOut()
    }

    fun deleteRecipe(){
        TODO()
    }

    private fun uploadImageToDatabase(myImage: MyImage) {
        val imageId = databaseRef.push().key
        if (imageId != null) {
            databaseRef.child(imageId).setValue(myImage)
        }
    }

    fun uploadImageToStorage(uri: Uri, fileExtension: String?) {
        Log.d(TAG_APP_DB, "url upload image - $uri")
        val imagePath = System.currentTimeMillis().toString() + "." + fileExtension
        val fileRef = storageRef.child(imagePath)
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
                        uploadImageToDatabase(MyImage(url))
                    } else {
                        Log.d(TAG_APP_DB, "dowloadUri.isSuccessful = false")
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

    fun loadRecipes() {
        val recipesRefList = currUser?.recipes
        Log.d(TAG_APP_DB, "currUser?.recipes size = ${recipesRefList?.size}")
        if (recipesRefList != null) {
            for (recipeRef in recipesRefList) {
                recipeRef.get().addOnSuccessListener { documentSnapshot ->
                    val recipe = documentSnapshot.toObject<Recipe>()
                    if (recipe != null) {
                        usersRecipes?.add(recipe)
                        Log.e(TAG_APP_DB, "usersRecipes.size = ${usersRecipes?.size}")
//                        Log.e(TAG_APP_DB, "usersRecipes.size = $")
//                        postRecipes(recipes)
                    } else {
                        Log.d(TAG_APP_DB, "recipe = null")
                    }
                }
            }
            Log.e(TAG_APP_DB, "usersRecipes.size = ${usersRecipes?.size} last")
            postRecipes()
        }
    }

    fun loadRecipesFirstTime(){
        val recipesRefList = currUser?.recipes
        val tasks = ArrayList<Task<DocumentSnapshot>>()
        usersRecipes = ArrayList()

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
                            usersRecipes?.add(recipe)
                        }
                    }
                    postRecipes()
                    Log.e(TAG_APP_DB, "usersRecipes.size = ${usersRecipes?.size} last")
                }
        }
    }

    suspend fun loadRecipes_1(index : Int) {
        val recipesRefList = currUser?.recipes
        Log.d(TAG_APP_DB, "currUser?.recipes size = ${recipesRefList?.size}")
        if (recipesRefList != null) {
            for (recipeRef in recipesRefList) {
                Log.d(TAG_APP_DB, "current thread = ${Thread.currentThread().name}")
                recipeRef.get().addOnSuccessListener { documentSnapshot ->
                    val recipe = documentSnapshot.toObject<Recipe>()
                    if (recipe != null) {
                        usersRecipes?.add(recipe)
                        Log.e(TAG_APP_DB, "usersRecipes.size = ${usersRecipes?.size}")
//                        Log.e(TAG_APP_DB, "usersRecipes.size = $")
//                        postRecipes(recipes)
                    } else {
                        Log.d(TAG_APP_DB, "recipe = null")
                    }
                }
            }
            Log.e(TAG_APP_DB, "usersRecipes.size = ${usersRecipes?.size} last")
        }

//        Log.d(TAG_APP_DB, "current thread = ${Thread.currentThread().name}")

    }
}

// TODO - add on single event listener
