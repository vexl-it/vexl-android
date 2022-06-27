package cz.cleevio.repository.model.chat

import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.model.user.User

data class ChatListUser constructor(
	val message: ChatMessage,
	//maybe User later?
	val user: User? = null,
	val offer: Offer
)