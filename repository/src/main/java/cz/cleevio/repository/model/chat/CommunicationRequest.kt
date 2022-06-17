package cz.cleevio.repository.model.chat

import cz.cleevio.repository.model.offer.Offer

data class CommunicationRequest constructor(
	val message: ChatMessage,
	val offer: Offer,
	//maybe user later?
)
