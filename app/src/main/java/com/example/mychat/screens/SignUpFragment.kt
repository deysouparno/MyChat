package com.example.mychat.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.example.mychat.databinding.FragmentSingnUpBinding
import com.example.mychat.models.User
import com.example.mychat.saveData
import com.example.mychat.viewmodels.SignUpViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collect

private const val Tag = "SignUp"

class SignUpFragment : Fragment() {

    private val cropActivityResultContracts = object : ActivityResultContract<Any?, Uri?>() {
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage
                .activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .getIntent(context)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uriContent
        }

    }
    private lateinit var cropActivityResultLauncher: ActivityResultLauncher<Any?>
//    private val sharedViewModel: SharedViewModel by activityViewModels()

    //    private var currentUser: FirebaseUser? = null
    private lateinit var binding: FragmentSingnUpBinding
    private var selectedPhoto: Uri? = null
    private val viewModel: SignUpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (FirebaseAuth.getInstance().currentUser != null) {
            findNavController().navigate(
                SignUpFragmentDirections.actionSignUpFragmentToHomeScreenFragment()
            )
        }
        binding = FragmentSingnUpBinding.inflate(inflater)

        Log.d(Tag, "current user is ${FirebaseAuth.getInstance().currentUser?.uid}")

        binding.LoginText.setOnClickListener {
            it.findNavController().navigateUp()
        }


        binding.registerButton.setOnClickListener {
            if (binding.registerUsername.text!!.isBlank()
                || binding.registerEmail.text!!.isBlank()
                || binding.registerUsername.text!!.isBlank()
            ) {
                Toast.makeText(context, "invalid", Toast.LENGTH_SHORT).show()
            } else if (binding.registerPassword.text!!.length < 6) {
                Toast.makeText(
                    context,
                    "password must be at least 6 characters",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                binding.registerProgressBar.visibility = View.VISIBLE
                (context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(it.windowToken, 0)
//                it.clearFocus()
                activity?.currentFocus?.clearFocus()
                viewModel.signUp(
                    binding.registerEmail.text.toString(),
                    binding.registerPassword.text.toString()
                )
            }
        }

        cropActivityResultLauncher = registerForActivityResult(cropActivityResultContracts) {
            it?.let {
                selectedPhoto = it
                Glide.with(this)
                    .load(it)
                    .centerCrop()
                    .circleCrop()
                    .into(binding.profileImage)
            }
        }

        binding.profileImage.setOnClickListener {
            cropActivityResultLauncher.launch(null)
        }

//        Log.d("BackStack", "back stack size is ${findNavController().backStack.size}")

        lifecycleScope.launchWhenStarted {
            viewModel.signInUiState.collect {
                when (it) {
                    is SignUpViewModel.SignInUiState.Registered -> {
                        Log.d("SignUp", "user registered")
                        viewModel.uploadImage(selectedPhoto, viewModel.uid)
                    }

                    is SignUpViewModel.SignInUiState.ImgUploaded -> {
                        val user = User(
                            viewModel.uid,
                            binding.registerUsername.text.toString(),
                            "true",
                            viewModel.imgUrl,
                        )
                        saveData(
                            context,
                            user.uid,
                            user.username,
                            user.profileImg,
                            binding.registerPassword.text.toString(),
                            binding.registerEmail.text.toString(),
                            ""
                        )
                        viewModel.uploadUserData(user)
                    }

                    is SignUpViewModel.SignInUiState.Success -> {
                        findNavController().navigate(
                            SignUpFragmentDirections.actionSignUpFragmentToHomeScreenFragment()
                        )
                    }

                    is SignUpViewModel.SignInUiState.Error -> {
                        binding.registerProgressBar.isVisible = false
                        Toast.makeText(context, it.msg, Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                    }
                }
            }
        }

        return binding.root
    }


}
