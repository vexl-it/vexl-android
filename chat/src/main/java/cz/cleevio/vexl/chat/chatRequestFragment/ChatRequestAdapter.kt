package cz.cleevio.vexl.chat.chatRequestFragment

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cz.cleevio.core.R
import cz.cleevio.core.model.OfferType
import cz.cleevio.core.utils.BuySellColorizer.colorizeTransactionType
import cz.cleevio.core.widget.FriendLevel
import cz.cleevio.core.widget.toFriendLevel
import cz.cleevio.repository.RandomUtils
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
			val offer = item.offer
			val isSell = (offer.offerType == OfferType.SELL.name && !offer.isMine)
				|| (offer.offerType == OfferType.BUY.name && offer.isMine)

			val username = offer.userName ?: RandomUtils.generateName()
			if (isSell) {
				colorizeTransactionType(
					binding.userName.resources.getString(R.string.marketplace_detail_user_sell, username),
					username,
					binding.userName,
					R.color.pink_100
				)
			} else {
				colorizeTransactionType(
					binding.userName.resources.getString(R.string.marketplace_detail_user_buy, username),
					username,
					binding.userName,
					R.color.green_100
				)
			}

			val friendLevels = item.contactLevels.map { it.toFriendLevel() }
			binding.userType.text = when {
				friendLevels.contains(FriendLevel.GROUP) && item.group != null -> {
					binding.userType.resources.getString(R.string.offer_widget_groups, item.group?.name)
				}
				friendLevels.contains(FriendLevel.FIRST_DEGREE) -> {
					binding.userType.resources.getString(R.string.marketplace_detail_friend_first)
				}
				friendLevels.contains(FriendLevel.SECOND_DEGREE) -> {
					binding.userType.resources.getString(R.string.marketplace_detail_friend_second)
				}
				//fallback
				else -> {
					""
				}
			}
			binding.requestMessage.text = item.message.text
			binding.offerWidget.bind(item = item.offer, group = item.group)
			val offerList = item.offer.commonFriends.map { it.contact }
			adapter.submitList(offerList)
			binding.noneCommonFriends.isVisible = offerList.isEmpty()
			binding.commonFriendsList.isVisible = offerList.isNotEmpty()
			binding.arrow.isVisible = offerList.isNotEmpty()
		}

		fun initAdapter() {
			// This listener handles removing touch events from parent view when scrolling with the child
			binding.commonFriendsList.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
				override fun onInterceptTouchEvent(recyclerView: RecyclerView, event: MotionEvent): Boolean {
					when (event.action) {
						MotionEvent.ACTION_MOVE -> recyclerView.parent.requestDisallowInterceptTouchEvent(true)
					}
					return false
				}

				override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
				override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
			})


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
