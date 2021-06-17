package com.example.mychat.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mychat.SharedViewModel
import com.example.mychat.adapters.HomeScreenAdapter
import com.example.mychat.databinding.FragmentHomeChatBinding
import com.example.mychat.interfaces.ClickListener
import com.example.mychat.models.User
import com.example.mychat.viewmodels.HomeScreenViewModel
import com.example.mychat.viewmodels.HomeViewModelFactory


class HomeChatFragment : Fragment(), ClickListener {

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var viewModel: HomeScreenViewModel
    private lateinit var binding: FragmentHomeChatBinding
    private lateinit var currentUser: User
    private lateinit var adapter: HomeScreenAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeChatBinding.inflate(inflater)

        currentUser = sharedViewModel.getUser(context)
        viewModel = ViewModelProvider(this, HomeViewModelFactory(currentUser))
            .get(HomeScreenViewModel::class.java)

        binding.homeScreenFab.setOnClickListener {
            findNavController().navigate(
                HomeScreenFragmentDirections.actionHomeScreenFragmentToNewChatFragment(currentUser)
            )
        }

        val manager = LinearLayoutManager(binding.homeScreenRV.context)
        manager.reverseLayout = true
        adapter = HomeScreenAdapter(viewModel.homeScreenUsers, this, currentUser.username)
        binding.homeScreenRV.adapter = adapter
        binding.homeScreenRV.layoutManager = manager

        viewModel.onChanged.observe(viewLifecycleOwner, {
            Log.d("HomeScreen", "observer called")
            adapter.updateList(viewModel.homeScreenUsers)
        })


//        Log.d("BackStack", "back stack size is ${findNavController().backStack.size}")
        return binding.root
    }

    override fun onClick(position: Int) {
        val user = User(
            viewModel.homeScreenUsers[position].uid,
            viewModel.homeScreenUsers[position].username,
            true,
            viewModel.homeScreenUsers[position].profileImg
        )
        findNavController().navigate(
            HomeScreenFragmentDirections.actionHomeScreenFragmentToChatLogFragment(
                user,
                currentUser
            )
        )
    }


}