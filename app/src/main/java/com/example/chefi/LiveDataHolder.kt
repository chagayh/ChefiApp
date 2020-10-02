package com.example.chefi

import android.app.Notification
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chefi.database.Recipe
import com.example.chefi.database.User

object LiveDataHolder {

    // lists init

    private val mutableUserLiveData : MutableLiveData<ObserveWrapper<User>> by lazy {
        MutableLiveData<ObserveWrapper<User>>()
    }

    private val mutableRecipesLiveData : MutableLiveData<ObserveWrapper<MutableList<Recipe>>> by lazy {
        MutableLiveData<ObserveWrapper<MutableList<Recipe>>>()
    }

    private val recipesLiveData : MutableLiveData<ObserveWrapper<Recipe>> by lazy {
        MutableLiveData<ObserveWrapper<Recipe>>()
    }

    private val stringLiveData : MutableLiveData<ObserveWrapper<String>> by lazy {
        MutableLiveData<ObserveWrapper<String>>()
    }

    private val usersListLiveData : MutableLiveData<ObserveWrapper<MutableList<User>>> by lazy {
        MutableLiveData<ObserveWrapper<MutableList<User>>>()
    }

    private val notificationsLiveData : MutableLiveData<ObserveWrapper<MutableList<Notification>>> by lazy {
        MutableLiveData<ObserveWrapper<MutableList<Notification>>>()
    }

    // funs

    fun getUsersListLiveData() : LiveData<ObserveWrapper<MutableList<User>>>{
        return usersListLiveData
    }

    fun getUsersListMutableLiveData() : MutableLiveData<ObserveWrapper<MutableList<User>>>{
        return usersListLiveData
    }

    fun getNotificationsLiveData() : LiveData<ObserveWrapper<MutableList<Notification>>>{
        return notificationsLiveData
    }

    fun getNotificationsMutableLiveData() : MutableLiveData<ObserveWrapper<MutableList<Notification>>>{
        return notificationsLiveData
    }

    fun getStringLiveData() : LiveData<ObserveWrapper<String>>{
        return stringLiveData
    }

    fun getStringMutableLiveData() : MutableLiveData<ObserveWrapper<String>>{
        return stringLiveData
    }

    fun getRecipeLiveData() : LiveData<ObserveWrapper<Recipe>>{
        return recipesLiveData
    }

    fun getRecipeMutableLiveData() : MutableLiveData<ObserveWrapper<Recipe>>{
        return recipesLiveData
    }

    fun getUserLiveData() : LiveData<ObserveWrapper<User>>{
        return mutableUserLiveData
    }

    fun getUserMutableLiveData() : MutableLiveData<ObserveWrapper<User>>{
        return mutableUserLiveData
    }

    fun getRecipeListLiveData() : LiveData<ObserveWrapper<MutableList<Recipe>>>{
        return mutableRecipesLiveData
    }

    fun getRecipeListMutableLiveData() : MutableLiveData<ObserveWrapper<MutableList<Recipe>>>{
        return mutableRecipesLiveData
    }

}