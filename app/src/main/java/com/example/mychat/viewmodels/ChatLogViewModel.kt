package com.example.mychat.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.example.mychat.models.ChatMessage
import com.example.mychat.models.User
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class ChatLogViewModelFactory(private val directory: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatLogViewModel::class.java)) {
            return ChatLogViewModel(directory) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ChatLogViewModel(private val directory: String) : ViewModel() {

    private val userData = MutableLiveData(User())

    private val _onAdd = MutableLiveData(true)
    val onAdd: LiveData<Boolean>
        get() = _onAdd

    private val _onRemove = MutableLiveData(true)
    val onRemove: LiveData<Boolean>
        get() = _onAdd

    val messages = ArrayList<ChatMessage>()

    private fun listenMessages() {
        FirebaseDatabase.getInstance().getReference(directory)
            .addChildEventListener(listener)
    }

    fun removeListener() {
        FirebaseDatabase.getInstance().getReference(directory)
            .removeEventListener(listener)
    }


    private val listener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val msg = snapshot.getValue(ChatMessage::class.java)
            if (msg != null) {
                messages.add(msg)
                _onAdd.value = !_onAdd.value!!
            }
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            val msg = snapshot.getValue(ChatMessage::class.java)
            if (msg != null) {
                messages.remove(msg)
                _onRemove.value = !_onRemove.value!!
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {}
    }

    private val userListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            userData.value = snapshot.getValue(User::class.java)
        }
        override fun onChildRemoved(snapshot: DataSnapshot) {}
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {}

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            if (previousChildName == null) {

            }
        }

    }

    init {
        Log.d("newchat", "chatlog viewmodel created")
        listenMessages()
    }
}