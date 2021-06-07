package com.example.mychat.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mychat.adapters.NewChatAdapter
import com.example.mychat.databinding.FragmentNewChatBinding
import com.example.mychat.interfaces.ClickListener
import com.example.mychat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private const val Tag = "NewChat"

class NewChatFragment : Fragment(), ClickListener {

    private lateinit var binding: FragmentNewChatBinding
    private lateinit var adapter: NewChatAdapter
    private lateinit var users: ArrayList<User>
    private lateinit var chatFromPerson: User
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewChatBinding.inflate(inflater)
        val args: NewChatFragmentArgs by navArgs()
        chatFromPerson = args.userObj
        adapter = NewChatAdapter(this)
        getUsers()
        binding.userRecyclerView.adapter = adapter
        Log.d("BackStack", "back stack size is ${findNavController().backStack.size}")
        return binding.root
    }

    private fun getUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        users = ArrayList()
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (!snapshot.exists()) {
                    Log.d(Tag, "snapshot does not exist")
                } else {

                    snapshot.children.forEach {
                        val user = it.getValue(User::class.java)

                        if (user != null && user.uid != FirebaseAuth.getInstance().uid) {
                            users.add(user)
                            Log.d(Tag, "new user added")
                        }
                    }

                    adapter.submitList(users)
                    adapter.notifyDataSetChanged()
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(Tag, "cancelled getUsers")
            }

        })
    }

    override fun onClick(position: Int) {
        findNavController().navigate(
            NewChatFragmentDirections.actionNewChatFragmentToChatLogFragment(
                users[position],
                chatFromPerson
            )
        )

    }

}


