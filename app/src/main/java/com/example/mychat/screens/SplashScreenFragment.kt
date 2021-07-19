package com.example.mychat.screens


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mychat.checkSecurity
import com.example.mychat.databinding.FragmentSplashScreenBinding
import com.example.mychat.deleteData
import com.example.mychat.getEmail
import com.example.mychat.getPassword
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.Executor


class SplashScreenFragment : Fragment() {

//    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private val uiState = MutableStateFlow(0)
    private lateinit var binding: FragmentSplashScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSplashScreenBinding.inflate(inflater)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launchWhenStarted {
            delay(1000)
            setAuthentication()
            binding.splashProgressbar.isVisible = true
            uiState.collect {
                when (it) {
                    1 -> {
                        binding.apply {
                            splashIcon.isVisible = false
                            splashText.isVisible = false
                        }
                    }
                    2 -> findNavController().navigate(
                        SplashScreenFragmentDirections.actionSplashScreenFragmentToHomeScreenFragment()
                    )
                    3 -> requestLogin()
                }
            }
        }
        verifyAccount()
    }

    private fun verifyAccount() {
        Log.d("Login", "verifying account")
        uiState.value = 1
        GlobalScope.launch {
            val password = getPassword(context)
            val email = getEmail(context)

            if (password != null && email != null) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            uiState.value = 2
                        } else {
                            Log.d("Login", "$email  $password")
                            Log.d("Login", "splash screen login failed")
                            uiState.value = 3
                        }
                    }
            } else {
                uiState.value = 3
            }
        }
    }
    private fun requestLogin() {
        deleteData(context)
        FirebaseAuth.getInstance().signOut()
        findNavController().navigate(
            SplashScreenFragmentDirections.actionSplashScreenFragmentToLoginFragment()
        )
    }

    private fun setAuthentication() {
        if (checkSecurity(context) != true) {
            Log.d("finger", "authentication disabled")
            return
        }
        executor = ContextCompat.getMainExecutor(context)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    Log.d("finger", "error occurred")
                    super.onAuthenticationError(errorCode, errString)
                    if(errorCode== BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        biometricPrompt.cancelAuthentication();
                        Log.d("finger", "cancel pressed")
                        requireActivity().finish()
                    }
                    else if(errorCode == BiometricPrompt.ERROR_USER_CANCELED){
                        requireActivity().finish()
                    }
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                }

                override fun onAuthenticationFailed() {
                    Log.d("finger", "failed")
                    requireActivity().finish()
                }
            })
        
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Sign In")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }


}