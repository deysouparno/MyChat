package com.example.mychat.models


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(val uid: String,
           val username: String,
           var isOnline: Boolean = false,
           val profileImg: String = ""): Parcelable {
               constructor():this("", "", profileImg ="" )
           }
