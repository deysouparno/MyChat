package com.example.mychat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.mychat.models.User

class MainActivity : AppCompatActivity() {

    private lateinit var user : User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        user = getUser(this)
        if (user.uid.trim() != "") {
            updateStatus(user.uid, "online")
        }
    }
    override fun onStop() {
        super.onStop()
        if (user.uid.trim() != "") {
            updateStatus(user.uid, currentTime())
        }
    }

    override fun onResume() {
        super.onResume()
        if (user.uid.trim() != "") {
            updateStatus(user.uid, "online")
        }
    }


}