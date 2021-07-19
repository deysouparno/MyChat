package com.example.mychat.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.example.mychat.SharedViewModel
import com.example.mychat.databinding.FragmentImageViewerBinding
import com.example.mychat.getUser
import com.example.mychat.loadImage
import com.example.mychat.models.User


class ImageViewerFragment : Fragment() {

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var binding: FragmentImageViewerBinding

    private lateinit var cropActivityResultLauncher: ActivityResultLauncher<Any?>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImageViewerBinding.inflate(inflater)
        val args: ImageViewerFragmentArgs by navArgs()
        val code = args.code
        val currentUser = getUser(context)

        if (code == 1) {
            binding.toolbar.isVisible = false
            binding.messageType.isVisible = false
            binding.sendButton.isVisible = false

            Glide.with(requireContext())
                .load(currentUser.profileImg)
                .into(binding.theImage)
        } else if (code == 2) {
            handleCrop()
            binding.apply {
                doneButton.isVisible = false
//                theImage.setImageURI(sharedViewModel.image)
                Glide.with(context!!)
                    .load(sharedViewModel.image)
                    .into(theImage)
                personName.text = sharedViewModel.toPerson.username
                cropText.setOnClickListener {
                    cropActivityResultLauncher.launch(null)
                }
                sendButton.setOnClickListener {
                    sharedViewModel.send.value = true
                    findNavController().navigateUp()
                }
            }
            Glide.with(context!!)
                .load(sharedViewModel.toPerson.profileImg)
                .into(binding.profileDp)
        }
        return binding.root
    }

    private fun handleCrop() {
        val cropActivityResultContracts = object : ActivityResultContract<Any?, Uri?>() {
            override fun createIntent(context: Context, input: Any?): Intent {
                return CropImage
                    .activity(sharedViewModel.image)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .getIntent(context)
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
                return CropImage.getActivityResult(intent)?.uriContent
            }

        }

        cropActivityResultLauncher = registerForActivityResult(cropActivityResultContracts) {
            it?.let {
                sharedViewModel.image = it
//                Glide.with(this)
//                    .load(it)
//                    .centerCrop()
//                    .into(binding.theImage)
                binding.theImage.loadImage(it)
            }
        }
    }


}