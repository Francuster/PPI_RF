package com.example.myapplication.model

import android.os.Parcel
import android.os.Parcelable

data class Licencia(
    var _id: String,
    var fechaDesde: String,
    var fechaHasta: String,
    var userId: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(_id)
        parcel.writeString(fechaDesde)
        parcel.writeString(fechaHasta)
        parcel.writeString(userId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Licencia> {
        override fun createFromParcel(parcel: Parcel): Licencia {
            return Licencia(parcel)
        }

        override fun newArray(size: Int): Array<Licencia?> {
            return arrayOfNulls(size)
        }
    }
}
