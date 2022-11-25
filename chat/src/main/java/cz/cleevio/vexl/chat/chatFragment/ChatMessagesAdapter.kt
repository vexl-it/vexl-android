package cz.cleevio.vexl.chat.chatFragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cz.cleevio.core.utils.getBitmap
import cz.cleevio.core.utils.setUserAvatar
import cz.cleevio.repository.model.chat.ChatMessage
import cz.cleevio.repository.model.chat.ChatUserIdentity
import cz.cleevio.repository.model.chat.MessageType
import cz.cleevio.vexl.chat.R
import cz.cleevio.vexl.chat.databinding.ItemChatDeleteChatBinding
import cz.cleevio.vexl.chat.databinding.ItemChatMessageBinding
import cz.cleevio.vexl.chat.databinding.ItemChatMessageIdentityRevealBinding
import cz.cleevio.vexl.chat.databinding.ItemChatMessageIdentityRevealRejectedBinding
import cz.cleevio.vexl.lightbase.core.extensions.showToast

class ChatMessagesAdapter(
	val deleteChat: (ChatMessage) -> Unit
) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<ChatMessage>() {
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
			MessageType.DELETE_CHAT -> {
				DeleteChatViewHolder(
					ItemChatDeleteChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
		notifyDataSetChanged()
		// not nice solution, but somehow the combine of messages & user chat identity in fragment didn't work...
	}

	inner class TextViewHolder constructor(
		private val binding: ItemChatMessageBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: ChatMessage) {
			if (item.isMine) {
				binding.sentMessage.text = item.text
				binding.receivedMessage.isVisible = false
				binding.sentMessage.isVisible = true

				binding.sentMessage.setOnLongClickListener {
					copyTextToClipboard(binding.sentMessage)
					true
				}
			} else {
				binding.receivedMessage.text = item.text
				binding.receivedMessage.isVisible = true
				binding.sentMessage.isVisible = false

				binding.receivedMessage.setOnLongClickListener {
					copyTextToClipboard(binding.receivedMessage)
					true
				}
			}
		}

		private fun copyTextToClipboard(textView: TextView) {
			val clipboard: ClipboardManager = textView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
			val clip: ClipData = ClipData.newPlainText("Vexl", textView.text)
			clipboard.setPrimaryClip(clip)

			textView.showToast(textView.context.getString(R.string.chat_text_copied_clipboard))
		}
	}

	inner class AnonViewHolder constructor(
		private val binding: ItemChatMessageIdentityRevealBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: ChatMessage) {

			itemView.context.let { context ->
				when (item.type) {
					MessageType.REQUEST_REVEAL -> {
						setUserAvatar(
							binding.chatContactIcon,
							_chatUserIdentity?.avatarBase64?.getBitmap(),
							_chatUserIdentity?.anonymousAvatarImageIndex,
							itemView.context
						)
						binding.identityRevealHeading.text = context.getString(R.string.chat_message_identity_reveal_header)
						binding.identityRevealDescription.text = context.getString(R.string.chat_message_identity_reveal_subheader)
					}
					MessageType.APPROVE_REVEAL -> {
						setUserAvatar(
							binding.chatContactIcon,
							_chatUserIdentity?.avatarBase64?.getBitmap(),
							_chatUserIdentity?.anonymousAvatarImageIndex,
							itemView.context
						)
						binding.identityRevealHeading.text = context.getString(R.string.chat_message_identity_reveal_approved)
						binding.identityRevealDescription.text = _chatUserIdentity?.name
					}
					else -> Unit
				}
			}

		}
	}

	inner class RejectedIdentityViewHolder constructor(
		private val binding: ItemChatMessageIdentityRevealRejectedBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: ChatMessage) {
			binding.identityRevealDescription.text = if (item.isMine) {
				binding.identityRevealDescription.resources.getText(R.string.chat_message_identity_reveal_reject)
			} else {
				binding.identityRevealDescription.resources.getText(R.string.chat_message_identity_reveal_reject_received)
			}
		}
	}

	inner class DeleteChatViewHolder constructor(
		private val binding: ItemChatDeleteChatBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: ChatMessage) {
			//TODO: when we have design

			binding.chatDeleteMessage.setOnClickListener {
				deleteChat(item)
			}
		}
	}
}
