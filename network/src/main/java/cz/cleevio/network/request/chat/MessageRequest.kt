package cz.cleevio.network.request.chat

data class MessageRequest constructor(
	val publicKey: String,
	val signature: String
)
