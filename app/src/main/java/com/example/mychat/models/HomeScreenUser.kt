package com.example.mychat.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HomeScreenUser(
    val username: String,
    val profileImg: String = "",
    val lastMsg: String
): Parcelable {
    constructor(): this("", "", "")
}