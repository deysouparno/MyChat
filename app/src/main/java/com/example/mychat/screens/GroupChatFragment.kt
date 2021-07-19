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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.mychat.R
import com.example.mychat.adapters.ChatLogAdapter
import com.example.mychat.currentTime
import com.example.mychat.databinding.FragmentGroupChatBinding
import com.example.mychat.getUser
import com.example.mychat.models.ChatGroup
import com.example.mychat.models.ChatMessage
import com.example.mychat.models.User
import com.example.mychat.viewmodels.GroupChatViewModel
import com.example.mychat.viewmodels.GroupChatViewModelFactory
import com.google.firebase.database.FirebaseDatabase


class GroupChatFragment : Fragment() {

    private lateinit var binding: FragmentGroupChatBinding
    private lateinit var viewModel: GroupChatViewModel
    private lateinit var group: ChatGroup
    private lateinit var groupChatAdapter: ChatLogAdapter
    private lateinit var currentUser: User
    private lateinit var linearLayoutManager: LinearLayoutManager
    private val groupMembers = ArrayList<User>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentGroupChatBinding.inflate(inflater)
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.groupToolbar)

        val args: GroupChatFragmentArgs by navArgs()

        currentUser = getUser(context)
        group = args.groupObj

        viewModel = ViewModelProvider(
            this,
            GroupChatViewModelFactory(group.id, currentUser)
        ).get(GroupChatViewModel::class.java)

        setGroupDetails(group)
        setUpRecyclerView()

        viewModel.onAdd.observe(viewLifecycleOwner, {
            groupChatAdapter.addItem()
            binding.groupChatLogRV.scrollToPosition(viewModel.messages.size - 1)
        })

        viewModel.onGroupDataChange.observe(viewLifecycleOwner, {
            group = viewModel.group!!
            setGroupDetails(group)
        })

        binding.groupSendButton.setOnClickListener {
            sendMessage(group.id)
            binding.groupMessageType.text.clear()
        }

        binding.backArrow.setOnClickListener { findNavController().navigateUp() }

        handleTextChange()

        return binding.root
    }

    private fun setUpRecyclerView() {
        groupChatAdapter = ChatLogAdapter(currentUser.username)
        groupChatAdapter.submitList(viewModel.messages)
        linearLayoutManager = LinearLayoutManager(context)
        groupChatAdapter.submitList(viewModel.messages)
        binding.groupChatLogRV.apply {
            adapter = groupChatAdapter
            layoutManager = linearLayoutManager
        }
    }

    private fun setGroupDetails(group: ChatGroup) {
        Glide.with(context!!)
            .load(group.image)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.ic_baseline_group_24)
            .error(R.drawable.ic_baseline_group_24)
            .into(binding.groupDp)
        binding.groupName.text = group.name
        binding.groupMembers.text = viewModel.names
    }




    private fun sendMessage(directory: String) {
        val ref = FirebaseDatabase.getInstance().getReference("/${directory}").push()
        val message = ChatMessage(
            ref.key.toString(),
            binding.groupMessageType.text.toString(),
            currentUser.username,
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

    private fun handleTextChange() {
        binding.groupMessageType.addTextChangedListener(object : TextWatcher {
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

    override fun onDestroy() {
        super.onDestroy()
        groupChatAdapter.tailReset()
        viewModel.removeListeners()
    }


}