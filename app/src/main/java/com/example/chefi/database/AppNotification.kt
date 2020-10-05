package com.example.chefi.database

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class AppNotification (
    var dbUser: DbUser? = null,
    var notificationContent: String? = null,
    var notificationType: NotificationType = NotificationType.DEFAULT,
    @ServerTimestamp
    var timestamp: Date? = null
)