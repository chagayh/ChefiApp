package com.example.chefi

/*
https://github.com/firebase/snippets-android/blob/c2d8cfd95d996bd7c0c3e0bdf35a91430043f73d/firestore/app/src/main/java/com/google/example/firestore/kotlin/DocSnippets.kt#L462-L473
 */


import android.annotation.SuppressLint
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

@SuppressLint("Registered")
class AppDb {

    // Declare an instance of FirebaseAuth
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore = FirebaseFirestore.getInstance()
    private var currUser: User? = null

    companion object {
        // TAGS
        private const val TAG_ACCOUNT: String = "account"
    }

    fun getCurrUser() : User? {
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

    fun createUserWithEmailPassword(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG_ACCOUNT, "createUserWithEmail:success")
                    val user = auth.currentUser
                    LiveDataHolder.getFirebaseUserMutableLiveData().postValue(user)
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
        val commentsCollectionPath = Chefi.getCon().getString(R.string.recipesCommentsCollection)
        val document = firestore.collection(recipeCollectionPath).document()
//        val collectionReference = firestore.document(document.id).collection(commentsCollectionPath)
//        Log.d("account", "collectionReference = ${collectionReference.parent}")
        val recipe = Recipe(document.id, recipeTitle, 0, imageUri)
        Log.d("account", "prob before set")
        document.set(recipe)
            .addOnSuccessListener {
            Log.d("account", "in success")
        }
            .addOnFailureListener {
            Log.d("account", "in failure")
        }
        firestore.document(document.path).collection(commentsCollectionPath).add("comment" to "amazing")
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

}