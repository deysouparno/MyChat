package com.example.mychat.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mychat.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NewChatViewModelFactory(private val username : String) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewChatViewModel::class.java)) {
            return NewChatViewModel(username) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class NewChatViewModel(private val username : String) : ViewModel() {
    private val _onChange = MutableLiveData(true)
    val onChange: LiveData<Boolean>
        get() = _onChange

    val users = ArrayList<User>()
    private val directory = "/users"


    private fun getUsers() {
        FirebaseDatabase.getInstance().getReference(directory)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        snapshot.children.forEach {
                            val user = it.getValue(User::class.java)
                            if (user != null && user.username != username) {
                                users.add(user)
                            }
                        }
                        _onChange.value = !_onChange.value!!
                    }
                }

                override fun onCancelled(error: DatabaseError) { }

            })
    }
//    private val listener = object : ChildEventListener {
//        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//            val user = snapshot.getValue(User::class.java)
//            if (user != null && user.username != username) {
//                users.add(user)
//                _onChange.value = !_onChange.value!!
//            }
//        }
//
//        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//            val user = snapshot.getValue(User::class.java)
//            if (user != null) {
//                var index = 0
//                for (i in users.indices) {
//                    if (users[i].username == user.username) {
//                        index = i
//                        break
//                    }
//                }
//                users[index] = user
//                _onChange.value = !_onChange.value!!
//            }
//        }
//
//        override fun onChildRemoved(snapshot: DataSnapshot) {
//            val user = snapshot.getValue(User::class.java)
//            if (user != null) {
//                users.remove(user)
//            }
//        }
//
//        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
//
//        override fun onCancelled(error: DatabaseError) {}
//
//    }

    init {
//        Log.d("newchat", "viewmodel created")
//        addListener()
        getUsers()
    }

}