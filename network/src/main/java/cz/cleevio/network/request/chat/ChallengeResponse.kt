package cz.cleevio.network.request.chat

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChallengeResponse constructor(
	val publicKey: String,
	val challenge: String
)
