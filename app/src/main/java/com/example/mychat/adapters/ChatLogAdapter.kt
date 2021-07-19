package com.example.mychat.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import com.example.mychat.Constants.Companion.CHAT
import com.example.mychat.Constants.Companion.IMG_MSG
import com.example.mychat.Constants.Companion.TEXT_MSG
import com.example.mychat.R
import com.example.mychat.databinding.*
import com.example.mychat.getCurrentTime
import com.example.mychat.models.ChatMessage

private const val sendViewHolder = 1
private const val sendImgViewHolder = 11
private const val receiveViewHolder = 2
private const val receiveImgViewHolder = 22
private const val msgViewHolder = 0
private var lastMsg = -1

class ChatLogAdapter(
    private val fromPerson: String,
) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(ChatMessageDiffCallback) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            sendViewHolder -> ChatSendViewHolder.from(parent)
            sendImgViewHolder -> ImageSendViewHolder.from(parent)
            receiveViewHolder -> ChatReceiveViewHolder.from(parent)
            receiveImgViewHolder -> ImageReceiveViewHolder.from(parent)
            else -> RvMessageViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)

        lastMsg = when (getItemViewType(position)) {
            receiveViewHolder -> {
                (holder as ChatReceiveViewHolder).bind(message)
                receiveViewHolder
            }

            receiveImgViewHolder -> {
                (holder as ImageReceiveViewHolder).bind(message)
                receiveImgViewHolder
            }

            sendViewHolder -> {
                (holder as ChatSendViewHolder).bind(message)
                sendViewHolder
            }

            sendImgViewHolder -> {
                (holder as ImageSendViewHolder).bind(message)
                sendImgViewHolder
            }
            else -> {
                (holder as RvMessageViewHolder).bind(message)
                msgViewHolder
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val msg = getItem(position)
        return when (msg.fromPerson) {
            fromPerson -> {
                if (msg.type == IMG_MSG) sendImgViewHolder
                else sendViewHolder
            }
            "admin" -> msgViewHolder
            else -> {
                if (msg.type == IMG_MSG) receiveImgViewHolder
                else receiveViewHolder
            }
        }
    }

    fun addItem() {
        notifyItemInserted(itemCount - 1)
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
            binding.textFrom.background =
                ContextCompat.getDrawable(binding.textFrom.context, R.drawable.rounded_shape)
        }
    }

    companion object {
        fun from(parent: ViewGroup): ChatReceiveViewHolder {
            val binding =
                ChatLogRecieveItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            return ChatReceiveViewHolder(binding)
        }
    }
}

class ChatSendViewHolder(val binding: ChatLogSendItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(message: ChatMessage) {
        binding.textTo.text = message.text
        binding.timeText.text = getCurrentTime(message.time, 2)
//        Log.d("chatlog", "in adapter -> ${message.text}")
        if (lastMsg == sendViewHolder) {
            binding.textTo.background =
                ContextCompat.getDrawable(binding.textTo.context, R.drawable.rounded_shape)
        }
    }

    companion object {
        fun from(parent: ViewGroup): ChatSendViewHolder {
            val binding =
                ChatLogSendItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            return ChatSendViewHolder(binding)
        }
    }

}

class RvMessageViewHolder(val binding: RvMessageBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(message: ChatMessage) {
        binding.msgText.text = message.text
    }

    companion object {
        fun from(parent: ViewGroup): RvMessageViewHolder {
            val binding =
                RvMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            return RvMessageViewHolder(binding)
        }
    }
}

class ImageSendViewHolder(val binding: SendImgItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(msg : ChatMessage) {
        binding.timeText.text = getCurrentTime(msg.time, 2)
        binding.msg.isVisible = msg.text != ""
        Glide.with(binding.sendImg.context)
            .load(msg.link)
            .placeholder(R.drawable.loading_animation)
            .into(binding.sendImg)
    }

    companion object {
        fun from(parent: ViewGroup): ImageSendViewHolder {
            val binding =
                SendImgItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            return ImageSendViewHolder(binding)
        }
    }

}

class ImageReceiveViewHolder(val binding: ImgReceiveItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(msg : ChatMessage) {
        binding.timeText.text = getCurrentTime(msg.time, 2)
        binding.msg.isVisible = msg.text != ""
        Glide.with(binding.receiveImg.context)
            .load(msg.link)
            .placeholder(R.drawable.loading_animation)
            .into(binding.receiveImg)
    }

    companion object {
        fun from(parent: ViewGroup): ImageReceiveViewHolder {
            val binding =
                ImgReceiveItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            return ImageReceiveViewHolder(binding)
        }
    }

}