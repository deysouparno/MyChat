package com.example.mychat.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mychat.adapters.ChatLogAdapter
import com.example.mychat.databinding.FragmentChatLogBinding
import com.example.mychat.models.ChatMessage
import com.example.mychat.models.HomeScreenUser
import com.example.mychat.models.User
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase


class ChatLogFragment : Fragment() {

    private lateinit var binding: FragmentChatLogBinding
    private lateinit var chatPerson: User
    private lateinit var chatFromPerson: User
    private lateinit var messages: ArrayList<ChatMessage>
    private lateinit var adapter: ChatLogAdapter
    private var directory = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentChatLogBinding.inflate(inflater)

        val args: ChatLogFragmentArgs by navArgs()

        chatPerson = args.chatToPerson
        chatFromPerson = args.chatFromPerson

        val chatFromImg = chatFromPerson.profileImg
        val chatToImg = chatPerson.profileImg

        (requireActivity() as AppCompatActivity).supportActionBar?.title = chatPerson.username

        messages = ArrayList()

        adapter = ChatLogAdapter(chatFromPerson.username, chatFromImg, chatToImg)
        binding.chatLogRV.adapter = adapter
        adapter.submitList(messages)

        directory = if (chatFromPerson.uid < chatPerson.uid) {
            chatFromPerson.uid + chatPerson.uid
        } else {
            chatPerson.uid + chatFromPerson.uid
        }.trim()

        listenMessages()


        binding.sendButton.setOnClickListener {
            if (binding.messageType.text.isNotBlank()) {

                sendMessage(directory)
                updateLastMessage()
                binding.messageType.text.clear()

            }

        }

        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(
                    ChatLogFragmentDirections.actionChatLogFragmentToHomeScreenFragment()
                )
            }

        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeListener()
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

    private fun listenMessages() {
        val ref = FirebaseDatabase.getInstance().getReference(directory)
        ref.addChildEventListener(listener)
    }

    private fun removeListener() {
        val ref = FirebaseDatabase.getInstance().getReference(directory)
        ref.removeEventListener(listener)
    }

    private val listener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val msg = snapshot.getValue(ChatMessage::class.java)
            Log.d("ChatLog", "message sent")
            if (msg != null) {
                messages.add(msg)
                adapter.notifyItemInserted(messages.size - 1)
            }
            binding.chatLogRV.scrollToPosition(messages.size - 1)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            val msg = snapshot.getValue(ChatMessage::class.java)
            if (msg != null) {
                messages.remove(msg)
                adapter.notifyDataSetChanged()
            }
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

        }

        override fun onCancelled(error: DatabaseError) {

        }

    }


}