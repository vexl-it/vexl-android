package cz.cleevio.network.request.chat

data class SendMessageRequest constructor(
	val senderPublicKey: String,
	val receiverPublicKey: String,
	val message: String,
	val messageType: String
)
