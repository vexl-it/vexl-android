package cz.cleevio.network.response.user

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SignatureResponse constructor(
	val hash: String,
	val signature: String,
	val challengeValid: Boolean
)