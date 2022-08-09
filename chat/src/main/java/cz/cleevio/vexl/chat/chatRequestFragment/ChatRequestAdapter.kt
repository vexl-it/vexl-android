package cz.cleevio.vexl.chat.chatRequestFragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cz.cleevio.core.R
import cz.cleevio.core.utils.BuySellColorizer.colorizeTransactionType
import cz.cleevio.core.widget.FriendLevel
import cz.cleevio.repository.model.chat.CommunicationRequest
import cz.cleevio.vexl.chat.databinding.ItemChatRequestBinding

class ChatRequestAdapter : ListAdapter<CommunicationRequest, ChatRequestAdapter.ViewHolder>(
	object : DiffUtil.ItemCallback<CommunicationRequest>() {
		override fun areItemsTheSame(oldItem: CommunicationRequest, newItem: CommunicationRequest): Boolean =
			oldItem.message.uuid == newItem.message.uuid

		override fun areContentsTheSame(oldItem: CommunicationRequest, newItem: CommunicationRequest): Boolean =
			oldItem == newItem
	}) {

	fun getItemAtIndex(index: Int): CommunicationRequest = getItem(index)

	inner class ViewHolder constructor(
		private val binding: ItemChatRequestBinding
	) : RecyclerView.ViewHolder(binding.root) {

		private lateinit var adapter: ChatRequestCommonFriendAdapter

		fun bind(item: CommunicationRequest) {
			if (item.offer?.offerType == "SELL") {
				colorizeTransactionType(
					binding.userName.resources.getString(R.string.marketplace_detail_user_sell, "Unknown friend"),
					"Unknown friend",
					binding.userName,
					R.color.pink_100
				)
			} else {
				colorizeTransactionType(
					binding.userName.resources.getString(R.string.marketplace_detail_user_buy, "Unknown friend"),
					"Unknown friend",
					binding.userName,
					R.color.green_100
				)
			}
			binding.userType.text = if (item.offer?.friendLevel == FriendLevel.FIRST_DEGREE.name) {
				binding.userType.resources.getString(R.string.marketplace_detail_friend_first)
			} else {
				binding.userType.resources.getString(R.string.marketplace_detail_friend_second)
			}
			binding.requestMessage.text = item.message.text
			binding.offerWidget.bind(item.offer!!)
			adapter.submitList(item.offer?.commonFriends.orEmpty().map { it.contact })
		}

		fun initAdapter() {
			adapter = ChatRequestCommonFriendAdapter()
			binding.commonFriendsList.adapter = adapter
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val viewHolder = ViewHolder(
			ItemChatRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		)
		viewHolder.initAdapter()
		return viewHolder
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(getItem(position))
	}
}
