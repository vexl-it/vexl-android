package cz.cleevio.network.response.user

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UsernameAvailableResponse constructor(
	val available: Boolean
)
