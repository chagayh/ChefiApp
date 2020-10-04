package com.example.chefi.database

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Comment (
    var userName: String? = null,
    var name: String? = null,
    var commentContent: String? = null,
    @ServerTimestamp
    var timestamp: Date? = null
)