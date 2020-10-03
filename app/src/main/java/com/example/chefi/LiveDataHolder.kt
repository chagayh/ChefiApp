package com.example.chefi

import android.app.Notification
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chefi.database.Comment
import com.example.chefi.database.DbRecipe
import com.example.chefi.database.User

object LiveDataHolder {

    // lists init

    private val mutableUserLiveData : MutableLiveData<ObserveWrapper<User>> by lazy {
        MutableLiveData<ObserveWrapper<User>>()
    }

    private val MUTABLE_RECIPES_LIVE_DATA : MutableLiveData<ObserveWrapper<MutableList<DbRecipe>>> by lazy {
        MutableLiveData<ObserveWrapper<MutableList<DbRecipe>>>()
    }

    private val RECIPES_LIVE_DATA : MutableLiveData<ObserveWrapper<DbRecipe>> by lazy {
        MutableLiveData<ObserveWrapper<DbRecipe>>()
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

    private val commentsLiveData : MutableLiveData<ObserveWrapper<MutableList<Comment>>> by lazy {
        MutableLiveData<ObserveWrapper<MutableList<Comment>>>()
    }

    // getCommentsMutableLiveData

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

    fun getRecipeLiveData() : LiveData<ObserveWrapper<DbRecipe>>{
        return RECIPES_LIVE_DATA
    }

    fun getRecipeMutableLiveData() : MutableLiveData<ObserveWrapper<DbRecipe>>{
        return RECIPES_LIVE_DATA
    }

    fun getUserLiveData() : LiveData<ObserveWrapper<User>>{
        return mutableUserLiveData
    }

    fun getUserMutableLiveData() : MutableLiveData<ObserveWrapper<User>>{
        return mutableUserLiveData
    }

    fun getRecipeListLiveData() : LiveData<ObserveWrapper<MutableList<DbRecipe>>>{
        return MUTABLE_RECIPES_LIVE_DATA
    }

    fun getRecipeListMutableLiveData() : MutableLiveData<ObserveWrapper<MutableList<DbRecipe>>>{
        return MUTABLE_RECIPES_LIVE_DATA
    }

}