package cz.cleevio.network.response.chat

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChallengeCreatedResponse constructor(
	val challenge: String,
	val expiration: String
)
