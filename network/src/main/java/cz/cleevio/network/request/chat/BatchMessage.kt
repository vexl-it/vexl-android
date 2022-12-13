package cz.cleevio.network.request.chat

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BatchMessage(
	val receiverPublicKey: String,
	val message: String,
	val messageType: String
)