package com.example.chefi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chefi.database.DatabaseImage
import com.example.chefi.database.Recipe
import com.example.chefi.database.User

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

    private val databaseImageLiveData : MutableLiveData<DatabaseImage> by lazy {
        MutableLiveData<DatabaseImage>()
    }

    fun getDatabaseImageLiveData() : LiveData<DatabaseImage>{
        return databaseImageLiveData
    }

    fun getDatabaseImageMutableLiveData() : MutableLiveData<DatabaseImage>{
        return databaseImageLiveData
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