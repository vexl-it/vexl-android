package cz.cleevio.repository.model.chat

import cz.cleevio.repository.model.offer.Offer

data class ChatListUser constructor(
	val message: ChatMessage,
	//maybe User later?
	val user: ChatUserIdentity?,
	val offer: Offer
)