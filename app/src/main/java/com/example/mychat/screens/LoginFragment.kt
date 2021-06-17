package com.example.mychat.screens

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.mychat.SharedViewModel
import com.example.mychat.databinding.FragmentLoginBinding
import com.example.mychat.viewmodels.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collect


class LoginFragment : Fragment() {

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val viewModel: LoginViewModel by viewModels()
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
            if (binding.loginEmail.text!!.isBlank()
                || binding.loginPassword.text!!.isBlank()
            ) {
                Toast.makeText(context, "invalid credentials", Toast.LENGTH_SHORT).show()
            } else if (binding.loginPassword.text!!.length < 6) {
                Toast.makeText(context, "invalid credentials", Toast.LENGTH_SHORT).show()
            } else {
                (context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(it.windowToken, 0)
                binding.root.clearFocus()
                binding.loginProgressBar.visibility = View.VISIBLE
                viewModel.signIn(
                    binding.loginEmail.text.toString(),
                    binding.loginPassword.text.toString()
                )
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.signInUiState.collect {
                when (it) {
                    is LoginViewModel.LoginUiState.Loading -> {
                        Log.d("Login", it.msg)
                        if (it.code == 2) {
                            viewModel.getData()
                        }
                    }

                    is LoginViewModel.LoginUiState.Error -> {
                        binding.loginProgressBar.isVisible = false
                        Log.d("Login", it.msg)
                        FirebaseAuth.getInstance().signOut()
                        sharedViewModel.deleteData(context)
                    }

                    is LoginViewModel.LoginUiState.Success -> {
                        sharedViewModel.saveData(
                            context,
                            viewModel.user!!.uid,
                            viewModel.user!!.username,
                            viewModel.user!!.profileImg,
                            binding.loginPassword.text!!.toString(),
                            binding.loginEmail.text!!.toString()
                        )

                        findNavController().navigate(
                            LoginFragmentDirections.actionLoginFragmentToHomeScreenFragment()
                        )
                    }
                    else -> { }
                }
            }
        }

        return binding.root
    }

//    private fun login(email: String, password: String) {
//
//        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
//            .addOnCompleteListener { task ->
//                binding.loginProgressBar.visibility = View.GONE
//                if (task.isSuccessful) {
//                    Log.d("Login", "Successfully logged in")
//                    Toast.makeText(context, "Successfully logged in", Toast.LENGTH_SHORT).show()
////                    Log.d("Login", "task result is ${task.result!!.user!!.uid}")
//                    val result = "/users/${task.result!!.user!!.uid.trim()}"
//                    goToHomeScreen(result)
//                } else if (task.isCanceled) {
//                    Toast.makeText(context, "wrong email password", Toast.LENGTH_SHORT).show()
//                }
//            }
//            .addOnFailureListener {
//                Toast.makeText(context, "${it.message}", Toast.LENGTH_SHORT).show()
//                Log.d("Login", "${it.message}")
//            }
//
//    }
//
//    private fun goToHomeScreen(directory: String) {
//        val ref = FirebaseDatabase.getInstance().getReference(directory)
//        ref.get()
//            .addOnCompleteListener { task2 ->
//                Log.d("Login", "task2 complete")
//                if (task2.isSuccessful && task2.result != null) {
//                    val userObj = task2.result!!.getValue(User::class.java)!!
//                    sharedViewModel.saveData(
//                        context,
//                        userObj.uid,
//                        userObj.username,
//                        userObj.profileImg,
//                        binding.loginPassword.text.toString(),
//                        binding.loginEmail.text.toString()
//                    )
//                    findNavController().navigate(
//                        LoginFragmentDirections.actionLoginFragmentToHomeScreenFragment()
//                    )
//                } else {
//                    Log.d("Login", "Login e problem bc")
//                }
//            }
//    }


}