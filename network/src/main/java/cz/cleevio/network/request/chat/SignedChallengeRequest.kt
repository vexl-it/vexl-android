package cz.cleevio.network.request.chat

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SignedChallengeRequest constructor(
	val challenge: String,
	val signature: String
)
