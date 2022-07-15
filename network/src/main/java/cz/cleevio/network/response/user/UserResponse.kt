package cz.cleevio.network.response.user

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserResponse constructor(
	val username: String,
	val avatar: String,
	val publicKey: String
)
