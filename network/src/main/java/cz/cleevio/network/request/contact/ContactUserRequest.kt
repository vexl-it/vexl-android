package cz.cleevio.network.request.contact

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContactUserRequest constructor(
	val publicKey: String,
	val hash: String
)