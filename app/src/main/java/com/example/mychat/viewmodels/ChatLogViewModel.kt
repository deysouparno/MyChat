package com.example.mychat.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.example.mychat.models.ChatMessage
import com.example.mychat.models.User
import com.google.firebase.database.*
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

    var userData : User?  = User()

    private val _onUserDataChange = MutableLiveData(true)
    val onUserDataChange: LiveData<Boolean>
        get() = _onUserDataChange

    private val _onAdd = MutableLiveData(true)
    val onAdd: LiveData<Boolean>
        get() = _onAdd

    private val _onRemove = MutableLiveData(true)
    val onRemove: LiveData<Boolean>
        get() = _onRemove

    val messages = ArrayList<ChatMessage>()

    private fun listenMessages() {
        FirebaseDatabase.getInstance().getReference(directory)
            .addChildEventListener(listener)
        FirebaseDatabase.getInstance().getReference("/user")
    }

    fun removeListener() {
        FirebaseDatabase.getInstance().getReference(directory)
            .removeEventListener(listener)
    }

    fun listenUser(id : String) {
        FirebaseDatabase.getInstance().getReference("/users/$id")
            .addValueEventListener(userListener)
    }

    fun removeUserListener(id : String) {
        FirebaseDatabase.getInstance().getReference("/users/$id")
            .removeEventListener(userListener)
    }


    private val listener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val msg = snapshot.getValue(ChatMessage::class.java)
            Log.d("chats", "msg is ${msg?.link}")
            if (msg != null) {
                Log.d("chats", "add called")
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

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) = Unit
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit
        override fun onCancelled(error: DatabaseError) = Unit
    }

    private val userListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            userData = snapshot.getValue(User::class.java)
            if (userData != null) {
                _onUserDataChange.value = !_onUserDataChange.value!!
            }
        }

        override fun onCancelled(error: DatabaseError) {}

    }


    init {
        Log.d("newchat", "chatlog viewmodel created")
        listenMessages()
    }
}