package com.example.chefi.database

import com.google.firebase.firestore.DocumentReference

data class User (
    var uid: String? = null,
    var email: String? = null,
    var imageUrl: String? = null,       // TODO - set, remove, get
    var name: String? = null,           // TODO - set, remove etc
    var userName: String? = null,
    var aboutMe: String? = null,        // TODO - set, remove etc
    var recipes: ArrayList<DocumentReference>? = null,      // TODO - remove etc
    var notifications: ArrayList<DocumentReference>? = null,
    var favorites: ArrayList<DocumentReference>? = null,    // TODO - remove
    var following: ArrayList<DocumentReference>? = null,    // TODO - un follow, update to my profile when new follow request
    var followers: ArrayList<DocumentReference>? = null
)
