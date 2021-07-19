package com.example.mychat.screens

import android.app.AlertDialog
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.example.mychat.*
import com.example.mychat.databinding.FragmentAccountBinding
import com.example.mychat.databinding.UpdateStatusPopUpBinding
import com.example.mychat.models.User
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class AccountFragment : Fragment() {

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
    private lateinit var currentUser: User
    private lateinit var cropActivityResultLauncher: ActivityResultLauncher<Any?>
    private lateinit var binding: FragmentAccountBinding
    private var selectedPhoto: Uri? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountBinding.inflate(inflater)
        currentUser = getUser(context)
        binding.statusTextView.text = currentUser.status
        Glide.with(binding.profilePic.context)
            .load(currentUser.profileImg)
            .fitCenter()
            .placeholder(R.drawable.ic_baseline_account_circle_24)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.profilePic)


        binding.switch1.isChecked = checkSecurity(context) ?: false
        binding.switch1.setOnClickListener {
            if (binding.switch1.isChecked) {
                binding.switch1.isChecked = checkBiometricSupport()
            }
        }

        binding.profilePic.setOnClickListener {
            findNavController().navigate(HomeScreenFragmentDirections.actionHomeScreenFragmentToImageViewerFragment(1))
        }

        binding.editStatus.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            val dialog = builder.create()
            val popUpBinding = UpdateStatusPopUpBinding.inflate(layoutInflater)
            popUpBinding.updateStatusBt.setOnClickListener {
                FirebaseDatabase.getInstance()
                    .getReference("/users/${currentUser.uid}").child("status")
                    .setValue(popUpBinding.statusText.text.toString())
                binding.statusTextView.text = popUpBinding.statusText.text.toString()
                updateUserStatus(context, popUpBinding.statusText.text.toString())
                dialog.dismiss()
            }
            dialog.setView(popUpBinding.root)
            dialog.show()
        }

        cropActivityResultLauncher = registerForActivityResult(cropActivityResultContracts) {
            it?.let {
                selectedPhoto = it
                Glide.with(this)
                    .load(it)
                    .centerCrop()
                    .into(binding.profilePic)
                updateImage(1)
            }
        }

        handleImage()

        return binding.root
    }

    private fun handleImage() {
        val items = arrayOf("Choose Image", "Remove Image")
        binding.imageAction.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setItems(items) { dialog, which ->
                    when (items[which]) {
                        "Choose Image" -> {
                            cropActivityResultLauncher.launch(null)

                        }
                        else -> {
                            updateImage(0)
                        }
                    }
                }
                .show()
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("finger", "authentication state : ${binding.switch1.isChecked}")
        setSecurity(context, binding.switch1.isChecked)
    }

//    fun handleImageViewing()  {
//        val alertDialog = AlertDialog.Builder(context)
//        val binding = ViewImageBinding.inflate(layoutInflater)
//        binding.apply {
//            toolbar.isVisible = false
//            messageType.isVisible = false
//            sendButton.isVisible = false
//        }
//        alertDialog.setView(binding.root)
//        alertDialog.show()
//    }

    private fun updateImage(code: Int) {
        binding.accountPb.isVisible = true
        GlobalScope.launch {
            if (code == 0) {
                FirebaseDatabase.getInstance()
                    .getReference("/users/${currentUser.uid}").child("profileImg")
                    .setValue("").addOnSuccessListener {
                        binding.accountPb.isVisible = false
                        binding.profilePic.setBackgroundResource(R.drawable.ic_baseline_account_circle_24)
                    }
            }
        }
        if (code == 1 && selectedPhoto != null) {
            GlobalScope.launch {
                binding.accountPb.isVisible = true
                val ref = FirebaseStorage.getInstance().getReference("/images/${currentUser.uid}")
                ref.putFile(selectedPhoto!!)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            ref.downloadUrl.addOnSuccessListener {
                                FirebaseDatabase.getInstance()
                                    .getReference("/users/${currentUser.uid}").child("profileImg")
                                    .setValue(it.toString())
                                binding.accountPb.isVisible = false
                            }
                        } else {
                            Toast.makeText(context, "failed to update image", Toast.LENGTH_SHORT)
                                .show()
                        }

                    }

            }
        }
    }

    private fun checkBiometricSupport(): Boolean {
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

class ImgView(context: Context) : View(context) {

}