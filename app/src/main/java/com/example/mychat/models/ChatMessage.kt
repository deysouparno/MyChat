package com.example.mychat.models

import androidx.room.Entity
import androidx.room.PrimaryKey


data class ChatMessage(
    val key: String = "",
    val text: String = "",
    val fromPerson: String = "",
    val toPerson: String = "",
    val time: String = ""
)