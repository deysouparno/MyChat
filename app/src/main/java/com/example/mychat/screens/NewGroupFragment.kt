package com.example.mychat.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.example.mychat.R
import com.example.mychat.adapters.NewChatAdapter
import com.example.mychat.databinding.FragmentNewGroupBinding
import com.example.mychat.interfaces.ClickListener
import com.example.mychat.models.ChatGroup
import com.example.mychat.models.User
import com.example.mychat.viewmodels.NewChatViewModel
import com.example.mychat.viewmodels.NewChatViewModelFactory
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class NewGroupFragment : Fragment(), ClickListener {

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
    private var selectedPhoto: Uri? = null
    private lateinit var binding: FragmentNewGroupBinding
    private lateinit var adapter: NewChatAdapter
    private lateinit var chatFromPerson: User
    private lateinit var viewModel: NewChatViewModel
    private val members = ArrayList<User>()
    private var imgUrl = ""
    private val uiState: MutableLiveData<UiState> = MutableLiveData(UiState.Empty)
    private lateinit var chatGroup: ChatGroup

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewGroupBinding.inflate(inflater)
        cropActivityResultLauncher = registerForActivityResult(cropActivityResultContracts) {
            it?.let {
                selectedPhoto = it
                Glide.with(this)
                    .load(it)
                    .centerCrop()
                    .into(binding.groupImage)
            }
        }

        binding.groupImage.setOnClickListener {
            cropActivityResultLauncher.launch(null)
        }

        val args: NewChatFragmentArgs by navArgs()
        chatFromPerson = args.userObj
        adapter = NewChatAdapter(this)

        viewModel =
            ViewModelProvider(this, NewChatViewModelFactory(chatFromPerson.username)).get(
                NewChatViewModel::class.java
            )
        binding.groupRV.adapter = adapter

        viewModel.onChange.observe(viewLifecycleOwner, {
            adapter.submitList(viewModel.users)
            adapter.notifyDataSetChanged()
        })

        uiState.observe(viewLifecycleOwner, {
            when (it) {
                is UiState.Loading -> {
                    binding.newGroupPb.isVisible = true
                }

                is UiState.Error -> {
                    binding.newGroupPb.isVisible = false
                    Toast.makeText(context, it.msg, Toast.LENGTH_SHORT).show()
                }

                is UiState.ImgUploaded -> {
                    uploadData()
                }

                is UiState.Success -> {
                    notifymembers()
                    findNavController().navigate(
                        NewGroupFragmentDirections.actionNewGroupFragmentToGroupChatFragment(chatGroup)
                    )
                }
            }
        })

        binding.groupFav.setOnClickListener {
            if (binding.groupName.text!!.isEmpty()) {
                Toast.makeText(context, "Please add a group name", Toast.LENGTH_SHORT).show()
            }
            if (members.size < 2) {
                Toast.makeText(
                    context,
                    "at least two members required for a group",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                createGroup()
            }
        }
        return binding.root
    }

    override fun onClick(position: Int) {
        if (adapter.selected.contains(position)) {
            adapter.selected.remove(position)
            members.remove(viewModel.users[position])
        } else {
            adapter.selected.add(position)
            members.add(viewModel.users[position])
        }
        adapter.notifyItemChanged(position)
    }

    override fun showProfilePic(position: Int) {

    }

    private fun createGroup() {
        uploadImage(selectedPhoto)
    }

    private fun uploadImage(photo: Uri?) {
        if (photo != null) {
            uiState.value = UiState.Loading
            GlobalScope.launch {
                val ref = FirebaseStorage.getInstance().getReference("/images")
                ref.putFile(photo)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            ref.downloadUrl.addOnSuccessListener {
                                imgUrl = it.toString()
                                uiState.value = UiState.ImgUploaded
                            }
                        } else {
                            uiState.value = UiState.Error("failed to upload Image", 1)
                        }

                    }

            }
        }
    }

    private fun uploadData() {
        uiState.value = UiState.Loading

        val ref = FirebaseDatabase.getInstance().getReference("/groups").push()
        chatGroup = ChatGroup(
            ref.key.toString(),
            binding.groupName.text.toString(),
            binding.groupDescription.text.toString(),
            imgUrl,
            members
        )
        GlobalScope.launch {
            ref.setValue(chatGroup)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        uiState.value = UiState.Success
                    } else {
                        uiState.value = UiState.Error("failed to upload to database", 2)
                    }
                }
        }

    }

    private fun notifymembers() {
        members.forEach {
            FirebaseDatabase.getInstance().getReference("chats$")
        }
    }


}

sealed class UiState {
    object Success : UiState()
    data class Error(val msg: String, val code: Int) : UiState()
    object ImgUploaded : UiState()
    object Loading : UiState()
    object Empty : UiState()
}