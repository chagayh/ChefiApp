package com.example.chefi.database

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot

data class Recipe(
    var uid: String? = null,
    var name: String? = null,
    var likes: Int? = 0,
    var imageUrl: String? = null,
    var comments: ArrayList<DocumentReference>? = null,
    var directions: ArrayList<String>? = null, // TODO - new
    var ingredients: ArrayList<String>? = null, // TODO - new
    var status: Int? = null, // TODO - new
    var owner: DocumentReference? = null, // TODO - new
    var timestamp: String? = null // TODO - new
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        TODO("comments"),
        TODO("directions"),
        TODO("ingredients"),
        parcel.readValue(Int::class.java.classLoader) as? Int
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(name)
        parcel.writeValue(likes)
        parcel.writeString(imageUrl)
        parcel.writeValue(status)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Recipe> {
        override fun createFromParcel(parcel: Parcel): Recipe {
            return Recipe(parcel)
        }

        override fun newArray(size: Int): Array<Recipe?> {
            return arrayOfNulls(size)
        }
    }
}