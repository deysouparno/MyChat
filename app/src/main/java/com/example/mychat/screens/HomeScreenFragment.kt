package com.example.mychat.screens

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mychat.*
import com.example.mychat.adapters.SwipeViewAdapter
import com.example.mychat.databinding.FragmentHomeScreenBinding
import com.example.mychat.models.User
import com.example.mychat.services.FirebaseService
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging


class HomeScreenFragment : Fragment() {

    private lateinit var binding: FragmentHomeScreenBinding
    private lateinit var currentUser: User
    var flag = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (FirebaseAuth.getInstance().currentUser == null) {
            findNavController().navigate(
                HomeScreenFragmentDirections.actionHomeScreenFragmentToLoginFragment()
            )
        }
        currentUser = getUser(context)
        binding = FragmentHomeScreenBinding.inflate(inflater)
        setHasOptionsMenu(true)
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.homeToolbar)

        setHasOptionsMenu(true)

        FirebaseService.sharedPref =
            context?.getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            FirebaseService.token = it
            updateToken(it)
        }



        updateStatus(currentUser.uid, "online")

        val tabLayout = binding.tabLayout
        binding.viewPager.adapter = SwipeViewAdapter(this)
        TabLayoutMediator(tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.setIcon(R.drawable.ic_baseline_chat_24)
                }
                1 -> {
                    tab.setIcon(R.drawable.ic_baseline_account_circle_24)
                }
            }
        }.attach()


//        Log.d("BackStack", "back stack size is ${findNavController().backStack.size}")
        return binding.root
    }

    private fun updateToken(token: String) {
        FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}").child("token")
            .setValue(token)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.options_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.new_group -> {
                findNavController().navigate(
                    HomeScreenFragmentDirections.actionHomeScreenFragmentToNewGroupFragment(
                        currentUser
                    )
                )
            }
            R.id.logout_menu_item -> {
                flag = false
                FirebaseAuth.getInstance().signOut()
                updateToken("")
                deleteData(context)
                updateStatus(currentUser.uid, currentTime())
                Log.d("log out", "logged out successfully")
                findNavController().navigate(
                    HomeScreenFragmentDirections.actionHomeScreenFragmentToLoginFragment()
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }



}