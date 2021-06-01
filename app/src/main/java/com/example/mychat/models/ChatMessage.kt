package com.example.mychat.models


data class ChatMessage(
    val key: String,
    val text: String,
    val fromPerson: String,
    val toPerson: String,
    val time: Long
) {
    constructor():this("", "", "", "", 1L)
}