package cz.cleevio.network.response.chat

data class MessagesResponse constructor(
	val messages: List<MessageResponse>
)

data class MessageResponse constructor(
	val message: String,
	val senderPublicKey: String,
	// MESSAGE, REQUEST_REVEAL, APPROVE_REVEAL, REQUEST_MESSAGING, APPROVE_MESSAGING, DISAPPROVE_MESSAGING, DELETE_CHAT
	val messageType: String
)
