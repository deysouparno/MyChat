package com.example.mychat.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mychat.adapters.ChatLogAdapter
import com.example.mychat.databinding.FragmentChatLogBinding
import com.example.mychat.models.ChatMessage
import com.example.mychat.models.User
import com.google.firebase.database.*


class ChatLogFragment : Fragment() {

    private lateinit var binding: FragmentChatLogBinding
    private lateinit var chatPerson: User
    private lateinit var chatFromPerson: User
    private lateinit var messages: ArrayList<ChatMessage>
    private lateinit var adapter: ChatLogAdapter
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

        messages = ArrayList()

        adapter = ChatLogAdapter(chatFromPerson.username, chatFromImg, chatToImg)
        binding.chatLogRV.adapter = adapter
        adapter.submitList(messages)

        val directory = if (chatFromPerson.uid < chatPerson.uid) {
            chatFromPerson.uid + chatPerson.uid
        } else {
            chatPerson.uid + chatFromPerson.uid
        }.trim()

//        loadMessages(directory)
        listenMessages(directory)


        binding.sendButton.setOnClickListener {
            if (binding.messageType.text.isNotBlank()) {
                sendMessage(directory)
            }

        }

//        activity?.onBackPressedDispatcher?.addCallback(this, object: OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                findNavController().navigate(
//                    ChatLogFragmentDirections.actionChatLogFragmentToHomeScreenFragment()
//                )
//            }
//
//        })

        return binding.root
    }

    private fun loadMessages(directory: String) {
        val ref = FirebaseDatabase.getInstance().getReference(directory)
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEach {
                        val msg = it.getValue(ChatMessage::class.java)
                        if (msg != null) {
                            messages.add(msg)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
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
                    binding.chatLogRV.adapter!!.notifyDataSetChanged()
                    Toast.makeText(context, "Message sent successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }

    }

    private fun listenMessages(directory: String) {

        val ref = FirebaseDatabase.getInstance().getReference(directory)
        ref.addChildEventListener(object :ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val msg = snapshot.getValue(ChatMessage::class.java)
                Log.d("ChatLog", "$msg")
                if (msg != null) {
                    messages.add(msg)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }



}