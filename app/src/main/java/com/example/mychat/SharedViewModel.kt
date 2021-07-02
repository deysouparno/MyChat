package com.example.mychat

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.mychat.models.User
import com.google.firebase.database.FirebaseDatabase

class SharedViewModel : ViewModel() {

    var email = ""
    var password = ""

    fun setSecurity(context: Context?, flag : Boolean) {
        val sharedPref = context?.getSharedPreferences("user", Context.MODE_PRIVATE)
        val editor = sharedPref?.edit()
        editor?.apply {
            putBoolean("secured", flag)
                commit()
        }
    }

    fun checkSecurity(context: Context?) : Boolean? {
        val sharedPref = context?.getSharedPreferences("user", Context.MODE_PRIVATE)
        return sharedPref?.getBoolean("secured", false)
    }

    fun saveData(
        context: Context?,
        uid: String,
        username: String,
        profileImg: String,
        password: String,
        email: String
    ) {
        val sharedPref = context?.getSharedPreferences("user", Context.MODE_PRIVATE)
        val editor = sharedPref?.edit()
        editor?.apply {
            putString("username", username)
            putString("uid", uid)
            putString("image", profileImg)
            putString("email", email)
            putString("password", password)
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
        return User(uid, username, "true", image)
    }

    fun deleteData(context: Context?) {
        val sharedPref = context?.getSharedPreferences("user", Context.MODE_PRIVATE)
        val editor = sharedPref?.edit()
        editor?.apply {
            remove("username")
            remove("uid")
            remove("image")
            remove("password")
        }?.apply()

    }

    fun updateStatus(uid : String, status : String) {
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.child("online").setValue(status)
    }

}