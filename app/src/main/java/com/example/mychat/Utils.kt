package com.example.mychat

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar

fun currentTime(): String {
    val calendar = Calendar.getInstance()
    return SimpleDateFormat("dd mm yyyy hh mm a").format(calendar.time)
}

fun getCurrentTime(time : String, code : Int) : String {
    val calendar = Calendar.getInstance()
    val timeNow = SimpleDateFormat("dd mm yyyy hh mm a").format(calendar.time) .split(" ")
    val givenTime = time.split(" ")
    return if (timeNow[0] == givenTime[0] || code == 2) {
        "${givenTime[3]}:${givenTime[4]} ${givenTime[5]}"
    }
    else if (timeNow[0].toInt() - givenTime[0].toInt() == 1 && code == 1) {
        "yesterday"
    }
    else {
        "${givenTime[0]}/${givenTime[1]}/${givenTime[2]}"
    }
}