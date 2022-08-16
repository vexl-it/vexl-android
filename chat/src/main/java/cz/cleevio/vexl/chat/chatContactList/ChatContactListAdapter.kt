package cz.cleevio.vexl.chat.chatContactList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import cz.cleevio.core.utils.BuySellColorizer.colorizeTransactionType
import cz.cleevio.repository.model.chat.ChatListUser
import cz.cleevio.vexl.chat.R
import cz.cleevio.vexl.chat.databinding.ItemChatContactBinding
import java.text.SimpleDateFormat

class ChatContactListAdapter constructor(
	val chatWithUser: (ChatListUser) -> Unit
) : ListAdapter<ChatListUser, ChatContactListAdapter.ViewHolder>(object : DiffUtil.ItemCallback<ChatListUser>() {
	override fun areItemsTheSame(oldItem: ChatListUser, newItem: ChatListUser): Boolean =
		oldItem.message.uuid == newItem.message.uuid

	override fun areContentsTheSame(oldItem: ChatListUser, newItem: ChatListUser): Boolean =
		oldItem == newItem
}) {

	inner class ViewHolder constructor(
		private val binding: ItemChatContactBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: ChatListUser) {
			binding.chatContactIcon.load(item.user?.avatar) {
				crossfade(true)
				fallback(R.drawable.random_avatar_2)
				error(R.drawable.random_avatar_2)
				placeholder(R.drawable.random_avatar_2)
			}

			val isDeanonymized = item.user?.deAnonymized == true

			if (item.offer.offerType == "SELL") {
				colorizeTransactionType(
					binding.chatContactName.resources.getString(
						cz.cleevio.core.R.string.marketplace_detail_user_sell, if (isDeanonymized) item.user?.name else item.user?.anonymousUsername
					),
					if (isDeanonymized) item.user?.name ?: "" else item.user?.anonymousUsername ?: "",
					binding.chatContactName,
					R.color.pink_100
				)
			} else {
				colorizeTransactionType(
					binding.chatContactName.resources.getString(
						cz.cleevio.core.R.string.marketplace_detail_user_buy,  if (isDeanonymized) item.user?.name else item.user?.anonymousUsername
					),
					if (isDeanonymized) item.user?.name ?: "" else item.user?.anonymousUsername ?: "",
					binding.chatContactName,
					R.color.green_100
				)
			}

			binding.chatLastMessage.text = item.message.text
			binding.chatTime.text = SimpleDateFormat.getDateTimeInstance(
				SimpleDateFormat.SHORT,
				SimpleDateFormat.SHORT
			).format(item.message.time)

			binding.container.setOnClickListener {
				chatWithUser(item)
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
		ViewHolder(
			ItemChatContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		)

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(getItem(position))
	}
}
