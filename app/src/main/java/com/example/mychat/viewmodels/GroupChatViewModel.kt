package com.example.mychat.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.example.mychat.getUser
import com.example.mychat.models.ChatGroup
import com.example.mychat.models.ChatMessage
import com.example.mychat.models.User
import com.google.firebase.database.*
import kotlinx.coroutines.launch

class GroupChatViewModelFactory(private val id: String, private val currentUser : User) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupChatViewModel::class.java)) {
            return GroupChatViewModel(id, currentUser) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class GroupChatViewModel(private val id: String, private val currentUser : User) : ViewModel() {

    var group: ChatGroup? = ChatGroup()

    val messages = ArrayList<ChatMessage>()

    val members = ArrayList<User>()

    var names = ""

    private val _onGroupDataChange = MutableLiveData(true)
    val onGroupDataChange: LiveData<Boolean>
        get() = _onGroupDataChange

    private val _onAdd = MutableLiveData(true)
    val onAdd: LiveData<Boolean>
        get() = _onAdd

    private val _onRemove = MutableLiveData(true)
    val onRemove: LiveData<Boolean>
        get() = _onRemove

    private fun addListeners() {
        FirebaseDatabase.getInstance().getReference(id)
            .addChildEventListener(chatListener)
        FirebaseDatabase.getInstance().getReference("/groups/$id")
            .addValueEventListener(groupDetailsListener)
    }

    fun removeListeners() {
        FirebaseDatabase.getInstance().getReference(id)
            .removeEventListener(chatListener)
        FirebaseDatabase.getInstance().getReference("/groups/$id")
            .removeEventListener(groupDetailsListener)
    }

    private val groupDetailsListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            group = snapshot.getValue(ChatGroup::class.java)
            if (group != null) {
                _onGroupDataChange.value = !_onGroupDataChange.value!!
                getMembers(group!!.members)
            }
        }

        override fun onCancelled(error: DatabaseError) {}

    }
    private val chatListener = object : ChildEventListener {
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

    fun getMembers(data: String) {

        val list = data.split(" ")
        var count = 0
        list.forEach {

            FirebaseDatabase.getInstance().getReference("/users/$it")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(User::class.java)
                        Log.d("chats", "$user")
                        if (user != null) members.add(user); count++
                        if (count == list.size) getNames()
                    }

                    override fun onCancelled(error: DatabaseError) = Unit

                })
        }

    }

    private fun getNames() {
        var res = "you, "
        var count = 0
        for (i in members.indices) {
            if (members[i].username == currentUser.username) continue
            res += "${members[i].username}, "
            count++
            if (count == 3) break
        }
        names = res
        _onGroupDataChange.value = !_onGroupDataChange.value!!
    }

    fun loadMoreChats(id: String, directory: String) {

    }

    init {

        addListeners()

    }
}