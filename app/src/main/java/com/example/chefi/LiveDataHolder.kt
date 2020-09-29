package com.example.chefi

import android.app.Notification
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chefi.database.DatabaseImage
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

    private val stringLiveData : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val usersLiveData : MutableLiveData<MutableList<User>> by lazy {
        MutableLiveData<MutableList<User>>()
    }

    private val notificationsLiveData : MutableLiveData<MutableList<Notification>> by lazy {
        MutableLiveData<MutableList<Notification>>()
    }

    // funs

    fun getUsersLiveData() : LiveData<MutableList<User>>{
        return usersLiveData
    }

    fun getUsersMutableLiveData() : MutableLiveData<MutableList<User>>{
        return usersLiveData
    }

    fun getNotificationsLiveData() : LiveData<MutableList<Notification>>{
        return notificationsLiveData
    }

    fun getNotificationsMutableLiveData() : MutableLiveData<MutableList<Notification>>{
        return notificationsLiveData
    }

    fun getStringLiveData() : LiveData<String>{
        return stringLiveData
    }

    fun getStringMutableLiveData() : MutableLiveData<String>{
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