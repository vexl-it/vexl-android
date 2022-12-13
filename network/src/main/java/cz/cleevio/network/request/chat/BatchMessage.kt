package cz.cleevio.network.request.chat

data class BatchMessage(
	val receiverPublicKey: String,
	val message: String,
	val messageType: String
)