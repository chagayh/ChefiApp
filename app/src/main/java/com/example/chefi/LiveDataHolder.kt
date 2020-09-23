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

    private val databaseImageLiveData : MutableLiveData<DatabaseImage> by lazy {
        MutableLiveData<DatabaseImage>()
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