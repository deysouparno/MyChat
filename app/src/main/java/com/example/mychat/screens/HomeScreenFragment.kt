package com.example.mychat.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mychat.R
import com.example.mychat.adapters.HomeScreenAdapter
import com.example.mychat.databinding.FragmentHomeScreenBinding
import com.example.mychat.interfaces.ClickListener
import com.example.mychat.models.HomeScreenUser
import com.example.mychat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase


class HomeScreenFragment : Fragment(), ClickListener {

    private lateinit var binding: FragmentHomeScreenBinding
    private lateinit var homeScreenUsers: ArrayList<HomeScreenUser>
    private lateinit var adapter: HomeScreenAdapter
    private lateinit var currentUser: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(context, AuthActivity::class.java))
            activity?.finish()
        }
        binding = FragmentHomeScreenBinding.inflate(inflater)

        currentUser = getData()

        binding.homeScreenFab.setOnClickListener {
            findNavController().navigate(
                HomeScreenFragmentDirections.actionHomeScreenFragmentToNewChatFragment(currentUser)
            )
        }
        setHasOptionsMenu(true)

        homeScreenUsers = ArrayList()
        val manager = LinearLayoutManager(binding.homeScreenRV.context)
        manager.reverseLayout = true
        adapter = HomeScreenAdapter(homeScreenUsers, this, currentUser.username)
        binding.homeScreenRV.adapter = adapter
        binding.homeScreenRV.layoutManager = manager

        addListener()
//        loadMessages()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeListener()
    }

    private fun getData(): User {
        val sharedPref = context?.getSharedPreferences("user", Context.MODE_PRIVATE)
        val username = sharedPref?.getString("username", "") ?: ""
        val uid = sharedPref?.getString("uid", "") ?: ""
        val image = sharedPref?.getString("image", "") ?: ""
        return User(uid, username, true, image)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.options_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.logout_menu_item -> {
                FirebaseAuth.getInstance().signOut()
                deleteData()
                Log.d("log out", "logged out successfully")
                startActivity(Intent(context, AuthActivity::class.java))
                activity?.finish()

            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addListener() {
        Log.d("HomeScreen", "listener added")
        FirebaseDatabase.getInstance().getReference("/chats${currentUser.uid}")
            .addChildEventListener(listener)
    }

    private fun removeListener() {
        FirebaseDatabase.getInstance().getReference("/chats${currentUser.uid}")
            .removeEventListener(listener)
    }

    private fun deleteData() {
        val sharedPref = context?.getSharedPreferences("user", Context.MODE_PRIVATE)
        val editor = sharedPref?.edit()
        editor?.apply {
            remove("username")
            remove("uid")
            remove("image")
        }?.apply()

    }

    override fun onClick(position: Int) {
        val user = User(
            homeScreenUsers[position].uid,
            homeScreenUsers[position].username,
            true,
            homeScreenUsers[position].profileImg
        )
        findNavController().navigate(
            HomeScreenFragmentDirections.actionHomeScreenFragmentToChatLogFragment(
                user,
                currentUser
            )
        )
    }

    private val listener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val homeScreenUser = snapshot.getValue(HomeScreenUser::class.java)
            if (homeScreenUser != null) {
                Log.d("HomeScreen", "new homeuser is ${homeScreenUser.username}")
                homeScreenUsers.add(homeScreenUser)
                adapter.notifyItemInserted(homeScreenUsers.size - 1)
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            val homeScreenUser = snapshot.getValue(HomeScreenUser::class.java)
            if (homeScreenUser != null) {
                homeScreenUsers.add(homeScreenUser)
                adapter.notifyDataSetChanged()
            }
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            Log.d("HomeScreen", "child removed")
            val homeScreenUser = snapshot.getValue(HomeScreenUser::class.java)
            if (homeScreenUser != null) {
                homeScreenUsers.remove(homeScreenUser)
                adapter.notifyDataSetChanged()
            }
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d("HomeScreen", "child moved")
        }

        override fun onCancelled(error: DatabaseError) {
            Log.d("HomeScreen", "error")
            Toast.makeText(context, "something went wrong", Toast.LENGTH_SHORT).show()
        }

    }

}