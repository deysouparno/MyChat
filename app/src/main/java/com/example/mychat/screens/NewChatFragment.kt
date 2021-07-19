package com.example.mychat.screens

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mychat.R
import com.example.mychat.adapters.NewChatAdapter
import com.example.mychat.databinding.FragmentNewChatBinding
import com.example.mychat.getUser
import com.example.mychat.interfaces.ClickListener
import com.example.mychat.models.User
import com.example.mychat.viewmodels.NewChatViewModel
import com.example.mychat.viewmodels.NewChatViewModelFactory
import java.util.*
import kotlin.collections.ArrayList


class NewChatFragment : Fragment(), ClickListener {

//    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var binding: FragmentNewChatBinding
    private lateinit var adapter: NewChatAdapter
    private lateinit var chatFromPerson: User
    private lateinit var viewModel: NewChatViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewChatBinding.inflate(inflater)
        setHasOptionsMenu(true)
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.newChatToolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = ""
        chatFromPerson = getUser(context)
        adapter = NewChatAdapter(this)
        handleSearch()
        viewModel =
            ViewModelProvider(this, NewChatViewModelFactory(chatFromPerson.username)).get(
                NewChatViewModel::class.java
            )
        binding.userRecyclerView.adapter = adapter
//        viewModel.getUsers()
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }

        })

        viewModel.onChange.observe(viewLifecycleOwner, {
            adapter.submitList(viewModel.users)
            adapter.notifyDataSetChanged()
        })

        binding.closeImg.setOnClickListener {
            (context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)

            binding.searchView.startAnimation(
                AnimationUtils.loadAnimation(
                    context,
                    R.anim.search_exit
                )
            )
            adapter.submitList(viewModel.users)
            binding.searchEditText.text.clear()
            binding.searchView.isVisible = false
        }
        binding.backArrow.setOnClickListener {
            findNavController().navigateUp()
        }

//        Log.d("BackStack", "back stack size is ${findNavController().backStack.size}")
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.new_chat_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search_menu_item -> {
                binding.searchView.startAnimation(
                    AnimationUtils.loadAnimation(
                        context,
                        R.anim.search_enter
                    )
                )
                (context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .showSoftInput(binding.searchEditText, 0)
                binding.searchEditText.requestFocus()
                binding.searchView.isVisible = true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onClick(position: Int) {
        findNavController().navigate(
            NewChatFragmentDirections.actionNewChatFragmentToChatLogFragment(viewModel.users[position])
        )

    }

    override fun showProfilePic(position: Int) {
        TODO("Not yet implemented")
    }

    private fun filter(text: String) {
        val filteredList = ArrayList<User>()
        viewModel.users.forEach {
            if (it.username.lowercase(Locale.getDefault())
                    .contains(text.lowercase(Locale.getDefault()))
            ) {
                filteredList.add(it)
            }
        }
        adapter.submitList(filteredList)
    }

    private fun handleSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    filter(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {
                filter(s.toString())
            }

        })
    }

}


