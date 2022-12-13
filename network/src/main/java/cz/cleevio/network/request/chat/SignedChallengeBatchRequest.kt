package cz.cleevio.network.request.chat

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SignedChallengeBatchRequest constructor(
	val publicKey: String,
	val signedChallenge: SignedChallengeRequest
)
