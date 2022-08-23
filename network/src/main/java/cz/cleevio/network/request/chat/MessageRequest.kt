package cz.cleevio.network.request.chat

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessageRequest constructor(
	val publicKey: String,
	val signedChallenge: SignedChallengeRequest
)
