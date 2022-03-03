package cz.cleevio.network.request.user

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConfirmPhoneRequest constructor(
	val phoneNumber: String
)
