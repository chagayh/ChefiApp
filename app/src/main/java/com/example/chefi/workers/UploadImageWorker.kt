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
import com.example.chefi.R
import com.example.chefi.database.DatabaseImage
import com.google.common.util.concurrent.ListenableFuture

class UploadImageWorker(context: Context, workerParams: WorkerParameters)
    : ListenableWorker(context, workerParams){

    private var callback: CallbackToFutureAdapter.Completer<Result>? = null
    private val appContext: Chefi
        get() = applicationContext as Chefi

    private val TAG_UPLOAD_IMAGE_WORKER = "uploadImageWorker"
    private var observer : Observer<DatabaseImage>? = null

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
        val fileExtension = inputData.getString(appContext.getString(R.string.keyFileExtension))

        appContext.uploadImageToStorage(uri, fileExtension)
        return future
    }

    private fun setObserver() {
        observer = Observer<DatabaseImage> { value ->
            Log.d(TAG_UPLOAD_IMAGE_WORKER, "in set observer")
            val outPutData = Data.Builder()
                .putString(appContext.getString(R.string.keyUrl), value.url)
                .putString(appContext.getString(R.string.keyDataBaseId), value.dataBaseId)
                .build()
            Log.d(TAG_UPLOAD_IMAGE_WORKER, "value = $value")
            LiveDataHolder.getDatabaseImageLiveData().removeObserver(observer!!)
            this.callback?.set(Result.success(outPutData))
        }
        LiveDataHolder.getDatabaseImageLiveData().observeForever(observer!!)
    }
}