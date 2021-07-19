package com.example.mychat.screens

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mychat.*
import com.example.mychat.Constants.Companion.CHAT
import com.example.mychat.Constants.Companion.IMG_MSG
import com.example.mychat.Constants.Companion.TEXT_MSG
import com.example.mychat.adapters.ChatLogAdapter
import com.example.mychat.databinding.FragmentChatLogBinding
import com.example.mychat.models.*
import com.example.mychat.viewmodels.ChatLogViewModel
import com.example.mychat.viewmodels.ChatLogViewModelFactory
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

const val topic = "/topics/myTopics"

class ChatLogFragment : Fragment() {

    private val chooseImg = registerForActivityResult(ActivityResultContracts.GetContent()) {

        try {
            if (it != null) {
                sharedViewModel.image = it
                sharedViewModel.toPerson = chatPerson
                findNavController().navigate(
                    ChatLogFragmentDirections.actionChatLogFragmentToImageViewerFragment(2)
                )
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Chosen image is too large", Toast.LENGTH_SHORT).show()
        }

    }

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var binding: FragmentChatLogBinding
    private lateinit var chatPerson: User
    private lateinit var currentUser: User
    private lateinit var chatLogAdapter: ChatLogAdapter
    private var directory = ""
    private lateinit var viewModel: ChatLogViewModel
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var flag = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentChatLogBinding.inflate(inflater)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.chatLogToolbar)

        val args: ChatLogFragmentArgs by navArgs()

        chatPerson = args.chatToPerson
        currentUser = getUser(context)

        directory = if (currentUser.uid < chatPerson.uid) {
            currentUser.uid + chatPerson.uid
        } else {
            chatPerson.uid + currentUser.uid
        }.trim()

        viewModel = ViewModelProvider(
            this,
            ChatLogViewModelFactory(directory)
        ).get(ChatLogViewModel::class.java)

        binding.personName.text = chatPerson.username
        binding.lastSeen.text = getCurrentTime(chatPerson.isOnline, 0)
        Glide.with(context!!)
            .load(chatPerson.profileImg)
            .into(binding.profileDp)

        setUpRecyclerView()
        viewModel.onAdd.observe(viewLifecycleOwner, {
            chatLogAdapter.addItem()
            binding.chatLogRV.scrollToPosition(viewModel.messages.size - 1)
        })

        binding.backArrow.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.cameraButton.setOnClickListener {

            chooseImg.launch("image/*")


        }
        sharedViewModel.send.observe(viewLifecycleOwner, {
            Log.d("chats", "img send observed")
            if (it) {
                sendImg(directory, viewModel.messages.size)
                sharedViewModel.send.value = false
            }
        })

        FirebaseMessaging.getInstance().subscribeToTopic(topic)
        binding.sendButton.setOnClickListener {
            if (binding.messageType.text.isNotBlank()) {
                sendMessage(directory)
                if (viewModel.messages.isEmpty()) updateLastMessage()
                notifyUser()
                binding.messageType.text.clear()
            }
        }

        handleTextChange()

        observeUser()

        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }

        })

        return binding.root
    }

    private fun notifyUser() {
        if (chatPerson.token == "") return
        val title = currentUser.username
        val message = binding.messageType.text.toString()

        if (message.isNotEmpty()) {
            PushNotification(
                NotificationData(title, message, currentUser.uid, CHAT),
                chatPerson.token
            ).also {
                sendNotification(it)
            }

        }
    }

    private fun setUpRecyclerView() {
        linearLayoutManager = LinearLayoutManager(context)
        chatLogAdapter = ChatLogAdapter(currentUser.username)
        chatLogAdapter.submitList(viewModel.messages)
        binding.chatLogRV.apply {
            layoutManager = linearLayoutManager
            adapter = chatLogAdapter
        }

    }

    private fun observeUser() {
        viewModel.listenUser(chatPerson.uid)
        viewModel.onUserDataChange.observe(viewLifecycleOwner, {
            chatPerson = viewModel.userData!!
            val time = getCurrentTime(viewModel.userData!!.isOnline, 0)
            if (time == "online") {
                binding.lastSeen.text = "online"
            } else {
                binding.lastSeen.text = "last seen at $time"
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.removeListener()
        chatLogAdapter.tailReset()
        viewModel.removeUserListener(currentUser.uid)
    }


    private fun updateLastMessage() {

        GlobalScope.launch {

            FirebaseDatabase.getInstance()
                .getReference("/chats${currentUser.uid}/${chatPerson.uid}")
                .setValue(
                    HomeScreenUser(
                        chatPerson.uid,
                        chatPerson.username,
                        currentUser.username,
                        chatPerson.profileImg,
                        binding.messageType.text.toString(),
                        directory,
                        false
                    )
                )

            FirebaseDatabase.getInstance()
                .getReference("/chats${chatPerson.uid}/${currentUser.uid}")
                .setValue(
                    HomeScreenUser(
                        currentUser.uid,
                        currentUser.username,
                        currentUser.username,
                        currentUser.profileImg,
                        binding.messageType.text.toString(),
                        directory,
                        false
                    )
                )
        }
    }

    private fun sendMessage(directory: String) {

        val ref = FirebaseDatabase.getInstance().getReference("/${directory}").push()

        val message = ChatMessage(
            ref.key.toString(),
            binding.messageType.text.toString(),
            currentUser.username,
            currentTime(),
            TEXT_MSG
        )
        ref.setValue(message)
    }

    private fun handleTextChange() {
        binding.messageType.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (!binding.cameraButton.isVisible && s!!.isEmpty()) {
                    val animation = AnimationUtils.loadAnimation(
                        context,
                        R.anim.search_enter
                    )
                    animation.duration = 100
                    binding.cameraButton.startAnimation(animation)
                } else if (s!!.isNotEmpty() && binding.cameraButton.isVisible) {
                    val animation = AnimationUtils.loadAnimation(
                        context,
                        R.anim.search_exit
                    )
                    animation.duration = 100
                    binding.cameraButton.startAnimation(animation)
                }
                binding.cameraButton.isVisible = s!!.isEmpty()
            }

        })
    }

    private fun sendImg(directory: String, position: Int) {
        if (sharedViewModel.image == null) return
        val ref = FirebaseDatabase.getInstance().getReference("/$directory").push()
        val imgRef = FirebaseStorage.getInstance().getReference("/chats/${ref.key.toString()}")
        val msg = ChatMessage(
            ref.key.toString(),
            "",
            currentUser.username,
            currentTime(),
            IMG_MSG,
            ""
        )
        viewModel.messages.add(msg)
        chatLogAdapter.addItem()
        imgRef.putFile(sharedViewModel.image!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    imgRef.downloadUrl.addOnCompleteListener {
                        if (it.isSuccessful) {
                            val url = it.result.toString()
                            msg.link = url
                            ref.setValue(msg)
                            viewModel.messages[position].link = url
                            chatLogAdapter.notifyItemChanged(position)

                        }
                    }
                }
            }
    }

    private fun demo() {
        binding.chatLogRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val position = linearLayoutManager.findLastCompletelyVisibleItemPosition()
                flag = position == chatLogAdapter.itemCount - 1
            }
        })
    }

}