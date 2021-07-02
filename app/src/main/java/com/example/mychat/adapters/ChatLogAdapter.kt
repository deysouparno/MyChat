package com.example.mychat.adapters


import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.marginStart
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mychat.R
import com.example.mychat.databinding.ChatLogRecieveItemBinding
import com.example.mychat.databinding.ChatLogSendItemBinding
import com.example.mychat.getCurrentTime
import com.example.mychat.models.ChatMessage

private const val sendViewHolder = 1
private const val receiveViewHolder = 0
private var lastMsg = -1

class ChatLogAdapter(
    private val fromPerson: String,
    private val receiverImg: String
) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(ChatMessageDiffCallback) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == sendViewHolder) {
            ChatSendViewHolder.from(parent)
        } else {
            ChatReceiveViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        lastMsg = if (getItemViewType(position) == receiveViewHolder) {
            (holder as ChatReceiveViewHolder).bind(message)
            receiveViewHolder
        } else {
            (holder as ChatSendViewHolder).bind(message)
            sendViewHolder
        }


    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).fromPerson == fromPerson) {
            sendViewHolder
        } else {
            receiveViewHolder
        }
    }

    fun addItem() {
        notifyItemInserted(itemCount-1)
    }

    fun tailReset() {
        lastMsg = -1
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

class ChatReceiveViewHolder(val binding: ChatLogRecieveItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(message: ChatMessage) {
        binding.timeText.text = getCurrentTime(message.time, 2)
        binding.textFrom.text = message.text
        if (lastMsg == receiveViewHolder) {
            binding.textFrom.background = ContextCompat.getDrawable(binding.textFrom.context, R.drawable.rounded_shape)
        }
        Log.d("chatlog", "in adapter -> ${message.text}")
    }

    companion object {
        fun from(parent: ViewGroup): ChatReceiveViewHolder {
            val binding =
                ChatLogRecieveItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ChatReceiveViewHolder(binding)
        }
    }
}

class ChatSendViewHolder(val binding: ChatLogSendItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(message: ChatMessage) {
        binding.textTo.text = message.text
        binding.timeText.text = getCurrentTime(message.time, 2)
//        Log.d("chatlog", "in adapter -> ${message.text}")
        if (lastMsg == sendViewHolder) {
            binding.textTo.background = ContextCompat.getDrawable(binding.textTo.context, R.drawable.rounded_shape)
        }
    }

    companion object {
        fun from(parent: ViewGroup): ChatSendViewHolder {
            val binding =
                ChatLogSendItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ChatSendViewHolder(binding)
        }
    }

}