package com.example.chefi.database

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

enum class NotificationType{
    LIKE{
        override fun toString(): String {
            return "%s like your recipe_profile."

        }
    },
    FOLLOW{
        override fun toString(): String {
            return "%s is now follow you."
        }
    },
    COMMENT{
        override fun toString(): String {
            return "%s leave a comment: %s"
        }
    },
    DEFAULT{
        override fun toString(): String {
            return ""
        }
    }
}

data class DbNotificationItem (
    var userId: String? = null,
    var notificationContent: String? = null,
    var notificationType: NotificationType = NotificationType.DEFAULT,
    @ServerTimestamp
    var timestamp: Date? = null
){
//    override fun toString(): String {
//        return when (notificationType) {
//            NotificationType.DEFAULT -> {
//                super.toString()
//            }
//            NotificationType.COMMENT -> {
//                String.format(notificationType.toString(), user, notificationContent)
//            }
//            else -> {
//                String.format(notificationType.toString(), user)
//            }
//        }
//    }
}