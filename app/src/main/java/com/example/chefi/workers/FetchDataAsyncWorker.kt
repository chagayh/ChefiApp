package com.example.chefi.workers
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.content.IntentFilter
//import android.util.Log
//import androidx.concurrent.futures.CallbackToFutureAdapter
//import androidx.work.Data
//import androidx.work.ListenableWorker
//import androidx.work.Worker
//import androidx.work.WorkerParameters
//import com.example.chefi.Chefi
//import com.example.chefi.R
//import com.example.chefi.database.AppDb
//import com.example.chefi.database.Recipe
//import com.google.common.util.concurrent.ListenableFuture
//import com.google.firebase.firestore.ktx.toObject
//import com.google.gson.Gson
//
//class FetchDataAsyncWorker(context: Context, workerParams: WorkerParameters) : ListenableWorker(context, workerParams) {
//    private val appContext: Chefi
//        get() = applicationContext as Chefi
//    private var callback: CallbackToFutureAdapter.Completer<Result>? = null
//    private var receiver: BroadcastReceiver? = null // TODO - check
//    private val usersRecipes = Map<String, Recipe>
//    private val TAG_FETCH_WORKER : String = "fetchWorker"
//
//
//    override fun startWork(): ListenableFuture<Result> {
//        // 1. here we create the future and store the callback for later use
//        val future = CallbackToFutureAdapter.getFuture { callback: CallbackToFutureAdapter.Completer<Result> ->
//            this.callback = callback
//            return@getFuture null
//        }
//
//        when (inputData.getString(appContext.getString(R.string.keyDataType))) {
//            appContext.getString(R.string.keyRecipeType) -> loadRecipes()
//        }
//
//        // when finish the work
//        this.callback?.set(Result.success())
//
//        // we place the broadcast receiver and immediately return the "future" object
//        placeReceiver()
//        return future
//    }
//
//    // 2. we place the broadcast receiver now, waiting for it to fire in the future
//    private fun placeReceiver(){
//        // create the broadcast object and register it:
//
//        this.receiver = object : BroadcastReceiver() {
//            // notice that the fun onReceive() will get called in the future, not now
//            override fun onReceive(context: Context?, intent: Intent?) {
//                // got broadcast!
//                onReceivedBroadcast()
//            }
//        }
//
//        appContext.registerReceiver(this.receiver, IntentFilter("my_data_broadcast"))
//    }
//
//    // 3. when the broadcast receiver fired, we finished the work!
//    // so we will clean all and call the callback to tell WorkManager that we are DONE
//    private fun onReceivedBroadcast(){
//        appContext.unregisterReceiver(this.receiver)
//
//        val callback = this.callback
//        if (callback != null) {
//            callback.set(Result.success())
//        }
//    }
//
//    private fun loadRecipes(){
//
//        val user = appContext.getCurrUser()
//        val recipesRefList = user?.recipes
//
//        Log.d(TAG_FETCH_WORKER, "user.recipes size = ${recipesRefList?.size}")
//        if (recipesRefList != null) {
//            for (recipeRef in recipesRefList) {
//                recipeRef.get().addOnSuccessListener { documentSnapshot ->
//                    val recipe = documentSnapshot.toObject<Recipe>()
//                    if (recipe != null) {
//                        val recipeAsJson = Gson().toJson(recipe)
//                        usersRecipes.add(recipeAsJson)
//                        Log.e(TAG_FETCH_WORKER, "usersRecipes.size = ${usersRecipes.size}")
////                        Log.e(TAG_APP_DB, "usersRecipes.size = $")
////                        postRecipes(recipes)
//                    } else {
//                        Log.d(TAG_FETCH_WORKER, "recipe = null")
//                        return@addOnSuccessListener
//                    }
//                }
//            }
//        }
//    }
//
//    private suspend fun loadRecipes
//}