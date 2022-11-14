package cz.cleevio.vexl.chat.chatContactList

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cz.cleevio.core.model.OfferType
import cz.cleevio.core.utils.BuySellColorizer.colorizeTransactionType
import cz.cleevio.core.utils.getBitmap
import cz.cleevio.core.utils.setIcons
import cz.cleevio.core.utils.setUserAvatar
import cz.cleevio.repository.model.chat.ChatListUser
import cz.cleevio.repository.model.chat.ChatListUserType
import cz.cleevio.repository.model.chat.MessageType
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.vexl.chat.R
import cz.cleevio.vexl.chat.databinding.ItemChatContactBinding
import java.text.SimpleDateFormat

class ChatContactListAdapter constructor(
	val chatWithUser: (ChatListUser) -> Unit
) : ListAdapter<ChatListUser, ChatContactListAdapter.ViewHolder>(object : DiffUtil.ItemCallback<ChatListUser>() {
	override fun areItemsTheSame(oldItem: ChatListUser, newItem: ChatListUser): Boolean =
		oldItem.message?.uuid == newItem.message?.uuid

	override fun areContentsTheSame(oldItem: ChatListUser, newItem: ChatListUser): Boolean =
		oldItem == newItem
}) {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
		ViewHolder(
			ItemChatContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		)

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	inner class ViewHolder constructor(
		private val binding: ItemChatContactBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: ChatListUser) {
			when (item.type) {
				ChatListUserType.MESSAGE -> {
					setFooterVisibility(false)

					setUserAvatar(
						binding.chatContactIcon,
						item.user?.avatarBase64?.getBitmap(),
						item.user?.anonymousAvatarImageIndex,
						itemView.context
					)

					val isDeanonymized = item.user?.deAnonymized == true

					val username = if (isDeanonymized) item.user?.name ?: "" else item.user?.anonymousUsername ?: ""
					if (item.offer != null) {
						val offer = item.offer as Offer
						val isSell = (offer.offerType == OfferType.SELL.name && !offer.isMine)
							|| (offer.offerType == OfferType.BUY.name && offer.isMine)

						if (isSell) {
							colorizeTransactionType(
								binding.chatContactName.resources.getString(
									cz.cleevio.core.R.string.marketplace_detail_user_sell, if (isDeanonymized) item.user?.name else item.user?.anonymousUsername
								),
								userName = username,
								nameTv = binding.chatContactName,
								R.color.pink_100
							)
						} else {
							colorizeTransactionType(
								binding.chatContactName.resources.getString(
									cz.cleevio.core.R.string.marketplace_detail_user_buy, if (isDeanonymized) item.user?.name else item.user?.anonymousUsername
								),
								userName = username,
								nameTv = binding.chatContactName,
								R.color.green_100
							)
						}
					} else {
						binding.chatContactName.text = username
					}

					item.message?.let {
						val prefix = if (it.isMine)
							binding.chatContactName.resources.getString(R.string.chat_message_prefix)
						else ""
						binding.chatLastMessage.text = when (it.type) {
							MessageType.REQUEST_REVEAL -> itemView.resources.getString(R.string.chat_message_identity_reveal_sent)
							MessageType.APPROVE_REVEAL -> itemView.resources.getString(R.string.chat_message_identity_reveal_approved)
							MessageType.DISAPPROVE_REVEAL -> itemView.resources.getString(R.string.chat_message_identity_reveal_declined)
							else -> item.message?.text?.let { "$prefix $it" } ?: ""
						}

						binding.chatLastMessage.setTextColor(itemView.resources.getColor(getTextAndIconColorIfMessageIsRevealIdentity(it.type), null))
						binding.chatLastMessage.compoundDrawableTintList = ColorStateList.valueOf(itemView.resources.getColor(getTextAndIconColorIfMessageIsRevealIdentity(it.type), null))

						binding.chatLastMessage.setIcons(getStartIconIfMessageIsRevealIdentity(it.type), null, null, null)
					}

					binding.chatTime.text = SimpleDateFormat.getDateTimeInstance(
						SimpleDateFormat.SHORT,
						SimpleDateFormat.SHORT
					).format(item.message?.time)

					binding.container.setOnClickListener {
						chatWithUser(item)
					}
				}
				ChatListUserType.FOOTER -> {
					setFooterVisibility(true)
				}
			}
		}

		private fun getStartIconIfMessageIsRevealIdentity(messageType: MessageType): Int? =
			when (messageType) {
				MessageType.REQUEST_REVEAL -> R.drawable.ic_eye
				MessageType.APPROVE_REVEAL -> R.drawable.ic_eye_open
				MessageType.DISAPPROVE_REVEAL -> R.drawable.ic_prohibit
				else -> null
			}

		private fun getTextAndIconColorIfMessageIsRevealIdentity(messageType: MessageType): Int =
			if (messageType == MessageType.REQUEST_REVEAL || messageType == MessageType.APPROVE_REVEAL || messageType == MessageType.DISAPPROVE_REVEAL) {
				R.color.white
			} else {
				R.color.gray_4
			}

		private fun setFooterVisibility(visible: Boolean) {
			binding.footer.isVisible = visible

			binding.chatContactIcon.isVisible = !visible
			binding.chatTime.isVisible = !visible
			binding.chatContactName.isVisible = !visible
			binding.chatLastMessage.isVisible = !visible
		}
	}
}
