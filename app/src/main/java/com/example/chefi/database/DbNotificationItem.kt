package com.example.chefi.database

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

enum class NotificationType{
    LIKE{
        override fun toString(): String {
            return "%s like your recipe_profile"

        }
    },
    FOLLOW{
        override fun toString(): String {
            return "%s is now follow you"
        }
    },
    COMMENT{
        override fun toString(): String {
            return "%s leave a comment"
        }
    },
    TRADE{
        override fun toString(): String {
            return "%s offer you a trade!"
        }
    },
    DEFAULT{
        override fun toString(): String {
            return ""
        }
    }
}

data class DbNotificationItem (
    var uid: String? = null,
    var userId: String? = null,
    var notificationContent: String? = null,
    var notificationType: NotificationType = NotificationType.DEFAULT,
    @ServerTimestamp
    var timestamp: Date? = null
){}