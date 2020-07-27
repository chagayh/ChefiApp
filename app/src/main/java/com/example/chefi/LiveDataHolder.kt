package com.example.chefi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser

class LiveDataHolder {

    private val mutableUserLiveData = MutableLiveData<FirebaseUser>()

    fun getUserLiveData() : LiveData<FirebaseUser>{
        return mutableUserLiveData
    }

    fun getUserMutableLiveData() : MutableLiveData<FirebaseUser>{
        return mutableUserLiveData
    }

}