package com.example.mychat

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.mychat.models.PushNotification
import com.example.mychat.models.User
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Constants {
    companion object {
        const val CHAT = "CHAT"
        const val GROUP = "GROUP"
        const val TEXT_MSG = 10
        const val IMG_MSG = 20
        const val CHANNEL_ID = "my_channel"
        const val BASE_URL = "https://fcm.googleapis.com"
        const val SERVER_KEY =
            "AAAAM2S17ao:APA91bHeVjP80Czi_A_kF7JDKJlRv5mhEr7PMtnILqhs5xWylCOFQOnaXZqJWaWZ7-nEHIyAaKJAz2VXMaTZ5bI94Gmbbmj8qJUAkSdgRqhe0kfjq7cvaqTFW8hClBapQO0Uz2iHhmsK"
        const val CONTENT_TYPE = "application/json"
    }

}

//@RequiresApi(Build.VERSION_CODES.O)
//private fun createNotificationChannel(notificationManager: NotificationManager) {
//    val channelName = "myChat"
//    val channel = NotificationChannel(Constants.CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
//        description = "myChat notification description"
//    }
//    notificationManager.createNotificationChannel(channel)
//}

fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
    try {
        val response = RetrofitInstance.api.postNotification(notification)
        if (response.isSuccessful) {
            Log.d("notification", "notification sent")
        } else {
            Log.d("notification", "failed response -> ${response.errorBody().toString()}")
        }
    }
    catch (e : Exception) {
        Log.d("notification", "notification not send due to ${e.message}")
    }
}

fun ImageView.loadImage(uri: Uri? = null, url : String? = null) {

    val myOptions = RequestOptions()
        .override(this.width, this.height)
        .centerCrop()
    if (uri == null && url == null) return

    else if (uri != null) {

        Glide
            .with(this.context)
            .load(uri)
            .apply(myOptions)
            .into(this)
    } else {

        Glide
            .with(this.context)
            .load(url)
            .apply(myOptions)
            .into(this)
    }

}


fun currentTime(): String {
    val calendar = Calendar.getInstance()
    return SimpleDateFormat("dd mm yyyy hh mm a").format(calendar.time)
}

fun getCurrentTime(time: String, code: Int): String {

    if (time == "online" || time == "") {
        return time
    }
    val calendar = Calendar.getInstance()
    val timeNow = SimpleDateFormat("dd mm yyyy hh mm a").format(calendar.time).split(" ")
    val givenTime = time.split(" ")
    return if (timeNow[0] == givenTime[0] || code == 2) {
        "${givenTime[3]}:${givenTime[4]} ${givenTime[5]}"
    } else if (timeNow[0].toInt() - givenTime[0].toInt() == 1 && code == 1) {
        "yesterday"
    } else {
        "${givenTime[0]}/${givenTime[1]}/${givenTime[2]}"
    }
}

fun getDay(time: String): Int {
    return time.split(" ")[0].toInt()
}

fun setSecurity(context: Context?, flag: Boolean) {
    val sharedPref = context?.getSharedPreferences("user", Context.MODE_PRIVATE)
    val editor = sharedPref?.edit()
    editor?.apply {
        putBoolean("secured", flag)
        commit()
    }
}

fun checkSecurity(context: Context?): Boolean? {
    val sharedPref = context?.getSharedPreferences("user", Context.MODE_PRIVATE)
    return sharedPref?.getBoolean("secured", false)
}

fun saveData(
    context: Context?,
    uid: String,
    username: String,
    profileImg: String,
    password: String,
    email: String,
    status: String
) {
    val sharedPref = context?.getSharedPreferences("user", Context.MODE_PRIVATE)
    val editor = sharedPref?.edit()
    editor?.apply {
        putString("username", username)
        putString("uid", uid)
        putString("image", profileImg)
        putString("email", email)
        putString("password", password)
        putString("status", status)
        commit()
    }

}

fun getPassword(context: Context?): String? {
    val sharedPref = context?.getSharedPreferences("user", Context.MODE_PRIVATE)
    return sharedPref?.getString("password", null)
}

fun getEmail(context: Context?): String? {
    val sharedPref = context?.getSharedPreferences("user", Context.MODE_PRIVATE)
    return sharedPref?.getString("email", null)
}

fun getUser(context: Context?): User {
    val sharedPref = context?.getSharedPreferences("user", Context.MODE_PRIVATE)
    val username = sharedPref?.getString("username", "") ?: ""
    val uid = sharedPref?.getString("uid", "") ?: ""
    val image = sharedPref?.getString("image", "") ?: ""
    val status = sharedPref?.getString("status", "") ?: ""
    return User(uid, username, "true", image, status)
}

fun updateUserStatus(context: Context?, status: String) {
    val sharedPref = context?.getSharedPreferences("user", Context.MODE_PRIVATE)
    sharedPref?.edit()?.putString("status", status)?.apply()
}

fun deleteData(context: Context?) {
    val sharedPref = context?.getSharedPreferences("user", Context.MODE_PRIVATE)
    val editor = sharedPref?.edit()
    editor?.apply {
        remove("username")
        remove("uid")
        remove("image")
        remove("password")
        remove("status")
    }?.apply()

}

fun updateStatus(uid: String, status: String) {
    GlobalScope.launch {
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.child("online").setValue(status)
    }
}

