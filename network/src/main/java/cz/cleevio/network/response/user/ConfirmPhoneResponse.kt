package cz.cleevio.network.response.user

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConfirmPhoneResponse constructor(
	val verificationId: Long,
	val expirationAt: String
)
