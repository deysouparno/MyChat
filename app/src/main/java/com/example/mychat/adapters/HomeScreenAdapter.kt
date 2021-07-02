package com.example.mychat.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.mychat.databinding.ChatItemBinding
import com.example.mychat.interfaces.ClickListener
import com.example.mychat.models.HomeScreenUser

class HomeScreenAdapter(private var chats: ArrayList<HomeScreenUser>,
                        private val listener: ClickListener,
                        private val username: String) :
    RecyclerView.Adapter<HomeScreenViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeScreenViewHolder {
        return HomeScreenViewHolder.form(parent, listener)
    }

    override fun onBindViewHolder(holder: HomeScreenViewHolder, position: Int) {
        holder.bind(chats[position], username)
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    fun updateList(items: ArrayList<HomeScreenUser>) {
        chats = items
        notifyDataSetChanged()
    }
}

class HomeScreenViewHolder(val binding: ChatItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: HomeScreenUser, username: String) {
        binding.lastMessage.text = item.lastMsg
        binding.personName.text = item.username
        Log.d("chatlog", "home adapter -> ${item.lastMsg}")
        Glide.with(binding.imageView.context)
            .load(item.profileImg)
            .circleCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.imageView)

        binding.imageView2.isVisible = item.sender == username
    }

    companion object {
        fun form(parent: ViewGroup, listener: ClickListener): HomeScreenViewHolder {
            val binding =
                ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            val viewHolder = HomeScreenViewHolder(binding)
            binding.homeContainer.setOnClickListener {
                listener.onClick(viewHolder.adapterPosition)
            }
            binding.imageView.setOnClickListener {
                listener.showProfilePic(viewHolder.adapterPosition)
            }
            return viewHolder
        }
    }


}