package com.example.mychat.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.mychat.Constants.Companion.CHANNEL_ID
import com.example.mychat.Constants.Companion.GROUP
import com.example.mychat.MainActivity
import com.example.mychat.R
import com.example.mychat.getUser
import com.example.mychat.screens.topic
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random


class FirebaseService : FirebaseMessagingService() {

    companion object {
        var sharedPref: SharedPreferences? = null
        var token: String?
            get() = sharedPref?.getString("token", "")
            set(value) {
                sharedPref?.edit()?.putString("token", value)?.apply()
            }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
//        sharedPref = baseContext?.getSharedPreferences("user", Context.MODE_PRIVATE)
        token = p0
//        FirebaseDatabase.getInstance().getReference("/users/${getUser(baseContext).uid}")
//            .child("token").setValue(token)
    }

    override fun onMessageReceived(msg: RemoteMessage) {
        super.onMessageReceived(msg)

        if (msg.data["sender"] != null && msg.data["sender"]!! == getUser(baseContext).uid) return

        val intent = Intent(this, MainActivity::class.java)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val id = Random.nextInt()

        Log.d("notification", "msg sender is ${msg.senderId}\n msg data is ${msg.data}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel(
            notificationManager
        )

        if (msg.data["code"] == GROUP) {
            if (msg.data["id"] != null) {
                Log.d("notification", "subscribed to new group")
                FirebaseMessaging.getInstance().subscribeToTopic(msg.data["id"]!!)
            }
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_ONE_SHOT)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(msg.data["tittle"])
            .setContentText(msg.data["message"])
            .setSmallIcon(R.drawable.chat)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(id, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "my channel"
        val channel = NotificationChannel(CHANNEL_ID, channelName, IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
    }


}