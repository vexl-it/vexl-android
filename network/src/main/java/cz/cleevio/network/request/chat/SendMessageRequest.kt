package cz.cleevio.network.request.chat

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SendMessageRequest constructor(
	val senderPublicKey: String,
	val receiverPublicKey: String,
	val message: String,
	val messageType: String
)
