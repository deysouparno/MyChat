package com.example.mychat.screens

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.mychat.databinding.FragmentSingnUpBinding
import com.example.mychat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

private const val Tag = "SignUp"

class SignUpFragment : Fragment() {

    private var currentUser: FirebaseUser? = null
    private lateinit var binding: FragmentSingnUpBinding
    private var selectedPhoto: Uri? = null
    private val result = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            selectedPhoto = it
            Glide.with(this)
                .load(it)
                .centerCrop()
                .circleCrop()
                .into(binding.profileImage)

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (FirebaseAuth.getInstance().currentUser != null) {
            findNavController().navigate(
                SignUpFragmentDirections.actionSingnUpFragmentToHomeScreenFragment(getData())
            )
//            FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}").child("isOnline").setValue(true)
        }

        binding = FragmentSingnUpBinding.inflate(inflater)

        Log.d(Tag, "current user is ${FirebaseAuth.getInstance().currentUser?.uid}")

        binding.LoginText.setOnClickListener {
            it.findNavController().navigate(
                SignUpFragmentDirections.actionSingnUpFragmentToLoginFragment()
            )
        }


        binding.registerButton.setOnClickListener {
            if (binding.registerUsername.text.isBlank()
                || binding.registerEmail.text.isBlank()
                || binding.registerUsername.text.isBlank()
            ) {
                Toast.makeText(context, "invalid", Toast.LENGTH_SHORT).show()
            } else if (binding.registerPassword.text.length < 6) {
                Toast.makeText(
                    context,
                    "password must be at least 6 characters",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                signUp(
                    binding.registerEmail.text.toString(),
                    binding.registerPassword.text.toString(),
                    binding.registerUsername.text.toString()
                )
            }
        }


        binding.profileImage.setOnClickListener {
            result.launch("image/*")
        }

        return binding.root
    }

    private fun signUp(email: String, password: String, username: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
//                    Toast.makeText(context, "register successful", Toast.LENGTH_SHORT).show()
                    currentUser = task.result!!.user
                    Log.d(Tag, "registered successfully")
                    uploadDetails(username)
                } else {
                    Toast.makeText(context, "something went wrong", Toast.LENGTH_SHORT).show()
                    Log.d(Tag, "registration failed")
                }
            }
    }

    private fun saveData(uid: String, username: String, profileImg: String) {
        val sharedPref = context?.getSharedPreferences("user", Context.MODE_PRIVATE)
        val editor = sharedPref?.edit()
        editor?.apply {
            putString("username", username)
            putString("uid", uid)
            putString("image", profileImg)
            commit()
        }

    }

    private fun getData(): User {
        val sharedPref = context?.getSharedPreferences("user", Context.MODE_PRIVATE)
        val username = sharedPref?.getString("username", "")?:""
        val uid = sharedPref?.getString("uid", "") ?:""
        val image = sharedPref?.getString("image", "") ?:""
        return User(uid, username, true, image)
    }

    private fun uploadDetails(username: String) {

        if (currentUser == null) {
            Log.d(Tag, "returned as user is null")
            return
        }
        val uid = currentUser!!.uid
        val storageRef = FirebaseStorage.getInstance().getReference("/images/$uid")
        if (selectedPhoto != null) {
            storageRef.putFile(selectedPhoto!!)
                .addOnCompleteListener {
                    var photo = ""
                    if (it.isSuccessful) {
                        Toast.makeText(context, "photo uploaded", Toast.LENGTH_SHORT).show()
                        Log.d(Tag, "photo added successfully")
                    } else {
                        Toast.makeText(context, "photo upload failed", Toast.LENGTH_SHORT).show()
                    }

                    storageRef.downloadUrl.addOnCompleteListener { task2 ->

                        if (task2.isSuccessful) {
                            photo = task2.result.toString()
                            Log.d(Tag, "photo url is ${task2.result}")
                        }
                        Log.d(Tag, "photo url is $photo")

                        val userObj = User(
                            uid = uid,
                            username = username,
                            isOnline = true,
                            profileImg = photo
                        )
                        saveData(uid, username, photo)
                        FirebaseDatabase.getInstance().getReference("/users/$uid").setValue(userObj)
                            .addOnCompleteListener { task3 ->
                                if (task3.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "user added to database",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    findNavController().navigate(
                                        SignUpFragmentDirections.actionSingnUpFragmentToHomeScreenFragment(userObj)
                                    )

                                } else {
                                    Toast.makeText(
                                        context,
                                        "failed to add user",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                }
        }


    }


}