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
import com.example.chefi.database.DbUser
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson


class AddRecipeWorker(context: Context, workerParams: WorkerParameters)
    : ListenableWorker(context, workerParams){

    private var callback: CallbackToFutureAdapter.Completer<Result>? = null
    private val appContext: Chefi
        get() = applicationContext as Chefi

    private val TAG_ADD_RECIPE_WORKER = "addRecipeWorker"
    private var observer : Observer<ObserveWrapper<DbRecipe>>? = null

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
        val userType = object : TypeToken<DbUser>() {}.type

        val recipeDirections = Gson().fromJson<ArrayList<String>>(recipeDirectionsAsString, listType)
        val owner = Gson().fromJson<DbUser>(ownerAsString, userType)
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
        observer = Observer<ObserveWrapper<DbRecipe>> { value ->
            Log.d(TAG_ADD_RECIPE_WORKER, "in set observer")
            val content = value.getContentIfNotHandled()
            if (content != null) {
                val type = object: TypeToken<DbRecipe>(){}.type
                val outPutData = Data.Builder()
                    .putString(appContext.getString(R.string.keyRecipeType), Gson().toJson(content))
                    .build()
                LiveDataHolder.getRecipeLiveData().removeObserver(observer!!)
                Log.d(TAG_ADD_RECIPE_WORKER, "in set observer recipe = $value")
                this.callback?.set(Result.success(outPutData))
            }
        }
        LiveDataHolder.getRecipeLiveData().observeForever(observer!!)
    }
}

