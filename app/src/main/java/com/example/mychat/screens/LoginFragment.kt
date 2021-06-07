package com.example.mychat.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.mychat.MainActivity
import com.example.mychat.databinding.FragmentLoginBinding
import com.example.mychat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater)

        binding.SigninText.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToSignUpFragment()
            it.findNavController().navigate(action)
        }

        binding.loginButton.setOnClickListener {
            if (binding.loginEmail.text.isBlank()
                || binding.loginPassword.text.isBlank()
            ) {
                Toast.makeText(context, "invalid credentials", Toast.LENGTH_SHORT).show()
            } else if (binding.loginPassword.text.length < 6) {
                Toast.makeText(context, "invalid credentials", Toast.LENGTH_SHORT).show()
            } else {
                (context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(it.windowToken, 0)
                it.clearFocus()
                binding.loginProgressBar.visibility = View.VISIBLE
                login(binding.loginEmail.text.toString(), binding.loginPassword.text.toString())

            }
        }

        return binding.root
    }

    private fun login(email: String, password: String) {

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.loginProgressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    Log.d("Login", "Successfully logged in")
                    Toast.makeText(context, "Successfully logged in", Toast.LENGTH_SHORT).show()
//                    Log.d("Login", "task result is ${task.result!!.user!!.uid}")
                    val result = "/users/${task.result!!.user!!.uid.trim()}"
                    goToHomeScreen(result)
                } else if (task.isCanceled) {
                    Toast.makeText(context, "wrong email password", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "${it.message}", Toast.LENGTH_SHORT).show()
                Log.d("Login", "${it.message}")
            }

    }

    private fun goToHomeScreen(directory: String) {
        val ref = FirebaseDatabase.getInstance().getReference(directory)
        ref.get()
            .addOnCompleteListener { task2 ->
                Log.d("Login", "task2 complete")
                if (task2.isSuccessful && task2.result != null) {
                    val userObj = task2.result!!.getValue(User::class.java)!!
                    saveData(userObj.uid, userObj.username, userObj.profileImg)
//
                    startActivity(Intent(context, MainActivity::class.java))
                    activity?.finish()
                } else {
                    Log.d("Login", "Login e problem bc")
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
        }?.apply()

    }


}