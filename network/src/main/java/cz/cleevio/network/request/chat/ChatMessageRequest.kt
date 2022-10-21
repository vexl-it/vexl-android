package cz.cleevio.network.request.chat

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatMessageRequest constructor(
	val uuid: String,
	val text: String? = null,
	val image: String? = null,
	val time: Long = System.currentTimeMillis(),
	val deanonymizedUser: ChatUserRequest? = null
)

@JsonClass(generateAdapter = true)
data class ChatUserRequest constructor(
	val name: String?,
	@Deprecated("Avatar url is not used anymore, use imageBase64")
	val image: String? = null,
	val imageBase64: String?
)