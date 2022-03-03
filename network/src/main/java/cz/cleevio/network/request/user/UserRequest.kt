package cz.cleevio.network.request.user

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class UserRequest constructor(
	val username: String,
	val avatar: String
)
