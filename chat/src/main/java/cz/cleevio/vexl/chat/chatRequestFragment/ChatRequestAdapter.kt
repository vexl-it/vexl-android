package cz.cleevio.vexl.chat.chatRequestFragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cz.cleevio.core.R
import cz.cleevio.repository.model.chat.CommunicationRequest
import cz.cleevio.vexl.chat.databinding.ItemChatRequestBinding

class ChatRequestAdapter : ListAdapter<CommunicationRequest, ChatRequestAdapter.ViewHolder>(
	object : DiffUtil.ItemCallback<CommunicationRequest>() {
		override fun areItemsTheSame(oldItem: CommunicationRequest, newItem: CommunicationRequest): Boolean = oldItem.message.uuid == newItem.message.uuid

		override fun areContentsTheSame(oldItem: CommunicationRequest, newItem: CommunicationRequest): Boolean = oldItem == newItem
	}) {

	fun getItemAtIndex(index: Int): CommunicationRequest {
		return getItem(index)
	}

	inner class ViewHolder constructor(
		private val binding: ItemChatRequestBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: CommunicationRequest) {
			binding.userName.text = if (item.offer?.offerType == "SELL") {
				binding.userName.resources.getString(R.string.marketplace_detail_user_sell, "Unknown friend")
			} else {
				binding.userName.resources.getString(R.string.marketplace_detail_user_buy, "Unknown friend")
			}
			binding.userType.text = if (item.offer?.friendLevel == "FIRST") {
				binding.userType.resources.getString(R.string.marketplace_detail_friend_first)
			} else {
				binding.userType.resources.getString(R.string.marketplace_detail_friend_second)
			}
			binding.requestMessage.text = item.message.text
			binding.offerWidget.bind(item.offer!!)
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
		ViewHolder(
			ItemChatRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		)

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(getItem(position))
	}
}