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

class ChatMessagesAdapter : ListAdapter<ChatMessage, ChatMessagesAdapter.ViewHolder>(object : DiffUtil.ItemCallback<ChatMessage>() {
	override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean = oldItem.uuid == newItem.uuid

	override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean = oldItem == newItem
}) {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessagesAdapter.ViewHolder =
		ViewHolder(
			ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		)

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(getItem(position), position)
	}


	inner class ViewHolder constructor(
		private val binding: ItemChatMessageBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: ChatMessage, position: Int) {

			val itemTmp = if (item.type == MessageType.COMMUNICATION_REQUEST_RESPONSE) {
				item.copy(text = "Some super cool text")
			} else {
				item
			}

			if (item.isMine) {
				binding.sentMessage.text = itemTmp.text
				binding.receivedMessage.isVisible = false
				binding.sentMessage.isVisible = true
			} else {
				binding.receivedMessage.text = itemTmp.text
				binding.receivedMessage.isVisible = true
				binding.sentMessage.isVisible = false
			}

		}
	}
}