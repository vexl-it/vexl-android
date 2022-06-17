package cz.cleevio.network.response.chat

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessagesResponse constructor(
	val messages: List<MessageResponse>
)

@JsonClass(generateAdapter = true)
data class MessageResponse constructor(
	val message: String,
	val senderPublicKey: String,
	// MESSAGE, REQUEST_REVEAL, APPROVE_REVEAL, REQUEST_MESSAGING, APPROVE_MESSAGING, DISAPPROVE_MESSAGING, DELETE_CHAT
	val messageType: String
)
