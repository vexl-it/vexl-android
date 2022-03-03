package cz.cleevio.network.request.user

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConfirmChallengeRequest constructor(
	val userPublicKey: String,
	val signature: String
)
