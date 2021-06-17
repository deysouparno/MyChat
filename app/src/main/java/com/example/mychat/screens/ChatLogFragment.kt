package com.example.mychat.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mychat.adapters.ChatLogAdapter
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
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val args: ChatLogFragmentArgs by navArgs()

        chatPerson = args.chatToPerson
        chatFromPerson = args.chatFromPerson

        directory = if (chatFromPerson.uid < chatPerson.uid) {
            chatFromPerson.uid + chatPerson.uid
        } else {
            chatPerson.uid + chatFromPerson.uid
        }.trim()

        viewModel = ViewModelProvider(this, ChatLogViewModelFactory(directory))
            .get(ChatLogViewModel::class.java)

        val chatFromImg = chatFromPerson.profileImg
        val chatToImg = chatPerson.profileImg

        (requireActivity() as AppCompatActivity).supportActionBar?.title = chatPerson.username


        adapter = ChatLogAdapter(chatFromPerson.username, chatFromImg, chatToImg)
        binding.chatLogRV.adapter = adapter
        adapter.submitList(viewModel.messages)

        viewModel.onAdd.observe(viewLifecycleOwner, {
            adapter.addItem()
            binding.chatLogRV.scrollToPosition(viewModel.messages.size-1)
        })




        binding.sendButton.setOnClickListener {
            if (binding.messageType.text.isNotBlank()) {

                sendMessage(directory)
                updateLastMessage()
                binding.messageType.text.clear()

            }

        }

        binding.messageType.setOnClickListener {

        }

        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
//                findNavController().navigate(
//                    ChatLogFragmentDirections.actionChatLogFragmentToHomeScreenFragment()
//                )
                findNavController().navigateUp()
            }

        })

        setHasOptionsMenu(true)
//        Log.d("BackStack", "back stack size is ${findNavController().backStack.size}")
        return binding.root
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.options_menu, menu)
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                findNavController().navigateUp()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateLastMessage() {
        FirebaseDatabase.getInstance().getReference("/chats${chatFromPerson.uid}/${chatPerson.uid}")
            .setValue(
                HomeScreenUser(
                    chatPerson.uid,
                    chatPerson.username,
                    chatFromPerson.username,
                    chatPerson.profileImg,
                    binding.messageType.text.toString()
                )
            )

        FirebaseDatabase.getInstance().getReference("/chats${chatPerson.uid}/${chatFromPerson.uid}")
            .setValue(
                HomeScreenUser(
                    chatFromPerson.uid,
                    chatFromPerson.username,
                    chatFromPerson.username,
                    chatFromPerson.profileImg,
                    binding.messageType.text.toString()
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
            System.currentTimeMillis()
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