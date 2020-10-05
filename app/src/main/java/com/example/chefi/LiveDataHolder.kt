package com.example.chefi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chefi.database.*

object LiveDataHolder {

    // lists init

    private val MUTABLE_DB_USER_LIVE_DATA : MutableLiveData<ObserveWrapper<DbUser>> by lazy {
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

    private val USERS_LIST_LIVE_DATA : MutableLiveData<ObserveWrapper<MutableList<DbUser>>> by lazy {
        MutableLiveData<ObserveWrapper<MutableList<DbUser>>>()
    }

    private val NOTIFICATIONS_LIVE_DATA : MutableLiveData<ObserveWrapper<MutableList<AppNotification>>> by lazy {
        MutableLiveData<ObserveWrapper<MutableList<AppNotification>>>()
    }

    private val commentsLiveData : MutableLiveData<ObserveWrapper<MutableList<Comment>>> by lazy {
        MutableLiveData<ObserveWrapper<MutableList<Comment>>>()
    }

    // getCommentsMutableLiveData

    // funs

    fun getUsersListLiveData() : LiveData<ObserveWrapper<MutableList<DbUser>>>{
        return USERS_LIST_LIVE_DATA
    }

    fun getUsersListMutableLiveData() : MutableLiveData<ObserveWrapper<MutableList<DbUser>>>{
        return USERS_LIST_LIVE_DATA
    }

    fun getNotificationsLiveData() : LiveData<ObserveWrapper<MutableList<AppNotification>>>{
        return NOTIFICATIONS_LIVE_DATA
    }

    fun getNotificationsMutableLiveData() : MutableLiveData<ObserveWrapper<MutableList<AppNotification>>>{
        return NOTIFICATIONS_LIVE_DATA
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
        return MUTABLE_DB_USER_LIVE_DATA
    }

    fun getUserMutableLiveData() : MutableLiveData<ObserveWrapper<DbUser>>{
        return MUTABLE_DB_USER_LIVE_DATA
    }

    fun getRecipeListLiveData() : LiveData<ObserveWrapper<MutableList<AppRecipe>>>{
        return mutableRecipeLiveData
    }

    fun getRecipeListMutableLiveData() : MutableLiveData<ObserveWrapper<MutableList<AppRecipe>>>{
        return mutableRecipeLiveData
    }

}