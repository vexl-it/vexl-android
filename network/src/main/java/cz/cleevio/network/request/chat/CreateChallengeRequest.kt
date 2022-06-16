package cz.cleevio.network.request.chat

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateChallengeRequest constructor(
	val publicKey: String
)
