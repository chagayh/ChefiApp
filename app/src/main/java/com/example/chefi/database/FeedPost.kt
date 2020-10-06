package com.example.chefi.database

import java.util.*

data class FeedPost (
    var uid : String? = null,
    var dbRecipe: DbRecipe? = null,
    var date: Date? = null
)