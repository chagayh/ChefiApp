package com.example.chefi.database

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

enum class NotificationType{
    LIKE{
        override fun toString(): String {
            return "like your recipe."

        }
    },
    FOLLOW{
        override fun toString(): String {
            return "is now follow you."
        }
    },
    COMMENT{
        override fun toString(): String {
            return "leave a comment."
        }
    },
    TRADE{
        override fun toString(): String {
            return "offer you a trade!"
        }
    },
    DEFAULT{
        override fun toString(): String {
            return "accept your trade!"
        }
    }
}

data class DbNotificationItem (
    var uid: String? = null,
    var creatorRef: DocumentReference? = null,
    var destinationRef: DocumentReference? = null, // no need object
    var recipeRef: DocumentReference? = null, // always relevant
    var offeredRecipeRef: DocumentReference? = null, // relevant if status == 3
    var notificationType: NotificationType = NotificationType.DEFAULT,
    @ServerTimestamp
    var timestamp: Date? = null
){}