package com.example.mychat.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.mychat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel() : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Empty)
    val signInUiState : StateFlow<LoginUiState> = _uiState

    var user : User? = null
    var directory = ""

    fun signIn(email: String, password: String) {
        _uiState.value = LoginUiState.Loading("Signing In...", 1)
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    directory = "/users/${task.result!!.user!!.uid.trim()}"
                    Log.d("Login", "directory is --$directory--")
                    _uiState.value = LoginUiState.Loading("getting account data...", 2)
                } else {
                    _uiState.value = LoginUiState.Error("Failed to SignIn", 1)
                }
            }
    }

    fun getData() {
        val ref = FirebaseDatabase.getInstance().getReference(directory)
        ref.get()
            .addOnCompleteListener { task2 ->
                if (task2.isSuccessful && task2.result != null) {
                    user = task2.result!!.getValue(User::class.java)
                    _uiState.value = LoginUiState.Success
                } else {
                    _uiState.value = LoginUiState.Error("Failed to get user data", 2)
                }
            }
    }

    sealed class LoginUiState {
        object Empty : LoginUiState()
        object Success : LoginUiState()
        data class Loading(val msg : String, val code : Int) : LoginUiState()
        data class Error(val msg : String, val code : Int) : LoginUiState()
    }
}