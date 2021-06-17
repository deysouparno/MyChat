package com.example.mychat.adapters


import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.mychat.R
import com.example.mychat.databinding.ChatLogRecieveItemBinding
import com.example.mychat.databinding.ChatLogSendItemBinding
import com.example.mychat.models.ChatMessage

private const val sendViewHolder = 1
private const val receiveViewHolder = 0

class ChatLogAdapter(
    private val fromPerson: String,
    private val senderImg: String,
    private val receiverImg: String
) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(ChatMessageDiffCallback) {

    var flag = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == sendViewHolder) {
            ChatSendViewHolder.from(parent)
        } else {
            ChatReceiveViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (getItemViewType(position) == receiveViewHolder) {
            (holder as ChatReceiveViewHolder).bind(message, receiverImg)
        } else {
            (holder as ChatSendViewHolder).bind(message, senderImg)
        }

        if (flag) {
            Log.d("chatanim", "animation called")
            holder.itemView.startAnimation(
                AnimationUtils.loadAnimation(
                    holder.itemView.context,
                    R.anim.chat_enter
                )
            )
            flag = false
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
        flag = true
        notifyItemInserted(itemCount-1)
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

class ChatReceiveViewHolder(val binding: ChatLogRecieveItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(message: ChatMessage, chatFromImg: String) {
        binding.textFrom.text = message.text
        Glide.with(binding.imageViewFrom.context)
            .load(chatFromImg)
            .circleCrop()
            .placeholder(R.drawable.ic_baseline_account_circle_24)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.imageViewFrom)
//        binding.root.startAnimation(AnimationUtils.loadAnimation(binding.root.context, R.anim.pop_enter))
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

    fun bind(message: ChatMessage, chatToImg: String) {
        binding.textTo.text = message.text
        Glide.with(binding.imageViewTo.context)
            .load(chatToImg)
            .circleCrop()
            .placeholder(R.drawable.ic_baseline_account_circle_24)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.imageViewTo)
//        binding.root.startAnimation(AnimationUtils.loadAnimation(binding.root.context, R.anim.pop_enter))
    }

    companion object {
        fun from(parent: ViewGroup): ChatSendViewHolder {
            val binding =
                ChatLogSendItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ChatSendViewHolder(binding)
        }
    }
}