package com.example.mychat.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.mychat.databinding.ChatLogFromItemBinding
import com.example.mychat.databinding.ChatLogToItemBinding
import com.example.mychat.models.ChatMessage

private const val toViewHolder = 1
private const val fromViewHolder = 0

class ChatLogAdapter(
    private val fromPerson: String,
    private val chatFromImg: String,
    private val chatToImg: String
) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(ChatMessageDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == toViewHolder) {
            ChatToViewHolder.from(parent)
        } else {
            ChatFromViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (getItemViewType(position) == fromViewHolder) {
            (holder as ChatFromViewHolder).bind(message, chatFromImg)
        } else {
            (holder as ChatToViewHolder).bind(message, chatToImg)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).fromPerson == fromPerson) {
            fromViewHolder
        } else {
            toViewHolder
        }
    }


    object ChatMessageDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.key == newItem.key
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }


}

class ChatFromViewHolder(val binding: ChatLogFromItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(message: ChatMessage, chatFromImg: String) {
        binding.textFrom.text = message.text
        Glide.with(binding.imageViewFrom.context)
            .load(chatFromImg)
            .circleCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.imageViewFrom)
    }

    companion object {
        fun from(parent: ViewGroup): ChatFromViewHolder {
            val binding =
                ChatLogFromItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ChatFromViewHolder(binding)
        }
    }
}

class ChatToViewHolder(val binding: ChatLogToItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(message: ChatMessage, chatToImg: String) {
        binding.textTo.text = message.text
        Glide.with(binding.imageViewTo.context)
            .load(chatToImg)
            .circleCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.imageViewTo)
    }

    companion object {
        fun from(parent: ViewGroup): ChatToViewHolder {
            val binding =
                ChatLogToItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ChatToViewHolder(binding)
        }
    }
}