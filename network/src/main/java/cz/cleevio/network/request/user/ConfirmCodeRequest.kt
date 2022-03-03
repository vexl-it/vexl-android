package cz.cleevio.network.request.user

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConfirmCodeRequest constructor(
	val id: Long,
	val code: String,
	val userPublicKey: String
)
