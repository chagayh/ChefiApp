package com.example.chefi

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.lang.ref.Reference

data class Recipe (
    var uid: String,
    var comments: Reference<String>,
    var likes: Int = 0
)