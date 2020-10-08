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
import com.example.chefi.database.AppDb
import com.google.common.util.concurrent.ListenableFuture

class UpdateCurrUserFeedWorker(context: Context, workerParams: WorkerParameters)
    : ListenableWorker(context, workerParams) {

    private var userIdTo: String? = null
    private var callback: CallbackToFutureAdapter.Completer<Result>? = null
    private var observer : Observer<ObserveWrapper<Int>>? = null
    private val appContext: Chefi
        get() = applicationContext as Chefi

    private val TAG_UPDATE_FEED_FOLLOW_WORKER = "updateFollowersFollowFeedWorker"
    private val TAG_UPDATE_FEED = "updateFeed"

    override fun startWork(): ListenableFuture<Result> {
        val future = CallbackToFutureAdapter.getFuture {
                callback: CallbackToFutureAdapter.Completer<Result> ->
            this.callback = callback
            return@getFuture null
        }

        val typeOp = inputData.getString("type")
        userIdTo = inputData.getString("userIdTo")

        // we place the broadcast receiver and immediately return the "future" object
        setObserver()

        if (typeOp != null && userIdTo != null) {
            Log.e(TAG_UPDATE_FEED, "in UpdateCurrUserFeedWorker, userId = $userIdTo, type = $typeOp")
//            appContext.updatePostsToCurrUserFeed(typeOp, userIdTo!!)
        }

        return future
    }

    private fun setObserver() {
        observer = Observer<ObserveWrapper<Int>> { value ->
            Log.d(TAG_UPDATE_FEED_FOLLOW_WORKER, "in set observer")
            val content = value.getContentIfNotHandled()
            if (content != null) {
                val outPutData = Data.Builder()
                    .putString("string", userIdTo)
                    .build()
                LiveDataHolder.getIntLiveData().removeObserver(observer!!)
                Log.d(TAG_UPDATE_FEED_FOLLOW_WORKER, "in set observer recipeId = $value")
                this.callback?.set(Result.success(outPutData))
            }
        }
        LiveDataHolder.getIntLiveData().observeForever(observer!!)
    }
}