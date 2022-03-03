package cz.cleevio.network.response.user

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class ConfirmCodeResponse constructor(
	val challenge: String,
	val phoneVerified: Boolean
)
