package com.example.chefi.database

import com.google.firebase.firestore.DocumentReference

data class User (
    var uid: String? = null,
    var email: String? = null,
    var imageUrl: String? = null,
    var name: String? = null,
    var userName: String? = null,
    var aboutMe: String? = null,
    var recipes: ArrayList<DocumentReference>? = null,
    var notifications: ArrayList<DocumentReference>? = null,
    var favorites: ArrayList<DocumentReference>? = null,
    var following: ArrayList<DocumentReference>? = null,
    var followers: ArrayList<DocumentReference>? = null
)
