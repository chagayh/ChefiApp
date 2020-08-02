package com.example.chefi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser

object LiveDataHolder {

    private val mutableUserLiveData : MutableLiveData<User> by lazy {
        MutableLiveData<User>()
    }

    private val mutableFirebaseUserLiveData : MutableLiveData<FirebaseUser> by lazy {
        MutableLiveData<FirebaseUser>()
    }

    fun getUserLiveData() : LiveData<User>{
        return mutableUserLiveData
    }

    fun getUserMutableLiveData() : MutableLiveData<User>{
        return mutableUserLiveData
    }

    fun getFirebaseUserLiveData() : LiveData<FirebaseUser>{
        return mutableFirebaseUserLiveData
    }

    fun getFirebaseUserMutableLiveData() : MutableLiveData<FirebaseUser>{
        return mutableFirebaseUserLiveData
    }
}