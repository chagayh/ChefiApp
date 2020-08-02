package com.example.chefi

data class User (
    var uid: String?,
    var email: String?,
    var name: String? = null,
    var description: String? = null,
    var recipes: MutableList<Recipe>? = null,
    var following: MutableList<String>? = null,     // users id's
    var followers: MutableList<String>? = null      // users id's
)