package com.example.chefi.workers

import android.content.Context
import android.util.Log
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.lifecycle.Observer
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.example.chefi.Chefi
import com.example.chefi.LiveDataHolder
import com.example.chefi.ObserveWrapper
import com.google.common.util.concurrent.ListenableFuture

class UpdateFollowersFeedWorker(context: Context, workerParams: WorkerParameters)
    : ListenableWorker(context, workerParams) {

    private var callback: CallbackToFutureAdapter.Completer<Result>? = null
    private val appContext: Chefi
        get() = applicationContext as Chefi

    private var recipeId: String? = null
    private val TAG_UPDATE_FEED_WORKER = "updateFollowersFeedWorker"
    private var observer : Observer<ObserveWrapper<Int>>? = null

    override fun startWork(): ListenableFuture<Result> {
        // 1. here we create the future and store the callback for later use
        val future = CallbackToFutureAdapter.getFuture {
                callback: CallbackToFutureAdapter.Completer<Result> ->
            this.callback = callback
            return@getFuture null
        }

        // we place the broadcast receiver and immediately return the "future" object
        setObserver()

        val type =  inputData.getString("type")
        recipeId = inputData.getString("recipeUid")

        if (type != null && recipeId != null) {
            appContext.updateFeedWithRecipe(type, recipeId!!)
        } else {
            Log.d(TAG_UPDATE_FEED_WORKER, "(followersAsJson != null && type != null && recipeId != null) = false")
        }

        return future
    }

    private fun setObserver() {
        observer = Observer<ObserveWrapper<Int>> { value ->
            Log.d(TAG_UPDATE_FEED_WORKER, "in set observer")
            val content = value.getContentIfNotHandled()
            if (content != null) {
                val outPutData = Data.Builder()
                    .putString("string", recipeId)
                    .build()
                LiveDataHolder.getIntLiveData().removeObserver(observer!!)
                Log.d(TAG_UPDATE_FEED_WORKER, "in set observer recipeId = $value")
                this.callback?.set(Result.success(outPutData))
            }
        }
        LiveDataHolder.getIntLiveData().observeForever(observer!!)
    }

}