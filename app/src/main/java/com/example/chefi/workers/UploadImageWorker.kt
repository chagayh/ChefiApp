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
import com.google.common.util.concurrent.ListenableFuture

class UploadImageWorker(context: Context, workerParams: WorkerParameters)
    : ListenableWorker(context, workerParams){

    private var callback: CallbackToFutureAdapter.Completer<Result>? = null
    private val appContext: Chefi
        get() = applicationContext as Chefi

    private val TAG_ULOAD_IMAGE_WORKER = "uploadImageWorker"
    private var observer : Observer<String>? = null

    override fun startWork(): ListenableFuture<Result> {
        // 1. here we create the future and store the callback for later use
        val future = CallbackToFutureAdapter.getFuture { callback: CallbackToFutureAdapter.Completer<Result> ->
            this.callback = callback
            return@getFuture null
        }

        // we place the broadcast receiver and immediately return the "future" object
        setObserver()

        val uriAsString = inputData.getString(appContext.getString(R.string.keyUri))
//        val uriType = object : TypeToken<Uri>(){}.type
//        val uri = Gson().fromJson<Uri>(uriAsJson, uriType)
        val uri = Uri.parse(uriAsString)
        val fileExtension = inputData.getString(appContext.getString(R.string.keyFileExtension))

        appContext.uploadImageToStorage(uri, fileExtension)
        return future
    }

    private fun setObserver() {
        observer = Observer<String> { value ->
            Log.d(TAG_ULOAD_IMAGE_WORKER, "in set observer")
            val outPutData = Data.Builder()
                .putString(appContext.getString(R.string.keyUrl), value)
                .build()
            LiveDataHolder.getUrlLiveData().removeObserver(observer!!)
            this.callback?.set(Result.success(outPutData))
        }
        LiveDataHolder.getUrlLiveData().observeForever(observer!!)
    }
}