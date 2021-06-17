package com.example.mychat.screens

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mychat.R
import com.example.mychat.SharedViewModel
import com.example.mychat.adapters.SwipeViewAdapter
import com.example.mychat.databinding.FragmentHomeScreenBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth


class HomeScreenFragment : Fragment(){

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var binding: FragmentHomeScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (FirebaseAuth.getInstance().currentUser == null) {
            findNavController().navigate(
                HomeScreenFragmentDirections.actionHomeScreenFragmentToLoginFragment()
            )
        }
        binding = FragmentHomeScreenBinding.inflate(inflater)
        setHasOptionsMenu(true)
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.homeToolbar)

        setHasOptionsMenu(true)

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.options_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.logout_menu_item -> {
                FirebaseAuth.getInstance().signOut()
                sharedViewModel.deleteData(context)
                Log.d("log out", "logged out successfully")
                findNavController().navigate(
                    HomeScreenFragmentDirections.actionHomeScreenFragmentToLoginFragment()
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }



}