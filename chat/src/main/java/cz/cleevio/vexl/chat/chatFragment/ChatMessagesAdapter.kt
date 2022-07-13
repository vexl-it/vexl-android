package cz.cleevio.vexl.chat.chatFragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cz.cleevio.repository.model.chat.ChatMessage
import cz.cleevio.repository.model.chat.MessageType
import cz.cleevio.vexl.chat.databinding.ItemChatMessageBinding

class ChatMessagesAdapter : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<ChatMessage>() {
	override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean = oldItem.uuid == newItem.uuid

	override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean = oldItem == newItem
}) {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		val type: MessageType = MessageType.values()[viewType]
		return when (type) {
			MessageType.TEXT -> {
				TextViewHolder(
					ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
				)
			}
			MessageType.ANON_REQUEST -> {
				AnonViewHolder(
					ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
				)
			}
			else -> {
				TextViewHolder(
					ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
				)
			}
		}
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		when (holder) {
			is TextViewHolder ->
				holder.bind(getItem(position))
			is AnonViewHolder ->
				holder.bind(getItem(position))
		}
	}

	override fun getItemViewType(position: Int): Int {
		return getItem(position).type.ordinal
	}

	inner class TextViewHolder constructor(
		private val binding: ItemChatMessageBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: ChatMessage) {

			if (item.isMine) {
				binding.sentMessage.text = item.text
				binding.receivedMessage.isVisible = false
				binding.sentMessage.isVisible = true
			} else {
				binding.receivedMessage.text = item.text
				binding.receivedMessage.isVisible = true
				binding.sentMessage.isVisible = false
			}

		}
	}

	inner class AnonViewHolder constructor(
		private val binding: ItemChatMessageBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: ChatMessage) {

			if (item.isMine) {
				binding.sentMessage.text = item.text
				binding.receivedMessage.isVisible = false
				binding.sentMessage.isVisible = true
			} else {
				binding.receivedMessage.text = item.text
				binding.receivedMessage.isVisible = true
				binding.sentMessage.isVisible = false
			}

		}
	}
}