package com.example.mychat.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HomeScreenUser(
    val uid: String = "",
    val username: String = "",
    val sender: String = "",
    val profileImg: String = "",
    val lastMsg: String = "",
    val isGroup: Boolean = false
): Parcelable {
    init {

    }
}