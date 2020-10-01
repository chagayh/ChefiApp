package com.example.chefi.workers

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.lifecycle.Observer
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.example.chefi.Chefi
import com.example.chefi.LiveDataHolder
import com.example.chefi.ObserveWrapper
import com.example.chefi.R
import com.example.chefi.database.DatabaseImage
import com.google.common.util.concurrent.ListenableFuture

class UploadImageWorker(context: Context, workerParams: WorkerParameters)
    : ListenableWorker(context, workerParams){

    private var callback: CallbackToFutureAdapter.Completer<Result>? = null
    private val appContext: Chefi
        get() = applicationContext as Chefi

    private val TAG_UPLOAD_IMAGE_WORKER = "uploadImageWorker"
    private var observer : Observer<ObserveWrapper<String>>? = null

    override fun startWork(): ListenableFuture<Result> {
        // 1. here we create the future and store the callback for later use
        val future = CallbackToFutureAdapter.getFuture { callback: CallbackToFutureAdapter.Completer<Result> ->
            this.callback = callback
            return@getFuture null
        }

        // we place the broadcast receiver and immediately return the "future" object
        setObserver()

        val uriAsString = inputData.getString(appContext.getString(R.string.keyUri))
        val uri = Uri.parse(uriAsString)

        appContext.uploadImageToStorage(uri)
        return future
    }

    private fun setObserver() {
        observer = Observer<ObserveWrapper<String>> {
            it.getContentIfNotHandled()?.let { imageUrl ->
                Log.d(TAG_UPLOAD_IMAGE_WORKER, "in set observer image url = $imageUrl")
                Log.d("change_url", "in setObserver in UploadImageWorker image url = $imageUrl")
                val outPutData = Data.Builder()
                    .putString(appContext.getString(R.string.keyUrl), imageUrl)
                    .build()
                Log.d(TAG_UPLOAD_IMAGE_WORKER, "value = $imageUrl")
                LiveDataHolder.getStringLiveData().removeObserver(observer!!)
                this.callback?.set(Result.success(outPutData))
            }
        }
        LiveDataHolder.getStringLiveData().observeForever(observer!!)
    }
}