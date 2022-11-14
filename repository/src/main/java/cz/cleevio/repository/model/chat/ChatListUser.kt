package cz.cleevio.repository.model.chat

import cz.cleevio.repository.model.offer.Offer

data class ChatListUser constructor(
	val type: ChatListUserType = ChatListUserType.MESSAGE,
	//is mandatory for ChatListUserType.MESSAGE, but null for ChatListUserType.FOOTER
	val message: ChatMessage?,
	val user: ChatUserIdentity?,
	val offer: Offer?
)

enum class ChatListUserType {
	MESSAGE, FOOTER
}