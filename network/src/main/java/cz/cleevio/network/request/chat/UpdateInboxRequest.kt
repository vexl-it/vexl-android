package cz.cleevio.network.request.chat

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateInboxRequest constructor(
	//public key of user or offer
	val publicKey: String,
	//firebase token
	val token: String,
	val signedChallenge: SignedChallengeRequest
)