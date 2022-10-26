package cz.cleevio.repository.model.chat

import cz.cleevio.repository.model.offer.Offer

data class ChatWithOffer(
	val chat: Chat,
	val offer: Offer?
)
