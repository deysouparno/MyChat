package com.example.mychat.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mychat.models.HomeScreenUser
import com.example.mychat.models.User
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

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


    private fun addListener() {
        Log.d("HomeScreen", "listener added")
        FirebaseDatabase.getInstance().getReference("/chats${user.uid}")
            .addChildEventListener(listener)
    }


    private val listener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val homeScreenUser = snapshot.getValue(HomeScreenUser::class.java)
            if (homeScreenUser != null) {
                Log.d("HomeScreen", "new homeUser is ${homeScreenUser.username}")
                homeScreenUsers.add(homeScreenUser)
                _onChanged.value = !_onChanged.value!!
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
            Log.d("HomeScreen", "child removed")
            val homeScreenUser = snapshot.getValue(HomeScreenUser::class.java)
            if (homeScreenUser != null) {
                homeScreenUsers.remove(homeScreenUser)
            }
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d("HomeScreen", "child moved")
        }

        override fun onCancelled(error: DatabaseError) {
            Log.d("HomeScreen", "error")
        }

    }

    init {
        Log.d("HomeScreen", "viewmodel created")
        addListener()
    }
}