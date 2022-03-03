package cz.cleevio.network.request.user

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UsernameAvailableRequest constructor(
	val username: String
)
