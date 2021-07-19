package com.example.mychat.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.example.mychat.models.ChatMessage
import com.example.mychat.models.HomeScreenUser
import com.example.mychat.models.User
import com.google.firebase.database.*
import kotlinx.coroutines.launch

class HomeViewModelFactory(private val currentUser: User) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeScreenViewModel::class.java)) {
            return HomeScreenViewModel(currentUser) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class HomeScreenViewModel(private val user: User) : ViewModel() {
    val homeScreenUsers = ArrayList<HomeScreenUser>()

    private val _onChanged = MutableLiveData(true)
    val onChanged: LiveData<Boolean>
        get() = _onChanged

    private val _onAdded = MutableLiveData(true)
    val onAdded: LiveData<Boolean>
        get() = _onAdded


    private fun addListener() {
        FirebaseDatabase.getInstance().getReference("/chats${user.uid}")
            .addChildEventListener(listener)
    }


    private val listener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val homeScreenUser = snapshot.getValue(HomeScreenUser::class.java)
            if (homeScreenUser != null) {
                homeScreenUsers.add(homeScreenUser)
                _onAdded.value = !_onAdded.value!!
                addChatListeners(homeScreenUser)
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            val homeScreenUser = snapshot.getValue(HomeScreenUser::class.java)
            if (homeScreenUser != null) {
                var index = 0
                for (i in homeScreenUsers.indices) {
                    if (homeScreenUsers[i].username == homeScreenUser.username) {
                        index = i
                        break
                    }
                }
                homeScreenUsers.removeAt(index)
                homeScreenUsers.add(homeScreenUser)
                _onChanged.value = !_onChanged.value!!
            }
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            val homeScreenUser = snapshot.getValue(HomeScreenUser::class.java)
            if (homeScreenUser != null) {
                homeScreenUsers.remove(homeScreenUser)
                _onChanged.value = !_onChanged.value!!
            }
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit

        override fun onCancelled(error: DatabaseError) = Unit

    }

    fun addChatListeners(homeUser : HomeScreenUser) {
//        Log.d("chats", "when demo called homscreenusers size -> ${homeScreenUsers.size}")

//            Log.d("chats", "listener added for ${homeUser.username}\n and channel is ${homeUser.channel}")
            FirebaseDatabase.getInstance().getReference("/${homeUser.channel}").limitToLast(1)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val temp = arrayListOf<ChatMessage>()
                        snapshot.children.forEach {
                            val item = it.getValue(ChatMessage::class.java)
                            if (item != null) temp.add(item)
                        }
                        updateLastMessage(homeUser, temp[0])
//                        Log.d("chats", "last msg is ${temp[0].text}\n and chatmessage is is ${temp[0]}")
                    }

                    override fun onCancelled(error: DatabaseError) = Unit

                })

    }

    private fun updateLastMessage(hmUser: HomeScreenUser, msg : ChatMessage) {
        val text = if (msg.text == "") "image" else msg.text
        viewModelScope.launch {
            FirebaseDatabase.getInstance().getReference("chats${user.uid}/${hmUser.uid}")
                .child("lastMsg").setValue(text)
        }
    }

    init {
        addListener()
    }
}