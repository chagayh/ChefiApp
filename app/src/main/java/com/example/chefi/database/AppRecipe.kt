package com.example.chefi.database

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ServerTimestamp
import java.util.*
import kotlin.collections.ArrayList

data class AppRecipe (
    var uid: String? = null,
    var description: String? = null,
    var likes: Int? = 0,
    var imageUrl: String? = null,
    var comments: ArrayList<DocumentReference>? = null,
    var directions: ArrayList<String>? = null,
    var ingredients: ArrayList<String>? = null,
    var status: Int? = null,    // trade = 3
    var owner: DbUser? = null,
    @ServerTimestamp
    var timestamp: Date? = null
)