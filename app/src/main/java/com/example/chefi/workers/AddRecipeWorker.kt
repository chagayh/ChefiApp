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
import com.example.chefi.ObserveWrapper
import com.example.chefi.R
import com.example.chefi.database.DbRecipe
import com.example.chefi.database.AppRecipe
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson


class AddRecipeWorker(context: Context, workerParams: WorkerParameters)
    : ListenableWorker(context, workerParams){

    private var callback: CallbackToFutureAdapter.Completer<Result>? = null
    private val appContext: Chefi
        get() = applicationContext as Chefi

    private val TAG_ADD_RECIPE_WORKER = "addRecipeWorker"
    private var observer : Observer<ObserveWrapper<AppRecipe>>? = null

    override fun startWork(): ListenableFuture<Result> {
        // 1. here we create the future and store the callback for later use
        val future = CallbackToFutureAdapter.getFuture { callback: CallbackToFutureAdapter.Completer<Result> ->
            this.callback = callback
            return@getFuture null
        }

        // we place the broadcast receiver and immediately return the "future" object
        setObserver()

        val recipeDirectionsAsString = inputData.getString(appContext.getString(R.string.keyRecipeDirections))
        val recipeIngredientsAsString = inputData.getString(appContext.getString(R.string.keyRecipeIngredients))
        val ownerAsString = inputData.getString(appContext.getString(R.string.keyRecipeOwner))
        val listType = object : TypeToken<ArrayList<String>>() {}.type
        val userType = object : TypeToken<AppRecipe>() {}.type

        val recipeDirections = Gson().fromJson<ArrayList<String>>(recipeDirectionsAsString, listType)
        val recipeIngredients = Gson().fromJson<ArrayList<String>>(recipeIngredientsAsString, listType)
        val recipeImageUrl = inputData.getString(appContext.getString(R.string.keyRecipeImageUrl))
        val recipeName = inputData.getString(appContext.getString(R.string.keyRecipeName))
        val recipeStatus = inputData.getInt(appContext.getString(R.string.keyRecipeStatus), -1)

        appContext.addRecipeToDb(recipeName=recipeName,
                                 imageUrl=recipeImageUrl,
                                 direction=recipeDirections,
                                 ingredients=recipeIngredients,
                                 status=recipeStatus)

        return future
    }

    private fun setObserver() {
        observer = Observer<ObserveWrapper<AppRecipe>> { value ->
            Log.d(TAG_ADD_RECIPE_WORKER, "in set observer")
            val content = value.getContentIfNotHandled()
            if (content != null) {
                val gson = Gson()
                val outPutData = Data.Builder()
                    .putString(appContext.getString(R.string.keyUid), content.uid)
                    .putString(appContext.getString(R.string.keyDescription), content.description)
                    .putInt(appContext.getString(R.string.keyLikes), content.likes!!)
                    .putString(appContext.getString(R.string.keyImageUrl), content.imageUrl)
                    .putString(appContext.getString(R.string.keyComments), gson.toJson(content.comments))
                    .putString(appContext.getString(R.string.keyDirections), gson.toJson(content.directions))
                    .putString(appContext.getString(R.string.keyIngredients), gson.toJson(content.ingredients))
                    .putInt(appContext.getString(R.string.keyStatus), content.status!!)
                    .putString(appContext.getString(R.string.keyTimestamp), gson.toJson(content.timestamp))
                    .build()
                LiveDataHolder.getRecipeLiveData().removeObserver(observer!!)
                Log.d(TAG_ADD_RECIPE_WORKER, "in set observer recipe = $value")
                this.callback?.set(Result.success(outPutData))
            }
        }
        LiveDataHolder.getRecipeLiveData().observeForever(observer!!)
    }
}

