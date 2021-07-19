package com.example.mychat.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mychat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class SignUpViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<SignInUiState>(SignInUiState.Empty)
    val signInUiState : StateFlow<SignInUiState> = _uiState

    var imgUrl = ""
    var uid = ""

    fun signUp(email: String, password: String) {

        viewModelScope.launch {

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        uid = task.result!!.user!!.uid
                        _uiState.value = SignInUiState.Registered
                    } else {
                        _uiState.value = SignInUiState.Error("registration error", 1)
                    }
                }
        }

    }

    fun uploadImage(img : Uri?, uid : String) {
        if (img != null) {
            val storageRef = FirebaseStorage.getInstance().getReference("/images/$uid")
            viewModelScope.launch(Dispatchers.IO) {
                storageRef.putFile(img)
                    .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        storageRef.downloadUrl.addOnSuccessListener {
                            imgUrl = it.toString()
                            _uiState.value = SignInUiState.ImgUploaded
                        }
                    } else {
                        _uiState.value = SignInUiState.Error("Image upload error", 2)
                    }
                }
            }
        }
    }

    fun uploadUserData(user : User) {
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseDatabase.getInstance().getReference("/users/${user.uid}").setValue(user)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _uiState.value = SignInUiState.Success
                    } else {
                        _uiState.value = SignInUiState.Error("Error adding user to database", 3)
                    }
                }
        }
    }


    sealed class SignInUiState {
        object Success : SignInUiState()
        data class Error(val msg : String, val code : Int) : SignInUiState()
        data class Loading(val msg : String, val code : Int) : SignInUiState()
        object ImgUploaded : SignInUiState()
        object Registered : SignInUiState()
        object Empty : SignInUiState()
    }
}