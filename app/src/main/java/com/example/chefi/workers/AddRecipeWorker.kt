package com.example.chefi.workers

import android.content.Context
import android.util.Log
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.example.chefi.LiveDataHolder
import com.google.common.util.concurrent.ListenableFuture
import androidx.lifecycle.Observer
import androidx.work.Data
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.database.Recipe
import com.google.gson.Gson
class AddRecipeWorker(context: Context, workerParams: WorkerParameters)
    : ListenableWorker(context, workerParams){

    private var callback: CallbackToFutureAdapter.Completer<Result>? = null
    private val appContext: Chefi
        get() = applicationContext as Chefi

    private val TAG_ADD_RECIPE_WORKER = "addRecipeWorker"
    private var observer : Observer<Recipe>? = null

    override fun startWork(): ListenableFuture<Result> {
        // 1. here we create the future and store the callback for later use
        val future = CallbackToFutureAdapter.getFuture { callback: CallbackToFutureAdapter.Completer<Result> ->
            this.callback = callback
            return@getFuture null
        }

        // we place the broadcast receiver and immediately return the "future" object
        setObserver()

        val recipeImageUrl = inputData.getString(appContext.getString(R.string.keyRecipeImageUrl))
        val recipeTitle = inputData.getString(appContext.getString(R.string.keyRecipeTitle))

        appContext.addRecipeToDb(recipeTitle, recipeImageUrl)
        return future
    }

    private fun setObserver() {
        observer = Observer<Recipe> { value ->
            Log.d(TAG_ADD_RECIPE_WORKER, "in set observer")
            val outPutData = Data.Builder()
                .putString(appContext.getString(R.string.keyRecipeType), Gson().toJson(value))
                .build()
            LiveDataHolder.getRecipeLiveData().removeObserver(observer!!)
            this.callback?.set(Result.success(outPutData))
        }
        LiveDataHolder.getRecipeLiveData().observeForever(observer!!)
    }
}
