package com.example.chefi

import android.net.Uri
import com.google.firebase.firestore.CollectionReference

data class Recipe(
    var uid: String? = null,
    var name: String? = null,
    var comments: CollectionReference? = null,
    var likes: Int = 0,
    var imageUri: Uri? = null
)