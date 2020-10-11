package com.postpc.chefi.database

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ServerTimestamp
import java.util.*
import kotlin.collections.ArrayList

data class DbRecipe(
    var uid: String? = null,
    var description: String? = null,
    var likes: Int? = 0,
    var imageUrl: String? = null,
    var comments: ArrayList<DocumentReference>? = null,
    var directions: ArrayList<String>? = null,
    var ingredients: ArrayList<String>? = null,
    var status: Boolean? = null,
    var owner: DocumentReference? = null,
    @ServerTimestamp
    var timestamp: Date? = null,
    var myReference: DocumentReference? = null,
    var allowedUsers: ArrayList<DocumentReference>? = null,
    var location: String? = null
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        TODO("comments"),
        TODO("directions"),
        TODO("ingredients"),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        TODO("owner"),
        TODO("timestamp"),
        TODO("myReference"),
        TODO("allowedUsers")
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(description)
        parcel.writeValue(likes)
        parcel.writeString(imageUrl)
        parcel.writeValue(status)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DbRecipe> {
        override fun createFromParcel(parcel: Parcel): DbRecipe {
            return DbRecipe(parcel)
        }

        override fun newArray(size: Int): Array<DbRecipe?> {
            return arrayOfNulls(size)
        }
    }
}