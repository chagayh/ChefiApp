package com.example.chefi

import com.google.firebase.firestore.DocumentReference

data class User (
    var uid: String? = null,
    var email: String? = null,
    var name: String? = null,
    var description: String? = null,
    var recipes: ArrayList<DocumentReference>? = null,
    var following: ArrayList<DocumentReference>? = null,     // users id's
    var followers: ArrayList<DocumentReference>? = null      // users id's
)
