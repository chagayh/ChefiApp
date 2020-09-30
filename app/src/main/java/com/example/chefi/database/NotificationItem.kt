package com.example.chefi.database

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

data class NotificationItem(
    var userName: String? = null,
    var notificationContent: String? = null,
    var userImageUrl: String? = null,
    var notificationType: NotificationType = NotificationType.DEFAULT
){
    override fun toString(): String {
        return when (notificationType) {
            NotificationType.DEFAULT -> {
                super.toString()
            }
            NotificationType.COMMENT -> {
                String.format(notificationType.toString(), userName, notificationContent)
            }
            else -> {
                String.format(notificationType.toString(), userName)
            }
        }
    }
}