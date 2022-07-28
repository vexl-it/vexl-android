package cz.cleevio.network.request.contact

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateUserRequest constructor(
	val firebaseToken: String
)
