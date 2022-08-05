package cz.cleevio.cache.entity

data class MessageKeyPair constructor(
	val senderPublicKey: String,
	val recipientPublicKey: String
)