package com.example.chefi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chefi.database.Recipe
import com.example.chefi.database.User
import com.google.firebase.auth.FirebaseUser

object LiveDataHolder {

    private val mutableUserLiveData : MutableLiveData<User> by lazy {
        MutableLiveData<User>()
    }

    private val mutableRecipesLiveData : MutableLiveData<MutableList<Recipe>> by lazy {
        MutableLiveData<MutableList<Recipe>>()
    }

    fun getUserLiveData() : LiveData<User>{
        return mutableUserLiveData
    }

    fun getUserMutableLiveData() : MutableLiveData<User>{
        return mutableUserLiveData
    }

    fun getRecipeLiveData() : LiveData<MutableList<Recipe>>{
        return mutableRecipesLiveData
    }

    fun getRecipeMutableLiveData() : MutableLiveData<MutableList<Recipe>>{
        return mutableRecipesLiveData
    }

}