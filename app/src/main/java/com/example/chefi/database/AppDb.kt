package com.example.chefi.database

/*
https://github.com/firebase/snippets-android/blob/c2d8cfd95d996bd7c0c3e0bdf35a91430043f73d/firestore/app/src/main/java/com/google/example/firestore/kotlin/DocSnippets.kt#L462-L473
 */


import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.example.chefi.Chefi
import com.example.chefi.LiveDataHolder
import com.example.chefi.R
import com.example.chefi.activities.MainActivity
import com.google.common.io.Files.getFileExtension
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

@SuppressLint("Registered")
class AppDb : Application() {
    val appContext: Chefi
        get() = applicationContext as Chefi

    // Declare an instance of FirebaseAuth
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore = FirebaseFirestore.getInstance()
    // Chefi.getCon().getString(R.string.imageUpload)
    private val storageRef = FirebaseStorage.getInstance().getReference("uploads")
    private val databaseRef = FirebaseDatabase.getInstance().getReference("uploads")
    private var currUser: User? = null

    companion object {
        // TAGS
        private const val TAG_ACCOUNT: String = "account"
    }

    fun getCurrUser() : User? {
        updateCurrentUser(auth.currentUser)
        return currUser
    }

    private fun updateCurrentUser(firebaseUser: FirebaseUser?) {
        Log.d("account", "begin of update curr user = $currUser")
        val usersCollectionPath = Chefi.getCon().getString(R.string.usersCollection)
        val userId = firebaseUser?.uid
        val documentUser = userId?.let { firestore.collection(usersCollectionPath).document(it) }
        Log.d("account", "after docUser curr user = $currUser")
        documentUser?.get()?.addOnSuccessListener { documentSnapshot ->
            val user = documentSnapshot.toObject<User>()
            if (user != null) {
                Log.d("account", "user name = ${user.name}")
                currUser = user
                postUser(currUser)
            } else {
                Log.d("account", "user = null")
            }
        }
    }

    fun createUserWithEmailPassword(email: String, password: String, name: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG_ACCOUNT, "createUserWithEmail:success")
                    val user = auth.currentUser
                    val newUser = User(user?.uid
                                       ,user?.email
                                       ,name)
                    addUserToCollection(newUser)
                    postUser(newUser)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG_ACCOUNT, "createUserWithEmail:failure", task.exception)
                }
            }
    }

    /*
    * set without merge will overwrite a document or create it if it doesn't exist yet
    * set with merge will update fields in the document or create it if it doesn't exists
     */
    fun addUserToCollection(user: User?) {
        Log.d(TAG_ACCOUNT, "in add to collection, uid = ${user?.uid}")
        if (user?.uid != null){
            Log.d(TAG_ACCOUNT, "in add to collection")
            firestore.collection(Chefi.getCon().getString(R.string.usersCollection))
                .document(user.uid!!)
                .set(user)
        }
    }

    fun logIn(email: String, password: String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    updateCurrentUser(user)
                    Log.d(TAG_ACCOUNT, "logInWithEmail:success")
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG_ACCOUNT, "logInWithEmail:failure", task.exception)
                }
            }
    }

    fun addRecipe(recipeTitle: String, imageUri: Uri?) {
        val recipeCollectionPath = Chefi.getCon().getString(R.string.recipesCollection)
//        val commentsCollectionPath = Chefi.getCon().getString(R.string.recipesCommentsCollection)
        val document = firestore.collection(recipeCollectionPath).document()
        val recipe = Recipe(document.id, recipeTitle, 0, imageUri)
        Log.d("account", "prob before set")
        document.set(recipe)
            .addOnSuccessListener {
            Log.d("account", "in success")
        }
            .addOnFailureListener {
            Log.d("account", "in failure")
        }
        if (currUser?.recipes == null) {
            currUser?.recipes = ArrayList()
            Log.d("account", "currUser.recipe = ${currUser?.recipes}")
        }
        Log.d("account", "currUser.recipe = ${currUser?.recipes}")
        currUser?.recipes?.add(document)

        // update the user in the db
        currUser?.uid?.let {
            firestore.collection(Chefi.getCon().getString(R.string.usersCollection)).document(
                it
            ).set(currUser!!, SetOptions.merge())
        }
        // if we want to also add a comments collection - use next line
//        firestore.document(document.path).collection(commentsCollectionPath).add("comment" to "amazing")
        addRecipeToUserData(document)
    }

    private fun addRecipeToUserData(recipe: DocumentReference){
        if (currUser == null) {
            Log.w("account", "current user = null")
        } else {
            if (currUser!!.recipes == null){
                currUser!!.recipes = ArrayList()
                currUser!!.recipes?.add(recipe)
            }
        }
    }

    fun postUser(user: User?) {
        LiveDataHolder.getUserMutableLiveData().postValue(user)
    }

    fun signOut(){
        currUser = null
        auth.signOut()
    }

    fun deleteUser() {
        val currUser = auth.currentUser
        currUser?.delete()
            ?.addOnCompleteListener {
                Log.d(TAG_ACCOUNT, "deleted user")
            }
    }

    private fun uploadImageToDatabase(uri: Uri) {
        // TODO (implement)
    }

    fun uploadImageToStorage(uri: Uri, fileExtension: String?) {
        val imagePath = System.currentTimeMillis().toString() + "." + fileExtension
        val fileRef = storageRef.child(imagePath)
        val progressBar = ProgressBar(appContext)
        progressBar.display     // TODO - check to switch to visibility

        fileRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                progressBar.visibility = View.GONE
                Toast.makeText(appContext, "File uploaded", Toast.LENGTH_SHORT)
                    .show()
                // TODO - update progress bar
            }
            .addOnFailureListener { exeption ->
                Log.d("AppDb", "fail upload image - ${exeption.message}")
            }
            .addOnProgressListener { taskSnapshot ->
                val progress = (100 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                progressBar.progress = progress.toInt()
            }
    }

}