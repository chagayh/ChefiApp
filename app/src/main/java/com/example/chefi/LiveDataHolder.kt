package com.example.chefi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chefi.database.*

object LiveDataHolder {

    // lists init

    private val mutableDbUserLiveData : MutableLiveData<ObserveWrapper<DbUser>> by lazy {
        MutableLiveData<ObserveWrapper<DbUser>>()
    }

    private val mutableRecipeLiveData : MutableLiveData<ObserveWrapper<MutableList<AppRecipe>>> by lazy {
        MutableLiveData<ObserveWrapper<MutableList<AppRecipe>>>()
    }

    private val recipeLiveData : MutableLiveData<ObserveWrapper<AppRecipe>> by lazy {
        MutableLiveData<ObserveWrapper<AppRecipe>>()
    }

    private val stringLiveData : MutableLiveData<ObserveWrapper<String>> by lazy {
        MutableLiveData<ObserveWrapper<String>>()
    }

    private val usersListLiveData : MutableLiveData<ObserveWrapper<MutableList<DbUser>>> by lazy {
        MutableLiveData<ObserveWrapper<MutableList<DbUser>>>()
    }

    private val notificationLiveData : MutableLiveData<ObserveWrapper<MutableList<AppNotification>>> by lazy {
        MutableLiveData<ObserveWrapper<MutableList<AppNotification>>>()
    }

    private val commentsLiveData : MutableLiveData<ObserveWrapper<MutableList<Comment>>> by lazy {
        MutableLiveData<ObserveWrapper<MutableList<Comment>>>()
    }

    // getCommentsMutableLiveData

    // funs

    fun getUsersListLiveData() : LiveData<ObserveWrapper<MutableList<DbUser>>>{
        return usersListLiveData
    }

    fun getUsersListMutableLiveData() : MutableLiveData<ObserveWrapper<MutableList<DbUser>>>{
        return usersListLiveData
    }

    fun getNotificationsLiveData() : LiveData<ObserveWrapper<MutableList<AppNotification>>>{
        return notificationLiveData
    }

    fun getNotificationsMutableLiveData() : MutableLiveData<ObserveWrapper<MutableList<AppNotification>>>{
        return notificationLiveData
    }

    fun getCommentsLiveData() : LiveData<ObserveWrapper<MutableList<Comment>>>{
        return commentsLiveData
    }

    fun getCommentsMutableLiveData() : MutableLiveData<ObserveWrapper<MutableList<Comment>>>{
        return commentsLiveData
    }

    fun getStringLiveData() : LiveData<ObserveWrapper<String>>{
        return stringLiveData
    }

    fun getStringMutableLiveData() : MutableLiveData<ObserveWrapper<String>>{
        return stringLiveData
    }

    fun getRecipeLiveData() : LiveData<ObserveWrapper<AppRecipe>>{
        return recipeLiveData
    }

    fun getRecipeMutableLiveData() : MutableLiveData<ObserveWrapper<AppRecipe>>{
        return recipeLiveData
    }

    fun getUserLiveData() : LiveData<ObserveWrapper<DbUser>>{
        return mutableDbUserLiveData
    }

    fun getUserMutableLiveData() : MutableLiveData<ObserveWrapper<DbUser>>{
        return mutableDbUserLiveData
    }

    fun getRecipeListLiveData() : LiveData<ObserveWrapper<MutableList<AppRecipe>>>{
        return mutableRecipeLiveData
    }

    fun getRecipeListMutableLiveData() : MutableLiveData<ObserveWrapper<MutableList<AppRecipe>>>{
        return mutableRecipeLiveData
    }

}