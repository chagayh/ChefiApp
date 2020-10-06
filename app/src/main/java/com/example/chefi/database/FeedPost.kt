package com.example.chefi.database

import com.google.firebase.firestore.DocumentReference
import java.util.*

data class FeedPost (
    var uid : String? = null,
    var dbRecipe: DocumentReference? = null,
    var date: Date? = null,
    var recipeId: String? = null
)