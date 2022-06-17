package cz.cleevio.network.response.chat

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InboxResponse constructor(
	val firebaseToken: String
)
