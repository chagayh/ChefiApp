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

    private val recipesLiveData : MutableLiveData<Recipe> by lazy {
        MutableLiveData<Recipe>()
    }

    private val urlLiveData : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getUrlLiveData() : LiveData<String>{
        return urlLiveData
    }

    fun getUrlMutableLiveData() : MutableLiveData<String>{
        return urlLiveData
    }

    fun getRecipeLiveData() : LiveData<Recipe>{
        return recipesLiveData
    }

    fun getRecipeMutableLiveData() : MutableLiveData<Recipe>{
        return recipesLiveData
    }

    fun getUserLiveData() : LiveData<User>{
        return mutableUserLiveData
    }

    fun getUserMutableLiveData() : MutableLiveData<User>{
        return mutableUserLiveData
    }

    fun getRecipeListLiveData() : LiveData<MutableList<Recipe>>{
        return mutableRecipesLiveData
    }

    fun getRecipeListMutableLiveData() : MutableLiveData<MutableList<Recipe>>{
        return mutableRecipesLiveData
    }

}