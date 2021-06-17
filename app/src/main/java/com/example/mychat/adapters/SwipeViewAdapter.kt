package com.example.mychat.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mychat.screens.AccountFragment
import com.example.mychat.screens.HomeChatFragment

class SwipeViewAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> AccountFragment()
            else -> HomeChatFragment()
        }
    }
}