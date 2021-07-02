package com.example.mychat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.mychat.R
import com.example.mychat.databinding.ChatItemBinding
import com.example.mychat.databinding.SearchItemBinding
import com.example.mychat.interfaces.ClickListener
import com.example.mychat.models.User



class NewChatAdapter(private val listener: ClickListener) :
    ListAdapter<User, NewChatAdapter.UserViewHolder>(UserItemDiffCallback) {

    val selected = HashSet<Int>()

    class UserViewHolder private constructor(val binding: SearchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User, selected : HashSet<Int>) {
            binding.searchPersonName.text = user.username
            Glide.with(binding.searchImageView.context)
                .load(user.profileImg)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .circleCrop()
                .placeholder(R.drawable.ic_baseline_account_circle_24)
                .error(R.drawable.ic_baseline_account_circle_24)
                .into(binding.searchImageView)

            binding.selectedImage.isVisible = selected.contains(adapterPosition)

        }

        companion object {
            fun from(parent: ViewGroup, listener: ClickListener): UserViewHolder {
                val binding =
                    SearchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
        holder.bind(user, selected)
    }

    object UserItemDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem.uid == newItem.uid

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem
    }

}