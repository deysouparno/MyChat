package com.example.mychat

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mychat.models.User
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {

    var email = ""
    var password = ""
    var image : Uri? = null
    var toPerson = User()
    val send = MutableLiveData(false)
}