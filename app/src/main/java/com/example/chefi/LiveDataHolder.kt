package com.example.chefi

import android.app.Notification
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chefi.database.Recipe
import com.example.chefi.database.User

object LiveDataHolder {

    // lists init

    private val mutableUserLiveData : MutableLiveData<User> by lazy {
        MutableLiveData<User>()
    }

    private val mutableRecipesLiveData : MutableLiveData<MutableList<Recipe>> by lazy {
        MutableLiveData<MutableList<Recipe>>()
    }

    private val recipesLiveData : MutableLiveData<Recipe> by lazy {
        MutableLiveData<Recipe>()
    }

    private val stringLiveData : MutableLiveData<ObserveWrapper<String>> by lazy {
        MutableLiveData<ObserveWrapper<String>>()
    }

    private val usersListLiveData : MutableLiveData<MutableList<User>> by lazy {
        MutableLiveData<MutableList<User>>()
    }

    private val notificationsLiveData : MutableLiveData<MutableList<Notification>> by lazy {
        MutableLiveData<MutableList<Notification>>()
    }

    // funs

    fun getUsersListLiveData() : LiveData<MutableList<User>>{
        return usersListLiveData
    }

    fun getUsersListMutableLiveData() : MutableLiveData<MutableList<User>>{
        return usersListLiveData
    }

    fun getNotificationsLiveData() : LiveData<MutableList<Notification>>{
        return notificationsLiveData
    }

    fun getNotificationsMutableLiveData() : MutableLiveData<MutableList<Notification>>{
        return notificationsLiveData
    }

    fun getStringLiveData() : LiveData<ObserveWrapper<String>>{
        return stringLiveData
    }

    fun getStringMutableLiveData() : MutableLiveData<ObserveWrapper<String>>{
        return stringLiveData
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