package com.example.chefi.database

import android.net.Uri

data class Recipe(
    var uid: String? = null,
    var name: String? = null,
    var likes: Int? = 0,
    var imageUrl: String? = null
)