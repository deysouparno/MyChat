package com.example.mychat.screens

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.mychat.R
import com.example.mychat.adapters.ChatLogAdapter
import com.example.mychat.currentTime
import com.example.mychat.databinding.FragmentChatLogBinding
import com.example.mychat.models.ChatMessage
import com.example.mychat.models.HomeScreenUser
import com.example.mychat.models.User
import com.example.mychat.viewmodels.ChatLogViewModel
import com.example.mychat.viewmodels.ChatLogViewModelFactory
import com.google.firebase.database.FirebaseDatabase


class ChatLogFragment : Fragment() {

    //    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var binding: FragmentChatLogBinding
    private lateinit var chatPerson: User
    private lateinit var chatFromPerson: User
    private lateinit var adapter: ChatLogAdapter
    private var directory = ""
    private lateinit var viewModel: ChatLogViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentChatLogBinding.inflate(inflater)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.chatLogToolbar)
//        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val args: ChatLogFragmentArgs by navArgs()

        chatPerson = args.chatToPerson
        chatFromPerson = args.chatFromPerson

        directory = if (chatFromPerson.uid < chatPerson.uid) {
            chatFromPerson.uid + chatPerson.uid
        } else {
            chatPerson.uid + chatFromPerson.uid
        }.trim()

        viewModel = ViewModelProvider(
            this,
            ChatLogViewModelFactory(
                directory,
            )
        ).get(ChatLogViewModel::class.java)

        val chatFromImg = chatFromPerson.profileImg
        val chatToImg = chatPerson.profileImg

//        (requireActivity() as AppCompatActivity).supportActionBar?.title = chatPerson.username

        binding.personName.text = chatPerson.username
//        binding.lastSeen.text = chatPerson.lastSeen
        Glide.with(context!!)
            .load(chatPerson.profileImg)
            .into(binding.profileDp)


        adapter = ChatLogAdapter(chatFromPerson.username, chatFromImg)
        binding.chatLogRV.adapter = adapter
        adapter.submitList(viewModel.messages)

        viewModel.onAdd.observe(viewLifecycleOwner, {
            adapter.addItem()
            binding.chatLogRV.scrollToPosition(viewModel.messages.size - 1)
        })


        binding.backArrow.setOnClickListener {
            findNavController().navigateUp()
        }


        binding.sendButton.setOnClickListener {
            if (binding.messageType.text.isNotBlank()) {

                sendMessage(directory)
                updateLastMessage()
                binding.messageType.text.clear()

            }

        }

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

        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }

        })

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.removeListener()
        adapter.tailReset()
    }


    private fun updateLastMessage() {
        FirebaseDatabase.getInstance().getReference("/chats${chatFromPerson.uid}/${chatPerson.uid}")
            .setValue(
                HomeScreenUser(
                    chatPerson.uid,
                    chatPerson.username,
                    chatFromPerson.username,
                    chatPerson.profileImg,
                    binding.messageType.text.toString(),
                )
            )

        FirebaseDatabase.getInstance().getReference("/chats${chatPerson.uid}/${chatFromPerson.uid}")
            .setValue(
                HomeScreenUser(
                    chatFromPerson.uid,
                    chatFromPerson.username,
                    chatFromPerson.username,
                    chatFromPerson.profileImg,
                    binding.messageType.text.toString(),
                )
            )
    }

    private fun sendMessage(directory: String) {

        val ref = FirebaseDatabase.getInstance().getReference("/${directory}").push()

        val message = ChatMessage(
            ref.key.toString(),
            binding.messageType.text.toString(),
            chatFromPerson.username,
            chatPerson.username,
            currentTime()
        )

        ref.setValue(message)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
//                    Toast.makeText(context, "Message sent successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }

    }


}