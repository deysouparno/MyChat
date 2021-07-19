package com.example.mychat.models

data class NotificationData(
    val tittle : String,
    val message: String,
    val sender : String,
    val code : String,
    val id : String = ""
)