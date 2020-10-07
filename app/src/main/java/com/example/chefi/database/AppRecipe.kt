package com.example.chefi.database

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ServerTimestamp
import java.util.*
import kotlin.collections.ArrayList

data class AppRecipe (
    var uid: String? = null,
    var description: String? = null,
    var likes: Int? = 0,
    var imageUrl: String? = null,
    var comments: ArrayList<Comment>? = null,
    var directions: ArrayList<String>? = null,
    var ingredients: ArrayList<String>? = null,
    var status: Int? = null,    // trade = 3
    var owner: DbUser? = null,
    @ServerTimestamp
    var timestamp: Date? = null,
    var myReference: DocumentReference? = null,
    var allowedUsers: ArrayList<DocumentReference>? = null
): Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        TODO("comments"),
        TODO("directions"),
        TODO("ingredients"),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readParcelable(AppRecipe::class.java.classLoader),
        TODO("timestamp")
    ) {}

    val TRADE_STATUS = 3

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(description)
        parcel.writeValue(likes)
        parcel.writeString(imageUrl)
        parcel.writeValue(status)
        parcel.writeParcelable(owner, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AppRecipe> {
        override fun createFromParcel(parcel: Parcel): AppRecipe {
            return AppRecipe(parcel)
        }

        override fun newArray(size: Int): Array<AppRecipe?> {
            return arrayOfNulls(size)
        }
    }
}