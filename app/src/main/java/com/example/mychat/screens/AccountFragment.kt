package com.example.mychat.screens

import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.mychat.R
import com.example.mychat.SharedViewModel
import com.example.mychat.databinding.FragmentAccountBinding


class AccountFragment : Fragment() {

    private lateinit var binding: FragmentAccountBinding
    private val sharedViewModel: SharedViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountBinding.inflate(inflater)
        val currentUser = sharedViewModel.getUser(context)
        Glide.with(binding.profilePic.context)
            .load(currentUser.profileImg)
            .circleCrop()
            .placeholder(R.drawable.ic_baseline_account_circle_24)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.profilePic)
//        Log.d("BackStack", "back stack size is ${findNavController().backStack.size}")

        binding.switch1.isChecked = sharedViewModel.checkSecurity(context) ?: false
        binding.switch1.setOnClickListener {
            if (binding.switch1.isChecked) {
                binding.switch1.isChecked = checkBiometricSupport()
            }
        }

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        Log.d("finger", "authentication state : ${binding.switch1.isChecked}")
        sharedViewModel.setSecurity(context, binding.switch1.isChecked)
    }

    private fun checkBiometricSupport() : Boolean {
        val keyguardManager = context?.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (!keyguardManager.isKeyguardSecure) {
            Toast.makeText(context, "fingerprint is not enabled in settings", Toast.LENGTH_SHORT)
                .show()
            return false
        }

        if (!requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            return false
        }

        return if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.USE_BIOMETRIC
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            var flag = false
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (!isGranted) {
                    Toast.makeText(context, "Permission required", Toast.LENGTH_SHORT).show()
                } else {
                    flag = true
                }
            }
            flag
        } else true
    }


}