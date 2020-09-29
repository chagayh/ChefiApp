package com.example.chefi.database

import com.google.firebase.firestore.DocumentReference

data class Recipe(
    var uid: String? = null,
    var name: String? = null,
    var likes: Int? = 0,
    var imageUrl: String? = null,
    var comments: ArrayList<DocumentReference>? = null,
    var directions: ArrayList<String>? = null, // TODO - new
    var ingredients: ArrayList<String>? = null, // TODO - new
    var status: Int? = null // TODO - new
)