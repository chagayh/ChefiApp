package com.example.chefi.database

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.DocumentReference

data class DbUser(
    var uid: String? = null,
    var email: String? = null,
    var imageUrl: String? = null,
    var name: String? = null,
    var name_lowerCase: String? = null,
    var userName: String? = null,
    var userName_lowerCase: String? = null,
    var aboutMe: String? = null,
    var recipes: ArrayList<DocumentReference>? = null,
    var notifications: ArrayList<DocumentReference>? = null,
    var favorites: ArrayList<DocumentReference>? = null,
    var following: ArrayList<DocumentReference>? = null,
    var followers: ArrayList<DocumentReference>? = null
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        TODO("recipes"),
        TODO("notifications"),
        TODO("favorites"),
        TODO("following"),
        TODO("followers")
    ) {
    }

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        TODO("Not yet implemented")
    }

    companion object CREATOR : Parcelable.Creator<DbUser> {
        override fun createFromParcel(parcel: Parcel): DbUser {
            return DbUser(parcel)
        }

        override fun newArray(size: Int): Array<DbUser?> {
            return arrayOfNulls(size)
        }
    }
}