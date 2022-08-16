package cz.cleevio.vexl.chat.chatFragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import cz.cleevio.repository.model.chat.ChatMessage
import cz.cleevio.repository.model.chat.ChatUserIdentity
import cz.cleevio.repository.model.chat.MessageType
import cz.cleevio.vexl.chat.R
import cz.cleevio.vexl.chat.databinding.ItemChatMessageBinding
import cz.cleevio.vexl.chat.databinding.ItemChatMessageIdentityRevealBinding
import cz.cleevio.vexl.chat.databinding.ItemChatMessageIdentityRevealRejectedBinding

class ChatMessagesAdapter : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<ChatMessage>() {
	override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean = oldItem.uuid == newItem.uuid

	override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean = oldItem == newItem
}) {

	private var _chatUserIdentity: ChatUserIdentity? = null

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		val type: MessageType = MessageType.values()[viewType]
		return when (type) {
			MessageType.MESSAGE -> {
				TextViewHolder(
					ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
				)
			}
			MessageType.REQUEST_REVEAL, MessageType.APPROVE_REVEAL -> {
				AnonViewHolder(
					ItemChatMessageIdentityRevealBinding.inflate(LayoutInflater.from(parent.context), parent, false)
				)
			}
			MessageType.DISAPPROVE_REVEAL -> {
				RejectedIdentityViewHolder(
					ItemChatMessageIdentityRevealRejectedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
			is RejectedIdentityViewHolder ->
				holder.bind(getItem(position))
		}
	}

	override fun getItemViewType(position: Int): Int =
		getItem(position).type.ordinal

	fun updateChatUserIdentity(chatUserIdentity: ChatUserIdentity?) {
		_chatUserIdentity = chatUserIdentity
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
		private val binding: ItemChatMessageIdentityRevealBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: ChatMessage) {

			itemView.context.let { context ->
				when (item.type) {
					MessageType.REQUEST_REVEAL -> {
						binding.chatContactIcon.load(_chatUserIdentity?.avatar) {
							crossfade(true)
							fallback(R.drawable.random_avatar_5)
							error(R.drawable.random_avatar_5)
							placeholder(R.drawable.random_avatar_5)
						}
						binding.identityRevealHeading.text = context.getString(R.string.chat_message_identity_reveal_header)
						binding.identityRevealDescription.text = context.getString(R.string.chat_message_identity_reveal_subheader)
					}
					MessageType.APPROVE_REVEAL -> {
						binding.chatContactIcon.load(_chatUserIdentity?.avatar) {
							crossfade(true)
							fallback(R.drawable.random_avatar_6)
							error(R.drawable.random_avatar_6)
							placeholder(R.drawable.random_avatar_6)
						}
						binding.identityRevealHeading.text = context.getString(R.string.chat_message_identity_reveal_approved)
						binding.identityRevealDescription.text = _chatUserIdentity?.name
					}
				}
			}

		}
	}

	inner class RejectedIdentityViewHolder constructor(
		private val binding: ItemChatMessageIdentityRevealRejectedBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: ChatMessage) {
		}
	}
}
