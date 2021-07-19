package com.example.mychat.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.example.mychat.Constants
import com.example.mychat.adapters.NewChatAdapter
import com.example.mychat.currentTime
import com.example.mychat.databinding.FragmentNewGroupBinding
import com.example.mychat.getUser
import com.example.mychat.interfaces.ClickListener
import com.example.mychat.models.*
import com.example.mychat.sendNotification
import com.example.mychat.viewmodels.NewChatViewModel
import com.example.mychat.viewmodels.NewChatViewModelFactory
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


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
    private lateinit var members : ArrayList<User>
//    private val names = ArrayList<String>()
    private var imgUrl = ""
    private val uiState: MutableLiveData<UiState> = MutableLiveData(UiState.Empty)
    private lateinit var chatGroup: ChatGroup
    private var groupId = ""
    private lateinit var currentUser : User
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        groupId = UUID.randomUUID().toString()
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

        currentUser = getUser(context)

        members = ArrayList()
        members.add(currentUser)
        chatFromPerson = getUser(context)
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
                    notifyMembers()
                    findNavController().navigate(
                        NewGroupFragmentDirections.actionNewGroupFragmentToGroupChatFragment(
                            chatGroup
                        )
                    )
                }
            }
        })

        binding.groupFav.setOnClickListener {
            if (binding.groupName.text!!.trim().isEmpty()) {
                Toast.makeText(context, "Please add a group name", Toast.LENGTH_SHORT).show()
            }
            else if (members.size < 2) {
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
//            names.remove(viewModel.users[position].username)
            members.remove(viewModel.users[position])
        } else {
            adapter.selected.add(position)
//            names.add(viewModel.users[position].username)
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
                val ref = FirebaseStorage.getInstance().getReference("/images/$groupId")
                ref.putFile(photo)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            ref.downloadUrl.addOnSuccessListener {
                                imgUrl = it.toString()
                                uiState.value = UiState.ImgUploaded
                            }
                        } else {
                            uiState.value = UiState.ImgUploaded
                        }

                    }
            }
        } else {
            uiState.value = UiState.ImgUploaded
        }
    }

    private fun uploadData() {
        uiState.value = UiState.Loading

        val ref = FirebaseDatabase.getInstance().getReference("/groups/${groupId}")

        chatGroup = ChatGroup(
            groupId,
            binding.groupName.text.toString(),
            binding.groupDescription.text.toString(),
            imgUrl,
            getMembers(members)
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

    private fun notifyMembers() {
        uiState.value = UiState.Loading
        val group = HomeScreenUser(
            groupId,
            binding.groupName.text.toString(),
            "admin",
            imgUrl,
            "",
            groupId,
            true
        )

        GlobalScope.launch {

            var ref = FirebaseDatabase.getInstance().getReference("/$groupId").push()
            ref.setValue(ChatMessage(
                ref.key.toString(),
                "${currentUser.username} created this group",
                "admin",
                currentTime()
            ))

            members.forEach {
                ref = FirebaseDatabase.getInstance().getReference("/$groupId").push()
                ref.setValue(ChatMessage(
                    ref.key.toString(),
                    "${currentUser.username} added ${it.username} in this group",
                    "admin",
                    currentTime()
                ))
            }

            members.forEach {
                FirebaseDatabase.getInstance().getReference("/chats${it.uid}/$groupId")
                    .setValue(group)
                notifyUser(it)
            }
            FirebaseDatabase.getInstance().getReference("/chats${currentUser.uid}/$groupId")
                .setValue(group)
        }

    }

    private fun notifyUser(user: User) {
        if (user.token == "") return
        val title = binding.groupName.text.toString()
        val message = "${currentUser.username} added you to a new group"

        if (message.isNotEmpty()) {
            PushNotification(
                NotificationData(title, message, currentUser.uid, Constants.GROUP, groupId),
                user.token
            ).also {
                sendNotification(it)
            }

        }
    }

    private fun getMembers(members : ArrayList<User>) : String {
        var res = ""
        members.forEach {
            res += "${it.uid} "
        }
        return res.trim()
    }


}

sealed class UiState {
    object Success : UiState()
    data class Error(val msg: String, val code: Int) : UiState()
    object ImgUploaded : UiState()
    object Loading : UiState()
    object Empty : UiState()
}