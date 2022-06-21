package cz.cleevio.network.request.chat

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatMessageRequest constructor(
	val uuid: String,
	val inboxPublicKey: String,
	val senderPublicKey: String,
	val text: String? = null,
	val image: String? = null,
	val type: String,
	val time: Long = System.currentTimeMillis(),
	val deanonymizedUser: ChatUserRequest? = null
)

@JsonClass(generateAdapter = true)
data class ChatUserRequest constructor(
	val name: String?,
	val image: String?
)