package com.example.mychat.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.mychat.R
import com.example.mychat.databinding.ChatItemBinding
import com.example.mychat.databinding.FragmentNewChatBinding
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

class NewChatAdapter(private val listener: ClickListener) :
    ListAdapter<User, NewChatAdapter.UserViewHolder>(UserItemDiffCallback) {

    class UserViewHolder private constructor(val binding: ChatItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.personName.text = user.username
            Glide.with(binding.imageView.context)
                .load(user.profileImg)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .circleCrop()
                .error(R.drawable.loading)
                .into(binding.imageView)

        }

        companion object {
            fun from(parent: ViewGroup, listener: ClickListener): UserViewHolder {
                val binding =
                    ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                val viewHolder = UserViewHolder(binding)
                binding.root.setOnClickListener {
                    listener.onClick(viewHolder.adapterPosition)
                }
                return viewHolder
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder.from(parent, listener)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }

    object UserItemDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem.uid == newItem.uid

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem
    }

}

interface ClickListener {
    fun onClick(position: Int)
}


