package com.example.mychat.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mychat.R
import com.example.mychat.databinding.FragmentHomeScreenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class HomeScreenFragment : Fragment() {

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
        val args: HomeScreenFragmentArgs by navArgs()
        val user = args.userObj

//        Toast.makeText(context, "user details:\n${user.username}\n${user.profileImg}\n${user.uid}", Toast.LENGTH_LONG).show()

        binding.homeScreenFab.setOnClickListener {
            findNavController().navigate(
                HomeScreenFragmentDirections.actionHomeScreenFragmentToNewChatFragment(user)
            )
        }
        setHasOptionsMenu(true)

        listenMessages(user.username)


        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.options_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.logout_menu_item -> {
                FirebaseAuth.getInstance().signOut()
                Log.d("log out", "logged out successfully")
                findNavController().navigate(
                    HomeScreenFragmentDirections.actionHomeScreenFragmentToLoginFragment()
                )

            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun listenMessages(username: String) {
        val directory = "${username}chats"
        val ref = FirebaseDatabase.getInstance().getReference(directory)

    }

}