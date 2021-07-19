package com.example.mychat.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChatGroup(
    val id : String = "",
    val name: String = "",
    val description : String = "",
    val image: String = "",
    val members : String = ""
) : Parcelable{
}