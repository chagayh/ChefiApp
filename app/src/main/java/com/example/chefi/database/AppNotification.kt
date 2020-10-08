package com.example.chefi.database

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class AppNotification (
    var uid: String? = null,
    var creator: DbUser? = null,
    var destinationRef: DocumentReference? = null, // for db only
    var recipe: AppRecipe? = null, // always relevant
    var offeredRecipe: AppRecipe? = null, // relevant if status == 3
    var notificationType: NotificationType = NotificationType.DEFAULT,
    @ServerTimestamp
    var timestamp: Date? = null
)