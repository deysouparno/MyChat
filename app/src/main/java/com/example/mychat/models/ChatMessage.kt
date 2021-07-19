package com.example.mychat.models

import androidx.room.Entity
import androidx.room.PrimaryKey


data class ChatMessage(
    val key: String = "",
    val text: String = "",
    val fromPerson: String = "",
    val time: String = "",
    val type : Int = 0,
    var link : String = ""
)